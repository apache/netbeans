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
package org.netbeans.modules.editor.fold.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.spi.editor.fold.ContentReader;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Manages registered ContentReaders for individual Mime types.
 * The cache is flushed iff the set of ContentReader.Factory changes (e.g. during module (un)installation).
 *
 * @author sdedic
 */
public final class FoldContentReaders {
    private static final FoldContentReaders INSTANCE = new FoldContentReaders();
    
    private final Map<String, N> mimeNodes = new HashMap<String, N>();
    
    public static FoldContentReaders get() {
        return INSTANCE;
    }
    
    public CharSequence readContent(String mime, Document d, Fold f, FoldTemplate ft) throws BadLocationException {
        List<ContentReader> readers = getReaders(mime, f.getType());
        for (ContentReader r : readers) {
            CharSequence chs = r.read(d, f, ft);
            if (chs != null) {
                return chs;
            }
        }
        return null;
    }

    private class N implements LookupListener {
        String  mime;
        Lookup.Result result;
        Map<FoldType, List<ContentReader>> readers = new HashMap<FoldType, List<ContentReader>>();

        public N(String mime, Lookup mimeLookup) {
            this.mime = mime;
            init(mimeLookup);
        }
        
        void clear() {
            result.removeLookupListener(this);
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            flush();
        }
        
        private void init(Lookup mimeLookup) {
            Collection<? extends FoldType> types = 
                    new ArrayList<FoldType>((FoldUtilities.getFoldTypes(mime).values()));
            
            result = mimeLookup.lookupResult(ContentReader.Factory.class);
            Collection<? extends ContentReader.Factory> factories = result.allInstances();

            List<ContentReader> l;
            for (FoldType ft : types) {
                l = null;
                for (ContentReader.Factory f : factories) {
                    ContentReader cr = f.createReader(ft);
                    if (cr != null) {
                        if (l == null) {
                            l = new ArrayList<ContentReader>(3);
                        }
                        l.add(cr);
                    }
                }
                if (l != null) {
                    readers.put(ft, l);
                }
            }
            result.addLookupListener(this);
        }
        
        List<ContentReader> readers(FoldType ft) {
            List<ContentReader> r = readers.get(ft);
            return r == null ? Collections.<ContentReader>emptyList() : r;
        }
    }
    
    public List<ContentReader> getReaders(String mime, FoldType ft) {
        N node;
        synchronized (mimeNodes) {
            node = mimeNodes.get(mime);
        }
        if (node == null) {
            node = mimeNodes.get(mime);
            if (node == null) {
                node = new N(mime, MimeLookup.getLookup(mime));
                synchronized (mimeNodes) {
                    N n2 = mimeNodes.get(mime);
                    if (n2 == null) {
                        mimeNodes.put(mime, node);
                    } else {
                        node = n2;
                    }
                }
            }
        }
        return node.readers(ft);
    }
    
    private void flush() {
        synchronized (mimeNodes) {
            for (N n : mimeNodes.values()) {
                n.clear();
            }
            mimeNodes.clear();
        }
    }
}
