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

package org.netbeans.modules.cnd.loaders;

import java.io.IOException;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.filesystems.FileLock;
import org.openide.util.Exceptions;



/** Superclass for Elf objects in the Repository.
 *
 */
public class ExeElfObject extends ExeObject {

    /** Serial version number */
    static final long serialVersionUID = 77972208704093735L;

    public ExeElfObject(FileObject pf, ExeLoader loader)
	throws DataObjectExistsException {
	super(pf, loader);
    }
  
    @Override
    protected Node createNodeDelegate() {
	return new ExeNode(this);
    }

    /**
     *  Renames all entries and changes their files to new ones.
     *  We only override this to prevent you from changing the template
     *  name to something invalid (like an empty name)
     */
    @Override
    protected FileObject handleRename(String name) throws IOException {
        FileLock lock = getPrimaryFile().lock();
        int pos = name.lastIndexOf('.');

        try {
            if (pos <= 0){
                // file without separator
                getPrimaryFile().rename(lock, name, null);
            } else {
		getPrimaryFile().rename(lock, name.substring(0, pos), 
                        name.substring(pos + 1, name.length()));
            }
        } finally {
            lock.releaseLock ();
        }
        return getPrimaryFile ();
    }

    /*
     * Return name with extension so renaming etc works
     */
    @Override
    public String getName() {
	String ename = getPrimaryFile().getNameExt();
	return ename;
    }

    @Override
    protected DataObject handleCopy(DataFolder df) throws IOException {
	// let super do the job and then set the execution flag on the copy
	DataObject dao = super.handleCopy(df);
	setExecutionFlags(dao.getPrimaryFile());
	return dao;
    }

    @Override
    protected FileObject handleMove(DataFolder df) throws IOException {
	// let super do the job and then set the execution flag on the copy
	FileObject fob = super.handleMove(df);
	setExecutionFlags(fob);
	return fob;
    }

    private void setExecutionFlags(FileObject fob) {
	if (fob != null && fob.isValid()) {
            ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(fob);
            String path = CndFileUtils.normalizePath(fob);
            ProcessUtils.execute(env, "/bin/chmod", "755", path); // NOI18N
        }
    }
}

