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

package org.netbeans.test.java.editor.completion;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.spi.editor.completion.CompletionProvider;

/**
 *
 * @author jp159440
 */
public class SmartCCTests extends CompletionTestPerformer{
    
   
    public SmartCCTests(String name) {
        super(name);
    }
    
     
    public void testsmartassign() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "Double x = ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Assign.java", 11,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartassign2() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "Number x = ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Assign.java", 11,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartassign3() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "Number x = new ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Assign.java", 11,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartassign4() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "List x = new ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Assign.java", 11,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartassign5() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "String x = ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Assign.java", 11,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartExtends() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "class A extends ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Types.java", 32,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartImplements() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "class B implements ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Types.java", 32,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartThrows() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "public void method() throws ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Types.java", 32,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartAnnotation() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "@", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Types.java", 32,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartImport() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "import ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Types.java", 21,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartImportStatic() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "import static java.awt.Color.", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Types.java", 21,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartSuperParameter() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "super(", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/SmartCC.java", 31,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartInnerClassAsParameter() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "method(", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/SmartCC.java", 41,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartInnerClassAsParameter2() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "method( new ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/SmartCC.java", 41,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartReturn() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "return ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/SmartCC.java", 41,CompletionProvider.COMPLETION_QUERY_TYPE);                
    }
    
    public void testsmartReturn2() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "return new ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/SmartCC.java", 41,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartCatch() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "catch (", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/SmartCC.java", 51,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(SmartCCTests.class).enableModules(".*").clusters(".*"));
    }
    
}
