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
package org.openide.util.svg;

import com.github.weisj.jsvg.attributes.AttributeParser;
import com.github.weisj.jsvg.parser.ElementLoader;
import com.github.weisj.jsvg.parser.ParsedDocument;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

class DenyingElementLoader implements ElementLoader {
    private final Set<String> attemptedExternalURLsLoaded = new LinkedHashSet<>();

    public Set<String> getAttemptedExternalURLsLoaded() {
        return Collections.unmodifiableSet(attemptedExternalURLsLoaded);
    }

    @Override
    public <T> T loadElement(Class<T> type, String value,
            ParsedDocument document, AttributeParser attributeParser)
    {
        /* Same logic as in com.github.weisj.jsvg.parser.DefaultElementLoader for the
        AllowExternalResources.DENY case, but gathering up the attempted externally loaded URLs so
        we can make the whole loading operation fail and make testLoadImageWithExternalUseXlinkHref
        pass. */
        String url = attributeParser.parseUrl(value);
        if (url == null) {
            return null;
        }
        if (url.contains("#")) {
            String[] parts = url.split("#", 2);
            String name = parts[0];
            if (!name.isEmpty()) {
                attemptedExternalURLsLoaded.add(value);
                return null;
            }
            return document.getElementById(type, parts[1]);
        } else {
            return document.getElementById(type, url);
        }
    }
}
