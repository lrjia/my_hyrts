package set.hyrts.org.apache.commons.lang3.exception;

import java.util.List;
import java.util.Set;
import set.hyrts.org.apache.commons.lang3.tuple.Pair;

public interface ExceptionContext {
   ExceptionContext addContextValue(String var1, Object var2);

   ExceptionContext setContextValue(String var1, Object var2);

   List<Object> getContextValues(String var1);

   Object getFirstContextValue(String var1);

   Set<String> getContextLabels();

   List<Pair<String, Object>> getContextEntries();

   String getFormattedExceptionMessage(String var1);
}
