/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2016 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
