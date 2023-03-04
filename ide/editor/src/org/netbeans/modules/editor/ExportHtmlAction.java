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
package org.netbeans.modules.editor;

import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.actions.CookieAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.nodes.Node;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.awt.HtmlBrowser;
import org.netbeans.editor.*;

import javax.swing.text.StyledDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.text.AttributedCharacterIterator;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.text.AttributeSet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.netbeans.modules.editor.lib2.view.PrintUtils;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbPreferences;

public class ExportHtmlAction extends CookieAction {

    private static final String HTML_EXT = ".html";  //NOI18N
    private static final String OPEN_HTML_HIST = "ExportHtmlAction_open_html_history"; //NOI18N
    private static final String SHOW_LINES_HIST = "ExportHtmlAction_show_lines_history"; //NOI18N
    private static final String SELECTION_HIST = "ExportHtmlAction_selection_history"; //NOI18N
    private static final String FOLDER_NAME_HIST = "ExportHtmlAction_folder_name_history"; //NOI18N
    private static final String CHARSET = "UTF-8"; //NOI18N

    private Dialog dlg;

    public ExportHtmlAction () {
    }

    protected final int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }



    protected final Class[] cookieClasses() {
        return new Class[] {EditorCookie.class};
    }

    protected final void performAction(Node[] activatedNodes) {
        EditorCookie ec = (EditorCookie) activatedNodes[0].getCookie (EditorCookie.class);
        if (ec==null) return;
        StyledDocument doc = null;
        try {
            doc = ec.openDocument();
        } catch (IOException ioe) {
        }
        if (doc instanceof BaseDocument) {
            final BaseDocument bdoc = (BaseDocument) doc;
            final JTextComponent jtc = Utilities.getLastActiveComponent();
            final Presenter p = new Presenter ();
            String folderName = (String)EditorState.get(FOLDER_NAME_HIST);
            if (folderName == null)
                folderName = System.getProperty("user.home"); //NOI18N
            p.setFileName (folderName+File.separatorChar+
                    ((DataObject)bdoc.getProperty (Document.StreamDescriptionProperty)).getPrimaryFile().getName()+HTML_EXT);
            
            MimePath mimePath = jtc == null ? MimePath.EMPTY : MimePath.parse(org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(jtc));
            Preferences prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
            
            Boolean bool = (Boolean)EditorState.get(SHOW_LINES_HIST);            
            boolean showLineNumbers = bool != null ? bool : prefs.getBoolean(SimpleValueNames.LINE_NUMBER_VISIBLE, EditorPreferencesDefaults.defaultLineNumberVisible);
            p.setShowLines (showLineNumbers);
            
            p.setSelectionActive (jtc != null && jtc.getSelectionStart()!=jtc.getSelectionEnd());

            bool = (Boolean)EditorState.get(SELECTION_HIST);
            boolean selection = (jtc != null && jtc.getSelectionStart()!=jtc.getSelectionEnd()) && (bool != null ? bool.booleanValue() : true);
            p.setSelection (selection);
            
            bool = (Boolean)EditorState.get(OPEN_HTML_HIST);
            boolean setOpen = bool != null ? bool.booleanValue() : false;
            p.setOpenHtml(setOpen);
            
            DialogDescriptor dd = new DialogDescriptor (p, NbBundle.getMessage(ExportHtmlAction.class, "CTL_ExportHtml"));
            boolean overwrite = true;
            dlg = DialogDisplayer.getDefault().createDialog (dd);            
            do{
                dlg.setVisible (true);
                overwrite = true;
                if (dd.getValue() == DialogDescriptor.OK_OPTION && (!p.isToClipboard() && new File(p.getFileName()).exists())){
                    NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage( org.netbeans.modules.editor.ExportHtmlAction.class, "MSG_FileExists", p.getFileName()),
                    NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE
                    );

                    org.openide.DialogDisplayer.getDefault().notify(descriptor);
                    if (descriptor.getValue()!=NotifyDescriptor.YES_OPTION){
                        overwrite = false;
                    }
                }
            }while(!overwrite);
            
            dlg.dispose();
            dlg = null;
            if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                if (selection != p.isSelection()) {
                    selection = p.isSelection();
                    EditorState.put(SELECTION_HIST, selection ? Boolean.TRUE : Boolean.FALSE);
                }
                final String file = p.getFileName();
                int idx = file.lastIndexOf(File.separatorChar);
                if (idx != -1)
                    EditorState.put(FOLDER_NAME_HIST, file.substring(0, idx));
                final boolean lineNumbers = p.isShowLines();
                if (showLineNumbers != lineNumbers) {
                    EditorState.put(SHOW_LINES_HIST, lineNumbers ? Boolean.TRUE : Boolean.FALSE);
                }
                final boolean open = p.isOpenHtml();
                if (setOpen != open) {
                    EditorState.put(OPEN_HTML_HIST, open ? Boolean.TRUE : Boolean.FALSE);
                }
                final boolean toClipboard = p.isToClipboard();
                final int selectionStart = selection ? jtc.getSelectionStart() : 0;
                final int selectionEnd = selection ? jtc.getSelectionEnd() : bdoc.getLength();
                RequestProcessor.getDefault().post(
                        new Runnable () {
                            public void run () {
                                try {
                                    if (jtc!=null)
                                        this.setCursor (Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                    export (bdoc, file, lineNumbers, selectionStart, selectionEnd, toClipboard);
                                    if (!toClipboard && open) {
                                        HtmlBrowser.URLDisplayer.getDefault().showURL(new File(file).toURI().toURL());
                                    }
                                } catch (MalformedURLException mue) {
                                        Exceptions.printStackTrace (mue);
                                } catch (IOException ioe) {
                                    NotifyDescriptor nd = new NotifyDescriptor.Message (
                                            NbBundle.getMessage(ExportHtmlAction.class,"ERR_IOError",
                                                    new Object[]{((DataObject)bdoc.getProperty(Document.StreamDescriptionProperty)).getPrimaryFile().getNameExt()
                                            +HTML_EXT,file}),    //NOI18N
                                            NotifyDescriptor.ERROR_MESSAGE);
                                    DialogDisplayer.getDefault().notify (nd);
                                    return;
                                }
                                finally {
                                    if (jtc != null) {
                                        this.setCursor (null);
                                    }
                                }
                            }


                            private void setCursor (final Cursor c) {
                                SwingUtilities.invokeLater (new Runnable () {
                                        public void run() {
                                            jtc.setCursor (c);
                                        }
                                    });
                            }
                        }
                );
            }
        }
        else {
            Logger.getLogger("global").log (Level.FINE,NbBundle.getMessage(ExportHtmlAction.class,"MSG_DocError"));
        }
    }

    public final String getName() {
        return NbBundle.getMessage (ExportHtmlAction.class, "CTL_ExportHtmlAction");
    }

    public final HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected @Override final boolean asynchronous() {
        return false;
    }

    private void export (final BaseDocument bdoc, final String fileName, final boolean lineNumbers,
            final int selectionStart, final int selectionEnd, final boolean toClipboard) throws IOException
    {
        MimePath mimePath = MimePath.parse((String)bdoc.getProperty(BaseDocument.MIME_TYPE_PROP));
        FontColorSettings fcs = MimeLookup.getLookup(mimePath).lookup(FontColorSettings.class);
        
        AttributeSet defaultAttribs = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
        Coloring coloring =  Coloring.fromAttributeSet(defaultAttribs);
        Color bgColor = coloring.getBackColor();
        Color fgColor = coloring.getForeColor();
        Font font = coloring.getFont();
        
        AttributeSet lineNumberAttribs = fcs.getFontColors(FontColorNames.LINE_NUMBER_COLORING);
        Coloring lineNumberColoring = Coloring.fromAttributeSet(lineNumberAttribs);
        Color lnbgColor = lineNumberColoring.getBackColor();
        Color lnfgColor = lineNumberColoring.getForeColor();
        
        FileObject fo = ((DataObject)bdoc.getProperty (Document.StreamDescriptionProperty)).getPrimaryFile();
        final HtmlPrintContainer htmlPrintContainer = new HtmlPrintContainer();
        htmlPrintContainer.begin (fo, font, fgColor, bgColor,lnfgColor,lnbgColor, mimePath, CHARSET);
        final IOException[] ioExc = new IOException[1];
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                List<AttributedCharacterIterator> lines = PrintUtils.printDocument(
                        bdoc, lineNumbers, selectionStart, selectionEnd);
                htmlPrintContainer.addLines(lines);
                String result = htmlPrintContainer.end();
                if (toClipboard) {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(result), null);
                } else {
                    PrintWriter out = null;
                    try {
                        out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), CHARSET));
                        out.print(result);
                    } catch (IOException ex) {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(
                                NbBundle.getMessage(ExportHtmlAction.class, "ERR_IOError", new Object[]{fileName}), //NOI18N
                                NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                        return;
                    } finally {
                        if (out != null) {
                            out.close();
                        }
                    }
                }
            }
        });
    }

    private static final class Presenter extends JPanel implements ActionListener {
        private static final String KEY_OPEN = "open"; //NOI18N
        private static final String KEY_LINE_NUMBERS = "lineNumbers"; //NOI18N
        private static final String KEY_TO_FILE = "toFile"; //NOI18N
        private static final String KEY_SELECTION = "selection"; //NOI18N
        private JTextField fileName;
        private JCheckBox showLineNumbers;
        private JCheckBox openHtml;
        private JCheckBox selection;
        private JButton browseButton;
        private final JRadioButton toFileButton = new JRadioButton();
        private final JRadioButton toClipboardButton = new JRadioButton();
        private final ButtonGroup group = new ButtonGroup();
        private boolean wasOpen = NbPreferences.forModule(Presenter.class).getBoolean (KEY_OPEN, true);
        private boolean programmaticDisableOpen = false;
        public Presenter () {
            this.initGUI ();
        }

        @Override
        public void actionPerformed (ActionEvent ae) {
            if (ae.getSource() == toClipboardButton || ae.getSource() == toFileButton) {
                boolean isFile = toFileButton.isSelected();
                browseButton.setEnabled (isFile);
                fileName.setEnabled (isFile);
                openHtml.setEnabled (isFile);
                programmaticDisableOpen = true;
                try {
                    if (isFile) {
                        openHtml.setSelected (wasOpen);
                    } else {
                        openHtml.setSelected(false);
                    }
                } finally {
                    programmaticDisableOpen = false;
                }
                NbPreferences.forModule(ExportHtmlAction.class).putBoolean(
                        KEY_TO_FILE, isFile);
            } else if (ae.getSource() == openHtml && !programmaticDisableOpen) {
                NbPreferences.forModule(Presenter.class).putBoolean (KEY_OPEN, openHtml.isSelected());
            } else if (ae.getSource() == selection) {
                NbPreferences.forModule(Presenter.class).putBoolean (KEY_SELECTION, selection.isSelected());
            } else if (ae.getSource() == showLineNumbers) {
                NbPreferences.forModule(Presenter.class).putBoolean (KEY_LINE_NUMBERS, showLineNumbers.isSelected());
            }
        }

        public final String getFileName () {
            return this.fileName.getText();
        }

        public final void setFileName (String name) {
            this.fileName.setText (name);
        }

        public final boolean isShowLines () {
            return this.showLineNumbers.isSelected();
        }

        public final void setShowLines (boolean value) {
            this.showLineNumbers.setSelected (value);
        }

        public final boolean isSelection () {
            return this.selection.isSelected();
        }

        public final void setSelection(boolean value) {
            this.selection.setSelected(value);
        }

        public final boolean isOpenHtml () {
            return this.openHtml.isSelected();
        }

        public final void setOpenHtml (boolean value) {
            this.openHtml.setSelected (value);
        }

        public final void setSelectionActive (boolean value) {
            this.selection.setEnabled (value);
        }

        public final boolean isToClipboard() {
            return toClipboardButton.isSelected();
        }

        private void initGUI () {
            boolean isToFile = NbPreferences.forModule(ExportHtmlAction.class).getBoolean(KEY_TO_FILE, true);
            toFileButton.setSelected(isToFile);
            toClipboardButton.setSelected (!isToFile);
            Mnemonics.setLocalizedText(toClipboardButton, NbBundle.getMessage(ExportHtmlAction.class, "LBL_PRINT_TO_CLIPBOARD")); //NOI18N
            toClipboardButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ExportHtmlAction.class, "ACSN_PRINT_TO_CLIPBOARD")); //NOI18N
            toClipboardButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExportHtmlAction.class, "ACSD_PRINT_TO_CLIPBOARD")); //NOI18N

            Mnemonics.setLocalizedText(toFileButton, NbBundle.getMessage(ExportHtmlAction.class, "LBL_PRINT_TO_FILE")); //NOI18N
            toFileButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ExportHtmlAction.class, "ACSN_PRINT_TO_FILE")); //NOI18N
            toFileButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExportHtmlAction.class, "ACSD_PRINT_TO_FILE")); //NOI18N

            group.add (toFileButton);
            group.add (toClipboardButton);
            toFileButton.addActionListener(this);
            toClipboardButton.addActionListener(this);
            this.setLayout ( new GridBagLayout ());
            getAccessibleContext().setAccessibleName(NbBundle.getMessage (ExportHtmlAction.class, "ACSN_ExportToHTML")); // NOI18N
            getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (ExportHtmlAction.class, "ACSD_ExportToHTML")); // NOI18N
            
            GridBagConstraints c = new GridBagConstraints ();
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.WEST;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new Insets (12, 6, 6, 6);
            add (toFileButton, c);

            c = new GridBagConstraints ();
            c.gridx = 1;
            c.gridy = 0;
            c.insets = new Insets (12,6,6,6);
            fileName = new JTextField ();
            fileName.getAccessibleContext().setAccessibleName(NbBundle.getMessage (ExportHtmlAction.class, "AN_OutputDir")); //NOI18N
            fileName.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (ExportHtmlAction.class, "AD_OutputDir")); //NOI18N
            fileName.setColumns (25);
            c = new GridBagConstraints ();
            c.gridx = 2;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.ipadx = 275;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets (12,6,6,6);
            c.weightx = 1.0;
            ((GridBagLayout)this.getLayout()).setConstraints (fileName, c);
            this.add (this.fileName);
            browseButton = new JButton ();
            Mnemonics.setLocalizedText(browseButton, NbBundle.getMessage(ExportHtmlAction.class,"CTL_Select")); //NOI18N
            browseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ExportHtmlAction.class,"AN_Select")); //NOI18N
            browseButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(ExportHtmlAction.class,"AD_Select")); //NOI18N
            browseButton.addActionListener (new ActionListener () {
                public void actionPerformed(ActionEvent e) {
                    selectFile ();
                }
            });
            c = new GridBagConstraints ();
            c.gridx = 3;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets (12,6,6,12);
            ((GridBagLayout)this.getLayout()).setConstraints (browseButton,c);
            this.add (browseButton);

            c = new GridBagConstraints ();
            c.gridx = 0;
            c.gridy = 1;
            c.fill = GridBagConstraints.BOTH;
            c.gridy = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.gridheight = 1;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets (12, 6, 6, 6);
            this.add (toClipboardButton, c);

            selection = new JCheckBox ();
            Mnemonics.setLocalizedText(selection, NbBundle.getMessage(ExportHtmlAction.class, "CTL_Selection"));
            selection.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ExportHtmlAction.class,"AN_Selection"));
            selection.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExportHtmlAction.class,"AD_Selection"));
            selection.setSelected(NbPreferences.forModule(ExportHtmlAction.class).getBoolean (KEY_SELECTION, true));
            selection.addActionListener(this);
            c = new GridBagConstraints ();
            c.gridx = 2;
            c.gridy = 2;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.gridheight = 1;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets (6,6,6,12);
            c.weightx = 1.0;
            ((GridBagLayout)this.getLayout()).setConstraints (this.selection,c);
            this.add (this.selection);
            showLineNumbers = new JCheckBox ();
            Mnemonics.setLocalizedText(showLineNumbers, NbBundle.getMessage(ExportHtmlAction.class,"CTL_ShowLineNumbers"));
            showLineNumbers.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ExportHtmlAction.class,"AN_ShowLineNumbers"));
            showLineNumbers.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExportHtmlAction.class,"AD_ShowLineNumbers"));
            showLineNumbers.setSelected (NbPreferences.forModule(ExportHtmlAction.class).getBoolean (KEY_LINE_NUMBERS, true));
            showLineNumbers.addActionListener(this);
            c = new GridBagConstraints();
            c.gridx = 2;
            c.gridy = 3;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.gridheight = 1;
            c.anchor = GridBagConstraints.WEST;
            c.fill   = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets (6,6,6,12);
            c.weightx = 1.0;
            ((GridBagLayout)this.getLayout()).setConstraints (this.showLineNumbers,c);
            this.add (this.showLineNumbers);
            openHtml = new JCheckBox ();
            Mnemonics.setLocalizedText(openHtml, NbBundle.getMessage(ExportHtmlAction.class,"CTL_OpenHTML"));
            openHtml.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ExportHtmlAction.class,"AN_OpenHTML"));
            openHtml.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExportHtmlAction.class,"AD_OpenHTML"));
            openHtml.setSelected (NbPreferences.forModule(ExportHtmlAction.class).getBoolean (KEY_OPEN, true));
            openHtml.addActionListener(this);
            c = new GridBagConstraints ();
            c.gridx = 2;
            c.gridy = 4;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.gridheight = 1;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets (6,6,12,12);
            c.weightx = 1.0;
            ((GridBagLayout)this.getLayout()).setConstraints (this.openHtml,c);
            this.add (this.openHtml);
            actionPerformed (new ActionEvent (toClipboardButton, ActionEvent.ACTION_PERFORMED, "")); //NOI18N
        }

        private void selectFile () {
            JFileChooser chooser = new FileChooserBuilder(Presenter.class).
                    setFileFilter(new HtmlOrDirFilter()).
                    setAccessibleDescription(NbBundle.getMessage(ExportHtmlAction.class, "ACD_Browse_Dialog")). //NOI18N
                    setTitle(NbBundle.getMessage(ExportHtmlAction.class, "CTL_Browse_Dialog_Title")).createFileChooser(); //NOI18N
            chooser.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ExportHtmlAction.class, "ACN_Browse_Dialog")); // NOI18N
            chooser.setSelectedFile (new File (this.fileName.getText()));
            if (chooser.showDialog (this, NbBundle.getMessage(ExportHtmlAction.class, "CTL_Approve_Label")) == JFileChooser.APPROVE_OPTION) { // NOI18N
                this.fileName.setText (chooser.getSelectedFile().getAbsolutePath());
            }
        }
    }

    private static final class HtmlOrDirFilter extends javax.swing.filechooser.FileFilter {
        public boolean accept(File f) {
            if (f.isFile() && f.getName().endsWith (HTML_EXT) || f.isDirectory()) {
                return true;
            }
            else
              return false;
        }

        public String getDescription() {
            return NbBundle.getMessage (ExportHtmlAction.class, "TXT_HTMLFileType"); // NOI18N
        }
    }
}
