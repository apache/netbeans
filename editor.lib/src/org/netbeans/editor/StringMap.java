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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.editor;

import java.util.Map;
import java.util.HashMap;

/** Support for comparing part of char array
* to hash map with strings as keys.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class StringMap extends java.util.HashMap {

    char[] testChars;

    int testOffset;

    int testLen;

    static final long serialVersionUID =967608225972123714L;
    public StringMap() {
        super();
    }

    public StringMap(int initialCapacity) {
        super(initialCapacity);
    }

    public StringMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public StringMap(Map t) {
        super(t);
    }

    public Object get(char[] chars, int offset, int len) {
        testChars = chars;
        testOffset = offset;
        testLen = len;
        Object o = get(this);
        testChars = null; // enable possible GC
        return o;
    }

    public boolean containsKey(char[] chars, int offset, int len) {
        testChars = chars;
        testOffset = offset;
        testLen = len;
        boolean b = containsKey(this);
        testChars = null; // enable possible GC
        return b;
    }

    public Object remove(char[] chars, int offset, int len) {
        testChars = chars;
        testOffset = offset;
        testLen = len;
        Object o = remove(this);
        testChars = null;
        return o;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof String) {
            String s = (String)o;
            if (testLen == s.length()) {
                for (int i = testLen - 1; i >= 0; i--) {
                    if (testChars[testOffset + i] != s.charAt(i)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        if (o instanceof char[]) {
            char[] chars = (char[])o;
            if (testLen == chars.length) {
                for (int i = testLen - 1; i >= 0; i--) {
                    if (testChars[testOffset + i] != chars[i]) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        return false;
    }

    public int hashCode() {
        int h = 0;
        char[] chars = testChars;
        int off = testOffset;

        for (int i = testLen; i > 0; i--) {
            h = 31 * h + chars[off++];
        }

        return h;
    }

}
