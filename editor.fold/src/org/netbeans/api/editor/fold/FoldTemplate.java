/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.api.editor.fold;

import org.openide.util.NbBundle;

import static org.netbeans.api.editor.fold.Bundle.*;

/**
 * Template that describes how the fold should appear and interact with the
 * user. The FoldTemplate defines how many characters at the start (end) should
 * act as a fold guard: change to that area will remove the fold because it
 * becomes damaged. It also defines what placeholder should appear in place of the 
 * folded code.
 * <p/>
 * A template may be attached to a {@link FoldType} instance. Folds of that kind will
 * automatically use the attached Template, if not overriden explicitly.
 * <p/>
 * The string {@code "..."} ({@link #CONTENT_PLACEHOLDER}) is treated as a placeholder. If a 
 * ContentReader is available for the {@link FoldType}, the placeholder will be replaced by 
 * the product of the Reader. Otherwise the placeholder will remain in the displayText 
 * and will be presented.
 * 
 * @since 1.35
 * @author sdedic
 */
public final class FoldTemplate {
    /**
     * The default template for folded text: no markers before+after, ellipsis
     * shown.
     */
    @NbBundle.Messages("FT_DefaultTemplate=...")
    public static final FoldTemplate DEFAULT = new FoldTemplate(0, 0, FT_DefaultTemplate()); // NOI18N
    
    /**
     * A standard template, which represents a block of something.
     */
    @NbBundle.Messages("FT_DefaultBlockTemplate={...}")
    public static final FoldTemplate DEFAULT_BLOCK = new FoldTemplate(0, 0, FT_DefaultBlockTemplate()); // NOI18N
    
    /**
     * This string is interpreted as a placeholder for the infrastructure to inject 
     */
    public static final String CONTENT_PLACEHOLDER = FT_DefaultTemplate();
    
    /**
     * # of characters at the start of the fold, which serve as a marker that the fold
     * has been damaged/destroyed
     */
    private int     guardedStart;
    
    /**
     * The guarded portion at the end of the fold
     */
    private int     guardedEnd;
    
    /**
     * Description that appears in the folded area.
     */
    private String  displayText;
    
    /**
     * Creates a FoldTemplate with a fixed description.
     * 
     * @param guardedStart length of the start marker, or -1 if no start marker is present
     * @param guardedEnd length of the end marker, or -1 if no end marker is present
     * @param displayText text which should be displayed in place of the folded content
     */
    public FoldTemplate(int guardedStart, int guardedEnd, String displayText) {
        this.guardedStart = guardedStart;
        this.guardedEnd = guardedEnd;
        this.displayText = displayText;
    }

    public String getDescription() {
        return displayText;
    }

    public int getGuardedEnd() {
        return guardedEnd;
    }

    public int getGuardedStart() {
        return guardedStart;
    }
}
