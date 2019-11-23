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

package org.netbeans.modules.groovy.editor.api.completion;

import groovy.lang.MetaMethod;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.type.TypeMirror;
import javax.swing.ImageIcon;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.Variable;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.spi.DefaultCompletionProposal;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.elements.ast.ASTMethod;
import org.netbeans.modules.groovy.editor.api.elements.ElementHandleSupport;
import org.netbeans.modules.groovy.editor.api.elements.GroovyElement;
import org.netbeans.modules.groovy.editor.api.elements.KeywordElement;
import org.netbeans.modules.groovy.editor.api.elements.common.MethodElement.MethodParameter;
import org.netbeans.modules.groovy.editor.java.Utilities;
import org.netbeans.modules.groovy.editor.utils.GroovyUtils;
import org.netbeans.modules.groovy.support.api.GroovySources;
import org.openide.util.ImageUtilities;


/**
 *
 * @author schmidtm
 */
// FIXME static accessors
public abstract class CompletionItem extends DefaultCompletionProposal {

    protected final GroovyElement element;
    
    private static final Logger LOG = Logger.getLogger(CompletionItem.class.getName());
    private static volatile ImageIcon groovyIcon;
    private static volatile ImageIcon javaIcon;
    private static volatile ImageIcon newConstructorIcon;

    
    private CompletionItem(GroovyElement element, int anchorOffset) {
        this.element = element;
        this.anchorOffset = anchorOffset;

        LOG.setLevel(Level.OFF);
    }

    @Override
    public String getName() {
        return element.getName();
    }

    @Override
    public ElementHandle getElement() {
        LOG.log(Level.FINEST, "getElement() element : {0}", element);

        return null;
    }

    @Override
    public ElementKind getKind() {
        return element.getKind();
    }

    @Override
    public Set<Modifier> getModifiers() {
        return element.getModifiers();
    }

    @Override
    public String toString() {
        String cls = getClass().getName();
        cls = cls.substring(cls.lastIndexOf('.') + 1);

        return cls + "(" + getKind() + "): " + getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CompletionItem other = (CompletionItem) obj;
        if ((this.getName() == null) ? (other.getName() != null) : !this.getName().equals(other.getName())) {
            return false;
        }
        if (this.getKind() != other.getKind()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
        hash = 47 * hash + (this.getKind() != null ? this.getKind().hashCode() : 0);
        return hash;
    }

    public static CompletionItem forJavaMethod(String className, String simpleName, List<String> parameters,
            TypeMirror returnType, Set<javax.lang.model.element.Modifier> modifiers, int anchorOffset,
            boolean emphasise, boolean nameOnly) {
        return new JavaMethodItem(className, simpleName, parameters, returnType, modifiers, anchorOffset, emphasise, nameOnly);
    }

    public static CompletionItem forJavaMethod(String className, String simpleName, List<String> parameters,
            String returnType, Set<javax.lang.model.element.Modifier> modifiers, int anchorOffset,
            boolean emphasise, boolean nameOnly) {
        return new JavaMethodItem(className, simpleName, parameters, returnType, modifiers, anchorOffset, emphasise, nameOnly);
    }

    public static CompletionItem forDynamicMethod(int anchorOffset, String name, String[] parameters, String returnType, boolean prefix) {
        return new DynamicMethodItem(anchorOffset, name, parameters, returnType, prefix);
    }

    public static CompletionItem forDynamicField(int anchorOffset, String name, String type) {
        return new DynamicFieldItem(anchorOffset, name, type);
    }

    private static class JavaMethodItem extends CompletionItem {

        private final String className;
        private final String simpleName;
        private final List<String> parameters;
        private final String returnType;
        private final Set<javax.lang.model.element.Modifier> modifiers;
        private final boolean emphasise;
        private final boolean nameOnly;

        
        public JavaMethodItem(String className, String simpleName, List<String> parameters, TypeMirror returnType,
                Set<javax.lang.model.element.Modifier> modifiers, int anchorOffset, boolean emphasise, boolean nameOnly) {
            this(className, simpleName, parameters,
                    Utilities.getTypeName(returnType, false).toString(), modifiers, anchorOffset, emphasise, nameOnly);
        }

        public JavaMethodItem(String className, String simpleName, List<String> parameters, String returnType,
                Set<javax.lang.model.element.Modifier> modifiers, int anchorOffset, boolean emphasise, boolean nameOnly) {
            super(null, anchorOffset);
            this.className = className;
            this.simpleName = simpleName;
            this.parameters = parameters;
            this.returnType = GroovyUtils.stripPackage(returnType);
            this.modifiers = modifiers;
            this.emphasise = emphasise;
            this.nameOnly = nameOnly;
        }
        
        @Override
        public String getName() {
            return simpleName + "()";
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            if (emphasise) {
                formatter.emphasis(true);
            }
            formatter.appendText(simpleName + "(" + getParameters() + ")");
            if (emphasise) {
                formatter.emphasis(false);
            }
            return formatter.getText();
        }
        
        private String getParameters() {
            StringBuilder sb = new StringBuilder();
            for (String string : parameters) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(GroovyUtils.stripPackage(string));
            }
            return sb.toString();
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            String retType = "";
            if (returnType != null) {
                retType = returnType;
            }

            formatter.appendText(retType);

            return formatter.getText();
        }


        @Override
        public ImageIcon getIcon() {
            return (ImageIcon) ElementIcons.getElementIcon(javax.lang.model.element.ElementKind.METHOD, modifiers);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Utilities.modelModifiersToGsf(modifiers);
        }

        @Override
        public ElementHandle getElement() {
            return ElementHandleSupport.createHandle(className, simpleName, ElementKind.METHOD, getModifiers());
        }

        @Override
        public String getCustomInsertTemplate() {
            if (nameOnly) {
                return simpleName;
            }
            return super.getCustomInsertTemplate();
        }

    }

