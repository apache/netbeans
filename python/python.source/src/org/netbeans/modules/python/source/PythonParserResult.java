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
package org.netbeans.modules.python.source;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.python.source.PythonParser.Sanitize;
import org.netbeans.modules.python.source.scopes.SymbolTable;
import org.python.antlr.PythonTree;

/**
 * A ParserResult for Python. The AST Jython's AST.
 *
 * @todo Cache AstPath for caret position here!
 */
public class PythonParserResult extends ParserResult {
    private PythonTree root;
    private List<Error> errors;
    private OffsetRange sanitizedRange = OffsetRange.NONE;
    private String source;
    private String sanitizedContents;
    private PythonParser.Sanitize sanitized;
    private PythonStructureScanner.AnalysisResult analysisResult;
    private SymbolTable symbolTable;
    private int codeTemplateOffset = -1;

    public PythonParserResult(PythonTree tree, @NonNull Snapshot snapshot) {
        super(snapshot);
        this.root = tree;
        this.errors = new LinkedList<>();
    }

    public PythonTree getRoot() {
        return root;
    }

    @Override
    public List<? extends Error> getDiagnostics() {
        return errors;
    }

    @Override
    protected void invalidate() {
    }

    public void setErrors(List<? extends Error> errors) {
        this.errors.clear();
        this.errors.addAll(errors);
    }
    
    /**
     * Set the range of source that was sanitized, if any.
     */
    void setSanitized(PythonParser.Sanitize sanitized, OffsetRange sanitizedRange, String sanitizedContents) {
        this.sanitized = sanitized;
        this.sanitizedRange = sanitizedRange;
        this.sanitizedContents = sanitizedContents;
        if (sanitizedContents == null || sanitizedRange == OffsetRange.NONE) {
            this.sanitized = Sanitize.NONE;
        }
    }

    public PythonParser.Sanitize getSanitized() {
        return sanitized;
    }

    /**
     * Return whether the source code for the parse result was "cleaned"
     * or "sanitized" (modified to reduce chance of parser errors) or not.
     * This method returns OffsetRange.NONE if the source was not sanitized,
     * otherwise returns the actual sanitized range.
     */
    public OffsetRange getSanitizedRange() {
        return sanitizedRange;
    }

    public SymbolTable getSymbolTable() {
        if (symbolTable == null) {
            symbolTable = new SymbolTable(root, getSnapshot().getSource().getFileObject());
        }

        return symbolTable;
    }

    public String getSanitizedContents() {
        return sanitizedContents;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return the codeTemplateOffset
     */
    public int getCodeTemplateOffset() {
        return codeTemplateOffset;
    }

    /**
     * @param codeTemplateOffset the codeTemplateOffset to set
     */
    public void setCodeTemplateOffset(int codeTemplateOffset) {
        this.codeTemplateOffset = codeTemplateOffset;
    }

    public void addError(Error e) {
        errors.add(e);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }
}
