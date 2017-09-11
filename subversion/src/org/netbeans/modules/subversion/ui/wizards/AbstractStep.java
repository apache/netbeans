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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
