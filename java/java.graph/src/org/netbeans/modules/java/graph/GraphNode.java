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

package org.netbeans.modules.java.graph;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.netbeans.api.annotations.common.NonNull;
import static org.netbeans.modules.java.graph.DependencyGraphScene.VersionProvider.VERSION_CONFLICT;
import static org.netbeans.modules.java.graph.DependencyGraphScene.VersionProvider.VERSION_NO_CONFLICT;
import static org.netbeans.modules.java.graph.DependencyGraphScene.VersionProvider.VERSION_POTENTIAL_CONFLICT;
import org.openide.util.Parameters;

/**
 *
 * @author Milos Kleint 
 * @param <I> 
 */
public final class GraphNode<I extends GraphNodeImplementation> {

    public static final int UNMANAGED = 0;
    public static final int MANAGED = 1;
    public static final int OVERRIDES_MANAGED = 2;

    private I impl, parentAfterFix;
    
    private final Set<I> duplicates;
    private int level; 
    private int managedState = UNMANAGED;

    /** 
     * Creates a new instance of GraphNode
     * @param impl 
     **/
    public GraphNode(@NonNull I impl) {
        Parameters.notNull("impl", impl);   //NOI18N
        this.impl = impl;
        duplicates = new HashSet<>();
    }
        
    public String getName() {
        return impl.getName();
    }
    
    public I getImpl() {
        return impl;
    }

    public String getTooltipText() {
        return impl.getTooltipText();
    }    
    
    public void addDuplicateOrConflict(I i) {
        duplicates.add(i);
    }

    public void removeDuplicateOrConflict(I i) {
        duplicates.remove(i);
    }

    public Set<I> getDuplicatesOrConflicts() {
        return Collections.unmodifiableSet(duplicates);
    }
    
    /** 
     * After changes in graph parent may change, so it's always better to
     * call this method instead of getImpl().getParent()
     * 
     * @return 
     */
    public I getParent() {
        if (parentAfterFix != null) {
            return parentAfterFix;
        }
        return impl.getParent();
    }

    public void setParent(I newParent) {
        parentAfterFix = newParent;
    }
    
    public void setImpl(I i) {        
        impl = i;
    }

    boolean isRoot() {
        return level == 0;
    }
    
    public void setPrimaryLevel(int i) {
        level = i;
    }
    
    public int getPrimaryLevel() {
        return level;
    }
    
    int getManagedState() {
        return managedState;
    }

    public void setManagedState(int state) {
        this.managedState = state;
    }
    
    int getConflictType(Function<I, Boolean> isConflict, BiFunction<I, I, Integer> compare) {
        int ret = VERSION_NO_CONFLICT;
        int result;
        for (I dupl : duplicates) {
            if (isConflict.apply(dupl)) {
                result = compare.apply(impl, dupl);
                if (result < 0) {
                    return VERSION_CONFLICT;
                }
                if (result > 0) {
                    ret = VERSION_POTENTIAL_CONFLICT;
                }
            }
        }
        return ret;
    }
    
    public boolean represents(I i) {
        if (impl.equals(i)) {
            return true;
        }
        for (I dupl : duplicates) {
            if (i.equals(dupl)) {
                return true;
            }
        }
        return false;
    }
    
}
