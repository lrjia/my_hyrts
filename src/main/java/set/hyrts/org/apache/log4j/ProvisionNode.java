package set.hyrts.org.apache.log4j;

import java.util.Vector;

class ProvisionNode extends Vector {
   private static final long serialVersionUID = -4479121426311014469L;

   ProvisionNode(Logger logger) {
      this.addElement(logger);
   }
}