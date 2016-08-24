/**
 * *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************
 */
package org.jetbrains.kotlin.resolve.lang.java2;

import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TypeMirrorHandle;

/**
 *
 * @author Alexander.Baratynski
 */
public class TypeSearchers {
    
    public static class TypeNameSearcher implements Task<CompilationController> {

        private final TypeMirrorHandle handle;
        private String name = null;
        
        public TypeNameSearcher(TypeMirrorHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeMirror type = handle.resolve(info);
            if (type == null) {
                return;
            }
            
            name = type.toString();
        }
        
        public String getName() {
            return name;
        }
        
    }
    
}
