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

package org.openidex.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author  Marian Petras
 */
class CompoundSearchInfo implements SearchInfo.Files {

    /** */
    private final SearchInfo[] elements;

    /**
     * Creates a new instance of CompoundSearchInfo
     *
     * @param  elements  elements of this <code>SearchInfo</code>
     * @exception  java.lang.IllegalArgumentException
     *             if the argument was <code>null</code>
     */
    CompoundSearchInfo(SearchInfo... elements) {
        if (elements == null) {
            throw new IllegalArgumentException();
        }
        
        this.elements = elements.length != 0 ? elements
                                             : null;
    }

    /**
     */
    public boolean canSearch() {
        if (elements != null) {
            for (SearchInfo element : elements) {
                if (element.canSearch()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     */
    public Iterator<DataObject> objectsToSearch() {
        return Utils.toDataObjectIterator(filesToSearch());
    }

    /**
     */
    public Iterator<FileObject> filesToSearch() {
        if (elements == null) {
            return Collections.<FileObject>emptyList().iterator();
        }
        
        List<SearchInfo> searchableElements = new ArrayList<SearchInfo>(elements.length);
        for (SearchInfo element : elements) {
            if (element.canSearch()) {
                searchableElements.add(element);
            }
        }
        return new CompoundSearchIterator(
            searchableElements.toArray(new SearchInfo[0]));
    }
    
}
