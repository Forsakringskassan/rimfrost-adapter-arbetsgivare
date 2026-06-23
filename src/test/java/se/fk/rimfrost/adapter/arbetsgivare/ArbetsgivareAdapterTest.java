package se.fk.rimfrost.adapter.arbetsgivare;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import io.quarkus.test.component.QuarkusComponentTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.adapter.arbetsgivare.dto.ArbetsgivareRequest;
import se.fk.rimfrost.adapter.arbetsgivare.dto.ImmutableArbetsgivareRequest;
import se.fk.rimfrost.adapter.arbetsgivare.dto.ImmutableArbetsgivareResponse;
import se.fk.rimfrost.adapter.arbetsgivare.dto.ImmutableLoneradDto;
import se.fk.rimfrost.adapter.arbetsgivare.dto.ImmutableSpecificeradLonRequest;
import se.fk.rimfrost.adapter.arbetsgivare.dto.ImmutableSpecificeradLonResponse;
import se.fk.rimfrost.adapter.arbetsgivare.dto.LoneradDto;
import se.fk.rimfrost.adapter.arbetsgivare.dto.SpecificeradLonRequest;
import se.fk.rimfrost.adapter.arbetsgivare.exception.ArbetsgivareErrorCode;
import se.fk.rimfrost.adapter.arbetsgivare.exception.ArbetsgivareException;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusComponentTest(useSystemConfigSources = true, value =
{
      ArbetsgivareMapper.class
})
public class ArbetsgivareAdapterTest
{
   private static WireMockServer server;

   @Inject
   ArbetsgivareAdapter arbetsgivareAdapter;

   @BeforeAll
   public static void setup()
   {
      server = new WireMockServer(
            options()
                  .dynamicPort()
                  .usingFilesUnderDirectory("src/test/resources"));
      server.start();

      System.setProperty("arbetsgivare.api.base-url", server.baseUrl());
   }

   @AfterAll
   public static void teardown()
   {
      if (server != null)
      {
         server.stop();
      }
   }

   @BeforeEach
   void resetStubs()
   {
      server.resetToDefaultMappings();
   }

   @Test
   public void should_throw_with_error_type_not_found_on_status_404_during_get_arbetsgivare_info()
   {
      server.stubFor(WireMock.get(WireMock.urlPathMatching("/arbetsgivare/.+"))
            .willReturn(WireMock.aResponse().withStatus(404)));
      var exception = assertThrows(ArbetsgivareException.class,
            () -> arbetsgivareAdapter.getArbetsgivareInfo(createArbetsgivareRequest()));
      assertEquals(ArbetsgivareErrorCode.NOT_FOUND, exception.getErrorCode());
   }

   @Test
   public void should_throw_with_error_type_bad_request_on_status_400_during_get_arbetsgivare_info()
   {
      server.stubFor(WireMock.get(WireMock.urlPathMatching("/arbetsgivare/.+"))
            .willReturn(WireMock.aResponse().withStatus(400)));
      var exception = assertThrows(ArbetsgivareException.class,
            () -> arbetsgivareAdapter.getArbetsgivareInfo(createArbetsgivareRequest()));
      assertEquals(ArbetsgivareErrorCode.BAD_REQUEST, exception.getErrorCode());
   }

   @Test
   public void should_throw_with_error_type_service_unavailable_on_status_503_during_get_arbetsgivare_info()
   {
      server.stubFor(WireMock.get(WireMock.urlPathMatching("/arbetsgivare/.+"))
            .willReturn(WireMock.aResponse().withStatus(503)));
      var exception = assertThrows(ArbetsgivareException.class,
            () -> arbetsgivareAdapter.getArbetsgivareInfo(createArbetsgivareRequest()));
      assertEquals(ArbetsgivareErrorCode.SERVICE_UNAVAILABLE, exception.getErrorCode());
   }

   @Test
   public void should_throw_with_error_type_unexpected_error_on_status_500_during_during_get_arbetsgivare_info()
   {
      server.stubFor(WireMock.get(WireMock.urlPathMatching("/arbetsgivare/.+"))
            .willReturn(WireMock.aResponse().withStatus(500)));
      var exception = assertThrows(ArbetsgivareException.class,
            () -> arbetsgivareAdapter.getArbetsgivareInfo(createArbetsgivareRequest()));
      assertEquals(ArbetsgivareErrorCode.UNEXPECTED_ERROR, exception.getErrorCode());
   }

