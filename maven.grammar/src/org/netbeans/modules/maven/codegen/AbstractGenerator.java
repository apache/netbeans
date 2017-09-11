/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
public abstract class AbstractGenerator<T extends AbstractDocumentModel> implements CodeGenerator {
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
