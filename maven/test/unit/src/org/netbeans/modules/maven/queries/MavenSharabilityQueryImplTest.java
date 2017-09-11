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

package org.netbeans.modules.maven.queries;

import java.io.File;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;
import org.openide.util.test.TestFileUtils;

public class MavenSharabilityQueryImplTest extends NbTestCase {

    public MavenSharabilityQueryImplTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    public void testSharability() throws Exception {
        File prj = new File(getWorkDir(), "prj");
        TestFileUtils.writeFile(new File(prj, "pom.xml"), "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<packaging>jar</packaging><version>1.0</version></project>");
        TestFileUtils.writeFile(new File(prj, "src/main/java/p/C.java"), "package p; class C {}");
        TestFileUtils.writeFile(new File(prj, "target/classes/java/p/C.class"), "whatever");
        TestFileUtils.writeFile(new File(prj, "modules/mod/pom.xml"), "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>mod</artifactId>"
                + "<packaging>jar</packaging><version>1.0</version></project>");
        // XXX check nbactions*.xml
        /* Considered owned by NBM project, so skip this:
        assertSharability(SharabilityQuery.UNKNOWN, "");
         */
        assertSharability(SharabilityQuery.Sharability.MIXED, "prj");
        assertSharability(SharabilityQuery.Sharability.SHARABLE, "prj/src");
        assertSharability(SharabilityQuery.Sharability.SHARABLE, "prj/pom.xml");
        assertSharability(SharabilityQuery.Sharability.NOT_SHARABLE, "prj/target");
        assertSharability(SharabilityQuery.Sharability.UNKNOWN, "prj/stuff.xml");
        assertSharability(SharabilityQuery.Sharability.MIXED, "prj/modules");
        assertSharability(SharabilityQuery.Sharability.MIXED, "prj/modules/mod");
        assertSharability(SharabilityQuery.Sharability.SHARABLE, "prj/modules/mod/pom.xml");
        assertSharability(SharabilityQuery.Sharability.NOT_SHARABLE, "prj/modules/mod/target");
    }
    private void assertSharability(SharabilityQuery.Sharability expected, String path) throws Exception {
        assertEquals(path, expected, SharabilityQuery.getSharability(Utilities.toURI(new File(getWorkDir(), path))));
    }

}
