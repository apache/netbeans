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

package org.netbeans.modules.junit.plugin;

//import java.util.Collections;
//import java.util.EnumSet;
import java.awt.EventQueue;
import java.util.Map;
//import java.util.Set;
//import javax.lang.model.element.Element;
//import javax.lang.model.element.ElementKind;
//import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.junit.JUnitPluginTrampoline;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin;

/**
 * SPI for custom implementations of support for JUnit.
 * It declares methods for:
 * <ul>
 *     <li>navigation between source classes and corresponding test classes
 *         ({@link #getTestLocation getTestLocation},
 *          {@link #getTestedLocation getTestedLocation})</li>
 *     <li>creation of test class skeletons
 *         ({@link #createTests createTests})</li>
 * </ul>
 *
 * @author  Marian Petras
 */
public abstract class JUnitPlugin extends CommonPlugin {
    
    static {
        JUnitPluginTrampoline.DEFAULT = new JUnitPluginTrampoline() {
            public boolean createTestActionCalled(JUnitPlugin plugin,
                                                  FileObject[] filesToTest) {
                return plugin.createTestActionCalled(filesToTest);
            }
            @Override
            public FileObject[] createTests(
                    JUnitPlugin plugin,
                    FileObject[] filesToTest,
                    FileObject targetRoot,
                    Map<CreateTestParam,Object> params) {
                return plugin.createTests(filesToTest, targetRoot, params);
            }
            @Override
            public Location getTestLocation(
                    JUnitPlugin plugin,
                    Location sourceLocation) {
                return plugin.getTestLocation(sourceLocation);
            }
            @Override
            public Location getTestedLocation(
                    JUnitPlugin plugin,
                    Location testLocation) {
                return plugin.getTestedLocation(testLocation);
            }
            @Override
            public boolean canCreateTests(
                    JUnitPlugin plugin,
                    FileObject... fileObjects) {
                return plugin.canCreateTests(fileObjects);
            }
        };
    }
    

}
