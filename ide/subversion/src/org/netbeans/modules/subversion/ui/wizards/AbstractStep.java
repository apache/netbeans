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

package org.netbeans.modules.subversion.ui.wizards;

import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.*;
import java.util.logging.Level;
import org.netbeans.modules.subversion.Subversion;

/**
 * Abstract wizard panel with <codE>valid</code>
 * and <codE>errorMessage</code> bound properties.
 *
 * @author Petr Kuzel
 */
public abstract class AbstractStep implements WizardDescriptor.ValidatingPanel {

    private final List<ChangeListener> listeners = new LinkedList<ChangeListener>();
    private boolean valid;
    private JComponent panel;
    private volatile boolean underConstruction;
    private WizardMessage errorMessage;
    private boolean isInfo;

    /**
     * Calls to createComponent. Noramalizes size nad assigns
     * helpId based on subclass name.
     */
    @Override
    public final synchronized Component getComponent() {
        if (panel == null) {
            try {
                underConstruction = true;
                panel = createComponent();
                HelpCtx.setHelpIDString(panel, getClass().getName());
            } catch (RuntimeException ex) {
                Subversion.LOG.log(Level.SEVERE, null, ex);
            } finally {
                assert panel != null;
                underConstruction = false;
                fireChange();
            }
        }
        return panel;
    }

    /**
     * @return must not return null
     */
    protected abstract JComponent createComponent();

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void readSettings(Object settings) {
    }

    @Override
    public void storeSettings(Object settings) {
    }

    protected final void valid() {
        setValid(true, null);
    }

    /**
     * Valid with error message that can be corrected
     * by external change.
     */
    protected final void valid(WizardMessage msg) {
        setValid(true, msg);
    }

    protected final void invalid(WizardMessage msg) {
        setValid(false, msg);
    }

    @Override
    public final boolean isValid() {
        return valid;
    }

    public final WizardMessage getErrorMessage() {
        return errorMessage;
    }

    // comes on next or finish
    @Override
    public final void validate () throws WizardValidationException {
        validateBeforeNext();
        if (isValid() == false || errorMessage != null) {
            throw new WizardValidationException (
                panel,
                errorMessage.getMessage(),
                errorMessage.getMessage()
            );
        }
    }

    /**
     * Perform heavy validation reporting results
     * using {@link #valid} and {@link #invalid}.
     */
    protected abstract void validateBeforeNext();

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.remove(l);
        }
    }

    private void setValid(boolean valid, WizardMessage msg) {
        boolean fire = AbstractStep.this.valid != valid;
        fire |= ((msg != null && errorMessage == null)  ||
                 (msg == null && errorMessage != null)) ||
                 (msg != null && errorMessage !=null && !msg.getMessage().equals(errorMessage.getMessage())) ;
        AbstractStep.this.valid = valid;
        errorMessage = msg;
        if (fire) {
            fireChange();
        }
    }

    private void fireChange() {
        if (underConstruction) return;
        List<ChangeListener> clone;
        synchronized(listeners) {
            clone = new ArrayList<ChangeListener>(listeners);
        }
        Iterator<ChangeListener> it = clone.iterator();
        ChangeEvent event = new ChangeEvent(this);
        while (it.hasNext()) {
            ChangeListener listener = it.next();
            listener.stateChanged(event);
        }
    }

    public class WizardMessage {
        private final boolean info;
        private final String msg;
        public WizardMessage(String msg, boolean isInfo) {
            this.info = isInfo;
            this.msg = msg;
        }
        public boolean isInfo() {
            return info;
        }
        public String getMessage() {
            return msg;
        }
    }

}
