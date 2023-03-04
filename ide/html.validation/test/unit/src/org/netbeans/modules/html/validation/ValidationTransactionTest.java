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

package org.netbeans.modules.html.validation;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestSuite;
import nu.validator.servlet.ParserMode;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.html.editor.lib.api.*;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;
import org.xml.sax.SAXException;

/**
 *
 * @author marekfukala
 */
public class ValidationTransactionTest extends TestBase {

    public ValidationTransactionTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setInstances(
                new TestProjectFactory(),
                new SimpleFileOwnerQueryImplementation());

    }

    public static Test xsuite() {
        NbValidationTransaction.enableDebug();

        String testName = "testIssue194618";
        System.err.println("Running only following test: " + testName);
        TestSuite suite = new TestSuite();
        suite.addTest(new ValidationTransactionTest(testName));
        return suite;
    }

    public void testBasic() throws SAXException, IOException {
//        NbValidationTransaction.enableDebug();

        validate("<!doctype html> <html><head><title>hello</title></head><body><div>ahoj!</div></body></html>", true);
        validate("<!doctype html> chybi open tag</div>", false);
        validate("<!doctype html> <div> chybi close tag", false);

        validate("<!doctype html>\n"
                + "<html><head><title>hello</title></head>\n"
                + "<body>\n"
                + "<div>ahoj!</Xiv>\n"
                + "</body></html>\n", false);

        validate("1\n"
                + "23\n"
                + "345\n"
                + "<!doctype html>\n"
                + "<html><head><title>hello</title></head>\n"
                + "<body>\n"
                + "<div>ahoj!</Xiv>\n"
                + "</body></html>\n", false);

    }

    public void testErrorneousSources() throws SAXException {
        //IIOBE from LinesMapper.getSourceOffsetForLocation(LinesMapper.java:129)
        validate("<!doctype html> "
                + "<html>    "
                + "<title>dd</title>"
                + "<b"
                + "a"
                + "</body>"
                + "</html>    ", false);
    }

    public void testMathML() throws SAXException {
        validate("<!doctype html> "
                + "<html>    "
                + "<title>dd</title>"
                + "<body>"
                + "  <math>"
                + "     <mi>x</mi>"
                + "     <mo>=</mo>"
                + "  </math>"
                + "</body>"
                + "</html>    ", true);
    }

    public void testXhtml() throws SAXException {
        validate("<?xml version='1.0' encoding='UTF-8' ?>"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
                + "<head><title>title</title></head>"
                + "<body>"
                + "</body>"
                + "</html>    ", true, HtmlVersion.XHTML5);
    }

    public void testHtml4() throws SAXException {
        validate("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n"
                + "<html>"
                + "     <head>"
                + "         <title>hello</title>"
                + "     </head>"
                + "     <body>"
                + "         <div>ahoj!</div>"
                + "     </body>"
                + "</html>", true, HtmlVersion.HTML41_TRANSATIONAL);
    }

