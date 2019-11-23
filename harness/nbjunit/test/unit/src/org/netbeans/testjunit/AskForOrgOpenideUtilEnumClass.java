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
package org.netbeans.testjunit;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.*;
import junit.framework.TestCase;

/** This class needs to be in other package than
 * org.netbeans.junit in order to not be loaded by the application
 * classloader but by the started NetBeans IDE
 * 
 * @author Jaroslav Tulach
 */
public class AskForOrgOpenideUtilEnumClass extends TestCase {

    public AskForOrgOpenideUtilEnumClass(String t) {
        super(t);
    }

    public void testOne() {
        try {
            ClassLoader l = AskForOrgOpenideUtilEnumClass.class.getClassLoader();
            if (l == NbTestCase.class.getClassLoader()) {
                fail("This test shall not be loaded by the same classloader!");
            }
            Class<?> access = Class.forName("org.openide.util.enum.ArrayEnumeration");
            System.setProperty("en.one", "OK");
        } catch (Exception ex) {
            Logger.getLogger("testOne").log(Level.INFO, ex.getMessage(), ex);
        }
    }
}
