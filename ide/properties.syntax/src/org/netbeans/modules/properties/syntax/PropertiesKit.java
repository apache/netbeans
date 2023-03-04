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

package org.netbeans.modules.properties.syntax;

import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.TextAction;
import org.netbeans.editor.Syntax;
import org.netbeans.modules.editor.NbEditorKit;

/**
* Editor kit implementation for text/properties content type
*
* @author Miloslav Metelka, Karel Gardas
* @version 0.01
*/

public class PropertiesKit extends NbEditorKit {

    public static final String PROPERTIES_MIME_TYPE = "text/x-properties"; // NOI18N

    static final long serialVersionUID = 3229768447965508461L;

    @Override
    public String getContentType() {
        return PROPERTIES_MIME_TYPE;
    }
    
    /** Create new instance of syntax coloring parser */
    @Override
    public Syntax createSyntax(Document doc) {
        return new PropertiesSyntax();
    }

    @Override
    protected Action[] createActions() {
        Action[]  actions = new Action[] {
            new ToggleCommentAction("#"), //NOI18N
        };
        return TextAction.augmentList(super.createActions(), actions);
    }
    
}
