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

package org.netbeans.modules.cnd.makefile.utils;

import java.io.File;
import java.io.IOException;
import javax.swing.filechooser.FileSystemView;
import org.netbeans.modules.cnd.utils.CndPathUtilities;

/**
 *  Replace the default FileSystemView with one which understands tilde and
 *  environment variable expansion. This class should be used to replace the
 *  default FileSystemview in all of our FileChoosers.
 */
public class IpeFileSystemView extends FileSystemView {

    /** The original FileSystemView */
    FileSystemView fsv;


    /**
     *  Save the original FileSystemView. Because of the way swing works its
     *  probably not our superclass.
     */
    public IpeFileSystemView(FileSystemView fsv) {
	this.fsv = fsv;
    }


    /**
     *  Creates a JDK File object from the filename. In our case, we may change
     *  the original filename if it contains either ~ or $.
     */
    @Override
    public File createFileObject(String path) {
	return new File(CndPathUtilities.expandPath(path));
    }


    /**
     *  Creates a JDK File object from the filename in the given directory. In
     *  our case, we may change the original filename if it contains either
     *  ~ or $.
     */
    @Override
    public File createFileObject(File dir, String path) {
	String newPath = CndPathUtilities.expandPath(path);
	if (dir == null) {
	    return new File(newPath);
	} else {
	    return new File(dir, newPath);
	}
    }

    
    /**
     *  Expand the pathname if there are any '~' or '$' characters in it.
     *
     *  @param path The original path name
     *  @return	    The possibly expanded path name
    private String expandPath(String path) {
	int idx = 0;
	int dol;
	String end;

	if (path.charAt(0) == '~') {
	    if (path.length() == 1 || path.charAt(1) == '/') {
		newPath.append(System.getProperty("user.home"));	// NOI18N
		idx = 1;
	    } else {
		end = path.indexOf('/');
		// XXX - Replace with JNI lookup!!!
		if (end > 0) {
		    newPath.append("/home/");
		    newPath.append(path.substring(1, end));
		    idx = end;
		} else {
		    newPath.append("/home");
		    newPath.append(path.substring(1));
		    idx = path.length();
		}
	    }
	}

	while (idx < path.length()) {
	    var = null;
	    dol = path.indexOf('$', idx);
	    if (dol >= 0) {
		if (env == null
		newPath.append(path.substring(idx), dol);
		if (path.length() > (dol + 2) && path.charAt(dol + 1) == '{' &&
			    (end = path.indexOf(dol, '}')) > 0) {
		    var = path.substring(dol + 2, end1);
		    idx = end1 + 1;
		} else if ((end = path.indexOf('/', idx)) != -1) {
		    var = path.substring(dol + 1, end);
		} else if ((end = path.indexOf('.', idx)) != -1) {
		    var = path.substring(dol + 1, end);
		}
	    } else {
		newPath.append(path.substring(idx));
		idx = path.length();
	    }
    }
     */


    /** Tells if a file is the root directory */
    @Override
    public boolean isRoot(File f) {
	return fsv.isRoot(f);
    }


    /** Creates a new folder with a default folder name */
    public File createNewFolder(File containingDir) throws IOException {
	return fsv.createNewFolder(containingDir);
    }


    /** Tells if the file is hidden or not */
    @Override
    public boolean isHiddenFile(File f) {
	return fsv.isHiddenFile(f);
    }


    /** Return the root partitions on this system */
    @Override
    public File[] getRoots() {
	return fsv.getRoots();
    }
}
