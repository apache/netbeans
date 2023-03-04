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
package org.netbeans.modules.web.common.api;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.web.common.cssprep.CssPreprocessorAccessor;
import org.netbeans.modules.web.common.spi.CssPreprocessorImplementation;
import org.netbeans.modules.web.common.spi.CssPreprocessorImplementationListener;
import org.openide.util.Parameters;

/**
 * The API representation of a single CSS preprocessor.
 * @since 1.40
 */
public final class CssPreprocessor {

    private final CssPreprocessorImplementation delegate;

    static {
        CssPreprocessorAccessor.setDefault(new CssPreprocessorAccessor() {
            @Override
            public CssPreprocessor create(CssPreprocessorImplementation cssPreprocessorImplementation) {
                return new CssPreprocessor(cssPreprocessorImplementation);
            }
        });
    }

    private CssPreprocessor(CssPreprocessorImplementation delegate) {
        this.delegate = delegate;
    }

    CssPreprocessorImplementation getDelegate() {
        return delegate;
    }

    void addCssPreprocessorListener(@NullAllowed CssPreprocessorImplementationListener listener) {
        delegate.addCssPreprocessorListener(listener);
    }

    void removeCssPreprocessorListener(@NullAllowed CssPreprocessorImplementationListener listener) {
        delegate.removeCssPreprocessorListener(listener);
    }

    /**
     * Return the <b>non-localized (usually English)</b> identifier of this CSS preprocessor.
     * @return the <b>non-localized (usually English)</b> identifier; never {@code null}
     */
    @NonNull
    public String getIdentifier() {
        String identifier = delegate.getIdentifier();
        Parameters.notNull("identifier", identifier); // NOI18N
        return identifier;
    }

    /**
     * Return the display name of this CSS preprocessor. The display name is used
     * in the UI.
     * @return the display name; never {@code null}
     */
    @NonNull
    public String getDisplayName() {
        String displayName = delegate.getDisplayName();
        Parameters.notNull("displayName", displayName); // NOI18N
        return displayName;
    }

}
