/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.spi.editor.document;

import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;

/**
 * Interface to create documents. Implementations should be registered in the
 * {@link MimeLookup} for an appropriate MIME type. The system may provide a default
 * implementation for all MIME types.
 * 
 * @author sdedic
 */
public interface DocumentFactory {
    /**
     * Creates a document for the given mime type.
     * @param mimeType the MIME type
     * @return document instance or {@code null}
     */
    @CheckForNull
    public Document createDocument(@NonNull String mimeType);

    /**
     * Returns a {@link Document} for the given {@link FileObject}.
     * @param file the {@link FileObject} to create {@link Document} for
     * @return the document instance or {@code null}
     */
    @CheckForNull
    public Document getDocument(@NonNull FileObject file);

    /**
     * Returns a {@link FileObject} for given {@link Document}.
     * @param document the {@link Document} to find {@link FileObject} for
     * @return the {@link FileObject} or {@code null}
     */
    @CheckForNull
    public FileObject getFileObject(@NonNull Document document);

}