   @Test
   public void should_throw_with_error_type_unexpected_error_on_connection_reset_during_get_arbetsgivare_info()
   {
      server.stubFor(WireMock.get(WireMock.urlPathMatching("/arbetsgivare/.+"))
            .willReturn(WireMock.aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
      var exception = assertThrows(ArbetsgivareException.class,
            () -> arbetsgivareAdapter.getArbetsgivareInfo(createArbetsgivareRequest()));
      assertEquals(ArbetsgivareErrorCode.UNEXPECTED_ERROR, exception.getErrorCode());
   }

   @Test
   public void should_throw_with_error_type_unexpected_error_on_status_200_and_mapper_exception_during_get_arbetsgivare_info()
   {
      server.stubFor(WireMock.get(WireMock.urlPathMatching("/arbetsgivare/.+"))
            .willReturn(WireMock.aResponse().withBody((String) null)));
      var exception = assertThrows(ArbetsgivareException.class,
            () -> arbetsgivareAdapter.getArbetsgivareInfo(createArbetsgivareRequest()));
      assertEquals(ArbetsgivareErrorCode.UNEXPECTED_ERROR, exception.getErrorCode());
   }

   @Test
   public void should_get_arbetsgivare_info() throws ArbetsgivareException
   {
      var expectedArbetsgivareResponse = ImmutableArbetsgivareResponse.builder()
            .organisationsnummer("987654-3210")
            .organisationsnamn("Demo AB")
            .arbetstidProcent(100)
            .anstallningsdag(LocalDate.parse("2021-07-01"))
            .sistaAnstallningsdag(null)
            .build();
      var response = arbetsgivareAdapter.getArbetsgivareInfo(createArbetsgivareRequest());
      assertEquals(expectedArbetsgivareResponse, response);
   }

   @Test
   public void should_throw_with_error_type_not_found_on_status_404_during_get_specificerad_lon()
   {
      server.stubFor(WireMock.get(WireMock.urlPathMatching("/arbetsgivare/.+/specificerad-lon"))
            .willReturn(WireMock.aResponse().withStatus(404)));
      var exception = assertThrows(ArbetsgivareException.class,
            () -> arbetsgivareAdapter.getSpecificeradLon(createSpecificeradLonRequest()));
      assertEquals(ArbetsgivareErrorCode.NOT_FOUND, exception.getErrorCode());
   }

   @Test
   public void should_throw_with_error_type_bad_request_on_status_400_during_get_specificerad_lon()
   {
      server.stubFor(WireMock.get(WireMock.urlPathMatching("/arbetsgivare/.+/specificerad-lon"))
            .willReturn(WireMock.aResponse().withStatus(400)));
      var exception = assertThrows(ArbetsgivareException.class,
            () -> arbetsgivareAdapter.getSpecificeradLon(createSpecificeradLonRequest()));
      assertEquals(ArbetsgivareErrorCode.BAD_REQUEST, exception.getErrorCode());
   }

   @Test
   public void should_throw_with_error_type_service_unavailable_on_status_503_during_get_specificerad_lon()
   {
      server.stubFor(WireMock.get(WireMock.urlPathMatching("/arbetsgivare/.+/specificerad-lon"))
            .willReturn(WireMock.aResponse().withStatus(503)));
      var exception = assertThrows(ArbetsgivareException.class,
            () -> arbetsgivareAdapter.getSpecificeradLon(createSpecificeradLonRequest()));
      assertEquals(ArbetsgivareErrorCode.SERVICE_UNAVAILABLE, exception.getErrorCode());
   }

   @Test
   public void should_throw_with_error_type_unexpected_error_on_status_500_during_during_get_specificerad_lon()
   {
      server.stubFor(WireMock.get(WireMock.urlPathMatching("/arbetsgivare/.+/specificerad-lon"))
            .willReturn(WireMock.aResponse().withStatus(500)));
      var exception = assertThrows(ArbetsgivareException.class,
            () -> arbetsgivareAdapter.getSpecificeradLon(createSpecificeradLonRequest()));
      assertEquals(ArbetsgivareErrorCode.UNEXPECTED_ERROR, exception.getErrorCode());
   }

   @Test
   public void should_throw_with_error_type_unexpected_error_on_connection_reset_during_get_specificerad_lon()
   {
      server.stubFor(WireMock.get(WireMock.urlPathMatching("/arbetsgivare/.+/specificerad-lon"))
            .willReturn(WireMock.aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
      var exception = assertThrows(ArbetsgivareException.class,
            () -> arbetsgivareAdapter.getSpecificeradLon(createSpecificeradLonRequest()));
      assertEquals(ArbetsgivareErrorCode.UNEXPECTED_ERROR, exception.getErrorCode());
   }

   @Test
   public void should_throw_with_error_type_unexpected_error_on_status_200_and_mapper_exception_during_get_specificerad_lon()
   {
      server.stubFor(WireMock.get(WireMock.urlPathMatching("/arbetsgivare/.+/specificerad-lon"))
            .willReturn(WireMock.aResponse().withBody((String) null)));
      var exception = assertThrows(ArbetsgivareException.class,
            () -> arbetsgivareAdapter.getSpecificeradLon(createSpecificeradLonRequest()));
      assertEquals(ArbetsgivareErrorCode.UNEXPECTED_ERROR, exception.getErrorCode());
   }

   @Test
   public void should_get_specificerad_lon() throws ArbetsgivareException
   {
      var expectedSpecificeradLonResponse = ImmutableSpecificeradLonResponse.builder()
            .lonesumma(40000.0)
            .organisationsnamn("Region Dalarna")
            .organisationsnummer("123456-7890")
            .lonerader(List.of(
                  createLoneradDto("GRUNDLON", "Manadslon", 42000.0),
                  createLoneradDto("AVDRAG", "Franvaro VAB", -2000.0)))
            .build();
      var response = arbetsgivareAdapter.getSpecificeradLon(createSpecificeradLonRequest());
      assertEquals(expectedSpecificeradLonResponse, response);
   }

   private static ArbetsgivareRequest createArbetsgivareRequest()
   {
      return ImmutableArbetsgivareRequest.builder()
            .personnummer("19900101-1234")
            .build();
   }

   private static SpecificeradLonRequest createSpecificeradLonRequest()
   {
      return ImmutableSpecificeradLonRequest.builder()
            .fromDatum(LocalDate.now())
            .tomDatum(LocalDate.now())
            .personnummer("19900101-1234")
            .build();
   }

   private static LoneradDto createLoneradDto(String typ, String beskrivning, double belopp)
   {
      return ImmutableLoneradDto.builder()
            .typ(typ)
            .beskrivning(beskrivning)
            .belopp(belopp)
            .build();
   }
}
