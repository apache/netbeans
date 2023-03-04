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
package org.netbeans.modules.progress.spi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.api.progress.ProgressHandle;
import static org.netbeans.modules.progress.spi.InternalHandle.STATE_INITIALIZED;
import org.netbeans.progress.module.TrivialProgressUIWorkerProvider;
import org.netbeans.progress.module.UIInternalHandleAccessor;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;

/**
 * UI Counterpart of the {@link InternalHandle}. 
 * 
 * @author sdedic
 */
public final class UIInternalHandle extends InternalHandle {
    private static final Logger LOG = Logger.getLogger(UIInternalHandle.class.getName());
    
    private ActionListener viewAction;
    private ExtractedProgressUIWorker component;
    private boolean customPlaced1 = false;
    private boolean customPlaced2 = false;
    private boolean customPlaced3 = false;
    private ProgressHandle handle;
    
    public UIInternalHandle(String displayName, 
                   Cancellable cancel,
                   boolean userInitiated,
                   Action view) {
        super(displayName, cancel, userInitiated);
        viewAction = view;
    }

    public boolean isAllowCancel() {
        return super.isAllowCancel() && !isCustomPlaced();
    }
    
    public boolean isAllowView() {
        return viewAction != null && !isCustomPlaced();
    }

    public boolean isCustomPlaced() {
        return component != null;
    }
    
    public void requestView() {
        if (!isAllowView()) {
            return;
        }
        viewAction.actionPerformed(new ActionEvent(viewAction, ActionEvent.ACTION_PERFORMED, "performView"));
    }

    @Override
    public boolean requestAction(String actionCommand, Action al) {
        if (actionCommand != ProgressHandle.ACTION_VIEW) {
            // no UI atm
            return false;
        }
        viewAction = al;
        return true;
    }

    private void createExtractedWorker() {
        if (component == null) {
            ProgressUIWorkerProvider prov = Lookup.getDefault().lookup(ProgressUIWorkerProvider.class);
            if (prov == null) {
                LOG.log(Level.CONFIG, "Using fallback trivial progress implementation");
                prov = new TrivialProgressUIWorkerProvider();
            }
            component = prov.extractProgressWorker(this);
            setController(new SwingController(component));
        }
    }

    /**
     * have the component in custom location, don't include in the status bar.
     */
    public synchronized JComponent extractComponent() {
        if (customPlaced1) {
            throw new IllegalStateException("Cannot retrieve progress component multiple times");
        }
        if (getState() != STATE_INITIALIZED) {
            throw new IllegalStateException("You can request custom placement of progress component only before starting the task");
        }
        customPlaced1 = true;
        createExtractedWorker();
        return component.getProgressComponent();
    }
    
    public synchronized JLabel extractDetailLabel() {
        if (customPlaced2) {
            throw new IllegalStateException("Cannot retrieve progress detail label component multiple times");
        }
        if (getState() != STATE_INITIALIZED) {
            throw new IllegalStateException("You can request custom placement of progress component only before starting the task");
        }
        customPlaced2 = true;
        createExtractedWorker();
        return component.getDetailLabelComponent();
    }

    public synchronized JLabel extractMainLabel() {
        if (customPlaced3) {
            throw new IllegalStateException("Cannot retrieve progress main label component multiple times");
        }
        if (getState() != STATE_INITIALIZED) {
            throw new IllegalStateException("You can request custom placement of progress component only before starting the task");
        }
        customPlaced3 = true;
        createExtractedWorker();
        return component.getMainLabelComponent();
    }


    static {
        UIInternalHandleAccessor.setInstance(new UIInternalHandleAccessor() {
            @Override
            public void setController(InternalHandle h, Controller c) {
                h.setController(c);
            }

            @Override
            public void markCustomPlaced(InternalHandle h) {
                h.markCustomPlaced();
            }
        });
    }
}
