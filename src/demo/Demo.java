

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

import nodamushi.args.Arguments;
import nodamushi.args.ConvertException;
import nodamushi.args.Description;
import nodamushi.args.NArgument;
import nodamushi.args.Option;
import nodamushi.args.ReflectionException;

public class Demo{
  public static void main(String[] args){
    final NArgument<Args> nArgument = new NArgument<Args>(new Args());
    if(args.length==0){
      args = new String[]{
          //option a
          "-a","a1","-a","a2","-a","a3",
          //option b
          "--bbb",
          //option c & d
          "-cd",
          //option e
          "--encoding","utf-8",
          //option i
          "-i","-100",
          //option h
          "--help",
          //option n
          "-n","10.5",
          //arguments
          "test/test1.txt","img/img.jpg","test/test2.txt"
      };
    }
    nArgument.setIgnoreConvertException(false);
    try {
      nArgument.addArguments(args);
    } catch (final ConvertException e) {
      //setIgnoreConvertException(false);をしていないと、エラーは発生しない
      System.out.println("Parse error:"+e.getMessage());
      System.out.println(nArgument.getDescription());
      return;
    } catch (final ReflectionException e) {
      e.printStackTrace();
      return;
    }

    final Args a = nArgument.get();

    System.out.println(a);
    if(a.help){
      System.out.println();
      System.out.println(nArgument.getDescription());
    }

  }
}

@Description(
    "demo arguments.\n"
    //TODO ↓これも自動生成したい
    + "[-a String]* [(-b|--bbb|--bbbb)] [-c] [-d] [(-e|--encoding) File Encoding] "
    + "[-i Integer] [(-h|--help)] [-n Number] [InputFiles]+")
class Args{
  @Option(value="-a",description="multi string option")
  private List<String> strs;

  @Option(value="-b",alias={"--bbb","--bbbb"},description="flag option")
  private boolean flagB;

  @Option("-c")
  private boolean flagC;

  @Option("-d")
  private boolean flagD;

  @Option(value="-e",alias="--encoding",description="text file charset")
  private Charset charset;

  @Option(value="-i")
  private String integer;
  //setterがある場合はsetterを優先的に使う。たとえprivateであっても。
  //従って、integerはStringであるが、setterがintになっているので、int型のOptionと判定される
  private void setInteger(final int i){
    integer = "input integer value is '"+String.valueOf(i)+"'";
  }

  //@Optionはmethodにも付加可能
  public boolean help;
  @Option(value="-h",alias="--help",description="show this help")
  private void setHelp(final boolean b){
    help = b;
  }

  @Option("-n")
  private double number;

  @Arguments(match="\\.txt$")
  private List<Path> inputTextFiles;

  //inputTextFilesよりも先にinputFilesを書くと、
  //このArgumentsはすべての文字列にマッチするので、
  //inputTextFilesが空になってしまうことに注意
  @Arguments
  private List<File> inputFiles;


  @Override
  public String toString(){
    final StringBuilder sb=new StringBuilder();
    sb.append("Options\n");
    sb.append("-a = ").append(strs).append("\n");
    sb.append("-b = ").append(flagB).append("\n");
    sb.append("-c = ").append(flagC).append("\n");
    sb.append("-d = ").append(flagD).append("\n");
    sb.append("-e = ").append(charset).append("\n");
    sb.append("-i = ").append(integer).append("\n");
    sb.append("-h = ").append(help).append("\n");
    sb.append("-n = ").append(number).append("\n\n");
    sb.append("Arguments\n");
    sb.append("text files = ").append(inputTextFiles).append("\n");
    sb.append("else files = ").append(inputFiles);
    return sb.toString();
  }

}
