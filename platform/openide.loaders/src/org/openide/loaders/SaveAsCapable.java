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
package org.openide.loaders;

import java.io.IOException;
import org.openide.filesystems.FileObject;


/**
 * Save document under a different file name and/or extension.
 * 
 * The default implementation is available in {@link org.openide.text.DataEditorSupport}. So if your
 * editor support inherits from <code>DataEditorSupport</code> you can implement "Save As" feature
 * for your documents by adding the following lines into your {@link DataObject}'s constructor:
 * 
<code><pre>
        getCookieSet().assign(SaveAsCapable.class, new SaveAsCapable() {
            public void saveAs(FileObject folder, String fileName) throws IOException {
                getDataEditorSupport().saveAs( folder, fileName );
            }
        });
</pre></code>
 *
 * If you have {@link Node}, you may use the following code:
 * 
<code><pre>
    public class MyNode extends AbstractNode {
        
        public MyNode() {
            getCookieSet().assign(SaveAsCapable.class, new MySaveAsCapable());
            ...
        }

        private class MySaveAsCapable implements SaveAsCapable {
            public void saveAs(FileObject folder, String fileName) throws IOException {
                FileObject newFile = folder.getFileObject(fileName);

                if (newFile == null) {
                    newFile = FileUtil.createData(folder, fileName);
                }
                OutputStream output = newFile.getOutputStream();
                InputStream input = ... // get your input stream
                
                try {
                    byte[] buffer = new byte[4096];

                    while (input.available() > 0) {
                        output.write(buffer, 0, input.read(buffer));
                    }
                }
                finally {
                    if (input != null) {
                        input.close();
                    }
                    if (output != null) {
                        output.close();
                    }
                }
            }
        }
    }
</pre></code>
 *
 * @since 6.3
 * @author S. Aubrecht
 */
public interface SaveAsCapable {
    /** 
     * Invoke the save operation.
     * @param folder Folder to save to.
     * @param name New file name to save to.
     * @throws IOException if the object could not be saved
     */
    void saveAs( FileObject folder, String name ) throws IOException;
}
