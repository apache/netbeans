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

package org.netbeans.api.extexecution.print;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.extexecution.open.HttpOpenHandler;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;

/**
 *
 * @author Petr Hejl
 */
public class LineConvertorsTest extends NbTestCase {

    public LineConvertorsTest(String name) {
        super(name);
    }

    public void testProxy() {
        LineConvertor convertor1 = new LineConvertor() {
            public List<ConvertedLine> convert(String line) {
                if ("line1".equals(line)) {
                    return Collections.<ConvertedLine>singletonList(
                            ConvertedLine.forText("converted:" + line, null));
                }
                return null;
            }
        };
        LineConvertor convertor2 = new LineConvertor() {
            public List<ConvertedLine> convert(String line) {
                if ("line2".equals(line)) {
                    return Collections.<ConvertedLine>singletonList(
                            ConvertedLine.forText("converted:" + line, null));
                }
                return null;
            }
        };

        LineConvertor convertor = LineConvertors.proxy(convertor1, convertor2);
        List<ConvertedLine> convertedLines1 = convertor.convert("line1");
        List<ConvertedLine> convertedLines2 = convertor.convert("line2");

        assertEquals(1, convertedLines1.size());
        assertEquals(1, convertedLines2.size());

        assertEquals("converted:line1", convertedLines1.get(0).getText());
        assertEquals("converted:line2", convertedLines2.get(0).getText());

        assertNull(convertor.convert("line3"));
    }

    public void testFilePattern() {
        TestConvertor fallback = new TestConvertor();
        LineConvertor convertor = LineConvertors.proxy(LineConvertors.filePattern(
                null, Pattern.compile("myline:\\s*(myfile\\w*\\.\\w{3})\\s.*"), null, 1, -1), fallback);

        List<ConvertedLine> lines = new ArrayList<ConvertedLine>();
        lines.addAll(convertor.convert("otherline: something.txt"));
        lines.addAll(convertor.convert("myline: myfile01.txt other stuff"));
        lines.addAll(convertor.convert("total mess"));
        lines.addAll(convertor.convert("myline: myfile02.txt other stuff"));
        lines.addAll(convertor.convert("otherline: http://www.netbeans.org"));

        List<String> ignored = new ArrayList<String>();
        Collections.addAll(ignored, "otherline: something.txt", "total mess",
                "otherline: http://www.netbeans.org");
        assertEquals(ignored, fallback.getLines());

        assertEquals(2, lines.size());
        assertEquals("myline: myfile01.txt other stuff", lines.get(0).getText());
        assertEquals("myline: myfile02.txt other stuff", lines.get(1).getText());

        for (ConvertedLine line : lines) {
            assertNotNull(line.getListener());
        }
    }

    public void testFilePatternWithFilePattern() {
        TestConvertor fallback = new TestConvertor();
        LineConvertor convertor = LineConvertors.proxy(LineConvertors.filePattern(
                null, Pattern.compile("myline:\\s*(myfile\\w*\\.\\w{3})\\s.*"),
                Pattern.compile("myfile01\\.\\w{3}"), 1, -1), fallback);

        List<ConvertedLine> lines = new ArrayList<ConvertedLine>();
        lines.addAll(convertor.convert("otherline: something.txt"));
        lines.addAll(convertor.convert("myline: myfile01.txt other stuff"));
        lines.addAll(convertor.convert("total mess"));
        lines.addAll(convertor.convert("myline: myfile02.txt other stuff"));
        lines.addAll(convertor.convert("otherline: http://www.netbeans.org"));
        lines.addAll(convertor.convert("myline: myfile01.txt specific"));

        List<String> ignored = new ArrayList<String>();
        Collections.addAll(ignored, "otherline: something.txt", "total mess",
                "myline: myfile02.txt other stuff", "otherline: http://www.netbeans.org");
        assertEquals(ignored, fallback.getLines());

        assertEquals(2, lines.size());
        assertEquals("myline: myfile01.txt other stuff", lines.get(0).getText());
        assertEquals("myline: myfile01.txt specific", lines.get(1).getText());

        for (ConvertedLine line : lines) {
            assertNotNull(line.getListener());
        }
    }

