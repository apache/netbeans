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

package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Date;

import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

import org.xml.sax.Attributes;
import org.netbeans.modules.cnd.api.xml.*;

import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerAnnotation;
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorBridge;

import org.netbeans.modules.cnd.debugger.common2.values.Enum;

import org.netbeans.modules.cnd.debugger.common2.utils.props.Property;
import org.netbeans.modules.cnd.debugger.common2.utils.props.EnumProperty;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.spi.debugger.ui.BreakpointType;
import org.openide.filesystems.FileSystem;

class BreakpointXMLCodec extends XMLDecoder implements XMLEncoder {

    // map a type name to a NativeBreakpointType
    private final HashMap<String, BreakpointType> types;
    private BreakpointBag bag;		// ... to store bpts into
    private NativeBreakpoint parent;	// ... of sub breakpoints

    private NativeBreakpoint currentBreakpoint;	// decoded
    private NativeBreakpoint bpt;		// encoded

    private static final String TAG_BREAKPOINT = "breakpoint";  // NOI18N
    private static final String ATTR_BREAKPOINT_TYPE = "type";  // NOI18N
    private static final String ATTR_TIMESTAMP = "timestamp";	// NOI18N

    private static final String TAG_ATTRIBUTES = "attributes";  // NOI18N

    private static final String TAG_ANNOTATIONS = "annotations";  // NOI18N

    private static final String TAG_ANNOTATION = "annotation";  // NOI18N
    private static final String ATTR_ANNOTATION_FILE = "file";  // NOI18N
    private static final String ATTR_ANNOTATION_LINE = "line";  // NOI18N
    
    private static final String ATTR_ANNOTATION_FS = "fileSystem";  // NOI18N

    /**
     * decoder form
     */
    BreakpointXMLCodec(BreakpointBag bag,
		       NativeBreakpoint parent,
		       HashMap<String, BreakpointType> types) {
	this.bag = bag;
	this.parent = parent;
	this.types = types;
	if (parent == null || parent.isToplevel())
	    registerXMLDecoder(new SubBreakpointsXMLCodec(this, types));
    }

    /**
     * encoder form
     */
    BreakpointXMLCodec(NativeBreakpoint bpt) {
	this.bpt = bpt;
        this.types = null;
    }

    NativeBreakpoint currentBreakpoint() {
	return currentBreakpoint;
    } 

    // interface XMLDecoder
    @Override
    protected String tag() {
	return TAG_BREAKPOINT;
    } 

    // interface XMLDecoder
    @Override
    public void start(Attributes atts) {
	if (Log.Bpt.xml)
	    System.out.printf("BreakpointXMLCodec().start(%s)\n", tag()); // NOI18N

	String typeName = atts.getValue(ATTR_BREAKPOINT_TYPE);
	String dateString = atts.getValue(ATTR_TIMESTAMP);

	// If there's no timestamp attribute of the string is malformed we
	// end up having the breakpoints being "very old" and perpetually
	// out of date
	Date date = new Date(0);
	try {
	    date = new Date(Long.parseLong(dateString));
	} catch (NumberFormatException ex) {
	}

	NativeBreakpointType type = (NativeBreakpointType)types.get(typeName);

	try {
	    int flags = NativeBreakpoint.RESTORED;
	    // Midlevel bpts will be added by restoreChild
	    if (parent == null)
		flags |= NativeBreakpoint.TOPLEVEL;
	    else if (parent.isMidlevel())
		flags |= NativeBreakpoint.SUBBREAKPOINT;
	    else
		flags |= NativeBreakpoint.MIDBREAKPOINT;

	    currentBreakpoint = type.newInstance(flags);
	    currentBreakpoint.restoreTimestamp(date);

	    // 6568407
	    if (bag != null) {
		if (Log.Bpt.xml)
		    System.out.printf("\ttoplevel\n"); // NOI18N
		bag.restore(currentBreakpoint);
	    }

	    if (parent != null) {
		if (Log.Bpt.xml)
		    System.out.printf("\tsub-bpt\n"); // NOI18N
		parent.restoringChild(currentBreakpoint);
	    }

	} catch (Exception x) {
	    ErrorManager.getDefault().annotate(x,
		"Failed to parse bpt from XML"); // NOI18N
	    ErrorManager.getDefault().notify(x);
	}
    }

    // interface XMLDecoder
    @Override
    public void end() {
	if (Log.Bpt.xml)
	    System.out.printf("BreakpointXMLCodec().end(%s)\n", tag()); // NOI18N

	if (currentBreakpoint == null) {
	    if (Log.Bpt.xml)
		System.out.printf("\tno currentBreakpoint\n"); // NOI18N
	    return;
	} else if (bag != null) {
	    /* OLD 6568407
	    if (Log.Bpt.xml)
		System.out.printf("\ttoplevel\n");
	    bag.restore(currentBreakpoint);
	    */
	} else if (parent != null) {
	    if (Log.Bpt.xml)
		System.out.printf("\tsub-bpt\n"); // NOI18N
	    // OLD parent.restoreChild(currentBreakpoint);
	    parent.restoredChild();
	} else {
	    if (Log.Bpt.xml)
		System.out.printf("\tno parent and no bag\n"); // NOI18N
	}

	currentBreakpoint = null;
    }

