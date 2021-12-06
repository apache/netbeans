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

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.netbeans.modules.versionvault.ui.wizard;

import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.*;

/**
 * Abstract wizard panel with <codE>valid</code>
 * and <codE>errorMessage</code> bound properties.
 *
 * <p>Components use 3:2 (60x25 chars) size mode
 * to avoid wizard resizing after [next>].
 *
 * @author Petr Kuzel
 */
public abstract class AbstractStep implements WizardDescriptor.ValidatingPanel {

    private List<ChangeListener> listeners = new LinkedList<ChangeListener>();
    private boolean valid;
    private JComponent panel;
    private volatile boolean underConstruction;
    private String errorMessage;
    private boolean applyStandaloneLayout;

    /**
     * If called before getComponent it disables 3:2 size mode.
     */
    public void applyStandaloneLayout() {
        applyStandaloneLayout = true;
    }

    /**
     * Calls to createComponent. Noramalizes size nad assigns
     * helpId based on subclass name.
     */
    public final synchronized Component getComponent() {
        if (panel == null) {
            try {
                underConstruction = true;
                panel = createComponent();
                //HelpCtx.setHelpIDString(panel, getClass().getName());
                if (applyStandaloneLayout == false) {
                    JTextArea template = new JTextArea();
                    template.setColumns(60);
                    template.setRows(25);
                    panel.setPreferredSize(template.getPreferredSize());
                }
            } catch (RuntimeException ex) {
                ErrorManager.getDefault().notify(ex);
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

    public HelpCtx getHelp() {
        return null;
    }

    public void readSettings(Object settings) {
    }

    public void storeSettings(Object settings) {
    }

    protected final void valid() {
        setValid(true, null);
    }

    /**
     * Valid with error message that can be corrected
     * by external change.
     */
    protected final void valid(String extErrorMessage) {
        setValid(true, extErrorMessage);
    }

    protected final void invalid(String message) {
        setValid(false, message);
    }

    public final boolean isValid() {
        return valid;
    }

    public final String getErrorMessage() {
        return errorMessage;
    }

    // comes on next or finish
    public final void validate () throws WizardValidationException {
        validateBeforeNext();
        if (isValid() == false || errorMessage != null) {
            throw new WizardValidationException (
                panel,
                errorMessage,
                errorMessage
            );
        }
    }

    /**
     * Perform heavy validation reporting results
     * using {@link #valid} and {@link #invalid}.
     */
    protected abstract void validateBeforeNext();

    public final void addChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.add(l);
        }
    }

    public final void removeChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.remove(l);
        }
    }

    private void setValid(boolean valid, String errorMessage) {
        boolean fire = AbstractStep.this.valid != valid;
        fire |= errorMessage != null && (errorMessage.equals(this.errorMessage) == false);
        AbstractStep.this.valid = valid;
        this.errorMessage = errorMessage;
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

}
