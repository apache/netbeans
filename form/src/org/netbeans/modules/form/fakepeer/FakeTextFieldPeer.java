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


package org.netbeans.modules.form.fakepeer;

import java.awt.*;

/**
 *
 * @author Tran Duc Trung
 */
class FakeTextFieldPeer extends FakeTextComponentPeer
{
    FakeTextFieldPeer(TextField target) {
        super(target);
    }

    @Override
    Component createDelegate() {
        return new Delegate();
    }

    public void setEchoChar(char echoChar) {
    }

    public Dimension getPreferredSize(int columns) {
        return _delegate.getMinimumSize(); //new Dimension(100, 20);
    }

    public Dimension getMinimumSize(int columns) {
        return _delegate.getMinimumSize(); //new Dimension(100, 20);
    }

    public void setEchoCharacter(char c) {
        setEchoChar(c);
    }

    public Dimension preferredSize(int cols) {
        return getPreferredSize(cols);
    }

    public Dimension minimumSize(int cols) {
        return getMinimumSize(cols);
    }

    //
    //
    //

    private class Delegate extends FakeTextComponentPeer.Delegate
    {
        @Override
        public void paint(Graphics g) {
            super.paint(g);

            TextField target =(TextField) _target;
            String text = target.getText();

            if (text != null) { // draw the text
                String textOut = text.substring(target.getCaretPosition());
//                Dimension sz = target.getSize();
                g.setFont(target.getFont());

                FontMetrics fm = g.getFontMetrics();
                int h = fm.getHeight() - fm.getDescent();
                g.drawString(textOut, 4, 1 + h); //(sz.height - h) / 2 + h -2);
            }
        }

        @Override
        public Dimension getMinimumSize() {
            String text = ((TextField)_target).getText();

            FontMetrics fm = this.getFontMetrics(this.getFont());
            int w = fm.stringWidth(text);
            int h = fm.getHeight();

            return new Dimension(w > 92 ? 100 : w+8, h + 4);
        }
    }
}
