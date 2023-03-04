/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
