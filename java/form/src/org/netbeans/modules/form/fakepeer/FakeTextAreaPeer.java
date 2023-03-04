/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
