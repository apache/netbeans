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
