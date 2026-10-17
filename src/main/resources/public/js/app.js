async function checkServer() {
  const label = document.getElementById("server-status");
  const dot = document.getElementById("status-dot");

  try {
    const response = await fetch("/api/health");
    if (!response.ok) throw new Error("Health check returned an error");
    const data = await response.json();
    label.textContent = `Java server connected · ${data.status}`;
    dot.className = "status-dot online";
  } catch (error) {
    label.textContent = "Java server connection unavailable";
    dot.className = "status-dot offline";
    console.error(error);
  }
}

checkServer();
