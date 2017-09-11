/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.ant.grammar;

import java.io.File;
import java.io.StringReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.xml.api.model.GrammarResult;
import org.netbeans.modules.xml.api.model.HintContext;
import org.openide.modules.InstalledFileLocator;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

/**
 * Helpers for AntGrammarTest.
 * @author Jesse Glick
 */
final class TestUtil {
    
    private TestUtil() {}
    
    static {
        MockServices.setServices(IFL.class);
    }
    
    public static final class IFL extends InstalledFileLocator {
        public File locate(String name, String module, boolean loc) {
            String antHomeS = System.getProperty("test.ant.home");
            if (antHomeS == null) {
                throw new Error("Tests will not run unless test.ant.home and test.ant.bridge system properties are defined");
            }
            final File antHome = new File(antHomeS);
            final File antBridge = new File(System.getProperty("test.ant.bridge"));
            final File antJar = new File(new File(antHome, "lib"), "ant.jar");
            if (name.equals("ant")) {
                return antHome;
            } else if (name.equals("ant/nblib/bridge.jar")) {
                return antBridge;
            } else if (name.equals("ant/nblib")) {
                return antBridge.getParentFile();
            } else if (name.equals("ant/lib/ant.jar")) {
                return antJar;
            } else {
                return null;
            }
        }
    }
    
    private static HintContext createHintContext(final Node n, final String prefix) {
        Set<Class> interfaces = new HashSet<Class>();
        findAllInterfaces(n.getClass(), interfaces);
        interfaces.add(HintContext.class);
        class Handler implements InvocationHandler {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getDeclaringClass().equals(HintContext.class)) {
                    assert method.getName().equals("getCurrentPrefix");
                    return prefix;
                } else {
                    return method.invoke(n, args);
                }
            }
        }
        return (HintContext)Proxy.newProxyInstance(TestUtil.class.getClassLoader(), interfaces.toArray(new Class[interfaces.size()]), new Handler());
    }
    
    static void findAllInterfaces(Class c, Set<Class> interfaces) {
        if (c.isInterface()) {
            interfaces.add(c);
        }
        Class s = c.getSuperclass();
        if (s != null) {
            findAllInterfaces(s, interfaces);
        }
        Class[] is = c.getInterfaces();
        for (int i = 0; i < is.length; i++) {
            findAllInterfaces(is[i], interfaces);
        }
    }
    
    /**
     * Create a context for completing some XML.
     * The XML text must be a well-formed document.
     * It must contain exactly one element name, attribute name,
     * attribute value, or text node ending in the string <samp>HERE</samp>.
     * The context will be that node (Element, Attribute, or Text) with
     * the suffix stripped off and the prefix set to the text preceding that suffix.
     */
    public static HintContext createCompletion(String xml) throws Exception {
        Document doc = XMLUtil.parse(new InputSource(new StringReader(xml)), false, true, null, null);
        return findCompletion(doc.getDocumentElement(), doc);
    }
    
    private static HintContext findCompletion(Node n, Document doc) {
        switch (n.getNodeType()) {
            case Node.ELEMENT_NODE:
                Element el = (Element)n;
                String name = el.getTagName();
                if (name.endsWith("HERE")) {
                    String prefix = name.substring(0, name.length() - 4);
                    Node nue = doc.createElementNS(el.getNamespaceURI(), prefix);
                    NodeList nl = el.getChildNodes();
                    while (nl.getLength() > 0) {
                        nue.appendChild(nl.item(0));
                    }
                    el.getParentNode().replaceChild(nue, el);
                    return createHintContext(nue, prefix);
                }
                break;
            case Node.TEXT_NODE:
                Text text = (Text)n;
                String contents = text.getNodeValue();
                if (contents.endsWith("HERE")) {
                    String prefix = contents.substring(0, contents.length() - 4);
                    text.setNodeValue(prefix);
                    return createHintContext(text, prefix);
                }
                break;
            case Node.ATTRIBUTE_NODE:
                Attr attr = (Attr)n;
                name = attr.getName();
                if (name.endsWith("HERE")) {
                    String prefix = name.substring(0, name.length() - 4);
                    Attr nue = doc.createAttributeNS(attr.getNamespaceURI(), prefix);
                    Element owner = attr.getOwnerElement();
                    owner.removeAttributeNode(attr);
                    owner.setAttributeNodeNS(nue);
                    return createHintContext(nue, prefix);
                } else {
                    String value = attr.getNodeValue();
                    if (value.endsWith("HERE")) {
                        String prefix = value.substring(0, value.length() - 4);
                        attr.setNodeValue(prefix);
                        return createHintContext(attr, prefix);
                    }
                }
                break;
            default:
                // ignore
                break;
        }
        // Didn't find it, check children.
        NodeList nl = n.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            HintContext c = findCompletion(nl.item(i), doc);
            if (c != null) {
                return c;
            }
        }
        // Element's attr nodes are listed separately.
        NamedNodeMap nnm = n.getAttributes();
        if (nnm != null) {
            for (int i = 0; i < nnm.getLength(); i++) {
                HintContext c = findCompletion(nnm.item(i), doc);
                if (c != null) {
                    return c;
                }
            }
        }
        // Nope.
        return null;
    }
    
    /**
     * Get a particular element in a test XML document.
     * Pass in a well-formed XML document and an element name to search for.
     * Must be exactly one such.
     */
    public static Element createElementInDocument(String xml, String elementName, String elementNamespace) throws Exception {
        Document doc = XMLUtil.parse(new InputSource(new StringReader(xml)), false, true, null, null);
        NodeList nl = doc.getElementsByTagNameNS(elementNamespace, elementName);
        if (nl.getLength() != 1) {
            throw new IllegalArgumentException("Zero or more than one <" + elementName + ">s in \"" + xml + "\"");
        }
        return (Element)nl.item(0);
    }
    
    /**
     * Given a list of XML nodes returned in GrammarResult's, return a list of their names.
     * For elements, you get the name; for attributes, the name;
     * for text nodes, the value.
     * (No namespaces returned.)
     */
    public static List<String> grammarResultValues(Enumeration<GrammarResult> e) {
        List<String> l = new ArrayList<String>();
        while (e.hasMoreElements()) {
            l.add(e.nextElement().toString());
        }
        return l;
    }
    
}
