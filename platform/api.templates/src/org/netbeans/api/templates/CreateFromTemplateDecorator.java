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
package org.netbeans.api.templates;

import java.io.IOException;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Decorator for templating. The decorator will pre- or post-process the main file creation.
 * Decorators are called by the infrastructure, in the declaration order, before the template
 * file is processed and after it is processed, depending on {@link #isBeforeCreation()}
 * and {@link #isAfterCreation()} return values. A decorator may perform both pre- and post- 
 * processing.
 * <p>
 * First the decorator is asked to {@link #accept} the creation process; if it does not,
 * it will not be invoked at all. Before/after main file creation the {@link #decorate}
 * method is called to perform its magic. Any reported additional files will be returned
 * as part of {@link FileBuilder#build()} result.
 * @since 1.9
 * @author sdedic
 */
public interface CreateFromTemplateDecorator {
    /**
     * Determines if the decorator should be called before template processing. Pre-decorators
     * can setup the environment before the main file creation takes place.
     * @return true, if the decorator should be called before template processing
     */
    public boolean  isBeforeCreation();
    
    /**
     * Determines if the decorator should be called after template processing. Post-decorators
     * can make additional (e.g. settings, registrations) adjustments.
     * @return true, if the decorator should be called after template processing
     */
    public boolean  isAfterCreation();
    
    /** 
     * Determines whether the decorator is willing to participate in creation.
     * If the decorator returns {@code false}, its {@link #decorate} will not be called. It
     * @param desc describes the request that is about to be performed
     * @return true if this decorator wants to participate in files creation
     */
    public abstract boolean accept(CreateDescriptor desc);

    /**
     * Extends the creation process. The decorator may alter the created file (it is the first in the 
     * returned list) or create additional files. In case it creates files, it must return list of the
     * added files.
     * <p>
     * If the decorator is not interested, it should return simply {@code null}.
     * 
     * @param desc command objects that describes the file creation request
     * @param createdFiles  files created so far as part of the template creation
     * @return the newly create file(s)
     * @throws IOException if something goes wrong with I/O
     */
    public @CheckForNull List<FileObject> decorate(
            @NonNull CreateDescriptor    desc,
            @NonNull List<FileObject>    createdFiles
    ) throws IOException;
}
