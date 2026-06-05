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

package org.netbeans.modules.project.uiapi;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/** Implementation of standard customizer dialog.
 *
 * @author Petr Hrebejk
 */
public class CustomizerDialog {

    /** Factory class only
     */
    private CustomizerDialog() {}

    // Option indexes
    private static final int OPTION_OK = 0;
    private static final int OPTION_CANCEL = OPTION_OK + 1;

    // Option command names
    private static final String COMMAND_OK = "OK";          // NOI18N
    private static final String COMMAND_CANCEL = "CANCEL";  // NOI18N

    // Close action
    private static final int ACTION_CLOSE = OPTION_CANCEL + 1;

    private static final String CUSTOMIZER_DIALOG_X = "CustomizerDialog.dialog.x";
    private static final String CUSTOMIZER_DIALOG_Y = "CustomizerDialog.dialog.y";
    private static final String CUSTOMIZER_DIALOG_WIDTH = "CustomizerDialog.dialog.width";
    private static final String CUSTOMIZER_DIALOG_HEIGHT = "CustomizerDialog.dialog.height";

    public static Dialog createDialog(@NonNull ActionListener okOptionListener, @NullAllowed ActionListener storeListener, final CustomizerPane innerPane,
            HelpCtx helpCtx, final ProjectCustomizer.Category[] categories, 
           //#97998 related
            ProjectCustomizer.CategoryComponentProvider componentProvider ) {

        ListeningButton okButton = new ListeningButton(
                NbBundle.getMessage(CustomizerDialog.class, "LBL_Customizer_Ok_Option"), // NOI18N
                categories);
        okButton.setEnabled(CustomizerDialog.checkValidity(categories));

        // Create options
        JButton options[] = {
            okButton,
            new JButton( NbBundle.getMessage( CustomizerDialog.class, "LBL_Customizer_Cancel_Option" ) ) , // NOI18N
        };

        // Set commands
        options[ OPTION_OK ].setActionCommand( COMMAND_OK );
        options[ OPTION_CANCEL ].setActionCommand( COMMAND_CANCEL );

        //A11Y
        options[ OPTION_OK ].getAccessibleContext().setAccessibleDescription ( NbBundle.getMessage( CustomizerDialog.class, "AD_Customizer_Ok_Option") ); // NOI18N
        options[ OPTION_CANCEL ].getAccessibleContext().setAccessibleDescription ( NbBundle.getMessage( CustomizerDialog.class, "AD_Customizer_Cancel_Option") ); // NOI18N


        // RegisterListener
        ActionListener optionsListener = new OptionListener(okOptionListener, storeListener, categories , componentProvider);
        options[ OPTION_OK ].addActionListener( optionsListener );
        options[ OPTION_CANCEL ].addActionListener( optionsListener );

        innerPane.getAccessibleContext().setAccessibleName( NbBundle.getMessage( CustomizerDialog.class, "AN_ProjectCustomizer") ); //NOI18N
        innerPane.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( CustomizerDialog.class, "AD_ProjectCustomizer") ); //NOI18N

        if ( helpCtx == null ) {
            helpCtx = HelpCtx.DEFAULT_HELP;
        }

        DialogDescriptor dialogDescriptor = new DialogDescriptor(
            innerPane,                             // innerPane
            NbBundle.getMessage( CustomizerDialog.class, "LBL_Customizer_Title" ), // NOI18N // displayName
            false,                                  // modal
            options,                                // options
            options[OPTION_OK],                     // initial value
            DialogDescriptor.BOTTOM_ALIGN,          // options align
            helpCtx,                                // helpCtx
            null );                                 // listener

        innerPane.addPropertyChangeListener( new HelpCtxChangeListener( dialogDescriptor, helpCtx ) );
        if ( innerPane instanceof HelpCtx.Provider ) {
            HelpCtx help = ((HelpCtx.Provider) innerPane).getHelpCtx();
            if (!help.equals(HelpCtx.DEFAULT_HELP)) {
                dialogDescriptor.setHelpCtx(help);
            }
        }
        dialogDescriptor.setClosingOptions( new Object[] { options[ OPTION_OK ], options[ OPTION_CANCEL ] } );

