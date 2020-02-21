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

