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

public class FakePeerContainer extends Container
{
    public FakePeerContainer() {
        super();
        setFont(FakePeerSupport.getDefaultAWTFont());
    }

    @Override
    public void addNotify() {
        FakePeerSupport.attachFakePeerRecursively(this);
        super.addNotify();
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        FakePeerSupport.attachFakePeer(comp);
        super.addImpl(comp, constraints, index);
    }
    
    @Override
    public void update(Graphics g) {
    }

    @Override
    public void paint(Graphics g) {
        Dimension sz = getSize();
//        Shape oldClip = g.getClip();
//        g.setClip(0, 0, sz.width, sz.height);

        Color c = SystemColor.control;
        g.setColor(c);
        g.fillRect(0, 0, sz.width, sz.height);
//        g.setClip(oldClip);

        super.paint(g);
        paintFakePeersRecursively(g, this);
    }

    private static void paintFakePeersRecursively(Graphics g, Container container) {
        if (!container.isVisible())
            return;

        Component components[] = FakePeerSupport.getComponents(container);
        int ncomponents = components.length;

        Rectangle clip = g.getClipBounds();
        for (int i = 0; i < ncomponents; i++) {
            Component comp = components[i];
            if (comp != null && FakePeerSupport.getPeer(comp) instanceof FakePeer
                    && comp.isVisible()) {
                Rectangle cr = comp.getBounds();
                if ((clip == null) || cr.intersects(clip)) {
                    Graphics cg = g.create(cr.x, cr.y, cr.width, cr.height);
                    cg.setFont(comp.getFont());
                    try {
                        FakePeerSupport.getPeer(comp).paint(cg);
                    }
                    finally {
                        cg.dispose();
                    }
                }
            }
            if (comp instanceof Container) {
                Rectangle cr = comp.getBounds();
                if ((clip == null) || cr.intersects(clip)) {
                    Graphics cg = g.create(cr.x, cr.y, cr.width, cr.height);
                    paintFakePeersRecursively(cg,(Container) comp);
                    cg.dispose();
                }
            }
        }
    }
}
