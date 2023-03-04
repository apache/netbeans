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
 * LineDiff.java
 *
 * Created on March 28, 2002, 9:49 AM
 */

package org.netbeans.test.java.editor.lib;

import org.netbeans.junit.diff.Diff;
import java.io.File;
import java.io.FileOutputStream;
import java.io.LineNumberReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author  jlahoda, ehucka
 */
public class LineDiff implements Diff {

    private boolean ignoreCase;

    /** Creates a new instance of LineDiff */
    public LineDiff(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }
    
    public boolean getIgnoreCase() {
        return ignoreCase;
    }
    
    /**
     * @param l1 first line to compare
     * @param l2 second line to compare
     * @return true if lines equal
     */
    private boolean compareLines(String l1,String l2) {
        if (getIgnoreCase()) {
            if (l1.equalsIgnoreCase(l2))
                return true;
        } else {
            if (l1.equals(l2))
                return true;
        }
        return false;
    }
    
    /**
     * @param first first file to compare
     * @param second second file to compare
     * @param diff difference file, caller can pass null value, when results are not needed.
     * @return true iff files differ
     */
    public boolean diff(String first, String second, String diff) throws java.io.IOException {
        File fFirst = new File(first);
        File fSecond = new File(second);
        File fDiff = null != diff ? new File(diff) : null;
        return diff(fFirst, fSecond, fDiff);
    }
    
    /**
     * @param first first file to compare
     * @param second second file to compare
     * @param diff difference file, caller can pass null value, when results are not needed.
     * @return true iff files differ
     */
    public boolean diff(java.io.File firstFile, java.io.File secondFile, java.io.File diffFile) throws java.io.IOException {
        LineNumberReader first = new LineNumberReader(new FileReader(firstFile));
        LineNumberReader second = new LineNumberReader(new FileReader(secondFile));
        String firstLine;
        String secondLine;
        
        if (diffFile == null) {
            while ((firstLine = first.readLine()) != null) {
                secondLine = second.readLine();
                if (secondLine == null) {
                    first.close();
                    second.close();
                    return true;
                }
                if (!compareLines(firstLine,secondLine)) {
                    first.close();
                    second.close();
                    return true;
                }
            }
        } else {
            ArrayList a1,a2,newLines,missingLines;
            
            a1=new ArrayList();
            while ((firstLine = first.readLine()) != null) {
                a1.add(firstLine);
            }
            a2=new ArrayList();
            while ((secondLine = second.readLine()) != null) {
                a2.add(secondLine);
            }
            first.close();
            second.close();
            newLines=new ArrayList();
            missingLines=new ArrayList();
            
            int j=0,bj;
            boolean found;
            
            for (int i=0;i < a1.size();i++) {
                if (j >= a2.size()) {
                    for (int k=i;k < a1.size();k++) {
                        missingLines.add(k+"> "+a1.get(k));
                    }
                    break;
                }
                firstLine=(String)(a1.get(i));
                secondLine=(String)(a2.get(j));
                if (!compareLines(firstLine,secondLine)) {
                    found=false;
                    for (int k=j;k < a2.size();k++) {
                        secondLine = (String)(a2.get(k));
                        if (compareLines(firstLine,secondLine)) {
                            for (int l=j;l < k;l++) {
                                newLines.add(l+"> "+a2.get(l));
                            }
                            j=k;
                            found=true;
                            break;
                        }
                    }
                    if (!found) {
                        missingLines.add(i+"> "+firstLine);
                        j--;
                    }
                }
                j++;
            }
            if (j < a2.size()) {
                for (int i=j;i < a2.size();i++) {
                    newLines.add(i+"> "+a2.get(i));
                }
            }
            
            if (missingLines.size() > 0 || newLines.size() > 0) {
                PrintStream pw=null;
                pw=new PrintStream(new FileOutputStream(diffFile));
                //pw=System.out;
                if (missingLines.size() > 0) {
                    pw.println("-----------------------------Missing Lines:-----");
                    for (int i=0;i < missingLines.size();i++) {
                        pw.println(missingLines.get(i));
                    }
                }
                if (newLines.size() > 0) {
                    pw.println("-----------------------------New Lines:---------");
                    for (int i=0;i < newLines.size();i++) {
                        pw.println(newLines.get(i));
                    }
                }
                pw.close();
                return true;
            }
        }
        return false;
    }
    
    public static void main(String[] argv) {
        try {
            LineDiff diff=new LineDiff(true);
            diff.diff("/tmp/diff/test.pass","/tmp/diff/test.ref","/tmp/diff/test.diff");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
