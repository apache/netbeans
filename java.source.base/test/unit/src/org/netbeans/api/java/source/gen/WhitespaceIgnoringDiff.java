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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.api.java.source.gen;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.EnumSet;
import java.util.Set;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.junit.diff.Diff;
import org.netbeans.junit.diff.LineDiff;

/**
 *
 * @author Jan Lahoda
 */
public class WhitespaceIgnoringDiff implements Diff {
    
    /** Creates a new instance of WhitespaceIgnoringDiff */
    public WhitespaceIgnoringDiff() {
    }

    private Set<JavaTokenId> IGNORED_TOKENS = EnumSet.of(JavaTokenId.WHITESPACE, JavaTokenId.LINE_COMMENT, JavaTokenId.BLOCK_COMMENT, JavaTokenId.JAVADOC_COMMENT);
    
    public boolean diff(File first, File second, File diff) throws IOException {
        boolean result = diffImpl(first, second);
        
        if (result) {
            new LineDiff().diff(first, second, diff);
        }
        
        return result;
    }
    
    private boolean diffImpl(File first, File second) throws IOException {
        Reader firstReader = new FileReader(first);
        Reader secondReader = new FileReader(second);
        try {
            TokenHierarchy firstH = TokenHierarchy.create(firstReader, JavaTokenId.language(), /*IGNORED_TOKENS*/null, null);
            TokenHierarchy secondH = TokenHierarchy.create(secondReader, JavaTokenId.language(), /*IGNORED_TOKENS*/null, null);
            TokenSequence<JavaTokenId> firstTS = firstH.tokenSequence(JavaTokenId.language());
            TokenSequence<JavaTokenId> secondTS = secondH.tokenSequence(JavaTokenId.language());
            
//            if (firstTS.tokenCount() != secondTS.tokenCount()) {
//                return true;
//            }
            
            firstTS.moveNext();
            secondTS.moveNext();
            
            boolean firstHasNext = true;
            boolean secondHasNext = true;
            
            do {
                Token<JavaTokenId> firstToken = firstTS.token();
                Token<JavaTokenId> secondToken = secondTS.token();
                
                while (IGNORED_TOKENS.contains(firstToken.id()) && firstHasNext) {
                    firstHasNext = firstTS.moveNext();
                    firstToken = firstTS.token();
                }
                
                while (IGNORED_TOKENS.contains(secondToken.id()) && secondHasNext) {
                    secondHasNext = secondTS.moveNext();
                    secondToken = secondTS.token();
                }
                
                if (!firstHasNext || !secondHasNext)
                    break;
                
                if (firstToken.id() != secondToken.id() || !TokenUtilities.equals(firstToken.text(), secondToken.text()))
                    return true;
                
                firstHasNext = firstTS.moveNext();
                secondHasNext = secondTS.moveNext();
            } while (firstHasNext && secondHasNext);
            
            if (firstHasNext || secondHasNext)
                return true;
        } finally {
            firstReader.close();
            secondReader.close();
        }
        
        return false;
    }

    public boolean diff(String first, String second, String diff) throws IOException {
        File fFirst = new File(first);
        File fSecond = new File(second);
        File fDiff = null != diff ? new File(diff) : null;
        return diff(fFirst, fSecond, fDiff);
    }
    
}
