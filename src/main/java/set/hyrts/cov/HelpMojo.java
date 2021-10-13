package set.hyrts.cov;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "help", requiresProject = false, threadSafe = true)
public class HelpMojo extends AbstractMojo {
    private static final String PLUGIN_HELP_PATH = "/META-INF/maven/org.hyrts/hyrts-maven-plugin/plugin-help.xml";
    private static final int DEFAULT_LINE_LENGTH = 80;
    @Parameter(property = "detail", defaultValue = "false")
    private boolean detail;
    @Parameter(property = "goal")
    private String goal;
    @Parameter(property = "lineLength", defaultValue = "80")
    private int lineLength;
    @Parameter(property = "indentSize", defaultValue = "2")
    private int indentSize;

    private static boolean isNotEmpty(String string) {
        return (string != null && string.length() > 0);
    }

    private static String repeat(String str, int repeat) {
        StringBuilder buffer = new StringBuilder(repeat * str.length());
        for (int i = 0; i < repeat; i++)
            buffer.append(str);
        return buffer.toString();
    }

    private static List<String> toLines(String text, int indent, int indentSize, int lineLength) {
        List<String> lines = new ArrayList<>();
        String ind = repeat("\t", indent);
        String[] plainLines = text.split("(\r\n)|(\r)|(\n)");
        for (String plainLine : plainLines)
            toLines(lines, ind + plainLine, indentSize, lineLength);
        return lines;
    }

    private static void toLines(List<String> lines, String line, int indentSize, int lineLength) {
        int lineIndent = getIndentLevel(line);
        StringBuilder buf = new StringBuilder(256);
        String[] tokens = line.split(" +");
        for (String token : tokens) {
            if (buf.length() > 0)
                if (buf.length() + token.length() >= lineLength) {
                    lines.add(buf.toString());
                    buf.setLength(0);
                    buf.append(repeat(" ", lineIndent * indentSize));
                } else {
                    buf.append(' ');
                }
            for (int j = 0; j < token.length(); j++) {
                char c = token.charAt(j);
                if (c == '\t') {
                    buf.append(repeat(" ", indentSize - buf.length() % indentSize));
                } else if (c == ' ') {
                    buf.append(' ');
                } else {
                    buf.append(c);
                }
            }
        }
        lines.add(buf.toString());
    }

    private static int getIndentLevel(String line) {
        int level = 0;
        int i;
        for (i = 0; i < line.length() && line.charAt(i) == '\t'; i++)
            level++;
        for (i = level + 1; i <= level + 4 && i < line.length(); i++) {
            if (line.charAt(i) == '\t') {
                level++;
                break;
            }
        }
        return level;
    }

    private Document build() throws MojoExecutionException {
        getLog().debug("load plugin-help.xml: /META-INF/maven/org.hyrts/hyrts-maven-plugin/plugin-help.xml");
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream("/META-INF/maven/org.hyrts/hyrts-maven-plugin/plugin-help.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            return dBuilder.parse(is);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (SAXException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (IOException e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
        }
    }

    public void execute() throws MojoExecutionException {
        if (this.lineLength <= 0) {
            getLog().warn("The parameter 'lineLength' should be positive, using '80' as default.");
            this.lineLength = 80;
        }
        if (this.indentSize <= 0) {
            getLog().warn("The parameter 'indentSize' should be positive, using '2' as default.");
            this.indentSize = 2;
        }
        Document doc = build();
        StringBuilder sb = new StringBuilder();
        Node plugin = getSingleChild(doc, "plugin");
        String name = getValue(plugin, "name");
        String version = getValue(plugin, "version");
        String id = getValue(plugin, "groupId") + ":" + getValue(plugin, "artifactId") + ":" + version;
        if (isNotEmpty(name) && !name.contains(id)) {
            append(sb, name + " " + version, 0);
        } else if (isNotEmpty(name)) {
            append(sb, name, 0);
        } else {
            append(sb, id, 0);
        }
        append(sb, getValue(plugin, "description"), 1);
        append(sb, "", 0);
        String goalPrefix = getValue(plugin, "goalPrefix");
        Node mojos1 = getSingleChild(plugin, "mojos");
        List<Node> mojos = findNamedChild(mojos1, "mojo");
        if (this.goal == null || this.goal.length() <= 0) {
            append(sb, "This plugin has " + mojos.size() + ((mojos.size() > 1) ? " goals:" : " goal:"), 0);
            append(sb, "", 0);
        }
        for (Node mojo : mojos)
            writeGoal(sb, goalPrefix, (Element) mojo);
        if (getLog().isInfoEnabled())
            getLog().info(sb.toString());
    }

