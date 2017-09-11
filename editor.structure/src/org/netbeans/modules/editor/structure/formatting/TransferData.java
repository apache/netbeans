/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.structure.formatting;

import java.util.Arrays;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;

/**
 * This class is used to pass data to the formatters of embedded languages
 * @author tomslot
 */
public class TransferData {
    public static final String TRANSFER_DATA_DOC_PROPERTY = "TagBasedFormatter.TransferData"; //NOI18N
    
    public static final String ORG_CARET_OFFSET_DOCPROPERTY = "TagBasedFormatter.org_caret_offset";

    /**
     * Lines that must not be touched
     */
    private boolean[] formattableLines;
    /**
     * Indents before any formatter was called
     */
    private int[] originalIndents;
    /**
     * Indents after calling the current formatter.
     * It must be filled with valid data for at least
     * the current formatting range and the previous line
     */
    private int[] transformedOffsets;
    /**
     * Indents after calling the current formatter.
     * It must be filled with valid data for at least
     * the current formatting range and the previous line
     */
    private boolean[] alreadyProcessedByNativeFormatter;
    /**
     * Number of lines in the document
     */
    private int numberOfLines;

    public void init(BaseDocument doc) throws BadLocationException {
        numberOfLines = TagBasedLexerFormatter.getNumberOfLines(doc);
        formattableLines = new boolean[numberOfLines];
        alreadyProcessedByNativeFormatter = new boolean[numberOfLines];
        Arrays.fill(formattableLines, true);
        originalIndents = new int[numberOfLines];
        transformedOffsets = new int[numberOfLines];

        for (int i = 0; i < numberOfLines; i++) {
            originalIndents[i] = TagBasedLexerFormatter.getExistingIndent(doc, i);
        }

        doc.putProperty(TRANSFER_DATA_DOC_PROPERTY, this);
    }

    public static TransferData readFromDocument(BaseDocument doc) {
        return (TransferData) doc.getProperty(TRANSFER_DATA_DOC_PROPERTY);
    }

    public int getNumberOfLines() {
        return numberOfLines;
    }

    public boolean isFormattable(int line) {
        return formattableLines[line];
    }

    public void setNonFormattable(int line) {
        formattableLines[line] = false;
    }

    public int[] getTransformedOffsets() {
        return transformedOffsets;
    }

    public void setTransformedOffsets(int[] transformedOffsets) {
        this.transformedOffsets = transformedOffsets;
    }

    public int getOriginalIndent(int i) {
        return originalIndents[i];
    }

    public boolean wasProcessedByNativeFormatter(int line) {
        return alreadyProcessedByNativeFormatter[line];
    }

    public void setProcessedByNativeFormatter(int line) {
        alreadyProcessedByNativeFormatter[line] = true;
    }
}
