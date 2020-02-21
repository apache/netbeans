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

package org.netbeans.modules.cnd.debugger.common2.utils;

import java.io.*;
@Deprecated
public class ExecutableFileFilter extends javax.swing.filechooser.FileFilter {
	
    public ExecutableFileFilter() {
	super();
    }
    
    @Override
    public String getDescription() {
	return Catalog.get("FileChooser_Exefiles");	// NOI18N
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
		    MIMENames.ELF_EXE_MIME_TYPE.equals(mime)) {
		    return true;
	    */
	    return checkElfHeader(f);
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
	    (e_type == 2) // ET_EXEC=2
	    ) {
	    return true;
	}
	return false;
    }
    
}
