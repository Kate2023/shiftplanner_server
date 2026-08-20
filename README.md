# Shift Planner System

Welcome to the README for the Shift Planner System.

The Shift Planner System is a comprehensive solution designed to streamline the scheduling and management of employee shifts. This system provides an intuitive interface for both administrators and employees, allowing for efficient shift planning, tracking, and communication.

## 📋 System Requirements
*   **Java Development Kit (JDK)**: 21 (Specifically tested on 21.0.11)
*   **Gradle Wrapper**: 9.6.1
*   **Docker Desktop**: (Ensure the engine/daemon is actively running)
*   **Web Browser**: Google Chrome (recommended) or any modern web browser
*   **Development IDE**: IntelliJ IDEA (for source-code developers)
*   **Operating System**: Windows 10 or later, macOS, or Linux
*   **Network Ports**: Ensure local host ports **8089** (for Docker app mapping), **8088** (for local application runtime), and **5433** (for local PostgreSQL routing) are free.

---

## 🛠️ Quick Start Guide (For QAs or Demo)

Use this method to launch the entire environment instantly in containers without manually configuring compilation environments or IDE setups.

1. Open your terminal (**Git Bash** on Windows, or your native **Terminal** on Linux/macOS).
2. Navigate to the root directory of this project workspace.
3. If you are on Linux or macOS, make the automation scripts executable:
   ```bash
   chmod +x setup.sh cleanup.sh
   ```
4. Run the master setup script to spin up the network, launch the database, compile with Google Jib, and boot the server container:
   ```bash
   ./setup.sh
   ```
5. Once the terminal displays the success banner, **Ctrl+Click** (or Cmd+Click) the link below to access your interface:
   👉 **[http://localhost:8089/1.0/index.html](http://localhost:8089/1.0/index.html)**
6. After you finish testing or reviewing the demo, cleanly tear down the container processes and internal Docker networks by running:
   ```bash
   ./cleanup.sh
   ```

---

## 🖥️ Local Development Setup (For Developers)

Use these manual steps if you need to inspect code, run active debugging sessions, or make changes using IntelliJ IDEA.

### 1. Launch the PostgreSQL Container Backend
Run this standalone command to spin up your persistence layer. The script opens port `5433` on your host computer to prevent crashing against any local Postgres instances you might already have installed on port `5432`:
```bash
docker run --name dev-postgres --restart always -e POSTGRES_USER=sp_user -e POSTGRES_PASSWORD=secure_app_password -e POSTGRES_DB=shiftplanner -p 5433:5432 -d postgres:18.4
```

### 2. Import Into IntelliJ IDEA
1. Clone the repository to your local workspace environment.
2. Open IntelliJ IDEA.
3. Select **Open** from the welcome window (or navigate to **File** > **Open...**).
4. Select the project's root folder (the folder containing your `build.gradle` configuration).
5. If prompted, confirm by selecting **Open as Project**.
6. Wait for IntelliJ to detect Gradle and download dependencies automatically.

### 3. Verify IDE SDK and Toolchain Rules
*   **Project SDK**: Go to **File** > **Project Structure...** (`Ctrl+Alt+Shift+S` or `Cmd+;`). Under the **Project** tab, confirm that both your **SDK** and **Language level** are explicitly set to **21**.
*   **Gradle Compiler**: Go to **Settings** (`Ctrl+Alt+S`) > **Build, Execution, Deployment** > **Build Tools** > **Gradle**. Confirm the **Gradle JVM** dropdown points directly to your **Project SDK (version 21)**.

### 4. Compile and Run the App Natively
1. Build your source code workspace directly within the IDE by going to the top toolbar menu and selecting **Build** > **Build Project** (`Ctrl+F9` or `Cmd+F9`).
2. Open the file browser and navigate to the application main entry class: `ShiftplannerServerApplication.java`.
3. Right-click inside the file context and select **Run 'ShiftplannerServerApplication.main()'**.
4. Access your locally running web context through your browser at:
   👉 **[http://localhost:8088/1.0/index.html](http://localhost:8088/1.0/index.html)**
