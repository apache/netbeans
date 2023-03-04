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

package org.netbeans.modules.debugger.jpda.projectsui;

import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotatable;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;


/**
 * Debugger Annotation class.
 *
 * @author   Jan Jancura
 */
public class DebuggerAnnotation extends Annotation implements Lookup.Provider {

    private final Line        line;
    private final String      type;
    private final JPDAThread  thread;


    DebuggerAnnotation (String type, Line line, JPDAThread thread) {
        this.type = type;
        this.line = line;
        this.thread = thread;
        if ("Breakpoint".equals(type)) {    // Bug #169096
            Exceptions.printStackTrace(new IllegalStateException("Wrong annotation type: "+type+". Please report to bug #169096."));
        }
        attach (line);
    }
    
    DebuggerAnnotation (String type, Line.Part linePart) {
        this.type = type;
        this.line = linePart.getLine();
        this.thread = null;
        if ("Breakpoint".equals(type)) {    // Bug #169096
            Exceptions.printStackTrace(new IllegalStateException("Wrong annotation type: "+type+". Please report to bug #169096."));
        }
        attach (linePart);
    }
    
    DebuggerAnnotation (String type, AttributeSet attrs, int start, int end, FileObject fo) {
        this.type = type;
        this.line = null;
        this.thread = null;
        if ("Breakpoint".equals(type)) {    // Bug #169096
            Exceptions.printStackTrace(new IllegalStateException("Wrong annotation type: "+type+". Please report to bug #169096."));
        }
        attach (new HighlightAnnotatable(attrs, start, end, fo));
    }
    
    @Override
    public String getAnnotationType () {
        return type;
    }
    
    Line getLine () {
        return line;
    }
    
    @Override
    public String getShortDescription () {
        if (type == EditorContext.CURRENT_LINE_ANNOTATION_TYPE) {
            return NbBundle.getMessage 
                (DebuggerAnnotation.class, "TOOLTIP_CURRENT_PC"); // NOI18N
        }
        if (type == EditorContext.CURRENT_EXPRESSION_CURRENT_LINE_ANNOTATION_TYPE) {
            return NbBundle.getMessage 
                (DebuggerAnnotation.class, "TOOLTIP_CURRENT_EXP_LINE"); // NOI18N
        } else if (type == EditorContext.CALL_STACK_FRAME_ANNOTATION_TYPE) {
            return NbBundle.getBundle (DebuggerAnnotation.class).getString 
                ("TOOLTIP_CALLSITE"); // NOI18N
        }
        if (type == EditorContext.OTHER_THREAD_ANNOTATION_TYPE) {
            return NbBundle.getMessage(DebuggerAnnotation.class, "TOOLTIP_OTHER_THREAD", thread.getName());
        }
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException("Unknown annotation type '"+type+"'."));
        return null;
    }
    
    static synchronized OffsetsBag getHighlightsBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(DebuggerAnnotation.class);
        if (bag == null) {
            doc.putProperty(DebuggerAnnotation.class, bag = new OffsetsBag(doc, true));
        }
        return bag;
    }
    
    private static final class HighlightAnnotatable extends Annotatable {
        
        private AttributeSet attrs;
        private int start;
        private int end;
        private Document doc;
        
        public HighlightAnnotatable(AttributeSet attrs, int start, int end, FileObject fo) {
            this.attrs = attrs;
            this.start = start;
            this.end = end;
            try {
                DataObject dobj = DataObject.find(fo);
                EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
                if (ec != null) {
                    doc = ec.getDocument();
                    if (doc == null) {
                        try {
                            doc = ec.openDocument();
                        } catch (java.io.IOException ioex) {}
                    }
                }
            } catch (DataObjectNotFoundException ex) {
            }
        }
        
        @Override
        public String getText() {
            return null;
        }

        @Override
        protected void addAnnotation(Annotation anno) {
            synchronized(DebuggerAnnotation.class) {
                if (doc != null) {
                    OffsetsBag bag = getHighlightsBag(doc);
                    bag.addHighlight(start, end, attrs);
                }
            }
        }

        @Override
        protected void removeAnnotation(Annotation anno) {
            synchronized(DebuggerAnnotation.class) {
                if (doc != null) {
                    OffsetsBag bag = getHighlightsBag(doc);
                    bag.removeHighlights(start, end, false);
                }
            }
        }

    }

    @Override
    public Lookup getLookup() {
        if (thread == null) {
            return Lookup.EMPTY;
        } else {
            return Lookups.singleton(thread);
        }
    }
    
}
