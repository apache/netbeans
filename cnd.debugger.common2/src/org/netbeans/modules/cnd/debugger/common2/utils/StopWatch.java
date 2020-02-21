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

package org.netbeans.modules.cnd.debugger.common2.utils;

import java.util.ArrayList;

/**
 * A timing utiltiy. Use as follows:
 <pre>
    StopWatch sw = new StopWatch("parsing");
    sw.start();
     ... prepare
    sw.mark("preparation")
    ... pass1
    sw.mark("pass1")
    ... pass2
    sw.mark("pass2")
    sw.stop();
    sw.dump();
 </pre>
 */
public final class StopWatch {

    private static final class Mark {
	private final String name;
	private final long time;

	public Mark(String name) {
	    this.name = name;
	    this.time = System.currentTimeMillis();
	}

	public String name() {
	    return name;
	}

	public long time() {
	    return time;
	}
    }

    private final String name;
    private final ArrayList<Mark> marks = new ArrayList<Mark>();

    private boolean running;

    public StopWatch(String name) {
	this.name = name;
    }

    public void start() {
	marks.clear();
	marks.add(new Mark("start"));			// NOI18N
	running = true;
    }

    public void stop() {
	marks.add(new Mark("stop"));			// NOI18N
	running = false;
    }

    public void mark(String name) {
	if (running)
	    marks.add(new Mark(name));
    }

    private long totalElapsedTime() {
	if (marks.size() <= 1) {
	    return 0;
	} else {
	    long startTime = marks.get(0).time();
	    long endTime = marks.get(marks.size()-1).time();
	    return endTime - startTime;
	}
    }

    /**
     * Dump the contents of the stopwatch unto stdout.
     * If 'thresholdMillis' is 0 will always dump, otherwise will dump
     * only if the total elapsed time exceeds the threshold.
     */

    public void dump(int thresholdMillis) {

	// If we care about the threshold but elapsed time is below it
	// don't bother printing

	if (thresholdMillis > 0 && totalElapsedTime() < thresholdMillis)
	    return;

	System.out.printf("StopWatch[%s]\n", name);	// NOI18N
	if (marks.size() == 0) {
	    System.out.printf("\tnever started\n");	// NOI18N
	    return;
	} else if (running) {
	    System.out.printf("\tstill running\n");	// NOI18N
	    return;
	}
	long startTime = marks.get(0).time();
	for (Mark mark : marks) {
	    System.out.printf("%-10s: %d\n",		// NOI18N
		mark.name(), mark.time() - startTime);
	}
    }

    public void dump() {
	dump(0);
    }
}
