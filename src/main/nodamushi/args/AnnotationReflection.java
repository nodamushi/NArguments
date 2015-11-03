package nodamushi.args;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;



class AnnotationReflection<T>{

  static class Base{
    final Method m;
    final Class<?> type;
    final Field f;
    final boolean booleanType;
    final boolean isCollection;
    final boolean once;
    final boolean arraylistOk;

    int refCount;


    Base(final boolean once,final Method m){
      this.m = m;
      m.setAccessible(true);
      type = m.getParameterTypes()[0];
      booleanType = type == boolean.class || type == Boolean.class;
      if(!booleanType){
        checkType(type);
      }
      this.once = once;
      arraylistOk =false;
      isCollection = false;
      f = null;
    }
    Base(final boolean once,final Field f){
      this.f = f;
      f.setAccessible(true);
      Class<?> type = f.getType();
      if(Collection.class.isAssignableFrom(type)){
        arraylistOk = type.isAssignableFrom(ArrayList.class);
        isCollection = true;
        final ParameterizedType genericType = (ParameterizedType)f.getGenericType();
        final Type[] actualTypeArguments = genericType.getActualTypeArguments();
        if(actualTypeArguments.length>0 && (actualTypeArguments[0] instanceof Class)){
          type = (Class<?>)actualTypeArguments[0];
        }
      }else{
        arraylistOk = false;
        isCollection =false;
      }
      this.type =type;
      m = null;
      booleanType = type == boolean.class || type == Boolean.class;
      if(!booleanType){
        checkType(type);
      }
      this.once = once;
    }
    boolean isSingleOption(){
      return booleanType;
    }
    void clear(){
      refCount = 0;
    }
  }

  static class Opt extends Base{
    final Option o;
    final String name;
    Opt(final Option o,final Method m){
      super(o.single(),m);
      this.o = o;
      name = o.value();
    }
    Opt(final Option o,final Field f){
      super(o.single(),f);
      this.o = o;
      name = o.value();
    }
    Option getOption(){return o;}
  }

  static class Arg extends Base{
    final Arguments a;
    final Pattern ignore;
    final Pattern match;

    Arg(final Arguments arg,final Method m){
      super(arg.single(),m);
      ignore =!arg.ignore().isEmpty()? Pattern.compile(arg.ignore()):null;
      match =!arg.match().isEmpty()? Pattern.compile(arg.match()):null;
      a = arg;
    }
    Arg(final Arguments arg,final Field f){
      super(arg.single(),f);
      ignore =!arg.ignore().isEmpty()? Pattern.compile(arg.ignore()):null;
      match =!arg.match().isEmpty()? Pattern.compile(arg.match()):null;
      a=arg;
    }
    Arguments getArguments(){
      return a;
    }

  }



  private static final Class<?> PATH_CLASS;//Pathがない古いバージョン用
  private static final Class<?> PATHS_CLASS;
  static{
    Class<?> c=null,c2=null;
    try {
      c = Class.forName("java.nio.file.Path");
      c2 = Class.forName("java.nio.file.Paths");
    } catch (final ClassNotFoundException e) {
    }
    PATH_CLASS = c;
    PATHS_CLASS = c2;
  }


  private static void checkType(final Class<?> type){
    if(
        int.class != type && byte.class != type && char.class != type &&
        short.class != type && long.class != type && float.class != type &&
        double.class != type && String.class != type&&
        Integer.class != type && Byte.class != type && Character.class != type &&
        Short.class != type && Long.class != type && Float.class != type &&
        Double.class != type && Charset.class != type && File.class != type&&
        (PATH_CLASS!=null && PATH_CLASS != type)
        ){
      throw new RuntimeException("Not compatible class."+type.getName());
    }
  }


  private static Object createType(final Class<?> type,final String value)
      throws NumberFormatException,IllegalCharsetNameException,UnsupportedCharsetException,InvalidPathException{
    if(int.class == type || Integer.class == type){
      return Integer.parseInt(value);
    }

    if(byte.class == type || Byte.class == type ){
      return Byte.parseByte(value);
    }

    if(char.class == type ||    Character.class == type){
      return value.length()==0?(char)0:value.charAt(0);
    }

    if(short.class == type || Short.class == type){
      return Short.parseShort(value);
    }

    if(long.class == type ||Long.class == type ){
      return Long.parseLong(value);
    }

    if(float.class == type || Float.class == type){
      return Float.parseFloat(value);
    }

    if(double.class == type || Double.class == type ){
      return Double.parseDouble(value);
    }

    if(String.class == type){
      return value;
    }

    if(Charset.class == type){
      return Charset.forName(value);
    }

    if(File.class == type){
      return new File(value);
    }

    if((PATHS_CLASS!=null && PATH_CLASS == type)){
      return java.nio.file.Paths.get(value);
    }

    return null;
  }





