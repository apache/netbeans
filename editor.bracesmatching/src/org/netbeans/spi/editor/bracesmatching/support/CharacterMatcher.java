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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.spi.editor.bracesmatching.support;

import java.util.logging.Logger;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 *
 * @author Vita Stejskal
 */
/* package */ final class CharacterMatcher implements BracesMatcher {

    private static final Logger LOG = Logger.getLogger(CharacterMatcher.class.getName());
    
    private final MatcherContext context;
    private final char [] matchingPairs;
    private final int lowerBound;
    private final int upperBound;
    
    private int originOffset;
    private char originalChar;
    private char matchingChar;
    private boolean backward;
    
    public CharacterMatcher(MatcherContext context, int lowerBound, int upperBound, char... matchingPairs) {
        this.context = context;
        this.lowerBound = lowerBound == -1 ? Integer.MIN_VALUE : lowerBound;
        this.upperBound = upperBound == -1 ? Integer.MAX_VALUE : upperBound;
        
        assert matchingPairs.length % 2 == 0 : "The matchingPairs parameter must contain even number of characters."; //NOI18N
        this.matchingPairs = matchingPairs;
    }
    
    // -----------------------------------------------------
    // BracesMatcher implementation
    // -----------------------------------------------------
    
    public int [] findOrigin() throws BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            int result [] = BracesMatcherSupport.findChar(
                context.getDocument(),
                context.getSearchOffset(),
                context.isSearchingBackward() ?
                    Math.max(context.getLimitOffset(), lowerBound) :
                    Math.min(context.getLimitOffset(), upperBound),
                matchingPairs
            );

            if (result != null) {
                originOffset = result[0];
                originalChar = matchingPairs[result[1]];
                matchingChar = matchingPairs[result[1] + result[2]];
                backward = result[2] < 0;
                return new int [] { originOffset, originOffset + 1 };
            } else {
                return null;
            }
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    public int [] findMatches() throws BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            int offset = BracesMatcherSupport.matchChar(
                context.getDocument(),
                backward ? originOffset : originOffset + 1,
                backward ?
                    Math.max(0, lowerBound) :
                    Math.min(context.getDocument().getLength(), upperBound),
                originalChar,
                matchingChar
            );

            return offset != -1 ? new int [] { offset, offset + 1 } : null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }
}
