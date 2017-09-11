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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.csl.api;

import java.util.Collections;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.filesystems.FileObject;

/**
 * Based on org.netbeans.modules.gsfpath.api.source by Tomas Zezula
 *
 * @author Tor Norbye
 */
public interface ElementHandle {
    /** 
     * Return the FileObject associated with this handle, or null
     * if the file is unknown or in a parse tree (in which case the
     * file object is the same as the file object in the CompilationInfo
     * for the root of the parse tree.
     */
    @CheckForNull
    FileObject getFileObject();
    
    /**
     * The mime type associated with this element. This is typically
     * used to identify the type of element in embedded scenarios.
     */
    @CheckForNull
    String getMimeType();

    @NonNull
    String getName();

    @CheckForNull
    String getIn();

    @NonNull
    ElementKind getKind();

    @NonNull
    Set<Modifier> getModifiers();
    
    /** 
     * Tests if the handle has the same signature as the parameter.
     * @param handle to be checked
     * @return true if the handles refer to elements with the same signature
     */
    boolean signatureEquals (@NonNull final ElementHandle handle);

    OffsetRange getOffsetRange(@NonNull ParserResult result);

    /** 
     * A special handle which holds URL. Can be used to handle documentation
     * requests etc.
     */
    public static class UrlHandle implements ElementHandle {
        private String url;

        public UrlHandle(@NonNull String url) {
            this.url = url;
        }

        public FileObject getFileObject() {
            return null;
        }
        
        public String getMimeType() {
            return null;
        }

        public boolean signatureEquals(ElementHandle handle) {
            if (handle instanceof UrlHandle) {
                return url.equals(((UrlHandle)handle).url);
            }
            
            return false;
        }

        public OffsetRange getOffsetRange(@NonNull ParserResult result) {
            return null;
        }

        @NonNull
        public String getUrl() {
            return url;
        }

        public String getName() {
            return url;
        }

        public String getIn() {
            return null;
        }

        public ElementKind getKind() {
            return ElementKind.OTHER;
        }

        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }
    }
}
