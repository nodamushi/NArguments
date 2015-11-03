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
@Target({ElementType.TYPE})
public @interface Description{
  String value();
}
