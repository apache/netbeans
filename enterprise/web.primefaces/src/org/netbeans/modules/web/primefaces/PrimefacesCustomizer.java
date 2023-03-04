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
package org.netbeans.modules.web.primefaces;

import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.web.jsf.spi.components.JsfComponentCustomizer;
import org.netbeans.modules.web.primefaces.ui.PrimefacesCustomizerPanel;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Customizer of PrimeFaces JSF component library suites.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class PrimefacesCustomizer implements JsfComponentCustomizer {

    private static final RequestProcessor RP = new RequestProcessor(PrimefacesCustomizer.class);
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private PrimefacesCustomizerPanel panel;
    private Future<Boolean> result = null;
    private boolean fixedLibrary = false;

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public JComponent getComponent() {
        return getPanel();
    }

    private synchronized PrimefacesCustomizerPanel getPanel() {
        if (panel == null) {
            panel = new PrimefacesCustomizerPanel(this);
        }
        return panel;
    }

    @Override
    public boolean isValid() {
        Preferences preferences = PrimefacesImplementation.getPrimefacesPreferences();
        String primefacesLib = preferences.get(PrimefacesImplementation.PROP_PREFERRED_LIBRARY, ""); //NOI18N
        if (LibraryManager.getDefault().getLibrary(primefacesLib) != null) {
            return true;
        }

        synchronized (this) {
            if (result == null) {
                result = RP.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        for (Library library : LibraryManager.getDefault().getLibraries()) {
                            if (!"j2se".equals(library.getType())) { //NOI18N
                                continue;
                            }

                            List<URL> content = library.getContent("classpath"); //NOI18N
                            if (PrimefacesImplementation.isValidPrimefacesLibrary(content)) {
                                refreshParentValidation();
                                return true;
                            }
                        }
                        refreshParentValidation();
                        return false;
                    }

                    private void refreshParentValidation() {
                        // refresh validation of the parent panel
                        Mutex.EVENT.readAccess(new Runnable() {
                            @Override
                            public void run() {
                                fireChange();
                            }
                        });
                    }
                });
            } else if (!result.isDone()) {
                return false;
            } else {
                try {
                    return result.get() || fixedLibrary;
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return false;
    }

    @NbBundle.Messages({
        "PrimefacesCustomizer.err.searching.primefaces.library=Searching valid Primefaces library. Please wait..."
    })
    @Override
    public String getErrorMessage() {
        if ((result == null && !isValid()) || (result != null && !result.isDone())) {
            return Bundle.PrimefacesCustomizer_err_searching_primefaces_library();
        }
        return getPanel().getErrorMessage();
    }

    @Override
    public String getWarningMessage() {
        return getPanel().getWarningMessage();
    }

    @Override
    public void saveConfiguration() {
        Preferences preferences = PrimefacesImplementation.getPrimefacesPreferences();
        if (panel.getPrimefacesLibrary() != null) {
            preferences.put(PrimefacesImplementation.PROP_PREFERRED_LIBRARY, panel.getPrimefacesLibrary().getName());
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return panel.getHelpCtx();
    }

    /**
     * Sets to true when the library troubles were fixed.
     * @param fixed whether the library was really fixed
     */
    public void setFixedLibrary(boolean fixed) {
        fixedLibrary = fixed;
    }

    /** Fire event that validation should be redone. */
    public void fireChange() {
        changeSupport.fireChange();
    }
}
