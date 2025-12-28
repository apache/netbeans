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

package org.netbeans.spi.project.ui.support;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.project.uiapi.CategoryModel;
import org.netbeans.modules.project.uiapi.CategoryView;
import org.netbeans.modules.project.uiapi.CategoryChangeSupport;
import org.netbeans.modules.project.uiapi.CustomizerDialog;
import org.netbeans.modules.project.uiapi.CustomizerPane;
import org.netbeans.modules.project.uiapi.Utilities;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/** Support for creating dialogs which can be used as project
 * customizers. The dialog may display multiple panels or categories.
 * @see org.netbeans.spi.project.ui.CustomizerProvider
 * @see ProjectCustomizer.Category
 *
 * @author Petr Hrebejk, Martin Krauskopf
 */
public final class ProjectCustomizer {
    
    /** Factory/Namespace class only. */
    private ProjectCustomizer() {
    }
    
    private static final Logger LOG = Logger.getLogger(ProjectCustomizer.class.getName());

    /** Creates standard customizer dialog which can be used for implementation
     * of {@link org.netbeans.spi.project.ui.CustomizerProvider}. You don't need
     * to call <code>pack()</code> method on the dialog. The resulting dialog will
     * be non-modal. <br>
     * Call <code>show()</code> on the dialog to make it visible. The dialog 
     * will be closed automatically after click on "OK" or "Cancel" button.
     * 
     * @param categories nonempty array of descriptions of categories to be shown in the
     *        dialog. Note that categories have the <code>valid</code>
     *        property. If any of the given categories is not valid cusomizer's
     *        OK button will be disabled until all categories become valid
     *        again.
     * @param componentProvider creator of GUI components for categories in the
     *        customizer dialog.
     * @param preselectedCategory name of one of the supplied categories or null.
     *        Category with given name will be selected. If  <code>null</code>
     *        or if the category of given name does not exist the first category will
     *        be selected.
     * @param okOptionListener listener which will be notified when the user presses
     *        the OK button.
     * @param helpCtx Help context for the dialog, which will be used when the
     *        panels in the customizer do not specify their own help context.
     * @return standard project customizer dialog.
     */
    public static Dialog createCustomizerDialog( Category[] categories,
                                                 CategoryComponentProvider componentProvider,
                                                 String preselectedCategory,
                                                 @NonNull ActionListener okOptionListener,
                                                 HelpCtx helpCtx ) {
        return createCustomizerDialog(categories, componentProvider, preselectedCategory, okOptionListener, null, helpCtx);
    }
    
    /** Creates standard customizer dialog which can be used for implementation
     * of {@link org.netbeans.spi.project.ui.CustomizerProvider}. Use this version if you need 
     * to run processing of the customizer data partially off AWT Event Queue. You don't need
     * to call <code>pack()</code> method on the dialog. The resulting dialog will
     * be non-modal. <br>
     * Call <code>show()</code> on the dialog to make it visible. If you want the dialog to be
     * closed after user presses the "OK" button you have to call hide() and dispose() on it.
     * (Usually in the <code>actionPerformed(...)</code> method of the listener
     * you provided as a parameter. In case of the click on the "Cancel" button
     * the dialog will be closed automatically.
     * @since org.netbeans.modules.projectuiapi/1 1.26
     * @param categories nonempty array of descriptions of categories to be shown in the
     *        dialog. Note that categories have the <code>valid</code>
     *        property. If any of the given categories is not valid cusomizer's
     *        OK button will be disabled until all categories become valid
     *        again.
     * @param componentProvider creator of GUI components for categories in the
     *        customizer dialog.
     * @param preselectedCategory name of one of the supplied categories or null.
     *        Category with given name will be selected. If  <code>null</code>
     *        or if the category of given name does not exist the first category will
     *        be selected.
     * @param okOptionListener listener which will be notified when the user presses
     *        the OK button.
     * @param storeListener listener which will be notified when the user presses OK button.
     *        Listener will be executed after okOptionListener outside of AWT EventQueue.
     *        Usually to be used to save modified files on disk.
     * @param helpCtx Help context for the dialog, which will be used when the
     *        panels in the customizer do not specify their own help context.
     * @return standard project customizer dialog.
     */
    public static Dialog createCustomizerDialog( Category[] categories,
                                                 CategoryComponentProvider componentProvider,
                                                 String preselectedCategory,
                                                 @NonNull ActionListener okOptionListener,
                                                 @NullAllowed ActionListener storeListener,
                                                 HelpCtx helpCtx ) {
        Parameters.notNull("okOptionListener", okOptionListener);
        CustomizerPane innerPane = createCustomizerPane(categories, componentProvider, preselectedCategory);
        Dialog dialog = CustomizerDialog.createDialog(okOptionListener, storeListener, innerPane, helpCtx, categories, componentProvider);
        return dialog;
    }
    
