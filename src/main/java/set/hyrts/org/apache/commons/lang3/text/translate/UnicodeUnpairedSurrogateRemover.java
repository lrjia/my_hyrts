package set.hyrts.org.apache.commons.lang3.text.translate;

import java.io.IOException;
import java.io.Writer;

/** @deprecated */
@Deprecated
public class UnicodeUnpairedSurrogateRemover extends CodePointTranslator {
   public boolean translate(int codepoint, Writer out) throws IOException {
      return codepoint >= 55296 && codepoint <= 57343;
   }
}
