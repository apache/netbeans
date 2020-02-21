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

