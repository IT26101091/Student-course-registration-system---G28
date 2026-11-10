(() => {
  "use strict";
  const $ = id => document.getElementById(id);
  const select = $("queue-course-select");
  const message = $("queue-student-message");
  if (!select) return;

  let studentId = null;
  let monitoredCourses = [];
  const notifiedPromotions = new Set();
  let hideMessageTimer = null;

  async function api(url, options = {}) {
    const response = await fetch(url, {
      credentials: "same-origin",
      ...options,
      headers: {
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...(options.headers || {})
      }
    });
    const raw = response.status === 204 ? "" : await response.text();
    let data = null;
    if (raw) {
      try { data = JSON.parse(raw); } catch { data = { error: raw }; }
    }
    if (!response.ok) {
      if (response.status === 401) window.location.replace("/");
      throw new Error(data?.error || `Request failed (${response.status})`);
    }
    return data;
  }

  function show(text, success = false, autoHideMs = 6000) {
    if (hideMessageTimer) clearTimeout(hideMessageTimer);
    message.textContent = text;
    message.hidden = false;
    message.style.opacity = "1";
    message.style.transition = "opacity 0.4s ease";
    message.classList.toggle("success", success);

    if (autoHideMs > 0) {
      hideMessageTimer = setTimeout(() => {
        message.style.opacity = "0";
        setTimeout(() => {
          message.hidden = true;
          message.style.opacity = "1";
        }, 400);
      }, autoHideMs);
    }
  }

  function getPromotionStorageKey(courseCode) {
    return `ch_notified_promo_${studentId || "student"}_${courseCode}`;
  }

  function isPromotionAlreadyNotified(courseCode) {
    if (notifiedPromotions.has(courseCode)) return true;
    try {
      return localStorage.getItem(getPromotionStorageKey(courseCode)) === "true";
    } catch {
      return false;
    }
  }

  function markPromotionAsNotified(courseCode) {
    notifiedPromotions.add(courseCode);
    try {
      localStorage.setItem(getPromotionStorageKey(courseCode), "true");
    } catch {}
  }

  async function initializeQueue() {
    const account = await api("/api/auth/me");
    if (account.role !== "STUDENT") return;
    studentId = account.studentId;
    const courses = await api("/api/courses");

    monitoredCourses = (Array.isArray(courses) ? courses : [])
        .map(course => course.courseCode)
        .filter(Boolean);

    select.innerHTML = '<option value="">Choose a course...</option>';
    (Array.isArray(courses) ? courses : []).forEach(course => {
      const option = document.createElement("option");
      option.value = course.courseCode;
      option.textContent = `${course.courseCode} - ${course.title || course.courseName || "Course"}`;
      select.appendChild(option);
    });
  }

  function selectedCourse() {
    if (!select.value) throw new Error("Please select a course.");
    return select.value;
  }

  async function joinQueue() {
    try {
      const courseCode = selectedCourse();
      const entry = await api("/api/queue", { method: "POST", body: JSON.stringify({ courseCode }) });
      show(`You joined the waiting queue for ${courseCode}. Your position is ${entry.position}.`, true, 6000);
    } catch (error) {
      show(error.message, false, 6000);
    }
  }

  async function checkPosition() {
    try {
      const courseCode = selectedCourse();
      const result = await api(`/api/queue/my-position/${encodeURIComponent(courseCode)}`);
      show(`Your current position for ${courseCode} is ${result.position}.`, true, 6000);
    } catch (error) {
      show(error.message, false, 6000);
    }
  }

  async function cancelRequest() {
    try {
      const courseCode = selectedCourse();
      await api(`/api/queue/${encodeURIComponent(courseCode)}`, { method: "DELETE" });
      show(`Your waiting request for ${courseCode} was cancelled.`, true, 6000);
    } catch (error) {
      show(error.message, false, 6000);
    }
  }

  async function monitorQueueStatuses() {
    for (const courseCode of monitoredCourses) {
      try {
        const result = await api(
            `/api/queue/my-position/${encodeURIComponent(courseCode)}`
        );

        if (
            result.status === "PROMOTED" &&
            !isPromotionAlreadyNotified(courseCode)
        ) {
          markPromotionAsNotified(courseCode);

          if (typeof window.loadMyRegistrations === "function") {
            await window.loadMyRegistrations();
          }

          // Show only once, and disappear after 6 seconds
          show(
              `Good news! You have been automatically enrolled in ${courseCode}.`,
              true,
              6000
          );
        }

      } catch (error) {
        // Ignore courses where the student has no queue record.
        if (!error.message.includes("no waiting-list record")) {
          console.error(error);
        }
      }
    }
  }

  $("join-queue-button").addEventListener("click", joinQueue);
  $("check-queue-button").addEventListener("click", checkPosition);
  $("cancel-queue-button").addEventListener("click", cancelRequest);

  initializeQueue()
      .then(() => {
        monitorQueueStatuses();
        setInterval(monitorQueueStatuses, 5000);
      })
      .catch(error => show(error.message, false, 6000));
})();
