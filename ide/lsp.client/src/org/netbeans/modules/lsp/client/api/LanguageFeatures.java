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
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.lsp.client.bindings.configuration.BracesMatcherFactoryImpl;
import org.netbeans.modules.lsp.client.bindings.configuration.ToggleCommentActionImpl;
import org.netbeans.modules.lsp.client.bindings.configuration.TypingCompletionImpl.FactoryImpl;
import org.netbeans.modules.lsp.client.options.GenericEditorKit;
import org.netbeans.modules.lsp.client.spi.friend.LanguageConfiguration;
import org.openide.filesystems.FileObject;

/**Callbacks that can be put into the layer to get various features based on the
 * {@code language-configuration.json}. To be registered in the {@link MimeLookup}.
 * @since 1.38
 */
public class LanguageFeatures {
    /**creates an editor kit.
     * requires attribute {@code mimeType} of type String, or {@code text/plain} will be used; will be used as the content type for the EditorKit.
     * sample registration:
     * {@snippet language="xml":
     * <folder name="Editors">
     *     <folder name="mime">
     *         <folder name="type">
     *             <file name="EditorKit.instance">
     *                 <attr methodvalue="org.netbeans.modules.lsp.client.api.LanguageFeatures.editorKit" name="instanceCreate"/>
     *                 <attr name="instanceOf" stringvalue="javax.swing.text.EditorKit"/>
     *                 <attr name="mimeType" stringvalue="<mime-type>"/>
     *             </file>
     *         </folder>
     *     </folder>
     * </folder>
     * }
     * @param file the registration file, typically provided by the infrastructure.
     * @return
     */
    public static Object editorKit(FileObject file) {
        String mimeType = "text/plain";
        if (file.getAttribute("mimeType") instanceof String mt) {
            mimeType = mt;
        }
        return new GenericEditorKit(mimeType);
    }
    /**
     * registers the {@code language-configuration.json}. The content of the file is the configuration.
     * The content can be given using {@code nbres}.
     *
     * @param file the registration file, typically provided by the infrastructure.
     * @return
     * @throws IOException if cannot read the file.
     */
    public static Object languageConfiguration(FileObject file) throws IOException {
        return LanguageConfiguration.from(file.asText());
    }
    public static Object braceMatcher() {
        return new BracesMatcherFactoryImpl();
    }
    public static Object typingCompletion() {
        return new FactoryImpl();
    }
    /**
     * requires an EditorKit that returns an appropriate MIME type/content type out of {@linkplain EditorKit#getContentType}.
     * @return
     */
    public static Object toggleCommentAction() {
        return new ToggleCommentActionImpl();
    }
}
/*
actual existing registrations, for reference when writing the javadoc above
    <folder name="Editors">
        <folder name="text">
            <folder name="X-c++">
                <file name="EditorKit.instance">
                    <!--org.netbeans.modules.cpplite.editor.file.CPPDataObject-->
                    <attr methodvalue="org.netbeans.modules.lsp.client.api.LanguageFeatures.editorKit" name="instanceCreate"/>
                    <attr name="instanceOf" stringvalue="javax.swing.text.EditorKit"/>
                    <attr name="mimeType" stringvalue="text/X-c++"/>
                </file>
                <file name="languageConfiguration.instance" url="nbresloc:/org/netbeans/modules/cpplite/editor/file/resources/language-configuration.json">
                    <!--org.netbeans.modules.cpplite.editor.file.CPPDataObject-->
                    <attr methodvalue="org.netbeans.modules.lsp.client.api.LanguageFeatures.languageConfiguration" name="instanceCreate"/>
                </file>
                <folder name="BracesMatchers">
                    <file name="languageConfigurationMatcher.instance">
                        <!--org.netbeans.modules.cpplite.editor.file.CPPDataObject-->
                        <attr methodvalue="org.netbeans.modules.lsp.client.api.LanguageFeatures.braceMatcher" name="instanceCreate"/>
                        <attr name="instanceOf" stringvalue="org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory"/>
                    </file>
                </folder>
                <file name="typingCompletion.instance">
                    <!--org.netbeans.modules.cpplite.editor.file.CPPDataObject-->
                    <attr methodvalue="org.netbeans.modules.lsp.client.api.LanguageFeatures.typingCompletion" name="instanceCreate"/>
                    <attr name="instanceOf" stringvalue="org.netbeans.spi.editor.typinghooks.TypedTextInterceptor$Factory"/>
                </file>
                <folder name="Actions">
                    <file name="toggleComment.instance">
                        <!--org.netbeans.modules.cpplite.editor.file.CPPDataObject-->
                        <attr methodvalue="org.netbeans.modules.lsp.client.api.LanguageFeatures.toggleCommentAction" name="instanceCreate"/>
                        <attr name="instanceOf" stringvalue="javax.swing.text.TextAction"/>
                    </file>
                </folder>
            </folder>
        </folder>
    </folder>


*/