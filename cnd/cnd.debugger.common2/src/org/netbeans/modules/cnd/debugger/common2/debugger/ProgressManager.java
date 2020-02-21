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

