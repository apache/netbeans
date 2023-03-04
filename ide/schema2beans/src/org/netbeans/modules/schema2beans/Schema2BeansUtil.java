/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.schema2beans;

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.Writer;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author pfiala
 */
public class Schema2BeansUtil {

    /**
     * Write the current schema2beans graph as an XML document without reformating
     *
     * @param bean
     * @param writer
     * @throws IOException
     */
    public static void write(BaseBean bean, Writer writer) throws IOException {
        GraphManager graphManager = bean.graphManager();
        if (graphManager != null) {
            graphManager.write(writer);
        } else {
            throw new IllegalStateException(Common.getMessage("CantWriteBeanNotInDOMTree_msg"));
        }
    }

    /**
     * Write the current schema2beans graph as an XML document without reformating
     *
     * @param bean
     * @param outputStream
     * @throws IOException
     */
    public static void write(BaseBean bean, OutputStream outputStream) throws IOException {
        try {
            GraphManager graphManager = bean.graphManager();
            if (graphManager != null) {
                graphManager.write(outputStream);
            } else {
                throw new IllegalStateException(Common.getMessage("CantWriteBeanNotInDOMTree_msg"));
            }
        } catch (Schema2BeansException e) {
            throw new Schema2BeansRuntimeException(e);
        }
    }

    /**
     * Merge "unsupported" elements - elements which are not represented
     * in model (whitespaces, comments, unknown atributes and tags),
     * but they should be merged to DOMBinding
     *
     * @param patternBean
     */
    public static void mergeUnsupportedElements(BaseBean bean, BaseBean patternBean) {
        Map nodeMap = new HashMap();
        List l = new LinkedList();
        l.add(bean);
        bean.childBeans(true, l);
        l.add(patternBean);
        patternBean.childBeans(true, l);
        for (Iterator it = l.iterator(); it.hasNext();) {
            BaseBean baseBean = (BaseBean) it.next();
            DOMBinding binding = baseBean.binding;
            if (binding != null) {
                nodeMap.put(binding.getNode(), baseBean);
            }
        }
        mergeNode(nodeMap, getOwnerDocument(bean.binding.getNode()), getOwnerDocument(patternBean.binding.getNode()));
    }

    private static void mergeBeans(Map nodeMap, BaseBean bean, BaseBean patternBean) {
        if (bean.binding != null && patternBean.binding != null) {
            Node node = bean. binding.getNode();
            Node otherNode = patternBean.binding.getNode();
            if (node != null && otherNode != null) {
                mergeNode(nodeMap, node, otherNode);
            }
        }
    }

    /**
     * Retrieves owner document of a node
     *
     * @param node
     * @return the owner document
     */
    private static Document getOwnerDocument(Node node) {
        return node instanceof Document ? (Document) node : node.getOwnerDocument();
    }

    /**
     * Merge "unsupported" attributes and child elements of node
     *
     * @param nodeMap
     * @param node
     * @param patternNode
     */
    private static void mergeNode(Map nodeMap, Node node, Node patternNode) {
        mergeAttributes(node, patternNode);
        NodeList childNodes = node.getChildNodes();
        List children = relevantNodes(childNodes);
        Document document = getOwnerDocument(node);
        for (int i = childNodes.getLength() - 1; i >= 0; i--) {//remove all comments and break lines from current document node
            Node childNode = childNodes.item(i);
            if (!isRelevantNode(childNode)) {
                node.removeChild(childNode);
            }
        }
        NodeList patternChildNodes = patternNode.getChildNodes();
        for (int i = 0; i < patternChildNodes.getLength(); i++) {
            Node patternChild = patternChildNodes.item(i);
            Node currentChild = childNodes.item(i);
            if (isRelevantNode(patternChild)) {
                BaseBean patternBean = (BaseBean) nodeMap.get(patternChild);
                BaseBean foundBean;
                Node foundChild;
                if (patternBean != null) {
                    foundBean = takeEqualBean(nodeMap, children, patternBean);
                    foundChild = foundBean == null ? null : foundBean.binding.getNode();
                } else {
                    foundBean = null;
                    foundChild = null;
                }
                mergeChildNodes(node, foundChild, foundBean, currentChild, patternChild, nodeMap, document, patternBean, children);
            } else {
                mergeChildNodes(node, null, null, currentChild, patternChild, nodeMap, document, null, children);
            }
        }
    }
    
