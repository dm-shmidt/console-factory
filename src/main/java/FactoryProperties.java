import com.sun.jdi.connect.Connector.Argument;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FactoryProperties {

   private final List<Argument> arguments = new ArrayList<>();
   private final String prefix;

  public FactoryProperties(String prefix) {
    this.prefix = prefix;
  }
}
