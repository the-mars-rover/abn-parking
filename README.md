# ABN Parking

This project has been completed as a technical assignment for ABN AMRO. 

## Requirements

In the Netherlands, when you park your car on the streets, it's usually necessary to pay for parking between 08:00 - 21:00.
The process involves registering your license plate number in the parking system when you park and then unregistering it when you leave. The cost is calculated based on the duration (in minutes) of your parking stay, with the exception of free parking daily between 21:00 - 08:00 and on Sundays for the whole day.

Here's how it works:
1. Imagine a car driver arrives in the city center and parks at 2:00 PM. He visits a website (Or an app), insert his license plate number, street name where he is parking and hit the "START" button. He receives a confirmation that his parking session has begun. After 55 minutes, he returns to his car, revisits the website, enters now only the license plate, and press the "END" button. He receives a confirmation that his parking session has ended, and he owes x euros. The price depends on the street where the car is parked (You can use the table below as an example) and the duration.
2. To monitor parking, the city employs a specialized car equipped with a camera. This car drives in the streets and records the license plate numbers of parked cars along with the street names where they are parked and date of observation. This information is later transmitted to the parking system.
3. If a license plate is identified without a corresponding registration during an observation of step 2, the car owner receives a fine notification sent by a letter to his home address.

Please develop a REST API (ONLY BACKEND) using Java and Spring Boot for this parking system:
* Users should be able to register and unregister their cars (following step 1).
* The city's parking administrator should be able to put a list of collected license plates data (number, street name and date of observation) in the system (similar to step 2).
* A scheduled task must run to identify the unregistered plates (as described in step 3), resulting in the generation of a report, including car plate nrs, date of observation and street name.


Note: This assignment is designed to test your understanding of Java, RESTful API development, basic data structures and Spring Boot concepts. Focus on clean and organized code, proper error handling, unit tests, and adherence to the outlined requirements.

| Street Name  | Price (Cent/minute)  |
|--------------|----------------------|
| Java         | 15                   |
| Jakarta      | 13                   |
| Spring       | 10                   |
| Azure        | 10                   |


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
