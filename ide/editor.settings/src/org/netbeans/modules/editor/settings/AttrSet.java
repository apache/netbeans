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

package org.netbeans.modules.editor.settings;

import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.StyleConstants;
import javax.swing.text.AttributeSet;
import org.netbeans.api.editor.settings.EditorStyleConstants;

/**
 * Implementation of immutable {@link AttributeSet} that performs sharing
 * of commonly used attribute sets.
 * <br/>
 * The attributes are divided into ones that are shareable (like foreground, background
 * or font-related properties) and non-shareable (everything else). Attribute sets
 * with shareable attributes only are put into a weak set and shared eventually.
 * <br/>
 * Each attribute set can create a weak cache for composite sets optimization.
 * <br/>
 * In addition the implementation also implements {@link Iterable}
 * allowing the clients to subsequently get all keys and values (key1, value1, key2, value2 etc.).
 * <br/>
 * Parenting through {@link AttributeSet#ResolveAttribute} is currently not supported
 * and the implementations should use <code>AttributeUtilities.createImmutable(parentSet,childSet)</code> instead.
 * <br/>
 *
 * Attributes (keys) are divided into shareable (e.g. foreground or background and non-shareable (all that are not marked as shareable).
 * <br/>
 * It is assumed that attribute sets mostly contain the shareable attributes
 * and also that there is not many shareable attributes (e.g. less than 10) so the pairs
 * are currently all held in an array (extra attributes are held separately).
 *
 * @author Miloslav Metelka
 */
public abstract class AttrSet implements AttributeSet, Iterable<Object> {

    static final Map<Object,KeyWrapper> sharedKeys =
            new HashMap<Object,KeyWrapper>(64, 0.4f); // Intentional low load-factor

    private static final SimpleWeakSet<Shared> cache = new SimpleWeakSet<Shared>();

    private static final Object[] EMPTY_ARRAY = new Object[0];

    private static final Shared EMPTY = new Shared(EMPTY_ARRAY, 0);

    // -J-Dorg.netbeans.modules.editor.settings.AttrSet.level=FINE
    // -J-Dorg.netbeans.modules.editor.settings.AttrSet.level=FINER - also dump attr contents
    private static final Logger LOG = Logger.getLogger(AttrSet.class.getName());
    private static int opCount;
    private static int nextDumpOpCount;

    // Cache statistics
    private static int cacheGets;
    private static int cacheMisses;
    private static int overrideGets;
    private static int overrideMisses;
    private static int alienConvert;

    /**
     * Get attribute set for the given key-value pairs.
     *
     * @param keyValuePairs
     * @return
     */
    public static synchronized AttrSet get(Object... keyValuePairs) {
        if (keyValuePairs.length == 0) {
            return EMPTY;
        }
        AttrSetBuilder builder = new AttrSetBuilder(keyValuePairs.length);
        for (int i = keyValuePairs.length; i > 0;) {
            Object value = keyValuePairs[--i];
            Object key = keyValuePairs[--i];
            if (key != null && value != null) {
                if (key == AttributeSet.ResolveAttribute) {
                    throw new IllegalStateException("AttributeSet.ResolveAttribute key not supported"); // NOI18N
                }
                builder.add(key, value);
            }
        }
        AttrSet attrSet = builder.toAttrSet();

        if (LOG.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder(100);
            sb.append("AttrSet.get():\n"); // NOI18N
            for (int i = 0; i < keyValuePairs.length;) {
                Object key = keyValuePairs[i++];
                if (sharedKeys.containsKey(key)) {
                    sb.append("  S ");
                } else {
                    sb.append("    ");
                }
                sb.append(key).append(" => ").append(keyValuePairs[i++]).append('\n');
            }
            sb.append("=> ").append(attrSet).append("; cacheSize=").append(cacheSize()).append('\n'); // NOI18N
            dumpCache(sb);
            LOG.fine(sb.toString());
        }
        opCount++;
        return attrSet;
    }

