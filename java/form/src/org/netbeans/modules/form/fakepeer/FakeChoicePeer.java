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
class FakeChoicePeer extends FakeComponentPeer
{
    FakeChoicePeer(Choice target) {
        super(target);
    }

    @Override
    Component createDelegate() {
        return new Delegate();
    }

    public void add(String item, int index) {
    }

    public void remove(int index) {
    }

    // JDK 1.3
    public void removeAll() {
    }

    public void select(int index) {
    }

    public void addItem(String item, int index) {
        add(item, index);
    }

    //
    //
    //

    private class Delegate extends Component
    {
        Delegate() {
            this.setBackground(SystemColor.window);
            this.setForeground(SystemColor.controlText);
        }
        
        @Override
        public void paint(Graphics g) {
            Choice target =(Choice) _target;
            Dimension sz = target.getSize();
            
            FontMetrics fm = g.getFontMetrics();
            int w = sz.width,
                h = sz.height,
                fh = fm.getHeight(), // font's height
                comph = h > fh+4 ? fh+4 : h, // component's height
                y = (h-comph)/2; // component's vertical position

            g.setColor(target.getBackground());
            FakePeerUtils.drawLoweredBox(g, 0,y,w,comph);

            String item = target.getSelectedItem();
            if (item != null) {
                if (target.isEnabled()) {
                    g.setColor(target.getForeground());
                }
                else {
                    g.setColor(SystemColor.controlShadow);
                }
                g.setFont(target.getFont());

                g.setClip(2,y+2,w-4,comph-4);
                int ih = fh - fm.getDescent(), // item's height
                    iy = y + 1 + ih;

                g.drawString(item, 4, iy);
            }

            // combo-box button (Windows style)
            FakePeerUtils.drawArrowButton(
                g, w-BUT_W-2, y+2, BUT_W, comph-4, 4, target.isEnabled());
        }

        @Override
        public Dimension getMinimumSize() {
            String label = ((Choice)_target).getSelectedItem();

            FontMetrics fm = this.getFontMetrics(this.getFont());
            int w = label != null ? fm.stringWidth(label)+5 : 8,
                h = fm.getHeight();

            return new Dimension(w + 4 + BUT_W, h + 4);
        }
    }

    private static final int BUT_W = 16, BUT_H = 16;
}
