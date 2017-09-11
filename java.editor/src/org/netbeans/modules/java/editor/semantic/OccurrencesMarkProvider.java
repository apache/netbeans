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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
