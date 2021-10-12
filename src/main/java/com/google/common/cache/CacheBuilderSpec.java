package com.google.common.cache;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

@GwtIncompatible
public final class CacheBuilderSpec {
   private static final Splitter KEYS_SPLITTER = Splitter.on(',').trimResults();
   private static final Splitter KEY_VALUE_SPLITTER = Splitter.on('=').trimResults();
   private static final ImmutableMap<String, CacheBuilderSpec.ValueParser> VALUE_PARSERS;
   @VisibleForTesting
   Integer initialCapacity;
   @VisibleForTesting
   Long maximumSize;
   @VisibleForTesting
   Long maximumWeight;
   @VisibleForTesting
   Integer concurrencyLevel;
   @VisibleForTesting
   LocalCache.Strength keyStrength;
   @VisibleForTesting
   LocalCache.Strength valueStrength;
   @VisibleForTesting
   Boolean recordStats;
   @VisibleForTesting
   long writeExpirationDuration;
   @VisibleForTesting
   TimeUnit writeExpirationTimeUnit;
   @VisibleForTesting
   long accessExpirationDuration;
   @VisibleForTesting
   TimeUnit accessExpirationTimeUnit;
   @VisibleForTesting
   long refreshDuration;
   @VisibleForTesting
   TimeUnit refreshTimeUnit;
   private final String specification;

   private CacheBuilderSpec(String specification) {
      this.specification = specification;
   }

   public static CacheBuilderSpec parse(String cacheBuilderSpecification) {
      CacheBuilderSpec spec = new CacheBuilderSpec(cacheBuilderSpecification);
      if (!cacheBuilderSpecification.isEmpty()) {
         Iterator i$ = KEYS_SPLITTER.split(cacheBuilderSpecification).iterator();

         while(i$.hasNext()) {
            String keyValuePair = (String)i$.next();
            List<String> keyAndValue = ImmutableList.copyOf(KEY_VALUE_SPLITTER.split(keyValuePair));
            Preconditions.checkArgument(!keyAndValue.isEmpty(), "blank key-value pair");
            Preconditions.checkArgument(keyAndValue.size() <= 2, "key-value pair %s with more than one equals sign", (Object)keyValuePair);
            String key = (String)keyAndValue.get(0);
            CacheBuilderSpec.ValueParser valueParser = (CacheBuilderSpec.ValueParser)VALUE_PARSERS.get(key);
            Preconditions.checkArgument(valueParser != null, "unknown key %s", (Object)key);
            String value = keyAndValue.size() == 1 ? null : (String)keyAndValue.get(1);
            valueParser.parse(spec, key, value);
         }
      }

      return spec;
   }

   public static CacheBuilderSpec disableCaching() {
      return parse("maximumSize=0");
   }

   CacheBuilder<Object, Object> toCacheBuilder() {
      CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
      if (this.initialCapacity != null) {
         builder.initialCapacity(this.initialCapacity);
      }

      if (this.maximumSize != null) {
         builder.maximumSize(this.maximumSize);
      }

      if (this.maximumWeight != null) {
         builder.maximumWeight(this.maximumWeight);
      }

      if (this.concurrencyLevel != null) {
         builder.concurrencyLevel(this.concurrencyLevel);
      }

      if (this.keyStrength != null) {
         switch(this.keyStrength) {
         case WEAK:
            builder.weakKeys();
            break;
         default:
            throw new AssertionError();
         }
      }

      if (this.valueStrength != null) {
         switch(this.valueStrength) {
         case WEAK:
            builder.weakValues();
            break;
         case SOFT:
            builder.softValues();
            break;
         default:
            throw new AssertionError();
         }
      }

      if (this.recordStats != null && this.recordStats) {
         builder.recordStats();
      }

      if (this.writeExpirationTimeUnit != null) {
         builder.expireAfterWrite(this.writeExpirationDuration, this.writeExpirationTimeUnit);
      }

      if (this.accessExpirationTimeUnit != null) {
         builder.expireAfterAccess(this.accessExpirationDuration, this.accessExpirationTimeUnit);
      }

      if (this.refreshTimeUnit != null) {
         builder.refreshAfterWrite(this.refreshDuration, this.refreshTimeUnit);
      }

      return builder;
   }

   public String toParsableString() {
      return this.specification;
   }

   public String toString() {
      return MoreObjects.toStringHelper((Object)this).addValue(this.toParsableString()).toString();
   }

   public int hashCode() {
      return Objects.hashCode(this.initialCapacity, this.maximumSize, this.maximumWeight, this.concurrencyLevel, this.keyStrength, this.valueStrength, this.recordStats, durationInNanos(this.writeExpirationDuration, this.writeExpirationTimeUnit), durationInNanos(this.accessExpirationDuration, this.accessExpirationTimeUnit), durationInNanos(this.refreshDuration, this.refreshTimeUnit));
   }

