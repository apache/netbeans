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

import java.io.IOException;
import java.io.StringReader;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.html.editor.lib.api.*;
import org.netbeans.modules.html.editor.lib.api.validation.ValidationContext;
import org.netbeans.modules.html.editor.lib.api.validation.ValidationException;
import org.netbeans.modules.html.editor.lib.api.validation.ValidationResult;
import org.netbeans.modules.html.editor.lib.api.validation.Validator;
import org.openide.util.Lookup;
import org.xml.sax.SAXException;

/**
 *
 * @author marekfukala
 */
public class ValidatorImplTest extends NbTestCase {

    public ValidatorImplTest(String name) {
        super(name);
    }

    public static Test xsuite() {
//        ValidationTransaction.enableDebug();

        String testName = "testFragment";
        System.err.println("Running only following test: " + testName);
        TestSuite suite = new TestSuite();
        suite.addTest(new ValidationTransactionTest(testName));
        return suite;
    }

    public void testInstanceInLookup() {
        assertNotNull(Lookup.getDefault().lookup(Validator.class));
    }

    public void testBasic() throws SAXException, IOException, ParseException, ValidationException {
        String code = "<!doctype html> <html><head><title>hello</title></head><body><div>ahoj!</div></body></html>";
        HtmlSource source = new HtmlSource(code);
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze();

        Validator instance = Lookup.getDefault().lookup(Validator.class);
        assertNotNull(instance);

        ValidationResult validationResult = instance.validate(new ValidationContext(new StringReader(code), HtmlVersion.HTML5, null, result));
        assertNoProblems(validationResult);

    }

    //should fail on missing title element even if the file is a fragment, but contains the head element
    public void testFragmentWithHead() throws SAXException, IOException, ParseException, ValidationException {
        String code = "<html><head></head><body><div>ahoj!</div></body></html>";
        HtmlSource source = new HtmlSource(code);
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze();

        assertNull(result.getDetectedHtmlVersion());

        Validator instance = Lookup.getDefault().lookup(Validator.class);
        assertNotNull(instance);

        ValidationResult validationResult = instance.validate(new ValidationContext(new StringReader(code), HtmlVersion.HTML5, null, result));
        assertNotNull(validationResult);

        assertFalse(validationResult.isSuccess());

    }

    //pure fragment, no missing head children error
    public void testFragmentWithoutHead() throws SAXException, IOException, ParseException, ValidationException {
        String code = "<div>ahoj!</div>";
        HtmlSource source = new HtmlSource(code);
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze();

        assertNull(result.getDetectedHtmlVersion());

        Validator instance = Lookup.getDefault().lookup(Validator.class);
        assertNotNull(instance);

        ValidationResult validationResult = instance.validate(new ValidationContext(new StringReader(code), HtmlVersion.HTML5, null, result));
        assertNoProblems(validationResult);

    }

    public void testNETBEANS2333() throws ValidationException {
        String code = ""
                + "<!DOCTYPE html>\n"
                + "<html>\n"
                + "    <head>\n"
                + "        <title>NETBEANS2333</title>\n"
                + "    </head>\n"
                + "    <body>\n"
                + "        <input type=\"text\" placeholder=\"input text\" name=\"text3\" minlength=\"3\" maxlength=\"15\" required>\n"
                + "    </body>\n"
                + "</html>\n";
        testNoProblems(code);
    }

    public void testNETBEANS3681() throws ValidationException {
        String code = ""
                + "<!DOCTYPE html>\n"
                + "<html>\n"
                + "    <head>\n"
                + "        <title>NETBEANS3681</title>\n"
                + "        <script type=\"module\">\n"
                + "        </script>\n"
                + "    </head>\n"
                + "    <body>\n"
                + "    </body>\n"
                + "</html>\n";
        testNoProblems(code);
    }

    private void testNoProblems(String code) throws ValidationException {
        HtmlSource source = new HtmlSource(code);
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze();

        Validator instance = Lookup.getDefault().lookup(Validator.class);
        assertNotNull(instance);

        ValidationResult validationResult = instance.validate(new ValidationContext(new StringReader(code), HtmlVersion.HTML5, null, result));
        assertNoProblems(validationResult);
    }

//    //tests if the validation ignores the templating language marks @@@
//    public void testValidateVirtualHtmlSource() throws SAXException, IOException, ParseException, ValidationException {
//        String code = "@@@<!doctype html> <html @@@><head><title>hello</title></head><body><div>ahoj!</div></body></html>";
//        HtmlSource source = new HtmlSource(code);
//        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze();
//
//        Validator instance = Lookup.getDefault().lookup(Validator.class);
//        assertNotNull(instance);
//
//        ValidationResult validationResult = instance.validate(new ValidationContext(new StringReader(code), HtmlVersion.HTML5, null, result));
//        assertNoProblems(validationResult);
//
//    }

    private void assertNoProblems(ValidationResult result) {
        assertNotNull(result);
        if (!result.isSuccess()) {
            StringBuilder b = new StringBuilder();
            b.append("Unexpected problem(s) found: ");
            for (ProblemDescription pd : result.getProblems()) {
                b.append(pd.toString());
                b.append(',');
            }
            assertFalse(b.toString(), true);
        }
    }
}
