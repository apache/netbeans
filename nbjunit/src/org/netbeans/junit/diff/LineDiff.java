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

package org.netbeans.junit.diff;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Line Diff with formated textual output.
 * Number of context lines in output is configurable through system property 'nbjunit.linediff.context'.
 *
 * @author  ehucka
 */
public class LineDiff implements Diff {
    
    public static int CONTEXT = 3;
    
    boolean ignoreCase;
    boolean ignoreEmptyLines;
    int contextLines;
    
    public LineDiff() {
        this(false, false);
    }
    
    public LineDiff(boolean ignoreCase) {
        this(ignoreCase, false);
    }
    
    public LineDiff(boolean ignoreCase, boolean ignoreEmptyLines) {
        this.ignoreCase = ignoreCase;
        this.ignoreEmptyLines = ignoreEmptyLines;
        //set number of context lines
        String value = System.getProperty("nbjunit.linediff.context");
        int number = -1;
        if (value != null) {
            try {
            number = Integer.parseInt(value);
            } catch (NumberFormatException ex) {ex.printStackTrace();}
        }
        if (number < 0) {
            number = CONTEXT;
        }
        contextLines = number;
    }
    
    public boolean getIgnoreCase() {
        return ignoreCase;
    }
    
    /**
     * @param l1 first line to compare
     * @param l2 second line to compare
     * @return true if lines equal
     */
    protected boolean compareLines(String l1,String l2) {
        if (getIgnoreCase()) {
            if (l1.equalsIgnoreCase(l2))
                return true;
        } else {
            if (l1.equals(l2))
                return true;
        }
        return false;
    }
    
    public int getNContextLines() {
        return contextLines;
    }

    /**
     * @param ref first file to compare
     * @param pass second file to compare
     * @param diff difference file, caller can pass null value, when results are not needed.
     * @return true iff files differ
     * @throws IOException when readin of files fails
     */
    public boolean diff(String ref, String pass, String diff) throws IOException {
        File fFirst = new File(ref);
        File fSecond = new File(pass);
        File fDiff = null != diff ? new File(diff) : null;
        return diff(fFirst, fSecond, fDiff);
    }
    
    /**
     * @param refFile first file to compare -- ref
     * @param passFile second file to compare -- golden
     * @param diffFile difference file, caller can pass null value, when results are not needed.
     * @return true iff files differ
     */
    public boolean diff(File refFile,File passFile,File diffFile) throws IOException {
        LineNumberReader first = new LineNumberReader(new FileReader(refFile));
        LineNumberReader second = new LineNumberReader(new FileReader(passFile));
        String line;
        
        String[] refLines, passLines;
        
        //read golden file
        List<String> tmp = new ArrayList<String>(64);
        while ((line = second.readLine()) != null) {
            if (ignoreEmptyLines && line.trim().length() == 0) {
                continue;
            }
            tmp.add(line);
        }
        passLines = tmp.toArray(new String[tmp.size()]);
        tmp.clear();
        second.close();
        //read ref file
        tmp = new ArrayList<String>(64);
        while ((line = first.readLine()) != null) {
            if (ignoreEmptyLines && line.trim().length() == 0) {
                continue;
            }
            tmp.add(line);
        }
        refLines = tmp.toArray(new String[tmp.size()]);
        tmp.clear();
        first.close();
        //collect differences
        List<Result> results = findDifferences(passLines, refLines);
        //without differences it can be finished here
        if (results.size() == 0) return false;
        if (diffFile == null) return results.size() > 0;
        //merge
        merge(results);
        //print
        printResults(passLines, refLines, results, diffFile);
        return results.size() > 0;
    }
    
