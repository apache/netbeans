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

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;

/**
 * Factory for creating <code>SearchInfo</code> objects.
 *
 * @see  SearchInfo
 * @since  org.openidex.util/3 3.3
 * @author  Marian Petras
 */
public final class SearchInfoFactory {

    /**
     * filter that skips files and folders which are not visible according
     * to the <code>VisibilityQuery</code>
     *
     * @see  org.netbeans.api.queries.VisibilityQuery  VisibilityQuery
     */
    public static final FileObjectFilter VISIBILITY_FILTER
            = new VisibilityFilter();

    /**
     * filter that skips files and folders which are not sharable according
     * to the <code>SharabilityQuery</code>
     *
     * @see  org.netbeans.api.queries.SharabilityQuery  SharabilityQuery
     */
    public static final FileObjectFilter SHARABILITY_FILTER
            = new SharabilityFilter();

    /**
     */
    private SearchInfoFactory() {}

//    /**
//     * Creates a <code>SearchInfo</code> object for a given folder.
//     * The returned <code>SearchInfo</code> object's method
//     * {@link SearchInfo#canSearch()} always returns <code>true</code>
//     * and iterates through <code>DataObject</code>s found in the given
//     * folder. Non-sharable and/or non-visible <code>FileObject</code>s
//     * may be skipped, according to enabled filters.
//     *
//     * @param  folder  folder which should be searched
//     * @param  recursive  whether the folder's subfolders should be taken
//     *                    into account
//     * @param  checkVisibility  whether the visibility filter should be used
//     * @param  checkSharability  whether the sharability filter should be used
//     * @return  <code>SearchInfo</code> object which iterates through
//     *          <code>DataObject</code>s found in the specified folder
//     *          and (optionally) its subfolders
//     * @see  org.netbeans.api.queries.SharabilityQuery
//     * @see  org.netbeans.api.queries.VisibilityQuery
//     */
//    public static SearchInfo createSearchInfo(
//                FileObject folder,
//                boolean recursive,
//                boolean checkVisibility,
//                boolean checkSharability) {
//        if (!folder.isFolder()) {
//            throw new IllegalArgumentException("folder expected");      //NOI18N
//        }
//
//        DataFolder dataFolder = DataFolder.findFolder(folder);
//
//        int filtersCount = 0;
//        if (checkVisibility) {
//            filtersCount++;
//        }
//        if (checkSharability) {
//            filtersCount++;
//        }
//        
//        if (filtersCount == 0) {
//            return new SimpleSearchInfo(dataFolder, recursive, null);
//        } else {
//            FileObjectFilter[] filters = new FileObjectFilter[filtersCount];
//            
//            int i = 0;
//            if (checkVisibility) {
//                filters[i++] = VISIBILITY_FILTER;
//            }
//            if (checkSharability) {
//                filters[i++] = SHARABILITY_FILTER;
//            }
//            return new SimpleSearchInfo(dataFolder, recursive, filters);
//        }
//    }

//    /**
//     * Creates a <code>SearchInfo</code> object for given folders.
//     * The returned <code>SearchInfo</code> object's method
//     * {@link SearchInfo#canSearch()} always returns <code>true</code>
//     * and iterates through <code>DataObject</code>s found in the given
//     * folders. Non-sharable and/or non-visible <code>FileObject</code>s
//     * may be skipped, according to enabled filters.
//     *
//     * @param  folders  folders which should be searched
//     * @param  recursive  whether the folders' subfolders should be taken
//     *                    into account
//     * @param  checkVisibility  whether the visibility filter should be used
//     * @param  checkSharability  whether the sharability filter should be used
//     * @return  <code>SearchInfo</code> object which iterates through
//     *          <code>DataObject</code>s found in the specified folders
//     *          and (optionally) their subfolders
//     * @see  org.netbeans.api.queries.SharabilityQuery
//     * @see  org.netbeans.api.queries.VisibilityQuery
//     */
//    public static SearchInfo createSearchInfo(
//                final FileObject[] folders,
//                final boolean recursive,
//                final boolean checkVisibility,
//                final boolean checkSharability) {
//        if (folders.length == 0) {
//            return SimpleSearchInfo.EMPTY_SEARCH_INFO;
//        }
//        
//        if (folders.length == 1) {
//            return createSearchInfo(folders[0],
//                                    recursive,
//                                    checkVisibility,
//                                    checkSharability);
//        }
//        
//        for (int i = 0; i < folders.length; i++) {
//            if (!folders[i].isFolder()) {
//                throw new IllegalArgumentException(
//                          "folder expected (index " + i + ')');         //NOI18N
//            }
//        }
//        
//        SearchInfo[] nested = new SearchInfo[folders.length];
//        for (int i = 0; i < folders.length; i++) {
//            nested[i] = createSearchInfo(folders[i],
//                                         recursive,
//                                         checkVisibility,
//                                         checkSharability);
//        }
//        return new CompoundSearchInfo(nested);
//    }
    
