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
package org.netbeans.modules.xml.api.model;

import java.net.URI;
import java.net.URL;
import org.netbeans.api.annotations.common.CheckForNull;

/**
 * Optional mixin interface, which can be implemented together with the
 * {@link GrammarResult} interface on the returned completion item. 
 * It allows to resolve links and provide description contents.
 * <p/>
 * Implementation may return {@code null} from {@link #getDescription} to 
 * indicate that the system should load contents from the URL supplied by {@link #getContentURL()}.
 * <p/>
 * The implementation may choose to provide the content URL to open within the IDE
 * or in an external browser. Any links will be resolved relatively to that URL.
 * For special cases, the implementation may resolve a link to another instance
 * of DescriptionSource by implementing {@link #resolveLink}.
 * <p/>
 * If both {@link #getDescription} and {@link #getContentURL} return null, there's
 * no description at all for the {@link GrammarResult}.
 * 
 * @author sdedic
 * @since 1.28
 */
public interface DescriptionSource {
    /**
     * Provides text of the description. If the method returns null, the 
     * IDE will try to load contents of the {@link #getContentURL()} and use that
     * as a description.
     * 
     * @return Description contents or {@code null}, if contents should be loaded
     * from {@link #getContentURL}.
     */
    @CheckForNull
    public String  getDescription();

    /**
     * True, if the description can be opened by an external browser, following the
     * {@link #getContentURL}.
     * 
     * @return true, if the content URL can be used outside of the IDE
     */
    public boolean isExternal();
    
    /**
     * Returns URL for the content, so it can be retrieved. If this URL is provided,
     * any link not resolved by {@link #resolveLink} will be treated
     * as relative to this content URL.
     * 
     * @return URL of the description content
     */
    @CheckForNull
    public URL  getContentURL();
    
    /**
     * Resolves a link in the documentation to another DescriptionSource. Can
     * return null, if the link cannot be resolved.
     * 
     * @param link link found in the text
     * @return resolved description, or {@code null}
     */
    @CheckForNull
    public DescriptionSource  resolveLink(String link);
}
