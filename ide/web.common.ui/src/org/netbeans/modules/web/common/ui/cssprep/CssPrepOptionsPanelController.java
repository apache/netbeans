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
package org.netbeans.modules.web.common.ui.cssprep;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.common.ui.api.CssPreprocessorUI;
import org.netbeans.modules.web.common.ui.spi.CssPreprocessorUIImplementation;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

public final class CssPrepOptionsPanelController extends OptionsPanelController implements ChangeListener {

    private static final Logger LOGGER = Logger.getLogger(CssPrepOptionsPanelController.class.getName());

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final List<CssPreprocessorUIImplementation.Options> allOptions = new CopyOnWriteArrayList<>();

    // @GuardedBy("EDT")
    private volatile CssPrepOptionsPanel cssPrepOptionsPanel = null;
    private volatile boolean changed = false;


    @Override
    public void update() {
        assert EventQueue.isDispatchThread();
        for (CssPreprocessorUIImplementation.Options options : allOptions) {
            options.update();
        }

        changed = false;
    }

    @Override
    public void applyChanges() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (CssPreprocessorUIImplementation.Options options : allOptions) {
                    assert options.isValid() : "Saving invalid options: " + options.getDisplayName() + " (error: " + options.getErrorMessage() + ")";
                    try {
                        options.save();
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Error while saving CSS preprocessor: " + options.getDisplayName(), ex);
                    }
                }

                changed = false;
            }
        });
    }

    @Override
    public void cancel() {
    }

    @Override
    public boolean isValid() {
        assert EventQueue.isDispatchThread();
        CssPrepOptionsPanel panel = getCssPrepOptionsPanel();
        String warning = null;
        for (CssPreprocessorUIImplementation.Options options : allOptions) {
            if (!options.isValid()) {
                String error = options.getErrorMessage();
                Parameters.notNull("error", error); // NOI18N
                panel.setError(error);
                return false;
            }
            if (warning == null) {
                warning = options.getWarningMessage();
            }
        }
        if (warning != null) {
            panel.setWarning(warning);
        } else {
            // everything ok
            panel.setError(" "); // NOI18N
        }
        return true;
    }

    @Override
    public boolean isChanged() {
        boolean isChanged = false;
        for (CssPreprocessorUIImplementation.Options options : allOptions) {
            isChanged |= options.changed();
        }
        return isChanged;
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        assert EventQueue.isDispatchThread();
        return getCssPrepOptionsPanel();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.web.common.cssprep.CssPrepOptionsPanelController"); // NOI18N
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (!changed) {
            changed = true;
            propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    private CssPrepOptionsPanel getCssPrepOptionsPanel() {
        assert EventQueue.isDispatchThread();
        if (cssPrepOptionsPanel == null) {
            for (CssPreprocessorUI preprocessor : CssPreprocessorsAccessor.getDefault().getPreprocessors()) {
                CssPreprocessorUIImplementation.Options options = CssPreprocessorAccessor.getDefault().createOptions(preprocessor);
                if (options != null) {
                    allOptions.add(options);
                }
            }
            cssPrepOptionsPanel = new CssPrepOptionsPanel(new ArrayList<>(allOptions));
            for (CssPreprocessorUIImplementation.Options options : allOptions) {
                options.addChangeListener(this);
            }
        }
        return cssPrepOptionsPanel;
    }

}
