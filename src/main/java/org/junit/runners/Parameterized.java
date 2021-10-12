package org.junit.runners;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class Parameterized extends Suite {
   private static final List<Runner> NO_RUNNERS = Collections.emptyList();
   private final ArrayList<Runner> runners = new ArrayList();

   public Parameterized(Class<?> klass) throws Throwable {
      super(klass, NO_RUNNERS);
      Parameterized.Parameters parameters = (Parameterized.Parameters)this.getParametersMethod().getAnnotation(Parameterized.Parameters.class);
      this.createRunnersForParameters(this.allParameters(), parameters.name());
   }

   protected List<Runner> getChildren() {
      return this.runners;
   }

   private Iterable<Object[]> allParameters() throws Throwable {
      Object parameters = this.getParametersMethod().invokeExplosively((Object)null);
      if (parameters instanceof Iterable) {
         return (Iterable)parameters;
      } else {
         throw this.parametersMethodReturnedWrongType();
      }
   }

   private FrameworkMethod getParametersMethod() throws Exception {
      List<FrameworkMethod> methods = this.getTestClass().getAnnotatedMethods(Parameterized.Parameters.class);
      Iterator i$ = methods.iterator();

      FrameworkMethod each;
      do {
         if (!i$.hasNext()) {
            throw new Exception("No public static parameters method on class " + this.getTestClass().getName());
         }

         each = (FrameworkMethod)i$.next();
      } while(!each.isStatic() || !each.isPublic());

      return each;
   }

   private void createRunnersForParameters(Iterable<Object[]> allParameters, String namePattern) throws InitializationError, Exception {
      try {
         int i = 0;

         for(Iterator i$ = allParameters.iterator(); i$.hasNext(); ++i) {
            Object[] parametersOfSingleTest = (Object[])i$.next();
            String name = this.nameFor(namePattern, i, parametersOfSingleTest);
            Parameterized.TestClassRunnerForParameters runner = new Parameterized.TestClassRunnerForParameters(this.getTestClass().getJavaClass(), parametersOfSingleTest, name);
            this.runners.add(runner);
         }

      } catch (ClassCastException var8) {
         throw this.parametersMethodReturnedWrongType();
      }
   }

   private String nameFor(String namePattern, int index, Object[] parameters) {
      String finalPattern = namePattern.replaceAll("\\{index\\}", Integer.toString(index));
      String name = MessageFormat.format(finalPattern, parameters);
      return "[" + name + "]";
   }

   private Exception parametersMethodReturnedWrongType() throws Exception {
      String className = this.getTestClass().getName();
      String methodName = this.getParametersMethod().getName();
      String message = MessageFormat.format("{0}.{1}() must return an Iterable of arrays.", className, methodName);
      return new Exception(message);
   }

   private List<FrameworkField> getAnnotatedFieldsByParameter() {
      return this.getTestClass().getAnnotatedFields(Parameterized.Parameter.class);
   }

   private boolean fieldsAreAnnotated() {
      return !this.getAnnotatedFieldsByParameter().isEmpty();
   }

   private class TestClassRunnerForParameters extends BlockJUnit4ClassRunner {
      private final Object[] fParameters;
      private final String fName;

      TestClassRunnerForParameters(Class<?> type, Object[] parameters, String name) throws InitializationError {
         super(type);
         this.fParameters = parameters;
         this.fName = name;
      }

      public Object createTest() throws Exception {
         return Parameterized.this.fieldsAreAnnotated() ? this.createTestUsingFieldInjection() : this.createTestUsingConstructorInjection();
      }

      private Object createTestUsingConstructorInjection() throws Exception {
         return this.getTestClass().getOnlyConstructor().newInstance(this.fParameters);
      }

      private Object createTestUsingFieldInjection() throws Exception {
         List<FrameworkField> annotatedFieldsByParameter = Parameterized.this.getAnnotatedFieldsByParameter();
         if (annotatedFieldsByParameter.size() != this.fParameters.length) {
            throw new Exception("Wrong number of parameters and @Parameter fields. @Parameter fields counted: " + annotatedFieldsByParameter.size() + ", available parameters: " + this.fParameters.length + ".");
         } else {
            Object testClassInstance = this.getTestClass().getJavaClass().newInstance();
            Iterator i$ = annotatedFieldsByParameter.iterator();

            while(i$.hasNext()) {
               FrameworkField each = (FrameworkField)i$.next();
               Field field = each.getField();
               Parameterized.Parameter annotation = (Parameterized.Parameter)field.getAnnotation(Parameterized.Parameter.class);
               int index = annotation.value();

               try {
                  field.set(testClassInstance, this.fParameters[index]);
               } catch (IllegalArgumentException var9) {
                  throw new Exception(this.getTestClass().getName() + ": Trying to set " + field.getName() + " with the value " + this.fParameters[index] + " that is not the right type (" + this.fParameters[index].getClass().getSimpleName() + " instead of " + field.getType().getSimpleName() + ").", var9);
               }
            }

            return testClassInstance;
         }
      }

      protected String getName() {
         return this.fName;
      }

      protected String testName(FrameworkMethod method) {
         return method.getName() + this.getName();
      }

      protected void validateConstructor(List<Throwable> errors) {
         this.validateOnlyOneConstructor(errors);
         if (Parameterized.this.fieldsAreAnnotated()) {
            this.validateZeroArgConstructor(errors);
         }

      }

      protected void validateFields(List<Throwable> errors) {
         super.validateFields(errors);
         if (Parameterized.this.fieldsAreAnnotated()) {
            List<FrameworkField> annotatedFieldsByParameter = Parameterized.this.getAnnotatedFieldsByParameter();
            int[] usedIndices = new int[annotatedFieldsByParameter.size()];
            Iterator i$ = annotatedFieldsByParameter.iterator();

            while(true) {
               while(i$.hasNext()) {
                  FrameworkField each = (FrameworkField)i$.next();
                  int index = ((Parameterized.Parameter)each.getField().getAnnotation(Parameterized.Parameter.class)).value();
                  if (index >= 0 && index <= annotatedFieldsByParameter.size() - 1) {
                     int var10002 = usedIndices[index]++;
                  } else {
                     errors.add(new Exception("Invalid @Parameter value: " + index + ". @Parameter fields counted: " + annotatedFieldsByParameter.size() + ". Please use an index between 0 and " + (annotatedFieldsByParameter.size() - 1) + "."));
                  }
               }

               for(int indexx = 0; indexx < usedIndices.length; ++indexx) {
                  int numberOfUse = usedIndices[indexx];
                  if (numberOfUse == 0) {
                     errors.add(new Exception("@Parameter(" + indexx + ") is never used."));
                  } else if (numberOfUse > 1) {
                     errors.add(new Exception("@Parameter(" + indexx + ") is used more than once (" + numberOfUse + ")."));
                  }
               }
               break;
            }
         }

      }

      protected Statement classBlock(RunNotifier notifier) {
         return this.childrenInvoker(notifier);
      }

      protected Annotation[] getRunnerAnnotations() {
         return new Annotation[0];
      }
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.FIELD})
   public @interface Parameter {
      int value() default 0;
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.METHOD})
   public @interface Parameters {
      String name() default "{index}";
   }
}
