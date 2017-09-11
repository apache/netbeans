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
