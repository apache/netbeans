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

package org.netbeans.modules.versioning.diff;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.modules.versioning.util.CollectionUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.cookies.SaveCookie;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import static org.openide.NotifyDescriptor.CANCEL_OPTION;
import static org.openide.NotifyDescriptor.DEFAULT_OPTION;
import static org.openide.NotifyDescriptor.ERROR_MESSAGE;
import static org.openide.NotifyDescriptor.OK_OPTION;

/**
 *
 * @author  Marian Petras
 * @since  1.9.1
 */
public class FilesModifiedConfirmation {

    private static final Object PROTOTYPE_LIST_CELL_VALUE
                                = "Xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";    //NOI18N

    protected final JComponent mainComponent;
    protected final JList list;

    private final Listener listener = new Listener();

    private DialogDescriptor descriptor;
    protected final JButton btnSave;
    protected final JButton btnSaveAll;
    private ArrayListModel<SaveCookie> listModel;
    private Dialog dialog;


    public FilesModifiedConfirmation(SaveCookie[] saveCookies) {
        btnSaveAll = createSaveAllButton();
        btnSave    = createSaveButton();

        Mnemonics.setLocalizedText(btnSaveAll, getInitialSaveAllButtonText());
        Mnemonics.setLocalizedText(btnSave,    getInitialSaveButtonText());

        JScrollPane scrollPane
                = new JScrollPane(list = createFilesList(saveCookies));

        if (!listModel.isEmpty()) {
            list.setSelectedIndex(0);
        } else {
            updateSaveButtonState();
        }

        JComponent panel = new JPanel(new GridLayout(1, 1));
        panel.add(scrollPane);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        mainComponent = panel;
    }

    protected JButton createSaveAllButton() {
        return new JButton();
    }

    protected JButton createSaveButton() {
        return new JButton();
    }

    private JList createFilesList(SaveCookie[] saveCookies) {
        JList filesList = new JList(
                    listModel = new ArrayListModel<SaveCookie>(saveCookies));
        filesList.setVisibleRowCount(8);
        filesList.setPrototypeCellValue(PROTOTYPE_LIST_CELL_VALUE);
        filesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        filesList.addListSelectionListener(listener);
        filesList.setCellRenderer(new ListCellRenderer());

        return filesList;
    }

    protected String getInitialSaveAllButtonText() {
        return getMessage("LBL_SaveAll");                               //NOI18N
    }

    protected String getInitialSaveButtonText() {
        return getMessage("LBL_Save");                                  //NOI18N
    }

    private DialogDescriptor createDialogDescriptor() {
        DialogDescriptor desc = new DialogDescriptor(
                getMainDialogComponent(),
                getDialogTitle(),
                true,           //modal
                getDialogOptions(),
                null,           //no initial value
                DialogDescriptor.RIGHT_ALIGN,
                (HelpCtx) null,
                listener);

        desc.setAdditionalOptions(getAdditionalOptions());
        desc.setClosingOptions(getDialogClosingOptions());
        return desc;
    }

    protected Object[] getDialogOptions() {
        return new Object[] { btnSaveAll, btnSave };
    }

    protected Object[] getAdditionalOptions() {
        return new Object[] { CANCEL_OPTION };
    }

    protected Object[] getDialogClosingOptions() {
        return new Object[] { CANCEL_OPTION };
    }

    protected Object getMainDialogComponent() {
        return mainComponent;
    }

    protected String getDialogTitle() {
        return getMessage("MSG_Title_UnsavedFiles");                    //NOI18N
    }

    protected void tuneDialogDescriptor(DialogDescriptor descriptor) {}

    protected Dialog createDialog(DialogDescriptor descriptor) {
        return DialogDisplayer.getDefault().createDialog(descriptor);
    }

    public final Object displayDialog() {
        if (descriptor == null) {
            descriptor = createDialogDescriptor();
        }
        dialog = createDialog(descriptor);

        dialog.setVisible(true);
        dialog.dispose();

        return descriptor.getValue();
    }

