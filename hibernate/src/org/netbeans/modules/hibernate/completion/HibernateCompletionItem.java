/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.completion;

import org.netbeans.modules.hibernate.editor.HibernateEditorUtil;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.SimpleElementVisitor6;
import org.netbeans.api.editor.completion.Completion;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author Dongmei Cao
 */
public abstract class HibernateCompletionItem implements CompletionItem {

    /**
     * Creates items for completing static attribute values
     * 
     * @param substitutionOffset
     * @param displayText
     * @param docText
     * @return
     */
    public static HibernateCompletionItem createAttribValueItem(int substitutionOffset, String displayText, String docText) {
        return new AttribValueItem(substitutionOffset, displayText, docText);
    }

    /**
     * Creates items for completing package names
     * 
     * @param substitutionOffset
     * @param packageName
     * @param deprecated
     * @return
     */
    public static HibernateCompletionItem createPackageItem(int substitutionOffset, String packageName,
            boolean deprecated) {
        return new PackageItem(substitutionOffset, packageName, deprecated);
    }

    /**
     * Creates items for completing 
     * 
     * @param substitutionOffset
     * @param elem
     * @param elemHandle
     * @param deprecated
     * @param smartItem
     * @return
     */
    public static HibernateCompletionItem createTypeItem(int substitutionOffset, TypeElement elem, ElementHandle<TypeElement> elemHandle,
            boolean deprecated, boolean smartItem) {
        return new ClassItem(substitutionOffset, elem, elemHandle, deprecated, smartItem);
    }

    /**
     * Creates items for completing class properties/fields
     * 
     * @param substitutionOffset
     * @param variableElem
     * @param elemHandle
     * @param deprecated
     * @return
     */
    public static HibernateCompletionItem createClassPropertyItem(int substitutionOffset, VariableElement variableElem, ElementHandle<VariableElement> elemHandle, boolean deprecated) {
        return new ClassPropertyItem(substitutionOffset, variableElem, elemHandle, deprecated);
    }

    /**
     * Creates items for completing database table names
     * 
     * @param substitutionOffset
     * @param name
     * @return
     */
    public static HibernateCompletionItem createDatabaseTableItem(int substitutionOffset, String name) {
        return new DatabaseTableItem(substitutionOffset, name);
    }

    /**
     * Creates items for completing database table column names
     * 
     * @param substitutionOffset
     * @param name
     * @param pk
     * @return
     */
    public static HibernateCompletionItem createDatabaseColumnItem(int substitutionOffset, String name, boolean pk) {
        return new DatabaseColumnItem(substitutionOffset, name, pk);
    }

    /**
     * Creates items for completing database cascade styles
     * 
     * @param substitutionOffset
     * @param displayText
     * @param docText
     * @return
     */
    public static HibernateCompletionItem createCascadeStyleItem(int substitutionOffset, String displayText, String docText) {
        return new CascadeStyleItem(substitutionOffset, displayText, docText);
    }

    /**
     * Creates items for completing Hibernate mapping files
     * 
     * @param substitutionOffset
     * @param displayText
     * @return
     */
    public static HibernateCompletionItem createHbMappingFileItem(int substitutionOffset, String displayText) {
        return new HbMappingFileItem(substitutionOffset, displayText);
    }

    /**
     * Creates items for completing certain Hibernate properties
     * 
     * @param substitutionOffset
     * @param displayText
     * @return
     */
    public static HibernateCompletionItem createHbPropertyValueItem(int substitutionOffset, String displayText) {
        return new HbPropertyValueItem(substitutionOffset, displayText);
    }

    
    protected int substitutionOffset;

