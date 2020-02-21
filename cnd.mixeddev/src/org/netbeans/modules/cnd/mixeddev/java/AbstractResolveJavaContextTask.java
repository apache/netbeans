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
package org.netbeans.modules.cnd.mixeddev.java;

import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;

/**
 *
 */
public abstract class AbstractResolveJavaContextTask<T> implements ResolveJavaContextTask<T> {
        
    protected final int offset;
    
    protected T result;
    
    public AbstractResolveJavaContextTask(int offset) {
        this(offset, null);
    }

    public AbstractResolveJavaContextTask(int offset, T defaultResult) {
        this.offset = offset;
        this.result = defaultResult;
    }
    
    @Override
    public boolean hasResult() {
        return result != null;
    }
    
    @Override
    public T getResult() {
        return result;
    }
    
    @Override
    public void cancel() {
        // Do nothing
    }

    @Override
    public final void run(CompilationController controller) throws Exception {
        if (controller == null || controller.toPhase(JavaSource.Phase.RESOLVED).compareTo(JavaSource.Phase.RESOLVED) < 0) {
            return;
        }
        // Look for current element
        TreePath tp = controller.getTreeUtilities().pathFor(offset);
        if (tp != null) {
            resolve(controller, tp);
        }
    }
    
    protected abstract void resolve(CompilationController controller, TreePath tp);
}
