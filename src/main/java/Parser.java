import argument.Argument;
import argument.ArgumentTyped;
import java.util.ArrayList;
import java.util.List;

public class Parser {

  public List<Argument> parse(String s) {
    final var result = new ArrayList<Argument>();

    return result;
  }

  private ArgumentTyped parseOption(String s) {
    return ArgumentTyped.builder()
        .name("")
        .build();
  }

}
