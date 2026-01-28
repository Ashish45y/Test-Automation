# Project Setup

## Requirements and Quick Setup

**Start here!** This guide covers system requirements, prerequisites checklist, quick installation, and verification steps.

---

## System Requirements

### Minimum Requirements

- **OS**: Linux, macOS, or Windows (with WSL2)
- **RAM**: 4GB (8GB recommended)
- **Disk Space**: 5GB free space
- **CPU**: 2 cores (4 cores recommended)

### Required Software

1. **Java 17 or higher**
   - Download from: https://adoptium.net/
   - Verify: `java -version`

2. **Maven 3.6+**
   - Download from: https://maven.apache.org/download.cgi
   - Verify: `mvn -version`

3. **Docker and Docker Compose**
   - Docker Desktop: https://www.docker.com/products/docker-desktop
   - Verify: `docker --version` and `docker compose version`

4. **Git** (optional but recommended)
   - Download from: https://git-scm.com/downloads

---

## Prerequisites Checklist

Run the prerequisite check script:

```bash
./check-prerequisites.sh
```
## before run the .sh file(script file run this command only first time.)
-chmod +x followed by the filename.
-like (chmod +x filename.sh)
This will verify:
- Java 17+ installation
- Maven 3.6+ installation
- Docker and Docker Compose availability
- Docker daemon status
- Git (optional)

---

## Quick Installation

### Option 1: Automated Setup (Linux/macOS)

```bash
# 1. Check prerequisites
./check-prerequisites.sh

# 2. Run automated setup
./setup.sh

# 3. Pull Docker containers
./pull-containers.sh

# 4. Run the project
./run-project.sh
```

### Option 2: Quick Start (All Platforms)

```bash
./quick-start.sh
```

This single command will:
1. Check prerequisites
2. Pull Docker containers
3. Start services
4. Build the project
5. Run tests

### Option 3: Manual Setup

See [SETUP_GUIDE.md](SETUP_GUIDE.md) for detailed manual installation instructions.

---

## Verification Steps

### 1. Verify Prerequisites

```bash
./check-prerequisites.sh
```

All checks should pass before proceeding.

### 2. Verify Docker Containers

```bash
# Start containers
docker-compose up -d

# Check container status
docker-compose ps

# Verify services
curl http://localhost:4444/status  # Selenium Grid
curl http://localhost:9200          # Elasticsearch (if enabled)
```

### 3. Verify Project Build

```bash
mvn clean compile
```

Should complete without errors.

### 4. Verify Test Execution

```bash
mvn clean test
```

Tests should run successfully.

---

## Setup Checklist

Before running tests, ensure:

- [ ] Java 17+ is installed and in PATH
- [ ] Maven 3.6+ is installed and in PATH
- [ ] Docker is installed and daemon is running
- [ ] Docker Compose is available
- [ ] All prerequisites verified (`./check-prerequisites.sh`)
- [ ] Docker containers pulled (`./pull-containers.sh`)
- [ ] Project builds successfully (`mvn clean compile`)
- [ ] Services are accessible (Selenium Grid, MySQL, etc.)

---

## Next Steps

After completing setup:

1. **Run your first test**: `./run-project.sh`
2. **View results**: Check `target/cucumber-report.html`
3. **Read detailed guide**: [SETUP_GUIDE.md](SETUP_GUIDE.md)
4. **Understand Docker**: See Docker section in SETUP_GUIDE.md
5. **Learn to run tests**: See [HOW_TO_RUN_PROJECT.md](HOW_TO_RUN_PROJECT.md)

---

## Troubleshooting

If you encounter issues during setup:

1. **Prerequisites not met**: Run `./check-prerequisites.sh` to identify missing components
2. **Docker issues**: Ensure Docker daemon is running (`docker info`)
3. **Port conflicts**: See [PORT_CONFIGURATION.md](PORT_CONFIGURATION.md)
4. **Build failures**: Check Java version (must be 17+)
5. **Container conflicts**: Run `./cleanup-containers.sh`

For detailed troubleshooting, see [TROUBLESHOOTING.md](TROUBLESHOOTING.md).

---

**Last Updated**: January 2025
