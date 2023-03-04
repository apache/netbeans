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
package org.netbeans.modules.xml.axi.util;

import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AnyAttribute;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.visitor.PrintAXITreeVisitor;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Test visitor to visit all children in the AXI tree.
 * @author Samaresh
 */
public class ModelValidator extends PrintAXITreeVisitor {
    
    private String errorMessage;
    private boolean success;
    private URL expectedFileURL;
    private FileReader expectedFileReader;
        
    /**
     * Creates a new instance of AXITreeVisitor
     */
    public ModelValidator(URL url) {
        this.success = true;
        this.expectedFileURL = url;
    }
        
    public boolean visitAndCompareAgainstDOMElement(Element element) {
        this.visit(element);
        return success;
    }
    
    public void visit(Attribute attr) {
        //do nothing
    }
    
    public void visit(AnyAttribute attr) {
        //do nothing
    }
        
    protected void visitChildren(AXIComponent component) {
        if(canCompare()) {
            if(!compare(component))
                return;
        }
        super.visitChildren(component);
    }
            
    public String getErrorMessage() {
        return errorMessage;
    }
    
    private boolean canCompare() {
        if(expectedFileURL == null)
            return false;
        
        return true;
    }

    /**
     * Compares AXI component against corresponding DOM element.
     */
    private boolean compare(AXIComponent axiNode) {        
        FileUtil util = FileUtil.getInstance();
        try {
            InputSource inputSource = util.openFile(expectedFileURL);
            if(inputSource == null) {
                success = false;
                return false;
            }
            String expression = getExpression(axiNode);
            XPath xpath = XPathFactory.newInstance().newXPath();
            Node domNode = (Node) xpath.evaluate(expression, inputSource, XPathConstants.NODE);
            if(!axiNode.toString().equals(domNode.getNodeName())) {
                success = false;
                errorMessage = "Expected AXI node " + axiNode + ", but found DOM node " + domNode.getNodeName();
                return false;
            }
                        
            if(!compareChildren(axiNode, domNode)) {
                return false;
            }
            
        } catch(Exception ex) {
            ex.printStackTrace();
            success = false;
            errorMessage = "Exception: " + ex.getMessage();
            return false;
        } finally {
            util.closeFile();
        }
        
        return true;
    }
    
    /**
     * Compares each child of an AXI component against
     * corresponding DOM element's child.
     */
    private boolean compareChildren(AXIComponent axiNode, Node domNode) {        
        //compare sizes
        Collection<AXIComponent> axiChildren = getChildren(axiNode);
        Collection<Node> domChildren = getChildren(domNode);
        if( axiChildren.size() != domChildren.size() ) {
            success = false;
            errorMessage = "For AXI node " + axiNode +
                    ", expected child count is " + axiChildren.size() +
                    " where as, actual count is " + domChildren.size();
            return success;
        }
        
        Collection<Attribute> attrs = axiNode.getChildren(Attribute.class);
        if( attrs.size() != domNode.getAttributes().getLength() ) {
            success = false;
            errorMessage = "For AXI node " + axiNode +
                    ", expected attribute count is " + attrs.size() +
                    " where as, actual count is " + domNode.getAttributes().getLength();
            return success;
        }
        
        Iterator<AXIComponent> axiIterator = axiChildren.iterator();
        Iterator<Node> domIterator = domChildren.iterator();
        while(axiIterator.hasNext() && domIterator.hasNext()) {
            AXIComponent axiChild = axiIterator.next();
            Node domChild = domIterator.next();
            if(!axiChild.toString().equals(domChild.getNodeName())) {
                success = false;
                errorMessage = "For AXI node " + axiNode +
                        ", expecting child "  + axiChild  +
                        " where as, found child " + domChild.getNodeName();
                return success;
            }
        }
        
        return true;
    }
                    
    private String getExpression(AXIComponent component) {
        if(component.getParent() instanceof AXIDocument)
            return "/" + component;
        
        return getExpression(component.getParent()) + "/" + component;
    }
    
    private Collection<Node> getChildren(Node node) {
        Collection<Node> nodes = new ArrayList<Node>();
        NodeList children = node.getChildNodes();
        for(int i=0; i<children.getLength(); i++) {
            Node child = children.item(i);
            if(child.getNodeType() != Node.ELEMENT_NODE)
                continue;            
            nodes.add(child);
        }
        return nodes;
    }
    
    private Collection<AXIComponent> getChildren(AXIComponent component) {
        Collection<AXIComponent> children = new ArrayList<AXIComponent>();
        for(AXIComponent child : component.getChildren()) {
            if(child instanceof Attribute)
                continue;
            children.add(child);
        }
        return children;
    }
        
}
