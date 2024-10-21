package abn.parking.core;

import abn.parking.core.service.ObservationsService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.jdbc.Sql;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"/sql/init.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"/sql/clean.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CoreApplicationTests {

    @LocalServerPort
    private Integer port;

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
        var requestSpecification = given().body("""
                {
                    "license": "TEST_LICENSE",
                    "street": "Europaplein"
                }
                """).contentType(ContentType.JSON);

        var response = requestSpecification.when().post("/sessions/start");

        response.then().statusCode(200)
                .body("sessionId", notNullValue())
                .body("license", equalTo("TEST_LICENSE"))
                .body("street", equalTo("Europaplein"))
                .body("startInstant", notNullValue());
        given().param("license", "TEST_LICENSE")
                .when().get("/sessions")
                .then().statusCode(200)
                .body("sessionId", notNullValue())
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
    void givenStartedSessionForFreeStreet_whenSessionStopped_thenNoInvoiceCreated() {
        var startedSessionId = given().body("""
                {
                    "license": "TEST_LICENSE",
                    "street": "Free Street"
                }
                """).contentType(ContentType.JSON).when().post("/sessions/start").thenReturn().body().path("sessionId");
        var requestSpecification = given().pathParam("sessionId", startedSessionId);

        var response = requestSpecification.when().post("/sessions/{sessionId}/stop");

        given().param("license", "TEST_LICENSE")
                .when().get("/sessions")
                .then().statusCode(404);
        response.then().statusCode(200).body(emptyString());
    }

    @Test
    void givenStartedSessionForPaidStreet_whenSessionStoppedOneMinuteLater_thenInvoiceCreated() {
        var startedSessionId = given().param("license", "ALREADY_PARKING")
                .when().get("/sessions")
                .then().extract().jsonPath().getInt("sessionId");
        var requestSpecification = given().pathParam("sessionId", startedSessionId);

        var response = requestSpecification.when().post("/sessions/{sessionId}/stop");

        given().param("license", "ALREADY_PARKING")
                .when().get("/sessions")
                .then().statusCode(404);
        response.then().statusCode(200)
                .body("invoiceId", notNullValue())
                .body("amount", equalTo(100));
        given().param("license", "ALREADY_PARKING")
                .when().get("/invoices")
                .then().statusCode(200)
                .body("invoices[0].invoiceId", notNullValue())
                .body("invoices[0].amount", equalTo(100))
                .body("invoices[0].paid", equalTo(false))
                .body("invoices[0].session.license", equalTo("ALREADY_PARKING"));
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
        given().body("""
                {
                    "license": "TEST_LICENSE",
                    "street": "Europaplein"
                }
                """).contentType(ContentType.JSON).when().post("/sessions/start").thenReturn();

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
