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
package org.netbeans.modules.html.editor.lib.api.elements;

import java.util.Iterator;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.editor.lib.ElementsParser;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * Plain HTML syntax analyzer
 *
 * @author mfukala@netbeans.org
 */
public class ElementsIterator implements Iterator<Element> {


    private Iterator<Element> wrapped;
    
    public ElementsIterator(Snapshot snapshot) {
        if(!"text/html".equals(snapshot.getMimeType())) {
            throw new IllegalStateException();
        }
        
        CharSequence source = snapshot.getText();
        TokenSequence<HTMLTokenId> tokenSequence = snapshot.getTokenHierarchy().tokenSequence(HTMLTokenId.language());
        
        this.wrapped = ElementsParser.forTokenIndex(source, tokenSequence, 0);
        
    }

    public ElementsIterator(HtmlSource source) {
        CharSequence sourceCode = source.getSourceCode();
        Snapshot snapshot = source.getSnapshot();
        TokenHierarchy hi;
        if (snapshot != null) {
            //use the snapshot's token hierarchy (cached) if possible
            hi = snapshot.getTokenHierarchy();
        } else {
            hi = TokenHierarchy.create(sourceCode, HTMLTokenId.language());
        }
        TokenSequence<HTMLTokenId> tokenSequence = hi.tokenSequence(HTMLTokenId.language());
        
        this.wrapped = ElementsParser.forTokenIndex(sourceCode, tokenSequence, 0);
        
    }
    
    @Override
    public boolean hasNext() {
        return wrapped.hasNext();
    }

    @Override
    public Element next() {
        return wrapped.next();
    }

    @Override
    public void remove() {
        wrapped.remove();
    }
   
}
