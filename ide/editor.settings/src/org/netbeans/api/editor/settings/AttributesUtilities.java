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

package org.netbeans.api.editor.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;

/**
 * A collection of utility menthods for working with <code>AttributeSet</code>s.
 *
 * @author Vita Stejskal
 */
public final class AttributesUtilities {

    private static final String ATTR_DISMANTLED_STRUCTURE = "dismantled-structure"; //NOI18N
    
    /**
     * Creates an immutable <code>AttributeSet</code>, which will contain the
     * <code>keyValuePairs</code> attributes. If the pairs
     * contain attributes with the same name the resulting <code>AttributeSet</code>
     * will return value of the first attribute it will find going through
     * the pairs in the order as they were passed in.
     *
     * @param keyValuePairs    The contents of the <code>AttributeSet</code> created
     *                         by this method. This parameter should list pairs
     *                         of key-value objects; each pair defining one attribute.
     *
     * @return The new immutable <code>AttributeSet</code>.
     */
    public static AttributeSet createImmutable(Object... keyValuePairs) {
        assert keyValuePairs.length % 2 == 0 : "There must be even number of prameters. " +
            "They are key-value pairs of attributes that will be inserted into the set.";
        if (true) {
            return org.netbeans.modules.editor.settings.AttrSet.get(keyValuePairs);
        }
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        
        for(int i = keyValuePairs.length / 2 - 1; i >= 0 ; i--) {
            Object attrKey = keyValuePairs[2 * i];
            Object attrValue = keyValuePairs[2 * i + 1];

            map.put(attrKey, attrValue);
        }
        
        return map.size() > 0 ? new Immutable(map) : SimpleAttributeSet.EMPTY;
    }

    /**
     * Creates an immutable <code>AttributeSet</code> as a copy of <code>AttributeSet</code>s
     * passed into this method. If the <code>AttributeSet</code>s
     * contain attributes with the same name the resulting <code>AttributeSet</code>
     * will return value of the first attribute it will find going through
     * the sets in the order as they were passed in.
     *
     * @param sets    The <code>AttributeSet</code>s which attributes will become
     *                a contents of the newly created <code>AttributeSet</code>.
     *
     * @return The new immutable <code>AttributeSet</code>.
     */
    public static AttributeSet createImmutable(AttributeSet... sets) {
        if (true) {
            return org.netbeans.modules.editor.settings.AttrSet.merge(sets);
        }
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        
        for(int i = sets.length - 1; i >= 0; i--) {
            AttributeSet set = sets[i];
            for(Enumeration<?> keys = set.getAttributeNames(); keys.hasMoreElements(); ) {
                Object attrKey = keys.nextElement();
                Object attrValue = set.getAttribute(attrKey);

                map.put(attrKey, attrValue);
            }
        }

        return map.size() > 0 ? new Immutable(map) : SimpleAttributeSet.EMPTY;
    }

    /**
     * Creates a proxy <code>AttributeSet</code> that will delegate to the
     * <code>AttributeSet</code>s passed in as a parameter. If the <code>AttributeSet</code>s
     * contain attributes with the same name the composite <code>AttributeSet</code>
     * will return value of the first attribute it will find going through
     * the sets in the order as they were passed in.
     *
     * @param sets    The <code>AttributeSet</code>s to delegate to.
     *
     * @return The new composite <code>AttributeSet</code> that will delegate
     *         to the <code>sets</code> passed in.
     */
    public static AttributeSet createComposite(AttributeSet... sets) {
        if (true) {
            return org.netbeans.modules.editor.settings.AttrSet.merge(sets);
        }
        if (sets.length == 0) {
            return SimpleAttributeSet.EMPTY;
        } else if (sets.length == 1) {
            return sets[0];
        } else {
            LinkedList<AttributeSet> all = new LinkedList<AttributeSet>();

            for(AttributeSet s : sets) {
                if (s instanceof AttributesUtilities.CompositeAttributeSet) {
                    all.addAll(((AttributesUtilities.CompositeAttributeSet) s).getDelegates());
                } else if (s != null && s != SimpleAttributeSet.EMPTY) {
                    all.add(s);
                }
            }

            switch (all.size()) {
                case 0: return SimpleAttributeSet.EMPTY;
                case 1: return all.get(0);
                case 2: return new Composite2(all.get(0), all.get(1));
                case 3: return new Composite4(all.get(0), all.get(1), all.get(2), null);
                case 4: return new Composite4(all.get(0), all.get(1), all.get(2), all.get(3));
                default: return new BigComposite(all);
            }
        }
    }

