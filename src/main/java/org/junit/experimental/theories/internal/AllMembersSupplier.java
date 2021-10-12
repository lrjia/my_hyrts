package org.junit.experimental.theories.internal;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.junit.experimental.theories.PotentialAssignment;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

public class AllMembersSupplier extends ParameterSupplier {
   private final TestClass fClass;

   public AllMembersSupplier(TestClass type) {
      this.fClass = type;
   }

   public List<PotentialAssignment> getValueSources(ParameterSignature sig) {
      List<PotentialAssignment> list = new ArrayList();
      this.addFields(sig, list);
      this.addSinglePointMethods(sig, list);
      this.addMultiPointMethods(sig, list);
      return list;
   }

   private void addMultiPointMethods(ParameterSignature sig, List<PotentialAssignment> list) {
      Iterator i$ = this.fClass.getAnnotatedMethods(DataPoints.class).iterator();

      while(i$.hasNext()) {
         FrameworkMethod dataPointsMethod = (FrameworkMethod)i$.next();

         try {
            this.addMultiPointArrayValues(sig, dataPointsMethod.getName(), list, dataPointsMethod.invokeExplosively((Object)null));
         } catch (Throwable var6) {
         }
      }

   }

   private void addSinglePointMethods(ParameterSignature sig, List<PotentialAssignment> list) {
      Iterator i$ = this.fClass.getAnnotatedMethods(DataPoint.class).iterator();

      while(i$.hasNext()) {
         FrameworkMethod dataPointMethod = (FrameworkMethod)i$.next();
         if (this.isCorrectlyTyped(sig, dataPointMethod.getType())) {
            list.add(new AllMembersSupplier.MethodParameterValue(dataPointMethod));
         }
      }

   }

   private void addFields(ParameterSignature sig, List<PotentialAssignment> list) {
      Field[] arr$ = this.fClass.getJavaClass().getFields();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Field field = arr$[i$];
         if (Modifier.isStatic(field.getModifiers())) {
            Class<?> type = field.getType();
            if (sig.canAcceptArrayType(type) && field.getAnnotation(DataPoints.class) != null) {
               try {
                  this.addArrayValues(field.getName(), list, this.getStaticFieldValue(field));
               } catch (Throwable var9) {
               }
            } else if (sig.canAcceptType(type) && field.getAnnotation(DataPoint.class) != null) {
               list.add(PotentialAssignment.forValue(field.getName(), this.getStaticFieldValue(field)));
            }
         }
      }

   }

   private void addArrayValues(String name, List<PotentialAssignment> list, Object array) {
      for(int i = 0; i < Array.getLength(array); ++i) {
         list.add(PotentialAssignment.forValue(name + "[" + i + "]", Array.get(array, i)));
      }

   }

   private void addMultiPointArrayValues(ParameterSignature sig, String name, List<PotentialAssignment> list, Object array) throws Throwable {
      for(int i = 0; i < Array.getLength(array); ++i) {
         if (!this.isCorrectlyTyped(sig, Array.get(array, i).getClass())) {
            return;
         }

         list.add(PotentialAssignment.forValue(name + "[" + i + "]", Array.get(array, i)));
      }

   }

   private boolean isCorrectlyTyped(ParameterSignature parameterSignature, Class<?> type) {
      return parameterSignature.canAcceptType(type);
   }

   private Object getStaticFieldValue(Field field) {
      try {
         return field.get((Object)null);
      } catch (IllegalArgumentException var3) {
         throw new RuntimeException("unexpected: field from getClass doesn't exist on object");
      } catch (IllegalAccessException var4) {
         throw new RuntimeException("unexpected: getFields returned an inaccessible field");
      }
   }

   static class MethodParameterValue extends PotentialAssignment {
      private final FrameworkMethod fMethod;

      private MethodParameterValue(FrameworkMethod dataPointMethod) {
         this.fMethod = dataPointMethod;
      }

      public Object getValue() throws PotentialAssignment.CouldNotGenerateValueException {
         try {
            return this.fMethod.invokeExplosively((Object)null);
         } catch (IllegalArgumentException var2) {
            throw new RuntimeException("unexpected: argument length is checked");
         } catch (IllegalAccessException var3) {
            throw new RuntimeException("unexpected: getMethods returned an inaccessible method");
         } catch (Throwable var4) {
            throw new PotentialAssignment.CouldNotGenerateValueException();
         }
      }

      public String getDescription() throws PotentialAssignment.CouldNotGenerateValueException {
         return this.fMethod.getName();
      }

      // $FF: synthetic method
      MethodParameterValue(FrameworkMethod x0, Object x1) {
         this(x0);
      }
   }
}
