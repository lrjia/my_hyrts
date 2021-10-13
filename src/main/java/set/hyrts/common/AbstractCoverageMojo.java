package set.hyrts.common;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.surefire.SurefirePlugin;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;
import set.hyrts.coverage.maven.DynamicAgentLoader;
import set.hyrts.diff.VersionDiff;
import set.hyrts.utils.Properties;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

public class AbstractCoverageMojo extends SurefirePlugin {
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    public File outputDirectory;

    @Parameter(property = "project")
    protected MavenProject mavenProject;

    @Parameter(defaultValue = "${basedir}")
    protected String baseDir;

    @Parameter(defaultValue = "1.0-SNAPSHOT")
    protected String version;

    @Parameter(defaultValue = "meth-cov")
    protected String coverageLevel;

    @Parameter(property = "RTS", defaultValue = "NONE")
    protected Properties.RTSVariant RTS;

    @Parameter(property = "testMethLevel", defaultValue = "false")
    protected boolean testMethLevel;

    @Parameter(property = "execOnly", defaultValue = "false")
    protected boolean execOnly;

    @Parameter(property = "hybridLevel")
    protected String hybridLevel;

    @Parameter(property = "oldVersion", defaultValue = "${basedir}")
    protected String oldVersionLocation;

    @Parameter(property = "traceLib", defaultValue = "false")
    protected boolean traceLib;

    @Parameter(property = "traceRTInfo", defaultValue = "true")
    protected boolean traceRTInfo;

    @Parameter(property = "agentJar", defaultValue = "")
    protected String agentJar;

    @Parameter(property = "skipIT", defaultValue = "false")
    protected boolean skipIT;

    @Parameter(property = "debug", defaultValue = "false")
    protected boolean debug;

    @Component
    protected MavenSession mavenSession;

    @Component
    protected BuildPluginManager pluginManager;

    protected Plugin surefire;

    protected Plugin failsafe;

    public void execute() throws MojoExecutionException {
        this.agentJar = fasterGetPathToHyRTSJar();
        String arguments = getConfigs();
        if (this.RTS != Properties.RTSVariant.NONE)
            RTSUtil.getRTSRes(this);
        DynamicAgentLoader.loadDynamicAgent(this.agentJar, arguments);
        runSurefire();
        if (!this.skipIT)
            runIntegrationTests();
    }

    private void runSurefire() throws MojoExecutionException {
        this
                .surefire = lookupPlugin("org.apache.maven.plugins:maven-surefire-plugin");
        if (this.surefire == null)
            getLog().error("Make sure surefire is in your pom.xml!");
        Xpp3Dom domNode = (Xpp3Dom) this.surefire.getConfiguration();
        if (domNode == null)
            domNode = new Xpp3Dom("configuration");
        MojoExecutor.executeMojo(this.surefire, "test", domNode,
                MojoExecutor.executionEnvironment(this.mavenProject, this.mavenSession, this.pluginManager));
    }

    private void runIntegrationTests() throws MojoExecutionException {
        this
                .failsafe = lookupPlugin("org.apache.maven.plugins:maven-failsafe-plugin");
        if (this.failsafe != null) {
            Xpp3Dom failsafeDomNode = (Xpp3Dom) this.failsafe.getConfiguration();
            if (failsafeDomNode == null)
                failsafeDomNode = new Xpp3Dom("configuration");
            MojoExecutor.executeMojo(this.failsafe, "integration-test", failsafeDomNode,

                    MojoExecutor.executionEnvironment(this.mavenProject, this.mavenSession, this.pluginManager));
        }
    }

    private Xpp3Dom transformPom(Xpp3Dom domNode) {
        getLog().info("outputDir: " + this.outputDirectory.getAbsolutePath());
        getLog().info("current project: " + this.mavenProject.toString());
        getLog().info("baseDir: " + this.baseDir);
        if (domNode == null)
            domNode = new Xpp3Dom("configuration");
        getLog().info("Original surefire config: \n" + domNode.toString());
        if (domNode.getChild("argLine") != null) {
            String str = domNode.getChild("argLine").getValue();
        } else {
            domNode.addChild(new Xpp3Dom("argLine"));
        }
        String hyrtsArgLine = getHyRTSArgLine();
        domNode.getChild("argLine").setValue(hyrtsArgLine);
        getLog().info(this.mavenProject + "\n" + this.mavenSession + "\n" + this.pluginManager);
        getLog().info("Modified surefire config: \n" + domNode.toString());
        return domNode;
    }

    private String getHyRTSArgLine() {
        String jarLocation = getPathToHyRTSJar();
        String arguments = getConfigs();
        return "-javaagent:" + jarLocation + "=" + arguments;
    }

