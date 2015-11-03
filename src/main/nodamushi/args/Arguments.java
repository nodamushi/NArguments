package nodamushi.args;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Optionにマッチしなかった引数を受け入れることを示すアノテーション<br/>
 * @author nodamushi
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD})
public @interface Arguments{
  /**
   * 名前
   * @return
   */
  String value() default "";
  /**
   * 無視をする文字列を定義する
   * @return 無視をする文字列の正規表現
   */
  String ignore() default "";
  /**
   * 受け入れる文字列を定義する
   * @return 受け入れる文字列の正規表現
   */
  String match() default "";
  /**
   * 説明
   * @return 説明
   */
  String description() default "";
  /**
   *
   * @return
   */
  boolean single() default false;
}