    /**
     * Merge the given sets so that an attribute in earlier attribute set overrides the same attribute in latter one.
     * @param sets
     * @return non-null merged attribute set.
     */
    public static synchronized AttrSet merge(AttributeSet... sets) {
        if (sets.length == 0) {
            return EMPTY;
        }
        AttrSet attrSet = null;
        for (int i = sets.length - 1; i >= 0; i--) {
            AttributeSet set = sets[i];
            if (set == null) { // Skip null attribute set e.g. from html.editor's navigation sidebar
                continue;
            }
            if (attrSet == null) {
                attrSet = toAttrSet(set);
            } else if (set == EMPTY) {
                continue; // Skip this one since it won't make any change
            } else {
                attrSet = attrSet.findOverride(toAttrSet(set));
            }
        }
        if (LOG.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder(100);
            sb.append("AttrSet.merge():\n");
            for (int i = 0; i < sets.length; i++) {
                sb.append("    ").append(sets[i]).append('\n');
            }
            sb.append("=> ").append(attrSet).append("; cacheSize=").append(cacheSize()).append('\n');
            dumpCache(sb);
            LOG.fine(sb.toString());
        }
        opCount++;
        return attrSet;
    }

    private static void dumpCache(StringBuilder sb) {
        if (opCount >= nextDumpOpCount) {
            nextDumpOpCount = opCount + 100;
            sb.append("AttrSet CACHE DUMP START -------------------------\n"); // NOI18N
            List<Shared> cacheAsList = cache.asList();
            int i = 0;
            for (Shared shared : cacheAsList) {
                appendSpaces(sb, 4);
                sb.append("[").append(i++).append("] ");
                shared.appendInfo(sb, true, true, 4).append('\n');
            }
            sb.append("Actual CACHE SIZE is ").append(cacheAsList.size()).append('\n'); // NOI18N
            sb.append("  Cache gets/misses: ").append(cacheGets).append('/').append(cacheMisses).append('\n'); // NOI18N
            sb.append("  Override cache gets/misses: ").append(overrideGets).append('/');
            sb.append(overrideMisses).append('\n'); // NOI18N
            sb.append("  Override cache gets/misses: ").append(overrideGets).append('/');
            sb.append(overrideMisses).append('\n'); // NOI18N
            sb.append("  Alien convert: ").append(alienConvert).append('\n');
            sb.append("AttrSet CACHE DUMP END ---------------------------\n"); // NOI18N
        }
    }

    /**
     * Register a shareable attribute's key which means that implementation
     * will attempt to share attribute sets instances containing this attribute.
     *
     * @param key non-null key.
     * @param valueType class that values should be of (used for debugging purposes only
     *  and may be null).
     */
    private static synchronized void registerSharedKey(Object key, Class<?> valueType) {
        if (!sharedKeys.containsKey(key)) {
            KeyWrapper keyWrapper = new KeyWrapper(key, valueType);
            sharedKeys.put(key, keyWrapper);
        }
    }

    static {
        // Registrations must be done at the begining before any AttrSet instances get created.
        registerSharedKey(StyleConstants.Background, Color.class);
        registerSharedKey(StyleConstants.Foreground, Color.class);
        registerSharedKey(StyleConstants.FontFamily, String.class);
        registerSharedKey(StyleConstants.FontSize, Integer.class);
        registerSharedKey(StyleConstants.Bold, Boolean.class);
        registerSharedKey(StyleConstants.Italic, Boolean.class);
        registerSharedKey(StyleConstants.Underline, Boolean.class);
        registerSharedKey(StyleConstants.StrikeThrough, Boolean.class);
        registerSharedKey(StyleConstants.Superscript, Boolean.class);
        registerSharedKey(StyleConstants.Subscript, Boolean.class);
        registerSharedKey(StyleConstants.Alignment, Boolean.class);
        registerSharedKey(StyleConstants.NameAttribute, String.class);
        // EditorStyleConstants
        registerSharedKey(EditorStyleConstants.WaveUnderlineColor, Color.class);
        registerSharedKey(EditorStyleConstants.DisplayName, String.class);
        registerSharedKey(EditorStyleConstants.Default, String.class);
        registerSharedKey(EditorStyleConstants.TopBorderLineColor, Color.class);
        registerSharedKey(EditorStyleConstants.BottomBorderLineColor, Color.class);
        registerSharedKey(EditorStyleConstants.LeftBorderLineColor, Color.class);
        registerSharedKey(EditorStyleConstants.RightBorderLineColor, Color.class);
        // From HighlightsContainer:
        registerSharedKey("org.netbeans.spi.editor.highlighting.HighlightsContainer.ATTR_EXTENDS_EOL", Boolean.class); // NOI18N
        registerSharedKey("org.netbeans.spi.editor.highlighting.HighlightsContainer.ATTR_EXTENDS_EMPTY_LINE", Boolean.class); // NOI18N
    }

