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

package org.netbeans.modules.editor.settings.storage;

import java.io.IOException;
import java.io.OutputStream;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Vita Stejskal
 */
public final class TestUtilities {
    
    /** Creates a new instance of TestUtilities */
    private TestUtilities() {
    }

    // no delay
    
    public static FileObject createFile(String path) throws IOException {
        return createFO(path, false, null, 0);
    }
    
    public static FileObject createFile(String path, String contents) throws IOException {
        return createFO(path, false, contents, 0);
    }
    
    public static FileObject createFolder(String path) throws IOException {
        return createFO(path, true, null, 0);
    }

    // delay
    
    public static FileObject createFile(String path, long delay) throws IOException {
        return createFO(path, false, null, delay);
    }
    
    public static FileObject createFile(String path, String contents, long delay) throws IOException {
        return createFO(path, false, contents, delay);
    }
    
    public static FileObject createFolder(String path, long delay) throws IOException {
        return createFO(path, true, null, delay);
    }
    
    
    private static FileObject createFO(final String path, final boolean folder, final String contents, long delay) throws IOException {
        final FileObject [] createdFo = new FileObject[1];
        FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject fo = FileUtil.getConfigRoot();
                String [] pathElements = path.split("/", -1);
                for (int i = 0; i < pathElements.length; i++ ) {
                    String elementName = pathElements[i];

                    if (elementName.length() == 0) {
                        continue;
                    }

                    FileObject f = fo.getFileObject(elementName);
                    if (f != null && f.isValid()) {
                        fo = f;
                    } else {
                        if (i + 1 < pathElements.length || folder) {
                            fo = fo.createFolder(elementName);
                        } else {
                            // The last element in the path should be a file
                            fo = fo.createData(elementName);
                            if (contents != null) {
                                OutputStream os = fo.getOutputStream();
                                try {
                                    os.write(contents.getBytes());
                                } finally {
                                    os.close();
                                }
                            }
                        }
                    }
                }
                createdFo[0] = fo;
            }
        });
        
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ie) {
                // ignore
            }
        }
        
        return createdFo[0];
    }

    public static void delete(String path) throws IOException {
        delete(path, 0);
    }
    
    public static void delete(String path, long delay) throws IOException {
        FileObject fo = FileUtil.getConfigFile(path);
        if (fo != null) {
            fo.delete();
        }
        
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ie) {
                // ignore
            }
        }
    }
    
}
