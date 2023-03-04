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

import junit.framework.TestCase;

import java.io.*;

import org.netbeans.api.diff.Difference;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider;

/**
 * Takes all files from a directory and compares them all with each other comparing results with external diff engine. 
 *
 * @author Maros Sandor
 */
public class UnifiedDiffTest extends NbTestCase {
    
    private File    dataRootDir;
    private File[]  testFiles;
    private int     idx0;
    private int     idx1;
    private BuiltInDiffProvider diffProvider;

    public UnifiedDiffTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        //data.root.dir defined in project.properties
        dataRootDir = getDataDir();
        diffProvider = new BuiltInDiffProvider();
        initPermutations();
    }

    private void initPermutations() {
        testFiles = new File(dataRootDir, "unidiff").listFiles();
        idx0 = 0;
        idx1 = 0;
    }

    private File[] getNextPermutation() {
        if (++idx1 == idx0) idx1++;
        if (idx1 >= testFiles.length) {
            if (++idx0 >= testFiles.length) return null;
            idx1 = 0;
        }
        return new File [] { testFiles[idx0], testFiles[idx1] };
    }
    
    // TODO stabilize sometimes.
    @RandomlyFails
    public void testUnifiedDiff() throws Exception {
        for (;;) {
            File [] toDiff = getNextPermutation();
            if (toDiff == null) break;
            System.out.println("Testing: " + toDiff[0].getName() + " <-> " + toDiff[1].getName());
            String internalDiff = getInternalDiff(toDiff);
            String externalDiff = getExternalDiff(toDiff);
            if (!diffsEqual(internalDiff, externalDiff)) {
                saveFailure(externalDiff, internalDiff);
                fail("Diff failed: " + toDiff[0].getName() + " <-> " + toDiff[1].getName());
            }
        }
    }

    private void saveFailure(String externalDiff, String internalDiff) throws IOException {
        try {
            copyStreamsCloseAll(new FileWriter("F:/diff-unit-failure-external.txt"), new StringReader(externalDiff));
            copyStreamsCloseAll(new FileWriter("F:/diff-unit-failure-internal.txt"), new StringReader(internalDiff));
        } catch (Exception e) {
            // ignore save errors
        }
    }

    private boolean diffsEqual(String sa, String sb) throws IOException {
        // ignore header
        int idxa = sa.indexOf("@@");
        int idxb = sb.indexOf("@@");
        if (idxa == -1 && idxb == -1) return true;
        if (idxa == -1 || idxb == -1) return false;
        sa = sa.substring(idxa);
        sb = sb.substring(idxb);
        return sa.equals(sb);
    }

    private String getInternalDiff(File[] toDiff) throws IOException {
        Reader r1 = new FileReader(toDiff[0]);
        Reader r2 = new FileReader(toDiff[1]);
        Difference [] diffs = diffProvider.computeDiff(r1, r2);
        r1.close();
        r2.close();
        r1 = new FileReader(toDiff[0]);
        r2 = new FileReader(toDiff[1]);
        
        TextDiffVisualizer.TextDiffInfo diffInfo = new TextDiffVisualizer.TextDiffInfo(
                "name1", "name2", "title1", "title2", r1, r2, diffs);
        diffInfo.setContextMode(true, 3);
        return TextDiffVisualizer.differenceToUnifiedDiffText(diffInfo);
    }

    private String getExternalDiff(File[] toDiff) throws IOException {
        Process p = Runtime.getRuntime().exec(new String [] { "diff", "-u", toDiff[0].getAbsolutePath(), toDiff[1].getAbsolutePath() });
        InputStreamReader isr = new InputStreamReader(p.getInputStream());
        StringWriter sw = new StringWriter();
        copyStreamsCloseAll(sw, isr);
        return sw.toString();
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
