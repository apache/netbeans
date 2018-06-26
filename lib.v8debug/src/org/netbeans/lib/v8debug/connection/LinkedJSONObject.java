/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.lib.v8debug.connection;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONObject;

/**
 * An implementation is {@link JSONObject} that keeps the order of added elements.
 * 
 * @author Martin Entlicher
 */
public class LinkedJSONObject extends JSONObject {

    LinkedHashMap linkedMap = new LinkedHashMap();

    public LinkedJSONObject() {
    }

    @Override
    public void clear() {
        linkedMap.clear();
    }

    @Override
    public Object clone() {
        LinkedJSONObject ljo = new LinkedJSONObject();
        ljo.linkedMap = (LinkedHashMap) linkedMap.clone();
        return ljo;
    }

    @Override
    public boolean containsKey(Object key) {
        return linkedMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return linkedMap.containsValue(value);
    }

    @Override
    public Set entrySet() {
        return linkedMap.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LinkedJSONObject)) {
            return false;
        }
        return ((LinkedJSONObject) o).linkedMap.equals(linkedMap);
    }

    @Override
    public Object get(Object key) {
        return linkedMap.get(key);
    }

    @Override
    public int hashCode() {
        return linkedMap.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return linkedMap.isEmpty();
    }

    @Override
    public Set keySet() {
        return linkedMap.keySet();
    }

    @Override
    public Object put(Object key, Object value) {
        return linkedMap.put(key, value);
    }

    @Override
    public void putAll(Map m) {
        linkedMap.putAll(m);
    }

    @Override
    public Object remove(Object key) {
        return linkedMap.remove(key);
    }

    @Override
    public int size() {
        return linkedMap.size();
    }

    @Override
    public Collection values() {
        return linkedMap.values();
    }
    
}
