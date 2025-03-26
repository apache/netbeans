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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.completion.PHPCompletionItem;
import org.netbeans.modules.php.editor.completion.PHPCompletionItem.MethodDeclarationItem;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement.PrintAs;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.EnumCaseElement;
import org.netbeans.modules.php.editor.api.elements.FieldElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.elements.MethodElementImpl;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.CaseElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.EnumScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 * @author Radek Matous
 */
public class IntroduceSuggestion extends SuggestionRule {

    private static final String UNKNOWN_FILE_NAME = "?"; //NOI18N
    private static final String NAMESPACE_PARAMETER_NAME = "namespace"; //NOI18N
    private static final String NAMESPACE_SEPARATOR = "\\"; //NOI18N

    @Override
    public String getId() {
        return "Introduce.Hint"; //NOI18N
    }

    @Override
    @Messages("IntroduceHintDesc=Introduce Hint")
    public String getDescription() {
        return Bundle.IntroduceHintDesc();
    }

    @Override
    @Messages("IntroduceHintDispName=Introduce Hint")
    public String getDisplayName() {
        return Bundle.IntroduceHintDispName();
    }

    protected PhpVersion getPhpVersion(@NullAllowed FileObject fileObject) {
        return fileObject == null ? PhpVersion.getDefault() : CodeUtils.getPhpVersion(fileObject);
    }

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileObject == null) {
            return;
        }
        int caretOffset = getCaretOffset();
        final BaseDocument doc = context.doc;
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        OffsetRange lineBounds = VerificationUtils.createLineBounds(caretOffset, doc);
        if (lineBounds.containsInclusive(caretOffset)) {
            final Model model = phpParseResult.getModel();
            IntroduceFixVisitor introduceFixVisitor = new IntroduceFixVisitor(model, lineBounds, getPhpVersion(fileObject));
            phpParseResult.getProgram().accept(introduceFixVisitor);
            List<IntroduceFix> variableFixes = introduceFixVisitor.getIntroduceFixes();
            if (!variableFixes.isEmpty()) {
                for (IntroduceFix variableFixe : variableFixes) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    hints.add(new Hint(IntroduceSuggestion.this, getDisplayName(),
                            fileObject, variableFixe.getOffsetRange(),
                            Collections.<HintFix>singletonList(variableFixe), 500));
                }
            }
        }
    }

    private static class IntroduceFixVisitor extends DefaultTreePathVisitor {

        private final Model model;
        private final OffsetRange lineBounds;
        private final List<IntroduceFix> fixes = new ArrayList<>();
        private final PhpVersion phpVersion;

        IntroduceFixVisitor(Model model, OffsetRange lineBounds, PhpVersion phpVersion) {
            this.lineBounds = lineBounds;
            this.model = model;
            this.phpVersion = phpVersion;
        }

        private boolean hasConstants(TypeScope typeScope) {
            return phpVersion.hasConstantsInTraits()
                    || !(typeScope instanceof TraitScope);
        }

        @Override
        public void scan(ASTNode node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node != null && (VerificationUtils.isBefore(node.getStartOffset(), lineBounds.getEnd()))) {
                super.scan(node);
            }
        }

        @Override
        public void visit(ClassInstanceCreation instanceCreation) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (!instanceCreation.isAnonymous()
                    && lineBounds.containsInclusive(instanceCreation.getStartOffset())) {
                String clzName = CodeUtils.extractClassName(instanceCreation.getClassName());
                clzName = (clzName != null && clzName.trim().length() > 0) ? clzName : null;
                if (!isSpecialTypeName(clzName)) {
                    // other than "new static;" and "new self;"
                    ElementQuery.Index index = model.getIndexScope().getIndex();
                    Set<ClassElement> classes = Collections.emptySet();
                    if (StringUtils.hasText(clzName)) {
                        classes = index.getClasses(NameKind.exact(clzName));
                    }
                    if (clzName != null && classes.isEmpty()) {
                        ClassElement clz = getIndexedClass(clzName);
                        if (clz == null) {
                            fixes.add(IntroduceClassFix.getInstance(clzName, model, instanceCreation));
                        }
                    }
                }
            }
            super.visit(instanceCreation);
        }

        @Override
        public void visit(MethodInvocation methodInvocation) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (lineBounds.containsInclusive(methodInvocation.getStartOffset())) {
                String methName = CodeUtils.extractFunctionName(methodInvocation.getMethod());
                if (StringUtils.hasText(methName)) {
                    Collection<? extends TypeScope> allTypes = ModelUtils.resolveType(model, methodInvocation);
                    if (allTypes.size() == 1) {
                        TypeScope type = ModelUtils.getFirst(allTypes);
                        ElementQuery.Index index = model.getIndexScope().getIndex();
                        Set<MethodElement> allMethods = ElementFilter.forName(NameKind.exact(methName)).filter(index.getAllMethods(type));
                        if (allMethods.isEmpty()) {
                            assert type != null;
                            FileObject fileObject = type.getFileObject();
                            BaseDocument document = fileObject != null ? (BaseDocument) GsfUtilities.getADocument(fileObject, true) : null;
                            if (document != null && fileObject.canWrite()) {
                                fixes.add(new IntroduceMethodFix(document, methodInvocation, type));
                            }
                        }
                    }
                }
            }
            super.visit(methodInvocation);
        }

        @Override
        public void visit(StaticMethodInvocation methodInvocation) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (lineBounds.containsInclusive(methodInvocation.getStartOffset())) {
                String methName = CodeUtils.extractFunctionName(methodInvocation.getMethod());
                String clzName = CodeUtils.extractUnqualifiedClassName(methodInvocation);

                if (clzName != null && StringUtils.hasText(methName)) {
                    Collection<? extends TypeScope> allTypes = ModelUtils.resolveType(model, methodInvocation);
                    if (allTypes.size() == 1) {
                        TypeScope type = ModelUtils.getFirst(allTypes);
                        ElementQuery.Index index = model.getIndexScope().getIndex();
                        final ElementFilter nameFilter = ElementFilter.forName(NameKind.exact(methName));
                        final ElementFilter staticFilter = ElementFilter.forStaticModifiers(true);
                        Set<MethodElement> allMethods = ElementFilter.allOf(nameFilter, staticFilter).filter(index.getAllMethods(type));
                        if (allMethods.isEmpty()) {
                            assert type != null;
                            FileObject fileObject = type.getFileObject();
                            BaseDocument document = fileObject != null ? (BaseDocument) GsfUtilities.getADocument(fileObject, true) : null;
                            if (document != null && fileObject.canWrite()) {
                                fixes.add(new IntroduceStaticMethodFix(document, methodInvocation, type));
                            }
                        }
                    }
                }
            }
            super.visit(methodInvocation);
        }

        @Override
        public void visit(FieldAccess fieldAccess) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (lineBounds.containsInclusive(fieldAccess.getStartOffset())) {
                String fieldName = CodeUtils.extractVariableName(fieldAccess.getField());
                if (StringUtils.hasText(fieldName)) {
                    Collection<? extends TypeScope> allTypes = ModelUtils.resolveType(model, fieldAccess);
                    if (allTypes.size() == 1) {
                        TypeScope type = ModelUtils.getFirst(allTypes);
                        ElementQuery.Index index = model.getIndexScope().getIndex();
                        Set<FieldElement> allFields = ElementFilter.forName(NameKind.exact(fieldName)).filter(index.getAlllFields(type));
                        if (allFields.isEmpty()) {
                            assert type != null;
                            FileObject fileObject = type.getFileObject();
                            BaseDocument document = fileObject != null ? (BaseDocument) GsfUtilities.getADocument(fileObject, true) : null;
                            if (document != null && fileObject.canWrite()) {
                                if (type instanceof ClassScope || type instanceof TraitScope) {
                                    fixes.add(new IntroduceFieldFix(document, fieldAccess, type));
                                }
                            }
                        }

                    }
                }
            }
            super.visit(fieldAccess);
        }

        @Override
        public void visit(StaticFieldAccess fieldAccess) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (lineBounds.containsInclusive(fieldAccess.getStartOffset())) {
                final Variable field = fieldAccess.getField();
                String clzName = CodeUtils.extractUnqualifiedClassName(fieldAccess);
                if (clzName != null) {
                    String fieldName = CodeUtils.extractVariableName(field);
                    if (!StringUtils.hasText(fieldName)) {
                        return;
                    }
                    assert fieldName != null;
                    if (fieldName.startsWith("$")) { //NOI18N
                        fieldName = fieldName.substring(1);
                    }

                    Collection<? extends TypeScope> allTypes = ModelUtils.resolveType(model, fieldAccess);
                    if (allTypes.size() == 1) {
                        TypeScope type = ModelUtils.getFirst(allTypes);
                        ElementQuery.Index index = model.getIndexScope().getIndex();
                        ElementFilter staticFieldsFilter = ElementFilter.allOf(
                                ElementFilter.forName(NameKind.exact(fieldName)),
                                ElementFilter.forStaticModifiers(true));
                        Set<FieldElement> allFields = staticFieldsFilter.filter(index.getAlllFields(type));
                        if (allFields.isEmpty()) {
                            assert type != null;
                            FileObject fileObject = type.getFileObject();
                            BaseDocument document = fileObject != null ? (BaseDocument) GsfUtilities.getADocument(fileObject, true) : null;
                            if (document != null && fileObject.canWrite()) {
                                if (type instanceof ClassScope || type instanceof TraitScope) {
                                    fixes.add(new IntroduceStaticFieldFix(document, fieldAccess, type));
                                }
                            }
                        }

                    }
                }
            }
            super.visit(fieldAccess);
        }

        @Override
        public void visit(StaticConstantAccess staticConstantAccess) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (!staticConstantAccess.isDynamicName()
                    && (staticConstantAccess.getDispatcher() instanceof NamespaceName) // e.g. ClassName::CONSTANT, self::CONSTANT
                    && lineBounds.containsInclusive(staticConstantAccess.getStartOffset())) {
                String constName = staticConstantAccess.getConstantName().getName();
                String clzName = CodeUtils.extractUnqualifiedClassName(staticConstantAccess);

                if (clzName != null && StringUtils.hasText(constName)) {
                    Collection<? extends TypeScope> allTypes = ModelUtils.resolveType(model, staticConstantAccess);
                    if (allTypes.size() == 1) {
                        TypeScope type = ModelUtils.getFirst(allTypes);
                        // trait can have constants since PHP 8.2
                        if (hasConstants(type)) {
                            ElementQuery.Index index = model.getIndexScope().getIndex();
                            Set<TypeConstantElement> allConstants = ElementFilter.forName(NameKind.exact(constName)).filter(index.getAllTypeConstants(type));
                            Set<EnumCaseElement> allEnumCases = type instanceof EnumScope
                                    ? ElementFilter.forName(NameKind.exact(constName)).filter(index.getAllEnumCases(type))
                                    : Collections.emptySet();
                            if (allConstants.isEmpty() && allEnumCases.isEmpty()) {
                                assert type != null;
                                FileObject fileObject = type.getFileObject();
                                BaseDocument document = fileObject != null ? (BaseDocument) GsfUtilities.getADocument(fileObject, true) : null;
                                // if fileObject is null, document is null
                                if (document != null && fileObject.canWrite()) {
                                    fixes.add(new IntroduceClassConstantFix(document, staticConstantAccess, type));
                                    if (type instanceof EnumScope) {
                                        fixes.add(new IntroduceEnumCaseFix(document, staticConstantAccess, (EnumScope) type));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            super.visit(staticConstantAccess);
        }

        /**
         * Get Fixes.
         *
         * @return fixes
         */
        public List<IntroduceFix> getIntroduceFixes() {
            return Collections.unmodifiableList(fixes);
        }

        private ClassElement getIndexedClass(String name) {
            ClassElement retval = null;
            ElementQuery.Index index = model.getIndexScope().getIndex();
            Collection<ClassElement> classes = Collections.emptyList();
            if ("self".equals(name) || "parent".equals(name)) { //NOI18N
                ClassDeclaration classDeclaration = null;
                for (ASTNode aSTNode : getPath()) {
                    if (aSTNode instanceof ClassDeclaration) {
                        classDeclaration = (ClassDeclaration) aSTNode;
                        break;
                    }
                }
                if (classDeclaration != null) {
                    String clzName = CodeUtils.extractClassName(classDeclaration);
                    classes = index.getClasses(NameKind.exact(clzName));
                }
            } else {
                classes = index.getClasses(NameKind.exact(name));
            }
            if (classes.size() == 1) {
                retval = classes.iterator().next();
                if ("parent".equals(name)) { // NOI18N
                    QualifiedName superClassQualifiedName = retval.getSuperClassName();
                    if (superClassQualifiedName != null) {
                        String superClassName = superClassQualifiedName.getName();
                        if (superClassName != null) {
                            classes = index.getClasses(NameKind.exact(superClassName));
                            retval = (classes.size() == 1) ? classes.iterator().next() : null;
                        }
                    }
                }
            }
            return retval;
        }
    }

    private static class IntroduceClassFix extends IntroduceFix {
        private final String nsPart;
        private final String className;
        private final String classNameWithNsPart;
        private final FileObject folder;
        private final FileObject template;

        static IntroduceClassFix getInstance(String className, Model model, ClassInstanceCreation instanceCreation) {
            FileObject currentFile = model.getFileScope().getFileObject();
            FileObject folder = currentFile == null ? null : currentFile.getParent();
            String templatePath = "Templates/Scripting/PHPClass.php"; //NOI18N
            FileObject template = FileUtil.getConfigFile(templatePath);
            return (template != null && folder != null && folder.canWrite())
                    ? new IntroduceClassFix(className, template, folder, instanceCreation) : null;
        }

        IntroduceClassFix(String classNameWithNsPart, FileObject template, FileObject folder, ClassInstanceCreation instanceCreation) {
            super(null, instanceCreation);
            int lastIndexOfNsSeparator = classNameWithNsPart.lastIndexOf(NAMESPACE_SEPARATOR);
            this.nsPart = lastIndexOfNsSeparator == -1 ? "" : classNameWithNsPart.substring(0, lastIndexOfNsSeparator);
            this.className = classNameWithNsPart.substring(lastIndexOfNsSeparator + 1);
            this.classNameWithNsPart = classNameWithNsPart;
            this.template = template;
            this.folder = folder;
        }

        @Override
        public void implement() throws Exception {
            final DataFolder dataFolder = DataFolder.findFolder(folder);
            final DataObject configDataObject = DataObject.find(template);
            final FileObject[] clsFo = new FileObject[1];
            FileUtil.runAtomicAction(new Runnable() {

                @Override
                public void run() {
                    try {
                        Map<String, String> parameters = new HashMap<>();
                        if (StringUtils.hasText(nsPart)) {
                            parameters.put(NAMESPACE_PARAMETER_NAME, nsPart); //NOI18N
                        }
                        DataObject clsDataObject = configDataObject.createFromTemplate(dataFolder, className, parameters);
                        clsFo[0] = clsDataObject.getPrimaryFile();
                        FileObject fo = clsFo[0];
                        FileLock lock = fo.lock();
                        try {
                            fo.rename(lock, fo.getName(), "php"); //NOI18N
                        } finally {
                            lock.releaseLock();
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
            if (clsFo[0] != null) {
                UiUtils.open(clsFo[0], 0);
            }
        }

        @Override
        @Messages({
            "# {0} - Class name",
            "# {1} - File name",
            "IntroduceHintClassDesc=Create Class \"{0}\" in {1}"
        })
        public String getDescription() {
            String fileName = FileUtil.getFileDisplayName(folder);
            int length = fileName.length();
            if (length > 30) {
                fileName = fileName.substring(length - 30);
                final int indexOf = fileName.indexOf(File.separator);
                if (indexOf != -1) { //NOI18N
                    fileName = fileName.substring(indexOf);
                }
                fileName = String.format("...%s%s%s.php", fileName, File.separator, className); //NOI18N
            }
            return Bundle.IntroduceHintClassDesc(classNameWithNsPart, fileName);
        }
    }

    private static class IntroduceMethodFix extends IntroduceFix {
        private final TypeScope type;
        private final MethodDeclarationItem item;

        public IntroduceMethodFix(BaseDocument doc, MethodInvocation node, TypeScope type) {
            super(doc, node);
            this.type = type;
            this.item = createMethodDeclarationItem(type, node);
        }

        @Override
        public void implement() throws Exception {
            int templateOffset = getOffset();
            EditList edits = new EditList(doc);
            edits.replace(templateOffset, 0, "\n" + item.getCustomInsertTemplate(), true, 0); //NOI18N
            edits.apply();
            templateOffset = LineDocumentUtils.getLineEnd(doc, templateOffset + 1);
            UiUtils.open(type.getFileObject(), LineDocumentUtils.getLineEnd(doc, templateOffset + 1) - 1);
        }

        @Override
        @Messages({
            "# {0} - Method name",
            "# {1} - Type kind",
            "# {2} - Type name",
            "# {3} - File name",
            "IntroduceHintMethodDesc=Create Method \"{0}\" in {1} \"{2}\" ({3})"
        })
        public String getDescription() {
            String typeName = type.getName();
            FileObject fileObject = type.getFileObject();
            String fileName = fileObject == null ? UNKNOWN_FILE_NAME : fileObject.getNameExt();
            String typeKindName = getTypeKindName(type);
            return Bundle.IntroduceHintMethodDesc(item.getMethod().asString(PrintAs.NameAndParamsDeclaration), typeKindName, typeName, fileName);
        }

        int getOffset() throws BadLocationException {
            return IntroduceSuggestion.getOffset(doc, type, PhpElementKind.METHOD);
        }
    }

    private static class IntroduceStaticMethodFix extends IntroduceFix {
        private final TypeScope type;
        private final MethodDeclarationItem item;

        public IntroduceStaticMethodFix(BaseDocument doc, StaticMethodInvocation node, TypeScope type) {
            super(doc, node);
            this.type = type;
            this.item = createMethodDeclarationItem(type, node);
        }

        @Override
        public void implement() throws Exception {
            int templateOffset = getOffset();
            EditList edits = new EditList(doc);
            edits.replace(templateOffset, 0, "\n" + item.getCustomInsertTemplate(), true, 0); //NOI18N
            edits.apply();
            templateOffset = LineDocumentUtils.getLineEnd(doc, templateOffset + 1);
            UiUtils.open(type.getFileObject(), LineDocumentUtils.getLineEnd(doc, templateOffset + 1) - 1);
        }

        @Override
        @Messages({
            "# {0} - Method name",
            "# {1} - Type kind",
            "# {2} - Type name",
            "# {3} - File name",
            "IntroduceHintStaticMethodDesc=Create Method \"{0}\" in {1} \"{2}\" ({3})"
        })
        public String getDescription() {
            String typeName = type.getName();
            FileObject fileObject = type.getFileObject();
            String fileName = fileObject == null ? UNKNOWN_FILE_NAME : fileObject.getNameExt();
            String typeKindName = getTypeKindName(type);
            return Bundle.IntroduceHintStaticMethodDesc(item.getMethod().asString(PrintAs.NameAndParamsDeclaration), typeKindName, typeName, fileName);
        }

        int getOffset() throws BadLocationException {
            return IntroduceSuggestion.getOffset(doc, type, PhpElementKind.METHOD);
        }
    }

    private static class IntroduceFieldFix extends IntroduceFix {
        private final TypeScope type;
        private final String templ;
        private final VariableBase dispatcher;
        private String fieldName;

        public IntroduceFieldFix(BaseDocument doc, FieldAccess node, TypeScope type) {
            super(doc, node);
            this.type = type;
            this.dispatcher = node.getDispatcher();
            this.templ = createTemplate();
        }

        @Override
        public void implement() throws Exception {
            int templateOffset = getOffset();
            EditList edits = new EditList(doc);
            edits.replace(templateOffset, 0, "\n" + templ, true, 0); //NOI18N
            edits.apply();
            templateOffset = LineDocumentUtils.getLineEnd(doc, templateOffset + 1) - 2;
            UiUtils.open(type.getFileObject(), templateOffset);
        }

        @Override
        @Messages({
            "# {0} - Field name",
            "# {1} - Type kind",
            "# {2} - Type name",
            "# {3} - File name",
            "IntroduceHintFieldDesc=Create Field \"{0}\" in {1} \"{2}\" ({3})"
        })
        public String getDescription() {
            String typeName = type.getName();
            FileObject fileObject = type.getFileObject();
            String fileName = fileObject == null ? UNKNOWN_FILE_NAME : fileObject.getNameExt();
            String typeKindName = getTypeKindName(type);
            return Bundle.IntroduceHintFieldDesc(templ, typeKindName, typeName, fileName);
        }

        int getOffset() throws BadLocationException {
            return IntroduceSuggestion.getOffset(doc, type, PhpElementKind.FIELD);
        }

        private String createTemplate() {
            Variable fieldVar = ((FieldAccess) node).getField();
            this.fieldName = CodeUtils.extractVariableName(fieldVar);
            if (!fieldVar.isDollared()) {
                this.fieldName = "$" + this.fieldName; //NOI18N
            }
            return String.format("%s %s;", isInternal() ? "private" : "public", fieldName); //NOI18N
        }

        private boolean isInternal() {
            boolean result = false;
            if (dispatcher instanceof Variable) {
                Variable variable = (Variable) dispatcher;
                String dispatcherName = CodeUtils.extractVariableName(variable);
                result = "$this".equals(dispatcherName); //NOI18N
            }
            return result;
        }
    }

    private static class IntroduceStaticFieldFix extends IntroduceFix {
        private final TypeScope type;
        private final String templ;
        private String fieldName;

        public IntroduceStaticFieldFix(BaseDocument doc, StaticFieldAccess node, TypeScope type) {
            super(doc, node);
            this.type = type;
            this.templ = createTemplate();
        }

        @Override
        public void implement() throws Exception {
            int templateOffset = getOffset();
            EditList edits = new EditList(doc);
            edits.replace(templateOffset, 0, "\n" + templ, true, 0); //NOI18N
            edits.apply();
            templateOffset = LineDocumentUtils.getLineEnd(doc, templateOffset + 1) - 2;
            UiUtils.open(type.getFileObject(), templateOffset);
        }

        @Override
        @Messages({
            "# {0} - Field name",
            "# {1} - Type kind",
            "# {2} - Type name",
            "# {3} - File name",
            "IntroduceHintStaticFieldDesc=Create Field \"{0}\" in {1} \"{2}\" ({3})"
        })
        public String getDescription() {
            String typeName = type.getName();
            FileObject fileObject = type.getFileObject();
            String fileName = fileObject == null ? UNKNOWN_FILE_NAME : fileObject.getNameExt();
            String typeKindName = getTypeKindName(type);
            return Bundle.IntroduceHintStaticFieldDesc(fieldName, typeKindName, typeName, fileName);
        }

        int getOffset() throws BadLocationException {
            return IntroduceSuggestion.getOffset(doc, type, PhpElementKind.FIELD);
        }

        private String createTemplate() {
            Variable fieldVar = ((StaticFieldAccess) node).getField();
            fieldName = CodeUtils.extractVariableName(fieldVar);
            if (!fieldVar.isDollared()) {
                fieldName = "$" + fieldName; //NOI18N
            }
            return String.format("static %s = \"\";", fieldName);
        }
    }

    private static class IntroduceClassConstantFix extends IntroduceFix {
        private final TypeScope type;
        private final String templ;
        private final String constantName;

        public IntroduceClassConstantFix(BaseDocument doc, StaticConstantAccess node, TypeScope type) {
            super(doc, node);
            this.type = type;
            this.constantName = ((StaticConstantAccess) node).getConstantName().getName();
            this.templ = String.format("const %s = \"\";", constantName);
        }

        @Override
        public void implement() throws Exception {
            int templateOffset = getOffset();
            EditList edits = new EditList(doc);
            edits.replace(templateOffset, 0, "\n" + templ, true, 0); //NOI18N
            edits.apply();
            templateOffset = LineDocumentUtils.getLineEnd(doc, templateOffset + 1) - 2;
            UiUtils.open(type.getFileObject(), templateOffset);
        }

        @Override
        @Messages({
            "# {0} - Constant name",
            "# {1} - Type kind",
            "# {2} - Type name",
            "# {3} - File name",
            "IntroduceHintClassConstDesc=Create Constant \"{0}\" in {1} \"{2}\" ({3})"
        })
        public String getDescription() {
            String typeName = type.getName();
            FileObject fileObject = type.getFileObject();
            String fileName = fileObject == null ? UNKNOWN_FILE_NAME : fileObject.getNameExt();
            String typeKindName = getTypeKindName(type);
            return Bundle.IntroduceHintClassConstDesc(constantName, typeKindName, typeName, fileName);
        }

        int getOffset() throws BadLocationException {
            return IntroduceSuggestion.getOffset(doc, type, PhpElementKind.TYPE_CONSTANT);
        }
    }

    private static class IntroduceEnumCaseFix extends IntroduceFix {

        private final EnumScope type;
        private final String template;
        private final String enumCaseName;
        private final String backingType;

        public IntroduceEnumCaseFix(BaseDocument doc, StaticConstantAccess node, EnumScope type) {
            super(doc, node);
            this.type = type;
            this.enumCaseName = ((StaticConstantAccess) node).getConstantName().getName();
            this.backingType = type.getBackingType() != null ? type.getBackingType().toString() : null;
            this.template = String.format(getTemplate(backingType), enumCaseName);
        }

        private String getTemplate(String backingType) {
            String caseTemplate = "case %s;"; // NOI18N
            if (backingType != null) {
                if (Type.STRING.equals(backingType)) {
                    caseTemplate = "case %s = '';"; // NOI18N
                } else if (Type.INT.equals(backingType)) {
                    caseTemplate = "case %s = " + (getLastIntIndex() + 1) + ";"; // NOI18N
                }
            }
            return caseTemplate;
        }

        private int getLastIntIndex() {
            int index = 0;
            for (CaseElement enumCase : type.getDeclaredEnumCases()) {
                String value = enumCase.getValue();
                try {
                    int intValue = Integer.parseInt(value);
                    index = Integer.max(index, intValue);
                } catch (NumberFormatException e) {
                    // no-op
                }
            }
            return index;
        }

        @Override
        public void implement() throws Exception {
            int templateOffset = getOffset();
            EditList edits = new EditList(doc);
            edits.replace(templateOffset, 0, "\n" + template, true, 0); // NOI18N
            edits.apply();
            int caretPositionFromEndOftemplate = Type.STRING.equalsIgnoreCase(backingType) ? 2 : 1;
            templateOffset = LineDocumentUtils.getLineEnd(doc, templateOffset + 1) - caretPositionFromEndOftemplate;
            UiUtils.open(type.getFileObject(), templateOffset);
        }

        @Override
        @Messages({
            "# {0} - Case name",
            "# {1} - Type kind",
            "# {2} - Type name",
            "# {3} - File name",
            "IntroduceHintEnumCaseDesc=Create Enum Case \"{0}\" in {1} \"{2}\" ({3})"
        })
        public String getDescription() {
            String typeName = type.getName();
            FileObject fileObject = type.getFileObject();
            String fileName = fileObject == null ? UNKNOWN_FILE_NAME : fileObject.getNameExt();
            String typeKindName = getTypeKindName(type);
            return Bundle.IntroduceHintEnumCaseDesc(enumCaseName, typeKindName, typeName, fileName);
        }

        int getOffset() throws BadLocationException {
            return IntroduceSuggestion.getOffset(doc, type, PhpElementKind.ENUM_CASE);
        }
    }

    abstract static class IntroduceFix implements HintFix {

        BaseDocument doc;
        ASTNode node;

        public IntroduceFix(BaseDocument doc, ASTNode node) {
            this.doc = doc;
            this.node = node;
        }

        OffsetRange getOffsetRange() {
            return new OffsetRange(node.getStartOffset(), node.getEndOffset());
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

        @Override
        public boolean isSafe() {
            return true;
        }
    }

    private static boolean isSpecialTypeName(String typeName) {
        return Type.STATIC.equals(typeName) || Type.SELF.equals(typeName) || Type.PARENT.equals(typeName);
    }

    private static String getParameters(final List<Expression> parameters) {
        StringBuilder paramNames = new StringBuilder();
        for (int i = 0; i < parameters.size(); i++) {
            Expression expression = parameters.get(i);
            String varName = null;
            if (expression instanceof Variable) {
                varName = CodeUtils.extractVariableName((Variable) expression);
            }
            if (varName == null) {
                varName = String.format("$param%d", i); //NOI18N
            }
            if (i > 0) {
                paramNames.append(", ");
            }
            paramNames.append(varName);
        }
        return paramNames.toString();
    }

    private static int getOffset(BaseDocument doc, TypeScope typeScope, PhpElementKind kind) throws BadLocationException {
        int offset = -1;
        Collection<ModelElement> elements = new HashSet<>();
        elements.addAll(typeScope.getDeclaredConstants());
        switch (kind) {
            case METHOD:
                if (typeScope instanceof ClassScope) {
                    ClassScope clz = (ClassScope) typeScope;
                    elements.addAll(clz.getDeclaredFields());
                    elements.addAll(clz.getDeclaredMethods());
                } else if (typeScope instanceof TraitScope) {
                    TraitScope trait = (TraitScope) typeScope;
                    elements.addAll(trait.getDeclaredFields());
                    elements.addAll(trait.getDeclaredMethods());
                } else if (typeScope instanceof EnumScope) {
                    EnumScope enumScope = (EnumScope) typeScope;
                    elements.addAll(enumScope.getDeclaredEnumCases());
                    elements.addAll(enumScope.getDeclaredMethods());
                }
                break;
            case FIELD:
                if (typeScope instanceof ClassScope) {
                    ClassScope clz = (ClassScope) typeScope;
                    elements.addAll(clz.getDeclaredFields());
                } else if (typeScope instanceof TraitScope) {
                    TraitScope trait = (TraitScope) typeScope;
                    elements.addAll(trait.getDeclaredFields());
                }
                break;
            case ENUM_CASE:
                if (typeScope instanceof EnumScope) {
                    EnumScope enumScope = (EnumScope) typeScope;
                    elements.addAll(enumScope.getDeclaredEnumCases());
                }
                break;
            case TYPE_CONSTANT:
                // no-op
                break;
            default:
                assert false;
        }
        int newOffset;
        for (ModelElement elem : elements) {
            newOffset = elem.getOffset();
            if (elem instanceof MethodScope) {
                newOffset = getOffsetAfterBlockCloseCurly(doc, newOffset);
            } else {
                newOffset = getOffsetAfterNextSemicolon(doc, newOffset);
            }
            if (newOffset > offset) {
                offset = newOffset;
            }
        }

        if (offset == -1) {
            if (typeScope.isTraited()) {
                // has use trait statements
                offset = getOffsetAfterUseTrait(doc, typeScope);
            } else {
                offset = getOffsetAfterClassOpenCurly(doc, typeScope.getOffset());
            }
        }
        return offset;
    }

    private static int getOffsetAfterBlockCloseCurly(BaseDocument doc, int offset) throws BadLocationException {
        int retval = offset;
        doc.readLock();
        try {
            TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, retval);
            if (ts != null) {
                ts.move(retval);
                int curlyMatch = 0;
                while (ts.moveNext()) {
                    Token t = ts.token();
                    if (t.id() == PHPTokenId.PHP_CURLY_OPEN || t.id() == PHPTokenId.PHP_CURLY_CLOSE) {
                        if (t.id() == PHPTokenId.PHP_CURLY_OPEN) {
                            curlyMatch++;
                        } else if (t.id() == PHPTokenId.PHP_CURLY_CLOSE) {
                            curlyMatch--;
                        }
                        if (curlyMatch == 0) {
                            ts.moveNext();
                            retval = ts.offset();
                            break;
                        }
                    }
                }
            }
        } finally {
            doc.readUnlock();
        }
        return retval;
    }

    private static int getOffsetAfterNextSemicolon(BaseDocument doc, int offset) throws BadLocationException {
        return getOffsetAfterNextTokenId(doc, offset, PHPTokenId.PHP_SEMICOLON);
    }

    private static int getOffsetAfterClassOpenCurly(BaseDocument doc, int offset) throws BadLocationException {
        return getOffsetAfterNextTokenId(doc, offset, PHPTokenId.PHP_CURLY_OPEN);
    }

    private static int getOffsetAfterNextTokenId(BaseDocument doc, int offset, PHPTokenId tokenId) throws BadLocationException {
        int retval = offset;
        doc.readLock();
        try {
            TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, retval);
            if (ts != null) {
                ts.move(retval);
                while (ts.moveNext()) {
                    Token t = ts.token();
                    if (t.id() == tokenId) {
                        ts.moveNext();
                        retval = ts.offset();
                        break;
                    }
                }
            }
        } finally {
            doc.readUnlock();
        }
        return retval;
    }

    /**
     * Get an offset after a use trait statement.
     * <b>NOTE:</b>This method should be used when a type doesn't have
     * constants. If the use trait statement is after fields or methods, the
     * offset after the open curry for type is returned.
     *
     * @param document the document
     * @param typeScope the type scope
     * @return The offset after the last use trait statement if traits are used,
     * otherwise the offset after the open curly for the type
     */
    private static int getOffsetAfterUseTrait(BaseDocument document, TypeScope typeScope) throws BadLocationException {
        OffsetRange blockRange = typeScope.getBlockRange();
        if (blockRange == null) {
            // GH-6258 Block range of ClassScope created from ClassElement is null
            return getOffsetAfterClassOpenCurly(document, typeScope.getOffset());
        }
        int offset = blockRange.getEnd() - 1; // before close curly "}"
        Collection<ModelElement> elements = new HashSet<>();
        elements.addAll(typeScope.getDeclaredMethods());
        if (typeScope instanceof ClassScope) {
            elements.addAll(((ClassScope) typeScope).getDeclaredFields());
        } else if (typeScope instanceof TraitScope) {
            elements.addAll(((TraitScope) typeScope).getDeclaredFields());
        }
        for (ModelElement element : elements) {
            int newOffset = element.getOffset();
            if (newOffset < offset) {
                offset = newOffset;
            }
        }

        document.readLock();
        try {
            TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(document, offset);
            if (ts != null) {
                ts.move(offset);
                if (ts.movePrevious()) {
                    // find the following cases : "use TraitA;", "use TraitA{}" and "TypeName {"
                    List<PHPTokenId> lookfor = Arrays.asList(
                            PHPTokenId.PHP_SEMICOLON,
                            PHPTokenId.PHP_CURLY_CLOSE,
                            PHPTokenId.PHP_CURLY_OPEN
                    );
                    Token<? extends PHPTokenId> previousToken = LexUtilities.findPreviousToken(ts, lookfor);
                    if (previousToken != null) {
                        return ts.offset() + previousToken.length();
                    }
                }
            }
        } finally {
            document.readUnlock();
        }
        return offset;
    }

    private static PHPCompletionItem.MethodDeclarationItem createMethodDeclarationItem(final TypeScope typeScope, final MethodInvocation node) {
        final String methodName = CodeUtils.extractFunctionName(node.getMethod());
        final MethodElement method = MethodElementImpl.forIntroduceHint(typeScope,
                methodName, 0, getParameters(node.getMethod().getParameters()));
        return typeScope.isInterface()
                ? PHPCompletionItem.MethodDeclarationItem.forIntroduceInterfaceHint(method, null)
                : PHPCompletionItem.MethodDeclarationItem.forIntroduceHint(method, null);
    }

    private static PHPCompletionItem.MethodDeclarationItem createMethodDeclarationItem(final TypeScope typeScope, final StaticMethodInvocation node) {
        final String methodName = CodeUtils.extractFunctionName(node.getMethod());
        final MethodElement method = MethodElementImpl.forIntroduceHint(typeScope, methodName,
                Modifier.STATIC, getParameters(node.getMethod().getParameters()));
        return PHPCompletionItem.MethodDeclarationItem.forIntroduceHint(method, null);
    }

    @Messages({
        "IntroduceHintClassName=Class",
        "IntroduceHintInterfaceName=Interface",
        "IntroduceHintTraitName=Trait",
        "IntroduceHintEnumName=Enum",
    })
    private static String getTypeKindName(TypeScope typeScope) {
        if (typeScope instanceof ClassScope) {
            return Bundle.IntroduceHintClassName();
        } else if (typeScope instanceof InterfaceScope) {
            return Bundle.IntroduceHintInterfaceName();
        } else if (typeScope instanceof TraitScope) {
            return Bundle.IntroduceHintTraitName();
        } else if (typeScope instanceof EnumScope) {
            return Bundle.IntroduceHintEnumName();
        }
        assert false;
        return "?"; // NOI18N
    }

}
