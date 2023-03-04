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
package org.netbeans.modules.j2ee.persistence.editor.completion;

import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.completion.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.eclipse.persistence.jpa.jpql.tools.ContentAssistProposals;
import org.eclipse.persistence.jpa.jpql.tools.ResultQuery;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.persistence.editor.JPAEditorUtil;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek, Andrei Badea, Marek Fukala
 */
public abstract class JPACompletionItem implements CompletionItem {

    static JPACompletionItem createAttribValueItem(int substitutionOffset, String displayText) {
        return new AttribValueItem(substitutionOffset, displayText);
    }

    static JPACompletionItem createTypeItem(int substitutionOffset, TypeElement typeElement, ElementHandle<TypeElement> create, boolean deprecated, boolean b) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    static JPACompletionItem createDatabaseTableItem(int i, String tableName) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    static JPACompletionItem createCascadeStyleItem(int i, String string, String string0) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    static JPACompletionItem createPackageItem(int substitutionOffset, String displayText) {
        return new AttribValueItem(substitutionOffset, displayText);
    }

    static JPACompletionItem createClassPropertyItem(int substitutionOffset, VariableElement variableElem, ElementHandle<VariableElement> elemHandle, boolean deprecated) {
        return new ClassPropertyItem(substitutionOffset, variableElem, elemHandle, deprecated);
    }

    static JPACompletionItem createMappingFileItem(int substitutionOffset, String displayText) {
        return new MappingFileItem(substitutionOffset, displayText);
    }

    static JPACompletionItem createHbPropertyValueItem(int substitutionOffset, String displayText) {
        return new PropertyValueItem(substitutionOffset, displayText);
    }
    protected int substituteOffset = -1;

    public abstract String getItemText();

    public String getSubstitutionText() {
        return getItemText();
    }

    public int getSubstituteOffset() {
        return substituteOffset;
    }

    public boolean substituteCommonText(JTextComponent c, int offset, int len, int subLen) {
        // [PENDING] not enough info in parameters...
        // commonText
        // substituteExp
        return false;
    }

    public boolean substituteText(JTextComponent c, int offset, int len, boolean shifted) {
        BaseDocument doc = (BaseDocument) c.getDocument();
        String text = getSubstitutionText();

        if (text != null) {
            if (toAdd != null && !toAdd.equals("\n")) // NOI18N
            {
                text += toAdd;
            }
            // Update the text
            doc.atomicLock();
            try {
                String textToReplace = doc.getText(offset, len);
                if (text.equals(textToReplace)) {
                    return false;
                }

                if(!shifted) {//we are not in part of literal completion
                    //dirty hack for @Table(name=CUS|
                    if (!text.startsWith("\"")) {
                        text = quoteText(text);
                    }

                    //check if there is already an end quote
                    char ch = doc.getText(offset + len, 1).charAt(0);
                    if (ch == '"') {
                        //remove also this end quote since the inserted value is always quoted
                        len++;
                    }
                }

                doc.remove(offset, len);
                doc.insertString(offset, text, null);
            } catch (BadLocationException e) {
                // Can't update
            } finally {
                doc.atomicUnlock();
            }
            return true;

        } else {
            return false;
        }
    }

    public boolean canFilter() {
        return true;
    }

    public boolean cutomPosition() {
        return false;
    }

    public int getCutomPosition() {
        return -1;
    }

    public Component getPaintComponent(javax.swing.JList list, boolean isSelected, boolean cellHasFocus) {
        Component ret = getPaintComponent(isSelected);
        if (ret == null) {
            return null;
        }
        if (isSelected) {
            ret.setBackground(list.getSelectionBackground());
            ret.setForeground(list.getSelectionForeground());
        } else {
            ret.setBackground(list.getBackground());
            ret.setForeground(list.getForeground());
        }
        ret.getAccessibleContext().setAccessibleName(getItemText());
        ret.getAccessibleContext().setAccessibleDescription(getItemText());
        return ret;
    }

