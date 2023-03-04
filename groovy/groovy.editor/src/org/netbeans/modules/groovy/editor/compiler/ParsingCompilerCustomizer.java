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
package org.netbeans.modules.groovy.editor.compiler;

import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 * Customizes the Groovy Compiler for parsing purposes. Implementations may add some processors,
 * transformations or disable global library transformations, or revert their effects.
 * 
 * Note: this is not yet an API class, but may become one when declarative registration (@link SimpleTransformationCustomizer} 
 * is not sufficient.
 * 
 * @author sdedic
 */
@MimeLocation(subfolderName = "Parser")
public interface ParsingCompilerCustomizer {
    
    /**
     * Decorates initial {@link CompilerConfig} before the {@link CompilationUnit} is created. Some initialization
     * may happen early in the constructor, like fetching global transformations. The method may return a different instance
     * of CompilerConfiguration.
     * 
     * @param ctx parsing context
     * @param cfg configuration to customize
     * @return new configuration object
     */
    public CompilerConfiguration configureParsingCompiler(Context ctx, CompilerConfiguration cfg);
    
    /**
     * Decorates a compilation after the CompilationUnit is constructed and before the parsing is done. Transformers added here
     * follow the standard ones.
     * @param ctx parsing context
     * @param cu compilation unit instance
     * @return decorated compilation unit
     */
    public void decorateCompilation(Context ctx, CompilationUnit cu);
    
    /**
     * Context for the parsing task.
     */
    public final class Context {
        private final Snapshot snapshot;
        private Task consumerTask;

        /**
         * Constructs the context. 
         * @param snap Snapshot to be parsed.
         * @param consumerTask task that is going to consume the parsing result. 
         */
        public Context(Snapshot snap, Task consumerTask) {
            this.snapshot = snap;
            this.consumerTask = consumerTask;
        }

        /**
         * Returns the CompilationUnit that should be parsed. Use it to extract the URI
         * to the source.
         * 
         * @return the originating CompilationUnit.
         */
        public Snapshot getSnapshot() {
            return snapshot;
        }

        /**
         * Returns the task that is about to consume the parser's results.
         * @return 
         */
        public Task getConsumerTask() {
            return consumerTask;
        }

        /**
         * Sets the task that is about to consume the parser's results.
         */
        public void setConsumerTask(Task consumerTask) {
            this.consumerTask = consumerTask;
        }
    }
}
