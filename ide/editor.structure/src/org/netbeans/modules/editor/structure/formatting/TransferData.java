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
