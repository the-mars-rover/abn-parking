## Solution

The Parking system consists of the following components:

* The [Core application](core). This is application provides the REST API for the core functionalities of the parking system. This will be used by any client-side application to interact with the parking system.
* The [Admin application](admin). This is application provides a UI to administrators. The UI will be used by a parking administrator to configure parking rates or view reports.


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

### Important Decisions
* Used contract-first approach to design the REST API. This means the Open API specifications were created first and then interfaces were generated from the specifications.
* Used mutation testing (with a coverage of 85%) to ensure that the tests are effective. Mutation testing is a technique to test the quality of the tests. It works by making small changes to the source code and then running the tests. If the tests fail, then the tests are effective.
* Ideally unit, integration, and end-to-end tests would be present. However, due to time constraints, and the smallish size of the application, only integration testing was done. However, the integration tests effectively cover more than 85% of scenarios
* 
