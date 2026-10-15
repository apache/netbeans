/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.xml.xdm.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.w3c.dom.NodeList;

/**
 * Finder for nodes from a document using arbitrary XPath expression.
 *
 * @author Nam Nguyen
 */
public class XPathFinder extends ChildVisitor {
    
    /**
     * @return an xpath that can be used to retrieve the given node using #findNode
     * @param root root node
     * @param target Element or Attribute node to generate the xpath for.
     */
    public static String getXpath(Document root, Node target) {
        if (!(target instanceof Element) && !(target instanceof Attribute)) {
            throw new IllegalArgumentException("Can only get XPath expression for Element or Attribute");
        }
        List<Node> result = new PathFromRootVisitor().findPath(root, target);
        StringBuffer sb = new StringBuffer(128);
        Element parent = null;
        for (int j=result.size()-1; j >= 0; j--) {
            Node n = result.get(j);
            boolean isElement = n instanceof Element;
            boolean isAttribute = n instanceof Attribute;
            if (! isElement && ! isAttribute) {
                continue;
            }
            sb.append(SEP);
            int index = isElement ? xpathIndexOf(parent, (Element) n) : -1;
            String prefix = n.getPrefix();
            String namespace = n.lookupNamespaceURI(prefix);
            if (isAttribute) {
                sb.append(AT);
            }
            if (prefix != null) {
                sb.append(prefix);
                sb.append(':');
            }
            sb.append(n.getLocalName());
            if (isElement && index > 0) {
                sb.append(BRACKET0);
                sb.append(String.valueOf(index));
                sb.append(BRACKET1);
            }
            if (isAttribute) {
                break;
            }
            parent = (Element) n;
        }
        return sb.toString();
    }
    
    /**
     * @return the node correspond to specified xpath, if provided xpath result
     * in more than one nodes, only the first on get returned.
     * @param root context in which to find the node.
     * @param xpath location path for the attribute or element.
     */
    public Node findNode(Document root, String xpath) {
        List<Node> nodes = findNodes(root, xpath);
        return nodes.size() > 0 ? nodes.get(0) : null;
    }
    
    /**
     * @return the nodes correspond to specified xpath.
     * @param root context in which to find the node.
     * @param xpathExpression location path for the attribute or element.
     */
    public List<Node> findNodes(Document root, String xpathExpression) {
        if (root == null || root.getDocumentElement() == null) {
            return Collections.emptyList();
        }
        
        init(root, xpathExpression);
        if (! isReadyForEvaluation()) {
            return new ArrayList<Node>();
        }
        
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(getNamespaceContext());
        NodeList result = null;
        try {
            result = (NodeList) xpath.evaluate(getFixedUpXpath(), root, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e.getCause().getLocalizedMessage());
        }
        assert(result != null);
        List<Node> ret = new ArrayList<Node>();
        for (int i=0; i<result.getLength(); i++) {
            ret.add((Node) result.item(i));
        }
        return ret;
    }
    
    // 0-based index for xpath
    private static int xpathIndexOf(Node parentNode, Element child) {
        assert(child != null);
        if (!(parentNode instanceof Element || parentNode instanceof Document)) {
            return -1;
        }

        NodeList children = parentNode.getChildNodes();
        int index = 0;
        String namespace = child.getNamespaceURI();
        String name = child.getLocalName();
        for (int i = 0; i<children.getLength(); i++) {
            if (! (children.item(i) instanceof Element)) {
                continue;
            }
            Element n = (Element) children.item(i);
            if (namespace != null && ! namespace.equals(n.getNamespaceURI()) ||
                namespace == null && n.getNamespaceURI() != null) 
            {
                continue;
            }
            
            if (name.equals(n.getLocalName())) {
                index++;
            }
            
            if (n.getId() == child.getId()) {
                return index;
            }
        }
        
        return -1;
    }
    
    private void init(Document root, String xpath) {
        currentParent = root;
        initTokens(xpath);
        ((Element)root.getDocumentElement()).accept(this);
    }
    
