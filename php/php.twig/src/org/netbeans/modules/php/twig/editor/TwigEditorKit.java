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
/*
 * Contributor(s): Sebastian Hörl
 */
package org.netbeans.modules.php.twig.editor;

import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.TextAction;
import org.netbeans.modules.csl.api.CslActions;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.php.twig.editor.actions.ToggleBlockCommentAction;
import org.netbeans.modules.php.twig.editor.gsf.TwigLanguage;

public class TwigEditorKit extends NbEditorKit {

    @Override
    public Document createDefaultDocument() {
        return super.createDefaultDocument();
    }

    @Override
    public String getContentType() {
        return TwigLanguage.TWIG_MIME_TYPE;
    }

    @Override
    protected Action[] createActions() {
        return TextAction.augmentList(super.createActions(), new Action[] {
            new ToggleBlockCommentAction(),
            CslActions.createInstantRenameAction()});
    }

}
