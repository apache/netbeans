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
package org.netbeans.modules.php.project.ui.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.options.OptionsPanelController;

/**
 * Common functionality of {@link OptionsPanelController}.
 */
abstract class BaseOptionsPanelController extends OptionsPanelController implements ChangeListener {

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    volatile boolean changed = false;


    /**
     * Validate the panel (component).
     * @return {@code true} if the panel is valid, {@code false} otherwise
     */
    protected abstract boolean validateComponent();

    /**
     * @see OptionsPanelController#update()
     */
    protected abstract void updateInternal();

    /**
     * @see OptionsPanelController#applyChanges()
     */
    protected abstract void applyChangesInternal();
    
    /**
     * Determine if the panel is modified by the user. This will help the infrastructure
     * to enable or disable the Apply button in options window.
     * @return {@code true} if the panel is modified by the user through the UI, {@code false} otherwise
     */
    protected abstract boolean areOptionsChanged();

    @Override
    public final void update() {
        updateInternal();
        changed = false;
    }

    @Override
    public final void applyChanges() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                applyChangesInternal();
                changed = false;
            }
        });
    }

    @Override
    public final void cancel() {
        changed = false;
    }

    @Override
    public final boolean isValid() {
        return validateComponent();
    }

    @Override
    public final boolean isChanged() {
        return areOptionsChanged();
    }

    @Override
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    @Override
    public final void stateChanged(ChangeEvent e) {
        if (!changed) {
            propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    /**
     * Return interger or {@code null} if the input cannot be parsed.
     * @param input input to be parsed
     * @return interger or {@code null} if the input cannot be parsed
     */
    protected Integer parseInteger(String input) {
        Integer number = null;
        try {
            number = Integer.parseInt(input);
        } catch (NumberFormatException exc) {
            // ignored
        }
        return number;
    }

    /**
     * Get PHP options.
     * @return PHP options
     */
    protected final PhpOptions getPhpOptions() {
        return PhpOptions.getInstance();
    }

}
