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

/*
 * SimpleDiff.java
 *
 * Created on February 8, 2001, 2:59 PM
 */

package org.netbeans.junit.diff;

import java.io.*;

/**
 *
 * @author  vstejskal, Jan Becicka
 * @version 2.0
 */
public class SimpleDiff extends Object implements Diff {

    private static final int BUFSIZE = 1024;
    private final LineDiff lineDiff;

    /** Creates new SimpleDiff */
    public SimpleDiff() {
        lineDiff = new LineDiff(false);
    }

    /**
     * @param first first file to compare
     * @param second second file to compare
     * @param diff difference file
     * @return true iff files differ
     */
    public boolean diff(final java.io.File first, final java.io.File second, java.io.File diff)  throws java.io.IOException {
        if (isBinaryFile(first) || isBinaryFile(second)) {
            return binaryCompare(first, second, diff);
        }
        else {
            return textualCompare(first, second, diff);
        }
    }
    
    /**
     * @param first first file to compare
     * @param second second file to compare
     * @param diff difference file
     * @return true iff files differ
     */
    public boolean diff(final String first, final String second, String diff)  throws java.io.IOException {
        File fFirst = new File(first);
        File fSecond = new File(second);
        File fDiff = null != diff ? new File(diff) : null;
        return diff(fFirst, fSecond, fDiff);
    }
    
    protected boolean isBinaryFile(File file) throws java.io.IOException {
        byte[] buffer = new byte[BUFSIZE];
        FileInputStream is = new FileInputStream(file);
        try {
            int bytesRead = is.read(buffer, 0, BUFSIZE);
            if (bytesRead == -1)
                return false;
            for (int i = 0; i != bytesRead; i++) {
                if (buffer[i] < 0)
                    return true;
            }
            return false;
        } finally {
            is.close();
        }
    }
    
    protected boolean binaryCompare(final File first, final File second, File diff) throws java.io.IOException {
        if (first.length() != second.length())
            return true;
        
        InputStream fs1 = new BufferedInputStream(new FileInputStream(first));
        InputStream fs2 = new BufferedInputStream(new FileInputStream(second));
        byte[] b1 = new byte[BUFSIZE];
        byte[] b2 = new byte[BUFSIZE];

        try {
            while (true) {
                int l1 = fs1.read(b1);
                int l2 = fs2.read(b2);
                if (l1 == -1) {
                    // files differ in length if l2 != -1; otherwise are equal
                    return l2 != -1;
                }
                if (l1 != l2)
                    return true;    // files differ in length
                if (!java.util.Arrays.equals(b1,b2))
                    return true;  // files differ in content
            }
        } finally {
            fs1.close();
            fs2.close();
        }
    }
    
    protected boolean textualCompare(final File first, final File second, File diff) throws java.io.IOException {
        return lineDiff.diff(first, second, diff);
    }
}
