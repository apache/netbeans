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

package org.netbeans.modules.form.wizard;

import javax.swing.event.*;
import java.lang.reflect.Method;
import org.netbeans.modules.form.*;

/**
 * The third panel of connection wizard - for entering parameters for method
 * which will be called on the target component (specified in step 2).
 *
 * @author Tomas Pavek
 */

class ConnectionWizardPanel3 implements org.openide.WizardDescriptor.Panel {

    private FormModel formModel;
    private Method method;

    private EventListenerList listenerList = null;

    private ConnectionPanel3 uiPanel;

    // --------

    ConnectionWizardPanel3(FormModel model) {
        formModel = model;
    }

    FormModel getFormModel() {
        return formModel;
    }

    void setMethod(Method m) {
        method = m;
        if (uiPanel != null)
            uiPanel.setMethod(m);
    }

    String getParametersText() {
        return uiPanel != null ? uiPanel.getParametersText() : null;
    }

    Object[] getParameters() {
        return uiPanel != null ? uiPanel.getParameters() : null;
    }

    // ---------
    // WizardDescriptor.Panel implementation

    @Override
    public java.awt.Component getComponent() {
        if (uiPanel == null) {
            uiPanel = new ConnectionPanel3(this);
            if (method != null)
                uiPanel.setMethod(method);
        }
        return uiPanel;
    }

    @Override
    public org.openide.util.HelpCtx getHelp() {
        return new org.openide.util.HelpCtx("gui.connecting.param"); // NOI18N
    }

    @Override
    public boolean isValid() {
        return uiPanel != null ? uiPanel.isFilled() : false;
    }

    @Override
    public void readSettings(java.lang.Object settings) {
    }

    @Override
    public void storeSettings(java.lang.Object settings) {
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        if (listenerList == null)
            listenerList = new EventListenerList();
        listenerList.add(ChangeListener.class, listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        if (listenerList != null)
            listenerList.remove(ChangeListener.class, listener);
    }

    // ------

    void fireStateChanged() {
        if (listenerList == null)
            return;

        ChangeEvent e = null;
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == ChangeListener.class) {
                if (e == null)
                    e = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(e);
            }
        }
    }
}
