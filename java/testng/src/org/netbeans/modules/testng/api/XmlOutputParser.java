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
package org.netbeans.modules.testng.api;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.gsf.testrunner.api.Trouble.ComparisonFailure;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author lukas
 */
public final class XmlOutputParser extends DefaultHandler {

    private static final Logger LOG = Logger.getLogger(XmlOutputParser.class.getName());
    private int allTestsCount;
    private int failedTestsCount;
    private int passedTestsCount;
    private int skippedTestsCount;
    private int failedConfCount;
    private int skippedConfCount;
    private String status;
    private int suiteTime;
    /** */
    private static final int STATE_OUT_OF_SCOPE = 0;
    private static final int STATE_SUITE = 3;
    private static final int STATE_GROUPS = 4;
    private static final int STATE_GROUP = 5;
    private static final int STATE_METHOD = 6;
    private static final int STATE_TEST = 7;
    private static final int STATE_CLASS = 8;
    private static final int STATE_TEST_METHOD = 9;
    private static final int STATE_TEST_PARAMS = 10;
    private static final int STATE_TEST_PARAM = 11;
    private static final int STATE_TEST_VALUE = 12;
    private static final int STATE_EXCEPTION = 13;
    private static final int STATE_MESSAGE = 14;
    private static final int STATE_FULL_STACKTRACE = 15;
    private int state = STATE_OUT_OF_SCOPE;
    /** */
    private XmlResult reports;
    private TestNGTest test;
    private TestNGTestSuite testsuite;
    private TestNGTestcase testcase;
    private Trouble trouble;
    private String tcClassName;
    private StringBuffer text;
    private final XMLReader xmlReader;
    private TestSession testSession;

    /** Creates a new instance of XMLOutputParser */
    private XmlOutputParser(TestSession session) throws SAXException {
        this.testSession = session;
        xmlReader = XMLUtil.createXMLReader();
        xmlReader.setContentHandler(this);
    }

