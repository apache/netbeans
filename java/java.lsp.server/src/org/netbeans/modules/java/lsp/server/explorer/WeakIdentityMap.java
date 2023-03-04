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
package org.netbeans.modules.java.lsp.server.explorer;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements a combination of {@link java.util.WeakHashMap} and {@link java.util.IdentityHashMap}.
 * Useful for caches that need to key off of a {@code ==} comparison instead of a {@code .equals}.
 *
 * The code was originally part of Apache Lucene
 */
public final class WeakIdentityMap<K, V> {
  private final ReferenceQueue<Object> queue = new ReferenceQueue<>();
  private final Map<IdentityWeakReference, V> backingStore;
  private final boolean reapOnRead;

  /**
   * Creates a new {@code WeakIdentityMap} based on a non-synchronized {@link HashMap}. The map <a
   * href="#reapInfo">cleans up the reference queue on every read operation</a>.
   */
  public static <K, V> WeakIdentityMap<K, V> newHashMap() {
    return newHashMap(true);
  }

  /**
   * Creates a new {@code WeakIdentityMap} based on a non-synchronized {@link HashMap}.
   *
   * @param reapOnRead controls if the map <a href="#reapInfo">cleans up the reference queue on
   *     every read operation</a>.
   */
  public static <K, V> WeakIdentityMap<K, V> newHashMap(boolean reapOnRead) {
    return new WeakIdentityMap<>(new HashMap<IdentityWeakReference, V>(), reapOnRead);
  }

  /**
   * Creates a new {@code WeakIdentityMap} based on a {@link ConcurrentHashMap}. The map <a
   * href="#reapInfo">cleans up the reference queue on every read operation</a>.
   */
  public static <K, V> WeakIdentityMap<K, V> newConcurrentHashMap() {
    return newConcurrentHashMap(true);
  }

  /**
   * Creates a new {@code WeakIdentityMap} based on a {@link ConcurrentHashMap}.
   *
   * @param reapOnRead controls if the map <a href="#reapInfo">cleans up the reference queue on
   *     every read operation</a>.
   */
  public static <K, V> WeakIdentityMap<K, V> newConcurrentHashMap(boolean reapOnRead) {
    return new WeakIdentityMap<>(new ConcurrentHashMap<IdentityWeakReference, V>(), reapOnRead);
  }

  /** Private only constructor, to create use the static factory methods. */
  private WeakIdentityMap(Map<IdentityWeakReference, V> backingStore, boolean reapOnRead) {
    this.backingStore = backingStore;
    this.reapOnRead = reapOnRead;
  }

  /** Removes all of the mappings from this map. */
  public void clear() {
    backingStore.clear();
    reap();
  }

  /** Returns {@code true} if this map contains a mapping for the specified key. */
  public boolean containsKey(Object key) {
    if (reapOnRead) reap();
    return backingStore.containsKey(new IdentityWeakReference(key, null));
  }

  /** Returns the value to which the specified key is mapped. */
  public V get(Object key) {
    if (reapOnRead) reap();
    return backingStore.get(new IdentityWeakReference(key, null));
  }

  /**
   * Associates the specified value with the specified key in this map. If the map previously
   * contained a mapping for this key, the old value is replaced.
   */
  public V put(K key, V value) {
    reap();
    return backingStore.put(new IdentityWeakReference(key, queue), value);
  }

  /** Returns {@code true} if this map contains no key-value mappings. */
  public boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Removes the mapping for a key from this weak hash map if it is present. Returns the value to
   * which this map previously associated the key, or {@code null} if the map contained no mapping
   * for the key. A return value of {@code null} does not necessarily indicate that the map
   * contained.
   */
  public V remove(Object key) {
    reap();
    return backingStore.remove(new IdentityWeakReference(key, null));
  }

  /**
   * Returns the number of key-value mappings in this map. This result is a snapshot, and may not
   * reflect unprocessed entries that will be removed before next attempted access because they are
   * no longer referenced.
   */
  public int size() {
    if (backingStore.isEmpty()) return 0;
    if (reapOnRead) reap();
    return backingStore.size();
  }

  /**
   * Returns an iterator over all weak keys of this map. Keys already garbage collected will not be
   * returned. This Iterator does not support removals.
   */
  public Iterator<K> keyIterator() {
    reap();
    final Iterator<IdentityWeakReference> iterator = backingStore.keySet().iterator();
    // IMPORTANT: Don't use oal.util.FilterIterator here:
    // We need *strong* reference to current key after setNext()!!!
    return new Iterator<K>() {
      // holds strong reference to next element in backing iterator:
      private Object next = null;
      // the backing iterator was already consumed:
      private boolean nextIsSet = false;

      @Override
      public boolean hasNext() {
        return nextIsSet || setNext();
      }

      @Override
      @SuppressWarnings("unchecked")
      public K next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        assert nextIsSet;
        try {
          return (K) next;
        } finally {
          // release strong reference and invalidate current value:
          nextIsSet = false;
          next = null;
        }
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

      private boolean setNext() {
        assert !nextIsSet;
        while (iterator.hasNext()) {
          next = iterator.next().get();
          if (next == null) {
            // the key was already GCed, we can remove it from backing map:
            iterator.remove();
          } else {
            // unfold "null" special value:
            if (next == NULL) {
              next = null;
            }
            return nextIsSet = true;
          }
        }
        return false;
      }
    };
  }

  /**
   * Returns an iterator over all values of this map. This iterator may return values whose key is
   * already garbage collected while iterator is consumed, especially if {@code reapOnRead} is
   * {@code false}.
   */
  public Iterator<V> valueIterator() {
    if (reapOnRead) reap();
    return backingStore.values().iterator();
  }

  /**
   * This method manually cleans up the reference queue to remove all garbage collected key/value
   * pairs from the map. Calling this method is not needed if {@code reapOnRead = true}. Otherwise
   * it might be a good idea to call this method when there is spare time (e.g. from a background
   * thread).
   *
   * @see <a href="#reapInfo">Information about the <code>reapOnRead</code> setting</a>
   */
  public void reap() {
    Reference<?> zombie;
    while ((zombie = queue.poll()) != null) {
      backingStore.remove(zombie);
    }
  }

  // we keep a hard reference to our NULL key, so map supports null keys that never get GCed:
  static final Object NULL = new Object();

  private static final class IdentityWeakReference extends WeakReference<Object> {
    private final int hash;

    IdentityWeakReference(Object obj, ReferenceQueue<Object> queue) {
      super(obj == null ? NULL : obj, queue);
      hash = System.identityHashCode(obj);
    }

    @Override
    public int hashCode() {
      return hash;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o instanceof IdentityWeakReference) {
        final IdentityWeakReference ref = (IdentityWeakReference) o;
        if (this.get() == ref.get()) {
          return true;
        }
      }
      return false;
    }
  }
}