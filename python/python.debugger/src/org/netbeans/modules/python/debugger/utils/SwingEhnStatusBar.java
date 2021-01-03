/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.python.debugger.utils;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**

Define a Swing status bar behavior



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
