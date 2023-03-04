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
package org.netbeans.modules.web.jsf.richfaces;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.common.ClasspathUtil;
import org.netbeans.modules.web.jsf.richfaces.ui.Richfaces4CustomizerPanelVisual;
import org.netbeans.modules.web.jsf.spi.components.JsfComponentCustomizer;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class Richfaces4Customizer implements JsfComponentCustomizer {

    private static final RequestProcessor RP = new RequestProcessor(Richfaces4Customizer.class);
    private Richfaces4CustomizerPanelVisual panel;
    private ChangeSupport changeSupport = new ChangeSupport(this);
    private Future<Boolean> result = null;
    private boolean fixedLibrary = false;

    public static final Logger LOGGER = Logger.getLogger(Richfaces4Customizer.class.getName());

    public Richfaces4Customizer() {
    }

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
            panel = new Richfaces4CustomizerPanelVisual(this);
            panel.initLibraries(true);
        }
        return panel;
    }

    @Override
    public boolean isValid() {
        Preferences preferences = Richfaces4Implementation.getRichfacesPreferences();
        String richfacesLibrary = preferences.get(Richfaces4Implementation.PREF_RICHFACES_LIBRARY, "");
        if (LibraryManager.getDefault().getLibrary(richfacesLibrary) != null) {
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
                            if (isValidRichfacesLibrary(content)) {
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

    @Override
    public String getWarningMessage() {
        return panel.getWarningMessage();
    }

    @NbBundle.Messages({
        "Richfaces4Customizer.err.searching.richfaces.library=Searching valid RichFaces library. Please wait..."
    })
    @Override
    public String getErrorMessage() {
        if ((result == null && !isValid()) || (result != null && !result.isDone())) {
            return Bundle.Richfaces4Customizer_err_searching_richfaces_library();
        }
        return panel.getErrorMessage();
    }

    @Override
    public void saveConfiguration() {
        Preferences preferences = Richfaces4Implementation.getRichfacesPreferences();
        preferences.put(Richfaces4Implementation.PREF_RICHFACES_LIBRARY, panel.getRichFacesLibrary());
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

    public static List<Library> getRichfacesLibraries() {
        List<Library> libraries = new ArrayList<Library>();
        List<URL> content;
        for (Library library : LibraryManager.getDefault().getLibraries()) {
            if (!"j2se".equals(library.getType())) { // NOI18N
                continue;
            }

            content = library.getContent("classpath"); //NOI18N
            if (Richfaces4Customizer.isValidRichfacesLibrary(content)) {
                libraries.add(library);
            }
        }
        return libraries;
    }

    public static boolean isValidRichfacesLibrary(List<URL> libraryContent) {
        Iterator<String> iterator = Richfaces4Implementation.RF_LIBRARIES.iterator();
        while (iterator.hasNext()) {
            String libraryName = iterator.next();
            try {
                if (!ClasspathUtil.containsClass(libraryContent, libraryName)) {
                    return false;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
                return false;
            }
        }
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return panel.getHelpCtx();
    }

}
