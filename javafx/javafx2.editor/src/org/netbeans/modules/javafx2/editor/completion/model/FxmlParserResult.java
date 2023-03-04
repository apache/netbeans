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
package org.netbeans.modules.javafx2.editor.completion.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.javafx2.editor.ErrorMark;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;

/**
 * Result of parsing the .fxml file. The result contains the model of the FXML
 * source, and a log of errors encountered during parsing and attributing the file.
 * <p/>
 * A client may need to access {@link FxTreeUtilities} instances
 * to work with positions and navigate through xml structure.
 * 
 * 
 * @author sdedic
 */
public abstract class FxmlParserResult extends Parser.Result {
    /**
     * The source model
     */
    private final FxModel     sourceModel;
    
    /**
     * Problems found during parsing
     */
    private final Collection<ErrorMark>   problems;
    
    private FxTreeUtilities treeUtils;
    
    private final TokenHierarchy<?> tokenHierarchy;

    protected FxmlParserResult(Snapshot _snapshot, FxModel sourceModel, Collection<ErrorMark> problems, 
            TokenHierarchy<?> h) {
        super(_snapshot);
        this.sourceModel = sourceModel;
        this.problems = Collections.unmodifiableCollection(problems);
        this.tokenHierarchy = h;
    }
    
    protected abstract FxTreeUtilities createTreeUtilities();
    
    @Override
    protected void invalidate() {
    }
    
    public FxModel  getSourceModel() {
        return sourceModel;
    }
    
    public Collection<ErrorMark>    getProblems() {
        return problems;
    }
    
    public FxTreeUtilities getTreeUtilities() {
        if (treeUtils == null) {
            treeUtils = createTreeUtilities();
        }
        return treeUtils;
    }

    public TokenHierarchy<?> getTokenHierarchy() {
        return tokenHierarchy;
    }
    
    public abstract FxNewInstance resolveInstance(FxInclude include);
    
    public static FxmlParserResult get(Parser.Result p) {
        if (p instanceof FxmlParserResult) {
            return (FxmlParserResult)p;
        } else {
            return null;
        }
    }
    
    public abstract Set<String> resolveClassName(CompilationInfo info, String className);
}
