package consolefactory;

import static consolefactory.FactoryConfig.HELP_OPTION_NAME;

import com.fasterxml.jackson.core.type.TypeReference;
import consolefactory.exception.OptionException;
import consolefactory.exception.ParseException;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Setter;

@Setter
public class ConsoleFactory {

  private Consumer<LinkedHashMap<Option, Object>> function;
  private final FactoryConfig factoryConfig = FactoryConfig.getInstance();
  private final Parser parser = Parser.getInstance();

  public ConsoleFactory() throws OptionException {
    factoryConfig.addOption(Option.builder()
        .name(HELP_OPTION_NAME)
        .aliases(Set.of("-h", "--h"))
        .defaultValue("")
        .helpInfo("help info.")
        .type(new TypeReference<String>() {
        })
        .build());
  }

  public void run(String input) {
    try {
      if (input == null || input.isEmpty() || input.isBlank()) {
        throw new ParseException("Input is not provided");
      }
      final var options = parser.parse(input);
      if (!checkAndPrintHelpInfo(options)) {
        function.accept(options);
      }
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  public void addOption(Option option) throws OptionException {
    factoryConfig.addOption(option);
  }

  public void addPrefix(String prefix) throws OptionException {
    factoryConfig.addPrefix(prefix);
  }

  private boolean checkAndPrintHelpInfo(LinkedHashMap<Option, Object> options)
      throws OptionException {
    if (options.keySet().stream().noneMatch(o -> o.getName().equals(HELP_OPTION_NAME))) {
      return false;
    }
    if (options.size() > 2) {
      System.out.println("Wrong use of option -h or --h. Run with only -h (or --h) for more info "
          + "or -h <option> for help about particular option.");
      return true;
    }
    if (options.size() == 1) {
      final var helpInfo = factoryConfig.getOptions().stream()
          .map(Option::getHelpInfo)
          .collect(Collectors.joining("\n"));
      if (helpInfo.equals("null")) {
        System.out.println("No options are configured.");
        return true;
      }
      System.out.println(helpInfo);
    } else {
      options.keySet().stream().filter(option -> !option.getName().equals(HELP_OPTION_NAME))
          .findFirst().ifPresent(option -> System.out.println(option.getHelpInfo()));
    }
    return true;
  }

  public static void main(String[] args) throws OptionException, ParseException {

    final var consoleFactory = new ConsoleFactory();
    consoleFactory.addOption(
        Option.builder()
            .name("a")
            .aliases(Set.of("-a"))
            .helpInfo("integer value")
            .type(new TypeReference<Integer>() {
            })
            .build());

    consoleFactory.addOption(
        Option.builder()
            .name("b")
            .aliases(Set.of("-b", "--b"))
            .helpInfo("integer value")
            .type(new TypeReference<Integer>() {
            })
            .build());

    consoleFactory.addOption(
        Option.builder()
            .name("func")
            .aliases(Set.of("PLUS", "MINUS"))
            .helpInfo("function")
            .type(new TypeReference<MathOperation>() {
            })
            .build());

    consoleFactory.setFunction(options -> {
      final var list = options.entrySet().stream()
          .map(entry -> entry.getKey().getName() + ": " + entry.getValue())
          .collect(Collectors.joining("\n"));
      System.out.println(list);

    });

    consoleFactory.run("-a 123 --b 846 PLUS");
  }

  enum MathOperation {
    PLUS("+"), MINUS("-");
    final String value;

    MathOperation(String s) {
      value = s;
    }

    @Override
    public String toString() {
      return value;
    }
  }
}
