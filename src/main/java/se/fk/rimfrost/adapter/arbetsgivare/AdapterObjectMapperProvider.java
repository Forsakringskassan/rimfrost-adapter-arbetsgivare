package se.fk.rimfrost.adapter.arbetsgivare;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.ext.ContextResolver;

class AdapterObjectMapperProvider implements ContextResolver<ObjectMapper>
{
   private final ObjectMapper objectMapper;

   public AdapterObjectMapperProvider()
   {
      objectMapper = createObjectMapper();
   }

   @Override
   public ObjectMapper getContext(Class<?> type)
   {
      return objectMapper;
   }

   private ObjectMapper createObjectMapper()
   {
      var objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      return objectMapper;
   }
}
