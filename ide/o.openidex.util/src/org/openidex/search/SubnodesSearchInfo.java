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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 *
 * @author  Marian Petras
 */
final class SubnodesSearchInfo implements SearchInfo.Files {

    /** */
    private final Node node;

    /**
     *
     */
    public SubnodesSearchInfo(final Node node) {
        this.node = node;
    }

    /**
     */
    public boolean canSearch() {
        final Node[] nodes = node.getChildren().getNodes(true);
        for (int i = 0; i < nodes.length; i++) {
            SearchInfo searchInfo = Utils.getSearchInfo(nodes[i]);
            if (searchInfo != null && searchInfo.canSearch()) {
                return true;
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
        final Node[] nodes = node.getChildren().getNodes(true);
        if (nodes.length == 0) {
            return SimpleSearchInfo.EMPTY_SEARCH_INFO.filesToSearch();
        }
        
        List<SearchInfo> searchInfoElements = new ArrayList<SearchInfo>(nodes.length);
        for (int i = 0; i < nodes.length; i++) {
            SearchInfo subInfo = Utils.getSearchInfo(nodes[i]);
            if (subInfo != null && subInfo.canSearch()) {
                searchInfoElements.add(subInfo);
            }
        }
        
        final int size = searchInfoElements.size();
        switch (size) {
            case 0:
                return Collections.<FileObject>emptyList().iterator();
            case 1:
                return Utils.getFileObjectsIterator(searchInfoElements.get(0));
            default:
                return new CompoundSearchIterator(
                        searchInfoElements.toArray(new SearchInfo[size]));
        }
    }

}