    /**
     * Merge child nodes
     * 
     * @param node     current node base
     * @param childNode child node if exist
     * @param foundBean child bean
     * @param currentChild current child node
     * @param patternChild current pattern child
     * @param nodeMap node map
     * @param document document
     * @param patternBean pattern bean
     * @param children list relevant childs current node base
     */
    private static void mergeChildNodes(Node node, Node childNode, BaseBean foundBean,
            Node currentChild, Node patternChild, Map nodeMap, Document document,
            BaseBean patternBean, List children) {
        Node foundChild = childNode;
        if (foundChild == null) {
            foundChild = takeEqualNode(children, patternChild);
        }
        if (foundChild != null) {
            if (foundChild != currentChild) {
                node.removeChild(foundChild);
                node.insertBefore(foundChild, currentChild);
            }
            if (foundBean != null) {
                mergeBeans(nodeMap, foundBean, patternBean);
            } else if (isRelevantNode(foundChild) && foundChild.hasChildNodes()) {
                mergeNode(nodeMap, foundChild, patternChild);
            } else {
                foundChild.setNodeValue(patternChild.getNodeValue());
            }
        } else {
            Node child = document.importNode(patternChild, true);
            node.insertBefore(child, currentChild);
        }
    }

    /**
     * Merge "unsupported" attributes of a node
     *
     * @param node
     * @param otherNode
     */
    private static void mergeAttributes(Node node, Node otherNode) {
        NamedNodeMap attributes = node.getAttributes();
        NamedNodeMap otherAttributes = otherNode.getAttributes();
        if (attributes == null) {
            if (otherAttributes == null) {
                return;
            } else {
                TraceLogger.error(
                        "Attributes merge error: " + node.getClass().getName() + "  " + otherNode.getClass().getName());
                return;
            }
        } else if (otherAttributes == null) {
            TraceLogger.error(
                    "Attributes merge error: " + node.getClass().getName() + "  " + otherNode.getClass().getName());
            return;
        }
        List names = new LinkedList();
        for (int i = 0; i < attributes.getLength(); i++) {
            names.add(attributes.item(i).getNodeName());
        }
        for (Iterator it = names.iterator(); it.hasNext();) {
            String name = (String) it.next();
            if (otherAttributes.getNamedItem(name) == null) {
                attributes.removeNamedItem(name);
            }
        }
        Document document = getOwnerDocument(node);
        for (int i = 0; i < otherAttributes.getLength(); i++) {
            Node newAttribute = otherAttributes.item(i);
            String name = newAttribute.getNodeName();
            String value = newAttribute.getNodeValue();
            Node currentAttribute = attributes.getNamedItem(name);
            if (currentAttribute == null) {
                currentAttribute = document.createAttribute(name);
                attributes.setNamedItem(currentAttribute);
            }
            currentAttribute.setNodeValue(value);
        }
    }

    /**
     * Search list of nodes for related bean that is equivalent of given pattern
     *
     * @param nodeMap
     * @param nodes
     * @param patternBean
     * @return the bean
     */
    private static BaseBean takeEqualBean(Map nodeMap, List nodes, BaseBean patternBean) {
        for (Iterator it = nodes.iterator(); it.hasNext();) {
            Node node = (Node) it.next();
            BaseBean bean = (BaseBean) nodeMap.get(node);
            if (bean != null && bean.isEqualTo(patternBean)) {
                it.remove();
                return bean;
            }
        }
        return null;
    }

