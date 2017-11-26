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
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;

/** This is an interface for <i>smart templating</i> that allows
 * any module to intercept calls to 
 * {@link FileBuilder#build()}
 * and handle them themselves. The NetBeans IDE provides default
 * implementation that allows use of Freemarker templating engine.
 * Read more in the <a href="@TOP@/architecture-summary.html#script">howto document</a>.
 * <p>
 * An implementation of CreateHandler should honor {@link CreateDescriptor#hasFreeExtension()} and
 * {@link CreateDescriptor#isPreformatted()}.
 * 
 * @author Jaroslav Tulach
 * @author Svatopluk Dedic
 */
public abstract class CreateFromTemplateHandler {
    /** Method that allows a handler to reject a file. If all handlers
     * reject a file, regular processing defined in {@link FileBuilder#createFromTemplate(org.openide.filesystems.FileObject, org.openide.filesystems.FileObject, java.lang.String, java.util.Map, org.netbeans.api.templates.FileBuilder.Mode)}
     * is going to take place.
     * 
     * @param desc describes the request that is about to be performed
     * @return true if this handler wants to handle the createFromTemplate operation
     */
    protected abstract boolean accept(CreateDescriptor desc);
    
    /** Handles the creation of new files. The Handler may create one or more files. The 
     * files should be ordered so that the "important" file (i.e. the one which is then presented
     * to the user etc) is ordered first in the list.
     * 
     * @param desc command objects that describes the file creation request
     * @return the newly create file
     * @throws IOException if something goes wrong with I/O
     */
    protected abstract @NonNull List<FileObject> createFromTemplate(
            CreateDescriptor    desc
    ) throws IOException;

}
