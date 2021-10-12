package set.hyrts.org.apache.log4j.or.sax;

import org.xml.sax.Attributes;
import set.hyrts.org.apache.log4j.or.ObjectRenderer;

public class AttributesRenderer implements ObjectRenderer {
   public String doRender(Object o) {
      if (o instanceof Attributes) {
         StringBuffer sbuf = new StringBuffer();
         Attributes a = (Attributes)o;
         int len = a.getLength();
         boolean first = true;

         for(int i = 0; i < len; ++i) {
            if (first) {
               first = false;
            } else {
               sbuf.append(", ");
            }

            sbuf.append(a.getQName(i));
            sbuf.append('=');
            sbuf.append(a.getValue(i));
         }

         return sbuf.toString();
      } else {
         try {
            return o.toString();
         } catch (Exception var7) {
            return var7.toString();
         }
      }
   }
}
