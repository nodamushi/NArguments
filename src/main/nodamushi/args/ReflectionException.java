package nodamushi.args;
/**
 * Reflection処理でエラーが発生
 * @author nodamushi
 *
 */
public class ReflectionException extends Exception{
  public ReflectionException(final String name,final String message,final Throwable cause){
    super(message, cause);
  }
}
