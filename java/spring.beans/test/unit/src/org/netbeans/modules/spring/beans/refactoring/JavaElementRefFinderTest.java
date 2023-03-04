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

package org.netbeans.modules.spring.beans.refactoring;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel.DocumentAccess;
import org.netbeans.modules.spring.beans.ConfigFileTestCase;
import org.netbeans.modules.spring.beans.TestUtils;
import org.netbeans.modules.spring.beans.refactoring.Occurrences.JavaClassRefMatcher;
import org.netbeans.modules.spring.beans.refactoring.Occurrences.JavaPackageRefMatcher;
import org.netbeans.modules.spring.beans.refactoring.Occurrences.Occurrence;

/**
 *
 * @author Andrei Badea
 */
public class JavaElementRefFinderTest extends ConfigFileTestCase {

    public JavaElementRefFinderTest(String testName) {
        super(testName);
    }

    public void testClass() throws Exception {
        final String PACKAGE = "org.example";
        final String CLASS = PACKAGE + ".Foo";
        final String contents = TestUtils.createXMLConfigText("<bean id='foo' class='" + CLASS + "'/>");
        TestUtils.copyStringToFile(contents, configFile);
        SpringConfigModel model = createConfigModel(configFile);
        model.runDocumentAction(new Action<DocumentAccess>() {
            public void run(DocumentAccess docAccess) {
                JavaElementRefFinder finder = new JavaElementRefFinder(docAccess);
                List<Occurrence> occurrences = new ArrayList<Occurrence>();
                // Test class.
                try {
                    finder.addOccurrences(new JavaClassRefMatcher(CLASS), occurrences);
                } catch (BadLocationException e) {
                    fail(e.toString());
                }
                assertEquals(occurrences.size(), 1);
                Occurrence occurrence = occurrences.get(0);
                int begin = contents.indexOf(CLASS);
                int end = begin + CLASS.length();
                assertEquals(begin, occurrence.getPosition().getBegin().getOffset());
                assertEquals(end, occurrence.getPosition().getEnd().getOffset());
            }
        });
    }

    public void testPackage() throws Exception {
        final String PACKAGE = "org.example";
        final String FOO_CLASS = PACKAGE + ".Foo";
        final String BAR_CLASS = PACKAGE + "bar.Bar";
        final String contents = TestUtils.createXMLConfigText(
                "<bean id='foo' class='" + FOO_CLASS + "'/>" +
                "<bean id='bar' class='" + BAR_CLASS + "'/>");
        TestUtils.copyStringToFile(contents, configFile);
        SpringConfigModel model = createConfigModel(configFile);
        model.runDocumentAction(new Action<DocumentAccess>() {
            public void run(DocumentAccess docAccess) {
                JavaElementRefFinder finder = new JavaElementRefFinder(docAccess);
                List<Occurrence> occurrences = new ArrayList<Occurrence>();
                // Test non-recursive.
                try {
                    finder.addOccurrences(new JavaPackageRefMatcher(PACKAGE, false), occurrences);
                } catch (BadLocationException e) {
                    fail(e.toString());
                }
                assertEquals(occurrences.size(), 1);
                Occurrence occurrence = occurrences.get(0);
                int begin = contents.indexOf(FOO_CLASS);
                int end = begin + PACKAGE.length();
                assertEquals(begin, occurrence.getPosition().getBegin().getOffset());
                assertEquals(end, occurrence.getPosition().getEnd().getOffset());
                // Test recursive.
                occurrences.clear();
                try {
                    finder.addOccurrences(new JavaPackageRefMatcher(PACKAGE, true), occurrences);
                } catch (BadLocationException e) {
                    fail(e.toString());
                }
                assertEquals(occurrences.size(), 2);
                occurrence = occurrences.get(0);
                begin = contents.indexOf(FOO_CLASS);
                end = begin + PACKAGE.length();
                assertEquals(begin, occurrence.getPosition().getBegin().getOffset());
                assertEquals(end, occurrence.getPosition().getEnd().getOffset());
                occurrence = occurrences.get(1);
                begin = contents.indexOf(BAR_CLASS);
                end = begin + PACKAGE.length();
                assertEquals(begin, occurrence.getPosition().getBegin().getOffset());
                assertEquals(end, occurrence.getPosition().getEnd().getOffset());
            }
        });
    }
}
