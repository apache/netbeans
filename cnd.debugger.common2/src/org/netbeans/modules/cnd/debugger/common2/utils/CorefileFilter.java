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

package org.netbeans.modules.cnd.debugger.common2.utils;

import java.io.*;
import java.util.*;
// deprecated in NB65
// import org.netbeans.modules.cnd.MIMENames;
import org.openide.util.NbBundle;

@Deprecated
public class CorefileFilter extends javax.swing.filechooser.FileFilter {
    
    public CorefileFilter() {
	super();
    }
    
    @Override
    public String getDescription() {
	return getString("FileChooser_Corefiles"); // NOI18N
    }
    

    @Override
   public boolean accept(File f) {
	if(f != null) {
	    if(f.isDirectory()) {
		return true;
	    }
	    /* This doesn't work for files outside of mounted filesystems -
	       which makes it kind of useless since the file chooser can
	       point anywhere...
	    FileObject[] foa = FileUtil.fromFile(f);
	    if (foa.length >= 1) {
		String mime = foa[0].getMIMEType();
		if (mime != null &&
		    MIMENames.ELF_CORE_MIME_TYPE.equals(mime)) {
		    return true;
	    */
            if (f.isFile()) {
                return checkElfHeader(f);
            }
	}
	return false;
    }

    /** Check if this file's header represents an elf executable */
    private boolean checkElfHeader(File f) {
        byte b[] = new byte[18];
	int left = 18; // bytes left to read
	int offset = 0; // offset into b array
	InputStream is = null;
	try {
	    is = new FileInputStream(f);
	    while (left > 0) {
		int n = is.read(b, offset, left);
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

	short e_type;
	if (b[5] == 1) {  // ELFDATA2LSB=1
	    // byte order on i386
	    e_type = (short) b[17];
	    e_type <<= 8;
	    e_type += (short) b[16];
	} else {
	    e_type = (short) b[16];
	    e_type <<= 8;
	    e_type += (short) b[17];
	}
	/*
	System.out.println("b[0] was " + b[0]);
	System.out.println("b[1] was " + b[1]);
	System.out.println("b[2] was " + b[2]);
	System.out.println("b[3] was " + b[3]);
	System.out.println("b[5] was " + b[5]);
	System.out.println("b[16] was " + b[16]);
	System.out.println("b[17] was " + b[17]);
	System.out.println("etype was " + e_type);
	*/
	if (
	    // Elf header	    
	    (b[0] == 0x7f) && (b[1] == (byte) 'E') &&
	    (b[2] == (byte) 'L') && (b[3] == (byte) 'F') &&
	    // Executable
	    (e_type == 4) // ET_CORE=4
	    ) {
	    return true;
	}
	return false;
    }

    /** Look up i18n strings here */
    private ResourceBundle bundle;
    private String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(CorefileFilter.class);
	}
	return bundle.getString(s);
    }
}
