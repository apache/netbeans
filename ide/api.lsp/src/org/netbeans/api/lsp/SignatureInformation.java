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
package org.netbeans.api.lsp;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.lsp.SignatureInformationAccessor;
import org.netbeans.spi.lsp.SignatureInformationCollector;

/**
 * Represents the signature of something callable. A signature can have a label,
 * like a function-name, a doc-comment, and a set of parameters.
 *
 * @author Dusan Balek
 * @since 1.20
 */
public final class SignatureInformation {

    static {
        SignatureInformationAccessor.setDefault(new SignatureInformationAccessor() {
            @Override
            public SignatureInformation createSignatureInformation(String label, List<ParameterInformation> params, boolean isActive, String documentation) {
                return new SignatureInformation(label, params, isActive, documentation);
            }

            @Override
            public ParameterInformation createParameterInformation(String label, boolean isActive, String documentation) {
                return new ParameterInformation(label, isActive, documentation);
            }
        });
    }

    private final String label;
    private final List<ParameterInformation> params;
    private final boolean isActive;
    private final String documentation;

    private SignatureInformation(String label, List<ParameterInformation> params, boolean isActive, String documentation) {
        this.label = label;
        this.params = params;
        this.isActive = isActive;
        this.documentation = documentation;
    }

    /**
     * The label of this signature information.
     *
     * @since 1.20
     */
    @NonNull
    public String getLabel() {
        return label;
    }

    /**
     * The parameters of this signature.
     *
     * @since 1.20
     */
    @NonNull
    public List<ParameterInformation> getParameters() {
        return Collections.unmodifiableList(params);
    }

    /**
     * Returns true if the signature is active.
     *
     * @since 1.20
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * A human-readable string that represents a doc-comment. An HTML format is
     * supported.
     *
     * @since 1.20
     */
    @CheckForNull
    public String getDocumentation() {
        return documentation;
    }

    /**
     * Computes and collects signature information for a document at a given offset. Example
     * usage can be illustrated by:
     * {@snippet file="org/netbeans/api/lsp/SignatureInformationTest.java" region="testSignatureInformationCollect"}
     *
     * @param doc a text document
     * @param offset an offset inside the text document
     * @param context an optional signature help context
     * @param consumer an operation accepting collected signature information
     *
     * @since 1.20
     */
    public static void collect(@NonNull Document doc, int offset, @NullAllowed Context context, @NonNull Consumer<SignatureInformation> consumer) {
        MimePath mimePath = MimePath.parse(DocumentUtilities.getMimeType(doc));
        for (SignatureInformationCollector collector : MimeLookup.getLookup(mimePath).lookupAll(SignatureInformationCollector.class)) {
            collector.collectSignatureInformation(doc, offset, context, consumer);
        }
    }

    /**
     * Represents a parameter of a callable-signature. A parameter can
     * have a label and a doc-comment.
     *
     * @since 1.20
     */
    public static final class ParameterInformation {

        private final String label;
        private final boolean isActive;
        private final String documentation;

        private ParameterInformation(String label, boolean isActive, String documentation) {
            this.label = label;
            this.isActive = isActive;
            this.documentation = documentation;
        }

        /**
	 * The label of this parameter information.
	 * <p>
	 * <i>Note</i>: a label should be a substring of its containing
	 * signature label. Its intended use case is to highlight the parameter
	 * label part in the {@code SignatureInformation.label}.
         *
         * @since 1.20
	 */
        @NonNull
        public String getLabel() {
            return label;
        }

        /**
         * Returns true if the parameter is active.
         *
         * @since 1.20
         */
        public boolean isActive() {
            return isActive;
        }

        /**
         * A human-readable string that represents a doc-comment. An HTML format is
         * supported.
         *
         * @since 1.20
         */
        @CheckForNull
        public String getDocumentation() {
            return documentation;
        }
    }

    /**
     * Additional information about the context in which a signature help request
     * was triggered.
     *
     * @since 1.20
     */
    public static final class Context {

        private final TriggerKind triggerKind;
        private final Character triggerCharacter;

        public Context(@NonNull TriggerKind triggerKind, @NullAllowed Character triggerCharacter) {
            this.triggerKind = triggerKind;
            this.triggerCharacter = triggerCharacter;
        }

        /**
         * Action that caused signature help to be triggered.
         *
         * @since 1.20
         */
        @NonNull
        public TriggerKind getTriggerKind() {
            return triggerKind;
        }

        /**
         * Character that caused signature help to be triggered.
         * Is undefined if {@code triggerKind != TriggerKind.TriggerCharacter}.
         *
         * @since 1.20
         */
        @CheckForNull
        public Character getTriggerCharacter() {
            return triggerCharacter;
        }
    }

    /**
     * Specifies how a signature help was triggered.
     *
     * @since 1.20
     */
    public enum TriggerKind {

        /**
         * Signature help was invoked manually by the user or by a command.
         *
         * @since 1.20
         */
        Invoked,

        /**
         * Signature help was triggered by a trigger character.
         *
         * @since 1.20
         */
        TriggerCharacter,

        /**
         * Signature help was triggered by the cursor moving or by the document
	 * content changing.
         *
         * @since 1.20
         */
        ContentChange
    }
}
