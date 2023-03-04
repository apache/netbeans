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

package org.netbeans.modules.options.classic;

import java.lang.reflect.Modifier;
import org.netbeans.junit.AssertionFailedErrorException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.options.classic.SettingChildren.FileStateProperty;

/**
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class FileStatePropertyTest extends NbTestCase {

    public FileStatePropertyTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(FileStatePropertyTest.class));
    }
    
    /** This test assures compatibility with Jelly library.
     * Please contact QA or any JellyTools developer in case of failure.
     */    
    public void testJellyCompatibility() {
        try {
            assertTrue("SettingChildren class is public", Modifier.isPublic(SettingChildren.class.getModifiers()));
            assertTrue("FileStateProperty class is public", Modifier.isPublic(FileStateProperty.class.getModifiers()));
            try {
                new FileStateProperty("Modules-Layer").getValue();
            } catch (NullPointerException npe) {}
        } catch (Exception e) {
            throw new AssertionFailedErrorException("JellyTools compatibility conflict, please contact QA or any JellyTools developer.", e);
        }
    }
    
    
}
