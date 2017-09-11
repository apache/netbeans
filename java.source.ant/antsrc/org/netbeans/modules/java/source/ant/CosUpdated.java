/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
