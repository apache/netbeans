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

package org.netbeans.modules.xml.schema.model.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.event.UndoableEditListener;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.dom.DocumentModelAccess;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class EmbeddedSchemaModelImpl extends SchemaModelImpl {
    
    private DocumentModel delegate;
    private Element element;
    private SchemaImpl schema;
    
    public EmbeddedSchemaModelImpl(DocumentModel delegate, Element element) {
        super(delegate.getModelSource());
        if (! (delegate instanceof AbstractDocumentModel)) {
            throw new IllegalArgumentException("Expect instance of AbstractComponent");
        }
        this.delegate = delegate;
        this.element = element;
    }
    
    //reimplementation of Model APIs to account for delegate model
    public void addComponentListener(ComponentListener cl){
        delegate.addComponentListener(cl);
    }
    
    public void removeComponentListener(ComponentListener cl){
        delegate.removeComponentListener(cl);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pcl){
        delegate.removePropertyChangeListener(pcl);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pcl){
        delegate.addPropertyChangeListener(pcl);
    }
    
    public void removeUndoableEditListener(UndoableEditListener uel){
        delegate.removeUndoableEditListener(uel);
    }
    
    public void addUndoableEditListener(UndoableEditListener uel){
        delegate.addUndoableEditListener(uel);
    }
    
    public ModelSource getModelSource() {
        return delegate.getModelSource();
    }
    
    public void sync() throws java.io.IOException{
        delegate.sync();
    }
    
    public boolean inSync(){
        return delegate.inSync();
    }
    
    public State getState(){
        return delegate.getState();
    }
    
    public boolean isIntransaction(){
        return delegate.isIntransaction();
    }
    
    public boolean startTransaction(){
        return delegate.startTransaction();
    }
    
    public void endTransaction() {
        delegate.endTransaction();
    }
    
    public SchemaImpl getSchema() {
        if(schema == null){
            schema = (SchemaImpl)this.getFactory().create(element, null);
        }
        return schema;
    }
    
    //from AbstractModel
    public  Document getDocument(){
        return delegate.getDocument();
    }
    
    public synchronized void validateWrite() {
        getDelegate().validateWrite();
    }
    
    public DocumentModelAccess getAccess(){
        return getDelegate().getAccess();
    }
    
    public void firePropertyChangeEvent(PropertyChangeEvent event) {
        getDelegate().firePropertyChangeEvent(event);
    }
    
    public void fireComponentChangedEvent(ComponentEvent evt) {
        getDelegate().fireComponentChangedEvent(evt);
    }

    public SchemaComponent getRootComponent() {
        return getSchema();
    }
    
    private AbstractDocumentModel getDelegate() {
        return (AbstractDocumentModel) delegate;
    }

    public boolean isEmbedded() {
        return true;
    }

}
