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
    return result.keySet().stream().filter(option -> option.getName().equals(name))
        .collect(Collectors.toList());
  }

  public Option getOptionByName(String name) {
    final var list = getOptionsByName(name);
    return getListFirstElement(name, list, "options");
  }

  public <T> List<T> getValuesByOptionName(String name, Class<T> classType) {
    final var optionType = getOptionsByName(name).get(0).getType().getType();
    if (!optionType.equals(classType)) {
      System.out.println(
          "Cannot cast " + optionType.getTypeName() + " to " + classType.getTypeName());
      return null;
    }
    return getValuesByOptionName(name);
  }

  public <T> T getValueByOptionName(String name, Class<T> classType) {
    final var list = getValuesByOptionName(name, classType);
    return getListFirstElement(name, list, "values");
  }

  public <T> List<T> getValuesByOptionName(String name, TypeReference<T> typeRef) {
    final var optionType = getOptionsByName(name).get(0).getType();
    if (!typeRef.getType().equals(optionType.getType())) {
      System.out.println(
          "Cannot cast " + optionType + " to " + typeRef);
      return null;
    }
    return getValuesByOptionName(name);
  }

  public <T> T getValueByOptionName(String name, TypeReference<T> typeRef) {
    final var list = getValuesByOptionName(name, typeRef);
    return getListFirstElement(name, list, "values");
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> getValuesByOptionName(String name) {
    return result.entrySet().stream()
        .filter(entry -> entry.getKey().getName().equals(name))
        .map(entry -> (T) entry.getValue())
        .collect(Collectors.toList());
  }


  private <T> T getListFirstElement(String name, List<T> list, String entitiesName) {
    if (list.isEmpty()) {
      System.out.println("Error: No " + entitiesName + " for option " + name + " found.");
      return null;
    }
    return list.get(0);
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
