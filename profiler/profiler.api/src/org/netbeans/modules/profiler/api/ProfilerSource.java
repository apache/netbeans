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
package org.netbeans.modules.profiler.api;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * IDE agnostic source file wrapper<br/>
 * A simple {@linkplain Lookup.Provider} derived from the appropriate {@linkplain MimeLookup} registrations.
 * @author Jaroslav Bachorik
 */
public abstract class ProfilerSource implements Lookup.Provider {
    private FileObject file;
    
    protected ProfilerSource(FileObject file) {
        this.file = file;
    }
    
    /**
     * The wrapped file
     * @return Returns the {@linkplain FileObject|} representing a particular {@linkplain ProfilerSource}
     */
    public final FileObject getFile() {
        return file;
    }

    @Override
    public final Lookup getLookup() {
        return MimeLookup.getLookup(file.getMIMEType());
    }
    
    /**
     * Indicates whether a source can be run by the IDE or not
     * @return Returns <b>TRUE</b> if the source can be run by the IDE (eg. main class, test etc.), <b>FALSE</b> otherwise
     */
    public abstract boolean isRunnable();
}
