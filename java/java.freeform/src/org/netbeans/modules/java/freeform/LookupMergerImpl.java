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

package org.netbeans.modules.java.freeform;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.LookupMerger;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Merges ClassPathProviders.
 *
 * @author David Konecny
 */
public class LookupMergerImpl implements LookupMerger<ClassPathProvider> {

    public LookupMergerImpl() {}

    public Class<ClassPathProvider> getMergeableClass() {
        return ClassPathProvider.class;
    }

    public ClassPathProvider merge(Lookup lookup) {
        return new ClassPathProviderImpl(lookup);
    }
    
    private static class ClassPathProviderImpl implements ClassPathProvider {
        
        private Lookup lkp;
        
        public ClassPathProviderImpl(Lookup lkp) {
            this.lkp = lkp;
        }
        
        public ClassPath findClassPath(FileObject file, String type) {
            for (ClassPathProvider cpp : lkp.lookupAll(ClassPathProvider.class)) {
                ClassPath cp = cpp.findClassPath(file, type);
                if (cp != null) {
                    return cp;
                }
            }
            return null;
        }
        
    }

}
