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

package org.netbeans.modules.editor.lib2.highlighting;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.editor.util.random.PropertyProvider;
import org.netbeans.lib.editor.util.random.RandomTestContainer;
import org.netbeans.lib.editor.util.random.RandomTestContainer.Context;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;

public class HighlightsMergeTesting {
    
    static final Color[] colors = {
        Color.BLACK, Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW
    };
    public static final AttributeSet[] attrSets = new AttributeSet[colors.length];
    static {
        for (int i = 0; i < colors.length; i++) {
            attrSets[i] = AttributesUtilities.createImmutable(StyleConstants.Background, colors[i]);
        }
    }

    public static final String ADD_LAYER = "highlight-add-layer";

    public static final String ADD_EMPTY_LAYER = "highlight-add-empty-layer";

    public static final String REMOVE_LAYER = "highlight-remove-layer";

    private static final int DOCUMENT_LENGTH = 1000; // Fixed document length
    private static final int MAX_LAYER_HIGHLIGHT_COUNT = 20;
    
    private static boolean logChecks;
    
    public static void setLogChecks(boolean logChecks) {
        HighlightsMergeTesting.logChecks = logChecks;
    }

    public static RandomTestContainer createContainer() {
        RandomTestContainer container = new RandomTestContainer();
        Document doc = new PlainDocument();
        container.putProperty(Document.class, doc);
        // Fill document
        try {
            doc.insertString(0, "abcdef\n", null);
            int docLen;
            while ((docLen = doc.getLength()) < DOCUMENT_LENGTH) {
                int insertLen = Math.min(docLen, DOCUMENT_LENGTH - docLen);
                doc.insertString(docLen, doc.getText(0, insertLen), null);
            }
        } catch (BadLocationException ex) {
            throw new IllegalStateException(ex);
        }
        container.putProperty(DirectMergeContainer.class, new DirectMergeContainer(new HighlightsContainer[0], false));
        container.putProperty(CompoundHighlightsContainer.class,
                new CompoundHighlightsContainer(doc, new HighlightsContainer[0]));

        container.addOp(new AddLayerOp(ADD_LAYER));
        container.addOp(new AddLayerOp(ADD_EMPTY_LAYER));
        container.addOp(new RemoveLayerOp(REMOVE_LAYER));
        container.addCheck(new MergeCheck());
        return container;
    }
    
    public static RandomTestContainer.Round addRound(RandomTestContainer container) throws Exception {
        RandomTestContainer.Round round = container.addRound();
        round.setOpCount(100);
        round.setRatio(ADD_LAYER, 20);
        round.setRatio(ADD_EMPTY_LAYER, 1);
        round.setRatio(REMOVE_LAYER, 8);
        return round;
    }

    static Document document(PropertyProvider provider) {
        return provider.getInstance(Document.class);
    }

    static DirectMergeContainer directMergeContainer(PropertyProvider provider) {
        return provider.getInstance(DirectMergeContainer.class);
    }
    
    static CompoundHighlightsContainer compoundHighlightsContainer(PropertyProvider provider) {
        return provider.getInstance(CompoundHighlightsContainer.class);
    }
    
    static int layerCount(PropertyProvider provider) {
        return compoundHighlightsContainer(provider).getLayers().length;
    }

