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


package org.netbeans.modules.i18n;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;

import org.openide.cookies.EditorCookie;
import org.openide.DialogDescriptor;
import org.openide.loaders.DataObject;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.netbeans.api.project.Project;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;


/**
 * Manages performing of i18n action -&gt; i18n-zation of one source.
 *
 * @author   Peter Zavadsky
 */
public class I18nManager {

    static final Logger LOG = Logger.getLogger(I18nManager.class.getName());
    
    /** Singleton instance of I18nManager. */
    private static I18nManager manager;

    /** Support for this internatioanlize session. */
    private I18nSupport support;

    /** Weak reference to i18n panel. */
    private WeakReference<I18nPanel> i18nPanelWRef = new WeakReference<I18nPanel>(null);
    
    /** Weak reference to top component in which internationalizing will be provided. */
    private WeakReference<Dialog> dialogWRef = new WeakReference<Dialog>(null);
    
    /** Weak reference to caret in editor pane. */
    private WeakReference<Caret> caretWRef;

    /** Found hard coded string. */
    private HardCodedString hcString;
    
    private int replaceCount = 0;

    /** Private constructor. To ge instance use <code>getI18nMananger</code> method instead. */
    private I18nManager() {
    }

    
    /** Gets the only instance of I18nSupport. */
    public static synchronized I18nManager getDefault() {
        if (manager == null) {
            manager = new I18nManager();
        }
        return manager;
    }
    
    /** Get i18n support. */
    private void initSupport(DataObject sourceDataObject) throws IOException {
        I18nSupport.Factory factory = FactoryRegistry.getFactory(sourceDataObject.getClass());
        
        support = factory.create(sourceDataObject);

        if(support == null && LOG.isLoggable(Level.SEVERE)) {
                LOG.logp(Level.SEVERE, getClass().getName(),
                        "initSupport(DataObject)",
                        "I18nSupport is null for " + sourceDataObject);// NOI18N
        }
    }
    
