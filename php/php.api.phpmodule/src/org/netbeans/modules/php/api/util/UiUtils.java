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

package org.netbeans.modules.php.api.util;

import java.awt.Image;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.ProjectProblems;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.ui.SearchPanel;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Miscellaneous UI utilities.
 * @author Tomas Mysik
 */
public final class UiUtils {
    /**
     * SFS path where all the PHP options can be found.
     */
    public static final String OPTIONS_PATH = "org-netbeans-modules-php-project-ui-options-PHPOptionsCategory"; // NOI18N
    /**
     * SFS path where all the PHP frameworks and tools can be found.
     */
    public static final String FRAMEWORKS_AND_TOOLS_SUB_PATH = "FrameworksAndTools"; // NOI18N
    /**
     * SFS full path where all the PHP frameworks and tools can be found.
     */
    public static final String FRAMEWORKS_AND_TOOLS_OPTIONS_PATH = OPTIONS_PATH + "/" + FRAMEWORKS_AND_TOOLS_SUB_PATH; // NOI18N
    /**
     * SFS path where all the PHP customizer panels can be found.
     */
    public static final String CUSTOMIZER_PATH = "org-netbeans-modules-php-project"; // NOI18N
    /**
     * The General Options category ID.
     */
    public static final String GENERAL_OPTIONS_SUBCATEGORY = "General"; // NOI18N

    private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER = "Tree.openIcon"; // NOI18N
    private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.openedIcon"; // NOI18N
    private static final String ICON_PATH = "org/netbeans/modules/php/api/ui/resources/defaultFolder.gif"; // NOI18N
    private static final String OPENED_ICON_PATH = "org/netbeans/modules/php/api/ui/resources/defaultFolderOpen.gif"; // NOI18N


    private UiUtils() {
    }

    /**
     * Display a dialog with the message and then open IDE PHP options.
     * @param message message to display before IDE options are opened
     * @see #invalidScriptProvided(String, String)
     */
    public static void invalidScriptProvided(@NonNull String message) {
        invalidScriptProvided(message, null);
    }

