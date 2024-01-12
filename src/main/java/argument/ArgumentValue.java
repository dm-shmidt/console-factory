package argument;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArgumentValue implements Argument {

  private final String name;
  private final Set<String> aliases;
  private Object value;
  private final Object defaultValue;
  private final Class<?> type;
  private final boolean mandatory;
  private final boolean unique;
  private final Object[] bounds;
  private final String helpInfo;

  @Override
  public boolean hasDefaultValue() {
    return defaultValue != null;
  }

  @Override
  public ArgumentValue copy() {
    return ArgumentValue.builder()
        .name(name)
        .aliases(Set.copyOf(aliases))
        .value(value)
        .defaultValue(defaultValue)
        .type(type)
        .mandatory(mandatory)
        .unique(unique)
        .bounds(bounds)
        .helpInfo(helpInfo)
        .build();
  }
}
