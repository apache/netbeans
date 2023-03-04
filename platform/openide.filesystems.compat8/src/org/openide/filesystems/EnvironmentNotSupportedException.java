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

package org.openide.filesystems;

import java.io.IOException;

/** Exception thrown to signal that external
* execution and compilation is not supported on a given filesystem.
*
* @author Jaroslav Tulach
* @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
*/
@Deprecated
public class EnvironmentNotSupportedException extends IOException {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -1138390681913514558L;

    /** the throwing exception */
    private FileSystem fs;

    /**
    * @param fs filesystem that caused the error
    */
    public EnvironmentNotSupportedException(FileSystem fs) {
        this.fs = fs;
        assert false : "Deprecated.";
    }

    /**
    * @param fs filesystem that caused the error
    * @param reason text description for the error
    */
    public EnvironmentNotSupportedException(FileSystem fs, String reason) {
        super(reason);
        this.fs = fs;
        assert false : "Deprecated.";
    }

    /** Getter for the filesystem that does not support environment operations.
    */
    public FileSystem getFileSystem() {
        return fs;
    }
}
