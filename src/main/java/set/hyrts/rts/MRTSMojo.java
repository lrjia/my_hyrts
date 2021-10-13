package set.hyrts.rts;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import set.hyrts.common.AbstractCoverageMojo;
import set.hyrts.utils.Properties;

@Deprecated
@Execute(phase = LifecyclePhase.TEST_COMPILE)
public class MRTSMojo extends AbstractCoverageMojo {
    public void execute() throws MojoExecutionException {
        this.RTS = Properties.RTSVariant.MRTS;
        this.coverageLevel = "meth-cov";
        super.execute();
    }
}
