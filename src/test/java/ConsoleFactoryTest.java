import com.fasterxml.jackson.core.type.TypeReference;
import consolefactory.ConsoleFactory;
import consolefactory.Option;
import consolefactory.exception.OptionException;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class ConsoleFactoryTest {

  final static ConsoleFactory CONSOLE_FACTORY = new ConsoleFactory();

  @Test
  void testHelp() throws OptionException {
    addOptionsToConfiguration();
    CONSOLE_FACTORY.run("--h");
    CONSOLE_FACTORY.clearOptions();
  }

  @Test
  void testPrintAll() throws OptionException {
    addOptionsToConfiguration();
    CONSOLE_FACTORY.setFunction(ConsoleFactoryTest::printAll);
    CONSOLE_FACTORY.run("-a 50 --b 100 PLUS -v");
    CONSOLE_FACTORY.clearOptions();
  }

  @Test
  void testCalculator() throws OptionException {
    addOptionsToConfiguration();
    CONSOLE_FACTORY.setFunction(ConsoleFactoryTest::calculatorFunction);
    CONSOLE_FACTORY.run("-a 50 --b 100 PLUS -v");
    CONSOLE_FACTORY.clearOptions();
  }

  private static void printAll() {
    final var list = CONSOLE_FACTORY.getResult().entrySet().stream()
        .map(entry -> entry.getKey().getName() + ": " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(list);
  }

  private static void calculatorFunction() {
    final var a = CONSOLE_FACTORY.getValuesByOptionName("a", 1).get(0);
    final var b = CONSOLE_FACTORY.getValuesByOptionName("b", 1).get(0);
    final var operation = CONSOLE_FACTORY.getValuesByOptionName("math-operation",
        MathOperation.PLUS).get(0);
    final var verbose = CONSOLE_FACTORY.getOptionsByName("verbose").get(0);
    final var result = performMath(a, b, operation.toString());
    if (verbose != null) {
      System.out.println(a + operation.toString() + b + "=" + result);
    } else {
      System.out.println(result);
    }
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

  private void addOptionsToConfiguration() throws OptionException {
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