    /**
     * Search list of nodes for node that is equivalent of given pattern
     *
     * @param nodes
     * @param patternNode
     * @return the node if found otherwise null
     */
    private static Node takeEqualNode(List nodes, Node patternNode) {
        List result = filterNodes(nodes, patternNode);
        if (result.size() == 0) {
            return null;
        } else {
            Node node = (Node) result.get(0);
            nodes.remove(node);
            return node;
        }
    }

    private static List filterNodes(List nodes, Node patternNode) {
        List trueList = new LinkedList();
        String name = patternNode.getNodeName();
        for (Iterator it = nodes.iterator(); it.hasNext();) {
            Node node = (Node) it.next();
            if (name.equals(node.getNodeName())) {
                trueList.add(node);
            }
        }
        if (trueList.size() <= 1) {
            return trueList;
        }

        List falseList = new LinkedList();
        for (Iterator it = trueList.iterator(); it.hasNext();) {
            Node node = (Node) it.next();
            if (patternNode.equals(node)) {
                it.remove();
                falseList.add(node);
            }
        }
        if (trueList.size() == 1) {
            return trueList;
        } else if (trueList.isEmpty()) {
            trueList = falseList;
        }

        falseList = new LinkedList();
        String value = patternNode.getNodeValue();
        for (Iterator it = trueList.iterator(); it.hasNext();) {
            Node node = (Node) it.next();
            if (!equals(value, node.getNodeValue())) {
                it.remove();
                falseList.add(node);
            }
        }
        if (trueList.size() == 1) {
            return trueList;
        } else if (trueList.isEmpty()) {
            trueList = falseList;
        }

        if (patternNode.getChildNodes().getLength() == 1) {
            Node child = patternNode.getFirstChild();
            if (child.getNodeType() == Node.TEXT_NODE) {
                for (Iterator it = trueList.iterator(); it.hasNext();) {
                    Node node = (Node) it.next();
                    boolean keep = false;
                    if (node.getChildNodes().getLength() == 1) {
                        Node otherChild = node.getFirstChild();
                        keep = otherChild.getNodeType() == Node.TEXT_NODE
                                && equals(child.getTextContent(), otherChild.getTextContent());
                    }
                    if (!keep) {
                        it.remove();
                        falseList.add(node);
                    }
                }
            }
        }
        if (trueList.size() == 1) {
            return trueList;
        } else if (trueList.isEmpty()) {
            trueList = falseList;
        }

        falseList = new LinkedList();
        NamedNodeMap attributes = patternNode.getAttributes();
        for (Iterator it = trueList.iterator(); it.hasNext();) {
            Node node = (Node) it.next();
            if (!equals(attributes, node.getAttributes())) {
                it.remove();
                falseList.add(node);
            }
        }
        if (trueList.isEmpty()) {
            return falseList;
        } else {
            return trueList;
        }
    }

    /**
     * Test strings for equivalency
     *
     * @param s1
     * @param s2
     * @return true if they are equivalent, otherwise false
     */
    private static boolean equals(String s1, String s2) {
        return s1 == null ? s2 == null : !(s2 == null) && s1.trim().equals(s2.trim());

    }

