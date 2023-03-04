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
package org.netbeans.modules.diff.builtin.visualizer;

import org.netbeans.api.diff.Difference;
import org.netbeans.modules.diff.builtin.Hunk;

import java.io.*;

/**
 * Unified diff engine.
 * 
 * @author Maros Sandor
 */
final class UnifiedDiff {
    
    private final TextDiffVisualizer.TextDiffInfo diffInfo;
    private BufferedReader baseReader; 
    private BufferedReader modifiedReader;
    private final String newline;
    private boolean baseEndsWithNewline;
    private boolean modifiedEndsWithNewline;
    
    private int   currentBaseLine;
    private int   currentModifiedLine;

    public UnifiedDiff(TextDiffVisualizer.TextDiffInfo diffInfo) {
        this.diffInfo = diffInfo;
        currentBaseLine = 1;
        currentModifiedLine = 1;
        this.newline = System.getProperty("line.separator");
    }
    
    public String computeDiff() throws IOException {
        baseReader = checkEndingNewline(diffInfo.createFirstReader(), true);
        modifiedReader = checkEndingNewline(diffInfo.createSecondReader(), false);
        
        StringBuilder buffer = new StringBuilder();
        buffer.append("--- ");
        buffer.append(diffInfo.getName1());
        buffer.append(newline);
        buffer.append("+++ ");
        buffer.append(diffInfo.getName2());
        buffer.append(newline);
        
        Difference[] diffs = diffInfo.getDifferences();
        
        for (int currentDifference = 0; currentDifference < diffs.length; ) {
            // the new hunk will span differences from currentDifference to lastDifference (exclusively)
            int lastDifference = getLastIndex(currentDifference);
            Hunk hunk = computeHunk(currentDifference, lastDifference);
            dumpHunk(buffer, hunk);
            currentDifference = lastDifference;
        }
        
        return buffer.toString();
    }
    
    private BufferedReader checkEndingNewline(Reader reader, boolean isBase) throws IOException {
        StringWriter sw = new StringWriter();
        copyStreamsCloseAll(sw, reader);
        String s = sw.toString();
        char endingChar = s.length() == 0 ? 0 : s.charAt(s.length() - 1);
        if (isBase) {
            baseEndsWithNewline = endingChar == '\n' || endingChar == '\r';
        } else {
            modifiedEndsWithNewline = endingChar == '\n' || endingChar == '\r';
        }
        return new BufferedReader(new StringReader(s));
    }

    private Hunk computeHunk(int firstDifference, int lastDifference) throws IOException {

        Hunk hunk = new Hunk();
        
        Difference firstDiff = diffInfo.getDifferences()[firstDifference];
        int contextLines = diffInfo.getContextNumLines();

        int skipLines;
        if (firstDiff.getType() == Difference.ADD) {
            if (contextLines > firstDiff.getFirstStart()) {
                contextLines = firstDiff.getFirstStart();
            }
            skipLines = firstDiff.getFirstStart() - contextLines - currentBaseLine + 1;
        } else {
            if (contextLines >= firstDiff.getFirstStart()) {
                contextLines = firstDiff.getFirstStart() - 1;
            }
            skipLines = firstDiff.getFirstStart() - contextLines - currentBaseLine;
        }
        
        // move file pointers to the beginning of hunk
        while (skipLines-- > 0) {
            readLine(baseReader);
            readLine(modifiedReader);
        }
        
        hunk.baseStart = currentBaseLine;
        hunk.modifiedStart = currentModifiedLine;
        
        // output differences with possible contextual lines in-between
        for (int i = firstDifference; i < lastDifference; i++) {
            Difference diff = diffInfo.getDifferences()[i];
            writeContextLines(hunk, diff.getFirstStart() - currentBaseLine + ((diff.getType() == Difference.ADD) ? 1 : 0));
            
            if (diff.getFirstEnd() > 0) {
                int n = diff.getFirstEnd() - diff.getFirstStart() + 1;
                outputLines(hunk, baseReader, "-", n);
                hunk.baseCount += n;
                if (!baseEndsWithNewline && i == diffInfo.getDifferences().length - 1 && diff.getFirstEnd() == currentBaseLine - 1) {
                    hunk.lines.add(Hunk.ENDING_NEWLINE);
                }
            }
            if (diff.getSecondEnd() > 0) {
                int n = diff.getSecondEnd() - diff.getSecondStart() + 1;
                outputLines(hunk, modifiedReader, "+", n);
                hunk.modifiedCount += n;
                if (!modifiedEndsWithNewline && i == diffInfo.getDifferences().length - 1 && diff.getSecondEnd() == currentModifiedLine - 1) {
                    hunk.lines.add(Hunk.ENDING_NEWLINE);
                }
            }
        }
        
        // output bottom context lines 
        writeContextLines(hunk, diffInfo.getContextNumLines());
        
        if (hunk.modifiedCount == 0) hunk.modifiedStart = 0;    // empty file
        if (hunk.baseCount == 0) hunk.baseStart = 0;    // empty file
        return hunk;
    }
    
    private void writeContextLines(Hunk hunk, int count) throws IOException {
        while (count-- > 0) {
            String line = readLine(baseReader);
            if (line == null) return;
            hunk.lines.add(" " + line);
            readLine(modifiedReader);  // move the modified file pointer as well
            hunk.baseCount++;
            hunk.modifiedCount++;
        }
    }

    private String readLine(BufferedReader reader) throws IOException {
        String s = reader.readLine();
        if (s != null) {
            if (reader == baseReader) currentBaseLine++;
            if (reader == modifiedReader) currentModifiedLine++;
        }
        return s;
    }

    private void outputLines(Hunk hunk, BufferedReader reader, String mode, int n) throws IOException {
        while (n-- > 0) {
            String line = readLine(reader);
            hunk.lines.add(mode + line);
        }
    }

    private int getLastIndex(int firstIndex) {
        int contextLines = diffInfo.getContextNumLines() * 2;
        Difference [] diffs = diffInfo.getDifferences();
        for (++firstIndex; firstIndex < diffs.length; firstIndex++) {
            Difference prevDiff = diffs[firstIndex - 1]; 
            Difference currentDiff = diffs[firstIndex];
            int prevEnd = 1 + ((prevDiff.getType() == Difference.ADD) ? prevDiff.getFirstStart() : prevDiff.getFirstEnd());
            int curStart = (currentDiff.getType() == Difference.ADD) ? (currentDiff.getFirstStart() + 1) : currentDiff.getFirstStart();
            if (curStart - prevEnd > contextLines) {
                break;
            }
        }
        return firstIndex;
    }
    
    private void dumpHunk(StringBuilder buffer, Hunk hunk) {
        buffer.append("@@ -");
        buffer.append(Integer.toString(hunk.baseStart));
        if (hunk.baseCount != 1) {
            buffer.append(",");
            buffer.append(Integer.toString(hunk.baseCount));
        }
        buffer.append(" +");
        buffer.append(Integer.toString(hunk.modifiedStart));
        if (hunk.modifiedCount != 1) {
            buffer.append(",");
            buffer.append(Integer.toString(hunk.modifiedCount));
        }
        buffer.append(" @@");
        buffer.append(newline);
        for (String line : hunk.lines) {
            buffer.append(line);
            buffer.append(newline);
        }
    }

    private static void copyStreamsCloseAll(Writer writer, Reader reader) throws IOException {
        char [] buffer = new char[4096];
        int n;
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }
        writer.close();
        reader.close();
    }
}
