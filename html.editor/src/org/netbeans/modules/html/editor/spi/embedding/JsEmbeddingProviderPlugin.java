/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.spi.embedding;

import java.util.List;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.embedding.JsEmbeddingProvider;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * PROTOTYPE of an extension of the {@link JsEmbeddingProvider}.
 * 
 * The {@link JsEmbeddingProvider} is lexer based so no parser result can be used here.
 * 
 * Register the plugin into mime lookup.
 * 
 * TODO: Possibly make the processing parser based.
 *
 * @since 2.21
 * @author marekfukala
 */
public abstract class JsEmbeddingProviderPlugin {
  
    /**
     * Called before the first call to {@link #processToken(org.netbeans.modules.parsing.api.Snapshot, org.netbeans.api.lexer.TokenSequence, java.util.List)}.
     * 
     * Clients may initialize resources here.
     * 
     * @param snapshot
     * @param ts
     * @param embeddings 
     * @since 2.32
     * @return true if this plugin is interested in processing the token sequence, false otherwise.
     */
    public abstract boolean startProcessing(HtmlParserResult parserResult, Snapshot snapshot, TokenSequence<HTMLTokenId> ts, List<Embedding> embeddings);
    
    /**
     * Called after the last call to {@link #processToken(org.netbeans.modules.parsing.api.Snapshot, org.netbeans.api.lexer.TokenSequence, java.util.List)}.
     * 
     * Clients may release used resources here.
     * 
     * @since 2.28
     */
    public void endProcessing() {
    }
    
    /**
     * Adds one or more embeddings for the active token of the given token sequence.
     * 
     * The {@link TokenSequence<HTMLTokenId>} passed to the {@link #startProcessing(org.netbeans.modules.parsing.api.Snapshot, org.netbeans.api.lexer.TokenSequence, java.util.List)} method
     * is properly positioned before calling this method. The client must not call moveNext/Previous() methods on the 
     * token sequence itself. The client may obtain embedded token sequences and reposition these freely.
     * 
     * @return true if it embedding(s) were created or false if not.
     */
    public abstract boolean processToken();
    
}