    /**
     * Creates standard customizer dialog that can be used for implementation of
     * {@link org.netbeans.spi.project.ui.CustomizerProvider} based on content of a folder in Layers.
     * Use this method when you want to allow composition and 3rd party additions to your customizer UI.
     * You don't need to call <code>pack()</code> method on the dialog. The resulting dialog will
     * be non-modal. <br> 
     * Call <code>show()</code> on the dialog to make it visible. The dialog 
     * will be closed automatically after click on "OK" or "Cancel" button.
     * 
     * @since org.netbeans.modules.projectuiapi/1 1.15
     * @param folderPath the path in the System Filesystem that is used as root for panel composition.
     *        The content of the folder is assummed to be {@link org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider} instances
     * @param context the context for the panels, up to the project type what the context shall be, for example org.netbeans.api.project.Project instance
     * @param preselectedCategory name of one of the supplied categories or null.
     *        Category with given name will be selected. If  <code>null</code>
     *        or if the category of given name does not exist the first category will
     *        be selected.
     * @param okOptionListener listener which will be notified when the user presses
     *        the OK button.
     * @param helpCtx Help context for the dialog, which will be used when the
     *        panels in the customizer do not specify their own help context.
     * @return standard project customizer dialog.
     */
    public static Dialog createCustomizerDialog( String folderPath,
                                                 Lookup context,
                                                 String preselectedCategory,
                                                 @NonNull ActionListener okOptionListener,
                                                 HelpCtx helpCtx) {
        return createCustomizerDialog(folderPath, context, preselectedCategory, 
                                      okOptionListener, null, helpCtx);
    }
    
    /**
     * Creates standard customizer dialog that can be used for implementation of
     * {@link org.netbeans.spi.project.ui.CustomizerProvider} based on content of a folder in Layers.
     * Use this method when you want to allow composition and 3rd party additions to your customizer UI.
     * This version runs processing of the customizer data partially off AWT Event Queue.
     * You don't need to call <code>pack()</code> method on the dialog. The resulting dialog will
     * be non-modal. <br> 
     * Call <code>show()</code> on the dialog to make it visible. If you want the dialog to be
     * closed after user presses the "OK" button you have to call hide() and dispose() on it.
     * (Usually in the <code>actionPerformed(...)</code> method of the listener
     * you provided as a parameter. In case of the click on the "Cancel" button
     * the dialog will be closed automatically.
     * 
     * @since org.netbeans.modules.projectuiapi/1 1.26
     * @param folderPath the path in the System Filesystem that is used as root for panel composition.
     *        The content of the folder is assummed to be {@link org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider} instances
     * @param context the context for the panels, up to the project type what the context shall be, for example org.netbeans.api.project.Project instance
     * @param preselectedCategory name of one of the supplied categories or null.
     *        Category with given name will be selected. If  <code>null</code>
     *        or if the category of given name does not exist the first category will
     *        be selected.
     * @param okOptionListener listener which will be notified when the user presses
     *        the OK button.
     * @param storeListener listener which will be notified when the user presses OK button.
     *        Listener will be executed after okOptionListener outside of AWT EventQueue.
     *        Usually to be used to save modified files on disk
     * @param helpCtx Help context for the dialog, which will be used when the
     *        panels in the customizer do not specify their own help context.
     * @return standard project customizer dialog.
     */
    public static Dialog createCustomizerDialog( String folderPath,
                                                 Lookup context,
                                                 String preselectedCategory,
                                                 @NonNull ActionListener okOptionListener,
                                                 @NullAllowed ActionListener storeListener,
                                                 HelpCtx helpCtx) {
        FileObject root = FileUtil.getConfigFile(folderPath);
        if (root == null) {
            throw new IllegalArgumentException("The designated path " + folderPath + " doesn't exist. Cannot create customizer.");
        }
        DataFolder def = DataFolder.findFolder(root);
        assert def != null : "Cannot find DataFolder for " + folderPath;
        DelegateCategoryProvider prov = new DelegateCategoryProvider(def, context);
        Category[] categories = prov.getSubCategories();
        if (categories.length == 0) {
            return new JDialog((Frame) null, "<broken>"); // what else to do?
        }
        return createCustomizerDialog(categories, prov, preselectedCategory, okOptionListener, storeListener, helpCtx);
    }
    
