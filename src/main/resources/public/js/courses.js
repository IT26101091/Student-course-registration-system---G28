
(() => {
    "use strict";

    const API_URL = "/api/courses";

    let courses = [];
    let editingCourseCode = null;

    const elements = {
        tableBody: document.getElementById("courses-table-body"),
        search: document.getElementById("course-search"),

        total: document.getElementById("total-courses"),
        credits: document.getElementById("total-credits"),
        displayed: document.getElementById("displayed-count"),
        tableSummary: document.getElementById("table-summary"),

        dialog: document.getElementById("course-dialog"),
        form: document.getElementById("course-form"),

        code: document.getElementById("course-code"),
        title: document.getElementById("course-title"),
        description: document.getElementById("course-description"),
        creditsInput: document.getElementById("course-credits"),
        capacity: document.getElementById("course-capacity"),

        dialogTitle: document.getElementById("dialog-title"),
        formError: document.getElementById("form-error"),
        saveButton: document.getElementById("save-course-button"),

        toast: document.getElementById("toast")
    };

    // Send HTTP requests to the Java backend.
    async function request(url, options = {}) {
        const response = await fetch(url, {
            ...options,
            headers: {
                ...(options.body
                    ? { "Content-Type": "application/json" }
                    : {}),
                ...(options.headers || {})
            }
        });

        if (response.status === 204) {
            return null;
        }

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
            const message =
                body && typeof body === "object" && body.error
                    ? body.error
                    : `Request failed (${response.status})`;

            throw new Error(message);
        }

        return body;
    }

    // Prevent HTML injection when displaying course information.
    function escapeHtml(value) {
        return String(value ?? "").replace(/[&<>"']/g, character => ({
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        })[character]);
    }

    // Display success and error messages.
    let toastTimer;

    function showToast(message, isError = false) {
        elements.toast.textContent = message;
        elements.toast.classList.toggle("error", isError);
        elements.toast.classList.add("show");

        window.clearTimeout(toastTimer);

        toastTimer = window.setTimeout(() => {
            elements.toast.classList.remove("show");
        }, 3500);
    }

    // Search courses.
    function getFilteredCourses() {
        const query = elements.search.value.trim().toLowerCase();

        if (!query) {
            return courses;
        }

        return courses.filter(course =>
            [
                course.courseCode,
                course.title,
                course.description,
                course.credits,
                course.capacity
            ].some(value =>
                String(value ?? "").toLowerCase().includes(query)
            )
        );
    }

    // Display courses in the table.
    function render() {
        const filtered = getFilteredCourses();

        const totalCredits = courses.reduce(
            (sum, course) => sum + Number(course.credits || 0),
            0
        );

        elements.total.textContent = String(courses.length);
        elements.credits.textContent = String(totalCredits);
        elements.displayed.textContent = String(filtered.length);

        elements.tableSummary.textContent =
            `Showing ${filtered.length} of ${courses.length} courses`;

        if (filtered.length === 0) {
            const hasSearch = elements.search.value.trim().length > 0;

            elements.tableBody.innerHTML = `
                <tr>
                    <td colspan="5" class="table-message">
                        ${hasSearch
                ? "No courses match your search."
                : "No courses available yet. Select Add course to create the first course."}
                    </td>
                </tr>
            `;

            return;
        }

        elements.tableBody.innerHTML = filtered.map(course => {
            const code = escapeHtml(course.courseCode);
            const title = escapeHtml(course.title);
            const credits = escapeHtml(course.credits);
            const capacity = escapeHtml(course.capacity);

            return `
                <tr>
                    <td>
                        <span class="id-chip">${code}</span>
                    </td>

                    <td>
                        <div class="student-cell">
                            <span class="student-initials"
                                  aria-hidden="true">C</span>

                            <span class="student-name">
                                <strong>${title}</strong>
                                <span>Course</span>
                            </span>
                        </div>
                    </td>

                    <td>
                        <span class="program-chip">${credits} credits</span>
                    </td>

                    <td>${capacity} students</td>

                    <td class="action-cell">
                        <span class="table-actions">

                            <button class="action-button"
                                    type="button"
                                    data-action="edit"
                                    data-code="${code}"
                                    title="Edit course">
                                ✎
                            </button>

                            <button class="action-button delete"
                                    type="button"
                                    data-action="delete"
                                    data-code="${code}"
                                    title="Delete course">
                                ⌫
                            </button>

                        </span>
                    </td>
                </tr>
            `;
        }).join("");
    }

    // Load courses from the backend.
    async function loadCourses(showErrors = true) {
        elements.tableBody.innerHTML = `
            <tr>
                <td colspan="5" class="table-message">
                    Loading courses...
                </td>
            </tr>
        `;

        try {
            const data = await request(API_URL);

            if (!Array.isArray(data)) {
                throw new Error("Unexpected course data received.");
            }

            courses = data;
            render();

        } catch (error) {
            elements.tableBody.innerHTML = `
                <tr>
                    <td colspan="5" class="table-message">
                        Could not load courses. Check your server and login.
                    </td>
                </tr>
            `;

            elements.total.textContent = "—";
            elements.credits.textContent = "—";
            elements.displayed.textContent = "—";
            elements.tableSummary.textContent = "Unable to load courses";

            if (showErrors) {
                showToast(error.message, true);
            }
        }
    }

    // Open the course form for adding or editing.
    function openDialog(course = null) {
        editingCourseCode = course
            ? String(course.courseCode)
            : null;

        elements.form.reset();

        elements.formError.hidden = true;
        elements.formError.textContent = "";

        if (course) {
            elements.dialogTitle.textContent = "Edit course";
            elements.saveButton.textContent = "Save changes";

            elements.code.value = course.courseCode ?? "";
            elements.code.readOnly = true;

            elements.title.value = course.title ?? "";
            elements.description.value = course.description ?? "";
            elements.creditsInput.value = course.credits ?? "";
            elements.capacity.value = course.capacity ?? "";

        } else {
            elements.dialogTitle.textContent = "Add a course";
            elements.saveButton.textContent = "Save course";

            elements.code.readOnly = false;
        }

        elements.dialog.showModal();
        elements.code.focus();
    }

    function closeDialog() {
        elements.dialog.close();
        editingCourseCode = null;
    }

    function showFormError(message) {
        elements.formError.textContent = message;
        elements.formError.hidden = false;
    }

    // Create a new course or update an existing course.
    async function saveCourse(event) {
        event.preventDefault();

        elements.formError.hidden = true;

        if (!elements.form.reportValidity()) {
            return;
        }

        const course = {
            courseCode: elements.code.value.trim(),
            title: elements.title.value.trim(),
            description: elements.description.value.trim(),
            credits: Number(elements.creditsInput.value),
            capacity: Number(elements.capacity.value)
        };

        if (
            !course.courseCode ||
            !course.title ||
            !Number.isInteger(course.credits) ||
            course.credits <= 0 ||
            !Number.isInteger(course.capacity) ||
            course.capacity <= 0
        ) {
            showFormError("Please enter valid course details.");
            return;
        }

        const isEditing = editingCourseCode !== null;

        const url = isEditing
            ? `${API_URL}/${encodeURIComponent(editingCourseCode)}`
            : API_URL;

        const method = isEditing ? "PUT" : "POST";

        elements.saveButton.disabled = true;
        elements.saveButton.textContent = isEditing
            ? "Saving..."
            : "Creating...";

        try {
            await request(url, {
                method,
                body: JSON.stringify(course)
            });

            closeDialog();

            await loadCourses(false);

            showToast(
                isEditing
                    ? "Course updated successfully."
                    : "Course added successfully."
            );

        } catch (error) {
            showFormError(error.message || "Could not save the course.");

        } finally {
            elements.saveButton.disabled = false;
            elements.saveButton.textContent = isEditing
                ? "Save changes"
                : "Save course";
        }
    }

    // Delete a course.
    async function deleteCourse(courseCode) {
        const course = courses.find(
            item => String(item.courseCode) === String(courseCode)
        );

        if (!course) {
            showToast("Course not found. Please refresh.", true);
            return;
        }

        const confirmed = window.confirm(
            `Delete ${course.title} (${course.courseCode})?`
        );

        if (!confirmed) {
            return;
        }

        try {
            await request(
                `${API_URL}/${encodeURIComponent(course.courseCode)}`,
                { method: "DELETE" }
            );

            await loadCourses(false);

            showToast("Course deleted successfully.");

        } catch (error) {
            showToast(error.message || "Could not delete course.", true);
        }
    }

    // Handle Edit and Delete button clicks.
    elements.tableBody.addEventListener("click", event => {
        const button = event.target.closest("button[data-action]");

        if (!button) {
            return;
        }

        const code = button.dataset.code;

        if (button.dataset.action === "edit") {
            const course = courses.find(
                item => String(item.courseCode) === String(code)
            );

            if (course) {
                openDialog(course);
            }

        } else if (button.dataset.action === "delete") {
            deleteCourse(code);
        }
    });

    // Connect page buttons and form events.
    document.getElementById("add-course-button")
        .addEventListener("click", () => openDialog());

    document.getElementById("close-dialog-button")
        .addEventListener("click", closeDialog);

    document.getElementById("cancel-dialog-button")
        .addEventListener("click", closeDialog);

    document.getElementById("refresh-button")
        .addEventListener("click", () => loadCourses());

    elements.search.addEventListener("input", render);

    elements.form.addEventListener("submit", saveCourse);

    // Load courses when the page opens.
    loadCourses();

})();
