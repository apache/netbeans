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

package org.netbeans.spi.project.support.ant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

/**
 * Similar to {@link java.util.Properties} but designed to retain additional
 * information needed for safe hand-editing.
 * Useful for various <em>*.properties</em> in a project:
 * <ol>
 * <li>Can associate comments with particular entries.
 * <li>Order of entries preserved during modifications whenever possible.
 * <li>VCS-friendly: lines which are not semantically modified are not textually modified.
 * <li>Can automatically insert line breaks in new or modified values at positions
 *     that are likely to be semantically meaningful, e.g. between path components
 * </ol>
 * The file format (including encoding etc.) is compatible with the regular JRE implementation.
 * Only (non-null) String is supported for keys and values.
 * This class is not thread-safe; use only from a single thread, or use {@link java.util.Collections#synchronizedMap}.
 * <p>This class exists here only for historical reasons. It delegates to {@link org.openide.util.EditableProperties}.
 */
public final class EditableProperties extends AbstractMap<String,String> implements Cloneable {

    private final org.openide.util.EditableProperties delegate;

    private EditableProperties(org.openide.util.EditableProperties delegate) {
        this.delegate = delegate;
    }
    
    /**
     * Creates empty instance whose items will not be alphabetized.
     * @deprecated Use {@link #EditableProperties(boolean)} for clarity instead.
     */
    @Deprecated
    public EditableProperties() {
        this(/* mentioned in #64174 - documented default */false);
    }

    /**
     * Creates empty instance.
     * @param alphabetize alphabetize new items according to key or not
     */
    public EditableProperties(boolean alphabetize) {
        this(new org.openide.util.EditableProperties(alphabetize));
    }
    
    /**
     * Creates instance from an existing map. No comments will be defined.
     * Any order from the existing map will be retained,
     * and further additions will not be alphabetized.
     * @param map a map from String to String
     */
    public EditableProperties(Map<String,String> map) {
        this(false);
        putAll(map);
    }
    
    /**
     * Returns a set view of the mappings ordered according to their file 
     * position.  Each element in this set is a Map.Entry. See
     * {@link AbstractMap#entrySet} for more details.
     * @return set with Map.Entry instances.
     */
    public Set<Map.Entry<String,String>> entrySet() {
        return delegate.entrySet();
    }
    
    /**
     * Load properties from a stream.
     * @param stream an input stream
     * @throws IOException if the contents are malformed or the stream could not be read
     */
    public void load(InputStream stream) throws IOException {
        delegate.load(stream);
    }

    /**
     * Store properties to a stream.
     * @param stream an output stream
     * @throws IOException if the stream could not be written to
     */
    public void store(OutputStream stream) throws IOException {
        delegate.store(stream);
    }

    @Override
    public String get(Object key) {
        return key instanceof String ? delegate.get((String) key) : null;
    }

    @Override
    public String put(String key, String value) {
        return delegate.put(key, value);
    }

    /**
     * Convenience method to get a property as a string.
     * Same as {@link #get}; only here because of pre-generic code.
     * @param key a property name; cannot be null nor empty
     * @return the property value, or null if it was not defined
     */
    public String getProperty(String key) {
        return delegate.getProperty(key);
    }
    
    /**
     * Convenience method to set a property.
     * Same as {@link #put}; only here because of pre-generic code.
     * @param key a property name; cannot be null nor empty
     * @param value the desired value; cannot be null
     * @return previous value of the property or null if there was not any
     */
    public String setProperty(String key, String value) {
        return delegate.setProperty(key, value);
    }

    /**
     * Sets a property to a value broken into segments for readability.
     * Same behavior as {@link #setProperty(String,String)} with the difference that each item
     * will be stored on its own line of text. {@link #getProperty} will simply concatenate
     * all the items into one string, so generally separators
     * (such as <em>:</em> for path-like properties) must be included in
     * the items (for example, at the end of all but the last item).
     * @param key a property name; cannot be null nor empty
     * @param value the desired value; cannot be null; can be empty array
     * @return previous value of the property or null if there was not any
     */
    public String setProperty(String key, String[] value) {
        return delegate.setProperty(key, value);
    }

    /**
     * Returns comment associated with the property. The comment lines are
     * returned as defined in properties file, that is comment delimiter is
     * included. Comment for property is defined as: continuous block of lines
     * starting with comment delimiter which are followed by property
     * declaration (no empty line separator allowed).
     * @param key a property name; cannot be null nor empty
     * @return array of String lines as specified in properties file; comment
     *    delimiter character is included
     */
    public String[] getComment(String key) {
        return delegate.getComment(key);
    }

    /**
     * Create comment for the property.
     * <p>Note: if a comment includes non-ISO-8859-1 characters, they will be written
     * to disk using Unicode escapes (and {@link #getComment} will interpret
     * such escapes), but of course they will be unreadable for humans.
     * @param key a property name; cannot be null nor empty
     * @param comment lines of comment which will be written just above
     *    the property; no reformatting; comment lines must start with 
     *    comment delimiter; cannot be null; cannot be emty array
     * @param separate whether the comment should be separated from previous
     *    item by empty line
     */
    public void setComment(String key, String[] comment, boolean separate) {
        delegate.setComment(key, comment, separate);
    }

    @Override
    public Object clone() {
        return cloneProperties();
    }
    
    /**
     * Create an exact copy of this properties object.
     * @return a clone of this object
     */
    public EditableProperties cloneProperties() {
        return new EditableProperties(delegate.cloneProperties());
    }

}
