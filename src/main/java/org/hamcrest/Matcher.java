package org.hamcrest;

public interface Matcher<T> extends SelfDescribing {
   boolean matches(Object var1);

   void describeMismatch(Object var1, Description var2);

   /** @deprecated */
   @Deprecated
   void _dont_implement_Matcher___instead_extend_BaseMatcher_();
}
