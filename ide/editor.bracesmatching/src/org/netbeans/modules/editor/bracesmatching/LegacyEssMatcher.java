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
package org.netbeans.modules.editor.bracesmatching;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 *
 * @author Vita Stejskal
 */
public final class LegacyEssMatcher implements BracesMatcher, BracesMatcherFactory {

    private final MatcherContext context;
    private final ExtSyntaxSupport ess;
    
    private int [] block;
    
    public LegacyEssMatcher() {
        this(null, null);
    }

    private LegacyEssMatcher(MatcherContext context, ExtSyntaxSupport ess) {
        this.context = context;
        this.ess = ess;
    }
    
    // -----------------------------------------------------
    // BracesMatcher implementation
    // -----------------------------------------------------
    
    public int[] findOrigin() throws BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            int offset;

            if (context.isSearchingBackward()) {
                offset = context.getSearchOffset() - 1;
            } else {
                offset = context.getSearchOffset();
            }

            block = ess.findMatchingBlock(offset, false);
            if (block == null) {
                return null;
            } else if (block.length == 0) {
                return block;
            } else {
                return new int [] { offset, offset };
            }
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    public int[] findMatches() throws InterruptedException {
        return block;
    }

    // -----------------------------------------------------
    // BracesMatcherFactory implementation
    // -----------------------------------------------------
    
    public BracesMatcher createMatcher(MatcherContext context) {
        Document d = context.getDocument();
        
        if (d instanceof BaseDocument) {
            SyntaxSupport ss = ((BaseDocument) d).getSyntaxSupport();
            if (ss instanceof ExtSyntaxSupport && ss.getClass() != ExtSyntaxSupport.class) {
                return new LegacyEssMatcher(context, (ExtSyntaxSupport) ss);
            }
        }
        
        return null;
    }

}
