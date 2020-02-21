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

package org.netbeans.modules.cnd.makefile.utils;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *  A Unix Regular Expression is one using the standard Unix shell syntax. Its
 *  less powerfull than the full Posix RE but the expected behavior for file
 *  filters.
 */
public class UnixRE {

    /** the UnixRE is stored as a regexp RE */
    Pattern re;

    /** Save a copy of the original pattern */
    boolean starPattern;

    boolean debugRE = false;

    public UnixRE(String pattern) throws PatternSyntaxException {
	StringBuffer unixText = new StringBuffer(256);
	char prev = 0;

	if (Boolean.getBoolean("ifdef.debug.unixre") &&	// NOI18N
			pattern.startsWith("[[[")) {	// NOI18N
	    // OLD re = new RE(pattern.substring(3));
	    re = Pattern.compile(pattern.substring(3));
	    return;
	}

	if (pattern.charAt(0) == '*') {
	    starPattern = true;
	} else {
	    starPattern = false;
	}

	// TODO: Escape all regexp magic chars that UnixRE doesn't want glob'ed
	unixText.append('^');
	for (int i = 0; i < pattern.length(); i++) {
	    char c = pattern.charAt(i);

	    if (c == '*' && prev != '\\') {
		unixText.append(".*");					// NOI18N
	    } else if (c == '?' && prev != '\\') {
		unixText.append(".{1}");				// NOI18N
	    } else if (c == '.' && prev != '\\') {
		unixText.append("\\.");					// NOI18N
	    } else {
		unixText.append(c);
	    }
	    prev = c;
	}
	unixText.append('$');

	// OLD re = new RE(unixText.toString());
	re = Pattern.compile(unixText.toString());
    }

    public boolean match(String s) {

	if (starPattern && s.charAt(0) == '.') {
	    return false;
	} else {
	    return re.matcher(s).find();
	}
    }

    /**
     *  Tells if the string is a Unix regular expression.
     */
    static public boolean isUnixRE(String s) {
	char	prev = 0;			// previous character
	char	c;				// current character

	for (int i = 0; i < s.length(); i++) {
	    c = s.charAt(i);
	    if ((c == '*' || c == '?' || c == '[' || c == ']')
				&& prev != '\\') {
		return true;
	    }
	    prev = c;
	}

	return false;
    }
}

