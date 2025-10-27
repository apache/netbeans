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

package org.netbeans.modules.masterfs.filebasedfs.naming;


import java.io.File;
import java.io.IOException;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;

/**
 * @author Radek Matous
 */
public interface FileNaming {
    String getName();

    FileNaming getParent();

    boolean isRoot();

    File getFile();

    //not to touch disk by getFile().isFile()...
    boolean isFile();
    boolean isDirectory();

    ID getId();

    FileNaming rename(String name, ProvidedExtensions.IOHandler handler) throws IOException;
    
    
    /// FileObject ID as identity object.
    /// FileName and FileInfo objects may hold a strong reference to it, but
    /// don't let it escape, since FileObjectFactory uses it in a WeakHashMap to track
    /// the BaseFileObj lifecycle.
    public record ID(int value) {}
    
}
