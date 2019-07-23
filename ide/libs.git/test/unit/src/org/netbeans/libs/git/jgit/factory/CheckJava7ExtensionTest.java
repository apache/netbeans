/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.libs.git.jgit.factory;

import java.io.IOException;
import java.lang.reflect.Field;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.FS.FSFactory;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;

/**
 *
 * @author ondra
 */
public class CheckJava7ExtensionTest extends AbstractGitTestCase {

    public CheckJava7ExtensionTest (String name) throws IOException {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testExtension () throws Exception {
        // ping the factory
        FS fs = FS.DETECTED;
        Field f = FS.class.getDeclaredField("factory");
        f.setAccessible(true);
        FSFactory fact = (FSFactory) f.get(FS.class);
        assertEquals("org.eclipse.jgit.util.Java7FSFactory", fact.getClass().getName());
    }
    
}
