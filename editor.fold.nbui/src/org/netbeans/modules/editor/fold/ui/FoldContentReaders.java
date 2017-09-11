/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
