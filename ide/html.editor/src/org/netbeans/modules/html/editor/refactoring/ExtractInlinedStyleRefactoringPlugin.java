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
package org.netbeans.modules.html.editor.refactoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position.Bias;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.csl.spi.support.ModificationResult.Difference;
import org.netbeans.modules.css.refactoring.api.CssRefactoring;
import org.netbeans.modules.css.refactoring.api.Entry;
import org.netbeans.modules.css.refactoring.api.RefactoringElementType;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.html.editor.HtmlSourceUtils;
import org.netbeans.modules.html.editor.lib.api.HtmlParsingResult;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.html.editor.refactoring.api.ExtractInlinedStyleRefactoring;
import org.netbeans.modules.html.editor.refactoring.api.SelectorType;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringCommit;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @todo Define some pattern for the generation the class and id selector in the
 * css options.
 *
 * @author marekfukala
 */
public class ExtractInlinedStyleRefactoringPlugin implements RefactoringPlugin {

    private boolean cancelled;
    private ExtractInlinedStyleRefactoring refactoring;

    public ExtractInlinedStyleRefactoringPlugin(ExtractInlinedStyleRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    //TODO implement the checks!
    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    public void cancelRequest() {
        cancelled = true;
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        if (cancelled) {
            return null;
        }
        ModificationResult modificationResult = new ModificationResult();
        RefactoringContext context = refactoring.getRefactoringSource().lookup(RefactoringContext.class);
        assert context != null;

        switch (refactoring.getMode()) {
            case refactorToExistingEmbeddedSection:
                OffsetRange sectionRange = refactoring.getExistingEmbeddedCssSection();
                if (sectionRange == null) {
                    return new Problem(true, NbBundle.getMessage(ExtractInlinedStyleRefactoringPlugin.class, "MSG_ErrorCannotDetermineEmbeddedSectionEnd"));
                } else {
                    refactorToEmbeddedSection(modificationResult, context, sectionRange.getEnd());
                }
                break;
            case refactorToNewEmbeddedSection:
                refactorToNewEmbeddedSection(modificationResult, context);
                break;
            case refactorToReferedExternalSheet:
                refactorToStyleSheet(modificationResult, context);
                break;
            case refactorToExistingExternalSheet:
                if (refactorToStyleSheet(modificationResult, context)) {
                importStyleSheet(modificationResult, context);
            }
                break;
        }

        refactoringElements.registerTransaction(new RefactoringCommit(Collections.singletonList(modificationResult)));

        for (FileObject fo : modificationResult.getModifiedFileObjects()) {
            for (Difference diff : modificationResult.getDifferences(fo)) {
                refactoringElements.add(refactoring, DiffElement.create(diff, fo, modificationResult));
            }
        }

        return null;

    }

    private boolean importStyleSheet(ModificationResult modificationResult, RefactoringContext context) {
        FileObject target = refactoring.getExternalSheet();
        return HtmlSourceUtils.importStyleSheet(modificationResult, 
                context.getModel().getParserResult(), 
                context.getModel().getSnapshot(), target);
    }

    private boolean refactorToStyleSheet(ModificationResult modificationResult, RefactoringContext context) {
        Document extSheetDoc = GsfUtilities.getDocument(refactoring.getExternalSheet(), true);

        int insertOffset = extSheetDoc.getLength();
        int baseIndent = getPreviousLineIndent(extSheetDoc, insertOffset);

        return refactorToEmbeddedSection(modificationResult, context, refactoring.getExternalSheet(),
                insertOffset, baseIndent, null, null);
    }

    private boolean refactorToNewEmbeddedSection(ModificationResult modifications, RefactoringContext context) {
        try {
            //create a new embedded css section
            HtmlParsingResult result = context.getModel().getParserResult();

            final AtomicInteger insertPositionRef = new AtomicInteger(-1);
            final AtomicBoolean increaseIndent = new AtomicBoolean();

            //jsf hack - we need to put the generated <link/> or <style/> sections to the proper place,
            //which is <h:head> tag in case of JSF. Ideally there should be an SPI which the frameworks
            //would implement and which would provide a default places for such elements.
            Node jsfHtmlLibRoot = result.root("http://java.sun.com/jsf/html"); //NOI18N
            if (jsfHtmlLibRoot != null) {
                ElementUtils.visitChildren(jsfHtmlLibRoot, new ElementVisitor() {
                    @Override
                    public void visit(Element node) {
                        //assume <h:head>
                        OpenTag t = (OpenTag) node;
                        if (LexerUtils.equals("head", t.unqualifiedName(), true, true)) { //NOI18N
                            //append the section as first head's child if there are
                            //no existing link attribute
                            insertPositionRef.set(node.to()); //end of the open tag offset
                            increaseIndent.set(true);
                        }
                    }
                }, ElementType.OPEN_TAG);

            }

            Node root = result.root();
            ElementUtils.visitChildren(root, new ElementVisitor() {
                @Override
                public void visit(Element node) {
                    OpenTag t = (OpenTag) node;
                    if (LexerUtils.equals("html", t.name(), true, true)) {
                        if (insertPositionRef.get() == -1) { //h:head already found?
                            //append the section as first html's child if there are
                            //no existing link attribute and head tag
                            insertPositionRef.set(node.to()); //end of the open tag offset
                            increaseIndent.set(true);
                        }
                    } else if (LexerUtils.equals("head", t.name(), true, true)) {
                        //NOI18N
                        //append the section as first head's child if there are
                        //no existing style sections
                        insertPositionRef.set(node.to()); //end of the open tag offset
                        increaseIndent.set(true);
                    } else if (LexerUtils.equals("style", t.name(), true, true)) {
                        //NOI18N
                        //existing style section
                        //append the new section after the last one
                        insertPositionRef.set(t.semanticEnd()); //end of the end tag offset
                        increaseIndent.set(false);
                    }
                }
            }, ElementType.OPEN_TAG);

            int embeddedInsertOffset = insertPositionRef.get();
            if (embeddedInsertOffset
                    == -1) {
                //TODO probably missing head tag? - generate? html tag may be missing as well
                return false;
            }
            int insertOffset = context.getModel().getSnapshot().getOriginalOffset(embeddedInsertOffset);
            if (insertOffset
                    == -1) {
                return false; //cannot properly map back
            }
            int baseIndent = Utilities.getRowIndent((BaseDocument) context.getDocument(), insertOffset);
            if (baseIndent
                    == -1) {
                //in case of empty line
                baseIndent = 0;
            }

            if (increaseIndent.get()) {
                //add one indent level (after HEAD open tag)
                baseIndent += IndentUtils.indentLevelSize(context.getDocument());
            }
            //generate the embedded id selector section
            String baseIndentString = IndentUtils.createIndentString(context.getDocument(), baseIndent);
            String prefix = new StringBuilder().append('\n').append(baseIndentString).append("<style type=\"text/css\">\n").toString(); //NOI18N
            String postfix = new StringBuilder().append('\n').append(baseIndentString).append("</style>").toString(); //NOI18N

            return refactorToEmbeddedSection(modifications, context, context.getFile(), insertOffset, baseIndent, prefix, postfix);

        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        return false;
    }

    private boolean refactorToEmbeddedSection(ModificationResult modifications, RefactoringContext context, final int insertOffset) {
        int baseIndent = getPreviousLineIndent(context.getDocument(), insertOffset);
        return refactorToEmbeddedSection(modifications, context, context.getFile(), insertOffset, baseIndent, null, null);
    }

    private boolean refactorToEmbeddedSection(ModificationResult modifications, RefactoringContext context,
            FileObject targetStylesheet,
            int insertOffset, int baseIndent, String prefix, String postfix) {
        List<InlinedStyleInfo> inlinedStyles = context.getInlinedStyles();
        CloneableEditorSupport currentFileEditor = GsfUtilities.findCloneableEditorSupport(context.getFile());
        List<Difference> diffs = new LinkedList<>();

        StringBuilder generatedSelectorsSection = new StringBuilder();
        if (prefix != null) {
            generatedSelectorsSection.append(prefix);
        }

        //TODO consolidate the SelectorType and RefactoringElementType
        SelectorType selectorType = refactoring.getSelectorType();
        RefactoringElementType cssElementType = getCssElementType(selectorType);

        Map<InlinedStyleInfo, ResolveDeclarationItem> resolvedDeclarations
                = selectorType == SelectorType.CLASS ? context.getClassSelectorsToResolve() : context.getIdSelectorsToResolve();

        boolean atLeastOneRefactorToDefaultLocation = false;

        //we need to remember all used (and possibly generated) class or id selector names so we can avoid clashes
        //remember such list per each file
        Map<SelectorType, Map<FileObject, Collection<String>>> usedNames = new HashMap<>();
        usedNames.put(SelectorType.CLASS, new HashMap<FileObject, Collection<String>>());
        usedNames.put(SelectorType.ID, new HashMap<FileObject, Collection<String>>());

        usedNames.get(selectorType).put(context.getFile(), getAllSelectorNames(context.getFile(), cssElementType));
        usedNames.get(selectorType).put(targetStylesheet, getAllSelectorNames(targetStylesheet, cssElementType));

        for (InlinedStyleInfo si : inlinedStyles) {
            try {

                ResolveDeclarationItem declaration = resolvedDeclarations.get(si);
                if (declaration != null) {
                    //the css code is inlined in a tag with ID/CLASS attribute already defined
                    if (selectorType == SelectorType.ID) {
                        DeclarationItem resolvedDeclaration = declaration.getResolvedTarget();

                        if (resolvedDeclaration == null) {
                            //no declaration of the selector found in the project,
                            //we need to generate  a new one.
                            List<String> lines = new ArrayList<>();
                            lines.add(""); //empty line = will add new line
                            lines.add(new StringBuilder().append('.').append(si.getTagsId()).append('{').toString()); //NOI18N
                            appendConvertedInlinedCodeLines(lines, si);
                            lines.add("}"); //NOI18N

                            //if prefix is set indent the content by one level
                            String idSelectorText = formatCssCode(context.getDocument(), baseIndent, prefix == null ? 0 : 1, lines.toArray(new String[]{}));
                            generatedSelectorsSection.append(idSelectorText);

                            atLeastOneRefactorToDefaultLocation = true;

                        } else {
                            FileObject file = resolvedDeclaration.getSource();
                            final BaseDocument doc = (BaseDocument) resolvedDeclaration.getDocument();
                            Entry entry = resolvedDeclaration.getDeclaration().entry();
                            //In case of id selector, just add the code to the refered selector body and remove the inlined style

                            //get the selector's body range { ... }
                            OffsetRange docBodyRange = entry.getDocumentBodyRange();
                            if (docBodyRange == null) {
                                //cannot refactor this inlined style
                                continue;
                            }

                            //where to put the moved code
                            final AtomicInteger appendOffset = new AtomicInteger(docBodyRange.getStart()); //points to the open curly bracket '{'
                            //put the section to the next line beginning
                            doc.render(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        if (Utilities.getFirstNonWhiteFwd(doc, appendOffset.get()) == -1) {
                                            //just WS at the rest of the line
                                            //=>put the section at the beginning of the next line
                                            int newPos = Utilities.getRowEnd(doc, appendOffset.get()) + 1;
                                            appendOffset.set(newPos);
                                        } else {
                                            //put right after the open curly bracket
                                            appendOffset.incrementAndGet();
                                        }
                                    } catch (BadLocationException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }

                            });

                            //get the indentation from the selector's line indent + base indent
                            int prevLineIndent = getPreviousLineIndent(doc, appendOffset.get());
                            List<String> lines = new LinkedList<>();
                            lines.add(""); //empty line = will add new line
                            appendConvertedInlinedCodeLines(lines, si);

                            String addedCode = formatCssCode(context.getDocument(), prevLineIndent, 0, lines.toArray(new String[]{}));

                            //append the code to the existing selector
                            CloneableEditorSupport editor = GsfUtilities.findCloneableEditorSupport(file);
                            Difference appendDiff = new Difference(Difference.Kind.INSERT,
                                    editor.createPositionRef(appendOffset.get(), Bias.Forward),
                                    editor.createPositionRef(appendOffset.get(), Bias.Backward),
                                    null,
                                    addedCode,
                                    NbBundle.getMessage(ExtractInlinedStyleRefactoringPlugin.class, "MSG_AppendCssCodeToExistingSelector")); //NOI18N

                            modifications.addDifferences(file, Collections.singletonList(appendDiff));
                        }
                        //remove the inlined code
                        int deleteFrom = si.getAttributeStartOffset();
                        int deleteTo = si.getRange().getEnd() + (si.isValueQuoted() ? 1 : 0);
                        String originalText = context.getDocument().getText(deleteFrom, deleteTo - deleteFrom);
                        Difference removeDiff = new Difference(Difference.Kind.REMOVE,
                                currentFileEditor.createPositionRef(deleteFrom, Bias.Forward),
                                currentFileEditor.createPositionRef(deleteTo, Bias.Backward),
                                originalText,
                                null,
                                NbBundle.getMessage(ExtractInlinedStyleRefactoringPlugin.class, "MSG_RemoveInlinedCode")); //NOI18N

                        //modification of the edited file, will be added to the modification result later
                        diffs.add(removeDiff);

                    } else if (selectorType == SelectorType.CLASS) {
                        //In case of class selector type we need to create a new one and add it to the existing class reference
                        //  <div class="original newclass" ... />

                        //XXX Reconsider - The newly generated classes are not be added to the same location as the already
                        //existing, but to the default target selected by the user
                        //find first free selector name
                        Collection<String> editedFileElements = usedNames.get(selectorType).get(context.getFile());
                        Collection<String> targetFileElements = usedNames.get(selectorType).get(targetStylesheet);
                        //the call to getFirstFreeSelectorName also updates the given collections -
                        //adds the new free element name into all of them
                        String generatedSelectorName = getFirstFreeSelectorName(
                                selectorType, si.getTag(), editedFileElements, targetFileElements);

                        //add the new generated class to the default css code section
                        String selectorNamePrefix = (selectorType == SelectorType.CLASS ? "." : "#");
                        List<String> lines = new ArrayList<>();
                        lines.add(""); //empty line = will add new line
                        lines.add(new StringBuilder().append(selectorNamePrefix).append(generatedSelectorName).append('{').toString()); //NOI18N
                        appendConvertedInlinedCodeLines(lines, si);
                        lines.add("}"); //NOI18N

                        //if prefix is set indent the content by one level
                        String idSelectorText = formatCssCode(context.getDocument(), baseIndent, prefix == null ? 0 : 1, lines.toArray(new String[]{}));
                        generatedSelectorsSection.append(idSelectorText);
                        atLeastOneRefactorToDefaultLocation = true;

                        //remove the inlined code
                        int deleteFrom = si.getAttributeStartOffset();
                        int deleteTo = si.getRange().getEnd() + (si.isValueQuoted() ? 1 : 0);
                        String originalText = context.getDocument().getText(deleteFrom, deleteTo - deleteFrom);
                        Difference removeDiff = new Difference(Difference.Kind.REMOVE,
                                currentFileEditor.createPositionRef(deleteFrom, Bias.Forward),
                                currentFileEditor.createPositionRef(deleteTo, Bias.Backward),
                                originalText,
                                null,
                                NbBundle.getMessage(ExtractInlinedStyleRefactoringPlugin.class, "MSG_RemoveInlinedCode")); //NOI18N

                        int appendToPosition = si.getClassValueAppendOffset();
                        String toAdd = " " + generatedSelectorName;

                        //append the new class name behind the existing one
                        Difference appendDiff = new Difference(Difference.Kind.INSERT,
                                currentFileEditor.createPositionRef(appendToPosition, Bias.Forward),
                                currentFileEditor.createPositionRef(appendToPosition, Bias.Backward),
                                null,
                                toAdd,
                                NbBundle.getMessage(ExtractInlinedStyleRefactoringPlugin.class, "MSG_AddClassSelectorReference")); //NOI18N

                        //modification of the edited file, will be added to the modification result later
                        diffs.add(removeDiff);
                        diffs.add(appendDiff);

                    }

                } else {
                    //find first free selector name
                    Collection<String> editedFileElements = usedNames.get(selectorType).get(context.getFile());
                    Collection<String> targetFileElements = usedNames.get(selectorType).get(targetStylesheet);
                    //the call to getFirstFreeSelectorName also updates the given collections -
                    //adds the new free element name into all of them

                    //replace the namespace postfix by something allowed in css selector name
                    String tagName = si.getTag().replace(":", "_"); //NOI18N

                    String generatedSelectorName = getFirstFreeSelectorName(
                            selectorType, tagName, editedFileElements, targetFileElements);

                    //delete the inlined style - attribute name, equal sign, whitespaces and the value
                    //and replace with id selector reference
                    int deleteFrom = si.getAttributeStartOffset();
                    int deleteTo = si.getRange().getEnd() + (si.isValueQuoted() ? 1 : 0);
                    String selectorName = (selectorType == SelectorType.CLASS ? "class" : "id");
                    String idSelectorUsageText = selectorName + "=\"" + generatedSelectorName + "\""; //NOI18N
                    String originalText = context.getDocument().getText(deleteFrom, deleteTo - deleteFrom);

                    Difference diff;
                    if (si.getRange().isEmpty()) {
                        //empty value of the style attribute - just delete
                        diff = new Difference(Difference.Kind.REMOVE,
                                currentFileEditor.createPositionRef(deleteFrom, Bias.Forward),
                                currentFileEditor.createPositionRef(deleteTo, Bias.Backward),
                                originalText,
                                null,
                                NbBundle.getMessage(ExtractInlinedStyleRefactoringPlugin.class, "MSG_RemoveEmptyStyleAttribute")); //NOI18N
                    } else {
                        diff = new Difference(Difference.Kind.CHANGE,
                                currentFileEditor.createPositionRef(deleteFrom, Bias.Forward),
                                currentFileEditor.createPositionRef(deleteTo, Bias.Backward),
                                originalText,
                                idSelectorUsageText,
                                NbBundle.getMessage(ExtractInlinedStyleRefactoringPlugin.class, "MSG_ReplaceInlinedStyleWithIdSelectorReference")); //NOI18N

                        List<String> lines = new ArrayList<>();
                        lines.add(""); //empty line = will add new line

                        String selectorNamePrefix = (selectorType == SelectorType.CLASS ? "." : "#");
                        lines.add(new StringBuilder().append(selectorNamePrefix).append(generatedSelectorName).append('{').toString()); //NOI18N

                        appendConvertedInlinedCodeLines(lines, si);

                        lines.add("}"); //NOI18N

                        //if prefix is set indent the content by one level
                        String idSelectorText = formatCssCode(context.getDocument(), baseIndent, prefix == null ? 0 : 1, lines.toArray(new String[]{}));
                        generatedSelectorsSection.append(idSelectorText);
                    }

                    diffs.add(diff);

                    atLeastOneRefactorToDefaultLocation = true;
                }

            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        modifications.addDifferences(context.getFile(), diffs);

        if (atLeastOneRefactorToDefaultLocation) {
            //not only inlined code moves to already existing class/id have been performed
            if (postfix != null) {
                generatedSelectorsSection.append(postfix);
            }
            //generate the cumulated embedded id selector section
            CloneableEditorSupport targetStylesheetEditor = GsfUtilities.findCloneableEditorSupport(targetStylesheet);
            modifications.addDifferences(targetStylesheet, Collections.singletonList(new Difference(Difference.Kind.INSERT,
                    targetStylesheetEditor.createPositionRef(insertOffset, Bias.Forward),
                    targetStylesheetEditor.createPositionRef(insertOffset, Bias.Backward),
                    null,
                    generatedSelectorsSection.toString(),
                    NbBundle.getMessage(ExtractInlinedStyleRefactoringPlugin.class, "MSG_GenerateIDSelectors")))); //NOI18N

            return true; //signal that some of the defaumt moves have been done
        } else {
            return false;
        }
    }

    //TODO there should be a generic facility allowing to reformat a piece of code
    //according to the css formatter options. I could possibly invoke the formatter
    //on an artificial document with the new code content, but since we do not have the
    //pretty printer it would not help much.
    private String formatCssCode(Document doc, int baseIndent, int additionalIndent, String... lines) {
        StringBuilder b = new StringBuilder();

        int indentLevelSize = IndentUtils.indentLevelSize(doc);

        for (String line : lines) {
            //add base indent
            b.append(IndentUtils.createIndentString(doc, baseIndent));

            String indentString = IndentUtils.createIndentString(doc, indentLevelSize);
            //append additional indents
            for (int i = 0; i < additionalIndent; i++) {
                b.append(indentString);
            }

            //replace each \t by proper indentation level size
            //and copy the line to the buffer
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '\t') { //NOI18N
                    b.append(indentString);
                } else if (c == '\n') {
                    //swallow the new lines if they were possibly present in the inlined css code
                } else {
                    b.append(c);
                }
            }

            //and new line at the end
            b.append('\n'); //NOI18N
        }

        return b.toString();
    }

