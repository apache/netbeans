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
class FakeTextFieldPeer extends FakeTextComponentPeer
{
    FakeTextFieldPeer(TextField target) {
        super(target);
    }

    @Override
    Component createDelegate() {
        return new Delegate();
    }

    public void setEchoChar(char echoChar) {
    }

    public Dimension getPreferredSize(int columns) {
        return _delegate.getMinimumSize(); //new Dimension(100, 20);
    }

    public Dimension getMinimumSize(int columns) {
        return _delegate.getMinimumSize(); //new Dimension(100, 20);
    }

    public void setEchoCharacter(char c) {
        setEchoChar(c);
    }

    public Dimension preferredSize(int cols) {
        return getPreferredSize(cols);
    }

    public Dimension minimumSize(int cols) {
        return getMinimumSize(cols);
    }

    //
    //
    //

    private class Delegate extends FakeTextComponentPeer.Delegate
    {
        @Override
        public void paint(Graphics g) {
            super.paint(g);

            TextField target =(TextField) _target;
            String text = target.getText();

            if (text != null) { // draw the text
                String textOut = text.substring(target.getCaretPosition());
//                Dimension sz = target.getSize();
                g.setFont(target.getFont());

                FontMetrics fm = g.getFontMetrics();
                int h = fm.getHeight() - fm.getDescent();
                g.drawString(textOut, 4, 1 + h); //(sz.height - h) / 2 + h -2);
            }
        }

        @Override
        public Dimension getMinimumSize() {
            String text = ((TextField)_target).getText();

            FontMetrics fm = this.getFontMetrics(this.getFont());
            int w = fm.stringWidth(text);
            int h = fm.getHeight();

            return new Dimension(w > 92 ? 100 : w+8, h + 4);
        }
    }
}
