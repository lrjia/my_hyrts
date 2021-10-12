package org.junit.internal.builders;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

public class AllDefaultPossibilitiesBuilder extends RunnerBuilder {
   private final boolean fCanUseSuiteMethod;

   public AllDefaultPossibilitiesBuilder(boolean canUseSuiteMethod) {
      this.fCanUseSuiteMethod = canUseSuiteMethod;
   }

   public Runner runnerForClass(Class<?> testClass) throws Throwable {
      List<RunnerBuilder> builders = Arrays.asList(this.ignoredBuilder(), this.annotatedBuilder(), this.suiteMethodBuilder(), this.junit3Builder(), this.junit4Builder());
      Iterator i$ = builders.iterator();

      Runner runner;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         RunnerBuilder each = (RunnerBuilder)i$.next();
         runner = each.safeRunnerForClass(testClass);
      } while(runner == null);

      return runner;
   }

   protected JUnit4Builder junit4Builder() {
      return new JUnit4Builder();
   }

   protected JUnit3Builder junit3Builder() {
      return new JUnit3Builder();
   }

   protected AnnotatedBuilder annotatedBuilder() {
      return new AnnotatedBuilder(this);
   }

   protected IgnoredBuilder ignoredBuilder() {
      return new IgnoredBuilder();
   }

   protected RunnerBuilder suiteMethodBuilder() {
      return (RunnerBuilder)(this.fCanUseSuiteMethod ? new SuiteMethodBuilder() : new NullBuilder());
   }
}
