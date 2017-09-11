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
