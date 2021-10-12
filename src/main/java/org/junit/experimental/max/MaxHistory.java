package org.junit.experimental.max;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class MaxHistory implements Serializable {
   private static final long serialVersionUID = 1L;
   private final Map<String, Long> fDurations = new HashMap();
   private final Map<String, Long> fFailureTimestamps = new HashMap();
   private final File fHistoryStore;

   public static MaxHistory forFolder(File file) {
      if (file.exists()) {
         try {
            return readHistory(file);
         } catch (CouldNotReadCoreException var2) {
            var2.printStackTrace();
            file.delete();
         }
      }

      return new MaxHistory(file);
   }

   private static MaxHistory readHistory(File storedResults) throws CouldNotReadCoreException {
      try {
         FileInputStream file = new FileInputStream(storedResults);

         MaxHistory var3;
         try {
            ObjectInputStream stream = new ObjectInputStream(file);

            try {
               var3 = (MaxHistory)stream.readObject();
            } finally {
               stream.close();
            }
         } finally {
            file.close();
         }

         return var3;
      } catch (Exception var14) {
         throw new CouldNotReadCoreException(var14);
      }
   }

   private MaxHistory(File storedResults) {
      this.fHistoryStore = storedResults;
   }

   private void save() throws IOException {
      ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(this.fHistoryStore));
      stream.writeObject(this);
      stream.close();
   }

   Long getFailureTimestamp(Description key) {
      return (Long)this.fFailureTimestamps.get(key.toString());
   }

   void putTestFailureTimestamp(Description key, long end) {
      this.fFailureTimestamps.put(key.toString(), end);
   }

   boolean isNewTest(Description key) {
      return !this.fDurations.containsKey(key.toString());
   }

   Long getTestDuration(Description key) {
      return (Long)this.fDurations.get(key.toString());
   }

   void putTestDuration(Description description, long duration) {
      this.fDurations.put(description.toString(), duration);
   }

   public RunListener listener() {
      return new MaxHistory.RememberingListener();
   }

   public Comparator<Description> testComparator() {
      return new MaxHistory.TestComparator();
   }

   private class TestComparator implements Comparator<Description> {
      private TestComparator() {
      }

      public int compare(Description o1, Description o2) {
         if (MaxHistory.this.isNewTest(o1)) {
            return -1;
         } else if (MaxHistory.this.isNewTest(o2)) {
            return 1;
         } else {
            int result = this.getFailure(o2).compareTo(this.getFailure(o1));
            return result != 0 ? result : MaxHistory.this.getTestDuration(o1).compareTo(MaxHistory.this.getTestDuration(o2));
         }
      }

      private Long getFailure(Description key) {
         Long result = MaxHistory.this.getFailureTimestamp(key);
         return result == null ? 0L : result;
      }

      // $FF: synthetic method
      TestComparator(Object x1) {
         this();
      }
   }

   private final class RememberingListener extends RunListener {
      private long overallStart;
      private Map<Description, Long> starts;

      private RememberingListener() {
         this.overallStart = System.currentTimeMillis();
         this.starts = new HashMap();
      }

      public void testStarted(Description description) throws Exception {
         this.starts.put(description, System.nanoTime());
      }

      public void testFinished(Description description) throws Exception {
         long end = System.nanoTime();
         long start = (Long)this.starts.get(description);
         MaxHistory.this.putTestDuration(description, end - start);
      }

      public void testFailure(Failure failure) throws Exception {
         MaxHistory.this.putTestFailureTimestamp(failure.getDescription(), this.overallStart);
      }

      public void testRunFinished(Result result) throws Exception {
         MaxHistory.this.save();
      }

      // $FF: synthetic method
      RememberingListener(Object x1) {
         this();
      }
   }
}
