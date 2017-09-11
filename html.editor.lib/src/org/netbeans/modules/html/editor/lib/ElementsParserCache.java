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
package org.netbeans.modules.html.editor.lib;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.editor.lib.api.elements.Element;

/**
 * Creates instances of Iterator<Element> which used weakly cached elements.
 * 
 * The parsed elements are cached in blocks of 100 elements, each block is weakly referenced.
 *
 * @author mfukala@netbeans.org
 */
public class ElementsParserCache {

    /* not private final for unit testing */ static int CACHE_BLOCK_SIZE = 1000; //number of Element-s in one cache block
    /* test */ final List<CacheBlock> cacheBlocks = new ArrayList<>();
    
    private final CharSequence sourceCode;
    private final TokenSequence<HTMLTokenId> tokenSequence;

    public ElementsParserCache(CharSequence sourceCode, TokenSequence<HTMLTokenId> tokenSequence) {
        this.sourceCode = sourceCode;
        this.tokenSequence = tokenSequence;
    }

    public Iterator<Element> createElementsIterator() {
        return new Iterator<Element>() {
            
            private int index = 0;
            
            @Override
            public boolean hasNext() {
                return getCacheBlock().getEndIndex() > index;
            }

            @Override
            public Element next() {
                return getCacheBlock().getElementAtIndex(index++);                
            }

            //notice: new empty block will be created at the end of the source
            //        if the number of elements modulo CACHE_BLOCK_SIZE == 0;
            private CacheBlock getCacheBlock() {

                synchronized(cacheBlocks) {
                    int blockIndex = index / CACHE_BLOCK_SIZE;

                    CacheBlock item = cacheBlocks.size() > blockIndex ? cacheBlocks.get(blockIndex) : null;
                    if (item == null) {
                        //no data, load

                        //first token of the new block - either 0 or last block end token index + 1
                        int firstTokenIndex = blockIndex == 0 ? 0 : cacheBlocks.get(blockIndex - 1).getLastTokenIndex() + 1;

                        item = new CacheBlock(sourceCode, tokenSequence, index, firstTokenIndex);

                        assert blockIndex == cacheBlocks.size(); //always last

                        cacheBlocks.add(blockIndex, item);
                    }
                    return item;
                }
            }

            @Override
            public void remove() {
                //no-op
            }
        };

    }

    static class CacheBlock {

        /* test */ int blockReads = 0;
        
        Reference<CacheBlockContent> blockReference;
        private final int startIndex;
        private final int endIndex;
        private final int startOffset;
        private final int endOffset;
        private final int firstTokenIndex;
        private final int lastTokenIndex;
        private final CharSequence code;
        private final TokenSequence<HTMLTokenId> tokenSequence;

        private CacheBlock(CharSequence code, TokenSequence<HTMLTokenId> tokenSequence, int firstElementIndex, int firstTokenIndex) {
            this.code = code;
            this.tokenSequence = tokenSequence;

            this.startIndex = firstElementIndex;
            this.firstTokenIndex = firstTokenIndex;

            CacheBlockContent block = new CacheBlockContent(code, tokenSequence, firstTokenIndex);
            int blockSize = block.getElements().size();
            this.endIndex = firstElementIndex + blockSize;
            
            this.startOffset = blockSize == 0 ? -1 : block.getFirstElement().from();
            
            this.endOffset = blockSize == 0 ? -1 : block.getLastElement().to();
            this.lastTokenIndex = block.getLastTokenIndex();
            
            blockReads++;

            blockReference = new SoftReference<>(block);
        }

        public int getStartIndex() {
            return startIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }
        
        public int getFirstTokenIndex() {
            return firstTokenIndex;
        }
        
        public  int getLastTokenIndex() {
            return lastTokenIndex;
        }
        
        public Element getElementAtIndex(int index) {
            return getElements().get(index - getStartIndex());
        }

        //synchronized as the new CacheBlockContent(...) creates a new instance
        //of ElementsParser which however operates on shared TokenSequence and
        //repositions it.
        public synchronized List<Element> getElements() {
            CacheBlockContent block = blockReference.get();
            if (block == null) {
                //reload the content
                block = new CacheBlockContent(code, tokenSequence, startIndex);
                
                blockReads++;
                
                blockReference = new SoftReference<>(block);
                
//                System.out.println("block at " + getStartIndex() + " - cache reloaded " + blockReads + " times");
            }
            return block.getElements();
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append("CacheBlock(hash=")
                    .append(hashCode())
                    .append(",items=")
                    .append(getElements().size())
                    .toString(); //NOI18N
        }
        
    }
    

    private static class CacheBlockContent {

        private final List<Element> elements;
        private final int firstTokenIndex;
        private final int lastTokenIndex;

        private CacheBlockContent(CharSequence code, TokenSequence<HTMLTokenId> tokenSequence, int firstTokenIndex) {
            this.firstTokenIndex = firstTokenIndex;
            //load the elements
            ElementsParser parser = ElementsParser.forTokenIndex(code, tokenSequence, firstTokenIndex);
            elements = new ArrayList<>(CACHE_BLOCK_SIZE);
            int limit = CACHE_BLOCK_SIZE;
            while (limit-- > 0 && parser.hasNext()) {
                elements.add(parser.next());
            }
            lastTokenIndex = tokenSequence.index();

        }

        List<Element> getElements() {
            return elements;
        }

        Element getFirstElement() {
            return elements.get(0);
        }
        
        Element getLastElement() {
            return elements.get(elements.size() - 1);
        }
        
        int getFirstTokenIndex() {
            return firstTokenIndex;
        }
        
        int getLastTokenIndex() {
            return lastTokenIndex;
        }
        
    }
}
