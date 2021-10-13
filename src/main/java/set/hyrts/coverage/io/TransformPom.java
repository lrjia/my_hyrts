package set.hyrts.coverage.io;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;

public class TransformPom {
    public static void main(String[] args) throws Exception {
        String inPath = args[0];
        String outPath = args[1];
        String prefix = args[2];
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(inPath);
        Element argLine = doc.createElement("argLine");
        argLine.appendChild(doc.createTextNode("-javaagent:/home/lingming/hybrid-rts/hyrts-maven/target/hyrts-maven-1.0-SNAPSHOT.jar=prefix=" + prefix + " -Xss400m"));
        Element properties = doc.createElement("properties");
        Element property = doc.createElement("property");
        Element name = doc.createElement("name");
        name.appendChild(doc.createTextNode("listener"));
        Element value = doc.createElement("value");
        value.appendChild(doc.createTextNode("set.hyrts.coverage.core.TestMethodListener"));
        property.appendChild(name);
        property.appendChild(value);
        properties.appendChild(property);
        Element configuration = doc.createElement("configuration");
        configuration.appendChild(argLine);
        configuration.appendChild(properties);
        Element plugin = doc.createElement("plugin");
        Element groupId = doc.createElement("groupId");
        groupId.appendChild(doc.createTextNode("org.apache.maven.plugins"));
        Element artifactId = doc.createElement("artifactId");
        artifactId.appendChild(doc.createTextNode("maven-surefire-plugin"));
        plugin.appendChild(groupId);
        plugin.appendChild(artifactId);
        plugin.appendChild(configuration);
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr1 = xpath.compile("//project/build/plugins");
        NodeList nlist = (NodeList) expr1.evaluate(doc, XPathConstants.NODESET);
        Node plugins = nlist.item(0);
        NodeList list = plugins.getChildNodes();
        boolean hasSurefire = false;

        for (int i = 0; i < list.getLength(); ++i) {
            Node node = list.item(i);
            if ("plugin".equals(node.getNodeName())) {
                for (int j = 0; j < node.getChildNodes().getLength(); ++j) {
                    if ("maven-surefire-plugin".equals(node.getChildNodes().item(j).getTextContent())) {
                        hasSurefire = true;
                        boolean hasConfig = false;

                        for (int k = 0; k < node.getChildNodes().getLength(); ++k) {
                            Node n = node.getChildNodes().item(k);
                            if (n.getNodeName().equals("configuration")) {
                                hasConfig = true;
                                n.appendChild(argLine);
                                n.appendChild(properties);
                                break;
                            }
                        }

                        if (!hasConfig) {
                            node.appendChild(configuration);
                        }
                        break;
                    }
                }
            }
        }

        if (!hasSurefire) {
            plugins.appendChild(plugin);
        }

        XPathExpression project_expr = xpath.compile("//project");
        NodeList proj_list = (NodeList) project_expr.evaluate(doc, XPathConstants.NODESET);
        Node proj = proj_list.item(0);
        XPathExpression expr2 = xpath.compile("//project/dependencies");
        NodeList nlist2 = (NodeList) expr2.evaluate(doc, XPathConstants.NODESET);
        Node dependencies = nlist2.item(0);
        if (dependencies == null) {
            dependencies = doc.createElement("dependencies");
            proj.appendChild((Node) dependencies);
        }

        Element dependency = doc.createElement("dependency");
        Element dependencyGroup = doc.createElement("groupId");
        dependencyGroup.appendChild(doc.createTextNode("set.hyrts"));
        Element dependencyArtifact = doc.createElement("artifactId");
        dependencyArtifact.appendChild(doc.createTextNode("hyrts-maven"));
        Element dependencyVersion = doc.createElement("version");
        dependencyVersion.appendChild(doc.createTextNode("1.0-SNAPSHOT"));
        dependency.appendChild(dependencyGroup);
        dependency.appendChild(dependencyArtifact);
        dependency.appendChild(dependencyVersion);
        ((Node) dependencies).appendChild(dependency);

        for (int i = 0; i < ((Node) dependencies).getChildNodes().getLength(); ++i) {
            Node node = ((Node) dependencies).getChildNodes().item(i);
            if ("dependency".equals(node.getNodeName())) {
                boolean isJunit = false;

                int j;
                for (j = 0; j < node.getChildNodes().getLength(); ++j) {
                    if (node.getChildNodes().item(j).getNodeName().equals("groupId") && node.getChildNodes().item(j).getTextContent().equals("junit")) {
                        isJunit = true;
                        break;
                    }
                }

                if (isJunit) {
                    for (j = 0; j < node.getChildNodes().getLength(); ++j) {
                        if (node.getChildNodes().item(j).getNodeName().equals("version")) {
                            node.getChildNodes().item(j).setTextContent("4.12");
                        }
                    }
                }
            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(outPath));
        transformer.transform(source, result);
    }
}