    public void testFilePatternWithLocator() {
        TestConvertor fallback = new TestConvertor();
        TestFileLocator locator = new TestFileLocator();

        LineConvertor convertor = LineConvertors.proxy(LineConvertors.filePattern(
                locator, Pattern.compile("myline:\\s*(myfile\\w*\\.\\w{3})\\s.*"), null, 1, -1), fallback);

        List<ConvertedLine> lines = new ArrayList<ConvertedLine>();
        lines.addAll(convertor.convert("myline: myfile01.txt other stuff"));
        lines.addAll(convertor.convert("myline: myfile02.txt other stuff"));

        assertEquals(2, lines.size());
        assertEquals("myline: myfile01.txt other stuff", lines.get(0).getText());
        assertEquals("myline: myfile02.txt other stuff", lines.get(1).getText());

        for (ConvertedLine line : lines) {
            line.getListener().outputLineAction(new OutputEvent(InputOutput.NULL) {
                @Override
                public String getLine() {
                    return "line";
                }
            });
        }

        List<String> paths = new ArrayList<String>();
        Collections.addAll(paths, "myfile01.txt", "myfile02.txt");
        assertEquals(paths, locator.getPaths());
    }

    public void testHttpUrl() {
        TestConvertor fallback = new TestConvertor();
        LineConvertor convertor = LineConvertors.proxy(LineConvertors.httpUrl(), fallback);

        List<ConvertedLine> lines = new ArrayList<ConvertedLine>();
        lines.addAll(convertor.convert("nourl1"));
        lines.addAll(convertor.convert("NetBeans site: http://www.netbeans.org"));
        lines.addAll(convertor.convert("nourl2"));
        lines.addAll(convertor.convert("https://www.netbeans.org"));
        lines.addAll(convertor.convert("nourl3"));

        List<String> ignored = new ArrayList<String>();
        Collections.addAll(ignored, "nourl1", "nourl2", "nourl3");
        assertEquals(ignored, fallback.getLines());

        assertEquals(2, lines.size());
        assertEquals("NetBeans site: http://www.netbeans.org", lines.get(0).getText());
        assertEquals("https://www.netbeans.org", lines.get(1).getText());

        for (ConvertedLine line : lines) {
            assertNotNull(line.getListener());
        }
    }

    public void testHttpOpenHandler() throws MalformedURLException {
        LineConvertor convertor = LineConvertors.httpUrl();

        List<ConvertedLine> lines = new ArrayList<ConvertedLine>();
        assertNull(convertor.convert("nourl1"));
        lines.addAll(convertor.convert("NetBeans site: http://www.netbeans.org"));
        lines.addAll(convertor.convert("https://www.netbeans.org"));

        assertEquals(2, lines.size());
        assertEquals("NetBeans site: http://www.netbeans.org", lines.get(0).getText());
        assertEquals("https://www.netbeans.org", lines.get(1).getText());

        for (ConvertedLine line : lines) {
            assertNotNull(line.getListener());
            line.getListener().outputLineAction(null);
        }

        HttpOpenHandler handler = Lookup.getDefault().lookup(HttpOpenHandler.class);
        assertTrue(handler instanceof TestHttpOpenHandler);

        List<URL> opened = ((TestHttpOpenHandler) handler).getOpened();
        assertEquals(2, opened.size());
        assertEquals(new URL("http://www.netbeans.org"), opened.get(0));
        assertEquals(new URL("https://www.netbeans.org"), opened.get(1));
    }

    private static <T> void assertEquals(List<T> expected, List<T> value) {
        assertEquals(expected.size(), value.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), value.get(i));
        }
    }

    private static class TestConvertor implements LineConvertor {

        private final List<String> lines = new ArrayList<String>();

        public List<ConvertedLine> convert(String line) {
            lines.add(line);
            return Collections.emptyList();
        }

        public List<String> getLines() {
            return lines;
        }
    }

    private static class TestFileLocator implements LineConvertors.FileLocator {

        private final List<String> paths = new ArrayList<String>();

        public FileObject find(String filename) {
            paths.add(filename);
            return null;
        }

        public List<String> getPaths() {
            return paths;
        }
    }

}
