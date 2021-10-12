package org.junit.runner;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Description implements Serializable {
   private static final long serialVersionUID = 1L;
   private static final Pattern METHOD_AND_CLASS_NAME_PATTERN = Pattern.compile("(.*)\\((.*)\\)");
   public static final Description EMPTY = new Description((Class)null, "No Tests", new Annotation[0]);
   public static final Description TEST_MECHANISM = new Description((Class)null, "Test mechanism", new Annotation[0]);
   private final ArrayList<Description> fChildren;
   private final String fDisplayName;
   private final Serializable fUniqueId;
   private final Annotation[] fAnnotations;
   private Class<?> fTestClass;

   public static Description createSuiteDescription(String name, Annotation... annotations) {
      return new Description((Class)null, name, annotations);
   }

   public static Description createSuiteDescription(String name, Serializable uniqueId, Annotation... annotations) {
      return new Description((Class)null, name, uniqueId, annotations);
   }

   public static Description createTestDescription(String className, String name, Annotation... annotations) {
      return new Description((Class)null, formatDisplayName(name, className), annotations);
   }

   public static Description createTestDescription(Class<?> clazz, String name, Annotation... annotations) {
      return new Description(clazz, formatDisplayName(name, clazz.getName()), annotations);
   }

   public static Description createTestDescription(Class<?> clazz, String name) {
      return new Description(clazz, formatDisplayName(name, clazz.getName()), new Annotation[0]);
   }

   public static Description createTestDescription(String className, String name, Serializable uniqueId) {
      return new Description((Class)null, formatDisplayName(name, className), uniqueId, new Annotation[0]);
   }

   private static String formatDisplayName(String name, String className) {
      return String.format("%s(%s)", name, className);
   }

   public static Description createSuiteDescription(Class<?> testClass) {
      return new Description(testClass, testClass.getName(), testClass.getAnnotations());
   }

   private Description(Class<?> clazz, String displayName, Annotation... annotations) {
      this(clazz, displayName, displayName, annotations);
   }

   private Description(Class<?> clazz, String displayName, Serializable uniqueId, Annotation... annotations) {
      this.fChildren = new ArrayList();
      if (displayName != null && displayName.length() != 0) {
         if (uniqueId == null) {
            throw new IllegalArgumentException("The unique id must not be null.");
         } else {
            this.fTestClass = clazz;
            this.fDisplayName = displayName;
            this.fUniqueId = uniqueId;
            this.fAnnotations = annotations;
         }
      } else {
         throw new IllegalArgumentException("The display name must not be empty.");
      }
   }

   public String getDisplayName() {
      return this.fDisplayName;
   }

   public void addChild(Description description) {
      this.getChildren().add(description);
   }

   public ArrayList<Description> getChildren() {
      return this.fChildren;
   }

   public boolean isSuite() {
      return !this.isTest();
   }

   public boolean isTest() {
      return this.getChildren().isEmpty();
   }

   public int testCount() {
      if (this.isTest()) {
         return 1;
      } else {
         int result = 0;

         Description child;
         for(Iterator i$ = this.getChildren().iterator(); i$.hasNext(); result += child.testCount()) {
            child = (Description)i$.next();
         }

         return result;
      }
   }

   public int hashCode() {
      return this.fUniqueId.hashCode();
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof Description)) {
         return false;
      } else {
         Description d = (Description)obj;
         return this.fUniqueId.equals(d.fUniqueId);
      }
   }

   public String toString() {
      return this.getDisplayName();
   }

   public boolean isEmpty() {
      return this.equals(EMPTY);
   }

   public Description childlessCopy() {
      return new Description(this.fTestClass, this.fDisplayName, this.fAnnotations);
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
      Annotation[] arr$ = this.fAnnotations;
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Annotation each = arr$[i$];
         if (each.annotationType().equals(annotationType)) {
            return (Annotation)annotationType.cast(each);
         }
      }

      return null;
   }

   public Collection<Annotation> getAnnotations() {
      return Arrays.asList(this.fAnnotations);
   }

   public Class<?> getTestClass() {
      if (this.fTestClass != null) {
         return this.fTestClass;
      } else {
         String name = this.getClassName();
         if (name == null) {
            return null;
         } else {
            try {
               this.fTestClass = Class.forName(name, false, this.getClass().getClassLoader());
               return this.fTestClass;
            } catch (ClassNotFoundException var3) {
               return null;
            }
         }
      }
   }

   public String getClassName() {
      return this.fTestClass != null ? this.fTestClass.getName() : this.methodAndClassNamePatternGroupOrDefault(2, this.toString());
   }

   public String getMethodName() {
      return this.methodAndClassNamePatternGroupOrDefault(1, (String)null);
   }

   private String methodAndClassNamePatternGroupOrDefault(int group, String defaultString) {
      Matcher matcher = METHOD_AND_CLASS_NAME_PATTERN.matcher(this.toString());
      return matcher.matches() ? matcher.group(group) : defaultString;
   }
}
