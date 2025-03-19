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
package org.netbeans.modules.refactoring.java.ui;

import java.awt.Color;
import java.awt.Component;
import java.io.CharConversionException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.swing.plaf.LFCustoms;
import org.openide.util.Lookup;
import org.openide.xml.XMLUtil;

/**
 * Class containing various utility methods and inner classes useful when
 * creating refactoring UI.
 *
 * @author Martin Matula, Jan Becicka
 */
public final class UIUtilities {

    // XXX: Remove "test_" when #211651 is fixed
    private static final String TEST_JAVA_MIME_TYPE = "test_text/x-java"; // NOI18N
    /**
     * Element.Kind values allowed to be used when calling ElementHandle.create
     *
     * @see javax.lang.model.element.ElementKind
     * @see org.netbeans.api.java.source.ElementHandle
     */
    public static final EnumSet allowedElementKinds = EnumSet.of(ElementKind.PACKAGE, ElementKind.CLASS, ElementKind.INTERFACE, ElementKind.RECORD,
            ElementKind.ENUM, ElementKind.ANNOTATION_TYPE, ElementKind.METHOD, ElementKind.CONSTRUCTOR, ElementKind.INSTANCE_INIT,
            ElementKind.STATIC_INIT, ElementKind.FIELD, ElementKind.ENUM_CONSTANT, ElementKind.TYPE_PARAMETER);

    // not to be instantiated
    private UIUtilities() {
    }

    /**
     * Returns the same string as passed in or " " if the passed string was an
     * empty string. This method is used as a workaround for issue #58302.
     *
     * @param name Original table column name.
     * @return "Fixed" column name.
     */
    public static String getColumnName(String name) {
        return name == null || name.length() == 0 ? " " : name; // NOI18N
    }

    /**
     * Initializes preferred (and eventually maximum) width of a table column
     * based on the size of its header and the estimated longest value.
     *
     * @param table Table to adjust the column width for.
     * @param index Index of the column.
     * @param longValue Estimated long value for the column.
     * @param padding Number of pixes for padding.
     */
    public static void initColumnWidth(JTable table, int index, Object longValue, int padding) {
        TableColumn column = table.getColumnModel().getColumn(index);

        // get preferred size of the header
        TableCellRenderer headerRenderer = column.getHeaderRenderer();
        if (headerRenderer == null) {
            headerRenderer = table.getTableHeader().getDefaultRenderer();
        }
        Component comp = headerRenderer.getTableCellRendererComponent(
                table, column.getHeaderValue(), false, false, 0, 0);
        int width = comp.getPreferredSize().width;

        // get preferred size of the long value (remeber max of the pref. size for header and long value)
        comp = table.getDefaultRenderer(table.getModel().getColumnClass(index)).getTableCellRendererComponent(
                table, longValue, false, false, 0, index);
        width = Math.max(width, comp.getPreferredSize().width) + 2 * padding;

        // set preferred width of the column
        column.setPreferredWidth(width);
        // if the column contains boolean values, the preferred width
        // should also be its max width
        if (longValue instanceof Boolean) {
            column.setMaxWidth(width);
        }
    }

