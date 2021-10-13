package set.hyrts.cov;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import set.hyrts.common.AbstractCoverageMojo;

@Mojo(name = "stmtCov", requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST_COMPILE)
public class StmtCovMojo extends AbstractCoverageMojo {
  public void execute() throws MojoExecutionException {
    this.coverageLevel = "stmt-cov";
    super.execute();
  }
}
