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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.openide.filesystems.FileObject;
import static org.junit.Assert.*;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.openide.util.RequestProcessor;
import org.openide.util.UserQuestionException;

/**
 *
 * @author marekfukala
 */
public class ElementsParserCacheTest extends CslTestBase {

    public ElementsParserCacheTest(String name) {
        super(name);
    }

    public void testIterator() {
        String code = "<div></div>";
        //             012345678901
        TokenHierarchy th = TokenHierarchy.create(code, HTMLTokenId.language());
        TokenSequence ts = th.tokenSequence(HTMLTokenId.language());

        ElementsParserCache parserCache = new ElementsParserCache(code, ts);

        Iterator<Element> itr = parserCache.createElementsIterator();
        assertNotNull(itr);

        assertTrue(itr.hasNext());

        Element e = itr.next();
        assertNotNull(e);

        assertEquals(ElementType.OPEN_TAG, e.type());
        assertEquals(0, e.from());
        assertEquals(5, e.to());

        assertTrue(itr.hasNext());

        e = itr.next();
        assertNotNull(e);

        assertEquals(ElementType.CLOSE_TAG, e.type());
        assertEquals(5, e.from());
        assertEquals(11, e.to());
        
        assertFalse(itr.hasNext());

    }

    public void testCacheBlock() {
        CharSequence code = "<div></div>";
        //             012345678901
        TokenHierarchy th = TokenHierarchy.create(code, HTMLTokenId.language());
        TokenSequence ts = th.tokenSequence(HTMLTokenId.language());

        ElementsParserCache parserCache = new ElementsParserCache(code, ts);

        Iterator<Element> itr = parserCache.createElementsIterator();
        assertNotNull(itr);

        assertTrue(itr.hasNext());

        //now the first block should be loaded
        List<ElementsParserCache.CacheBlock> blocks = parserCache.cacheBlocks;
        assert (blocks.size() == 1);

        ElementsParserCache.CacheBlock block = blocks.get(0);

        assertEquals(0, block.getStartOffset());
        assertEquals(11, block.getEndOffset());

        assertEquals(0, block.getStartIndex());
        assertEquals(2, block.getEndIndex());

    }
    
    public void testCaching() {
        CharSequence code = "<div></div>";
        //             012345678901
        TokenHierarchy th = TokenHierarchy.create(code, HTMLTokenId.language());
        TokenSequence ts = th.tokenSequence(HTMLTokenId.language());

        ElementsParserCache parserCache = new ElementsParserCache(code, ts);

        Iterator<Element> itr = parserCache.createElementsIterator();
        assertNotNull(itr);
        assertTrue(itr.hasNext());

        //now the first block should be loaded
        List<ElementsParserCache.CacheBlock> blocks = parserCache.cacheBlocks;
        assertEquals(1, blocks.size());
        ElementsParserCache.CacheBlock block = blocks.get(0);
        
        //hold the internal CacheBlockContent so it cannot be GCed in the test
        Object contentReference = block.blockReference.get();

        assertEquals(1, block.blockReads);
        
        itr = parserCache.createElementsIterator();
        assertNotNull(itr);
        assertTrue(itr.hasNext());
        
        //the cache elements should be used, cache never GCed, held by the 'content' field
        assertEquals(1, block.blockReads);

        //now remove the reference and try to GC
        contentReference = null;
        
        assertGC("CacheBlockContent not GCed", block.blockReference);
        
        //now an attempt to iterate more should reread the CacheBlockContent
        assertTrue(itr.hasNext());
        assertNotNull(itr.next());
        
        assertEquals(2, block.blockReads);
        
    }
    
