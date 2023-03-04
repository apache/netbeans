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
package org.netbeans.modules.html.editor.indexing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import org.netbeans.modules.html.editor.completion.AttrValuesCompletion;
import org.netbeans.modules.html.editor.lib.api.HtmlParsingResult;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.parsing.api.*;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.ValueCompletion;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class HtmlFileModel {

    private static final String STYLE_TAG_NAME = "style"; //NOI18N
    private static final Logger LOGGER = Logger.getLogger(HtmlFileModel.class.getSimpleName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    private List<HtmlLinkEntry> references;
    private List<OffsetRange> embeddedCssSections;
    private HtmlParsingResult htmlResult;
    private Parser.Result parserResult;

    public HtmlFileModel(Source source) throws ParseException {
        ParserManager.parse(Collections.singletonList(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                ResultIterator ri = WebUtils.getResultIterator(resultIterator, HtmlKit.HTML_MIME_TYPE);
                if (ri != null) {
                    parserResult = ri.getParserResult();
                    htmlResult = (HtmlParsingResult) ri.getParserResult();
                    init();
                }
            }
        });
    }

    public HtmlFileModel(Parser.Result parserResult, HtmlParsingResult htmlResult) {
        this.parserResult = parserResult;
        this.htmlResult = htmlResult;
        init();
    }

    public HtmlParsingResult getParserResult() {
        return htmlResult;
    }

    public Snapshot getSnapshot() {
        return parserResult.getSnapshot();
    }

    public FileObject getFileObject() {
        return getSnapshot().getSource().getFileObject();
    }

    public List<HtmlLinkEntry> getReferences() {
        return references == null ? Collections.<HtmlLinkEntry>emptyList() : references;
    }

    public List<OffsetRange> getEmbeddedCssSections() {
        return embeddedCssSections == null ? Collections.<OffsetRange>emptyList() : embeddedCssSections;
    }

    /**
     *
     * @return true if the model is empty - nothing interesting found in the
     * page.
     */
    public boolean isEmpty() {
        return null == references;
    }

    private List<HtmlLinkEntry> getReferencesCollectionInstance() {
        if (references == null) {
            references = new ArrayList<>();
        }
        return references;
    }

    private List<OffsetRange> getEmbeddedCssSectionsCollectionInstance() {
        if (embeddedCssSections == null) {
            embeddedCssSections = new ArrayList<>();
        }
        return embeddedCssSections;
    }

    private void init() {
        Iterator<Element> elements = htmlResult.getSyntaxAnalyzerResult().getElementsIterator();
        while (elements.hasNext()) {
            Element element = elements.next();
            switch (element.type()) {
                case OPEN_TAG:
                    handleOpenTag((OpenTag) element);
                    break;
                case CLOSE_TAG:
                    handleCloseTag((CloseTag) element);
            }
        }
    }

    private void handleOpenTag(OpenTag tnode) {
        handleReference(tnode);
        handleEmbeddedCssSectionStart(tnode);
    }

    private void handleCloseTag(CloseTag closeTag) {
        handleEmbeddedCssSectionEnd(closeTag);
    }
    
    private int embedded_css_section_start = -1;

    private void handleEmbeddedCssSectionStart(OpenTag tnode) {
        //check if the tag can contain css code
        if (LexerUtils.equals(STYLE_TAG_NAME, tnode.name(), true, true)) { //NOI18N
            //XXX maybe we should also check the type attribute for text/css mimetype
            if (!tnode.isEmpty()) {
                embedded_css_section_start = tnode.to();
            }
        }
    }

    private void handleEmbeddedCssSectionEnd(CloseTag closeTag) {
        if (embedded_css_section_start == -1) {
            //bug, relax
            return;
        }
        int embedded_css_section_end = closeTag.from();

        //Bug 212445 - AssertionError: Invalid start:591 end:583
        if (embedded_css_section_start > embedded_css_section_end) {
            dumpIssue212445DebugInfo(embedded_css_section_start);
        }

        getEmbeddedCssSectionsCollectionInstance().add(new OffsetRange(embedded_css_section_start, embedded_css_section_end));
        embedded_css_section_start = -1;
    }

    private void dumpIssue212445DebugInfo(int from) {
        //it looks like under some circumstances the <style> open
        //tag end offset "to()" points to the end of the subsequent
        //close tag </style>. Since in most of the reports 
        //negative difference is -8 which is "</style>".length()
        //it looks like the errorneous case happens 
        //for <style></style> element's pair. But since I cannot 
        //reproduce it I'll just add here a new assertion which
        //shows the text causing the issue.
        StringBuilder msg = new StringBuilder();
        msg.append("A bug #212445 just happended for source text \"");//NOI18N
        CharSequence source = getSnapshot().getText();
        CharSequence sample = source.subSequence(
                Math.max(0, from - 50),
                Math.min(source.length(), from + 50));

        msg.append(sample);
        msg.append("\". ");//NOI18N
        msg.append("Please report a new bug or reopen the existing issue #212445 "
                + "and attach this exception + ideally the whole file \"");//NOI18N

        FileObject file = getSnapshot().getSource().getFileObject();
        msg.append(file == null ? "???" : file.getPath());

        msg.append("\" there. Thank you for your help!");//NOI18N

        throw new IllegalStateException(msg.toString());
    }

    private void handleReference(OpenTag tnode) {
        //XXX This is HTML specific - USE TagMetadata!!!
        //TODO this is a funny way how to figure out if the attribute contains
        //a file reference or not. The code needs to be generified later.
        Map<String, ValueCompletion<HtmlCompletionItem>> completions = AttrValuesCompletion.getSupportsForTag(tnode.name().toString());
        if (completions != null) {
            for (Attribute attr : tnode.attributes()) {
                ValueCompletion<HtmlCompletionItem> avc = completions.get(attr.name().toString());
                if (AttrValuesCompletion.FILE_NAME_SUPPORT == avc) {
                    //found file reference
                    CharSequence unquotedValue = attr.unquotedValue();
                    //html5 parser retunrs empty strings for non existing value,
                    //html4 parse returns null in the same situation
                    if (unquotedValue != null && unquotedValue.length() > 0) {
                        boolean isQuoted = attr.isValueQuoted();
                        int offset = attr.valueOffset() + (isQuoted ? 1 : 0);

                        getReferencesCollectionInstance().add(
                                createFileReferenceEntry(unquotedValue.toString(),
                                new OffsetRange(offset,
                                offset + unquotedValue.length()),
                                tnode.name().toString(),
                                attr.name().toString()));
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(super.toString());
        buf.append(":"); //NOI18N
        for (HtmlLinkEntry c : getReferences()) {
            buf.append(" references="); //NOI18N
            buf.append(c);
            buf.append(','); //NOI18N
        }
        return buf.toString();
    }

    private HtmlLinkEntry createFileReferenceEntry(String name, OffsetRange range, String tagName, String attributeName) {
        //normalize the link so it contains just the file reference, not the possible query part
        //TODO query part handling should be moved to the HtmlLinkEntry possibly
        int qmIndex = name.indexOf("?");//NOI18N
        if (qmIndex >= 0) {
            //modify the range
            range = new OffsetRange(range.getStart(), range.getEnd() - (name.length() - qmIndex));
            //strip the name
            name = name.substring(0, qmIndex);
        }

        int documentFrom = getSnapshot().getOriginalOffset(range.getStart());
        int documentTo = getSnapshot().getOriginalOffset(range.getEnd());

        OffsetRange documentRange = null;
        if (documentFrom == -1 || documentTo == -1) {
            if (LOG) {
                LOGGER.log(Level.FINER, "Ast offset range {0}, text=''{1}" + "'', "
                        + " cannot be properly mapped to source offset range: [{2},{3}] in file {4}", new Object[]{range.toString(), getSnapshot().getText().subSequence(range.getStart(), range.getEnd()), documentFrom, documentTo, getFileObject().getPath()}); //NOI18N
            }
        } else {
            documentRange = new OffsetRange(documentFrom, documentTo);
        }
        return new HtmlLinkEntry(getFileObject(), name, range, documentRange, tagName, attributeName);
    }
}