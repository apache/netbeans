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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.templates.CreateDescriptor;
import org.openide.filesystems.FileObject;

/** This is an interface for <q>smart templating</q> that allows
 * any module to intercept calls to {@link DataObject#createFromTemplate} 
 * and handle them themselves. The NetBeans IDE provides default
 * implementation that allows use of Freemarker templating engine.
 * Read more in the <a href="@TOP@/architecture-summary.html#script">howto document</a>.
 * <p/>
 * This SPI is now <b>deprecated</b> and serves just a backward compatilibity SPI adapter
 * which allows the template API to work with legacy handlers. The templating SPI is delegated
 * to the original handler methods.
 * 
 * @author Jaroslav Tulach
 * @since 6.1
 * @deprecated in 7.59. Use {@link org.netbeans.api.templates.CreateFromTemplateHandler} instead.
 */
@Deprecated
public abstract class CreateFromTemplateHandler extends org.netbeans.api.templates.CreateFromTemplateHandler {
    
    @Override
    public boolean accept(CreateDescriptor desc) {
        return accept(desc.getTemplate());
    }

    @Override
    protected List<FileObject> createFromTemplate(CreateDescriptor desc) throws IOException {
        return Collections.singletonList(
                createFromTemplate(
                        desc.getTemplate(),
                        desc.getTarget(),
                        desc.getName(),
                        desc.getParameters()
                )
        );
    }
    
    /** Method that allows a handler to reject a file. If all handlers
     * reject a file, regular processing defined in {@link DataObject#handleCreateFromTemplate}
     * is going to take place.
     * 
     * @param orig the file of the template
     * @return true if this handler wants to handle the createFromTemplate operation
     */
    protected abstract boolean accept(FileObject orig);
    
    /** Handles the creation of new file. 
     * @param orig the source file 
     * @param f the folder to create a file in
     * @param name the name of new file to create in the folder (see {@link #FREE_FILE_EXTENSION} regarding extension)
     * @param parameters map of additional arguments as specified by registered {@link CreateFromTemplateAttributesProvider}s
     * @return the newly create file
     * @throws IOException if something goes wrong with I/O
     */
    protected abstract FileObject createFromTemplate(
        FileObject orig,
        FileObject f, 
        String name,
        Map<String,Object> parameters
    ) throws IOException;

    /**
     * Parameter to enable free file extension mode.
     * By default, the extension of the newly created file will be inherited
     * from the template. But if {@link #createFromTemplate} is called with this
     * parameter set to {@link Boolean#TRUE}
     * (such as from {@link DataObject#createFromTemplate(DataFolder,String,Map)}),
     * and the file name already seems to
     * include an extension (<samp>*.*</samp>), the handler should not append
     * any extension from the template.
     * @since org.openide.loaders 7.16
     * @see <a href="@org-netbeans-modules-projectuiapi@/org/netbeans/spi/project/ui/templates/support/Templates.SimpleTargetChooserBuilder.html#freeFileExtension()"><code>Templates.SimpleTargetChooserBuilder.freeFileExtension</code></a>
     */
    public static final String FREE_FILE_EXTENSION = "freeFileExtension"; // NOI18N
    
}
