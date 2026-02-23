const API_BASE = "/api/v1/notifications";

const metricsGrid = document.getElementById("metrics-grid");
const apiStatus = document.getElementById("api-status");
const createForm = document.getElementById("create-request-form");
const createOutput = document.getElementById("create-response");
const requestsTable = document.getElementById("requests-table");
const tasksTable = document.getElementById("tasks-table");
const dlqTable = document.getElementById("dlq-table");

function formatDate(value) {
  if (!value) {
    return "-";
  }
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
}

function setApiStatus(ok, text) {
  apiStatus.textContent = text;
  apiStatus.style.color = ok ? "#1f7a3f" : "#a32929";
  apiStatus.style.borderColor = ok ? "#96cda7" : "#e0a0a0";
}

function pretty(value) {
  return JSON.stringify(value, null, 2);
}

function badge(status) {
  return `<span class="badge ${status || ""}">${status || "-"}</span>`;
}

function toQuery(params) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined && String(value).trim() !== "") {
      search.set(key, value);
    }
  });
  return search.toString();
}

async function apiFetch(path, options = {}) {
  const response = await fetch(path, {
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options,
  });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok || payload.success === false) {
    const message = payload.message || `HTTP ${response.status}`;
    throw new Error(message);
  }
  return payload;
}

function renderMetrics(data) {
  const cards = [
    ["Pending", data.pendingTasks],
    ["Sending", data.sendingTasks],
    ["Sent", data.sentTasks],
    ["Failed", data.failedTasks],
    ["DLQ", data.dlqTasks],
    ["Queue Lag", data.requestQueuedLag],
    ["Malformed Lag", data.malformedQueuedLag],
    ["Success Rate", `${(data.successRate || 0).toFixed(2)}%`],
    ["Avg Latency", `${(data.averageLatencyMs || 0).toFixed(2)} ms`],
  ];

  metricsGrid.innerHTML = cards
    .map(([name, value]) => `<div class="metric-card"><p>${name}</p><strong>${value ?? 0}</strong></div>`)
    .join("");
}

function renderRequests(items) {
  requestsTable.innerHTML = (items || [])
    .map(
      (item) => `
      <tr>
        <td>${item.requestId}</td>
        <td>${item.requestKey}</td>
        <td>${badge(item.status)}</td>
        <td>${item.priority || "-"}</td>
        <td>${formatDate(item.requestedAt)}</td>
      </tr>`
    )
    .join("");
}

function renderTasks(items) {
  tasksTable.innerHTML = (items || [])
    .map(
      (item) => `
      <tr>
        <td>${item.taskId}</td>
        <td>${item.requestId}</td>
        <td>${badge(item.status)}</td>
        <td>${item.channel || "-"}</td>
        <td>${item.retryCount}/${item.maxRetry}</td>
        <td><button class="btn" data-retry-id="${item.taskId}">Retry</button></td>
      </tr>`
    )
    .join("");
}

function renderDlq(items) {
  dlqTable.innerHTML = (items || [])
    .map(
      (item) => `
      <tr>
        <td>${item.taskId}</td>
        <td>${item.requestId}</td>
        <td>${badge(item.status)}</td>
        <td>${item.retryCount}/${item.maxRetry}</td>
        <td>${item.lastResultCode || "-"}</td>
        <td><button class="btn" data-replay-id="${item.taskId}">Replay</button></td>
      </tr>`
    )
    .join("");
}

async function loadMetrics() {
  const payload = await apiFetch(`${API_BASE}/metrics`);
  renderMetrics(payload.data || {});
}

async function listRequests() {
  const payload = await apiFetch(`${API_BASE}/requests?page=0&size=20`);
  renderRequests(payload.data?.items || []);
}

async function findRequestByKey() {
  const key = document.getElementById("request-key-input").value.trim();
  if (!key) {
    throw new Error("Request Key를 입력하세요.");
  }
  const payload = await apiFetch(`${API_BASE}/requests/by-key?${toQuery({ requestKey: key })}`);
  renderRequests(payload.data ? [payload.data] : []);
  document.getElementById("task-request-id").value = payload.data?.requestId || "";
}

async function listTasks() {
  const requestId = document.getElementById("task-request-id").value.trim();
  const status = document.getElementById("task-status").value;
  const query = toQuery({ requestId, status, page: 0, size: 20 });
  const payload = await apiFetch(`${API_BASE}/tasks?${query}`);
  renderTasks(payload.data?.items || []);
}

