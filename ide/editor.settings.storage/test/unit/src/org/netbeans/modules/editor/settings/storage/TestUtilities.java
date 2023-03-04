/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
