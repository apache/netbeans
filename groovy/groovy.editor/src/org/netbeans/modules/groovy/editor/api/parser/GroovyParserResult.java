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

package org.netbeans.modules.groovy.editor.api.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.ClassNodeResolver.LookupResult;
import org.codehaus.groovy.control.CompilationUnit;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.groovy.editor.api.StructureAnalyzer;
import org.netbeans.modules.groovy.editor.api.elements.ast.ASTRoot;
import org.codehaus.groovy.control.ErrorCollector;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author Martin Adamek
 */
public class GroovyParserResult extends ParserResult {

    private final GroovyParser parser;
    
    private List<Error> errors = new ArrayList<Error>();

    private ASTRoot rootElement;
    private OffsetRange sanitizedRange = OffsetRange.NONE;
    private String sanitizedContents;
    private StructureAnalyzer.AnalysisResult analysisResult;
    private GroovyParser.Sanitize sanitized;
    private ErrorCollector errorCollector;  // keep track of pending errors (if any)
    private NbGroovyErrorCollector nbCollector;  // keep track of pending errors (if any)
    private CompilationUnit unit;

    GroovyParserResult(GroovyParser parser, Snapshot snapshot, ModuleNode rootNode,
            ErrorCollector errorCollector) {
        super(snapshot);
        this.parser = parser;
        this.rootElement = new ASTRoot(snapshot.getSource().getFileObject(), rootNode);
        this.errorCollector = errorCollector;
        if (errorCollector instanceof NbGroovyErrorCollector) {
            nbCollector = (NbGroovyErrorCollector)errorCollector;
        }
    }

    void setUnit(CompilationUnit unit) {
        this.unit = unit;
    }

    /**
     * Resolves qualified class name into ClassNode.
     * @param className fully qualified class name.
     * @return ClassNode, or {@code null} if the name is not resolvable
     * @since 1.80
     */
    @CheckForNull
    public ClassNode resolveClassName(@NonNull String className) {
        if (unit == null) {
            return null;
        }
        LookupResult lr = unit.getClassNodeResolver().resolveName(className, unit);
        if (lr != null) {
            return lr.getClassNode();
        }
        return null;
    }

    // FIXME remove this
    public ErrorCollector getErrorCollector() {
        return errorCollector;
    }

    public ASTRoot getRootElement() {
        return rootElement;
    }

    public void setErrors(Collection<? extends Error> errors) {
        this.errors = new ArrayList<Error>(errors);
    }
    
    @Override
    public List<? extends Error> getDiagnostics() {
        return errors;
    }

    @Override
    protected void invalidate() {
        // FIXME parsing API
        // remove from parser cache (?)
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
    
    public String getSanitizedContents() {
        return sanitizedContents;
    }

    /**
     * Set the range of source that was sanitized, if any.
     */
    void setSanitized(GroovyParser.Sanitize sanitized, OffsetRange sanitizedRange, String sanitizedContents) {
        this.sanitized = sanitized;
        this.sanitizedRange = sanitizedRange;
        this.sanitizedContents = sanitizedContents;
    }

    GroovyParser.Sanitize getSanitized() {
        return sanitized;
    }    

    public void setStructure(@NonNull StructureAnalyzer.AnalysisResult result) {
        this.analysisResult = result;
    }

    @NonNull
    public StructureAnalyzer.AnalysisResult getStructure() {
        if (analysisResult == null) {
            analysisResult = new StructureAnalyzer().analyze(this);
        }
        return analysisResult;
    }

}
