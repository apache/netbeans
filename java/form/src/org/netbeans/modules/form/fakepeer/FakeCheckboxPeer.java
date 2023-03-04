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
class FakeCheckboxPeer extends FakeComponentPeer
{
    FakeCheckboxPeer(Checkbox target) {
        super(target);
    }

    @Override
    Component createDelegate() {
        return new Delegate();
    }

    public void setState(boolean state) {
    }

    public void setCheckboxGroup(CheckboxGroup g) {
    }

    public void setLabel(String label) {
    }

    //
    //
    //

    private class Delegate extends Component
    {
        Delegate() {
//            this.setBackground(SystemColor.control);
            this.setForeground(SystemColor.controlText);
        }
        
        @Override
        public void paint(Graphics g) {
            Checkbox target = (Checkbox) _target;
            Dimension sz = target.getSize();
            int bx = 0;
            int by = (sz.height - BOX_H) / 2;

            g.setColor(target.getBackground());
            g.fillRect(0, 0, sz.width, sz.height);

            String label = target.getLabel();
            
            if (label != null) {
                g.setFont(target.getFont());

                FontMetrics fm = g.getFontMetrics();
                int h = fm.getHeight() - fm.getDescent(),
                    x = 18,
                    y = (sz.height - h) / 2 + h - 2;
            
                if (target.isEnabled()) {
                    g.setColor(target.getForeground());
                }
                else {
                    g.setColor(SystemColor.controlLtHighlight);
                    g.drawString(label, x+1, y+1);
                    g.setColor(SystemColor.controlShadow);
                }

                g.drawString(label, x, y);
                by = y - h + 2;
            }

            // the check-box (Windows like - lowered, white background)
            
            if (target.getCheckboxGroup() == null) {
                g.setColor(SystemColor.window);
                FakePeerUtils.drawLoweredBox(g,bx,by,BOX_W,BOX_H);

                if (target.getState()) { // checkbox is checked
                    g.setColor(SystemColor.controlText);
                    for (int i=1; i < drCheckPosX_W.length; i++)
                        g.drawLine(drCheckPosX_W[i-1]+bx,drCheckPosY_W[i-1]+by,
                                   drCheckPosX_W[i]+bx,drCheckPosY_W[i]+by);
                }
            }
            else { // radio button
                if (radButtIcon1 == null || radButtIcon2 == null)
                    initRBImages();
                g.drawImage(target.getState() ? radButtIcon2:radButtIcon1, bx+1, by+1, this);
            }
        }

        @Override
        public Dimension getMinimumSize() {
            String label = ((Checkbox)_target).getLabel();

            FontMetrics fm = this.getFontMetrics(this.getFont());
            int w = fm.stringWidth(label);
            int h = fm.getHeight();

            return new Dimension(w + 6+BOX_W+4, h + 4);
        }

        void initRBImages() {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            java.net.URL source = this.getClass().getResource("radbutt1.gif");
            radButtIcon1 = toolkit.getImage(source);
            source = this.getClass().getResource("radbutt2.gif");
            radButtIcon2 = toolkit.getImage(source);

            MediaTracker mt = new MediaTracker(this);
            mt.addImage(radButtIcon1,0);
            mt.addImage(radButtIcon2,1);
            try {
                mt.waitForAll();
            } catch (java.lang.InterruptedException e) {
            }
        }
    }

    private static final int BOX_W = 16, BOX_H = 16;
    private static final int[] drCheckPosX_W = { 4,6,10,10,6,4,4,6,10 };
    private static final int[] drCheckPosY_W = { 6,8,4,5,9,7,8,10,6 };

    private static Image radButtIcon1 = null;
    private static Image radButtIcon2 = null;
}
