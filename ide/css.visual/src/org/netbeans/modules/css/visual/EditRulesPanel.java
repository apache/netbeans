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
package org.netbeans.modules.css.visual;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.model.api.Body;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.ElementFactory;
import org.netbeans.modules.css.model.api.Media;
import org.netbeans.modules.css.model.api.MediaBody;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelUtils;
import org.netbeans.modules.css.model.api.ModelVisitor;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.Selector;
import org.netbeans.modules.css.model.api.SelectorsGroup;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModelFactory;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * !!!UNUSED!!!
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "EditRulesPanel.none.item=<none>"
})
public class EditRulesPanel extends javax.swing.JPanel {

    private String selectorText;
    /**
     * Models for stylesheets and at rules comboboxes.
     */
    private DefaultComboBoxModel STYLESHEETS_MODEL, AT_RULES_MODEL, SELECTORS_MODEL;
    /**
     * Context of the create rule panel.
     */
    private FileObject context;
    /**
     * Css source {@link Model} for the selected stylesheet.
     */
    private Model selectedStyleSheetModel;
    private Collection<String> TAG_NAMES;
    private Map<FileObject, Collection<String>> findAllClassDeclarations;
    private Map<FileObject, Collection<String>> findAllIdDeclarations;

    public EditRulesPanel() {
        SELECTORS_MODEL = new DefaultComboBoxModel();
        STYLESHEETS_MODEL = new DefaultComboBoxModel();
        AT_RULES_MODEL = new DefaultComboBoxModel();

        initComponents();
        //install autocomplete
        ComboBoxAutoCompleteSupport.install(selectorCB);

        selectorCB.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Object item = e.getItem();
                if (item instanceof String) {
                    //user typed sg.
                    setSelector((String) item);
                } else {
                    //user selected sg. from drop down
                    setSelector((SelectorItem) e.getItem());
                }
            }
        });
        final ComboBoxEditor editor = selectorCB.getEditor();
        if (editor.getEditorComponent() instanceof JTextComponent) {
            JTextComponent textEditor = (JTextComponent) editor.getEditorComponent();
            Document doc = textEditor.getDocument();
            doc.addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent de) {
                    change();
                }

                @Override
                public void removeUpdate(DocumentEvent de) {
                    change();
                }

                @Override
                public void changedUpdate(DocumentEvent de) {
                    change();
                }
                private void change() {
                    setSelector(editor.getItem().toString());
                }
            });
        }
        
    }
   

    private void setSelector(String text) {
        this.selectorText = text;
        styleSheetCB.setEnabled(true);
//        addStylesheetButton.setEnabled(true);
        atRuleCB.setEnabled(true);
    }

    private void setSelector(SelectorItem selector) {
        this.selectorText = selector.getItemName();

        FileObject existsIn = selector.getFile();
        boolean exists = existsIn != null;
        if (exists) {
            STYLESHEETS_MODEL.setSelectedItem(existsIn);
        }
        styleSheetCB.setEnabled(!exists);
//        addStylesheetButton.setEnabled(!exists);
        atRuleCB.setEnabled(!exists);
        //TODO select the at rule in which the element is located

    }

    public void setContext(FileObject context) {
        this.context = context;
        updateModels();
    }

    private void updateModels() {
        //update selectors model
        updateSelectorsModel();

        //update stylesheets combobox model
        updateStyleSheetsModel();

        //create css model for the selected stylesheet
        updateCssModel(context);

        if (selectedStyleSheetModel == null) {
            //no css code to perform on
            return;
        }

        //update at rules model
        updateAtRulesModel();
    }

    private void updateStyleSheetsModel() {
        try {
            STYLESHEETS_MODEL.removeAllElements();
            Project project = FileOwnerQuery.getOwner(context);
            if (project == null) {
                return;
            }
            CssIndex index = CssIndex.create(project);
            for (FileObject file : index.getAllIndexedFiles()) {
                if ("text/css".equals(file.getMIMEType())) {
                    STYLESHEETS_MODEL.addElement(file);
                }
            }

            if (STYLESHEETS_MODEL.getIndexOf(context) >= 0) {
                //the context may be the html file itself
                STYLESHEETS_MODEL.setSelectedItem(context);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private void updateCssModel(FileObject file) {
        try {
            Source source = Source.create(file);
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    resultIterator = WebUtils.getResultIterator(resultIterator, "text/css");
                    if (resultIterator != null) {
                        CssParserResult result = (CssParserResult) resultIterator.getParserResult();
                        selectedStyleSheetModel = Model.getModel(result);
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void updateAtRulesModel() {
        AT_RULES_MODEL.removeAllElements();

        AT_RULES_MODEL.addElement(null);
        selectedStyleSheetModel.runReadTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet styleSheet) {
                ModelVisitor visitor = new ModelVisitor.Adapter() {
                    @Override
                    public void visitMedia(Media media) {
                        String displayName = selectedStyleSheetModel.getElementSource(media.getMediaQueryList()).toString();
                        AT_RULES_MODEL.addElement(new MediaItem(displayName, media));
                    }
                };
                styleSheet.accept(visitor);
            }
        });

        atRuleCB.setEnabled(AT_RULES_MODEL.getSize() > 1);
    }

    /**
     * call outside of AWT thread, it does some I/Os
     */
    public void applyChanges() {
        if (selectorText == null) {
            //no value set
            return;
        }

        //called if the dialog is confirmed
        selectedStyleSheetModel.runWriteTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet styleSheet) {

                ElementFactory factory = selectedStyleSheetModel.getElementFactory();
                Selector s = factory.createSelector(selectorText);
                SelectorsGroup sg = factory.createSelectorsGroup(s);
                Declarations ds = factory.createDeclarations();
                Rule rule = factory.createRule(sg, ds);

                Media media = getSelectedMedia();
                if (media == null) {
                    //add to the body
                    Body body = styleSheet.getBody();
                    if (body == null) {
                        //create body if empty file
                        body = factory.createBody();
                        styleSheet.setBody(body);
                    }
                    styleSheet.getBody().addRule(rule);
                } else {
                    //add to the media
                    MediaBody mediaBody = media.getMediaBody();
                    if(mediaBody == null) {
                        mediaBody = factory.createMediaBody();
                    }
                    mediaBody.addRule(rule);
                }

                try {
                    selectedStyleSheetModel.applyChanges();
                    selectTheRuleInEditorIfOpened(selectedStyleSheetModel, rule);
                } catch (IOException | BadLocationException | ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    private void selectTheRuleInEditorIfOpened(final Model omodel, final Rule orule) throws DataObjectNotFoundException, ParseException {
        FileObject file = omodel.getLookup().lookup(FileObject.class);
        DataObject dobj = DataObject.find(file);
        final EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
        //first get instance of the new model so we can resolve the element's positions
        final AtomicInteger ruleOffset = new AtomicInteger(-1);
        Source source = Source.create(file);
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                resultIterator = WebUtils.getResultIterator(resultIterator, "text/css");
                if (resultIterator != null) {
                    CssParserResult result = (CssParserResult) resultIterator.getParserResult();
                    final Model model = Model.getModel(result);
                    model.runReadTask(new Model.ModelTask() {
                        @Override
                        public void run(StyleSheet styleSheet) {
                            ModelUtils utils = new ModelUtils(model);
                            Rule match = utils.findMatchingRule(omodel, orule);
                            if (match != null) {
                                ruleOffset.set(match.getStartOffset());
                            }
                        }
                    });
                }
            }
        });
        if (ruleOffset.get() == -1) {
            return;
        }
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                JEditorPane[] openedPanes = ec.getOpenedPanes();
                if (openedPanes != null && openedPanes.length > 0) {
                    JEditorPane pane = openedPanes[0];
                    pane.setCaretPosition(ruleOffset.get());
                }
            }
        });

    }

    private Media getSelectedMedia() {
        Object selected = atRuleCB.getSelectedItem();
        if (selected == null) {
            return null;
        }
        return ((MediaItem) selected).getMedia();
    }

    private void updateSelectorsModel() {
        try {
            SELECTORS_MODEL.removeAllElements();
            //1.add classes && ids
            Project project = FileOwnerQuery.getOwner(context);
            if (project == null) {
                return;
            }

            CssIndex index = CssIndex.create(project);
            findAllClassDeclarations = index.findAllClassDeclarations();
            for (Map.Entry<FileObject, Collection<String>> entry : findAllClassDeclarations.entrySet()) {
                FileObject file = entry.getKey();
                Collection<String> classes = entry.getValue();
                for (String clz : classes) {
                    SELECTORS_MODEL.addElement(SelectorItem.createClass(clz, file));
                }
            }

            findAllIdDeclarations = index.findAllIdDeclarations();
            for (Map.Entry<FileObject, Collection<String>> entry : findAllIdDeclarations.entrySet()) {
                FileObject file = entry.getKey();
                Collection<String> ids = entry.getValue();
                for (String id : ids) {
                    SELECTORS_MODEL.addElement(SelectorItem.createId(id, file));
                }
            }

            //2.add html elements
            for (String tag : getTagNames()) {
                SELECTORS_MODEL.addElement(SelectorItem.createElement(tag));
            }

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }


    }

    private Collection<String> getTagNames() {
        if (TAG_NAMES == null) {
            TAG_NAMES = new TreeSet<>();
            HtmlModel model = HtmlModelFactory.getModel(HtmlVersion.HTML5);
            for (HtmlTag tag : model.getAllTags()) {
                TAG_NAMES.add(tag.getName());
            }
        }
        return TAG_NAMES;
    }
    private SelectorItemRenderer SELECTOR_MODEL_ITEM_RENDERER;

    private SelectorItemRenderer getSelectorModelItemRenderer() {
        if (SELECTOR_MODEL_ITEM_RENDERER == null) {
            SELECTOR_MODEL_ITEM_RENDERER = new SelectorItemRenderer();
        }
        return SELECTOR_MODEL_ITEM_RENDERER;
    }

    private ListCellRenderer createSelectorsRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    SelectorItemRenderer selectorModelItemRenderer = getSelectorModelItemRenderer();
                    selectorModelItemRenderer.setItem((SelectorItem) value, isSelected);
                    return selectorModelItemRenderer;

