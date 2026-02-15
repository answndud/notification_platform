package com.example.notification.domain.receiver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "receiver")
public class Receiver {

    @Id
    private Long id;

    @Column(name = "receiver_type", nullable = false, length = 20)
    private String receiverType;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "phone", length = 40)
    private String phone;

    @Column(name = "push_token", length = 200)
    private String pushToken;

    @Column(name = "timezone", nullable = false, length = 40)
    private String timezone;

    @Column(name = "active", nullable = false)
    private boolean active;
}
