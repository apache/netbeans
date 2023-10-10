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


package org.netbeans.modules.options;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.options.classic.OptionsAction;
import org.netbeans.modules.options.export.OptionsChooserPanel;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.Utilities;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.windows.WindowManager;


public class OptionsDisplayerImpl {
    /** Link to dialog, if its opened. */
    private static Dialog           dialog;
    /** weak link to options dialog DialogDescriptor. */
    private static WeakReference<DialogDescriptor>    descriptorRef = new WeakReference<DialogDescriptor> (null);
    private static String title = loc("CTL_Options_Dialog_Title");    
    private static Logger log = Logger.getLogger(OptionsDisplayerImpl.class.getName ());
    private FileChangeListener fcl;
    private boolean modal;
    static final LookupListener lookupListener = new LookupListenerImpl();
    /** OK button. */
    private JButton bOK;
    /** APPLY button. */
    private JButton bAPPLY;
    /** Advanced Options button. */
    private JButton bClassic;
    /** Export Options button */
    private JButton btnExport;
    /** Import Options button */
    private JButton btnImport;
    private static final RequestProcessor RP = new RequestProcessor(OptionsDisplayerImpl.class.getName(), 1, true);
    private static final int DELAY = 500;
    private boolean savingInProgress = false;
    private FileObject configFile;
    
