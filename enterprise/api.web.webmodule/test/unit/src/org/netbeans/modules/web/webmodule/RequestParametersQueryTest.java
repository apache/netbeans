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
