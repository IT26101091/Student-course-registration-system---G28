(() => {
  "use strict";

  const API_URL = "/api/students";
  const form = document.getElementById("self-registration-form");
  const errorBox = document.getElementById("registration-error");
  const successPanel = document.getElementById("registration-success");
  const successSummary = document.getElementById("success-summary");
  const submitButton = document.getElementById("create-profile-button");
  const registrationActions = document.getElementById("registration-actions");
  const successActions = document.getElementById("success-actions");

  async function createStudent(student) {
    const response = await fetch(API_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(student)
    });

    const text = await response.text();
    let body = null;
    if (text) {
      try { body = JSON.parse(text); } catch { body = text; }
    }

    if (!response.ok) {
      const message = body && typeof body === "object" && body.error
        ? body.error
        : response.status === 409
          ? "This student ID is already registered. Check the ID or contact the administrator."
          : `Could not create your profile (${response.status}). Please try again.`;
      throw new Error(message);
    }
    return body;
  }

  function showError(message) {
    errorBox.textContent = message;
    errorBox.hidden = false;
    successPanel.hidden = true;
  }

  function resetForAnother() {
    form.reset();
    errorBox.hidden = true;
    successPanel.hidden = true;
    registrationActions.hidden = false;
    successActions.hidden = true;
    document.getElementById("self-student-id").focus();
  }

  form.addEventListener("submit", async event => {
    event.preventDefault();
    errorBox.hidden = true;
    successPanel.hidden = true;
    if (!form.reportValidity()) return;

    const student = {
      studentId: document.getElementById("self-student-id").value.trim(),
      name: document.getElementById("self-student-name").value.trim(),
      email: document.getElementById("self-student-email").value.trim(),
      degreeProgram: document.getElementById("self-degree-program").value.trim()
    };

    if (Object.values(student).some(value => !value)) {
      showError("Please complete all required fields.");
      return;
    }

    submitButton.disabled = true;
    submitButton.textContent = "Creating profile…";
    try {
      const created = await createStudent(student);
      successSummary.textContent = `${created?.name || student.name} (${created?.studentId || student.studentId}) has been added to CourseHub.`;
      successPanel.hidden = false;
      registrationActions.hidden = true;
      successActions.hidden = false;
      form.querySelectorAll("input").forEach(input => { input.readOnly = true; });
    } catch (error) {
      showError(error.message || "Something went wrong. Please try again.");
    } finally {
      submitButton.disabled = false;
      submitButton.innerHTML = 'Create profile <span aria-hidden="true">→</span>';
    }
  });

  document.getElementById("register-another-button").addEventListener("click", () => {
    form.querySelectorAll("input").forEach(input => { input.readOnly = false; });
    resetForAnother();
  });
})();
