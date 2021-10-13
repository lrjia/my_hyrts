package set.hyrts.common;

import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "clean")
public class CleanMojo extends AbstractCoverageMojo {
  public void execute() throws MojoExecutionException {
    File file = new File(this.baseDir + File.separator + "hyrts-files");
    delete(file);
  }
  
  public void delete(File file) {
    if (file.isDirectory())
      for (File childFile : file.listFiles())
        delete(childFile);  
    file.delete();
  }
}
