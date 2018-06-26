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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
        return Collections.EMPTY_MAP;
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
    
    static public Element createElementNS(JSFConfigModel model,JSFConfigQNames jsfqname) {
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
