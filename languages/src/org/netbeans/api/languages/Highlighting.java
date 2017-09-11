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

package org.netbeans.api.languages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.Map;
import javax.swing.event.EventListenerList;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;


/**
 * Support for semantic highlighting.
 * 
 * @author Jan Jancura
 */
public class Highlighting {
    
    
    private static Map<Document,WeakReference<Highlighting>> highlightings = new WeakHashMap<Document,WeakReference<Highlighting>> ();

    
    /**
     * Returns Highlighting for given document.
     * 
     * @param document a document
     */
    public static Highlighting getHighlighting (Document document) {
        WeakReference<Highlighting> wr = highlightings.get (document);
        Highlighting highlighting = wr == null ? null : wr.get ();
        if (highlighting == null) {
            highlighting = new Highlighting (document);
            highlightings.put (document, new WeakReference<Highlighting> (highlighting));
        }
        return highlighting;
    }
    
    
    private Document                                document;
    private EventListenerList                       listeners;
    
    
    private Highlighting (Document document) {
        this.document = document;
    }
    
    /**
     * Defines highlighting for given item.
     * 
     * @param item a item
     * @param as set of highlighting attributes
     */
//    public Highlight highlight (int start, int end, AttributeSet as) {
//        return highlight (item.getOffset (), item.getEndOffset (), as);
//    }
    
    /**
     * Returns highlighting for given AST item.
     * 
     * @param highlighting for given AST item
     */
//    public AttributeSet get (ASTItem item) {
//        Highlight highlight = get (
//            item.getOffset (), 
//            item.getEndOffset ()
//        );
//        if (highlight == null) return null;
//        return highlight.attributeSet;
//    }
    
    private Set<Highlight> items = new HashSet<Highlight> ();
    
    public AttributeSet get (int start, int end) {
        Highlight highlight = getHighlight (start, end);
        if (highlight == null) return null;
        return highlight.attributeSet;
    }
    
    private Highlight getHighlight (int start, int end) {
        Iterator<Highlight> it = items.iterator ();
        while (it.hasNext()) {
            Highlight item =  it.next();
            if (item.start.getOffset () == start && item.end.getOffset () == end)
                return item;
        }
        return null;
    }
    
    public Highlight highlight (int startOffset, int endOffset, AttributeSet as) {
        try {
            Highlight result = new Highlight (
                document.createPosition (startOffset),
                document.createPosition (endOffset),
                as
            );
            items.add (result);
            fire (startOffset, endOffset);
            return result;
        } catch (BadLocationException ex) {
            ex.printStackTrace ();
            return null;
        }
    }
    
    public void addPropertyChangeListener (PropertyChangeListener l) {
        if (listeners == null)
            listeners = new EventListenerList ();
        listeners.add (PropertyChangeListener.class, l);
    }
    
    public void removePropertyChangeListener (PropertyChangeListener l) {
        if (listeners == null) return;
        listeners.remove (PropertyChangeListener.class, l);
    }
    
    protected void fire (int startOffset, int endOffset) {
        if (listeners == null) return;
        Object[] l = listeners.getListenerList ();
        PropertyChangeEvent event = null;
        for (int i = l.length-2; i>=0; i-=2) {
            if (event == null)
                event = new PropertyChangeEvent (this, null, startOffset, endOffset);
            ((PropertyChangeListener) l [i+1]).propertyChange (event);
        }
    }
     
    public class Highlight {
        private Position start, end;
        private AttributeSet attributeSet;
        
        private Highlight (Position start, Position end, AttributeSet attributeSet) {
            this.start = start;
            this.end = end;
            this.attributeSet = attributeSet;
        }
        
        public void remove () {
            items.remove (this);
            fire (start.getOffset (), end.getOffset ());
        }
    }
}


