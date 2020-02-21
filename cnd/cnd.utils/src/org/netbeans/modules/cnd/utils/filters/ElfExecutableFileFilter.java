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
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.FileAndFileObjectFilter;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public class ElfExecutableFileFilter extends FileAndFileObjectFilter {

    private static ElfExecutableFileFilter instance = null;

    public ElfExecutableFileFilter() {
	super();
    }

    public static ElfExecutableFileFilter getInstance() {
	if (instance == null) {
            instance = new ElfExecutableFileFilter();
        }
	return instance;
    }
    
    @Override
    public String getDescription() {
        return NbBundle.getMessage(ElfExecutableFileFilter.class, "FILECHOOSER_ELFEXECUTABLE_FILEFILTER"); // NOI18N
    }
    
    @Override
    protected boolean mimeAccept(File f) {
        FileObject fo = CndFileSystemProvider.toFileObject(f);
	return (fo == null) ? checkElfHeader(f) : checkElfHeader(fo);
    }

    @Override
    protected boolean mimeAccept(FileObject f) {
        return checkElfHeader(f);
    }


    /** Check if this file's header represents an elf executable */
    private boolean checkElfHeader(Object f) {
        CndUtils.assertTrue((f instanceof File) || (f instanceof FileObject));
        byte b[] = new byte[18];
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

    @Override
    protected String[] getSuffixes() {
        return new String[]{};
    }
}