    /** Creates standard innerPane for customizer dialog.
     */
    private static CustomizerPane createCustomizerPane( Category[] categories,
                                                CategoryComponentProvider componentProvider,
                                                String preselectedCategory ) {
        
        CategoryChangeSupport changeSupport = new CategoryChangeSupport();
        registerCategoryChangeSupport(changeSupport, categories);
        
        CategoryModel categoryModel = new CategoryModel( categories );
        JPanel categoryView = new CategoryView( categoryModel );
        CustomizerPane customizerPane = new CustomizerPane( categoryView, categoryModel, componentProvider );
        
        if ( preselectedCategory == null ) {
            preselectedCategory = categories[0].getName();
        }
        
        Category c = categoryModel.getCategory( preselectedCategory );
        if ( c != null ) {
            categoryModel.setCurrentCategory( c );
        }
        
        return customizerPane;
    }

    private static void registerCategoryChangeSupport(final CategoryChangeSupport changeSupport, 
            final Category[] categories) {        
        for (int i = 0; i < categories.length; i++) {
            Utilities.putCategoryChangeSupport(categories[i], changeSupport);
            Category[] subCategories = categories[i].getSubcategories();
            if (subCategories != null) {
                registerCategoryChangeSupport(changeSupport, subCategories);
            }
        }
    }

    
    /** Provides components for categories.
     */
    public static interface CategoryComponentProvider {
        
        /** Creates component which has to be shown for given category.
         * @param category The Category
         * @return UI component for category customization
         */
        JComponent create( Category category );
        
    }

    /**
     * Interface for creation of Customizer categories and their respective UI panels.
     * Used by {@link ProjectCustomizer#createCustomizerDialog(String,Lookup,String,ActionListener,HelpCtx)}.
     * <p>The panel/category created by the provider can get notified that the customizer got
     * closed by setting an <code>ActionListener</code> to
     * {@link ProjectCustomizer.Category#setOkButtonListener}.
     * <p>Implementations can be registered using {@link Registration}.
     * Otherwise they can be manually registered in a tree structure in the system filesystem.
     * UI Component can be defined for category folder that is represented as node with subnodes in the category
     * tree of project customizer. The file that defines the instance class in layer for such category
     * must be named {@code Self}. Such a provider will not have the {@link #createCategory} method called
     * (display name will be taken from the folder), but will have the children created by
     * the infrastructure based on the folder content.
     * For details and usage see issue #91276.
     * @since org.netbeans.modules.projectuiapi/1 1.22
     */
    public static interface CompositeCategoryProvider {

        /**
         * create the Category instance for the given project customizer context.
         * @param context Lookup instance passed from project The content is up to the project type, please consult documentation
         * for the project type you want to integrate your panel into.
         * @return A category instance, can be null, in which case no category and no panels are created for given context.
         *   The instance is expected to have no subcategories.
         */
        Category createCategory( Lookup context );

