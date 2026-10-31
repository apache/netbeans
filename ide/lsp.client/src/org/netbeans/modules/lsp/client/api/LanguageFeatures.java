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
package org.netbeans.modules.lsp.client.api;

import java.io.IOException;
import org.netbeans.modules.lsp.client.bindings.configuration.BracesMatcherFactoryImpl;
import org.netbeans.modules.lsp.client.bindings.configuration.ToggleCommentActionImpl;
import org.netbeans.modules.lsp.client.bindings.configuration.TypingCompletionImpl.FactoryImpl;
import org.netbeans.modules.lsp.client.options.GenericEditorKit;
import org.netbeans.modules.lsp.client.spi.friend.LanguageConfiguration;
import org.openide.filesystems.FileObject;

/**
 * Callbacks that can be put into the layer to enable various editor features
 * driven by a {@code language-configuration.json} file.
 *
 * <p>Each method is a factory suitable for use as a layer
 * {@code instanceCreate} target and should be registered under the
 * {@code Editors/<mime-type>}/...} folder or an appropriate sub-folder.
 *
 * @since 1.38
 */
//javadoc partially by qwen3.8
public class LanguageFeatures {

    /**
     * Creates a {@code GenericEditorKit} for the given MIME type.
     *
     * <p>Requires the {@code mimeType} attribute (String) on the registration
     * file. If absent, {@code text/plain} is used as a fallback.
     *
     * <p>Sample registration:
     * {@snippet language="xml":
     * <folder name="Editors">
     *     <folder name="text">
     *         <folder name="x-c++">
     *             <file name="EditorKit.instance">
     *                 <attr methodvalue="org.netbeans.modules.lsp.client.api.LanguageFeatures.editorKit" name="instanceCreate"/>
     *                 <attr name="instanceOf" stringvalue="javax.swing.text.EditorKit"/>
     *                 <attr name="mimeType" stringvalue="text/x-c++"/>
     *             </file>
     *         </folder>
     *     </folder>
     * </folder>
     * }
     *
     * @param file the registration file, typically provided by the layer infrastructure.
     * @return a {@link GenericEditorKit} configured for the specified MIME type.
     */
    public static Object editorKit(FileObject file) {
        String mimeType = "text/plain";
        if (file.getAttribute("mimeType") instanceof String mt) {
            mimeType = mt;
        }
        return new GenericEditorKit(mimeType);
    }

    /**
     * Loads and registers a {@code language-configuration.json} for the given
     * MIME type.
     *
     * <p>The configuration content can be supplied either inline (as file text)
     * or referenced via a {@code url} attribute (e.g. {@code nbresloc:}).
     *
     * <p>Sample registration:
     * {@snippet language="xml":
     * <folder name="Editors">
     *     <folder name="text">
     *         <folder name="x-c++">
     *             <file name="languageConfiguration.instance"
     *                   url="nbresloc:/org/netbeans/modules/.../resources/language-configuration.json">
     *                 <attr methodvalue="org.netbeans.modules.lsp.client.api.LanguageFeatures.languageConfiguration" name="instanceCreate"/>
     *             </file>
     *         </folder>
     *     </folder>
     * </folder>
     * }
     *
     * @param file the registration file, typically provided by the layer infrastructure.
     * @return a parsed {@link LanguageConfiguration} instance.
     * @throws IOException if the configuration content cannot be read or parsed.
     */
    public static Object languageConfiguration(FileObject file) throws IOException {
        return LanguageConfiguration.from(file.asText());
    }

    /**
     * Creates a {@code BracesMatcherFactory} that pairs braces according to the
     * language configuration.
     *
     * <p>Sample registration:
     * {@snippet language="xml":
     * <folder name="Editors">
     *     <folder name="text">
     *         <folder name="x-c++">
     *             <folder name="BracesMatchers">
     *                 <file name="languageConfigurationMatcher.instance">
     *                     <attr methodvalue="org.netbeans.modules.lsp.client.api.LanguageFeatures.braceMatcher" name="instanceCreate"/>
     *                     <attr name="instanceOf" stringvalue="org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory"/>
     *                 </file>
     *             </folder>
     *         </folder>
     *     </folder>
     * </folder>
     * }
     *
     * @return a {@link BracesMatcherFactoryImpl} instance.
     */
    public static Object braceMatcher() {
        return new BracesMatcherFactoryImpl();
    }

    /**
     * Creates a {@code TypedTextInterceptor.Factory} that drives typing
     * completion (e.g. auto-close braces, auto-indent) based on the language
     * configuration.
     *
     * <p>Sample registration:
     * {@snippet language="xml":
     * <folder name="Editors">
     *     <folder name="text">
     *         <folder name="x-c++">
     *             <file name="typingCompletion.instance">
     *                 <attr methodvalue="org.netbeans.modules.lsp.client.api.LanguageFeatures.typingCompletion" name="instanceCreate"/>
     *                 <attr name="instanceOf" stringvalue="org.netbeans.spi.editor.typinghooks.TypedTextInterceptor$Factory"/>
     *             </file>
     *         </folder>
     *     </folder>
     * </folder>
     * }
     *
     * @return a {@link FactoryImpl} instance.
     */
    public static Object typingCompletion() {
        return new FactoryImpl();
    }

    /**
     * Creates a {@code TextAction} that toggles comments on the current
     * selection or line, using the comment delimiters from the language
     * configuration.
     *
     * <p>Requires that an appropriate {@code EditorKit} (as created by
     * {@link #editorKit(FileObject)}) is registered for the same MIME type so
     * that {@code EditorKit#getContentType()} resolves correctly.
     *
     * <p>Sample registration:
     * {@snippet language="xml":
     * <folder name="Editors">
     *     <folder name="text">
     *         <folder name="x-c++">
     *             <folder name="Actions">
     *                 <file name="toggleComment.instance">
     *                     <attr methodvalue="org.netbeans.modules.lsp.client.api.LanguageFeatures.toggleCommentAction" name="instanceCreate"/>
     *                     <attr name="instanceOf" stringvalue="javax.swing.text.TextAction"/>
     *                 </file>
     *             </folder>
     *         </folder>
     *     </folder>
     * </folder>
     * }
     *
     * @return a {@link ToggleCommentActionImpl} instance.
     */
    public static Object toggleCommentAction() {
        return new ToggleCommentActionImpl();
    }
}
