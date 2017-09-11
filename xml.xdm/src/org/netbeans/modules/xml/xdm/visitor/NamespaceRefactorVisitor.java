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

package org.netbeans.modules.xml.xdm.visitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.NodeImpl;
import org.w3c.dom.NamedNodeMap;

/**
 *
 * @author Nam Nguyen
 */
public class NamespaceRefactorVisitor extends ChildVisitor {
    private String namespace;
    private String prefix;
    private List<Node> path;
    
    // keep track of prefixes used by attributes so we avoid removing 
    // their declaration when the new prefix is default prefix.
    private Set<String> prefixesUsedByAttributesForDefaultNS = new HashSet<String>();
    
    private XDMModel model;
    
    /**
     * @deprecated use constructor with QName-valued attributes map.  Prefix refactoring 
     * should not without complete map.
     */
    public NamespaceRefactorVisitor() {
        this(null);
    }
    
    public NamespaceRefactorVisitor(XDMModel xdmModel) {
        model = xdmModel;
    }
    
    public void refactor(NodeImpl tree, String namespace, String newPrefix, List<Node> ancestors) {
        if (model.getQNameValuedAttributes() == null) return;
        
        assert namespace != null : "Cannot refactor null namespace";
        this.namespace = namespace;
        prefix = newPrefix;
        path = ancestors;
        tree.accept(this);
    }
    
    public void visit(Element e) {
        path.add(0, e);
        NamespaceCheck redec = new NamespaceCheck(prefix, namespace, e);
        if (redec.getPrefixRedeclaration() == null) {
            visitNode(e);
            
            if (namespace.equals(NodeImpl.lookupNamespace(e.getPrefix(), path))) {
                e.setPrefix(prefix);
            }

            for (Attribute sameNamespace : redec.getNamespaceRedeclaration()) {
                String prefixToRemove = sameNamespace.getLocalName();
                if (! prefixesUsedByAttributesForDefaultNS.remove(prefixToRemove)) {
                    e.removeAttributeNode(sameNamespace);
                }
            }
            
            if (redec.getDuplicateDeclaration() != null) {
                e.removeAttributeNode(redec.getDuplicateDeclaration());
            } 
        }
        path.remove(e);
    }
    
    public void visit(Attribute attr) {
        if (attr.isXmlnsAttribute()) return;
        String attrPrefix = attr.getPrefix();

        // default namespace is not applicable for attribute, just have no namespaces.
        if (! isDefaultPrefix(attrPrefix)) {
            if (namespace.equals(NodeImpl.lookupNamespace(attrPrefix, path))) {
                if (isDefaultPrefix(prefix)) {
                    prefixesUsedByAttributesForDefaultNS.add(attrPrefix);
                } else {
                    attr.setPrefix(prefix);
                }
            }
        }        
        if (isQNameValued(attr)) {
            prefixesUsedByAttributesForDefaultNS.addAll(
                    refactorAttributeValue(attr, namespace, prefix, path, model));
        }
    }
    
    public static class NamespaceCheck {
        Attribute duplicate;
        Attribute prefixRedeclaration;
        List<Attribute> namespaceRedeclaredAttributes = new ArrayList<Attribute>();
        public NamespaceCheck(String existingPrefix, String existingNamespace, Element e) {
            init(existingPrefix, existingNamespace, e);
        }
        public Attribute getPrefixRedeclaration() {
            return prefixRedeclaration;
        }
        public List<Attribute> getNamespaceRedeclaration() {
            return namespaceRedeclaredAttributes;
        }
        public Attribute getDuplicateDeclaration() {
            return duplicate;
        }
        private void init(String existingPrefix, String existingNamespace, Element e) {
            NamedNodeMap nnm = e.getAttributes();
            for (int i=0; i<nnm.getLength(); i++) {
                if (! (nnm.item(i) instanceof Attribute))  continue;
                Attribute attr = (Attribute) nnm.item(i);
                if (attr.isXmlnsAttribute()) {
                    Attribute samePrefix = null;
                    Attribute sameNamespace = null;
                    String prefix = attr.getLocalName();
                    if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
                        prefix = XMLConstants.DEFAULT_NS_PREFIX;
                    }
                    if (prefix.equals(existingPrefix)) {
                        samePrefix = attr;
                    }
                    if (existingNamespace.equals(attr.getValue())) {
                        sameNamespace = attr;
                    }
                    if (samePrefix != null && sameNamespace != null) {
                        duplicate = attr;
                    } else if (samePrefix != null) {
                        prefixRedeclaration = attr;
                    } else if (sameNamespace != null) {
                        namespaceRedeclaredAttributes.add(attr);
                    }
                }
            }
        }
    }
    
    private QName getQName(Element node) {
        String ns = NodeImpl.lookupNamespace(node.getPrefix(), path);
        return new QName(ns, node.getLocalName());
    }
    
    private QName getQName(Attribute node) {
        String p = node.getPrefix();
        String ns = (p == null || p.length() == 0) ? null : NodeImpl.lookupNamespace(p, path);
        return new QName(ns, node.getLocalName());
    }

    private boolean isQNameValued(Attribute attr) {
        assert path != null && path.size() > 0;
        Element e = (Element) path.get(0);
        QName elementQName = getQName(e);
        QName attrQName = getQName(attr);
        List<QName> attrQNames = model.getQNameValuedAttributes().get(elementQName);
        if (attrQNames != null) {
            return attrQNames.contains(attrQName);
        }
        return false;
    }
    
    public static boolean isDefaultPrefix(String prefix) {
        return prefix == null || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX);
    }

    private static final Pattern p = Pattern.compile("\\s*(\\S+)\\s*");

    public static List<String> refactorAttributeValue(Attribute attr, 
            String namespace, String prefix, List<Node> context, XDMModel model) 
    {
        ArrayList<String> prefixesUsedForDefaultNS = new ArrayList<String>();
        String value = attr.getValue();
        StringBuilder newValue = null;
        Matcher m = p.matcher(value);
        while (m.find()) {
            String qname = m.group(1);
            String[] parts = qname.split(":");
            if (parts.length > 1) {
                String valuePrefix = parts[0];
                String valueNamespace = context.size() == 1 ?
                    context.get(0).lookupNamespaceURI(valuePrefix) :
                    NodeImpl.lookupNamespace(valuePrefix, context);
                if (namespace.equals(valueNamespace)) {
                    if (isDefaultPrefix(prefix)) {
                        prefixesUsedForDefaultNS.add(valuePrefix);
                    } else {
                        if (newValue == null) newValue = new StringBuilder();
                        newValue.append(prefix);
                        newValue.append(":");
                        newValue.append(parts[1]);
                        newValue.append(" ");
                    }
                }
            }
        }
        if (newValue != null) {
            attr.setValue(newValue.toString().trim());
        }
        return prefixesUsedForDefaultNS;
    }
}
