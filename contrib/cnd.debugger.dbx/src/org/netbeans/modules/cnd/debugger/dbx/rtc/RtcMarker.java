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


package org.netbeans.modules.cnd.debugger.dbx.rtc;

import org.openide.text.Line;

import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerAnnotation;

/**
 * A shared annotation between all handlers of
 * RtcController.showErrorInEditor(). 
 * RtcController.showFreameInEditor(). 
 *
 * Any owner can grab the annotation, by setting a line, but only
 * the owner of an annotation may relinquish or clear it.
 * This allows for care-free calls to relinquish and clear.
 */

public class RtcMarker {
    
    private static RtcMarker DEFAULT_ERROR;
    private static RtcMarker DEFAULT_FRAME;

    private final DebuggerAnnotation annotation;
    private Object currentOwner;
    private static final String TYPE_RTC = "RTC"; // NOI18N

    private RtcMarker(String type) {
	annotation = new DebuggerAnnotation(null,
					    type,
					    null,
					    true);
    }

    /**
     * Get sigleton annotation used for access errors.
     */
    public static RtcMarker getDefaultError() {
	if (DEFAULT_ERROR == null)
	    DEFAULT_ERROR = new RtcMarker(TYPE_RTC);
	return DEFAULT_ERROR;
    }

    /**
     * Get sigleton annotation used for locations corresponding to stack frames.
     */
    public static RtcMarker getDefaultFrame() {
	if (DEFAULT_FRAME == null)
	    DEFAULT_FRAME = new RtcMarker(DebuggerAnnotation.TYPE_CALLSITE);
	return DEFAULT_FRAME;
    }


    public void setLine(Object owner, Line line) {
	currentOwner = owner;
	annotation.setLine(line, true);
	if (Log.Rtc.hyperlink) {
	    System.out.printf("RtcMarker.setLine() grabbed by %s\n", owner); // NOI18N
	    // DEBUG Thread.dumpStack();
	}
    }

    public void clearLine(Object owner) {
	if (owner == currentOwner) {
	    annotation.setLine(null, true);
	}
    }

    public void relinquish(Object owner) {
	if (owner == currentOwner) {
	    annotation.setLine(null, true);
	    currentOwner = null;
	    if (Log.Rtc.hyperlink) {
		System.out.printf("RtcMarker.relinquish() by %s\n", owner); // NOI18N
	    }
	} else {
	    if (Log.Rtc.hyperlink) {
		System.out.printf("RtcMarker.relinquish() failed\n"); // NOI18N
		System.out.printf("      currentOwner %s\n", currentOwner); // NOI18N
		System.out.printf("  requesting owner %s\n", owner); // NOI18N
	    }
	}
    }
}
