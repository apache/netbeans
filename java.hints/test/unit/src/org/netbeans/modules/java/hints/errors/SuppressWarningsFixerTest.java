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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.java.hints.errors;

import org.netbeans.modules.java.hints.infrastructure.HintsTestBase;
import org.netbeans.modules.java.hints.spiimpl.TestCompilerSettings;

/**
 *
 * @author Jan Lahoda
 */
public class SuppressWarningsFixerTest extends HintsTestBase {
    
    public SuppressWarningsFixerTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.doSetUp("org/netbeans/modules/java/hints/resources/layer.xml");
        TestCompilerSettings.commandLine = "-Xlint:deprecation -Xlint:fallthrough -Xlint:unchecked";
    }
    
    @Override
    protected boolean createCaches() {
        return false;
    }
    
    @Override
    protected String testDataExtension() {
        return "org/netbeans/test/java/hints/SuppressWarningsFixerTest/";
    }
    
    public void testSuppressWarnings1() throws Exception {
        performTest("Test", "unchecked", 8, 5);
    }
    
    public void testSuppressWarnings2() throws Exception {
        performTest("Test", "unchecked", 11, 5);
    }
    
    public void testSuppressWarnings3() throws Exception {
        performTest("Test", "unchecked", 16, 5);
    }
    
    public void testSuppressWarnings4() throws Exception {
        performTest("Test", "unchecked", 22, 5);
    }
    
    public void testSuppressWarnings5() throws Exception {
        performTest("Test", "unchecked", 28, 5);
    }
    
    public void testSuppressWarnings6() throws Exception {
        performTest("Test", "unchecked", 35, 5);
    }
    
    public void testSuppressWarnings7() throws Exception {
        performTest("Test2", "unchecked", 10, 5);
    }
    
    public void testSuppressWarnings8() throws Exception {
        performTest("Test2", "unchecked", 16, 5);
    }
    
    public void testSuppressWarnings9() throws Exception {
        performTest("Test2", "unchecked", 22, 5);
    }
    
    public void testSuppressWarnings10() throws Exception {
        performTestDoNotPerform("Test2", 31, 5);
    }
    
    public void testSuppressWarnings11() throws Exception {
        performTestDoNotPerform("Test2", 38, 5);
    }
    
    public void testSuppressWarnings106794() throws Exception {
	performTestDoNotPerform("Test3", 3, 10);
    }
    
}
