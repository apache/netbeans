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

package org.openidex.search;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;

 /**
  * Search API utility class.
  *
  * @since org.openidex.util/3 3.20
  * @author  Marian Petras
  * @author  kaktus
  */
public final class Utils {

    private Utils() {
    }

    /**
     */
    static SearchInfo getSearchInfo(Node node) {
        /* 1st try - is the SearchInfo object in the node's lookup? */
        SearchInfo info = node.getLookup().lookup(SearchInfo.class);
        if (info != null) {
            return info;
        }

        /* 2nd try - does the node represent a DataObject.Container? */
        DataFolder container = node.getLookup().lookup(DataFolder.class);
        if (container == null) {
            return null;
        } else {
            return SearchInfoFactory.createSearchInfo(
                    container.getPrimaryFile(),
                    true,                       //recursive
                    new FileObjectFilter[] {
                            SearchInfoFactory.VISIBILITY_FILTER });
        }
    }

    /**
     * Returns <code>Iterator</code> of <code>FileObject</code>'s for the provided <code>SearchInfo</code>.
     * If provided <code>SearchInfo</code> object is implementation of <code>SearchInfo.Files</code> interface
     * then the result of method <code>SearchInfo.Files.filesToSearch</code> is returned. Otherwise the objects
     * are getting from the <code>SearchInfo.objectsToSearch</code> method.
     *
     * @param si <code>SearchInfo</code> object to return the iterator for
     * @return iterator which iterates over <code>FileObject</code>s
     * @since org.openidex.util/3 3.20
     */
    public static Iterator<FileObject> getFileObjectsIterator(SearchInfo si){
        if (si instanceof SearchInfo.Files){
            return ((SearchInfo.Files)si).filesToSearch();
        }else{
            Set<FileObject> set = new HashSet<FileObject>();
            for(Iterator<DataObject> iter = si.objectsToSearch(); iter.hasNext();){
                set.add(iter.next().getPrimaryFile());
            }
            return set.iterator();
        }
    }

    static Iterator<DataObject> toDataObjectIterator(Iterator<FileObject> itFO){
        Set<DataObject> set = new HashSet<DataObject>();
        while(itFO.hasNext()){
            try {
                set.add(DataObject.find(itFO.next()));
            } catch (DataObjectNotFoundException ex){}
        }
        return set.iterator();
    }
    
}