    /**
     * Display a dialog with the message and then open IDE options.
     * @param message message to display before IDE options are opened
     * @param optionsSubcategory IDE options subcategory to open (suitable e.g. for frameworks), can be {@code null}
     * @see #invalidScriptProvided(String)
     */
    public static void invalidScriptProvided(@NonNull String message, @NullAllowed String optionsSubcategory) {
        Parameters.notNull("message", message); // NOI18N

        informAndOpenOptions(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE), optionsSubcategory);
    }

    /**
     * Open project customizer for the given category. Possibly display
     * dialog with a message first.
     * @param phpModule PHP module to be used
     * @param customizerCategory customizer category to be opened
     * @param message message to be displayed before project customizer is opened
     * @since 2.38
     */
    public static void invalidScriptProvided(@NonNull PhpModule phpModule, @NonNull String customizerCategory,
            @NullAllowed String message) {
        Parameters.notNull("phpModule", phpModule); // NOI18N
        Parameters.notNull("customizerCategory", customizerCategory); // NOI18N

        NotifyDescriptor descriptor = null;
        if (message != null) {
            descriptor = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
        }
        openCustomizer(phpModule, customizerCategory, descriptor);
    }

    /**
     * Show a dialog that informs user about exception during running an external process.
     * Opens IDE options, PHP General category.
     * @param exc {@link ExecutionException} thrown
     * @see #processExecutionException(ExecutionException, String)
     */
    public static void processExecutionException(@NonNull ExecutionException exc) {
        processExecutionException(exc, null);
    }

    /**
     * Show a dialog that informs user about exception during running an external process.
     * Opens IDE options, PHP &lt;subcategory> category or General category if no <code>subcategory</code> given.
     * @param exc {@link ExecutionException} thrown
     * @param optionsSubcategory IDE options subcategory to open (suitable e.g. for frameworks), can be {@code null}
     * @see #processExecutionException(ExecutionException)
     */
    public static void processExecutionException(@NonNull final ExecutionException exc, @NullAllowed final String optionsSubcategory) {
        Parameters.notNull("exc", exc); // NOI18N
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                informAndOpenOptions(createNotifyDescriptor(exc), optionsSubcategory);
            }
        });
    }

    /**
     * Show a dialog that informs user about exception during running an external process.
     * Open project customizer for the given category.
     * @param exc {@link ExecutionException} thrown
     * @param phpModule PHP module to be used
     * @param customizerCategory customizer category to be opened
     * @since 2.38
     */
    public static void processExecutionException(@NonNull final ExecutionException exc, @NonNull final PhpModule phpModule,
            @NonNull final String customizerCategory) {
        Parameters.notNull("exc", exc); // NOI18N
        Parameters.notNull("phpModule", phpModule); // NOI18N
        Parameters.notNull("customizerCategory", customizerCategory); // NOI18N
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                openCustomizer(phpModule, customizerCategory, createNotifyDescriptor(exc));
            }
        });
    }

    /**
     * Display Options dialog with PHP > General panel preselected.
     * @see #showOptions(String)
     */
    public static void showGeneralOptions() {
        showOptions(null);
    }

    /**
     * Display Options dialog with PHP > &lt;subcategory> panel preselected.
     * @param optionsSubcategory PHP Options subcategory to be opened, can be {@code null} (then, the General panel is opened)
     * @see #showGeneralOptions()
     */
    public static void showOptions(@NullAllowed String optionsSubcategory) {
        String path = OPTIONS_PATH;
        if (!StringUtils.hasText(optionsSubcategory)) {
            optionsSubcategory = GENERAL_OPTIONS_SUBCATEGORY;
        }
        OptionsDisplayer.getDefault().open(path + "/" + optionsSubcategory); // NOI18N
    }

    /**
     * Returns default folder icon as {@link Image}. Never returns {@code null}.
     * @param opened whether closed or opened icon should be returned
     * @return default folder icon
     */
    public static Image getTreeFolderIcon(boolean opened) {
        Image base = (Image) UIManager.get(opened ? OPENED_ICON_KEY_UIMANAGER_NB : ICON_KEY_UIMANAGER_NB); // #70263
        if (base == null) {
            Icon baseIcon = UIManager.getIcon(opened ? OPENED_ICON_KEY_UIMANAGER : ICON_KEY_UIMANAGER); // #70263
            if (baseIcon != null) {
                base = ImageUtilities.icon2Image(baseIcon);
            } else { // fallback to our owns
                base = ImageUtilities.loadImage(opened ? OPENED_ICON_PATH : ICON_PATH, false);
            }
        }
        return base;
    }

    /**
     * Informs user about broken PHP module.
     * <p>
     * This method shows a dialog with possibility to open Project Problems dialog.
     * @param phpModule broken PHP module
     * @since 2.45
     */
    @NbBundle.Messages({
        "# {0} - project name",
        "UiUtils.metadata.corrupted=<html><b>Project {0} is corrupted.</b><br><br>Do you want to open Project Problems dialog?"
    })
    public static void warnBrokenProject(PhpModule phpModule) {
        Parameters.notNull("phpModule", phpModule); // NOI18N
        assert phpModule.isBroken() : "Not broken php module " + phpModule.getName();
        String name = phpModule.getDisplayName();
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                Bundle.UiUtils_metadata_corrupted(name),
                name,
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.WARNING_MESSAGE);
        if (DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.YES_OPTION) {
            Project project = FileOwnerQuery.getOwner(phpModule.getProjectDirectory());
            assert project != null : "Must found project for " + phpModule.getProjectDirectory();
            ProjectProblems.showCustomizer(project);
        }
    }

    static NotifyDescriptor createNotifyDescriptor(ExecutionException exc) {
        assert exc != null;
        final Throwable cause = exc.getCause();
        assert cause != null;
        return new NotifyDescriptor.Message(
                NbBundle.getMessage(UiUtils.class, "MSG_ExceptionDuringRunScript", cause.getLocalizedMessage()),
                NotifyDescriptor.ERROR_MESSAGE);
    }

    static void openCustomizer(@NonNull PhpModule phpModule, @NonNull String customizerCategory, @NullAllowed NotifyDescriptor descriptor) {
        assert phpModule != null;
        assert customizerCategory != null;
        if (descriptor != null) {
            DialogDisplayer.getDefault().notify(descriptor);
        }
        phpModule.getLookup().lookup(CustomizerProvider2.class).showCustomizer(customizerCategory, null);
    }

    static void informAndOpenOptions(NotifyDescriptor descriptor, String optionsSubcategory) {
        assert descriptor != null;
        DialogDisplayer.getDefault().notify(descriptor);
        showOptions(optionsSubcategory);
    }

    /**
     * Utility class for searching which is done in a separate thread so the UI is not blocked.
     */
    public static final class SearchWindow {
        private SearchWindow() {
        }

        /**
         * Open a serch window, start searching (in a separate thread) and display the results.
         * @param support {@link SearchWindowSupport search window support}
         * @return selected item (can be <code>null</code>) if user clicks OK button, <code>null</code> otherwise
         */
        @CheckForNull
        public static String search(SearchWindowSupport support) {
            Parameters.notNull("support", support);

            SearchPanel panel = SearchPanel.create(support);
            if (panel.open()) {
                return panel.getSelectedItem();
            }
            return null;
        }

        public interface SearchWindowSupport {
            /**
             * Detector which runs in a separate thread and its results are displayed to a user.
             * @return list of search result
             */
            List<String> detect();
            /**
             * Get the title of the window.
             * @return the title of the window
             */
            String getWindowTitle();
            /**
             * Get the title of the list of items.
             * @return the title of the list of items
             */
            String getListTitle();
            /**
             * Get the "important" part (e.g. "PHPUnit script") of message that is displayed during running of a {@link #detect() detect} method.
             * @return the "important" part (e.g. "PHPUnit script") of message that is displayed during running of a {@link #detect() detect} method
             */
            String getPleaseWaitPart();
            /**
             * Get message that is displayed when no items are found.
             * @return message that is displayed when no items are found
             */
            String getNoItemsFound();
        }
    }

    /**
     * Registers a subpanel inside PHP's Framework and Tools panel.
     * Should be placed on a {@link org.netbeans.spi.options.OptionsPanelController} instance.
     * @see org.netbeans.spi.options.AdvancedOption
     * @since 2.35
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PhpOptionsPanelRegistration {

        /**
         * Panel identifier.
         * @return panel identifier
         */
        String id();

        /**
         * Label shown on the tab. You may use {@code #key} syntax.
         * @return tab label
         */
        String displayName();

        /**
         * Optional keywords (separated by commas) for use with Quick Search (must also specify {@link #keywordsCategory}).
         * You may use {@code #key} syntax.
         * @return optional keywords (separated by commas)
         */
        String keywords() default ""; // NOI18N

        /**
         * Keyword category for use with Quick Search (must also specify {@link #keywords}).
         * @return keyword category for use with Quick Search
         */
        String keywordsCategory() default ""; // NOI18N

        /**
         * Position relative to sibling subpanels.
         * @return position relative to sibling subpanels
         */
        int position() default Integer.MAX_VALUE;

    }

}
