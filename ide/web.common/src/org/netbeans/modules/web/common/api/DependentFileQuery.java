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
package org.netbeans.modules.web.common.api;

import org.netbeans.modules.web.common.spi.DependentFileQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * An API entry point to query dependency relationships between files. 
 * None SPI is really implemented at this stage - see comments below.
 */
public class DependentFileQuery {

    private static Lookup.Result<DependentFileQueryImplementation> lookup = 
            Lookup.getDefault().lookupResult(DependentFileQueryImplementation.class);
    
    /**
     * Does "master" FileObject depends on "dependent" FileObject? Typical usage will
     * be to answer questions like "does foo.html depends on style.css?"
     */
    public static boolean isDependent(FileObject master, FileObject dependent) {
        if (dependent.equals(master)) {
            return true;
        }
        for (DependentFileQueryImplementation impl : lookup.allInstances()) {
            if (impl.isDependent(master, dependent) == DependentFileQueryImplementation.Dependency.YES) {
                return true;
            }
        }
        
        return false;
    }
    
    /*
     * Find collection of files whose change affects changes in the view of <code>fileObject</code>.
     * F.e. if <code>fileObject</code> is html file then its view is affected 
     * by changes in included JS or CSS files. Related to {@link #isDependent} .
     * @param fileObject
     * @return collection of files from which <code>fileObject</code> depends on
     *
    public static Set<FileObject> getDependent(FileObject fileObject) {
        Collection<? extends DependentFileQueryImplementation> impls = lookup.allInstances();
        if ( impls.isEmpty() ){
            return Collections.emptySet();
        }
        if ( impls.size() == 1){
            return impls.iterator().next().getDependent(fileObject);
        }
        HashSet<FileObject> result = new HashSet<FileObject>();
        for( DependentFileQueryImplementation impl : lookup.allInstances()){
            Set<FileObject> set = impl.getDependent(fileObject);
            result.addAll(set);
        }
        return result;
    }*/
}
