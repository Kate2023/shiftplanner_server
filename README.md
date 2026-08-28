# Shift Planner System

Welcome to the README for the Shift Planner System.

The Shift Planner System is a comprehensive solution designed to streamline the scheduling and management of employee shifts. This system provides an intuitive interface for both administrators and employees, allowing for efficient shift planning, tracking, and communication.

## 📦 Included Artefacts

The following artefacts are included in this submission and are required for the build, configuration, and execution of the Shift Planner System:

- `src/` — application source code and resources, including:
  - `src/main/java/` — Java server and application logic
  - `src/main/resources/` — application configuration, static assets, and database runtime resources
  - `src/test/java/` — automated test cases
  - `src/test/resources/` — test configuration and fixtures
- `build.gradle` and `settings.gradle` — Gradle build configuration and dependency management
- `gradlew`, `gradlew.bat`, and `gradle/` — Gradle wrapper scripts and supporting files
- `setup.sh` and `cleanup.sh` — environment setup and shutdown scripts
- `README.md` and `HELP.md` — project documentation and usage instructions
- `build/` — generated build artefacts, reports, compiled classes, and Jib image metadata
- `.editorconfig`, `.gitattributes`, `.gitignore`, `.idea/`, `.gradle/`, and `shiftplanner_server.iml` — repository, IDE, and project metadata

All artefacts listed above are included in the final submitted archive to ensure the complete project can be compiled and executed as provided.

## 📋 System Requirements (Important)
To successfully build and run the Shift Planner System, ensure your development environment meets the following requirements:
*   **Java Development Kit (JDK)**: 21 (Specifically tested on 21.0.11)
*   **Gradle Wrapper**: 9.6.1 (Ensure you have the necessary permissions to run Gradle commands and build the project)
*   **Docker Desktop**: (Ensure the engine/daemon is actively running, and you have sufficient permissions to execute Docker commands)
*   **Web Browser**: Google Chrome (recommended) or any modern web browser
*   **Development IDE**: IntelliJ IDEA (Optional, for source-code developers)
*   **Operating System**: Windows 10 or later, macOS, or Linux
*   **Network Ports**: Ensure local host ports **8089** (for Docker app mapping), **8088** (for local application runtime), and **5433** (for local PostgreSQL routing) are free.
*   **Memory Requirements**: At least 16GB of RAM is recommended for running Docker containers and the application simultaneously.
---

## 🛠️ Quick Start Guide (For Build, Run a Demo or QA testing)

Use this method to launch the entire environment instantly in containers without manually configuring compilation environments or IDE setups.

1. Check the above system requirements and ensure your environment is ready for Docker and Gradle operations.
2. Open your terminal (**Git Bash** on Windows, or your native **Terminal** on Linux/macOS).
3. Navigate to the root directory of this project workspace.
4. If you are on Linux or macOS, make the automation scripts executable:
   ```bash
   chmod +x setup.sh cleanup.sh
   ```
5. Run the master setup script to spin up the network, launch the database, compile with Google Jib, and boot the server container:
   ```bash
   ./setup.sh
   ```
6. If you see any errors, including missing JDK, Gradle, Docker, etc or face any permission errors, fix those issues and re-run the setup script. The script is idempotent and can be run multiple times without side effects.
7. Once the terminal displays the success banner, **Ctrl+Click** (or Cmd+Click) the link below to access your interface:
   👉 **[http://localhost:8089/1.0/index.html](http://localhost:8089/1.0/index.html)**
6. After you finish testing or reviewing the demo, cleanly tear down the container processes and internal Docker networks by running:
   ```bash
   ./cleanup.sh
   ```

---

## 🖥️ Local Development Setup (For Developers -- Optional. You can skip this if you only need to build and run the demo)

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
