package set.hyrts.org.apache.log4j.spi;

import set.hyrts.org.apache.log4j.or.ObjectRenderer;
import set.hyrts.org.apache.log4j.or.RendererMap;

public interface RendererSupport {
   RendererMap getRendererMap();

   void setRenderer(Class var1, ObjectRenderer var2);
}
