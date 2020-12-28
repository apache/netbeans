/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */package org.netbeans.modules.python.debugger.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * 
 * A way to change the mouse cursor's shape over a given Swing
 * component
 * 
 * two ways are provided by this class :
 * - Standard way using the startWaiting/stopWaiting cursor
 * - Animated dynamic cursor through the startAnimation/ run /stopAnimation
 * methods
 * 
 * @author jean-yves Mengant
 *
 */
public class AnimatedCursor
        implements Runnable {

  private Component _candidate;
  private Cursor[] _cursors;
  private boolean _animated = false;

  public AnimatedCursor(Component c) {
    _candidate = c;
    _cursors = new Cursor[8];
    _cursors[0] = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
    _cursors[1] = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
    _cursors[2] = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
    _cursors[3] = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
    _cursors[4] = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
    _cursors[5] = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
    _cursors[6] = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
    _cursors[7] = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);

  }

  public void startWaitingCursor() {
    _animated = true;
    _candidate.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
  }

  public void stopWaitingCursor() {
    _candidate.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    _animated = false;
  }

  public synchronized void stopAnimation() {
    _animated = false;
  }

  public synchronized void startAnimation() {
    _animated = true;
  }

  public boolean isAnimated() {
    return _animated;
  }

  @Override
  public void run() {
    int count = 0;
    System.out.println("entering animation");
    while (_animated) {
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
      }
      _candidate.setCursor(_cursors[count % _cursors.length]);
      count++;
    }
    System.out.println("leaving animation");
    _candidate.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

  }

  public static void main(String[] args) {
    final JFrame f = new JFrame("changing cursor shape");
    final JButton button = new JButton("Start Animation");
    button.addActionListener(
            new ActionListener() {

              AnimatedCursor _cursor = new AnimatedCursor(f);

              @Override
              public void actionPerformed(ActionEvent e) {
                if (_cursor.isAnimated()) {
                  button.setText("start Animation");
                  // _cursor.stopWaitingCursor() ;
                  _cursor.stopAnimation();
                  _cursor = new AnimatedCursor(f);
                } else {
                  button.setText("stop Animation");
                  new Thread(_cursor).start();
                //_cursor.startWaitingCursor() ;
                }
              }
            });

    f.getContentPane().add(button);
    f.pack();
    f.setVisible(true);

  }
}
