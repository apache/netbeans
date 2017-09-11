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
