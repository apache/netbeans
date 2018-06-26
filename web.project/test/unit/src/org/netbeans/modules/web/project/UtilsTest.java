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

package org.netbeans.modules.web.project;

import org.netbeans.junit.NbTestCase;

/**
 * Test case for {@link Utils}.
 * @author Tomas Mysik
 */
public class UtilsTest extends NbTestCase {

    public UtilsTest(String testName) {
        super(testName);
    }
    
    /**
     * Test for correcting given <tt>debug.classpath</tt>.
     */
    public void testCorrectDebugClassPath() throws Exception { //#118187
        final String NB_55 = "${javac.classpath}:${build.classes.dir}:${build.ear.classes.dir}";
        final String NB_55_EXPECTED = "${javac.classpath}:${build.classes.dir}";
        assertEquals(NB_55_EXPECTED, Utils.correctDebugClassPath(NB_55));
        
        // notice semicolon usage
        final String CASE_1 = "${some.directory};${build.classes.dir}:${javac.classpath}:${another.directory}";
        final String CASE_1_EXPECTED = "${some.directory};${build.classes.dir}:${javac.classpath}:${another.directory}";
        assertEquals(CASE_1_EXPECTED, Utils.correctDebugClassPath(CASE_1));
        
        final String CASE_2 = null;
        final String CASE_2_EXPECTED = Utils.getDefaultDebugClassPath();
        assertEquals(CASE_2_EXPECTED, Utils.correctDebugClassPath(CASE_2));
        
        final String CASE_3 = "${j2ee.platform.classpath}:${build.ear.classes.dir}";
        final String CASE_3_EXPECTED = "${j2ee.platform.classpath}";
        assertEquals(CASE_3_EXPECTED, Utils.correctDebugClassPath(CASE_3));
        
        // incorrect classpath => remains incorrect
        final String CASE_4 = "defect";
        final String CASE_4_EXPECTED = "defect";
        assertEquals(CASE_4_EXPECTED, Utils.correctDebugClassPath(CASE_4));
        
        final String CASE_5 = "${some.directory}";
        final String CASE_5_EXPECTED = "${some.directory}";
        assertEquals(CASE_5_EXPECTED, Utils.correctDebugClassPath(CASE_5));
    }

}