//    public void testFragment() throws SAXException {
//        String code = "<div>aaa</div>";
//        NbValidationTransaction vt = NbValidationTransaction.create(HtmlVersion.HTML5);
//        vt.setBodyFragmentContextMode(true);
//        vt.validateCode(code);
//        for (ProblemDescription pd : vt.getFoundProblems()) {
//                System.err.println(pd);
//            }
//        assertTrue(vt.isSuccess());
//    }
    //xhtml 1.0 strict, proper xml pi, doctype and root namespace
    public void testXhtmlFile1() throws SAXException {
        FileObject fo = getTestFile("testfiles/test1.xhtml");
        Source source = Source.create(fo);
        String code = source.createSnapshot().getText().toString();
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(new HtmlSource(fo)).analyze();
        assertNotNull(result);

        HtmlVersion version = result.getHtmlVersion();
        assertSame(HtmlVersion.XHTML10_STICT, version);

        NbValidationTransaction vt = NbValidationTransaction.create(result.getHtmlVersion());
        validate(code, true, result.getHtmlVersion(), vt);

        assertSame(ParserMode.XML_NO_EXTERNAL_ENTITIES, vt.parser);
        assertNotNull(vt.xmlParser);
        assertNull(vt.htmlParser);
    }

    //xhtml 5, proper xml pi, namespace, MISSING doctype
    public void testXhtmlFile2() throws SAXException {
        FileObject fo = getTestFile("testfiles/test2.xhtml");
        Source source = Source.create(fo);
        String code = source.createSnapshot().getText().toString();
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(new HtmlSource(fo)).analyze();
        assertNotNull(result);

        assertNull(result.getDetectedHtmlVersion());
        HtmlVersion version = result.getHtmlVersion();
        assertSame(HtmlVersion.XHTML5, version);

        NbValidationTransaction vt = NbValidationTransaction.create(result.getHtmlVersion());
        validate(code, true, result.getHtmlVersion(), vt);

        assertSame(ParserMode.XML_NO_EXTERNAL_ENTITIES, vt.parser);
        assertNotNull(vt.xmlParser);
        assertNull(vt.htmlParser);
    }

    // Ensure files with embedded CSS pass
    public void testEmbeddedCSS() throws SAXException {
        FileObject fo = getTestFile("testfiles/testEmbeddedCSS.xhtml");
        Source source = Source.create(fo);
        String code = source.createSnapshot().getText().toString();
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(new HtmlSource(fo)).analyze();
        assertNotNull(result);

        assertSame(HtmlVersion.HTML5, result.getDetectedHtmlVersion());
        HtmlVersion version = result.getHtmlVersion();
        assertSame(HtmlVersion.HTML5, version);

        NbValidationTransaction vt = NbValidationTransaction.create(result.getHtmlVersion());
        validate(code, true, result.getHtmlVersion(), vt);
    }

    public void testNamespacesFiltering() throws SAXException {
        FileObject fo = getTestFile("testfiles/wicket.xhtml");
        Source source = Source.create(fo);
        String code = source.createSnapshot().getText().toString();
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(new HtmlSource(fo)).analyze();
        assertNotNull(result);

        NbValidationTransaction vt = NbValidationTransaction.create(result.getHtmlVersion());
        validate(code, true, result.getHtmlVersion(), vt, Collections.singleton("http://wicket.apache.org"));

    }

    public void testXhtmlFailOnXmlParsing() throws SAXException {
        validate("<?xml version='1.0' encoding='UTF-8' ?>"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
                + "<head><title>title</title></head>"
                + "<bodyyyyyy>"
                + "</body>"
                + "</html>    ", false, HtmlVersion.XHTML5);
    }

    public void testXhtmlFailOnSchamatron() throws SAXException {
        validate("<?xml version='1.0' encoding='UTF-8' ?>"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
                + "<head><title>title</title></head>"
                + "<body unknown=\"attribute\">"
                + "</body>"
                + "</html>    ", false, HtmlVersion.XHTML5);
    }

    //Aelfred replacement by Xerces and CharacterHandlerReader caused the error locations
    //to be weirdly shifted
    public void testXhtmlErrorsPositions() throws SAXException {
        String code = "<?xml version='1.0' encoding='UTF-8' ?>"   //1
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\">" //1
                //012345678901 23456789012345678901234567890 123456789
                // 0         1          2         3         4
                + "<head><title>title</title></head>\n"             //1
                //01234567890123456789012345678901234567890 123456789
                // 0        1         2         3         4
                + "<body>\n"                                        //2
                //0123456789
                + "ABCD<a>\n"                                       //3
                //012345678
                + "1234</xxx>\n"                                  //4
                //0123456789
                
                + "</body>\n"
                + "</html>\n";

        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(new HtmlSource(code)).analyze();
        assertNotNull(result);
        assertNull(result.getDetectedHtmlVersion());

        NbValidationTransaction vt = NbValidationTransaction.create(result.getHtmlVersion());
        validate(code, false, result.getHtmlVersion(), vt);

        Collection<ProblemDescription> problems = vt.getFoundProblems(ProblemDescription.WARNING);
        assertSame(1, problems.size());

        ProblemDescription pd = problems.iterator().next();
        assertNotNull(pd);
        assertEquals(135, pd.getFrom());
        assertEquals(138, pd.getTo());
            
    }

    public void testFindBackwardDiff() {
        String pattern = "XXX\n1234XXX";
        //                0123

        String suffix = "234!";
        String text = "\n1234" + suffix;
        int diff = NbValidationTransaction.findBackwardDiff(text, text.length(), pattern.toCharArray(), 3, 5);
        assertSame(suffix.length(), diff);

        suffix = "234!";
        text = "\n1234" + suffix;
        int tlen = text.length();
        text += "0000000000000";
        diff = NbValidationTransaction.findBackwardDiff(text, tlen, pattern.toCharArray(), 3, 5);
        assertSame(suffix.length(), diff);

        suffix = "4!";
        text = "\n1234" + suffix;
        diff = NbValidationTransaction.findBackwardDiff(text, text.length(), pattern.toCharArray(), 3, 5);
        assertSame(suffix.length(), diff);

        suffix = "";
        text = "\n1234" + suffix;
        diff = NbValidationTransaction.findBackwardDiff(text, text.length(), pattern.toCharArray(), 3, 5);
        assertSame(suffix.length(), diff);

        suffix = "!34";
        text = "\n1234" + suffix;
        diff = NbValidationTransaction.findBackwardDiff(text, text.length(), pattern.toCharArray(), 3, 5);
        assertSame(suffix.length(), diff);

        suffix = "";
        text = suffix;
        diff = NbValidationTransaction.findBackwardDiff(text, text.length(), pattern.toCharArray(), 3, 5);
        assertSame(suffix.length(), diff);

        //does not match, but is suffix, lets return 0 diff in this case
        suffix = "34";
        text = suffix;
        diff = NbValidationTransaction.findBackwardDiff(text, text.length(), pattern.toCharArray(), 3, 5);
        assertSame(0, diff);

        suffix = "\n1234";
        text = "abcdXXXXX\n1234" + suffix;
        diff = NbValidationTransaction.findBackwardDiff(text, text.length(), pattern.toCharArray(), 3, 5);
        assertSame(0, diff);

        suffix = "\n1234";
        text = "abcd\n1234" + suffix;
        diff = NbValidationTransaction.findBackwardDiff(text, text.length(), pattern.toCharArray(), 3, 5);
        assertSame(0, diff);

        //try limited pattern length
        suffix = "\nxx34";
        text = "abcd" + suffix;
        NbValidationTransaction.PATTERN_LEN_LIMIT = 2;
        diff = NbValidationTransaction.findBackwardDiff(text, text.length(), pattern.toCharArray(), 3, 5);
        assertSame(0, diff);

    }

    public void testIssue194618() {
        String text = "<html  xmlns=\"http://www.w3.org/1999/xhtml\">\n<img/> &nbsp;\n</html>";
        //             0123456789012 34567890123456789012345678901 234 567890123456789
        //             0         1         2         3         4
        int tend = 47;

        int pfrom = 44;
        int plen = 1;

        int diff = NbValidationTransaction.findBackwardDiff(text, tend, text.toCharArray(), pfrom, plen);
        assertEquals(2, diff);

    }
    
    private void validate(String code, boolean expectedPass) throws SAXException {
        validate(code, expectedPass, HtmlVersion.HTML5);
    }

    private void validate(String code, boolean expectedPass, HtmlVersion version) throws SAXException {
        NbValidationTransaction vt = NbValidationTransaction.create(version);
        validate(code, expectedPass, version, vt);
    }

    private void validate(String code, boolean expectedPass, HtmlVersion version, NbValidationTransaction vt) throws SAXException {
        validate(code, expectedPass, version, vt, Collections.<String>emptySet());
    }

    private void validate(String code, boolean expectedPass, HtmlVersion version, NbValidationTransaction vt, Set<String> filteredNamespaces) throws SAXException {
        System.out.println(String.format("Validating code %s chars long, using %s.", code.length(), version));
        vt.validateCode(new StringReader(code), null, filteredNamespaces, "UTF-8");

        Collection<ProblemDescription> problems = vt.getFoundProblems(
                new ProblemDescriptionFilter.CombinedFilter(new ProblemDescriptionFilter.SeverityFilter(ProblemDescription.WARNING),
                new ProblemDescriptionFilter() {

                    @Override
                    public boolean accepts(ProblemDescription pd) {
                        return !isFilteredNamespacesProblem(pd);
                    }
                }));

        if (expectedPass && !problems.isEmpty()) {
            System.err.println("There are some unexpected problems:");
            for (ProblemDescription pd : problems) {
                System.err.println(pd.toString());
            }
        }

//        assertEquals(expectedPass, vt.isSuccess());
        assertEquals(expectedPass, problems.isEmpty());

        System.out.println("validated in " + vt.getValidationTime() + " ms with " + problems.size() + " problems.");
    }

    private FileObject getTestFile(String relFilePath) {
        File wholeInputFile = new File(getDataDir(), relFilePath);
        if (!wholeInputFile.exists()) {
            NbTestCase.fail("File " + wholeInputFile + " not found.");
        }
        FileObject fo = FileUtil.toFileObject(wholeInputFile);
        assertNotNull(fo);

        return fo;
    }

    private boolean isFilteredNamespacesProblem(ProblemDescription pd) {
        //ugly, but the validation doesn't provide any message keys or anything at all :-(
        for(int i=0; i < FILTERED_NS_PROBLEM_NAMES.length; i++) {
            if(pd.getText().contains(FILTERED_NS_PROBLEM_NAMES[i])) {
                return true;
            }
        }
        return false;
    }

    private static final String[] FILTERED_NS_PROBLEM_NAMES = new String[]{
        "Content is being hidden from the validator based on namespace filtering.",
        "Cannot filter out the root element.",
        "Filtering out selected namespaces causes descendants in other namespaces to be dropped as well."
    };
    
}
