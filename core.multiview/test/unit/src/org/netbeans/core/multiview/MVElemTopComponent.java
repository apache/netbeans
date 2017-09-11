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

package org.netbeans.core.multiview;

import org.netbeans.core.spi.multiview.MultiViewElementCallback;

import javax.swing.JComponent;
import javax.swing.JToolBar;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.awt.UndoRedo;
import org.openide.windows.TopComponent;

/**
 *
 * @author  mkleint
 */
public class MVElemTopComponent extends TopComponent implements MultiViewElement {
    private StringBuffer log;
    public MultiViewElementCallback observer;
    private transient UndoRedo undoredo;
    
    MVElemTopComponent() {
        resetLog();
    }
    
    
    public String getLog() {
        return log.toString();
    }
    
    public void resetLog() {
        log = new StringBuffer();
    }
    
    public void componentActivated() {
        super.componentActivated();
        log.append("componentActivated-");
        
    }
    
    public void componentClosed() {
        super.componentClosed();
        log.append("componentClosed-");
    }
    
    public void componentDeactivated() {
        super.componentDeactivated();
        log.append("componentDeactivated-");
    }
    
    public void componentHidden() {
        super.componentHidden();
        log.append("componentHidden-");
    }
    
    public void componentOpened() {
        super.componentOpened();
        log.append("componentOpened-");
    }
    
    public void componentShowing() {
        super.componentShowing();
        log.append("componentShowing-");
    }
    
    
    public JComponent getToolbarRepresentation() {
        return new JToolBar();
    }
    
    public javax.swing.JComponent getVisualRepresentation() {
        return this;
    }
    
    public String preferredID() {
        return super.preferredID();
    }
    
//    public void removeActionRequestObserver() {
//        observer = null;
//    }
    
    
    public void setMultiViewCallback (MultiViewElementCallback callback) {
        this.observer = callback;
    }
    
    public void doRequestActive() {
        observer.requestActive();
    }

    public void doRequestVisible() {
        observer.requestVisible();
    }
    
    public void setUndoRedo(UndoRedo redo) {
        undoredo = redo;
    }
    
//    public UndoRedo getUndoRedo() {
//        return undoredo;
//    }
    
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }
    
}

