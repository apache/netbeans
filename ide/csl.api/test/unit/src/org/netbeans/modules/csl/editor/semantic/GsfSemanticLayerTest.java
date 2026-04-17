/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.csl.editor.semantic;

import java.util.List;
import java.util.TreeSet;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import org.junit.Test;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.core.Language;

import static org.junit.Assert.assertEquals;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;


public class GsfSemanticLayerTest {

    @Test
    public void testFirstSequenceElement() throws Exception {
        Document doc = new DefaultEditorKit().createDefaultDocument();
        doc.insertString(0, "Hello World!/n".repeat(10), SimpleAttributeSet.EMPTY);
        List<SequenceElement> elements = List.of(
                new SequenceElement(new Language("text/x-dummy"), doc.createPosition(10), doc.createPosition(20), ColoringAttributes.empty()),
                new SequenceElement(new Language("text/x-dummy"), doc.createPosition(30), doc.createPosition(40), ColoringAttributes.empty()),
                new SequenceElement(new Language("text/x-dummy"), doc.createPosition(50), doc.createPosition(60), ColoringAttributes.empty())
        );

        assertEquals(0, GsfSemanticLayer.firstSequenceElement(elements, -1));
        assertEquals(0, GsfSemanticLayer.firstSequenceElement(elements, 0));
        assertEquals(0, GsfSemanticLayer.firstSequenceElement(elements, 5));
        assertEquals(0, GsfSemanticLayer.firstSequenceElement(elements, 9));
        assertEquals(0, GsfSemanticLayer.firstSequenceElement(elements, 10));
        assertEquals(0, GsfSemanticLayer.firstSequenceElement(elements, 15));
        assertEquals(0, GsfSemanticLayer.firstSequenceElement(elements, 19));
        assertEquals(0, GsfSemanticLayer.firstSequenceElement(elements, 20));
        assertEquals(0, GsfSemanticLayer.firstSequenceElement(elements, 25));
        assertEquals(0, GsfSemanticLayer.firstSequenceElement(elements, 29));
        assertEquals(1, GsfSemanticLayer.firstSequenceElement(elements, 30));
        assertEquals(1, GsfSemanticLayer.firstSequenceElement(elements, 35));
        assertEquals(1, GsfSemanticLayer.firstSequenceElement(elements, 41));
        assertEquals(1, GsfSemanticLayer.firstSequenceElement(elements, 49));
        assertEquals(2, GsfSemanticLayer.firstSequenceElement(elements, 50));
        assertEquals(2, GsfSemanticLayer.firstSequenceElement(elements, 59));
        assertEquals(2, GsfSemanticLayer.firstSequenceElement(elements, 60));
        assertEquals(2, GsfSemanticLayer.firstSequenceElement(elements, 61));
        assertEquals(2, GsfSemanticLayer.firstSequenceElement(elements, 120));

        assertEquals(0, GsfSemanticLayer.firstSequenceElement(List.of(), -1));
        assertEquals(0, GsfSemanticLayer.firstSequenceElement(List.of(), 120));
    }

    @Test
    public void testHighlightSequence() throws Exception {
        Document doc = new DefaultEditorKit().createDefaultDocument();
        doc.insertString(0, "Hello World!/n".repeat(10), SimpleAttributeSet.EMPTY);

        GsfSemanticLayer layer = GsfSemanticLayer.getLayer(GsfSemanticLayer.class, doc);
        Language lang = new Language("text/x-dummy");

        TreeSet<SequenceElement> highlights = new TreeSet<>(SequenceElement.POSITION_ORDER);

        highlights.add(new SequenceElement(lang, doc.createPosition(10), doc.createPosition(20), ColoringAttributes.empty()));
        highlights.add(new SequenceElement(lang, doc.createPosition(50), doc.createPosition(60), ColoringAttributes.empty()));
        highlights.add(new SequenceElement(lang, doc.createPosition(30), doc.createPosition(40), ColoringAttributes.empty()));
        highlights.add(new SequenceElement(lang, doc.createPosition(70), doc.createPosition(80), ColoringAttributes.empty()));

        layer.setColorings(highlights);

        HighlightsSequence seq = layer.getHighlights(0, doc.getLength());
        assertEquals(4, countSequenceElements(seq));

        doc.remove(30, 40); //remove thw two highlighted area in the middle
        seq = layer.getHighlights(0, doc.getLength());
        assertEquals(2, countSequenceElements(seq));
    }

    private static int countSequenceElements(HighlightsSequence seq) {
        int ret = 0;
        while (seq.moveNext()) ret++;
        return ret;
    }
}
