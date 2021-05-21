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
package org.netbeans.modules.maven.codegen;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import static org.netbeans.modules.maven.codegen.Bundle.*;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author mkleint
 */
public abstract class AbstractGenerator<T extends AbstractDocumentModel<? extends DocumentComponent<?>>> implements CodeGenerator {
    protected final JTextComponent component;
    protected final T model;


    protected AbstractGenerator(T model, JTextComponent component) {
        this.model = model;
        this.component = component;
    }
    
    protected abstract void doInvoke();
    
    @Override
    @Messages("MSG_Cannot_Parse=Cannot parse document. Unable to generate content.")
    public final void invoke() {
        try {
            model.sync();
        } catch (IOException ex) {
            Logger.getLogger(ProfileGenerator.class.getName()).log(Level.INFO, "Error while syncing the editor document with model for pom.xml file", ex); //NOI18N
        }
        if (!model.getState().equals(Model.State.VALID)) {
            StatusDisplayer.getDefault().setStatusText(MSG_Cannot_Parse());
            return;
        }
        doInvoke();
    }

    
    @Messages("ERR_CannotWriteModel=Cannot write to the model: {0}")
    protected final void writeModel(ModelWriter writer) {
        int newPos = -1;
        try {
            if (model.startTransaction()) {
                newPos = writer.write();
            }
        } finally {
            try {
                model.endTransaction();
            } catch (IllegalStateException ex) {
                StatusDisplayer.getDefault().setStatusText(
                        ERR_CannotWriteModel(Exceptions.findLocalizedMessage(ex)),
                        StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
            }
        }
        if (newPos != -1) {
            component.setCaretPosition(newPos);
        }


    }

    public static interface ModelWriter {
        int write();
    }
            
    protected static interface ChildrenListProvider<C extends DocumentComponent> {
        List<C> get();
    }
    
    protected <C extends DocumentComponent> boolean addAtPosition(String parentTagName, ChildrenListProvider<C> childrenListProvider, C newChild) {
        return addAtPosition(null, parentTagName, childrenListProvider, newChild);
    }
    
    protected <C extends DocumentComponent> boolean addAtPosition(DocumentComponent componentAtCarret, String parentTagName, ChildrenListProvider<C> childrenListProvider, C newChild) {
        int pos = component.getCaretPosition();        
        if (componentAtCarret == null) {
            componentAtCarret = model.findComponent(pos);
        }
        if(componentAtCarret != null) {
            Component c = null;
            if(componentAtCarret.getPeer().getTagName().equals(parentTagName)) {
                c = componentAtCarret;
            } else if(componentAtCarret.getClass() == newChild.getClass() && componentAtCarret.findPosition() == pos) {
                // if carret positioned at the begining of e.g. "<license>"
                c = componentAtCarret.getParent(); // get the parent (<licenses>) in such a case
            }
            if(c != null) {
                List<C> list = childrenListProvider.get();
                for (int i = 0; i < list.size(); i++) {
                    C l = list.get(i);
                    if(pos <= l.findPosition()) {
                        model.addChildComponent(c, newChild, i);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
}
