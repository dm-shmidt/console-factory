package consolefactory;

import com.fasterxml.jackson.core.type.TypeReference;
import consolefactory.exception.OptionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FactoryConfig {

  private static FactoryConfig INSTANCE;
  private final Map<TypeReference<?>, Function<String, Object>> customParsers = new HashMap<>();
  private final List<Option> options = new ArrayList<>();
  private Set<String> prefixes = new HashSet<>();
  private final static short MAX_PREFIX_LENGTH = 2;
  public static final String HELP_OPTION_NAME = "help";
  private final Set<Character> forbiddenCharsInPrefixes =
      Set.of('"', '\'', '[', '{', ']', '}', '(', ')');
  private final Set<Character> openingChars = Set.of('"', '{', '[', '(');
  private final Set<Character> closingChars = Set.of('"', '}', ']', ')');

  private FactoryConfig() {
    prefixes.add("");
    prefixes.add("-");
    prefixes.add("--");
  }

  protected static FactoryConfig getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new FactoryConfig();
    }
    return INSTANCE;
  }

  protected void addOption(Option option) throws OptionException {
    validateArgument(option);
    options.add(option);
  }

  protected void clearOptions() {
    options.clear();
  }

  protected void addPrefix(String prefix) throws OptionException {
    if (prefix.length() > MAX_PREFIX_LENGTH) {
      throw new OptionException("Prefix length must be lesser than " + MAX_PREFIX_LENGTH);
    }
    for (int i = 0; i < prefix.length(); i++) {
      if (Character.isLetterOrDigit(prefix.charAt(0))) {
        throw new OptionException("Prefix must consist of non-alphanumeric symbols");
      }
      if (forbiddenCharsInPrefixes.contains(prefix.charAt(i))) {
        throw new OptionException("Prefix must not contain symbols: " +
            forbiddenCharsInPrefixes.stream()
                .map(Object::toString).collect(Collectors.joining(",")));
      }
    }
  }

  protected void addParser(TypeReference<?> type, Function<String, Object> parser) {
    customParsers.put(type, parser);
  }

  protected Function<String, Object> getParser(TypeReference<?> type) {
    return customParsers.get(type);
  }

  protected Option getOptionByAlias(String alias) {
    if (alias == null || alias.isBlank()) {
      return null;
    }
    return options.stream()
        .filter(option -> option.hasAlias(alias))
        .findFirst()
        .orElse(null);
  }

  protected boolean existsOption(String alias) {
    return options.stream().anyMatch(option -> option.getAliases().contains(alias));
  }

  /**
   * Validates new Argument against existing ones. New Argument has to have unique name and
   * aliases!!!
   *
   * @param option - an Argument to be added to ARGUMENTS list.
   */
  private void validateArgument(Option option) throws OptionException {
    validateAliasPrefixes(option);
    if (options.stream().anyMatch(a -> a.getName().equals(option.getName())
        || a.getAliases().stream().anyMatch(alias -> option.getAliases().contains(alias)))) {
      throw new OptionException(
          "An argument with already existing name and/or aliases could not be added: " + option);
    }
  }

  private void validateAliasPrefixes(Option option) throws OptionException {
    if (option.getAliases().stream().anyMatch(alias -> !prefixes.contains(getPrefix(alias)))) {
      throw new OptionException("All aliases must start of predefined prefixes");
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
