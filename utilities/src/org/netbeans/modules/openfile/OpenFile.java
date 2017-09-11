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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.openfile;

import java.io.File;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Opens files when requested. Main functionality.
 * @author Jaroslav Tulach, Jesse Glick
 * @author Marian Petras
 */
public final class OpenFile {

    /** do not instantiate */
    private OpenFile() {}

    /**
     * Open a file (object) at the beginning.
     * @param fileObject the file to open
     * @param line 
     * @return error message or null on success
     * @usecase  API
     */
    public static String open(FileObject fileObject, int line) {
        for (OpenFileImpl impl : Lookup.getDefault().lookupAll(OpenFileImpl.class)) {
            if (impl.open(fileObject, line)) {
                return null;
            }
        }
        return NbBundle.getMessage(OpenFile.class, "MSG_FileIsNotPlainFile", fileObject);
    }
    
    /**
     * Opens a file.
     *
     * @param  file  file to open (must exist)
     * @param  line  line number to try to open to (starting at zero),
     *               or <code>-1</code> to ignore
     * @return null on success, otherwise the error message
     * @usecase CallbackImpl, OpenFileAction
     */
    static String openFile(File file, int line) {
        String msg = checkFileExists(file);
        if (msg != null) {
            return msg;
        }
                              
        FileObject fileObject;
        fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if (fileObject != null) {
            return open(fileObject, line);
        }
        return NbBundle.getMessage(OpenFile.class, "MSG_FileDoesNotExist", file);
    }
    
    /**
     * Checks whether the specified file exists.
     * If the file doesn't exists, displays a message.
     * <p>
     * The code for displaying the message is running in a separate thread
     * so that it does not block the current thread.
     *
     * @param  file  file to check for existence
     * @return  null on success, otherwise the error message
     */
    private static String checkFileExists(File file) {
        if (!file.exists() || (!file.isFile() && !file.isDirectory())) {
            return NbBundle.getMessage(OpenFile.class, "MSG_fileNotFound", file.toString());  //NOI18N
        } else {
            return null;
        }
    }
}