    /**
     * Test attributes for equivalency
     *
     * @param attributes1
     * @param attributes2
     * @return true if they are equivalent, otherwise false
     */
    private static boolean equals(NamedNodeMap attributes1, NamedNodeMap attributes2) {
        if (attributes1 == null || attributes2 == null) {
            return attributes1 == attributes2;
        } else {
            int n = attributes1.getLength();
            if (n != attributes2.getLength()) {
                return false;
            }
            for (int i = 0; i < n; i++) {
                Node attr1 = attributes1.item(i);
                Node attr2 = attributes2.getNamedItem(attr1.getNodeName());
                if (attr2 == null || !attr2.getNodeValue().equals(attr2.getNodeValue())) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Provides list of relevant nodes
     *
     * @param nodeList
     * @return list of relevant nodes
     */
    private static List relevantNodes(NodeList nodeList) {
        List list = new LinkedList();
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (isRelevantNode(node)) {
                    list.add(node);
                }
            }
        }
        return list;
    }

    /**
     * Tests whether the node is relevant - not comment nor text node containing white spaces solely
     *
     * @param node
     * @return true if  the node is relevant
     */
    private static boolean isRelevantNode(Node node) {
        short type = node.getNodeType();
        return type != Node.COMMENT_NODE && !isWhiteSpaceNode(node);
    }

    /**
     * Tests whether the node contains white spaces solely
     *
     * @param node
     * @return true if node contains only white spaces, otherwise false
     */
    private static boolean isWhiteSpaceNode(Node node) {
        short type = node.getNodeType();
        return (type == Node.TEXT_NODE && node.getNodeValue().trim().length() == 0);
    }

    /**
     * Searches node corresponding to the path
     * @param bean
     * @param path
     * @return the node if found, otherwise null
     */
    private static Node findNode(BaseBean bean, String path) {
        if (path.startsWith(bean.fullName())) {
            BaseBean matchingChild;
            while ((matchingChild = getMatchingChild(bean, path)) != null) {
                bean = matchingChild;
            }
            if (path.equals(bean.fullName())) {
                return bean.binding.node;
            }
            BeanProp[] beanProps = bean.beanProps();
            for (int i = 0; i < beanProps.length; i++) {
                BeanProp prop = beanProps[i];
                int n = prop.bindingsSize();
                for (int j = 0; j < n; j++) {
                    if (path.equals(prop.getFullName(j))) {
                        return prop.getBinding(j).node;
                    }
                }
            }
        }
        return null;
    }

    private static BaseBean getMatchingChild(BaseBean bean, String path) {
        BaseBean[] beans = bean.childBeans(false);
        for (int i = 0; i < beans.length; i++) {
            BaseBean baseBean = beans[i];
            if (path.startsWith(baseBean.fullName())) {
                return baseBean;
            }
        }
        return null;
    }

    /**
     * The listener processes property changes caused by adding a new element of graph
     * Then the new element in DOM binding is properly reformated
     */
    public static class ReindentationListener implements PropertyChangeListener {
        String indent = "    ";

        public void propertyChange(PropertyChangeEvent evt) {
            Object oldValue = evt.getOldValue();
            Object newValue = evt.getNewValue();
            if (oldValue == null && newValue != null) {
                String path = evt.getPropertyName();
                BaseBean bean = (BaseBean) evt.getSource();
                Node node = findNode(bean, path);
                if (node != null) {
                    reindentNode(node);
                }
            }
        }

        private void reindentNode(Node node) {
            Document document = node.getOwnerDocument();
            int level = 0;
            StringBuffer sb = new StringBuffer("\n");
            for (Node parent = node.getParentNode(); parent != document; parent = parent.getParentNode()) {
                level++;
                sb.append(indent);
            }
            String indentString = sb.toString();

            Node parentNode = node.getParentNode();
            Node previousNode = node.getPreviousSibling();
            if (previousNode != null && isWhiteSpaceNode(previousNode)) {
                String s = previousNode.getNodeValue();
                int i = s.lastIndexOf('\n');
                s = i > 0 ? s.substring(0, i) + indentString : indentString;
                previousNode.setNodeValue(s); //set proper indent
            } else {
                parentNode.insertBefore(document.createTextNode(indentString), node); //break line
            }
            Node nextNode = node.getNextSibling();
            if (nextNode != null && isWhiteSpaceNode(nextNode)) {
                String s = nextNode.getNodeValue();
                int i = s.indexOf('\n');
                if (i == -1) {
                    nextNode.setNodeValue(indentString);  //set proper indent
                }
            } else {
                parentNode.insertBefore(document.createTextNode(indentString), nextNode); //break line
            }
            XMLUtil.reindent(document, node, level, indent);  // reindent the new node
        }

        public String getIndent() {
            return indent;
        }

        public void setIndent(String indent) {
            this.indent = indent;
        }
    }
}
