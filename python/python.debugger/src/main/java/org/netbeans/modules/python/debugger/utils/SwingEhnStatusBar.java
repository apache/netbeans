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
 */
package org.netbeans.modules.python.debugger.utils;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**

Define a Swing status bar behavior

@Author Jean-Yves Mengant


 */
public class SwingEhnStatusBar extends JPanel {

  private boolean _errorOn = false;
  private boolean _warningOn = false;
  JLabel _text = new JLabel();

  public SwingEhnStatusBar() {
    super();
    setLayout(new CardLayout());
    setBorder(BorderFactory.createRaisedBevelBorder());
    _text.setHorizontalAlignment(SwingConstants.LEFT);
    add("panel", _text);
  }

  /** Clear any on going error */
  public void reset() {
    _text.setBackground(Color.gray);
    _text.setForeground(Color.gray);
    _errorOn = false;
    _warningOn = false;
    _text.setText("");
  }

  /** reset informing user */
  public void reset(String msg) {
    reset();
    setMessage(msg);
  }

  /** Display sample message */
  public void setMessage(String msg) {
    if ((!_errorOn) && (!_warningOn)) {
      _text.setForeground(Swing.BLUE);
      _text.setBackground(Swing.WHITE);
      _text.setText("INFO : " + msg);
    }
  }

  /** use this for displaying errors */
  public void setError(String error) {
    _text.setForeground(Swing.RED);
    _text.setBackground(Swing.WHITE);
    _text.setText("ERROR :: " + error);
    _errorOn = true;
  }

  /** use this for displaying warnings */
  public void setWarning(String wrn) {
    if (!_errorOn) {
      _text.setBackground(Swing.WHITE);
      _text.setForeground(Swing.MAGENTA);
      _text.setText("WARNING :: " + wrn);
      _warningOn = true;
      System.out.println("size warning :" + getPreferredSize());
    }
  }

  public boolean is_errorOn() {
    return _errorOn;
  }

  public static void main(String argv[]) {
    // Exit the debug window frame
    class WL extends WindowAdapter {

      @Override
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    }

    class _SET_ implements ActionListener {

      SwingEhnStatusBar _ehn;
      boolean _on;

      public _SET_(SwingEhnStatusBar ehn) {
        _ehn = ehn;
      }

      @Override
      public void actionPerformed(ActionEvent e) {
        if (_on) {
          _on = false;
          _ehn.setMessage("hello");
        } else {
          _on = true;
          _ehn.reset();
        }
      }
    }

    JFrame f = new JFrame("Testing Swing Status bar");
    f.setForeground(Color.black);
    f.setBackground(Color.lightGray);
    f.getContentPane().setLayout(new BorderLayout());
    f.addWindowListener(new WL());

    SwingEhnStatusBar status = new SwingEhnStatusBar();


    // status.setText("Hello") ;
    JButton b = new JButton("Action");
    b.addActionListener(new _SET_(status));

    f.getContentPane().add("North", b);
    f.getContentPane().add("South", status);
    status.setWarning("Hello New wranError Displayed");

    f.pack();
    f.setVisible(true);
  }
}
