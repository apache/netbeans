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
package org.netbeans.modules.javascript2.jsdoc;

/**
 * Represents specific comment type for jsDoc documentation tool.
 * <p>
 * //   single line type
 * /*   traditional type <star>/
 * /**  documentation type <star>/
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public enum JsDocCommentType {

    DOC_COMMON("common"), //NOI18N
    DOC_NO_CODE_START("noCodeStart"), //NOI18N
    DOC_NO_CODE_END("noCodeEnd"), //NOI18N
    DOC_SHARED_TAG_START("sharedTagStart"), //NOI18N
    DOC_SHARED_TAG_END("sharedTagEnd"); //NOI18N

    private final String value;

    private JsDocCommentType(String textValue) {
        this.value = textValue;
    }

    @Override
    public String toString() {
        return value;
    }
}
