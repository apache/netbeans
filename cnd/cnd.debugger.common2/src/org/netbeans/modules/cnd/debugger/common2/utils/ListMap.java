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

/*
 * "ListMap.java"
 */

package org.netbeans.modules.cnd.debugger.common2.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * A bit like LinkedHashMap.
 * ListMapItems that are added awlays get stored in a list.
 * If ListMapItems have a key they also get stored in a Map.
 * ListMapItems can be retrieved by their key.
 * ListMapItems can be iterated over and removed by item (as opposed to by key).
 */

public class ListMap<O extends ListMapItem> implements Iterable<O> {

    private final HashMap<Object, O> map = new HashMap<Object, O>();
    private final List<O> list = new CopyOnWriteArrayList<O>();

    public O byKey(Object key) {
	return map.get(key);
    }

    public void add(O w) {
	if (w.hasKey())
	    map.put(w.getKey(), w);
	list.add(w);
    }

    public O remove(O w) {
	O removed = null;
	if (w.hasKey()) {
	    removed = map.remove(w.getKey());
            /*
	    assert removed != null :
		   "ListMap.remove(): " +
		   "object with key " + w.getKey() + " not in map";
             */
	}
	list.remove(w);
	return removed;
    }

    public void replaceWith(O o, O n) {
	this.remove(o);
	this.add(n);
    }

    public int size() {
	return list.size();
    }

    // interface Iterable
    @Override
    public Iterator<O> iterator() {
	return list.iterator();
    }

    public O[] toArray(O[] array) {
	list.toArray(array);
	return array;
    }
}
