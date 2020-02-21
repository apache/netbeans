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


import org.netbeans.modules.cnd.debugger.common2.utils.PhasedProgress;
import org.netbeans.modules.cnd.debugger.common2.debugger.ProgressManager;

public class RtcProgressManager extends ProgressManager {

    private static final String[] levelLabels = new String[] {
	Catalog.get("MSG_PATCHING"),		// NOI18N
	"",
	""
    };
    private final PhasedProgress.CancelListener cancelListener;

    private int rtcProgresssLevel = 0;


    public RtcProgressManager(PhasedProgress.CancelListener cancelListener) {
	super();
	this.cancelListener = cancelListener;
    }

    protected String[] levelLabels() {
	return levelLabels;
    }

    protected String title() {
	return Catalog.get("TITLE_PATCHING");	// NOI18N
    }

    public boolean startProgress(PhasedProgress.CancelListener cancelListener) {
	if (super.startProgress(cancelListener, false)) {
	    phasedProgress().setCancelMsg(Catalog.get("MSG_RTC_CANCEL"));// NOI18N
	    phasedProgress().setVisible(true);
	    return true;
	} else {
	    return false;
	}
    }

    public void updateProgress(char beginEnd, int level,
				  String message, int count, int total) {
	/* OLD
	// We might get updates due to dlopened LO's ...
	if (phasedProgress == null)
	    return;
	*/

	super.updateProgress(beginEnd, level, message, count, total);
    }

    public void rtc_patching(byte beginEnd, String label, String message,
				      int count, int total) {

	// See dbx.gp for a description of usage

	if ("patching".equals(label)) {		// NOI18N
	    if (Log.Rtc.progress) {
		System.out.printf(">>>>>>>>>> rtc_patching %c L%d '%s' %d/%d\n", // NOI18N
		    beginEnd, rtcProgresssLevel, message, count, total);
	    }
	    if (beginEnd == '>') {
		rtcProgresssLevel = 0;
		startProgress(cancelListener);
	    } else {
		finishProgress();
	    }
	} else {
	    if (beginEnd == '>')
		rtcProgresssLevel++;

	    if (Log.Rtc.progress) {
		System.out.printf(">>>>>>>>>> rtc_patching %c L%d '%s' %d/%d\n", // NOI18N
		    beginEnd, rtcProgresssLevel, message, count, total);
	    }

	    if (message == null)
		message = "";

	    if ("lo".equals(label)) {	// NOI18N
		message = Catalog.get("LBL_RTC_LOADOBJECT") + message;
	    } else {
		message = Catalog.get("LBL_RTC_PHASE") + message;
	    }
	    updateProgress((char) beginEnd, rtcProgresssLevel,
					message, count, total);
	    if (beginEnd == '<')
		rtcProgresssLevel--;
	}
    }
}

