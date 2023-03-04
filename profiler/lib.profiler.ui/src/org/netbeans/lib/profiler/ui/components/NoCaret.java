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
package org.netbeans.lib.profiler.ui.components;

import java.awt.Graphics;
import java.awt.Point;
import javax.swing.event.ChangeListener;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Jiri Sedlacek
 */
public final class NoCaret implements Caret {
    
    public void install(JTextComponent c) {}
    public void deinstall(JTextComponent c) {}
    public void paint(Graphics g) {}
    public void addChangeListener(ChangeListener l) {}
    public void removeChangeListener(ChangeListener l) {}
    public boolean isVisible() { return false; }
    public void setVisible(boolean v) {}
    public boolean isSelectionVisible() { return false; }
    public void setSelectionVisible(boolean v) {}
    public void setMagicCaretPosition(Point p) {}
    public Point getMagicCaretPosition() { return new Point(0, 0); }
    public void setBlinkRate(int rate) {}
    public int getBlinkRate() { return 1; }
    public int getDot() { return 0; }
    public int getMark() { return 0; }
    public void setDot(int dot) {}
    public void moveDot(int dot) {}
    
}
