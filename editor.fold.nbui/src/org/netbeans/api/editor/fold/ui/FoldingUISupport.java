/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.api.editor.fold.ui;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.fold.ui.CodeFoldingSideBar;
import org.netbeans.spi.editor.SideBarFactory;

/**
 *
 * @author
 * sdedic
 */
public class FoldingUISupport {
    private FoldingUISupport() {}
    
    /**
     * Creates a component for the code folding sidebar.
     * Returns a standard folding sidebar component, which displays code folds. This sidebar implementation 
     * can be created only once per text component.
     *
     * @param textComponent the text component which should work with the Sidebar
     * @return Sidebar instance.
     */
    public static JComponent sidebarComponent(JTextComponent textComponent) {
        return new CodeFoldingSideBar(textComponent);
    }

    private static SideBarFactory FACTORY = null;

    /**
     * Obtains an instance of folding sidebar factory. This method should
     * be used in layer, in the MIME lookup area, to register a sidebar with
     * an editor for a specific MIMEtype.
     * <p/>
     * There's a default sidebar instance registered for all MIME types. 
     *
     * @return shared sidebar factory instance
     */
    public static SideBarFactory foldingSidebarFactory() {
        if (FACTORY != null) {
            return FACTORY;
        }
        return FACTORY = new CodeFoldingSideBar.Factory();
    }

    public static void disableCodeFoldingSidebar(JTextComponent text) {
        text.putClientProperty(CodeFoldingSideBar.PROP_SIDEBAR_MARK, true);
    }
}
