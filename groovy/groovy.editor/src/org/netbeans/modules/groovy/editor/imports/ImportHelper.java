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
package org.netbeans.modules.groovy.editor.imports;

import java.awt.Dialog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.groovy.editor.api.GroovyIndex;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedClass;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.utils.GroovyUtils;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Utility class used for changes in import statements. Typically used by "Fix imports"
 * action or possibly could be used by Move/Rename refactoring when old used imports
 * don't need to be in code anymore and on the other hand new imports need to be added.
 *
 * @author schmidtm
 * @author Martin Janicek
 */
public final class ImportHelper {

    private static final Logger LOG = Logger.getLogger(ImportHelper.class.getName());


    private ImportHelper() {
    }

    /**
     * Resolve import and add it as an import statements into the source code if
     * it's missing.
     *
     * @param fo fileObject of the current file where the import is missing
     * @param packageName name of the package where the current class is placed
     * @param importName name of the import to resolve (not fully qualified name)
     */
    public static void resolveImport(FileObject fo, String packageName, String importName) {
        resolveImports(fo, packageName, Collections.singletonList(importName));
    }

    /**
     * Resolve imports and add them as import statements into the source code.
     *
     * @param fo fileObject of the current file where the imports are missing
     * @param packageName name of the package where the current class is placed
     * @param missingNames list of missing names (not fully qualified names)
     */
    public static void resolveImports(
        final FileObject fo,
        final String packageName,
        final List<String> missingNames) {

        final AtomicBoolean cancel = new AtomicBoolean();
        final List<String> singleCandidates = new ArrayList<>();
        final Map<String, Set<ImportCandidate>> multipleCandidates = new HashMap<>();

        // go over list of missing imports, fix it - if there is only one candidate
        // or populate choosers input list if there is more than one candidate.

        for (String name : missingNames) {
            Set<ImportCandidate> importCandidates = getImportCandidate(fo, packageName, name);

            switch (importCandidates.size()) {
                case 0: continue;
                case 1: singleCandidates.add(importCandidates.iterator().next().getFqnName()); break;
                default: multipleCandidates.put(name, importCandidates);
            }
        }

        // do we have multiple candidate? In this case we need to present a chooser

        if (!multipleCandidates.isEmpty()) {
            List<String> choosenCandidates = showFixImportChooser(multipleCandidates);
            singleCandidates.addAll(choosenCandidates);
        }

        if (!singleCandidates.isEmpty()) {
            Collections.sort(singleCandidates);
            BaseProgressUtils.runOffEventDispatchThread(new Runnable() {

                @Override
                public void run() {
                    addImportStatements(fo, singleCandidates).apply();
                }
            }, "Adding imports", cancel, false);
        }
    }

    /**
     * For the given missing import finds out possible candidates. This means if
     * there are more class types with the same name only with different packaging
     * (e.g. java.lang.Object and org.netbeans.modules.whatever.Object) we will get
     * list of two candidates as a result. Typically used by "Add import hint" where
     * we know that some line is incorrect due to missing import, but we don't know
     * what could be possibly imported to fix the problem.
     *
     * @param fo current file
     * @param packageName name of the package where the current class is placed
     * @param missingClass class name for which we are looking for import candidates
     * @return list of possible import candidates
     */
    public static Set<ImportCandidate> getImportCandidate(FileObject fo, String packageName, String missingClass) {
        LOG.log(Level.FINEST, "Looking for class: {0}", missingClass);

        Set<ImportCandidate> candidates = new HashSet<>();
        candidates.addAll(findGroovyImportCandidates(fo, packageName, missingClass));
        candidates.addAll(findJavaImportCandidates(fo, packageName, missingClass));

        return candidates;
    }

