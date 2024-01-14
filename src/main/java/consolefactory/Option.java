package consolefactory;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Option {

  private final String name;
  private final Set<String> aliases;
  private final Object defaultValue;
  private final TypeReference<?> type;
  private final boolean mandatory = false;
  private final boolean unique = true;
  private final Object[] bounds;
  private final String helpInfo;

  public static class OptionBuilder {

    private String helpInfo;

    public OptionBuilder helpInfo(String helpInfo) {
      this.helpInfo = String.join(", ", aliases) + ": " + helpInfo;
      return this;
    }

  }

  protected boolean hasDefaultValue() {
    return defaultValue != null;
  }

  protected boolean hasAlias(String alias) {
    return aliases.contains(alias);
  }
}
