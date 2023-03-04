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
package org.netbeans.modules.web.jsf.icefaces;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.common.ClasspathUtil;
import org.netbeans.modules.web.jsf.icefaces.ui.Icefaces2CustomizerPanelVisual;
import org.netbeans.modules.web.jsf.spi.components.JsfComponentCustomizer;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class Icefaces2Customizer implements JsfComponentCustomizer {

    private static final Logger LOGGER = Logger.getLogger(Icefaces2Customizer.class.getName());
    private Icefaces2CustomizerPanelVisual panel;
    private ChangeSupport changeSupport = new ChangeSupport(this);
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
        if (panel == null) {
            panel = new Icefaces2CustomizerPanelVisual(this);
            panel.initLibraries(true);
        }
        return panel;
    }

    @Override
    public boolean isValid() {
        Preferences preferences = Icefaces2Implementation.getIcefacesPreferences();
        String icefacesLibrary = preferences.get(Icefaces2Implementation.PREF_LIBRARY_NAME, ""); //NOI18N
        if (LibraryManager.getDefault().getLibrary(icefacesLibrary) != null) {
            return true;
        }

        synchronized (this) {
            if (result == null) {
                result = RequestProcessor.getDefault().submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        for (Library library : LibraryManager.getDefault().getLibraries()) {
                            if (!"j2se".equals(library.getType())) { //NOI18N
                                continue;
                            }

                            List<URL> content = library.getContent("classpath"); //NOI18N
                            if (isValidIcefacesLibrary(content)) {
                                refreshParentValidation();
                                return true;
                            }
                        }
                        refreshParentValidation();
                        return false;
                    }

                    private void refreshParentValidation() {
                        // refresh validation of the parent panel
                        SwingUtilities.invokeLater(new Runnable() {
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
        "Icefaces2Customizer.err.searching.icefaces.library=Searching valid ICEfaces library. Please wait..."
    })
    @Override
    public String getErrorMessage() {
        if ((result == null && !isValid()) || (result != null && !result.isDone())) {
            return Bundle.Icefaces2Customizer_err_searching_icefaces_library();
        }
        return panel.getErrorMessage();
    }

    @Override
    public String getWarningMessage() {
        return panel.getWarningMessage();
    }

    @Override
    public void saveConfiguration() {
        Preferences preferences = Icefaces2Implementation.getIcefacesPreferences();
        if (panel.getIcefacesLibrary() != null) {
            preferences.put(Icefaces2Implementation.PREF_LIBRARY_NAME, panel.getIcefacesLibrary());
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
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

    /**
     * Returns {@code List} of all ICEfaces2 libraries registered in the IDE.
     *
     * @return {{@code List} of libraries
     */
    public static List<Library> getIcefacesLibraries() {
        List<Library> libraries = new ArrayList<Library>();
        List<URL> content;
        for (Library library : LibraryManager.getDefault().getLibraries()) {
            if (!"j2se".equals(library.getType())) { //NOI18N
                continue;
            }

            content = library.getContent("classpath"); //NOI18N
            if (isValidIcefacesLibrary(content)) {
                libraries.add(library);
            }
        }
        return libraries;
    }

    /**
     * Checks if given library content contains mandatory class in cases of ICEfaces2 library.
     *
     * @param libraryContent library content
     * @return {@code true} if the given content contains ICEfaces2 library, {@code false} otherwise
     */
    public static boolean isValidIcefacesLibrary(List<URL> libraryContent) {
        try {
            return ClasspathUtil.containsClass(libraryContent, Icefaces2Implementation.ICEFACES_CORE_CLASS);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return false;
        }
    }

}