   public boolean equals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof CacheBuilderSpec)) {
         return false;
      } else {
         CacheBuilderSpec that = (CacheBuilderSpec)obj;
         return Objects.equal(this.initialCapacity, that.initialCapacity) && Objects.equal(this.maximumSize, that.maximumSize) && Objects.equal(this.maximumWeight, that.maximumWeight) && Objects.equal(this.concurrencyLevel, that.concurrencyLevel) && Objects.equal(this.keyStrength, that.keyStrength) && Objects.equal(this.valueStrength, that.valueStrength) && Objects.equal(this.recordStats, that.recordStats) && Objects.equal(durationInNanos(this.writeExpirationDuration, this.writeExpirationTimeUnit), durationInNanos(that.writeExpirationDuration, that.writeExpirationTimeUnit)) && Objects.equal(durationInNanos(this.accessExpirationDuration, this.accessExpirationTimeUnit), durationInNanos(that.accessExpirationDuration, that.accessExpirationTimeUnit)) && Objects.equal(durationInNanos(this.refreshDuration, this.refreshTimeUnit), durationInNanos(that.refreshDuration, that.refreshTimeUnit));
      }
   }

   @Nullable
   private static Long durationInNanos(long duration, @Nullable TimeUnit unit) {
      return unit == null ? null : unit.toNanos(duration);
   }

   private static String format(String format, Object... args) {
      return String.format(Locale.ROOT, format, args);
   }

   static {
      VALUE_PARSERS = ImmutableMap.builder().put("initialCapacity", new CacheBuilderSpec.InitialCapacityParser()).put("maximumSize", new CacheBuilderSpec.MaximumSizeParser()).put("maximumWeight", new CacheBuilderSpec.MaximumWeightParser()).put("concurrencyLevel", new CacheBuilderSpec.ConcurrencyLevelParser()).put("weakKeys", new CacheBuilderSpec.KeyStrengthParser(LocalCache.Strength.WEAK)).put("softValues", new CacheBuilderSpec.ValueStrengthParser(LocalCache.Strength.SOFT)).put("weakValues", new CacheBuilderSpec.ValueStrengthParser(LocalCache.Strength.WEAK)).put("recordStats", new CacheBuilderSpec.RecordStatsParser()).put("expireAfterAccess", new CacheBuilderSpec.AccessDurationParser()).put("expireAfterWrite", new CacheBuilderSpec.WriteDurationParser()).put("refreshAfterWrite", new CacheBuilderSpec.RefreshDurationParser()).put("refreshInterval", new CacheBuilderSpec.RefreshDurationParser()).build();
   }

   static class RefreshDurationParser extends CacheBuilderSpec.DurationParser {
      protected void parseDuration(CacheBuilderSpec spec, long duration, TimeUnit unit) {
         Preconditions.checkArgument(spec.refreshTimeUnit == null, "refreshAfterWrite already set");
         spec.refreshDuration = duration;
         spec.refreshTimeUnit = unit;
      }
   }

   static class WriteDurationParser extends CacheBuilderSpec.DurationParser {
      protected void parseDuration(CacheBuilderSpec spec, long duration, TimeUnit unit) {
         Preconditions.checkArgument(spec.writeExpirationTimeUnit == null, "expireAfterWrite already set");
         spec.writeExpirationDuration = duration;
         spec.writeExpirationTimeUnit = unit;
      }
   }

   static class AccessDurationParser extends CacheBuilderSpec.DurationParser {
      protected void parseDuration(CacheBuilderSpec spec, long duration, TimeUnit unit) {
         Preconditions.checkArgument(spec.accessExpirationTimeUnit == null, "expireAfterAccess already set");
         spec.accessExpirationDuration = duration;
         spec.accessExpirationTimeUnit = unit;
      }
   }

   abstract static class DurationParser implements CacheBuilderSpec.ValueParser {
      protected abstract void parseDuration(CacheBuilderSpec var1, long var2, TimeUnit var4);

      public void parse(CacheBuilderSpec spec, String key, String value) {
         Preconditions.checkArgument(value != null && !value.isEmpty(), "value of key %s omitted", (Object)key);

         try {
            char lastChar = value.charAt(value.length() - 1);
            TimeUnit timeUnit;
            switch(lastChar) {
            case 'd':
               timeUnit = TimeUnit.DAYS;
               break;
            case 'h':
               timeUnit = TimeUnit.HOURS;
               break;
            case 'm':
               timeUnit = TimeUnit.MINUTES;
               break;
            case 's':
               timeUnit = TimeUnit.SECONDS;
               break;
            default:
               throw new IllegalArgumentException(CacheBuilderSpec.format("key %s invalid format.  was %s, must end with one of [dDhHmMsS]", key, value));
            }

            long duration = Long.parseLong(value.substring(0, value.length() - 1));
            this.parseDuration(spec, duration, timeUnit);
         } catch (NumberFormatException var8) {
            throw new IllegalArgumentException(CacheBuilderSpec.format("key %s value set to %s, must be integer", key, value));
         }
      }
   }

   static class RecordStatsParser implements CacheBuilderSpec.ValueParser {
      public void parse(CacheBuilderSpec spec, String key, @Nullable String value) {
         Preconditions.checkArgument(value == null, "recordStats does not take values");
         Preconditions.checkArgument(spec.recordStats == null, "recordStats already set");
         spec.recordStats = true;
      }
   }

   static class ValueStrengthParser implements CacheBuilderSpec.ValueParser {
      private final LocalCache.Strength strength;

      public ValueStrengthParser(LocalCache.Strength strength) {
         this.strength = strength;
      }

      public void parse(CacheBuilderSpec spec, String key, @Nullable String value) {
         Preconditions.checkArgument(value == null, "key %s does not take values", (Object)key);
         Preconditions.checkArgument(spec.valueStrength == null, "%s was already set to %s", key, spec.valueStrength);
         spec.valueStrength = this.strength;
      }
   }

   static class KeyStrengthParser implements CacheBuilderSpec.ValueParser {
      private final LocalCache.Strength strength;

      public KeyStrengthParser(LocalCache.Strength strength) {
         this.strength = strength;
      }

      public void parse(CacheBuilderSpec spec, String key, @Nullable String value) {
         Preconditions.checkArgument(value == null, "key %s does not take values", (Object)key);
         Preconditions.checkArgument(spec.keyStrength == null, "%s was already set to %s", key, spec.keyStrength);
         spec.keyStrength = this.strength;
      }
   }

   static class ConcurrencyLevelParser extends CacheBuilderSpec.IntegerParser {
      protected void parseInteger(CacheBuilderSpec spec, int value) {
         Preconditions.checkArgument(spec.concurrencyLevel == null, "concurrency level was already set to ", (Object)spec.concurrencyLevel);
         spec.concurrencyLevel = value;
      }
   }

   static class MaximumWeightParser extends CacheBuilderSpec.LongParser {
      protected void parseLong(CacheBuilderSpec spec, long value) {
         Preconditions.checkArgument(spec.maximumWeight == null, "maximum weight was already set to ", (Object)spec.maximumWeight);
         Preconditions.checkArgument(spec.maximumSize == null, "maximum size was already set to ", (Object)spec.maximumSize);
         spec.maximumWeight = value;
      }
   }

   static class MaximumSizeParser extends CacheBuilderSpec.LongParser {
      protected void parseLong(CacheBuilderSpec spec, long value) {
         Preconditions.checkArgument(spec.maximumSize == null, "maximum size was already set to ", (Object)spec.maximumSize);
         Preconditions.checkArgument(spec.maximumWeight == null, "maximum weight was already set to ", (Object)spec.maximumWeight);
         spec.maximumSize = value;
      }
   }

   static class InitialCapacityParser extends CacheBuilderSpec.IntegerParser {
      protected void parseInteger(CacheBuilderSpec spec, int value) {
         Preconditions.checkArgument(spec.initialCapacity == null, "initial capacity was already set to ", (Object)spec.initialCapacity);
         spec.initialCapacity = value;
      }
   }

   abstract static class LongParser implements CacheBuilderSpec.ValueParser {
      protected abstract void parseLong(CacheBuilderSpec var1, long var2);

      public void parse(CacheBuilderSpec spec, String key, String value) {
         Preconditions.checkArgument(value != null && !value.isEmpty(), "value of key %s omitted", (Object)key);

         try {
            this.parseLong(spec, Long.parseLong(value));
         } catch (NumberFormatException var5) {
            throw new IllegalArgumentException(CacheBuilderSpec.format("key %s value set to %s, must be integer", key, value), var5);
         }
      }
   }

   abstract static class IntegerParser implements CacheBuilderSpec.ValueParser {
      protected abstract void parseInteger(CacheBuilderSpec var1, int var2);

      public void parse(CacheBuilderSpec spec, String key, String value) {
         Preconditions.checkArgument(value != null && !value.isEmpty(), "value of key %s omitted", (Object)key);

         try {
            this.parseInteger(spec, Integer.parseInt(value));
         } catch (NumberFormatException var5) {
            throw new IllegalArgumentException(CacheBuilderSpec.format("key %s value set to %s, must be integer", key, value), var5);
         }
      }
   }

   private interface ValueParser {
      void parse(CacheBuilderSpec var1, String var2, @Nullable String var3);
   }
}
