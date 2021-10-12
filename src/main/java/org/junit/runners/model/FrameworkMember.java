package org.junit.runners.model;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;

public abstract class FrameworkMember<T extends FrameworkMember<T>> {
   abstract Annotation[] getAnnotations();

   abstract boolean isShadowedBy(T var1);

   boolean isShadowedBy(List<T> members) {
      Iterator i$ = members.iterator();

      FrameworkMember each;
      do {
         if (!i$.hasNext()) {
            return false;
         }

         each = (FrameworkMember)i$.next();
      } while(!this.isShadowedBy(each));

      return true;
   }

   public abstract boolean isPublic();

   public abstract boolean isStatic();

   public abstract String getName();

   public abstract Class<?> getType();
}
