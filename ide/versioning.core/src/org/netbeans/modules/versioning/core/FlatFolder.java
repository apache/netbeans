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
package org.netbeans.modules.versioning.core;

import java.io.File;
import java.net.URI;

/**
 * Marker class, specifies that the folder is NOT recursive for actions that operate on it.
 * 
 * @author Maros Sandor
 */
public final class FlatFolder extends File {

    private static final long serialVersionUID = 1L;

    public FlatFolder(String pathname) {
        super(pathname);
    }

    public FlatFolder(URI uri) {
        super(uri);
    }

    public FlatFolder(File parent, String child) {
        super(parent, child);
    }

    public FlatFolder(String parent, String child) {
        super(parent, child);
    }
}
