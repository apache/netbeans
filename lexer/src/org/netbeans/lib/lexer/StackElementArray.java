/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.lib.lexer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Array of stack trace elements may be used for logging of unique stack traces only
 * (for frequent problematic code invocations).
 *
 * @author Miloslav Metelka
 */
public class StackElementArray {

    public static Set<StackElementArray> createSet() {
        return Collections.synchronizedSet(new HashSet<StackElementArray>());
    }

    public static boolean addStackIfNew(Set<StackElementArray> stacks, int stackCompareSize) {
        StackTraceElement[] stackElems = new Exception().getStackTrace();
        int startIndex = 2; // For faster comparison cut of first two (this method and the caller's place)
        int endIndex = Math.min(stackElems.length, startIndex + stackCompareSize);
        StackTraceElement[] compareElems = new StackTraceElement[endIndex - startIndex];
        System.arraycopy(stackElems, startIndex, compareElems, 0, endIndex - startIndex);
        StackElementArray stackElementArray = new StackElementArray(compareElems);
        if (!stacks.contains(stackElementArray)) {
            stacks.add(stackElementArray);
            return true;
        }
        return false;
    }

    private final StackTraceElement[] stackTrace;

    private final int hashCode;

    private StackElementArray(StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace;
        int hc = 0;
        for (int i = 0; i < stackTrace.length; i++) {
            hc ^= stackTrace[i].hashCode();
        }
        hashCode = hc;
    }

    int length() {
        return stackTrace.length;
    }

    StackTraceElement element(int i) {
        return stackTrace[i];
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof StackElementArray)) {
            return false;
        }
        StackElementArray sea = (StackElementArray) obj;
        if (sea.length() != length()) {
            return false;
        }
        for (int i = 0; i < stackTrace.length; i++) {
            if (!element(i).equals(sea.element(i))) {
                return false;
            }
        }
        return true;
    }

}
