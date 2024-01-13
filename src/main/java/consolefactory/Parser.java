package consolefactory;

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

final class Parser {

  private static Parser INSTANCE;
  private final FactoryConfig factoryConfig = FactoryConfig.getInstance();
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private Parser(String dateFormat) {
    MAPPER.setDateFormat(new SimpleDateFormat(dateFormat));
  }

  private Parser() {
  }

  public static Parser getInstance(String dateFormat) {
    if (INSTANCE == null) {
      INSTANCE = new Parser(dateFormat);
    }
    return INSTANCE;
  }

  public static Parser getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new Parser();
    }
    return INSTANCE;
  }

  public void setDateFormat(String dateFormat) {
    MAPPER.setDateFormat(new SimpleDateFormat(dateFormat));
  }

  public Object deserialize(String s, Option option) throws Exception {
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

  public LinkedHashMap<Option, Object> parse(String input) throws Exception {
    final Queue<String> tokensQueue = Pattern.compile("\\S*\\S")
        .matcher(input)
        .results()
        .map(MatchResult::group)
        .collect(Collectors.toCollection(LinkedList::new));

    if (factoryConfig.getOptions().stream()
        .noneMatch(option -> option.hasAlias(tokensQueue.peek()))) {
      throw new ParseException(
          "Arguments' string must start with a predefined option (key), but not a value.");
    }

    final var result = new LinkedHashMap<Option, Object>();

    while (!tokensQueue.isEmpty()) {
      final var option = parseOption(tokensQueue, result);
      if (option == null) {
        break;
      }
      final var value = deserialize(parseValue(tokensQueue, option), option);
      if (option.isUnique() && result.containsKey(option)) {
        throw new ParseException("Option " + option.getName() + " must be unique.");
      }
      result.put(option, value);
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
  private Option parseOption(Queue<String> tokensQueue,
      LinkedHashMap<Option, Object> result) throws ParseException {
    while (!tokensQueue.isEmpty()) {
      final var currentToken = tokensQueue.poll();
      var option = factoryConfig.getArgumentByAlias(currentToken);
      if (option == null) {
        throw new ParseException("Not an option: " + currentToken);
      }
      final var nextToken = tokensQueue.peek();
      final var nextPossibleOption = factoryConfig.getArgumentByAlias(nextToken);
      if (nextPossibleOption != null && option.hasDefaultValue()) {
        result.put(option, option.getDefaultValue());
      } else if (nextPossibleOption != null || tokensQueue.isEmpty()) {
        throw new ParseException("Value is not provided for the option " + currentToken);
      } else {
        return option;
      }
    }
    return null;
  }

  private String parseValue(Queue<String> tokensQueue, Option option) throws ParseException {
    if (option == null) {
      throw new ParseException("Option can not be null");
    }

    final var value = new StringBuilder();

    var token = tokensQueue.poll();
    if (Utils.isPrimitiveType(option.getType())) {
      return token;
    }
    if (!isOpeningToken(value.toString())) {
      throw new ParseException("A value for option " + option.getName() + " must start of {,[,\" ");
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
    switch (token.charAt(0)) {
      case '[':
      case '{':
      case '"':
        return true;
    }
    return false;
  }

  private boolean isClosingToken(String token) {
    if (token == null || token.isEmpty()) {
      return false;
    }
    switch (token.charAt(token.length() - 1)) {
      case ']':
      case '}':
      case '"':
        return true;
    }
    return false;
  }

  /**
   * Check for mandatory and unique options in the result.
   *
   * @param result - parsed map
   */
  private void validateMandatoryOptions(LinkedHashMap<Option, Object> result) throws ParseException {
    final var mandatoryInResultList = result.keySet().stream()
        .filter(Option::isMandatory)
        .collect(Collectors.toList());
    final var absentMandatory = factoryConfig.getOptions().stream()
        .filter(o -> !mandatoryInResultList.contains(o))
        .map(Option::getName)
        .collect(Collectors.joining(", "));
    if (!absentMandatory.isEmpty()) {
      throw new ParseException("Not all of mandatory options provided: " + absentMandatory);
    }
  }
}

