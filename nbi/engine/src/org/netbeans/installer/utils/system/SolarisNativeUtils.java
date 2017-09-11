/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils.system;

import java.io.IOException;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.Platform;
/**
 *
 * @author Kirill Sorokin
 */
public class SolarisNativeUtils extends UnixNativeUtils {
    
    public static final String LIBRARY_PATH_SOLARIS_SPARC =
            NATIVE_JNILIB_RESOURCE_SUFFIX +
            "solaris-sparc/" + //NOI18N
            "solaris-sparc.so"; //NOI18N
    
    public static final String LIBRARY_PATH_SOLARIS_SPARCV9 =
            NATIVE_JNILIB_RESOURCE_SUFFIX +
            "solaris-sparc/" + //NOI18N
            "solaris-sparcv9.so"; //NOI18N
    
    public static final String LIBRARY_PATH_SOLARIS_X86 =
            NATIVE_JNILIB_RESOURCE_SUFFIX +
            "solaris-x86/" + //NOI18N
            "solaris-x86.so"; // NOI18N
    
    public static final String LIBRARY_PATH_SOLARIS_X64 =
            NATIVE_JNILIB_RESOURCE_SUFFIX +
            "solaris-x86/" + //NOI18N
            "solaris-amd64.so"; // NOI18N

    public static final String[] POSSIBLE_BROWSER_LOCATIONS_SOLARIS = new String[]{
        "/usr/sfw/lib/firefox/firefox",
        "/opt/csw/bin/firefox",
        "/usr/sfw/lib/mozilla/mozilla",
        "/opt/csw/bin/mozilla",
        "/usr/dt/bin/sun_netscape",
        "/usr/bin/firefox",
        "/usr/bin/mozilla-firefox",
        "/usr/local/firefox/firefox",
        "/opt/bin/firefox",
        "/usr/bin/mozilla",
        "/usr/local/mozilla/mozilla",
        "/opt/bin/mozilla"
    };
    
    private static final String[] FORBIDDEN_DELETING_FILES_SOLARIS = {};
    
    SolarisNativeUtils() {
        String library = null;
        
        if(System.getProperty("os.arch").contains("sparc")) {
            library = SystemUtils.isCurrentJava64Bit() ? 
                LIBRARY_PATH_SOLARIS_SPARCV9 : 
                LIBRARY_PATH_SOLARIS_SPARC;
        } else {
            library = SystemUtils.isCurrentJava64Bit() ? 
                LIBRARY_PATH_SOLARIS_X64 : 
                LIBRARY_PATH_SOLARIS_X86;
        }
        
        loadLibrary(library);
        initializeForbiddenFiles(FORBIDDEN_DELETING_FILES_SOLARIS);
    }
    @Override
    protected Platform getPlatform() {        
        final String osArch = System.getProperty("os.arch");
        return osArch.contains("sparc") ? 
            Platform.SOLARIS_SPARC : 
            Platform.SOLARIS_X86;
    }
    @Override
    protected String [] getPossibleBrowserLocations() {
        return POSSIBLE_BROWSER_LOCATIONS_SOLARIS;
    }
}
