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
/*
 * ContextProviderSupport.java
 *
 * Created on January 27, 2004, 3:02 PM
 */

package org.netbeans.actions.spi;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.netbeans.actions.api.ContextProvider;

/** Provides a more convenient and efficient way to implement ContextProvider -
 *  assembles the context map out of a set of Contributors.  Provides two
 *  implementations of Contributor, the preferred one composing its portion of
 *  the context from simple arrays of keys/values (FixedContributor) and another
 *  version which allows the application to supply its own map.
 *  <p>
 *  Note that the contributions of the various components are not checked for
 *  duplicates (this could be added at 0 cost with assertions if needed) - if
 *  there is duplication, the actual contents of the map are undefined.  It is
 *  the responsibility of the implementer to ensure this does not happen.
 *  <p>
 *  One caveat to using this class - do not use the string &quot;identity&quot; as
 *  one of the keys for a Contributor's map (defined as ContextProvider.IDENTITY).
 *  ContextProviderSupport reserves the use of this key.
 *  <p>
 *  Note that the Maps returned by ContextProviderSupport.getContext() are 
 *  immutable - put, clear, etc. will throw an UnsupportedOperationException.
 *
 * @author  Tim Boudreau
 */
public final class ContextProviderSupport implements ContextProvider {
    private Contributor[] contributors = null;
    /** Creates a new instance of DefaultContextProvider */
    public ContextProviderSupport() {
    }
    
    public ContextProviderSupport (Contributor[] contributors) {
        setContributors(contributors);
    }
    
    /** Set the contributors that will add context information to the application
     * context */
    public void setContributors (Contributor[] contributors) { 
        //XXX maybe use a list argument so list can change dynamically?
        this.contributors = contributors;
    }
    
    /** Get a map composed of the contributions of each contributor */
    public final Map getContext() {
        if (contributors == null || contributors.length == 0) {
            return Collections.emptyMap();
        } else {
            Map[] m = new Map[contributors.length];
            for (int i=0; i < contributors.length; i++) {
                Map curr = contributors[i].getContribution();
                if (curr instanceof ActiveContributor) {
                    //Avoid concurrentModificationExceptions by making a fast
                    //clone of the map
                    curr = ((ActiveContributor) curr).toFixedMap();
                }
                m[i] = contributors[i].getContribution();
            }
            return new ProxyMap (m);
        }
    }    
    
    
    /** A contributor provides a portion of the application context used to
     * determine action availability */
    public interface Contributor {
        public Map getContribution();
    }
    
    /** Implementation of Contributor which takes fixed arrays of keys and
     * values */
    public abstract static class FixedContributor  implements Contributor {
        public final Map getContribution() {
            return new FixedMap(getKeys(), getValues());
        }
        
        public abstract String[] getKeys();
        public abstract String[] getValues();
    }
    
    /** Implementation of Contributor which allows dynamic creation of the
     * map of keys and values.  Where possible it is preferred to use 
     * FixedContributor - it is fairly unusual for an application to not know
     * enough about what state it is in to do that, and it is more efficient.
     * <p>
     * Note that the map that will actually be used is a snapshot of the map
     * returned, taken at the time of updating.
     */
    public abstract static class ActiveContributor implements Contributor {
        public abstract Map getContribution();
        
        Map toFixedMap() {
            //Hmm, should we bother?  This is just cloning the map.  Maybe
            //kill this class, although if someone uses a weird map impl,
            //this avoid concurrent modification exceptions
            return new FixedMap (getKeys(), getValues());
        }
         
        public final String[] getKeys() {
            Map m = getContribution();
            String[] result = new String[m.size()];
            try {
                return (String[]) m.keySet().toArray(result);
            } catch (ClassCastException cce) {
                System.err.println(
                "Only Strings are allowed as keys in the context map"); //NOI18N
                throw cce;
            }
        }
        
        public final Object[] getValues() {
            Map m = getContribution();
            String[] result = new String[m.size()];
            return m.values().toArray(result);
        }
    }
    
    /** Implementation of the Map interface over a pair of key/value arrays */
    static class FixedMap implements Map {
        private String[] keys;
        private Object[] values;
        public FixedMap (String[] key, Object[] values) {
            if (values.length != keys.length) {
                throw new IllegalArgumentException (
                    "Keys and values must be same length arrays"); //NOI18N
            }
            assert !Arrays.asList(key).contains(IDENTITY) :
                "The key " + IDENTITY + " is reserved by the actions framework, "
                + "and may not be used as a key"; //NOI18N
            this.keys = keys;
            this.values = values;
        }
        
        public void clear() {
            throw new UnsupportedOperationException();
        }
        
        private Integer identity = null;
        Integer getIdentity() {
            if (identity == null) {
                identity = new Integer (identityOf(keys));
            }
            return identity;
        }
        
        public boolean containsKey(Object key) {
            if (IDENTITY.equals(key)) {
                return true;
            }
            return Arrays.asList(keys).contains(key);
        }
        
        public boolean containsValue(Object value) {
            boolean result = Arrays.asList(values).contains(value);
            if (!result && getIdentity().equals(value)) {
                result = true;
            }
            return result;
        }
        
