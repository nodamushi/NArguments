package nodamushi.args;

import nodamushi.args.AnnotationReflection.Opt;

/**
 * Arguments,Option,Descriptionアノテーションを処理するクラス。
 * @author nodamushi
 *
 * @param <T>
 */
public class NArgument<T>{

  private AnnotationReflection<T> ar;
  private boolean inputed=false;
  private boolean ignoreConvertException = true;
  private boolean error=false;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public NArgument(final T t){
    ar = new AnnotationReflection(t.getClass());
    ar.setT(t);
  }

  public NArgument(final Class<T> t){
    ar = new AnnotationReflection<T>(t);
  }

  public void set(final T t){
    error = false;
    inputed = false;
    ar.setT(t);
  }

  /**
   *
   * @return
   */
  public T get(){
    return ar.getT();
  }

  /**
   * 説明を返す
   * @return
   */
  public String getDescription(){
    final String nl = System.getProperty("line.separator");
    final StringBuilder sb=new StringBuilder();
    final String d = ar.getDescription();
    if(!d.isEmpty()){
      sb.append(d).append(nl).append(nl);
    }
    String s = getOptionDescription(6);
    if(!s.isEmpty()){
      sb.append("   Options:").append(nl).append(s).append(nl);
    }
    s = getArgumentDescription(6);
    if(!s.isEmpty()){
      sb.append("   Arguments:").append(nl).append(s).append(nl);
    }
    return sb.toString();
  }

  /**
   * addArgumentsで有効な入力があったかどうか
   * @return
   */
  public boolean wasInputed(){
    return inputed;
  }
  /**
   * Argumentの説明を返す
   * @param indent
   * @return
   */
  public String getArgumentDescription(final int indent){
    final StringBuilder sb = new StringBuilder();
    ar.argString(sb, indent);
    return sb.toString();
  }
  /**
   * Optionの説明を返す
   * @param indent
   * @return
   */
  public String getOptionDescription(final int indent){
    final StringBuilder sb = new StringBuilder();
    ar.optionString(sb, indent);
    return sb.toString();
  }
  /**
   * trueのとき、addArgumentsがConverExceptionを投げなくなる
   * @param b
   */
  public void setIgnoreConvertException(final boolean b){
    ignoreConvertException=b;
  }

  /**
   * trueのとき、addArgumentsがConverExceptionを投げなくなる
   * @param b
   */
  public boolean isIgnoreConvertException(){
    return ignoreConvertException;
  }


  /**
   * addArgumentsの途中でエラーが発生したかどうか
   * @return
   */
  public boolean isErrorOccered(){
    return error;
  }
  /**
   * 入力を受け取る
   * @param args 入力
   * @throws ReflectionException 何らかのリフレクションエラーが発生した場合。
   * @throws ConvertException 何らかの変換処理においてエラーが発生した場合。ただし、isIgnoreConvertExceptionがtrueのときは投げない。
   */
  public void addArguments(final String[] args) throws ReflectionException, ConvertException{
    if(ar.getT()==null){
      ar.newInstance();
    }
    for(int i=0,e=args.length;i<e;i++){
      try{
        final String s = args[i];
        final Opt opt = ar.findOption(s);
        if(opt==null){
          boolean flag=true;

          //フラグオプションを複数連結して書いている場合
          if(s.charAt(0)=='-'){
            boolean t = true;
            for(int ii=1;ii<s.length();ii++){
              final String ss = new String(new char[]{'-',s.charAt(ii)});
              final Opt optShort = ar.findOption(ss);
              if(optShort==null || !optShort.isSingleOption()){
                t = false;
              }
            }
            if(t){
              flag = false;
              for(int ii=1;ii<s.length();ii++){
                final String ss = new String(new char[]{'-',s.charAt(ii)});
                final Opt optShort = ar.findOption(ss);
                ar.setBooleanOption(optShort);
              }
            }
          }

          //どれにも該当しない場合
          if(flag){
            final boolean b = ar.setArgument(s);
            if(!b) {
              error=true;
            }else{
              inputed=true;
            }
          }
        }else{
          if(opt.isSingleOption()){
            ar.setBooleanOption(opt);
          }else if(i+1 < args.length){
            ar.setOption(opt, args[i+1]);
            inputed=true;
            i++;
          }else{
            error = true;
          }
        }
      }catch(final ConvertException ex){
        error = true;
        if(!ignoreConvertException){
          throw ex;
        }
      }
    }


  }

}
