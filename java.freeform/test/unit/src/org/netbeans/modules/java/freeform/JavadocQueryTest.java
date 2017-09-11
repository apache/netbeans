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

package org.netbeans.modules.java.freeform;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.modules.ant.freeform.TestBase;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Tests Javadoc reporting.
 * @author Jesse Glick
 */
public class JavadocQueryTest extends TestBase {

    public JavadocQueryTest(String name) {
        super(name);
    }

    private URL classes1Dir, classes1Jar, classes2Dir, javadoc1Dir, javadoc2Zip;
    
    protected void setUp() throws Exception {
        super.setUp();
        classes1Dir = asDir("classes1");
        classes1Jar = asJar("classes1.jar");
        classes2Dir = asDir("classes2");
        javadoc1Dir = asDir("javadoc1");
        javadoc2Zip = asJar("javadoc2.zip");
    }
    
    private URL asDir(String path) throws Exception {
        URL u = Utilities.toURI(simple2.helper().resolveFile(path)).toURL();
        String us = u.toExternalForm();
        if (us.endsWith("/")) {
            return u;
        } else {
            return new URL(us + "/");
        }
    }
    
    private URL asJar(String path) throws Exception {
        return FileUtil.getArchiveRoot(Utilities.toURI(simple2.helper().resolveFile(path)).toURL());
    }
    
    private List<URL> javadocFor(URL binary) {
        return Arrays.asList(JavadocForBinaryQuery.findJavadoc(binary).getRoots());
    }
    
    public void testFindJavadoc() throws Exception {
        List<URL> both = Arrays.asList(new URL[] {javadoc1Dir, javadoc2Zip});
        assertEquals("both Javadoc found for " + classes1Dir, both, javadocFor(classes1Dir));
        assertEquals("both Javadoc found for " + classes1Jar, both, javadocFor(classes1Jar));
        assertEquals("no Javadoc found for " + classes2Dir, Collections.EMPTY_LIST, javadocFor(classes2Dir));
    }

    // XXX testChangeFiring?
    
}
