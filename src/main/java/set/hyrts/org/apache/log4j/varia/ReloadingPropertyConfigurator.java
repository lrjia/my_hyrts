package set.hyrts.org.apache.log4j.varia;

import java.io.InputStream;
import java.net.URL;
import set.hyrts.org.apache.log4j.PropertyConfigurator;
import set.hyrts.org.apache.log4j.spi.Configurator;
import set.hyrts.org.apache.log4j.spi.LoggerRepository;

public class ReloadingPropertyConfigurator implements Configurator {
   PropertyConfigurator delegate = new PropertyConfigurator();

   public void doConfigure(InputStream inputStream, LoggerRepository repository) {
   }

   public void doConfigure(URL url, LoggerRepository repository) {
   }
}