    public static void addFixedLayer(Context context, int zIndex, Object... highlights) throws Exception {
        CompoundHighlightsContainer compoundHighlightsContainer = compoundHighlightsContainer(context);
        Document doc = document(context);
        List<Highlight> highlightList = new ArrayList<Highlight>();
        if (highlights.length % 3 != 0) {
            throw new IllegalArgumentException("highlights.length=" + highlights.length + " % 3 != 0");
        }
        for (int i = 0; i < highlights.length; i += 3) {
            int offset0 = (Integer)highlights[i];
            int offset1 = (Integer)highlights[i+1];
            Highlight highlight = new Highlight(offset0, offset1, (AttributeSet)highlights[i+2]);
            highlightList.add(highlight);
        }
        HighlightsContainer[] layers = compoundHighlightsContainer.getLayers();
        HighlightsContainer[] newLayers = new HighlightsContainer[layers.length + 1];
        zIndex = Math.min(zIndex, layers.length);
        System.arraycopy(layers, 0, newLayers, 0, zIndex);
        System.arraycopy(layers, zIndex, newLayers, zIndex + 1, layers.length - zIndex);
        OffsetsBag bag = new OffsetsBag(doc);
        for (Highlight highlight : highlightList) {
            highlight.addTo(bag);
        }
        newLayers[zIndex] = bag;
        
        // Possibly do logging
        if (context.isLogOp()) {
            StringBuilder sb = context.logOpBuilder();
            sb.append(" ADD_LAYER(").append(zIndex).append("): ");
            sb.append('\n');
            int digitCount = ArrayUtilities.digitCount(highlightList.size());
            for (int i = 0; i < highlightList.size(); i++) {
                Highlight hi = highlightList.get(i);
                sb.append("Parameter highlight");
                ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
                sb.append(hi);
                sb.append('\n');
            }
            HighlightsSequence hs = bag.getHighlights(0, Integer.MAX_VALUE);
            int i = 0;
            while (hs.moveNext()) {
                int startOffset = hs.getStartOffset();
                int endOffset = hs.getEndOffset();
                AttributeSet attrs = hs.getAttributes();
                sb.append("Bag highlight");
                ArrayUtilities.appendBracketedIndex(sb, i, digitCount); // May be actually more/less digits due to splitting/merging
                sb.append(new Highlight(startOffset, endOffset, attrs));
                sb.append('\n');
            }
            context.logOp(sb);
        }
        compoundHighlightsContainer.setLayers(doc, newLayers);
        DirectMergeContainer directMergeContainer = directMergeContainer(context);
        directMergeContainer = new DirectMergeContainer(newLayers, false);
        context.putProperty(DirectMergeContainer.class, directMergeContainer);
    }

    public static void removeLayer(Context context, int zIndex) throws Exception {
        CompoundHighlightsContainer compoundHighlightsContainer = compoundHighlightsContainer(context);
        Document doc = document(context);
        // Possibly do logging
        if (context.isLogOp()) {
            StringBuilder sb = context.logOpBuilder();
            sb.append(" REMOVE_LAYER(").append(zIndex).append(")");
            sb.append("\n");
            context.logOp(sb);
        }

        HighlightsContainer[] layers = compoundHighlightsContainer.getLayers();
        HighlightsContainer[] newLayers = new HighlightsContainer[layers.length - 1];
        zIndex = Math.min(zIndex, layers.length);
        System.arraycopy(layers, 0, newLayers, 0, zIndex);
        System.arraycopy(layers, zIndex + 1, newLayers, zIndex, layers.length - zIndex - 1);
        compoundHighlightsContainer.setLayers(doc, newLayers);

        DirectMergeContainer directMergeContainer = directMergeContainer(context);
        directMergeContainer = new DirectMergeContainer(newLayers, false);
        context.putProperty(DirectMergeContainer.class, directMergeContainer);
    }
    
