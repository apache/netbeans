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

package org.netbeans.modules.xml.schema.model.impl.resolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Special container very close to Map. 
 * The difference is that it holds multiple objects for a key.
 *
 * @author Nikita Krjukov
 */
public class MultivalueMap<K, V> {

    private HashMap<K, Object> mContainer = new HashMap<K, Object>();

    public void put(K key, V value) {
        Object oldValue = mContainer.get(key);
        if (oldValue == null) {
            mContainer.put(key, value);
        } else if (oldValue instanceof List) {
            List valueList = List.class.cast(oldValue);
            //
            // Dublicate links not allowed!
            if (!valueList.contains(value)) {
                valueList.add(value);
            }
        } else {
            ArrayList newList = new ArrayList();
            mContainer.put(key, newList);
            newList.add(oldValue);
            newList.add(value);
        }
    }

    public List<V> get(K key) {
        Object values = mContainer.get(key);
        if (values == null) {
            return Collections.EMPTY_LIST;
        } else if (values instanceof List) {
            return List.class.cast(values);
        } else {
            return Collections.singletonList((V)values);
        }
    }

    public boolean containsKey(K key) {
        return mContainer.containsKey(key);
    }

    public boolean isEmpty() {
        return mContainer.isEmpty();
    }

    /**
     * Special kind of MultivalueMap. It has key and value of the same type.
     * The is called Graph because it is helpful for graph representation.
     * 
     * @param <I>
     */
    public static class Graph<I> extends MultivalueMap<I, I> {
    }

    public static class BidirectionalGraph<I> {
        private Graph<I> mForward = new Graph<I>();
        private Graph<I> mBackward = new Graph<I>();
        private Set<I> mRoots = null;

        public void put(I from, I to) {
            mForward.put(from, to);
            mBackward.put(to, from);
        }

        public List<I> getTo(I key) {
            return mForward.get(key);
        }

        public List<I> getFrom(I key) {
            return mBackward.get(key);
        }

        public boolean containsKey(I key) {
            return mForward.containsKey(key) || mBackward.containsKey(key);
        }

        public boolean isEmpty() {
            return mForward.isEmpty();
        }

        public Set<I> getRoots(I startItem, boolean recalculate) {
            if (mRoots == null) {
                recalculate = true;
            }
            //
            if (recalculate) {
                mRoots = Utils.getTreeRoots(this, startItem);
            }
            //
            return mRoots;
        }

    }

    public static class Utils {

        public static <I> Set<I> getTreeLeafs(Graph<I> source, I startItem) {
            HashSet<I> leafs = new HashSet<I>();
            //
            // The processedItems is required because the sourceTree can be not
            // a tree but rather a graph. So it's necessary to exclude cycling!
            HashSet<I> processedItems = new HashSet<I>();
            populateLeafs(source, startItem, leafs, processedItems);
            return leafs;
        }

        private static <I> void populateLeafs(Graph<I> source,
                I startItem, Set<I> leafs, Set<I> processedItems) {
            //
            List<I> children = source.get(startItem);
            if (children.isEmpty()) {
                leafs.add(startItem);
            }
            processedItems.add(startItem);
            //
            // Process children
            for (I child : children) {
                if (!processedItems.contains(child)) {
                    populateLeafs(source, child, leafs, processedItems);
                }
            }
        }

        public static <I> Set<I> collectAllSubItems(Graph<I> source, I startItem) {
            HashSet<I> result = new HashSet<I>();
            populateAllSubItems(source, startItem, result);
            return result;
        }

        public static <I> void populateAllSubItems(Graph<I> source, I startItem, Set<I> result) {
            result.add(startItem);
            //
            // Process children
            List<I> children = source.get(startItem);
            for (I child : children) {
                if (!result.contains(child)) {
                    populateAllSubItems(source, child, result);
                }
            }
        }

        //---------------------------------------------------------------------

        public static <I> Set<I> getTreeRoots(BidirectionalGraph<I> source, I startItem) {
            HashSet<I> leafs = new HashSet<I>();
            //
            // The processedItems is required because the sourceTree can be not
            // a tree but rather a graph. So it's necessary to exclude cycling!
            HashSet<I> processedItems = new HashSet<I>();
            Path<I> iterationStack = new Path<I>(startItem);
            populateRoots(source, iterationStack, leafs, processedItems);
            return leafs;
        }

        private static class Path<I> {

            private I mItem;
            private Path<I> mParentPath;

            public Path(I item) {
                mItem = item;
            }

            public Path(Path<I> parentPath, I item) {
                mItem = item;
                mParentPath = parentPath;
            }

            public I getItem() {
                return mItem;
            }

            public boolean containsInPath(I item) {
                if (item == mItem) {
                    return true;
                } else if (mParentPath != null) {
                    return mParentPath.containsInPath(item);
                } else {
                    return false;
                }
            }

        }

        private static <I> void populateRoots(BidirectionalGraph<I> source,
                Path<I> iterationStack, Set<I> leafs, Set<I> processedItems) {
            //
            I startItem = iterationStack.getItem();
            processedItems.add(startItem);
            //
            List<I> referencesToStartItem = source.getFrom(startItem);
            if (referencesToStartItem.isEmpty()) {
                leafs.add(startItem);
            }
            //
            // Process children
            for (I referenceToStartItem : referencesToStartItem) {
                if (!processedItems.contains(referenceToStartItem)) {
                    Path<I> path = new Path<I>(iterationStack, referenceToStartItem);
                    populateRoots(source, path, leafs, processedItems);
                } else if (iterationStack.containsInPath(referenceToStartItem)) {
                    leafs.add(referenceToStartItem);
                }
            }
        }

        public static <I> void populateAllSubItems(BidirectionalGraph<I> source, 
                I startItem, I excludeItem, Set<I> result) {
            //
            if (startItem == excludeItem) {
                // exclude item has to be ignored
                return;
            }
            //
            result.add(startItem);
            //
            // Process children
            List<I> children = source.getTo(startItem);
            for (I child : children) {
                if (!result.contains(child)) {
                    populateAllSubItems(source, child, excludeItem, result);
                }
            }
        }

    }
}
