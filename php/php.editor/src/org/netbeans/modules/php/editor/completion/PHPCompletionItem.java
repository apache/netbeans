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
package org.netbeans.modules.php.editor.completion;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.Cache;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.completion.CompletionContextFinder.CompletionContext;
import org.netbeans.modules.php.editor.completion.CompletionContextFinder.KeywordCompletionType;
import org.netbeans.modules.php.editor.actions.IconsUtils;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.QualifiedNameKind;
import org.netbeans.modules.php.editor.api.elements.AliasedElement;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement.PrintAs;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.ConstantElement;
import org.netbeans.modules.php.editor.api.elements.FieldElement;
import org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement;
import org.netbeans.modules.php.editor.api.elements.FunctionElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.NamespaceElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement.OutputType;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TraitElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeMemberElement;
import org.netbeans.modules.php.editor.api.elements.TypeNameResolver;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.api.elements.VariableElement;
import org.netbeans.modules.php.editor.codegen.CodegenUtils;
import org.netbeans.modules.php.editor.elements.ParameterElementImpl;
import org.netbeans.modules.php.editor.elements.TypeNameResolverImpl;
import org.netbeans.modules.php.editor.indent.CodeStyle;
import org.netbeans.modules.php.editor.index.PredefinedSymbolElement;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;
import org.netbeans.modules.php.editor.NavUtils;
import static org.netbeans.modules.php.editor.PredefinedSymbols.Attributes.OVERRIDE;
import org.netbeans.modules.php.editor.api.elements.EnumCaseElement;
import org.netbeans.modules.php.editor.api.elements.EnumElement;
import org.netbeans.modules.php.editor.codegen.AutoImport;
import org.netbeans.modules.php.editor.elements.ElementUtils;
import org.netbeans.modules.php.editor.options.CodeCompletionPanel;
import org.netbeans.modules.php.editor.options.CodeCompletionPanel.CodeCompletionType;
import org.netbeans.modules.php.editor.options.OptionsUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.project.api.PhpLanguageProperties;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public abstract class PHPCompletionItem implements CompletionProposal {

    protected static final ImageIcon KEYWORD_ICON = IconsUtils.loadKeywordIcon();
    protected static final ImageIcon ENUM_CASE_ICON = IconsUtils.loadEnumCaseIcon();
    private static final int TYPE_NAME_MAX_LENGTH = Integer.getInteger("nb.php.editor.ccTypeNameMaxLength", 30); // NOI18N
    private static final Cache<FileObject, PhpLanguageProperties> PROPERTIES_CACHE
            = new Cache<>(new WeakHashMap<>());
    private static final String AUTO_IMPORT_PARAM_FORMAT = "%s${php-auto-import default=\"\" fqName=%s aliasName=\"%s\" useType=%s editable=false}"; // NOI18N
    private static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private static volatile Boolean ADD_FIRST_CLASS_CALLABLE = null; // for unit tests

    final CompletionRequest request;
    private final ElementHandle element;
    private final boolean isPlatform;
    private final boolean isDeprecated;
    private QualifiedNameKind generateAs;

    // for unit tests
    private PhpVersion phpVersion;
    private CodeCompletionType codeCompletionType;
    private Boolean isAutoImport = null;
    private Boolean isGlobalItemImportable = null;

    PHPCompletionItem(ElementHandle element, CompletionRequest request, QualifiedNameKind generateAs) {
        this.request = request;
        this.element = element;
        this.generateAs = generateAs;
        if (element instanceof PhpElement) {
            final PhpElement phpElement = (PhpElement) element;
            isPlatform = phpElement.isPlatform();
            isDeprecated = phpElement.isDeprecated();
        } else {
            isPlatform = false;
            isDeprecated = false;
        }
    }

    PHPCompletionItem(ElementHandle element, CompletionRequest request) {
        this(element, request, null);
    }

    // for unit tests
    static void setAddFirstClassCallable(Boolean add) {
        ADD_FIRST_CLASS_CALLABLE = add;
    }

    @Override
    public int getAnchorOffset() {
        return request.anchor;
    }

    @Override
    public ElementHandle getElement() {
        return element;
    }

    @Override
    public String getName() {
        return element.getName();
    }

    @Override
    public String getSortText() {
        return getName();
    }

    @Override
    public int getSortPrioOverride() {
        return 0;
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        formatter.name(getKind(), true);
        String name = getName();
        if (CodeUtils.isSyntheticTypeName(name)) {
            // anonymous class
            name = "{}"; // NOI18N
        }
        if (isDeprecated()) {
            formatter.deprecated(true);
            formatter.appendText(name);
            formatter.deprecated(false);
        } else {
            formatter.appendText(name);
        }
        formatter.name(getKind(), false);
        return formatter.getText();
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public Set<Modifier> getModifiers() {
        Set<Modifier> emptyModifiers = Collections.emptySet();
        ElementHandle handle = getElement();
        return (handle != null) ? handle.getModifiers() : emptyModifiers;
    }

    public String getFileNameURL() {
        ElementHandle elem = getElement();
        return (elem instanceof PhpElement) ? ((PhpElement) elem).getFilenameUrl() : ""; //NOI18N
    }

    @Override
    public boolean isSmart() {
        return element instanceof AliasedElement
                || (request.currentlyEditedFileURL != null && request.currentlyEditedFileURL.equals(getFileNameURL()));
    }

    protected boolean isDeprecated() {
        return isDeprecated;
    }

    boolean isPlatform() {
        return isPlatform;
    }

    // for unit tests for custom template
    void setPhpVersion(PhpVersion phpVersion) {
        this.phpVersion = phpVersion;
    }

    protected PhpVersion getPhpVersion(@NullAllowed FileObject file) {
        if (phpVersion != null) {
            return phpVersion;
        }
        return file != null ? CodeUtils.getPhpVersion(file) : PhpVersion.getDefault();
    }

    private static NamespaceDeclaration findEnclosingNamespace(PHPParseResult info, int offset) {
        final Program program = info.getProgram();
        List<ASTNode> nodes = NavUtils.underCaret(info, Math.min((program != null) ? program.getEndOffset() : offset, offset));
        for (ASTNode node : nodes) {
            if (node instanceof NamespaceDeclaration) {
                return (NamespaceDeclaration) node;
            }
        }
        return null;
    }

    @Override
    public String getCustomInsertTemplate() {
        return null;
    }

    @Override
    public String getInsertPrefix() {
        StringBuilder template = new StringBuilder();
        ElementHandle elem = getElement();
        if (elem instanceof MethodElement) {
            final MethodElement method = (MethodElement) elem;
            if (method.isConstructor() && isNewClassContext(request.context)) {
                elem = method.getType();
            }
        }
        if (elem instanceof FullyQualifiedElement) {
            FullyQualifiedElement ifq = (FullyQualifiedElement) elem;
            final QualifiedName qn = QualifiedName.create(request.prefix);
            final FileObject fileObject = request.result.getSnapshot().getSource().getFileObject();
            PhpLanguageProperties props = PROPERTIES_CACHE.get(fileObject);
            if (props == null) {
                props = PhpLanguageProperties.forFileObject(fileObject);
                PropertyChangeListener propertyChangeListener = WeakListeners.propertyChange(new PhpVersionChangeListener(fileObject), props);
                props.addPropertyChangeListener(propertyChangeListener);
                PROPERTIES_CACHE.save(fileObject, props);
            }
            if (props.getPhpVersion() != PhpVersion.PHP_5) {
                if (generateAs == null) {
                    CodeCompletionType completionType = getCodeCompletionType();
                    switch (completionType) {
                        case FULLY_QUALIFIED:
                            template.append(ifq.getFullyQualifiedName());
                            return template.toString();
                        case UNQUALIFIED:
                            String autoImportTemplate = createAutoImportTemplate(ifq);
                            if (autoImportTemplate != null) {
                                return autoImportTemplate;
                            }
                            template.append(getName());
                            return template.toString();
                        case SMART:
                            generateAs = qn.getKind();
                            break;
                        default:
                            assert false : completionType;
                    }
                }
            } else {
                template.append(getName());
                return template.toString();
            }
            switch (generateAs) {
                case FULLYQUALIFIED:
                    template.append(ifq.getFullyQualifiedName());
                    break;
                case QUALIFIED:
                    final String fqn = ifq.getFullyQualifiedName().toString();
                    int indexOf = fqn.toLowerCase().indexOf(qn.toNamespaceName().toString().toLowerCase());
                    if (indexOf != -1) {
                        template.append(fqn.substring(indexOf == 0 ? 1 : indexOf));
                        break;
                    }
                case UNQUALIFIED:
                    String enclosingScopeName = ifq.getIn();
                    boolean fncOrConstFromDefaultNamespace = (((ifq instanceof FunctionElement) || (ifq instanceof ConstantElement))
                            && (enclosingScopeName == null || enclosingScopeName.isEmpty())
                            && NamespaceDeclarationInfo.DEFAULT_NAMESPACE_NAME.equals(ifq.getNamespaceName().toString()));
                    final boolean isUnqualified = ifq.isAliased()
                            && (ifq instanceof AliasedElement) && ((AliasedElement) ifq).isNameAliased();
                    if (!fncOrConstFromDefaultNamespace && !isUnqualified) {
                        Model model = request.result.getModel();
                        NamespaceDeclaration namespaceDeclaration = findEnclosingNamespace(request.result, request.anchor);
                        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(namespaceDeclaration, model.getFileScope());

                        if (namespaceScope != null) {
                            LinkedList<String> segments = ifq.getFullyQualifiedName().getSegments();
                            QualifiedName fqna = QualifiedName.create(false, segments);
                            if (!namespaceScope.isDefaultNamespace() || !fqna.getKind().isUnqualified()) {
                                QualifiedName suffix = VariousUtils.getPreferredName(fqna, namespaceScope);
                                if (suffix != null) {
                                    template.append(suffix.toString());
                                    break;
                                }
                            }
                        }
                    }
                    template.append(getName());
                    break;
                default:
                    assert false : generateAs;
            }

            // XXX improve?
            String tpl = template.toString();
            String extraPrefix = request.extraPrefix;
            if (StringUtils.hasText(extraPrefix)) {
                if (tpl.startsWith(extraPrefix)) {
                    tpl = tpl.substring(extraPrefix.length());
                } else {
                    assert false : "[" + tpl + "] should start with [" + extraPrefix + "]";
                }
            }
            String autoImportTemplate = createAutoImportTemplate(ifq);
            if (autoImportTemplate != null) {
                return autoImportTemplate;
            }
            return tpl;
        }

        return getName();
    }

    @CheckForNull
    private String createAutoImportTemplate(FullyQualifiedElement fullyQualifiedElement) {
        if (isAutoImport()) {
            String fqName = fullyQualifiedElement.getFullyQualifiedName().toString().substring(CodeUtils.NS_SEPARATOR.length());
            String name = getName();
            String useType = getUseType();
            String aliasName = CodeUtils.EMPTY_STRING;
            boolean isGlobalNamespace = !fqName.contains(CodeUtils.NS_SEPARATOR);
            Model model = request.result.getModel();
            NamespaceDeclaration namespaceDeclaration = findEnclosingNamespace(request.result, request.anchor);
            NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(namespaceDeclaration, model.getFileScope());
            if (!useType.isEmpty() && isImportableScope() && !AutoImport.sameUseNameExists(name, fqName, AutoImport.getUseScopeType(useType), namespaceScope)) {
                if (isAutoImportContext(request.context) && !fullyQualifiedElement.isAliased() && isGlobalItemImportable(isGlobalNamespace)) {
                    // note: add an empty parameter(hidden parameter) after a name to avoid filtering completion items
                    // if we add a default value to the parameter template, completion items are removed(filtered) from a completion list when we move the caret.
                    // see: org.netbeans.modules.csl.editor.completion.GsfCompletionProvider.JavaCompletionQuery.getFilteredData()
                    return String.format(AUTO_IMPORT_PARAM_FORMAT, name, fqName, aliasName, useType);
                }
            }
        }
        return null;
    }

    // for unit tests
    void setAutoImport(boolean isAutoImport) {
        this.isAutoImport = isAutoImport;
    }

    private boolean isAutoImport() {
        if (isAutoImport != null) {
            // for unit tests
            return isAutoImport;
        }
        return OptionsUtils.autoImport();
    }

    // for unit tests
    void setCodeCompletionType(CodeCompletionType codeCompletionType) {
        this.codeCompletionType = codeCompletionType;
    }

    private CodeCompletionType getCodeCompletionType() {
        if (codeCompletionType != null) {
            // for unit tests
            return codeCompletionType;
        }
        return OptionsUtils.codeCompletionType();
    }

    // for unit tests
    void setGlobalItemImportable(boolean isGlobalItemImportable) {
        this.isGlobalItemImportable = isGlobalItemImportable;
    }

    private boolean isGlobalItemImportable(boolean isGlobalNamespace) {
        if (isGlobalNamespace) {
            if (isGlobalItemImportable != null) {
                // for unit tests
                return isGlobalItemImportable;
            }
            CodeCompletionPanel.GlobalNamespaceAutoImportType globalNSImport = null;
            switch (getUseType()) {
                case AutoImport.USE_TYPE:
                    globalNSImport = OptionsUtils.globalNSImportType();
                    break;
                case AutoImport.USE_FUNCTION:
                    globalNSImport = OptionsUtils.globalNSImportFunction();
                    break;
                case AutoImport.USE_CONST:
                    globalNSImport = OptionsUtils.globalNSImportConst();
                    break;
                default:
                    assert false : "Unknown use type: " + getUseType(); // NOI18N
            }
            return globalNSImport == CodeCompletionPanel.GlobalNamespaceAutoImportType.IMPORT;
        }
        return true;
    }

    private boolean isImportableScope() {
        Model model = request.result.getModel();
        Collection<? extends NamespaceScope> declaredNamespaces = model.getFileScope().getDeclaredNamespaces();
        NamespaceDeclaration namespaceDeclaration = findEnclosingNamespace(request.result, request.anchor);
        if (declaredNamespaces.size() > 1 && namespaceDeclaration == null) {
            return false;
        } else if (declaredNamespaces.size() == 1) {
            return OptionsUtils.autoImportFileScope();
        } else if (namespaceDeclaration != null) {
            return OptionsUtils.autoImportNamespaceScope();
        }
        return false;
    }

    private String getUseType() {
        String useType = CodeUtils.EMPTY_STRING;
        if (getKind() == ElementKind.CLASS || getKind() == ElementKind.CONSTRUCTOR) {
            useType = AutoImport.USE_TYPE;
        } else if (this instanceof ConstantItem) {
            useType = AutoImport.USE_CONST;
        } else if (this instanceof FunctionElementItem) {
            useType = AutoImport.USE_FUNCTION;
        }
        return useType;
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        if (element instanceof TypeMemberElement) {
            TypeMemberElement classMember = (TypeMemberElement) element;
            TypeElement type = classMember.getType();
            String name = type.getName();
            if (CodeUtils.isSyntheticTypeName(name)) {
                // anonymous class
                formatter.appendText("{}"); // NOI18N
                return formatter.getText();
            }
            QualifiedName qualifiedName = type.getNamespaceName();
            if (qualifiedName.isDefaultNamespace()) {
                formatter.appendText(name);
                return formatter.getText();
            } else {
                formatter.appendText(type.getFullyQualifiedName().toString());
                return formatter.getText();
            }
        }
        final String in = element.getIn();
        if (in != null && in.length() > 0) {
            formatter.appendText(in);
            return formatter.getText();
        } else if (element instanceof PhpElement) {
            PhpElement ie = (PhpElement) element;
            if (isPlatform) {
                return NbBundle.getMessage(PHPCompletionItem.class, "PHPPlatform");
            }

            String filename = ie.getFilenameUrl();
            int index = filename.lastIndexOf('/');
            if (index != -1) {
                filename = filename.substring(index + 1);
            }

            formatter.appendText(filename);
            return formatter.getText();
        }


        return null;
    }

    public static boolean insertOnlyParameterName(CompletionRequest request) {
        boolean result = false;
        TokenHierarchy<?> tokenHierarchy = request.result.getSnapshot().getTokenHierarchy();
        TokenSequence<PHPTokenId> tokenSequence = (TokenSequence<PHPTokenId>) tokenHierarchy.tokenSequence();
        if (tokenSequence != null) {
            tokenSequence.move(request.anchor);
            // na^me: -> only parameter name
            // n^ age: -> add also ":"
            while (tokenSequence.moveNext()) {
                Token<PHPTokenId> token = tokenSequence.token();
                PHPTokenId id = token.id();
                if (id == PHPTokenId.PHP_STRING) {
                    continue;
                }
                if (id == PHPTokenId.PHP_TOKEN
                        && TokenUtilities.textEquals(token.text(), ":")) { // NOI18N
                    result = true;
                    break;
                }
                break;
            }
        }
        return result;
    }

    public static boolean insertOnlyMethodsName(CompletionRequest request) {
        if (request.insertOnlyMethodsName != null) {
            return request.insertOnlyMethodsName;
        }
        if (isUseFunctionContext(request.context)) {
            // GH-7041
            // e.g. use function Vendor\Package\myFunction;
            return true;
        }
        boolean result = false;
        TokenHierarchy<?> tokenHierarchy = request.result.getSnapshot().getTokenHierarchy();
        TokenSequence<PHPTokenId> tokenSequence = (TokenSequence<PHPTokenId>) tokenHierarchy.tokenSequence();
        if (tokenSequence != null) {
            VariableScope variableScope = request.result.getModel().getVariableScope(request.anchor);
            if (variableScope != null) {
                tokenSequence = tokenSequence.subSequence(request.anchor, variableScope.getBlockRange().getEnd());
            }
            boolean wasWhitespace = false;
            while (tokenSequence.moveNext()) {
                Token<PHPTokenId> token = tokenSequence.token();
                PHPTokenId id = token.id();
                if (PHPTokenId.PHP_STRING.equals(id)) {
                    if (wasWhitespace) {
                        // this needs brackets: curl_set^ curl_setopt($ch, $option, $ch);
                        break;
                    } else {
                        // this doesn't need brackets: curl_setopt^  ($ch, $option, $ch);
                        continue;
                    }
                } else if (PHPTokenId.WHITESPACE.equals(id)) {
                    wasWhitespace = true;
                    continue;
                } else if (PHPTokenId.PHP_TOKEN.equals(id) && TokenUtilities.textEquals(token.text(), "(")) { //NOI18N
                    result = true;
                    break;
                } else {
                    break;
                }
            }
        }
        return result;
    }

    private static boolean isUseFunctionContext(CompletionContext context) {
        return context == CompletionContext.USE_FUNCTION_KEYWORD
                || context == CompletionContext.GROUP_USE_FUNCTION_KEYWORD;
    }

    private boolean isNewClassContext(CompletionContext context) {
        return context.equals(CompletionContext.NEW_CLASS)
                || context.equals(CompletionContext.THROW_NEW)
                || context.equals(CompletionContext.ATTRIBUTE);
    }

    private boolean isAutoImportContext(CompletionContext context) {
        return context != CompletionContext.GROUP_USE_KEYWORD
                && context != CompletionContext.GROUP_USE_FUNCTION_KEYWORD
                && context != CompletionContext.GROUP_USE_CONST_KEYWORD
                && context != CompletionContext.USE_KEYWORD
                && context != CompletionContext.USE_FUNCTION_KEYWORD
                && context != CompletionContext.USE_CONST_KEYWORD;
    }

    static class NewClassItem extends MethodElementItem {

        /**
         * @return more than one instance in case if optional parameters exists
         */
        static List<NewClassItem> getNewClassItems(final MethodElement methodElement, CompletionRequest request) {
            final List<NewClassItem> retval = new ArrayList<>();
            List<FunctionElementItem> items = FunctionElementItem.getItems(methodElement, request);
            for (FunctionElementItem functionElementItem : items) {
                retval.add(new NewClassItem(functionElementItem));
            }
            return retval;
        }

        private NewClassItem(FunctionElementItem function) {
            super(function);
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            ElementHandle element = getElement();
            if (element != null && element.getIn() != null) {
                String namespaceName = ((MethodElement) element).getType().getNamespaceName().toString();
                if (namespaceName != null && !NamespaceDeclarationInfo.DEFAULT_NAMESPACE_NAME.equals(namespaceName)) {
                    formatter.appendText(namespaceName);
                    return formatter.getText();
                }
            }
            return super.getRhsHtml(formatter);
        }

        @Override
        public String getName() {
            ElementHandle element = getElement();
            String in = element == null ? null : element.getIn();
            return (in != null) ? in : super.getName();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CONSTRUCTOR;
        }

        @Override
        public boolean isSmart() {
            return (getElement() instanceof AliasedElement) ? true : super.isSmart();
        }
    }

    public static class MethodElementItem extends FunctionElementItem {

        private final boolean completeAccessPrefix;

        /**
         * @return more than one instance in case if optional parameters exists
         */
        static List<MethodElementItem> getItems(final MethodElement methodElement, CompletionRequest request) {
            return getItems(methodElement, request, false);
        }

        /**
         * @return more than one instance in case if optional parameters exists
         */
        static List<MethodElementItem> getItems(final MethodElement methodElement, CompletionRequest request, boolean completeAccessPrefix) {
            final List<MethodElementItem> retval = new ArrayList<>();
            List<FunctionElementItem> items = FunctionElementItem.getItems(methodElement, request);
            for (FunctionElementItem functionElementItem : items) {
                retval.add(new MethodElementItem(functionElementItem, completeAccessPrefix));
            }
            return retval;
        }

        MethodElementItem(FunctionElementItem function) {
            this(function, false);
        }

        MethodElementItem(FunctionElementItem function, boolean completeAccessPrefix) {
            super(function.getBaseFunctionElement(), function.request, function.parameters, null, function.isFirstClassCallable);
            this.completeAccessPrefix = completeAccessPrefix;
        }

        @Override
        public String getCustomInsertTemplate() {
            String prefix = ""; // NOI18N
            if (completeAccessPrefix) {
                Set<Modifier> modifiers = getModifiers();
                prefix = modifiers.contains(Modifier.STATIC) ? "self::" : "$this->"; // NOI18N
            }
            return prefix + super.getCustomInsertTemplate();
        }
    }

    private static class ExistingVariableResolver {

        private final CompletionRequest request;
        private final int caretOffset;
        private final List<VariableName> usedVariables = new LinkedList<>();
        private static final RequestProcessor RP = new RequestProcessor("ExistingVariableResolver"); //NOI18N
        private static final Logger LOGGER = Logger.getLogger(ExistingVariableResolver.class.getName());
        private static final int RESOLVING_TIMEOUT = 300;

        public ExistingVariableResolver(CompletionRequest request) {
            this.request = request;
            caretOffset = request.anchor;
        }

        public ParameterElement resolveVariable(final ParameterElement param) {
            if (OptionsUtils.codeCompletionSmartParametersPreFilling()) {
                Future<VariableName> futureVariableToUse = RP.submit(new Callable<VariableName>() {

                    @Override
                    public VariableName call() throws Exception {
                        Collection<? extends VariableName> declaredVariables = getDeclaredVariables();
                        VariableName variableToUse = null;
                        if (declaredVariables != null) {
                            int oldOffset = 0;
                            for (VariableName variable : declaredVariables) {
                                if (!usedVariables.contains(variable) && !variable.representsThis()) {
                                    if (isPreviousVariable(variable)) {
                                        if (hasCorrectType(variable, param.getTypes())) {
                                            if (variable.getName().equals(param.getName())) {
                                                variableToUse = variable;
                                                break;
                                            }
                                            int newOffset = variable.getNameRange().getStart();
                                            if (newOffset > oldOffset) {
                                                oldOffset = newOffset;
                                                variableToUse = variable;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        return variableToUse;
                    }
                });
                VariableName variableToUseName = null;
                try {
                    variableToUseName = futureVariableToUse.get(RESOLVING_TIMEOUT, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.FINE, "Resolving of existing variables has been interrupted.");
                } catch (ExecutionException ex) {
                    LOGGER.log(Level.SEVERE, "Exception has been thrown during resolving of existing variables.", ex);
                } catch (TimeoutException ex) {
                    LOGGER.log(Level.FINE, "Timeout for resolving existing variables has been exceed: {0}", RESOLVING_TIMEOUT);
                }
                if (variableToUseName != null) {
                    usedVariables.add(variableToUseName);
                    return new ParameterElementImpl(
                            variableToUseName.getName(),
                            param.getDefaultValue(),
                            param.getOffset(),
                            param.getDeclaredType(),
                            param.getPhpdocType(),
                            param.getTypes(),
                            param.isMandatory(),
                            param.hasDeclaredType(),
                            param.isReference(),
                            param.isVariadic(),
                            param.isUnionType(),
                            param.getModifier(),
                            param.isIntersectionType()
                    );
                }
            }
            return param;
        }

        private Collection<? extends VariableName> getDeclaredVariables() {
            VariableScope variableScope = request.result.getModel().getVariableScope(caretOffset);
            if (variableScope != null) {
                return variableScope.getDeclaredVariables();
            }
            return null;
        }

        private boolean isPreviousVariable(VariableName variable) {
            int offsetDiff = caretOffset - variable.getNameRange().getStart();
            if (offsetDiff > 0) {
                return true;
            }
            return false;
        }

        private boolean hasCorrectType(VariableName variable, Set<TypeResolver> possibleTypes) {
            Collection<? extends String> typeNames = variable.getTypeNames(caretOffset);
            if (!typeNames.isEmpty()) {
                for (TypeResolver type : possibleTypes) {
                    if (typeNames.contains(type.getRawTypeName()) || Type.MIXED.equals(type.getRawTypeName())
                            || (typeNames.contains(Type.REAL) && Type.FLOAT.equals(type.getRawTypeName()))
                            || (typeNames.contains(Type.INT) && Type.INTEGER.equals(type.getRawTypeName()))) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    static class FunctionElementItem extends PHPCompletionItem {

        private List<ParameterElement> parameters;
        // NETBEANS-5599 PHP 8.1
        // https://wiki.php.net/rfc/first_class_callable_syntax
        // https://www.php.net/manual/en/functions.first_class_callable_syntax.php
        private final boolean isFirstClassCallable;

        /**
         * @return more than one instance in case if optional parameters exists
         */
        static List<FunctionElementItem> getItems(final BaseFunctionElement function, CompletionRequest request) {
            return getItems(function, request, null);
        }

        static List<FunctionElementItem> getItems(final BaseFunctionElement function, CompletionRequest request, QualifiedNameKind generateAs) {
            final List<FunctionElementItem> retval = new ArrayList<>();
            final List<ParameterElement> parameters = new ArrayList<>();
            for (ParameterElement param : function.getParameters()) {
                if (!param.isMandatory()) {
                    if (retval.isEmpty()) {
                        retval.add(new FunctionElementItem(function, request, parameters, generateAs));
                    }
                    parameters.add(param);
                    retval.add(new FunctionElementItem(function, request, parameters, generateAs));
                } else {
                    //assert retval.isEmpty():param.asString();
                    parameters.add(param);
                }
            }
            if (retval.isEmpty()) {
                retval.add(new FunctionElementItem(function, request, parameters, generateAs));
            }
            if (addFirstClassCallableItem(function)) {
                retval.add(createFirstClassCallableItem(function, request));
            }

            return retval;
        }

        static FunctionElementItem createFirstClassCallableItem(BaseFunctionElement function, CompletionRequest request) {
            return new FunctionElementItem(function, request, Collections.emptyList(), null, true);
        }

        private static boolean addFirstClassCallableItem(BaseFunctionElement function) {
            return !isConstructor(function)
                    && (OptionsUtils.codeCompletionFirstClassCallable()
                    || (ADD_FIRST_CLASS_CALLABLE != null && ADD_FIRST_CLASS_CALLABLE)); // for unit tests
        }

        private static boolean isConstructor(BaseFunctionElement function) {
            if (function instanceof MethodElement) {
                return ((MethodElement) function).isConstructor();
            }
            return false;
        }

        FunctionElementItem(BaseFunctionElement function, CompletionRequest request, List<ParameterElement> parameters) {
            this(function, request, parameters, null, false);
        }

        FunctionElementItem(BaseFunctionElement function, CompletionRequest request, List<ParameterElement> parameters, QualifiedNameKind generateAs) {
            this(function, request, parameters, generateAs, false);
        }

        FunctionElementItem(BaseFunctionElement function, CompletionRequest request, List<ParameterElement> parameters, QualifiedNameKind generateAs, boolean isFirstClassCallable) {
            super(function, request, generateAs);
            this.parameters = new ArrayList<>(parameters);
            this.isFirstClassCallable = isFirstClassCallable;
        }

        public BaseFunctionElement getBaseFunctionElement() {
            return (BaseFunctionElement) getElement();
        }

        @Override
        public ElementKind getKind() {
            return getBaseFunctionElement().getPhpElementKind().getElementKind();
        }

        @Override
        public String getInsertPrefix() {
            // used for filtering purposes
            return getName();
        }

        @Override
        public String getCustomInsertTemplate() {
            StringBuilder template = new StringBuilder();
            template.append(super.getInsertPrefix());
            if (!insertOnlyMethodsName(request)) {
                template.append("("); //NOI18N
                if (isFirstClassCallable) {
                    template.append(CodeUtils.ELLIPSIS); // ...
                }
                List<String> params = getInsertParams();
                for (int i = 0; i < params.size(); i++) {
                    String param = params.get(i);
                    if (param.startsWith("&")) { //NOI18N
                        param = param.substring(1);
                    }
                    template.append(String.format("${php-cc-%d  default=\"%s\"}", i, param)); // NOI18N

                    if (i < params.size() - 1) {
                        template.append(", "); //NOI18N
                    }
                }
                template.append(')');
            }
            return template.toString();
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            ElementKind kind = getKind();

            formatter.name(kind, true);
            if (emphasisName()) {
                formatter.emphasis(true);
                if (isDeprecated()) {
                    formatter.deprecated(true);
                    formatter.appendText(getName());
                    formatter.deprecated(false);
                } else {
                    formatter.appendText(getName());
                }
                formatter.emphasis(false);
            } else {
                if (isDeprecated()) {
                    formatter.deprecated(true);
                    formatter.appendText(getName());
                    formatter.deprecated(false);
                } else {
                    formatter.appendText(getName());
                }
            }

            formatter.name(kind, false);

            formatter.appendHtml("("); // NOI18N
            formatter.parameters(true);
            appendParamsStr(formatter);
            formatter.parameters(false);
            formatter.appendHtml(")"); // NOI18N

            return formatter.getText();
        }

        protected boolean emphasisName() {
            return true;
        }

        public List<String> getInsertParams() {
            List<String> insertParams = new LinkedList<>();
            final ExistingVariableResolver existingVariableResolver = new ExistingVariableResolver(request);
            for (ParameterElement parameter : parameters) {
                insertParams.add(existingVariableResolver.resolveVariable(parameter).getName());
            }
            return insertParams;
        }

        @Override
        public String getSortText() {
            if (isFirstClassCallable) {
                // put first-class callable syntax on the last position
                // e.g.
                // strlen($length)
                // strlen(...)
                return getName() + "99"; // NOI18N
            }
            return getName() + parameters.size();
        }

        private void appendParamsStr(HtmlFormatter formatter) {
            List<ParameterElement> allParameters = parameters;
            for (int i = 0; i < allParameters.size(); i++) {
                ParameterElement parameter = allParameters.get(i);
                if (i != 0) {
                    formatter.appendText(", "); // NOI18N
                }

                final String paramTpl = parameter.asString(OutputType.SHORTEN_DECLARATION);
                if (!parameter.isMandatory()) {
                    formatter.appendText(paramTpl);
                } else {
                    formatter.emphasis(true);
                    formatter.appendText(paramTpl);
                    formatter.emphasis(false);
                }
            }
            if (isFirstClassCallable) {
                formatter.appendText(CodeUtils.ELLIPSIS); // ...
            }
        }
    }

    static class BasicFieldItem extends PHPCompletionItem {

        private String typeName;

        public static BasicFieldItem getItem(PhpElement field, String type, CompletionRequest request) {
            return new BasicFieldItem(field, type, request);
        }

        private BasicFieldItem(PhpElement field, String typeName, CompletionRequest request) {
            super(field, request);
            this.typeName = typeName;
        }

        @Override
        public String getInsertPrefix() {
            Completion.get().showToolTip();
            return getName();
        }

        @Override
        public ElementKind getKind() {
            //TODO: variable just because originally VARIABLE was returned and thus all tests fail
            //return ElementKind.FIELD;
            return ElementKind.VARIABLE;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.type(true);
            formatter.appendText(getTypeName() == null ? "" : getTypeName()); //NOI18N
            formatter.type(false);
            formatter.appendText(" "); //NOI18N
            formatter.name(getKind(), true);
            if (isDeprecated()) {
                formatter.deprecated(true);
            }
            formatter.appendText(getName());
            if (isDeprecated()) {
                formatter.deprecated(false);
            }
            formatter.name(getKind(), false);
            return formatter.getText();
        }

        @Override
        public String getName() {
            ElementHandle element = getElement();
            assert element != null;
            final String name = element.getName();
            return name.startsWith("$") ? name.substring(1) : name;
        }

        /**
         * @return the typeName
         */
        protected String getTypeName() {
            return typeName;
        }
    }

    // Backed cases have an additional read-only property (value)
    // e.g. EnumName::CASE_NAME->value;
    // see https://www.php.net/manual/en/language.enumerations.backed.php
    static class AdditionalFieldItem extends BasicFieldItem {

        private final String fieldName;
        private final String fieldTypeName;
        private final String typeName;

        public static AdditionalFieldItem getItem(String fieldName, String fieldTypeName, String typeName, CompletionRequest request) {
            return new AdditionalFieldItem(fieldName, fieldTypeName, typeName, request);
        }

        private AdditionalFieldItem(String fieldName, String filedTypeName, String typeName, CompletionRequest request) {
            super(null, fieldName, request);
            this.fieldName = fieldName;
            this.fieldTypeName = filedTypeName;
            this.typeName = typeName;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            if (typeName != null) {
                formatter.appendText(typeName);
            }
            return formatter.getText();
        }

        @Override
        public String getName() {
            return fieldName;
        }

        @Override
        protected String getTypeName() {
            return fieldTypeName;
        }
    }

    static class FieldItem extends BasicFieldItem {
        private final boolean forceDollared;
        private final boolean completeAccessPrefix;

        public static FieldItem getItem(FieldElement field, CompletionRequest request) {
            return getItem(field, request, false);
        }

        public static FieldItem getItem(FieldElement field, CompletionRequest request, boolean forceDollared) {
            return new FieldItem(field, request, forceDollared, false);
        }

        public static FieldItem getItem(FieldElement field, CompletionRequest request, boolean forceDollared, boolean completeAccessPrefix) {
            return new FieldItem(field, request, forceDollared, completeAccessPrefix);
        }

        private FieldItem(FieldElement field, CompletionRequest request, boolean forceDollared, boolean completeAccessPrefix) {
            super(field, null, request);
            this.forceDollared = forceDollared;
            this.completeAccessPrefix = completeAccessPrefix;
        }

        FieldElement getField() {
            return (FieldElement) getElement();
        }

        @Override
        public String getName() {
            final FieldElement field = getField();
            return field.getName(forceDollared || field.isStatic());
        }

        @Override
        protected String getTypeName() {
            String declaredType = getField().getDeclaredType();
            if (declaredType != null) {
                return StringUtils.truncate(declaredType, 0, TYPE_NAME_MAX_LENGTH, CodeUtils.ELLIPSIS);
            }
            Set<TypeResolver> types = getField().getInstanceTypes();
            List<String> typeNames = new ArrayList<>();
            for (TypeResolver type : types) {
                String typeName = "?"; //NOI18N
                if (type.isResolved()) {
                    QualifiedName qualifiedName = type.getTypeName(false);
                    if (qualifiedName != null) {
                        typeName = qualifiedName.toString();
                        if (type.isNullableType()) {
                            typeName = CodeUtils.NULLABLE_TYPE_PREFIX + typeName;
                        }
                    }
                }
                typeNames.add(typeName);
            }
            String typeName;
            if (typeNames.isEmpty()) {
                typeName = "?"; // NOI18N
            } else if (typeNames.size() == 1) {
                typeName = typeNames.get(0);
            } else {
                if (getField().isUnionType()) {
                    typeName = StringUtils.implode(typeNames, Type.SEPARATOR);
                } else if (getField().isIntersectionType()) {
                    typeName = StringUtils.implode(typeNames, Type.SEPARATOR_INTERSECTION);
                } else {
                    typeName = StringUtils.implode(typeNames, Type.SEPARATOR);
                }
            }
            return StringUtils.truncate(typeName, 0, TYPE_NAME_MAX_LENGTH, CodeUtils.ELLIPSIS); // ...
        }

        @Override
        public String getCustomInsertTemplate() {
            if (completeAccessPrefix) {
                Set<Modifier> modifiers = getModifiers();
                String prefix = modifiers.contains(Modifier.STATIC) ? "self::" : "$this->"; // NOI18N
                return prefix + getName();
            }
            return super.getCustomInsertTemplate();
        }
    }

    static class TypeConstantItem extends PHPCompletionItem {

        private final boolean completeAccessPrefix;

        public static TypeConstantItem getItem(TypeConstantElement constant, CompletionRequest request) {
            return getItem(constant, request, false);
        }

        public static TypeConstantItem getItem(TypeConstantElement constant, CompletionRequest request, boolean completeAccessPrefix) {
            return new TypeConstantItem(constant, request, completeAccessPrefix);
        }

        private TypeConstantItem(TypeConstantElement constant, CompletionRequest request, boolean completeAccessPrefix) {
            super(constant, request);
            this.completeAccessPrefix = completeAccessPrefix;
        }

        TypeConstantElement getConstant() {
            return (TypeConstantElement) getElement();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CONSTANT;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.name(getKind(), true);
            if (isDeprecated()) {
                formatter.deprecated(true);
                formatter.appendText(getName());
                formatter.deprecated(false);
            } else {
                formatter.appendText(getName());
            }
            formatter.name(getKind(), false);
            formatter.appendText(" "); //NOI18N
            String value = getConstant().getValue();
            formatter.type(true);
            formatter.appendText(value != null ? value : "?"); //NOI18N
            formatter.type(false);

            return formatter.getText();
        }

        @Override
        public String getName() {
            return getConstant().getName();
        }

        @Override
        public String getInsertPrefix() {
            Completion.get().showToolTip();
            return getName();
        }

        @Override
        @NbBundle.Messages("MagicConstant=Magic Constant")
        public String getRhsHtml(HtmlFormatter formatter) {
            if (getConstant().isMagic()) {
                formatter.appendText(Bundle.MagicConstant());
                return formatter.getText();
            }
            return super.getRhsHtml(formatter);
        }

        @Override
        public String getCustomInsertTemplate() {
            if (completeAccessPrefix) {
                return "self::" + getName(); // NOI18N
            }
            return super.getCustomInsertTemplate();
        }
    }

    static class EnumCaseItem extends PHPCompletionItem {

        private final boolean completeAccessPrefix;

        public static EnumCaseItem getItem(EnumCaseElement constant, CompletionRequest request) {
            return getItem(constant, request, false);
        }

        public static EnumCaseItem getItem(EnumCaseElement constant, CompletionRequest request, boolean completeAccessPrefix) {
            return new EnumCaseItem(constant, request, completeAccessPrefix);
        }

        private EnumCaseItem(EnumCaseElement constant, CompletionRequest request, boolean completeAccessPrefix) {
            super(constant, request);
            this.completeAccessPrefix = completeAccessPrefix;
        }

        EnumCaseElement getEnumCase() {
            return (EnumCaseElement) getElement();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CONSTANT;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.name(getKind(), true);
            if (isDeprecated()) {
                formatter.deprecated(true);
                formatter.appendText(getName());
                formatter.deprecated(false);
            } else {
                formatter.appendText(getName());
            }
            formatter.name(getKind(), false);
            if (getEnumCase().isBacked()) {
                formatter.appendText(" "); //NOI18N
                String value = getEnumCase().getValue();
                formatter.type(true);
                formatter.appendText(value != null ? value : "?"); // NOI18N
                formatter.type(false);
            }

            return formatter.getText();
        }

        @Override
        public String getName() {
            return getEnumCase().getName();
        }

        @Override
        public String getInsertPrefix() {
            Completion.get().showToolTip();
            return getName();
        }

        @Override
        public String getCustomInsertTemplate() {
            if (completeAccessPrefix) {
                return "self::" + getName(); // NOI18N
            }
            return super.getCustomInsertTemplate();
        }

        @Override
        public ImageIcon getIcon() {
            return ENUM_CASE_ICON;
        }
    }

    public static class MethodDeclarationItem extends MethodElementItem {

        public static MethodDeclarationItem getDeclarationItem(final MethodElement methodElement, CompletionRequest request) {
            return new MethodDeclarationItem(new FunctionElementItem(methodElement, request, methodElement.getParameters()));
        }

        public static MethodDeclarationItem forIntroduceHint(final MethodElement methodElement, CompletionRequest request) {
            return new MethodDeclarationItem(new FunctionElementItem(methodElement, request, methodElement.getParameters())) {

                @Override
                protected String getFunctionBodyForTemplate() {
                    return "\n"; //NOI18N
                }
            };
        }

        public static MethodDeclarationItem forMethodName(final MethodElement methodElement, CompletionRequest request) {
            return new MethodDeclarationItem(new FunctionElementItem(methodElement, request, methodElement.getParameters())) {

                @Override
                public String getCustomInsertTemplate() {
                    return insertOnlyMethodsName(request) ? super.getInsertPrefix() : super.getNameAndFunctionBodyForTemplate();
                }
            };
        }

        public static MethodDeclarationItem forIntroduceInterfaceHint(final MethodElement methodElement, CompletionRequest request) {
            return new MethodDeclarationItem(new FunctionElementItem(methodElement, request, methodElement.getParameters())) {

                @Override
                protected String getBodyPart() {
                    return ";"; //NOI18N
                }
            };
        }

        private MethodDeclarationItem(FunctionElementItem functionItem) {
            super(functionItem);
        }

        public MethodElement getMethod() {
            return (MethodElement) getBaseFunctionElement();
        }

        @Override
        public boolean isSmart() {
            return !isMagic();
        }

        @Override
        protected boolean emphasisName() {
            return isMagic() ? false : super.emphasisName();
        }

        public boolean isMagic() {
            return ((MethodElement) getBaseFunctionElement()).isMagic();
        }

        @Override
        public String getCustomInsertTemplate() {
            StringBuilder template = new StringBuilder();
            String modifierStr = BodyDeclaration.Modifier.toString(getBaseFunctionElement().getFlags());
            template.append(getOverrideAttribute()); // PHP 8.3
            if (modifierStr.length() != 0) {
                modifierStr = modifierStr.replace("abstract", CodeUtils.EMPTY_STRING).trim(); //NOI18N
                template.append(modifierStr);
            }
            template.append(" ").append("function"); //NOI18N
            template.append(getNameAndFunctionBodyForTemplate());
            return template.toString();
        }

        private String getOverrideAttribute() {
            MethodElement method = (MethodElement) getBaseFunctionElement();
            TypeElement type = method.getType();
            if (!isMagic()
                    && (!type.isTrait() || ElementUtils.isAbstractTraitMethod(method))
                    && request != null) {
                FileObject fileObject = request.result.getSnapshot().getSource().getFileObject();
                PhpVersion phpVersion = getPhpVersion(fileObject);
                if (phpVersion.hasOverrideAttribute()) {
                    return OVERRIDE.asAttributeExpression() + CodeUtils.NEW_LINE;
                }
            }
            return CodeUtils.EMPTY_STRING;
        }

        protected String getNameAndFunctionBodyForTemplate() {
            FileObject fileObject = null;
            if (request != null) {
                fileObject = request.result.getSnapshot().getSource().getFileObject();
            }
            PhpVersion phpVersion = getPhpVersion(fileObject);
            StringBuilder template = new StringBuilder();
            TypeNameResolver typeNameResolver = getBaseFunctionElement().getParameters().isEmpty() || request == null
                    ? TypeNameResolverImpl.forNull()
                    : CodegenUtils.createSmarterTypeNameResolver(getBaseFunctionElement(), request.result.getModel(), request.anchor);
            template.append(getBaseFunctionElement().asString(PrintAs.NameAndParamsDeclaration, typeNameResolver, phpVersion));
            // #270237
            if (request != null) {
                // resquest is null if completion items are used in the IntroduceSuggestion hint
                if (phpVersion != null
                        && phpVersion.compareTo(PhpVersion.PHP_70) >= 0) {
                    Collection<TypeResolver> returnTypes = getBaseFunctionElement().getReturnTypes();
                    // we can also write a union type in phpdoc e.g. @return int|float
                    // check whether the union type is actual declared return type to avoid adding the union type for phpdoc
                    if (returnTypes.size() == 1
                            || getBaseFunctionElement().isReturnUnionType()
                            || getBaseFunctionElement().isReturnIntersectionType()) {
                        String returnType = getBaseFunctionElement().asString(PrintAs.ReturnTypes, typeNameResolver, phpVersion);
                        if (StringUtils.hasText(returnType)) {
                            boolean nullableType = CodeUtils.isNullableType(returnType);
                            if (nullableType) {
                                returnType = returnType.substring(1);
                            }
                            if ("\\self".equals(returnType) // NOI18N
                                    && getBaseFunctionElement() instanceof TypeMemberElement) {
                                returnType = ((TypeMemberElement) getBaseFunctionElement()).getType().getFullyQualifiedName().toString();
                            }
                            template.append(": "); // NOI18N
                            if (nullableType) {
                                template.append(CodeUtils.NULLABLE_TYPE_PREFIX);
                            }
                            template.append(returnType);
                        }
                    }
                }
            }
            template.append(getBodyPart());
            return template.toString();
        }

        protected String getBodyPart() {
            StringBuilder template = new StringBuilder();
            template.append(" ").append("{\n"); //NOI18N
            template.append(getFunctionBodyForTemplate()); //NOI18N
            template.append("}"); //NOI18N
            return template.toString();
        }

        /**
         * @return body or null
         */
        protected String getFunctionBodyForTemplate() {
            StringBuilder template = new StringBuilder();
            MethodElement method = (MethodElement) getBaseFunctionElement();
            TypeElement type = method.getType();
            Collection<TypeResolver> returnTypes = getBaseFunctionElement().getReturnTypes();
            if (ElementUtils.isToStringMagicMethod(method)) {
                template.append(getToStringMethodBody(type)).append("${cursor}\n"); // NOI18N
            } else if (isMagic() || type.isInterface() || method.isAbstract() || type.isTrait() || ElementUtils.isVoidOrNeverType(returnTypes)) {
                template.append("${cursor};\n"); //NOI18N
            } else {
                if (returnTypes.size() == 1 || getBaseFunctionElement().isReturnUnionType()) {
                    template.append("${cursor}return parent::").append(getSignature().replace("&$", "$")).append(";\n"); //NOI18N
                } else {
                    template.append("${cursor}parent::").append(getSignature().replace("&$", "$")).append(";\n"); //NOI18N
                }
            }
            return template.toString();
        }

        private String getToStringMethodBody(TypeElement type) {
            if (request != null) {
                return ElementUtils.getToStringMagicMethodBody(type, request.index);
            }
            return CodeUtils.EMPTY_STRING;
        }

        private String getSignature() {
            StringBuilder retval = new StringBuilder();
            retval.append(getBaseFunctionElement().getName());
            retval.append("(");
            StringBuilder parametersInfo = new StringBuilder();
            List<ParameterElement> parameters = getBaseFunctionElement().getParameters();
            for (ParameterElement parameter : parameters) {
                if (parametersInfo.length() > 0) {
                    parametersInfo.append(", "); //NOI18N
                }
                parametersInfo.append(parameter.getName());
            }
            retval.append(parametersInfo);
            retval.append(")"); //NOI18N
            return retval.toString();
        }

        @Override
        @NbBundle.Messages({
            "Generate=- generate",
            "Override=- override"
        })
        public String getLhsHtml(HtmlFormatter formatter) {
            StringBuilder sb = new StringBuilder();
            sb.append(super.getLhsHtml(formatter));
            sb.append(' ');
            if (getMethod().isAbstract() || isMagic()) {
                sb.append(Bundle.Generate());
            } else {
                sb.append(Bundle.Override());
            }
            return sb.toString();
        }

        @Override
        @NbBundle.Messages("MagicMethod=Magic Method")
        public String getRhsHtml(HtmlFormatter formatter) {
            if (isMagic()) {
                final String message = Bundle.MagicMethod();
                formatter.appendText(message);
                return formatter.getText();
            }
            return super.getRhsHtml(formatter);
        }
    }

    static class ClassScopeKeywordItem extends KeywordItem {

        private final String className;

        ClassScopeKeywordItem(final String className, final String keyword, final CompletionRequest request) {
            super(keyword, request);
            this.className = className;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            if (keyword.startsWith("$")) { //NOI18N
                if (className != null) {
                    formatter.type(true);
                    formatter.appendText(CodeUtils.isSyntheticTypeName(className) ? "{}" : className); // NOI18N
                    formatter.type(false);
                }
                formatter.appendText(" "); //NOI18N
            }
            return super.getLhsHtml(formatter);
        }
    }

    static class KeywordItem extends PHPCompletionItem {

        final String keyword;
        private static final List<String> CLS_KEYWORDS =
                Arrays.asList(PHPCodeCompletion.PHP_CLASS_KEYWORDS);

        KeywordItem(String keyword, CompletionRequest request) {
            super(null, request);
            assert keyword != null;
            this.keyword = keyword;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.name(getKind(), true);
            formatter.appendText(getName());
            formatter.name(getKind(), false);

            return formatter.getText();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return null;
        }

        @Override
        public ImageIcon getIcon() {
            return KEYWORD_ICON;
        }

        @Override
        public boolean isSmart() {
            return CLS_KEYWORDS.contains(getName()) ? true : super.isSmart();
        }

        @Override
        public String getInsertPrefix() {
            return getName();
        }

        @Override
        public String getCustomInsertTemplate() {
            StringBuilder builder = new StringBuilder();
            if (CLS_KEYWORDS.contains(getName())) {
                scheduleShowingCompletion();
            }
            KeywordCompletionType type = PHPCodeCompletion.PHP_KEYWORDS.get(getName());
            if (type == null) {
                return getName();
            }
            CodeStyle codeStyle = CodeStyle.get(EditorRegistry.lastFocusedComponent().getDocument());
            boolean appendSpace = true;
            String name;
            switch (type) {
                case SIMPLE:
                    return null;
                case ENDS_WITH_SPACE:
                    builder.append(getName());
                    builder.append(" ${cursor}"); //NOI18N
                    break;
                case CURSOR_INSIDE_BRACKETS:
                    name = getName();
                    builder.append(name);
                    boolean appendBrackets = true;
                    switch (name) {
                        case "foreach": //NOI18N
                        case "for": //NOI18N
                            appendSpace = codeStyle.spaceBeforeForParen();
                            break;
                        case "if": //NOI18N
                            appendSpace = codeStyle.spaceBeforeIfParen();
                            break;
                        case "switch": //NOI18N
                            appendSpace = codeStyle.spaceBeforeSwitchParen();
                            break;
                        case "array": //NOI18N
                            if (request.context == CompletionContext.TYPE_NAME
                                    || request.context == CompletionContext.VISIBILITY_MODIFIER_OR_TYPE_NAME
                                    || request.context == CompletionContext.RETURN_TYPE_NAME
                                    || request.context == CompletionContext.FIELD_TYPE_NAME
                                    || request.context == CompletionContext.CONST_TYPE_NAME) {
                                // e.g. return type
                                appendBrackets = false;
                                appendSpace = false;
                            } else {
                                appendSpace = codeStyle.spaceBeforeArrayDeclParen();
                            }
                            break;
                        case "while": //NOI18N
                            appendSpace = codeStyle.spaceBeforeWhileParen();
                            break;
                        case "catch": //NOI18N
                            appendSpace = codeStyle.spaceBeforeCatchParen();
                            break;
                        default:
                            // no-op
                    }
                    if (appendSpace) {
                        builder.append(" "); //NOI18N
                    }
                    if (appendBrackets) {
                        builder.append("(${cursor})"); //NOI18N
                    }
                    break;
                case ENDS_WITH_CURLY_BRACKETS:
                    name = getName();
                    builder.append(name);
                    switch (name) {
                        case "try": //NOI18N
                            appendSpace = codeStyle.spaceBeforeTryLeftBrace();
                            break;
                        case "do": //NOI18N
                            appendSpace = codeStyle.spaceBeforeDoLeftBrace();
                            break;
                        case "else": //NOI18N
                            appendSpace = codeStyle.spaceBeforeElseLeftBrace();
                            break;
                        default:
                            // no-op
                    }
                    if (appendSpace) {
                        builder.append(" "); //NOI18N
                    }
                    builder.append("{${cursor}}"); //NOI18N
                    if ("try".equals(name)) { //NOI18N
                        builder.append("catch (Exception $ex) {}"); //NOI18N
                    }
                    break;
                case ENDS_WITH_BRACKETS_AND_CURLY_BRACKETS:
                    name = getName();
                    builder.append(name);
                    if (name.equals("elseif")) { //NOI18N
                        appendSpace = codeStyle.spaceBeforeIfParen();
                    }
                    if (appendSpace) {
                        builder.append(" "); //NOI18N
                    }
                    builder.append("(${cursor})"); //NOI18N
                    if (name.equals("elseif")) { //NOI18N
                        appendSpace = codeStyle.spaceBeforeIfLeftBrace();
                    }
                    if (appendSpace) {
                        builder.append(" "); //NOI18N
                    }
                    builder.append("{}"); //NOI18N
                    break;
                case ENDS_WITH_SEMICOLON:
                    builder.append(getName());
                    CharSequence text = request.info.getSnapshot().getText();
                    int index = request.anchor + request.prefix.length();
                    if (index == text.length() || ';' != text.charAt(index)) { //NOI18N
                        builder.append(";"); //NOI18N
                    }
                    break;
                case ENDS_WITH_COLON:
                    builder.append(getName());
                    builder.append(" ${cursor}:"); //NOI18N
                    break;
                case CURSOR_BEFORE_ENDING_SEMICOLON:
                    builder.append(getName());
                    builder.append(" ${cursor};"); //NOI18N
                    break;
                default:
                    assert false : type.toString();
                    break;
            }
            return builder.toString();
        }
    }

    static class SuperGlobalItem extends PHPCompletionItem {

        private String name;

        public SuperGlobalItem(CompletionRequest request, String name) {
            super(new PredefinedSymbolElement(name), request);
            this.name = name;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.name(getKind(), true); // <b> is added, so don't call formatter.emphasis(true)
            formatter.appendText(getName());
            formatter.name(getKind(), false); // </b> is added

            return formatter.getText();
        }

        @Override
        public String getName() {
            return "$" + name; //NOI18N
        }

        @Override
        public String getInsertPrefix() {
            //todo insert array brackets for array vars
            return getName();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.VARIABLE;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            formatter.appendText(NbBundle.getMessage(PHPCompletionItem.class, "PHPPlatform"));
            return formatter.getText();
        }

        public String getDocumentation() {
            return null;
        }

        @Override
        public ImageIcon getIcon() {
            return KEYWORD_ICON;
        }
    }

    static class NamespaceItem extends PHPCompletionItem {

        private Boolean isSmart;

        NamespaceItem(NamespaceElement namespace, CompletionRequest request, QualifiedNameKind generateAs) {
            super(namespace, request, generateAs);
        }

        @Override
        public String getInsertPrefix() {
            // used for filtering purposes
            return getName();
        }

        @Override
        public String getCustomInsertTemplate() {
            return super.getInsertPrefix();
        }

        @Override
        public int getSortPrioOverride() {
            return isSmart() ? -10001 : super.getSortPrioOverride();
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.name(getKind(), true);
            formatter.appendText(getName());
            formatter.name(getKind(), false);

            return formatter.getText();
        }

        @Override
        public String getName() {
            return getNamespaceElement().getName();
        }

        NamespaceElement getNamespaceElement() {
            return (NamespaceElement) getElement();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.PACKAGE;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            QualifiedName namespaceName = getNamespaceElement().getNamespaceName();
            if (namespaceName != null && !namespaceName.isDefaultNamespace()) {
                formatter.appendText(namespaceName.toString());
                return formatter.getText();
            }

            return null;
        }

        @Override
        public boolean isSmart() {
            if (isSmart == null && getElement() instanceof AliasedElement) {
                isSmart = true;
            }
            if (isSmart == null) {
                QualifiedName namespaceName = getNamespaceElement().getNamespaceName();
                isSmart = !(namespaceName == null || !namespaceName.isDefaultNamespace());
                if (!isSmart) {
                    FileScope fileScope = request.result.getModel().getFileScope();
                    NamespaceScope namespaceScope = (fileScope != null)
                            ? ModelUtils.getNamespaceScope(fileScope, request.anchor) : null;
                    if (namespaceScope != null) {
                        NamespaceElement ifq = getNamespaceElement();
                        LinkedList<String> segments = ifq.getFullyQualifiedName().getSegments();
                        QualifiedName fqna = QualifiedName.create(false, segments);
                        Collection<QualifiedName> relativeUses = VariousUtils.getRelativesToUses(namespaceScope, fqna);
                        for (QualifiedName qualifiedName : relativeUses) {
                            if (qualifiedName.getSegments().size() == 1) {
                                isSmart = true;
                                break;
                            }
                        }
                        if (!isSmart) {
                            relativeUses = VariousUtils.getRelativesToNamespace(namespaceScope, fqna);
                            for (QualifiedName qualifiedName : relativeUses) {
                                if (qualifiedName.getSegments().size() == 1) {
                                    isSmart = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            return isSmart;
        }
    }

    static class ConstantItem extends PHPCompletionItem {

        ConstantItem(ConstantElement constant, CompletionRequest request) {
            this(constant, request, null);
        }

        ConstantItem(ConstantElement constant, CompletionRequest request, QualifiedNameKind generateAs) {
            super(constant, request, generateAs);
        }

        @Override
        public String getName() {
            String name = super.getName();
            // #235450 TRUE, FALSE and NULL are defined with uppercase letters by default
            if (isPlatform()) {
                if ("TRUE".equals(name) || "FALSE".equals(name) || "NULL".equals(name)) { // NOI18N
                    if (OptionsUtils.autoCompletionUseLowercaseTrueFalseNull()) {
                        // default option is true
                        return super.getName().toLowerCase();
                    }
                }
            }
            return super.getName();
        }

        @Override
        public String getCustomInsertTemplate() {
            return super.getInsertPrefix();
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            ElementHandle element = getElement();
            assert element != null;
            String value = ((ConstantElement) element).getValue();
            formatter.name(getKind(), true);
            if (emphasisName()) {
                formatter.emphasis(true);
                formatter.appendText(getName());
                formatter.emphasis(false);
            } else {
                formatter.appendText(getName());
            }
            formatter.name(getKind(), false);
            formatter.appendText(" "); //NOI18N
            formatter.type(true);
            formatter.appendText(value != null ? value : "?"); //NOI18N
            formatter.type(false);

            return formatter.getText();
        }

        protected boolean emphasisName() {
            return true;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CONSTANT;
        }
    }

    static class TraitItem extends PHPCompletionItem {

        private static final ImageIcon ICON = IconsUtils.getElementIcon(PhpElementKind.TRAIT);

        TraitItem(TraitElement element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        public ImageIcon getIcon() {
            return ICON;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CLASS;
        }

        @Override
        public String getCustomInsertTemplate() {
            return super.getInsertPrefix();
        }
    }

    static class ClassItem extends PHPCompletionItem {

        private boolean endWithDoubleColon;

        ClassItem(ClassElement clazz, CompletionRequest request, boolean endWithDoubleColon, QualifiedNameKind generateAs) {
            super(clazz, request, generateAs);
            this.endWithDoubleColon = endWithDoubleColon;
        }

        @Override
        public ElementKind getKind() {
            if (request.context == CompletionContext.ATTRIBUTE) {
                return ElementKind.CONSTRUCTOR;
            }
            return ElementKind.CLASS;
        }

        @Override
        public String getInsertPrefix() {
            return getName();
        }

        @Override
        public String getCustomInsertTemplate() {
            final String superTemplate = super.getInsertPrefix();
            if (endWithDoubleColon) {
                StringBuilder builder = new StringBuilder();
                builder.append(superTemplate);
                boolean includeDoubleColumn = true;
                if (EditorRegistry.lastFocusedComponent() != null) {
                    Document doc = EditorRegistry.lastFocusedComponent().getDocument();
                    int caret = EditorRegistry.lastFocusedComponent().getCaretPosition();
                    try {
                        if (caret + 2 < doc.getLength() && "::".equals(doc.getText(caret, 2))) { //NOI18N
                            includeDoubleColumn = false;
                        }
                    } catch (BadLocationException ex) {
                        // do nothing
                    }
                }

                if (includeDoubleColumn) {
                    builder.append("::");
                }
                builder.append("${cursor}"); //NOI18N
                scheduleShowingCompletion();
                return builder.toString();
            } else if (CompletionContext.NEW_CLASS.equals(request.context)) {
                scheduleShowingCompletion();
            }
            return superTemplate;
        }
    }

    static class EnumItem extends PHPCompletionItem {

        private static final ImageIcon ICON = IconsUtils.getElementIcon(PhpElementKind.ENUM);
        private boolean endWithDoubleColon;

        EnumItem(EnumElement enumElement, CompletionRequest request, boolean endWithDoubleColon, QualifiedNameKind generateAs) {
            super(enumElement, request, generateAs);
            this.endWithDoubleColon = endWithDoubleColon;
        }

        @Override
        public ImageIcon getIcon() {
            return ICON;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CLASS;
        }

        @Override
        public String getInsertPrefix() {
            return getName();
        }

        @Override
        public String getCustomInsertTemplate() {
            final String superTemplate = super.getInsertPrefix();
            if (endWithDoubleColon) {
                StringBuilder builder = new StringBuilder();
                builder.append(superTemplate);
                boolean includeDoubleColumn = true;
                if (EditorRegistry.lastFocusedComponent() != null) {
                    Document doc = EditorRegistry.lastFocusedComponent().getDocument();
                    int caret = EditorRegistry.lastFocusedComponent().getCaretPosition();
                    try {
                        if (caret + 2 < doc.getLength() && "::".equals(doc.getText(caret, 2))) { //NOI18N
                            includeDoubleColumn = false;
                        }
                    } catch (BadLocationException ex) {
                        // do nothing
                    }
                }

                if (includeDoubleColumn) {
                    builder.append("::"); // NOI18N
                }
                builder.append("${cursor}"); //NOI18N
                scheduleShowingCompletion();
                return builder.toString();
            }
            return superTemplate;
        }
    }

    public static ImageIcon getInterfaceIcon() {
        return InterfaceItem.icon();
    }

    static class InterfaceItem extends PHPCompletionItem {

        private static final String PHP_INTERFACE_ICON = "org/netbeans/modules/php/editor/resources/interface.png"; //NOI18N
        private static ImageIcon interfaceIcon = null;
        private final boolean endWithDoubleColon;

        InterfaceItem(InterfaceElement iface, CompletionRequest request, boolean endWithDoubleColon) {
            super(iface, request);
            this.endWithDoubleColon = endWithDoubleColon;
        }

        InterfaceItem(InterfaceElement iface, CompletionRequest request, QualifiedNameKind generateAs, boolean endWithDoubleColon) {
            super(iface, request, generateAs);
            this.endWithDoubleColon = endWithDoubleColon;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CLASS;
        }

        private static ImageIcon icon() {
            if (interfaceIcon == null) {
                interfaceIcon = new ImageIcon(ImageUtilities.loadImage(PHP_INTERFACE_ICON));
            }
            return interfaceIcon;
        }

        @Override
        public ImageIcon getIcon() {
            return icon();
        }

        @Override
        public String getInsertPrefix() {
            return getName();
        }

        @Override
        public String getCustomInsertTemplate() {
            final String superTemplate = super.getInsertPrefix();
            if (endWithDoubleColon) {
                StringBuilder builder = new StringBuilder();
                builder.append(superTemplate);
                builder.append("::${cursor}"); //NOI18N
                scheduleShowingCompletion();
                return builder.toString();
            }
            return superTemplate;
        }
    }

    static class VariableItem extends PHPCompletionItem {

        VariableItem(VariableElement variable, CompletionRequest request) {
            super(variable, request);
        }

        VariableElement getVariable() {
            return (VariableElement) getElement();
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.type(true);
            formatter.appendText(getTypeName());
            formatter.type(false);
            formatter.appendText(" "); //NOI18N
            formatter.name(getKind(), true);
            formatter.appendText(getName());
            formatter.name(getKind(), false);

            return formatter.getText();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.VARIABLE;
        }

        @Override
        public String getInsertPrefix() {
            Completion.get().showToolTip();
            return getName();
        }

        protected String getTypeName() {
            Set<TypeResolver> types = getVariable().getInstanceTypes();
            String typeName = types.isEmpty() ? "?" : types.size() > 1 ? Type.MIXED : "?"; //NOI18N
            if (types.size() == 1) {
                TypeResolver typeResolver = types.iterator().next();
                if (typeResolver.isResolved()) {
                    QualifiedName qualifiedName = typeResolver.getTypeName(false);
                    if (qualifiedName != null) {
                        if (CodeUtils.isSyntheticTypeName(qualifiedName.getName())) {
                            // anonymous class
                            typeName = "{}"; // NOI18N
                        } else {
                            typeName = qualifiedName.toString();
                        }
                    }
                }
            }
            return typeName;
        }
    }

    @NbBundle.Messages("LBL_PARAMETER_NAME=Parameter Name")
    static class ParameterNameItem extends PHPCompletionItem {

        private final ParameterElement parameterElement;

        public ParameterNameItem(ParameterElement parameterElement, CompletionRequest request) {
            super(null, request);
            this.parameterElement = parameterElement;
        }

        @Override
        public String getName() {
            return getParameterName() + ":"; // NOI18N
        }

        private String getParameterName() {
            return parameterElement.getName().substring(1);
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.PARAMETER;
        }

        public ParameterElement getParameterElement() {
            return parameterElement;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.name(getKind(), true);
            formatter.appendText(getName());
            formatter.name(getKind(), false);
            return formatter.getText();
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return Bundle.LBL_PARAMETER_NAME();
        }

        @Override
        public boolean isSmart() {
            return true;
        }

        @Override
        public int getSortPrioOverride() {
            // NamespaceItem can be -10001
            return -10010;
        }

        @Override
        public String getInsertPrefix() {
            return getName();
        }

        @Override
        public String getCustomInsertTemplate() {
            if (insertOnlyParameterName(request)) {
                // ^maxLength: (^: caret)
                // we are unsure whether a user expects to add or override an item in this context
                // so, just add a name (i.e. don't add ":")
                return getParameterName();
            }
            return getName() + " "; // NOI18N
        }
    }

    @NbBundle.Messages("LBL_LANGUAGE_CONSTRUCT=Language Construct")
    abstract static class LanguageConstructItem extends KeywordItem {

        private static final String SORT_AFTER_KEYWORDS = "z"; // NOI18N

        public LanguageConstructItem(String fncName, CompletionRequest request) {
            super(fncName, request);
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            formatter.appendText(Bundle.LBL_LANGUAGE_CONSTRUCT());
            return formatter.getText();
        }

        @Override
        public String getSortText() {
            return SORT_AFTER_KEYWORDS + super.getSortText();
        }

        protected void prependName(HtmlFormatter formatter) {
            formatter.name(getKind(), true);
            formatter.appendText(getName());
            formatter.name(getKind(), false);
        }
    }

    static class LanguageConstructWithQuotesItem extends LanguageConstructItem {

        public LanguageConstructWithQuotesItem(String fncName, CompletionRequest request) {
            super(fncName, request);
        }

        @Override
        public String getCustomInsertTemplate() {
            StringBuilder builder = new StringBuilder();
            builder.append(getName());
            builder.append(" '${cursor}';"); // NOI18N
            return builder.toString();
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            prependName(formatter);
            formatter.appendText(" '';"); // NOI18N
            return formatter.getText();
        }
    }

    static class LanguageConstructWithParenthesesItem extends LanguageConstructItem {

        public LanguageConstructWithParenthesesItem(String fncName, CompletionRequest request) {
            super(fncName, request);
        }

        @Override
        public String getCustomInsertTemplate() {
            StringBuilder builder = new StringBuilder();
            builder.append(getName());
            builder.append("(${cursor})"); // NOI18N
            return builder.toString();
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            prependName(formatter);
            formatter.appendText("()"); // NOI18N
            return formatter.getText();
        }
    }

    static class LanguageConstructWithSemicolonItem extends LanguageConstructItem {

        public LanguageConstructWithSemicolonItem(String fncName, CompletionRequest request) {
            super(fncName, request);
        }

        @Override
        public String getCustomInsertTemplate() {
            StringBuilder builder = new StringBuilder();
            builder.append(getName());
            builder.append(" ${cursor};"); // NOI18N
            return builder.toString();
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            prependName(formatter);
            formatter.appendText(" ;"); // NOI18N
            return formatter.getText();
        }
    }

    static class TagItem extends KeywordItem {

        private int sortKey;

        public TagItem(String tag, int sortKey, CompletionRequest request) {
            super(tag, request);
            this.sortKey = sortKey;
        }

        @Override
        public String getSortText() {
            return "" + sortKey + getName();
        }
    }

    public static class CompletionRequest {

        public int anchor;
        public PHPParseResult result;
        public ParserResult info;
        public String prefix;
        // used in special cases (e.g. in group use)
        public String extraPrefix;
        // whether to complete '()' (default: null == autodetection)
        public Boolean insertOnlyMethodsName;
        public String currentlyEditedFileURL;
        public CompletionContext context;
        ElementQuery.Index index;
    }

    private static void scheduleShowingCompletion() {
        if (OptionsUtils.autoCompletionTypes()) {
            service.schedule(new Runnable() {

                @Override
                public void run() {
                    Completion.get().showCompletion();
                }
            }, 750, TimeUnit.MILLISECONDS);
        }
    }

    private static class PhpVersionChangeListener implements PropertyChangeListener {

        private final WeakReference<FileObject> fileObjectReference;

        public PhpVersionChangeListener(FileObject fileObject) {
            this.fileObjectReference = new WeakReference<>(fileObject);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (PhpLanguageProperties.PROP_PHP_VERSION.equals(evt.getPropertyName())) {
                FileObject fileObject = fileObjectReference.get();
                if (fileObject != null) {
                    PROPERTIES_CACHE.save(fileObject, PhpLanguageProperties.forFileObject(fileObject));
                }
            }
        }
    }
}
