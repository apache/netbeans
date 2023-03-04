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
package org.netbeans.modules.java.source.parsing;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.spi.Parser;

/**
 *
 * @author Tomas Zezula
 */
public final class MimeTask extends ClasspathInfoTask {

        private final Task<CompilationController> task;
        private final JavaSource js;

        public MimeTask (final JavaSource js,
                         final Task<CompilationController> task,
                         final ClasspathInfo cpInfo) {
            super (cpInfo);
            assert js != null;
            assert task != null;
            this.js = js;
            this.task = task;
        }
        
        @NonNull
        JavaSource getJavaSource() {
            return js;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            Parser.Result result = resultIterator.getParserResult ();
            final CompilationController cc = CompilationController.get(result);
            assert cc != null;
            JavaSourceAccessor.getINSTANCE().setJavaSource(cc, js);
            task.run (cc);
        }
    }
