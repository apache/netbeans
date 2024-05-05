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
package org.netbeans.modules.lsp.client.bindings;

import java.awt.Color;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;
import org.openide.text.NbDocument;

/**
 * This is based on the OccurrencesMarkProvider from csl.api
 */
public class OccurrencesMarkProvider extends MarkProvider {

    private static final Map<Document, Reference<OccurrencesMarkProvider>> providers = new WeakHashMap<>();

    @SuppressWarnings("NestedAssignment")
    public static synchronized OccurrencesMarkProvider get(Document doc) {
        Reference<OccurrencesMarkProvider> ref = providers.get(doc);
        OccurrencesMarkProvider p = ref != null ? ref.get() : null;

        if (p == null) {
            p = new OccurrencesMarkProvider();
            providers.put(doc, new WeakReference(p));
        }

        return p;
    }

    private List<Mark> occurrences = Collections.emptyList();

    @Override
    public List<Mark> getMarks() {
        return Collections.unmodifiableList(occurrences);
    }

    public void setOccurrences(final Document doc, final List<int[]> bag, final Color color, final String tooltip) {

        List<Mark> old;

        synchronized (this) {
            old = occurrences;

            if(doc.getProperty(OccurrencesMarkProvider.class) == null || bag == null) {
                occurrences = Collections.emptyList();
            } else {
                occurrences = bag
                        .stream()
                        .map(hs -> new MarkImpl(doc, hs[0], color, tooltip))
                        .collect(Collectors.toList());
            }

        }

        firePropertyChange(PROP_MARKS, old, occurrences);
    }

    private static final class MarkImpl implements Mark {

        private final int line;
        private final Color color;
        private final String tooltip;

        public MarkImpl(Document doc, int startOffset, Color color, String tooltip) {
            this.line = NbDocument.findLineNumber((StyledDocument) doc, startOffset);
            this.color = color;
            this.tooltip = tooltip;
        }

        @Override
        public int getType() {
            return TYPE_ERROR_LIKE;
        }

        @Override
        public Status getStatus() {
            return Status.STATUS_OK;
        }

        @Override
        public int getPriority() {
            return PRIORITY_DEFAULT;
        }

        @Override
        public Color getEnhancedColor() {
            return color;
        }

        @Override
        public int[] getAssignedLines() {
            return new int[] {line, line};
        }

        @Override
        public String getShortDescription() {
            return tooltip;
        }

    }
}
