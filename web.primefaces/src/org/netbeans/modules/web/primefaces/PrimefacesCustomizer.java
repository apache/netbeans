/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
