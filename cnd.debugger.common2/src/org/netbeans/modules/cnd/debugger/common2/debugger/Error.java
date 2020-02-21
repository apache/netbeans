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

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

/**
 * Encapsulates lists of error messages.
 */

public class Error {

    /**
     * Error severity.
     */
    public static enum Severity {
	NONE, WARNING, ERROR		// DON'T CHANGE THE ORDER
    };

    public static final class Msg {
	private final Severity severity;
	private final String msg;
	private final String cookie;

	/**
	 * Create a single error message.
	 * @param severity Severity of the message
	 * @param msg Translated (by engine) text of the message.
	 * @param cookie Untranslated keyword for special actions to be taken.
	 */
	public Msg(Severity severity, String msg, String cookie) {
	    this.severity = severity;
	    this.msg = msg;
	    this.cookie = cookie;
	}

	public Severity severity() {
	    return severity;
	}

	public String msg() {
	    return msg;
	}

	public String cookie() {
	    return cookie;
	}
    }

    private final Msg errors[];
    private Severity maxSeverity = Severity.NONE;
    private String errorToken = null;
    private String msg = null;
    private String text = null;
    private boolean processed = false;

    protected Error(Msg errors[]) {
	this.errors = errors;
    }

    public boolean isRedundantPathmap() {
	// XXX WORKAROUND for dbx problem - see bugid (bugid here)
	final String theMsg = "dbx: Already mapping path";	// NOI18N
	return errors.length > 0 &&
	       errors[0].msg != null &&
	       errors[0].msg.startsWith(theMsg);
    }

    public boolean isCancelled() {
	return IpeUtils.sameString("cancelled", errorToken()); // NOI18N
    }

    public boolean isOodSrc() {
	return IpeUtils.sameString("source-modified", errorToken()); // NOI18N
    }

    public boolean isXExec64() {
	return IpeUtils.sameString("xexec64", errorToken()); // NOI18N
    }

    public boolean isXExec32() {
	return IpeUtils.sameString("xexec32", errorToken()); // NOI18N
    }

    public boolean isBadcore() {
	return IpeUtils.sameString("badcore", errorToken()); // NOI18N
    }

    public boolean isBadcoreOod() {
	return IpeUtils.sameString("badcore-timestamp", errorToken()); // NOI18N
    }

    public boolean isBadcoreNoprog() {
	return IpeUtils.sameString("badcore-noprog", errorToken()); // NOI18N
    }

    public boolean isRunFailed() {
	return IpeUtils.sameString("run-failed", errorToken()); // NOI18N
    }



    private void process() {
	if (processed)
	    return;

	StringBuffer buffer = new StringBuffer(errors.length*40);
	for (int ex = 0; ex < errors.length; ex++) {
	    Msg err = errors[ex];
	    if (err.severity.compareTo(maxSeverity) > 0) {
		maxSeverity = err.severity;
	    }
	    if (err.cookie != null) {
		errorToken = err.cookie;
	    }
	    if (ex > 0) {
		buffer.append(" / "); // NOI18N
	    }
	    buffer.append(err.msg);
	} 

	msg = buffer.toString();
	processed = true;
    }

    public final Severity maxSeverity() {
	process();
	return maxSeverity;
    }

    public final String errorToken() {
	process();
	return errorToken;
    }

    /**
     * Return the full error message separated by '/'s.
     * @return
     */
    public final String msg() {
	process();
	return msg;
    }

    /**
     * Return the full error message separated by '\n's.
     * @return
     */
    public final String text() {
	if (text == null) {
	    StringBuffer buffer = new StringBuffer();
	    for (Msg e : errors)
		buffer.append(e.msg + "\n");		// NOI18N
	    text = buffer.toString();
	}
	return text;
    }

    /**
     * Return the first error message or an empty String.
     * @return
     */
    public final String first() {
	if (errors.length > 0 && errors[0].msg != null) {
	    return errors[0].msg;
	} else {
	    return "";
	}
    }
}
