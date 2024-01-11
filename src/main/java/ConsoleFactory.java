import argument.Argument;
import argument.ArgumentTyped;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

public class ConsoleFactory {

  private List<Argument> arguments;
  private Runnable function;
  private Parser parser = new Parser();

  public void run(String argumentsString) {

    function.run();
  }

  @Data
  @AllArgsConstructor
  public static class SomeClass {

    private String str;
  }

  public static void main(String[] args) {
    final var list = new ArrayList<Argument>();
    final var arg = ArgumentTyped.builder()
        .value(new SomeClass("hello"))
        .build();
    list.add(arg);
    list.add(ArgumentTyped.builder().value(1).build());

    System.out.println(((SomeClass)list.get(0).getValue()).getStr());
    System.out.println(list.get(1).getValue().getClass().getSimpleName());
  }
}
