package abn.parking.core;

import abn.parking.core.configuration.TestClockConfiguration;
import abn.parking.core.service.ObservationsService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// the TestClockConfiguration is the only bean where we use a test bean instead of the real one
// this is because we want to control the time in the tests
@Import(TestClockConfiguration.class)
@Sql(scripts = {"/sql/init.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"/sql/clean.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CoreApplicationTests {

    @LocalServerPort
    private Integer port;

    // the only reason we autowire this is to call the verifyObservations() method, which is usually scheduled.
    @Autowired
    private ObservationsService observationsService;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
    }

    @Test
    void givenNoSessionForLicense_whenGetSession_then404() {
        var requestSpecification = given().param("license", "TEST_LICENSE");

        var response = requestSpecification.when().get("/sessions");

        response.then().statusCode(404);
    }

    @Test
    void givenLicenseAndStreet_whenStartSession_thenSessionStarted() {
        var requestSpecification = given().pathParam("license", "TEST_LICENSE").body("""
                {
                    "street": "Europaplein"
                }
                """).contentType(ContentType.JSON);

        var response = requestSpecification.when().post("/sessions/{license}/start");

        response.then().statusCode(200)
                .body("license", equalTo("TEST_LICENSE"))
                .body("street", equalTo("Europaplein"))
                .body("startInstant", notNullValue());
    }

    @Test
    void givenNoInvoicesForLicense_whenGetInvoices_thenReturnNoInvoices() {
        var requestSpecification = given().param("license", "TEST_LICENSE");

        var response = requestSpecification.when().get("/invoices");

        response.then().statusCode(200).body("invoices", hasSize(0));
    }

    @Test
    void givenStartedSessionForFreeStreetInParkingPeriod_whenSessionStoppedOneHourLater_thenNoInvoiceCreated() {
        // Saturday 6 January 2024 20:00:00 UTC -> Saturday 6 January 2024 21:00:00 UTC (Free Street)
        var requestSpecification = given().pathParam("license", "PARKING_FREELY");

        var response = requestSpecification.when().post("/sessions/{license}/stop");

        response.then().statusCode(200).body(emptyString());
        given().param("license", "PARKING_FREELY")
                .when().get("/invoices")
                .then().statusCode(200)
                .body("invoices", emptyIterable());
        given().pathParam("license", "PARKING_FREELY")
                .when().post("/sessions/{license}/stop")
                .then().statusCode(404);
    }

    @Test
    void givenStartedSessionForPaidStreetInParkingPeriod_whenSessionStoppedOneHourLater_thenInvoiceCreatedCorrectly() {
        // Saturday 6 January 2024 20:00:00 UTC -> Saturday 6 January 2024 21:00:00 UTC (Europalein)
        var requestSpecification = given().pathParam("license", "ALREADY_PARKING");

        var response = requestSpecification.when().post("/sessions/{license}/stop");

        response.then().statusCode(200)
                .body("invoiceId", notNullValue())
                .body("amount", equalTo(6000));
        given().param("license", "ALREADY_PARKING")
                .when().get("/invoices")
                .then().statusCode(200)
                .body("invoices[0].invoiceId", notNullValue())
                .body("invoices[0].amount", equalTo(6000))
                .body("invoices[0].paid", equalTo(false))
                .body("invoices[0].session.license", equalTo("ALREADY_PARKING"));
        given().pathParam("license", "ALREADY_PARKING")
                .when().post("/sessions/{license}/stop")
                .then().statusCode(404);
    }

    @Test
    void givenStartedSessionForPaidStreetInParkingPeriod_whenSessionStoppedOneWeekLater_thenInvoiceCreatedCorrectly() {
        // Saturday 30 December 2023 21:00:00 UTC -> Saturday 6 January 2024 21:00:00 UTC (Europalein)
        var requestSpecification = given().pathParam("license", "PARKING_LONG");

        var response = requestSpecification.when().post("/sessions/{license}/stop");

        response.then().statusCode(200)
                .body("invoiceId", notNullValue())
                .body("amount", equalTo(468000));
        given().param("license", "PARKING_LONG")
                .when().get("/invoices")
                .then().statusCode(200)
                .body("invoices[0].invoiceId", notNullValue())
                .body("invoices[0].amount", equalTo(468000))
                .body("invoices[0].paid", equalTo(false))
                .body("invoices[0].session.license", equalTo("PARKING_LONG"));
        given().pathParam("license", "PARKING_LONG")
                .when().post("/sessions/{license}/stop")
                .then().statusCode(404);
    }

    @Test
    void givenObservationsWithoutSessions_whenVerifyObservations_thenFineInvoicesCreated() {
        var requestSpecification = given().body("""
                        {
                        	"observations": [
                        		{
                        			"license": "DDD999",
                        			"street": "Europaplein",
                        			"observationInstant": "2021-01-01T12:00:00Z"
                        		}
                        	]
                        }
                        """)
                .contentType(ContentType.JSON);

        var response = requestSpecification.when().post("/observations");
        observationsService.verifyObservations();
        observationsService.verifyObservations();// to test idempotency

        response.then().statusCode(200).body(emptyString());
        given().param("license", "DDD999")
                .when().get("/invoices")
                .then().statusCode(200)
                .body("invoices", hasSize(1))
                .body("invoices[0].amount", equalTo(10000))
                .body("invoices[0].observation.license", equalTo("DDD999"));
    }

    @Test
    void givenObservationsWithSessions_whenVerifyObservations_thenNoInvoicesCreated() {
        given().pathParam("license", "TEST_LICENSE").body("""
                {
                    "street": "Europaplein"
                }
                """).contentType(ContentType.JSON).when().post("/sessions/{license}/start").thenReturn();

        observationsService.verifyObservations();

        given().param("license", "TEST_LICENSE")
                .when().get("/invoices")
                .then().statusCode(200)
                .body("invoices", hasSize(0));
    }

    @Test
    void givenUnpaidInvoice_whenPayInvoices_thenInvoicePaid() {
        given().body("""
                {
                	"observations": [
                		{
                		    "license": "DDD999",
                			"street": "Europaplein",
                			"observationInstant": "2021-01-01T12:00:00Z"
                		}
                	]
                }
                """).contentType(ContentType.JSON).when().post("/observations").thenReturn();
        observationsService.verifyObservations();
        var invoiceId = given().param("license", "DDD999")
                .when().get("/invoices")
                .then().statusCode(200)
                .extract().jsonPath().getInt("invoices[0].invoiceId");
        var requestSpecification = given().pathParam("invoiceId", invoiceId);

        var response = requestSpecification.when().post("/invoices/{invoiceId}/pay");

        response.then().statusCode(200).body(emptyString());
        given().param("license", "DDD999")
                .when().get("/invoices")
                .then().statusCode(200)
                .body("invoices[0].paid", equalTo(true));
    }

}
