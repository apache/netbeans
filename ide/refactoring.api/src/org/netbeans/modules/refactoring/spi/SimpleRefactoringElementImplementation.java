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
