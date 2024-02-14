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

package org.netbeans.lib.editor.codetemplates.storage.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.table.DefaultTableModel;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.CodeTemplateDescription;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
import org.netbeans.lib.editor.codetemplates.storage.CodeTemplateSettingsImpl;
import org.netbeans.lib.editor.codetemplates.storage.CodeTemplateSettingsImpl.OnExpandAction;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.openide.util.NbBundle;


final class CodeTemplatesModel {

    // -J-Dorg.netbeans.lib.editor.codetemplates.storage.ui.CodeTemplatesModel.level=FINEST
    private static final Logger LOG = Logger.getLogger(CodeTemplatesModel.class.getName());

    // A mime type is something like text/x-java and a language is a localized name of
    // the programming language denoted by a mime type (e.g. Java).
    
    private final List<String> languages = new ArrayList<String>();
    private final Map<String, String> languageToMimeType = new HashMap<String, String>();
    private final Map<String,TM> languageToModel = new HashMap<String,TM>();
    private final Map<TM, String> modelToLanguage = new HashMap<TM, String>();
    
    CodeTemplatesModel () {
        
        Vector<String> columns = new Vector<String>();
        columns.add(loc("Abbreviation_Title")); //NOI18N
        columns.add(loc("Expanded_Text_Title")); //NOI18N
        columns.add(loc("Description_Title")); //NOI18N

        for (String mimeType : EditorSettings.getDefault().getAllMimeTypes()) {
            
            // Load the code templates
            MimePath mimePath = MimePath.parse(mimeType);
            Map<String, CodeTemplateDescription> abbreviationsMap = 
                CodeTemplateSettingsImpl.get(mimePath).getCodeTemplates();

            // Skip compound mime types (e.g. text/x-ant+xml), they inherit
            // code templates from their base mime type
            if (abbreviationsMap.isEmpty() && isCompoundMimeType(mimeType)) {
                continue;
            }
            
            // Add the language and its mime type to the map
            String language = EditorSettings.getDefault().getLanguageName(mimeType);
            if (language.equals (mimeType))
                continue;
            languages.add(language);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("CodeTemplatesModel: Added language \"" + language + "\" for mimeType \"" + mimeType + "\"\n"); // NOI18N
            }
            languageToMimeType.put(language, mimeType);
            
            // Load the table
            List<Vector<String>> table = new ArrayList<Vector<String>>();
            for(Map.Entry<String, CodeTemplateDescription> entry : abbreviationsMap.entrySet()) {
                String abbreviation = entry.getKey();
                CodeTemplateDescription ctd = entry.getValue();
                Vector<String> line =  new Vector<String>(3);
                line.add(abbreviation);
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("CodeTemplatesModel:     Added abbrev \"" + abbreviation + "\"\n"); // NOI18N
                }
                line.add(ctd.getParametrizedText());
                line.add(ctd.getDescription());
                table.add(line);
            }
            table.sort(new MComparator());
            
            List<String> supportedContexts = new ArrayList<>();
            for (CodeTemplateFilter.ContextBasedFactory factory : MimeLookup.getLookup(mimeType).lookupAll(CodeTemplateFilter.ContextBasedFactory.class)) {
                supportedContexts.addAll(factory.getSupportedContexts());
            }
            Collections.sort(supportedContexts);

            // Create the code templates table model for this language
            TM tableModel = new TM(mimeType, abbreviationsMap, columns, table, supportedContexts);
            
