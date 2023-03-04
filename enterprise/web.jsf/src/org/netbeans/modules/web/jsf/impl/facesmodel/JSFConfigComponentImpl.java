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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.netbeans.modules.web.jsf.api.facesmodel.Application;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Petr Pisl
 */
public abstract class JSFConfigComponentImpl extends AbstractDocumentComponent <JSFConfigComponent>
        implements JSFConfigComponent {
    
    /** Creates a new instance of JSFConfigComponentImp */
    public JSFConfigComponentImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public JSFConfigModelImpl getModel(){
        return (JSFConfigModelImpl)super.getModel();
    }
    
    protected void populateChildren(List<JSFConfigComponent> children) {
        NodeList nodeList = getPeer().getChildNodes();
        if (nodeList != null){
            for (int i = 0; i < nodeList.getLength(); i++) {
                org.w3c.dom.Node node = nodeList.item(i);
                if (node instanceof Element) {
                    JSFConfigModel model = getModel();
                    JSFConfigComponent comp = 
                        (JSFConfigComponent) model.getFactory().
                            create((Element)node, this);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }
    
    /**
     * Set the value of the text node from the child element with given QName.
     * This method is use to implement mapping of "property" as component attribute.
     * @param propertyName property change event name
     * @param text the string to set value of the child element text node.
     * @param qname QName of the child element to get text from.
     */
    protected void setChildElementText(String propertyName, String text, QName qname) {
        super.setChildElementText(propertyName, text, qname);
        reorderChildren();
    }
    
    protected void appendChild(String propertyName, JSFConfigComponent child) {
        super.appendChild(propertyName, child);
        reorderChildren();
    }
    
    protected Object getAttributeValueOf(Attribute attr, String stringValue) {
        if ( attr instanceof FacesAttributes ){
            Class<?> clazz = attr.getType();
            if ( clazz.equals( Boolean.class )){
                return Boolean.valueOf( stringValue );
            }
        }
        return stringValue;
    }
    
    protected List<String> getSortedListOfLocalNames(){
        return Collections.EMPTY_LIST;
    }
    
    protected Map<String,Integer> getOrderedMapOfLocalNames(){
        return Collections.<String,Integer>emptyMap();
    }
    
    protected void reorderChildren(){
        NodeList nodes = getPeer().getChildNodes();
        int length = nodes.getLength();
        
        Node node;
        int orderNumber;
        int lastRealOrderNumber = -1;
        SortingItem[] sortingItems = new SortingItem[length];
        for(int i = length-1; i > -1; i--){
            node = nodes.item(i);
            Map<String,Integer> orderMap = getOrderedMapOfLocalNames();
            Integer index  = orderMap.get(node.getLocalName());
            if ( index != null ){
                orderNumber = index;
            }
            else {
                orderNumber = getSortedListOfLocalNames().indexOf(node.getLocalName());
            }
            if (orderNumber == -1){
                if (lastRealOrderNumber == -1){
                    orderNumber = getSortedListOfLocalNames().size()+1;
                }
                else {
                    orderNumber = lastRealOrderNumber;
                }
            }
            else {
                lastRealOrderNumber = orderNumber;
            }
            sortingItems[i]= new SortingItem(i, orderNumber);
        }
        
        Arrays.sort(sortingItems);
        
        int[] newIndexes = new int[length];
        for (int i = 0; i < length; i++){
            newIndexes[sortingItems[i].getOriginalIndex()] = i;
        }
        getModel().getAccess().reorderChildren(getPeer(), newIndexes, this);
    }
    
    public static Element createElementNS(JSFConfigModel model,JSFConfigQNames jsfqname) {
        return model.getDocument().createElementNS(model.getRootComponent().getPeer().getNamespaceURI(), jsfqname.getQualifiedName(model.getVersion()));
    }
    
    private class SortingItem implements Comparable<SortingItem>{
        //index of the element, before sorting
        int originalIndex;
        //number, how it should be sorted
        int orderNumber;
        
        
        public SortingItem(int originalIndex, int orderNumber){
            this.originalIndex = originalIndex;
            this.orderNumber = orderNumber;
        }
        
        public int getOriginalIndex(){
            return originalIndex;
        }
        
        public void setOriginalIndex(int originalIndex){
            this.originalIndex = originalIndex;
        }
        
        public int getOrderNumber(){
            return orderNumber;
        }
        
        public void setOrderNumber(int orderNumber){
            this.orderNumber = orderNumber;
        }

        public int compareTo(SortingItem item) {
            int result;
            
            if (orderNumber == item.getOrderNumber()){
                result = 0;
            } else if (orderNumber < item.getOrderNumber()){
                result = -1;
            } else {
                result = 1;
            }
            return result;
        }
    }
    
}
