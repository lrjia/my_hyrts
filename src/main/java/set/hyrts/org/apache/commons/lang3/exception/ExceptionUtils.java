package set.hyrts.org.apache.commons.lang3.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import set.hyrts.org.apache.commons.lang3.ArrayUtils;
import set.hyrts.org.apache.commons.lang3.ClassUtils;
import set.hyrts.org.apache.commons.lang3.StringUtils;
import set.hyrts.org.apache.commons.lang3.Validate;

public class ExceptionUtils {
   static final String WRAPPED_MARKER = " [wrapped] ";
   private static final String[] CAUSE_METHOD_NAMES = new String[]{"getCause", "getNextException", "getTargetException", "getException", "getSourceException", "getRootCause", "getCausedByException", "getNested", "getLinkedException", "getNestedException", "getLinkedCause", "getThrowable"};

   /** @deprecated */
   @Deprecated
   public static String[] getDefaultCauseMethodNames() {
      return (String[])ArrayUtils.clone((Object[])CAUSE_METHOD_NAMES);
   }

   /** @deprecated */
   @Deprecated
   public static Throwable getCause(Throwable throwable) {
      return getCause(throwable, (String[])null);
   }

   /** @deprecated */
   @Deprecated
   public static Throwable getCause(Throwable throwable, String[] methodNames) {
      if (throwable == null) {
         return null;
      } else {
         if (methodNames == null) {
            Throwable cause = throwable.getCause();
            if (cause != null) {
               return cause;
            }

            methodNames = CAUSE_METHOD_NAMES;
         }

         String[] var7 = methodNames;
         int var3 = methodNames.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            String methodName = var7[var4];
            if (methodName != null) {
               Throwable legacyCause = getCauseUsingMethodName(throwable, methodName);
               if (legacyCause != null) {
                  return legacyCause;
               }
            }
         }

         return null;
      }
   }

   public static Throwable getRootCause(Throwable throwable) {
      List<Throwable> list = getThrowableList(throwable);
      return list.size() < 2 ? null : (Throwable)list.get(list.size() - 1);
   }

   private static Throwable getCauseUsingMethodName(Throwable throwable, String methodName) {
      Method method = null;

      try {
         method = throwable.getClass().getMethod(methodName);
      } catch (SecurityException | NoSuchMethodException var4) {
      }

      if (method != null && Throwable.class.isAssignableFrom(method.getReturnType())) {
         try {
            return (Throwable)method.invoke(throwable);
         } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException var5) {
         }
      }

      return null;
   }

   public static int getThrowableCount(Throwable throwable) {
      return getThrowableList(throwable).size();
   }

   public static Throwable[] getThrowables(Throwable throwable) {
      List<Throwable> list = getThrowableList(throwable);
      return (Throwable[])list.toArray(new Throwable[list.size()]);
   }

   public static List<Throwable> getThrowableList(Throwable throwable) {
      ArrayList list;
      for(list = new ArrayList(); throwable != null && !list.contains(throwable); throwable = getCause(throwable)) {
         list.add(throwable);
      }

      return list;
   }

   public static int indexOfThrowable(Throwable throwable, Class<?> clazz) {
      return indexOf(throwable, clazz, 0, false);
   }

   public static int indexOfThrowable(Throwable throwable, Class<?> clazz, int fromIndex) {
      return indexOf(throwable, clazz, fromIndex, false);
   }

   public static int indexOfType(Throwable throwable, Class<?> type) {
      return indexOf(throwable, type, 0, true);
   }

   public static int indexOfType(Throwable throwable, Class<?> type, int fromIndex) {
      return indexOf(throwable, type, fromIndex, true);
   }

   private static int indexOf(Throwable throwable, Class<?> type, int fromIndex, boolean subclass) {
      if (throwable != null && type != null) {
         if (fromIndex < 0) {
            fromIndex = 0;
         }

         Throwable[] throwables = getThrowables(throwable);
         if (fromIndex >= throwables.length) {
            return -1;
         } else {
            int i;
            if (subclass) {
               for(i = fromIndex; i < throwables.length; ++i) {
                  if (type.isAssignableFrom(throwables[i].getClass())) {
                     return i;
                  }
               }
            } else {
               for(i = fromIndex; i < throwables.length; ++i) {
                  if (type.equals(throwables[i].getClass())) {
                     return i;
                  }
               }
            }

            return -1;
         }
      } else {
         return -1;
      }
   }

   public static void printRootCauseStackTrace(Throwable throwable) {
      printRootCauseStackTrace(throwable, System.err);
   }

   public static void printRootCauseStackTrace(Throwable throwable, PrintStream stream) {
      if (throwable != null) {
         Validate.isTrue(stream != null, "The PrintStream must not be null");
         String[] trace = getRootCauseStackTrace(throwable);
         String[] var3 = trace;
         int var4 = trace.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String element = var3[var5];
            stream.println(element);
         }

         stream.flush();
      }
   }

   public static void printRootCauseStackTrace(Throwable throwable, PrintWriter writer) {
      if (throwable != null) {
         Validate.isTrue(writer != null, "The PrintWriter must not be null");
         String[] trace = getRootCauseStackTrace(throwable);
         String[] var3 = trace;
         int var4 = trace.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String element = var3[var5];
            writer.println(element);
         }

         writer.flush();
      }
   }

   public static String[] getRootCauseStackTrace(Throwable throwable) {
      if (throwable == null) {
         return ArrayUtils.EMPTY_STRING_ARRAY;
      } else {
         Throwable[] throwables = getThrowables(throwable);
         int count = throwables.length;
         List<String> frames = new ArrayList();
         List<String> nextTrace = getStackFrameList(throwables[count - 1]);
         int i = count;

         while(true) {
            --i;
            if (i < 0) {
               return (String[])frames.toArray(new String[frames.size()]);
            }

            List<String> trace = nextTrace;
            if (i != 0) {
               nextTrace = getStackFrameList(throwables[i - 1]);
               removeCommonFrames(trace, nextTrace);
            }

            if (i == count - 1) {
               frames.add(throwables[i].toString());
            } else {
               frames.add(" [wrapped] " + throwables[i].toString());
            }

            for(int j = 0; j < trace.size(); ++j) {
               frames.add(trace.get(j));
            }
         }
      }
   }

   public static void removeCommonFrames(List<String> causeFrames, List<String> wrapperFrames) {
      if (causeFrames != null && wrapperFrames != null) {
         int causeFrameIndex = causeFrames.size() - 1;

         for(int wrapperFrameIndex = wrapperFrames.size() - 1; causeFrameIndex >= 0 && wrapperFrameIndex >= 0; --wrapperFrameIndex) {
            String causeFrame = (String)causeFrames.get(causeFrameIndex);
            String wrapperFrame = (String)wrapperFrames.get(wrapperFrameIndex);
            if (causeFrame.equals(wrapperFrame)) {
               causeFrames.remove(causeFrameIndex);
            }

            --causeFrameIndex;
         }

      } else {
         throw new IllegalArgumentException("The List must not be null");
      }
   }

   public static String getStackTrace(Throwable throwable) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw, true);
      throwable.printStackTrace(pw);
      return sw.getBuffer().toString();
   }

   public static String[] getStackFrames(Throwable throwable) {
      return throwable == null ? ArrayUtils.EMPTY_STRING_ARRAY : getStackFrames(getStackTrace(throwable));
   }

   static String[] getStackFrames(String stackTrace) {
      String linebreak = System.lineSeparator();
      StringTokenizer frames = new StringTokenizer(stackTrace, linebreak);
      ArrayList list = new ArrayList();

      while(frames.hasMoreTokens()) {
         list.add(frames.nextToken());
      }

      return (String[])list.toArray(new String[list.size()]);
   }

   static List<String> getStackFrameList(Throwable t) {
      String stackTrace = getStackTrace(t);
      String linebreak = System.lineSeparator();
      StringTokenizer frames = new StringTokenizer(stackTrace, linebreak);
      List<String> list = new ArrayList();
      boolean traceStarted = false;

      while(frames.hasMoreTokens()) {
         String token = frames.nextToken();
         int at = token.indexOf("at");
         if (at != -1 && token.substring(0, at).trim().isEmpty()) {
            traceStarted = true;
            list.add(token);
         } else if (traceStarted) {
            break;
         }
      }

      return list;
   }

   public static String getMessage(Throwable th) {
      if (th == null) {
         return "";
      } else {
         String clsName = ClassUtils.getShortClassName(th, (String)null);
         String msg = th.getMessage();
         return clsName + ": " + StringUtils.defaultString(msg);
      }
   }

   public static String getRootCauseMessage(Throwable th) {
      Throwable root = getRootCause(th);
      root = root == null ? th : root;
      return getMessage(root);
   }

   public static <R> R rethrow(Throwable throwable) {
      return typeErasure(throwable);
   }

   private static <R, T extends Throwable> R typeErasure(Throwable throwable) throws T {
      throw throwable;
   }

   public static <R> R wrapAndThrow(Throwable throwable) {
      if (throwable instanceof RuntimeException) {
         throw (RuntimeException)throwable;
      } else if (throwable instanceof Error) {
         throw (Error)throwable;
      } else {
         throw new UndeclaredThrowableException(throwable);
      }
   }

   public static boolean hasCause(Throwable chain, Class<? extends Throwable> type) {
      if (chain instanceof UndeclaredThrowableException) {
         chain = chain.getCause();
      }

      return type.isInstance(chain);
   }
}
