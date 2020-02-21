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

package org.netbeans.modules.cnd.debugger.common2.debugger;

import javax.swing.SwingUtilities;

import org.netbeans.modules.cnd.debugger.common2.utils.PhasedProgress;

public abstract class ProgressManager {
    private final boolean old = false;	// use old progress bar	
					// for comparison

    private PhasedProgress phasedProgress;

    protected ProgressManager() {
    }

    protected PhasedProgress phasedProgress() {
	return phasedProgress;
    }

    protected abstract String[] levelLabels();

    protected String title() {
	String title = javax.swing.UIManager.
	    getString("ProgressMonitor.progressText");	// NOI18N
	return title;
    }

    public boolean startProgress(PhasedProgress.CancelListener cancelListener,
				 boolean shortNames) {

	assert SwingUtilities.isEventDispatchThread();
	if (old) {
	    NativeDebuggerManager.get().updateProgress("Progress", "START", 0); // NOI18N
	    return false;
	}
	if (phasedProgress != null) {
	    return false;
        }
	int cols = shortNames? 40: 60;
	phasedProgress = new PhasedProgress(title(),
					   levelLabels(),
					   cancelListener,
					   cols);
        setCancelListener(cancelListener);
	return true;
    }

    public void finishProgress() {
	assert SwingUtilities.isEventDispatchThread();
	if (old) {
	    NativeDebuggerManager.get().cancelProgress();
	}
	if (phasedProgress != null) {
	    phasedProgress.setVisible(false);
	    phasedProgress.dispose();
	    phasedProgress = null;
	}
    }

    public void updateProgress(char beginEnd, int level,
				    String message, int count, int total) {
	assert SwingUtilities.isEventDispatchThread();
	if (old) {
	    if (beginEnd == '>') {
		NativeDebuggerManager.get().updateProgress(null,
		    "UPDATE " + message, 10); // NOI18N
	    }
	    return;
	}

	// 6831432
	if (isCancelled())
	    return;

	if (beginEnd == '>') {
	    if (count == 0)
		total = 0;
	    phasedProgress.setMessageFor(level, message, total);
	} else {
	    phasedProgress.setProgressFor(level, count);
	}
    }

    public boolean isCancelled() {
	if (old) 
	    return false;

	if (phasedProgress == null)
	    return true;
	else
	    return phasedProgress.isCancelled();
    }

    public void setCancelListener(PhasedProgress.CancelListener cl) {
	if (phasedProgress != null) {
	    phasedProgress.setCancelListener(cl);
        }
    }
}