    public static class DynamicFieldItem extends CompletionItem {

        private final String name;
        private final String type;
        

        public DynamicFieldItem(int anchorOffset, String name, String type) {
            super(null, anchorOffset);
            this.name = name;
            this.type = type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.FIELD;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            // no FQN return types but only the classname, please:

            String retType = type;
            retType = GroovyUtils.stripPackage(retType);

            formatter.appendText(retType);

            return formatter.getText();
        }

        @Override
        public ImageIcon getIcon() {
            if (groovyIcon == null) {
                groovyIcon = ImageUtilities.loadImageIcon(GroovySources.GROOVY_FILE_ICON_16x16, false);
            }

            return groovyIcon;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.singleton(Modifier.PROTECTED);
        }

        @Override
        public ElementHandle getElement() {
            return ElementHandleSupport.createHandle(null, name, ElementKind.FIELD, getModifiers());
        }
    }

    private static class DynamicMethodItem extends CompletionItem {

        private final String name;
        private final String[] parameters;
        private final String returnType;
        private final boolean prefix;
        

        public DynamicMethodItem(int anchorOffset, String name, String[] parameters, String returnType, boolean prefix) {
            super(null, anchorOffset);
            this.name = name;
            this.parameters = parameters;
            this.returnType = returnType;
            this.prefix = prefix;
        }

        @Override
        public String getName() {
            return name + "()";
        }

        @Override
        public String getSortText() {
            return (name + (prefix ? 1 : 0)) + parameters.length;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {

            ElementKind kind = getKind();

            formatter.name(kind, true);

            formatter.appendText(name);

            if (!prefix) {
                StringBuilder buf = new StringBuilder();
                // construct signature by removing package names.
                for (String param : parameters) {
                    if (buf.length() > 0) {
                        buf.append(", ");
                    }
                    buf.append(GroovyUtils.stripPackage(Utilities.translateClassLoaderTypeName(param)));
                }

                String simpleSig = buf.toString();
                formatter.appendText("(" + simpleSig + ")");
            } else {
                formatter.appendText("..."); // NOI18N
            }

            formatter.name(kind, false);

            return formatter.getText();
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            // no FQN return types but only the classname, please:

            String retType = returnType;
            retType = GroovyUtils.stripPackage(retType);

            formatter.appendText(retType);

            return formatter.getText();
        }

        @Override
        public ImageIcon getIcon() {
            if (groovyIcon == null) {
                groovyIcon = ImageUtilities.loadImageIcon(GroovySources.GROOVY_FILE_ICON_16x16, false);
            }

            return groovyIcon;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.singleton(Modifier.PROTECTED);
        }

        @Override
        public ElementHandle getElement() {
            return ElementHandleSupport.createHandle(null, name, ElementKind.METHOD, getModifiers());
        }

        @Override
        public String getCustomInsertTemplate() {
            return name;
        }

    }

    public static class MetaMethodItem extends CompletionItem {

