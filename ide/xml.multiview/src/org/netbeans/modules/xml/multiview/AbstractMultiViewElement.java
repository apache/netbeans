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
package org.netbeans.modules.xml.multiview;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.Action;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewFactory;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.openide.actions.FileSystemAction;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * @author pfiala
 */
public abstract class AbstractMultiViewElement implements MultiViewElement, Serializable {
    private static final long serialVersionUID = 20110816L;

    private static final Logger LOGGER = Logger.getLogger(AbstractMultiViewElement.class.getName());

    protected XmlMultiViewDataObject dObj;
    protected transient MultiViewElementCallback callback;

    protected AbstractMultiViewElement() {
    }

    protected AbstractMultiViewElement(XmlMultiViewDataObject dObj) {
        this.dObj = dObj;
    }

    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
        if (dObj!=null) {
            XmlMultiViewEditorSupport support = dObj.getEditorSupport();
            if (support!=null) {
                support.setMVTC(callback.getTopComponent());
                support.updateDisplayName();
            }
        }
    }

    @Override
    public CloseOperationState canCloseElement() {
        if (dObj == null || dObj.canClose()) {
            return CloseOperationState.STATE_OK;
        } else if (!this.callback.isSelectedElement()) {
            return CloseOperationState.STATE_OK;
        } else if (!dObj.isModified()) {
            return CloseOperationState.STATE_OK;
        } else {
            boolean differ = false;
            String message = dObj.getEditorSupport().messageSave();
            try {
                String encoding = dObj.encoding();
                differ = dObj.encodingDiffer(encoding);
                if (differ) {
                    message += " <b>" + dObj.encodingMessage(encoding) + "</b>";
                }
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
            message = "<html>" + message + "</html>";
            return MultiViewFactory.createUnsafeCloseState(
                    message, new SaveAction(differ), new DiscardAction());
        }
    }

    public javax.swing.Action[] getActions() {
        Action[] actions = callback.createDefaultActions();
        SystemAction fsAction = SystemAction.get(FileSystemAction.class);
        if (!Arrays.asList(actions).contains(fsAction)) {
            Action[] newActions = new Action[actions.length+1];
            System.arraycopy(actions, 0, newActions, 0, actions.length);
            newActions[actions.length] = fsAction;
            actions = newActions;
        }
        return actions;
    }

    public void componentOpened() {
    }

    public void componentClosed() {
    }

    public org.openide.awt.UndoRedo getUndoRedo() {
        return dObj ==null ? null : dObj.getEditorSupport().getUndoRedo0();
    }

    private class SaveAction extends AbstractAction {

        private final boolean encodingReset;

        public SaveAction(boolean encodingReset) {
            this.encodingReset = encodingReset;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if (encodingReset) {
                dObj.encodingReset();
            }
            try {
                dObj.getEditorSupport().onCloseSave();
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

    }

    private class DiscardAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            dObj.getEditorSupport().onCloseDiscard();
        }

    }
}