  private Class<T> claz;

  private HashMap<String, Opt> optionmap;
  private List<Opt> allOptions=new ArrayList<Opt>();
  private List<Arg> argumentslist =new ArrayList<Arg>();
  private String description;
  private T t;


  AnnotationReflection(final Class<T> claz){
    this.claz = claz;
    final Description d = claz.getAnnotation(Description.class);
    if(d!=null){
      description = d.value();
    }else{
      description = "";
    }
    optionmap = new HashMap<String, Opt>();
    final Method[] methods = claz.getDeclaredMethods();
    final Field[] fields = claz.getDeclaredFields();
    final List<Arg> am = new ArrayList<Arg>();
    final List<Arg> af = new ArrayList<Arg>();
    for(final Field f:fields){
      String name = f.getName();
      name ="set"+ Character.toUpperCase(name.charAt(0))+name.substring(1);
      Method set=null;
      for(final Method m:methods){
        if(name.equals(m.getName()) && m.getParameterTypes().length==1){
          set = m;
          break;
        }
      }

      final Option o = f.getAnnotation(Option.class);
      final Arguments a = f.getAnnotation(Arguments.class);
      if(set!=null){
        if(o!=null){
          final Opt oo = new Opt(o, set);
          optionmap.put(o.value(), oo);
          allOptions.add(oo);
          for(final String n:o.alias()){
            optionmap.put(n, oo);
          }
        }
        if(a != null){
          am.add(new Arg(a, set));
        }
      }else{
        if(o!=null){
          final Opt oo = new Opt(o, f);
          optionmap.put(o.value(), oo);
          allOptions.add(oo);
          for(final String n:o.alias()){
            optionmap.put(n, oo);
          }
        }
        if(a != null){
          af.add(new Arg(a, f));
        }
      }
    }
    for(final Method m:methods){
      if(m.getParameterTypes().length!=1) {
        continue;
      }
      final Option o = m.getAnnotation(Option.class);
      final Arguments a = m.getAnnotation(Arguments.class);
      if(o!=null){
        final Opt oo = new Opt(o, m);
        optionmap.put(o.value(), oo);
        allOptions.add(oo);
        for(final String n:o.alias()){
          optionmap.put(n, oo);
        }
      }
      if(a != null){
        am.add(new Arg(a, m));
      }
    }
    Collections.sort(allOptions, new Comparator<Opt>(){
      @Override
      public int compare(final Opt o1 ,final Opt o2){
        return o1.name.compareTo(o2.name);
      }});
    argumentslist.addAll(am);
    argumentslist.addAll(af);

  }


  String getDescription(){
    return description;
  }
  private int getOptNameLength(){
    int max = 0;
    for(final Opt o:allOptions){
      max = Math.max(max,o.name.length());
      for(final String s:o.o.alias()){
        max = Math.max(max,s.length());
      }
    }
    return max;
  }
  private int getArgNameLength(){
    int max = 0;
    for(final Arg a:argumentslist){
      max = Math.max(max,a.a.value().length());
    }
    return max;
  }


  void optionString(final StringBuilder sb,final int indent){
    final int namelen = getOptNameLength();
    final String nl = System.getProperty("line.separator");
    for(final Opt o:allOptions){
      int last = 0;
      {
        final String s =o.name;
        final int sp = indent;
        for(int i=0;i<sp;i++) {
          sb.append(' ');
        }
        sb.append(s);
        last = s.length();

      }
      for(final String s:o.o.alias()){
        sb.append(nl);
        final int sp = indent;
        for(int i=0;i<sp;i++) {
          sb.append(' ');
        }
        sb.append(s);
        last = s.length();

      }
      for(int i=0;i<namelen-last;i++) {
        sb.append(' ');
      }
      sb.append(':');
      if(o.o.description().isEmpty()){
        sb.append(nl);sb.append(nl);
        continue;
      }
      @SuppressWarnings("resource")
      final
      Scanner s = new Scanner(o.o.description());
      boolean first = true;
      while(s.hasNext()){
        if(first){
          first = false;
        }else{
          for(int i=0;i<indent+namelen+1;i++){
            sb.append(' ');
          }
        }
        sb.append(s.nextLine());
        sb.append(nl);
      }
      sb.append(nl);
    }
  }

