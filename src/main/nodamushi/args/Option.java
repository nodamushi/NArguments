package nodamushi.args;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 名前のつく引数。<br/>
 * Methodにつける場合は、必ず引数は一つで、以下に列挙する対応クラスを引数にとること。<br/>
 * Fieldに設定した場合でも、setterが存在する場合は、直接設定せずに、setterを利用する<br/><br/>
 *
 * Fieldの型もしくは、Methodの引数が、booleanもしくは、Booleanである場合は、引数なしのオプションと見なす。<br/><br/>
 *
 * 対象がFieldで、かつsetterが存在せず、FieldがCollectionの実装であり、Collectionが保持する総称型が以下の対応クラスである場合は、
 *
 *
 * 対応しているクラスは、boolean,int,byte,char,short,long,float,double,String,Charset,File,Path<br/>
 * @author nodamushi
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD})
public @interface Option{
  public String value();
  public String[] alias() default {};
  public String description() default "";
  /**
   * trueの時、複数回の入力があるとエラーになります。<br/>
   * ただし、対象がbooleanであるか、もしくは、Fieldで、かつsetterが存在せず、FieldがCollection型である場合はこの値は無視されます。
   * @return
   */
  public boolean single() default true;
}
