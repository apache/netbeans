/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