        /** Not implemented - returns the empty set */
        public java.util.Set entrySet() {
            return Collections.EMPTY_SET;
        }
        
        public Object get(Object key) {
            if (IDENTITY.equals(key)) {
                return getIdentity();
            }
            int i = Arrays.asList(keys).indexOf(key);
            if (i != -1) {
                return values[i];
            }
            return null;
        }
        
        /** Will return the real values length, not including the
         * identity key */
        public boolean isEmpty() {
            return values.length == 0;
        }
        
        public java.util.Set keySet() {
            Set result = new HashSet (Arrays.asList(keys));
            result.add(IDENTITY);
            return result;
        }
        
        public Object put(Object key, Object value) {
            throw new UnsupportedOperationException();
        }
        
        public void putAll(Map t) {
            throw new UnsupportedOperationException();
        }
        
        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }
        
        public int size() {
            //add 1 for identity
            return keys.length == 0 ? 0 : keys.length + 1;
        }
        
        public java.util.Collection values() {
            return new HashSet (Arrays.asList(values));
        }
        
    }

    /** Utility method that returns a String array of the keys in a map
     * (only called if we're not dealing with a fixed map).
     */
    private static String[] keys(Map m) {
        Set keys = new HashSet(m.keySet());
        keys.remove(IDENTITY);
        String[] result = new String[keys.size()];
        return (String[]) keys.toArray(result);
    }

    /** Returns a unique hash of an array of strings.  Used for composing an
     * identity value for multiple maps, and by FixedMap to compute its identity
     * value */
    private static int identityOf (String[] st) {
        if (st.length == 0) {
            return 0;
        } else {
            int result = 0;
            for (int i=0; i < st.length; i++) {
                result ^= st.hashCode();
            }
            return result;
        }
    }
    
    /** A map that proxies an array of maps.  Note that due to its nature,
     * it is possible to have duplicate keys - filtering them doesn't make
     * sense for performance reasons.  */
    static class ProxyMap implements Map {
        private Map[] maps;
        public ProxyMap (Map[] maps) {
            this.maps = maps;
        }
        
        private Integer identity = null;
        private Integer getIdentity() {
            if (identity == null) {
                computeIdentity();
            }
            return identity;
        }
        
        private void computeIdentity() {
            int result = 0;
            for (int i=0; i < maps.length; i++) {
                //Some minor optimizations for known types
                if (maps[i] instanceof FixedMap) {
                    result ^= ((FixedMap) maps[i]).getIdentity().intValue();
                } else if (maps[i] instanceof ProxyMap) {
                    result ^= ((ProxyMap) maps[i]).getIdentity().intValue();
                } else {
                    result ^= identityOf (keys(maps[i]));
                }
            }
            identity = new Integer(result);
        }
        
        //XXX more effcient may be to simply compose a real hashmap on any
        //call that accesses one of the proxied maps, and then proxy that
        //for the rest of the ProxyMap's lifetime.  Would also cure the
        //duplicate problem.
        
        public void clear() {
            throw new UnsupportedOperationException();
        }
        
        public boolean containsKey(Object key) {
            if (IDENTITY.equals(key)) {
                return true;
            }
            boolean result = false;
            for (int i=0; i < maps.length; i++) {
                result |= maps[i].containsKey(key);
                if (result) {
                    break;
                }
            }
            return result;
        }
        
        public boolean containsValue(Object value) {
            boolean result = false;
            for (int i=0; i < maps.length; i++) {
                result |= maps[i].containsValue(value);
                if (result) {
                    break;
                }
            }
            if (!result && value.equals(getIdentity())) {
                return true;
            }
            return result;
        }
        
        public java.util.Set entrySet() {
            return Collections.EMPTY_SET;
        }
        
        public Object get(Object key) {
            if (IDENTITY.equals(key)) {
                return getIdentity();
            }
            Object result = null;
            for (int i=0; i < maps.length; i++) {
                result = maps[i].get(key);
                if (result != null) {
                    break;
                }
            }
            return result;
        }
        
        /** Note this does not account for the IDENTITY key or it would
         * always be true */
        public boolean isEmpty() {
            boolean result = maps.length == 0;
            if (!result) {
                for (int i=0; i < maps.length; i++) {
                    result &= maps[i].isEmpty();
                    if (result) {
                        break;
                    }
                }
            }
            return result;
        }
        
        public Set keySet() {
            Set result = new HashSet();
            for (int i=0; i < maps.length; i++) {
                result.addAll(maps[i].keySet());
            }
            result.add(IDENTITY);
            return result;
        }
        
        public Object put(Object key, Object value) {
            throw new UnsupportedOperationException();
        }
        
        public void putAll(Map t) {
            throw new UnsupportedOperationException();
        }
        
        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }
        
        public int size() {
            int result = 0;
            if (maps.length > 0) {
                for (int i=0; i < maps.length; i++) {
                    result += maps[i].size();
                }
            }
            //Add one for the identity key.
            return result + 1;
        }
        
        public java.util.Collection values() {
            if (maps.length == 0) {
                return Collections.EMPTY_SET;
            } else {
                Set result = new HashSet();
                for (int i=0; i < maps.length; i++) {
                    result.addAll(maps[i].values());
                }
                result.add (getIdentity());
                return result;
            }
        }
    }
}
