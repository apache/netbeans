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
class FakeTextAreaPeer extends FakeTextComponentPeer
{
    FakeTextAreaPeer(TextArea target) {
        super(target);
    }

    @Override
    Component createDelegate() {
        return new Delegate();
    }

    public void insert(String text, int pos) {
    }

    public void replaceRange(String text, int start, int end) {
    }

    public Dimension getPreferredSize(int rows, int columns) {
        return new Dimension(100, 80);
    }

    public Dimension getMinimumSize(int rows, int columns) {
        return new Dimension(100, 80);
    }

    public void insertText(String txt, int pos) {
        insert(txt, pos);
    }

    public void replaceText(String txt, int start, int end) {
        replaceRange(txt, start, end);
    }

    public Dimension preferredSize(int rows, int cols) {
        return getPreferredSize(rows, cols);
    }

    public Dimension minimumSize(int rows, int cols) {
        return getMinimumSize(rows, cols);
    }

    //
    //
    //

    private class Delegate extends FakeTextComponentPeer.Delegate
    {
        @Override
        public void paint(Graphics g) {
            super.paint(g);

            TextArea target = (TextArea) _target;
            Dimension sz = target.getSize();
            int w = sz.width;
            int h = sz.height;
            String text = target.getText();

            if (text != null) {
                g.setFont(target.getFont());
                g.setColor(target.getForeground());
                
                FontMetrics fm = g.getFontMetrics();
                int th = fm.getHeight();
                int ty = th;
                int i = target.getCaretPosition();
                int len = text.length();
                
                StringBuilder buf = new StringBuilder(len);

                for ( ; i < len; i++) {
                    char ch = text.charAt(i);
                    if (ch != '\n' && ch != '\r') buf.append(ch);
                    else if (ch == '\n') {
                        g.drawString(buf.toString(),4,ty);
                        if (ty > h)
                            break;
                        ty += th;
                        buf.delete(0,buf.length());
                    }
                }
                g.drawString(buf.toString(), 4, ty);
            }

            if (sz.width > FakePeerUtils.SCROLL_W*2 && 
                sz.height > FakePeerUtils.SCROLL_H*2) {
                g.setColor(SystemColor.controlHighlight);
                FakePeerUtils.drawScrollbar(g,2,h-FakePeerUtils.SCROLL_H-2,
                                            w-4-FakePeerUtils.SCROLL_W,FakePeerUtils.SCROLL_H,
                                            Scrollbar.HORIZONTAL,false,true,0,0,0);

                g.setColor(SystemColor.controlHighlight);
                FakePeerUtils.drawScrollbar(g,w-FakePeerUtils.SCROLL_W-2,2,
                                            FakePeerUtils.SCROLL_W,h-4-FakePeerUtils.SCROLL_H,
                                            Scrollbar.VERTICAL,false,true,0,0,0);

                g.setColor(SystemColor.controlHighlight);
                g.fillRect(w-FakePeerUtils.SCROLL_W-2,h-FakePeerUtils.SCROLL_H-2,
                           FakePeerUtils.SCROLL_W,FakePeerUtils.SCROLL_H);
            }
        }

        @Override
        public Dimension getMinimumSize() {
            TextArea target = (TextArea)_target;
            return FakeTextAreaPeer.this.getMinimumSize(target.getColumns(),target.getRows());
        }
    }
}
