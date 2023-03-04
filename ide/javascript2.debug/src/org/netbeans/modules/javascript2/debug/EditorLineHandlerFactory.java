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
package org.netbeans.modules.javascript2.debug;

import java.net.URL;
import org.netbeans.api.annotations.common.CheckForNull;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Martin
 */
public abstract class EditorLineHandlerFactory {
    
    protected EditorLineHandlerFactory() {}
    
    @CheckForNull
    public abstract EditorLineHandler get(FileObject fo, int line);
    
    public abstract EditorLineHandler get(URL url, int line);
    
    private static EditorLineHandlerFactory getDefault() {
        return Lookup.getDefault().lookup(EditorLineHandlerFactory.class);
    }
    
    @CheckForNull
    public static EditorLineHandler getHandler(FileObject fo, int line) {
        return getDefault().get(fo, line);
    }
    
    public static EditorLineHandler getHandler(URL url, int line) {
        return getDefault().get(url, line);
    }
}
