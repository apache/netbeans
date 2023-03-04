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
