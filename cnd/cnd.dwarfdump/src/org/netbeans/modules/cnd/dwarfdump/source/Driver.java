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
package org.netbeans.modules.cnd.dwarfdump.source;

import java.util.List;
import java.util.ListIterator;

/**
 *
 */
public interface Driver {
    /**
     * Framework include directory ends with. For example: predefined system
     * framework "/Library/Frameworks" path or path included by
     * "-F/Library/Frameworks" will be represented in by string
     * "/Library/Frameworks/{framework}". For example code model will resolve
     * directive with "/": #include <GLUT/glut.h>
     * in the folder the "/System/Library/Frameworks/GLUT.framework/Headers".
     */
    public static final String FRAMEWORK = "/{framework}"; //NOI18N
    
    /**
     * Change root of system search paths.
     * For example: Predefined system framework "/Library/Frameworks" with option
     * -isysroot /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX10.11.sdk
     * will fits
     * /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX10.11.sdk/Library/Frameworks
     */
    public static final String ISYSROOT_FLAG = "-isysroot"; // NOI18N

    List<String> splitCommandLine(String line, CompileLineOrigin isScriptOutput);

    Artifacts gatherCompilerLine(String line, CompileLineOrigin isScriptOutput, boolean isCpp);

    Artifacts gatherCompilerLine(ListIterator<String> st, CompileLineOrigin isScriptOutput, boolean isCpp);
    
}
