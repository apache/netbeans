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
package org.netbeans.modules.php.editor.elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.Set;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.php.project.api.PhpSourcePath.FileType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * @author Radek Matous
 */
public abstract class PhpElementImpl implements PhpElement {

    static enum Separator {
        SEMICOLON(";"), //NOI18N
        COMMA(","), //NOI18N
        COLON(":"), //NOI18N
        PIPE(Type.SEPARATOR);

        public static EnumSet<Separator> toEnumSet() {
            return EnumSet.allOf(Separator.class);
        }

        private final String value;

        private Separator(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private static final String CLUSTER_URL = "cluster:"; // NOI18N
    private static String clusterUrl = null;
    private final String name;
    private final String in;
    private final String fileUrl;
    private final int offset;
    private final ElementQuery elementQuery;
    //@GuardedBy("this")
    private FileObject fileObject;
    private final boolean isDeprecated;

    public static PhpElementImpl create(final String variableName, final String in, final int offset, final FileObject fo, final PhpElementKind kind) {
        return new PhpElementImpl(variableName, in, null, offset, null, false) {

            @Override
            public String getSignature() {
                return ""; //NOI18N
            }

            @Override
            public PhpElementKind getPhpElementKind() {
                return kind;
            }

            @Override
            public synchronized FileObject getFileObject() {
                return fo;
            }
        };
    }


    PhpElementImpl(final String name, final String in, final String fileUrl, final int offset, final ElementQuery elementQuery, boolean isDeprecated) {
        this.name = name;
        this.in = in;
        this.fileUrl = fileUrl == null ? "" : fileUrl;
        this.offset = offset;
        if (fileUrl != null && fileUrl.contains(" ")) { //NOI18N
            throw new IllegalArgumentException("fileURL may not contain spaces!"); //NOI18N
        }
        this.elementQuery = elementQuery;
        this.isDeprecated = isDeprecated;
    }

    @Override
    public final String getFilenameUrl() {
        return fileUrl;
    }

    @Override
    public PhpModifiers getPhpModifiers() {
        return PhpModifiers.noModifiers();
    }

    @Override
    public final int getOffset() {
        return offset;
    }

    @Override
    public final Set<Modifier> getModifiers() {
        return getPhpModifiers().toModifiers();
    }

    @Override
    public OffsetRange getOffsetRange(ParserResult result) {
        return new OffsetRange(offset, offset + getName().length());
    }

    @Override
    public ElementQuery getElementQuery() {
        return elementQuery;
    }

    @Override
    public synchronized FileObject getFileObject() {
        String urlStr = fileUrl;
        if ((fileObject == null) && StringUtils.hasText(fileUrl)) {
            fileObject = resolveFileObject(urlStr);
        }
        return fileObject;
    }

    public synchronized void setFileObject(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    public static FileObject resolveFileObject(final String urlStr) {
        String url = urlStr;
        if (url.startsWith(CLUSTER_URL)) {
            clusterUrl = getClusterUrl();
            url = clusterUrl + url.substring(CLUSTER_URL.length()); // NOI18N
        }
        return toFileObject(url);
    }

    /** Get the FileObject corresponding to a URL returned from the index. */
    public static FileObject toFileObject(String urlStr) {
        try {
            URL url = new URL(urlStr);
            return URLMapper.findFileObject(url);
        } catch (MalformedURLException mue) {
            Exceptions.printStackTrace(mue);
        }

        return null;
    }

    private static String getClusterUrl() {
        String retval = null;
        if (retval == null) {
            File f =
                    InstalledFileLocator.getDefault().locate("modules/org-netbeans-modules-php-editor.jar", null, false); // NOI18N

            if (f == null) {
                throw new RuntimeException("Can't find cluster");
            }

            f = new File(f.getParentFile().getParentFile().getAbsolutePath());

            try {
                f = f.getCanonicalFile();
                retval = Utilities.toURI(f).toURL().toExternalForm();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return retval;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getIn() {
        return in;
    }

    public abstract String getSignature();

    @Override
    public final boolean signatureEquals(ElementHandle handle) {
        if (handle instanceof PhpElementImpl) {
            PhpElementImpl other = (PhpElementImpl) handle;
            return this.getSignature().equals(other.getSignature());
        }
        return false;
    }

    @Override
    public final boolean isPlatform() {
        FileObject fo = getFileObject();
        if (fo != null) {
            FileType fileType = PhpSourcePath.getFileType(fo);
            return fileType.equals(FileType.INTERNAL);
        }
        return false;
    }

    @Override
    public boolean isDeprecated() {
        return isDeprecated;
    }

    @Override
    public final String getMimeType() {
        return FileUtils.PHP_MIME_TYPE;
    }

    @Override
    public final int getFlags() {
        return getPhpModifiers().toFlags();
    }

    @Override
    public final ElementKind getKind() {
        return getPhpElementKind().getElementKind();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PhpElementImpl other = (PhpElementImpl) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.in == null) ? (other.in != null) : !this.in.equals(other.in)) {
            return false;
        }
        if (this.offset != other.offset) {
            return false;
        }
        if ((this.fileUrl == null) ? (other.fileUrl != null) : !this.fileUrl.equals(other.fileUrl)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 71 * hash + (this.in != null ? this.in.hashCode() : 0);
        hash = 71 * hash + (this.fileUrl != null ? this.fileUrl.hashCode() : 0);
        hash = 71 * hash + this.offset;
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPhpElementKind().toString()).append(" "); //NOI18N
        if (this instanceof FullyQualifiedElement) {
            sb.append(((FullyQualifiedElement) this).getFullyQualifiedName().toString());
        } else {
            sb.append(getName());
        }
        return sb.toString();
    }
}