    /** The 'heart' method called by <code>I18nAction</code>. */
    public void internationalize(final DataObject sourceDataObject) {

        // If there is i18n action working -> cancel it.
        closeDialog();

        // Initilialize support.
        try {
            initSupport(sourceDataObject);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
            return;
        }

        // initialize the component
        final EditorCookie ec = sourceDataObject.getCookie(EditorCookie.class);
        if (ec == null) {
            return;
        }

        // Add i18n panel to top component.
        getDialog(sourceDataObject);
        final I18nPanel i18nPanel = i18nPanelWRef.get();
        i18nPanel.showBundleMessage("TXT_SearchingForStrings");         //NOI18N
        i18nPanel.getCancelButton().requestFocusInWindow();
        
        final class SearchResultDisplayer implements Runnable {
            private final boolean success;
            SearchResultDisplayer(boolean success) {
                this.success = success;
            }
            public void run() {
                if (success) {
                    initCaret(ec);
                    highlightHCString();
                    fillDialogValues();                        
                    i18nPanel.getReplaceButton().requestFocusInWindow();
                } else {
                    i18nPanel.showBundleMessage("TXT_NoHardcodedString");//NOI18N
                    i18nPanel.getCancelButton().requestFocusInWindow();

                }
            }
        }

        // do the search on background
        RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    boolean found = find();
                    EventQueue.invokeLater(new SearchResultDisplayer(found));
                }
            });


        
    }

    /** Initializes caret. */
    private void initCaret(EditorCookie ec) {
        JEditorPane[] panes = ec.getOpenedPanes();
        if (panes == null) {
            NotifyDescriptor.Message message = new NotifyDescriptor.Message(
                    I18nUtil.getBundle().getString("MSG_CouldNotOpen"), //NOI18N
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(message);
            return;
        }

        // Keep only weak ref to caret, the strong one maintains editor pane itself.
        caretWRef = new WeakReference<Caret>(panes[0].getCaret());
    }
    
    /** Highlights found hasrdcoded string. */
    private void highlightHCString() {
        HardCodedString hStr = hcString;
        
        if (hStr == null) {
            return;
        }
        
        // Highlight found hard coded string.
        Caret caret = caretWRef.get();
        
        if (caret != null) {
            caret.setDot(hStr.getStartPosition().getOffset());
            caret.moveDot(hStr.getEndPosition().getOffset());
        }
    }

    /** Finds hard coded string. */
    private boolean find() {
        // Actual find on finder.
        hcString = support.getFinder().findNextHardCodedString();

        if (hcString != null) {
            return true;
        } 
        
        // not found in entire source document
        return false;
    }

    /** Fills values presented in internationalize dialog. */
    private void fillDialogValues() {
        // It has to work this way, at this time the strong reference in top component have to exist.
        I18nPanel i18nPanel = i18nPanelWRef.get();

        if(support == null) {
            if(LOG.isLoggable(Level.SEVERE)) {
                LOG.logp(Level.SEVERE, getClass().getName(),
                        "fillDialogValues()",
                        "I18nSupport is null"); // NOI18N
            }
            return;
        }

        i18nPanel.setI18nString(support.getDefaultI18nString(hcString));
        
        showDialog();
    }
    
    /** Replaces current found hard coded string and continue the search for next one. */
    private void replace() {
        I18nString i18nString = null;
        
        try {
            // To call weak without check have to be save here cause strong reference in the top component have to exist.
            i18nString = i18nPanelWRef.get().getI18nString();
        } catch (IllegalStateException e) {
            NotifyDescriptor.Message nd = new NotifyDescriptor.Message(
                    I18nUtil.getBundle().getString("EXC_BadKey"),       //NOI18N
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }

        // Try to add key to bundle.
        support.getResourceHolder().addProperty(i18nString.getKey(), i18nString.getValue(), i18nString.getComment());

        replaceCount++;
        // Provide additional changes if they are available.
        if (support.hasAdditionalCustomizer()) {
            support.performAdditionalChanges();
        }
        
        // Replace hardcoded string.
        support.getReplacer().replace(hcString, i18nString);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                skip();
            }
        });
  }
    
    /** Skips foudn hard coded string and conitnue to search for next one. */
    private void skip() {
        if (find()) {
            highlightHCString();
            fillDialogValues();
        } else {
            i18nPanelWRef.get().showBundleMessage("TXT_NoMoreStrings"); //NOI18N
            i18nPanelWRef.get().getCancelButton().requestFocusInWindow();
        }
    }

    /**
     * Appends //NOI18N to the current line
     */
    private void ignore() {
        I18nString i18nString = null;
        try {
            // To call weak without check have to be save here cause strong reference in the top component have to exist.
            i18nString = i18nPanelWRef.get().getI18nString();
        } catch (IllegalStateException e) {
            NotifyDescriptor.Message nd = new NotifyDescriptor.Message(
                    I18nUtil.getBundle().getString("EXC_BadKey"),       //NOI18N
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }
        i18nString.setKey(null);
        support.getReplacer().replace(hcString, i18nString);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                skip();
            }
        });
    }

    /** Shows info about found hard coded string. */
    private void showInfo() {
        JPanel infoPanel = support.getInfo(hcString);

        DialogDescriptor dd = new DialogDescriptor(
                infoPanel,
                I18nUtil.getBundle().getString("CTL_InfoPanelTitle"));  //NOI18N
        
        dd.setModal(true);
        dd.setOptionType(DialogDescriptor.DEFAULT_OPTION);
        dd.setOptions(new Object[] {DialogDescriptor.OK_OPTION});
        dd.setAdditionalOptions(new Object[0]);

        
        Dialog infoDialog = DialogDisplayer.getDefault().createDialog(dd);
        infoDialog.setVisible(true);
    }
    
    /** Cancels current internationalizing session and re-layout top component to original layout. */
    public void cancel() {
        if (replaceCount>0) {
            //Need to save resource
            DataObject resource = support.getResourceHolder().getResource();
            if (resource != null) {
                SaveCookie save = resource.getCookie(SaveCookie.class);
                if (save!=null) {
                    try {
                        save.save();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        replaceCount = 0;
        // No memory leaks.
        support = null;

        if(LOG.isLoggable(Level.FINEST)) {
            LOG.logp(Level.FINEST, getClass().getName(), "cancel()",
                     "Sets the I18nSupport to  null"); // NOI18N
        }
        
        closeDialog();
    }
    
    /** Gets dialog. In our case it is a top component. 
     * @param name name of top component */
    private void getDialog(DataObject sourceDataObject) {
        Project project = Util.getProjectFor(sourceDataObject);

        Dialog dialog = dialogWRef.get();
        I18nPanel i18nPanel = i18nPanelWRef.get();

        // Dialog was not created yet or garbaged already.
        if (i18nPanel == null) {
            
            // Create i18n panel.
            i18nPanel = new I18nPanel(support.getPropertyPanel(),
                                      project,
                                      sourceDataObject.getPrimaryFile());

            // Helper final.
            final I18nPanel panel = i18nPanel;
            
            // Set button listeners.
            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    final Object source = evt.getSource();
                    if (source == panel.getReplaceButton()) {
                        replace();
                    } else if (source == panel.getSkipButton()) {
                        skip();
                    } else if (source == panel.getIgnoreButton()) {
                        ignore();
                    } else if (source == panel.getInfoButton()) {
                        showInfo();
                    } else if (source == panel.getCancelButton()) {
                        cancel();
                    }
                }
            };
            
            i18nPanel.getReplaceButton().addActionListener(listener);
            i18nPanel.getSkipButton().addActionListener(listener);
            i18nPanel.getIgnoreButton().addActionListener(listener);
            i18nPanel.getInfoButton().addActionListener(listener);
            i18nPanel.getCancelButton().addActionListener(listener);
            
            // Reset weak reference.
            i18nPanelWRef = new WeakReference<I18nPanel>(i18nPanel);

        } else {
//            i18nPanel.setProject(project);
            i18nPanel.setFile(sourceDataObject.getPrimaryFile());
        }

        // Set default i18n string.
        i18nPanel.setI18nString(support.getDefaultI18nString());
        i18nPanel.setDefaultResource(sourceDataObject);


        if (dialog == null) {
            String title = Util.getString("CTL_I18nDialogTitle"); // NOI18N
            DialogDescriptor dd = new DialogDescriptor(
                    i18nPanel,
                    title,
                    false,
                    new Object[] {},
                    null,
                    DialogDescriptor.DEFAULT_ALIGN,
                    null,
                    null);
            dialog = DialogDisplayer.getDefault().createDialog(dd);
            dialog.setLocation(80, 80);
            dialogWRef = new WeakReference<Dialog>(dialog);
        }

        dialog.setVisible(true); 
    }
    
    /** Shows dialog. In our case opens top component if it is necessary and
     * sets caret visible in editor part. */
    private void showDialog() {
        // Open dialog if available
        Dialog dialog = dialogWRef.get();
        if (dialog != null) {
            dialog.setVisible(true);
        }

        // Set caret visible.
        Caret caret = caretWRef.get();
        if (caret != null) { 
            if (!caret.isVisible()) {
                caret.setVisible(true);
            }
        }
    }
    
    /** Closes dialog. In our case removes <code>I18nPanel</code> from top component
     * and 'reconstruct it' to it's original layout. */
    private void closeDialog() {
        Dialog dialog = dialogWRef.get();
        if (dialog != null) {
            dialog.setVisible(false);
        }
    }

}