            modelToLanguage.put(tableModel, language);
            languageToModel.put(language, tableModel);
        }
        Collections.sort(languages);
        
        expander = CodeTemplateSettingsImpl.get(MimePath.EMPTY).getExpandKey();
        onExpandAction = CodeTemplateSettingsImpl.get(MimePath.EMPTY).getOnExpandAction();
    }
    
    private boolean isCompoundMimeType(String mimeType) {
        int idx = mimeType.lastIndexOf('+');
        return idx != -1 && idx < mimeType.length() - 1;
    }
    
    List<String> getLanguages () {
        return Collections.unmodifiableList(languages);
    }
    
    String findLanguage(String mimeType) {
        for(Map.Entry<String, String> entry : languageToMimeType.entrySet()) {
            String lang = entry.getKey();
            String mt = entry.getValue();
            if (mt.equals(mimeType)) {
                return lang;
            }
        }
        return null;
    }
    
    String getMimeType(String language) {
        return languageToMimeType.get(language);
    }
    
    TM getTableModel(String language) {
        return languageToModel.get(language);
    }
    
    void saveChanges () {
        // Save modified code templates
        for(Map.Entry<String, TM> entry : languageToModel.entrySet()) {
            String language = entry.getKey();
            TM tableModel = entry.getValue();
            
            if (!tableModel.isModified()) {
                continue;
            }
            
            // Get the code templates from the model
            String mimeType = languageToMimeType.get(language);
            Map<String, CodeTemplateDescription> newMap = new HashMap<String, CodeTemplateDescription>();
            for (int idx = 0; idx < tableModel.getRowCount(); idx++) {
                String abbreviation = tableModel.getAbbreviation(idx);
                CodeTemplateDescription ctd = new CodeTemplateDescription(
                    abbreviation,
                    tableModel.getDescription(idx),
                    tableModel.getText(idx),
                    new ArrayList<String>(tableModel.getContexts(idx)),
                    tableModel.getUniqueId(idx),
                    mimeType
                );
                
                newMap.put(abbreviation, ctd);
            }
            
            // Save the code templates
            MimePath mimePath = MimePath.parse(mimeType);
            CodeTemplateSettingsImpl.get(mimePath).setCodeTemplates(newMap);
        }

        // Save modified expander key
        if (expander != null) {
            CodeTemplateSettingsImpl.get(MimePath.EMPTY).setExpandKey(expander);
        }
        
        // Save on expand action
        if (onExpandAction != null) {
            CodeTemplateSettingsImpl.get(MimePath.EMPTY).setOnExpandAction(onExpandAction);
        }
    }
    
    boolean isChanged() {
        if (!CodeTemplateSettingsImpl.get(MimePath.EMPTY).getExpandKey().equals(expander)) {
            return true;
        }

        if (CodeTemplateSettingsImpl.get(MimePath.EMPTY).getOnExpandAction() != onExpandAction) {
            return true;
        }

        for(TM tableModel : languageToModel.values()) {
            if (tableModel.isModified()) {
                return true;
            }
        }
        
        return false;
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (CodeTemplatesModel.class, key);
    }
    
    KeyStroke getExpander () {
        return expander;
    }
    
    private KeyStroke expander;
    void setExpander (KeyStroke expander) {
        this.expander = expander;
    }
    
    OnExpandAction getOnExpandAction() {
        return onExpandAction;
    }
    
    private OnExpandAction onExpandAction;
    void setOnExpandAction(OnExpandAction action) {
        onExpandAction = action;
    }
    
    private static class MComparator implements Comparator<Vector<String>> {
        public int compare(Vector<String> o1, Vector<String> o2) {
            String s1 = o1.get(0);
            String s2 = o2.get(0);
            return s1.compareTo(s2);
        }
    } // End of MComparator class
    
    /* package */ static class TM extends DefaultTableModel {

        private final String mimeType;
        private final Map<String, CodeTemplateDescription> codeTemplatesMap;
        private final Map<String, Set<String>> contexts;
        private final DefaultListModel<String> supportedContexts;
        private boolean modified = false;
        
        public TM(
            String mimeType,
            Map<String, CodeTemplateDescription> codeTemplatesMap, 
            Vector<String> headers,
            List<Vector<String>> data,
            List<String> supportedContexts
        ) {
            super(new Vector<Vector>(data), headers);
            this.mimeType = mimeType;
            this.codeTemplatesMap = codeTemplatesMap;
            this.contexts = new HashMap<>(codeTemplatesMap.size());
            this.supportedContexts = new DefaultListModel<>();
            for (String context : supportedContexts) {
                this.supportedContexts.addElement(context);                
            }
        }

        public @Override boolean isCellEditable(int row, int column) {
            return false;
        }
        
        public String getAbbreviation(int row) {
            return (String) getValueAt(row, 0);
        }

        public String getDescription(int row) {
            return (String) getValueAt(row, 2);
        }

        public void setDescription(int row, String description) {
            if (compareTexts(description, getDescription(row))) {
                return;
            }
            
            setValueAt(description.isEmpty() ? null : description, row, 2);
            fireChanged();
        }

        public String getText(int row) {
            return (String) getValueAt(row, 1);
        }

        public void setText(int row, String text) {
            if (compareTexts(text, getText(row))) {
                return;
            }
            
            setValueAt(text, row, 1);
            fireChanged();
        }

        public Set<String> getContexts(int row) {
            String abbreviation = getAbbreviation(row);
            Set<String> ret = contexts.get(abbreviation);
            if (ret == null) {
                CodeTemplateDescription ctd = codeTemplatesMap.get(abbreviation);
                final boolean[] afterInit = {false};
                ret = new LinkedHashSet<String>() {
                    @Override
                    public boolean add(String e) {
                        boolean b = super.add(e);
                        if (b && afterInit[0]) {
                            TM.this.fireChanged();
                        }
                        return b;
                    }
                    @Override
                    public boolean remove(Object o) {
                        boolean b = super.remove(o);
                        if (b && afterInit[0]) {
                            TM.this.fireChanged();
                        }
                        return b;
                    }                    
                };
                if (ctd != null) {
                    ret.addAll(ctd.getContexts());
                }
                afterInit[0] = true;
                contexts.put(abbreviation, ret);
            }
            return ret;
        }
        
        public ListModel<String> getSupportedContexts() {
            return supportedContexts;
        }
        
        public String getUniqueId(int row) {
            CodeTemplateDescription ctd = codeTemplatesMap.get(getAbbreviation(row));
            return ctd == null ? null : ctd.getUniqueId();
        }
        
        public int addCodeTemplate(String abbreviation) {
            addRow(new Object [] { abbreviation, "", null }); //NOI18N
            fireChanged();
            return getRowCount() - 1;
        }
        
        public void removeCodeTemplate(int row) {
            removeRow(row);
            fireChanged();
        }
        
        public boolean isModified() {
            return modified;
        }
        
        private void fireChanged() {
            Map<String, CodeTemplateDescription> current = new HashMap<>();
            for (int idx = 0; idx < getRowCount(); idx++) {
                String abbreviation = getAbbreviation(idx);
                CodeTemplateDescription ctd = new CodeTemplateDescription(
                        abbreviation,
                        getDescription(idx),
                        getText(idx),
                        new ArrayList<String>(getContexts(idx)),
                        getUniqueId(idx),
                        mimeType
                );

                current.put(abbreviation, ctd);
            }
            MimePath mimePath = MimePath.parse(mimeType);
            Map<String, CodeTemplateDescription> saved = CodeTemplateSettingsImpl.get(mimePath).getCodeTemplates();
            modified = !current.equals(saved);
        }
        
        private static boolean compareTexts(String t1, String t2) {
            if (t1 == null || t1.length() == 0) {
                t1 = null;
            }
            if (t2 == null || t2.length() == 0) {
                t2 = null;
            }
            if (t1 != null && t2 != null) {
                return t1.equals(t2);
            } else {
                return t1 == null && t2 == null;
            }
        }
    } // End of TableModel class
}


