/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
