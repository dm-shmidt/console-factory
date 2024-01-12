import static java.lang.Boolean.FALSE;

import argument.Argument;
import argument.ArgumentValue;
import exception.ArgumentException;
import exception.ParseException;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Setter;

@Setter
public class ConsoleFactory {

  public ConsoleFactory() throws ArgumentException {
    FACTORY_CONFIG.addArgument(ArgumentValue.builder()
        .name("help")
        .aliases(Set.of("-h", "--h"))
        .type(String.class)
        .build());
  }

  private Consumer<List<Argument>> function;
  private static final FactoryConfig FACTORY_CONFIG = FactoryConfig.getInstance();
  private static final Parser PARSER = Parser.getInstance();

  public void run(String argumentsString) throws ParseException {
    final var arguments = PARSER.parse(argumentsString);
    // TODO: run help if the first arg is -h or --h
    function.accept(arguments);
  }

  public void addArgument(Argument argument) throws ArgumentException {
    FACTORY_CONFIG.addArgument(argument);
  }

  public void addPrefix(String prefix) throws ArgumentException {
    FACTORY_CONFIG.addPrefix(prefix);
  }

  public static void main(String[] args) throws ArgumentException, ParseException {

    final var tokens = Pattern.compile("\\S*\\S")
        .matcher("-asdf dsdf a [sdf,asdf]")
        .results()
        .map(Object::toString)
        .collect(Collectors.toList());

    final var consoleFactory = new ConsoleFactory();
    consoleFactory.addArgument(
        ArgumentValue.builder()
            .name("a")
            .aliases(Set.of("-a"))
            .type(Integer.class)
            .build());
    consoleFactory.addArgument(
        ArgumentValue.builder()
            .name("b")
            .aliases(Set.of("-b"))
            .type(Integer.class)
            .build());
    consoleFactory.addArgument(
        ArgumentValue.builder()
            .name("check")
            .type(Boolean.class)
            .defaultValue(FALSE)
            .build());

    consoleFactory.run("-a 123 --b 100 check");

//    (Class<?>) ((ParameterizedType) getClass()
//        .getGenericSuperclass()).getActualTypeArguments()[0]
//    final var list = new ArrayList<Argument>();
//    final var om = new ObjectMapper();
//    final var json = "{\"str1\":\"654\",\"str2\": \"asdfasdf\"}";
//    final var arg = ArgumentValue.builder()
//        .value(new Value(json, SomeClass.class, str -> {
//          try {
//            return om.readValue(str, SomeClass.class);
//          } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//          }
//        }, null))
//        .build();
//    list.add(arg);

//    System.out.println(((SomeClass)list.get(0).getValue()).getStr());
//    System.out.println(((Value)list.get(0).getValue()).getValue());
  }
}
