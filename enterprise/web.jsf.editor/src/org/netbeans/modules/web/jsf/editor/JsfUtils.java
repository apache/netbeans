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
package org.netbeans.modules.web.jsf.editor;

import javax.swing.text.BadLocationException;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.web.jsfapi.api.LibraryInfo;

/**
 *
 * @author mfukala@netbeans.org
 */
public class JsfUtils {

    private JsfUtils() {
    }

    /**
     * Mimetype of XHTML files - text/xhtml
     */
    public static final String XHTML_MIMETYPE = "text/xhtml"; //NOI18N
    /**
     * Mimetype of plain XML files - text/xml
     */
    public static final String XML_MIMETYPE = "text/xml"; //NOI18N
    /**
     * Mimetype of Tag Library Descriptor files - text/x-tld
     */
    public static final String TLD_MIMETYPE = "text/x-tld"; //NOI18N
    
    /**
     * Creates an OffsetRange of source document offsets for given embedded offsets.
     */
    public static OffsetRange createOffsetRange(Snapshot snapshot, CharSequence documentText, int embeddedOffsetFrom, int embeddedOffsetTo) {

        int originalFrom = 0;
        int originalTo = documentText.length();

        //try to find nearest original offset if the embedded offsets cannot be directly recomputed
        //from - try backward
        for (int i = embeddedOffsetFrom; i >= 0; i--) {
            int originalOffset = snapshot.getOriginalOffset(i);
            if (originalOffset != -1) {
                originalFrom = originalOffset;
                break;
            }
        }

        try {
            //some heuristic - use end of line where the originalFrom lies
            //in case if we cannot match the end offset at all
            originalTo = GsfUtilities.getRowEnd(documentText, originalFrom);
        } catch (BadLocationException ex) {
            //ignore, end of the document will be used as end offset
        }

        //to - try forward
        for (int i = embeddedOffsetTo; i <= snapshot.getText().length(); i++) {
            int originalOffset = snapshot.getOriginalOffset(i);
            if (originalOffset != -1) {
                originalTo = originalOffset;
                break;
            }
        }

        return new OffsetRange(originalFrom, originalTo);
    }

    public static Result getEmbeddedParserResult(ResultIterator resultIterator, String mimeType) throws ParseException {
        for (Embedding e : resultIterator.getEmbeddings()) {
            if (e.getMimeType().equals(mimeType)) {
                return resultIterator.getResultIterator(e).getParserResult();
            }
        }
        return null;
    }

    public static Node getRoot(HtmlParserResult parserResult, LibraryInfo library) {
        for (String namespace : library.getValidNamespaces()) {
            Node rootNode = parserResult.root(namespace);
            if (rootNode != null && !rootNode.children().isEmpty()) {
                return rootNode;
            }
        }

        return null;
    }
}
