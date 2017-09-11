/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * This class implements the {@link Set} interface, backed by an
 * {@link ArrayList}.
 * <p>The {@link ArrayList} supports :
 * <ul>
 * <li>the reordering of elements after adding each new element (see {@link
 *     #ordering(boolean)}). The elements are reordered using their {@linkplain
 *     Comparable natural ordering}. Reordering of elements is disabled by
 *     default.</li>
 * <li>the avoiding to add {@code null} elements (see {@link
 *     #nullIsAllowed(boolean)}). A {@code null} element can be added by 
 *     default.</li>
 * <li>the disabling of adding more elements than it is specified at creation
 *     time. (see {@link ArraySet#ArraySet(int)})</li>
 * </ul>
 *
 * <p><b>Example:</b>
 * <pre>{@code
 * List<Object> objects = new ArraySet<Object>(LIMIT).
 *                            ordering(true).
 *                            nullIsAllowed(false);
 * }</pre>
 *
 * @author Victor G. Vasilyev <vvg@netbeans.org>
 */
public class ArraySet<E extends Comparable<E>>
        extends ArrayList<E> implements Set<E> {
    private int limit;
    private boolean isSorted;
    private boolean isNullAllowed;

    /**
     * Creates new {@code ArraySet}.
     * @param limit max number of elements that can be added to this
     *        {@code ArraySet}. <b>Note:</b> <i>If reordering of elements is
     *        switched on then you should avoid using a big limit due to
     *        performance problems.</i> Seems 500 elements with a quick
     *        {@link Comparable#compareTo(java.lang.Object)} method
     *        implementation is OK.
     */
    public ArraySet(int limit) {
        this.limit = limit;
    }

    /**
     * {@inheritDoc}
     * @throws IllegalStateException if {@link #size()} {@code >= limit}.
     * @throws IllegalArgumentException if {@code null} element is not allowed
     * and a specified element {@code e} is {@code null}.
     */
    @Override
    public boolean add(E e) throws IllegalStateException,
                                   IllegalArgumentException {
        if(!isNullAllowed && e == null) {
            throw new IllegalArgumentException();
        }
        if(size() >= limit) {
            throw new IllegalStateException();
        }
        if (contains(e)) {
            throw new IllegalArgumentException();
        }
        if(super.add(e)) {
            if(isSorted) {
                Collections.sort(this);
            }
            return true;
        }
        return false;
    }

    /**
     * Sets mode of ordering.
     * @param doOrdering if {@code true} - ordering of elements will be allowed,
     *        and it will be performed during the next call of the
     *        {@link #add(java.lang.Object) add} method, otherwise, if {@code
     *        false} - ordering will be disabled.
     * @return this {@code ArraySet}.
     */
    public ArraySet<E> ordering(boolean doOrdering) {
        this.isSorted = doOrdering;
        return this;
    }

    /**
     * Sets mode of adding the {@code null} element.
     * @param isNullAllowed if {@code true} - adding the {@code null} element 
     *        will be allowed, otherwise, if {@code false} - adding will be 
     *        disabled.
     * @return this {@code ArraySet}.
     */
    public ArraySet<E> nullIsAllowed(boolean isNullAllowed) {
        this.isNullAllowed = isNullAllowed;
        return this;
    }

}
