package org.netbeans.modules.html.validation;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
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
