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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.makeproject.api.ui.configurations;

import javax.swing.JComponent;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.ui.customizer.MakeContext;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

public class CustomizerNode {
    public static final String iconbase = "org/netbeans/modules/cnd/makeproject/ui/resources/general"; // NOI18N
    public static final String icon = "org/netbeans/modules/cnd/makeproject/ui/resources/general.gif"; // NOI18N

    private final String name;
    private final String displayName;
    private CustomizerNode[] children;
    protected final Lookup lookup;

    public enum CustomizerStyle {SHEET, PANEL};
        
    public final MakeContext getContext(){
        return lookup.lookup(MakeContext.class);
    }

    public CustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
        this.name = name;
        this.displayName = displayName;
        this.children = children;
        this.lookup = lookup;
    }
    
    public CustomizerStyle customizerStyle() {
        return CustomizerStyle.SHEET; // Backward compatible
    }

    public Sheet[] getSheets(Configuration configuration) {
        return null;
    }
    
    public JComponent getPanel(Configuration configuration) {
        return null;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(""); // NOI18N // See CR 6718766
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public CustomizerNode[] getChildren() {
        return children;
    }
    
    public void setChildren(CustomizerNode[] children) {
        this.children = children;
    }
}