    private static List<AttributeSet> dismantle(AttributeSet set) {
        ArrayList<AttributeSet> sets = new ArrayList<AttributeSet>();
        
        if (set instanceof CompositeAttributeSet) {
            Collection<? extends AttributeSet> delegates = ((CompositeAttributeSet) set).getDelegates();
            for(AttributeSet delegate : delegates) {
                sets.addAll(dismantle(delegate));
            }
        } else {
            sets.add(set);
        }
        
        return sets;
    }
    
    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------

    private AttributesUtilities() {
        // no-op, just to prevent instantiation
    }
    
    private static class Immutable implements AttributeSet {
        
        private final HashMap<Object, Object> attribs;
        private AttributeSet parent = null;

        /** Creates a new instance of SmartAttributeSet */
        private Immutable(HashMap<Object, Object> attribs) {
            this.attribs = attribs == null ? new HashMap<Object, Object>() : attribs;
            Object resolver = this.attribs.get(AttributeSet.ResolveAttribute);
            if(resolver instanceof AttributeSet){
            	setResolveParent((AttributeSet)resolver);
            }else{
            	//broken or null resolver key. Ignore (log if different class maybe ?)
            }
        }

        public synchronized void setResolveParent(AttributeSet parent) {
            this.parent = parent;
        }

        public synchronized boolean containsAttributes(AttributeSet attributes) {
            for(Enumeration<?> names = attributes.getAttributeNames(); names.hasMoreElements(); ) {
                Object name = names.nextElement();
                Object value = attributes.getAttribute(name);

                if (!containsAttribute(name, value)) {
                    return false;
                }
            }

            return true;
        }

        public synchronized boolean isEqual(AttributeSet attr) {
            return containsAttributes(attr) && attr.containsAttributes(this);
        }

        public synchronized Object getAttribute(Object key) {

            // Somebody is asking for the parent
            if (AttributeSet.ResolveAttribute == key) {
                return parent;
            }

            // Get the normal value
            if (attribs.containsKey(key)) {
                return attribs.get(key);
            }

            // Value not found, try parent if we have any
            if (parent != null) {
                return parent.getAttribute(key);
            } else {
                return null;
            }
        }

        public synchronized boolean isDefined(Object key) {
            return attribs.containsKey(key);
        }

        public synchronized boolean containsAttribute(Object key, Object value) {
            if (attribs.containsKey(key)) {
                Object attrValue = attribs.get(key);
                if ((value == null && attrValue == null) || 
                    (value != null && attrValue != null && value.equals(attrValue))
                ) {
                    return true;
                }
            }

            return false;
        }

        public AttributeSet copyAttributes() {
            return new Proxy(this);
        }

        /**
         * This is really slow don't use it!
         */
        public synchronized int getAttributeCount() {
            return attribs.size();
        }

        /**
         * This is really slow don't use it!
         */
        public synchronized Enumeration<?> getAttributeNames() {
            return Collections.enumeration(attribs.keySet());
        }

        public synchronized AttributeSet getResolveParent() {
            return parent;
        }

    } // End of Immutable class
    
    private static final class Proxy implements AttributeSet, CompositeAttributeSet {
        
        private AttributeSet original;
        
        public Proxy(AttributeSet original) {
            this.original = original;
        }

        public Collection<? extends AttributeSet> getDelegates() {
            return Arrays.asList(original);
        }
        
        public boolean isEqual(AttributeSet attr) {
            return original.isEqual(attr);
        }

        public boolean containsAttributes(AttributeSet attributes) {
            return original.containsAttributes(attributes);
        }

        public boolean isDefined(Object attrName) {
            return original.isDefined(attrName);
        }

        public Object getAttribute(Object key) {
            if (key instanceof String && key.equals(ATTR_DISMANTLED_STRUCTURE)) {
                return dismantle(this);
            } else {
                return original.getAttribute(key);
            }
        }

        public AttributeSet getResolveParent() {
            return original.getResolveParent();
        }

        public Enumeration<?> getAttributeNames() {
            return original.getAttributeNames();
        }

        public int getAttributeCount() {
            return original.getAttributeCount();
        }

        public AttributeSet copyAttributes() {
            return original.copyAttributes();
        }

