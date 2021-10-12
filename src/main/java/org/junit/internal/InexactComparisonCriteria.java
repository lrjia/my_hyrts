package org.junit.internal;

import org.junit.Assert;

public class InexactComparisonCriteria extends ComparisonCriteria {
   public Object fDelta;

   public InexactComparisonCriteria(double delta) {
      this.fDelta = delta;
   }

   public InexactComparisonCriteria(float delta) {
      this.fDelta = delta;
   }

   protected void assertElementsEqual(Object expected, Object actual) {
      if (expected instanceof Double) {
         Assert.assertEquals((Double)expected, (Double)actual, (Double)this.fDelta);
      } else {
         Assert.assertEquals((Float)expected, (Float)actual, (Float)this.fDelta);
      }

   }
}