    class Listener implements ActionListener, ListSelectionListener {

        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source == btnSaveAll) {
                pressedSaveAll();
            } else if (source == btnSave) {
                pressedSave();
            } else {
                anotherActionPerformed(source);
            }
        }

        public void valueChanged(ListSelectionEvent e) {
            assert e.getSource() == list;
            listSelectionChanged();
        }

    }

    protected void listSelectionChanged() {
        updateSaveButtonState();
    }

    private void updateSaveButtonState() {
        btnSave.setEnabled(list.getSelectedIndex() != -1);
    }

    protected void pressedSaveAll() {
        Collection<String> errMsgs = saveAll();
        if (errMsgs == null) {
            handleSaveAllSucceeded();
        } else {
            handleSaveAllFailed(errMsgs);
        }
    }

    protected void pressedSave() {
        String errMsg = save();
        if (errMsg != null) {
            handleSaveFailed(errMsg);
        }
        if (listModel.isEmpty()) {
            savedLastFile();
        }
    }

    protected Collection<String> saveAll() {
        List<String> errMsgs = null;
        for (int i = 0; i < listModel.getSize(); i++) {
            String itemErrMsg = save(i, false);
            if (itemErrMsg != null) {
                if (errMsgs == null) {
                    errMsgs = new ArrayList<String>(listModel.getSize());
                }
                errMsgs.add(itemErrMsg);
            }
        }
        listModel.removeAll();
        return errMsgs;
    }

    protected String save() {
        return save(list.getSelectedIndex(), true);
    }

    protected void handleSaveAllSucceeded() {
        closeDialog(btnSaveAll);
    }

    protected void handleSaveAllFailed(Collection<String> errMsgs) {
        JButton btnShowMoreInfo = new JButton();
        DialogDescriptor errDescr = new DialogDescriptor(
                new ExpandableMessage(
                      "MSG_ExceptionWhileSavingMoreFiles_Intro",        //NOI18N
                      errMsgs,
                      null,
                      btnShowMoreInfo),
                getMessage("MSG_Title_SavingError"),                    //NOI18N
                true,                                   //modal
                DEFAULT_OPTION,
                OK_OPTION,
                null);                                  //no button listener
        errDescr.setMessageType(ERROR_MESSAGE);
        errDescr.setOptions(new Object[] { OK_OPTION });
        errDescr.setAdditionalOptions(new Object[] { btnShowMoreInfo });
        errDescr.setClosingOptions(new Object[] { OK_OPTION });

        DialogDisplayer.getDefault().notify(errDescr);
        closeDialog(btnSaveAll);
    }

    protected void handleSaveFailed(String errMsg) {
        showSaveError(errMsg);
    }

    protected void showSaveError(String errMsg) {
        NotifyDescriptor errDialog
                = new NotifyDescriptor.Message(errMsg, ERROR_MESSAGE);
        errDialog.setTitle(getMessage("MSG_Title_SavingError"));        //NOI18N
        DialogDisplayer.getDefault().notify(errDialog);
    }

    @NbBundle.Messages({
        "# {0} - file name", "MSG_SavingFile=Saving {0}"
    })
    private String save(int index, boolean removeSavedFromList) {
        final SaveCookie saveCookie = listModel.getElementAt(index);

        final AtomicReference<String> errMsg = new AtomicReference<String>();
        BaseProgressUtils.showProgressDialogAndRun(new Runnable() {
            @Override
            public void run () {
                try {
                    saveCookie.save();
                } catch (IOException ex) {
                    String msg = Exceptions.findLocalizedMessage(ex);
                    errMsg.set(msg);
                    if (msg == null) {
                        msg = getMessage("MSG_exception_while_saving",       //NOI18N
                                            saveCookie.toString());
                        ex = Exceptions.attachLocalizedMessage(ex, msg);
                    }
                    Exceptions.printStackTrace(ex);
                }
            }
        }, Bundle.MSG_SavingFile(saveCookie.toString()));
        // only remove the object if the save succeeded
        if (errMsg.get() == null && removeSavedFromList) {
            listModel.removeItem(index);
        }
        return errMsg.get();
    }

    protected void anotherActionPerformed(Object source) {}

    protected final void closeDialog(Object value) {
        descriptor.setValue(value);
        dialog.setVisible(false);
    }

    protected void savedLastFile() {
        handleSaveAllSucceeded();
    }

    protected String getMessage(String msgKey, Object... params) {
        return getMessage(getClass(), msgKey, params);
    }

    protected static String getMessage(Class<?> clazz,
                                       String msgKey,
                                       Object... params) {
        return NbBundle.getMessage(clazz, msgKey, params);
    }


    private static class ArrayListModel<T> extends AbstractListModel {

        private T[] array;

        ArrayListModel(T[] array) {
            if (array == null) {
                throw new IllegalArgumentException("null");             //NOI18N
            }
            this.array = array;
        }

        public boolean isEmpty() {
            return array.length == 0;
        }

        public int getSize() {
            return array.length;
        }

        public T getElementAt(int index) {
            return array[index];
        }

        void removeItem(int index) {
            array = CollectionUtils.removeItem(array, index);
            fireIntervalRemoved(this, index, index);
        }

        void removeAll() {
            int size = getSize();
            if (size != 0) {
                array = CollectionUtils.shortenArray(array, 0);
                fireIntervalRemoved(this, 0, size - 1);
            }
        }

    }

    class ListCellRenderer extends DefaultListCellRenderer {

        public ListCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            super.getListCellRendererComponent(list,
                                               value,
                                               index,
                                               isSelected,
                                               cellHasFocus);
            if (isSelected){
                setBackground(UIManager.getColor("List.selectionBackground")); //NOI18N
                setForeground(UIManager.getColor("List.selectionForeground")); //NOI18N
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }

    }

}
