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
class FakeScrollPanePeer extends FakeContainerPeer
{
    FakeScrollPanePeer(ScrollPane target) {
        super(target);
    }

    @Override
    Component createDelegate() {
        return new Delegate();
    }

    public int getHScrollbarHeight() {
        return 16;
    }

    public int getVScrollbarWidth() {
        return 16;
    }

    public void setScrollPosition(int x, int y) {}
    public void childResized(int w, int h) {}
    public void setUnitIncrement(Adjustable adj, int u) {}
    public void setValue(Adjustable adj, int v) {}

    //
    //
    //

    private class Delegate extends Component
    {
//        Delegate() {
//            this.setBackground(SystemColor.scrollbar);
//        }
        
        @Override
        public void paint(Graphics g) {
            ScrollPane target = (ScrollPane) _target;
            Dimension sz = target.getSize();

            g.setColor(target.getBackground());
            FakePeerUtils.drawLoweredBox(g,0,0,sz.width,sz.height);
        }

        @Override
        public Dimension getMinimumSize() {
            ScrollPane scrollPane = (ScrollPane) _target;
            int n = scrollPane.getComponentCount();
            return n > 0 ?
                     scrollPane.getComponent(n-1).getMinimumSize():
                     new Dimension(100, 100);
        }
    }
}
