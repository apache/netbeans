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

import java.net.URL;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/**
 * Represents documentation displayed in code completion window.
 *
 * @author Petr Hejl
 * @since 2.43
 */
public final class Documentation {

    private final String content;

    private final URL url;

    private Documentation(String content, URL url) {
        assert content != null;
        this.content = content;
        this.url = url;
    }

    @NonNull
    public static Documentation create(@NonNull String content) {
        Parameters.notNull("content", content);
        return new Documentation(content, null);
    }

    @NonNull
    public static Documentation create(@NonNull String content, URL url) {
        Parameters.notNull("content", content);
        return new Documentation(content, url);
    }

    /**
     * The documentation itself.
     *
     * @return documentation itself
     */
    @NonNull
    public String getContent() {
        return content;
    }

    /**
     * The external documentation URL. Might be {@code null}.
     *
     * @return external documentation URL
     */
    @CheckForNull
    public URL getUrl() {
        return url;
    }

}
