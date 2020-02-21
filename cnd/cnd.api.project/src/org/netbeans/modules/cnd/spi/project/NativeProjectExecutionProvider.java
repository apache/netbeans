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
package org.netbeans.modules.cnd.spi.project;

import java.io.IOException;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectSupport.NativeExitStatus;

/**
 *
 */
public interface NativeProjectExecutionProvider {
    /**
     * constant to register services
     */
    public static String PATH = "CND/NativeProjectExecutionProvider"; // NOI18N

    /**
     * Execute a command from user's PATH in the context of the native project
     *
     * @param executable Executable name (not path)
     * @param env Additional environment variables
     * @param args Arguments
     * @return NativeExitStatus status if executed, null if can not execute (allow to pass to the next provider)
     */
    NativeExitStatus execute(NativeProject project, final String executable, final String[] env, final String... args) throws IOException;

    /**
     * Return the name of the development platform (Solaris-x86, Solaris-sparc,
     * MacOSX, Windows, Linux-x86)
     *
     * @return development platform name, null if can not execute (allow to pass to the next provider)
     */
    String getPlatformName(NativeProject project);

}