        private final MetaMethod method;
        private final boolean isGDK;
        private final ASTMethod methodElement;
        private final boolean nameOnly;
        

        public MetaMethodItem(Class clz, MetaMethod method, int anchorOffset, boolean isGDK, boolean nameOnly) {
            super(null, anchorOffset);
            this.method = method;
            this.isGDK = isGDK;
            this.nameOnly = nameOnly;

            // This is an artificial, new ElementHandle which has no real
            // equivalent in the AST. It's used to match the one passed to super.document()
            methodElement = new ASTMethod(new ASTNode(), clz, method, isGDK);
        }

        public MetaMethod getMethod() {
            return method;
        }

        @Override
        public String getName() {
            return method.getName() + "()";
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {

            ElementKind kind = getKind();

            formatter.name(kind, true);

            if (isGDK) {
                formatter.appendText(method.getName());

                // construct signature by removing package names.

                String signature = method.getSignature();
                int start = signature.indexOf("(");
                int end = signature.indexOf(")");

                String sig = signature.substring(start + 1, end);

                StringBuilder buf = new StringBuilder();

                for (String param : sig.split(",")) {
                    if (buf.length() > 0) {
                        buf.append(", ");
                    }
                    buf.append(GroovyUtils.stripPackage(Utilities.translateClassLoaderTypeName(param)));
                }

                String simpleSig = buf.toString();
                formatter.appendText("(" + simpleSig + ")");
            } else {
                formatter.appendText(CompletionHandler.getMethodSignature(method, false, isGDK));
            }


            formatter.name(kind, false);

            return formatter.getText();
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            // no FQN return types but only the classname, please:

            String retType = method.getReturnType().getSimpleName();
            retType = GroovyUtils.stripPackage(retType);

            formatter.appendText(retType);

            return formatter.getText();
        }

        @Override
        public ImageIcon getIcon() {
            if (!isGDK) {
                return (ImageIcon) ElementIcons.getElementIcon(javax.lang.model.element.ElementKind.METHOD,
                        Utilities.reflectionModifiersToModel(method.getModifiers()));
            }

            if (groovyIcon == null) {
                groovyIcon = ImageUtilities.loadImageIcon(GroovySources.GROOVY_FILE_ICON_16x16, false);
            }

            return groovyIcon;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {

            // to display the documentation box for each element, the completion-
            // element needs to implement this method. Otherwise document(...)
            // won't even be called at all.

            return methodElement;
        }

        @Override
        public String getCustomInsertTemplate() {
            if (nameOnly) {
                return method.getName();
            }
            return super.getCustomInsertTemplate();
        }

    }

    public static class KeywordItem extends CompletionItem {

        private static final String JAVA_KEYWORD   = "org/netbeans/modules/groovy/editor/resources/duke.png"; //NOI18N
        private final String keyword;
        private final String description;
        private final boolean isGroovy;
        private final ParserResult info;

        public KeywordItem(String keyword, String description, int anchorOffset, ParserResult info, boolean isGroovy) {
            super(null, anchorOffset);
            this.keyword = keyword;
            this.description = description;
            this.info = info;
            this.isGroovy = isGroovy;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            if (description != null) {
                //formatter.appendText(description);
                formatter.appendHtml(description);

                return formatter.getText();
            } else {
                return null;
            }
        }

        @Override
        public ImageIcon getIcon() {

            if (isGroovy) {
                if (groovyIcon == null) {
                    groovyIcon = ImageUtilities.loadImageIcon(GroovySources.GROOVY_FILE_ICON_16x16, false);
                }
                return groovyIcon;
            } else {
                if (javaIcon == null) {
                    javaIcon = ImageUtilities.loadImageIcon(JAVA_KEYWORD, false);
                }
                return javaIcon;
            }
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return ElementHandleSupport.createHandle(info, new KeywordElement(keyword));
        }
    }

    public static class PackageItem extends CompletionItem {

        private final String packageName;
        private final ParserResult info;

        
        public PackageItem(String packageName, int anchorOffset, ParserResult info) {
            super(null, anchorOffset);
            this.packageName = packageName;
            this.info = info;
        }

        @Override
        public String getName() {
            return packageName;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.PACKAGE;
        }

        @Override
        public ImageIcon getIcon() {
            return (ImageIcon) ElementIcons.getElementIcon(javax.lang.model.element.ElementKind.PACKAGE, null);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return ElementHandleSupport.createHandle(info, new KeywordElement(packageName));
        }

        @Override
        public String getCustomInsertTemplate() {
            return packageName + "."; //NOI18N
        }
    }

    public static class TypeItem extends CompletionItem {

        private final String fqn;
        private final String name;
        private final javax.lang.model.element.ElementKind ek;

        public TypeItem(String fqn, String name, int anchorOffset, javax.lang.model.element.ElementKind ek) {
            super(null, anchorOffset);
            this.fqn = fqn;
            this.name = name;
            this.ek = ek;
        }

        public String getFqn() {
            return fqn;
        }
        
        @Override
        public String getName() {
            return name;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CLASS;
        }

        @Override
        public ImageIcon getIcon() {
            return (ImageIcon) ElementIcons.getElementIcon(ek, null);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            // return ElementHandleSupport.createHandle(request.info, new ClassElement(name));
            return null;
        }
    }

    public static class ConstructorItem extends CompletionItem {

        private static final String NEW_CSTR   = "org/netbeans/modules/groovy/editor/resources/new_constructor_16.png"; //NOI18N
        private final boolean expand; // should this item expand to a constructor body?
        private final String name;
        private final String paramListString;
        private final List<MethodParameter> parameters;


        public ConstructorItem(String name, List<MethodParameter> parameters, int anchorOffset, boolean expand) {
            super(null, anchorOffset);
            this.name = name;
            this.expand = expand;
            this.parameters = parameters;
            this.paramListString = parseParams();
        }

        private String parseParams() {
            StringBuilder sb = new StringBuilder();
            if (!parameters.isEmpty()) {
                for (MethodParameter parameter : parameters) {
                    sb.append(parameter.getType());
                    sb.append(", ");
                }
                // Removing last ", "
                sb.delete(sb.length() - 2, sb.length());
            }
            return sb.toString();
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            if (expand) {
                return name + "(" + paramListString +  ") - generate"; // NOI18N
            } else {
                return name + "(" + paramListString +  ")";
            }
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CONSTRUCTOR;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return null;
        }

        @Override
        public ImageIcon getIcon() {
            if (newConstructorIcon == null) {
                newConstructorIcon = ImageUtilities.loadImageIcon(NEW_CSTR, false);
            }
            return newConstructorIcon;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            // return ElementHandleSupport.createHandle(request.info, new ClassElement(name));
            return null;
        }

        // Constructors are smart by definition (have to be place above others)
        @Override
        public boolean isSmart() {
            return true;
        }

        @Override
        public String getCustomInsertTemplate() {

            StringBuilder sb = new StringBuilder();

            sb.append(getInsertPrefix());
            sb.append("(");

            int id = 1;

            // sb.append("${cursor}"); // NOI18N

            if (parameters != null) {
                for (MethodParameter paramDesc : parameters) {

                    LOG.log(Level.FINEST, "-------------------------------------------------------------------");
                    LOG.log(Level.FINEST, "paramDesc.fullTypeName : {0}", paramDesc.getFqnType());
                    LOG.log(Level.FINEST, "paramDesc.typeName     : {0}", paramDesc.getType());
                    LOG.log(Level.FINEST, "paramDesc.name         : {0}", paramDesc.getName());

                    sb.append("${"); //NOI18N

                    sb.append("groovy-cc-"); // NOI18N
                    sb.append(Integer.toString(id));

                    sb.append(" default=\""); // NOI18N
                    sb.append(paramDesc.getName());
                    sb.append("\""); // NOI18N

                    sb.append("}"); //NOI18N

                    // simply hardcoded values. For testing purposes.
                    // sb.append(paramDesc.name);


                    if (id < parameters.size()) {
                        sb.append(", "); //NOI18N
                    }

                    id++;
                }
            }

            sb.append(")");
            if (expand) {
                sb.append(" {\n}");
            }

            LOG.log(Level.FINEST, "Template returned : {0}", sb.toString());
            return sb.toString();

        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ConstructorItem other = (ConstructorItem) obj;
            if (this.expand != other.expand) {
                return false;
            }
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            if ((this.paramListString == null) ? (other.paramListString != null) : !this.paramListString.equals(other.paramListString)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 61 * hash + (this.expand ? 1 : 0);
            hash = 61 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 61 * hash + (this.paramListString != null ? this.paramListString.hashCode() : 0);
            return hash;
        }
    }

    public static class NamedParameter extends CompletionItem {

        private final String typeName;
        private final String name;


        public NamedParameter(String typeName, String name, int anchorOffset) {
            super(null, anchorOffset);
            this.typeName = typeName;
            this.name = name;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            return name + ": " + typeName; // NOI18N
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getCustomInsertTemplate() {
            return name + ": " + typeName;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.PARAMETER;
        }

        @Override
        public boolean isSmart() {
            return true;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + Objects.hashCode(this.typeName);
            hash = 89 * hash + Objects.hashCode(this.name);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final NamedParameter other = (NamedParameter) obj;
            if (!Objects.equals(this.typeName, other.typeName)) {
                return false;
            }
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            return true;
        }
    }

    public static class JavaFieldItem extends CompletionItem {

        private final String className;
        private final String name;
        private final TypeMirror type;
        private final Set<javax.lang.model.element.Modifier> modifiers;
        private final boolean emphasise;

        
        public JavaFieldItem(String className, String name, TypeMirror type,
                Set<javax.lang.model.element.Modifier> modifiers, int anchorOffset, boolean emphasise) {
            super(null, anchorOffset);
            this.className = className;
            this.name = name;
            this.type = type;
            this.modifiers = modifiers;
            this.emphasise = emphasise;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.FIELD;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            String retType = "";
            if (type != null) {
                retType = Utilities.getTypeName(type, false).toString();
            }

            formatter.appendText(retType);

            return formatter.getText();
        }

        @Override
        public ImageIcon getIcon() {
            return (ImageIcon) ElementIcons.getElementIcon(
                    javax.lang.model.element.ElementKind.FIELD, modifiers);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Utilities.modelModifiersToGsf(modifiers);
        }

        @Override
        public ElementHandle getElement() {
            return ElementHandleSupport.createHandle(className, name, ElementKind.FIELD, getModifiers());
        }
    }

    public static class FieldItem extends CompletionItem {

        private final String typeName;
        private final String fieldName;
        private final Set<Modifier> modifiers;

        
        public FieldItem(String typeName, String fieldName, int modifiers, int anchorOffset) {
            this(typeName, fieldName, Utilities.modelModifiersToGsf(Utilities.reflectionModifiersToModel(modifiers)), anchorOffset);
        }

        public FieldItem(String typeName, String fieldName, Set<Modifier> modifiers, int anchorOffset) {
            super(null, anchorOffset);
            this.typeName = typeName;
            this.fieldName = fieldName;
            this.modifiers = modifiers;
        }

        @Override
        public String getName() {
            return fieldName;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.FIELD;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return typeName;
        }

        @Override
        public ImageIcon getIcon() {
            return (ImageIcon) ElementIcons.getElementIcon(javax.lang.model.element.ElementKind.FIELD,
                    Utilities.gsfModifiersToModel(modifiers, null));
        }

        @Override
        public Set<Modifier> getModifiers() {
            return modifiers;
        }

        @Override
        public ElementHandle getElement() {
            return ElementHandleSupport.createHandle(typeName, fieldName, ElementKind.FIELD, getModifiers());
        }
    }

    public static class LocalVarItem extends CompletionItem {

        private final Variable var;

        public LocalVarItem(Variable var, int anchorOffset) {
            super(null, anchorOffset);
            this.var = var;
        }

        @Override
        public String getName() {
            return var.getName();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.VARIABLE;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return GroovyUtils.stripPackage(Utilities.translateClassLoaderTypeName(var.getType().getName()));
        }

        @Override
        public ImageIcon getIcon() {
            return (ImageIcon) ElementIcons.getElementIcon(javax.lang.model.element.ElementKind.LOCAL_VARIABLE, null);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }
    }

    public static class NewVarItem extends CompletionItem {

        private final String var;

        public NewVarItem(String var, int anchorOffset) {
            super(null, anchorOffset);
            this.var = var;
        }

        @Override
        public String getName() {
            return var;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.VARIABLE;
        }

        @Override
        public ImageIcon getIcon() {
            return (ImageIcon) ElementIcons.getElementIcon(javax.lang.model.element.ElementKind.LOCAL_VARIABLE, null);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }
    }

    public static class NewFieldItem extends CompletionItem {

        private final String fieldName;

        public NewFieldItem(String var, int anchorOffset) {
            super(null, anchorOffset);
            this.fieldName = var;
        }

        @Override
        public String getName() {
            return fieldName;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.FIELD;
        }

        @Override
        public ImageIcon getIcon() {
            return (ImageIcon) ElementIcons.getElementIcon(javax.lang.model.element.ElementKind.FIELD, null);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }
    }
}

