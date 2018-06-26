/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
        Node rootNode = parserResult.root(library.getNamespace());
        if ((rootNode == null || rootNode.children().isEmpty()) && library.getLegacyNamespace() != null) {
            rootNode = parserResult.root(library.getLegacyNamespace());
        }
        return rootNode;
    }
    
}
