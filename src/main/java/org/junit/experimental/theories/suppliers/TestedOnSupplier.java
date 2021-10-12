package org.junit.experimental.theories.suppliers;

import java.util.ArrayList;
import java.util.List;
import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.junit.experimental.theories.PotentialAssignment;

public class TestedOnSupplier extends ParameterSupplier {
   public List<PotentialAssignment> getValueSources(ParameterSignature sig) {
      List<PotentialAssignment> list = new ArrayList();
      TestedOn testedOn = (TestedOn)sig.getAnnotation(TestedOn.class);
      int[] ints = testedOn.ints();
      int[] arr$ = ints;
      int len$ = ints.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         int i = arr$[i$];
         list.add(PotentialAssignment.forValue("ints", i));
      }

      return list;
   }
}
