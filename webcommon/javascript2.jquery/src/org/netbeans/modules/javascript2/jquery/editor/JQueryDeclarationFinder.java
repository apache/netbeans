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
package org.netbeans.modules.javascript2.jquery.editor;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.refactoring.api.CssRefactoring;
import org.netbeans.modules.css.refactoring.api.EntryHandle;
import org.netbeans.modules.css.refactoring.api.RefactoringElementType;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.editor.spi.DeclarationFinder;
import org.netbeans.modules.javascript2.jquery.model.JQueryUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Pisl
 */
@DeclarationFinder.Registration(priority=10)
public class JQueryDeclarationFinder implements DeclarationFinder {

    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        if (JQueryUtils.isJQuery(info, caretOffset)) {
            TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(info.getSnapshot().getTokenHierarchy(), caretOffset);
            Rule rule = getRule(ts, caretOffset);
            if (rule != null) {
                RefactoringElementType type = rule.rule.charAt(0) == '#' ? RefactoringElementType.ID : RefactoringElementType.CLASS;
                Map<FileObject, Collection<EntryHandle>> findAll = CssRefactoring.findAllOccurances(rule.rule.substring(1), type, info.getSnapshot().getSource().getFileObject(), true);
                if (findAll == null) {
                    return DeclarationLocation.NONE;
                }
                DeclarationLocation dl = null;
                for (Map.Entry<FileObject, Collection<EntryHandle>> entry : findAll.entrySet()) {
                    FileObject f = entry.getKey();
                    Collection<EntryHandle> entries = entry.getValue();
                    for (EntryHandle entryHandle : entries) {
                        //grrr, the main declarationlocation must be also added to the alternatives
                        //if there are more than one
                        DeclarationLocation dloc = new DeclarationLocation(f, entryHandle.entry().getDocumentRange().getStart());
                        if (dl == null) {
                            //ugly DeclarationLocation alternatives handling workaround - one of the
                            //locations simply must be "main"!!!
                            dl = dloc;
                        }
                        AlternativeLocation aloc = new AlternativeLocationImpl(dloc, entryHandle, type);
                        dl.addAlternative(aloc);
                    }
                }

                //and finally if there was just one entry, remove the "alternative"
                if (dl != null && dl.getAlternativeLocations().size() == 1) {
                    dl.getAlternativeLocations().clear();
                }

                if (dl != null) {
                    return dl;
                }
            }
            
        }
        return DeclarationLocation.NONE;
    }

    @Override
    public OffsetRange getReferenceSpan(final Document doc, final int caretOffset) {
        final OffsetRange[] value = new OffsetRange[1];
        doc.render(new Runnable() {

            @Override
            public void run() {
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, caretOffset);
                Rule rule = getRule(ts, caretOffset);
                if (rule != null) {
                    value[0] = new OffsetRange(rule.startOffset, rule.endOffset);
                } else {
                    value[0] = OffsetRange.NONE;
                }   
            }
        });
        
        return value[0];
    }
   
    private static class Rule {
        String rule;
        int startOffset;
        int endOffset;

        public Rule(String rule, int startOffset, int endOffset) {
            this.rule = rule;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
        
        
    }
    
    private Rule getRule(TokenSequence<? extends JsTokenId> ts, int caretOffset) {
        if (ts == null) {
            return null;
        }
        ts.move(caretOffset);
        if (!(ts.movePrevious() && ts.moveNext())) {
            return null;
        }
        Token<? extends JsTokenId> token = ts.token();
        if (token.id() == JsTokenId.STRING) {
            String text = token.text().toString();
            if (text.indexOf(' ') == -1 && text.indexOf('/') > -1) {
                // probably the string is not a rule, but path to a file
                return null;
            }
            boolean isRule = false;
            int offset = caretOffset - ts.offset();
            int startRule = -1;
            while(offset > -1 && !isRule) {
                char ch = text.charAt(offset);
                if(ch == '.' || ch == '#') {
                    isRule = true;
                    startRule = offset;
                } else if (ch == ' ' || ch == ':' || ch == '[') {
                    offset = 0;
                }
                offset --;
            }
            if (isRule) {
                
                int endRule = -1;
                offset = startRule + 1;
                while(offset < text.length()) {
                    char ch = text.charAt(offset);
                    if(ch == ' ' || ch == '[' || ch == '.' || ch == '#' || ch == ':') {
                        endRule = offset;
                        offset = text.length();
                    }
                    offset++;
                }
                if (endRule == -1) {
                    endRule = text.length();
                }
                return new Rule(text.substring(startRule, endRule), ts.offset() + startRule, ts.offset() + endRule);
            }
        }
        return null;
    }
    
    //useless class just because we need to put something into the AlternativeLocation to be
    //able to get some icon from it
    private static final CssSelectorElementHandle CSS_SELECTOR_ELEMENT_HANDLE_SINGLETON = new CssSelectorElementHandle();

    // Note: this class has a natural ordering that is inconsistent with equals.
    // We have to implement AlternativeLocation
    @org.netbeans.api.annotations.common.SuppressWarnings("EQ_COMPARETO_USE_OBJECT_EQUALS")
    private static class AlternativeLocationImpl implements AlternativeLocation {

        private DeclarationLocation location;
        private EntryHandle entryHandle;
        private RefactoringElementType type;
        private static final int SELECTOR_TEXT_MAX_LENGTH = 50;

        public AlternativeLocationImpl(DeclarationLocation location, EntryHandle entry, RefactoringElementType type) {
            this.location = location;
            this.entryHandle = entry;
            this.type = type;
        }

        @Override
        public ElementHandle getElement() {
            return CSS_SELECTOR_ELEMENT_HANDLE_SINGLETON;
        }

        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
            StringBuilder b = new StringBuilder();
            //colorize the 'current line text' a bit
            //find out if there's the opening curly bracket
            String lineText = entryHandle.entry().getLineText().toString();
            assert lineText != null;
            int curlyBracketIndex = lineText.indexOf('{'); //NOI18N
            String croppedLineText = curlyBracketIndex == -1 ? lineText : lineText.substring(0, curlyBracketIndex);

            //split the text to three parts: the element text itself, its prefix and postfix
            //then render the element test in bold
            String elementTextPrefix;
            switch (type) {
                case CLASS:
                    elementTextPrefix = "."; //NOI18N
                    break;
                case ID:
                    elementTextPrefix = "#"; //NOI18N
                    break;
                default:
                    elementTextPrefix = "";
            }
            String elementText = elementTextPrefix + entryHandle.entry().getName();
            int elementTextIndex = croppedLineText.indexOf(elementText);
            if(elementTextIndex == -1) {
                String msg = "A parsing error occured when trying to extract display name for html declaration finder."
                        + "elementText='" + elementText
                        + "'; lineText='" + lineText + "'; croppedLineText='"
                        + croppedLineText + "'; elementTextPrefix='" + elementTextPrefix + "'"; //NOI18N
                Logger.getAnonymousLogger().log(Level.INFO, msg, new IllegalStateException());//NOI18N

                return entryHandle.entry().getName();
            }
            String prefix = croppedLineText.substring(0, elementTextIndex).trim();
            String postfix = croppedLineText.substring(elementTextIndex + elementText.length()).trim();

            //now strip the prefix and postfix so the whole text is not longer than SELECTOR_TEXT_MAX_LENGTH
            int overlap = croppedLineText.length() - SELECTOR_TEXT_MAX_LENGTH;
            if (overlap > 0) {
                //strip
                int stripFromPrefix = Math.min(overlap / 2, prefix.length());
                prefix = ".." + prefix.substring(stripFromPrefix);
                int stripFromPostfix = Math.min(overlap - stripFromPrefix, postfix.length());
                postfix = postfix.substring(0, postfix.length() - stripFromPostfix) + "..";
            }

            b.append("<font color=007c00>");//NOI18N
            b.append(prefix);
            b.append(' '); //NOI18N
            b.append("<b>"); //NOI18N
            b.append(elementText);
            b.append("</b>"); //NOI18N
            b.append(' '); //NOI18N
            b.append(postfix);
            b.append("</font> in "); //NOI18N

            //add a link to the file relative to the web root
            FileObject file = location.getFileObject();
            Project project = FileOwnerQuery.getOwner(file);
            FileObject pathRoot = null; // ProjectWebRootQuery.getWebRoot(file);

            if (project != null) {
                pathRoot = project.getProjectDirectory();
            }
            
            String path = null;
            String resolveTo = null;
            if (pathRoot != null) {
                path = FileUtil.getRelativePath(pathRoot, file); //this may also return null
            }
            if (path == null) {
                //the file cannot be resolved relatively to the webroot or no webroot found
                //try to resolve relative path to the project's root folder
                if (project != null) {
                    pathRoot = project.getProjectDirectory();
                    path = FileUtil.getRelativePath(pathRoot, file); //this may also return null
                    if (path != null) {
                        resolveTo = "${project.home}/"; //NOI18N
                    }
                }
            }

            if (path == null) {
                //if everything fails, just use the absolute path
                path = file.getPath();
            }

            if (resolveTo != null) {
                b.append("<i>"); //NOI18N
                b.append(resolveTo);
                b.append("</i>"); //NOI18N
            }
            b.append(path);
            int lineOffset = entryHandle.entry().getLineOffset();
            if (lineOffset != -1) {
                b.append(":"); //NOI18N
                b.append(lineOffset + 1); //line offsets are counted from zero, but in editor lines starts with one.
            }
            return b.toString();
        }

        @Override
        public DeclarationLocation getLocation() {
            return location;
        }

        @Override
        public int compareTo(AlternativeLocation o) {
            //compare according to the file paths
            return getComparableString(this).compareTo(getComparableString(o));
        }

        private static String getComparableString(AlternativeLocation loc) {
            StringBuilder sb = new StringBuilder();
            sb.append(loc.getLocation().getOffset()); //offset
            FileObject fo = loc.getLocation().getFileObject();
            if (fo != null) {
                sb.append(fo.getPath()); //filename
            }
            return sb.toString();
        }
    }
    
    private static class CssSelectorElementHandle implements ElementHandle {

        @Override
        public FileObject getFileObject() {
            return null;
        }

        @Override
        public String getMimeType() {
            return null;
        }

        @Override
        public String getName() {
            return ""; // NOI18N
        }

        @Override
        public String getIn() {
            return null;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.RULE;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return false;
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return OffsetRange.NONE;
        }
    }
}
