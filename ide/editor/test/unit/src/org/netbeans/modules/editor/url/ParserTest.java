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
package org.netbeans.modules.editor.url;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jan Lahoda
 */
public class ParserTest {

    public ParserTest() {
    }

    /**
     * Test of recognizeURLs method, of class Parser.
     */
    @Test
    public void testRecognizeURLs() {
        performTest("    http://www.netbeans.org/\n", 4, 28);
        performTest("    http://www.netbeans.org/~s?d=3_\n", 4, 35);
        performTest("    http://www.test-test.test/\n", 4, 30);
        performTest("    http://www.test-test.test/a.jsp?tt\\&t$=$\n", 4, 44);
        performTest("    ftp://www.test-test.test/a.jsp?tt\\&t$=$\n", 4, 43);
        performTest("    f t p://www.test-test.test/a.jsp?tt\\&t$=$\n", null);
        performTest("    ftp://www.test-test.test/a.jsp?tt\\&t$=$", 4, 43);
        performTest("    \"http://www.netbeans.org/\"", 5, 29);
        performTest("    http://some.where/some-thing.html#section\n", 4, 45);
        performTest("    http://netbeans.org/bugzilla/buglist.cgi?bug_id=181772,172312\n", 4, 65);
        // stop at the apostrophe - see defect #187840
        performTest("    http://some.where/some-thing.html#section?cow=moo's\n", 4, 53);
        performTest("    h", null);
        // accept URLencoded apostrophe
        performTest("    http://some.where:1234/some-thing.html#section?cow=moo%27s\n", 4, 62);
        performTest("    https://www.netbeans.org/\n", 4, 29);
        performTest("    https://www.netbeans.org/~s?d=3_\n", 4, 36);
        performTest("    https://www.test-test.test/\n", 4, 31);
        performTest("    https://www.test-test.test/a.jsp?tt\\&t$=$\n", 4, 45);
        performTest("    ftps://www.test-test.test/a.jsp?tt\\&t$=$\n", 4, 44);
        performTest("    f t p://www.test-test.test/a.jsp?tt\\&t$=$\n", null);
        performTest("    ftps://www.test-test.test/a.jsp?tt\\&t$=$", 4, 44);
        performTest("    \"https://www.netbeans.org/\"", 5, 30);
        performTest("    https://some.where/some-thing.html#section\n", 4, 46);
        performTest("    https://netbeans.org/bugzilla/buglist.cgi?bug_id=181772,172312\n", 4, 66);
        performTest("    https://some.where/some-thing.html#section?cow=moos\n", 4, 55);
        performTest("    h", null);
        performTest("    https://some.where:1234/some-thing.html#section?cow=moos\n", 4, 60);
        performTest("    https://some.where:1234/some-thing.html#section?cow=moos\n", 4, 60);
        performTest("    httpss://some.where:1234/some-thing.html#section?cow=moo's\n", null);
        performTest("    httpps://some.where:1234/some-thing.html#section?cow=moo's\n", null);
        performTest("    https//some.where:1234/some-thing.html#section?cow=moo's\n", null);
        performTest("    http://hudson.gotdns.com/wiki/display/HUDSON/Structured+Form+Submission\n", 4, 75);
        performTest("    https://hudson.gotdns.com/wiki/display/HUDSON/Structured+Form+Submission\n", 4, 76);
        performTest("    ht+tp://hudson.gotdns.com/wiki/display/HUDSON/Structured+Form+Submission\n", null);
        performTest("    http://hudson.gotdns.com/wiki/display/HUDSON/Structured+Form+Submission(v=vs.85).aspx\n", 4,89);
        performTest("    http://hudson.gotdns.com/wiki/display/#!/api/dd13\n", 4,53);
        performTest("    file:///C:/CodeSourceryG++Lite/share/doc/arm-arm-none-eabi/html/getting-started/sec-cs3-startup.html", 4, 104);
        // NETBEANS-4593 accept @
        performTest("    https://cdn.jsdelivr.net/npm/jquery@3.2/dist/jquery.min.js\n", 4, 62);
        performTest("    nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java", 4, 63);
    }

    @Test
    public void testInvalidUrls() {
        performTest("    http::request\n", null);
    }

    @Test
    public void testContentType() {
        assertEquals("ISO-8859-2", Parser.decodeContentType("text/html; charset=ISO-8859-2"));
        assertEquals("ISO-8859-2", Parser.decodeContentType("text/html; charset=ISO-8859-2;"));
    }

//    @Test
    public void testPerformance() {
        StringBuilder sb = new StringBuilder();

        for (int cntr = 0; cntr < 100000; cntr++) {
            sb.append("test test test http://www.netbeans.org/index.html adsfasdf adsfadsfadf asdfadf\n");
        }

        long s = System.currentTimeMillis();

        for (int cntr = 0; cntr < 100; cntr++) {
            Parser.recognizeURLs(sb);
        }

        long handWritten = System.currentTimeMillis() - s;

        s = System.currentTimeMillis();

        for (int cntr = 0; cntr < 100; cntr++) {
            Parser.recognizeURLsREBased(sb);
        }

        long reBased = System.currentTimeMillis() - s;

        System.err.println("times=" + handWritten + " vs. " + reBased);
    }

    private void performTest(String code, int... spans) {
        List<List<Integer>> golden = new LinkedList<List<Integer>>();

        if (spans != null) {
            List<Integer> goldenSpans = new LinkedList<Integer>();

            for (int off : spans) {
                goldenSpans.add(off);
            }
            
            golden.add(goldenSpans);
        }

        assertEquals(golden, unpackArray(Parser.recognizeURLs(code)));
        assertEquals(golden, unpackArray(Parser.recognizeURLsREBased(code)));
    }
    
    private List<List<Integer>> unpackArray(Iterable<int[]> spans) {
        List<List<Integer>> r = new LinkedList<List<Integer>>();

        for (int[] span : spans) {
            r.add(Arrays.asList(span[0], span[1]));
        }

        return r;
    }
    
}
