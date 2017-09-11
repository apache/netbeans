/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.webkit.debugging.api.css;

import org.json.simple.JSONObject;

/**
 * Media query descriptor.
 *
 * @author Jan Stola
 */
public class Media {
    /** Media query text. */
    private final String text;
    /** Source of the media query. */
    private final Source source;

    /**
     * Creates a new {@code Media} that corresponds to the given JSONObject.
     *
     * @param media JSONObject describing the media query.
     */
    Media(JSONObject media) {
        text = (String)media.get("text"); // NOI18N
        String codeOfSource = (String)media.get("source"); // NOI18N
        source = Source.forCode(codeOfSource);
    }

    /**
     * Returns the media query text.
     *
     * @return media query text.
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the source of the media query.
     *
     * @return source of the media query.
     */
    public Source getSource() {
        return source;
    }

    /**
     * Source of the media query.
     */
    public static enum Source {
        /** Specified by a {@code @media} rule. */
        MEDIA_RULE,
        /** Specified by an {@code @import} rule. */
        IMPORT_RULE,
        /** Specified by a {@code media} attribute in a linked stylesheet's {@code LINK} tag. */
        LINKED_SHEET,
        /** Specified by a {@code media} attribute in an inline stylesheet's {@code STYLE} tag. */
        INLINE_SHEET;

        /**
         * Returns the media source for the given code.
         *
         * @param code code of the media source.
         * @return media source matching the given code or {@code null}
         * for an unknown code.
         */
        static Source forCode(String code) {
            Source source = null;
            if ("mediaRule".equals(code)) { // NOI18N
                source = MEDIA_RULE;
            } else if ("importRule".equals(code)) { // NOI18N
                source = IMPORT_RULE;
            } else if ("linkedSheet".equals(code)) { // NOI18N
                source = LINKED_SHEET;
            } else if ("inlineSheet".equals(code)) { // NOI18N
                source = INLINE_SHEET;
            }
            return source;
        }
    }

}
