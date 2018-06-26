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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.editor.api.completion;

/**
 * 
 * @author schmidtm, Martin Janicek
 */
public class ConstructorsCCTest extends GroovyCCTestBase {

    public ConstructorsCCTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestType() {
        return "constructors";
    }
    

    public void testConstructors1() throws Exception {
        checkCompletion(BASE + "Constructors1.groovy", "StringBuffer sb = new StringBuffer^", false);
    }

    public void testConstructors2() throws Exception {
        checkCompletion(BASE + "Constructors2.groovy", "StringBuffer sb = new stringbuffer^", false);
    }

    public void testConstructors3() throws Exception {
        checkCompletion(BASE + "Constructors3.groovy", "FileOutputStream fos = new fileoutputstr^", false);
    }

    public void testConstructors4() throws Exception {
        checkCompletion(BASE + "Constructors4.groovy", "    Foo f = new F^", false);
    }

    public void testConstructors5() throws Exception {
        checkCompletion(BASE + "Constructors5.groovy", "    Foo f = new F^", false);
    }

    public void testConstructors6() throws Exception {
        checkCompletion(BASE + "Constructors6.groovy", "    Foo f = new Foo^", false);
    }

    public void testConstructors7() throws Exception {
        checkCompletion(BASE + "Constructors7.groovy", "        String s = new String^", false);
    }

    public void testConstructors8() throws Exception {
        checkCompletion(BASE + "Constructors8.groovy", "        String s = new String^(\"abc\");", false);
    }

    public void testSamePackage() throws Exception {
        checkCompletion(BASE + "SamePackage.groovy", "    Bar bar = new Bar^", false);
    }

    public void testSamePackageMoreConstructors() throws Exception {
        checkCompletion(BASE + "SamePackageMoreConstructors.groovy", "    Bar bar = new Bar^", false);
    }

    public void testImportedType() throws Exception {
        checkCompletion(BASE + "ImportedType.groovy", "    Bar bar = new Bar^", false);
    }

    public void testImportedTypeMoreConstructors() throws Exception {
        checkCompletion(BASE + "ImportedTypeMoreConstructors.groovy", "    Bar bar = new Bar^", false);
    }
}