    static int cacheSize() { // For debugging purposes
        return cache.size();
    }

    /**
     * Merge of this attribute set with an attribute set being a key in the cache.
     * The value of the entry in the cache is the target overriding attribute set.
     */
    private Map<AttrSet,WeakReference<AttrSet>> overrideCache; // 20 + 4 = 24 bytes

    protected AttrSet() {
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof AttrSet) {
            return isEqual((AttrSet) obj);
        } else if (obj instanceof AttrSetBuilder) {
            return ((AttrSetBuilder)obj).isEqual(this);
        }
        return false;
    }

    @Override
    public final AttributeSet copyAttributes() {
        return this; // Immutable
    }

    @Override
    public final boolean containsAttribute(Object key, Object value) {
        return value.equals(getAttribute(key));
    }

    @Override
    public boolean containsAttributes(AttributeSet attrs) {
        if (attrs instanceof AttrSet) {
            Iterator<?> it = ((AttrSet)attrs).iterator();
            while (it.hasNext()) {
                Object key = it.next();
                Object value = it.next(); // hasNext() should always return true here
                if (!containsAttribute(key, value))
                    return false;
            }
        } else { // nonAttrSet
            Enumeration<?> en = attrs.getAttributeNames();
            while (en.hasMoreElements()) {
                Object key = en.nextElement();
                Object value = attrs.getAttribute(key);
                if (!containsAttribute(key, value)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public AttributeSet getResolveParent() {
        return null;
    }

    protected abstract Object[] sharedPairs();

    protected abstract int sharedHashCode();

    protected abstract Object[] extraPairs();

    protected abstract int extrasHashCode();

    @Override
    public final Enumeration<?> getAttributeNames() {
        return new KeysEnumeration(sharedPairs(), extraPairs());
    }

    public final Iterator<Object> iterator() {
        return new KeyValueIterator(sharedPairs(), extraPairs());
    }

    final AttrSet cachedOverride(Object override) {
        AttrSet attrSet;
        if (overrideCache == null) {
            overrideCache = new WeakHashMap<>(4);
            attrSet = null;
        } else {
            WeakReference<AttrSet> ref = overrideCache.get(override);
            attrSet = (ref != null) ? ref.get() : null;
        }
        overrideGets++;
        return attrSet;
    }

    void addOverride(AttrSet override, AttrSet attrSet) {
        overrideCache.put(override, new WeakReference<AttrSet>(attrSet));
        overrideMisses++;
    }

    final AttrSet findOverride(AttrSet override) {
        AttrSet attrSet = cachedOverride(override);
        if (attrSet == null) {
            Object[] oSharedPairs = override.sharedPairs();
            Object[] oExtraPairs = override.extraPairs();
            int osIndex = oSharedPairs.length;
            int oeIndex = oExtraPairs.length;
            AttrSetBuilder builder = new AttrSetBuilder(sharedPairs(), sharedHashCode(),
                    extraPairs(), extrasHashCode(), osIndex, oeIndex);
            while (osIndex > 0) {
                Object value = oSharedPairs[--osIndex];
                KeyWrapper keyWrapper = (KeyWrapper) oSharedPairs[--osIndex];
                builder.addShared(keyWrapper, value);
            }
            while (oeIndex > 0) {
                Object value = oExtraPairs[--oeIndex];
                Object key = oExtraPairs[--oeIndex];
                builder.addExtra(key, value);
            }
            attrSet = builder.toAttrSet();
            addOverride(override, attrSet);
        }
        return attrSet;
    }

    static int findKeyWrapperIndex(Object[] pairs, int pairsLength, KeyWrapper keyWrapper) {
        int high = pairsLength - 2;
        int low = 0;
        while (low <= high) {
            int mid = ((low + high) >>> 1) & (~1);
            KeyWrapper kw = (KeyWrapper) pairs[mid]; // already <<1
            int cmp = (kw.order - keyWrapper.order);

            if (cmp < 0) {
                low = mid + 2;
            } else if (cmp > 0) {
                high = mid - 2;
            } else {
                return mid; // key found
            }
        }
        return -(low + 1);  // key not found
    }

    static AttrSet toAttrSet(AttributeSet attrs) {
        if (attrs instanceof AttrSet) {
            return (AttrSet) attrs;
        }
        alienConvert++;
        return toAttrSetBuilder(attrs).toAttrSet();
    }

    static AttrSetBuilder toAttrSetBuilder(AttributeSet attrs) {
        AttrSetBuilder builder = new AttrSetBuilder(attrs.getAttributeCount() << 1);
        Enumeration<?> en = attrs.getAttributeNames();
        while (en.hasMoreElements()) {
            Object key = en.nextElement();
            Object value = attrs.getAttribute(key);
            builder.add(key, value);
        }
        return builder;
    }

    static Object[] trimArray(Object[] array, int size) {
        Object[] ret = new Object[size];
        while (--size >= 0) {
            ret[size] = array[size];
        }
        return ret;
    }

    void checkIntegrity() {
        String error = findIntegrityError();
        if (error != null) {
            throw new IllegalStateException(error);
        }
    }

    String findIntegrityError() {
        int lastOrder = -1;
        Object[] sharedPairs = sharedPairs();
        Object[] extraPairs = extraPairs();
        int sharedHashCode = sharedHashCode();
        int extrasHashCode = extrasHashCode();
        int hashCode = 0;
        for (int i = 0; i < sharedPairs.length;) {
            Object key = sharedPairs[i];
            if (key == null) {
                return "[" + i + "] is null"; // NOI18N
            }
            if (!(key instanceof KeyWrapper)) {
                return "[" + i + "]=" + key + " not KeyWrapper"; // NOI18N
            }
            KeyWrapper keyWrapper = (KeyWrapper) key;
            if (keyWrapper.order <= lastOrder) {
                return "[" + i + "] KeyWrapper.order=" + keyWrapper.order + " <= lastOrder=" + lastOrder; // NOI18N
            }
            hashCode ^= keyWrapper.key.hashCode();
            lastOrder = keyWrapper.order;
            Object value = sharedPairs[++i];
            if (value == null) {
                return "[" + i + "] is null"; // NOI18N
            }
            hashCode ^= value.hashCode();
            i++;
        }
        if (hashCode != sharedHashCode) {
            return "Invalid hashCode=" + hashCode + " != sharedHashCode=" + sharedHashCode;
        }
        hashCode = 0;
        for (int i = 0; i < extraPairs.length;) {
            Object key = extraPairs[i];
            if (key == null) {
                return "[" + i + "] is null"; // NOI18N
            }
            if (sharedKeys.containsKey(key)) {
                return "[" + i + "]: KeyWrapper-like key in extraPairs: " + key; // NOI18N
            }
            hashCode ^= key.hashCode();
            Object value = extraPairs[++i];
            if (value == null) {
                return "[" + i + "] is null"; // NOI18N
            }
            hashCode ^= value.hashCode();
            i++;
        }
        if (hashCode != extrasHashCode) {
            return "Invalid hashCode=" + hashCode + " != extrasHashCode=" + extrasHashCode;
        }
        return null;
    }


    @Override
    public String toString() {
        return appendAttrs(new StringBuilder(100)).toString();
    }

    StringBuilder appendInfo(StringBuilder sb, boolean attrs, boolean overrides, int indent) {
        Object[] sharedPairs = sharedPairs();
        Object[] extraPairs = extraPairs();
        sb.append("AttrSet[").append(sharedPairs.length >> 1);
        sb.append(",").append(extraPairs.length >> 1).append("]@");
        sb.append(System.identityHashCode(this));
        if (attrs) {
            for (int i = 0; i < sharedPairs.length;) {
                sb.append('\n');
                Object key = ((KeyWrapper)sharedPairs[i++]).key;
                Object value = sharedPairs[i++];
                appendSpaces(sb, indent + 4);
                sb.append("S ").append(key).append(" => ").append(value);
            }
            if (extraPairs != null) {
                for (int i = 0; i < extraPairs.length;) {
                    sb.append('\n');
                    Object key = extraPairs[i++];
                    Object value = extraPairs[i++];
                    appendSpaces(sb, indent + 4);
                    sb.append("E ").append(key).append(" => ").append(value);
                }
            }
            if (overrides && overrideCache != null) {
                sb.append('\n');
                appendSpaces(sb, indent + 2);
                sb.append("Overrides:");
                for (Map.Entry<AttrSet,WeakReference<AttrSet>> entry : overrideCache.entrySet()) {
                    sb.append('\n');
                    AttrSet key = entry.getKey();
                    AttrSet value = entry.getValue().get();
                    appendSpaces(sb, indent + 4);
                    key.appendInfo(sb, true, false, indent + 4).append('\n');
                    appendSpaces(sb, indent + 6);
                    sb.append("=> ");
                    if (value != null) {
                        value.appendInfo(sb, true, false, indent + 8);
                    } else {
                        sb.append("NULL");
                    }
                }
            }
        }
        return sb;
    }

    StringBuilder appendAttrs(StringBuilder sb) {
        Object[] sharedPairs = sharedPairs();
        Object[] extraPairs = extraPairs();
        sb.append("{");
        if (sharedPairs.length > 0) {
            sb.append('S').append(sharedPairs.length).append('{');
            for (int i = 0; i < sharedPairs.length;) {
                if (i > 0) sb.append(',');
                Object key = ((KeyWrapper) sharedPairs[i++]).key;
                Object value = sharedPairs[i++];
                sb.append(key).append("=").append(value);
            }
            sb.append('}');
        }
        if (extraPairs.length > 0) {
            sb.append('S').append(extraPairs.length).append('{');
            for (int i = 0; i < extraPairs.length;) {
                if (i > 0) sb.append(',');
                Object key = extraPairs[i++];
                Object value = extraPairs[i++];
                sb.append(key).append("=").append(value);
            }
            sb.append('}');
        }
        sb.append('}');
        return sb;
    }

    private static void appendSpaces(StringBuilder sb, int spaceCount) {
        while (--spaceCount >= 0) {
            sb.append(' ');
        }
    }


    private static final class Shared extends AttrSet {

        /**
         * Shared keyWrapper-value pairs ordered by the KeyWrapper.order.
         * Always non-null.
         */
        private final Object[] sharedPairs; // 8-super + 4 = 12 bytes
        /**
         * Cached hash code.
         */
        private final int hashCode; // 16 + 4 = 20 bytes

        Shared(Object[] sharedPairs, int hashCode) {
            this.sharedPairs = sharedPairs;
            this.hashCode = hashCode;
        }

        @Override
        protected Object[] sharedPairs() {
            return sharedPairs;
        }

        @Override
        protected Object[] extraPairs() {
            return EMPTY_ARRAY;
        }

        @Override
        protected int sharedHashCode() {
            return hashCode;
        }

        @Override
        protected int extrasHashCode() {
            return 0;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean isEqual(AttributeSet attrs) {
            if (attrs == this) {
                return true;
            }
            if (attrs.getClass() == Shared.class) {
                // Since all attr sets with shared attrs are weakly cached => compare by ==
                return (this == attrs);
            } else if (attrs.getClass() == Extra.class) {
                return false; // Cannot be equal since "attrs" contains non-shared attrs
            } else {
                return isEqual(toAttrSet(attrs));
            }
        }

        boolean isEqualSharedPairs(Object[] sharedPairs2, int sharedPairs2Length) {
            if (sharedPairs.length != sharedPairs2Length) {
                return false;
            }
            // Since keys are ordered it's possible to compare arrays by traversing
            for (int i = sharedPairs.length - 2; i >= 0; i -= 2) {
                if (sharedPairs[i] != sharedPairs2[i]) // Keys must ==
                {
                    return false;
                }
                if (!sharedPairs[i + 1].equals(sharedPairs2[i + 1])) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int getAttributeCount() {
            return sharedPairs.length >> 1;
        }

        @Override
        public boolean isDefined(Object key) {
            KeyWrapper keyWrapper = sharedKeys.get(key);
            return (keyWrapper != null) && (findKeyWrapperIndex(keyWrapper) >= 0);
        }

        /**
         * Use bin-search for finding index of the given key wrapper or return an insert index;
         *
         * @param keyWrapper non-null keyWrapper to search for.
         * @return index of the keyWrapper or -(insertIndex+1).
         */
        int findKeyWrapperIndex(KeyWrapper keyWrapper) {
            return findKeyWrapperIndex(sharedPairs, sharedPairs.length, keyWrapper);
        }

        @Override
        public Object getAttribute(Object key) {
            KeyWrapper keyWrapper = sharedKeys.get(key);
            return (keyWrapper != null) ? getAttribute(keyWrapper) : null;
        }

        public Object getAttribute(KeyWrapper keyWrapper) {
            int keyIndex;
            if ((keyIndex = findKeyWrapperIndex(keyWrapper)) >= 0) {
                return sharedPairs[keyIndex + 1];
            }
            return null;
        }

    }


    /**
     * AttrSet carrying extra attributes.
     */
    private static final class Extra extends AttrSet {

        final Shared shared; // Set holding shared attributes

        /**
         * Extra non-shared key-value pairs.
         * Always non-null.
         */
        private final Object[] extraPairs; // 12 + 4 = 16 bytes

        /**
         * Cached hash code for extra pairs. Total hashCode is XOR of shared pairs hashcode
         * with this extra hashcode.
         */
        private final int extrasHashCode; // 16 + 4 = 20 bytes

        Extra(Shared shared, Object[] extraPairs, int extrasHashCode) {
            this.shared = shared;
            this.extraPairs = extraPairs;
            this.extrasHashCode = extrasHashCode;
        }

        @Override
        protected Object[] sharedPairs() {
            return shared.sharedPairs();
        }

        @Override
        protected Object[] extraPairs() {
            return extraPairs;
        }

        @Override
        protected int sharedHashCode() {
            return shared.hashCode();
        }

        @Override
        protected int extrasHashCode() {
            return extrasHashCode;
        }

        @Override
        public int hashCode() {
            return shared.hashCode() ^ extrasHashCode;
        }

        @Override
        public boolean isEqual(AttributeSet attrs) {
            if (attrs == this) {
                return true;
            }
            if (attrs.getClass() == Shared.class) { // No extra attrs => false
                return false;
            } else if (attrs.getClass() == Extra.class) {
                Extra extra = (Extra) attrs;
                return (shared == extra.shared) &&
                        isEqualExtraPairs(extra.extraPairs, extra.extraPairs.length);
            } else {
                return isEqual(toAttrSet(attrs));
            }
        }

        boolean isEqualExtraPairs(Object[] extraPairs2, int extraPairs2Length) {
            if (extraPairs.length != extraPairs2Length) {
                return false;
            }
            for (int i = extraPairs2Length - 2; i >= 0; i -= 2) {
                int index = findExtraPairsIndex(extraPairs2[i]);
                if (index < 0) {
                    return false;
                }
                if (!extraPairs[i + 1].equals(extraPairs2[i + 1])) {
                    return false;
                }
            }
            return true;
        }

        private int findExtraPairsIndex(Object key) {
            if (extraPairs != null) {
                for (int i = extraPairs.length - 2; i >= 0; i -= 2) {
                    if (extraPairs[i].equals(key)) {
                        return i;
                    }
                }
            }
            return -1;
        }

        @Override
        public int getAttributeCount() {
            return shared.getAttributeCount() + extraPairs.length >> 1;
        }

        @Override
        public boolean isDefined(Object key) {
            KeyWrapper keyWrapper = sharedKeys.get(key);
            return (keyWrapper != null)
                    ? (shared.findKeyWrapperIndex(keyWrapper) >= 0)
                    : (findExtraPairsIndex(key) >= 0);
        }

        @Override
        public Object getAttribute(Object key) {
            KeyWrapper keyWrapper = sharedKeys.get(key);
            if (keyWrapper != null) {
                return shared.getAttribute(keyWrapper);
            } else {
                int keyIndex;
                if ((keyIndex = findExtraPairsIndex(key)) >= 0) {
                    return extraPairs[keyIndex + 1];
                }
                return null;
            }
        }

    }



    private class KeysEnumeration implements Enumeration<Object> {

        private Object[] sharedPairs;

        private Object[] extraPairs;

        private int index;

        private Object nextKey;

        KeysEnumeration(Object[] sharedPairs, Object[] extraPairs) {
            this.sharedPairs = sharedPairs;
            this.extraPairs = extraPairs;
            fetchNextKey();
        }

        @Override
        public boolean hasMoreElements() {
            return (nextKey != null);
        }

        @Override
        public Object nextElement() {
            if (nextKey == null) {
                throw new NoSuchElementException();
            }
            Object next = nextKey;
            fetchNextKey();
            return next;
        }

        private void fetchNextKey() {
            if (sharedPairs != null) {
                if (index < sharedPairs.length) {
                    nextKey = ((KeyWrapper)sharedPairs[index]).key;
                    index += 2;
                } else {
                    sharedPairs = null; // Switch to extras
                    index = 0;
                    fetchNextKey();
                }
            } else {
                if (index < extraPairs.length) {
                    nextKey = extraPairs[index];
                    index += 2; // skip value
                } else {
                    nextKey = null;
                }
            }
        }

    }


    private class KeyValueIterator implements Iterator<Object> {

        private Object[] sharedPairs;

        private Object[] extraPairs;

        private int index;

        private Object nextKeyOrValue;

        KeyValueIterator(Object[] sharedPairs, Object[] extraPairs) {
            this.sharedPairs = sharedPairs;
            this.extraPairs = extraPairs;
            fetchNext();
        }

        @Override
        public boolean hasNext() {
            return (nextKeyOrValue != null);
        }

        @Override
        public Object next() {
            if (nextKeyOrValue == null) {
                throw new NoSuchElementException();
            }
            Object next = nextKeyOrValue;
            fetchNext();
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove not allowed.");
        }

        private void fetchNext() {
            if (sharedPairs != null) {
                if (index < sharedPairs.length) {
                    if ((index & 1) == 0) { // key
                        nextKeyOrValue = ((KeyWrapper)sharedPairs[index]).key;
                    } else {
                        nextKeyOrValue = sharedPairs[index];
                    }
                    index++;
                } else {
                    sharedPairs = null; // Switch to extras
                    index = 0;
                    fetchNext();
                }
            } else {
                if (index < extraPairs.length) {
                    nextKeyOrValue = extraPairs[index++];
                } else {
                    nextKeyOrValue = null;
                }
            }
        }

    }


    /**
     * Wrapper around a shareable key.
     */
    private static final class KeyWrapper {

        private static int orderCounter;
        
        final Object key;

        final Class<?> valueType;
        
        final int order; // used for ordering pairs in sharedPairs array

        final int keyHashCode;

        KeyWrapper(Object key, Class<?> valueType) {
            this.key = key;
            this.valueType = valueType;
            this.order = orderCounter++;
            this.keyHashCode = key.hashCode();
        }

        @Override
        public String toString() {
            return "key=" + key + // ", valueType=" + valueType + // NOI18N
                    ", order=" + order + ", hash=" + keyHashCode; // NOI18N
        }

    }

    private static final class AttrSetBuilder implements SimpleWeakSet.ElementProvider<Shared> {

        Object[] sharedPairs;

        Object[] extraPairs;

        int sharedLength;

        int extrasLength;

        int sharedHashCode;

        int extrasHashCode;

        private boolean extrasInEquals;

        private Shared shared;

        AttrSetBuilder(int attrsLength) {
            sharedPairs = new Object[attrsLength];
            extraPairs = new Object[attrsLength];
        }

        AttrSetBuilder(Object[] sharedSrc, int sharedHashCode,
                Object[] extrasSrc, int extrasHashCode, int sharedPlus, int extrasPlus)
        {
            sharedPairs = new Object[sharedSrc.length + sharedPlus];
            extraPairs = new Object[extrasSrc.length + extrasPlus];
            this.sharedHashCode = sharedHashCode;
            this.extrasHashCode = extrasHashCode;
            sharedLength = sharedSrc.length;
            if (sharedLength > 0) {
                System.arraycopy(sharedSrc, 0, sharedPairs, 0, sharedLength);
            }
            extrasLength = extrasSrc.length;
            if (extrasLength > 0) {
                System.arraycopy(extrasSrc, 0, extraPairs, 0, extrasLength);
            }
        }
        
        void add(Object key, Object value) {
            KeyWrapper keyWrapper = sharedKeys.get(key);
            if (keyWrapper != null) {
                addShared(keyWrapper, value);
            } else { // Extra key
                addExtra(key, value);
            }
        }

        void addShared(KeyWrapper keyWrapper, Object value) {
            int i = findKeyWrapperIndex(sharedPairs, sharedLength, keyWrapper);
            if (i < 0) { // Does not exist yet
                i = -i - 1;
                if (i < sharedLength) {
                    System.arraycopy(sharedPairs, i, sharedPairs, i + 2, sharedLength - i);
                }
                sharedPairs[i] = keyWrapper;
                sharedHashCode ^= keyWrapper.keyHashCode;
                sharedLength += 2;
            } else { // Already exists => just replace value
                sharedHashCode ^= sharedPairs[i + 1].hashCode(); // Unapply present value's hashcode
            }
            sharedPairs[i + 1] = value;
            sharedHashCode ^= value.hashCode();
        }

        void addExtra(Object key, Object value) {
            for (int i = 0; i < extrasLength; i += 2) {
                if (extraPairs[i].equals(key)) { // Exists
                    extrasHashCode ^= extraPairs[i + 1].hashCode(); // Unapply present value's hashcode
                    extraPairs[i + 1] = value;
                    extrasHashCode ^= value.hashCode();
                    return;
                }
            }
            extraPairs[extrasLength++] = key;
            extrasHashCode ^= key.hashCode();
            extraPairs[extrasLength++] = value;
            extrasHashCode ^= value.hashCode();
        }

        AttrSet toAttrSet() {
            // Note: here extrasInEquals == false
            // Hack - use equality for AttrSetBuilder to search in the cache
            // So convert the cache to look like holding AttrSetBuilder instances.
            @SuppressWarnings("unchecked")
            SimpleWeakSet<AttrSetBuilder> cacheL = (SimpleWeakSet<AttrSetBuilder>) ((SimpleWeakSet<?>) cache);
            @SuppressWarnings("unchecked")
            SimpleWeakSet.ElementProvider<AttrSetBuilder> elementProvider =
                    (SimpleWeakSet.ElementProvider<AttrSetBuilder>) ((SimpleWeakSet.ElementProvider<?>) this);
            cacheGets++;
            Object o = cacheL.getOrAdd(this, elementProvider);
            assert (o != null);
            shared = (Shared) o;

            AttrSet attrSet;
            if (extrasLength > 0) { // Try to find in cache first
                extrasInEquals = true; // Compare extra attrs only in equals()
                extraPairs = (extrasLength > 0) ? trimArray(extraPairs, extrasLength) : null;
                attrSet = shared.cachedOverride(this);
                if (attrSet == null) {
                    attrSet = new Extra(shared, extraPairs, extrasHashCode);
                    shared.addOverride(new Extra(EMPTY, extraPairs, extrasHashCode), attrSet);
                }
                extrasInEquals = false; // return back for possible repetitive call (though "shared" left inited)
            } else {
                attrSet = shared;
            }
            return attrSet;
        }

        @Override
        public Shared createElement() {
            sharedPairs = (sharedLength > 0) ? trimArray(sharedPairs, sharedLength) : EMPTY_ARRAY;
            cacheMisses++;
            return new Shared(sharedPairs, sharedHashCode);
        }

        boolean isEqual(AttrSet attrs) {
            // So the target
            if (extrasInEquals) {
                // Compare just extra attrs from this builder and check that the attrs just contains
                // extra attributes (shared is EMPTY). This mode is used when matching into override cache.
                if (attrs instanceof Extra) {
                    Extra extra = (Extra) attrs;
                    return (extra.shared == EMPTY) && extra.isEqualExtraPairs(extraPairs, extrasLength);
                }
            } else {
                // Compare just shared attrs from this builder. This is used when matching
                // into shared attrs cache so cast to Shared.
                return ((Shared)attrs).isEqualSharedPairs(sharedPairs, sharedLength);
            }
            return false;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj instanceof AttrSet) {
                return isEqual((AttrSet)obj);
            } else if (obj instanceof AttrSetBuilder) {
                throw new IllegalStateException("Unexpected call - not implemented."); // NOI18N
            }
            return false;
        }

        @Override
        public int hashCode() {
            return extrasInEquals ? extrasHashCode : sharedHashCode;
        }

    }

}
