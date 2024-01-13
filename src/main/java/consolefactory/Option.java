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
  private final boolean mandatory;
  private final boolean unique = true;
  private final Object[] bounds;
  private final String helpInfo;

  public boolean hasDefaultValue() {
    return defaultValue != null;
  }

  public boolean hasAlias(String alias) {
    return aliases.contains(alias);
  }
}
