/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.api.options;

import java.awt.Cursor;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.modules.options.CategoryModel;
import org.netbeans.modules.options.OptionsDisplayerImpl;
import org.netbeans.spi.options.OptionsPanelController.ContainerRegistration;
import org.netbeans.spi.options.OptionsPanelController.SubRegistration;
import org.netbeans.spi.options.OptionsPanelController.TopLevelRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Permits Options Dialog to open the options dialog with some category pre-selected.
 * @since 1.5
 * @author Radek Matous
 */
public final class OptionsDisplayer {
    private static final OptionsDisplayer INSTANCE = new OptionsDisplayer();
    private final OptionsDisplayerImpl impl = new OptionsDisplayerImpl(false);
    private static Logger log = Logger.getLogger(OptionsDisplayer.class.getName());
    /** Registration name of Advanced category (aka Miscellaneous). 
     * @since 1.8
     */
    public static final String ADVANCED = "Advanced"; // NOI18N
    /** Registration name of Keymaps category (aka Keymap). 
     * @since 1.29
     */
    public static final String KEYMAPS = "Keymaps"; // NOI18N
    /** Registration name of FontsAndColors category (aka Fonts &amp; Colors).
     * @since 1.29
     */
    public static final String FONTSANDCOLORS = "FontsAndColors"; // NOI18N
    /** Registration name of Editor category.
     * @since 1.29
     */
    public static final String EDITOR = "Editor"; // NOI18N
    /** Registration name of General category.
     * @since 1.29
     */
    public static final String GENERAL = "General"; // NOI18N
    private String currentCategoryID = null;
    private AtomicBoolean operationCancelled;
    private CategoryModel categoryModel;
        
    private OptionsDisplayer() {}    
    /**
     * Get the default <code>OptionsDisplayer</code>
     * @return the default instance
     */
    public static OptionsDisplayer getDefault() {
        return INSTANCE;
    }
    
    /**
     * Open the options dialog (in non-modal mode) with no guarantee which category is pre-selected.
     * @return true if optins dialog was sucesfully opened with some pre-selected
     * category. If no category is registered at all then false will be returned and
     * options dialog won't be opened.
     */
    @NbBundle.Messages("CTL_Loading_Options_Waiting=Loading Options Settings")
    public boolean open() {
        showWaitCursor();
        if (categoryModel == null || operationCancelled == null || operationCancelled.get()) {
            if (operationCancelled == null) {
                operationCancelled = new AtomicBoolean();
            }
            if (operationCancelled.get()) {
                currentCategoryID = null;
                operationCancelled.set(false);
            }
            BaseProgressUtils.runOffEventDispatchThread(new Runnable() {

                @Override
                public void run() {
		    categoryModel = CategoryModel.getInstance();
                    currentCategoryID = categoryModel.getCurrentCategoryID();
                }
            }, Bundle.CTL_Loading_Options_Waiting(), operationCancelled, false, 0 , 3000);
            return open(currentCategoryID);
        }
            
        return open(categoryModel.getCurrentCategoryID());
    }
    
    /**
     * Open the options dialog (in non-modal mode) with some panel preselected.
     * To open a top-level panel, pass its {@link TopLevelRegistration#id}.
     * To open a subpanel, pass its {@link SubRegistration#location} followed by {@code /}
     * followed by its {@link SubRegistration#id}.
     * To open a container panel without specifying a particular subpanel, pass its {@link ContainerRegistration#id}.
     * To avoid typos and keep track of dependencies it is recommended to define compile-time
     * constants for all these IDs, to be used both by the annotations and by calls to this method.
     * @param path slash-separated path of category and perhaps subcategories to be selected
     * @return true if optins dialog was sucesfully opened with required category.
     * If this method is called when options dialog is already opened then this method
     * will return immediately false without affecting currently selected category
     * in opened options dialog.
     * If category (i.e. the first item in the path) does not correspond to any
     * of registered categories then false is returned and options dialog is not opened
     * at all (e.g. in case that module providing such category is not installed or enabled).
     * If subcategory doesn't exist, it opens with category selected and
     * it returns true. It is up to particular <code>OptionsPanelController</code> 
     * to handle such situation.
     * @since 1.8
     */
    public boolean open(final String path) {
        log.fine("Open Options Dialog: " + path); //NOI18N
        showWaitCursor();
        try {
	    if (path != null && (categoryModel == null || operationCancelled == null || operationCancelled.get())) {
                if (operationCancelled == null) {
                    operationCancelled = new AtomicBoolean();
                }
                if (operationCancelled.get()) {
                    operationCancelled.set(false);
                }
                BaseProgressUtils.runOffEventDispatchThread(new Runnable() {

                    @Override
                    public void run() {
			categoryModel = CategoryModel.getInstance();
                        categoryModel.getCategoryIDs();
                    }
                }, Bundle.CTL_Loading_Options_Waiting(), operationCancelled, false, 0, 3000);
                if(operationCancelled.get()) {
                    return true;
                }
            }
            return openImpl(path);
        } finally {
            hideWaitCursor();
        }
    }

