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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.dbschema.jdbcimpl.wizard;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.swing.AbstractListModel;


/**
 *
 */
public class SortedListModel extends AbstractListModel
{

    /**
     *
     */
    public static final Comparator DEFAULT_COMPARATOR = new Comparator()
    {
        public int compare(Object o1, Object o2)
        {
            if (o1 == null)
                return -1;

            if (o2 == null)
                return 1;

            return o1.toString().compareTo(o2.toString());
        }

        public boolean equals(Object obj)
        {
            return obj == this;
        }
    };

    /**
     *
     */
    private List elements;

    /**
     *
     */
    private Comparator comp = DEFAULT_COMPARATOR;

    ///////////////////////////////////////////////////////////////////////////
    // construction
    ///////////////////////////////////////////////////////////////////////////

    /**
     *
     */
    public SortedListModel()
    {
        elements = new ArrayList();
    }

    /**
     *
     */
    public SortedListModel(Collection c)
    {
        elements = new ArrayList(c);
        Collections.sort(elements, comp);
    }

    /**
     *
     */
    public SortedListModel(int initialCapacity)
    {
        elements = new ArrayList(initialCapacity);
    }

    /**
     *
     */
    public int getSize()
    {
        return elements.size();
    }

    /**
     *
     */
    public Object getElementAt(int index)
    {
        return elements.get(index);
    }

    /**
     * Returns the comparator used to sort the elements of this list model.
     *
     * @see #setComparator
     */
    public Comparator getComparator()
    {
        return comp;
    }

    /**
     *
     */
    public void setComparator(Comparator newComp)
    {
        if (comp == newComp)
            return;

        comp = newComp;
        Collections.sort(elements, comp);

        int last = elements.size() - 1;

        if (last >= 0)
            super.fireContentsChanged(this, 0, last);
    }

    /**
     * Returns <code>true</code> if this list model contains no elements.
     */
    public boolean isEmpty()
    {
        return elements.isEmpty();
    }

    /**
     *
     */
    public boolean contains(Object o)
    {
        return Collections.binarySearch(elements, o, getComparator()) >= 0;
    }

    /**
     *
     */
    public Object[] toArray()
    {
        return elements.toArray();
    }

    /**
     *
     */
    public Object[] toArray(Object[] a)
    {
        return elements.toArray(a);
    }

    /**
     *
     */
    public int add(Object o)
    {
        int index = Collections.binarySearch(elements, o, getComparator());
        if (index < 0)
            index = -index - 1;

        elements.add(index, o);
        fireIntervalAdded(this, index, index);

        return index;
    }

    /**
     *
     */
    public int indexOf(Object o)
    {
        return Collections.binarySearch(elements, o, getComparator());
    }

    /**
     *
     */
    public int remove(Object o)
    {
        int index = Collections.binarySearch(elements, o, getComparator());
        if (index >= 0)
        {
            remove(index);
        }
        return index;
    }

    /**
     *
     */
    public boolean remove(int index)
    {
        elements.remove(index);
        fireIntervalRemoved(this, index, index);

        return true;
    }

    /**
     *
     */
    public void clear()
    {
        int last = elements.size() - 1;

        if (last >= 0)
        {
            elements.clear();
            fireIntervalRemoved(this, 0, last);
        }
    }

    /**
     * Returns a string that displays and identifies this
     * object's properties.
     *
     * @return a String representation of this object
     */
    public String toString()
    {
        return elements.toString();
    }
}
