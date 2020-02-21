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

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;

/**
 * Utility class to help manage folders and files in userdir.
 *
 * Configurations and options are nominally stored in
 * "$userdir/config/<moduleFolderName>".
 * The <moduleFolderName> folder is "statically" created by virtue of
 * being declared in resources/mf-layer.xml.
 */

public final class UserdirFile {

    public interface Reader {
	public void readFrom(InputStream is) throws IOException;
    };

    public interface Writer {
	public void writeTo(OutputStream os)
	    throws IOException, FileStateInvalidException;
    }

    private String moduleFolderName;

    private String folderName;

    private String filename;

    public UserdirFile(String moduleFolderName,
		       String folderName,
		       String filename) {
	this.moduleFolderName = moduleFolderName;
	this.folderName = folderName;
	this.filename = filename;
    } 

    /**
     * name of folder under
     *	"$userdir/config"
     * where this modules data is stored.
     */
    public String moduleFolderName() {
	return moduleFolderName;
    } 

    /**
     * folder under
     *	"$userdir/config/<moduleFolderName>"
     * where our data is stored.
     */
    public String folderName() {
	return folderName;
    } 

    /** 
     * file under
     *	"$userdir/config/<moduleFolderName>/<folderName>"
     * where we store ourselves.
     */
    public String filename() {
	return filename;
    } 

    public String fullPath() {
	final String fullPath = moduleFolderName() +
			        '/' +
			        folderName() +
			        '/' +
			        filename() +
			        "." + // NOI18N
			        "xml"; // NOI18N
	return fullPath;
    }

    public void read(Reader reader) throws IOException {
	final FileObject fo = FileUtil.getConfigFile(fullPath());

	if (fo == null) {
	    // not an error
	    return;
	}

	InputStream inputStream = fo.getInputStream();
	reader.readFrom(inputStream);
    }

    public void write(final Writer writer)
	throws IOException, FileStateInvalidException {

	final FileObject rootFo = FileUtil.getConfigRoot();
        final FileSystem fs = rootFo.getFileSystem();

	final String fullPath = fullPath();

	fs.runAtomicAction(new FileSystem.AtomicAction() {
            @Override
	    public void run() throws IOException {

		FileObject fo = FileUtil.createData(rootFo, fullPath);
		FileLock lock = fo.lock();
		try {
		    OutputStream os = fo.getOutputStream(lock);
		    writer.writeTo(os);
		} finally {
		    lock.releaseLock();
		}
	    }
	} );
    }

    @Override
    public String toString() {
        return fullPath();
    }
}

