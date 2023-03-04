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

package org.netbeans.modules.xml.text.folding;

import org.netbeans.api.editor.fold.FoldType;

/**
 * This class defines @see org.netbeans.api.editor.fold.FoldType
 * instancies used in XML code folding.
 *
 * @author  mf100882
 */

public class XmlFoldTypes {

    private static final String FOLD_TYPE_PREFIX = "xml-";//NOI18N

    /** XML tag fold type */
    public static final FoldType TAG = new FoldType(FOLD_TYPE_PREFIX + "tag"); // NOI18N

    /** XML comment fold type */
    public static final FoldType COMMENT = new FoldType(FOLD_TYPE_PREFIX + "comment"); // NOI18N

    /** XML processing instruction fold type */
    public static final FoldType PI = new FoldType(FOLD_TYPE_PREFIX + "pi"); // NOI18N

    /** XML doctype fold type */
    public static final FoldType DOCTYPE = new FoldType(FOLD_TYPE_PREFIX + "doctype"); // NOI18N

    /** XML cdata section fold type */
    public static final FoldType CDATA = new FoldType(FOLD_TYPE_PREFIX + "cdata"); // NOI18N

    /** XML content section fold type */
    public static final FoldType TEXT = new FoldType(FOLD_TYPE_PREFIX + "text"); // NOI18N

}
