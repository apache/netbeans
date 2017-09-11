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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    
}
