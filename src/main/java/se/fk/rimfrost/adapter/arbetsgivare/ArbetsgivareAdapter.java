package se.fk.rimfrost.adapter.arbetsgivare;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.github.jaxrsclientfactory.JaxrsClientFactory;
import se.fk.github.jaxrsclientfactory.JaxrsClientOptionsBuilders;
import se.fk.rimfrost.adapter.arbetsgivare.dto.ArbetsgivareRequest;
import se.fk.rimfrost.adapter.arbetsgivare.dto.ArbetsgivareResponse;
import se.fk.rimfrost.adapter.arbetsgivare.dto.SpecificeradLonRequest;
import se.fk.rimfrost.adapter.arbetsgivare.dto.SpecificeradLonResponse;
import se.fk.rimfrost.adapter.arbetsgivare.exception.ArbetsgivareErrorCode;
import se.fk.rimfrost.adapter.arbetsgivare.exception.ArbetsgivareException;
import se.fk.rimfrost.api.arbetsgivare.jaxrsspec.controllers.generatedsource.ArbetsgivareControllerApi;
import se.fk.rimfrost.api.arbetsgivare.jaxrsspec.controllers.generatedsource.model.GetArbetsgivare200Response;
import se.fk.rimfrost.api.arbetsgivare.jaxrsspec.controllers.generatedsource.model.SpecificeradLon;

@SuppressWarnings(
{
      "unused", "LoggingSimilarMessage"
})
@ApplicationScoped
public class ArbetsgivareAdapter
{
   private static final Logger LOGGER = LoggerFactory.getLogger(ArbetsgivareAdapter.class);

   @ConfigProperty(name = "arbetsgivare.api.base-url")
   String arbetsgivareApiBaseUrl;

   @Inject
   ArbetsgivareMapper mapper;

   private ArbetsgivareControllerApi arbetsgivareClient;

   @PostConstruct
   void init()
   {
      this.arbetsgivareClient = new JaxrsClientFactory()
            .create(JaxrsClientOptionsBuilders.createClient(arbetsgivareApiBaseUrl, ArbetsgivareControllerApi.class)
                  .build());
   }

   public ArbetsgivareResponse getArbetsgivareInfo(ArbetsgivareRequest arbetsgivareRequest)
   {
      GetArbetsgivare200Response apiResponse;
      try
      {
         apiResponse = arbetsgivareClient.getArbetsgivare(arbetsgivareRequest.personnummer());
      }
      catch (NotFoundException e)
      {
         var message = "Arbetsgivare not found for personnummer " + arbetsgivareRequest.personnummer();
         LOGGER.error(message);
         throw new ArbetsgivareException(message, ArbetsgivareErrorCode.NOT_FOUND);
      }
      catch (BadRequestException e)
      {
         var message = "Bad request when fetching arbetsgivare for personnummer " + arbetsgivareRequest.personnummer();
         LOGGER.error(message);
         throw new ArbetsgivareException(message, ArbetsgivareErrorCode.BAD_REQUEST);
      }
      catch (ProcessingException | WebApplicationException e)
      {
         var message = "Service unavailable when fetching arbetsgivare for personnummer " + arbetsgivareRequest.personnummer();
         LOGGER.error(message);
         throw new ArbetsgivareException(message, ArbetsgivareErrorCode.SERVICE_UNAVAILABLE);
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

   public SpecificeradLonResponse getSpecificeradLon(SpecificeradLonRequest request)
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
      catch (NotFoundException e)
      {
         var message = "Arbetsgivare not found for personnummer " + request.personnummer();
         LOGGER.error(message);
         throw new ArbetsgivareException(message, ArbetsgivareErrorCode.NOT_FOUND);
      }
      catch (BadRequestException e)
      {
         var message = "Bad request when fetching specificeradLon for personnummer " + request.personnummer();
         LOGGER.error(message);
         throw new ArbetsgivareException(message, ArbetsgivareErrorCode.BAD_REQUEST);
      }
      catch (ProcessingException | WebApplicationException e)
      {
         var message = "Service unavailable when fetching specificeradLon for personnummer " + request.personnummer();
         LOGGER.error(message);
         throw new ArbetsgivareException(message, ArbetsgivareErrorCode.SERVICE_UNAVAILABLE);
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
}
