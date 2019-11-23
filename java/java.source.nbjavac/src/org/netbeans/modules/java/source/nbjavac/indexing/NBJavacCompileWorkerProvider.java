/**
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
package org.netbeans.modules.java.source.nbjavac.indexing;

import java.util.List;
import org.netbeans.modules.java.source.indexing.CompileWorker;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileTuple;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileWorkerProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=JavaCustomIndexer.CompileWorkerProvider.class, position=100)
public class NBJavacCompileWorkerProvider implements CompileWorkerProvider {

    private static final int TRESHOLD = 500;

    @Override
    public CompileWorker[] getWorkers(List<CompileTuple> toCompile) {
        return new CompileWorker[] {
            toCompile.size() < TRESHOLD ? new SuperOnePassCompileWorker() : new OnePassCompileWorker(),
            new MultiPassCompileWorker()
        };
    }

}
