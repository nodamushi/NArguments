package nodamushi.args;
/**
 * 文字列→オブジェクトや数値への変換処理でエラーが発生した
 * @author nodamushi
 *
 */
public class ConvertException extends RuntimeException{
  String name;
  public ConvertException(final String name,final String message,final Throwable cause){
    super(message,cause);
    this.name = name;
  }
}
