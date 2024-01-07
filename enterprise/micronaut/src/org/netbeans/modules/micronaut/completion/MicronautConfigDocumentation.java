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
package org.netbeans.modules.micronaut.completion;

import java.net.URL;
import javax.swing.Action;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.Deprecation;

/**
 *
 * @author Dusan Balek
 */
public class MicronautConfigDocumentation implements CompletionDocumentation {

    private final ConfigurationMetadataProperty element;

    public MicronautConfigDocumentation(ConfigurationMetadataProperty element) {
        this.element = element;
    }

    @Override
    public String getText() {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>").append(makeNameLineBreakable(element.getId())).append("</b>");
        String type = element.getType();
        if (type != null) {
            sb.append("<pre>").append(type).append("</pre>");
        }
        String description = element.getDescription();
        String params = null;
        if (description != null) {
            int idx = description.indexOf("@param");
            if (idx >= 0) {
                params = description.substring(idx + 6).trim();
                description = description.substring(0, idx).trim();
            }
            if (!description.isEmpty()) {
                sb.append("<p>").append(description).append("</p>");
            }
        }
        Deprecation deprecation = element.getDeprecation();
        if (deprecation != null) {
            sb.append("<p><b>Deprecated");
            String reason = deprecation.getReason();
            if (reason != null) {
                sb.append(":</b> <i>").append(reason).append("</i>");
            } else {
                sb.append("</b>");
            }
            String replacement = deprecation.getReplacement();
            if (replacement != null) {
                sb.append("<br/>Replaced by: <code>").append(replacement).append("</code>");
            }
            sb.append("</p>");
        }
        if (params != null) {
            sb.append("<p><b>Parameters:</b><blockquote>");
            int idx = params.indexOf(' ');
            if (idx < 0) {
                sb.append(params).append("</p>");
            } else {
                sb.append("<code>").append(params.substring(0, idx)).append("</code>").append(params.substring(idx)).append("</blockquote></p>");
            }
        }
        return sb.toString();
    }

    @Override
    public URL getURL() {
        return null;
    }

    @Override
    public CompletionDocumentation resolveLink(String link) {
        return null;
    }

    @Override
    public Action getGotoSourceAction() {
        return null;
    }

    private String makeNameLineBreakable(String name) {
        return name.replace(".", /* ZERO WIDTH SPACE */".&#x200B;");
    }
}
