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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.api.extexecution.input;

import org.netbeans.api.extexecution.input.InputProcessor;

/**
 *
 * This class is <i>NotThreadSafe</i>.
 * @author Petr Hejl
 */
public class TestInputProcessor implements InputProcessor {

    private final boolean cleanBytesOnReset;

    private StringBuilder charsProcessed = new StringBuilder();

    private int resetCount = 0;

    private boolean closed;

    public TestInputProcessor(boolean cleanBytesOnReset) {
        this.cleanBytesOnReset = cleanBytesOnReset;
    }

    public void processInput(char[] chars) {
        charsProcessed.append(chars);
    }

    public void reset() {
        resetCount++;
        if (cleanBytesOnReset) {
            charsProcessed.setLength(0);
        }
    }

    public void close() {
        closed = true;
    }

    public char[] getCharsProcessed() {
        return charsProcessed.toString().toCharArray();
    }

    public int getResetCount() {
        return resetCount;
    }

    public boolean isClosed() {
        return closed;
    }

}