async function listDlq() {
  const payload = await apiFetch(`${API_BASE}/dlq?page=0&size=20`);
  renderDlq(payload.data?.items || []);
}

async function retryTask(taskId) {
  const payload = await apiFetch(`${API_BASE}/tasks/${taskId}/retry`, { method: "POST" });
  createOutput.textContent = pretty({ action: "retry", result: payload.data });
  await Promise.all([loadMetrics(), listTasks(), listDlq()]);
}

async function replayDlq(taskId) {
  const payload = await apiFetch(`${API_BASE}/dlq/${taskId}/replay`, { method: "POST" });
  createOutput.textContent = pretty({ action: "replay", result: payload.data });
  await Promise.all([loadMetrics(), listTasks(), listDlq()]);
}

createForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const formData = new FormData(createForm);

  try {
    const variables = JSON.parse(formData.get("variables"));
    const receiverIds = String(formData.get("receiverIds"))
      .split(",")
      .map((part) => part.trim())
      .filter(Boolean)
      .map((part) => Number(part));

    if (!receiverIds.length || receiverIds.some((value) => Number.isNaN(value))) {
      throw new Error("Receiver IDs는 숫자 목록이어야 합니다.");
    }

    const requestBody = {
      requestKey: formData.get("requestKey"),
      templateCode: formData.get("templateCode"),
      receiverIds,
      variables,
      priority: formData.get("priority"),
    };

    const payload = await apiFetch(`${API_BASE}/requests`, {
      method: "POST",
      body: JSON.stringify(requestBody),
    });

    createOutput.textContent = pretty(payload);
    document.getElementById("request-key-input").value = requestBody.requestKey;
    document.getElementById("task-request-id").value = payload.data?.requestId || "";

    await Promise.all([loadMetrics(), listRequests(), listTasks()]);
    setApiStatus(true, "API 연결 정상");
  } catch (error) {
    createOutput.textContent = String(error.message || error);
    setApiStatus(false, `오류: ${error.message || error}`);
  }
});

document.getElementById("find-request").addEventListener("click", async () => {
  try {
    await findRequestByKey();
    setApiStatus(true, "API 연결 정상");
  } catch (error) {
    setApiStatus(false, `오류: ${error.message || error}`);
  }
});

document.getElementById("list-requests").addEventListener("click", async () => {
  try {
    await listRequests();
    setApiStatus(true, "API 연결 정상");
  } catch (error) {
    setApiStatus(false, `오류: ${error.message || error}`);
  }
});

document.getElementById("list-tasks").addEventListener("click", async () => {
  try {
    await listTasks();
    setApiStatus(true, "API 연결 정상");
  } catch (error) {
    setApiStatus(false, `오류: ${error.message || error}`);
  }
});

document.getElementById("list-dlq").addEventListener("click", async () => {
  try {
    await listDlq();
    setApiStatus(true, "API 연결 정상");
  } catch (error) {
    setApiStatus(false, `오류: ${error.message || error}`);
  }
});

document.getElementById("refresh-all").addEventListener("click", async () => {
  try {
    await Promise.all([loadMetrics(), listRequests(), listTasks(), listDlq()]);
    setApiStatus(true, "전체 새로고침 완료");
  } catch (error) {
    setApiStatus(false, `오류: ${error.message || error}`);
  }
});

tasksTable.addEventListener("click", async (event) => {
  const button = event.target.closest("button[data-retry-id]");
  if (!button) {
    return;
  }
  try {
    await retryTask(button.dataset.retryId);
    setApiStatus(true, `Task ${button.dataset.retryId} retry 완료`);
  } catch (error) {
    setApiStatus(false, `오류: ${error.message || error}`);
  }
});

dlqTable.addEventListener("click", async (event) => {
  const button = event.target.closest("button[data-replay-id]");
  if (!button) {
    return;
  }
  try {
    await replayDlq(button.dataset.replayId);
    setApiStatus(true, `DLQ ${button.dataset.replayId} replay 완료`);
  } catch (error) {
    setApiStatus(false, `오류: ${error.message || error}`);
  }
});

(async function boot() {
  try {
    await Promise.all([loadMetrics(), listRequests(), listTasks(), listDlq()]);
    setApiStatus(true, "API 연결 정상");
  } catch (error) {
    setApiStatus(false, `오류: ${error.message || error}`);
  }
})();
