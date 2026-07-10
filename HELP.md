# Getting Started

## Reference Documentation

For further reference, please consider:

- [Official Gradle documentation](https://docs.gradle.org)
- [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/4.1.0/gradle-plugin)
- [Create an OCI image](https://docs.spring.io/spring-boot/4.1.0/gradle-plugin/packaging-oci-image.html)

## Additional Links

- [Gradle Build Scans - insights for your project's build](https://scans.gradle.com#gradle)

## Tools

### draw.io (Docker)

```powershell
docker run -it --rm --name="draw" -p 8084:8080 -p 8443:8443 jgraph/drawio
```

Open:

```text
http://localhost:8084/?offline=1&https=0
```

### Java

IntelliJ installed JDK at:

```text
C:\Users\Kate\.jdks\ms-21.0.11
```

Set `JAVA_HOME` to that path.

1. Open the Windows Start Menu, search for `environment variables`, and select **Edit the system environment variables**.
2. Click **Environment Variables...**.
3. Under **System variables**, click **New...** and create:
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Users\Kate\.jdks\ms-21.0.11`
4. Under **System variables**, select `Path` and click **Edit...**.
5. Click **New** and add:

```text
%JAVA_HOME%\bin
```

6. Click **OK** on all windows to save.

### Gradle

IntelliJ installed Gradle at:

```text
C:\Users\Kate\.gradle\wrapper\dists\gradle-9.6.1-bin\4ticwg1pgcbps2hj28r8so764\gradle-9.6.1\bin
```

Gradle wrapper distribution path (example local cache):

```text
C:\Users\Kate\.gradle\wrapper\dists\gradle-9.6.1-bin\4ticwg1pgcbps2hj28r8so764\gradle-9.6.1\bin
```

#### Configure `Path`

1. Open the Windows Start Menu, search for `environment variables`, and select **Edit the system environment variables**.
2. Click **Environment Variables...**.
3. Under **System variables**, select `Path` and click **Edit...**.
4. Click **New** and paste:

```text
C:\Users\Kate\.gradle\wrapper\dists\gradle-9.6.1-bin\4ticwg1pgcbps2hj28r8so764\gradle-9.6.1\bin
```

5. Click **OK** on all windows to save.

#### Verify installation

Open a new PowerShell or Command Prompt window and run:

```powershell
gradle -v
```

If successful, the terminal shows the installed Gradle version and local environment details.

