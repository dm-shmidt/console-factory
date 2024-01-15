package consolefactory;

import static consolefactory.FactoryConfig.HELP_OPTION_NAME;

import com.fasterxml.jackson.core.type.TypeReference;
import consolefactory.exception.OptionException;
import consolefactory.exception.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Setter
public class ConsoleFactory {

  private Runnable function;
  private final FactoryConfig factoryConfig = FactoryConfig.getInstance();
  private final Parser parser = Parser.getInstance();
  @Getter
  private LinkedHashMap<Option, Object> result = new LinkedHashMap<>();

  private void init() throws OptionException {
    if (factoryConfig.existsOptionByName(HELP_OPTION_NAME)) {
      return;
    }
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
      init();
      if (input == null || input.isEmpty() || input.isBlank()) {
        throw new ParseException("Input is not provided");
      }
      result = parser.parse(input);
      if (!checkAndPrintHelpInfo(result) && function != null) {
        function.run();
      }
      result.clear();
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  public void addOption(Option option) throws OptionException {
    factoryConfig.addOption(option);
  }

  public void clearOptions() {
    factoryConfig.clearOptions();
  }

  public List<Option> getOptionsByName(String name) {
    final var list = result.keySet().stream().filter(option -> option.getName().equals(name))
        .collect(Collectors.toList());
    if (list.isEmpty()) {
      System.out.println("No options with name " + name + " found.");
      return null;
    }
    return list;
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> getValuesByOptionName(String name, Class<T> v) {
    final var optionType = getOptionsByName(name).get(0).getType().getType();
    if (!optionType.equals(v)) {
      System.out.println(
          "Cannot cast " + optionType.getTypeName() + " to " + v.getTypeName());
      return null;
    }
    return result.entrySet().stream()
        .filter(entry -> entry.getKey().getName().equals(name))
        .map(entry -> (T) entry.getValue())
        .collect(Collectors.toList());
  }

  public void addPrefix(String prefix) throws OptionException {
    factoryConfig.addPrefix(prefix);
  }

  private boolean checkAndPrintHelpInfo(LinkedHashMap<Option, Object> options) {
    if (options.keySet().stream().noneMatch(o -> o.getName().equals(HELP_OPTION_NAME))) {
      return false;
    }
    if (options.size() > 2) {
      System.out.println("Wrong use of option -h or --h. Run with only -h (or --h) for more info "
          + "or -h <option> for help about particular option.");
      return true;
    }
    System.out.println("Help info: ");
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

}
