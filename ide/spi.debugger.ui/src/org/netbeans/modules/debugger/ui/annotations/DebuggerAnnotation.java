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

package org.netbeans.modules.debugger.ui.annotations;

import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import org.netbeans.api.debugger.Watch;
import org.netbeans.spi.debugger.ui.PinWatchUISupport;
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

    /** Annotation type constant. */
    static final String WATCH_ANNOTATION_TYPE = "PinnedWatch";

    private final Line        line;
    private final String      type;
    private Watch             watch;

    DebuggerAnnotation (String type, Line line) {
        this.type = type;
        this.line = line;
        attach (line);
    }
    
    DebuggerAnnotation (String type, Line.Part linePart) {
        this.type = type;
        this.line = linePart.getLine();
        attach (linePart);
    }
    
    @Override
    public String getAnnotationType () {
        return type;
    }
    
    void setWatch(Watch watch) {
        this.watch = watch;
    }
    
    Line getLine () {
        return line;
    }
    
    @NbBundle.Messages("TOOLTIP_WATCH_PIN=Watch")
    @Override
    public String getShortDescription () {
        if (type == WATCH_ANNOTATION_TYPE) {
            return Bundle.TOOLTIP_WATCH_PIN();
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
    
    @Override
    public Lookup getLookup() {
        if (watch == null) {
            return Lookup.EMPTY;
        } else {
            return Lookups.singleton(watch);
        }
    }
    
}