    public static void checkMerge(Context context, boolean logChecks) {
        CompoundHighlightsContainer compoundHighlightsContainer = compoundHighlightsContainer(context);
        DirectMergeContainer directMergeContainer = directMergeContainer(context);
        Document doc = document(context);
        HighlightsSequence expectedSeq = compoundHighlightsContainer.getHighlights(0, doc.getLength());
        HighlightsSequence testSeq = directMergeContainer.getHighlights(0, doc.getLength());
        int startOffset = 0;
        int endOffset = 0;
        AttributeSet attrs = null;
        int i = 0;
        while (expectedSeq.moveNext()) {
            startOffset = expectedSeq.getStartOffset();
            endOffset = expectedSeq.getEndOffset();
            attrs = expectedSeq.getAttributes();
            assert (testSeq.moveNext()) : "No highlight after <" + startOffset + ", " + endOffset + "> attrs=" + attrs;
            int testStartOffset = testSeq.getStartOffset();
            int testEndOffset = testSeq.getEndOffset();
            AttributeSet testAttrs = testSeq.getAttributes();
            assert (startOffset == testStartOffset) : "startOffset=" + startOffset + " != testStartOffset=" + testStartOffset
                    + ", endOffset=" + endOffset + ", attrs=" + attrs + " seq: " + testSeq;
            assert (endOffset == testEndOffset) : "endOffset=" + endOffset + " != testEndOffset=" + testEndOffset
                    + ", startOffset=" + startOffset + ", attrs=" + attrs + " seq: " + testSeq;
            assert (attrs != null) : "Attrs == null";
            assert (attrs.equals(testAttrs)) : "attrs=" + attrs + " != testAttrs=" + testAttrs
                    + ", startOffset=" + startOffset + ", endOffset=" + endOffset + " seq: " + testSeq;
            if (logChecks) {
                StringBuilder sb = context.logOpBuilder();
                sb.append("DMContainer passed highlight");
                ArrayUtilities.appendBracketedIndex(sb, i, 1); // Unknown digit count
                sb.append(new Highlight(startOffset, endOffset, attrs));
                sb.append('\n');
                context.logOp(sb);
            }
            i++;
        }
    }

    static final class AddLayerOp extends RandomTestContainer.Op {

        public AddLayerOp(String name) {
            super(name);
        }

        @Override
        protected void run(Context context) throws Exception {
            Document doc = document(context);
            Random random = context.container().random();
            int lastOffset = 0;
            List<Object> highlights = new ArrayList<Object>();
            int addCount = random.nextInt(MAX_LAYER_HIGHLIGHT_COUNT);
            for (int i = 0; i < addCount; i++) {
                boolean nextCont = random.nextBoolean();
                int offset0 = nextCont
                        ? lastOffset
                        : random.nextInt(doc.getLength() - lastOffset) + lastOffset;
                int offset1 = random.nextInt(doc.getLength() - offset0) + offset0 + 1;
                AttributeSet attrSet = attrSets[random.nextInt(attrSets.length)];
                highlights.add(offset0);
                highlights.add(offset1);
                highlights.add(attrSet);
            }
            int layerCount = layerCount(context);
            int zIndex = (layerCount > 0) ? random.nextInt(layerCount) : 0;
            addFixedLayer(context, zIndex, highlights.toArray());
        }

    }

    static final class RemoveLayerOp extends RandomTestContainer.Op {

        public RemoveLayerOp(String name) {
            super(name);
        }

        @Override
        protected void run(Context context) throws Exception {
            CompoundHighlightsContainer compoundHighlightsContainer = compoundHighlightsContainer(context);
            Random random = context.container().random();
            HighlightsContainer[] layers = compoundHighlightsContainer.getLayers();
            if (layers.length > 0) {
                int zIndex = random.nextInt(layers.length);
                removeLayer(context, zIndex);
            }
        }

    }
    
    static final class MergeCheck extends RandomTestContainer.Check {

        @Override
        protected void check(Context context) throws Exception {
            checkMerge(context, HighlightsMergeTesting.logChecks);
        }
        
    }

    static final class Highlight {
        
        final int startOffset;
        
        final int endOffset;
        
        final AttributeSet attrs;

        public Highlight(int startOffset, int endOffset, AttributeSet attrs) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.attrs = attrs;
        }
        
        void addTo(OffsetsBag bag) {
            bag.addHighlight(startOffset, endOffset, attrs);
        }

        @Override
        public String toString() {
            return "<" + startOffset + "," + endOffset + ">: " + attrs;
        }

    }

}