    private void initTokens(String xpath) {
        tokens = new ArrayList<XPathSegment>();
        StringTokenizer tokenizer = new StringTokenizer(xpath, SEP);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            tokens.add(new XPathSegment(token));
        }
    }
    
    private static class XPathSegment {
        private final String token;
        private String prefix;
        private String localPart;
        int index = -1;
        private String remaining;
        private boolean isAttribute;
        private boolean hasConditions;
        
        XPathSegment(String token) {
            this.token = token;
            parse();
        }
             
        private void parse() {
            StringTokenizer tokenizer = new StringTokenizer(token, ":[");
            List<String> parts = new ArrayList<String>();
            while (tokenizer.hasMoreTokens()) {
                String t = tokenizer.nextToken();
                parts.add(t);
            }
            if (parts.size() == 1) {
                prefix = "";
                localPart = parts.get(0);
                remaining = "";
            } else if (parts.size() == 3) {
                prefix = parts.get(0);
                localPart = parts.get(1);
                remaining = parts.get(2);
            } else if (parts.size() == 2) {
                String part0 = parts.get(0);
                if (token.charAt(part0.length()) == ':') {
                    prefix = part0;
                    localPart = parts.get(1);
                    remaining = "";
                } else {
                    localPart = parts.get(0);
                    remaining = parts.get(1);
                    prefix = "";
                }
            }
            if (prefix != null && prefix.startsWith(AT)) {
                isAttribute = true;
                prefix = prefix.substring(1);
            } else if (localPart.startsWith(AT)) {
                isAttribute = true;
                localPart = localPart.substring(1);
            }
            if (! remaining.equals("")) {
                String indexString = remaining.substring(0, remaining.length()-1);
                try {
                    index = Integer.parseInt(indexString);
                } catch (NumberFormatException ex) {
                    hasConditions = true;
                }
                remaining = BRACKET0 + remaining;
            }
        }
        
        public boolean checkTypeAndName(Node e, Node parent) {
            if (e instanceof Element) {
                if (isAttribute()) return false;
                if (hasPlainIndex() && getIndex() != xpathIndexOf(parent, (Element) e)) {
                    return false;
                }
            } else if (e instanceof Attribute) {
                if (! isAttribute()) return false;
            } else {
                return false;
            }
            if (! e.getLocalName().equals(getLocalPart())){
                return false;
            }
            return true;
        }
             
        public void addToXpath(StringBuffer xpath, String prefix) {
            xpath.append(SEP);
            if (isAttribute()) {
                xpath.append(AT);
            }
            xpath.append(prefix);
            xpath.append(COLON);
            xpath.append(localPart);
            if (hasConditions() && !prefix.startsWith(XPNS)) {
                String prefixing = AT + prefix + COLON;
                //TODO: a better RE to skip those already prefixed
                xpath.append(remaining.replaceAll(AT, prefixing));
            } else {
                xpath.append(remaining);
            }
        }
             
        public void addToXpath(StringBuffer xpath) {
            xpath.append(SEP);
            xpath.append(token);
        }
             
        public String getPrefix() { return prefix;  }
        public String getLocalPart() { return localPart; }
        public boolean hasPlainIndex() { return index > -1; }
        public boolean hasConditions() { return hasConditions; }
        public int getIndex() { return index; }
        public String getRemaining() { return remaining; }
        public String getToken() { return token; }
        public boolean isAttribute() { return isAttribute; }
        public String toString() { return token; }
    }
             
    protected void visitNode(Node e) {
        if (tokens.size() == 0) {
            done = true;
            return;
        }
             
        XPathSegment segment = tokens.get(0);
        if (! segment.checkTypeAndName(e, currentParent)) {
            return; // not matched
        }
             
        String currentNamespace = e.getNamespaceURI();
        if (currentNamespace != null) {
            String prefix = segment.getPrefix();
            if (prefix.length() > 0) {
                String namespace = e.lookupNamespaceURI(prefix);
                if (namespace == null) {
                    namespace = namespaces.get(prefix);
                } if (namespace != null && ! currentNamespace.equals(namespace)) {
                    return; // not matched
                }
                segment.addToXpath(fixedUpXpath);
            } else {
                // no prefix, make up one
                prefix = prefixes.get(currentNamespace);
                if (prefix == null) {
                    prefix = XPNS + String.valueOf(countNamespaceSuffix++);
                }
                segment.addToXpath(fixedUpXpath, prefix);
            }
            // push current namespace context
            prefixes.put(currentNamespace, prefix);
            namespaces.put(prefix, currentNamespace);
        } else {
            segment.addToXpath(fixedUpXpath);
        }
             
        tokens.remove(0);
        currentParent = e;
        super.visitNode(e);
    }
             
    public boolean isReadyForEvaluation() {
        return done;
    }
    public NamespaceContext getNamespaceContext() {
        return new HashNamespaceResolver(namespaces, prefixes);
    }
    private String getFixedUpXpath() {
        return fixedUpXpath.toString();
    }
             
    private boolean done = false;
    private Node currentParent = null;
    private Map<String, String> namespaces = new HashMap<String,String>();
    private Map<String, String> prefixes = new HashMap<String,String>();
    private List<XPathSegment> tokens;
    private StringBuffer fixedUpXpath = new StringBuffer();
             
    public static final String SEP = "/"; //NOI18N
    public static final String AT = "@";  //NOI18N
    public static final String COLON = ":"; //NOI18N
    public static final String BRACKET0 = "["; //NOI18N
    public static final String BRACKET1 = "]"; //NOI18N
    public static final String XPNS = "xpns"; //NOI18N
    private int countNamespaceSuffix = 0;
}
