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

package org.netbeans.modules.profiler.spi.java;

import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Bachorik
 */
public abstract class GoToSourceProvider {
    /**
     * Implementors will provide a specific functionality to open a source code
     * @param project The associated project
     * @param className The class name
     * @param methodName The method name or NULL
     * @param signature The signature or NULL
     * @param line The line number or {@linkplain Integer#MIN_VALUE}
     * @return Returns TRUE if the infrastructure was able to open the source code, FALSE otherwise
     */
    public abstract boolean openSource(Lookup.Provider project, String className, String methodName, String signature, int line);
    
    /**
     * Implementors will provide a specific functionality to open a source code file on a given position
     * @param srcFile The source file to be opened
     * @param offset The position to open the file at
     * @return  Returns TRUE if such file exists and the offset is valid
     */
    public abstract boolean openFile(FileObject srcFile, int offset);
}
