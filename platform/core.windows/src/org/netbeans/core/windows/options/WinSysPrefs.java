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

package org.netbeans.core.windows.options;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Keys and access to window system related preferences. 
 * 
 * @author Dafe Simonek
 */
public interface WinSysPrefs {
    
    public static final Preferences HANDLER = NbPreferences.forModule(WinSysPrefs.class);

    public final String DND_SMALLWINDOWS = "dnd.smallwindows";
     
    public final String DND_SMALLWINDOWS_WIDTH = "dnd.smallwindows.width";
    
    public final String DND_SMALLWINDOWS_HEIGHT = "dnd.smallwindows.height";
    
    public final String DND_DRAGIMAGE = "dnd.dragimage";
     
    public final String TRANSPARENCY_DRAGIMAGE = "transparency.dragimage";
    
    public final String TRANSPARENCY_DRAGIMAGE_ALPHA = "transparency.dragimage.alpha";
    
    public final String TRANSPARENCY_FLOATING = "transparency.floating";
    
    public final String TRANSPARENCY_FLOATING_TIMEOUT = "transparency.floating.timeout";
    
    public final String TRANSPARENCY_FLOATING_ALPHA = "transparency.floating.alpha";
    
    public final String SNAPPING = "snapping";
    
    public final String SNAPPING_SCREENEDGES = "snapping.screenedges";
    
    public final String SNAPPING_ACTIVE_SIZE = "snapping.active.size";
    
    /**
     * If true then the most recent document is activate when some editor is closed.
     * If false then the editor window to the left of the document being closed is activated.
     */
    public final String EDITOR_CLOSE_ACTIVATES_RECENT = "editor.closing.activates.recent";
    
    /**
     * If true then new documents will open next to the active document tab.
     * If false new documents will open as the last document tab.
     * @since 2.38
     */
    public final String OPEN_DOCUMENTS_NEXT_TO_ACTIVE_TAB = "editor.open.next.to.active";

    /**
     * @since 2.43
     */
    public final String DOCUMENT_TABS_PLACEMENT = "document.tabs.placement";

    /**
     * @since 2.43
     */
    public final String DOCUMENT_TABS_MULTIROW = "document.tabs.multirow";

    /**
     * @since 2.54
     */
    public static String MAXIMIZE_NATIVE_LAF = "laf.maximize.native"; //NOI18N

    /**
     * @since 2.87
     */
    public static String EDITOR_SORT_TABS = "editor.sort.tabs"; //NOI18N
}
