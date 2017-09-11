/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc.
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
