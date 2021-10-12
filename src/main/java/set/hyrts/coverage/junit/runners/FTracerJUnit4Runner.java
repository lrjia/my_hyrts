package set.hyrts.coverage.junit.runners;

import java.io.IOException;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import set.hyrts.coverage.junit.FTracerJUnitUtils;

public class FTracerJUnit4Runner extends Runner implements Filterable, Sortable {
   private Runner runner;
   private Class<?> testClass;

   public FTracerJUnit4Runner(Runner runner, Class<?> testClass) {
      this.runner = runner;
      this.testClass = testClass;
   }

   public Description getDescription() {
      return this.runner.getDescription();
   }

   public void run(RunNotifier notifier) {
      this.runner.run(notifier);

      try {
         FTracerJUnitUtils.dumpCoverage(this.testClass);
      } catch (IOException var3) {
         var3.printStackTrace();
      }

   }

   public void filter(Filter paramFilter) throws NoTestsRemainException {
      if (this.runner instanceof Filterable) {
         Filterable localFilterable = (Filterable)this.runner;
         localFilterable.filter(paramFilter);
      }
   }

   public void sort(Sorter paramSorter) {
      if (this.runner instanceof Sortable) {
         Sortable localSortable = (Sortable)this.runner;
         localSortable.sort(paramSorter);
      }
   }
}
