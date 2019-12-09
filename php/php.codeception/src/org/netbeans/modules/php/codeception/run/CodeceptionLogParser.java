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
package org.netbeans.modules.php.codeception.run;

import java.io.IOException;
import java.io.Reader;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.api.util.FileUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public final class CodeceptionLogParser extends DefaultHandler {

    private static final Logger LOGGER = Logger.getLogger(CodeceptionLogParser.class.getName());

    private enum Content {
        NONE,
        ERROR,
        FAILURE
    };

    final XMLReader xmlReader;
    private final TestSessionVo givenTestSession;
    private final TestSessionVo tmpTestSession = new TestSessionVo();
    private final Deque<TestSuiteVo> testSuites = new LinkedList<>();
    private final Set<TestSuiteVo> parentTestSuites = new HashSet<>();

    private TestCaseVo testCase;
    private Content content = Content.NONE;
    private StringBuilder buffer = new StringBuilder(200); // for error/failure: buffer for the whole message


    private CodeceptionLogParser(TestSessionVo testSession) throws SAXException {
        assert testSession != null;
        this.givenTestSession = testSession;
        this.xmlReader = FileUtils.createXmlReader();
    }

    static boolean parse(Reader reader, TestSessionVo testSession) {
        try {
            CodeceptionLogParser parser = new CodeceptionLogParser(testSession);
            parser.xmlReader.setContentHandler(parser);
            parser.xmlReader.parse(new InputSource(reader));
            parser.finish();
            return true;
        } catch (SAXException ex) {
            // ignore (this can happen e.g. if one interrupts debugging)
            LOGGER.log(Level.INFO, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
        return false;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        assert qName != null;
        switch (qName) {
            case "testsuites": // NOI18N
                break;
            case "testsuite": // NOI18N
                processTestSuiteStart(attributes);
                break;
            case "testcase": // NOI18N
                processTestCase(attributes);
                break;
            case "failure": // NOI18N
            case "warning": // NOI18N
                startTestFailure(attributes);
                break;
            case "error": // NOI18N
                startTestError(attributes);
                break;
            default:
                // noop
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        assert qName != null;
        switch (qName) {
            case "testsuite": // NOI18N
                processTestSuiteEnd();
                break;
            case "testcase": // NOI18N
                assert testCase != null;
                testCase = null;
                break;
            case "failure": // NOI18N
            case "warning": // NOI18N
            case "error": // NOI18N
                endTestContent();
                break;
            default:
                // noop
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        switch (content) {
            case FAILURE:
            case ERROR:
                buffer.append(new String(ch, start, length));
                break;
            case NONE:
                // noop
                break;
            default:
                assert false : "Unknown content: " + content;
        }
    }

    private void processTestSuiteStart(Attributes attributes) {
        long time = getTime(attributes);
        if (tmpTestSession.getTime() != -1) {
            time += tmpTestSession.getTime();
        }
        tmpTestSession.setTime(time);
        int testSize = getTests(attributes);
        if (tmpTestSession.getTests() != -1) {
            testSize += tmpTestSession.getTests();
        }
        tmpTestSession.setTests(testSize);

        // in any other suite?
        // XXX this case exists?
        TestSuiteVo parentSuite = testSuites.peek();
        if (parentSuite != null) {
            parentTestSuites.add(parentSuite);
        }

        TestSuiteVo testSuite = new TestSuiteVo(getName(attributes), getTime(attributes));
        testSuites.push(testSuite);
        tmpTestSession.addTestSuite(testSuite);
    }

    private void processTestSuiteEnd() {
        testSuites.pop();
    }

    private void processTestCase(Attributes attributes) {
        assert testCase == null;
        testCase = new TestCaseVo(
                getClass(attributes),
                getName(attributes),
                getFile(attributes),
                getLine(attributes),
                getTime(attributes));
        testSuites.getFirst().addTestCase(testCase);
    }

    private void startTestError(Attributes attributes) {
        content = Content.ERROR;
    }

    private void startTestFailure(Attributes attributes) {
        content = Content.FAILURE;
    }

    private void endTestContent() {
        assert testCase != null;
        assert buffer.length() > 0;

        fillStacktrace();
        switch (content) {
            case FAILURE:
                testCase.setFailureStatus();
                break;
            case ERROR:
                testCase.setErrorStatus();
                break;
            default:
                assert false : "Unknown content type: " + content;
        }
        buffer = new StringBuilder(200);
        content = Content.NONE;
    }

    private void fillStacktrace() {
        String[] lines = buffer.toString().trim().split("\n"); // NOI18N
        assert lines.length >= 2 : "At least 2 lines must be found (message + stacktrace)";

        buffer = new StringBuilder(200);
        boolean stacktraceStarted = false;
        // 1st line is skipped
        for (int i = 1; i < lines.length; ++i) {
            String line = lines[i];
            if (line.trim().length() == 0) {
                if (!stacktraceStarted) {
                    // empty line => stacktrace started
                    stacktraceStarted = true;
                    testCase.addStacktrace(buffer.toString().trim());
                }
            } else if (line.startsWith("\n\n")) { // NOI18N
                // empty line => stacktrace started
                stacktraceStarted = true;
                testCase.addStacktrace(buffer.toString().trim());
            } else if (!stacktraceStarted) {
                buffer.append(line);
                buffer.append("\n"); // NOI18N
            } else {
                testCase.addStacktrace(line.trim());
            }
        }
        if (testCase.getStackTrace().length == 0) {
            testCase.addStacktrace(buffer.toString().trim());
        }
    }

    private int getTests(Attributes attributes) {
        return getInt(attributes, "tests"); // NOI18N
    }

    private long getTime(Attributes attributes) {
        long l = -1;
        try {
            l = Math.round(Double.parseDouble(attributes.getValue("time")) * 1000); // NOI18N
        } catch (NumberFormatException exc) {
            // ignored
        }
        return l;
    }

    private String getClass(Attributes attributes) {
        return attributes.getValue("class"); // NOI18N
    }

    private String getName(Attributes attributes) {
        return attributes.getValue("name"); // NOI18N
    }

    private String getFile(Attributes attributes) {
        return getFile(attributes, null);
    }

    private String getFile(Attributes attributes, String defaultFile) {
        String f = attributes.getValue("file"); // NOI18N
        if (f != null) {
            return f;
        }
        if (defaultFile != null) {
            return defaultFile;
        }
        return null;
    }

    private int getLine(Attributes attributes) {
        return getInt(attributes, "line"); // NOI18N
    }

    private int getInt(Attributes attributes, String name) {
        int i = -1;
        try {
            i = Integer.parseInt(attributes.getValue(name));
        } catch (NumberFormatException exc) {
            // ignored
        }
        return i;
    }

    private void finish() {
        givenTestSession.setTime(tmpTestSession.getTime());
        givenTestSession.setTests(tmpTestSession.getTests());
        for (TestSuiteVo testSuiteVo : tmpTestSession.getTestSuites()) {
            if (testSuiteVo.getPureTestCases().isEmpty()
                    && parentTestSuites.contains(testSuiteVo)) {
                // test suite that contains only other test suites and no tests => ignore it completely
                continue;
            }
            givenTestSession.addTestSuite(testSuiteVo);
        }
    }

}
