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

package org.netbeans.api.java.classpath;

/**
 * Java related classpath constants.
 * 
 * @author Jan Lahoda
 * @since 1.22
 */
public class JavaClassPathConstants {

    /**
     * ClassPath for annotation processors. If undefined, {@link ClassPath#COMPILE}
     * should be used.
     * <p class="nonnormative">
     * It corresponds to the <code>-processorpath</code> option of <code>javac</code>.
     * </p>
     *
     * @since 1.22
     */
    public static final String PROCESSOR_PATH = "classpath/processor";  //NOI18N
    
    /**
     * A part of the compilation classpath which is not included into runtime classpath.
     * @since 1.39
     */
    public static final String COMPILE_ONLY = "classpath/compile_only"; //NOI18N

    /**
     * Module path for bootstrap modules.
     * @since 1.64
     */
    public static final String MODULE_BOOT_PATH = "modules/boot";   //NOI18N
    /**
     * Module path for user modules.
     * @since 1.64
     */
    public static final String MODULE_COMPILE_PATH = "modules/compile"; //NOI18N
    /**
     * Additional classpath for modular compilation.
     * @since 1.64
     */
    public static final String MODULE_CLASS_PATH = "modules/classpath"; //NOI18N
    
    /**
     * Runtime module path for user modules.
     * @since 1.64
     */
    public static final String MODULE_EXECUTE_PATH="modules/execute";   //NOI18N
    
    /**
     * Runtime additional classpath for modular compilation.
     * @since 1.64
     */
    public static final String MODULE_EXECUTE_CLASS_PATH = "modules/execute-classpath"; //NOI18N

    /**
     * Module source path.
     * @since 1.65
     */
    public static final String MODULE_SOURCE_PATH ="modules/source";    //NOI18N

    /**
     * Module processor path.
     * @since 1.66
     */
    public static final String MODULE_PROCESSOR_PATH ="modules/processor";    //NOI18N
}
