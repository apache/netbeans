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

package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.debugger.common2.utils.props.IntegerProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.props.StringProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerAnnotation;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.text.Line;

public final class LineBreakpoint extends NativeBreakpoint {
    
    private FileSystem fs = null;

    public IntegerProperty lineNumber = 
	new IntegerProperty(pos, "lineNumber", null, false, -1); // NOI18N
    public StringProperty fileName =
	new StringProperty(pos, "fileName", null, false, null); // NOI18N

    public LineBreakpoint(int flags) {
	super(LineBreakpointType.getDefault(), flags);
    }

    private void setFileName(String fileName) {
	if (IpeUtils.sameString(this.fileName.toString(), fileName))
	    return;
	this.fileName.set(fileName);
    } 

    public String getShortFileName() {
	if (fileName.get() == null)
	    return "";
	else
	    return CndPathUtilities.getBaseName(fileName.get());
    } 

    public String getFileName() {
	if (fileName.get() == null)
	    return "";
	else
	    return fileName.get();
    } 

    /**
     * Transfer line number information from annotation to slot so
     * getLineNumber() will have something to return before proceeding
     * with the regular NativeBreakpoint.removeAnnotations().
     */

    // override NativeBreakpoint
    @Override
    public void removeAnnotations() {

	int annoLineNo = annoLineNo();
	if (annoLineNo != 0)
	    lineNumber.set(annoLineNo);

	super.removeAnnotations();
    }

    // was package private -- only used by makeEditableCopy
    private void setLineNumber(int newLineNumber) {
	/* DEBUG
	System.out.println("LineBreakpoint.setLineNumber(): " + newLineNumber);
	*/
	if (annoLineNo() != 0) {
	    // DEBUG System.out.println("\thas annotation -- adjusting");
	    // DEBUG Thread.dumpStack();

	    removeAnnotations();

	    // removeAnnotations sets lineNumber, so set it again
	    lineNumber.set(newLineNumber);

	    addAnnotation(fileName.get(), lineNumber.get(), 0);
	} else {
	    // DEBUG System.out.println("\taccepted");
	    lineNumber.set(newLineNumber);
	}
    }

    // was package private -- only used by makeEditableCopy
    public void setLineNumberInitial(int newLineNumber) {
	/* DEBUG
	System.out.println("LineBreakpoint.setLineNumber(): " + newLineNumber);
	*/
	if (annoLineNo() != 0) {
	    // DEBUG System.out.println("\thas annotation -- adjusting");
	    // DEBUG Thread.dumpStack();

	    removeAnnotations();

	    // removeAnnotations sets lineNumber, so set it again
	    lineNumber.setFromObjectInitial(newLineNumber);

	    addAnnotation(fileName.get(), lineNumber.get(), 0);
	} else {
	    // DEBUG System.out.println("\taccepted");
	    lineNumber.setFromObjectInitial(newLineNumber);
	}
    }

    @Override
    protected Line getLine(String filename, int line) {
        if (line != 0 && fs != null) {
            return EditorBridge.getLine(filename, line, fs);
        } else {
            return super.getLine(filename, line);
        }
    }

    @Override
    public NativeBreakpoint makeEditableCopy() {
        LineBreakpoint res = (LineBreakpoint)super.makeEditableCopy();
        res.fs = fs;
        return res;
    }
    
    @Override
    public void seedToplevelAnnotations() {
	super.seedToplevelAnnotations();

	if (NativeDebuggerManager.isPerTargetBpts()) {
	    return;
        }

        addAnnotation(getFileName(), getLineNumber(), 0);
    }

    /**
     * Return a representative line number.
     * If we can get the line number from annotatiosn, get it from there,
     * otherwise fall back on the slot.
     */

    public int getLineNumber() {
	int lineNo = annoLineNo();
	if (lineNo == 0) {
	    lineNo = lineNumber.get();
	    /* DEBUG
	    System.out.println("LineBreakpoint.getLineNumber(): from slot " +
		lineNo);
	    */
	} else {
	    /* DEBUG
	    System.out.println("LineBreakpoint.getLineNumber(): from anno " +
		lineNo);
	    */
	}
	return lineNo;
    }

    /**
     * Extract a representative lineno for this bpt from annotations.
     */
    private int annoLineNo() {
	DebuggerAnnotation[] annotations = annotations(); 
	if (annotations.length > 0) {
	    DebuggerAnnotation a = annotations[0];
	    return a.getLineNo();
	}
	return 0;
    }


    public void setFileAndLine(String fileName, int lineNumber) {
	setFileName(fileName);
	setLineNumber(lineNumber);
    }
    
    public void setFileAndLine(String fileName, int lineNumber, FileSystem fs) {
	setFileName(fileName);
	setLineNumber(lineNumber);
        this.fs = fs;
    }
    
    public void setFilesystem(FileSystem fs) {
        this.fs = fs;
    }

    @Override
    protected final String getSummary() {
	return Catalog.format("CTL_Line_event_name", // NOI18N
		              getFileName(),
		              getLineNumber());
    } 

    @Override
    protected String getDisplayNameHelp() {
	return CndPathUtilities.getBaseName(getFileName()) +
	    ":" + getLineNumber(); // NOI18N
    }

    @Override
    protected void processOriginalEventspec(String oeventspec) {
	assert IpeUtils.isEmpty(oeventspec);
    }

    @Override
    public GroupProperties getGroupProperties() {
        return new LineGroupProperties();
    }

    public FileSystem getFileSystem() {
        return fs;
    }
    
    private class LineGroupProperties extends NativeGroupProperties {
        private final FileObject fo = EditorBridge.findFileObject(getFileName(), getDebugger());

        @Override
        public FileObject[] getFiles() {
            if (fo != null) {
                return new FileObject[]{ fo };
            }
            return null;
        }

        @Override
        public Project[] getProjects() {
            Project p = FileOwnerQuery.getOwner(fo);
            if (p != null) {
                return new Project[]{p};
            } else {
                return null;
            }
        }
    }
}