    public OptionsDisplayerImpl (boolean modal) {
        this.modal = modal;
	fcl = new DefaultFSListener();
        try {
            // 91106 - listen to default FS changes to update Advanced Options, Export and Import buttons
            FileUtil.getConfigRoot().getFileSystem().addFileChangeListener(fcl);
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void setIsModal(boolean isModal) {
	this.modal = isModal;
    }
    
    public boolean isOpen() {
        return dialog != null;
    }

    /** Selects requested category or subcategory in opened Options dialog.
     * It is used in QuickSearchprovider.
     * @param path path of category and subcategories to be selected. Path is
     * composed from registration names divided by slash. See {OptionsDisplayer#open}.
     */
    static void selectCategory(String path) {
        DialogDescriptor descriptor = null;
        synchronized (lookupListener) {
            descriptor = descriptorRef.get();
        }
        if (descriptor != null) {
            OptionsPanel optionsPanel = (OptionsPanel) descriptor.getMessage();
            String categoryId = path.indexOf('/') == -1 ? path : path.substring(0, path.indexOf('/'));
            String subpath = path.indexOf('/') == -1 ? null : path.substring(path.indexOf('/') + 1);
            optionsPanel.initCurrentCategory(categoryId, subpath);
        }
        dialog.toFront();
    }

    public void showOptionsDialog (String categoryID, String subpath, CategoryModel categoryInstance) {
        log.fine("showOptionsDialog(" + categoryID + ", " + subpath+ ")");
        if (isOpen()) {
            // dialog already opened
            dialog.setVisible (true);
            dialog.toFront ();
            log.fine("Front Options Dialog"); //NOI18N
            return;
        }
        
        DialogDescriptor descriptor = null;
        synchronized(lookupListener) {
            descriptor = descriptorRef.get ();
        }

        OptionsPanel optionsPanel = null;
        if (descriptor == null) {
            optionsPanel = categoryID == null ? new OptionsPanel (categoryInstance) : new OptionsPanel(categoryID, categoryInstance);
            bOK = (JButton) loc(new JButton(), "CTL_OK");//NOI18N
            bOK.getAccessibleContext().setAccessibleDescription(loc("ACS_OKButton"));//NOI18N
            bAPPLY = (JButton) loc(new JButton(), "CTL_APPLY");//NOI18N
            bAPPLY.getAccessibleContext().setAccessibleDescription(loc("ACS_APPLYButton"));//NOI18N
	    bAPPLY.setEnabled(false);
            bClassic = (JButton) loc(new JButton(), "CTL_Classic");//NOI18N
            bClassic.getAccessibleContext().setAccessibleDescription(loc("ACS_ClassicButton"));//NOI18N
            btnExport = (JButton) loc(new JButton(), "CTL_Export");//NOI18N
            btnExport.getAccessibleContext().setAccessibleDescription(loc("ACS_Export"));//NOI18N
            btnImport = (JButton) loc(new JButton(), "CTL_Import");//NOI18N
            btnImport.getAccessibleContext().setAccessibleDescription(loc("ACS_Import"));//NOI18N
            updateButtons();
            boolean isMac = Utilities.isMac();
            Object[] options = new Object[3];
            options[0] = isMac ? DialogDescriptor.CANCEL_OPTION : bOK;
            options[1] = bAPPLY;
            options[2] = isMac ? bOK : DialogDescriptor.CANCEL_OPTION;
            descriptor = new DialogDescriptor(optionsPanel,title,modal,options,DialogDescriptor.OK_OPTION,DialogDescriptor.DEFAULT_ALIGN, null, null, false);
            
            // by-passing EqualFlowLayout manager in NbPresenter
            JPanel additionalOptionspanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            additionalOptionspanel.setBorder(new EmptyBorder(0, 0, 0, 0));
            additionalOptionspanel.add(bClassic);
            additionalOptionspanel.add(btnExport);
            additionalOptionspanel.add(btnImport);
            setUpButtonListeners(optionsPanel);
            
            descriptor.setAdditionalOptions(new Object[] {additionalOptionspanel});
            descriptor.setHelpCtx(optionsPanel.getHelpCtx());
            OptionsPanelListener listener = new OptionsPanelListener(descriptor, optionsPanel, bOK, bAPPLY);
	    descriptor.setClosingOptions(new Object[] { DialogDescriptor.CANCEL_OPTION, bOK });
            descriptor.setButtonListener(listener);
            optionsPanel.addPropertyChangeListener(listener);
            synchronized(lookupListener) {
                descriptorRef = new WeakReference<DialogDescriptor>(descriptor);
            }
            log.fine("Create new Options Dialog"); //NOI18N
        } else {
            optionsPanel = (OptionsPanel) descriptor.getMessage ();
	    optionsPanel.setCategoryInstance(categoryInstance);
            //TODO: 
            //just in case that switched from advanced
            optionsPanel.update();
            log.fine("Reopen Options Dialog"); //NOI18N
        }
        
        // #213022 - Trying to diagnose why the NPE occurs. For some reason
        // after the dialog is created, with DD.getDefault.createDialog(), it is nulled.
        Dialog tmpDialog = DialogDisplayer.getDefault ().createDialog (descriptor, WindowManager.getDefault().getMainWindow());
        log.fine("Options Dialog created; descriptor.title = " + descriptor.getTitle() +
                "; descriptor.message = " + descriptor.getMessage());
        optionsPanel.initCurrentCategory(categoryID, subpath);        
        tmpDialog.addWindowListener (new MyWindowListener (optionsPanel, tmpDialog));
        Point userLocation = getUserLocation(optionsPanel);
        if (userLocation != null) {
            tmpDialog.setLocation(userLocation);
            log.fine("userLocation is set to " + userLocation);
        }
        log.fine("setting Options Dialog visible");
        tmpDialog.setVisible (true);
        dialog = tmpDialog;
	setUpApplyChecker(optionsPanel);
    }

    private void setUpApplyChecker(final OptionsPanel optsPanel) {
	final RequestProcessor.Task applyChecker = RP.post(new Runnable() {
	    @Override
	    public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (!savingInProgress) {
                            bAPPLY.setEnabled(optsPanel.isChanged() && optsPanel.dataValid());
                        }
                    }
                });
	    }
	});
	applyChecker.addTaskListener(new TaskListener() {
	    @Override
	    public void taskFinished(Task task) {
		if (dialog != null) {
		    applyChecker.schedule(DELAY);
		}
	    }
	});
    }
    
    private void setUpButtonListeners(OptionsPanel optionsPanel) {
        btnExport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionsChooserPanel.showExportDialog();
            }
        });
        btnImport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionsChooserPanel.showImportDialog();
            }
        });
        final OptionsPanel finalOptionsPanel = optionsPanel;
        bClassic.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.fine("Options Dialog - Classic pressed."); //NOI18N
                Dialog d = dialog;
                dialog = null;
                if (finalOptionsPanel.isChanged()) {
                    Confirmation confirmationDescriptor = new Confirmation(
                            loc("CTL_Some_values_changed"),
                            NotifyDescriptor.YES_NO_CANCEL_OPTION,
                            NotifyDescriptor.QUESTION_MESSAGE);
                    Object result = DialogDisplayer.getDefault().
                            notify(confirmationDescriptor);
                    if (result == NotifyDescriptor.YES_OPTION) {
                        finalOptionsPanel.save();
                        d.dispose();
                    } else if (result == NotifyDescriptor.NO_OPTION) {
                        finalOptionsPanel.cancel();
                        d.dispose();
                    } else {
                        dialog = d;
                        return;
                    }
                } else {
                    d.dispose();
                    finalOptionsPanel.cancel();
                }
                try {
                    CallableSystemAction a = SystemAction.get(OptionsAction.class);
                    a.putValue("additionalActionName", loc("CTL_Modern"));
                    a.putValue("optionsDialogTitle", loc("CTL_Classic_Title"));
                    a.putValue("additionalActionListener", new OpenOptionsListener());
                    a.performAction();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    /** Set visibility of Advanced Options button (AKA classic) according to
     * existence of advanced options. */
    private void updateButtons() {
        if(bClassic != null) {
            bClassic.setVisible(advancedOptionsNotEmpty());
        }
        boolean optionsExportNotEmpty = optionsExportNotEmpty();
        if (btnExport != null) {
            btnExport.setVisible(optionsExportNotEmpty);
        }
        if (btnImport != null) {
            btnImport.setVisible(optionsExportNotEmpty);
        }
    }

    /** Returns true if some non hidden advanced options are registered
     * under UI/Services folder.
     * @return true if exists some advanced options, false otherwise
     */
    private boolean advancedOptionsNotEmpty() {
        FileObject servicesFO = doGetConfigFile("UI/Services");  //NOI18N
        if(servicesFO != null) {
            FileObject[] advancedOptions = servicesFO.getChildren();
            for (FileObject advancedOption : advancedOptions) {
                Object hidden = advancedOption.getAttribute("hidden");  //NOI18N
                if(hidden == null || !(Boolean)hidden) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Returns true if some non hidden files are registered under OptionsExport
     * folder.
     * @return true if something is registered under OptionsExport, false otherwise
     */
    private boolean optionsExportNotEmpty() {
        FileObject optionsExportFO = doGetConfigFile("OptionsExport");  //NOI18N
        if(optionsExportFO != null) {
            FileObject[] categories = optionsExportFO.getChildren();
            for (FileObject category : categories) {
                Object hiddenCategory = category.getAttribute("hidden");  //NOI18N
                if (hiddenCategory != null && (Boolean)hiddenCategory) {
                    // skip hidden category folder
                    continue;
                }
                FileObject[] items = category.getChildren();
                for (FileObject item : items) {
                    Object hiddenItem = item.getAttribute("hidden");  //NOI18N
                    if(hiddenItem == null || !(Boolean)hiddenItem) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @NbBundle.Messages({"Get_Config_File_Lengthy_Operation=Please wait while getting config file."})
    private FileObject doGetConfigFile(final String path) {
        configFile = null;
        AtomicBoolean getConfigFileCancelled = new AtomicBoolean(false);
        BaseProgressUtils.runOffEventDispatchThread(new Runnable() {
            @Override
            public void run() {
                configFile = FileUtil.getConfigFile(path);
            }
        }, Bundle.Get_Config_File_Lengthy_Operation(), getConfigFileCancelled, false, 50, 1000);
        if (getConfigFileCancelled.get()) {
            log.log(Level.FINE, "Options Dialog - Getting config file for path ''{0}'', cancelled by user.", path); //NOI18N
        }
        return configFile;
    }

    private Point getUserLocation(OptionsPanel optionsPanel) {
        Point userLocation;
        GraphicsDevice[] screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        for(GraphicsDevice device : screenDevices) { // iterate through all available displays
            userLocation = getUserLocation(device.getDefaultConfiguration(), optionsPanel);
            if(userLocation != null) { // found display that accommodated the options window last time
                return userLocation;
            }
        }
        return null;
    }
    
    private Point getUserLocation(GraphicsConfiguration gconf, OptionsPanel optionsPanel) {
        final Rectangle screenBounds = Utilities.getUsableScreenBounds(gconf);
        int x = NbPreferences.forModule(OptionsDisplayerImpl.class).getInt("OptionsX", Integer.MAX_VALUE);//NOI18N
        int y = NbPreferences.forModule(OptionsDisplayerImpl.class).getInt("OptionsY", Integer.MAX_VALUE);//NOI18N
        Dimension userSize = optionsPanel.getUserSize();
        if (x > screenBounds.x + screenBounds.getWidth() || y > screenBounds.y + screenBounds.getHeight()
                || x + userSize.width > screenBounds.x + screenBounds.getWidth() 
                || y + userSize.height > screenBounds.y + screenBounds.getHeight()
                || (x < screenBounds.x && screenBounds.x >= 0)
		|| (x > screenBounds.x && screenBounds.x < 0)
		|| (y < screenBounds.y && screenBounds.y >= 0)
		|| (y > screenBounds.y && screenBounds.y < 0)){
            return null;
        } else {
            return new Point(x, y);
        }
    }

    private static String loc (String key) {
        return NbBundle.getMessage (OptionsDisplayerImpl.class, key);
    }
    
    private static Component loc (Component c, String key) {
        if (c instanceof AbstractButton) {
            Mnemonics.setLocalizedText (
                (AbstractButton) c, 
                loc (key)
            );
        } else {
            Mnemonics.setLocalizedText (
                (JLabel) c, 
                loc (key)
            );
        }
        return c;
    }
    
    private class OptionsPanelListener implements PropertyChangeListener,
    ActionListener {
        private DialogDescriptor    descriptor;
        private OptionsPanel        optionsPanel;
        private JButton             bOK;
        private JButton             bAPPLY;
        private HelpCtx helpCtx = HelpCtx.DEFAULT_HELP;
        
        
        OptionsPanelListener (
            DialogDescriptor    descriptor, 
            OptionsPanel        optionsPanel,
            JButton             bOK,
            JButton             bAPPLY
        ) {
            this.descriptor = descriptor;
            this.optionsPanel = optionsPanel;
            this.bOK = bOK;
            this.bAPPLY = bAPPLY;
        }
        
        @NbBundle.Messages({"Loading_HelpCtx_Lengthy_Operation=Please wait while help context is being loaded."})
        @Override
        public void propertyChange (PropertyChangeEvent ev) {
            if (ev.getPropertyName ().equals ("buran" + OptionsPanelController.PROP_HELP_CTX)) {               //NOI18N
                AtomicBoolean helpCtxLoadingCancelled = new AtomicBoolean(false);
                ProgressUtils.runOffEventDispatchThread(new Runnable() {
                    @Override
                    public void run() {
                        helpCtx = optionsPanel.getHelpCtx();
                    }
                }, Bundle.Loading_HelpCtx_Lengthy_Operation(), helpCtxLoadingCancelled, false, 50, 5000);
                if(helpCtxLoadingCancelled.get()) {
                    log.fine("Options Dialog - HelpCtx loading cancelled by user."); //NOI18N
                }
                descriptor.setHelpCtx(helpCtx);
            } else if (ev.getPropertyName ().equals ("buran" + OptionsPanelController.PROP_VALID)) {                  //NOI18N            
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        bOK.setEnabled(optionsPanel.dataValid());
                        bAPPLY.setEnabled(optionsPanel.isChanged() && optionsPanel.dataValid());
                    }
                });
            }
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void actionPerformed (ActionEvent e) {
//                // listener is called twice ...
            if (e.getSource () == bOK) {
                log.fine("Options Dialog - Ok pressed."); //NOI18N
                saveOptionsOffEDT(true);
                if (isOpen()) { //WORKARROUND for some bug in NbPresenter
                    dialog.dispose();
                    dialog = null;
                }
            } else if (e.getSource () == bAPPLY) {
                log.fine("Options Dialog - Apply pressed."); //NOI18N
                saveOptionsOffEDT(false);
                bAPPLY.setEnabled(false);
            } else
            if (e.getSource () == DialogDescriptor.CANCEL_OPTION ||
                e.getSource () == DialogDescriptor.CLOSED_OPTION
            ) {
                log.fine("Options Dialog - Cancel pressed."); //NOI18N
                optionsPanel.cancel ();
                bOK.setEnabled(true);
                bAPPLY.setEnabled(false);
                if (isOpen()) { //WORKARROUND for some bug in NbPresenter
                    dialog.dispose();
                    dialog = null;
                }
            }
        }
        
        @NbBundle.Messages({"Saving_Options_Lengthy_Operation_Title=Lengthy operation in progress",
        "Saving_Options_Lengthy_Operation=Please wait while options are being saved."})
        private void saveOptionsOffEDT(final boolean okPressed) {
            savingInProgress = true;
            JPanel content = new JPanel();
            content.add(new JLabel(Bundle.Saving_Options_Lengthy_Operation()));
            ProgressUtils.runOffEventThreadWithCustomDialogContent(new Runnable() {
                @Override
                public void run() {
                    if(okPressed) {
                        optionsPanel.save();
                    } else {
                        optionsPanel.save(true);
                    }
                }
            }, Bundle.Saving_Options_Lengthy_Operation_Title(), content, 50, 5000);
            savingInProgress = false;
        }
    }
    
    private class MyWindowListener implements WindowListener {        
        private OptionsPanel optionsPanel;
        private Dialog originalDialog;

                
        MyWindowListener (OptionsPanel optionsPanel, Dialog tmpDialog) {
            this.optionsPanel = optionsPanel;
            this.originalDialog = tmpDialog;
        }
        
        @Override
        public void windowClosing (WindowEvent e) {
            if (dialog == null) {
                return;
            }
            log.fine("Options Dialog - windowClosing "); //NOI18N
            optionsPanel.cancel ();
            bOK.setEnabled(true);
            bAPPLY.setEnabled(false);
            if (this.originalDialog == dialog) {
                dialog = null;            
            }
        }

        @Override
        public void windowClosed(WindowEvent e) {
            optionsPanel.storeUserSize();
            // store location of dialog
            NbPreferences.forModule(OptionsDisplayerImpl.class).putInt("OptionsX", originalDialog.getX());//NOI18N
            NbPreferences.forModule(OptionsDisplayerImpl.class).putInt("OptionsY", originalDialog.getY());//NOI18N
	    try {
		FileUtil.getConfigRoot().getFileSystem().removeFileChangeListener(fcl);
	    } catch (FileStateInvalidException ex) {
		Exceptions.printStackTrace(ex);
	    }
            if (optionsPanel.needsReinit()) {
                synchronized (lookupListener) {
                    descriptorRef = new WeakReference<DialogDescriptor>(null);
                }
            }
            if (this.originalDialog == dialog) {
                dialog = null;            
            }
            log.fine("Options Dialog - windowClosed"); //NOI18N
        }
        @Override public void windowDeactivated (WindowEvent e) {}
        @Override public void windowOpened (WindowEvent e) {}
        @Override public void windowIconified (WindowEvent e) {}
        @Override public void windowDeiconified (WindowEvent e) {}
        @Override public void windowActivated (WindowEvent e) {}
    }
    
    class OpenOptionsListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            log.fine("Options Dialog - Back to modern."); //NOI18N
                            //OptionsDisplayerImpl.this.showOptionsDialog(null);
                            OptionsDisplayer.getDefault().open();
                        }
                    });
                }
            });
        }
    }
    
    private static class LookupListenerImpl implements LookupListener {
        public void resultChanged(LookupEvent ev) {
            synchronized (lookupListener) {
                descriptorRef = new WeakReference<DialogDescriptor>(null);
                // #156947 - close dialog when categories change
		if (dialog != null) {
		    Mutex.EVENT.readAccess(new Runnable() {
			@Override
			public void run() {
			    if (dialog != null) {
				log.log(Level.FINE, "Options Dialog - closing dialog when categories change."); //NOI18N
				dialog.setVisible(false);
				dialog = null;
			    }
			}
		    });
		}
            }
        }
        
    }

    /** 91106 - used to listen to default FS changes to update Advanced Options, Export and Import buttons. */
    private class DefaultFSListener implements FileChangeListener {

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            updateButtons();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            updateButtons();
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            updateButtons();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            updateButtons();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            updateButtons();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            updateButtons();
        }
    };
}

