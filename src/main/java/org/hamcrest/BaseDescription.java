package org.hamcrest;

import java.util.Arrays;
import java.util.Iterator;
import org.hamcrest.internal.ArrayIterator;
import org.hamcrest.internal.SelfDescribingValueIterator;

public abstract class BaseDescription implements Description {
   public Description appendText(String text) {
      this.append(text);
      return this;
   }

   public Description appendDescriptionOf(SelfDescribing value) {
      value.describeTo(this);
      return this;
   }

   public Description appendValue(Object value) {
      if (value == null) {
         this.append("null");
      } else if (value instanceof String) {
         this.toJavaSyntax((String)value);
      } else if (value instanceof Character) {
         this.append('"');
         this.toJavaSyntax((Character)value);
         this.append('"');
      } else if (value instanceof Short) {
         this.append('<');
         this.append(this.descriptionOf(value));
         this.append("s>");
      } else if (value instanceof Long) {
         this.append('<');
         this.append(this.descriptionOf(value));
         this.append("L>");
      } else if (value instanceof Float) {
         this.append('<');
         this.append(this.descriptionOf(value));
         this.append("F>");
      } else if (value.getClass().isArray()) {
         this.appendValueList("[", ", ", "]", (Iterator)(new ArrayIterator(value)));
      } else {
         this.append('<');
         this.append(this.descriptionOf(value));
         this.append('>');
      }

      return this;
   }

   private String descriptionOf(Object value) {
      try {
         return String.valueOf(value);
      } catch (Exception var3) {
         return value.getClass().getName() + "@" + Integer.toHexString(value.hashCode());
      }
   }

   public <T> Description appendValueList(String start, String separator, String end, T... values) {
      return this.appendValueList(start, separator, end, (Iterable)Arrays.asList(values));
   }

   public <T> Description appendValueList(String start, String separator, String end, Iterable<T> values) {
      return this.appendValueList(start, separator, end, values.iterator());
   }

   private <T> Description appendValueList(String start, String separator, String end, Iterator<T> values) {
      return this.appendList(start, separator, end, (Iterator)(new SelfDescribingValueIterator(values)));
   }

   public Description appendList(String start, String separator, String end, Iterable<? extends SelfDescribing> values) {
      return this.appendList(start, separator, end, values.iterator());
   }

   private Description appendList(String start, String separator, String end, Iterator<? extends SelfDescribing> i) {
      boolean separate = false;
      this.append(start);

      while(i.hasNext()) {
         if (separate) {
            this.append(separator);
         }

         this.appendDescriptionOf((SelfDescribing)i.next());
         separate = true;
      }

      this.append(end);
      return this;
   }

   protected void append(String str) {
      for(int i = 0; i < str.length(); ++i) {
         this.append(str.charAt(i));
      }

   }

   protected abstract void append(char var1);

   private void toJavaSyntax(String unformatted) {
      this.append('"');

      for(int i = 0; i < unformatted.length(); ++i) {
         this.toJavaSyntax(unformatted.charAt(i));
      }

      this.append('"');
   }

   private void toJavaSyntax(char ch) {
      switch(ch) {
      case '\t':
         this.append("\\t");
         break;
      case '\n':
         this.append("\\n");
         break;
      case '\r':
         this.append("\\r");
         break;
      case '"':
         this.append("\\\"");
         break;
      default:
         this.append(ch);
      }

   }
}