    public void testIteratorAtBlockBoundary() {
        String code = "<div></div><a></a>";
        //             0123456789012345678
        TokenHierarchy th = TokenHierarchy.create(code, HTMLTokenId.language());
        TokenSequence ts = th.tokenSequence(HTMLTokenId.language());

        ElementsParserCache.CACHE_BLOCK_SIZE = 2;
        
        ElementsParserCache parserCache = new ElementsParserCache(code, ts);

        Iterator<Element> itr = parserCache.createElementsIterator();
        assertNotNull(itr);

        assertTrue(itr.hasNext());
        
        //now the first block should be loaded
        List<ElementsParserCache.CacheBlock> blocks = parserCache.cacheBlocks;
        assertEquals(1, blocks.size());
        ElementsParserCache.CacheBlock block1 = blocks.get(0);
        
        //first cache block is used
        
        assertEquals(2, block1.getElements().size());
        
        assertEquals(0, block1.getStartIndex());
        assertEquals(2, block1.getEndIndex());
        
        assertEquals(0, block1.getStartOffset());
        assertEquals(11, block1.getEndOffset());
        
        assertEquals(0, block1.getFirstTokenIndex());
        assertEquals(5, block1.getLastTokenIndex());
        
        //block read
        assertEquals(1, block1.blockReads); 

        //try the elements
        Element e = itr.next();
        assertNotNull(e);

        assertEquals(ElementType.OPEN_TAG, e.type());
        assertEquals(0, e.from());
        assertEquals(5, e.to());

        assertTrue(itr.hasNext());

        e = itr.next();
        assertNotNull(e);

        assertEquals(ElementType.CLOSE_TAG, e.type());
        assertEquals(5, e.from());
        assertEquals(11, e.to());

        //now second block is used
        
        assertTrue(itr.hasNext());

        assertEquals(2, blocks.size());
        ElementsParserCache.CacheBlock block2 = blocks.get(1);
        
        assertEquals(2, block2.getElements().size());
        
        assertEquals(2, block2.getStartIndex());
        assertEquals(4, block2.getEndIndex());
        
        assertEquals(11, block2.getStartOffset());
        assertEquals(18, block2.getEndOffset());
        
        assertEquals(6, block2.getFirstTokenIndex());
        assertEquals(11, block2.getLastTokenIndex());
        
        //read already
        assertEquals(1, block2.blockReads); 
        
        e = itr.next();
        assertNotNull(e);

        assertEquals(ElementType.OPEN_TAG, e.type());
        assertEquals(11, e.from());
        assertEquals(14, e.to());

        assertTrue(itr.hasNext());

        e = itr.next();
        assertNotNull(e);

        assertEquals(ElementType.CLOSE_TAG, e.type());
        assertEquals(14, e.from());
        assertEquals(18, e.to());
        
        //read beyond the second block -> new empty third block will be created
        assertFalse(itr.hasNext());
        
        assertEquals(3, blocks.size());
        ElementsParserCache.CacheBlock block3 = blocks.get(2);
        
        assertEquals(0, block3.getElements().size());
        
        assertEquals(4, block3.getStartIndex());
        assertEquals(4, block3.getEndIndex());
        
        assertEquals(-1, block3.getStartOffset());
        assertEquals(-1, block3.getEndOffset());
    }
    
    //Bug 230170 - AssertionError at org.netbeans.modules.html.editor.lib.ElementsParserCache$1.getCacheBlock 
    public void testIssue230170() {
        StringBuilder code = new StringBuilder();
        ElementsParserCache.CACHE_BLOCK_SIZE = 10;
        for(int i = 0; i <= 200; i++) {
            code.append("<div id='");
            code.append(i);
            code.append("'/>");
        }
        TokenHierarchy th = TokenHierarchy.create(code, HTMLTokenId.language());
        TokenSequence ts = th.tokenSequence(HTMLTokenId.language());
        final ElementsParserCache parserCache = new ElementsParserCache(code, ts);
        
        //test concurrent access to the element iterators obtained from one shared
        //instance of ElementsParserCache
        RequestProcessor myrp = new RequestProcessor("test", 3);
        myrp.post( new Runnable() {
            @Override
            public void run() {
                Iterator<Element> ei = parserCache.createElementsIterator();
                while(ei.hasNext()) {
                    System.out.println("T1"+ei.next());
                }
            }
        });
        myrp.post( new Runnable() {
            @Override
            public void run() {
                Iterator<Element> ei = parserCache.createElementsIterator();
                while(ei.hasNext()) {
                    System.out.println("T2"+ei.next());
                }
            }
        });
        myrp.post( new Runnable() {
            @Override
            public void run() {
                Iterator<Element> ei = parserCache.createElementsIterator();
                while(ei.hasNext()) {
                    System.out.println("T3"+ei.next());
                }
            }
        });
        
    }

    public void testPerformance() throws IOException {
        
        FileObject file = getTestFile("testfiles/huge.html");
        String content = file.asText();
        
        TokenHierarchy hi = TokenHierarchy.create(content, HTMLTokenId.language());
        ElementsParserCache cache = new ElementsParserCache(content, hi.tokenSequence(HTMLTokenId.language()));
        
        Iterator<Element> itr = cache.createElementsIterator();
        
        long start = System.currentTimeMillis();
        while(itr.hasNext()) {
            itr.next();
        }
        long end = System.currentTimeMillis();
        
        System.out.println("ElementsParserCache");
        System.out.println("========================");
        
        float diff1 = end - start;
        System.out.println("first iteration took " + diff1 + "ms.");
        
        //~2700ms versus the ~2500ms of the plain ElementsParser
        
        //second iteration - from cache
        
        itr = cache.createElementsIterator();
        
        start = System.currentTimeMillis();
        while(itr.hasNext()) {
            itr.next();
        }
        end = System.currentTimeMillis();
        
        float diff2 = end - start;
        System.out.println("second iteration took " + diff2 + "ms.");
        
        //~70ms versus 600ms of the second iteration of the plain parser
        
        float ratio = diff1 / diff2;
        System.out.println("first / second ratio = " + ratio);
        
        
    }
    
}
