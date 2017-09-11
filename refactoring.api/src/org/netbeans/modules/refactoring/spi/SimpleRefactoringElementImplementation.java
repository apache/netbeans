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

package org.netbeans.modules.refactoring.spi;

import java.awt.Container;
import javax.swing.JEditorPane;
import org.netbeans.editor.JumpList;
import org.netbeans.modules.refactoring.spi.impl.ParametersPanel;
import org.netbeans.modules.refactoring.spi.impl.PreviewManager;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Default implementation of RefactoringElementImplementation interface.
 * It contains implementations of
 * @see RefactoringElementImplementation#isEnabled()
 * @see RefactoringElementImplementation#setEnabled(boolean)
 * @see RefactoringElementImplementation#getStatus()
 * @see RefactoringElementImplementation#setStatus(int) and
 * @see RefactoringElementImplementation#openInEditor()
 * @see RefactoringElementImplementation#showPreview()
 * @author Jan Becicka
 * @see RefactoringElementImplementation
 * @since 1.5.0
 */
public abstract class SimpleRefactoringElementImplementation implements RefactoringElementImplementation {
    
    private boolean enabled = true;
    private int status = NORMAL;
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public int getStatus() {
        return status;
    }
    
    @Override
    public void setStatus(int status) {
        this.status = status;
    }
    
    @Override
    public void openInEditor() {
        
        PositionBounds bounds = getPosition();
        if (bounds == null)
            return;
        PositionRef beginPos=bounds.getBegin();
        CloneableEditorSupport editSupp=beginPos.getCloneableEditorSupport();
        editSupp.edit();
        JEditorPane[] panes=editSupp.getOpenedPanes();
        
        if (panes!=null) {
            JumpList.checkAddEntry();
            try {
                panes[0].setCaretPosition(bounds.getEnd().getOffset());
                panes[0].moveCaretPosition(beginPos.getOffset());
            } catch (IllegalArgumentException iae) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, iae);
            }
            getTopComponent(panes[0]).requestActive();
        } else {
            // todo (#pf): what to do if there is no pane? -- now, there
            // is a error message. I'm not sure, maybe this code will be
            // never called.
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(ParametersPanel.class,"ERR_ErrorOpeningEditor"))
                    );
        }
    }
    
    @Override
    public void showPreview() {
        PreviewManager manager = PreviewManager.getDefault();
        manager.refresh(this);
    }
    
    /**
     * this method is under development. Might be removed in final release.
     * Do not override it so far.
     * return String representation of whole file after refactoring
     * @return 
     */ 
    protected String getNewFileContent() {
        return null;
    }

    private static final TopComponent getTopComponent(Container temp) {
        while (!(temp instanceof TopComponent)) {
            temp = temp.getParent();
        }
        return (TopComponent) temp;
    }
    
    @Override
    public void undoChange() {
    }
    
}
