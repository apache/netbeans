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
package org.netbeans.modules.css.editor.module.spi;

import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;

/**
 * An instance of this class or its subclass is typically passed to the
 * CssModule.getXXX() methods.
 * 
 * @author mfukala@netbeans.org
 */
public class FeatureContext {

    private CssParserResult result;

    /**
     * @todo do not allow to instantiate
     */
    public FeatureContext(CssParserResult result) {
        this.result = result;
    }
    
    /**
     * @return a parsing.api result
     */
    public CssParserResult getParserResult() {
        return result;
    }
    
    /**
     * @return the root node of the css parse tree
     */
    public Node getParseTreeRoot() {
        return getParserResult().getParseTree();
    }
    
    /**
     * @return snapshot of the source
     */
    public Snapshot getSnapshot() {
        return result.getSnapshot();
    }
    
    /**
     * @return the parsing.api's source
     */
    public Source getSource() {
        return getSnapshot().getSource();
    }
        
    /**
     * @return the associated file object, it there's any
     */
    public FileObject getFileObject() {
        return getSource().getFileObject();
    }
    
    /**
     * @return token sequence created from the snapshot.
     */
    public TokenSequence<CssTokenId> getTokenSequence() {
        return getSnapshot().getTokenHierarchy().tokenSequence(CssTokenId.language());
    }
    
    /**
     * @return an instance of {@link Model}.
     */
    public Model getSourceModel() {
        return Model.getModel(result);
    }
    
}