    private static Set<ImportCandidate> findGroovyImportCandidates(FileObject fo, String packageName, String missingClass) {
        final Set<ImportCandidate> candidates = new HashSet<>();
        final GroovyIndex index = GroovyIndex.get(QuerySupport.findRoots(fo,
                Collections.singleton(ClassPath.SOURCE), null, null));

        Set<IndexedClass> classes = index.getClasses(missingClass, QuerySupport.Kind.PREFIX);
        for (IndexedClass indexedClass : classes) {
            if (!indexedClass.getName().equals(missingClass)) {
                continue;
            }

            // Skip classes within the same package
            String pkgName = GroovyUtils.stripClassName(indexedClass.getFqn());
            if (pkgName == null || "".equals(pkgName.trim())) {
                // The class is from default package - no way to add import statement for shit like that
                continue;
            }
            if (packageName != null && packageName.equals(pkgName)) {
                // Both in the same package
                continue;
            }

            if (indexedClass.getKind() == org.netbeans.modules.csl.api.ElementKind.CLASS) {
                candidates.add(createImportCandidate(missingClass, indexedClass.getFqn(), ElementKind.CLASS));
            }
            if (indexedClass.getKind() == org.netbeans.modules.csl.api.ElementKind.INTERFACE) {
                candidates.add(createImportCandidate(missingClass, indexedClass.getFqn(), ElementKind.INTERFACE));
            }
        }
        return candidates;
    }

    private static Set<ImportCandidate> findJavaImportCandidates(FileObject fo, String packageName, String missingClass) {
        final Set<ImportCandidate> candidates = new HashSet<>();
        final ClasspathInfo pathInfo = ClasspathInfo.create(fo);

        Set<ElementHandle<TypeElement>> typeNames = pathInfo.getClassIndex().getDeclaredTypes(
                missingClass, NameKind.SIMPLE_NAME, EnumSet.allOf(ClassIndex.SearchScope.class));

        for (ElementHandle<TypeElement> typeName : typeNames) {
            ElementKind kind = typeName.getKind();

            // Skip classes within the same package
            String pkgName = GroovyUtils.stripClassName(typeName.getQualifiedName());
            if (packageName == null && pkgName == null) {
                // Probably both in default package
                continue;
            }

            if (packageName != null && packageName.equals(pkgName)) {
                continue;
            }

            if (kind == ElementKind.CLASS || kind == ElementKind.INTERFACE || kind == ElementKind.ANNOTATION_TYPE) {
                candidates.add(createImportCandidate(missingClass, typeName.getQualifiedName(), kind));
            }
        }
        return candidates;
    }

    private static ImportCandidate createImportCandidate(String missingClass, String fqnName, ElementKind kind) {
        int level = getImportanceLevel(fqnName);
        Icon icon = ElementIcons.getElementIcon(kind, null);

        return new ImportCandidate(missingClass, fqnName, icon, level);
    }

    private static int getImportanceLevel(String fqn) {
        int weight = 50;
        if (fqn.startsWith("java.lang") || fqn.startsWith("java.util")) { // NOI18N
            weight -= 10;
        } else if (fqn.startsWith("org.omg") || fqn.startsWith("org.apache")) { // NOI18N
            weight += 10;
        } else if (fqn.startsWith("com.sun") || fqn.startsWith("com.ibm") || fqn.startsWith("com.apple")) { // NOI18N
            weight += 20;
        } else if (fqn.startsWith("sun") || fqn.startsWith("sunw") || fqn.startsWith("netscape")) { // NOI18N
            weight += 30;
        }
        return weight;
    }

    /**
     * For the given error message finds out missing class name. The error message
     * parameter should be directly from groovy parser.
     *
     * @param errorMessage groovy parser error message
     * @return missing class name
     */
    public static String getMissingClassName(String errorMessage) {
        String errorText = "unable to resolve class "; // NOI18N
        String missingClass = null;
        if (errorMessage.startsWith(errorText)) {
            missingClass = errorMessage.substring(errorText.length());
        } else {
            // I really don't like how the missing class is founded, but so far .....
            errorText = "doesn't refer to a local variable, static field or class"; //NOI18N
            if (errorMessage.contains(errorText)) {
                int startIndex = errorMessage.indexOf('\'');
                if (startIndex > -1) {
                    startIndex++;
                    int endIndex = errorMessage.indexOf('\'', startIndex);
                    if (endIndex > -1) {
                        missingClass = errorMessage.substring(startIndex, endIndex);
                    }
                }
            }
        }
            
        if (missingClass == null) {
            return null;
        }
        int idx = missingClass.indexOf(" ");
        if (idx != -1) {
            missingClass = missingClass.substring(0, idx);
        }

        return missingClass.trim();
    }

