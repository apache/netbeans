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