  boolean argString(final StringBuilder sb,final int indent){
    boolean write=false;
    final int namelen = getArgNameLength();
    final String nl = System.getProperty("line.separator");
    for(final Arg a:argumentslist){
      if(!a.a.description().isEmpty()){
        final String name = a.a.value();
        final String des = a.a.description();
        for(int i=0;i<indent;i++) {
          sb.append(' ');
        }
        sb.append(name);
        final int nlen = name.length();
        for(int i=0;i<namelen-nlen;i++) {
          sb.append(' ');
        }
        sb.append(':');
        @SuppressWarnings("resource")
        final
        Scanner s = new Scanner(des);
        boolean first = true;
        while(s.hasNext()){
          if(first){
            first = false;
          }else{
            for(int i=0;i<indent+namelen+1;i++){
              sb.append(' ');
            }
          }
          sb.append(s.nextLine());
          sb.append(nl);
        }
        sb.append(nl);
        write = true;
      }
    }
    return write;
  }


  Opt findOption(final String name){
    return optionmap.get(name);
  }

  @SuppressWarnings("unchecked")
  void setBooleanOption(final Opt o)
      throws ReflectionException, ConvertException{
    if(!o.booleanType){
      throw new ConvertException(o.name, "not a boolean property", null);
    }
    try{
      if(o.m!=null){
        o.m.invoke(t, true);
      }else{
        if(o.isCollection){
          Collection<Boolean> object = (Collection<Boolean>) o.f.get(t);
          if(object == null){
            if(!o.arraylistOk){
              throw new NullPointerException(o.f.getName()+" is null collection.");
            }
            object = new ArrayList<Boolean>();
            o.f.set(t,object);
          }
          object.add(true);
        }else{
          o.f.set(t, true);
        }
      }
    }catch (final IllegalAccessException e) {
      throw new ReflectionException(o.name,e.getMessage(), e);
    } catch (final InvocationTargetException e) {
      throw new ReflectionException(o.name, e.getMessage(), e);
    }
  }


  @SuppressWarnings({ "rawtypes", "unchecked" })
  void setOption(final Opt o,final String value)
      throws ReflectionException, ConvertException{
    try{
      if(o.booleanType){
        setBooleanOption(o);
      }
      final Object obj = createType(o.type, value);
      if(o.m!=null){
        if(o.once && o.refCount!=0){
          throw new MultiInputArgumentException(o.name);
        }
        o.m.invoke(t, obj);
        o.refCount++;
      }else{
        if(o.isCollection){
          Collection object = (Collection) o.f.get(t);
          if(object == null){
            if(!o.arraylistOk){
              throw new NullPointerException(o.f.getName()+" is null collection.");
            }
            object = new ArrayList();
            o.f.set(t,object);
          }
          object.add(obj);
        }else{
          if(o.once && o.refCount!=0){
            throw new MultiInputArgumentException(o.name);
          }
          o.f.set(t, obj);
          o.refCount++;
        }
      }
    }catch (final IllegalAccessException e) {
      throw new ReflectionException(o.name,e.getMessage(), e);
    } catch (final InvocationTargetException e) {
      throw new ReflectionException(o.name, e.getMessage(), e);
    }catch(final RuntimeException e){
      throw new ConvertException(o.name, e.getMessage(), e);
    }
  }


  @SuppressWarnings({ "rawtypes", "unchecked" })
  boolean setArgument(final String v)
      throws ReflectionException, ConvertException{
    try{
      for(final Arg a:argumentslist){
        if((a.ignore!=null && a.ignore.matcher(v).find())||
            (a.match!=null && !a.match.matcher(v).find())) {
          continue;
        }
        if(a.isCollection){
          Collection object = (Collection) a.f.get(t);
          if(object == null){
            if(!a.arraylistOk){
              continue;
            }
            object = new ArrayList();
            a.f.set(t,object);
          }
          object.add(createType(a.type, v));
          return true;
        }else if(a.m!=null){
          a.m.invoke(t, createType(a.type,v));
          return true;
        }else if(!a.isSingleOption() || a.refCount!=0){
          a.f.set(t, createType(a.type,v));
          return true;
        }
      }
      return false;
    }catch (final IllegalAccessException e) {
      throw new ReflectionException("arguments",e.getMessage(), e);
    } catch (final InvocationTargetException e) {
      throw new ReflectionException("arguments",e.getMessage(), e);
    }catch(final RuntimeException e){
      throw new ConvertException("arguments", e.getMessage(), e);
    }
  }

  private void clear(){
    for(final Base b:argumentslist){
      b.clear();
    }
    for(final String s:optionmap.keySet()){
      optionmap.get(s).clear();
    }
  }

  T getT(){
    return t;
  }
  void setT(final T t){
    this.t = t;
    clear();
  }
  void newInstance()
      throws ReflectionException{
    try {
      t = claz.newInstance();
    } catch (final InstantiationException e) {
      throw new ReflectionException("newInstance",e.getMessage(), e);
    } catch (final IllegalAccessException e) {
      throw new ReflectionException("newInstance",e.getMessage(), e);
    }
    clear();
  }

}
