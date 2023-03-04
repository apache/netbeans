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
package org.netbeans.modules.java.editor.semantic;

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
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class OccurrencesMarkProvider extends MarkProvider {
    
    private static Map<Document, Reference<OccurrencesMarkProvider>> providers = new WeakHashMap<Document, Reference<OccurrencesMarkProvider>>();
    
    public static synchronized OccurrencesMarkProvider get(Document doc) {
        Reference<OccurrencesMarkProvider> ref = providers.get(doc);
        OccurrencesMarkProvider p = ref != null ? ref.get() : null;
        
        if (p == null) {
            providers.put(doc, new WeakReference(p = new OccurrencesMarkProvider()));
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
    
    public synchronized List getMarks() {
        return joint;
    }
    
    public void setSematic(Collection<Mark> s) {
        List<Mark> old;
        List<Mark> nue;
        
        synchronized (this) {
            semantic = new ArrayList<Mark>(s);
            
            old = joint;
            
            nue = joint = new ArrayList<Mark>();
            
            joint.addAll(semantic);
            joint.addAll(occurrences);
        }
        
        //#85919: fire outside the lock:
        firePropertyChange(PROP_MARKS, old, nue);
    }
    
    public void setOccurrences(Collection<Mark> s) {
        List<Mark> old;
        List<Mark> nue;
        
        synchronized (this) {
            occurrences = new ArrayList<Mark>(s);
            
            old = joint;
            
            nue = joint = new ArrayList<Mark>();
            
            joint.addAll(semantic);
            joint.addAll(occurrences);
        }
        
        //#85919: fire outside the lock:
        firePropertyChange(PROP_MARKS, old, nue);
    }
    
    public static Collection<Mark> createMarks(final Document doc, final List<int[]> bag, final Color color, final String tooltip) {
        final List<Mark> result = new LinkedList<Mark>();
        
        doc.render(new Runnable() {
            public void run() {
                int docLen = doc.getLength();
                for (int[] span : bag) {
                    try {
                        int offset = span[0];
                        if (offset >= 0 && offset <= docLen) {
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
    
    private static final class MarkImpl implements Mark {

        private Document doc;
        private Position startOffset;
        private Color color;
        private String tooltip;

        public MarkImpl(Document doc, Position startOffset, Color color, String tooltip) {
            this.doc = doc;
            this.startOffset = startOffset;
            this.color = color;
            this.tooltip = tooltip;
        }

        public int getType() {
            return TYPE_ERROR_LIKE;
        }

        public Status getStatus() {
            return Status.STATUS_OK;
        }

        public int getPriority() {
            return PRIORITY_DEFAULT;
        }

        public Color getEnhancedColor() {
            return color;
        }

        public int[] getAssignedLines() {
            int line = NbDocument.findLineNumber((StyledDocument) doc, startOffset.getOffset());
            
            return new int[] {line, line};
        }

        public String getShortDescription() {
            return tooltip;
        }
        
    }
}
