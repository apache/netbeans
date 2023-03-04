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

package org.netbeans.modules.csl.api;

import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author Tor Norbye
 */
public interface StructureItem {
    @NonNull String getName();
    @NonNull String getSortText();
    @NonNull String getHtml(@NonNull HtmlFormatter formatter);
    @NonNull ElementHandle getElementHandle();
    @NonNull ElementKind getKind();
    @NonNull Set<Modifier> getModifiers();
    boolean isLeaf();
    @NonNull List<? extends StructureItem> getNestedItems(); 
    long getPosition();
    long getEndPosition();
    /** Icon to use instead of the default implied by the ElementKind */
    @CheckForNull ImageIcon getCustomIcon();

    @Override
    public abstract boolean equals(Object o);
    @Override
    public abstract int hashCode();

    /**
     * Check whether the StructureItem is an inherited item.
     *
     * @since 2.72.0
     * @param structureItem the structure item
     * @return {@code true} if it is an inherited item (a class doesn't override
     * that member), otherwise {@code false} (a class declares it as an
     * overriding member)
     */
    public static boolean isInherited(StructureItem structureItem) {
        // see also http://wiki.apidesign.org/wiki/ExtendingInterfaces
        return structureItem instanceof StructureItem.InheritedItem
                && ((StructureItem.InheritedItem) structureItem).isInherited();
    }

    public interface CollapsedDefault extends StructureItem {

        /**
         * Returns whether this StructureItem should be collapsed
         * when shown for the first time in Navigator.
         * @since 2.7.0
         * @return true, if it should be
         */
        boolean isCollapsedByDefault ();

    }

    public interface InheritedItem extends StructureItem {

        /**
         * Check whether this StructureItem is an inherited item.
         *
         * @since 2.72.0
         * @return {@code true} if it is an inherited item (a class doesn't
         * override that member), otherwise {@code false} (a class declares it
         * as an overriding member)
         */
        boolean isInherited();

        /**
         * Get the declaring element handle. e.g. class
         *
         * @since 2.72.0
         * @return the declaring element handle of the inherited item
         */
        @NonNull
        ElementHandle getDeclaringElement();
    }
}
