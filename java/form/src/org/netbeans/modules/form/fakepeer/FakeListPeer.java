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
class FakeListPeer extends FakeComponentPeer
{
    FakeListPeer(List target) {
        super(target);
    }

    @Override
    Component createDelegate() {
        return new Delegate();
    }

    public int[] getSelectedIndexes() {
        return new int[0];
    }

    public void add(String item, int index) {
    }

    public void delItems(int start, int end) {
    }

    public void removeAll() {
    }

    public void select(int index) {
    }

    public void deselect(int index) {
    }

    public void makeVisible(int index) {
    }

    public void setMultipleMode(boolean b) {
    }

    public Dimension getPreferredSize(int rows) {
        return new Dimension(40, 80);
    }

    public Dimension getMinimumSize(int rows) {
        return new Dimension(40, 80);
    }

    public void addItem(String item, int index) {
        add(item, index);
    }

    public void clear() {
        removeAll();
    }

    public void setMultipleSelections(boolean v) {
        setMultipleMode(v);
    }

    public Dimension preferredSize(int rows) {
        return getPreferredSize(rows);
    }

    public Dimension minimumSize(int rows) {
        return getMinimumSize(rows);
    }

    //
    //
    //

    private class Delegate extends Component 
    {
        Delegate() {
            this.setBackground(SystemColor.window);
            this.setForeground(SystemColor.windowText);
        }
        
        @Override
        public void paint(Graphics g) {
            List target =(List) _target;
            Dimension sz = target.getSize();
            int w = sz.width;
            int h = sz.height;

            g.setColor(target.getBackground());
            FakePeerUtils.drawLoweredBox(g,0,0,w,h);

            int n = target.getItemCount();
            if (n <= 0)
                return;

            if (target.isEnabled()) {
                g.setColor(target.getForeground());
            }
            else {
                g.setColor(SystemColor.controlShadow);
            }
            
            g.setFont(target.getFont());
            g.setClip(1,1,w-5,h-4);

            FontMetrics fm = g.getFontMetrics();
            int th = fm.getHeight(),
                ty = th+2;
            
            for (int i=0; i < n; i++) {
                g.drawString(target.getItem(i), 4, ty);
                if (ty > h) break;
                ty += th;
            }
        }

        @Override
        public Dimension getMinimumSize() {
            return FakeListPeer.this.getMinimumSize(1);
        }
    }
}
