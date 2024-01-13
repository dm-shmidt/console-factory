package consolefactory;

import com.fasterxml.jackson.core.type.TypeReference;
import consolefactory.exception.ArgumentException;
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
final class FactoryConfig {

  private static FactoryConfig INSTANCE;
  private final Map<TypeReference<?>, Function<String, Object>> customParsers = new HashMap<>();
  private final List<Option> options = new ArrayList<>();
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

  public void addArgument(Option option) throws ArgumentException {
    validateArgument(option);
    options.add(option);
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

  public void addParser(TypeReference<?> type, Function<String, Object> parser) {
    customParsers.put(type, parser);
  }

  public Function<String, Object> getParser(TypeReference<?> type) {
    return customParsers.get(type);
  }

  public Option getArgumentByAlias(String alias) {
    if (alias == null || alias.isBlank()) {
      return null;
    }
    return options.stream()
        .filter(option -> option.hasAlias(alias))
        .findFirst()
        .orElse(null);
  }

  public boolean existsArgument(String alias) {
    return options.stream().anyMatch(option -> option.getAliases().contains(alias));
  }

  /**
   * Validates new Argument against existing ones. New Argument has to have unique name and
   * aliases!!!
   *
   * @param option - an Argument to be added to ARGUMENTS list.
   */
  private void validateArgument(Option option) throws ArgumentException {
    validateAliasPrefixes(option);
    if (options.stream().anyMatch(a -> a.getName().equals(option.getName())
        || a.getAliases().stream().anyMatch(alias -> option.getAliases().contains(alias)))) {
      throw new ArgumentException(
          "An argument with already existing name and/or aliases could not be added: " + option);
    }
  }

  private void validateAliasPrefixes(Option option) throws ArgumentException {
    if (option.getAliases().stream().anyMatch(alias -> !prefixes.contains(getPrefix(alias)))) {
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
