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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JDialog;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import static org.netbeans.modules.apisupport.project.ui.customizer.Bundle.*;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Convenient class to be used by {@link CustomizerProvider} implementations.
 *
 * @author Martin Krauskopf
 */
public abstract class BasicCustomizer implements CustomizerProvider {
    
    static final String LAST_SELECTED_PANEL = "lastSelectedPanel"; // NOI18N
    
    /** Project <code>this</code> customizer customizes. */
    private final Project project;
    
    /** Keeps reference to a dialog representing <code>this</code> customizer. */
    private Dialog dialog;
    
    private String lastSelectedCategory;
    
    
    private String layerPath;
    
    protected BasicCustomizer(final Project project, String path) {
        this.project = project;
        layerPath = path;
    }
    
    /**
     * All changes should be store at this point. Is called under the write
     * access from {@link ProjectManager#mutex}.
     */
    abstract void storeProperties() throws IOException;
    
    /**
     * Be sure that you will prepare all the data (typically subclass of {@link
     * ModuleProperties}) needed by a customizer and its panels and that the
     * data is always up-to-date after this method was called.
     *
     * <i>NOTE: Since 6.8 called from background thread.</i>
     */
    abstract Lookup prepareData();
    
    abstract void dialogCleanup();
    
    
    protected Project getProject() {
        return project;
    }
    
    /** Show customizer with the first category selected. */
    @Override public void showCustomizer() {
        showCustomizer(null);
    }
    
    /** Show customizer with preselected category. */
    public void showCustomizer(String preselectedCategory) {
        showCustomizer(preselectedCategory, null);
    }

    @Messages({
        "PROGRESS_loading_data=Loading project information",
        "# {0} - project display name", "LBL_CustomizerTitle=Project Properties - {0}"
    })
    public void showCustomizer(String preselectedCategory, final String preselectedSubCategory) {
        if (dialog != null) {
            dialog.setVisible(true);
        } else {
            final String category = (preselectedCategory != null) ? preselectedCategory : lastSelectedCategory;
            final AtomicReference<Lookup> context = new AtomicReference<Lookup>();
            ProgressUtils.runOffEventDispatchThread(new Runnable() {
                @Override public void run() {
                    context.set(new ProxyLookup(prepareData(), Lookups.fixed(new SubCategoryProvider(category, preselectedSubCategory))));
                }
            }, PROGRESS_loading_data(), /* currently unused */new AtomicBoolean(), false);
            if (context.get() == null) { // canceled
                return;
            }
            OptionListener listener = new OptionListener();
            dialog = ProjectCustomizer.createCustomizerDialog(layerPath, context.get(), category, listener, null);
            dialog.addWindowListener(listener);
            dialog.setTitle(LBL_CustomizerTitle(ProjectUtils.getInformation(getProject()).getDisplayName()));
            dialog.setVisible(true);
        }
    }
    
    
    public final void save() {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override public Void run() throws IOException {
                    storeProperties();
                    ProjectManager.getDefault().saveProject(project);
                    return null;
                }
            });
        } catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException)e.getException());
        }
    }
    
    private String findLastSelectedCategory() {
        if (dialog instanceof JDialog) {
            return (String)((JDialog)dialog).getRootPane().getClientProperty(BasicCustomizer.LAST_SELECTED_PANEL);
        }
        return null;
    }
    
    protected class OptionListener extends WindowAdapter implements ActionListener {
        
        // Listening to OK button ----------------------------------------------
        @Override public void actionPerformed(ActionEvent e) {
            save();
        }
        
        // remove dialog for this customizer's project
        @Override
        public void windowClosed(WindowEvent e) {
            doClose();
        }
        
        @Override
        public void windowClosing(WindowEvent e) {
            // Dispose the dialog otherwise the
            // {@link WindowAdapter#windowClosed} may not be called
            doClose();
        }
        
        public void doClose() {
            if (dialog != null) {
                lastSelectedCategory = findLastSelectedCategory();
                dialog.removeWindowListener(this);
                dialog.setVisible(false);
                dialog.dispose();
                dialogCleanup();
            }
            dialog = null;
        }
        
    }
    

    
    public static final class SubCategoryProvider {

        private String subcategory;

        private String category;

        SubCategoryProvider(String category, String subcategory) {
            this.category = category;
            this.subcategory = subcategory;
        }
        public String getCategory() {
            return category;
        }
        public String getSubcategory() {
            return subcategory;
        }
    }
    
}

