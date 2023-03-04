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
package org.netbeans.core.spi.multiview.text;

import java.io.Serializable;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElement.Registration;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.UndoRedo;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.windows.CloneableTopComponent;

/** Standard {@link MultiViewElement} to integrate editor with 
 * {@link MultiViews}. It can be used directly via a factory method:
 * <pre>
 <code>@</code>MultiViewElement.Registration(
   displayName = "#BUNDLE_KEY",
   iconBase = "path/to/some-icon.png",
   mimeType = "text/yourmime",
   persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
   preferredID = "yourId",
   position = 1000
 )
 public static MultiViewEditorElement createEditor(Lookup lkp) {
   return new MultiViewEditorElement(lkp);
 }</pre>
 * Or one can subclass the class, override some methods (it is recommended
 * to continue to call super implementation) and register the
 * subclass.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 * @since 1.24
 */
public class MultiViewEditorElement implements 
MultiViewElement, CloneableEditorSupport.Pane, Serializable {
    static final long serialVersionUID = 430840231840923L;
    
    private MultiViewCloneableEditor editor;

    /** Constructor suitable for use with {@link Registration} annotation. 
     * The {@link Lookup} parameter is expected to contain 
     * {@link CloneableEditorSupport}, otherwise it yields an exception
     * 
     * @param lookup context for the editor. Should contain instance of {@link CloneableEditorSupport}
     *   class
     * @throws IllegalArgumentException if {@link CloneableEditorSupport} is not present
     *   in provided {@link Lookup}
     */
    public MultiViewEditorElement(Lookup lookup) {
        CloneableEditorSupport sup = lookup.lookup(CloneableEditorSupport.class);
        if (sup == null) {
            throw new IllegalArgumentException("We expect CloneableEditorSupport in " + lookup);
        }
        editor = new MultiViewCloneableEditor(sup);
    }
    
    @Override
    public JComponent getVisualRepresentation() {
        return editor.getVisualRepresentation();
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return editor.getToolbarRepresentation();
    }

    @Override
    public Action[] getActions() {
        return editor.getActions();
    }

    @Override
    public Lookup getLookup() {
        return editor.getLookupSuper();
    }

    @Override
    public void componentOpened() {
        editor.componentOpened();
    }

    @Override
    public void componentClosed() {
        editor.componentClosed();
    }

    @Override
    public void componentShowing() {
        editor.componentShowing();
    }

    @Override
    public void componentHidden() {
        editor.componentHidden();
    }

    @Override
    public void componentActivated() {
        editor.componentActivated();
    }

    @Override
    public void componentDeactivated() {
        editor.componentDeactivated();
    }

    @Override
    public UndoRedo getUndoRedo() {
        return editor.getUndoRedo();
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        editor.setMultiViewCallback(callback);
    }

    @Override
    public CloseOperationState canCloseElement() {
        return editor.canCloseElement();
    }

    @Override
    public JEditorPane getEditorPane() {
        return editor.getEditorPane();
    }

    @Override
    public CloneableTopComponent getComponent() {
        return editor.getComponent();
    }

    @Override
    public void updateName() {
        editor.updateName();
    }

    @Override
    public void ensureVisible() {
        editor.ensureVisible();
    }
}
