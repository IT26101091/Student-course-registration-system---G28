"use strict";

const loginPanel = document.getElementById("login-panel");
const passwordPanel = document.getElementById("password-panel");

const loginForm = document.getElementById("login-form");
const passwordForm = document.getElementById("password-form");

const message = document.getElementById("message");

function showMessage(text) {
  message.textContent = text;
  message.hidden = false;
}

function clearMessage() {
  message.textContent = "";
  message.hidden = true;
}

function showPasswordChange() {
  loginPanel.hidden = true;
  passwordPanel.hidden = false;
  clearMessage();
}

function openDashboard(account) {
  if (account.mustChangePassword) {
    showPasswordChange();
    return;
  }

  if (account.role === "ADMIN") {
    window.location.replace("/students.html");
  } else if (account.role === "STUDENT") {
    window.location.replace("/student-dashboard.html");
  } else {
    showMessage("Your account has an unrecognized role.");
  }
}

async function requestAccount() {
  const response = await fetch("/api/auth/me");

  if (!response.ok) {
    return null;
  }

  return await response.json();
}

// Sign in.
loginForm.addEventListener("submit", async function (event) {
  event.preventDefault();
  clearMessage();

  if (!loginForm.reportValidity()) {
    return;
  }

  const button = document.getElementById("login-button");
  button.disabled = true;
  button.textContent = "Signing in...";

  try {
    const response = await fetch("/api/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        username: document.getElementById("username").value.trim(),
        password: document.getElementById("password").value
      })
    });

    const body = await response.json().catch(() => ({}));

    if (!response.ok) {
      throw new Error(body.error || "Login failed.");
    }

    openDashboard(body);

  } catch (error) {
    showMessage(error.message || "Unable to sign in.");
  } finally {
    button.disabled = false;
    button.textContent = "Sign in";
  }
});

// Change the temporary password.
passwordForm.addEventListener("submit", async function (event) {
  event.preventDefault();
  clearMessage();

  if (!passwordForm.reportValidity()) {
    return;
  }

  const currentPassword =
      document.getElementById("current-password").value;

  const newPassword =
      document.getElementById("new-password").value;

  const confirmPassword =
      document.getElementById("confirm-password").value;

  if (newPassword !== confirmPassword) {
    showMessage("The new passwords do not match.");
    return;
  }

  const button = document.getElementById("password-button");
  button.disabled = true;
  button.textContent = "Updating password...";

  try {
    const response = await fetch("/api/auth/change-password", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        currentPassword: currentPassword,
        newPassword: newPassword
      })
    });

    const body = await response.json().catch(() => ({}));

    if (!response.ok) {
      throw new Error(body.error || "Password change failed.");
    }

    const account = await requestAccount();

    if (!account) {
      throw new Error(
          "Password changed, but the session could not be verified. Please sign in again."
      );
    }

    openDashboard(account);

  } catch (error) {
    showMessage(error.message || "Unable to change password.");
  } finally {
    button.disabled = false;
    button.textContent = "Change password";
  }
});

// Restore an existing session if the user opens the login page again.
(async function restoreSession() {
  try {
    const account = await requestAccount();

    if (account) {
      openDashboard(account);
    }
  } catch (error) {
    console.error("Could not check existing session.", error);
  }
})();