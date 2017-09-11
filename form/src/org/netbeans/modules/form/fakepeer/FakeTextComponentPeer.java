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
class FakeTextComponentPeer extends FakeComponentPeer
{
    private String _text;
    private int _caretPosition = 0;
    private int selStart = -1;
    private int selEnd = -1;

    FakeTextComponentPeer(TextComponent target) {
        super(target);
    }

    @Override
    Component createDelegate() {
        return new Delegate();
    }

    @Override
    void initDelegate() {
        _text = ((TextComponent)_target).getText();
        super.initDelegate();
    }

    @Override
    public boolean isFocusTraversable() {
        return true;
    }

    public void setEditable(boolean editable) {
        repaint();
    }

    public String getText() {
        return _text;
    }

    public void setText(String text) {
        _text = text;
        repaint();
    }

    public int getSelectionStart() {
        return selStart;
    }

    public int getSelectionEnd() {
        return selEnd;
    }

    public void select(int selStart, int selEnd) {
        this.selStart = selStart;
        this.selEnd = selEnd;
    }

    public void setCaretPosition(int pos) {
        if (pos == 0 || (_text != null && _text.length() > pos))
            _caretPosition = pos;
    }

    public int getCaretPosition() {
        return _caretPosition;
    }

    // JDK 1.3
    public int getIndexAtPoint(int x, int y) {
        return 0;
    }

    // JDK 1.3
    public Rectangle getCharacterBounds(int i) {
        return null;
    }

    // JDK 1.3
    public long filterEvents(long mask) {
        return 0;
    }

    // JDK 1.5
    public java.awt.im.InputMethodRequests getInputMethodRequests() {
        return null;
    }

    //
    //
    //

    protected class Delegate extends Component
    {
        Delegate() {
            this.setBackground(SystemColor.window);
            this.setForeground(SystemColor.windowText);
        }
        
        @Override
        public void paint(Graphics g) {
            Dimension sz = _target.getSize();
            int w = sz.width;
            int h = sz.height;

            g.setColor(_target.getBackground());
            FakePeerUtils.drawLoweredBox(g,0,0,w,h);

            Rectangle r = g.getClipBounds();
            if (r.x < 1) r.x = 1;
            if (r.y < 1) r.y = 1;
            if (r.width > w-3) r.width = w - 3;
            if (r.height > h-3) r.height = h - 3;
            g.setClip(r);
//            g.setClip(1,1,w-3,h-3);

            if (_target.isEnabled()) {
                g.setColor(_target.getForeground());
            }
            else {
                g.setColor(SystemColor.controlShadow);
            }
        }
    }
}
