/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.versioning.util.common;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import javax.swing.JComponent;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;

/**
 *
 * @author Tomas Stupka
 */
public abstract class VCSCommitDiffProvider<T extends VCSFileNode> {
    
    private final HashMap<File, JComponent> displayedDiffs = new HashMap<File, JComponent>();
        
    JComponent getDiffComponent(File file) {
        JComponent component = displayedDiffs.get(file);
        if (component == null) {
            component = createDiffComponent(file); //new MultiDiffPanel(file, HgRevision.BASE, HgRevision.CURRENT, false); // switch the last parameter to true if editable diff works poorly
            displayedDiffs.put(file, component);                
        }   
        return component;
    }

    protected abstract JComponent createDiffComponent(File file);
    
    protected JComponent getDiffComponent (T[] files) {
        return null;
    }

    protected Set<File> getModifiedFiles() {
        return Collections.emptySet();
    }

    protected SaveCookie[] getSaveCookies() {
        return new SaveCookie[0];
    }

    protected EditorCookie[] getEditorCookies() {
        return new EditorCookie[0];
    }

    /**
     * Selects the file in the opened diff view. Makes sense only if the diff
     * view is capable of showing more files.
     * @param file file to select
     */
    protected void selectFile (File file) {
        
    }
}
