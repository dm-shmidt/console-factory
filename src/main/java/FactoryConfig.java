import argument.Argument;
import exception.ArgumentException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class FactoryConfig {

  private static FactoryConfig INSTANCE;
  private final Map<Class<?>, Function<String, Object>> customParsers = new HashMap<>();
  private final List<Argument> arguments = new ArrayList<>();
  private Set<String> prefixes = new HashSet<>();
  private final static short MAX_PREFIX_LENGTH = 2;

  private FactoryConfig() {
    prefixes.add("");
    prefixes.add("-");
    prefixes.add("--");
  }

  public static FactoryConfig getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new FactoryConfig();
    }
    return INSTANCE;
  }

  public void addArgument(Argument argument) throws ArgumentException {
    validateArgument(argument);
    arguments.add(argument);
  }

  public void addPrefix(String prefix) throws ArgumentException {
    if (prefix.length() > MAX_PREFIX_LENGTH) {
      throw new ArgumentException("Prefix length must be lesser than " + MAX_PREFIX_LENGTH);
    }
    for (int i = 0; i < prefix.length(); i++) {
      if (Character.isLetterOrDigit(prefix.charAt(0))) {
        throw new ArgumentException("Prefix must consist of non-alphanumeric symbols");
      }
      switch (prefix.charAt(i)) {
        case '[':
        case '{':
        case ']':
        case '}': throw new ArgumentException("Prefix must not contain symbols {, [, ], }");
      }
    }

  }

  public void addParser(Class<?> classType, Function<String, Object> parser) {
    customParsers.put(classType, parser);
  }

  public Function<String, Object> getParser(Class<?> type) {
    return customParsers.get(type);
  }

  public Argument getArgumentByAlias(String alias) {
    return arguments.stream()
        .filter(argument -> argument.getAliases().contains(alias))
        .findFirst()
        .orElse(null);
  }

  public boolean existsArgument(String alias) {
    return arguments.stream().anyMatch(argument -> argument.getAliases().contains(alias));
  }

  /**
   * Validates new Argument against existing ones. New Argument has to have unique name and
   * aliases!!!
   *
   * @param argument - an Argument to be added to ARGUMENTS list.
   */
  private void validateArgument(Argument argument) throws ArgumentException {
    validateAliasPrefixes(argument);
    if (arguments.stream().anyMatch(a -> a.getName().equals(argument.getName())
        || a.getAliases().stream().anyMatch(alias -> argument.getAliases().contains(alias)))) {
      throw new ArgumentException(
          "An argument with already existing name and/or aliases could not be added: " + argument);
    }
  }

  private void validateAliasPrefixes(Argument argument) throws ArgumentException {
    if (argument.getAliases().stream().anyMatch(alias -> !prefixes.contains(getPrefix(alias)))) {
      throw new ArgumentException("All aliases must start of predefined prefixes");
    }
  }

  private String getPrefix(String alias) {
    final var prefix = new StringBuilder();
    int i = 0;
    while (!Character.isLetterOrDigit(alias.charAt(i))) {
      prefix.append(alias.charAt(i++));
    }
    return prefix.toString();
  }
}
