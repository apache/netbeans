/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
