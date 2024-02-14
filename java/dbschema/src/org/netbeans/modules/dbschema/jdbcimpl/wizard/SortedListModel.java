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
        elements.sort(comp);
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
        elements.sort(comp);

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
