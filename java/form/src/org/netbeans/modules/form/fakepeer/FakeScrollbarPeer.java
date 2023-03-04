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
class FakeScrollbarPeer extends FakeComponentPeer
{
    FakeScrollbarPeer(Scrollbar target) {
        super(target);
    }

    @Override
    Component createDelegate() {
        return new Delegate();
    }

    public void setValues(int value, int visible, int minimum, int maximum) {}
    public void setLineIncrement(int l) {}
    public void setPageIncrement(int l) {}

    //
    //
    //

    private class Delegate extends Component
    {
        Delegate() {
            this.setBackground(SystemColor.scrollbar);
        }
        
        @Override
        public void paint(Graphics g) {
            Scrollbar target = (Scrollbar) _target;
            Dimension sz = target.getSize();
            int scrollRange = target.getMaximum() - target.getMinimum();
            int scrollValue = target.getValue() - target.getMinimum();
            int thumbAmount = target.getVisibleAmount();

            g.setColor(target.getBackground());

            FakePeerUtils.drawScrollbar(g,
                                        0, 0, sz.width, sz.height,
                                        target.getOrientation(),
                                        target.isEnabled(),
                                        true,
                                        scrollValue,
                                        thumbAmount,
                                        scrollRange);
        }

        @Override
        public Dimension getMinimumSize() {
            Scrollbar target =(Scrollbar) _target;
            return target.getOrientation() == Scrollbar.HORIZONTAL ?
                new Dimension(3 * FakePeerUtils.SCROLL_W, FakePeerUtils.SCROLL_H) :
                new Dimension(FakePeerUtils.SCROLL_W, 3 * FakePeerUtils.SCROLL_H);
        }
    }
}
