package consolefactory;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.ClassUtils;

public class Utils {

  protected static boolean isPrimitiveType(TypeReference<?> typeReference) {
    try {
      return ClassUtils.isPrimitiveOrWrapper(Class.forName(typeReference.getType().getTypeName()));
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

}
