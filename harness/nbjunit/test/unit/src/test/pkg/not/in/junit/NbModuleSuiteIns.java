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

package test.pkg.not.in.junit;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.netbeans.insane.scanner.ObjectMap;
import org.netbeans.insane.scanner.Visitor;

public class NbModuleSuiteIns extends TestCase implements Visitor {
    private static Logger LOG = Logger.getLogger("test.logger");

    public NbModuleSuiteIns(String t) {
        super(t);
    }

    public void testOne() {
        try {
            Class<?> access = Class.forName("org.netbeans.insane.model.Support");
            System.setProperty("ins.one", "OK");
        } catch (Exception ex) {
            LOG.log(Level.INFO, "Error loading class", ex);
        }
    }

    public void testFS() {
        try {
            ClassLoader l = NbModuleSuiteIns.class.getClassLoader();
            Class<?> access = l.loadClass("org.openide.filesystems.FileSystem");
            System.setProperty("ins.fs", "OK");
        } catch (Exception ex) {
            LOG.log(Level.INFO, "Error loading class", ex);
        }
    }

    public void testWindowSystem() {
        try {
            ClassLoader l = NbModuleSuiteIns.class.getClassLoader();
            Class<?> access = l.loadClass("org.netbeans.api.java.platform.JavaPlatform");
            System.setProperty("ins.java", "OK");
        } catch (Exception ex) {
            LOG.log(Level.INFO, "Error loading class", ex);
        }
    }

    public void testSecond() {
        System.setProperty("ins.two", "OK");
    }

    public void testThree() {
        System.setProperty("ins.three", "OK");
    }

    public void visitClass(Class cls) {
    }

    public void visitObject(ObjectMap map, Object object) {
    }

    public void visitObjectReference(ObjectMap map, Object from, Object to, Field ref) {
    }

    public void visitArrayReference(ObjectMap map, Object from, Object to, int index) {
    }

    public void visitStaticReference(ObjectMap map, Object to, Field ref) {
    }
}
