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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.lib2.view;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Various statistics about view creation and maintaining.
 * 
 * @author Miloslav Metelka
 */

public final class ViewStats {

    // -J-Dorg.netbeans.modules.editor.lib2.view.ViewStats.level=FINE
    private static final Logger LOG = Logger.getLogger(ViewStats.class.getName());
    
    private static final int TEXT_LAYOUT_CREATED_OR_REUSED_THRESHOLD = 200;
    
    private static final int STALE_VIEW_CREATION_TIMEOUT = 10;
    
    private static int textLayoutCreatedCount;
    
    private static int textLayoutCreatedCharCount;
    
    private static int textLayoutReusedCount;
    
    private static int textLayoutReusedCharCount;
    
    private static int staleViewCreationCount;

    private ViewStats() { // No instances
    }
    
    public static void incrementTextLayoutCreated(int charCount) {
        textLayoutCreatedCount++;
        textLayoutCreatedCharCount += charCount;
        if (LOG.isLoggable(Level.FINE)) {
            if (LOG.isLoggable(Level.FINEST) ||
                    (textLayoutCreatedCount % TEXT_LAYOUT_CREATED_OR_REUSED_THRESHOLD) == 0)
            {
                LOG.fine(stats());
            }
        }
    }

    public static void incrementTextLayoutReused(int charCount) {
        textLayoutReusedCount++;
        textLayoutReusedCharCount += charCount;
        if (LOG.isLoggable(Level.FINE)) {
            if (LOG.isLoggable(Level.FINEST) ||
                    (textLayoutReusedCount % TEXT_LAYOUT_CREATED_OR_REUSED_THRESHOLD) == 0)
            {
                LOG.fine(stats());
            }
        }
    }

    public static void incrementStaleViewCreations() {
        staleViewCreationCount++;;
        if (LOG.isLoggable(Level.FINE)) {
            if (LOG.isLoggable(Level.FINEST) ||
                (staleViewCreationCount % STALE_VIEW_CREATION_TIMEOUT) == 0)
            {
                LOG.fine(stats());
            }
        }
    }
    
    public static String stats() {
        return "TextLayouts:" + // NOI18N
                "\n  Created:\tcount: " + textLayoutCreatedCount + // NOI18N
                "\tchar-count: " + textLayoutCreatedCharCount + // NOI18N
                "\n  Reused:\tcount: " + textLayoutReusedCount + // NOI18N
                "\tchar-count: " + textLayoutReusedCharCount + // NOI18N
                "\nStaleCreations: " + staleViewCreationCount + // NOI18N
                "\n"; // NOI18N
    }

}
