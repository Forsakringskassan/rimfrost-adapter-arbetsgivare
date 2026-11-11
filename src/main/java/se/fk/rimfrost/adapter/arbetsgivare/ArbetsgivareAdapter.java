package se.fk.rimfrost.adapter.arbetsgivare;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.rimfrost.adapter.arbetsgivare.dto.ArbetsgivareRequest;
import se.fk.rimfrost.adapter.arbetsgivare.dto.ArbetsgivareResponse;
import se.fk.rimfrost.adapter.arbetsgivare.dto.SpecificeradLonRequest;
import se.fk.rimfrost.adapter.arbetsgivare.dto.SpecificeradLonResponse;
import se.fk.rimfrost.adapter.arbetsgivare.exception.ArbetsgivareErrorCode;
import se.fk.rimfrost.adapter.arbetsgivare.exception.ArbetsgivareException;
import se.fk.rimfrost.api.arbetsgivare.jaxrsspec.controllers.generatedsource.ArbetsgivareControllerApi;
import se.fk.rimfrost.api.arbetsgivare.jaxrsspec.controllers.generatedsource.model.GetArbetsgivare200Response;
import se.fk.rimfrost.api.arbetsgivare.jaxrsspec.controllers.generatedsource.model.SpecificeradLon;

@ApplicationScoped
public class ArbetsgivareAdapter
{
   private static final Logger LOGGER = LoggerFactory.getLogger(ArbetsgivareAdapter.class);

   @ConfigProperty(name = "arbetsgivare.api.base-url")
   String arbetsgivareApiBaseUrl;

   @Inject
   ArbetsgivareMapper mapper;

   private ArbetsgivareControllerApi arbetsgivareClient;

   private Client client;

   @PostConstruct
   void init()
   {
      ClientConfig clientConfig = new ClientConfig();
      clientConfig.connectorProvider(new Apache5ConnectorProvider());
      clientConfig.register(AdapterObjectMapperProvider.class);
      client = JerseyClientBuilder.createClient(clientConfig);
      this.arbetsgivareClient = WebResourceFactory.newResource(
            ArbetsgivareControllerApi.class,
            client.target(arbetsgivareApiBaseUrl));
   }

   @PreDestroy
   void destroy()
   {
      arbetsgivareClient = null;

      if (client != null)
      {
         client.close();
         client = null;
      }
   }

   public ArbetsgivareResponse getArbetsgivareInfo(ArbetsgivareRequest arbetsgivareRequest) throws ArbetsgivareException
   {
      GetArbetsgivare200Response apiResponse;
      try
      {
         apiResponse = arbetsgivareClient.getArbetsgivare(arbetsgivareRequest.personnummer());
      }
      catch (WebApplicationException e)
      {
         throw toArbetsgivareException(e, "arbetsgivare", arbetsgivareRequest.personnummer());
      }
      catch (ProcessingException e)
      {
         var message = "Unexpected server response when fetching arbetsgivare for personnummer "
               + arbetsgivareRequest.personnummer();
         LOGGER.error(message);
         throw new ArbetsgivareException(message, ArbetsgivareErrorCode.UNEXPECTED_ERROR);
      }

      try
      {
         return mapper.toArbetsgivareResponse(apiResponse);
      }
      catch (Exception e)
      {
         var message = "Unexpected error when mapping arbetsgivare response for personnummer "
               + arbetsgivareRequest.personnummer();
         LOGGER.error(message);
         throw new ArbetsgivareException(message, ArbetsgivareErrorCode.UNEXPECTED_ERROR);
      }
   }

   public SpecificeradLonResponse getSpecificeradLon(SpecificeradLonRequest request) throws ArbetsgivareException
   {
      if (request.tomDatum().isBefore(request.fromDatum()))
      {
         throw new ArbetsgivareException("tomDatum cannot be before fromDatum", ArbetsgivareErrorCode.BAD_REQUEST);
      }

      SpecificeradLon apiResponse;
      try
      {
         apiResponse = arbetsgivareClient.getSpecificeradLon(
               request.personnummer(),
               request.fromDatum(),
               request.tomDatum());
      }
      catch (WebApplicationException e)
      {
         throw toArbetsgivareException(e, "specificeradLon", request.personnummer());
      }
      catch (ProcessingException e)
      {
         var message = "Unexpected server response when fetching specificeradLon for personnummer " + request.personnummer();
         LOGGER.error(message);
         throw new ArbetsgivareException(message, ArbetsgivareErrorCode.UNEXPECTED_ERROR);
      }

      try
      {
         return mapper.toSpecificeradLonResponse(apiResponse);
      }
      catch (Exception e)
      {
         var message = "Unexpected error when mapping specificeradLon response for personnummer " + request.personnummer();
         LOGGER.error(message);
         throw new ArbetsgivareException(message, ArbetsgivareErrorCode.UNEXPECTED_ERROR);
      }
   }

   private ArbetsgivareException toArbetsgivareException(WebApplicationException e, String resource, String personnummer)
   {
      return switch (e.getResponse().getStatus())
      {
         case 404 -> {
            var message = "Arbetsgivare not found for personnummer " + personnummer;
            LOGGER.error(message);
            yield new ArbetsgivareException(message, ArbetsgivareErrorCode.NOT_FOUND);
         }
         case 400 -> {
            var message = "Bad request when fetching " + resource + " for personnummer " + personnummer;
            LOGGER.error(message);
            yield new ArbetsgivareException(message, ArbetsgivareErrorCode.BAD_REQUEST);
         }
         case 503 -> {
            var message = "Service unavailable when fetching " + resource + " for personnummer " + personnummer;
            LOGGER.error(message);
            yield new ArbetsgivareException(message, ArbetsgivareErrorCode.SERVICE_UNAVAILABLE);
         }
         default -> {
            var message = "Unexpected server response when fetching " + resource + " for personnummer " + personnummer;
            LOGGER.error(message);
            yield new ArbetsgivareException(message, ArbetsgivareErrorCode.UNEXPECTED_ERROR);
         }
      };
   }
}
