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
