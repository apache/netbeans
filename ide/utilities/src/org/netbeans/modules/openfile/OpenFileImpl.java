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

package org.netbeans.modules.openfile;

import org.openide.filesystems.FileObject;

/**
 * Interface for Open File implementations.
 *
 * @author  Marian Petras
 */
public interface OpenFileImpl {

    /**
     * Opens the specified <code>FileObject</code>.
     *
     * @param  fileObject  file to open
     * @param  line    line number to try to open to (starting at zero),
     *                 or <code>-1</code> to ignore
     * @return true on success, false on failure
     */
    boolean open(FileObject fileObject, int line);

}
