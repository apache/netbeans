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
package org.netbeans.modules.php.editor.verification;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import static org.netbeans.modules.php.editor.PredefinedSymbols.Attributes.OVERRIDE;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement.PrintAs;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeNameResolver;
import org.netbeans.modules.php.editor.elements.TypeNameResolverImpl;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.EnumScope;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 * @author Radek Matous, Ondrej Brejla
 */
public class ImplementAbstractMethodsHintError extends HintErrorRule {

    private static final String ABSTRACT_PREFIX = "abstract "; //NOI18N
    private static final Logger LOGGER = Logger.getLogger(ImplementAbstractMethodsHintError.class.getName());

    @Override
    @Messages("ImplementAbstractMethodsDispName=Implement All Abstract Methods")
    public String getDisplayName() {
        return Bundle.ImplementAbstractMethodsDispName();
    }

    @Override
    @Messages({
        "ImplementAbstractMethodsHintError.class.anonymous=Anonymous class",
        "# {0} - Class name",
        "# {1} - Abstract method name",
        "# {2} - Owner (class) of abstract method",
        "ImplementAbstractMethodsHintDesc={0} is not abstract and does not override abstract method {1} in {2}"
    })
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        FileScope fileScope = context.fileScope;
        FileObject fileObject = context.parserResult.getSnapshot().getSource().getFileObject();
        if (fileScope != null && fileObject != null) {
            Collection<? extends ClassScope> allClasses = ModelUtils.getDeclaredClasses(fileScope);
            addHints(allClasses, context, hints, fileObject);
            Collection<? extends EnumScope> allEnums = ModelUtils.getDeclaredEnums(fileScope);
            addHints(allEnums, context, hints, fileObject);
        }
    }

    private void addHints(Collection<? extends TypeScope> allClasses, PHPRuleContext context, List<Hint> hints, FileObject fileObject) {
        for (FixInfo fixInfo : checkHints(allClasses, context)) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            final String className;
            if (fixInfo.anonymousClass) {
                className = Bundle.ImplementAbstractMethodsHintError_class_anonymous();
            } else {
                className = fixInfo.className;
            }
            hints.add(new Hint(
                    ImplementAbstractMethodsHintError.this,
                    Bundle.ImplementAbstractMethodsHintDesc(className, fixInfo.lastMethodDeclaration, fixInfo.lastMethodOwnerName),
                    fileObject,
                    fixInfo.classNameRange,
                    createHintFixes(context.doc, fixInfo),
                    500));
        }
    }

    // for unit tests
    protected PhpVersion getPhpVersion(@NullAllowed FileObject file) {
        if (file == null) {
            return PhpVersion.getDefault();
        }
        return CodeUtils.getPhpVersion(file);
    }

    private List<HintFix> createHintFixes(BaseDocument doc, FixInfo fixInfo) {
        List<HintFix> hintFixes = new ArrayList<>();
        hintFixes.add(new ImplementAllFix(doc, fixInfo));
        if (!fixInfo.anonymousClass && !fixInfo.isEnum) {
            hintFixes.add(new AbstractClassFix(doc, fixInfo));
        }
        return Collections.unmodifiableList(hintFixes);
    }

    private Collection<FixInfo> checkHints(Collection<? extends TypeScope> allTypes, PHPRuleContext context) {
        List<FixInfo> retval = new ArrayList<>();
        final PhpVersion phpVersion = getPhpVersion(context.parserResult.getSnapshot().getSource().getFileObject());
        for (TypeScope typeScope : allTypes) {
            if (CancelSupport.getDefault().isCancelled()) {
                return Collections.emptyList();
            }
            if (!isAbstract(typeScope)) {
                Index index = context.getIndex();
                Set<String> allValidMethods = new HashSet<>();
                allValidMethods.addAll(toNames(getValidInheritedMethods(getInheritedMethods(typeScope, index))));
                allValidMethods.addAll(toNames(index.getDeclaredMethods(typeScope)));
                ElementFilter declaredMethods = ElementFilter.forExcludedNames(allValidMethods, PhpElementKind.METHOD);
                List<MethodElement> accessibleMethods = new ArrayList<>(declaredMethods.filter(index.getAccessibleMethods(typeScope, typeScope)));
                // sort to get the same result
                accessibleMethods.sort((MethodElement m1, MethodElement m2) -> {
                    int result = m1.getFilenameUrl().compareTo(m2.getFilenameUrl());
                    if (result == 0) {
                        return Integer.compare(m1.getOffset(), m2.getOffset());
                    }
                    return result;
                });
                Set<String> methodSkeletons = new LinkedHashSet<>();
                MethodElement lastMethodElement = null;
                FileObject lastFileObject = null;
                FileScope fileScope = null;
                for (MethodElement methodElement : accessibleMethods) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return Collections.emptyList();
                    }
                    final TypeElement type = methodElement.getType();
                    if ((type.isInterface() || methodElement.isAbstract()) && !methodElement.isFinal()) {
                        FileObject fileObject = methodElement.getFileObject();
                        if (lastFileObject != fileObject
                                && fileObject != null) {
                            lastFileObject = fileObject;
                            fileScope = getFileScope(fileObject);
                        }
                        if (fileScope != null) {
                            NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(fileScope, methodElement.getOffset());
                            List typeNameResolvers = new ArrayList<>();
                            if (phpVersion == PhpVersion.PHP_5) {
                                typeNameResolvers.add(TypeNameResolverImpl.forUnqualifiedName());
                            } else {
                                typeNameResolvers.add(TypeNameResolverImpl.forFullyQualifiedName(namespaceScope, methodElement.getOffset()));
                                typeNameResolvers.add(TypeNameResolverImpl.forSmartName(typeScope, typeScope.getOffset()));
                            }
                            TypeNameResolver typeNameResolver = TypeNameResolverImpl.forChainOf(typeNameResolvers);
                            String skeleton = methodElement.asString(PrintAs.DeclarationWithEmptyBody, typeNameResolver, phpVersion);
                            if (phpVersion.hasOverrideAttribute()) {
                                skeleton = OVERRIDE.asAttributeExpression() + CodeUtils.NEW_LINE + skeleton; // PHP 8.3
                            }
                            skeleton = skeleton.replace(ABSTRACT_PREFIX, CodeUtils.EMPTY_STRING);
                            methodSkeletons.add(skeleton);
                            lastMethodElement = methodElement;
                        }
                    }
                }
                if (!methodSkeletons.isEmpty() && lastMethodElement != null) {
                    int classDeclarationOffset = getClassDeclarationOffset(context.parserResult.getSnapshot().getTokenHierarchy(), typeScope.getOffset());
                    int newMethodsOffset = getNewMethodsOffset(typeScope, context.doc, classDeclarationOffset);
                    if (newMethodsOffset != -1 && classDeclarationOffset != -1) {
                        retval.add(new FixInfo(typeScope, methodSkeletons, lastMethodElement, newMethodsOffset, classDeclarationOffset, isAnonymous(typeScope)));
                    }
                }
            }
        }
        return retval;
    }

    private boolean isAbstract(TypeScope typeScope) {
        if (typeScope instanceof ClassScope) {
            return ((ClassScope) typeScope).isAbstract();
        }
        return false;
    }

    private boolean isAnonymous(TypeScope typeScope) {
        if (typeScope instanceof ClassScope) {
            return ((ClassScope) typeScope).isAnonymous();
        }
        return false;
    }

    private FileScope getFileScope(final FileObject fileObject) {
        final FileScope[] fileScope = new FileScope[1];
        try {
            ParserManager.parse(Collections.singletonList(Source.create(fileObject)), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Result parserResult = resultIterator.getParserResult();
                    PHPParseResult phpResult = (PHPParseResult) parserResult;
                    fileScope[0] = phpResult.getModel().getFileScope();
                }
        });
        } catch (ParseException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return fileScope[0];
    }

    private Set<MethodElement> getInheritedMethods(final TypeScope typeScope, final Index index) {
        Set<MethodElement> inheritedMethods = new HashSet<>();
        Set<MethodElement> declaredSuperMethods = new HashSet<>();
        Set<MethodElement> accessibleSuperMethods = new HashSet<>();
        Collection<? extends ClassScope> superClasses = getSuperClasses(typeScope);
        for (ClassScope cls : superClasses) {
            declaredSuperMethods.addAll(index.getDeclaredMethods(cls));
            accessibleSuperMethods.addAll(index.getAccessibleMethods(cls, typeScope));
        }
        Collection<? extends InterfaceScope> superInterface = typeScope.getSuperInterfaceScopes();
        for (InterfaceScope interfaceScope : superInterface) {
            declaredSuperMethods.addAll(index.getDeclaredMethods(interfaceScope));
            accessibleSuperMethods.addAll(index.getAccessibleMethods(interfaceScope, typeScope));
        }
        Collection<? extends TraitScope> traits = getTraits(typeScope);
        for (TraitScope traitScope : traits) {
            declaredSuperMethods.addAll(index.getDeclaredMethods(traitScope));
            accessibleSuperMethods.addAll(index.getAccessibleMethods(traitScope, typeScope));
        }
        inheritedMethods.addAll(declaredSuperMethods);
        inheritedMethods.addAll(accessibleSuperMethods);
        return inheritedMethods;
    }

    private Collection<? extends ClassScope> getSuperClasses(TypeScope typeScope) {
        if (typeScope instanceof ClassScope) {
            return ((ClassScope) typeScope).getSuperClasses();
        }
        return Collections.emptyList();
    }

    private Collection<? extends TraitScope> getTraits(TypeScope typeScope) {
        if (typeScope instanceof ClassScope) {
            return ((ClassScope) typeScope).getTraits();
        } else if (typeScope instanceof EnumScope) {
            return ((EnumScope) typeScope).getTraits();
        }
        return Collections.emptyList();
    }

    private Set<MethodElement> getValidInheritedMethods(Set<MethodElement> inheritedMethods) {
        Set<MethodElement> retval = new HashSet<>();
        for (MethodElement methodElement : inheritedMethods) {
            if (!methodElement.isAbstract()) {
                retval.add(methodElement);
            }
        }
        return retval;
    }

    private static Set<String> toNames(Set<? extends PhpElement> elements) {
        Set<String> names = new HashSet<>();
        for (PhpElement elem : elements) {
            String name = elem.getName();
            if (!StringUtils.isEmpty(name)) {
                names.add(name);
            }
        }
        return names;
    }

    private static int getClassDeclarationOffset(TokenHierarchy<?> th, int classNameOffset) {
        TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(th, classNameOffset);
        ts.move(classNameOffset);
        ts.movePrevious();
        Token<? extends PHPTokenId> previousToken = LexUtilities.findPreviousToken(ts, Collections.<PHPTokenId>singletonList(PHPTokenId.PHP_CLASS));
        return previousToken.offset(th);
    }

    private static int getNewMethodsOffset(TypeScope typeScope, BaseDocument doc, int classDeclarationOffset) {
        int offset = -1;
        Collection<? extends MethodScope> declaredMethods = typeScope.getDeclaredMethods();
        for (MethodScope methodScope : declaredMethods) {
            OffsetRange blockRange = methodScope.getBlockRange();
            if (blockRange != null && blockRange.getEnd() > offset) {
                offset = blockRange.getEnd();
            }
        }
        if (offset == -1 && typeScope.getBlockRange() != null) {
            try {
                int rowStartOfClassEnd = LineDocumentUtils.getLineStart(doc, typeScope.getBlockRange().getEnd());
                int rowEndOfPreviousRow = rowStartOfClassEnd - 1;

                // #254173 the previous row may have something to break the code
                // e.g. "{" for the class declaration, a field accross multiple lines
                int rowStartOfPreviousRow = LineDocumentUtils.getLineStart(doc, rowEndOfPreviousRow);
                int newMethodPossibleOffset = rowStartOfPreviousRow < rowEndOfPreviousRow ? rowStartOfClassEnd : rowStartOfPreviousRow;

                int newMethodLineOffset = LineDocumentUtils.getLineIndex(doc, newMethodPossibleOffset);
                int classDeclarationLineOffset = LineDocumentUtils.getLineIndex(doc, classDeclarationOffset);
                if (newMethodLineOffset == classDeclarationLineOffset) {
                    offset = rowEndOfPreviousRow;
                } else {
                    offset = newMethodPossibleOffset;
                }
            } catch (BadLocationException ex) {
                offset = -1;
            }
        }
        return offset;
    }

    //~ inner classes
    private static class ImplementAllFix implements HintFix {
        private final BaseDocument doc;
        private final FixInfo fixInfo;

        ImplementAllFix(BaseDocument doc, FixInfo fixInfo) {
            this.doc = doc;
            this.fixInfo = fixInfo;
        }

        @Override
        @Messages("ImplementAbstractMethodsDesc=Implement All Abstract Methods")
        public String getDescription() {
            return Bundle.ImplementAbstractMethodsDesc();
        }

        @Override
        public void implement() throws Exception {
            getEditList().apply();
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

        EditList getEditList() throws Exception {
            EditList edits = new EditList(doc);
            edits.setFormatAll(true);
            for (String methodScope : fixInfo.methodSkeletons) {
                edits.replace(fixInfo.newMethodsOffset, 0, methodScope, true, 0);
            }
            return edits;
        }
    }

    private static class AbstractClassFix implements HintFix {
        private final BaseDocument doc;
        private final FixInfo fixInfo;

        public AbstractClassFix(BaseDocument doc, FixInfo fixInfo) {
            this.doc = doc;
            this.fixInfo = fixInfo;
        }

        @Override
        @Messages("AbstractClassFixDesc=Declare Abstract Class")
        public String getDescription() {
            return Bundle.AbstractClassFixDesc();
        }

        @Override
        public void implement() throws Exception {
            EditList edits = new EditList(doc);
            edits.replace(fixInfo.classDeclarationOffset, 0, ABSTRACT_PREFIX, true, 0);
            edits.apply();
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

    }

    private static class FixInfo {

        private List<String> methodSkeletons;
        private String className;
        private int newMethodsOffset;
        private OffsetRange classNameRange;
        private final String lastMethodDeclaration;
        private final String lastMethodOwnerName;
        private final int classDeclarationOffset;
        private final boolean anonymousClass;
        private final boolean isEnum;

        FixInfo(TypeScope typeScope, Set<String> methodSkeletons, MethodElement lastMethodElement, int newMethodsOffset, int classDeclarationOffset, boolean anonymousClass) {
            this.methodSkeletons = new ArrayList<>(methodSkeletons);
            className = typeScope.getFullyQualifiedName().toString();
            Collections.sort(this.methodSkeletons);
            this.classNameRange = typeScope.getNameRange();
            this.classDeclarationOffset = classDeclarationOffset;
            this.newMethodsOffset = newMethodsOffset;
            lastMethodDeclaration = lastMethodElement.asString(PrintAs.NameAndParamsDeclaration);
            lastMethodOwnerName = lastMethodElement.getType().getFullyQualifiedName().toString();
            this.anonymousClass = anonymousClass;
            this.isEnum = typeScope instanceof EnumScope;
        }
    }
}