        /**
         * create the UI component for given category and context.
         * The panel/category created by the provider can get notified that the customizer got
         * closed by setting an <code>ActionListener</code> to 
         * {@link org.netbeans.spi.project.ui.support.ProjectCustomizer.Category#setOkButtonListener}.
         * @param category Category instance that was created in the createCategory method.
         * @param context Lookup instance passed from project The content is up to the project type, please consult documentation
         * for the project type you want to integrate your panel into.
         */
        JComponent createComponent (Category category, Lookup context );

        /**
         * Used to register customizer panels.
         * There are three ways this annotation can be used:
         * <ol>
         * <li>Register a "leaf" panel with no children.
         *     {@link #category} can be omitted for a top-level panel;
         *     if specified, the panel is placed in the named subcategory.
         *     {@link #categoryLabel} should not be specified.
         *     The annotation must be placed on a class or factory method implementing {@link CompositeCategoryProvider}.
         * <li>Register a category folder with no panel.
         *     {@link #category} must be specified; the last path component is the
         *     folder being defined, and any previous components are parent folders.
         *     {@link #categoryLabel} must be specified.
         *     The annotation must be placed on some package declaration (in {@code package-info.java}).
         * <li>Register a category folder also with its own panel (i.e. {@code Self}).
         *     {@link #category} and {@link #categoryLabel} must be specified as for #2,
         *     but the annotation must be on a provider implementation as for #1.
         * </ol>
         * To represent hierarchies of panels, the {@link #category} of a #1 can
         * match the {@link #category} of a #2 or #3, and the {@link #category} of a #2 or #3
         * preceding the last {@code /} can match the {@link #category} of another #2 or #3.
         * <p>Multiple registrations may be made in one place using {@link Registrations}.
         * @since org.netbeans.modules.projectuiapi/1 1.38
         */
        @Target({ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE})
        @Retention(RetentionPolicy.SOURCE)
        @interface Registration {
            /**
             * Project type to associate with, such as {@code org-netbeans-modules-java-j2seproject}.
             * The {@code folderPath} passed to {@link ProjectCustomizer#createCustomizerDialog(String,Lookup,String,ActionListener,HelpCtx)}
             * should be {@code Projects/<projectType>/Customizer}.
             */
            String projectType();
            /**
             * Category folder (perhaps multiple components separated by {@code /})
             * in which to place this panel or which is the name of this panel folder.
             */
            String category() default "";
            /**
             * Display name when defining a category folder.
             * Can use {@code pkg.of.Bundle#key_name} syntax.
             */
            String categoryLabel() default "";
            /**
             * Position of this panel or subfolder within its folder.
             */
            int position() default Integer.MAX_VALUE;
        }
        /**
         * Used in case multiple registrations are needed in one place.
         * @since org.netbeans.modules.projectuiapi/1 1.38
         */
        @Target({ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE})
        @Retention(RetentionPolicy.SOURCE)
        @interface Registrations {
            Registration[] value();
        }
    }
    
    /** Describes category of properties to be customized by given component
     */
    public static final class Category {
        
        private final String name;
        private final String displayName;
        private final Image icon;
        private final Category[] subcategories;
        private boolean valid;
        private String errorMessage;
        private ActionListener okListener;
        private ActionListener storeListener;
        private ActionListener closeListener;
        
        /** Private constructor. See the factory method.
         */
        private Category( String name,
                         String displayName,
                         Image icon,
                         Category[] subcategories ) {
            
            this.name = name;
            this.displayName = displayName;
            this.icon = icon;
            this.subcategories = subcategories;
            this.valid = true; // default
        }
        
        /** Factory method which creates new category description.
         * @param name Programmatic name of the category
         * @param displayName Name to be shown to the user
         * @param icon Icon for given category. Will use default icon if null.
         * @param subcategories Subcategories to be shown under given category.
         *        Category won't be expandable if null or empty array.
         * @return a new category description
         */
        public static Category create( String name,
                                       String displayName,
                                       Image icon,
                                       Category... subcategories ) {
            return new Category( name, displayName, icon, subcategories );
        }
        
        // Public methods ------------------------------------------------------
        
