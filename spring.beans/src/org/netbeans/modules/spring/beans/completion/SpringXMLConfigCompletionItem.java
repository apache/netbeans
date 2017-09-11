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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.spring.beans.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.beans.BeanInfo;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.beans.completion.SpringXMLConfigCompletionDoc.JavaElementDoc;
import org.netbeans.modules.spring.java.JavaUtils;
import org.netbeans.modules.spring.java.Property;
import org.netbeans.modules.spring.java.PropertyType;
import org.netbeans.modules.spring.util.SpringBeansUIs;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.netbeans.swing.plaf.LFCustoms;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.xml.XMLUtil;

/**
 * A completion item shown in a valid code completion request
 * in a Spring XML Configuration file
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public abstract class SpringXMLConfigCompletionItem implements CompletionItem {

    public static SpringXMLConfigCompletionItem createBeanRefItem(int substitutionOffset, String displayName,
            SpringBean bean, FileObject containerFO) {
        return new BeanRefItem(substitutionOffset, displayName, bean, containerFO);
    }

    public static SpringXMLConfigCompletionItem createPackageItem(int substitutionOffset, String packageName,
            boolean deprecated) {
        return new PackageItem(substitutionOffset, packageName, deprecated);
    }

    public static SpringXMLConfigCompletionItem createTypeItem(int substitutionOffset, TypeElement elem, ElementHandle<TypeElement> elemHandle,
                boolean deprecated, boolean smartItem) {
        return new ClassItem(substitutionOffset, elem, elemHandle, deprecated, smartItem);
    }

    public static SpringXMLConfigCompletionItem createMethodItem(int substitutionOffset, ExecutableElement element,
            boolean isInherited, boolean isDeprecated) {
        return new MethodItem(substitutionOffset, element, isInherited, isDeprecated);
    }

    public static SpringXMLConfigCompletionItem createPropertyItem(int substitutionOffset, Property property) {
        return new PropertyItem(substitutionOffset, property);
    }

    public static SpringXMLConfigCompletionItem createAttribValueItem(int substitutionOffset, String displayText, String docText) {
        return new AttribValueItem(substitutionOffset, displayText, docText);
    }

    public static SpringXMLConfigCompletionItem createFolderItem(int substitutionOffset, FileObject folder) {
        return new FolderItem(substitutionOffset, folder);
    }

    public static SpringXMLConfigCompletionItem createSpringXMLFileItem(int substitutionOffset, FileObject file) {
        return new FileItem(substitutionOffset, file);
    }

    public static SpringXMLConfigCompletionItem createPropertyAttribItem(int substitutionOffset, String text, Property property) {
        return new PropertyAttribItem(substitutionOffset, text, property);
    }

    public static SpringXMLConfigCompletionItem createBeanNameItem(int substitutionOffset, String text, int sortPriority) {
        return new BeanNameItem(substitutionOffset, text, sortPriority);
    }

    protected int substitutionOffset;

    protected SpringXMLConfigCompletionItem(int substitutionOffset) {
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

    protected void substituteText(JTextComponent c, final int offset, final int len, String toAdd) {
        final BaseDocument doc = (BaseDocument) c.getDocument();
        CharSequence prefix = getSubstitutionText();
        String text = prefix.toString();
        if(toAdd != null) {
            text += toAdd;
        }
        final String finalText = text;
        doc.runAtomic(new Runnable() {

            @Override
            public void run() {
                try {
                    Position position = doc.createPosition(offset);
                    doc.remove(offset, len);
                    doc.insertString(position.getOffset(), finalText.toString(), null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
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

    private static String getHTMLColor(int r, int g, int b) {
        Color c = LFCustoms.shiftColor(new Color(r, g, b));
        return "<font color=#" //NOI18N
                + LFCustoms.getHexString(c.getRed())
                + LFCustoms.getHexString(c.getGreen())
                + LFCustoms.getHexString(c.getBlue())
                + ">"; //NOI18N
    }

    private static class BeanRefItem extends SpringXMLConfigCompletionItem {

        private static final String CLASS_COLOR = getHTMLColor(128, 128, 128);

        private String beanId;
        private String beanClass;
        private List<String> beanNames;
        private String displayName;
        private String beanLocFile;
        private Action goToBeanAction;
        private String leftText;

        public BeanRefItem(int substitutionOffset, String displayName, SpringBean bean, FileObject containerFO) {
            super(substitutionOffset);
            this.beanId = bean.getId();
            this.beanClass = bean.getClassName();
            this.beanNames = bean.getNames();
            if (bean.getLocation() != null) {
                FileObject fo = bean.getLocation().getFile();
                if (fo != null) {
                    this.beanLocFile = FileUtil.getRelativePath(containerFO.getParent(), fo);
                }
            }
            goToBeanAction = SpringBeansUIs.createGoToBeanAction(bean);
            this.displayName = displayName;
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

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(displayName);
                sb.append(CLASS_COLOR);
                sb.append(" ("); // NOI18N
                if(this.beanClass != null) {
                    sb.append(beanClass);
                }
                sb.append(")"); // NOI18N
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }

        @Override
        protected String getRightHtmlText() {
            return beanLocFile;
        }

        @Override
        protected ImageIcon getIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/spring/beans/resources/spring-bean.png", false); // NOI18N
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return new AsyncCompletionTask(new AsyncCompletionQuery() {
                @Override
                protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                    CompletionDocumentation docItem = SpringXMLConfigCompletionDoc.createBeanRefDoc(beanId,
                            beanNames, beanClass, beanLocFile, goToBeanAction);
                    resultSet.setDocumentation(docItem);
                    resultSet.finish();
                }
            });
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
    private static class ClassItem extends SpringXMLConfigCompletionItem {
        private static final String CLASS = "org/netbeans/modules/editor/resources/completion/class_16.png"; //NOI18N
        private static final String CLASS_COLOR = getHTMLColor(86, 0, 0);
        private static final String PKG_COLOR = getHTMLColor(128, 128, 128);

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
            while(parent.getKind() != ElementKind.PACKAGE) {
                sb.insert(0, parent.getSimpleName().toString() + "$"); // NOI18N
                parent = parent.getEnclosingElement();
            }

            return sb.toString();
        }

        public int getSortPriority() {
            return 200;
        }

        public CharSequence getSortText() {
            return sortText;
        }

        public CharSequence getInsertPrefix() {
            return smartItem ? "" : elemHandle.getBinaryName(); // NOI18N
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
                if (deprecated)
                    sb.append(STRIKE);
                sb.append(displayName);
                if (deprecated)
                    sb.append(STRIKE_END);
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
            return new AsyncCompletionTask(new JavaElementDocQuery(elemHandle), EditorRegistry.lastFocusedComponent());
        }
    }

    private static class PackageItem extends SpringXMLConfigCompletionItem {

        private static final String PACKAGE = "org/netbeans/modules/java/editor/resources/package.gif"; // NOI18N
        private static final String PACKAGE_COLOR = getHTMLColor(0, 86, 0);
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
            return 50;
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
                if(evt.getKeyChar() == '.') { // NOI18N
                    Completion.get().hideDocumentation();
                    JTextComponent component = (JTextComponent)evt.getSource();
                    int caretOffset = component.getSelectionEnd();
                    int len = caretOffset - substitutionOffset;
                    if (len >= 0) {
                        substituteText(component, substitutionOffset, len, Character.toString(evt.getKeyChar()));
                        Completion.get().showCompletion();
                        evt.consume();
                    }
                }
            }
        }

        @Override
        protected ImageIcon getIcon(){
            if (icon == null) icon = ImageUtilities.loadImageIcon(PACKAGE, false);
            return icon;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(PACKAGE_COLOR);
                if (deprecated)
                    sb.append(STRIKE);
                sb.append(simpleName);
                if (deprecated)
                    sb.append(STRIKE_END);
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }
    }

    private static class MethodItem extends SpringXMLConfigCompletionItem {

        private static final String METHOD_PUBLIC = "org/netbeans/modules/editor/resources/completion/method_16.png"; //NOI18N
        private static final String METHOD_PROTECTED = "org/netbeans/modules/editor/resources/completion/method_protected_16.png"; //NOI18N
        private static final String METHOD_PACKAGE = "org/netbeans/modules/editor/resources/completion/method_package_private_16.png"; //NOI18N
        private static final String METHOD_PRIVATE = "org/netbeans/modules/editor/resources/completion/method_private_16.png"; //NOI18N
        private static final String METHOD_ST_PUBLIC = "org/netbeans/modules/editor/resources/completion/method_static_16.png"; //NOI18N
        private static final String METHOD_ST_PROTECTED = "org/netbeans/modules/editor/resources/completion/method_static_protected_16.png"; //NOI18N
        private static final String METHOD_ST_PRIVATE = "org/netbeans/modules/editor/resources/completion/method_static_private_16.png"; //NOI18N
        private static final String METHOD_ST_PACKAGE = "org/netbeans/modules/editor/resources/completion/method_static_package_private_16.png"; //NOI18N
        private static final String METHOD_COLOR = getHTMLColor(0, 0, 0);; //NOI18N
        private static final String PARAMETER_NAME_COLOR = getHTMLColor(160, 96, 1);
        private static ImageIcon icon[][] = new ImageIcon[2][4];

        private ElementHandle<ExecutableElement> elementHandle;
        private boolean isDeprecated;
        private String simpleName;
        private Set<Modifier> modifiers;
        private List<ParamDesc> params;
        private boolean isPrimitive;
        private String typeName;
        private String sortText;
        private String leftText;
        private boolean isInherited;
        private String rightText;

        public MethodItem(int substitutionOffset, ExecutableElement element, boolean isInherited, boolean isDeprecated) {
            super(substitutionOffset);
            this.elementHandle = ElementHandle.create(element);
            this.isDeprecated = isDeprecated;
            this.isInherited = isInherited;
            this.simpleName = element.getSimpleName().toString();
            this.modifiers = element.getModifiers();
            this.params = new ArrayList<ParamDesc>();
            Iterator<? extends VariableElement> it = element.getParameters().iterator();
            Iterator<? extends TypeMirror> tIt = ((ExecutableType) element.asType()).getParameterTypes().iterator();
            while(it.hasNext() && tIt.hasNext()) {
                TypeMirror tm = tIt.next();
                this.params.add(new ParamDesc(tm.toString(), getTypeName(tm, false, element.isVarArgs() && !tIt.hasNext()).toString(), it.next().getSimpleName().toString()));
            }
            TypeMirror retType = element.getReturnType();

            this.typeName = getTypeName(retType, false).toString();
            this.isPrimitive = retType.getKind().isPrimitive() || retType.getKind() == TypeKind.VOID;
        }

        public int getSortPriority() {
            return 100;
        }

        public CharSequence getSortText() {
            if (sortText == null) {
                StringBuilder sortParams = new StringBuilder();
                sortParams.append('('); // NOI18N
                int cnt = 0;
                for(Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                    ParamDesc param = it.next();
                    sortParams.append(param.typeName);
                    if (it.hasNext()) {
                        sortParams.append(','); // NOI18N
                    }
                    cnt++;
                }
                sortParams.append(')'); // NOI18N
                sortText = simpleName + "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString(); //NOI18N
            }
            return sortText;
        }

        public CharSequence getInsertPrefix() {
            return simpleName;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder lText = new StringBuilder();
                lText.append(METHOD_COLOR);
                if (!isInherited)
                    lText.append(BOLD);
                if (isDeprecated)
                    lText.append(STRIKE);
                lText.append(simpleName);
                if (isDeprecated)
                    lText.append(STRIKE_END);
                if (!isInherited)
                    lText.append(BOLD_END);
                lText.append(COLOR_END);
                lText.append('('); // NOI18N
                for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                    ParamDesc paramDesc = it.next();
                    lText.append(escape(paramDesc.typeName));
                    lText.append(' ');
                    lText.append(PARAMETER_NAME_COLOR);
                    lText.append(paramDesc.name);
                    lText.append(COLOR_END);
                    if (it.hasNext()) {
                        lText.append(", "); //NOI18N
                    }
                }
                lText.append(')'); // NOI18N
                return lText.toString();
            }
            return leftText;
        }

        @Override
        protected String getRightHtmlText() {
            if (rightText == null)
                rightText = escape(typeName);
            return rightText;
        }

        @Override
        protected ImageIcon getIcon() {
            int level = getProtectionLevel(modifiers);
            boolean isStatic = modifiers.contains(Modifier.STATIC);
            ImageIcon cachedIcon = icon[isStatic?1:0][level];
            if (cachedIcon != null)
                return cachedIcon;

            String iconPath = METHOD_PUBLIC;
            if (isStatic) {
                switch (level) {
                    case PRIVATE_LEVEL:
                        iconPath = METHOD_ST_PRIVATE;
                        break;

                    case PACKAGE_LEVEL:
                        iconPath = METHOD_ST_PACKAGE;
                        break;

                    case PROTECTED_LEVEL:
                        iconPath = METHOD_ST_PROTECTED;
                        break;

                    case PUBLIC_LEVEL:
                        iconPath = METHOD_ST_PUBLIC;
                        break;
                }
            }else{
                switch (level) {
                    case PRIVATE_LEVEL:
                        iconPath = METHOD_PRIVATE;
                        break;

                    case PACKAGE_LEVEL:
                        iconPath = METHOD_PACKAGE;
                        break;

                    case PROTECTED_LEVEL:
                        iconPath = METHOD_PROTECTED;
                        break;

                    case PUBLIC_LEVEL:
                        iconPath = METHOD_PUBLIC;
                        break;
                }
            }
            ImageIcon newIcon = ImageUtilities.loadImageIcon(iconPath, false);
            icon[isStatic?1:0][level] = newIcon;
            return newIcon;
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return new AsyncCompletionTask(new JavaElementDocQuery(elementHandle), EditorRegistry.lastFocusedComponent());
        }

        private static final int PUBLIC_LEVEL = 3;
        private static final int PROTECTED_LEVEL = 2;
        private static final int PACKAGE_LEVEL = 1;
        private static final int PRIVATE_LEVEL = 0;

        private static int getProtectionLevel(Set<Modifier> modifiers) {
            if (modifiers.contains(Modifier.PUBLIC)) {
                return PUBLIC_LEVEL;
            }
            if (modifiers.contains(Modifier.PROTECTED)) {
                return PROTECTED_LEVEL;
            }
            if (modifiers.contains(Modifier.PRIVATE)) {
                return PRIVATE_LEVEL;
            }
            return PACKAGE_LEVEL;
        }

        static class ParamDesc {

            private String fullTypeName;
            private String typeName;
            private String name;

            public ParamDesc(String fullTypeName, String typeName, String name) {
                this.fullTypeName = fullTypeName;
                this.typeName = typeName;
                this.name = name;
            }
        }
    }

    private static class AttribValueItem extends SpringXMLConfigCompletionItem {

        private String displayText;
        private String docText;

        public AttribValueItem(int substitutionOffset, String displayText, String docText) {
            super(substitutionOffset);
            this.displayText = displayText;
            this.docText = docText;
        }

        public int getSortPriority() {
            return 50;
        }

        public CharSequence getSortText() {
            return displayText;
        }

        public CharSequence getInsertPrefix() {
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
                    if(docText != null) {
                        CompletionDocumentation documentation = SpringXMLConfigCompletionDoc.getAttribValueDoc(docText);
                        resultSet.setDocumentation(documentation);
                    }
                    resultSet.finish();
                }
            });
        }
    }

    private static class BeanNameItem extends AttribValueItem {

        private final int sortPriority;

        public BeanNameItem(int substitutionOffset, String displayText, int sortPriority) {
            super(substitutionOffset, displayText, null);
            this.sortPriority = sortPriority;
        }

        @Override
        public int getSortPriority() {
            return sortPriority;
        }
    }

    private static class FolderItem extends SpringXMLConfigCompletionItem {

        private FileObject folder;

        public FolderItem(int substitutionOffset, FileObject folder) {
            super(substitutionOffset);
            this.folder = folder;
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
            if (evt.getID() == KeyEvent.KEY_TYPED) {
                if(evt.getKeyChar() == '/') { // NOI18N
                    Completion.get().hideDocumentation();
                    JTextComponent component = (JTextComponent)evt.getSource();
                    int caretOffset = component.getSelectionEnd();
                    substituteText(component, substitutionOffset, caretOffset - substitutionOffset, Character.toString(evt.getKeyChar()));
                    Completion.get().showCompletion();
                    evt.consume();
                }
            }
        }

        public int getSortPriority() {
            return 300;
        }

        public CharSequence getSortText() {
            return folder.getName();
        }

        public CharSequence getInsertPrefix() {
            return folder.getName();
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }

        @Override
        protected ImageIcon getIcon() {
            return new ImageIcon(getTreeFolderIcon());
        }

        @Override
        protected String getLeftHtmlText() {
            return folder.getName();
        }

        private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon"; // NOI18N
        private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon"; // NOI18N

        /**
         * Returns default folder icon as {@link java.awt.Image}. Never returns
         * <code>null</code>.Adapted from J2SELogicalViewProvider
         */
        private static Image getTreeFolderIcon() {
            Image base = null;
            Icon baseIcon = UIManager.getIcon(ICON_KEY_UIMANAGER); // #70263
            if (baseIcon != null) {
                base = ImageUtilities.icon2Image(baseIcon);
            } else {
                base = (Image) UIManager.get(ICON_KEY_UIMANAGER_NB); // #70263
                if (base == null) { // fallback to our owns
                    final Node n = DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
                    base = n.getIcon(BeanInfo.ICON_COLOR_16x16);
                }
            }
            assert base != null;
            return base;
        }
    }

    private static class PropertyItem extends SpringXMLConfigCompletionItem {

        private static final String PROP_RO = "org/netbeans/modules/beans/resources/propertyRO.gif"; // NOI18N
        private static final String PROP_RW = "org/netbeans/modules/beans/resources/propertyRW.gif"; // NOI18N
        private static final String PROP_WO = "org/netbeans/modules/beans/resources/propertyWO.gif"; // NOI18N

        private String displayName;
        private PropertyType propertyType;
        private String typeName;
        private static EnumMap<PropertyType, ImageIcon> type2Icon = new EnumMap<PropertyType, ImageIcon>(PropertyType.class);

        public PropertyItem(int substitutionOffset, Property property) {
            super(substitutionOffset);
            this.displayName = property.getName();
            this.typeName = escape(getTypeName(property.getImplementationType(), false).toString());
            this.propertyType = property.getType();
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

        @Override
        protected String getLeftHtmlText() {
            return displayName;
        }

        @Override
        protected String getRightHtmlText() {
            return typeName;
        }

        @Override
        protected ImageIcon getIcon() {
            ImageIcon cachedIcon = type2Icon.get(propertyType);
            if(cachedIcon == null) {
                switch(propertyType) {
                    case READ_ONLY:
                        cachedIcon = ImageUtilities.loadImageIcon(PROP_RO, false);
                        break;
                    case READ_WRITE:
                        cachedIcon = ImageUtilities.loadImageIcon(PROP_RW, false);
                        break;
                    case WRITE_ONLY:
                        cachedIcon = ImageUtilities.loadImageIcon(PROP_WO, false);
                        break;
                }

                type2Icon.put(propertyType, cachedIcon);
            }

            return cachedIcon;
        }
    }

    private static class PropertyAttribItem extends PropertyItem {

        private String text;

        public PropertyAttribItem(int substitutionOffset, String text, Property property) {
            super(substitutionOffset, property);
            this.text = text;
        }

        @Override
        public int getSortPriority() {
            return 200;
        }

        @Override
        public CharSequence getSortText() {
            return text;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return text;
        }

        @Override
        protected CharSequence getSubstitutionText() {
            return text + "=\"\""; // NOI18N
        }

        @Override
        protected String getLeftHtmlText() {
            return text;
        }

        @Override
        protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
            super.substituteText(c, offset, len, toAdd);
            int newCaretPos = c.getCaretPosition() - 1; // for achieving p:something-ref="|" on completion
            c.setCaretPosition(newCaretPos);
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }
    }

    private static class FileItem extends SpringXMLConfigCompletionItem {

        private FileObject file;

        public FileItem(int substitutionOffset, FileObject file) {
            super(substitutionOffset);
            this.file = file;
        }

        public int getSortPriority() {
            return 100;
        }

        public CharSequence getSortText() {
            return file.getNameExt();
        }

        public CharSequence getInsertPrefix() {
            return file.getNameExt();
        }

        @Override
        protected ImageIcon getIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/spring/beans/resources/spring.png", false); // NOI18N
        }

        @Override
        protected String getLeftHtmlText() {
            return file.getNameExt();
        }
    }

    public static CharSequence getElementName(Element el, boolean fqn) {
        if (el == null || el.asType().getKind() == TypeKind.NONE)
            return ""; //NOI18N
        return new ElementNameVisitor().visit(el, fqn);
    }

    private static class ElementNameVisitor extends SimpleElementVisitor6<StringBuilder,Boolean> {

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

    public static int getImportanceLevel(String fqn) {
        int weight = 50;
        if (fqn.startsWith("java.lang") || fqn.startsWith("java.util")) // NOI18N
            weight -= 10;
        else if (fqn.startsWith("org.omg") || fqn.startsWith("org.apache")) // NOI18N
            weight += 10;
        else if (fqn.startsWith("com.sun") || fqn.startsWith("com.ibm") || fqn.startsWith("com.apple")) // NOI18N
            weight += 20;
        else if (fqn.startsWith("sun") || fqn.startsWith("sunw") || fqn.startsWith("netscape")) // NOI18N
            weight += 30;
        return weight;
    }

    public static CharSequence getTypeName(TypeMirror type, boolean fqn) {
        return getTypeName(type, fqn, false);
    }

    public static CharSequence getTypeName(TypeMirror type, boolean fqn, boolean varArg) {
	if (type == null)
            return ""; //NOI18N
        return new TypeNameVisitor(varArg).visit(type, fqn);
    }

    private static final String UNKNOWN = "<unknown>"; //NOI18N
    private static final String CAPTURED_WILDCARD = "<captured wildcard>"; //NOI18N

    private static class TypeNameVisitor extends SimpleTypeVisitor6<StringBuilder,Boolean> {

        private boolean varArg;

        private TypeNameVisitor(boolean varArg) {
            super(new StringBuilder());
            this.varArg = varArg;
        }

        @Override
        public StringBuilder defaultAction(TypeMirror t, Boolean p) {
            return DEFAULT_VALUE.append(t);
        }

        @Override
        public StringBuilder visitDeclared(DeclaredType t, Boolean p) {
            Element e = t.asElement();
            if (e instanceof TypeElement) {
                TypeElement te = (TypeElement)e;
                DEFAULT_VALUE.append((p ? te.getQualifiedName() : te.getSimpleName()).toString());
                Iterator<? extends TypeMirror> it = t.getTypeArguments().iterator();
                if (it.hasNext()) {
                    DEFAULT_VALUE.append("<"); //NOI18N
                    while(it.hasNext()) {
                        visit(it.next(), p);
                        if (it.hasNext())
                            DEFAULT_VALUE.append(", "); //NOI18N
                    }
                    DEFAULT_VALUE.append(">"); //NOI18N
                }
                return DEFAULT_VALUE;
            } else {
                return DEFAULT_VALUE.append(UNKNOWN); //NOI18N
            }
        }

        @Override
        public StringBuilder visitArray(ArrayType t, Boolean p) {
            boolean isVarArg = varArg;
            varArg = false;
            visit(t.getComponentType(), p);
            return DEFAULT_VALUE.append(isVarArg ? "..." : "[]"); //NOI18N
        }

        @Override
        public StringBuilder visitTypeVariable(TypeVariable t, Boolean p) {
            Element e = t.asElement();
            if (e != null) {
                String name = e.getSimpleName().toString();
                if (!CAPTURED_WILDCARD.equals(name))
                    return DEFAULT_VALUE.append(name);
            }
            DEFAULT_VALUE.append("?"); //NOI18N
            TypeMirror bound = t.getLowerBound();
            if (bound != null && bound.getKind() != TypeKind.NULL) {
                DEFAULT_VALUE.append(" super "); //NOI18N
                visit(bound, p);
            } else {
                bound = t.getUpperBound();
                if (bound != null && bound.getKind() != TypeKind.NULL) {
                    DEFAULT_VALUE.append(" extends "); //NOI18N
                    if (bound.getKind() == TypeKind.TYPEVAR)
                        bound = ((TypeVariable)bound).getLowerBound();
                    visit(bound, p);
                }
            }
            return DEFAULT_VALUE;
        }

        @Override
        public StringBuilder visitWildcard(WildcardType t, Boolean p) {
            DEFAULT_VALUE.append("?"); //NOI18N
            TypeMirror bound = t.getSuperBound();
            if (bound == null) {
                bound = t.getExtendsBound();
                if (bound != null) {
                    DEFAULT_VALUE.append(" extends "); //NOI18N
                    if (bound.getKind() == TypeKind.WILDCARD)
                        bound = ((WildcardType)bound).getSuperBound();
                    visit(bound, p);
                } else {
                    bound = SourceUtils.getBound(t);
                    if (bound != null && (bound.getKind() != TypeKind.DECLARED || !((TypeElement)((DeclaredType)bound).asElement()).getQualifiedName().contentEquals("java.lang.Object"))) { //NOI18N
                        DEFAULT_VALUE.append(" extends "); //NOI18N
                        visit(bound, p);
                    }
                }
            } else {
                DEFAULT_VALUE.append(" super "); //NOI18N
                visit(bound, p);
            }
            return DEFAULT_VALUE;
        }

        @Override
        public StringBuilder visitError(ErrorType t, Boolean p) {
            Element e = t.asElement();
            if (e instanceof TypeElement) {
                TypeElement te = (TypeElement)e;
                return DEFAULT_VALUE.append((p ? te.getQualifiedName() : te.getSimpleName()).toString());
            }
            return DEFAULT_VALUE;
        }
    }

    private static String escape(String s) {
        if (s != null) {
            try {
                return XMLUtil.toAttributeValue(s);
            } catch (Exception ex) {}
        }
        return s;
    }

    private static class JavaElementDocQuery extends AsyncCompletionQuery {

        private CompletionDocumentation documentation;
        private ElementHandle<?> elemHandle;

        public JavaElementDocQuery(ElementHandle<?> elemHandle) {
            this.elemHandle = elemHandle;
        }

        @Override
        protected void query(final CompletionResultSet resultSet, Document doc, int caretOffset) {
            try {
                JavaSource js = JavaUtils.getJavaSource(doc);
                if (js == null) {
                    return;
                }

                js.runUserActionTask(new org.netbeans.api.java.source.Task<CompilationController>() {
                    @Override
                    public void run(CompilationController cc) throws Exception {
                        cc.toPhase(Phase.RESOLVED);
                        Element element = elemHandle.resolve(cc);
                        if (element == null) {
                            return;
                        }
                        resolveDocumentation(cc, element);
                        if (documentation instanceof JavaElementDoc) {
                            while (!isTaskCancelled()) {
                                try {
                                    ((JavaElementDoc) documentation).getFutureText().get(250, TimeUnit.MILLISECONDS);
                                    resultSet.setDocumentation(documentation);
                                    break;
                                } catch (InterruptedException ex) {
                                    Exceptions.printStackTrace(ex);
                                } catch (ExecutionException ex) {
                                    Exceptions.printStackTrace(ex);
                                } catch (TimeoutException timeOut) {/*retry*/}
                            }
                        }
                    }
                }, false);

                resultSet.finish();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        private void resolveDocumentation(CompilationController controller, Element el) throws IOException {
            switch (el.getKind()) {
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
                if (el.asType().getKind() == TypeKind.ERROR)
                    break;
            case CONSTRUCTOR:
            case ENUM_CONSTANT:
            case FIELD:
            case METHOD:
                documentation = SpringXMLConfigCompletionDoc.createJavaDoc(controller, el);
            }
        }
    }
}
