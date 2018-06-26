/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.groovy.editor.api.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.codehaus.groovy.ast.ModuleNode;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.groovy.editor.api.StructureAnalyzer;
import org.netbeans.modules.groovy.editor.api.elements.ast.ASTRoot;
import org.codehaus.groovy.control.ErrorCollector;
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

    GroovyParserResult(GroovyParser parser, Snapshot snapshot, ModuleNode rootNode,
            ErrorCollector errorCollector) {
        super(snapshot);
        this.parser = parser;
        this.rootElement = new ASTRoot(snapshot.getSource().getFileObject(), rootNode);
        this.errorCollector = errorCollector;
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
