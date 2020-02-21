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

/*
 * Patterned on
 * debuggerjpda/ant/src/org/netbeans/modules/debugger/projects/
 *	DebuggerAnnotation.java
 * Asking Hanz to make it part of debuggercore.
 */
package org.netbeans.modules.cnd.debugger.common2.debugger;

import java.util.Date;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.openide.text.Line;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.spi.debugger.ui.BreakpointAnnotation;
import org.openide.util.Utilities;

/**
 * Debugger Annotation class.
 *
 * Annotation types are defined as xml files in the layers directory
 * Editors/AnnotationTypes. See 
 *	src/org/netbeans/modules/debugger/resources/mf-layer.xml
 * for an example. Examples of the xml files are in the same resources
 * directory. I"m not sure where the format of the annotation xml
 * file is described.
 */
public class DebuggerAnnotation
        extends BreakpointAnnotation
        implements PropertyChangeListener {

    public static final String TYPE_CURRENT_PC = "CurrentPC"; // NOI18N
    public static final String TYPE_CALLSITE = "CallSite"; // NOI18N
    public static final String TYPE_BPT = "Breakpoint"; // NOI18N
    public static final String TYPE_BPTX_COMPLEX = "Cond"; // NOI18N
    public static final String TYPE_BPTX_BROKEN = "_broken"; // NOI18N
    public static final String TYPE_BPTX_DISABLED = "Disabled"; // NOI18N
    private final Listener owner;
    private Line line;
    private String type;
    private final long addr;
    private String shortDescription = null;
    private final NativeBreakpoint breakpoint;

    public static interface Listener {

        public void annotationMoved();
    };

    public DebuggerAnnotation(Listener owner, String type, Line line, long addr,
            boolean isCurrent, NativeBreakpoint breakpoint) {
        this.addr = addr;
        this.owner = owner;
        this.type = type;
        this.line = line;
        this.breakpoint = breakpoint;
        ourAttach(line, isCurrent);
    }

    public DebuggerAnnotation(Listener owner, String type, Line line,
            boolean isCurrent) {
        this(owner, type, line, 0, isCurrent, null);
    }

    @Override
    public NativeBreakpoint getBreakpoint() {
        return breakpoint;
    }

    // interface PropertyChangeListener
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        String pName = e.getPropertyName();

        /* DEBUG
        Object v = e.getNewValue();
        System.out.println("Line property " + pName + " changed to " + v);
         */

        // NOTE:
        // We don't get these notifications if a files edits are "discarded"
        // Not sure what we SHOULD do, but for the record filed IZ 62738.
        //
        // We don't get a PROP_DELETED when the line is deleted so the end-user
        // effect is that of bpt glyphs "dropping down". Would be interesting
        // to actually get a DELETED, so filed IZ 63375 which actually asks
        // for PROP_REINSTATED to handle undos of deletes.
        // MSVC 2005 deletes bpts if the line is deleted.

        if (pName == Line.PROP_LINE_NUMBER) {
            if (owner != null) {
                owner.annotationMoved();
            }
        }
    }

    public void setAnnotationType(String newType) {
        String oldType = type;
        if (IpeUtils.sameString(oldType, newType)) {
            return;
        }
        type = newType;
        firePropertyChange(PROP_ANNOTATION_TYPE, oldType, newType);
    }

    public void setLine(Line newLine, boolean isCurrent) {
        if (this.line == newLine) {
            if (this.line == null) {
                ourDetach();
            }
            return;
        }
        if (newLine != null) {
            ourAttach(newLine, isCurrent);
        } else {
            ourDetach();
        }
        this.line = newLine;
    }
    
    /*
     * [re]attach to the line associated with us.
     */
    public void attach(boolean isCurrent) {
        ourAttach(this.line, isCurrent);
    }

    private void ourAttach(Line line, boolean isCurrent) {

        // isCurrent was added as a fix for 6570554.
        // When we follow-fork-both we get "visit"s from both engines's and
        // they both end up here. In general we only want the visits to have
        // visible side-effects for the current session. They will get
        // properly activated when we switch sessions.
        //
        // The parameter is passed and the test is made here rather than
        // the callsite in order to ensure tat all callsites are cognizant of
        // this flag.
        //
        // In order to be conservative pretty much all callsites pass a
        // hard-coded true to minimize regressions.

        if (!isCurrent) {
            return;
        }

        if (line != null) {
            attach(line);
            //remove first to avoid miltiple notfication
            line.removePropertyChangeListener(this);
            line.addPropertyChangeListener(this);
        }
    }

    public void ourDetach() {
        if (this.line != null) {
            this.line.removePropertyChangeListener(this);
            super.detach();
        }
    }

    /**
     * Returns name of the file which describes the annotation type.
     */

    // interface Annotation
    @Override
    public String getAnnotationType() {
        return type;
    }

    public long getAddr() {
        return addr;
    }

    public Line getLine() {
        return line;
    }

    public String getFilename() {
        if (line == null) {
            return null;
        }
        return EditorBridge.filenameFor(line);
    }

    public int getLineNo() {
        if (line == null) {
            return 0;
        }

        int lineNo = line.getLineNumber();

        // Editor numbers lines from 0!
        lineNo += 1;
        return lineNo;
    }

    /**
     * Return true if this Annotation is on the given line
     */
    public boolean matchesLine(String src, int ln) {
        if (line == null) {
            return false;
        }
        String fileName = EditorBridge.filenameFor(line);
        if (Utilities.isWindows()) {
            fileName = fileName.replace("\\", "/"); //NOI18N
        }
        return src.equals(fileName) && getLineNo() == ln;
    }

    /**
     * Return true if file associated with his annotation is newer than
     * the timestamp associated with this annotation.
     * The timestamnp is typically stored in a BreakpointBag.
     */
    public boolean fileIsNewerThan(Date timeStamp) {
        if (line == null) {
            /* DEBUG
            System.out.println("DebuggerAnnotation.fileIsNewerThan(): NO LINE");
             */
            return true;
        }

        Date fileTime = EditorBridge.lastModified(line);
        boolean after = fileTime.after(timeStamp);
        /* DEBUG
        System.out.println("fileIsNewerThan: " +
        "bpt " + timeStamp +
        "  file " + fileTime +
        "  after " + after);
         */
        return after;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    // interface Annotation
    @Override
    public String getShortDescription() {

        // These are used only for simple annotations.
        // combined annotations get their tips specified in
        // ../resources/Dbx_Bpt*.xml
        // See IZ 66668 for a suggestion for a possible remedy.

        if (shortDescription != null) {
            return shortDescription;
        }

        if (type == TYPE_CURRENT_PC) {
            return Catalog.get("TOOLTIP_CURRENT_PC"); // NOI18N
        } else if (type == TYPE_CALLSITE) {
            return Catalog.get("TOOLTIP_CALLSITE"); // NOI18N
        } else if (type.contains(TYPE_BPT)) {
            // SHOULD refine based on other extensions?
            return Catalog.get("TOOLTIP_BREAKPOINT"); // NOI18N
        } else {
            return Catalog.get("TOOLTIP_ANNOTATION"); // NOI18N
        }
    }

    public void enableInstBpt(NativeBreakpoint bpt) {
        NativeDebugger debugger = NativeDebuggerManager.get().currentDebugger();
        if (debugger != null) {
            debugger.InstBptEnabled(this.addr, bpt);
        }
    }

    public void disableInstBpt(NativeBreakpoint bpt) {
        NativeDebugger debugger = NativeDebuggerManager.get().currentDebugger();
        if (debugger != null) {
            debugger.InstBptDisabled(this.addr, bpt);
        }
    }

    public void addInstBpt(NativeBreakpoint bpt) {
        NativeDebugger debugger = NativeDebuggerManager.get().currentDebugger();
        if (debugger != null) {
            debugger.InstBptAdded(this.addr, bpt);
        }
    }

    public void removeInstBpt(NativeBreakpoint bpt) {
        NativeDebugger debugger = NativeDebuggerManager.get().currentDebugger();
        if (debugger != null) {
            debugger.InstBptRemoved(this.addr, bpt);
        }
    }
}
