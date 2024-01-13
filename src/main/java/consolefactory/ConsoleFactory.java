package consolefactory;

import com.fasterxml.jackson.core.type.TypeReference;
import consolefactory.exception.ArgumentException;
import consolefactory.exception.ParseException;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Consumer;
import lombok.Setter;

@Setter
public class ConsoleFactory {

  public ConsoleFactory() throws ArgumentException {
    FACTORY_CONFIG.addArgument(Option.builder()
        .name("help")
        .aliases(Set.of("-h", "--h"))
        .type(new TypeReference<String>() {})
        .build());
  }

  private Consumer<LinkedHashMap<Option, Object>> function;
  private static final FactoryConfig FACTORY_CONFIG = FactoryConfig.getInstance();
  private static final Parser PARSER = Parser.getInstance();

  public void run(String argumentsString) throws Exception {
    final var arguments = PARSER.parse(argumentsString);
    // TODO: run help if the first arg is -h or --h
    function.accept(arguments);
  }

  public void addArgument(Option option) throws ArgumentException {
    FACTORY_CONFIG.addArgument(option);
  }

  public void addPrefix(String prefix) throws ArgumentException {
    FACTORY_CONFIG.addPrefix(prefix);
  }

  public static void main(String[] args) throws ArgumentException, ParseException {

//    final var consoleFactory = new console_factory.ConsoleFactory();
//    consoleFactory.addArgument(
//        ArgumentValue.builder()
//            .name("a")
//            .aliases(Set.of("-a"))
//            .type(Integer.class)
//            .build());
//    consoleFactory.addArgument(
//        ArgumentValue.builder()
//            .name("b")
//            .aliases(Set.of("-b"))
//            .type(Integer.class)
//            .build());
//    consoleFactory.addArgument(
//        ArgumentValue.builder()
//            .name("check")
//            .type(Boolean.class)
//            .defaultValue(FALSE)
//            .build());
//
//    consoleFactory.run("-a 123 --b 100 check");
  }

  private static void f(int i) {
    i++;
    i++;
    System.out.println(i);
  }
}
