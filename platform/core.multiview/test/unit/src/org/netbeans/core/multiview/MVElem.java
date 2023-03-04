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

import java.lang.ref.WeakReference;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.awt.UndoRedo;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author  mkleint
 */
public class MVElem implements MultiViewElement {
    private StringBuffer log;
    private Action[] actions;
    public MultiViewElementCallback observer;
    private JComponent visualRepre;
    private WeakReference<JComponent> visualRepreW;
    private transient UndoRedo undoredo;
    
    MVElem() {
        this(new Action[0]);
    }
    
    MVElem(Action[] actions) {
        log = new StringBuffer();
        this.actions = actions;
    }
    
    public String getLog() {
        return log.toString();
    }
    
    public void resetLog() {
        log = new StringBuffer();
    }
    
    public void componentActivated() {
        log.append("componentActivated-");
        
    }
    
    public void componentClosed() {
        log.append("componentClosed-");
        visualRepre = null;
    }
    
    public void componentDeactivated() {
        log.append("componentDeactivated-");
    }
    
    public void componentHidden() {
        log.append("componentHidden-");
    }
    
    public void componentOpened() {
        log.append("componentOpened-");
        visualRepre = getVisualRepresentation();
    }
    
    public void componentShowing() {
        log.append("componentShowing-");
    }
    
    public javax.swing.Action[] getActions() {
        return actions;
    }
    
    public org.openide.util.Lookup getLookup() {
        return Lookups.fixed(new Object[] {this});
    }
    
    public JComponent getToolbarRepresentation() {
        return new JToolBar();
    }
    
    public javax.swing.JComponent getVisualRepresentation() {
        // modified as part of 130919 fix - hold visual repre more weakly
        JComponent result = null;
        if (visualRepreW == null || visualRepreW.get() == null) {
            result = new JPanel();
            visualRepreW = new WeakReference<JComponent>(result);
        } else {
            result = visualRepreW.get();
        }
        return result;
    }
    
    public String preferredID() {
        return "test";
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
    
    public UndoRedo getUndoRedo() {
        return undoredo;
    }
    
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }
    
}

