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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import org.openide.filesystems.FileObject;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.utils.MIMENames;

/**
 *  Recognizes EXE files (Windows, Linux, and Solaris executables, shared objects and
 *  core files).
 */
public class ExeLoader extends UniFileLoader {

    /** Serial version number */
    static final long serialVersionUID = -602486606840357846L;

    /** Single depth cache of last MIME type */
    private static String lastMime;
    
    /** Single depth cache of FileObjects */
    private static Reference<FileObject> lastFo;

    private static final String KNOWN_EXEFILE_TYPE =
	    "org.netbeans.modules.cnd.ExeLoader.KNOWN_EXEFILE_TYPE"; // NOI18N

    public ExeLoader() {
	super("org.netbeans.modules.cnd.loaders.ExeObject"); // NOI18N
    }

    public ExeLoader(String representationClassName) {
	super(representationClassName);
    }
    
    @Override
    protected String actionsContext() {
        return "Loaders/application/x-executable+elf/Actions/"; // NOI18N
    }

    /** set the default display name */
    @Override
    protected String defaultDisplayName() {
	return NbBundle.getMessage(ExeLoader.class, "PROP_ExeLoader_Name"); // NOI18N
    }

    @Override
    protected FileObject findPrimaryFile(FileObject fo) {
	String mime;

	if (fo.isFolder()) {
	    return null;
	}

//	Object o = fo.getAttribute(KNOWN_EXEFILE_TYPE);
//	if (o != null) {
//	    mime = o.toString();
//	} else {
	    mime = fo.getMIMEType();
	    if (MIMENames.ELF_GENERIC_MIME_TYPE.equals(mime)) {
		// Fallback matching. We shouldn't see this anymore.
		String name = fo.getNameExt();
		if (name.equals("core")) { // NOI18N
		    mime = MIMENames.ELF_CORE_MIME_TYPE;
		} else if (name.indexOf(".so.") >= 0) { // NOI18N
		    mime = MIMENames.ELF_SHOBJ_MIME_TYPE;
		} else {
		    mime = MIMENames.ELF_EXE_MIME_TYPE;
		}
	    }
//	}

	if (MIMENames.EXE_MIME_TYPE.equals(mime) ||
                    MIMENames.ELF_EXE_MIME_TYPE.equals(mime) ||
		    MIMENames.ELF_CORE_MIME_TYPE.equals(mime) ||
		    MIMENames.ELF_SHOBJ_MIME_TYPE.equals(mime) ||
		    MIMENames.ELF_STOBJ_MIME_TYPE.equals(mime) ||
		    MIMENames.ELF_OBJECT_MIME_TYPE.equals(mime)) {
	    lastMime = mime;
	    lastFo = new WeakReference<FileObject>(fo);

//	    try {
//		fo.setAttribute(KNOWN_EXEFILE_TYPE, mime);
//	    } catch (IOException ex) {
//		// We've figured out the mime type, which is the main thing this
//		// method needed to do. Its much less important that we couldn't
//		// save it. So we just ignore the exception!
//	    }

	    return fo;
	} else {
	    return null;
	}
    }
    
    @Override
    protected MultiDataObject createMultiObject(FileObject primaryFile)
			throws DataObjectExistsException, IOException {
	String mime;
        FileObject last = lastFo.get();
	if (primaryFile.equals(last)) {
	    mime = lastMime;
	} else {
	    mime = primaryFile.getMIMEType();
	}

	if (mime.equals(MIMENames.EXE_MIME_TYPE)) {
	    return new ExeObject(primaryFile, this);
	} else if (mime.equals(MIMENames.ELF_EXE_MIME_TYPE)) {
	    return new ExeElfObject(primaryFile, this);
	} else if (mime.equals(MIMENames.ELF_CORE_MIME_TYPE)) {
	    return new CoreElfObject(primaryFile, this);
	} else if (mime.equals(MIMENames.ELF_SHOBJ_MIME_TYPE)) {
	    return new DllObject(primaryFile, this);
	} else if (mime.equals(MIMENames.ELF_STOBJ_MIME_TYPE)) {
	    return new StaticLibraryObject(primaryFile, this);
	} else {
	    return new OrphanedElfObject(primaryFile, this);
	}
    }
}
