@echo off
echo Starting Metrolog SIH 2026

echo Setting JAVA_HOME for Maven Wrapper...
set "JAVA_HOME=C:\Users\desai\.vscode\extensions\redhat.java-1.55.0-win32-x64\jre\21.0.11-win32-x86_64"

echo Starting Spring Boot Backend...
start cmd /k "cd backend && .\mvnw spring-boot:run"

echo Starting Vite React Frontend...
start cmd /k "cd finalfrontend && pnpm dev"
