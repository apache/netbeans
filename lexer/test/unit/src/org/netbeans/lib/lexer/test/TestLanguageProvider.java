/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.lib.lexer.test;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.util.Lookup;

/**
 * Language provider for various lexer-related tests.
 * <br/>
 * For using it a "META-INF/services/org.netbeans.spi.lexer.LanguageProvider" file
 * should be creating containing a single line with "org.netbeans.lib.lexer.test.TestLanguageProvider".
 * <br/>
 * Then the tests should register their test languages into it.
 *
 * @author Miloslav Metelka
 */
public class TestLanguageProvider extends LanguageProvider {
    
//    private static TestLanguageProvider INSTANCE;
    
    private static Map<String,Language<?>> mime2language = new HashMap<String,Language<?>>();
    
    private static Map<String,Map<TokenId,LanguageEmbedding<?>>> mime2embeddings
            = new HashMap<String,Map<TokenId,LanguageEmbedding<?>>>();
    
    private static final Object LOCK = new String("TestLanguageProvider.LOCK");
    
    public static void register(Language language) {
        register(language.mimeType(), language);
    }

    public static void register(String mimePath, Language language) {
//        checkInstanceExists();
        synchronized (LOCK) {
            mime2language.put(mimePath, language);
        }
        fireChange();
    }
    
    public static void registerEmbedding(String mimePath, TokenId id,
    Language<?> language, int startSkipLength, int endSkipLength, boolean joinSections) {
        registerEmbedding(mimePath, id, LanguageEmbedding.create(language, startSkipLength, endSkipLength, joinSections));
    }

    public static void registerEmbedding(String mimePath, TokenId id, LanguageEmbedding<?> embedding) {
//        checkInstanceExists();
        synchronized (LOCK) {
            Map<TokenId,LanguageEmbedding<?>> id2embedding = mime2embeddings.get(mimePath);
            if (id2embedding == null) {
                id2embedding = new HashMap<TokenId,LanguageEmbedding<?>>();
                mime2embeddings.put(mimePath, id2embedding);
            }
            id2embedding.put(id, embedding);
        }
        fireChange();
    }
    
    public static void fireChange() {
//        checkInstanceExists();
//        INSTANCE.firePropertyChange(PROP_LANGUAGE);
//        INSTANCE.firePropertyChange(PROP_EMBEDDED_LANGUAGE);
        TestLanguageProvider tlp = Lookup.getDefault().lookup(TestLanguageProvider.class);
        assert tlp != null : "No TestLanguageProvider in default Lookup";
        tlp.firePropertyChange(PROP_LANGUAGE);
        tlp.firePropertyChange(PROP_EMBEDDED_LANGUAGE);
    }
    
    public TestLanguageProvider() {
//        assert (INSTANCE == null) : "More than one instance of this class prohibited";
//        INSTANCE = this;
    }
    
//    private static void checkInstanceExists() {
//        if (INSTANCE == null)
//            throw new IllegalStateException("No instance of created yet.");
//    }

    public Language<?> findLanguage(String mimeType) {
        synchronized (LOCK) {
            return mime2language.get(mimeType);
        }
    }

    public LanguageEmbedding<?> findLanguageEmbedding(
    Token<?> token, LanguagePath languagePath, InputAttributes inputAttributes) {
        Map<TokenId,LanguageEmbedding<?>> id2embedding = mime2embeddings.get(languagePath.mimePath());
        return (id2embedding != null) ? id2embedding.get(token.id()) : null;
    }
    
}
