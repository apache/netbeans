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
package org.netbeans.modules.java.source.compat8;

import java.lang.reflect.Method;
import junit.framework.Test;
import junit.framework.TestCase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.junit.NbModuleSuite;
import org.openide.text.PositionRef;

/**
 */
public class ModificationResult8Test extends TestCase {
    
    public ModificationResult8Test(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        return NbModuleSuite.createConfiguration(ModificationResult8Test.class).
            gui(false).suite();
    }
    
    public void testGetStartPosition() throws Exception {
        Class<?> c = ModificationResult.Difference.class;
        Class<?> r = findMethod(c, "getStartPosition").getReturnType();
        assertEquals(PositionRef.class, r);
    }

    public void testGetEndPosition() throws Exception {
        Class<?> c = ModificationResult.Difference.class;
        Class<?> r = findMethod(c, "getEndPosition").getReturnType();
        assertEquals(PositionRef.class, r);
    }
    
    private static Method findMethod(Class<?> c, String n) throws Exception {
        for (;;) {
            Method m = c.getMethod(n);
            if (m.getReturnType() == PositionRef.class) {
                return m;
            }
            c = c.getSuperclass();
        }
    }
}