    private String getValue(Node node, String elementName) throws MojoExecutionException {
        return getSingleChild(node, elementName).getTextContent();
    }

    private Node getSingleChild(Node node, String elementName) throws MojoExecutionException {
        List<Node> namedChild = findNamedChild(node, elementName);
        if (namedChild.isEmpty())
            throw new MojoExecutionException("Could not find " + elementName + " in plugin-help.xml");
        if (namedChild.size() > 1)
            throw new MojoExecutionException("Multiple " + elementName + " in plugin-help.xml");
        return namedChild.get(0);
    }

    private List<Node> findNamedChild(Node node, String elementName) {
        List<Node> result = new ArrayList<>();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (elementName.equals(item.getNodeName()))
                result.add(item);
        }
        return result;
    }

    private Node findSingleChild(Node node, String elementName) throws MojoExecutionException {
        List<Node> elementsByTagName = findNamedChild(node, elementName);
        if (elementsByTagName.isEmpty())
            return null;
        if (elementsByTagName.size() > 1)
            throw new MojoExecutionException("Multiple " + elementName + "in plugin-help.xml");
        return elementsByTagName.get(0);
    }

    private void writeGoal(StringBuilder sb, String goalPrefix, Element mojo) throws MojoExecutionException {
        String mojoGoal = getValue(mojo, "goal");
        Node configurationElement = findSingleChild(mojo, "configuration");
        Node description = findSingleChild(mojo, "description");
        if (this.goal == null || this.goal.length() <= 0 || mojoGoal.equals(this.goal)) {
            append(sb, goalPrefix + ":" + mojoGoal, 0);
            Node deprecated = findSingleChild(mojo, "deprecated");
            if (deprecated != null && isNotEmpty(deprecated.getTextContent())) {
                append(sb, "Deprecated. " + deprecated.getTextContent(), 1);
                if (this.detail && description != null) {
                    append(sb, "", 0);
                    append(sb, description.getTextContent(), 1);
                }
            } else if (description != null) {
                append(sb, description.getTextContent(), 1);
            }
            append(sb, "", 0);
            if (this.detail) {
                Node parametersNode = getSingleChild(mojo, "parameters");
                List<Node> parameters = findNamedChild(parametersNode, "parameter");
                append(sb, "Available parameters:", 1);
                append(sb, "", 0);
                for (Node parameter : parameters)
                    writeParameter(sb, parameter, configurationElement);
            }
        }
    }

    private void writeParameter(StringBuilder sb, Node parameter, Node configurationElement) throws MojoExecutionException {
        String parameterName = getValue(parameter, "name");
        String parameterDescription = getValue(parameter, "description");
        Element fieldConfigurationElement = null;
        if (configurationElement != null)
            fieldConfigurationElement = (Element) findSingleChild(configurationElement, parameterName);
        String parameterDefaultValue = "";
        if (fieldConfigurationElement != null && fieldConfigurationElement.hasAttribute("default-value"))
            parameterDefaultValue = " (Default: " + fieldConfigurationElement.getAttribute("default-value") + ")";
        append(sb, parameterName + parameterDefaultValue, 2);
        Node deprecated = findSingleChild(parameter, "deprecated");
        if (deprecated != null && isNotEmpty(deprecated.getTextContent())) {
            append(sb, "Deprecated. " + deprecated.getTextContent(), 3);
            append(sb, "", 0);
        }
        append(sb, parameterDescription, 3);
        if ("true".equals(getValue(parameter, "required")))
            append(sb, "Required: Yes", 3);
        if (fieldConfigurationElement != null && isNotEmpty(fieldConfigurationElement.getTextContent())) {
            String property = getPropertyFromExpression(fieldConfigurationElement.getTextContent());
            append(sb, "User property: " + property, 3);
        }
        append(sb, "", 0);
    }

    private void append(StringBuilder sb, String description, int indent) {
        for (String line : toLines(description, indent, this.indentSize, this.lineLength))
            sb.append(line).append('\n');
    }

    private String getPropertyFromExpression(String expression) {
        if (expression != null && expression.startsWith("${") && expression.endsWith("}") &&
                !expression.substring(2).contains("${"))
            return expression.substring(2, expression.length() - 1);
        return null;
    }
}
