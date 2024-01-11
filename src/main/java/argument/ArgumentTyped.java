package argument;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArgumentTyped<T> implements Argument {

  private String name;
  private String helpInfo;
  private boolean mandatory;
  private T value;
  private T defaultValue;

//  MOVE TO PARSER
//  private Function<String, Object> parse;
//
//  public void setValue(String s) {
//    Optional.ofNullable(parse).ifPresentOrElse(
//        parseFunction -> value = parseFunction.apply(s),
//        () -> value = s);
//  }
}
