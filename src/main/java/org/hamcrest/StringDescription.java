package org.hamcrest;

import java.io.IOException;

public class StringDescription extends BaseDescription {
   private final Appendable out;

   public StringDescription() {
      this(new StringBuilder());
   }

   public StringDescription(Appendable out) {
      this.out = out;
   }

   public static String toString(SelfDescribing selfDescribing) {
      return (new StringDescription()).appendDescriptionOf(selfDescribing).toString();
   }

   public static String asString(SelfDescribing selfDescribing) {
      return toString(selfDescribing);
   }

   protected void append(String str) {
      try {
         this.out.append(str);
      } catch (IOException var3) {
         throw new RuntimeException("Could not write description", var3);
      }
   }

   protected void append(char c) {
      try {
         this.out.append(c);
      } catch (IOException var3) {
         throw new RuntimeException("Could not write description", var3);
      }
   }

   public String toString() {
      return this.out.toString();
   }
}
