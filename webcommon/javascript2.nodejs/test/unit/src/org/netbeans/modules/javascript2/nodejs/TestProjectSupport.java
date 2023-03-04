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

package org.netbeans.modules.javascript2.nodejs;

import java.net.URI;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Pisl
 */
public class TestProjectSupport {

    public static final class TestProject implements Project {

        private Lookup lookup;
        private final FileObject dir;
        final ProjectState state;
        Throwable error;
        
        public TestProject(FileObject dir, ProjectState state) {
            this.dir = dir;
            this.state = state;
        }
        
        public void setLookup( Lookup lookup ) {
            this.lookup = lookup;
        }
        
        @Override
        public Lookup getLookup() {
            if ( lookup == null ) {
                return Lookup.EMPTY;
            }
            else {
                return lookup;
            }
        }
        
        @Override
        public FileObject getProjectDirectory() {
            return dir;
        }
        
        @Override
        public String toString() {
            return "testproject:" + getProjectDirectory().getNameExt();
        }
        
    }
    
    public static class FileOwnerQueryImpl implements FileOwnerQueryImplementation {

        private Project testProject1;

        public FileOwnerQueryImpl(Project testProject1) {
            this.testProject1 = testProject1;
        }

        @Override
        public Project getOwner(URI file) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Project getOwner(FileObject file) {
            if (file.getPath().startsWith(testProject1.getProjectDirectory().getPath())){
                return testProject1;
            }
            return null;
        }

    }
}
