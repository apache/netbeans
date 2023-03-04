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
package org.netbeans.modules.css.model.api;

//import java.util.List;

import java.util.Iterator;

/**
 *
 * @author marekfukala
 */
public interface Element {
    
    public void accept(ModelVisitor modelVisitor);
    
    public Element getParent();

    public void setParent(Element e);
    
    public int addElement(Element e);

    public Element removeElement(int index);
    
    public boolean removeElement(Element e);
    
    public void insertElement(int index, Element element);

    public int getElementsCount();
    
    public int getElementIndex(Element e);

    public Element getElementAt(int index);
    
    public Element setElementAt(int index, Element e);
    
    public Iterator<Element> childrenIterator();

    public void addElementListener(ElementListener listener);
    
    public void removeElementListener(ElementListener listener);
 
    //XXX what should happen to the element offsets when the model is changed
    //by adding/removing some element. Clearly the original offsets become invalid then.
    
    /**
     * @return offset of the element start in the source code. 
     * May return -1 if the element has been added to the model.
     */
    public int getStartOffset();
    
    /**
     * @return offset of the element end in the source code. 
     * May return -1 if the element has been added to the model.
     */
    public int getEndOffset();
    
    /**
     * Notice: No semantic checks beyond parsing are done with respect to the returned value.
     * 
     * @return true if there's no parsing error in the element, false otherwise
     */
    public boolean isValid();
    
    /**
     * @since 1.6
     * @return Non-null instance of {@link Model} containing this element.
     */
    public Model getModel();
 
    
    /**
     * Creates an instance of a handle to a live model's element. Such instance can be later used to resolve 
     * to a live model's element from different (newer) css model by 
     * {@link ElementHandle#resolve(org.netbeans.modules.css.model.api.Model)}.
     * 
     * @since 1.22
     * @return non null instance of the {@link ElementHandle}
     */
    public ElementHandle getElementHandle();
    
    
}
