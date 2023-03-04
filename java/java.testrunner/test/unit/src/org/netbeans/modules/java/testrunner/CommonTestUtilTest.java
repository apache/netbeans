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
package org.netbeans.modules.java.testrunner;

import java.io.File;
import java.io.IOException;
import org.junit.*;
import static org.junit.Assert.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Theofanis Oikonomou
 */
public class CommonTestUtilTest {
    
    private CommonTestUtil util;
    
    public CommonTestUtilTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        util = new CommonTestUtil();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of isJavaFile method, of class CommonTestUtil.
     */
    @Test
    public void testIsJavaFile() {
        try {
            FileObject fileObj = FileUtil.createData(FileUtil.getConfigRoot(), "tmpFile.java");
            assertTrue(CommonTestUtil.isJavaFile(fileObj));
            fileObj = FileUtil.createData(FileUtil.getConfigRoot(), "tmpFile.jav");
            assertFalse(CommonTestUtil.isJavaFile(fileObj));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