    /**
     * compare right to left lines (pass to ref) and left to right lines
     *
     */
    private List<Result> findDifferences(String[] passLines, String[] refLines) {
        int stepLeft = 0;
        int stepRight = 0;
        boolean jump = false;
        //test is left, pass is right
        List<Result> results = new ArrayList<Result>(64);
        //start right
        boolean right = true;
        
        while (stepRight < passLines.length || stepLeft < refLines.length) {
            if (right) {
                if (stepRight >= passLines.length) {
                    if (stepLeft < refLines.length) {
                        results.add(new Result(stepLeft, refLines.length, stepRight, true));  //add new lines
                    }
                    break;
                }
                String v = passLines[stepRight];
                int found = find(v, refLines, stepLeft);
                if (found >= 0) {
                    if (found > stepLeft) {
                        if (!jump && found-stepLeft >= 2) { //could be wrong jump - try tp skip left
                            jump = true;
                            right=false;
                            continue;
                        } else {
                            results.add(new Result(stepLeft, found, stepRight, true));  //add new lines
                        }
                    }
                    stepLeft=found+1;
                } else {
                    results.add(new Result(stepRight, stepRight+1, false));  //add one missing
                    //switch to left
                    right=false;
                }
                stepRight++;
            } else {
                if (stepLeft >= refLines.length) {
                    if (stepRight < passLines.length) {
                        results.add(new Result(stepRight, passLines.length-1, false));  //add missing lines
                    }
                    break;
                }
                String v = refLines[stepLeft];
                int found = find(v, passLines, stepRight);
                if (found >= 0) {
                    if (!jump && found - stepRight >= 2) { //eliminate wrong jumps
                        jump = true;
                        right = true;
                        continue;
                    }
                    if (found > stepRight) {
                        results.add(new Result(stepRight, found, false));  //add missing lines
                    }
                    stepRight=found+1;
                    //switch to right
                    right=true;
                } else {
                    results.add(new Result(stepLeft, stepLeft+1, stepRight, true));  //add one new
                    right=true;
                }
                stepLeft++;
            }
            jump = false;
        }
        return results;
    }
    
    private void printResults(String[] passLines, String[] refLines, List<Result> results, File diffFile) throws IOException {
        int numLength = (refLines.length > passLines.length) ? String.valueOf(refLines.length).length() :
            String.valueOf(passLines.length).length();
        PrintStream ps = new PrintStream(new FileOutputStream(diffFile));
        boolean precontext=false;
        for (int i = 0; i < results.size(); i++) {
            Result rs = results.get(i);
            if (!precontext) {
                int si = rs.passIndex-contextLines;
                if (si < 0) si = 0;
                for (int j=si;j < rs.passIndex;j++) {
                    printContext(passLines, ps, j, numLength);
                }
            } else {
                precontext=false;
            }
            results.get(i).print(passLines, refLines, ps, numLength);
            int e1 = (rs.newLine) ? rs.passIndex : rs.end;
            int e2 = e1+contextLines;
            if (i < results.size()-1 && results.get(i+1).passIndex < e2) {
                e2 = results.get(i+1).passIndex;
                precontext=true;
            } else if (e2 > passLines.length) {
                e2=passLines.length;
            }
            for (int j=e1;j < e2;j++) {
                printContext(passLines, ps, j, numLength);
            }
        }
        ps.close();
    }
    
    private int find(String value, String[] lines, int startIndex) {
        for (int i  = startIndex;i < lines.length;i++) {
            if (compareLines(value, lines[i])) {
                return i;
            }
        }
        return -1;
    }
    
    private void merge(List<Result> results) {
        for (int i  = 0;i < results.size()-1;i++) {
            if (results.get(i).newLine && results.get(i+1).newLine &&
                    results.get(i).end == results.get(i+1).start) {
                results.get(i).end = results.get(i+1).end;
                results.remove(i+1);
                i--;
            }
        }
    }
    
    private void printContext(String[] passLines, PrintStream ps, int lineNumber, int numLength) {
        String num=String.valueOf(lineNumber+1);
        int rem=numLength+1-num.length();
        for (int j=0;j < rem;j++)
            ps.print(' ');
        ps.print(num);
        ps.print("   ");
        ps.println(passLines[lineNumber]);
    }
    
    static class Result {
        
        boolean newLine = false;
        int start, end;
        int passIndex;
        
        public Result(int start, int end, int passIndex, boolean newLine) {
            this.start = start;
            this.end = end;
            this.passIndex = passIndex;
            this.newLine = newLine;
        }
        
        public Result(int start, int end, boolean newLine) {
            this.passIndex = start;
            this.start = start;
            this.end = end;
            this.newLine = newLine;
        }
        
        public void print(String[] passLines, String[] refLines, PrintStream ps, int numLength) {
            for  (int i=start;i < end;i++) {
                if (newLine) {
                    for (int j=0;j < numLength+2;j++)
                        ps.print(' ');
                    ps.print("+ ");
                    ps.println(refLines[i]);
                } else {
                    String num=String.valueOf(i+1);
                    int rem=numLength+1-num.length();
                    for (int j=0;j < rem;j++)
                        ps.print(' ');
                    ps.print(num);
                    ps.print(" - ");
                    ps.println(passLines[i]);
                }
            }
        }
    }
}
