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
