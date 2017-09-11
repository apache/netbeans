/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils.helper;

import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.HashMap;

/**
 * @noinspection ALL
 */
//todo: not tested yet so use arefully
public class MutualHashMap<F, S> implements MutualMap<F, S> {

  private Map<F, S> map = new HashMap<F, S>();
  private Map<S, F> reversedMap = new HashMap<S, F>();

  public int size() {
    return map.size();
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  public S get(Object key) {
    return map.get(key);
  }

  public F reversedGet(S object) {
    return reversedMap.get(object);
  }

  public S put(F key, S value) {
    reversedMap.put(value, key);
    return map.put(key, value);
  }

  public S remove(Object key) {
    final S removed = map.remove(key);
    reversedMap.remove(removed);
    return removed;
  }

  public F reversedRemove(Object value) {
    final F removed = reversedMap.remove(value);
    map.remove(removed);
    return removed;
  }

  public void putAll(Map<? extends F, ? extends S> m) {
    map.putAll(m);
    for (Map.Entry<? extends F, ? extends S> entry : m.entrySet()) {
      reversedMap.put(entry.getValue(), entry.getKey());
    }
  }

  public void clear() {
    map.clear();
    reversedMap.clear();
  }

  public Set<F> keySet() {
    return map.keySet();
  }

  public Collection<S> values() {
    return map.values();
  }

  public Set<Entry<F, S>> entrySet() {
    return map.entrySet();
  }

  public Set<Entry<S, F>> reversedEntrySet() {
    return reversedMap.entrySet();
  }
}
