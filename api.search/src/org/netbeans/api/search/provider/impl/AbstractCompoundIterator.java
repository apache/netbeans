/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.api.search.provider.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchListener;

/**
 *
 * @author jhavlin
 */
public abstract class AbstractCompoundIterator<E, T> implements Iterator<T> {

    /**
     *
     */
    private final E[] elements;
    /**
     *
     */
    private int elementIndex;
    /**
     *
     */
    private Iterator<T> elementIterator = null;
    /**
     *
     */
    private T nextObject;
    /**
     *
     */
    private boolean upToDate;
    private SearchScopeOptions options;
    private SearchListener listener;
    private AtomicBoolean terminated;

    /**
     * Creates a new instance of
     * <code>CompoundSearchIterator</code>.
     *
     * @param elements elements of the compound iterator
     * @exception java.lang.IllegalArgumentException if the argument is
     * <code>null</code>
     */
    public AbstractCompoundIterator(E[] elements,
            SearchScopeOptions options, SearchListener listener,
            AtomicBoolean terminated) {

        if (elements == null) {
            throw new IllegalArgumentException("Elements are null");    //NOI18N
        } else if (options == null) {
            throw new IllegalArgumentException("Options are null");     //NOI18N
        }

        this.options = options;

        if (elements.length == 0) {
            this.elements = null;
            elementIndex = 0;
            upToDate = true;                //hasNext() returns always false
        } else {
            this.elements = elements;
            upToDate = false;
        }
        this.listener = listener;
        this.terminated = terminated;
    }

    /**
     */
    @Override
    public boolean hasNext() {
        if (!upToDate) {
            update();
        }
        return (elements != null) && (elementIndex < elements.length);
    }

    /**
     */
    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        upToDate = false;
        return nextObject;
    }

    /**
     */
    private void update() {
        assert upToDate == false;

        if (elementIterator == null) {
            elementIterator = getIteratorFor(elements[elementIndex = 0], options,
                    listener, terminated);
        }

        while (!elementIterator.hasNext()) {
            elements[elementIndex] = null;

            if (++elementIndex == elements.length) {
                break;
            }
            elementIterator = getIteratorFor(elements[elementIndex], options,
                    listener, terminated);
        }

        if (elementIndex < elements.length) {
            nextObject = elementIterator.next();
        } else {
            elementIterator = null;
            nextObject = null;
        }

        upToDate = true;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    protected abstract Iterator<T> getIteratorFor(E element,
            SearchScopeOptions options, SearchListener listener,
            AtomicBoolean terminated);
}