        Dialog dialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );

        Preferences prefs = NbPreferences.forModule(org.netbeans.modules.project.uiapi.CustomizerDialog.class);
        int dialogX = prefs.getInt(CUSTOMIZER_DIALOG_X, 0);
        int dialogY = prefs.getInt(CUSTOMIZER_DIALOG_Y, 0);
        int dialogWidth = prefs.getInt(CUSTOMIZER_DIALOG_WIDTH, 0);
        int dialogHeight = prefs.getInt(CUSTOMIZER_DIALOG_HEIGHT, 0);
        if ((dialogWidth != 0) && (dialogHeight != 0)) {

            GraphicsConfiguration gf = WindowManager.getDefault().getMainWindow().getGraphicsConfiguration();
            Rectangle gbounds = gf.getBounds();

            //Check bounds if saved size is bigger than size of current display, dialog should use the same display
            //as main window
            int maxWidth = gbounds.width;
            if (dialogWidth > maxWidth) {
                dialogWidth = maxWidth * 3 / 4;
            }
            int maxHeight = gbounds.height;
            if (dialogHeight > maxHeight) {
                dialogHeight = maxHeight * 3 / 4;
            }

            int minx = gbounds.x;
            int maxx = minx + gbounds.width;
            int miny = gbounds.y;
            int maxy = miny + gbounds.height;

            dialog.setBounds(dialogX, dialogY, dialogWidth, dialogHeight);

            // #187608: make sure the dialog remains in some visible area of the screen
            if (dialogX < minx || dialogX > maxx || dialogY < miny || dialogY > maxy) {
                dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
            }
        }

        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                Preferences prefs = NbPreferences.forModule(org.netbeans.modules.project.uiapi.CustomizerDialog.class);
                prefs.putInt(CUSTOMIZER_DIALOG_X, e.getWindow().getX());
                prefs.putInt(CUSTOMIZER_DIALOG_Y, e.getWindow().getY());
                prefs.putInt(CUSTOMIZER_DIALOG_WIDTH, e.getWindow().getWidth());
                prefs.putInt(CUSTOMIZER_DIALOG_HEIGHT, e.getWindow().getHeight());

                innerPane.clearPanelComponentCache();
                List<ProjectCustomizer.Category> queue = new LinkedList<ProjectCustomizer.Category>(Arrays.asList(categories));

                while (!queue.isEmpty()) {
                    ProjectCustomizer.Category category = queue.remove(0);

                    Utilities.removeCategoryChangeSupport(category);

                    ActionListener listener = category.getCloseListener();
                    if (listener != null) {
                        listener.actionPerformed(new ActionEvent(this, ACTION_CLOSE, e.paramString()));
                    }
                    
                    if (category.getSubcategories() != null) {
                        queue.addAll(Arrays.asList(category.getSubcategories()));
                    }
                }
            }
        });
        
        return dialog;

    }

    /** Returns whether all given categories are valid or not. */
    private static boolean checkValidity(ProjectCustomizer.Category[] categories) {
        for (ProjectCustomizer.Category c : categories) {
            if (!c.isValid()) {
                return false;
            }
            ProjectCustomizer.Category[] subCategories = c.getSubcategories();
            if (subCategories != null) {
                if (!checkValidity(subCategories)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Listens to the actions on the Customizer's option buttons */
    private static class OptionListener implements ActionListener {

        private @NonNull ActionListener okOptionListener;
        private @NullAllowed ActionListener storeListener;
        private ProjectCustomizer.Category[] categories;
        private Lookup.Provider prov;

        OptionListener(@NonNull ActionListener okOptionListener, @NullAllowed ActionListener storeListener, ProjectCustomizer.Category[] categs,
                ProjectCustomizer.CategoryComponentProvider componentProvider) {
            this.okOptionListener = okOptionListener;
            this.storeListener = storeListener;
            categories = categs;
            //#97998 related
            if (componentProvider instanceof Lookup.Provider) {
                prov = (Lookup.Provider)componentProvider;
            }
        }
        
        public void actionPerformed( final ActionEvent e ) {
            String command = e.getActionCommand();

            if ( COMMAND_OK.equals( command ) ) {
                // Call the OK option listener
                ProjectManager.mutex().writeAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        okOptionListener.actionPerformed( e ); // XXX maybe create new event
                        actionPerformed(e, categories);
                        return null;
                    }
                });
                
                final ProgressHandle handle = ProgressHandle.createHandle(NbBundle.getMessage(CustomizerDialog.class, "LBL_Saving_Project_data_progress"));
                JComponent component = ProgressHandleFactory.createProgressComponent(handle);
                Frame mainWindow = WindowManager.getDefault().getMainWindow();
                final JDialog dialog = new JDialog(mainWindow, 
                        NbBundle.getMessage(CustomizerDialog.class, "LBL_Saving_Project_data"), true);
                SavingProjectDataPanel panel = new SavingProjectDataPanel(component);
                
                dialog.getContentPane().add(panel);
                dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                dialog.pack();
                
                Rectangle bounds = mainWindow.getBounds();
                int middleX = bounds.x + bounds.width / 2;
                int middleY = bounds.y + bounds.height / 2;
                Dimension size = dialog.getPreferredSize();
                dialog.setBounds(middleX - size.width / 2, middleY - size.height / 2, size.width, size.height);
                
                // Call storeListeners out of AWT EQ
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ProjectManager.mutex().writeAccess(new Mutex.Action<Object>() {
                                @Override
                                public Object run() {
                                    FileUtil.runAtomicAction(new Runnable() {
                                        @Override
                                        public void run() {
                                    handle.start();
                                    if (storeListener != null) {
                                        storeListener.actionPerformed(e);
                                    }
                                    storePerformed(e, categories);
                                    // #97998 related
                                    saveModifiedProject();
                                        }
                                    });
                                    return null;
                                }
                            });
                        } finally {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.setVisible(false);
                                    dialog.dispose();
                                }
                            });
                        }
                    }
                });
                
                dialog.setVisible(true);
                
            }
        }
        
        private void actionPerformed(ActionEvent e, ProjectCustomizer.Category[] categs) {
            for (ProjectCustomizer.Category category : categs) {
                ActionListener list = category.getOkButtonListener();
                if (list != null) {
                    list.actionPerformed(e);// XXX maybe create new event
                }
                if (category.getSubcategories() != null) {
                    actionPerformed(e, category.getSubcategories());
                }
            }
        }
        
        private void storePerformed(ActionEvent e, ProjectCustomizer.Category[] categories) {
            for (ProjectCustomizer.Category category : categories) {
                ActionListener listener = category.getStoreListener();
                if (listener != null) {
                    listener.actionPerformed(e); // XXX maybe create new event
                }
                if (category.getSubcategories() != null) {
                    storePerformed(e, category.getSubcategories());
                }
            }
        }
        
        private void saveModifiedProject() {
            if (prov != null) {
                Project prj = prov.getLookup().lookup(Project.class);
                if (ProjectManager.getDefault().isModified(prj)) {
                    try {
                        ProjectManager.getDefault().saveProject(prj);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IllegalArgumentException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        
    }

    private static class HelpCtxChangeListener implements PropertyChangeListener {

        DialogDescriptor dialogDescriptor;
        HelpCtx defaultHelpCtx;

        HelpCtxChangeListener( DialogDescriptor dialogDescriptor, HelpCtx defaultHelpCtx ) {
            this.dialogDescriptor = dialogDescriptor;
            this.defaultHelpCtx = defaultHelpCtx;
        }

        public void propertyChange( PropertyChangeEvent evt ) {

            if ( CustomizerPane.HELP_CTX_PROPERTY.equals( evt.getPropertyName() ) ) {
                HelpCtx newHelp = (HelpCtx)evt.getNewValue();
                dialogDescriptor.setHelpCtx( newHelp == null  || newHelp == HelpCtx.DEFAULT_HELP  ? defaultHelpCtx : newHelp );
            }

        }

    }

    private static class ListeningButton extends JButton implements PropertyChangeListener {

        private ProjectCustomizer.Category[] categories;

        public ListeningButton(String label, ProjectCustomizer.Category[] categories) {
            super(label);
            this.categories = categories;
            for (ProjectCustomizer.Category c : categories) {
                Utilities.getCategoryChangeSupport(c).addPropertyChangeListener(this);
            }

        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == CategoryChangeSupport.VALID_PROPERTY) {
                boolean valid = (Boolean) evt.getNewValue();
                // enable only if all categories are valid
                setEnabled(valid && CustomizerDialog.checkValidity(categories));
            }
        }

    }

}