        public boolean containsAttribute(Object name, Object value) {
            return original.containsAttribute(name, value);
        }
    } // End of Proxy class

    private static interface CompositeAttributeSet {
        public Collection<? extends AttributeSet> getDelegates();
    }

    private static final class BigComposite implements AttributeSet, CompositeAttributeSet {
        
        private final AttributeSet[] delegates;
        
        public BigComposite(List<AttributeSet> delegates) {
            this.delegates = delegates.toArray(new AttributeSet[0]);
        }

        public Collection<? extends AttributeSet> getDelegates() {
            return Arrays.asList(delegates);
        }
        
        public boolean isEqual(AttributeSet attr) {
            return containsAttributes(attr) && attr.containsAttributes(this);
        }

        public boolean containsAttributes(AttributeSet attributes) {
            for(Enumeration<?> keys = attributes.getAttributeNames(); keys.hasMoreElements(); ) {
                Object key = keys.nextElement();
                Object value = attributes.getAttribute(key);
                
                if (!containsAttribute(key, value)) {
                    return false;
                }
            }
            
            return true;
        }

        public boolean isDefined(Object key) {
            for(AttributeSet delegate : delegates) {
                if (delegate.isDefined(key)) {
                    return true;
                }
            }

            return false;
        }

        public Object getAttribute(Object key) {
            if (key instanceof String && key.equals(ATTR_DISMANTLED_STRUCTURE)) {
                return dismantle(this);
            }
            
            for(AttributeSet delegate : delegates) {
            	AttributeSet current = delegate;
            	while (current != null) {
            		if (current.isDefined(key)) {
            			return current.getAttribute(key);
            		}
            		current = current.getResolveParent();
            	}
            }

            return null;
        }

        public AttributeSet getResolveParent() {
            return null;
        }

        public Enumeration<?> getAttributeNames() {
            return Collections.enumeration(getAllKeys());
        }

        public int getAttributeCount() {
            return getAllKeys().size();
        }

        public AttributeSet copyAttributes() {
            return createImmutable(delegates);
        }

        public boolean containsAttribute(Object key, Object value) {
            for(AttributeSet delegate : delegates) {
                if (delegate.containsAttribute(key, value)) {
                    return true;
                }
            }

            return false;
        }
        
        private Collection<?> getAllKeys() {
            HashSet<Object> allKeys = new HashSet<Object>();

            for(AttributeSet delegate : delegates) {
                for(Enumeration<?> keys = delegate.getAttributeNames(); keys.hasMoreElements(); ) {
                    Object key = keys.nextElement();
                    allKeys.add(key);
                }
            }

            return allKeys;
        }
    } // End of BigComposite class

    private static final class Composite2 implements AttributeSet, CompositeAttributeSet {

        private final AttributeSet delegate0;
        private final AttributeSet delegate1;

        public Composite2(AttributeSet delegate0, AttributeSet delegate1) {
            this.delegate0 = delegate0;
            this.delegate1 = delegate1;
        }

        public Collection<? extends AttributeSet> getDelegates() {
            return Arrays.asList(delegate0, delegate1);
        }

        public boolean isEqual(AttributeSet attr) {
            return containsAttributes(attr) && attr.containsAttributes(this);
        }

        public boolean containsAttributes(AttributeSet attributes) {
            for(Enumeration<?> keys = attributes.getAttributeNames(); keys.hasMoreElements(); ) {
                Object key = keys.nextElement();
                Object value = attributes.getAttribute(key);

                if (!containsAttribute(key, value)) {
                    return false;
                }
            }

            return true;
        }

        public boolean isDefined(Object key) {
            return delegate0.isDefined(key) || delegate1.isDefined(key);
        }

        public Object getAttribute(Object key) {
            if (key instanceof String && key.equals(ATTR_DISMANTLED_STRUCTURE)) {
                return dismantle(this);
            }

            for(AttributeSet delegate : new AttributeSet[] {delegate0, delegate1}) {
            	AttributeSet current = delegate;
            	while (current != null) {
            		if (current.isDefined(key)) {
                            return current.getAttribute(key);
            		}
            		current = current.getResolveParent();
            	}
            }

            return null;
        }

        public AttributeSet getResolveParent() {
            return null;
        }

        public Enumeration<?> getAttributeNames() {
            return Collections.enumeration(getAllKeys());
        }

