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
package org.netbeans.modules.profiler.nbimpl.javac;

import java.util.Set;
import org.netbeans.modules.profiler.api.java.ProfilerTypeUtils;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.api.java.SourceMethodInfo;
import org.netbeans.modules.profiler.nbimpl.BaseProjectTest;

/**
 *
 * @author Jaroslav Bachorik
 */
public class JavacClassInfoTest extends BaseProjectTest {    
    public JavacClassInfoTest(String name) {
        super(name);
    }
        
    @Override
    public void setUp() throws Exception{
        super.setUp(); 
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testInnerClasses() {
        System.out.println("testInnerClasses");
        Set<SourceClassInfo> cs = ProfilerTypeUtils.resolveClass("classinfo.ClassInfoTest", getProject()).getInnerClases();
        assertNotNull(cs);
        assertEquals(4, cs.size());
    }
    
    public void testMethods() {
        System.out.println("testMethods");
        Set<SourceMethodInfo> ms = ProfilerTypeUtils.resolveClass("classinfo.ClassInfoTest", getProject()).getMethods(true);
        assertNotNull(ms);
        assertEquals(3, ms.size());
    }

    public void testMethodsNoSource() {
        System.out.println("testMethods /no source/");
        Set<SourceMethodInfo> ms = ProfilerTypeUtils.resolveClass("sun.org.mozilla.javascript.internal.Callable", getProject()).getMethods(true);
        assertNotNull(ms);
        assertEquals(1, ms.size());
    }
    
    public void testSubclassesNoSource() {
        System.out.println("testSubclasses /no source/");
        // Disabling due to #209056; will enable once that problem is fixed
        
//        Set<SourceClassInfo> cs = ProfilerTypeUtils.resolveClass("sun.org.mozilla.javascript.internal.Callable", getProject()).getSubclasses();
//        assertNotNull(cs);
//        assertEquals(1, cs.size());
    }
    
    public void testSubclasses() {
        System.out.println("testSubclasses");
        Set<SourceClassInfo> cs = ProfilerTypeUtils.resolveClass("classinfo.ClassInfoTest", getProject()).getSubclasses();
        assertNotNull(cs);
        assertEquals(1, cs.size());
    }
    
    public void testInterfaces() {
        System.out.println("testInterfaces");
        Set<SourceClassInfo> cs = ProfilerTypeUtils.resolveClass("classinfo.ClassInfoTest", getProject()).getInterfaces();
        assertNotNull(cs);
        assertEquals(2, cs.size());
    }
    
    public void testSupertype() {
        System.out.println("testSupertype");
        SourceClassInfo superType = ProfilerTypeUtils.resolveClass("classinfo.ClassInfoTest", getProject());
        SourceClassInfo sci = ProfilerTypeUtils.resolveClass("classinfo.ClassInfoTest$StaticInner", getProject()).getSuperType();
        assertNotNull(sci);
        assertEquals(superType, sci);
    }
}
