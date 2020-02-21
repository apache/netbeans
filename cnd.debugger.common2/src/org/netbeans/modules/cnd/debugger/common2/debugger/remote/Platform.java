/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.debugger.common2.debugger.remote;

public enum Platform {

//  name()		 variant()      variant64       isLinux    isSolaris

    Linux_x86		("intel-Linux",	"amd64-Linux",	true, 	   false), // NOI18N
    Solaris_x86		("intel-S2",	"amd64-S2",	false,     true ), // NOI18N
    Solaris_Sparc	("sparc-S2",	"sparcv9-S2",	false,     true ), // NOI18N
    MacOSX_x86		("intel-MacOSX","",		false,     false), // NOI18N
    Windows_x86		("intel-Windows","",		false,     false), // NOI18N
    Unknown		("",		"",		false,     false); // NOI18N

    private String variant;
    private String variant64;
    private boolean isLinux;
    private boolean isSolaris;

    private Platform (String variant, String variant64, 
			boolean isLinux, boolean isSolaris) {
	this.variant = variant;
	this.variant64 = variant64;
	this.isLinux = isLinux;
	this.isSolaris = isSolaris;
    }

    public static Platform byName(String name) {
	if (name == null)
	    return Unknown;
	try {
	    return valueOf(Platform.class, name);
	} catch (IllegalArgumentException x) {
	    return Unknown;
	}
    }

    /*8
     * Return the Platform corresponding to the machine we're running on
     */
    public static Platform local() {
	String os_name = System.getProperty("os.name", "");	// NOI18N
	String os_arch = System.getProperty("os.arch", "");	// NOI18N

	String platform_arch = "";

	if (os_arch.equals("sparc"))		// NOI18N
	    platform_arch = "Sparc";		// NOI18N
	else if (os_arch.equals("sparcv9"))	// NOI18N
	    platform_arch = "Sparc";		// NOI18N
	else if (os_arch.equals("x86"))		// NOI18N
	    platform_arch = "x86";		// NOI18N
	else if (os_arch.equals("i386"))	// NOI18N
	    platform_arch = "x86";		// NOI18N
        else if (os_arch.equals("x86_64"))	// NOI18N
	    platform_arch = "x86";		// NOI18N
	else if (os_arch.equals("amd64"))	// NOI18N
	    platform_arch = "x86";		// NOI18N
	else
	    assert false :
		String.format("Unrecognized os.arch '%s'", os_arch);// NOI18N

	String platform_os = "";
	if (os_name.equals("SunOS"))		// NOI18N
	    platform_os="Solaris";		// NOI18N
	else if (os_name.equals("Linux"))	// NOI18N
	    platform_os="Linux";		// NOI18N
	else if (os_name.equals("Mac OS X"))	// NOI18N
	    platform_os="MacOSX";		// NOI18N
	else if (os_name.startsWith("Windows"))	// NOI18N
	    platform_os="Windows";		// NOI18N
	else
	    assert false :
		String.format("Unrecognized os.name '%s'", os_name);// NOI18N

	return Platform.byName(platform_os + "_" + platform_arch); // NOI18N
    }

    /*
     * only apply to local
     */
//    public boolean is64() {
//            String uname = null;
//            try {
//                Runtime rt = Runtime.getRuntime();
//                String[] args = new String[2];
//                args[0] = "/bin/uname"; // NOI18N
//                args[1] = "-m"; // NOI18N
//
//                Process proc = rt.exec(args);
//                InputStream procIn = proc.getInputStream();
//                BufferedReader br = new BufferedReader(new InputStreamReader(procIn));
//                uname = br.readLine();
//                br.close();
//            } catch (Exception e) {
//                ErrorManager.getDefault().annotate(e, "Failed to /bin/uname -m "); // NOI18N
//                ErrorManager.getDefault().notify(e);
//                return false;
//            }
//            if (uname == null)
//                return false;
//
//            int found = uname.indexOf("64"); // NOI18N
//            if (found == -1)
//                return false;
//            else
//                return true;
//    }

    public boolean isSolaris() {
	return isSolaris;
    }

    public boolean isLinux() {
	return isLinux;
    }

    public String variant() {
	return variant;
    }

    public String variant64() {
	return variant64;
    }
}
