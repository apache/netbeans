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

package org.netbeans.api.editor.mimelookup;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.netbeans.modules.editor.mimelookup.APIAccessor;
import org.netbeans.modules.editor.mimelookup.MimeLookupCacheSPI;
import org.netbeans.modules.editor.mimelookup.MimePathLookup;
import org.openide.util.Lookup;

/**
 * The mime path is a concatenation of one or more mime types. The purpose of
 * a mime path is to describe the fact that a document of a certain mime type
 * can contain fragments of another document with a different mime type. The fragment
 * and its mime type is refered to as an embedded document and an embedded mime
 * type respectively. 
 *
 * <p>In order to fully understand the scale of the problem the mime path
 * is trying to describe you should consider two things. First a document can
 * contain several different embedded fragments each of a different
 * mime type. Second, each embeded fragment itself can possibly contain one or
 * more other embedded fragments and this nesting can in theory go indefinitely
 * deep.
 *
 * <p>In reality the nesting probably will not be very deep. As an example of a
 * document containing an embedded fragment of another document of the different
 * mime type you could imagine a JSP page containing a Java scriplet. The main
 * document is the JSP page of the 'text/x-jsp' mime type, which includes a fragment
 * of Java source code of the 'text/x-java' mime type.
 *
 * <p>The mime path comes handy when we want to distinguish between the ordinary
 * 'text/x-java' mime type and the 'text/x-java' mime type embedded in the JSP
 * page, because both of those 'text/x-java' mime types will have a different
 * mime path. The ordinary 'text/x-java' mime type has a mime path consisting
 * of just one mime type - 'text/x-java'. The 'text/x-java' mime type embeded in
 * the JSP page, however, has a mime path comprised from two mime types
 * 'text/x-jsp' and 'text/x-java'. The order of mime types in a mime path is
 * obviously very important, because it describes how the mime types are embedded.
 *
 * <p>The mime path can be represented as a <code>String</code> simply by
 * concatenating all its mime types separated by the '/' character. Since
 * mime types always contain one and only one '/' character it is clear which
 * '/' character belongs to a mime type and which is the mime path separator.
 *
 * <p>In the above example the mime path of the 'text/x-java' mime type embedded
 * in the 'text/x-jsp' mime type can be represented as 'text/x-jsp/text/x-java'.
 *
 * <p class="nonnormative">For some languages it is not uncommon to allow embedding of itself. For example
 * in Ruby it is allowed to use Ruby code within strings and Ruby will
 * evaluate this code when evaluating the value of the strings. Depending on the
 * implementation of a lexer there can be tokens with <code>MimePath</code> that
 * contains several consecutive mime types that are the same.
 * 
 * <p>The format of a valid mime type string is described in
 * <a href="http://tools.ietf.org/html/rfc4288#section-4.2">RFC 4288</a>.
 * <code>MimePath</code> performs internall checks according to this specification.
 * 
 * <p><b>Identity:</b> By definition two <code>MimePath</code> instances are equal
 * if they represent the same string mime path. The implementation guarantees
 * that by caching and reusing instances of the <code>MimePath</code> that it
 * creates. The <code>MimePath</code> instances can be used as keys in maps.
 *
 * <p><b>Lifecycle:</b> Although the instances of <code>MimePath</code> are
 * internally cached and should survive for certain time without being referenced
 * from outside of the MimePath API, clients are strongly encouraged to hold
 * a reference to the <code>MimePath</code> they obtained throughout the whole
 * lifecycle of their component. For example an opened java editor with a document
 * should keep its instance of the 'text/x-java' <code>MimePath</code> for the
 * whole time the editor is open.
 *
 * @author Miloslav Metelka, Vita Stejskal
 * @see MimeLookup
 * @see <a href="http://tools.ietf.org/html/rfc4288#section-4.2">RFC 4288</a>
 */
public final class MimePath {
    
    /**
     * The root of all mime paths. The empty mime path does not refer to any
     * mime type.
     */
    public static final MimePath EMPTY = new MimePath();

