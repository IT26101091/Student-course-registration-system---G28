(() => {
  "use strict";
  const $ = id => document.getElementById(id);
  const select = $("course-select"), body = $("queue-table-body");
  const message = $("queue-message"), count = $("queue-count");
  async function api(url) {
    const response = await fetch(url, { credentials: "same-origin" });
    const raw = response.status === 204 ? "" : await response.text();
    let data = null;
    if (raw) { try { data = JSON.parse(raw); } catch { data = { error: raw }; } }
    if (!response.ok) {
      if (response.status === 401) window.location.replace("/");
      throw new Error(data?.error || `Request failed (${response.status})`);
    }
    return data;
  }
  function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, c => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));
  }
  function showMessage(text, error=false) { message.textContent=text; message.classList.toggle("error",error); }
  async function loadCourses() {
    const account = await api("/api/auth/me");
    if (account.role !== "ADMIN") { window.location.replace(account.role === "STUDENT" ? "/student-dashboard.html" : "/"); return; }
    const courses = await api("/api/courses");
    select.innerHTML = '<option value="">Choose a course...</option>';
    (Array.isArray(courses) ? courses : []).forEach(course => {
      const option=document.createElement("option");
      option.value=course.courseCode;
      option.textContent=`${course.courseCode} - ${course.title || course.courseName || "Course"}`;
      select.appendChild(option);
    });
  }
  async function loadQueue() {
    const code=select.value;
    if (!code) { showMessage("Please select a course.",true); return; }
    body.innerHTML='<tr><td colspan="4" class="table-message">Loading waiting list...</td></tr>';
    try {
      const entries=await api(`/api/queue/course/${encodeURIComponent(code)}`);
      const rows=Array.isArray(entries)?entries:[];
      body.innerHTML=rows.length ? rows.map(entry=>`<tr>
        <td><span class="queue-position">${escapeHtml(entry.position)}</span></td>
        <td>${escapeHtml(entry.studentId)}</td>
        <td>${escapeHtml(formatDate(entry.queueDateTime))}</td>
        <td><span class="queue-status">${escapeHtml(entry.status)}</span></td>
      </tr>`).join("") : '<tr><td colspan="4" class="table-message">No students are waiting for this course.</td></tr>';
      count.textContent=`${rows.length} student${rows.length===1?"":"s"} waiting`;
      showMessage(`Waiting list for ${code}`);
    } catch(error) { body.innerHTML=`<tr><td colspan="4" class="table-message">${escapeHtml(error.message)}</td></tr>`; count.textContent="Could not load"; showMessage(error.message,true); }
  }
  function formatDate(value) {
    if (Array.isArray(value)) { const [y,m,d,h=0,min=0,s=0,ns=0]=value; return new Date(y,m-1,d,h,min,s,Math.floor(ns/1000000)).toLocaleString(); }
    const date=new Date(value); return Number.isNaN(date.getTime())?"—":date.toLocaleString();
  }
  $("load-queue").addEventListener("click",loadQueue);
  $("refresh-queue").addEventListener("click",loadQueue);
  select.addEventListener("change",()=>{ if(select.value) loadQueue(); });
  loadCourses().catch(error=>showMessage(error.message,true));
})();
