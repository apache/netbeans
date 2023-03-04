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

package org.netbeans.spi.editor.codegen;

import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.openide.util.Lookup;

/**
 * Serves for adding an additonal content to the context which is passed
 * as a parameter to the {@link CodeGenerator.Factory#create(org.openide.util.Lookup)}
 * method.<br> Instances of this interface are looked up by the
 * {@link org.netbeans.api.editor.mimelookup.MimeLookup} so they should be
 * registered in an xml-layer in
 * <i>Editors/&lt;mime-type&gt;/CodeGeneratorContextProviders</i> directory.
 *
 * @author Dusan Balek
 * @since 1.8
 */
@MimeLocation(subfolderName="CodeGeneratorContextProviders")
public interface CodeGeneratorContextProvider {

    /**
     * Adds an additional content to the original context and runs the given task
     * with the new context as a parameter.
     * @param context the original context
     * @param task the task to be run
     * @since 1.8
     */
    public void runTaskWithinContext(Lookup context, Task task);
    
    /**
     * Represents the task passed to the {@link #runTaskWithinContext(org.openide.util.Lookup,Task)}
     * method. It is supposed to be implemented by the infrastructure implementor
     * (not by the SPI clients).
     * @since 1.8
     */
    public interface Task {
        
        /**
         * Runs the task.
         * @param context the task's context
         * @since 1.8
         */
        public void run(Lookup context);
    }
}
