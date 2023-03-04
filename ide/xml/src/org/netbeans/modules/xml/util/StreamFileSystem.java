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
package org.netbeans.modules.xml.util;

import org.netbeans.modules.xml.lib.*;
import org.openide.filesystems.*;

/**
 * It represents r/o filesystem given by an InputStream.
 * Such filesystem contains just one StreamFileObject.
 *
 * @author  Petr Kuzel
 * @version untested draft
 */
class StreamFileSystem extends FileSystem {

    /** Serial Version UID */
    private static final long serialVersionUID =2822790916118072639L;

    private FileObject root;

    // == StreamFileObject

    /** Creates new StreamFileSystem */
    public StreamFileSystem(StreamFileObject root) {
        this.root = root;
    }

    public org.openide.filesystems.FileObject getRoot() {
        return root;
    }
    
    public org.openide.filesystems.FileObject findResource(java.lang.String str) {
        return null;
    }
    
    public org.openide.util.actions.SystemAction[] getActions() {
        return new org.openide.util.actions.SystemAction[0];
    }
    
    public boolean isReadOnly() {
        return true;
    }
    
    public java.lang.String getDisplayName() {
        return Util.THIS.getString (
                StreamFileSystem.class, "PROP_StreamFileSystem");
    }
    
}