    /**
     * Creates a <code>SearchInfo</code> object for a given folder.
     * The returned <code>SearchInfo</code> object's method
     * {@link SearchInfo#canSearch()} always returns <code>true</code>
     * and iterates through <code>DataObject</code>s found in the given
     * folder. Files and folders that do not pass any of the given filters
     * are skipped (not searched). If multiple filters are passed,
     * the filters are applied on each file/folder in the same order
     * as in the array passed to this method.
     *
     * @param  folder  folder which should be searched
     * @param  recursive  whether the folder's subfolders should be taken
     *                    into account
     * @param  filters  filters to be used when searching;
     *                  or <code>null</code> if no filters should be used
     * @return  <code>SearchInfo</code> object which iterates through
     *          <code>DataObject</code>s found in the specified folder
     *          and (optionally) its subfolders
     * @see  FileObjectFilter
     */
    public static SearchInfo createSearchInfo(
                final FileObject folder,
                final boolean recursive,
                final FileObjectFilter[] filters) {
        if (folder == null) {
            throw new IllegalArgumentException("folder is <null>");     //NOI18N
        }
        if (!folder.isFolder()) {
            throw new IllegalArgumentException("folder expected");      //NOI18N
        }

        DataFolder dataFolder;
        try {
            dataFolder = DataFolder.findFolder(folder);
        } catch (IllegalArgumentException ex) {
            return SimpleSearchInfo.EMPTY_SEARCH_INFO;
        }

        return new SimpleSearchInfo(dataFolder,
                                    recursive,
                                    filters);
    }

    /**
     * Creates a <code>SearchInfo</code> object for given folders.
     * The returned <code>SearchInfo</code> object's method
     * {@link SearchInfo#canSearch()} always returns <code>true</code>
     * and iterates through <code>DataObject</code>s found in the given
     * folders. Files and folders that do not pass any of the given filters
     * are skipped (not searched). If multiple filters are passed,
     * the filters are applied on each file/folder in the same order
     * as in the array passed to this method.
     *
     * @param  folders  folders which should be searched
     * @param  recursive  whether the folders' subfolders should be taken
     *                    into account
     * @param  filters  filters to be used when searching;
     *                  or <code>null</code> if no filters should be used
     * @return  <code>SearchInfo</code> object which iterates through
     *          <code>DataObject</code>s found in the specified folders
     *          and (optionally) their subfolders
     * @see  FileObjectFilter
     */
    public static SearchInfo createSearchInfo(
                final FileObject[] folders,
                final boolean recursive,
                final FileObjectFilter[] filters) {
        if (folders.length == 0) {
            return SimpleSearchInfo.EMPTY_SEARCH_INFO;
        }
        
        if (folders.length == 1) {
            return createSearchInfo(folders[0],
                                    recursive,
                                    filters);
        }
        
        for (int i = 0; i < folders.length; i++) {
            if (!folders[i].isFolder()) {
                throw new IllegalArgumentException(
                          "folder expected (index " + i + ')');         //NOI18N
            }
        }
        
        SearchInfo[] nested = new SearchInfo[folders.length];
        for (int i = 0; i < folders.length; i++) {
            nested[i] = createSearchInfo(folders[i],
                                         recursive,
                                         filters);
        }
        return new CompoundSearchInfo(nested);
    }
    
    /**
     * Creates a <code>SearchInfo</code> object combining
     * <code>SearchInfo</code> objects returned by the node's subnodes.
     *
     * Method {@link SearchInfo#canSearch()} of the resulting
     * <code>SearchInfo</code> objects returns <code>true</code> if and only if
     * at least one of the nodes is searchable
     * (its method <code>canSearch()</code> returns <code>true</code>).
     * The iterator iterates through all <code>DataObject</code>s returned
     * by the subnode's <code>SearchInfo</code> iterators.
     *
     * @param  node  node to create <code>SearchInfo</code> for
     * @return  <code>SearchInfo</code> object representing combination
     *          of <code>SearchInfo</code> objects of the node's subnodes
     */
    public static SearchInfo createSearchInfoBySubnodes(Node node) {
        return new SubnodesSearchInfo(node);
    }
    
    /**
     * Creates a <code>SearchInfo</code> compound of the given delegates.
     * It combines the delegates such that:
     * <ul>
     *     <li>its method {@link SearchInfo#canSearch()} returns
     *         <code>true</code> if and only if at least one of the delegate's
     *         <code>canSearch()</code> returns <code>true</code></li>
     *     <li>its method {@link SearchInfo#objectsToSearch()} chains iterators
     *         of the delegates, skipping those delegates whose
     *         <code>canSearch()</code> method returns <code>false</code></li>
     * </ul>
     *
     * @param  delegates  delegates the compound <code>SearchInfo</code> should
     *                    delegate to
     * @return  created compound <code>SearchInfo</code>
     * @exception  java.lang.IllegalArgumentException
     *             if the argument is <code>null</code>
     * @since  3.13
     */
    public static SearchInfo createCompoundSearchInfo(SearchInfo... delegates) {
        if (delegates == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
        return new CompoundSearchInfo(delegates);
    }

}
