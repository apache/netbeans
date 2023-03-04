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
package org.netbeans.spi.java.hints.support;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.modules.java.hints.spiimpl.TestBase;
import org.openide.LifecycleManager;

/**
 *
 * @author lahvac
 */
public class FixFactoryTest extends TestBase {
    
    public FixFactoryTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SourceUtilsTestUtil.makeScratchDir(this);
    }
    
    public void testInterfaceModifiers() throws Exception {
        prepareTest("test/Test.java", "package test; public interface I { }");
        
        ClassTree i = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
        
        FixFactory.removeModifiersFix(info, TreePath.getPath(info.getCompilationUnit(), i.getModifiers()), EnumSet.of(Modifier.PUBLIC), "").implement();
        
        LifecycleManager.getDefault().saveAll();
        
        assertEquals("package test; interface I { }", info.getFileObject().asText());
    }
}