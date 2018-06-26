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

package org.netbeans.modules.web.webmodule;

import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.api.webmodule.RequestParametersQuery;
import org.netbeans.modules.web.spi.webmodule.RequestParametersQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Pavel Buzek, Andrei Badea
 */
public class RequestParametersQueryTest extends NbTestCase {

    private static final String PARAMS = "MyJsp?foo=1&bar=0";

    public RequestParametersQueryTest(String name) {
        super(name);
    }

    private FileObject datadir;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(RequestParametersQueryImpl.class);
        datadir = FileUtil.toFileObject(getDataDir());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        MockServices.setServices();
    }

    public void testGetParams() throws Exception {
        FileObject foo = datadir.getFileObject("a.foo");
        FileObject bar = datadir.getFileObject("b.bar");
        String params1 = RequestParametersQuery.getFileAndParameters(foo);
        assertNotNull("found params", params1);
        String params2 = RequestParametersQuery.getFileAndParameters(bar);
        assertEquals("different parameters expected", PARAMS, params1);
        assertNull("no params expected", params2);
    }

    public static final class RequestParametersQueryImpl implements RequestParametersQueryImplementation {

        public RequestParametersQueryImpl() {}

        public String getFileAndParameters(FileObject f) {
            if (f.getNameExt().equals("a.foo")) {
                return PARAMS;
            }
            return null;
        }
    }
}
