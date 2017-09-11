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
 * Software is Sun Microsystems, Inc. Portions Copyright 2000-2001 Sun
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
 *
 */
/*
 * Contributor(s): Thomas Ball
 */

/**
 * A simple class for calculating and reporting elapsed system time.
 * Each timer has a start time and may have an end time.  If an
 * elapsed time value is requested of a timer which doesn't have its
 * stop time set, the current system time is used.
 *
 * @author Tom Ball
 */
public class ElapsedTimer {
    // System times in milliseconds; see System.currentTimeMillis();
    private long startTime;
    private long stopTime;

    /**
     * Create a new timer.
     */
    public ElapsedTimer() {
	reset();
    }

    /**
     * Stop the current timer; that is, set its stopTime.
     */
    public void stop() {
	stopTime = System.currentTimeMillis();
    }

    /**
     * Reset the starting time to the current system time.
     */
    public final void reset() {
	startTime = System.currentTimeMillis();
    }

    public long getElapsedMilliseconds() {
	long st = (stopTime == 0) ? System.currentTimeMillis() : stopTime;
	return st - startTime;
    }

    public int getElapsedSeconds() {
	return (int)(getElapsedMilliseconds() / 1000L);
    }

    public String toString() {
	long ms = getElapsedMilliseconds();
	int sec = (int)(ms / 1000L);
	int frac = (int)(ms % 1000L);
	StringBuffer sb = new StringBuffer();
	sb.append(ms / 1000L);
	sb.append('.');
	sb.append(ms % 1000L);
	sb.append(" seconds");
	return sb.toString();
    }
}
