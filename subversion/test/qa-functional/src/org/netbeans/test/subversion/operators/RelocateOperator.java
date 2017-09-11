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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.test.subversion.operators;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
;
import org.netbeans.test.subversion.operators.actions.RelocateAction;

/**
 *
 * @author peterpis
 */
public class RelocateOperator extends NbDialogOperator {

    private JLabelOperator _lblWC;
    private JLabelOperator _lblCurrentURL;
    private JLabelOperator _lblNewURL;
    private JTextFieldOperator _tfWC;
    private JTextFieldOperator _tfCurrentURL;
    private JTextFieldOperator _tfNewURL;
    private JButtonOperator _btnRelocate;
    private JButtonOperator _btnCancel;
    private JButtonOperator _btnHelp;

    public RelocateOperator() {
        super("Relocate");
    }

    public static RelocateOperator invoke(Node node) {
        new RelocateAction().perform(node);
        return new RelocateOperator();
    }


    public JLabelOperator lblWC() {
        if (_lblWC == null) {
            _lblWC = new JLabelOperator(this, "Working Copy:");
        }
        return _lblWC;
    }

    public JLabelOperator lblCurrentURL() {
        if (_lblCurrentURL == null) {
            _lblCurrentURL = new JLabelOperator(this, "Current URL:");
        }
        return _lblCurrentURL;
    }

    public JLabelOperator lblNewURL() {
        if (_lblNewURL == null) {
            _lblNewURL = new JLabelOperator(this, "New URL:");
        }
        return _lblNewURL;
    }

    public JTextFieldOperator tfWC() {
        if (_tfWC == null) {
            _tfWC = new JTextFieldOperator(this, 0);
        }
        return _tfWC;
    }

    public JTextFieldOperator tfCurrentURL() {
        if (_tfCurrentURL == null) {
            _tfCurrentURL = new JTextFieldOperator(this, 1);
        }
        return _tfCurrentURL;
    }

    public JTextFieldOperator tfNewURL() {
        if (_tfNewURL == null) {
            _tfNewURL = new JTextFieldOperator(this, 2);
        }
        return _tfNewURL;
    }

    public void typeText(String text) {
        tfNewURL().typeText(text);
    }

    public JButtonOperator btnRelocate() {
        if (_btnRelocate == null) {
            _btnRelocate = new JButtonOperator(this, "Relocate");
        }
        return _btnRelocate;
    }

    public JButtonOperator btnCancel() {
        if (_btnCancel == null) {
            _btnCancel = new JButtonOperator(this, "Cancel");
        }
        return _btnCancel;
    }

    public JButtonOperator btnHelp() {
        if (_btnHelp == null) {
            _btnHelp = new JButtonOperator(this, "Help");
        }
        return _btnHelp;
    }

    public void verify() {
        lblWC();
        lblCurrentURL();
        lblNewURL();
        tfWC();
        tfCurrentURL();
        tfNewURL();
    }
}
