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
package org.netbeans.modules.cnd.highlight.semantic;

import java.awt.Color;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

public class OccurrencesMarkProvider extends MarkProvider {
    
    private static final Map<Document, Reference<OccurrencesMarkProvider>> providers = new WeakHashMap<>();
    
    public static synchronized OccurrencesMarkProvider get(Document doc) {
        Reference<OccurrencesMarkProvider> ref = providers.get(doc);
        OccurrencesMarkProvider p = ref != null ? ref.get() : null;
        
        if (p == null) {
            providers.put(doc, new WeakReference<>(p = new OccurrencesMarkProvider()));
        }
        
        return p;
    }
    
    private List<Mark> semantic;
    private List<Mark> occurrences;
    private List<Mark> joint;
    
    /** Creates a new instance of OccurrencesMarkProvider */
    private OccurrencesMarkProvider() {
        semantic = Collections.emptyList();
        occurrences = Collections.emptyList();
        joint = Collections.emptyList();
    }
    
    @Override
    public synchronized List<Mark> getMarks() {
        return joint;
    }
    
    public void setSematic(Collection<Mark> s) {
        List<Mark> old;
        List<Mark> nue;
        
        synchronized (this) {
            semantic = new ArrayList<>(s);
            
            old = joint;
            
            nue = joint = new ArrayList<>();
            
            joint.addAll(semantic);
            joint.addAll(occurrences);
        }
        
        firePropertyChange(PROP_MARKS, old, nue);
    }
    
    public void setOccurrences(Collection<Mark> s) {
        List<Mark> old;
        List<Mark> nue;
        
        synchronized (this) {
            occurrences = new ArrayList<>(s);
            
            old = joint;
            
            nue = joint = new ArrayList<>();
            
            joint.addAll(semantic);
            joint.addAll(occurrences);
        }
        
        //#85919: fire outside the lock:
        firePropertyChange(PROP_MARKS, old, nue);
    }
    
    public static Collection<Mark> createMarks(final Document doc, final Collection<CsmReference> list, final Color color, final String tooltip) {
        final List<Mark> result = new LinkedList<>();
        
        doc.render(new Runnable() {
            @Override
            public void run() {
                for (CsmReference ref : list) {
                    try {
                        int offset = getDocumentOffset(doc, ref.getStartOffset());
                        if (offset >= 0 && offset < doc.getLength()) {
                            result.add(new MarkImpl(doc, doc.createPosition(offset), color, tooltip));
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
        
        return result;
    }

    private static int getDocumentOffset(Document doc, int fileOffset) {
        return CsmMacroExpansion.getOffsetInExpandedText(doc, fileOffset);
    }
    
    private static final class MarkImpl implements Mark {

        private final Document doc;
        private final Position startOffset;
        private final Color color;
        private final String tooltip;

        public MarkImpl(Document doc, Position startOffset, Color color, String tooltip) {
            this.doc = doc;
            this.startOffset = startOffset;
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
            int line = NbDocument.findLineNumber((StyledDocument) doc, startOffset.getOffset());
            
            return new int[] {line, line};
        }

        @Override
        public String getShortDescription() {
            return tooltip;
        }
        
    }
}