        public int getAttributeCount() {
            return getAllKeys().size();
        }

        public AttributeSet copyAttributes() {
            return createImmutable(delegate0, delegate1);
        }

        public boolean containsAttribute(Object key, Object value) {
            return delegate0.containsAttribute(key, value) || delegate1.containsAttribute(key, value);
        }

        private Collection<?> getAllKeys() {
            HashSet<Object> allKeys = new HashSet<Object>();

            for(AttributeSet delegate : new AttributeSet[] {delegate0, delegate1}) {
                for(Enumeration<?> keys = delegate.getAttributeNames(); keys.hasMoreElements(); ) {
                    Object key = keys.nextElement();
                    allKeys.add(key);
                }
            }

            return allKeys;
        }
    } // End of Composite2 class

    private static final class Composite4 implements AttributeSet, CompositeAttributeSet {

        private final AttributeSet delegate0;
        private final AttributeSet delegate1;
        private final AttributeSet delegate2;
        private final AttributeSet delegate3;

        public Composite4(AttributeSet delegate0, AttributeSet delegate1, AttributeSet delegate2, AttributeSet delegate3) {
            this.delegate0 = delegate0;
            this.delegate1 = delegate1;
            this.delegate2 = delegate2;
            this.delegate3 = delegate3;
        }

        @Override
        public Collection<? extends AttributeSet> getDelegates() {
            if (delegate3 == null) {
                return Arrays.asList(delegate0, delegate1, delegate2);
            } else {
                return Arrays.asList(delegate0, delegate1, delegate2, delegate3);
            }
        }

        @Override
        public boolean isEqual(AttributeSet attr) {
            return containsAttributes(attr) && attr.containsAttributes(this);
        }

        @Override
        public boolean containsAttributes(AttributeSet attributes) {
            for(Enumeration<?> keys = attributes.getAttributeNames(); keys.hasMoreElements(); ) {
                Object key = keys.nextElement();
                Object value = attributes.getAttribute(key);

                if (!containsAttribute(key, value)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public boolean isDefined(Object key) {
            return delegate0.isDefined(key) || delegate1.isDefined(key) || delegate2.isDefined(key) || delegate3 != null && delegate3.isDefined(key);
        }

        @Override
        public Object getAttribute(Object key) {
            if (key instanceof String && key.equals(ATTR_DISMANTLED_STRUCTURE)) {
                return dismantle(this);
            }
            AttributeSet[] set;
            if (delegate3 == null) {
                set = new AttributeSet[] {delegate0, delegate1, delegate2};
            } else {
                set = new AttributeSet[] {delegate0, delegate1, delegate2, delegate3};
            }

            for(AttributeSet delegate : set) {
            	AttributeSet current = delegate;
            	while (current != null) {
            		if (current.isDefined(key)) {
                            return current.getAttribute(key);
            		}
            		current = current.getResolveParent();
            	}
            }

            return null;
        }

        @Override
        public AttributeSet getResolveParent() {
            return null;
        }

        @Override
        public Enumeration<?> getAttributeNames() {
            return Collections.enumeration(getAllKeys());
        }

        @Override
        public int getAttributeCount() {
            return getAllKeys().size();
        }

        @Override
        public AttributeSet copyAttributes() {
            if (delegate3 == null) {
                return createImmutable(delegate0, delegate1, delegate2);
            } else {
                return createImmutable(delegate0, delegate1, delegate2, delegate3);
            }
        }

        @Override
        public boolean containsAttribute(Object key, Object value) {
            return delegate0.containsAttribute(key, value) || delegate1.containsAttribute(key, value) ||
                   delegate2.containsAttribute(key, value) || delegate3 != null && delegate3.containsAttribute(key, value);
        }

        private Collection<?> getAllKeys() {
            HashSet<Object> allKeys = new HashSet<Object>();
            AttributeSet[] set;
            if (delegate3 == null) {
                set = new AttributeSet[] {delegate0, delegate1, delegate2};
            } else {
                set = new AttributeSet[] {delegate0, delegate1, delegate2, delegate3};
            }
            for(AttributeSet delegate : set) {
                for(Enumeration<?> keys = delegate.getAttributeNames(); keys.hasMoreElements(); ) {
                    Object key = keys.nextElement();
                    allKeys.add(key);
                }
            }

            return allKeys;
        }
    } // End of Composite4 class
}
