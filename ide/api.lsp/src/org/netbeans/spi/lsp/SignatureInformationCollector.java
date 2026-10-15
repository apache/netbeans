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
package org.netbeans.spi.lsp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.lsp.SignatureInformation;
import org.netbeans.modules.lsp.SignatureInformationAccessor;
import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 * Interface for computing and collecting signature information. Clients can use
 * this interface to collect signature information and send them for presentation
 * outside of NetBeans using the Language Server Protocol. Implementations of the
 * interface should be registered in MimeLookup.
 * {@snippet :
 *
 *  {@code @}MimeRegistration(mimeType = "text/foo", service = SignatureInformationCollector.class)
 *  public class FooSignatureInformationCollector implements SignatureInformationCollector {
 *     ...
 *  }
 * }
 *
 * @author Dusan Balek
 * @since 1.20
 */
@MimeLocation(subfolderName = "SignatureHelpProviders")
public interface SignatureInformationCollector {

    /**
     * Computes and collects signature information for a document at a given offset.
     *
     * @param doc a text document
     * @param offset an offset inside the text document
     * @param context an optional signature help context
     * @param consumer an operation accepting collected signature information
     *
     *
     * @since 1.0
     */
    public void collectSignatureInformation(@NonNull Document doc, int offset, @NullAllowed SignatureInformation.Context context, @NonNull Consumer<SignatureInformation> consumer);

    /**
     * Creates a builder for {@link SignatureInformation} instances.
     *
     * @param label the label of the signature information
     * @param isActive true if the signature is active
     * @return newly created builder
     *
     * @since 1.20
     */
    public static Builder newBuilder(@NonNull String label, boolean isActive) {
        return new Builder(label, isActive);
    }

    /**
     * Builder for {@link SignatureInformation} instances. Its usage can be illustrated by:
     * {@snippet file="org/netbeans/api/lsp/SignatureInformationTest.java" region="builder"}
     *
     * @since 1.20
     */
    public static final class Builder {

        private final String label;
        private final List<SignatureInformation.ParameterInformation> params;
        private final boolean isActive;
        private String documentation;

        private Builder(@NonNull String label, boolean isActive) {
            this.label = label;
            this.isActive = isActive;
            this.params = new ArrayList<>();
        }

        /**
         * A human-readable string that represents a doc-comment. An HTML format
         * is supported.
         *
         * @since 1.20
         */
        @NonNull
        public Builder documentation(@NonNull String documentation) {
            this.documentation = documentation;
            return this;
        }

        /**
         * Adds parameter information to this signature.
         *
         * @param label label of the parameter information
         * @param isActive true if the the parameter is active
         * @param documentation an optional doc-comment of the parameter
         *
         * @since 1.20
         */
        @NonNull
        public Builder addParameter(@NonNull String label, boolean isActive, @NullAllowed String documentation) {
            this.params.add(SignatureInformationAccessor.getDefault().createParameterInformation(label, isActive, documentation));
            return this;
        }

        /**
         * Builds signature information.
         *
         * @since 1.20
         */
        @NonNull
        public SignatureInformation build() {
            return SignatureInformationAccessor.getDefault().createSignatureInformation(label, params, isActive, documentation);
        }
    }
}
