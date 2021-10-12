package set.hyrts.org.apache.commons.codec.language.bm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rule {
   public static final Rule.RPattern ALL_STRINGS_RMATCHER = new Rule.RPattern() {
      public boolean isMatch(CharSequence input) {
         return true;
      }
   };
   public static final String ALL = "ALL";
   private static final String DOUBLE_QUOTE = "\"";
   private static final String HASH_INCLUDE = "#include";
   private static final Map<NameType, Map<RuleType, Map<String, Map<String, List<Rule>>>>> RULES = new EnumMap(NameType.class);
   private final Rule.RPattern lContext;
   private final String pattern;
   private final Rule.PhonemeExpr phoneme;
   private final Rule.RPattern rContext;

   private static boolean contains(CharSequence chars, char input) {
      for(int i = 0; i < chars.length(); ++i) {
         if (chars.charAt(i) == input) {
            return true;
         }
      }

      return false;
   }

   private static String createResourceName(NameType nameType, RuleType rt, String lang) {
      return String.format("set/hyrts/org/apache/commons/codec/language/bm/%s_%s_%s.txt", nameType.getName(), rt.getName(), lang);
   }

   private static Scanner createScanner(NameType nameType, RuleType rt, String lang) {
      String resName = createResourceName(nameType, rt, lang);
      InputStream rulesIS = Languages.class.getClassLoader().getResourceAsStream(resName);
      if (rulesIS == null) {
         throw new IllegalArgumentException("Unable to load resource: " + resName);
      } else {
         return new Scanner(rulesIS, "UTF-8");
      }
   }

   private static Scanner createScanner(String lang) {
      String resName = String.format("set/hyrts/org/apache/commons/codec/language/bm/%s.txt", lang);
      InputStream rulesIS = Languages.class.getClassLoader().getResourceAsStream(resName);
      if (rulesIS == null) {
         throw new IllegalArgumentException("Unable to load resource: " + resName);
      } else {
         return new Scanner(rulesIS, "UTF-8");
      }
   }

   private static boolean endsWith(CharSequence input, CharSequence suffix) {
      if (suffix.length() > input.length()) {
         return false;
      } else {
         int i = input.length() - 1;

         for(int j = suffix.length() - 1; j >= 0; --j) {
            if (input.charAt(i) != suffix.charAt(j)) {
               return false;
            }

            --i;
         }

         return true;
      }
   }

   public static List<Rule> getInstance(NameType nameType, RuleType rt, Languages.LanguageSet langs) {
      Map<String, List<Rule>> ruleMap = getInstanceMap(nameType, rt, langs);
      List<Rule> allRules = new ArrayList();
      Iterator i$ = ruleMap.values().iterator();

      while(i$.hasNext()) {
         List<Rule> rules = (List)i$.next();
         allRules.addAll(rules);
      }

      return allRules;
   }

   public static List<Rule> getInstance(NameType nameType, RuleType rt, String lang) {
      return getInstance(nameType, rt, Languages.LanguageSet.from(new HashSet(Arrays.asList(lang))));
   }

   public static Map<String, List<Rule>> getInstanceMap(NameType nameType, RuleType rt, Languages.LanguageSet langs) {
      return langs.isSingleton() ? getInstanceMap(nameType, rt, langs.getAny()) : getInstanceMap(nameType, rt, "any");
   }

   public static Map<String, List<Rule>> getInstanceMap(NameType nameType, RuleType rt, String lang) {
      Map<String, List<Rule>> rules = (Map)((Map)((Map)RULES.get(nameType)).get(rt)).get(lang);
      if (rules == null) {
         throw new IllegalArgumentException(String.format("No rules found for %s, %s, %s.", nameType.getName(), rt.getName(), lang));
      } else {
         return rules;
      }
   }

   private static Rule.Phoneme parsePhoneme(String ph) {
      int open = ph.indexOf("[");
      if (open >= 0) {
         if (!ph.endsWith("]")) {
            throw new IllegalArgumentException("Phoneme expression contains a '[' but does not end in ']'");
         } else {
            String before = ph.substring(0, open);
            String in = ph.substring(open + 1, ph.length() - 1);
            Set<String> langs = new HashSet(Arrays.asList(in.split("[+]")));
            return new Rule.Phoneme(before, Languages.LanguageSet.from(langs));
         }
      } else {
         return new Rule.Phoneme(ph, Languages.ANY_LANGUAGE);
      }
   }

   private static Rule.PhonemeExpr parsePhonemeExpr(String ph) {
      if (!ph.startsWith("(")) {
         return parsePhoneme(ph);
      } else if (!ph.endsWith(")")) {
         throw new IllegalArgumentException("Phoneme starts with '(' so must end with ')'");
      } else {
         List<Rule.Phoneme> phs = new ArrayList();
         String body = ph.substring(1, ph.length() - 1);
         String[] arr$ = body.split("[|]");
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String part = arr$[i$];
            phs.add(parsePhoneme(part));
         }

         if (body.startsWith("|") || body.endsWith("|")) {
            phs.add(new Rule.Phoneme("", Languages.ANY_LANGUAGE));
         }

         return new Rule.PhonemeList(phs);
      }
   }

   private static Map<String, List<Rule>> parseRules(Scanner scanner, final String location) {
      Map<String, List<Rule>> lines = new HashMap();
      final int currentLine = 0;
      boolean inMultilineComment = false;

      while(scanner.hasNextLine()) {
         ++currentLine;
         String rawLine = scanner.nextLine();
         String line = rawLine;
         if (inMultilineComment) {
            if (rawLine.endsWith("*/")) {
               inMultilineComment = false;
            }
         } else if (rawLine.startsWith("/*")) {
            inMultilineComment = true;
         } else {
            int cmtI = rawLine.indexOf("//");
            if (cmtI >= 0) {
               line = rawLine.substring(0, cmtI);
            }

            line = line.trim();
            if (line.length() != 0) {
               if (line.startsWith("#include")) {
                  String incl = line.substring("#include".length()).trim();
                  if (incl.contains(" ")) {
                     throw new IllegalArgumentException("Malformed import statement '" + rawLine + "' in " + location);
                  }

                  lines.putAll(parseRules(createScanner(incl), location + "->" + incl));
               } else {
                  String[] parts = line.split("\\s+");
                  if (parts.length != 4) {
                     throw new IllegalArgumentException("Malformed rule statement split into " + parts.length + " parts: " + rawLine + " in " + location);
                  }

                  try {
                     String pat = stripQuotes(parts[0]);
                     String lCon = stripQuotes(parts[1]);
                     String rCon = stripQuotes(parts[2]);
                     Rule.PhonemeExpr ph = parsePhonemeExpr(stripQuotes(parts[3]));
                     Rule r = new Rule(pat, lCon, rCon, ph) {
                        private final int myLine = currentLine;
                        private final String loc = location;

                        public String toString() {
                           StringBuilder sb = new StringBuilder();
                           sb.append("Rule");
                           sb.append("{line=").append(this.myLine);
                           sb.append(", loc='").append(this.loc).append('\'');
                           sb.append('}');
                           return sb.toString();
                        }
                     };
                     String patternKey = r.pattern.substring(0, 1);
                     List<Rule> rules = (List)lines.get(patternKey);
                     if (rules == null) {
                        rules = new ArrayList();
                        lines.put(patternKey, rules);
                     }

                     ((List)rules).add(r);
                  } catch (IllegalArgumentException var17) {
                     throw new IllegalStateException("Problem parsing line '" + currentLine + "' in " + location, var17);
                  }
               }
            }
         }
      }

      return lines;
   }

   private static Rule.RPattern pattern(final String regex) {
      boolean startsWith = regex.startsWith("^");
      boolean endsWith = regex.endsWith("$");
      final String content = regex.substring(startsWith ? 1 : 0, endsWith ? regex.length() - 1 : regex.length());
      boolean boxes = content.contains("[");
      if (!boxes) {
         if (startsWith && endsWith) {
            if (content.length() == 0) {
               return new Rule.RPattern() {
                  public boolean isMatch(CharSequence input) {
                     return input.length() == 0;
                  }
               };
            }

            return new Rule.RPattern() {
               public boolean isMatch(CharSequence input) {
                  return input.equals(content);
               }
            };
         }

         if ((startsWith || endsWith) && content.length() == 0) {
            return ALL_STRINGS_RMATCHER;
         }

         if (startsWith) {
            return new Rule.RPattern() {
               public boolean isMatch(CharSequence input) {
                  return Rule.startsWith(input, content);
               }
            };
         }

         if (endsWith) {
            return new Rule.RPattern() {
               public boolean isMatch(CharSequence input) {
                  return Rule.endsWith(input, content);
               }
            };
         }
      } else {
         boolean startsWithBox = content.startsWith("[");
         boolean endsWithBox = content.endsWith("]");
         if (startsWithBox && endsWithBox) {
            final String boxContent = content.substring(1, content.length() - 1);
            if (!boxContent.contains("[")) {
               boolean negate = boxContent.startsWith("^");
               if (negate) {
                  boxContent = boxContent.substring(1);
               }

               final boolean shouldMatch = !negate;
               if (startsWith && endsWith) {
                  return new Rule.RPattern() {
                     public boolean isMatch(CharSequence input) {
                        return input.length() == 1 && Rule.contains(boxContent, input.charAt(0)) == shouldMatch;
                     }
                  };
               }

               if (startsWith) {
                  return new Rule.RPattern() {
                     public boolean isMatch(CharSequence input) {
                        return input.length() > 0 && Rule.contains(boxContent, input.charAt(0)) == shouldMatch;
                     }
                  };
               }

               if (endsWith) {
                  return new Rule.RPattern() {
                     public boolean isMatch(CharSequence input) {
                        return input.length() > 0 && Rule.contains(boxContent, input.charAt(input.length() - 1)) == shouldMatch;
                     }
                  };
               }
            }
         }
      }

      return new Rule.RPattern() {
         Pattern pattern = Pattern.compile(regex);

         public boolean isMatch(CharSequence input) {
            Matcher matcher = this.pattern.matcher(input);
            return matcher.find();
         }
      };
   }

   private static boolean startsWith(CharSequence input, CharSequence prefix) {
      if (prefix.length() > input.length()) {
         return false;
      } else {
         for(int i = 0; i < prefix.length(); ++i) {
            if (input.charAt(i) != prefix.charAt(i)) {
               return false;
            }
         }

         return true;
      }
   }

   private static String stripQuotes(String str) {
      if (str.startsWith("\"")) {
         str = str.substring(1);
      }

      if (str.endsWith("\"")) {
         str = str.substring(0, str.length() - 1);
      }

      return str;
   }

   public Rule(String pattern, String lContext, String rContext, Rule.PhonemeExpr phoneme) {
      this.pattern = pattern;
      this.lContext = pattern(lContext + "$");
      this.rContext = pattern("^" + rContext);
      this.phoneme = phoneme;
   }

   public Rule.RPattern getLContext() {
      return this.lContext;
   }

   public String getPattern() {
      return this.pattern;
   }

   public Rule.PhonemeExpr getPhoneme() {
      return this.phoneme;
   }

   public Rule.RPattern getRContext() {
      return this.rContext;
   }

   public boolean patternAndContextMatches(CharSequence input, int i) {
      if (i < 0) {
         throw new IndexOutOfBoundsException("Can not match pattern at negative indexes");
      } else {
         int patternLength = this.pattern.length();
         int ipl = i + patternLength;
         if (ipl > input.length()) {
            return false;
         } else if (!input.subSequence(i, ipl).equals(this.pattern)) {
            return false;
         } else {
            return !this.rContext.isMatch(input.subSequence(ipl, input.length())) ? false : this.lContext.isMatch(input.subSequence(0, i));
         }
      }
   }

   static {
      NameType[] arr$ = NameType.values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         NameType s = arr$[i$];
         Map<RuleType, Map<String, Map<String, List<Rule>>>> rts = new EnumMap(RuleType.class);
         RuleType[] arr$ = RuleType.values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            RuleType rt = arr$[i$];
            Map<String, Map<String, List<Rule>>> rs = new HashMap();
            Languages ls = Languages.getInstance(s);
            Iterator i$ = ls.getLanguages().iterator();

            while(i$.hasNext()) {
               String l = (String)i$.next();

               try {
                  rs.put(l, parseRules(createScanner(s, rt, l), createResourceName(s, rt, l)));
               } catch (IllegalStateException var14) {
                  throw new IllegalStateException("Problem processing " + createResourceName(s, rt, l), var14);
               }
            }

            if (!rt.equals(RuleType.RULES)) {
               rs.put("common", parseRules(createScanner(s, rt, "common"), createResourceName(s, rt, "common")));
            }

            rts.put(rt, Collections.unmodifiableMap(rs));
         }

         RULES.put(s, Collections.unmodifiableMap(rts));
      }

   }

   public interface RPattern {
      boolean isMatch(CharSequence var1);
   }

   public static final class PhonemeList implements Rule.PhonemeExpr {
      private final List<Rule.Phoneme> phonemes;

      public PhonemeList(List<Rule.Phoneme> phonemes) {
         this.phonemes = phonemes;
      }

      public List<Rule.Phoneme> getPhonemes() {
         return this.phonemes;
      }
   }

   public interface PhonemeExpr {
      Iterable<Rule.Phoneme> getPhonemes();
   }

   public static final class Phoneme implements Rule.PhonemeExpr {
      public static final Comparator<Rule.Phoneme> COMPARATOR = new Comparator<Rule.Phoneme>() {
         public int compare(Rule.Phoneme o1, Rule.Phoneme o2) {
            for(int i = 0; i < o1.phonemeText.length(); ++i) {
               if (i >= o2.phonemeText.length()) {
                  return 1;
               }

               int c = o1.phonemeText.charAt(i) - o2.phonemeText.charAt(i);
               if (c != 0) {
                  return c;
               }
            }

            if (o1.phonemeText.length() < o2.phonemeText.length()) {
               return -1;
            } else {
               return 0;
            }
         }
      };
      private final StringBuilder phonemeText;
      private final Languages.LanguageSet languages;

      public Phoneme(CharSequence phonemeText, Languages.LanguageSet languages) {
         this.phonemeText = new StringBuilder(phonemeText);
         this.languages = languages;
      }

      public Phoneme(Rule.Phoneme phonemeLeft, Rule.Phoneme phonemeRight) {
         this((CharSequence)phonemeLeft.phonemeText, (Languages.LanguageSet)phonemeLeft.languages);
         this.phonemeText.append(phonemeRight.phonemeText);
      }

      public Phoneme(Rule.Phoneme phonemeLeft, Rule.Phoneme phonemeRight, Languages.LanguageSet languages) {
         this((CharSequence)phonemeLeft.phonemeText, (Languages.LanguageSet)languages);
         this.phonemeText.append(phonemeRight.phonemeText);
      }

      public Rule.Phoneme append(CharSequence str) {
         this.phonemeText.append(str);
         return this;
      }

      public Languages.LanguageSet getLanguages() {
         return this.languages;
      }

      public Iterable<Rule.Phoneme> getPhonemes() {
         return Collections.singleton(this);
      }

      public CharSequence getPhonemeText() {
         return this.phonemeText;
      }

      /** @deprecated */
      @Deprecated
      public Rule.Phoneme join(Rule.Phoneme right) {
         return new Rule.Phoneme(this.phonemeText.toString() + right.phonemeText.toString(), this.languages.restrictTo(right.languages));
      }
   }
}
