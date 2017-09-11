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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.api.options;

import java.awt.Cursor;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.options.CategoryModel;
import org.netbeans.modules.options.OptionsDisplayerImpl;
import org.netbeans.spi.options.OptionsPanelController.ContainerRegistration;
import org.netbeans.spi.options.OptionsPanelController.SubRegistration;
import org.netbeans.spi.options.OptionsPanelController.TopLevelRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
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
    /** Registration name of FontsAndColors category (aka Fonts & Colors).
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
            ProgressUtils.runOffEventDispatchThread(new Runnable() {

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
                ProgressUtils.runOffEventDispatchThread(new Runnable() {

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
