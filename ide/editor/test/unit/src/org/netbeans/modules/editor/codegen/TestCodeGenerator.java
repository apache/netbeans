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

package org.netbeans.modules.editor.codegen;

import java.util.Collections;
import java.util.List;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.netbeans.spi.editor.codegen.CodeGeneratorContextProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Dusan Balek
 */
public class TestCodeGenerator implements CodeGenerator {
    
    private boolean b;
    
    public TestCodeGenerator(boolean b) {
        this.b = b;
    }
    
    public String getDisplayName() {
        return b ? "CodeGenerator" : "SimpleCodeGenerator";
    }

    public void invoke() {
    }

    public static class Factory implements CodeGenerator.Factory {

        public List<? extends CodeGenerator> create(Lookup context) {
            Object o = context.lookup(ContextProvider.class);
            return Collections.singletonList(new TestCodeGenerator(o != null));            
        }        
    }

    public static class ContextProvider implements CodeGeneratorContextProvider {

        public void runTaskWithinContext(Lookup context, Task task) {
            task.run(new ProxyLookup(context, Lookups.singleton(this)));
        }
    }
}
