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