    private String getConfigs() {
        String testLevel = this.testMethLevel ? "test-meth" : "test-class";
        if (testLevel.equals("test-meth") || "branch-cov"
                .equals(this.coverageLevel) || "stmt-cov"
                .equals(this.coverageLevel))
            this.RTS = Properties.RTSVariant.NONE;
        if (this.RTS != Properties.RTSVariant.NONE)
            this.coverageLevel = this.RTS + "-" + this.coverageLevel;
        if (this.hybridLevel != null)
            readHybridConfig(this.hybridLevel);
        String arguments = Properties.DEBUG_MODE_KEY + this.debug + "," + Properties.RTS_KEY + this.RTS + "," + ((this.coverageLevel != null) ? (Properties.COV_TYPE_KEY + this.coverageLevel + ",") : "") + Properties.TEST_LEVEL_KEY + testLevel + "," + Properties.TRACE_LIB_KEY + this.traceLib + "," + Properties.TRACE_RTINFO_KEY + this.traceRTInfo + "," + Properties.OLD_DIR_KEY + this.oldVersionLocation + "," + Properties.NEW_DIR_KEY + this.baseDir.toString() + "," + Properties.NEW_CLASSPATH_KEY + this.outputDirectory + "," + Properties.EXECUTION_ONLY_KEY + this.execOnly;
        if (this.debug) {
            PrintStream printer = System.out;
            printer.println("[HyRTS] ===============Plugin Config===============");
            printer.println("[HyRTS] RTS type: " + this.RTS);
            printer.println("[HyRTS] Coverage level: " + this.coverageLevel);
            printer.println("[HyRTS] Test level: " + (this.testMethLevel ? "test-meth" : "test-class"));
            printer.println("[HyRTS] Trace library: " + this.traceLib);
            printer.println("[HyRTS] With runTime info?: " + this.traceRTInfo);
            printer.println("[HyRTS] Old version location: " + this.oldVersionLocation);
            printer.println("[HyRTS] New version location: " + this.baseDir);
            printer.println("[HyRTS] New version classpath location: " + this.outputDirectory);
            printer.println("[HyRTS] Hybrid config: " + VersionDiff.transDSI + "|" + VersionDiff.transASI + "|" + VersionDiff.transCSI + "|" + VersionDiff.transDI + "|" + VersionDiff.transAI + "|" + VersionDiff.transCI + "|" + VersionDiff.transDSM + "|" + VersionDiff.transASM + "|" + VersionDiff.transCSM + "|" + VersionDiff.transDIM + "|" + VersionDiff.transAIM + "|" + VersionDiff.transCIM + "|");
            printer.println("[HyRTS] Execution only config: " + this.execOnly);
        }
        return arguments;
    }

    private Plugin lookupPlugin(String paramString) {
        List<Plugin> localList = this.mavenProject.getBuildPlugins();
        Iterator<Plugin> localIterator = localList.iterator();
        while (localIterator.hasNext()) {
            Plugin localPlugin = localIterator.next();
            if (paramString.equalsIgnoreCase(localPlugin.getKey()))
                return localPlugin;
        }
        return null;
    }

    private String getPathToHyRTSJar() {
        getHyRTSVersion();
        String localRepo = this.mavenSession.getSettings().getLocalRepository();
        String result = Paths.get(this.mavenSession.getLocalRepository().getBasedir(), new String[]{"set", "hyrts", "hyrts-core", this.version, "hyrts-core-" + this.version + ".jar"}).toString();
        return result;
    }

    private String fasterGetPathToHyRTSJar() {
        File jarFile = new File(Properties.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String agentJar = jarFile.toString();
        return agentJar;
    }

    private void getHyRTSVersion() {
        Artifact artifact = (Artifact) this.mavenProject.getPluginArtifactMap().get("set.hyrts:hyrts-maven-plugin");
        this.version = artifact.getVersion();
    }

    public void readHybridConfig(String hybridInfo) {
        for (int i = 0; i < hybridInfo.length(); i++) {
            char tag = hybridInfo.charAt(i);
            if (tag <= 'z' && tag >= 'a')
                switch (tag) {
                    case 'a':
                        VersionDiff.transDSI = true;
                        break;
                    case 'b':
                        VersionDiff.transASI = true;
                        break;
                    case 'c':
                        VersionDiff.transCSI = true;
                        break;
                    case 'd':
                        VersionDiff.transDI = true;
                        break;
                    case 'e':
                        VersionDiff.transAI = true;
                        break;
                    case 'f':
                        VersionDiff.transCI = true;
                        break;
                    case 'g':
                        VersionDiff.transDSM = true;
                        break;
                    case 'h':
                        VersionDiff.transASM = true;
                        break;
                    case 'i':
                        VersionDiff.transCSM = true;
                        break;
                    case 'j':
                        VersionDiff.transDIM = true;
                        break;
                    case 'k':
                        VersionDiff.transAIM = true;
                        break;
                    case 'l':
                        VersionDiff.transCIM = true;
                        break;
                }
        }
        if (VersionDiff.transAIM && VersionDiff.transDIM)
            this.traceRTInfo = false;
    }
}
