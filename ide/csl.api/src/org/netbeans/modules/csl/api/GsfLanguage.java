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

package org.netbeans.modules.csl.api;

import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizerRegistration;


/**
 * Lexical information for a given language.
 *
 * @author <a href="mailto:tor.norbye@sun.com">Tor Norbye</a>
 */
public interface GsfLanguage {
    /**
     * <p>Return the prefix used for line comments in this language, or null if this language
     * does not have a line comment. As an example, a Java scanner would return <code>//</code>,
     * a Ruby scanner would return <code>#</code>, a Visual Basic scanner would return <code>'</code>, etc.
     * </p>
     */
    @CheckForNull
    String getLineCommentPrefix();
    
    /**
     * <p>Return true iff the given character is considered to be an identifier character. This
     * is used for example when the user double clicks in the editor to select a "word" or identifier
     * by checking to the left and to the right of the caret position and selecting until a character
     * is not considered an identifier char by the scanner.
     * </p>
     * <p>
     * For a language like Java, just return Character.isJavaIdentifierPart(). For something like
     * Ruby, we also want to include "@" and "$" such that double clicking on a global variable
     * for example will include the global prefix "$".
     */
    boolean isIdentifierChar(char c);
    
    /**
     * <p>Return the Lexer Language associated with this scanner
     *</p>
     */
    @NonNull
    Language getLexerLanguage();
    
    /**
     * Display name for this language. This name should be localized since it can be shown to
     * the user (currently, it shows up in the Tasklist filter for example).
     */
    @NonNull
    String getDisplayName();
    
    /**
     * Return a preferred file extension for this language (if any -- may be null).
     * The extension should NOT include the separating dot. For example, for Java the preferred
     * file extension is "java", not ".java". 
     * 
     * Note also that registering a preferred extension will NOT automatically cause GSF to
     * identify files of that extension as belonging to GSF (or this language's mime type).
     * You still need a MIME resolver for that. This method is primarily used with some
     * older mechanisms in NetBeans (such as template creation) which is still file extension
     * oriented.
     */
    @CheckForNull
    String getPreferredExtension();
    
    /**
     * Gets IDs uniquely identifying source classpaths used by this language.
     *
     * <p>This method should return a list of classpath IDs where files of this language
     * belong to. If your language support or its accompanying project support
     * creates <code>Classpath</code> objects and registers them in <code>GlobalPathRegistry</code>
     * then the IDs of you source classpath should be listed here.
     * However, if you don't have your own classpath for sources and/or your files
     * can live in different projects in various source roots you may
     * return <code>null</code> here.
     *
     * <p>Please note that there is a semantic difference between <code>null</code>
     * and <code>Collections.emptySet()</code> return values. The former one is used
     * as a wildcard and means that your language files can live in any source
     * classpath. In contrast to that <code>Collections.emptySet()</code> means
     * <b>no</b> source classpath.
     *
     * @return The set of source classpath IDs, can be empty or even <code>null</code>.
     * @deprecated Use {@link PathRecognizerRegistration} instead.
     */
    @Deprecated
    Set<String> getSourcePathIds();

    /**
     * Gets IDs uniquely identifying classpaths with libraries used by this language. This
     * method is similar as {@link #getSourcePathIds()} except that it returns
     * ids of classpath with libraries that contain source files (not binary libraries).
     *
     * @return The set of library classpath IDs, can be empty or even <code>null</code>.
     *   Please see {@link #getSourcePathIds()} for the exact meaning of those values.
     * @deprecated Use {@link PathRecognizerRegistration} instead.
     */
    @Deprecated
    Set<String> getLibraryPathIds();

    /**
     * Gets IDs uniquely identifying classpaths with binary libraries used by this language. This
     * method is similar as {@link #getLibraryPathIds()} except that it returns
     * ids of classpath with binary libraries, runtimes, etc.
     *
     * <p>When the infrastructure works with binary library classpath it uses
     * <code>SourceForBinaryQuery</code> in order to find sources relevant for the
     * libraries on the classpath.
     *
     * @return The set of binary library classpath IDs, can be empty or even <code>null</code>.
     *   Please see {@link #getLibraryPathIds()} for the exact meaning of those values.
     * @deprecated Use {@link PathRecognizerRegistration} instead.
     */
    @Deprecated
    Set<String> getBinaryLibraryPathIds();
}