        /** Gets programmatic name of given category.
         * @return Programmatic name of the category
         */
        public String getName() {
            return this.name;
        }
        
        /** Gets display name of given category.
         * @return Display name of the category
         */
        public String getDisplayName() {
            return this.displayName;
        }
        
        /** Gets icon of given category.
         * @return Icon name of the category or null
         */
        public Image getIcon() {
            return this.icon;
        }
        
        /** Gets subcategories of given category.
         * @return Subcategories of the category or null
         */
        public Category[] getSubcategories() {
            return this.subcategories;
        }
        
        /**
         * Returns an error message for this category.
         * @return the error message (could be null)
         */
        public String getErrorMessage() {
            return errorMessage;
        }
        
        /**
         * Returns whether this category is valid or not. See {@link
         * ProjectCustomizer#createCustomizerDialog} for more details.
         * @return whether this category is valid or not (true by default)
         */
        public boolean isValid() {
            return valid;
        }
        
        /**
         * Set a validity of this category. See {@link
         * ProjectCustomizer#createCustomizerDialog} for more details.
         * @param valid set whether this category is valid or not
         */
        public void setValid(boolean valid) {
            if (this.valid != valid) {
                this.valid = valid;
                Utilities.getCategoryChangeSupport(this).firePropertyChange(
                        CategoryChangeSupport.VALID_PROPERTY, !valid, valid);
            }
        }
        
        /**
         * Set an errror message for this category which than may be shown in a
         * project customizer.
         *
         * @param message message for this category. To <em>reset</em> a
         *        message usually <code>null</code> or an empty string is
         *        passed. (similar to behaviour of {@link
         *        javax.swing.text.JTextComponent#setText(String)})
         */
        public void setErrorMessage(String message) {
            if (message == null) {
                message = "";
            }
            if (!message.equals(this.errorMessage)) {
                String oldMessage = this.errorMessage;
                this.errorMessage = message;
                Utilities.getCategoryChangeSupport(this).firePropertyChange(
                        CategoryChangeSupport.ERROR_MESSAGE_PROPERTY, oldMessage, message);
            }
        }
        
        /**
         * Set the action listener that will get notified when the changes in the customizer 
         * are to be applied.
         * @param okButtonListener ActionListener to notify 
         * @since org.netbeans.modules.projectuiapi/1 1.20
         */ 
        public void setOkButtonListener(ActionListener okButtonListener) {
            okListener = okButtonListener;
        }
        
        /**
         * Returns the action listener associated with this category that gets notified
         * when OK button is pressed on the customizer.
         * @return instance of ActionListener or null if not set.
         * @since org.netbeans.modules.projectuiapi/1 1.20
         */ 
        public ActionListener getOkButtonListener() {
            return okListener;
        }
        
        /**
         * Set the action listener that will get notified when the changes in the customizer 
         * are to be applied. Listener is executed after OkButtonListener outside of AWT EventQueue. 
         * Usually to be used to save modified files on disk.
         * @param listener ActionListener to notify 
         * @since org.netbeans.modules.projectuiapi/1 1.25
         */
        public void setStoreListener(ActionListener listener) {
            storeListener = listener;
        }
        
        /**
         * Returns the action listener that is executed outside of AWT EQ and is associated 
         * with this category that gets notified when OK button is pressed on the customizer.
         * @return instance of ActionListener or null if not set.
         * @since org.netbeans.modules.projectuiapi/1 1.25
         */
        public ActionListener getStoreListener() {
            return storeListener;
        }

        /**
         * Set the action listener that will get notified when the customizer is going to be closed
         * Listener is executed outside of AWT EventQueue. Usually to be used to do cleanup.
         * @param listener ActionListener to notify 
         * @since org.netbeans.modules.projectuiapi/1 1.65
         */
        public void setCloseListener(ActionListener listener) {
            closeListener = listener;
        }
        
        /**
         * Returns the action listener that is executed outside of AWT EQ and is associated 
         * with this category that gets notified when the customizer is going to be closed.
         * @return instance of ActionListener or null if not set.
         * @since org.netbeans.modules.projectuiapi/1 1.65
         */
        public ActionListener getCloseListener() {
            return closeListener;
        }
        
    }

