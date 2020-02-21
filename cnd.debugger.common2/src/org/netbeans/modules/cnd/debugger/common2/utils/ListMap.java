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
