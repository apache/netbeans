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
 * License. When distributing the software, include this License Header
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
package org.netbeans.modules.print.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import static org.netbeans.modules.print.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.03.09
 */
public enum Macro {

    NAME, // the name of area
    USER, // the user name
    ROW, // row number
    COLUMN, // column number
    COUNT, // total count
    MODIFIED_DATE, // date of the last modification
    MODIFIED_TIME, // time of the last modification
    PRINTED_DATE, // date of printing
    PRINTED_TIME; // time of printing

    public interface Listener {
        void pressed(Macro macro);
    }

    private Macro() {
        myName = "%" + name() + "%"; // NOI18N
        myButton = new JButton();
        myButton.setFocusable(false);
        myButton.setToolTipText(getToolTipText());
        myButton.setMnemonic(KeyEvent.VK_1 + ordinal());
        myButton.setIcon(icon(getClass(), name().toLowerCase()));
    }

    public String getName() {
        return myName;
    }

    public JButton getButton() {
        return myButton;
    }

    private String getToolTipText() {
        String alt = " (Alt-" + (ordinal() + 1) + ")"; // NOI18N
        return i18n(Macro.class, name()) + alt;
    }

    public JButton getButton(final Listener listener) {
        myButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                listener.pressed(Macro.this);
            }
        });
        return getButton();
    }

    private String myName;
    private JButton myButton;
}