    protected HibernateCompletionItem(int substitutionOffset) {
        this.substitutionOffset = substitutionOffset;
    }

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
            doc.insertString(position.getOffset(), text.toString(), null);
        } catch (BadLocationException ble) {
        // nothing can be done to update
        } finally {
            doc.atomicUnlock();
        }
    }

    protected CharSequence getSubstitutionText() {
        return getInsertPrefix();
    }

    public void processKeyEvent(KeyEvent evt) {
    }

    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(),
                getRightHtmlText(), g, defaultFont);
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor,
            Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(),
                getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
    }

    public CompletionTask createDocumentationTask() {
        return null;
    }

    public CompletionTask createToolTipTask() {
        return null;
    }

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

    /**
     * Represents a class in the completion popup. 
     * 
     * Heavily derived from Java Editor module's JavaCompletionItem class
     * 
     */
    private static class ClassPropertyItem extends HibernateCompletionItem {

        private static final String FIELD_ICON = "org/netbeans/modules/editor/resources/completion/field_16.png"; //NOI18N
        private ElementHandle<VariableElement> elemHandle;
        private boolean deprecated;
        private String displayName;

        public ClassPropertyItem(int substitutionOffset, VariableElement elem, ElementHandle<VariableElement> elemHandle,
                boolean deprecated) {
            super(substitutionOffset);
            this.elemHandle = elemHandle;
            this.deprecated = deprecated;
            this.displayName = elem.getSimpleName().toString();
        }

        public int getSortPriority() {
            return 100;
        }

        public CharSequence getSortText() {
            return displayName;
        }

        public CharSequence getInsertPrefix() {
            return displayName;
        }
        
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
                        JavaSource js = HibernateEditorUtil.getJavaSource(doc);
                        if (js == null) {
                            return;
                        }

                        js.runUserActionTask(new Task<CompilationController>() {

                            public void run(CompilationController cc) throws Exception {
                                cc.toPhase(Phase.RESOLVED);
                                Element element = elemHandle.resolve(cc);
                                if (element == null) {
                                    return;
                                }
                                HibernateCompletionDocumentation doc = HibernateCompletionDocumentation.createJavaDoc(cc, element);
                                resultSet.setDocumentation(doc);
                            }
                        }, false);
                        resultSet.finish();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }, EditorRegistry.lastFocusedComponent());
        }
    }
    public static final String COLOR_END = "</font>"; //NOI18N
    public static final String STRIKE = "<s>"; //NOI18N
    public static final String STRIKE_END = "</s>"; //NOI18N
    public static final String BOLD = "<b>"; //NOI18N
    public static final String BOLD_END = "</b>"; //NOI18N

    /**
     * Represents a class in the completion popup. 
     * 
     * Heavily derived from Java Editor module's JavaCompletionItem class
     * 
     */
    private static class ClassItem extends HibernateCompletionItem {

        private static final String CLASS = "org/netbeans/modules/editor/resources/completion/class_16.png"; //NOI18N
        private static final String CLASS_COLOR = "<font color=#560000>"; //NOI18N
        private static final String PKG_COLOR = "<font color=#808080>"; //NOI18N
        private ElementHandle<TypeElement> elemHandle;
        private boolean deprecated;
        private String displayName;
        private String enclName;
        private String sortText;
        private String leftText;
        private boolean smartItem;

        public ClassItem(int substitutionOffset, TypeElement elem, ElementHandle<TypeElement> elemHandle,
                boolean deprecated, boolean smartItem) {
            super(substitutionOffset);
            this.elemHandle = elemHandle;
            this.deprecated = deprecated;
            this.displayName = smartItem ? elem.getSimpleName().toString() : getRelativeName(elem);
            this.enclName = getElementName(elem.getEnclosingElement(), true).toString();
            this.sortText = this.displayName + getImportanceLevel(this.enclName) + "#" + this.enclName; //NOI18N
            this.smartItem = smartItem;
        }

        private String getRelativeName(TypeElement elem) {
            StringBuilder sb = new StringBuilder();
            sb.append(elem.getSimpleName().toString());
            Element parent = elem.getEnclosingElement();
            while (parent.getKind() != ElementKind.PACKAGE) {
                sb.insert(0, parent.getSimpleName().toString() + "$"); // NOI18N
                parent = parent.getEnclosingElement();
            }

            return sb.toString();
        }

        public int getSortPriority() {
            return 100;
        }

        public CharSequence getSortText() {
            return sortText;
        }

        public CharSequence getInsertPrefix() {
            return smartItem ? "" : elemHandle.getBinaryName(); // NOI18N
        }
        
        public String getDisplayText() {
            return displayName;
        }

        @Override
        protected CharSequence getSubstitutionText() {
            return elemHandle.getBinaryName();
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(getColor());
                if (deprecated) {
                    sb.append(STRIKE);
                }
                sb.append(displayName);
                if (deprecated) {
                    sb.append(STRIKE_END);
                }
                if (smartItem && enclName != null && enclName.length() > 0) {
                    sb.append(COLOR_END);
                    sb.append(PKG_COLOR);
                    sb.append(" ("); //NOI18N
                    sb.append(enclName);
                    sb.append(")"); //NOI18N
                }
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }

        protected String getColor() {
            return CLASS_COLOR;
        }

        @Override
        protected ImageIcon getIcon() {
            return ImageUtilities.loadImageIcon(CLASS, false);
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return new AsyncCompletionTask(new AsyncCompletionQuery() {

                @Override
                protected void query(final CompletionResultSet resultSet, Document doc, int caretOffset) {
                    try {
                        JavaSource js = HibernateEditorUtil.getJavaSource(doc);
                        if (js == null) {
                            return;
                        }

                        js.runUserActionTask(new Task<CompilationController>() {

                            public void run(CompilationController cc) throws Exception {
                                cc.toPhase(Phase.RESOLVED);
                                Element element = elemHandle.resolve(cc);
                                if (element == null) {
                                    return;
                                }
                                HibernateCompletionDocumentation doc = HibernateCompletionDocumentation.createJavaDoc(cc, element);
                                resultSet.setDocumentation(doc);
                            }
                        }, false);
                        resultSet.finish();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }, EditorRegistry.lastFocusedComponent());
        }
    }

    private static class PackageItem extends HibernateCompletionItem {

        private static final String PACKAGE = "org/netbeans/modules/java/editor/resources/package.gif"; // NOI18N
        private static final String PACKAGE_COLOR = "<font color=#005600>"; //NOI18N
        private static ImageIcon icon;
        private boolean deprecated;
        private String simpleName;
        private String sortText;
        private String leftText;

        public PackageItem(int substitutionOffset, String packageFQN, boolean deprecated) {
            super(substitutionOffset);
            int idx = packageFQN.lastIndexOf('.'); // NOI18N
            this.simpleName = idx < 0 ? packageFQN : packageFQN.substring(idx + 1);
            this.deprecated = deprecated;
            this.sortText = this.simpleName + "#" + packageFQN; //NOI18N
        }

        public int getSortPriority() {
            return 100;
        }

        public CharSequence getSortText() {
            return sortText;
        }

        public CharSequence getInsertPrefix() {
            return simpleName;
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
            if (evt.getID() == KeyEvent.KEY_TYPED) {
                if (evt.getKeyChar() == '.') { // NOI18N
                    Completion.get().hideDocumentation();
                    JTextComponent component = (JTextComponent) evt.getSource();
                    int caretOffset = component.getSelectionEnd();
                    substituteText(component, substitutionOffset, caretOffset - substitutionOffset, Character.toString(evt.getKeyChar()));
                    Completion.get().showCompletion();
                    evt.consume();
                }
            }
        }
        
        public String getDisplayText() {
            return simpleName;
        }

        @Override
        protected ImageIcon getIcon() {
            if (icon == null) {
                icon = ImageUtilities.loadImageIcon(PACKAGE, false);
            }
            return icon;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(PACKAGE_COLOR);
                if (deprecated) {
                    sb.append(STRIKE);
                }
                sb.append(simpleName);
                if (deprecated) {
                    sb.append(STRIKE_END);
                }
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }
    }

    private static class AttribValueItem extends HibernateCompletionItem {

        private String displayText;
        private String docText;

        public AttribValueItem(int substitutionOffset, String displayText, String docText) {
            super(substitutionOffset);
            this.displayText = displayText;
            this.docText = docText;
        }

        public int getSortPriority() {
            return 100;
        }

        public CharSequence getSortText() {
            return displayText;
        }

        public CharSequence getInsertPrefix() {
            return displayText;
        }
        
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
                    if (docText != null) {
                        CompletionDocumentation documentation = HibernateCompletionDocumentation.getAttribValueDoc(docText);
                        resultSet.setDocumentation(documentation);
                    }
                    resultSet.finish();
                }
            });
        }
    }

    private static class DatabaseTableItem extends HibernateCompletionItem {

        private static final String TABLE_ICON = "org/netbeans/modules/hibernate/resources/completion/table.gif"; //NOI18N
        private String displayText;

        public DatabaseTableItem(int substitutionOffset, String name) {
            super(substitutionOffset);
            this.displayText = name;
        }

        public int getSortPriority() {
            return 100;
        }

        public CharSequence getSortText() {
            return displayText;
        }

        public CharSequence getInsertPrefix() {
            return displayText;
        }
        
        public String getDisplayText() {
            return displayText;
        }

        @Override
        protected String getLeftHtmlText() {
            return displayText;
        }

        @Override
        protected ImageIcon getIcon() {
            return ImageUtilities.loadImageIcon(TABLE_ICON, false);
        }
    }

    private static class DatabaseColumnItem extends HibernateCompletionItem {

        private static final String COLUMN_ICON = "org/netbeans/modules/hibernate/resources/completion/column.gif"; //NOI18N
        private static final String PK_COLUMN_ICON = "org/netbeans/modules/hibernate/resources/completion/columnPrimary.gif"; //NOI18N
        private String displayText;
        private boolean pk;

        public DatabaseColumnItem(int substitutionOffset, String columnName, boolean pk) {
            super(substitutionOffset);
            this.displayText = columnName;
            this.pk = pk;
        }

        public int getSortPriority() {
            if (pk) {
                return 1;
            } else {
                return 5;
            }
        }

        public CharSequence getSortText() {
            return displayText;
        }

        public CharSequence getInsertPrefix() {
            return displayText;
        }
        
        public String getDisplayText() {
            return displayText;
        }

        @Override
        protected String getLeftHtmlText() {
            return displayText;
        }

        @Override
        protected ImageIcon getIcon() {
            if (pk) {
                return ImageUtilities.loadImageIcon(PK_COLUMN_ICON, false);
            } else {
                return ImageUtilities.loadImageIcon(COLUMN_ICON, false);
            }
        }
    }

    private static class CascadeStyleItem extends HibernateCompletionItem {

        private String displayText;
        private String docText;

        public CascadeStyleItem(int substitutionOffset, String displayText, String docText) {
            super(substitutionOffset);
            this.displayText = displayText;
            this.docText = docText;
        }

        public int getSortPriority() {
            return 100;
        }

        public CharSequence getSortText() {
            return displayText;
        }

        public CharSequence getInsertPrefix() {
            return displayText;
        }
        
        public String getDisplayText() {
            return displayText;
        }

        @Override
        protected String getLeftHtmlText() {
            return displayText;
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
            if (evt.getID() == KeyEvent.KEY_TYPED) {
                if (evt.getKeyChar() == ',') { // NOI18N
                    Completion.get().hideDocumentation();
                    JTextComponent component = (JTextComponent) evt.getSource();
                    int caretOffset = component.getSelectionEnd();
                    substituteText(component, substitutionOffset, caretOffset - substitutionOffset, Character.toString(evt.getKeyChar()));
                    Completion.get().showCompletion();
                    evt.consume();
                }
            }
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return new AsyncCompletionTask(new AsyncCompletionQuery() {

                @Override
                protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                    if (docText != null) {
                        CompletionDocumentation documentation = HibernateCompletionDocumentation.getAttribValueDoc(docText);
                        resultSet.setDocumentation(documentation);
                    }
                    resultSet.finish();
                }
            });
        }
    }

    private static class HbMappingFileItem extends HibernateCompletionItem {

        private static final String HB_MAPPING_ICON = "org/netbeans/modules/hibernate/resources/hibernate-mapping.png"; //NOI18N
        private String displayText;

        public HbMappingFileItem(int substitutionOffset, String displayText) {
            super(substitutionOffset);
            this.displayText = displayText;
        }

        public int getSortPriority() {
            return 100;
        }

        public CharSequence getSortText() {
            return displayText;
        }

        public CharSequence getInsertPrefix() {
            return displayText;
        }
        
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
    }

    private static class HbPropertyValueItem extends HibernateCompletionItem {

        private String displayText;

        public HbPropertyValueItem(int substitutionOffset, String displayText) {
            super(substitutionOffset);
            this.displayText = displayText;
        }

        public int getSortPriority() {
            if(displayText.startsWith("--")) // NOI18N
                // The entry such as "--Enter your custom class--" should be the last 
                return 101;
            else if(displayText.equals("true")) // NOI18N
                // Want the "true" always to be the first
                return 98;
            else if(displayText.equals("false")) // NOI18N
                // Want the "false" always to be the second
                return 99;
            else
                // Everything else can be order alphabetically
                return 100;
        }

        public CharSequence getSortText() {
            return displayText;
        }

        public CharSequence getInsertPrefix() {
            return displayText;
        }
        
        public String getDisplayText() {
            return displayText;
        }

        @Override
        protected String getLeftHtmlText() {
            return displayText;
        }
    }

    public static int getImportanceLevel(String fqn) {
        int weight = 50;
        if (fqn.startsWith("java.lang") || fqn.startsWith("java.util")) // NOI18N
        {
            weight -= 10;
        } else if (fqn.startsWith("org.omg") || fqn.startsWith("org.apache")) // NOI18N
        {
            weight += 10;
        } else if (fqn.startsWith("com.sun") || fqn.startsWith("com.ibm") || fqn.startsWith("com.apple")) // NOI18N
        {
            weight += 20;
        } else if (fqn.startsWith("sun") || fqn.startsWith("sunw") || fqn.startsWith("netscape")) // NOI18N
        {
            weight += 30;
        }
        return weight;
    }

    public static CharSequence getElementName(Element el, boolean fqn) {
        if (el == null || el.asType().getKind() == TypeKind.NONE) {
            return "";
        } //NOI18N
        return new ElementNameVisitor().visit(el, fqn);
    }

    private static class ElementNameVisitor extends SimpleElementVisitor6<StringBuilder, Boolean> {

        private ElementNameVisitor() {
            super(new StringBuilder());
        }

        @Override
        public StringBuilder visitPackage(PackageElement e, Boolean p) {
            return DEFAULT_VALUE.append((p ? e.getQualifiedName() : e.getSimpleName()).toString());
        }

        @Override
        public StringBuilder visitType(TypeElement e, Boolean p) {
            return DEFAULT_VALUE.append((p ? e.getQualifiedName() : e.getSimpleName()).toString());
        }
    }
}
