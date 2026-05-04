package se.fk.rimfrost.adapter.arbetsgivare.exception;

public class ArbetsgivareException extends RuntimeException
{

   private final ArbetsgivareErrorCode errorCode;

   public ArbetsgivareException(String message, ArbetsgivareErrorCode errorCode)
   {
      super(message);
      this.errorCode = errorCode;
   }

   public ArbetsgivareErrorCode getErrorCode()
   {
      return errorCode;
   }
}
