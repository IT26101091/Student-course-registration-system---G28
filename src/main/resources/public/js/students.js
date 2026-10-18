(() => {
  "use strict";

  const API_URL = "/api/students";
  let students = [];
  let editingStudentId = null;
  let toastTimer = null;

  const elements = {
    tableBody: document.getElementById("students-table-body"),
    search: document.getElementById("student-search"),
    total: document.getElementById("total-students"),
    degrees: document.getElementById("degree-count"),
    displayed: document.getElementById("displayed-count"),
    tableSummary: document.getElementById("table-summary"),
    dialog: document.getElementById("student-dialog"),
    form: document.getElementById("student-form"),
    dialogTitle: document.getElementById("dialog-title"),
    dialogEyebrow: document.getElementById("dialog-eyebrow"),
    dialogDescription: document.getElementById("dialog-description"),
    studentId: document.getElementById("student-id"),
    studentIdHelp: document.getElementById("student-id-help"),
    name: document.getElementById("student-name"),
    email: document.getElementById("student-email"),
    degree: document.getElementById("degree-program"),
    formError: document.getElementById("form-error"),
    saveButton: document.getElementById("save-student-button"),
    toast: document.getElementById("toast")
  };

  async function request(url, options = {}) {
    const response = await fetch(url, {
      ...options,
      headers: {
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...(options.headers || {})
      }
    });

    if (response.status === 204) return null;

    const rawBody = await response.text();
    let body = null;
    if (rawBody) {
      try {
        body = JSON.parse(rawBody);
      } catch {
        body = rawBody;
      }
    }

    if (!response.ok) {
      const message = body && typeof body === "object" && body.error
        ? body.error
        : `Request failed (${response.status} ${response.statusText})`;
      throw new Error(message);
    }

    return body;
  }

  function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
      "&": "&amp;",
      "<": "&lt;",
      ">": "&gt;",
      '"': "&quot;",
      "'": "&#39;"
    })[character]);
  }

  function initials(name) {
    const words = String(name || "Student").trim().split(/\s+/).filter(Boolean);
    return words.slice(0, 2).map(word => word[0].toUpperCase()).join("") || "ST";
  }

  function showToast(message, isError = false) {
    elements.toast.textContent = message;
    elements.toast.classList.toggle("error", isError);
    elements.toast.classList.add("show");
    window.clearTimeout(toastTimer);
    toastTimer = window.setTimeout(() => {
      elements.toast.classList.remove("show");
    }, 3500);
  }

  function getFilteredStudents() {
    const query = elements.search.value.trim().toLowerCase();
    if (!query) return students;

    return students.filter(student => [
      student.studentId,
      student.name,
      student.email,
      student.degreeProgram
    ].some(value => String(value ?? "").toLowerCase().includes(query)));
  }

  function render() {
    const filtered = getFilteredStudents();
    const programs = new Set(
      students.map(student => String(student.degreeProgram || "").trim().toLowerCase())
        .filter(Boolean)
    );

    elements.total.textContent = String(students.length);
    elements.degrees.textContent = String(programs.size);
    elements.displayed.textContent = String(filtered.length);
    elements.tableSummary.textContent = `Showing ${filtered.length} of ${students.length} student${students.length === 1 ? "" : "s"}`;

    if (filtered.length === 0) {
      const hasSearch = elements.search.value.trim().length > 0;
      elements.tableBody.innerHTML = `
        <tr><td colspan="5" class="table-message">
          ${hasSearch ? "No students match your search. Try another keyword." : "No student records yet. Select ‘Add student’ to create the first record."}
        </td></tr>`;
      return;
    }

    elements.tableBody.innerHTML = filtered.map(student => {
      const id = escapeHtml(student.studentId);
      const name = escapeHtml(student.name);
      const email = escapeHtml(student.email);
      const degree = escapeHtml(student.degreeProgram);
      const avatar = escapeHtml(initials(student.name));

      return `
        <tr>
          <td>
            <div class="student-cell">
              <span class="student-initials" aria-hidden="true">${avatar}</span>
              <span class="student-name"><strong>${name}</strong><span>Student</span></span>
            </div>
          </td>
          <td><span class="id-chip">${id}</span></td>
          <td><span class="program-chip" title="${degree}">${degree}</span></td>
          <td class="email-cell">${email}</td>
          <td class="action-cell">
            <span class="table-actions">
              <button class="action-button" type="button" data-action="edit" data-id="${id}" aria-label="Edit ${name}" title="Edit student">✎</button>
              <button class="action-button delete" type="button" data-action="delete" data-id="${id}" aria-label="Delete ${name}" title="Delete student">⌫</button>
            </span>
          </td>
        </tr>`;
    }).join("");
  }

  async function loadStudents(showErrors = true) {
    elements.tableBody.innerHTML = '<tr><td colspan="5" class="table-message">Loading student records…</td></tr>';
    try {
      const data = await request(API_URL);
      if (!Array.isArray(data)) {
        throw new Error("The server returned an unexpected student list.");
      }
      students = data;
      render();
    } catch (error) {
      elements.tableBody.innerHTML = '<tr><td colspan="5" class="table-message">Could not load students. Check that the Java server is running, then refresh.</td></tr>';
      elements.total.textContent = "—";
      elements.degrees.textContent = "—";
      elements.displayed.textContent = "—";
      elements.tableSummary.textContent = "Unable to load records";
      if (showErrors) showToast(error.message || "Could not load student records.", true);
    }
  }

  function openDialog(student = null) {
    editingStudentId = student ? String(student.studentId) : null;
    elements.form.reset();
    elements.formError.hidden = true;
    elements.formError.textContent = "";

    if (student) {
      elements.dialogEyebrow.textContent = "UPDATE RECORD";
      elements.dialogTitle.textContent = "Edit student";
      elements.dialogDescription.textContent = "Update the details for this student.";
      elements.saveButton.textContent = "Save changes";
      elements.studentId.value = student.studentId ?? "";
      elements.studentId.readOnly = true;
      elements.studentIdHelp.textContent = "Student IDs cannot be changed after creation.";
      elements.name.value = student.name ?? "";
      elements.email.value = student.email ?? "";
      elements.degree.value = student.degreeProgram ?? "";
    } else {
      elements.dialogEyebrow.textContent = "NEW RECORD";
      elements.dialogTitle.textContent = "Add a student";
      elements.dialogDescription.textContent = "Enter the student's details below.";
      elements.saveButton.textContent = "Save student";
      elements.studentId.readOnly = false;
      elements.studentIdHelp.textContent = "Use a unique ID for each student.";
    }

    elements.dialog.showModal();
    window.setTimeout(() => (student ? elements.name : elements.studentId).focus(), 0);
  }

  function closeDialog() {
    elements.dialog.close();
    editingStudentId = null;
  }

  function showFormError(message) {
    elements.formError.textContent = message;
    elements.formError.hidden = false;
  }

  async function saveStudent(event) {
    event.preventDefault();
    elements.formError.hidden = true;

    if (!elements.form.reportValidity()) return;

    const student = {
      studentId: elements.studentId.value.trim(),
      name: elements.name.value.trim(),
      email: elements.email.value.trim(),
      degreeProgram: elements.degree.value.trim()
    };

    if (!student.studentId || !student.name || !student.email || !student.degreeProgram) {
      showFormError("Please fill in all required fields.");
      return;
    }

    const isEditing = editingStudentId !== null;
    const url = isEditing ? `${API_URL}/${encodeURIComponent(editingStudentId)}` : API_URL;
    const method = isEditing ? "PUT" : "POST";

    elements.saveButton.disabled = true;
    elements.saveButton.textContent = isEditing ? "Saving…" : "Creating…";

    try {
      await request(url, {
        method,
        body: JSON.stringify(student)
      });
      closeDialog();
      await loadStudents(false);
      showToast(isEditing ? "Student details updated successfully." : "Student created successfully.");
    } catch (error) {
      showFormError(error.message || "Could not save this student.");
    } finally {
      elements.saveButton.disabled = false;
      elements.saveButton.textContent = isEditing ? "Save changes" : "Save student";
    }
  }

  async function deleteStudent(studentId) {
    const student = students.find(item => String(item.studentId) === String(studentId));
    if (!student) {
      showToast("That student record could not be found. Refresh and try again.", true);
      return;
    }

    const confirmed = window.confirm(`Delete ${student.name} (${student.studentId})? This cannot be undone.`);
    if (!confirmed) return;

    try {
      await request(`${API_URL}/${encodeURIComponent(student.studentId)}`, { method: "DELETE" });
      await loadStudents(false);
      showToast("Student deleted successfully.");
    } catch (error) {
      showToast(error.message || "Could not delete this student.", true);
    }
  }

  document.getElementById("add-student-button").addEventListener("click", () => openDialog());
  document.getElementById("close-dialog-button").addEventListener("click", closeDialog);
  document.getElementById("cancel-dialog-button").addEventListener("click", closeDialog);
  document.getElementById("refresh-button").addEventListener("click", () => loadStudents());
  elements.search.addEventListener("input", render);
  elements.form.addEventListener("submit", saveStudent);

  elements.tableBody.addEventListener("click", event => {
    const button = event.target.closest("button[data-action]");
    if (!button) return;
    const id = button.dataset.id;
    if (button.dataset.action === "edit") {
      const student = students.find(item => String(item.studentId) === String(id));
      if (student) openDialog(student);
    } else if (button.dataset.action === "delete") {
      deleteStudent(id);
    }
  });

  // Small keyboard shortcut: Ctrl/Cmd+K focuses the search field.
  document.addEventListener("keydown", event => {
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === "k") {
      event.preventDefault();
      elements.search.focus();
    }
  });

  loadStudents();

  // Refresh the admin list quietly so student profiles created from the
  // Student Portal appear here without requiring a manual page refresh.
  async function refreshStudentsQuietly() {
    try {
      const data = await request(API_URL);
      if (Array.isArray(data)) {
        students = data;
        render();
      }
    } catch {
      // Keep the last successful view during a temporary connection issue.
    }
  }

  window.setInterval(refreshStudentsQuietly, 10000);
  window.addEventListener("focus", refreshStudentsQuietly);
})();
