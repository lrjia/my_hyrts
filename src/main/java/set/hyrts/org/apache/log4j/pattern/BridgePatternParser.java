package set.hyrts.org.apache.log4j.pattern;

public final class BridgePatternParser extends set.hyrts.org.apache.log4j.helpers.PatternParser {
   public BridgePatternParser(String conversionPattern) {
      super(conversionPattern);
   }

   public set.hyrts.org.apache.log4j.helpers.PatternConverter parse() {
      return new BridgePatternConverter(this.pattern);
   }
}