    private static int getPreviousLineIndent(final Document doc, final int insertOffset) {
        final AtomicInteger ret = new AtomicInteger(0); //default is 0 indent if something fails in the later runnable
        doc.render(new Runnable() {
            @Override
            public void run() {
                try {
                    //find last nonwhite line indent
                    int firstNonWhiteBw = Utilities.getFirstNonWhiteBwd((BaseDocument) doc, insertOffset);
                    //get the line indent
                    ret.set(firstNonWhiteBw == -1 ? 0 : Utilities.getRowIndent((BaseDocument) doc, firstNonWhiteBw));
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }

            }
        });

        int indent = ret.get();
        return indent == -1 ? 0 : indent; //the Utilities.getRowIndent() returns -1 for blank line
    }

    private static RefactoringElementType getCssElementType(SelectorType selectorType) {
        switch (selectorType) {
            case CLASS:
                return RefactoringElementType.CLASS;
            case ID:
                return RefactoringElementType.ID;
            default:
                return null;
        }
    }

    private static Collection<String> getSelectorNames(Collection<Entry> entries) {
        Collection<String> names = new ArrayList<>(entries.size());
        for (Entry e : entries) {
            names.add(e.getName());
        }
        return names;
    }

    private static Collection<String> getAllSelectorNames(FileObject file, RefactoringElementType type) {
        return getSelectorNames(CssRefactoring.getAllSelectors(file, type));
    }

    //find first free selector name
    private static String getFirstFreeSelectorName(
            SelectorType selectorType, String tagName, Collection<String>... names) {

        String selectorName = (selectorType == SelectorType.CLASS ? "class" : "id"); //NOI18N
        String selectorNameBase = tagName + selectorName;
        String generatedSelectorName;

        //merge all collections
        Collection<String> allElements = new ArrayList<>();
        for (Collection<String> namesCol : names) {
            allElements.addAll(namesCol);
        }

        //find first free name
        int counter = 0;
        while (allElements.contains(generatedSelectorName = selectorNameBase + (counter++ == 0 ? "" : counter))) {
        }

        //store the name back to all the names collections
        for (Collection<String> namesCol : names) {
            namesCol.add(generatedSelectorName);
        }

        return generatedSelectorName;
    }

    private static void appendConvertedInlinedCodeLines(List<String> lines, InlinedStyleInfo si) {
        for (String parsedDeclaration : si.getParsedDeclarations()) {
            StringBuilder b = new StringBuilder();
            b.append('\t'); //NOI18N
            b.append(parsedDeclaration);
            if (!parsedDeclaration.endsWith(";")) { //NOI18N
                b.append(';'); //NOI18N
            }
            lines.add(b.toString());
        }
    }
}
