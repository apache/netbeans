/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.modelimpl.util;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * A list that keeps weak references to its elements
 */
public class WeakList<T> implements Iterable<T> {

    private final List<WeakReference<T>> list = new ArrayList<>();

    /**
     * Adds a weak reference to the given element to this list
     */
    public synchronized void add(T element) {
        list.add(new WeakReference<>(element));
    }

    /**
     * Adds all weak references frim the given iterator to this list
     */
    public synchronized void addAll(Iterator<T> elements) {
        while (elements.hasNext()) {
            list.add(new WeakReference<>(elements.next()));
        }
    }

    /*
     * Removes all references to the given element from this list
     */
    public synchronized void remove(T element) {
        for (Iterator<WeakReference<T>> it = list.iterator(); it.hasNext();) {
            WeakReference<T> ref = it.next();
            if (ref.get() == element) {
                it.remove();
            }
        }
    }

    /** Removes all elements */
    public synchronized void clear() {
        list.clear();
    }

    /** 
     * Returns an iterator of non-null references.
     * NB: it iterates over a snapshot made at the moment of the call
     */
    @Override
    public synchronized Iterator<T> iterator() {
        List<T> result = new ArrayList<>();
        addTo(result);
        return result.iterator();
    }

    public synchronized Collection<T> join(Collection<? extends T> collection) {
        List<T> result = new ArrayList<>(collection.size() + list.size());
        result.addAll(collection);
        addTo(result);
        return result;
    }

    private void addTo(Collection<T> collection) {
        for (Iterator<WeakReference<T>> it = list.iterator(); it.hasNext();) {
            WeakReference<T> ref = it.next();
            T element = ref.get();
            if (element != null) {
                collection.add(element);
            }
        }
    }
}
