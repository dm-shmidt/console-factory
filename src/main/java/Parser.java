import argument.Argument;
import argument.ArgumentValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import exception.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ClassUtils;

public final class Parser {

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

  public void setArgumentValue(String s, Argument argument) throws Exception {
    final var customParser = factoryConfig.getParser(argument.getType());
    if (customParser != null) {
      argument.setValue(customParser.apply(s));
      return;
    }
    if (ClassUtils.isPrimitiveWrapper(argument.getType())) {
      argument.setValue(MAPPER.convertValue(s, argument.getType()));
    }
    argument.setValue(MAPPER.readValue(s, argument.getType()));
  }

  public List<Argument> parse(String input) throws ParseException {
    final var tokens = Pattern.compile("\\S*\\S")
        .matcher(input)
        .results()
        .map(MatchResult::group)
        .collect(Collectors.toList());

    if (factoryConfig.getArguments().stream()
        .anyMatch(a -> a.getAliases().contains(tokens.get(0)))) {
      throw new ParseException(
          "Arguments' string must start with a predefined option (key), but not a value.");
    }

    final var result = new ArrayList<Argument>();

    int i = 0;
    var valueFlag = false;
    while (i < tokens.size()) {

      // parsing options until value found
      while (!valueFlag && i < tokens.size()) {
        var argument1 = factoryConfig.getArgumentByAlias(tokens.get(i++)).copy();
        if (argument1 == null) {
          throw new ParseException("Not an option: " + tokens.get(i - 1));
        }
        if (factoryConfig.existsArgument(tokens.get(i++)) && argument1.hasDefaultValue()) {
          argument1.setValue(argument1.getDefaultValue());
          continue;
        }
        valueFlag = true;
      }

      // parsing value
    }

    // TODO: check mandatory and unique
    return result;
  }

//  private List<String> getTokens(String input) {
//    final var tokens = new ArrayList<String>();
//    var tokenFlag = true;
//    final var array = input.toCharArray();
//    int i = 0;
//    while (i < array.length) {
//      var token = new StringBuilder();
//      while (tokenFlag && i < array.length-1) {
//        token.append(array[i++]);
//        if (array[i] == ' ') {
//          tokenFlag = false;
//          tokens.add(token.toString());
//        }
//      }
//    }
//
//    return tokens;
//  }

  private ArgumentValue parseOption(String s) {
    return ArgumentValue.builder()
        .name("")
        .build();
  }

  private boolean isOpening(String token) {
    switch (token.charAt(0)) {
      case '[':
      case '{':
        return true;
    }
    return false;
  }

  private boolean isClosing(String token) {
    switch (token.charAt(token.length() - 1)) {
      case ']':
      case '}':
        return true;
    }
    return false;
  }
}
