package org.junit.runners.model;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;

class NoGenericTypeParametersValidator {
   private final Method fMethod;

   NoGenericTypeParametersValidator(Method method) {
      this.fMethod = method;
   }

   void validate(List<Throwable> errors) {
      Type[] arr$ = this.fMethod.getGenericParameterTypes();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Type each = arr$[i$];
         this.validateNoTypeParameterOnType(each, errors);
      }

   }

   private void validateNoTypeParameterOnType(Type type, List<Throwable> errors) {
      if (type instanceof TypeVariable) {
         errors.add(new Exception("Method " + this.fMethod.getName() + "() contains unresolved type variable " + type));
      } else if (type instanceof ParameterizedType) {
         this.validateNoTypeParameterOnParameterizedType((ParameterizedType)type, errors);
      } else if (type instanceof WildcardType) {
         this.validateNoTypeParameterOnWildcardType((WildcardType)type, errors);
      } else if (type instanceof GenericArrayType) {
         this.validateNoTypeParameterOnGenericArrayType((GenericArrayType)type, errors);
      }

   }

   private void validateNoTypeParameterOnParameterizedType(ParameterizedType parameterized, List<Throwable> errors) {
      Type[] arr$ = parameterized.getActualTypeArguments();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Type each = arr$[i$];
         this.validateNoTypeParameterOnType(each, errors);
      }

   }

   private void validateNoTypeParameterOnWildcardType(WildcardType wildcard, List<Throwable> errors) {
      Type[] arr$ = wildcard.getUpperBounds();
      int len$ = arr$.length;

      int i$;
      Type each;
      for(i$ = 0; i$ < len$; ++i$) {
         each = arr$[i$];
         this.validateNoTypeParameterOnType(each, errors);
      }

      arr$ = wildcard.getLowerBounds();
      len$ = arr$.length;

      for(i$ = 0; i$ < len$; ++i$) {
         each = arr$[i$];
         this.validateNoTypeParameterOnType(each, errors);
      }

   }

   private void validateNoTypeParameterOnGenericArrayType(GenericArrayType arrayType, List<Throwable> errors) {
      this.validateNoTypeParameterOnType(arrayType.getGenericComponentType(), errors);
   }
}
