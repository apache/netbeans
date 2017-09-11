/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomas Zezula
 */
public final class DeletedIndexable implements IndexableImpl, FileObjectProvider {

    private static final Logger LOG = Logger.getLogger(DeletedIndexable.class.getName());

    private final URL root;
    private final String relativePath;

    public DeletedIndexable (final URL root, final String relativePath) {
        assert root != null : "root must not be null"; //NOI18N
        assert relativePath != null : "relativePath must not be null"; //NOI18N
        assert relativePath.length() == 0 || relativePath.charAt(0) != '/'; //NOI18N
        this.root = root;
        this.relativePath = relativePath;
    }

    @Override
    public String getRelativePath() {
        return relativePath;
    }

    @Override
    public URL getURL() {
        try {
            
            return Util.resolveUrl(
                    root,
                    relativePath,
                    relativePath.isEmpty() || relativePath.charAt(relativePath.length()-1) == '/'?
                        null : Boolean.FALSE);
        } catch (MalformedURLException ex) {
            LOG.log(Level.WARNING, null, ex);
            return null;
        }
    }

    @Override
    public String getMimeType() {
        throw new UnsupportedOperationException("Mimetype related operations are not supported by DeletedIndexable"); //NOI18N
    }

    @Override
    public boolean isTypeOf(String mimeType) {
        throw new UnsupportedOperationException("Mimetype related operations are not supported by DeletedIndexable"); //NOI18N
    }

    @Override
    public FileObject getFileObject() {
        return null;
    }

    @Override
    @org.netbeans.api.annotations.common.SuppressWarnings(
    value="DMI_BLOCKING_METHODS_ON_URL",
    justification="URLs have never host part")
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DeletedIndexable other = (DeletedIndexable) obj;
        if (this.root != other.root && (this.root == null || !this.root.equals(other.root))) {
            return false;
        }
        if (this.relativePath != other.relativePath && (this.relativePath == null || !this.relativePath.equals(other.relativePath))) {
            return false;
        }
        return true;
    }

    @Override
    @org.netbeans.api.annotations.common.SuppressWarnings(
    value="DMI_BLOCKING_METHODS_ON_URL",
    justification="URLs have never host part")
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.root != null ? this.root.hashCode() : 0);
        hash = 83 * hash + (this.relativePath != null ? this.relativePath.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "DeletedIndexable@" + Integer.toHexString(System.identityHashCode(this)) + " [" + getURL() + "]"; //NOI18N
    }

}
