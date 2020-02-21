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

package org.netbeans.modules.cnd.callgraph.impl;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.modules.cnd.callgraph.api.Call;
import org.netbeans.modules.cnd.callgraph.api.Function;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 */
public class GoToReferenceAction extends AbstractAction implements Presenter.Popup {
    public static final int FUNCTION = 0;
    public static final int CALLER = 1;
    public static final int CALLEE = 2;
    
    private Call call;
    private Function function;
    private JMenuItem menuItem;
    
    public GoToReferenceAction(Call call) {
        this.call = call;
        putValue(Action.NAME, getString("GoToReference")); // NOI18N
        menuItem = new JMenuItem(this); 
        Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
    }

    public GoToReferenceAction(Function function, int what) {
        this.function = function;
        switch(what){
            case 0:
                putValue(Action.NAME, getString("GoToFunction")); // NOI18N
                break;
            case 1:
                putValue(Action.NAME, getString("GoToCaller")); // NOI18N
                break;
            case 2:
                putValue(Action.NAME, getString("GoToCallee")); // NOI18N
                break;
        }
        menuItem = new JMenuItem(this); 
        Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
    }

    public JMenuItem getPopupPresenter() {
        return menuItem;
    }
    
    public void actionPerformed(ActionEvent e) {
        if (call != null) {
            call.open();
        } else {
            function.open();
        }
    }

    private String getString(String key) {
        return NbBundle.getMessage(getClass(), key);
    }

}
