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

package org.netbeans.modules.j2ee.earproject.test;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 */
public class EarTestCase extends NbTestCase {

    private String oldNbUser;

    public EarTestCase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        oldNbUser = System.getProperty("netbeans.user"); // NOI18N
        FileObject root = FileUtil.toFileObject(getWorkDir());
        FileUtil.createFolder(root, "ud/system"); // NOI18N
        System.setProperty("netbeans.user", new File(getWorkDir(), "ud").getAbsolutePath()); // NOI18N
    }

    @Override
    protected void tearDown() throws Exception {
        if (oldNbUser != null) {
            System.setProperty("netbeans.user", oldNbUser); // NOI18N
        }

        super.tearDown();
    }

}
