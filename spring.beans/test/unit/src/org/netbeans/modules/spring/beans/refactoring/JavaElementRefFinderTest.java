/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
