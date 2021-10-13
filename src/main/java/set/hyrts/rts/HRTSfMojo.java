package set.hyrts.rts;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import set.hyrts.common.AbstractCoverageMojo;
import set.hyrts.utils.Properties;

@Mojo(name = "HyRTSf", requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST_COMPILE)
public class HRTSfMojo extends AbstractCoverageMojo {
  public void execute() throws MojoExecutionException {
    this.RTS = Properties.RTSVariant.HyRTSf;
    this.coverageLevel = "meth-cov";
    this.hybridLevel = "jk";
    super.execute();
  }
}
