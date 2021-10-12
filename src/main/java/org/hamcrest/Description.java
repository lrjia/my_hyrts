package org.hamcrest;

public interface Description {
   Description NONE = new Description.NullDescription();

   Description appendText(String var1);

   Description appendDescriptionOf(SelfDescribing var1);

   Description appendValue(Object var1);

   <T> Description appendValueList(String var1, String var2, String var3, T... var4);

   <T> Description appendValueList(String var1, String var2, String var3, Iterable<T> var4);

   Description appendList(String var1, String var2, String var3, Iterable<? extends SelfDescribing> var4);

   public static final class NullDescription implements Description {
      public Description appendDescriptionOf(SelfDescribing value) {
         return this;
      }

      public Description appendList(String start, String separator, String end, Iterable<? extends SelfDescribing> values) {
         return this;
      }

      public Description appendText(String text) {
         return this;
      }

      public Description appendValue(Object value) {
         return this;
      }

      public <T> Description appendValueList(String start, String separator, String end, T... values) {
         return this;
      }

      public <T> Description appendValueList(String start, String separator, String end, Iterable<T> values) {
         return this;
      }

      public String toString() {
         return "";
      }
   }
}
