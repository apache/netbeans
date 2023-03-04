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

package org.netbeans.modules.javadoc.search;

import java.beans.*;

/**
 *
 * @author  Petr Suchomel
 * @version
 */

public final class JapanJavadocEncodings extends PropertyEditorSupport {

    private static final String[] tags = { "JISAutoDetect", "SJIS", "EUC-JP", "ISO-2022-JP", "UTF-8"};     //NOI18N

    /** @return names of the supported encodings */
    public String[] getTags() {
        return tags;
    }

    /** @return text for the current value */
    public String getAsText () {
        return ((String)getValue());
    }

    /** @param text A text for the current value. */
    public void setAsText (String text) {
        setValue( text );
    }
}
