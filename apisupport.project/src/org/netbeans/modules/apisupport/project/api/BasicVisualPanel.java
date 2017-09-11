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

package org.netbeans.modules.apisupport.project.api;

import javax.swing.JPanel;
import org.openide.WizardDescriptor;

/**
 * Basic visual panel for APISupport wizard panels.
 *
 * @author Martin Krauskopf
 */
public abstract class BasicVisualPanel extends JPanel {

    private WizardDescriptor settings;

    protected BasicVisualPanel(final WizardDescriptor setting) {
        this.settings = setting;
    }

    public final WizardDescriptor getSettings() {
        return settings;
    }

    /**
     * Set an error message and mark the panel as invalid.
     */
    protected final void setError(String message) {
        if (message == null) {
            throw new NullPointerException();
        }
        setMessage(message);
        setValid(false);
    }
    
    /**
     * Set a warning message but mark the panel as valid.
     */
    protected final void setWarning(String message) {
        setWarning(message, true);
    }
    
    /**
     * Set a warning message and validity of the panel.
     */
    protected final void setWarning(String message, boolean valid) {
        if (message == null) {
            throw new NullPointerException();
        }
        settings.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, message);
        setValid(valid);
    }
    
    /**
     * Set an info message and validity of the panel.
     */
    protected final void setInfo(String message, boolean valid) {
        if (message == null) {
            throw new NullPointerException();
        }
        settings.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, message);
        setValid(valid);
    }
    
    /**
     * Mark the panel as invalid without any message.
     * Use with restraint; generally {@link #setError} is better.
     */
    protected final void markInvalid() {
        setMessage(null);
        setValid(false);
    }
    
    /**
     * Mark the panel as valid and clear any error or warning message.
     */
    protected final void markValid() {
        setMessage(null);
        setValid(true);
    }
    
    private final void setMessage(String message) {
        settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message); // NOI18N
    }
    
    /**
     * Sets this panel's validity and fires event to it's wrapper wizard panel.
     * See {@link BasicWizardPanel#propertyChange} for what happens further.
     */
    private final void setValid(boolean valid) {
        firePropertyChange("valid", null, Boolean.valueOf(valid)); // NOI18N
    }
    
}
