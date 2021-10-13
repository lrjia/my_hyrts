package set.hyrts.coverage.io;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TransformPomJUnitVersion {
   public static void main(String[] args) throws Exception {
      String inPath = args[0];
      String outPath = args[1];
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
      Document doc = docBuilder.parse(inPath);
      XPath xpath = XPathFactory.newInstance().newXPath();
      String[] pluginPaths = new String[]{"//project/build/pluginManagement/plugins", "//project/build/plugins"};
      String[] var8 = pluginPaths;
      int var9 = pluginPaths.length;

      int j;
      for(int var10 = 0; var10 < var9; ++var10) {
         String pluginPath = var8[var10];
         XPathExpression expr1 = xpath.compile(pluginPath);
         NodeList nlist = (NodeList)expr1.evaluate(doc, XPathConstants.NODESET);
         Node plugins = nlist.item(0);
         if (plugins != null) {
            NodeList list = plugins.getChildNodes();

            for(int i = 0; i < list.getLength(); ++i) {
               Node node = list.item(i);
               if ("plugin".equals(node.getNodeName())) {
                  for(j = 0; j < node.getChildNodes().getLength(); ++j) {
                     if ("maven-surefire-plugin".equals(node.getChildNodes().item(j).getTextContent())) {
                        boolean containsVersion = false;
                        String surefireVer = "2.19.1";

                        for(j = 0; j < node.getChildNodes().getLength(); ++j) {
                           Node n = node.getChildNodes().item(j);
                           if (!n.getNodeName().equals("configuration")) {
                              if (n.getNodeName().equals("version")) {
                                 n.setTextContent(surefireVer);
                                 containsVersion = true;
                              }
                           } else {
                              Set<Node> toRemove = new HashSet();

                              Node del;
                              for(int l = 0; l < n.getChildNodes().getLength(); ++l) {
                                 del = n.getChildNodes().item(l);
                                 if (del.getNodeName().equals("argLine") || del.getNodeName().toLowerCase().contains("fork") || del.getNodeName().equals("skip") || del.getNodeName().equals("parallel") || del.getNodeName().equals("threadCount") || del.getNodeName().equals("perCoreThreadCount")) {
                                    toRemove.add(del);
                                 }
                              }

                              Iterator var43 = toRemove.iterator();

                              while(var43.hasNext()) {
                                 del = (Node)var43.next();
                                 n.removeChild(del);
                              }
                           }
                        }

                        if (!containsVersion) {
                           Node v = doc.createElement("version");
                           v.setTextContent(surefireVer);
                           node.appendChild(v);
                        }
                        break;
                     }
                  }
               }
            }
         }
      }

      XPathExpression project_expr = xpath.compile("//project");
      NodeList proj_list = (NodeList)project_expr.evaluate(doc, XPathConstants.NODESET);
      Node proj = proj_list.item(0);
      XPathExpression expr2 = xpath.compile("//project/dependencies");
      NodeList nlist2 = (NodeList)expr2.evaluate(doc, XPathConstants.NODESET);
      Node dependencies = nlist2.item(0);
      if (dependencies == null) {
         dependencies = doc.createElement("dependencies");
         proj.appendChild((Node)dependencies);
      }

      Element dependency = doc.createElement("dependency");
      Element dependencyGroup = doc.createElement("groupId");
      dependencyGroup.appendChild(doc.createTextNode("org.ow2.asm"));
      Element dependencyArtifact = doc.createElement("artifactId");
      dependencyArtifact.appendChild(doc.createTextNode("asm"));
      Element dependencyVersion = doc.createElement("version");
      dependencyVersion.appendChild(doc.createTextNode("5.0.3"));
      dependency.appendChild(dependencyGroup);
      dependency.appendChild(dependencyArtifact);
      dependency.appendChild(dependencyVersion);
      ((Node)dependencies).insertBefore(dependency, ((Node)dependencies).getFirstChild());

      for(j = 0; j < ((Node)dependencies).getChildNodes().getLength(); ++j) {
         Node node = ((Node)dependencies).getChildNodes().item(j);
         if ("dependency".equals(node.getNodeName())) {
            boolean isJunit = false;

            for(j = 0; j < node.getChildNodes().getLength(); ++j) {
               if (node.getChildNodes().item(j).getNodeName().equals("groupId") && node.getChildNodes().item(j).getTextContent().equals("junit")) {
                  isJunit = true;
                  break;
               }
            }

            if (isJunit) {
               for(j = 0; j < node.getChildNodes().getLength(); ++j) {
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
