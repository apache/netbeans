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
                lists = listeners.toArray(new ChangeListener[listeners.size()]);
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