    /** Internal lock to manage the cache maps. */
    private static final Object LOCK = new Object();

    /** The List of Recently Used mime paths. */
    private static final ArrayList<MimePath> LRU = new ArrayList<>();

    /** The maximum size of the List of Recently Used mime paths.
    /* package */ static final int MAX_LRU_SIZE = 3;

    private static final Pattern REG_NAME_PATTERN = Pattern.compile("^[[\\p{Alnum}][!#$&.+\\-^_]]{1,127}$"); //NOI18N

    private static final Set<String> WELL_KNOWN_TYPES = new HashSet<>(Arrays.asList(
        "application", //NOI18N
        "audio", //NOI18N
        "content", //NOI18N   for content/unknown mime type
        "image", //NOI18N
        "message", //NOI18N
        "model", //NOI18N
        "multipart", //NOI18N
        "text", //NOI18N
        "video" //NOI18N
    ));
    
    private static final Map<String,Reference<MimePath>> string2mimePath = new ConcurrentHashMap<>();
    
    /**
     * Gets the mime path for the given mime type. The returned <code>MimePath</code>
     * will contain exactly one element and it will be the mime type passed in
     * as the parameter.
     *
     * @param mimeType The mime type to get the mime path for. If <code>null</code>
     * or empty string is passed in the <code>EMPTY</code> mime path will be
     * returned.
     *
     * @return The <code>MimePath</code> for the given mime type or
     * <code>MimePath.EMPTY</code> if the mime type is <code>null</code> or empty
     * string.
     */
    public static MimePath get(String mimeType) {
        if (mimeType == null || mimeType.length() == 0){
            return EMPTY;
        } else {
            return get(EMPTY, mimeType);
        }
    }
    
    /**
     * Gets the mime path corresponding to a mime type embedded in another
     * mime type. The embedding mime type is described in form of a mime path
     * passed in as the <code>prefix</code> parameter.
     *
     * <p>For example for a java scriplet embedded in a jsp page the <code>prefix</code> would 
     * be the mime path 'text/x-jsp' and <code>mimeType</code> would be 'text/x-java'.
     * The method will return the 'text/x-jsp/text/x-java' mime path.
     *
     *
     * @param prefix The mime path determining the mime type that embedds the mime
     * type passed in in the second parameter. It can be {@link #EMPTY} in which
     * case the call will be equivalent to calling <code>get(mimeType)</code> method.
     * @param mimeType The mime type that is embedded in the mime type determined
     * by the <code>prefix</code> mime path.
     *
     * @return The mime path representing the embedded mime type.
     */
    public static MimePath get(MimePath prefix, String mimeType) {
        if (!validate(mimeType)) {
            throw new IllegalArgumentException("Invalid mimeType=\"" + mimeType + "\""); //NOI18N
        }
        
        return prefix.getEmbedded(mimeType);
    }
    
    /**
     * Parses a mime path string and returns its <code>MimePath</code> representation.
     *
     * <p>The format of a mime path string representation is a string of mime
     * type components comprising the mime path separated by the '/' character.
     * For example a mime path representing the 'text/x-java' mime type embedded
     * in the 'text/x-jsp' mime type can be represented as the following string -
     * 'text/x-jsp/text/x-java'.
     *
     * <p>The mime path string can be an empty string, which represents the
     * {@link #EMPTY} mime path. By definition all valid mime paths except of
     * the empty one have to contain odd number of '/' characters.
     *
     * @param path The mime path string representation. 
     *
     * @return non-null mime-path corresponding to the given string path.
     */
    public static MimePath parse(String path) {
        assert path != null : "path cannot be null"; // NOI18N

        Reference<MimePath> mpRef = string2mimePath.get(path);
        MimePath mimePath = mpRef != null ? mpRef.get() : null;

        if (mimePath != null) {
            return mimePath;
        }

        // Parse the path
        Object o = parseImpl(path, false);
        if (!(o instanceof MimePath)) {
            throw new IllegalArgumentException((String) o);
        }

        mimePath = (MimePath) o;

        // Intern the path since the language path's string path is also interned
        // and thus they can be matched by identity
        string2mimePath.put(path.intern(), new WeakReference<>(mimePath));

        return mimePath;
    }