    public abstract Component getPaintComponent(boolean isSelected);

    @Override
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        Component renderComponent = getPaintComponent(false);
        return renderComponent.getPreferredSize().width;
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor,
            Color backgroundColor, int width, int height, boolean selected) {
        Component renderComponent = getPaintComponent(selected);
        renderComponent.setFont(defaultFont);
        renderComponent.setForeground(defaultColor);
        renderComponent.setBackground(backgroundColor);
        renderComponent.setBounds(0, 0, width, height);
        ((CCPaintComponent) renderComponent).paintComponent(g);
    }

    @Override
    public String toString() {
        return getItemText();
    }
    // CompletionItem implementation
    public static final String COMPLETION_SUBSTITUTE_TEXT = "completion-substitute-text"; //NOI18N
    static String toAdd;

    @Override
    public void processKeyEvent(KeyEvent evt) {
        if (evt.getID() == KeyEvent.KEY_TYPED) {
            Completion completion = Completion.get();
            switch (evt.getKeyChar()) {
                case ' ':
                    if (evt.getModifiers() == 0) {
                        completion.hideCompletion();
                        completion.hideDocumentation();
                    }
                    break;
//                case ';': //TODO, can special handling be added even if we press these buttons within a literal?
//                case ',':
//                case '(':
//                    completion.hideCompletion();
//                    completion.hideDocumentation();
//                case '.':
//                    if (defaultAction((JTextComponent) evt.getSource(), Character.toString(evt.getKeyChar()))) {
//                        evt.consume();
//                        break;
//                    }
            }
        }
    }

    protected String quoteText(String s) {
        return "\"" + s + "\"";
    }

    @Override
    public CharSequence getSortText() {
        return getItemText();
    }

    @Override
    public CharSequence getInsertPrefix() {
        return getItemText();
    }

    @Override
    public CompletionTask createDocumentationTask() {
        return null;
    }

    @Override
    public CompletionTask createToolTipTask() {
        return null;
    }

    @Override
    public boolean instantSubstitution(JTextComponent c) {
        Completion completion = Completion.get();
        completion.hideCompletion();
        completion.hideDocumentation();
        defaultAction(c);
        return true;
    }

    @Override
    public void defaultAction(JTextComponent component) {
        Completion completion = Completion.get();
        completion.hideCompletion();
        completion.hideDocumentation();
        defaultAction(component, "");
    }

    private boolean defaultAction(JTextComponent component, String addText) {
        int substOffset = substituteOffset;
        if (substOffset == -1) {
            substOffset = component.getCaret().getDot();
        }
        JPACompletionItem.toAdd = addText;
        return substituteText(component, substOffset, component.getCaret().getDot() - substOffset, false);
    }

    abstract static class DBElementItem extends JPACompletionItem {

        private String name;
        private boolean quote;
        protected static CCPaintComponent.DBElementPaintComponent paintComponent = null;

        // XXX should have an elementTypeName param
        public DBElementItem(String name, boolean quote, int substituteOffset) {
            this.name = name;
            this.quote = quote;
            this.substituteOffset = substituteOffset;
        }

        public DBElementItem(String name, boolean quote) {
            this(name, quote, -1);
        }

        protected String getName() {
            return name;
        }

        protected boolean getQuoted() {
            return quote;
        }

        @Override
        public String getItemText() {
            if (quote) {
                return quoteText(name);
            } else {
                return name;
            }
        }

        @Override
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new CCPaintComponent.DBElementPaintComponent();
            }
            paintComponent.setString(getTypeName() + ": " + name); // NOI18N
            paintComponent.setSelected(isSelected);
            return paintComponent;
        }

        public Object getAssociatedObject() {
            return this;
        }

        /**
         * Returns the element name (table, schema, etc.).
         */
        public abstract String getTypeName();
    }

    public static final class PersistenceUnitElementItem extends DBElementItem {

        protected static CCPaintComponent.PersistenceUnitElementPaintComponent paintComponent = null;

        public PersistenceUnitElementItem(String name, boolean quote, int substituteOffset) {
            super(name, quote, substituteOffset);
        }

        @Override
        public String getTypeName() {
            return "Persistence Unit";
        }

        @Override
        public int getSortPriority() {
            return 100;
        }

        @Override
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new CCPaintComponent.PersistenceUnitElementPaintComponent();
            }
            paintComponent.setContent(getName());
            paintComponent.setSelected(isSelected);
            return paintComponent;
        }
    }
    
    public static final class NamedQueryNameItem extends DBElementItem {

        protected static CCPaintComponent.NamedQueryNameElementPaintComponent paintComponent = null;
        private String entity;
        private String query;

        public NamedQueryNameItem(String nqname, String entityName, String query, boolean quote , int substituteOffset) {
            super(nqname, quote, substituteOffset);
            entity = entityName;
            this.query = query;
        }

        @Override
        public String getTypeName() {
            return "Named Query";
        }

        @Override
        protected String getName() {
            return super.getName() + " ("+entity + ")";
        }

        
        
        @Override
        public int getSortPriority() {
            return 100;
        }

        @Override
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new CCPaintComponent.NamedQueryNameElementPaintComponent();
            }
            paintComponent.setContent(getName());
            paintComponent.setSelected(isSelected);
            return paintComponent;
        }
        
        @Override
        public CompletionTask createDocumentationTask() {
            if(query != null){
            return new AsyncCompletionTask(new AsyncCompletionQuery() {
                @Override
                protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                    String docText = null;
                    try{
                        docText = NbBundle.getMessage(PUCompletionManager.class, "NAMED_QUERY_TEXT", getSubstitutionText(), entity, query);//NOI18N
                    } catch (Exception ex){
                        //just do not have doc by any reason
                    }
                    if (docText != null) {
                        CompletionDocumentation documentation = PersistenceCompletionDocumentation.getAttribValueDoc(docText);
                        resultSet.setDocumentation(documentation);
                    }
                    resultSet.finish();
                }
            });
            }
            return null;
        }
        
        @Override
         public boolean substituteText(JTextComponent c, int offset, int len, boolean shift) {
            BaseDocument doc = (BaseDocument) c.getDocument();
            String text = getSubstitutionText();

            if (text != null) {
                if (toAdd != null && !toAdd.equals("\n")) // NOI18N
                {
                    text += toAdd;
                }
                // Update the text
                doc.atomicLock();
                try {
                    String textToReplace = doc.getText(offset, len);
                    if (text.equals(textToReplace)) {
                        return false;
                    }

                    //
                    if (!text.startsWith("\"")) {
                        text = quoteText(text);
                    }

                    //check if there is already an end quote
                    char ch = doc.getText(offset + len, 1).charAt(0);
                    if (ch == '"') {
                        //remove also this end quote since the inserted value is always quoted
                        len++;
                    }
                    //check if there is already an start quote
                    ch = doc.getText(offset -1, 1).charAt(0);
                    if (ch == '"') {
                        //remove also this end quote since the inserted value is always quoted
                        len++;
                        offset--;
                    }

                    doc.remove(offset, len);
                    doc.insertString(offset, text, null);
                } catch (BadLocationException e) {
                    // Can't update
                } finally {
                    doc.atomicUnlock();
                }
                return true;

            } else {
                return false;
            }
        }
    }

    public static final class EntityPropertyElementItem extends DBElementItem {

        protected static CCPaintComponent.EntityPropertyElementPaintComponent paintComponent = null;

        public EntityPropertyElementItem(String name, boolean quote, int substituteOffset) {
            super(name, quote, substituteOffset);
        }

        @Override
        public String getTypeName() {
            return "Persistence Unit";
        }

        @Override
        public int getSortPriority() {
            return 100;
        }

        @Override
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new CCPaintComponent.EntityPropertyElementPaintComponent();
            }
            paintComponent.setContent(getName());
            paintComponent.setSelected(isSelected);
            return paintComponent;
        }
    }

    public static final class JPQLElementItem extends DBElementItem {

        protected static CCPaintComponent.EntityPropertyElementPaintComponent paintComponent = null;
        private final String initialvalue;
        private final ContentAssistProposals caProposal;
        private final int internalOffset;
        private int customOffset = 0;
        private boolean toQuote = true;

        public JPQLElementItem(String name, boolean quote, boolean toQuote, int substituteOffset, int internalOffset, String valueInitial, ContentAssistProposals buildContentAssistProposals) {
            super(name, quote, substituteOffset);
            this.initialvalue = valueInitial;
            this.caProposal = buildContentAssistProposals;
            this.internalOffset = internalOffset;
            this.toQuote = toQuote;
        }
        public JPQLElementItem(String name, boolean quote, int substituteOffset, int internalOffset, String valueInitial, ContentAssistProposals buildContentAssistProposals) {
            this(name, quote, true, substituteOffset, internalOffset, valueInitial, buildContentAssistProposals);
        }

        @Override
        public String getTypeName() {
            return "JPQL";
        }

        @Override
        public boolean canFilter() {
            return false;//all verification is internal to external library for entire jpq query
        }

        @Override
        public boolean cutomPosition() {
            return true;
        }

        @Override
        public int getCutomPosition() {
            return customOffset + (toQuote ? 1 : 0);
        }

        @Override
        public String getSubstitutionText() {
            ResultQuery buildEscapedQuery = caProposal.buildEscapedQuery(initialvalue, getName(), internalOffset, false);
            String newQ = buildEscapedQuery.getQuery();
            customOffset = buildEscapedQuery.getPosition();//TODO proper caret position
            return newQ;
        }

        @Override
        public boolean substituteText(JTextComponent c, int offset, int len, boolean shift) {
            len = initialvalue.length() + (getQuoted() ? 2 : 0);//we replace entire query with new 
            BaseDocument doc = (BaseDocument) c.getDocument();
            String text = getSubstitutionText();

            if (text != null) {
                if (toAdd != null && !toAdd.equals("\n")) // NOI18N
                {
                    text += toAdd;
                }
                // Update the text
                doc.atomicLock();
                try {
                    String textToReplace = doc.getText(offset, len);
                    if (text.equals(textToReplace)) {
                        return false;
                    }

                    //dirty hack for @Table(name=CUS|
                    if (toQuote && !text.startsWith("\"")) {
                        text = quoteText(text);
                    }

                    //check if there is already an end quote
                    char ch = doc.getText(offset + len, 1).charAt(0);
                    if (toQuote && ch == '"') {
                        //remove also this end quote since the inserted value is always quoted
                        len++;
                    }

                    doc.remove(offset, getCutomPosition() - ((text.length() - (toQuote ? 2 : 0)) - initialvalue.length()));
                    doc.insertString(offset, text.substring(0, getCutomPosition()), null);
                } catch (BadLocationException e) {
                    // Can't update
                } finally {
                    doc.atomicUnlock();
                }
                return true;

            } else {
                return false;
            }
        }

        @Override
        public int getSortPriority() {
            return 100;
        }

        @Override
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new CCPaintComponent.EntityPropertyElementPaintComponent();
            }
            paintComponent.setContent(getName());
            paintComponent.setSelected(isSelected);
            return paintComponent;
        }
    }

    public static final class CatalogElementItem extends DBElementItem {

        public CatalogElementItem(String name, boolean quote, int substituteOffset) {
            super(name, quote, substituteOffset);
        }

        @Override
        public String getTypeName() {
            return "Catalog";
        }

        @Override
        public int getSortPriority() {
            return 100;
        }
    }

    public static final class SchemaElementItem extends DBElementItem {

        public SchemaElementItem(String name, boolean quote, int substituteOffset) {
            super(name, quote, substituteOffset);
        }

        @Override
        public String getTypeName() {
            return "Schema";
        }

        @Override
        public int getSortPriority() {
            return 100;
        }
    }

    public static final class TableElementItem extends DBElementItem {

        protected static CCPaintComponent.TableElementPaintComponent paintComponent = null;

        public TableElementItem(String name, boolean quote, int substituteOffset) {
            super(name, quote, substituteOffset);
        }

        @Override
        public String getTypeName() {
            return "Table";
        }

        @Override
        public int getSortPriority() {
            return 100;
        }

        @Override
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new CCPaintComponent.TableElementPaintComponent();
            }
            paintComponent.setContent(getName());
            paintComponent.setSelected(isSelected);
            return paintComponent;
        }

        @Override
        public Object getAssociatedObject() {
            return this;
        }
    }
    public static final class IndexElementItem extends DBElementItem {

        public static final String ASC = "ASC";
        public static final String DESC = "DESC";
        public static final String[] PARAMS = {ASC, DESC};
        private final int shift;
        
        protected static CCPaintComponent.TableElementPaintComponent paintComponent = null;

        public IndexElementItem(String name, boolean quote, int substituteOffset, int shift) {
            super(name, quote, substituteOffset);
            this.shift = shift;
        }

        @Override
        public String getTypeName() {
            return "Table";
        }

        @Override
        public int getSortPriority() {
            return 100;
        }

        @Override
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new CCPaintComponent.TableElementPaintComponent();
            }
            paintComponent.setContent(getName());
            paintComponent.setSelected(isSelected);
            return paintComponent;
        }

        @Override
        public Object getAssociatedObject() {
            return this;
        }

        @Override
        public int getSubstituteOffset() {
            return super.getSubstituteOffset() + shift; 
        }   

        @Override
        public boolean substituteText(JTextComponent c, int offset, int len, boolean shift) {
            return super.substituteText(c, offset+this.shift, len-this.shift, this.shift!=0); 
        }
        
        
    }
    public static final class ColumnElementItem extends DBElementItem {

        private String tableName;
        protected static CCPaintComponent.ColumnElementPaintComponent paintComponent = null;

        public ColumnElementItem(String columnName, String tableName, boolean quote, int substituteOffset) {
            super(columnName, quote, substituteOffset);
            this.tableName = tableName;
        }

        @Override
        public String getTypeName() {
            return "Column";
        }

        @Override
        public int getSortPriority() {
            return 100;
        }

        @Override
        public String getItemText() {
            return "\"" + getName() + "\""; // NOI18N
        }

        @Override
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new CCPaintComponent.ColumnElementPaintComponent();
            }
            paintComponent.setContent(getName(), tableName);
            paintComponent.setSelected(isSelected);
            return paintComponent;
        }

        @Override
        public Object getAssociatedObject() {
            return this;
        }
    }

    public static final class NoConnectionElementItem extends JPACompletionItem {

        private static CCPaintComponent.NoConnectionItemPaintComponent paintComponent = null;
        private DatabaseConnection dbconn;

        public NoConnectionElementItem(DatabaseConnection dbconn) {
            this.dbconn = dbconn;
        }

        @Override
        public int getSortPriority() {
            return 1;
        }

        @Override
        public String getItemText() {
            return "";
        }

        @Override
        public boolean substituteText(JTextComponent c, int offset, int len, boolean shift) {
            ConnectionManager.getDefault().showConnectionDialog(dbconn);
            return false;
        }

        @Override
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new CCPaintComponent.NoConnectionItemPaintComponent();
            }
            paintComponent.setSelected(isSelected);
            return paintComponent;
        }

        public Object getAssociatedObject() {
            return this;
        }
    }
    public static final class AddConnectionElementItem extends JPACompletionItem {

        private static CCPaintComponent.AddConnectionItemPaintComponent paintComponent = null;

        @Override
        public int getSortPriority() {
            return 1000;
        }

        @Override
        public String getItemText() {
            return "";
        }
        
        @Override
        public boolean substituteText(JTextComponent c, int offset, int len, boolean shift) {
            ConnectionManager.getDefault().showAddConnectionDialog(null);
            return false;
        }

        @Override
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new CCPaintComponent.AddConnectionItemPaintComponent();
            }
            paintComponent.setSelected(isSelected);
            return paintComponent;
        }

        public Object getAssociatedObject() {
            return this;
        }
    }
    
    private abstract static class PersistenceXmlCompletionItem extends JPACompletionItem {
        /////////

        protected int substitutionOffset;

        protected PersistenceXmlCompletionItem(int substitutionOffset) {
            this.substitutionOffset = substitutionOffset;
        }

        @Override
        public void defaultAction(JTextComponent component) {
            if (component != null) {
                Completion.get().hideDocumentation();
                Completion.get().hideCompletion();
                int caretOffset = component.getSelectionEnd();
                substituteText(component, substitutionOffset, caretOffset - substitutionOffset, null);
            }
        }

        protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
            BaseDocument doc = (BaseDocument) c.getDocument();
            CharSequence prefix = getInsertPrefix();
            String text = prefix.toString();
            if (toAdd != null) {
                text += toAdd;
            }

            doc.atomicLock();
            try {
                Position position = doc.createPosition(offset);
                doc.remove(offset, len);
                doc.insertString(position.getOffset(), text, null);
            } catch (BadLocationException ble) {
                // nothing can be done to update
            } finally {
                doc.atomicUnlock();
            }
        }

        @Override
        public String getSubstitutionText() {
            return getInsertPrefix().toString();
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
        }

        @Override
        public int getPreferredWidth(Graphics g, Font defaultFont) {
            return CompletionUtilities.getPreferredWidth(getLeftHtmlText(),
                    getRightHtmlText(), g, defaultFont);
        }

        @Override
        public void render(Graphics g, Font defaultFont, Color defaultColor,
                Color backgroundColor, int width, int height, boolean selected) {
            CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(),
                    getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return null;
        }

        @Override
        public CompletionTask createToolTipTask() {
            return null;
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            defaultAction(component);
            return true;
        }

        protected String getLeftHtmlText() {
            return null;
        }

        protected String getRightHtmlText() {
            return null;
        }

        protected ImageIcon getIcon() {
            return null;
        }

        public abstract String getDisplayText();
        /////////
    }

    private static class AttribValueItem extends PersistenceXmlCompletionItem {

        private String displayText;

        public AttribValueItem(int substitutionOffset, String displayText) {
            super(substitutionOffset);
            this.displayText = displayText;
        }

        @Override
        public int getSortPriority() {
            return 100;
        }

        @Override
        public CharSequence getSortText() {
            return displayText;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return displayText;
        }

        @Override
        public String getDisplayText() {
            return displayText;
        }

        @Override
        protected String getLeftHtmlText() {
            return displayText;
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return new AsyncCompletionTask(new AsyncCompletionQuery() {

                @Override
                protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                    String docText = null;
                    try{
                        docText = NbBundle.getMessage(PUCompletionManager.class, displayText+"_DESC");//NOI18N
                    } catch (Exception ex){
                        //just do not have doc by any reason
                    }
                    if (docText != null) {
                        CompletionDocumentation documentation = PersistenceCompletionDocumentation.getAttribValueDoc(docText);
                        resultSet.setDocumentation(documentation);
                    }
                    resultSet.finish();
                }
            });
        }

        @Override
        public String getItemText() {
            return displayText;
        }

        @Override
        public Component getPaintComponent(boolean isSelected) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class ClassPropertyItem extends PersistenceXmlCompletionItem {

        private static final String FIELD_ICON = "org/netbeans/modules/editor/resources/completion/field_16.png"; //NOI18N
        private ElementHandle<VariableElement> elemHandle;
        private String displayName;

        public ClassPropertyItem(int substitutionOffset, VariableElement elem, ElementHandle<VariableElement> elemHandle,
                boolean deprecated) {
            super(substitutionOffset);
            this.elemHandle = elemHandle;
            this.displayName = elem.getSimpleName().toString();
        }

        @Override
        public int getSortPriority() {
            return 100;
        }

        @Override
        public CharSequence getSortText() {
            return displayName;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return displayName;
        }

        @Override
        public String getDisplayText() {
            return displayName;
        }

        @Override
        protected String getLeftHtmlText() {
            return displayName;
        }

        @Override
        protected ImageIcon getIcon() {

            return ImageUtilities.loadImageIcon(FIELD_ICON, false);
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return new AsyncCompletionTask(new AsyncCompletionQuery() {

                @Override
                protected void query(final CompletionResultSet resultSet, Document doc, int caretOffset) {
                    try {
                        JavaSource js = JPAEditorUtil.getJavaSource(doc);
                        if (js == null) {
                            return;
                        }

                        js.runUserActionTask( (CompilationController cc) -> {
                            cc.toPhase(JavaSource.Phase.RESOLVED);
                            Element element = elemHandle.resolve(cc);
                            if (element == null) {
                                return;
                            }
                            PersistenceCompletionDocumentation doc1 = PersistenceCompletionDocumentation.createJavaDoc(cc, element);
                            resultSet.setDocumentation(doc1);
                        }, false);
                        resultSet.finish();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }, EditorRegistry.lastFocusedComponent());
        }

        @Override
        public String getItemText() {
            return displayName;
        }

        @Override
        public Component getPaintComponent(boolean isSelected) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class MappingFileItem extends PersistenceXmlCompletionItem {

        private static final String HB_MAPPING_ICON = "org/netbeans/modules/j2ee/persistence/resources/hibernate-mapping.png"; //NOI18N
        private String displayText;

        public MappingFileItem(int substitutionOffset, String displayText) {
            super(substitutionOffset);
            this.displayText = displayText;
        }

        @Override
        public int getSortPriority() {
            return 100;
        }

        @Override
        public CharSequence getSortText() {
            return displayText;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return displayText;
        }

        @Override
        public String getDisplayText() {
            return displayText;
        }

        @Override
        protected String getLeftHtmlText() {
            return displayText;
        }

        @Override
        protected ImageIcon getIcon() {
            return ImageUtilities.loadImageIcon(HB_MAPPING_ICON, false);
        }

        @Override
        public String getItemText() {
            return displayText;
        }

        @Override
        public Component getPaintComponent(boolean isSelected) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class PropertyValueItem extends PersistenceXmlCompletionItem {

        private String displayText;

        public PropertyValueItem(int substitutionOffset, String displayText) {
            super(substitutionOffset);
            this.displayText = displayText;
        }

        @Override
        public int getSortPriority() {
            if (displayText.startsWith("--")) // NOI18N
            // The entry such as "--Enter your custom class--" should be the last 
            {
                return 101;
            } else if (displayText.equals("true")) // NOI18N
            // Want the "true" always to be the first
            {
                return 98;
            } else if (displayText.equals("false")) // NOI18N
            // Want the "false" always to be the second
            {
                return 99;
            } else // Everything else can be order alphabetically
            {
                return 100;
            }
        }

        @Override
        public CharSequence getSortText() {
            return displayText;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return displayText;
        }

        @Override
        public String getDisplayText() {
            return displayText;
        }

        @Override
        protected String getLeftHtmlText() {
            return displayText;
        }

        @Override
        public String getItemText() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Component getPaintComponent(boolean isSelected) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