    public static XmlResult parseXmlOutput(Reader reader, TestSession session) throws SAXException, IOException {
        assert reader != null;
        XmlOutputParser parser = new XmlOutputParser(session);
        try {
            parser.xmlReader.parse(new InputSource(reader));
        } catch (SAXException ex) {
            LOG.info("Exception while parsing XML output from TestNG: " + ex.getMessage()); //NOI18N
            throw ex;
        } catch (IOException ex) {
            assert false;            /* should never happen */
        } finally {
            reader.close();          //throws IOException
        }
        return parser.reports;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (state) {
            case STATE_SUITE:
                if ("groups".equals(qName)) { //NOI18N
                    //XXX - not handled yet, shoould perhaps create ie. "group" view
                    state = STATE_GROUPS;
                } else if ("test".equals(qName)) { //NOI18N
                    String name = attributes.getValue("name"); //NOI18N
                    test = name != null ? new TestNGTest(name) : new TestNGTest(""); //NOI18N
                    state = STATE_TEST;
                }
                break;
            case STATE_GROUPS:
                if ("group".equals(qName)) { //NOI18N
                    state = STATE_GROUP;
                }
                break;
            case STATE_GROUP:
                if ("method".equals(qName)) { //NOI18N
                    state = STATE_METHOD;
                }
                break;
            case STATE_METHOD:
                //empty for now
                break;
            case STATE_TEST:
                if ("class".equals(qName)) { //NOI18N
                    tcClassName = attributes.getValue("name"); //NOI18N
                    testsuite = new TestNGTestSuite(tcClassName, testSession);

                    state = STATE_CLASS;
                }
                break;
            case STATE_CLASS:
                if ("test-method".equals(qName)) { //NOI18N
                    int duration = Integer.valueOf(attributes.getValue("duration-ms")); //NOI18N
                    testcase = createTestcaseReport(tcClassName, attributes.getValue("name"), duration); //NOI18N
                    suiteTime += duration;
                    testcase.setConfigMethod(Boolean.valueOf(attributes.getValue("is-config"))); //NOI18N
                    status = attributes.getValue("status"); //NOI18N
                    if (!testcase.isConfigMethod()) {
                        allTestsCount++;
                    }
                    if ("FAIL".equals(status)) { //NOI18N
                        testcase.setStatus(Status.FAILED);
                        if (testcase.isConfigMethod()) {
                            failedConfCount++;
                        } else {
                            failedTestsCount++;
                        }
                        trouble = new Trouble(true);
                    } else if ("PASS".equals(status)) { //NOI18N
                        testcase.setStatus(Status.PASSED);
                        passedTestsCount++;
                    } else if ("SKIP".equals(status)) { //NOI18N
                        testcase.setStatus(Status.SKIPPED);
                        trouble = new Trouble(false);
                        if (testcase.isConfigMethod()) {
                            skippedConfCount++;
                        } else {
                            skippedTestsCount++;
                        }
                    }
                    state = STATE_TEST_METHOD;
                }
                break;
            case STATE_TEST_METHOD:
                if ("params".equals(qName)) { //NOI18N
                    state = STATE_TEST_PARAMS;
                } else if ("exception".equals(qName)) { //NOI18N
                    assert testcase != null && status != null;
                    if (!"PASS".equals(status)) {

//TODO:                        trouble.exceptionClsName = attributes.getValue("class"); //NOI18N
                    }
                    //if test passes, skip possible exception element
                    state = (trouble != null) ? STATE_EXCEPTION : STATE_TEST_METHOD;
                }
                break;
            case STATE_EXCEPTION:
                //how to get text msgs here?
                //exMessage =
                if ("message".equals(qName)) { //NOI18N
                    state = STATE_MESSAGE;
                } else if ("full-stacktrace".equals(qName)) { //NOI18N
                    state = STATE_FULL_STACKTRACE;
                }
                break;
            default:
                if (qName.equals("suite")) { //NOI18N
                    String name = attributes.getValue("name");
                    if (name == null || "".equals(name.trim())) {
                        name = NbBundle.getMessage(XmlOutputParser.class, "UNKNOWN_NAME");
                    }
                    reports = new XmlResult(name);
                    state = STATE_SUITE;
                }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (state) {
            case STATE_GROUPS:
                assert "groups".equals(qName); //NOI18N
                state = STATE_SUITE;
                break;
            case STATE_GROUP:
                assert "group".equals(qName); //NOI18N
                state = STATE_GROUPS;
                break;
            case STATE_METHOD:
                assert "method".equals(qName) : "was " + qName; //NOI18N
                state = STATE_GROUP;
                break;
            case STATE_SUITE:
                assert "suite".equals(qName) : "was " + qName; //NOI18N
                state = STATE_OUT_OF_SCOPE;
                break;
            case STATE_TEST:
                assert "test".equals(qName); //NOI18N
                reports.addTestNGTest(test);
                test = null;
                state = STATE_SUITE;
                break;
            case STATE_CLASS:
                assert "class".equals(qName); //NOI18N
                testsuite.setElapsedTime(suiteTime);
                test.addTestsuite(testsuite);
                testsuite = null;
                suiteTime = 0;
                skippedTestsCount = 0;
                failedTestsCount = 0;
                allTestsCount = 0;
                passedTestsCount = 0;
                failedConfCount = skippedConfCount = 0;
                tcClassName = null;
                testcase = null;
                state = STATE_TEST;
                break;
            case STATE_TEST_METHOD:
                //if test passes, wait for our element
                if (!"test-method".equals(qName)) {
                    break;
                }
                assert "test-method".equals(qName) : "was " + qName; //NOI18N
                assert testcase != null;
                testcase.setTrouble(trouble);
                //assing all methods including config ones
                testsuite.getTestcases().add(testcase);
                trouble = null;
                testcase = null;
                state = STATE_CLASS;
                break;
            case STATE_TEST_PARAMS:
                //XXX - param and value elements are not handled yet
                if ("param".equals(qName) || "value".equals(qName)) { //NOI18N
                    break;
                }
                assert "params".equals(qName) : "was " + qName; //NOI18N
                state = STATE_TEST_METHOD;
                break;
            case STATE_EXCEPTION:
                assert "exception".equals(qName); //NOI18N
                state = STATE_TEST_METHOD;
                break;
            case STATE_MESSAGE:
                assert "message".equals(qName); //NOI18N
                assert testcase != null;
                assert trouble != null;
                if (text != null) {
                    //there should be better way to do this
                    String s = text.toString().trim();
                    if (s.startsWith("expected:")) {  //NOI18N
                        int index = s.indexOf("<"); //NOI18N
                        if (index > -1) {
                            int ie = s.indexOf(">", index + 1); //NOI18N
                            String expected = s.substring(index + 1, ie);
                            String actual = s.substring(s.indexOf("<", ie + 1) + 1, s.indexOf(">", ie + 1)); //NOI18N
                            trouble.setComparisonFailure(new ComparisonFailure(expected, actual));
                        }
                    }
                    text = null;
                }
                state = STATE_EXCEPTION;
                break;
            case STATE_FULL_STACKTRACE:
                assert "full-stacktrace".equals(qName); //NOI18N
                if (text != null) {
                    String[] lines = text.toString().trim().split("[\\r\\n]+"); //NOI18N
                    trouble.setStackTrace(lines);
                    text = null;
                }
                state = STATE_EXCEPTION;
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        switch (state) {
            case STATE_MESSAGE:
            case STATE_FULL_STACKTRACE:
                if (text == null) {
                    text = new StringBuffer(512);
                }
                text.append(ch, start, length);
                break;
        }
    }

    private TestNGTestcase createTestcaseReport(String className, String name, int time) {
//        TestNGTestcase tc = new TestNGTestcase(name, "TestNG Test", testSession);
        TestNGTestcase tc = new TestNGTestcase(className + "." + name, "params", null, testSession);
        tc.setTimeMillis(time);
        return tc;
    }
}
