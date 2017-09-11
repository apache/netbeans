/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.csl.api;

import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;

/**
 * Manage a set of options configurable by the user in the IDE.
 * Language plugins can and should register their own options panels,
 * but some editor options (such as tab settings) are managed by the IDE,
 * in and these can be accessed via this class.
 * 
 * @author Tor Norbye
 */
public final class EditorOptions {
    
    @CheckForNull
    public static EditorOptions get (
        String                  mimeType
    ) {
        return new EditorOptions (mimeType);
    }
    
    private final String        mimeType;
    private final Preferences   preferences;

    private EditorOptions (
        String                  mimeType
    ) {
        this.mimeType = mimeType;
        this.preferences = MimeLookup.getLookup (mimeType).lookup (Preferences.class);
    }

    public int getTabSize () {
        return preferences.getInt (SimpleValueNames.TAB_SIZE, 8);
    }

    public boolean getExpandTabs () {
        return preferences.getBoolean (SimpleValueNames.EXPAND_TABS, true);
    }

    public int getSpacesPerTab () {
        return preferences.getInt (SimpleValueNames.SPACES_PER_TAB, 2);
    }

    public boolean getMatchBrackets () {
        return preferences.getBoolean (SimpleValueNames.COMPLETION_PAIR_CHARACTERS, true);
    }

    public int getRightMargin () {
        return preferences.getInt (SimpleValueNames.TEXT_LIMIT_WIDTH, 80);
    }
}
