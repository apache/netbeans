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
