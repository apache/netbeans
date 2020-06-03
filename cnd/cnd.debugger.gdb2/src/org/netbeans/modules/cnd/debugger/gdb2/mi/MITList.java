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

package org.netbeans.modules.cnd.debugger.gdb2.mi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Representation of a 'tuple/list' combo sub-tree from the MI spec.
 * A list can be one of:
 * <ul>
 * <li>"[]"
 * <li>"[" tlist-item ( "," tlist-item )* "]"
 * <li>"{" tlist-item ( "," tlist-item )* "}"
 * </ul>
 */

public class MITList extends MIValue implements Iterable<MITListItem> {

    private final ArrayList<MITListItem> list;
    private final boolean isList;
    private final boolean topLevel;

    private boolean sawResults;
    private boolean sawValues;

    private static final Logger LOG = Logger.getLogger(MITList.class.toString());

    MITList(boolean isList, boolean topLevel) {
	list = new ArrayList<MITListItem>();
	this.isList = isList;
	this.topLevel = topLevel;
    }

    @Override
    public String toString() {
	StringBuilder s = new StringBuilder();
	if (!topLevel)
	    s.append(isList()? '[': '{'); // NOI18N
	for (int vx = 0; vx < list.size(); vx++) {
	    /* OLD
	    if (isValueList) {
		MIValue value = (MIValue) list.get(vx);
		s += value.toString();
	    } else {
		MIResult result = (MIResult) list.get(vx);
		s += result.toString();
	    }
	    */
	    MITListItem item = list.get(vx);
	    s.append(item.toString());

	    if (vx+1 < list.size())
		s.append(','); // NOI18N
	}
	if (!topLevel)
	    s.append(isList()? ']': '}'); // NOI18N
	return s.toString();
    }


    // interface MIValue
    public boolean isList() {
	return isList;
    }

    @Override
    public boolean isTList() {
	return true;
    }

    @Override
    public Iterator<MITListItem> iterator() {
        return list.iterator();
    }

    /* OLD
    // interface MIValue
    @Deprecated
    public MIList asList() {
	return null;
    }
    */

    @Override
    public MITList asTList() { return this; }

    @Override
    public MITList asTuple() { return this; }

    @Override
    public MITList asList() { return this; }



    /**
     * Return true if list is of this form:
     * <br>
     * "[]"
     */

    public boolean isEmpty() {
	return list.isEmpty();
    }

    /**
     * return size of list
     */
    public int size() {
	return list.size();
    }

    public boolean isValueList() {
	return sawValues;
    }

    public boolean isResultList() {
	return sawResults;
    }

    /**
     * return one entry by index
     */
    public MITListItem get(int x) {
	return list.get(x);
    }

    void add(MIResult result) {
	assert !sawValues : "Adding results to a value list";
	sawResults = true;
	list.add(result);
    }

    void add(MIValue value) {
	assert !sawResults : "Adding values to a result list";
	sawValues = true;
	list.add(value);
    }

    public <Type> Iterable<Type> getOnly(final Class<Type> cls) {
        final Iterator<MITListItem> iterator = iterator();

        return new Iterable<Type>() {
            @Override
            public Iterator<Type> iterator() {
                return new Iterator<Type>() {
                    private Type item = nextImpl();

                    private Type nextImpl() {
                        while (iterator.hasNext()) {
                            MITListItem next = iterator.next();
                            if (cls.isInstance(next)) {
                                return cls.cast(next);
                            }
                        }
                        return null;
                    }

                    @Override
                    public boolean hasNext() {
                        return item != null;
                    }

                    @Override
                    public Type next() {
                        if (item == null) {
                            throw new NoSuchElementException();
                        }
                        Type res = item;
                        item = nextImpl();
                        return res;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Not supported."); //NOI18N
                    }
                };
            }
        };
    }

    /**
     * Retrieve the value of the given variable in this tuple.
     *
     * Will return null if no match for variable is found.
     */

    public MIValue valueOf(String variable) {
	//assert sawResults : "Getting value from a result list";
        for (MITListItem item : list) {
            if (item instanceof MIResult) {
                MIResult result = (MIResult) item;
                if (result.matches(variable)) {
                    return result.value();
                }
            } else {
                LOG.log(Level.WARNING, "Trying to get value from a result list :{0}", this);
            }
	}
	return null;
    }

    /**
     * Get const value directly
     * @param variable - name of the const variable in the list
     * @return value of the const or the empty string if no such const is in the list
     */
    public String getConstValue(String variable) {
        return getConstValue(variable, "");
    }

    /**
     * Get const value directly
     * @param variable - name of the const variable in the list
     * @param defaultValue  - value to return if there is no such const is in the list
     * @return value of the const or the defaultvalue
     */
    public String getConstValue(String variable, String defaultValue) {
        MIValue val = valueOf(variable);
        if (val != null) {
            return val.asConst().value();
        }
        return defaultValue;
    }
}