    /*private*/ static class DelegateCategoryProvider implements CategoryComponentProvider, CompositeCategoryProvider, Lookup.Provider {

        /** @see CompositeCategoryProvider */
        private static final String SELF = "Self"; // NOI18N

        private final Lookup context;
        private final Map<ProjectCustomizer.Category,CompositeCategoryProvider> category2provider;
        private final DataFolder folder;
        private final CompositeCategoryProvider selfProvider;

        public DelegateCategoryProvider(DataFolder folder, Lookup context) {
            this(folder, context, new HashMap<ProjectCustomizer.Category,CompositeCategoryProvider>());
        }

        private DelegateCategoryProvider(DataFolder folder, Lookup context, Map<ProjectCustomizer.Category,CompositeCategoryProvider> cat2Provider) {
            this(folder, context, cat2Provider, null);
        }
        
        private DelegateCategoryProvider(DataFolder folder, Lookup context, Map<ProjectCustomizer.Category,CompositeCategoryProvider> cat2Provider, CompositeCategoryProvider sProv) {
            this.context = context;
            this.folder = folder;
            category2provider = cat2Provider;
            selfProvider = sProv;
        }

        @Override
        public JComponent create(ProjectCustomizer.Category category) {
            CompositeCategoryProvider prov = category2provider.get(category);
            assert prov != null : "Category doesn't have a provider associated.";
            return prov.createComponent(category, context);
        }

        ProjectCustomizer.Category[] getSubCategories() {
           return readCategories(folder);
        }

        /* accessible from tests */ ProjectCustomizer.Category[] readCategories(DataFolder folder) {
            List<ProjectCustomizer.Category> toRet = new ArrayList<ProjectCustomizer.Category>();
            for (DataObject dob : folder.getChildren()) {
                if (dob instanceof DataFolder) {
                    CompositeCategoryProvider sProvider = null;
                    DataObject subDobs[] = ((DataFolder) dob).getChildren();
                    for (DataObject subDob : subDobs) {
                        if (subDob.getName().equals(SELF)) {
                            InstanceCookie cookie = subDob.getLookup().lookup(InstanceCookie.class);
                            try {
                                if (cookie != null && CompositeCategoryProvider.class.isAssignableFrom(cookie.instanceClass())) {
                                    sProvider = (CompositeCategoryProvider) cookie.instanceCreate();
                                }
                            } catch (IOException x) {
                                LOG.log(Level.WARNING, "Could not load " + subDob, x);
                            } catch (ClassNotFoundException x) {
                                LOG.log(Level.WARNING, "Could not load " + subDob, x);
                            }
                        }
                    }
                    CompositeCategoryProvider prov;
                    if (sProvider != null) {
                        prov = new DelegateCategoryProvider((DataFolder) dob, context, category2provider, sProvider);
                    } else {
                        prov = new DelegateCategoryProvider((DataFolder) dob, context, category2provider);
                    }
                    ProjectCustomizer.Category cat = prov.createCategory(context);
                    toRet.add(cat);
                    category2provider.put(cat, prov);
                }
                if (!dob.getName().equals(SELF)) {
                    InstanceCookie cook = dob.getLookup().lookup(InstanceCookie.class);
                    try {
                    if (cook != null && CompositeCategoryProvider.class.isAssignableFrom(cook.instanceClass())) {
                        CompositeCategoryProvider provider = (CompositeCategoryProvider)cook.instanceCreate();
                        if (provider != null) {
                            ProjectCustomizer.Category cat = provider.createCategory(context);
                            if (cat != null) {
                                toRet.add(cat);
                                category2provider.put(cat, provider);
                                includeSubcats(cat.getSubcategories(), provider);
                            }
                        }
                    }
                    } catch (IOException x) {
                        LOG.log(Level.WARNING, "Could not load " + dob, x);
                    } catch (ClassNotFoundException x) {
                        LOG.log(Level.WARNING, "Could not load " + dob, x);
                    }
                }
            }
            return toRet.toArray(new ProjectCustomizer.Category[0]);
        }
        
        private void includeSubcats(ProjectCustomizer.Category[] cats, ProjectCustomizer.CompositeCategoryProvider provider) {
            if (cats != null) {
                for (ProjectCustomizer.Category cat : cats) {
                    category2provider.put(cat, provider);
                    includeSubcats(cat.getSubcategories(), provider);
                }
            }
        }

        /**
         * provides category for folder..
         */
        @Override
        public ProjectCustomizer.Category createCategory(Lookup context) {
            FileObject fo = folder.getPrimaryFile();
            String dn = fo.getNameExt();
            try {
                dn = fo.getFileSystem().getDecorator().annotateName(fo.getNameExt(), Collections.singleton(fo));
            } catch (FileStateInvalidException ex) {
                LOG.log(Level.WARNING, "Cannot retrieve display name for folder " + fo.getPath(), ex);
            }
            return ProjectCustomizer.Category.create(folder.getName(), dn, null, getSubCategories());
        }

        /**
         * provides component for folder category
         */
        @Override
        public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
            if (selfProvider != null) {
                return selfProvider.createComponent(category, context);
            }
            return new JPanel();
        }
        //#97998 related
        @Override
        public Lookup getLookup() {
            return context;
        }
    }

    /**
     * Create a new cell renderer for lists or combo boxes whose model
     * object type is Charset.
     * @return A renderer
     * @since 1.42
     */
    public static ListCellRenderer encodingRenderer() {
        return new EncodingRenderer();
    }

    /**
     * Create a new combo box model of all available Charsets
     * whose initial selection is a Charset with the provided name.
     * If the provided name is null or not a known character set,
     * a dummy Charset instance will be used for the selection.
     *
     * @param initialCharset The initial character encoding, e.g. "UTF-8" or
     * Charset.defaultCharset().name()
     * @return A combo box model of all available character encodings
     * @since 1.42
     */
    public static ComboBoxModel encodingModel(String initialCharset) {
        return new EncodingModel(initialCharset);
    }

    private static final class EncodingRenderer extends DefaultListCellRenderer {
        EncodingRenderer() {
            //Needed for synth?
            setName ("ComboBox.listRenderer"); //NOI18N
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean isLeadSelection) {
            if (value instanceof Charset) {
                value = ((Charset) value).displayName();
            }
            return super.getListCellRendererComponent(list, value, index,
                    isSelected, isLeadSelection);
        }
    }

    private static final class EncodingModel extends DefaultComboBoxModel {

        EncodingModel(String originalEncoding) {
            Charset defEnc = null;
            if (originalEncoding != null) {
                for (Charset c : Charset.availableCharsets().values()) {
                    if (c.name().equals(originalEncoding)) {
                        defEnc = c;
                    } else if (c.aliases().contains(originalEncoding)) { //Mobility - can have hand-entered encoding
                        defEnc = c;
                    }
                    addElement(c);
                }

                if (defEnc == null) {
                    //Create artificial Charset to keep the original value
                    //May happen when the project was set up on the platform
                    //which supports more encodings
                    try {
                        defEnc = new UnknownCharset(originalEncoding);
                        addElement(defEnc);
                    } catch (IllegalCharsetNameException e) {
                        //The source.encoding property is completely broken
                        LOG.log(Level.INFO, "IllegalCharsetName: {0}", originalEncoding);
                    }
                }
            }
            if (defEnc == null) {
                defEnc = Charset.defaultCharset();
            }
            setSelectedItem(defEnc);
        }

        private static final class UnknownCharset extends Charset {
            UnknownCharset (String name) {
                super (name, new String[0]);
            }

            @Override
            public boolean contains(Charset c) {
                return false;
            }

            @Override
            public CharsetDecoder newDecoder() {
                throw new UnsupportedOperationException();
            }

            @Override
            public CharsetEncoder newEncoder() {
                throw new UnsupportedOperationException();
            }
        }
    }
}
