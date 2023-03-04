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
package org.netbeans.modules.masterfs.ui;

import java.awt.Image;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.modules.masterfs.filebasedfs.MasterFileSystemFactory;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.providers.AnnotationProvider;
import org.netbeans.modules.masterfs.providers.BaseAnnotationProvider;
import org.openide.filesystems.*;
import org.openide.util.lookup.ServiceProvider;

/**
 * Simple extension of master filesystem which provides 
 * icons for files.
 * 
 * @author sdedic
 */
public class FileBasedFSWithUI extends FileBasedFileSystem {
    private final StatusDecorator uiDecorator = new UiDecorator();
    
    @Override
    public StatusDecorator getDecorator() {
        return uiDecorator;
    }
    
    private class UiDecorator extends StatusImpl implements ImageDecorator {
        @Override
        public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
            Image retVal = null;

            Iterator<? extends BaseAnnotationProvider> it = annotationProviders.allInstances().iterator();
            while (retVal == null && it.hasNext()) {
                BaseAnnotationProvider ap = it.next();
                if (ap instanceof AnnotationProvider) {
                    retVal = ((AnnotationProvider)ap).annotateIcon(icon, iconType, files);
                }
            }
            if (retVal != null) {
                return retVal;
            }

            return icon;
        }
    }
    
    @ServiceProvider(service = MasterFileSystemFactory.class, 
            supersedes = "org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem$Factory")
    public static class Factory implements MasterFileSystemFactory {
        @Override
        public FileBasedFileSystem createFileSystem() {
            return new FileBasedFSWithUI();
        }
    }
}