    // interface XMLDecoder
    @Override
    public void startElement(String element, Attributes atts) {

	if (Log.Bpt.xml)
	    System.out.printf("BreakpointXMLCodec().startElement(%s)\n", element); // NOI18N

	if (element.equals(TAG_ATTRIBUTES)) {
	    if (currentBreakpoint != null) {
		currentBreakpoint.setAttrs(atts);
	    }

	} else if (element.equals(TAG_ANNOTATION)) {
	    String file = atts.getValue(ATTR_ANNOTATION_FILE);
	    String line = atts.getValue(ATTR_ANNOTATION_LINE);
            String fsUrl = atts.getValue(ATTR_ANNOTATION_FS);
	    try {
		int lineNo = Integer.parseInt(line);
                FileSystem fs = CndFileUtils.getLocalFileSystem();
                try {
                    if (fsUrl != null) {
                        fs = CndFileUtils.urlToFileObject(fsUrl).getFileSystem();
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
		if (currentBreakpoint != null) {
		    currentBreakpoint.addAnnotation(EditorBridge.getLine(file, lineNo, fs), 0);
		}
	    } catch (NumberFormatException x) {
		ErrorManager.getDefault().annotate(x,
		    "Bad line number in annotation: " + line); // NOI18N
		ErrorManager.getDefault().notify(ErrorManager.WARNING, x);
	    }
	}
    }

    // interface XMLDecoder
    @Override
    public void endElement(String element, String currentText) {
	if (Log.Bpt.xml)
	    System.out.printf("BreakpointXMLCodec().endElement(%s)\n", element); // NOI18N
    }

    private static AttrValuePair[] attributeAttrs(NativeBreakpoint bpt) {
	AttrValuePairs attrs = new AttrValuePairs();
	for (Iterator iter = bpt.iterator(); iter.hasNext();) {
	    Property p = (Property) iter.next();
	    String pname = p.name();
	    Object pobject = p.getAsObject();
	    if (pobject == null)	// don't save unset properties
		continue;
	    if (p instanceof EnumProperty) { 
		Enum eobject = (Enum) pobject;
		attrs.add(pname, eobject.name());
	    } else {
		attrs.add(pname, pobject.toString());
	    }
	} 
	return attrs.toArray();
    }

    private void encodeAnnotations(XMLEncoderStream xes) {
	DebuggerAnnotation[] annotations = bpt.annotations();
	if (annotations.length > 0) {
	    xes.elementOpen(TAG_ANNOTATIONS);
	    for (DebuggerAnnotation a : annotations) {
                ArrayList<AttrValuePair> annotationAttrs = new ArrayList<AttrValuePair>();
		String fileName = a.getFilename();
		if (fileName == null) {
		    continue;
                }
                annotationAttrs.add(new AttrValuePair(ATTR_ANNOTATION_FILE, fileName));
                
		int lineNo = a.getLineNo();
		if (lineNo == 0) {
		    continue;
                }
                annotationAttrs.add(new AttrValuePair(ATTR_ANNOTATION_LINE, "" + lineNo));
                
                DataObject dObj = EditorBridge.dataObjectForLine(a.getLine());
                if (dObj != null) {
                    try {
                        FileSystem fs = dObj.getPrimaryFile().getFileSystem();
                        if (!CndFileUtils.isLocalFileSystem(fs)) {
                            annotationAttrs.add(new AttrValuePair(ATTR_ANNOTATION_FS,
                                CndFileUtils.fileObjectToUrl(fs.getRoot()).toString()));
                        }
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                
		xes.element(TAG_ANNOTATION, annotationAttrs.toArray(new AttrValuePair[annotationAttrs.size()]));
	    }
	    xes.elementClose(TAG_ANNOTATIONS);
	}
    }

    // pseudo-interface XMLEncoder
    @Override
    public void encode(XMLEncoderStream xes) {
	bpt.prepareForSaving();

	String type = bpt.getBreakpointType().id();
	String timestampStr = Long.toString(bpt.timestamp().getTime());

	AttrValuePair breakpointAttrs[] = new AttrValuePair[] {
	    new AttrValuePair(ATTR_BREAKPOINT_TYPE, type),
	    new AttrValuePair(ATTR_TIMESTAMP, timestampStr),
	};

	xes.elementOpen(TAG_BREAKPOINT, breakpointAttrs);
	    xes.element(TAG_ATTRIBUTES, attributeAttrs(bpt));
	    encodeAnnotations(xes);
	    SubBreakpointsXMLCodec encoder = new SubBreakpointsXMLCodec(bpt);
	    encoder.encode(xes);
	xes.elementClose(TAG_BREAKPOINT);
    }
}
