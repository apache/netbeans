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

package org.netbeans.modules.schema2beansdev.gen;

import java.util.*;
import java.io.*;

public class IndentingWriter extends GenBuffer {
    protected boolean crDone[];
    protected int indentLevel[];
    protected String indentString = "\t";

    public IndentingWriter(int bufferCount) {
        super(bufferCount);
        crDone = new boolean[bufferCount];
        indentLevel = new int[bufferCount];
        privateInit();
    }

    public IndentingWriter(IndentingWriter source) {
        super(source);
        indentString = source.indentString;
        crDone = new boolean[bufferCount];
        indentLevel = new int[bufferCount];
        for (int i = 0; i < bufferCount; i++) {
            crDone[i] = source.crDone[i];
            indentLevel[i] = source.indentLevel[i];
        }
    }

    public void reset() {
        super.reset();
        privateInit();
    }

    private void privateInit() {
        for (int i = 0; i < bufferCount; i++) {
            crDone[i] = true;
            indentLevel[i] = 0;
        }
    }

    public void writeTo(GenBuffer o) {
        super.writeTo(o);
        if (o instanceof IndentingWriter) {
            IndentingWriter out = (IndentingWriter) o;
            int minInCommonBufferCount = bufferCount;
            if (out.bufferCount < bufferCount)
                minInCommonBufferCount = out.bufferCount;
            for (int i = 0; i < minInCommonBufferCount; i++) {
                out.crDone[i] = crDone[i];
                out.indentLevel[i] = indentLevel[i];
            }
        }
    }
    
    /**
     * Insert some additional buffers.
     * Previous buffers are not adjusted automatically.
     * select() should be called afterwards to reestablish current buffer.
     */
    public void insertAdditionalBuffers(int offset, int count) {
        boolean[] newCrDone = new boolean[bufferCount + count];
        // copy before and including offset
        System.arraycopy(crDone, 0, newCrDone, 0, offset+1);
        // copy after offset
        System.arraycopy(crDone, offset+1, newCrDone,
                         offset + 1 + count, bufferCount - offset - 1);
        // init the new elements
        for (int i = 0; i < count; ++i) {
            newCrDone[offset + 1 + i] = true;
        }
        crDone = newCrDone;

        int[] newIndentLevel = new int[bufferCount + count];
        // copy before and including offset
        System.arraycopy(indentLevel, 0, newIndentLevel, 0, offset+1);
        // copy after offset
        System.arraycopy(indentLevel, offset+1, newIndentLevel,
                         offset + 1 + count, bufferCount - offset - 1);
        // init the new elements
        for (int i = 0; i < count; ++i) {
            newIndentLevel[offset + 1 + i] = 0;
        }
        indentLevel = newIndentLevel;

        super.insertAdditionalBuffers(offset, count);
    }

    public void setIndent(String indent) {
        this.indentString = indent;
    }

    public String getIndent() {
        return indentString;
    }

    public void cr() throws IOException {
        listOut[curOut].append("\n");
        crDone[curOut] = true; 
    }

    public void write(String str) throws IOException {
        int len = str.length();
        if (len == 0)
            return;
        char lastChar = str.charAt(len-1);
        if (lastChar == '\n') {
            char firstChar = str.charAt(0);
            char secondLastChar = (len <= 1) ? ' ' : str.charAt(len-2);
            if (firstChar == '}' || secondLastChar == '}') {
                indentLeft();
            }
            super.write(str.substring(0, len-1));
            cr();
            if (secondLastChar == '{') {
                indentRight();
            }
        } else {
            super.write(str);
        }
    }

    public void writecr(String str) throws IOException {
        super.write(str);
        cr();
    }

    public void writecr(String s1, String s2) throws IOException {
        super.write(s1, s2);
        cr();
    }

    public void writecr(String s1, String s2, String s3) throws IOException {
        super.write(s1, s2, s3);
        cr();
    }

    public void writecr(String s1, String s2, String s3, String s4) throws IOException {
        super.write(s1, s2, s3, s4);
        cr();
    }

    public void indentRight() {
        ++indentLevel[curOut];
    }

    public void indentLeft() {
        --indentLevel[curOut];
    }

    protected void beforeWriteHook() {
        if (crDone[curOut]) {
            indent();
            crDone[curOut] = false;
        }
    }

    /**
     * Adds the indentString to the current buffer.
     */
    public void indentOneLevel() {
        listOut[curOut].append(indentString);
    }

    /**
     * Adds indentLevel[curOut] number of indentString's to the current
     * buffer.  Put another way, this will indent from the left margin to
     * the current indention level.
     */
    public void indent() {
        // This must not call a write as beforeWriteHook calls us
        for (int i = 0; i < indentLevel[curOut]; ++i)
            indentOneLevel();
    }
}
