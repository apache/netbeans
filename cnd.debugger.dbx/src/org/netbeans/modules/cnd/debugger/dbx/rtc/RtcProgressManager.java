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

