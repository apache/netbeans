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
package org.netbeans.modules.php.twig.editor.ui.options;

import java.util.prefs.Preferences;
import org.netbeans.modules.php.twig.editor.actions.ToggleBlockCommentAction;
import org.openide.util.NbPreferences;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class TwigOptions {
    private static final TwigOptions INSTANCE = new TwigOptions();
    private static final String TOGGLE_COMMENT = "twig-toggle-comment"; //NOI18N
    private static final String TWIG_OPTIONS = "twig-options"; //NOI18N

    private Preferences getPreferences() {
        return NbPreferences.forModule(TwigOptions.class).node(TWIG_OPTIONS);
    }

    private TwigOptions() {
    }

    public static TwigOptions getInstance() {
        return INSTANCE;
    }

    public void setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType toggleComment) {
        getPreferences().put(TOGGLE_COMMENT, toggleComment.name());
    }

    public ToggleBlockCommentAction.ToggleCommentType getToggleCommentType() {
        String toggleCommentName = getPreferences().get(TOGGLE_COMMENT, ToggleBlockCommentAction.ToggleCommentType.AS_TWIG_EVERYWHERE.name());
        return ToggleBlockCommentAction.ToggleCommentType.valueOf(toggleCommentName);
    }

}
