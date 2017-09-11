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

package org.apache.tools.ant.module.api.support;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class AntScriptUtilsTest extends NbTestCase {

    public AntScriptUtilsTest(String name) {
        super(name);
    }

    private FileObject d;

    protected @Override void setUp() throws Exception {
        super.setUp();
        d = FileUtil.toFileObject(new File(getDataDir(), "antscriptutils"));
        assertNotNull(d);
    }

    public void testGetAntScriptName() throws Exception {
        assertEquals("correct name for test1.xml", "test1", AntScriptUtils.getAntScriptName(d.getFileObject("test1.xml")));
        assertEquals("no name for test2.xml", null, AntScriptUtils.getAntScriptName(d.getFileObject("test2.xml")));
        assertEquals("correct name for test3.xml", "test3", AntScriptUtils.getAntScriptName(d.getFileObject("test3.xml")));
        assertEquals("no name for test5.xml", null, AntScriptUtils.getAntScriptName(d.getFileObject("test5.xml")));
    }

    public void testGetAntScriptTargetNames() throws Exception {
        assertEquals("correct targets for test1.xml",
            Arrays.asList(new String[] {"another", "main", "other"}),
            AntScriptUtils.getCallableTargetNames(d.getFileObject("test1.xml")));
        assertEquals("correct targets for test2.xml",
            Collections.singletonList("sometarget"),
            AntScriptUtils.getCallableTargetNames(d.getFileObject("test2.xml")));
        assertEquals("correct targets for test3.xml",
            Arrays.asList(new String[] {"imported1", "imported2", "main"}),
            AntScriptUtils.getCallableTargetNames(d.getFileObject("test3.xml")));
        try {
            AntScriptUtils.getCallableTargetNames(d.getFileObject("test5.xml"));
            fail();
        } catch (IOException x) {/*OK*/}
    }

}
