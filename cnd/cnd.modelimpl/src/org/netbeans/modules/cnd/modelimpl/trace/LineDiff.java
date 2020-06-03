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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Line Diff with formated textual output.
 * Number of context lines in output is configurable through system property 'nbjunit.linediff.context'.
 * based on org.netbeans.junit.diff.LineDiff, but extended with comparison of List of lines
 */
public final class LineDiff {
    
    public static final int CONTEXT = 3;
    
    private final boolean ignoreCase;
    private final boolean ignoreEmptyLines;
    private final int contextLines;

    public static List<String> diff(List<? extends CharSequence> first, List<? extends CharSequence> second) {
        LineDiff diff = new LineDiff(false);
        return diff.diffLines(first, second);
    }

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
            } catch (NumberFormatException ex) {ex.printStackTrace(System.err);}
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
    protected boolean compareLines(CharSequence l1,CharSequence l2) {
        if (getIgnoreCase()) {
            if (l1.toString().equalsIgnoreCase(l2.toString()))
                return true;
        } else {
            if (l1.toString().contentEquals(l2))
                return true;
        }
        return false;
    }
    
    public int getNContextLines() {
        return contextLines;
    }

    public List<String> diffLines(List<? extends CharSequence> ref, List<? extends CharSequence> pass) {
        CharSequence[] passLines = pass.toArray(new CharSequence[pass.size()]);
        CharSequence[] refLines = ref.toArray(new CharSequence[ref.size()]);
        //collect differences
        List<Result> results = findDifferences(passLines, refLines);
        //without differences it can be finished here
        if (results.isEmpty()) {
            return Collections.emptyList();
        }
        //merge
        merge(results);
        //get print variant
        try {
            File tmpDiffFile = File.createTempFile("cnd_diff_lines", ".diff");// NOI18N
            printResults(passLines, refLines, results, tmpDiffFile);
            FileObject fo = FileUtil.toFileObject(tmpDiffFile);
            List<String> asLines = new ArrayList<>(fo.asLines());
            try {
                tmpDiffFile.delete();
            } catch (Exception e) {
            }
            return asLines;
        } catch (IOException e) {
            return Collections.singletonList("ERROR ON DIFF LINES " + e.getLocalizedMessage());// NOI18N
        }
    }

    /**
     * @param ref first file to compare
     * @param pass second file to compare
     * @param diff difference file, caller can pass null value, when results are not needed.
     * @return true iff files differ
     * @throws IOException when readin of files fails
     */
    public boolean diffFiles(String ref, String pass, String diff) throws IOException {
        File fFirst = new File(ref);
        File fSecond = new File(pass);
        File fDiff = null != diff ? new File(diff) : null;
        return diffFiles(fFirst, fSecond, fDiff);
    }
    
    /**
     * @param refFile first file to compare -- ref
     * @param passFile second file to compare -- golden
     * @param diffFile difference file, caller can pass null value, when results are not needed.
     * @return true iff files differ
     */
    public boolean diffFiles(File refFile,File passFile,File diffFile) throws IOException {
        LineNumberReader first = new LineNumberReader(new FileReader(refFile));
        LineNumberReader second = new LineNumberReader(new FileReader(passFile));
        String line;
        
        String[] refLines, passLines;
        
        //read golden file
        List<String> tmp = new ArrayList<>(64);
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
        tmp = new ArrayList<>(64);
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
        if (results.isEmpty()) return false;
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
    private List<Result> findDifferences(CharSequence[] passLines, CharSequence[] refLines) {
        int stepLeft = 0;
        int stepRight = 0;
        boolean jump = false;
        //test is left, pass is right
        List<Result> results = new ArrayList<>(64);
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
                CharSequence v = passLines[stepRight];
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
                CharSequence v = refLines[stepLeft];
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
    
    private void printResults(CharSequence[] passLines, CharSequence[] refLines, List<Result> results, File diffFile) throws IOException {
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
    
    private int find(CharSequence value, CharSequence[] lines, int startIndex) {
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
    
    private void printContext(CharSequence[] passLines, PrintStream ps, int lineNumber, int numLength) {
        String num=String.valueOf(lineNumber+1);
        int rem=numLength+1-num.length();
        for (int j=0;j < rem;j++)
            ps.print(' ');// NOI18N
        ps.print(num);
        ps.print("   ");// NOI18N
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
        
        public void print(CharSequence[] passLines, CharSequence[] refLines, PrintStream ps, int numLength) {
            for  (int i=start;i < end;i++) {
                if (newLine) {
                    for (int j=0;j < numLength+2;j++)
                        ps.print(' ');// NOI18N
                    ps.print("+ ");// NOI18N
                    ps.println(refLines[i]);
                } else {
                    String num=String.valueOf(i+1);
                    int rem=numLength+1-num.length();
                    for (int j=0;j < rem;j++)
                        ps.print(' ');// NOI18N
                    ps.print(num);
                    ps.print(" - ");// NOI18N
                    ps.println(passLines[i]);
                }
            }
        }
    }
}
