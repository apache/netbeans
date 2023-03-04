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

package org.netbeans.spi.lexer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;

/**
 * The <code>Language</code> provider. This class is a hook into the
 * lexer framework allowing modules to provide a language resolution service.
 * Whenever a <code>Language</code> is not explicitly known the
 * framework tries to determine it by asking <code>LanguageProvider</code>s registered
 * in the <code>Lookup.getDefault()</code>.
 * 
 * <code>Language</code>s might be needed for a mime type or mime path
 * of a <code>Document</code> used as an input source or they might be needed for
 * some tokens that contain text in an another (embedded) language. In both cases
 * a <code>Language</code> can either be explicitely provided by setting
 * the document's property or implementing the <code>LanguageHierarchy.embedded()</code>
 * method respectively or the framework will use <code>LanguageProvider</code>s to
 * create the appropriate <code>Language</code>.
 * 
 * @author Vita Stejskal
 */
public abstract class LanguageProvider {
    
    /**
     * The name of the property, which should be fired when the mime paths to
     * <code>Language</code> mapping changes.
     */
    public static final String PROP_LANGUAGE = "LanguageProvider.PROP_LANGUAGE"; //NOI18N

    /**
     * The name of the property, which should be fired when the embedded language to
     * <code>Language</code> mapping changes.
     */
    public static final String PROP_EMBEDDED_LANGUAGE = "LanguageProvider.PROP_EMBEDDED_LANGUAGE"; //NOI18N
    
    /**
     * Finds <code>Language</code> for a given mime type.
     * 
     * <p>The lexer framework uses this method to find a <code>Language</code>
     * for <code>Document</code>s that are used as an input source. If the document
     * itself does not specify its <code>Language</code> the framework
     * will consult registered <code>LanguageProvider</code>s to find out the
     * <code>Language</code> appropriate for the document's mime type.
     * 
     * @param mimeType The mime type of a <code>Language</code> to find.
     * 
     * @return The <code>Language</code> registered for the given
     *   mime type or <code>null</code> if no such <code>Language</code> exists.
     */
    public abstract Language<?> findLanguage(String mimeType);
    
    /**
     * Finds <code>LanguageEmbedding</code> that will define what language is
     * embedded in a given token.
     * 
     * <p>If a <code>Token</code> contains text in a different language that could
     * further be used for lexing of this <code>Token</code> the framework will try
     * to find out the <code>Language</code> of that language by asking
     * the <code>Token</code>'s own <code>Language</code> first and then
     * by consulting registered <code>LanguageProvider</code>s. The <code>LanguageProvider</code>s
     * are expected to return a <code>LanguageEmbedding</code> for tokens they
     * care about and <code>null</code> for the rest. The first non-null
     * <code>LanguageEmbedding</code> found will be used.
     * 
     * <p><code>LanguageEmbedding</code> instances returned from this method
     * <b>must not</b> reference any of the attributes passed in and especially not
     * the <code>token</code> instance.
     * 
     * @param token The <code>Token</code> to get the <code>Language</code>
     *   for.
     * @param languagePath The <code>LanguagePath</code> of the token, which
     *   embedded language should be returned.
     * @param inputAttributes The attributes that could affect the creation of
     *   the embedded <code>Language</code>. It may be <code>null</code>
     *   if there are no extra attributes.
     * 
     * @return The <code>LanguageEmbedding</code> for the given <code>Token</code>
     *   or <code>null</code> if the token can't embedd any language
     *   or the token is unknown to this <code>LanguageProvider</code>.
     */
    public abstract LanguageEmbedding<?> findLanguageEmbedding(
    Token<?> token, LanguagePath languagePath, InputAttributes inputAttributes);
    
    /**
     * Add a listener for change notifications.
     * 
     * @param l The listener to add.
     */
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    /**
     * Remove a listener.
     * 
     * @param l The listener to remove.
     */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    protected final void firePropertyChange(String propertyName) {
        pcs.firePropertyChange(propertyName, null, null);
    }
    
    /**
     * The default constructor for subclasses.
     */
    protected LanguageProvider() {
        
    }
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
}