//                    setText(item.getDisplayName());
                }
                return c;
            }
        };
    }

    private ListCellRenderer createAtRulesRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText(Bundle.EditRulesPanel_none_item());
                } else {
                    setText(((MediaItem) value).getDisplayName());
                }
                return c;
            }
        };
    }

    private ListCellRenderer createStylesheetsRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    //empty model
                    return c;
                }
                FileObject file = (FileObject) value;
                String fileNameExt = file.getNameExt();
                setText(fileNameExt);

//                if(file.equals(context)) {
//                    StringBuilder sb = new StringBuilder();
//                    sb.append("<html><body><b>"); //NOI18N
//                    sb.append(fileNameExt);
//                    sb.append("</b></body></html>"); //NOI18N
//                    setText(sb.toString());
//                } else {
//                    setText(fileNameExt);
//                }
                return c;
            }
        };
    }

    private static class MediaItem {

        private Media media;
        private String displayName;

        public MediaItem(String displayName, Media media) {
            this.displayName = displayName;
            this.media = media;
        }

        public Media getMedia() {
            return media;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        styleSheetCB = new javax.swing.JComboBox();
        atRuleCB = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        selectorCB = new javax.swing.JComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        addStylesheetButton = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(EditRulesPanel.class, "EditRulesPanel.jButton1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(EditRulesPanel.class, "EditRulesPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(EditRulesPanel.class, "EditRulesPanel.jLabel3.text")); // NOI18N

        styleSheetCB.setModel(STYLESHEETS_MODEL);
        styleSheetCB.setRenderer(createStylesheetsRenderer());
        styleSheetCB.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                styleSheetCBItemStateChanged(evt);
            }
        });

        atRuleCB.setModel(AT_RULES_MODEL);
        atRuleCB.setRenderer(createAtRulesRenderer());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(EditRulesPanel.class, "EditRulesPanel.jLabel4.text")); // NOI18N

        selectorCB.setEditable(true);
        selectorCB.setModel(SELECTORS_MODEL);
        selectorCB.setRenderer(createSelectorsRenderer());

        addStylesheetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/css/visual/resources/plus.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(addStylesheetButton, org.openide.util.NbBundle.getMessage(EditRulesPanel.class, "EditRulesPanel.addStylesheetButton.text")); // NOI18N
        addStylesheetButton.setEnabled(false);

        jTextField1.setText(org.openide.util.NbBundle.getMessage(EditRulesPanel.class, "EditRulesPanel.jTextField1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(EditRulesPanel.class, "EditRulesPanel.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jSeparator1)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(styleSheetCB, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addStylesheetButton)
                                .addContainerGap())
                            .addComponent(jTextField1)
                            .addComponent(atRuleCB, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(selectorCB, 0, 316, Short.MAX_VALUE)))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel4)
                    .addComponent(selectorCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(styleSheetCB, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2))
                    .addComponent(addStylesheetButton))
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(atRuleCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap(40, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void styleSheetCBItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_styleSheetCBItemStateChanged
        FileObject file = (FileObject) STYLESHEETS_MODEL.getSelectedItem();
        //create css model for the selected stylesheet
        if (file != null) {
            updateCssModel(file);
            //update at rules model
            updateAtRulesModel();
        }
    }//GEN-LAST:event_styleSheetCBItemStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addStylesheetButton;
    private javax.swing.JComboBox atRuleCB;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JComboBox selectorCB;
    private javax.swing.JComboBox styleSheetCB;
    // End of variables declaration//GEN-END:variables

    private static class SelectorItem {

        private String clz, id, element;
        private FileObject existsIn;

        private static SelectorItem createClass(String name, FileObject existsIn) {
            return new SelectorItem(name, null, null, existsIn);
        }

        private static SelectorItem createId(String name, FileObject existsIn) {
            return new SelectorItem(null, name, null, existsIn);
        }

        private static SelectorItem createElement(String name) {
            return new SelectorItem(null, null, name, null);
        }

        public SelectorItem(String clz, String id, String element, FileObject existsIn) {
            this.clz = clz;
            this.id = id;
            this.element = element;
            this.existsIn = existsIn;
        }

        public FileObject getFile() {
            return existsIn;
        }

        public String getFileDisplayName() {
            return existsIn != null ? existsIn.getNameExt() : null;
        }

        public String getItemName() {
            StringBuilder sb = new StringBuilder();
            if (clz != null) {
                sb.append('.');
                sb.append(clz);
            } else if (id != null) {
                sb.append('#');
                sb.append(id);
            } else if (element != null) {
                sb.append(element);
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return getItemName().toString();
        }
    }

    private static class SelectorItemRenderer extends JPanel {

        private JLabel west, east;
        private Color bg, bgSelected, fg, fgSelected, inFile;

        public SelectorItemRenderer() {
            west = new JLabel();
            east = new JLabel();
            setLayout(new BorderLayout());
            add(west, BorderLayout.WEST);
            add(east, BorderLayout.EAST);

            fg = javax.swing.UIManager.getDefaults().getColor("ComboBox.foreground");
            bg = javax.swing.UIManager.getDefaults().getColor("ComboBox.background");
            fgSelected = javax.swing.UIManager.getDefaults().getColor("ComboBox.selectionForeground");
            bgSelected = javax.swing.UIManager.getDefaults().getColor("ComboBox.selectionBackground");
            inFile = Color.gray;
        }

        public void setItem(SelectorItem item, boolean isSelected) {
            west.setText(item.getItemName());
            east.setText(item.getFileDisplayName());

            if (isSelected) {
                west.setForeground(fgSelected);
                east.setForeground(fgSelected);
                setBackground(bgSelected);
            } else {
                west.setForeground(fg);
                east.setForeground(inFile);
                setBackground(bg);

            }
        }
    }
}
