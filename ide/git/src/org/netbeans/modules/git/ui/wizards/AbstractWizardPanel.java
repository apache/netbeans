/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.git.ui.wizards;

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.ValidatingPanel;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

/**
 *
 * @author ondra
 */
public abstract class AbstractWizardPanel implements ValidatingPanel<WizardDescriptor> {
    
    private final List<ChangeListener> listeners = new LinkedList<ChangeListener>();
    private boolean valid;
    private Message errMessage;

    @Override
    public final Component getComponent () {
        return getJComponent();
    }
    
    @Override
    public final void validate () throws WizardValidationException {
        if (!validateBeforeNext()) {
            throw new WizardValidationException (
                getJComponent(),
                errMessage.getMessage(),
                errMessage.getMessage()
            );
        }
    }

    @Override
    public void readSettings (WizardDescriptor settings) {
    }

    @Override
    public void storeSettings (WizardDescriptor settings) {
    }

    @Override
    public HelpCtx getHelp () {
        return null;
    }
    
    @Override
    public boolean isValid () {
        return valid;
    }

    @Override
    public void addChangeListener (ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public void removeChangeListener (ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    public Message getErrorMessage () {
        return this.errMessage;
    }
    
    protected void setValid (boolean valid, Message message) {
        boolean oldValid = this.valid;
        Message oldMessage = this.errMessage;
        this.valid = valid;
        this.errMessage = message;
        if (oldValid != valid || oldMessage != null && !oldMessage.equals(message) || message != null && !message.equals(oldMessage)) {
            ChangeEvent evt = new ChangeEvent(this);
            ChangeListener[] lists;
            synchronized (listeners) {
                lists = listeners.toArray(new ChangeListener[0]);
            }
            for (ChangeListener list : lists) {
                list.stateChanged(evt);
            }
        }
    }

    protected abstract boolean validateBeforeNext ();

    protected abstract JComponent getJComponent ();

    public static class Message {
        private final boolean info;
        private final String msg;

        public Message (String msg, boolean isInfo) {
            this.info = isInfo;
            this.msg = msg;
        }

        public boolean isInfo () {
            return info;
        }

        public String getMessage () {
            return msg;
        }

        @Override
        public boolean equals (Object obj) {
            if (obj instanceof Message) {
                Message other = (Message) obj;
                return (msg == null && other.msg == null || msg != null && msg.equals(other.msg)) && info == other.info;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode () {
            int hash = 3;
            hash = 17 * hash + (this.info ? 1 : 0);
            hash = 17 * hash + (this.msg != null ? this.msg.hashCode() : 0);
            return hash;
        }
    }
}