    /**
     * Table cell renderer that renders Java elements (instances of NamedElement
     * and its subtypes). When rendering the elements it displays element's icon
     * (if available) and display text.
     */
    public static class JavaElementTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, extractText(value), isSelected, hasFocus, row, column);
            if (value instanceof MemberInfo) {
                Icon i = ((MemberInfo) value).getIcon();
                setIcon(i);
            } else {
                setIcon(null);
            }
            return this;
        }

        /**
         * Can be overriden to return alter the standard display text returned
         * for elements.
         *
         * @param value Cell value.
         * @return Display text.
         */
        protected String extractText(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof MemberInfo) {
                return ((MemberInfo) value).getHtmlText();
            } else {
                return value.toString();
            }
        }
    }

    /**
     * Table cell renderer that renders Java elements (instances of NamedElement
     * and its subtypes). When rendering the elements it displays element's icon
     * (if available) and display text.
     */
    public static class JavaElementListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, extractText(value), index, isSelected, cellHasFocus);
            if (value instanceof MemberInfo) {
                Icon i = ((MemberInfo) value).getIcon();
                setIcon(i);
            }
            return this;
        }

        /**
         * Can be overriden to return alter the standard display text returned
         * for elements.
         *
         * @param value Cell value.
         * @return Display text.
         */
        protected String extractText(Object value) {
            if (value instanceof MemberInfo) {
                return ((MemberInfo) value).getHtmlText();
            } else {
                return value.toString();
            }
        }
    }

    /**
     * Table cell renderer for boolean values (a little more advanced that the
     * standard one). Enables hiding the combo box in case the value is
     * <code>null</code> rather than
     * <code>Boolean.TRUE</code> or
     * <code>Boolean.FALSE</code> or in case of read-only cells to give a better
     * visual feedback that the cells cannot be edited.
     */
    public static class BooleanTableCellRenderer implements TableCellRenderer {

        private final TableCellRenderer checkbox;
        private final TableCellRenderer label;

        public BooleanTableCellRenderer(JTable jt) {
            this(jt.getDefaultRenderer(String.class), jt.getDefaultRenderer(Boolean.class));
        }

        private BooleanTableCellRenderer(TableCellRenderer label, TableCellRenderer checkbox) {
            this.checkbox = checkbox;
            this.label = label;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            TableCellRenderer rend = value == null || !table.getModel().isCellEditable(row, column)
                    ? label : checkbox;
            // reset value in case the cell is not editable
            value = value != null && rend == label ? null : value;
            Component comp = rend.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            return comp;
        }
    }

    public static String getHtml(String text) {
        StringBuilder buf = new StringBuilder();
        TokenHierarchy tokenH = TokenHierarchy.create(text, JavaTokenId.language());
        Lookup lookup = MimeLookup.getLookup(MimePath.get(TEST_JAVA_MIME_TYPE));
        FontColorSettings settings = lookup.lookup(FontColorSettings.class);
        TokenSequence tok = tokenH.tokenSequence();
        while (tok.moveNext()) {
            Token<JavaTokenId> token = (Token) tok.token();
            String category = token.id().primaryCategory();
            if (category == null) {
                category = "whitespace"; //NOI18N
            }
            AttributeSet set = settings.getTokenFontColors(category);
            buf.append(color(htmlize(token.text().toString()), set));
        }
        return buf.toString();
    }

    private static String color(String string, AttributeSet set) {
        if (set == null) {
            return string;
        }
        if (string.trim().length() == 0) {
            return string.replace(" ", "&nbsp;").replace("\n", "<br>"); //NOI18N
        }
        StringBuffer buf = new StringBuffer(string);
        if (StyleConstants.isBold(set)) {
            buf.insert(0, "<b>"); //NOI18N
            buf.append("</b>"); //NOI18N
        }
        if (StyleConstants.isItalic(set)) {
            buf.insert(0, "<i>"); //NOI18N
            buf.append("</i>"); //NOI18N
        }
        if (StyleConstants.isStrikeThrough(set)) {
            buf.insert(0, "<s>"); // NOI18N
            buf.append("</s>"); // NOI18N
        }
        buf.insert(0, "<font color=" + getHTMLColor(LFCustoms.getForeground(set)) + ">"); //NOI18N
        buf.append("</font>"); //NOI18N
        return buf.toString();
    }

    private static String getHTMLColor(Color c) {
        String colorR = "0" + Integer.toHexString(c.getRed()); //NOI18N
        colorR = colorR.substring(colorR.length() - 2);
        String colorG = "0" + Integer.toHexString(c.getGreen()); //NOI18N
        colorG = colorG.substring(colorG.length() - 2);
        String colorB = "0" + Integer.toHexString(c.getBlue()); //NOI18N
        colorB = colorB.substring(colorB.length() - 2);
        String html_color = "#" + colorR + colorG + colorB; //NOI18N
        return html_color;
    }

    public static String htmlize(String input) {
        String temp = input.replace("<", "&lt;"); // NOI18N
        temp = temp.replace(">", "&gt;"); // NOI18N
        return temp;
    }
    
    static String format(Element element) {
        return format(element, false, false);
    }
    
    static String format(Element element, boolean forSignature, boolean FQNs) {
        StringBuilder stringBuilder = new StringBuilder();
        format(element, stringBuilder, forSignature, FQNs);

        return stringBuilder.toString();
    }

    static void format(Element element, StringBuilder stringBuilder, boolean forSignature, boolean FQNs) {
        if (element == null) {
            return;
        }

        boolean first = true;
        Set<Modifier> modifiers = element.getModifiers();

        switch (element.getKind()) {
        case PACKAGE:

            PackageElement packageElement = (PackageElement) element;
            if (forSignature) {
                stringBuilder.append("package "); // NOI18N
            }
            stringBuilder.append(packageElement.getQualifiedName());
            break;

        case CLASS:
        case INTERFACE:
        case RECORD:
        case ENUM:
        case ANNOTATION_TYPE:
            if (forSignature) {
                stringBuilder.append(toString(modifiers));
                if (modifiers.size() > 0) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(" ");
                    }
                }
            }
            
            if (forSignature) {
                switch (element.getKind()) {
                    case CLASS:
                        stringBuilder.append("class "); // NOI18N
                        break;
                    case INTERFACE:
                        stringBuilder.append("interface "); // NOI18N
                        break;
                    case ENUM:
                        stringBuilder.append("enum "); // NOI18N
                        break;
                    case RECORD:
                        stringBuilder.append("record "); // NOI18N
                        break;
                    case ANNOTATION_TYPE:
                        stringBuilder.append("@interface "); // NOI18N
                        break;
                }
            }
            
            TypeElement typeElement = (TypeElement) element;
            stringBuilder.append(FQNs
                ? typeElement.getQualifiedName().toString()
                : typeElement.getSimpleName().toString());

            formatTypeParameters(typeElement.getTypeParameters(), stringBuilder, FQNs);

            break;

        case CONSTRUCTOR:
            if (forSignature) {
                stringBuilder.append(toString(modifiers));
                if (modifiers.size() > 0) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(" ");
                    }
                }
            }

            ExecutableElement constructorElement = (ExecutableElement) element;
            stringBuilder.append(constructorElement.getEnclosingElement()
                                                   .getSimpleName().toString());
            stringBuilder.append("(");
            formatVariableElements(constructorElement.getParameters(),
                constructorElement.isVarArgs(), stringBuilder, FQNs);
            stringBuilder.append(")");

            List<? extends TypeMirror> thrownTypesMirrors = constructorElement.getThrownTypes();
            if (!thrownTypesMirrors.isEmpty()) {
                stringBuilder.append(" throws "); // NOI18N
                formatTypeMirrors(thrownTypesMirrors, stringBuilder, FQNs);
            }

            break;

        case METHOD:
            ExecutableElement methodElement = (ExecutableElement) element;
            TypeMirror returnTypeMirror = methodElement.getReturnType();
            List<?extends TypeParameterElement> typeParameters = methodElement.getTypeParameters();

            if (forSignature) {
                stringBuilder.append(toString(modifiers));
                
                if (modifiers.size() > 0) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(" ");
                    }
                }

                if ((typeParameters != null) && (typeParameters.size() > 0)) {
                    formatTypeParameters(typeParameters, stringBuilder, FQNs);
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(" ");
                    }
                }

                formatTypeMirror(returnTypeMirror, stringBuilder, FQNs);
            }

            if (stringBuilder.length() > 0) {
                    stringBuilder.append(" ");
            }

            stringBuilder.append(methodElement.getSimpleName().toString());
            stringBuilder.append("(");
            formatVariableElements(methodElement.getParameters(),
                methodElement.isVarArgs(), stringBuilder, FQNs);
            stringBuilder.append(")");

            List<? extends TypeMirror> thrownTypesMirrorsByMethod = methodElement.getThrownTypes();
            if (!thrownTypesMirrorsByMethod.isEmpty()) {
                stringBuilder.append(" throws "); // NOI18N
                formatTypeMirrors(thrownTypesMirrorsByMethod, stringBuilder, FQNs);
            }

            if (forSignature) {
                AnnotationValue annotationValue = methodElement.getDefaultValue();
                if (annotationValue != null) {
                    Object annotationValueValue = annotationValue.getValue();
                    if (annotationValueValue != null) {
                        stringBuilder.append(" default "); // NOI18N
                        if (annotationValueValue instanceof String) {
                            stringBuilder.append("\"");
                        } else if (annotationValueValue instanceof Character) {
                            stringBuilder.append("\'");
                        } 
                        stringBuilder.append(String.valueOf(annotationValueValue));
                        if (annotationValueValue instanceof String) {
                            stringBuilder.append("\"");
                        } else if (annotationValueValue instanceof Character) {
                            stringBuilder.append("\'");
                        }                    
                    }
                }
            } else {
                stringBuilder.append(":");

                formatTypeMirror(returnTypeMirror, stringBuilder, FQNs);

                if ((typeParameters != null) && (typeParameters.size() > 0)) {
                    stringBuilder.append(":");
                    formatTypeParameters(typeParameters, stringBuilder, FQNs);
                }
            }

            break;

        case TYPE_PARAMETER:
            TypeParameterElement typeParameterElement = (TypeParameterElement) element;
            stringBuilder.append(typeParameterElement.getSimpleName());

            List<?extends TypeMirror> bounds = null;
            try {
                bounds = typeParameterElement.getBounds();
                if ((bounds != null) && (bounds.size() > 0)) {
                    if(!(bounds.size() == 1 && "java.lang.Object".equals( bounds.get(0).toString()))) { // NOI18N
                        stringBuilder.append(" extends "); // NOI18N
                        first = true;
                        for (TypeMirror typeMirror : bounds) {
                            if (first) {
                                first = false;
                            } else {
                                stringBuilder.append(" & "); // NOI18N
                            }
                            formatTypeMirror(typeMirror, stringBuilder, FQNs);
                        }
                    }
                }
            } catch (NullPointerException npe) {
                // Bug?
            }

            break;

        case FIELD:
            VariableElement fieldElement = (VariableElement) element;
            if (forSignature) {
                stringBuilder.append(toString(modifiers));

                if (stringBuilder.length() > 0) {
                    stringBuilder.append(" ");
                }

                formatTypeMirror(fieldElement.asType(), stringBuilder, FQNs);
            }

            if (stringBuilder.length() > 0) {
                stringBuilder.append(" ");
            }

            stringBuilder.append(fieldElement.getSimpleName().toString());

            if (forSignature) {
                Object fieldValue = fieldElement.getConstantValue();
                if (fieldValue != null) {
                    stringBuilder.append(" = ");
                    if (fieldValue instanceof String) {
                        stringBuilder.append("\"");
                    } else if (fieldValue instanceof Character) {
                        stringBuilder.append("\'");
                    } 
                    stringBuilder.append(String.valueOf(fieldValue));
                    if (fieldValue instanceof String) {
                        stringBuilder.append("\"");
                    } else if (fieldValue instanceof Character) {
                        stringBuilder.append("\'");
                    }                    
                }
            } else {
                stringBuilder.append(":");

                formatTypeMirror(fieldElement.asType(), stringBuilder, FQNs);
            }
            
            break;

        case ENUM_CONSTANT:
            stringBuilder.append(element.toString());
            break;

        case PARAMETER:
        case LOCAL_VARIABLE:
            VariableElement variableElement = (VariableElement) element;
            formatTypeMirror(variableElement.asType(), stringBuilder, FQNs);
            stringBuilder.append(" ");
            stringBuilder.append(element.getSimpleName().toString());

            break;
        }
    }
    
    static void formatTypeMirror(TypeMirror typeMirror,
        StringBuilder stringBuilder, boolean FQNs) {
        if (typeMirror == null) {
            return;
        }

        boolean first = true;

        switch (typeMirror.getKind()) {
        case BOOLEAN:
        case BYTE:
        case CHAR:
        case DOUBLE:
        case FLOAT:
        case INT:
        case LONG:
        case NONE:
        case NULL:
        case SHORT:
        case VOID:
            stringBuilder.append(typeMirror);

            break;

        case TYPEVAR:
            TypeVariable typeVariable = (TypeVariable)typeMirror;
            stringBuilder.append(typeVariable.asElement().getSimpleName().toString());
            break;

        case WILDCARD:
            WildcardType wildcardType = (WildcardType)typeMirror;
            stringBuilder.append("?");
            if ( wildcardType.getExtendsBound() != null ) {
                stringBuilder.append(" extends "); // NOI18N
                formatTypeMirror(wildcardType.getExtendsBound(), stringBuilder, FQNs);
            }
            if ( wildcardType.getSuperBound() != null ) {
                stringBuilder.append(" super "); // NOI18N
                formatTypeMirror(wildcardType.getSuperBound(), stringBuilder, FQNs);
            }

            break;

        case DECLARED:
            DeclaredType declaredType = (DeclaredType) typeMirror;
            Element element = declaredType.asElement();
            if (element instanceof TypeElement) {
                stringBuilder.append(
                    FQNs ?
                    ((TypeElement)element).getQualifiedName().toString() :
                    element.getSimpleName().toString());
            } else {
                stringBuilder.append(element.getSimpleName().toString());
            }
            List<? extends TypeMirror> typeArgs = declaredType.getTypeArguments();
            if ( !typeArgs.isEmpty() ) {
                stringBuilder.append("<");
                formatTypeMirrors(typeArgs, stringBuilder, FQNs);
                stringBuilder.append(">");
            }

            break;

        case ARRAY:

            int dims = 0;

            while (typeMirror.getKind() == TypeKind.ARRAY) {
                dims++;
                typeMirror = ((ArrayType) typeMirror).getComponentType();
            }

            formatTypeMirror(typeMirror, stringBuilder, FQNs);

            for (int i = 0; i < dims; i++) {
                stringBuilder.append("[]");
            }

            break;
        }
    }

    static void formatTypeParameters(
        List<? extends TypeParameterElement> typeParameters,
        StringBuilder stringBuilder, boolean FQNs) {
        if ((typeParameters == null) || (typeParameters.size() == 0)) {
            return;
        }

        boolean first = true;        
        if (typeParameters.size() > 0) {
            stringBuilder.append("<");
            first = true;

            for (TypeParameterElement typeParameterElement : typeParameters) {
                if (first) {
                    first = false;
                } else {
                    stringBuilder.append(", ");
                }

                format(typeParameterElement, stringBuilder, false, FQNs);
            }

            stringBuilder.append(">");
        }
    }

    static void formatVariableElements(
        List<? extends VariableElement> variableElements, boolean varArgs,
        StringBuilder stringBuilder, boolean FQNs) {
        if ((variableElements == null) || (variableElements.size() == 0)) {
            return;
        }

        boolean first = true;

        for (VariableElement variableElement : variableElements) {
            if (first) {
                first = false;
            } else {
                stringBuilder.append(", ");
            }

           format(variableElement, stringBuilder, false, FQNs);
        }

        if (varArgs) {
            stringBuilder.append("...");
        }
    }

    static void formatTypeMirrors(List<?extends TypeMirror> thrownTypeMirros,
        StringBuilder stringBuilder, boolean FQNs) {
        if ((thrownTypeMirros == null) || (thrownTypeMirros.size() == 0)) {
            return;
        }

        boolean first = true;

        for (TypeMirror typeMirror : thrownTypeMirros) {
            if (first) {
                first = false;
            } else {
                stringBuilder.append(", ");
            }

            formatTypeMirror(typeMirror, stringBuilder, FQNs);
        }
    }

    static int getIntModifiers(Set<Modifier> modifiers) {
        int intModifiers = 0;

        if (modifiers.contains(Modifier.ABSTRACT)) {
            intModifiers |= java.lang.reflect.Modifier.ABSTRACT;
        }

        if (modifiers.contains(Modifier.FINAL)) {
            intModifiers |= java.lang.reflect.Modifier.FINAL;
        }

        if (modifiers.contains(Modifier.NATIVE)) {
            intModifiers |= java.lang.reflect.Modifier.NATIVE;
        }

        if (modifiers.contains(Modifier.PRIVATE)) {
            intModifiers |= java.lang.reflect.Modifier.PRIVATE;
        }

        if (modifiers.contains(Modifier.PROTECTED)) {
            intModifiers |= java.lang.reflect.Modifier.PROTECTED;
        }

        if (modifiers.contains(Modifier.PUBLIC)) {
            intModifiers |= java.lang.reflect.Modifier.PUBLIC;
        }

        if (modifiers.contains(Modifier.STATIC)) {
            intModifiers |= java.lang.reflect.Modifier.STATIC;
        }

        if (modifiers.contains(Modifier.STRICTFP)) {
            intModifiers |= java.lang.reflect.Modifier.STRICT;
        }

        if (modifiers.contains(Modifier.SYNCHRONIZED)) {
            intModifiers |= java.lang.reflect.Modifier.SYNCHRONIZED;
        }

        if (modifiers.contains(Modifier.TRANSIENT)) {
            intModifiers |= java.lang.reflect.Modifier.TRANSIENT;
        }

        if (modifiers.contains(Modifier.VOLATILE)) {
            intModifiers |= java.lang.reflect.Modifier.VOLATILE;
        }

        return intModifiers;
    }

    static String toString(Set<Modifier> modifiers) {
        return java.lang.reflect.Modifier.toString(getIntModifiers(modifiers));
    }

    static Set<Modifier> getModifiers(int intModifiers) {
        EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);

        if ((intModifiers & java.lang.reflect.Modifier.ABSTRACT) != 0) {
            modifiers.add(Modifier.ABSTRACT);
        }

        if ((intModifiers & java.lang.reflect.Modifier.FINAL) != 0) {
            modifiers.add(Modifier.FINAL);
        }

        if ((intModifiers & java.lang.reflect.Modifier.NATIVE) != 0) {
            modifiers.add(Modifier.NATIVE);
        }

        if ((intModifiers & java.lang.reflect.Modifier.PRIVATE) != 0) {
            modifiers.add(Modifier.PRIVATE);
        }

        if ((intModifiers & java.lang.reflect.Modifier.PROTECTED) != 0) {
            modifiers.add(Modifier.PROTECTED);
        }

        if ((intModifiers & java.lang.reflect.Modifier.PUBLIC) != 0) {
            modifiers.add(Modifier.PUBLIC);
        }

        if ((intModifiers & java.lang.reflect.Modifier.STATIC) != 0) {
            modifiers.add(Modifier.STATIC);
        }

        if ((intModifiers & java.lang.reflect.Modifier.STRICT) != 0) {
            modifiers.add(Modifier.STRICTFP);
        }

        if ((intModifiers & java.lang.reflect.Modifier.SYNCHRONIZED) != 0) {
            modifiers.add(Modifier.SYNCHRONIZED);
        }

        if ((intModifiers & java.lang.reflect.Modifier.TRANSIENT) != 0) {
            modifiers.add(Modifier.TRANSIENT);
        }

        if ((intModifiers & java.lang.reflect.Modifier.VOLATILE) != 0) {
            modifiers.add(Modifier.VOLATILE);
        }

        return modifiers;
    }
    
    static String getClassName(String className) {
        return getClassName(className, false);
    }
    
    static String getClassName(String className, boolean FQNs) {
        // Handle generic type names i.e. strip off parameters
        int firstLessThan = className.indexOf('<');

        if (firstLessThan != -1) {
            className = className.substring(0, firstLessThan);
        }

        if (!FQNs) {
            int lastDot = className.lastIndexOf('.');

            if (lastDot != -1) {
                className = className.substring(lastDot + 1);
            }
        }

        return className;
    }

    static String getClassNameSansPackage(String className) {
        // Handle generic type names i.e. strip off parameters
        int firstLessThan = className.indexOf('<');

        if (firstLessThan != -1) {
            className = className.substring(0, firstLessThan);
        }

        int lastDot = className.lastIndexOf('.');

        if (lastDot != -1) {
            className = className.substring(lastDot + 1);
        }

        return className;
    }
    
    // Tree movement
    static void firstRow(JTree tree) {
        int rowCount = tree.getRowCount();
        if (rowCount > 0) {
            tree.setSelectionRow(0);
            scrollTreeToSelectedRow(tree);
        }
    }
    
    static void previousRow(JTree tree) {
        int rowCount = tree.getRowCount();
        if (rowCount > 0) {
            int selectedRow = tree.getSelectionModel().getMinSelectionRow();
            if (selectedRow == -1) {
                selectedRow = (rowCount -1);
            } else {
                selectedRow--;
                if (selectedRow < 0) {
                    selectedRow = (rowCount -1);
                }
            }
            tree.setSelectionRow(selectedRow);
            scrollTreeToSelectedRow(tree);
        }
    }
    
    static void nextRow(JTree tree) {
        int rowCount = tree.getRowCount();
        if (rowCount > 0) {
            int selectedRow = tree.getSelectionModel().getMinSelectionRow();
            if (selectedRow == -1) {
                selectedRow = 0;
                tree.setSelectionRow(selectedRow);
            } else {
                selectedRow++;
            }
            tree.setSelectionRow(selectedRow % rowCount);
            scrollTreeToSelectedRow(tree);
        }
    }
    
    static void lastRow(JTree tree) {
        int rowCount = tree.getRowCount();
        if (rowCount > 0) {
            tree.setSelectionRow(rowCount - 1);
            scrollTreeToSelectedRow(tree);
        }
    }
    
    static void scrollTreeToSelectedRow(final JTree tree) {
        final int selectedRow = tree.getLeadSelectionRow();
        if (selectedRow >=0) {
            SwingUtilities.invokeLater(
                    new Runnable() {
                public void run() {
                    tree.scrollRectToVisible(tree.getRowBounds(selectedRow));
                }
            }
            );
        }
    }
    
    static String escape(String s) {
        if (s != null) {
            try {
                return XMLUtil.toAttributeValue(s);
            } catch (CharConversionException ex) {
            }
        }
        return null;
    }
    
    private static final String TYPE_COLOR = "#707070";
    private static final String INHERITED_COLOR = "#7D694A";

    /**
     * Creates HTML display name of the Executable element
     */
    public static String createHeader(ExecutableElement e, boolean isDeprecated, boolean isInherited, boolean html, boolean isBold) {
        StringBuilder sb = new StringBuilder();
        if (html) {
            if (isDeprecated) {
                sb.append("<s>"); // NOI18N
            } else if (isBold) {
                sb.append("<b>"); // NOI18N
            }
            if (isInherited) {
                sb.append("<font color=" + INHERITED_COLOR + ">"); // NOI18N
            }
        }
        Name name = e.getKind() == ElementKind.CONSTRUCTOR ? e.getEnclosingElement().getSimpleName() : e.getSimpleName();
        sb.append(UIUtilities.escape(name.toString()));
        if (html) {
            if (isDeprecated) {
                sb.append("</s>"); // NOI18N
            }
        }

        sb.append("("); // NOI18N

        List<? extends VariableElement> params = e.getParameters();
        for (Iterator<? extends VariableElement> it = params.iterator(); it.hasNext();) {
            VariableElement param = it.next();
            if (html) {
                sb.append("<font color=" + TYPE_COLOR + ">"); // NOI18N
            }
            final boolean vararg = !it.hasNext() && e.isVarArgs();
            sb.append(printArg(param.asType(), vararg, html));
            if (html) {
                sb.append("</font>"); // NOI18N
            }
            sb.append(" "); // NOI18N
            sb.append(UIUtilities.escape(param.getSimpleName().toString()));
            if (it.hasNext()) {
                sb.append(", "); // NOI18N
            }
        }


        sb.append(")"); // NOI18N

        if (html) {
            if (!isDeprecated && isBold) {
                sb.append("</b>"); // NOI18N
            }
        }
        if (e.getKind() != ElementKind.CONSTRUCTOR) {
            TypeMirror rt = e.getReturnType();
            if (rt.getKind() != TypeKind.VOID) {
                sb.append(" : "); // NOI18N
                if (html) {
                    sb.append("<font color=" + TYPE_COLOR + ">"); // NOI18N
                }
                sb.append(print(e.getReturnType(), html));
                if (html) {
                    sb.append("</font>"); // NOI18N                    
                }
            }
        }

        return sb.toString();
    }

    public static String createHeader(VariableElement e, boolean isDeprecated, boolean isInherited, boolean html, boolean isBold) {

        StringBuilder sb = new StringBuilder();

        if (html) {
            if (isDeprecated) {
                sb.append("<s>"); // NOI18N
            } else if (isBold) {
                sb.append("<b>"); // NOI18N
            }
            if (isInherited) {
                sb.append("<font color=" + INHERITED_COLOR + ">"); // NOI18N
            }
        }
        sb.append(UIUtilities.escape(e.getSimpleName().toString()));
        if (html) {
            if (isDeprecated) {
                sb.append("</s>"); // NOI18N
            } else if (isBold) {
                sb.append("</b>"); // NOI18N
            }
        }

        if (e.getKind() != ElementKind.ENUM_CONSTANT) {
            sb.append(" : "); // NOI18N
            if (html) {
                sb.append("<font color=" + TYPE_COLOR + ">"); // NOI18N
            }
            sb.append(print(e.asType(), html));
            if (html) {
                sb.append("</font>"); // NOI18N
            }
        }

        return sb.toString();
    }

    public static String createHeader(TypeParameterElement e, boolean isDeprecated, boolean isInherited, boolean html, boolean isBold) {

        StringBuilder sb = new StringBuilder();

        if (html) {
            if (isDeprecated) {
                sb.append("<s>"); // NOI18N
            } else if (isBold) {
                sb.append("<b>"); // NOI18N
            }
            if (isInherited) {
                sb.append("<font color=" + INHERITED_COLOR + ">"); // NOI18N
            }
        }
        sb.append(UIUtilities.escape(e.getSimpleName().toString()));
        if (html) {
            if (isDeprecated) {
                sb.append("</s>"); // NOI18N
            } else if (isBold) {
                sb.append("</b>"); // NOI18N
            }
        }

        if (e.getKind() != ElementKind.ENUM_CONSTANT) {
            sb.append(" : "); // NOI18N
            if (html) {
                sb.append("<font color=" + TYPE_COLOR + ">"); // NOI18N
            }
            sb.append(print(e.asType(), html));
            if (html) {
                sb.append("</font>"); // NOI18N
            }
        }

        return sb.toString();
    }

    public static String createHeader(PackageElement e, boolean isDeprecated, boolean isInherited, boolean html, boolean isBold) {

        StringBuilder sb = new StringBuilder();

        if (html) {
            if (isDeprecated) {
                sb.append("<s>"); // NOI18N
            } else if (isBold) {
                sb.append("<b>"); // NOI18N
            }
            if (isInherited) {
                sb.append("<font color=" + INHERITED_COLOR + ">"); // NOI18N
            }
        }
        sb.append(UIUtilities.escape(e.getSimpleName().toString()));
        if (html) {
            if (isDeprecated) {
                sb.append("</s>"); // NOI18N
            } else if (isBold) {
                sb.append("</b>"); // NOI18N
            }
        }

        if (e.getKind() != ElementKind.ENUM_CONSTANT) {
            sb.append(" : "); // NOI18N
            if (html) {
                sb.append("<font color=" + TYPE_COLOR + ">"); // NOI18N
            }
            sb.append(print(e.asType(), html));
            if (html) {
                sb.append("</font>"); // NOI18N
            }
        }

        return sb.toString();
    }

    public static String createHeader(TypeElement e, boolean isDeprecated, boolean isInherited, boolean html, boolean isBold) {

        StringBuilder sb = new StringBuilder();
        if (html) {
            if (isDeprecated) {
                sb.append("<s>"); // NOI18N
            } else if (isBold) {
                sb.append("<b>"); // NOI18N
            }
            if (isInherited) {
                sb.append("<font color=" + INHERITED_COLOR + ">"); // NOI18N
            }
        }
        sb.append(UIUtilities.escape(e.getSimpleName().toString()));
        if(html) {
            if (isDeprecated) {
                sb.append("</s>"); // NOI18N
            }
        }
        List<? extends TypeParameterElement> typeParams = e.getTypeParameters();
        if (typeParams != null && !typeParams.isEmpty()) {
            sb.append(html ? "&lt;" : "<"); // NOI18N

            for (Iterator<? extends TypeParameterElement> it = typeParams.iterator(); it.hasNext();) {
                TypeParameterElement tp = it.next();
                sb.append(UIUtilities.escape(tp.getSimpleName().toString()));
                List<? extends TypeMirror> bounds = null;
                try {
                    bounds = tp.getBounds();
                } catch (NullPointerException npe) {
                    // Ignore
                }
                if (bounds != null && !bounds.isEmpty()) {
                    sb.append(printBounds(bounds, html));
                }

                if (it.hasNext()) {
                    sb.append(", "); // NOI18N
                }
            }

            sb.append(html ? "&gt;" : ">"); // NOI18N
        }

        if (html && !isDeprecated && isBold) {
            sb.append("<b>"); // NOI18N
        }

        // Add superclass and implemented interfaces

        TypeMirror sc = e.getSuperclass();
        String scName = print(sc, html);

        if (sc == null
                || e.getKind() == ElementKind.ENUM
                || e.getKind() == ElementKind.ANNOTATION_TYPE
                || "java.lang.Object".equals(sc.toString()) || // NOI18N
                "<none>".equals(sc.toString())) { // NOI18N
            scName = null;
        }

        List<? extends TypeMirror> ifaces = e.getInterfaces();

        if ((scName != null || !ifaces.isEmpty())
                && e.getKind() != ElementKind.ANNOTATION_TYPE) {
            sb.append(" :: "); // NOI18N
            if (scName != null) {
                if (html) {
                    sb.append("<font color=" + TYPE_COLOR + ">"); // NOI18N                
                }
                sb.append(scName);
                if (html) {
                    sb.append("</font>"); // NOI18N
                }
            }
            if (!ifaces.isEmpty()) {
                if (scName != null) {
                    sb.append(" : "); // NOI18N
                }
                for (Iterator<? extends TypeMirror> it = ifaces.iterator(); it.hasNext();) {
                    TypeMirror typeMirror = it.next();
                    if (html) {
                        sb.append("<font color=" + TYPE_COLOR + ">"); // NOI18N                
                    }
                    sb.append(print(typeMirror, html));
                    if (html) {
                        sb.append("</font>"); // NOI18N
                    }
                    if (it.hasNext()) {
                        sb.append(", "); // NOI18N
                    }
                }

            }
        }

        return sb.toString();
    }
    
    public static boolean runWhenScanFinished (final Runnable runnable, final String actionName) {
            return ScanDialog.runWhenScanFinished(
                () -> {
                    if (ParserManager.isParsing()) {
                        SwingUtilities.invokeLater(runnable);
                    } else {
                        runnable.run();
                    }
                },
                actionName);
    }

    private static String printBounds(List<? extends TypeMirror> bounds, boolean html) {
        if (bounds.size() == 1 && "java.lang.Object".equals(bounds.get(0).toString())) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        sb.append(" extends "); // NOI18N

        for (Iterator<? extends TypeMirror> it = bounds.iterator(); it.hasNext();) {
            TypeMirror bound = it.next();
            sb.append(print(bound, html));
            if (it.hasNext()) {
                sb.append(" & "); // NOI18N
            }
        }

        return sb.toString();
    }

    private static String printArg(final TypeMirror tm, final boolean varArg, boolean html) {
        if (varArg) {
            if (tm.getKind() == TypeKind.ARRAY) {
                final ArrayType at = (ArrayType) tm;
                final StringBuilder sb = new StringBuilder(print(at.getComponentType(), html));
                sb.append("...");   //NOI18N
                return sb.toString();
            } else {
                assert false : "Expected array: " + tm.toString() + " ( " + tm.getKind() + " )"; //NOI18N
            }
        }
        return print(tm, html);
    }

    private static String print(TypeMirror tm, boolean html) {
        StringBuilder sb;

        switch (tm.getKind()) {
            case DECLARED:
                DeclaredType dt = (DeclaredType) tm;
                sb = new StringBuilder(dt.asElement().getSimpleName().toString());
                List<? extends TypeMirror> typeArgs = dt.getTypeArguments();
                if (!typeArgs.isEmpty()) {
                    sb.append(html ? "&lt;" : "<"); // NOI18N

                    for (Iterator<? extends TypeMirror> it = typeArgs.iterator(); it.hasNext();) {
                        TypeMirror ta = it.next();
                        sb.append(print(ta, html));
                        if (it.hasNext()) {
                            sb.append(", ");
                        }
                    }
                    sb.append(html ? "&gt;" : ">"); // NOI18N
                }

                return sb.toString();
            case TYPEVAR:
                TypeVariable tv = (TypeVariable) tm;
                sb = new StringBuilder(tv.asElement().getSimpleName().toString());
                return sb.toString();
            case ARRAY:
                ArrayType at = (ArrayType) tm;
                sb = new StringBuilder(print(at.getComponentType(), html));
                sb.append("[]");
                return sb.toString();
            case WILDCARD:
                WildcardType wt = (WildcardType) tm;
                sb = new StringBuilder("?");
                if (wt.getExtendsBound() != null) {
                    sb.append(" extends "); // NOI18N
                    sb.append(print(wt.getExtendsBound(), html));
                }
                if (wt.getSuperBound() != null) {
                    sb.append(" super "); // NOI18N
                    sb.append(print(wt.getSuperBound(), html));
                }
                return sb.toString();
            default:
                return UIUtilities.escape(tm.toString());
        }
    }
}