    /**
     * Validates components of a mime type. Each mime types is compound from
     * two components - <i>type</i> and <i>subtype</i>. There are rules that
     * both components must obey. For details see 
     * <a href="http://tools.ietf.org/html/rfc4288#section-4.2">RFC 4288</a>.
     * 
     * @param type The type component of a mime type to validate. If <code>null</code>
     *   the type component will not be validated.
     * @param subtype The subtype component of a mime type to validate. If <code>null</code>
     *   the subtype component will not be validated.
     * 
     * @return <code>true</code> if non-</code>null</code> components passed in
     *   are valid mime type components, otherwise <code>false</code>.
     * @since 1.7
     */
    public static boolean validate(CharSequence type, CharSequence subtype) {
        if (type != null) {
            // HACK: 
            if (startsWith(type, "test")) { //NOI18N
                for(int i = 4; i < type.length(); i++) {
                    if (type.charAt(i) == '_') { //NOI18N
                        type = type.subSequence(i + 1, type.length());
                        break;
                    }
                }
            }
            
            if (!WELL_KNOWN_TYPES.contains(type.toString())) {
                return false;
            }
        }

        if (subtype != null) {
            if (!REG_NAME_PATTERN.matcher(subtype).matches()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validates a path to check if it's a valid mime path. If this method
     * returns <code>true</code> the path is a valid mime path string and can
     * be used in the <code>MimePath.parse()</code> method.
     * 
     * @param path The path string to validate.
     * 
     * @return <code>true</code> if the path string is a valid mime path.
     * @since 1.7
     */
    public static boolean validate(CharSequence path) {
        if(path == null) {
            return false;
        }

        if (string2mimePath.containsKey(path.toString())) {
            return true;
        }

        // parseImpl will return error string if parsing fails
        return !(parseImpl(path, true) instanceof String);
    }
    
    /**
     * Array of component mime paths for this mime path.
     * <br>
     * The last member of the array is <code>this</code>.
     */
    private final MimePath[] mimePaths;
    
    /**
     * Complete string path of this mimePath.
     */
    private final String path;

    /**
     * Mime type string represented by this mime path component.
     */
    private final String mimeType;
    
    /**
     * Mapping of embedded mimeType to a weak reference to mimePath.
     */
    private Map<String, SoftReference<MimePath>> mimeType2mimePathRef;

    /**
     * The lookup with objects registered for this mime path.
     */
    private Lookup lookup;
    
    /**
     * Synchronization lock for creation of the mime path lookup.
     */
    private final String LOOKUP_LOCK = new String("MimePath.LOOKUP_LOCK"); //NOI18N
    
    private MimePath(MimePath prefix, String mimeType) {
        int prefixSize = prefix.size();
        this.mimePaths = new MimePath[prefixSize + 1];
        System.arraycopy(prefix.mimePaths, 0, this.mimePaths, 0, prefixSize);
        this.mimePaths[prefixSize] = this;
        String prefixPath = prefix.path;
        this.path = (prefixPath != null && prefixPath.length() > 0 ) ? 
            (prefixPath + '/' + mimeType).intern() : //NOI18N
            mimeType.intern();
        this.mimeType = mimeType;
    }
    
    /** Build EMPTY mimePath */
    private MimePath() {
        this.mimePaths = new MimePath[0];
        this.path = ""; //NOI18N
        this.mimeType = ""; //NOI18N
    }
    
    /**
     * Get string path represented by this mime-path.
     * <br/>
     * For example <code>"text/x-jsp/text/x-java"</code>.
     *
     * @return non-null string path.
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Get total number of mime-types in the mime-path.
     * <br>
     * {@link #EMPTY} mime-path has zero size.
     * <br>
     * <code>"text/x-jsp/text/x-java"</code> has size 2.
     *
     * @return >=0 number of mime-types contained in this mime-path.
     */
    public int size() {
        return mimePaths.length;
    }
    
    /**
     * Get mime type of this mime-path at the given index.
     * <br>
     * Index zero corresponds to the root mime-type.
     * <br>
     * For <code>"text/x-jsp/text/x-java"</code> 
     * <code>getMimeType(0)</code> returns <code>"text/x-jsp"</code>
     * and <code>getMimeType(1)</code> returns <code>"text/x-java"</code>.
     *
     * @param index >=0 && < {@link #size()}.
     * @return non-null mime-type at the given index.
     * @throws IndexOutOfBoundsException in case the index is not within
     *   required bounds.
     */
    public String getMimeType(int index) {
        return mimePaths[index].mimeType;
    }
    
    /**
     * Return prefix mime-path with the given number of mime-type components
     * ranging from zero till the size of this mime-path.
     *
     * @param size >=0 && <= {@link #size()}.
     *  <br>
     *  For zero size the {@link #EMPTY} will be returned.
     *  <br>
     *  For <code>size()</code> <code>this</code> will be returned.
     * @return non-null mime-type of the given size.
     * @throws IndexOutOfBoundsException in case the index is not within
     *   required bounds.
     */
    public MimePath getPrefix(int size) {
        return (size == 0)
            ? EMPTY
            : mimePaths[size - 1];
    }

    private MimePath getEmbedded(String mimeType) {
        // Attempt to retrieve from the cache first
        // It has also an advantage that the mime-type does not need
        // to be tested for correctness
        synchronized (LOCK) {
            if (mimeType2mimePathRef == null) {
                mimeType2mimePathRef = new HashMap<String, SoftReference<MimePath>>();
            }
            Reference mpRef = mimeType2mimePathRef.get(mimeType);
            MimePath mimePath;
            if (mpRef == null || (mimePath = (MimePath)mpRef.get()) == null) {
                // Construct the mimePath
                mimePath = new MimePath(this, mimeType);
                mimeType2mimePathRef.put(mimeType, new SoftReference<MimePath>(mimePath));

                // Hard reference the last few MimePaths created.
                LRU.add(0, mimePath);
                if (LRU.size() > MAX_LRU_SIZE) {
                    LRU.remove(LRU.size() - 1);
                }
            }
        
            return mimePath;
        }
    }
    
    private static Object parseImpl(CharSequence path, boolean validateOnly) {
        MimePath mimePath = EMPTY;
        int pathLen = path.length();
        int startIndex = 0;
        while (true) {
            int index = startIndex;
            int slashIndex = -1;
            // Search for first slash
            while (index < pathLen) {
                if (path.charAt(index) == '/') { //NOI18N
                    slashIndex = index;
                    break; // first slash found
                }
                index++;
            }
            if (slashIndex == -1) { // no slash found
                if (index != startIndex) {
                    return "mimeType '" + path.subSequence(startIndex, path.length()) + //NOI18N
                            "' does not contain '/'."; // NOI18N
                }
                // Empty mimeType
                break;
            }
            index++; // move after slash
            while (index < pathLen) {
                if (path.charAt(index) == '/') { //NOI18N
                    if (index == slashIndex + 1) { // empty second part of mimeType
                        return "Two successive slashes in '" +  //NOI18N
                                path.subSequence(startIndex, path.length()) + "'"; // NOI18N
                    }
                    break;
                }
                index++;
            }
            if (index == slashIndex + 1) { // nothing after first slash
                return "Empty string after '/' in '" +  //NOI18N
                        path.subSequence(startIndex, path.length()) + "'"; // NOI18N
            }
            
            // Mime type found, validate
            if (!validate(path.subSequence(startIndex, slashIndex), 
                          path.subSequence(slashIndex + 1, index))
            ) {
                return "Invalid mimeType=\"" + path.subSequence(startIndex, index) + "\""; //NOI18N
            }
            
            if (!validateOnly) {
                String mimeType = path.subSequence(startIndex, index).toString();
                mimePath = mimePath.getEmbedded(mimeType);
            }
            
            startIndex = index + 1; // after slash or after end of path
        }
        return mimePath;
    }

    /**
     * Gets the <code>MimePathLookup</code> for the given mime path. The lookups
     * are cached and reused.
     *
     * @param The mime path to get the lookup for.
     *
     * @return The mime path specific lookup.
     */
    /* package */ Lookup getLookup() {
        return Lookup.getDefault().lookup(MimeLookupCacheSPI.class).getLookup(this);
    }
    
    private Lookup getLookupImpl() {
        synchronized (LOOKUP_LOCK) {
            if (lookup == null) {
                lookup = new MimePathLookup(this);  
            }
            return lookup;
        }
    }
    
    public @Override String toString() {
        return "MimePath[" + path + "]"; // NOI18N
    }

    private static boolean startsWith(CharSequence sequence, CharSequence subSequence) {
        if (sequence.length() < subSequence.length()) {
            return false;
        }
        
        for(int i = 0; i < subSequence.length(); i++) {
            if (sequence.charAt(i) != subSequence.charAt(i)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Returns the inherited Mime type.
     * For {@link #EMPTY}, returns {@code null}. For most other mime types, returns
     * {@code ""}. If the mime type derives from another one, such as text/ant+xml derives
     * from xml, the return value will be the base mime type (text/xml in the example case).
     * <p/>
     * For MimePaths that identified embedded content (more components on the MimePath),
     * the method returns the parent MIME of the last MIME type on the path
     * 
     * @return inherited mime type, or {@code null}, if no parent exists (for {@link #EMPTY})
     */
    public String getInheritedType() {
        if ("".equals(mimeType)) {
            return null;
        }
        MimePath lastType = (size() == 1) ? this : MimePath.parse(mimeType);
        List<String> inheritedPaths = lastType.getInheritedPaths(null, null);
        if (inheritedPaths.size() > 1) {
            return inheritedPaths.get(1);
        } else {
            return null;
        }
    }
    
    
    /**
     * Returns the included Mime paths.
     * For MimePath, which nests several MIME types (i.e. text/php/text/html/text/css), it enumerates
     * sub-paths so that a following element represents one level of nesting of the content.
     * For the example example, the return would be:
     * <ol>
     * <li>text/php/text/html/text/css -- the full path
     * <li>text/html/text/css -- outer content is removed
     * <li>text/css -- the mime type of the identified content itself
     * <li> (empty string)
     * </ol>
     * <p/>
     * If a MIME type on the path has a generic MIME type (i.e. text/x-ant+xml 
     * has a generic MIME type text/xml), that generic type will be inserted. For example,
     * for text/java/text/x-ant+xml/text/javascript, the result will list:
     * <ol>
     * <li>text/java/text/x-ant+xml/text/javascript, -- the full MimePath
     * <li>text/x-ant+xml/text/javascript -- a prefix
     * <li>text/xml/text/javascript -- ant+xml is generalized to xml
     * <li>text/javascript
     * <li>  (empty string)
     * </ol>
     * For all but {@link #EMPTY} MimePaths, the list contains at least one entry, and the last
     * entry is the {@link #EMPTY}. Note also, that the complete MimePath is always returned
     * as the 1st member of the list.
     * <p/>
     * The returned sequence of MimePaths is suitable for searching settings or services
     * for the (embedded) content whose type is described by MimePath as it is ordered from the
     * most specific to the least specific paths (including generalization) and always contains
     * the mime type of the identified contents. The last component ({@link ""}) represents 
     * default settings (services).
     * <p/>
     * Note that for MimePaths created from a mime type (not empty!) string, the 
     * <code>getInheritedPaths().get(1)</code> is a parent mime type. Either empty,
     * or the generalized MIME.
     * <p/>
     * The caller should not modify the returned List.
     * 
     * @return list of inherited Mime paths
     */
    public List<MimePath> getIncludedPaths() {
        List<String> paths = getInheritedPaths(null, null);
        List<MimePath> mpaths = new ArrayList<MimePath>(paths.size());
        for (String p : paths) {
            mpaths.add(MimePath.parse(p));
        }
        return mpaths;
    }
    
    // XXX: This is currently called from editor/settings/storage (SettingsProvider)
    // and editor/mimelookup/impl via reflection.
    // We will eventually make it friend API. In the meantime just
    // make sure that any changes here still work for those modules.
    // See also http://www.netbeans.org/issues/show_bug.cgi?id=118099

    /* package */ List<String> getInheritedPaths(String prefixPath, String suffixPath) {
        synchronized (LOCK) {
            List<String[]> arrays = new ArrayList<String[]>(size());
            String [] mimePathArray = split(this);

            for(int i = 0; i <= mimePathArray.length; i++) {
                // Create array for the i-th suffix and fill it with mime types
                String [] arr = new String [mimePathArray.length - i];
                for(int j = 0; j < arr.length; j++) {
                    arr[j] = mimePathArray[i + j];
                }

                // Add the array to the list
                arrays.add(arr);

                if (arr.length > 0) {
                    // For compound mime types fork the existing path and add its
                // variant for the generic part of the mime type as well.
                // E.g. text/x-ant+xml adds both text/x-ant+xml and text/xml
                    String genericMimeType = getGenericPartOfCompoundMimeType(arr[0]);
                    if (genericMimeType != null) {
                        String arr2[] = new String [arr.length];
                        System.arraycopy(arr, 0, arr2, 0, arr.length);
                        arr2[0] = genericMimeType;

                        // Add the generic version to the list
                        arrays.add(arr2);
                    }
                }
            }

            List<String> paths = new ArrayList<String>(arrays.size());

            for (String[] p : arrays) {
                StringBuilder sb = new StringBuilder(10 * p.length + 20);

                if (prefixPath != null && prefixPath.length() > 0) {
                    sb.append(prefixPath);
                }
                for (int ii = 0; ii < p.length; ii++) {
                    if (p[ii].length() > 0) {
                        if (sb.length() > 0) {
                            sb.append('/'); //NOI18N
                        }
                        sb.append(p[ii]);
                    }
                }
                if (suffixPath != null && suffixPath.length() > 0) {
                    if (sb.length() > 0) {
                        sb.append('/'); //NOI18N
                    }
                    sb.append(suffixPath);
                }

                paths.add(sb.toString());
            }

            return paths;
        }
    }

    // See http://tools.ietf.org/html/rfc4288#section-4.2 for the structure of
    // mime type strings.
    // package private just for tests
    /* package */ static String getGenericPartOfCompoundMimeType(String mimeType) {
        int plusIdx = mimeType.lastIndexOf('+'); //NOI18N
        if (plusIdx != -1 && plusIdx < mimeType.length() - 1) {
            int slashIdx = mimeType.indexOf('/'); //NOI18N
            String prefix = mimeType.substring(0, slashIdx + 1);
            String suffix = mimeType.substring(plusIdx + 1);

            // fix for #61245
            if (suffix.equals("xml")) { //NOI18N
                prefix = "text/"; //NOI18N
            }

            return prefix + suffix;
        } else {
            return null;
        }
    }

    private static String [] split(MimePath mimePath) {
        String [] array = new String[mimePath.size()];
        
        for (int i = 0; i < mimePath.size(); i++) {
            array[i] = mimePath.getMimeType(i);
        }
        
        return array;
    }

    private static class AccessorImpl extends APIAccessor {
        @Override
        public Lookup cacheMimeLookup(MimePath path) {
            return path.getLookupImpl();
        }
    }
    
    static {
        new AccessorImpl();
    }
}
