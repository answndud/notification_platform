package com.example.notification.domain.metrics.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KafkaLagService {

    private static final Logger log = LoggerFactory.getLogger(KafkaLagService.class);

    private final String bootstrapServers;
    private final String requestQueuedTopic;
    private final String workerConsumerGroupId;

    public KafkaLagService(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${notification.kafka.topics.request-queued}") String requestQueuedTopic,
            @Value("${notification.kafka.monitor.worker-group-id:notification-worker}") String workerConsumerGroupId
    ) {
        this.bootstrapServers = bootstrapServers;
        this.requestQueuedTopic = requestQueuedTopic;
        this.workerConsumerGroupId = workerConsumerGroupId;
    }

    public long getRequestQueuedLag() {
        Map<String, Object> props = Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        try (AdminClient adminClient = AdminClient.create(props)) {
            long now = System.currentTimeMillis();

            var topicDescription = adminClient.describeTopics(List.of(requestQueuedTopic))
                    .allTopicNames()
                    .get(5, TimeUnit.SECONDS)
                    .get(requestQueuedTopic);

            Map<TopicPartition, Long> committedOffsets = new HashMap<>();
            adminClient.listConsumerGroupOffsets(workerConsumerGroupId)
                    .partitionsToOffsetAndMetadata()
                    .get(5, TimeUnit.SECONDS)
                    .forEach((topicPartition, metadata) -> {
                        if (topicPartition.topic().equals(requestQueuedTopic)) {
                            committedOffsets.put(topicPartition, metadata.offset());
                        }
                    });

            Map<TopicPartition, OffsetSpec> endOffsetQuery = new HashMap<>();
            topicDescription.partitions().forEach(partitionInfo ->
                    endOffsetQuery.put(new TopicPartition(requestQueuedTopic, partitionInfo.partition()), OffsetSpec.latest()));

            long lag = 0L;
            var endOffsets = adminClient.listOffsets(endOffsetQuery)
                    .all()
                    .get(5, TimeUnit.SECONDS);
            for (Map.Entry<TopicPartition, org.apache.kafka.clients.admin.ListOffsetsResult.ListOffsetsResultInfo> entry
                    : endOffsets.entrySet()) {
                long committed = committedOffsets.getOrDefault(entry.getKey(), 0L);
                long end = entry.getValue().offset();
                lag += Math.max(0L, end - committed);
            }

            log.debug(
                    "[METRICS] kafka lag calculated. topic={}, groupId={}, lag={}, elapsedMs={}",
                    requestQueuedTopic,
                    workerConsumerGroupId,
                    lag,
                    System.currentTimeMillis() - now
            );
            return lag;
        } catch (Exception ex) {
            log.warn(
                    "[METRICS] kafka lag unavailable. topic={}, groupId={}, reason={}",
                    requestQueuedTopic,
                    workerConsumerGroupId,
                    ex.getMessage()
            );
            return -1L;
        }
    }
}
