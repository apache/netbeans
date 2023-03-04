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

package org.netbeans.modules.editor.deprecated.pre65formatting;

import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.FormatterIndentEngine;
import org.netbeans.modules.editor.IndentEngineFormatter;
import org.netbeans.modules.editor.NbEditorDocument;
import org.openide.text.IndentEngine;

/**
 *
 * @author vita
 */
public final class ComplexValueSettingsFactory {

    private ComplexValueSettingsFactory() {
        // no-op
    }
    
    // -----------------------------------------------------------------------
    // 'formatter' setting
    // -----------------------------------------------------------------------

    public static Object getFormatterValue(MimePath mimePath, String settingName) {
        assert settingName.equals(NbEditorDocument.FORMATTER) : "The getFormatter factory called for '" + settingName + "'"; //NOI18N

        IndentEngine eng = org.netbeans.modules.editor.impl.ComplexValueSettingsFactory.getIndentEngine(mimePath);

        if (eng != null) {
            if (eng instanceof FormatterIndentEngine) {
                return ((FormatterIndentEngine)eng).getFormatter();
            } else {
                EditorKit kit = MimeLookup.getLookup(mimePath).lookup(EditorKit.class);
                if (kit != null) {
                    return new IndentEngineFormatter(kit.getClass(), eng);
                }
            }
        }
        
        return null;
    }
}