    private static List<String> showFixImportChooser(Map<String, Set<ImportCandidate>> multipleCandidates) {
        List<String> result = new ArrayList<>();
        ImportChooserInnerPanel panel = new ImportChooserInnerPanel();

        panel.initPanel(multipleCandidates);

        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(ImportHelper.class, "FixImportsDialogTitle")); //NOI18N
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);

        d.setVisible(true);
        d.setVisible(false);
        d.dispose();

        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            result = panel.getSelections();
        }
        return result;
    }

    /**
     * Add import directly to the source code (does not run any checks if the import
     * has more candidates from different packages etc.). Typically used by "Add import
     * hint" where we already know what to add.
     *
     * @param fo file where we want to put import statement
     * @param fqName fully qualified name of the import
     */
    public static void addImportStatement(FileObject fo, String fqName) {
        addImportStatements(fo, Collections.singletonList(fqName)).apply();
    }

    /**
     * Returns edits for adding import to the source code (does not run any checks if the import
     * has more candidates from different packages etc.). Typically used by "Add import
     * hint" where we already know what to add.
     *
     * @param fo file where we want to put import statement
     * @param fqName fully qualified name of the import
     * @return list of edits to be made
     */
    public static EditList addImportStatementEdits(FileObject fo, String fqName) {
        return addImportStatements(fo, Collections.singletonList(fqName));
    }

    private static EditList addImportStatements(FileObject fo, List<String> fqNames) {
        BaseDocument doc = LexUtilities.getDocument(fo, true);
        EditList edits = new EditList(doc);
        if (doc != null) {
            for (String fqName : fqNames) {
                try {
                    int packageLine = getPackageLineIndex(doc);
                    int afterPackageLine = packageLine + 1;
                    int afterPackageOffset = LineDocumentUtils.getLineStartFromIndex(doc, afterPackageLine);
                    int importLine = getAppropriateLine(doc, fqName);
                    if (importLine >= 0) {
                        // If the line after the package statement isn't empty, put one empty line there
                        if (!LineDocumentUtils.isLineWhitespace(doc, afterPackageOffset)) {
                            edits.replace(afterPackageOffset, 0, "\n", false, 0);
                        } else {
                            if (collectImports(doc).isEmpty()) {
                                importLine++;
                            }
                        }

                        // Find appropriate place to import and put it there
                        int importOffset = LineDocumentUtils.getLineStartFromIndex(doc, importLine);
                        edits.replace(importOffset, 0, "import " + fqName + "\n", false, 0);

                        // If it's the last import and if the line after the last import
                        // statement isn't empty, put one empty line there
                        int afterImportsOffset = LineDocumentUtils.getLineStartFromIndex(doc, importLine);

                        if (!LineDocumentUtils.isLineWhitespace(doc, afterImportsOffset) && isLastImport(doc, fqName)) {
                            edits.replace(afterImportsOffset, 0, "\n", false, 0);
                        }
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return edits;
    }

    private static int getAppropriateLine(BaseDocument doc, String fqName) throws BadLocationException {
        Map<String, Integer> imports = collectImports(doc);
        if (imports.isEmpty()) {
            // No imports in the source code yet, put the first one two lines behind package statement
            return getPackageLineIndex(doc) + 1;
        }

        if (imports.containsKey(fqName)) {
            // Already imported
            return -1;
        }

        imports.put(fqName, -1);

        String lastImportName = null;
        for (String importName : imports.keySet()) {
            if (fqName.equals(importName)) {
                break;
            }

            // Save import name for the next iteration --> If we find fqName then
            // we want to put new import statement after the last saved import name
            lastImportName = importName;
        }

        // It should be added as the first import statement
        if (lastImportName == null) {
            for (Integer importLine : imports.values()) {

                // Just find the first import with line set
                if (importLine > 0) {
                    return importLine;
                }
            }
        }

        return imports.get(lastImportName) + 1;
    }

    private static boolean isLastImport(BaseDocument doc, String fqName) throws BadLocationException {
        Map<String, Integer> imports = collectImports(doc);
        if (imports.isEmpty()) {
            return true;
        }

        String lastImportName = null;
        for (String importName : imports.keySet()) {
            lastImportName = importName;
        }

        // lastImportName is null if there is no other import statement yet
        if (lastImportName != null && fqName.compareTo(lastImportName) > 0) {
            return true;
        }
        return false;
    }

    private static Map<String, Integer> collectImports(BaseDocument doc) throws BadLocationException {
        TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, 1);

        Map<String, Integer> result = new TreeMap<>();
        while (ts.moveNext()) {
            if (ts.token().id() == GroovyTokenId.LITERAL_import) {
                StringBuilder sb = new StringBuilder();

                IMPORT_COUNTER:
                while (ts.moveNext()) {
                    GroovyTokenId tokenID = ts.token().id();
                    switch (tokenID) {
                        case IDENTIFIER:
                        case DOT:
                            sb.append(ts.token().text());
                            break;
                        case WHITESPACE:
                            // Probably space between 'import' and identifier --> Do nothing
                            break;
                        default:
                            break IMPORT_COUNTER;
                    }
                }
                result.put(sb.toString(), LineDocumentUtils.getLineIndex(doc, ts.offset()));
            }
        }
        return result;
    }

    /**
     * Returns line index (line number - 1) of the package statement or {@literal -1}
     * if no package statement was found within this in {@link BaseDocument}.
     *
     * @param doc document
     * @return line index (line number - 1) of the package statement or {@literal -1}
     *         if no package statement was found within this {@link BaseDocument}.
     */
    private static int getPackageLineIndex(BaseDocument doc) {
        try {
            int lastPackageOffset = getLastPackageStatementOffset(doc);
            if (lastPackageOffset != -1) {
                return LineDocumentUtils.getLineIndex(doc, lastPackageOffset);
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return -1;
    }

    /**
     * Returns offset of the package statement or {@literal -1} if no package
     * statement was found within this in {@link BaseDocument}.
     *
     * @param doc document
     * @return offset of the package statement or {@literal -1} if no package
     *         statement was found within this {@link BaseDocument}.
     */
    private static int getLastPackageStatementOffset(BaseDocument doc) {
        TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, 1);

        int packageOffset = -1;

        while (ts.moveNext()) {
            if (ts.token().id() == GroovyTokenId.LITERAL_package) {
                packageOffset = ts.offset();
            }
        }
        return packageOffset;
    }

    /**
     * Returns line index (line number - 1) of the last import statement or {@literal -1}
     * if no import statement was found within this in {@link BaseDocument}.
     *
     * @param doc document
     * @return line index (line number - 1) of the last import statement or {@literal -1}
     *         if no import statement was found within this {@link BaseDocument}.
     */
    private static int getLastImportLineIndex(BaseDocument doc) {
        try {
            int lastImportOffset = getLastImportStatementOffset(doc);
            if (lastImportOffset != -1) {
                return LineDocumentUtils.getLineIndex(doc, lastImportOffset);
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return -1;
    }

    /**
     * Returns offset of the last import statement or {@literal -1} if no import
     * statement was found within this in {@link BaseDocument}.
     *
     * @param doc document
     * @return offset of the last import statement or {@literal -1} if no import
     *         statement was found within this {@link BaseDocument}.
     */
    private static int getLastImportStatementOffset(BaseDocument doc) {
        TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, 1);

        int importEnd = -1;

        while (ts.moveNext()) {
            if (ts.token().id() == GroovyTokenId.LITERAL_import) {
                importEnd = ts.offset();
            }
        }
        return importEnd;
    }
}
