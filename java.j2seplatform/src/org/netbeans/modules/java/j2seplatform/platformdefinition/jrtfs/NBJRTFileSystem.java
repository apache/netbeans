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

package org.netbeans.modules.java.j2seplatform.platformdefinition.jrtfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.AbstractFileSystem;
import org.openide.filesystems.DefaultAttributes;
import org.openide.util.BaseUtilities;

/**
 *
 * @author lahvac
 * @author Tomas Zezula
 * TODO: Serialization
 */
public class NBJRTFileSystem extends AbstractFileSystem implements AbstractFileSystem.Change,
                                                                   AbstractFileSystem.List,
                                                                   AbstractFileSystem.Info {

    private static final Logger LOG = Logger.getLogger(NBJRTFileSystem.class.getName());
    private static final String PROTOCOL = "jrt";   //NOI18N
    private static final String PROP_JAVA_HOME = "java.home";   //NOI18N

    @CheckForNull
    public static NBJRTFileSystem create(File jdkHome) {
        final File jrtFsJar = NBJRTUtil.getNIOProvider(jdkHome);
        if (jrtFsJar == null) {
            return null;
        }
        try {
            final URLClassLoader jrtFsLoader = new URLClassLoader(
                    new URL[] {BaseUtilities.toURI(jrtFsJar).toURL()},
                    ClassLoader.getSystemClassLoader());
            final FileSystem fs = FileSystems.newFileSystem(
                    URI.create(String.format("%s:/", //NOI18N
                            PROTOCOL)),
                    Collections.singletonMap(PROP_JAVA_HOME, jdkHome.getAbsolutePath()),
                    jrtFsLoader);
            if (fs == null) {
                throw new IllegalStateException(String.format(
                    "No %s provider.",  //NOI18N
                    PROTOCOL));
            }
            return new NBJRTFileSystem(jdkHome, fs);
        } catch (IOException ex) {
            throw new IllegalStateException(
                String.format(
                    "Cannot create %s NIO FS for: %s",  //NOI18N
                    PROTOCOL,
                    jdkHome.getAbsolutePath()),
                ex);
        }
    }

    private final File jdkHome;
    private final FileSystem fileSystem;

    private NBJRTFileSystem(File jdkHome, FileSystem fileSystem) {
        this.jdkHome = jdkHome;
        this.fileSystem = fileSystem;
        this.list = this;
        this.info = this;
        this.change = this;
        this.attr = new PathAttributes(this, info, change, list);
    }

    @Override
    public String getDisplayName() {
        return String.format(
            "%s for: %s",   //NOI18N
            PROTOCOL,
            jdkHome.getAbsolutePath());
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public boolean readOnly(String name) {
        return true;
    }

    @Override
    public boolean folder(String name) {
        return Files.isDirectory(getPath(name));
    }

    @Override
    public Date lastModified(String name) {
        try {
            return new Date(Files.getLastModifiedTime(getPath(name)).toMillis());
        } catch (IOException ex) {
            throw new IllegalStateException(ex); //TODO: what to do here?
        }
    }

    @Override
    public long size(String name) {
        try {
            return Files.size(getPath(name));
        } catch (NoSuchFileException ex) {
            return 0;
        }catch (IOException ex) {
            LOG.log(Level.WARNING,
                String.format("Cannot read: %s in %s",
                    name,
                    getDisplayName()),
                ex);
            return 0;
        }
    }

    @Override
    public InputStream inputStream(String name) throws FileNotFoundException {
        try {
            return Files.newInputStream(getPath(name));
        } catch (IOException ex) {
            throw (FileNotFoundException) new FileNotFoundException(name).initCause(ex);
        }
    }

    @Override
    public String[] children(String f) {
        Path p = getPath(f);
        try (DirectoryStream<Path> dir = Files.newDirectoryStream(p)){
            java.util.List<String> result = new ArrayList<>();
            for (Path child : dir) {
                String name = child.getName(child.getNameCount() - 1).toString();
                if (name.endsWith("/"))
                    name = name.substring(0, name.length() - 1);
                result.add(name);
            }

            return result.toArray(new String[result.size()]);
        } catch (IOException ex) {
            return new String[0]; //huh?
        }
    }

    @Override
    public String mimeType(String name) {
        return null;
    }

    @Override
    @Deprecated
    public void markUnimportant(String name) {
        //NOP
    }

    //Write operations - unsupported
    @Override
    public void createFolder(String name) throws IOException {
        this.<Void,IOException>writeOp(IOException.class);
    }

    @Override
    public void createData(String name) throws IOException {
        this.<Void,IOException>writeOp(IOException.class);
    }

    @Override
    public void rename(String oldName, String newName) throws IOException {
        this.<Void,IOException>writeOp(IOException.class);
    }

    @Override
    public void delete(String name) throws IOException {
        this.<Void,IOException>writeOp(IOException.class);
    }

    @Override
    public OutputStream outputStream(String name) throws IOException {
        return this.<OutputStream,IOException>writeOp(IOException.class);
    }

    @Override
    public void lock(String name) throws IOException {
        this.<Void,IOException>writeOp(IOException.class);
    }

    @Override
    public void unlock(String name) {
        this.<Void,IllegalStateException>writeOp(IllegalStateException.class);
    }


    //pkg-private
    URI getRootURL() throws URISyntaxException {
        return NBJRTUtil.createURI(jdkHome, "");    //NOI18N
    }

    @NonNull
    Path getPath(@NonNull final String name) {
        return fileSystem.getPath("/" + name);
    }

    //private
    private <R,E extends Exception> R writeOp(Class<E> clz) throws E {
        final String message = String.format(
            "Unsupported write operation on readonly %s",   //NOI18N
            getDisplayName());
        E e;
        try {
            try {
                e = clz.getDeclaredConstructor(String.class).newInstance(message);
            } catch (NoSuchMethodException nsm) {
                e = clz.newInstance();
            }
        } catch (ReflectiveOperationException roe) {
            throw  new IllegalStateException(message);
        }
        throw  e;
    }

    private static final class PathAttributes extends DefaultAttributes {

        private static final long serialVersionUID = 1L;
        private final NBJRTFileSystem fs;

        public PathAttributes(
                NBJRTFileSystem fs,
                Info info,
                Change change,
                List list) {
            super(info, change, list);
            this.fs = fs;
        }

        @Override
        public Object readAttribute(final String name, final String attrName) {
            if (attrName.equals("java.nio.file.Path")) { // NOI18N
                return fs.getPath(name);
            }
            return super.readAttribute(name, attrName);
        }
    }

}
