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

package org.netbeans.modules.cnd.utils.filters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.FileAndFileObjectFilter;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public class MacOSXExecutableFileFilter extends FileAndFileObjectFilter {

    private static MacOSXExecutableFileFilter instance = null;

    public MacOSXExecutableFileFilter() {
	super();
    }

    public static MacOSXExecutableFileFilter getInstance() {
	if (instance == null) {
            instance = new MacOSXExecutableFileFilter();
        }
	return instance;
    }
    
    @Override
    public String getDescription() {
        return NbBundle.getMessage(MacOSXExecutableFileFilter.class, "FILECHOOSER_MACHOEXECUTABLE_FILEFILTER"); // NOI18N
    }
    
    @Override
    protected boolean mimeAccept(File f) {
        return checkHeader(f);
    }

    @Override
    protected boolean mimeAccept(FileObject f) {
        return checkHeader(f);
    }

    /** Check if this file's header represents an elf executable */
    private boolean checkHeader(Object f) {
        CndUtils.assertTrue((f instanceof File) || (f instanceof FileObject));
        byte bytes[] = new byte[18];
	int left = 18; // bytes left to read
	int offset = 0; // offset into b array
	InputStream is = null;
	try {
            if (f instanceof File) {
                is = new FileInputStream((File) f);
            } else { // (f instanceof FileObject)
                is = CndFileUtils.getInputStream((FileObject) f, left);
            }
	    while (left > 0) {
		int n = is.read(bytes, offset, left);
		if (n <= 0) {
		    // File isn't big enough to be an elf file...
		    return false;
		}
		offset += n;
		left -= n;
	    }
	} catch (Exception e) {
	    return false;
	} finally {
	    if (is != null) {
		try {
		    is.close();
		} catch (IOException e) {
		}
	    }
	}
//    <file>
//	<!-- Mac Power PC peff executable -->
//	<!--          J o y ! p e f f     -->
//	<magic   hex="4a6f792170656666"
//	        mask="ffffffffffffffff"/>
//	<!-- Next 4 bytes contains architecture type:
//             "pwpc" for the PowerPC CFM
//             "m68k" for CFM-68K
//             Ignore architecture. -->
//        <resolver mime="application/x-exe"/>
//    </file>
        if (bytes[0] == 'J' && bytes[1] == 'o' && bytes[2] == 'y' && bytes[3] == '!' && bytes[4] == 'p' && bytes[5] == 'e' && bytes[6] == 'f' && bytes[7] == 'f') {
            return true;
        }
//
//    <file>
//	<!-- Mach-O executable i386 -->
//	<!--                  v       v       v       v-->
//	<magic   hex="cefaedfe0000000000000000020000000000"
//	        mask="ffffffff0000000000000000ff0000000000"/>
//        <resolver mime="application/x-exe"/>
//    </file>
        if (bytes[0] == (byte)0xce && bytes[1] == (byte)0xfa && bytes[2] == (byte)0xed && bytes[3] == (byte)0xfe && bytes[12] == 2) {
            return true;
        }
//
//    <file>
//	<!-- Mach-O executable x86-64 -->
//	<!--                  v       v       v       v-->
//	<magic   hex="cffaedfe0000000000000000020000000000"
//	        mask="ffffffff0000000000000000ff0000000000"/>
//        <resolver mime="application/x-exe"/>
//    </file>
        if (bytes[0] == (byte)0xcf && bytes[1] == (byte)0xfa && bytes[2] == (byte)0xed && bytes[3] == (byte)0xfe){
            return true;
        }
//
//    <file>
//	<!-- Mach-O executable ppc -->
//	<!--                  v       v       v       v-->
//	<magic   hex="feedface0000000000000000000000020000"
//	        mask="ffffffff0000000000000000000000ff0000"/>
//        <resolver mime="application/x-exe"/>
//    </file>
        if (bytes[0] == (byte)0xfe && bytes[1] == (byte)0xed && bytes[2] == (byte)0xfa && bytes[3] == (byte)0xce){
            return true;
        }
//
//    <file>
//	<!-- Mach-O universal binary with 2 architectures-->
//        <!-- FIXUP: this mask matches too many files and doesn't
//                    check for right architecture -->
//	<!--                  v       v       v       v-->
//	<magic   hex="cafebabe0000000000000000000000000000"
//	        mask="ffffffff0000000000000000000000000000"/>
//        <resolver mime="application/x-exe"/>
//    </file>
        if (bytes[0] == (byte)0xca && bytes[1] == (byte)0xfe && bytes[2] == (byte)0xba && bytes[3] == (byte)0xbe){
            return true;
        }
        return false;
    }

    @Override
    protected String[] getSuffixes() {
        return new String[]{};
    }
}