    /**
     * Open the options dialog with no guarantee which category is pre-selected.
     * @param isModal true if the options window should be in modal mode, false otherwise
     * @return true if options dialog was successfully opened with some pre-selected
     * category. If no category is registered at all then false will be returned and
     * options dialog won't be opened.
     * @since 1.33
     */
    public boolean open(boolean isModal) {
	impl.setIsModal(isModal);
	return open();
    }

    /**
     * Open the options dialog with some panel preselected.
     * To open a top-level panel, pass its {@link TopLevelRegistration#id}.
     * To open a subpanel, pass its {@link SubRegistration#location} followed by {@code /}
     * followed by its {@link SubRegistration#id}.
     * To open a container panel without specifying a particular subpanel, pass its {@link ContainerRegistration#id}.
     * To avoid typos and keep track of dependencies it is recommended to define compile-time
     * constants for all these IDs, to be used both by the annotations and by calls to this method.
     * @param path slash-separated path of category and perhaps subcategories to be selected
     * @param isModal true if the options window should be in modal mode, false otherwise
     * @return true if options dialog was successfully opened with required category.
     * If this method is called when options dialog is already opened then this method
     * will return immediately false without affecting currently selected category
     * in opened options dialog.
     * If category (i.e. the first item in the path) does not correspond to any
     * of registered categories then false is returned and options dialog is not opened
     * at all (e.g. in case that module providing such category is not installed or enabled).
     * If subcategory doesn't exist, it opens with category selected and
     * it returns true. It is up to particular <code>OptionsPanelController</code>
     * to handle such situation.
     * @since 1.33
     */
    public boolean open(String path, boolean isModal) {
	impl.setIsModal(isModal);
	return open(path);
    }

    private boolean openImpl(final String path) {
        if(path == null) {
            log.warning("Category to open is null."); //NOI18N
            return false;
        }
        final String categoryId = path.indexOf('/') == -1 ? path : path.substring(0, path.indexOf('/'));
        final String subpath = path.indexOf('/') == -1 ? null : path.substring(path.indexOf('/')+1);
        Boolean retval = Mutex.EVENT.readAccess(new Mutex.Action<Boolean> () {
            public Boolean run() {
                Boolean r = impl.isOpen();
                boolean retvalForRun = !r;
                if (retvalForRun) {
                    retvalForRun = Arrays.asList(categoryModel.getCategoryIDs()).contains(categoryId);
                    if (!retvalForRun) {
                        log.warning("Unknown categoryId: " + categoryId); //NOI18N
                    }
                } else {
                    log.warning("Options Dialog is opened"); //NOI18N
                }
                if (retvalForRun) {
                    log.fine("openImpl:impl.showOptionsDialog(" + categoryId+ ", " + subpath+ ")");
                    impl.showOptionsDialog(categoryId, subpath, categoryModel);
                }
                log.fine("openImpl return " + Boolean.valueOf(retvalForRun));
		categoryModel = null;
		operationCancelled = null;
                return Boolean.valueOf(retvalForRun);
            }
        });
        return retval;
    }

    private static void showWaitCursor() {
        Mutex.EVENT.readAccess(new Runnable() {

            public void run() {
                JFrame mainWindow = (JFrame) WindowManager.getDefault().getMainWindow();
                mainWindow.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                mainWindow.getGlassPane().setVisible(true);
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(OptionsDisplayerImpl.class, "CTL_Loading_Options"));
            }
        });
    }

    private static void hideWaitCursor() {
        Mutex.EVENT.readAccess(new Runnable() {

            public void run() {
                StatusDisplayer.getDefault().setStatusText("");  //NOI18N
                JFrame mainWindow = (JFrame) WindowManager.getDefault().getMainWindow();
                mainWindow.getGlassPane().setVisible(false);
                mainWindow.getGlassPane().setCursor(null);
            }
        });
    }
}
