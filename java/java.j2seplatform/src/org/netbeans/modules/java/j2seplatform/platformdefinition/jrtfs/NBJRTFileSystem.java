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

            return result.toArray(new String[0]);
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
                e = clz.getDeclaredConstructor().newInstance();
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
