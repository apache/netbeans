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

package org.netbeans.modules.spring.api.beans.model;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel.DocumentAccess;
import org.netbeans.modules.spring.beans.ConfigFileTestCase;
import org.netbeans.modules.spring.beans.TestUtils;

/**
 *
 * @author Andrei Badea
 */
public class SpringConfigModelTest extends ConfigFileTestCase {

    public SpringConfigModelTest(String testName) {
        super(testName);
    }

    public void testRunReadAction() throws Exception {
        SpringConfigModel model = createConfigModel();
        final boolean[] actionRun = { false };
        model.runReadAction(new Action<SpringBeans>() {
            public void run(SpringBeans springBeans) {
                actionRun[0] = true;
            }
        });
        assertTrue(actionRun[0]);
    }

    public void testExceptionPropagation() throws IOException {
        String contents = TestUtils.createXMLConfigText("");
        TestUtils.copyStringToFile(contents, configFile);
        SpringConfigModel model = createConfigModel(configFile);
        try {
            model.runReadAction(new Action<SpringBeans>() {
                public void run(SpringBeans parameter) {
                    throw new RuntimeException();
                }
            });
            fail();
        } catch (RuntimeException e) {
            // OK.
        }
        try {
            model.runDocumentAction(new Action<DocumentAccess>() {
                public void run(DocumentAccess parameter) {
                    throw new RuntimeException();
                }
            });
            fail();
        } catch (RuntimeException e) {
            // OK.
        }
    }

    public void testDocumentAction() throws IOException {
        String contents = TestUtils.createXMLConfigText("");
        TestUtils.copyStringToFile(contents, configFile);
        File configFile2 = createConfigFileName("dispatcher-servlet.xml");
        TestUtils.copyStringToFile(contents, configFile2);
        SpringConfigModel model = createConfigModel(configFile, configFile2);
        final Set<File> invokedForFiles = new HashSet<File>();
        model.runDocumentAction(new Action<DocumentAccess>() {
            public void run(DocumentAccess docAccess) {
                invokedForFiles.add(docAccess.getFile());
            }
        });
        assertEquals(2, invokedForFiles.size());
        assertTrue(invokedForFiles.contains(configFile));
        assertTrue(invokedForFiles.contains(configFile2));
    }
}
