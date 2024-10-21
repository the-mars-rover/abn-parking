## Solution

The Parking system consists of the following components:

* The [Core application](core). This is a Spring Boot application which provides the REST API for the core functionalities of the parking system. This will be used by any client-side application to interact with the parking system.
* The [Admin application](core). This is a Spring Boot application which provides a UI administrators. The UI will be used by a parking administrator to configure parking rates or view reports.

### Demo

To run the entire system, start by building each maven project:

```bash
  cd core
  mvn clean install
```

```bash
  cd admin
  mvn clean install
```

Then, simply run the following command (note that ports 5432, 8080, and 8081 must be available):

```bash
docker-compose up
```

1. At http://localhost:8080 you can access the Swagger UI for the core application. 
2. At http://localhost:8081/admin you can access an administrative interface for the parking system. 
