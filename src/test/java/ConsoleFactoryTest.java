import com.fasterxml.jackson.core.type.TypeReference;
import consolefactory.ConsoleFactory;
import consolefactory.Option;
import consolefactory.exception.OptionException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ConsoleFactoryTest {

  final static ConsoleFactory CONSOLE_FACTORY = new ConsoleFactory();

  @BeforeAll
  static void setUp() throws OptionException {
    addOptionsToConfiguration();
  }

  @Test
  void testHelp() {
    CONSOLE_FACTORY.run("--h");
  }

  @Test
  void testPrintAll() {
    CONSOLE_FACTORY.setFunction(ConsoleFactoryTest::printAll);
    CONSOLE_FACTORY.run("-a 50 --b 100 PLUS -v");
  }

  @Test
  void testCalculator() {
    CONSOLE_FACTORY.setFunction(ConsoleFactoryTest::calculatorFunction);
    CONSOLE_FACTORY.run("-a 50 --b 100 PLUS -v");
  }

  @Test
  void testList() {
    CONSOLE_FACTORY.setFunction(ConsoleFactoryTest::printListValueFunction);
    CONSOLE_FACTORY.run("-l [\"qwer\",\"zxcv\",\"asdfg\"]");
  }

  private static void printAll() {
    final var list = CONSOLE_FACTORY.getResult().entrySet().stream()
        .map(entry -> entry.getKey().getName() + ": " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(list);
  }

  private static void calculatorFunction() {
    final var a = CONSOLE_FACTORY.getValueByOptionName("a", Integer.class);
    final var b = CONSOLE_FACTORY.getValueByOptionName("b", Integer.class);
    final var operation = CONSOLE_FACTORY.getValueByOptionName("math-operation",
        MathOperation.class);
    final var verbose = CONSOLE_FACTORY.getOptionByName("verbose");
    final var result = performMath(a, b, operation.toString());
    if (verbose != null) {
      System.out.println(a + operation.toString() + b + "=" + result);
    } else {
      System.out.println(result);
    }
  }

  private static void printListValueFunction() {
    final var listType = new TypeReference<List<String>>() {
    };
    final var list = CONSOLE_FACTORY.getValueByOptionName("list", listType);
    System.out.println(list);
  }

  private static Object performMath(Integer a, Integer b, String operation) {
    switch (operation) {
      case "+":
        return a + b;
      case "-":
        return a - b;
      case "*":
        return a * b;
      case "%":
        return a % b;
      case "/":
        return a / b;
      default:
        System.out.println("Operation not found: " + operation);
    }
    return null;
  }

  private static void addOptionsToConfiguration() throws OptionException {
    CONSOLE_FACTORY.addOption(
        Option.builder()
            .name("a")
            .aliases(Set.of("-a"))
            .helpInfo("integer value")
            .type(new TypeReference<Integer>() {
            })
            .build());

    CONSOLE_FACTORY.addOption(
        Option.builder()
            .name("b")
            .aliases(Set.of("-b", "--b"))
            .helpInfo("integer value")
            .type(new TypeReference<Integer>() {
            })
            .build());

    CONSOLE_FACTORY.addOption(
        Option.builder()
            .name("math-operation")
            .aliases(Set.of("PLUS", "MINUS"))
            .helpInfo("math operation")
            .type(new TypeReference<MathOperation>() {
            })
            .build());

    CONSOLE_FACTORY.addOption(
        Option.builder()
            .name("verbose")
            .aliases(Set.of("-v"))
            .helpInfo("verbose an equation")
            .defaultValue("verbose")
            .type(new TypeReference<Integer>() {
            })
            .build());

    CONSOLE_FACTORY.addOption(
        Option.builder()
            .name("list")
            .aliases(Set.of("-l"))
            .helpInfo("list of strings")
            .type(new TypeReference<List<String>>() {
            })
            .build());
  }

  enum MathOperation {
    PLUS("+"), MINUS("-");
    final String value;

    MathOperation(String s) {
      value = s;
    }

    @Override
    public String toString() {
      return value;
    }
  }
}

