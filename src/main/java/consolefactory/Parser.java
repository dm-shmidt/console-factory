package consolefactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import consolefactory.exception.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class Parser {

  private static Parser INSTANCE;
  private final FactoryConfig factoryConfig = FactoryConfig.getInstance();
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private Parser(String dateFormat) {
    MAPPER.setDateFormat(new SimpleDateFormat(dateFormat));
  }

  private Parser() {
  }

  protected static Parser getInstance(String dateFormat) {
    if (INSTANCE == null) {
      INSTANCE = new Parser(dateFormat);
    }
    return INSTANCE;
  }

  protected static Parser getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new Parser();
    }
    return INSTANCE;
  }

  protected void setDateFormat(String dateFormat) {
    MAPPER.setDateFormat(new SimpleDateFormat(dateFormat));
  }

  protected Object deserialize(String s, Option option) throws Exception {
    if (s == null) {
      throw new ParseException("Value can not be null for deserialization.");
    }
    final var customParser = factoryConfig.getParser(option.getType());
    if (customParser != null) {
      return customParser.apply(s);
    }
    try {
      if (ClassUtils.isPrimitiveWrapper(Class.forName(option.getType().getType().getTypeName()))) {
        return MAPPER.convertValue(s, option.getType());
      }
    } catch (ClassNotFoundException | IllegalArgumentException ignored) {
    }
    return MAPPER.readValue(s, option.getType());
  }

  protected LinkedHashMap<Option, Object> parse(String input) throws Exception {
    final Queue<String> tokensQueue = Pattern.compile("\\S*\\S")
        .matcher(input)
        .results()
        .map(MatchResult::group)
        .collect(Collectors.toCollection(LinkedList::new));

    if (factoryConfig.getOptions().stream()
        .noneMatch(option -> option.hasAlias(tokensQueue.peek()))) {
      throw new ParseException(
          "Options' string must start with a predefined option (key), but not a value.");
    }

    final var result = new LinkedHashMap<Option, Object>();

    while (!tokensQueue.isEmpty()) {
      final var optionAndToken = parseOption(tokensQueue, result);
      if (optionAndToken == null || tokensQueue.isEmpty()) {
        break;
      }
      final var value = deserialize(parseValue(tokensQueue, optionAndToken),
          optionAndToken.getKey());
      if (optionAndToken.getKey().isUnique() && result.containsKey(optionAndToken.getKey())) {
        throw new ParseException(
            "Option " + optionAndToken.getKey().getName() + " must be unique.");
      }
      result.put(optionAndToken.getKey(), value);
    }

    validateMandatoryOptions(result);
    return result;
  }

  /**
   * Parse options until a value encountered. And increment the index (parsing position).
   *
   * @param tokensQueue - a queue of tokens
   * @param result      - resulting map of options and their values.
   */
  private Pair<Option, String> parseOption(Queue<String> tokensQueue,
      LinkedHashMap<Option, Object> result) throws ParseException {
    ImmutablePair<Option, String> optionAndToken = null;
    while (!tokensQueue.isEmpty() && optionAndToken == null) {
      final var currentToken = tokensQueue.poll();
      var currentOption = factoryConfig.getOptionByAlias(currentToken);
      if (currentOption == null) {
        throw new ParseException("Not an option: " + currentToken);
      }
      final var nextToken = tokensQueue.peek();
      final var nextPossibleOption = factoryConfig.getOptionByAlias(nextToken);
      if (!Utils.isEnum(currentOption.getType())) {
        if (nextPossibleOption != null || tokensQueue.isEmpty()) {
          if (currentOption.hasDefaultValue()) {
            result.put(currentOption, currentOption.getDefaultValue());
          } else {
            throw new ParseException("Value is not provided for the option " + currentToken);
          }
        }
      }
      if (nextPossibleOption == null || Utils.isEnum(currentOption.getType())) {
        optionAndToken = new ImmutablePair<>(currentOption, currentToken);
      }
    }
    return optionAndToken;
  }

  private String parseValue(Queue<String> tokensQueue, Pair<Option, String> optionAndToken)
      throws ParseException {
    if (tokensQueue.isEmpty()) {
      return null;
    }
    if (optionAndToken == null || optionAndToken.getKey() == null) {
      throw new ParseException("Option can not be null");
    }
    if (Utils.isEnum(optionAndToken.getKey().getType())) {
      return optionAndToken.getValue();
    }

    final var value = new StringBuilder();

    var token = tokensQueue.poll();
    if (Utils.isPrimitiveType(optionAndToken.getKey().getType())) {
      return token;
    }
    if (optionAndToken.getKey().getType().equals(new TypeReference<String>() {
    })) {
      Option nextPossibleOption = null;
      while (!tokensQueue.isEmpty() && nextPossibleOption == null) {
        value.append(tokensQueue.poll());
        nextPossibleOption = factoryConfig.getOptionByAlias(tokensQueue.peek());
      }
      return value.toString();
    }

    if (!isOpeningToken(value.toString())) {
      throw new ParseException(
          "A value for option " + optionAndToken.getKey().getName() + " must start of {,[,\" ");
    }
    while (!tokensQueue.isEmpty() || !isClosingToken(token)) {
      token = tokensQueue.poll();
      value.append(token);
    }
    return value.toString();
  }


  private boolean isOpeningToken(String token) {
    if (token == null || token.isEmpty()) {
      return false;
    }
    return factoryConfig.getOpeningChars().contains(token.charAt(0));
  }

  private boolean isClosingToken(String token) {
    if (token == null || token.isEmpty()) {
      return false;
    }
    return factoryConfig.getClosingChars().contains(token.charAt(token.length() - 1));
  }

  /**
   * Check for mandatory and unique options in the result.
   *
   * @param result - parsed map
   */
  private void validateMandatoryOptions(LinkedHashMap<Option, Object> result)
      throws ParseException {
    final var mandatoryInResultList = result.keySet().stream()
        .filter(Option::isMandatory)
        .collect(Collectors.toList());
    final var absentMandatory = factoryConfig.getOptions().stream()
        .filter(o -> o.isMandatory() && !mandatoryInResultList.contains(o))
        .map(Option::getName)
        .collect(Collectors.joining(", "));
    if (!absentMandatory.isEmpty()) {
      throw new ParseException("Not all of mandatory options provided: " + absentMandatory);
    }
  }
}

