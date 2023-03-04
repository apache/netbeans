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
package org.netbeans.modules.java.source.ant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Objects;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.Resource;

/**
 *
 * @author Tomas Zezula
 */
public final class CosUpdated extends Task {
    private String id;
    private File srcdir;
    private String includes;
    
    public void setId(final String id) {
        id.getClass();
        this.id = id;
    }
    
    
    public String getId() {
        return this.id;
    }
    
    public void setSrcdir(final File dir) {
        this.srcdir = dir;
    }
    
    public File getSrcdir() {
        return this.srcdir;
    }
    
    public void setIncludes(final String includes) {
        this.includes = includes;
    }
    
    public String getIncludes() {
        return this.includes;
    }

    @Override
    public void execute() throws BuildException {
        if (this.id == null || this.id.isEmpty()) {
            throw new BuildException("The id has to be set.");    //NOI18N
        }
        if (this.srcdir == null || !this.srcdir.isDirectory()) {
            throw new BuildException("The srcdir has to point to a directory.");    //NOI18N
        }
        if (this.includes == null || this.includes.isEmpty()) {
            throw new BuildException("The includes has to be set.");    //NOI18N
        }
        final Project prj = getProject();
        final CosFileSet cfs = new CosFileSet();
        cfs.setProject(prj);
        cfs.setDir(this.srcdir);
        for (String include : includes.split(",")) {    //NOI18N
            include = include.trim();
            if (!include.isEmpty()) {
                cfs.createInclude().setName(include);
            }
        }
        prj.addReference(this.id, cfs);
    }
    
    private static final class CosFileSet extends FileSet {

        @Override
        public Iterator<Resource> iterator() {
            return new CosFileSetIterator(super.iterator());
        }

        @Override
        public boolean isFilesystemOnly() {
            return false;
        }
        
        private static final class CosFileSetIterator implements Iterator<Resource> {
            
            private final Iterator<Resource> delegate;
            
            CosFileSetIterator(final Iterator<Resource> delegate) {
                delegate.getClass();
                this.delegate = delegate;
            }

            @Override
            public boolean hasNext() {
                return delegate.hasNext();
            }

            @Override
            public Resource next() {
                return new CosResource(delegate.next());
            }            
        }
        
        private static final class CosResource extends Resource {
            
            private final Resource delegate;
            
            CosResource(final Resource delegate) {
                delegate.getClass();
                this.delegate = delegate;
            }

            @Override
            public void setRefid(Reference r) {
                throw tooManyAttributes();
            }

            @Override
            public String getName() {
                final String name = delegate.getName();
                return name.replace(".sig", ".class");  //NOI18N
            }

            @Override
            public boolean isExists() {
                return delegate.isExists();
            }

            @Override
            public long getLastModified() {
                return delegate.getLastModified();
            }

            @Override
            public boolean isDirectory() {
                return delegate.isDirectory();
            }

            @Override
            public long getSize() {
                return delegate.getSize();
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return delegate.getInputStream();
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                return delegate.getOutputStream();
            }

            public int compareTo(Resource another) {
                if (another instanceof CosResource) {
                    return delegate.compareTo(((CosResource)another).delegate);
                } else {
                    return toString().compareTo(String.valueOf(another));
                }
            }

            @Override
            public int hashCode() {
                int hash = 3;
                hash = 71 * hash + Objects.hashCode(this.delegate);
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                return Objects.equals(this.delegate, ((CosResource)obj).delegate);
            }

            @Override
            public String toString() {
                return delegate.toString();
            }

            @Override
            public boolean isFilesystemOnly() {
                return false;
            }
        }
    }
}
