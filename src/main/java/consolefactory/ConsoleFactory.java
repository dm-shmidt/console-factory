package consolefactory;

import com.fasterxml.jackson.core.type.TypeReference;
import consolefactory.exception.OptionException;
import consolefactory.exception.ParseException;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Consumer;
import lombok.Setter;

@Setter
public class ConsoleFactory {

  public ConsoleFactory() throws OptionException {
    FACTORY_CONFIG.addOption(Option.builder()
        .name("help")
        .aliases(Set.of("-h", "--h"))
        .type(new TypeReference<String>() {})
        .build());
  }

  private Consumer<LinkedHashMap<Option, Object>> function;
  private static final FactoryConfig FACTORY_CONFIG = FactoryConfig.getInstance();
  private static final Parser PARSER = Parser.getInstance();

  public void run(String optionsString) throws Exception {
    final var options = PARSER.parse(optionsString);
    // TODO: run help if the first arg is -h or --h
    function.accept(options);
  }

  public void addArgument(Option option) throws OptionException {
    FACTORY_CONFIG.addOption(option);
  }

  public void addPrefix(String prefix) throws OptionException {
    FACTORY_CONFIG.addPrefix(prefix);
  }

  public static void main(String[] args) throws OptionException, ParseException {

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
