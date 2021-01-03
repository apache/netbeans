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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ColorComboBox extends JComboBox {

  public static final String PROP_COLOR = "color";
  public static final Value CUSTOM_COLOR =
          new Value("Custom", null);
  private static Map colorMap = new HashMap();


  static {
    colorMap.put(Color.BLACK, "Black");
    colorMap.put(Color.BLUE, "Blue");
    colorMap.put(Color.CYAN, "Cyan");
    colorMap.put(Color.DARK_GRAY, "Dark_Gray");
    colorMap.put(Color.GRAY, "Gray");
    colorMap.put(Color.GREEN, "Green");
    colorMap.put(Color.LIGHT_GRAY, "Light_Gray");
    colorMap.put(Color.MAGENTA, "Magenta");
    colorMap.put(Color.ORANGE, "Orange");
    colorMap.put(Color.PINK, "Pink");
    colorMap.put(Color.RED, "Red");
    colorMap.put(Color.WHITE, "White");
    colorMap.put(Color.YELLOW, "Yellow");
  }
  private static Map mapColor = new HashMap();


  static {
    colorMap.put("Black", Color.BLACK);
    colorMap.put("Blue", Color.BLUE);
    colorMap.put("Cyan", Color.CYAN);
    colorMap.put("Dark_Gray", Color.DARK_GRAY);
    colorMap.put("Gray", Color.GRAY);
    colorMap.put("Green", Color.GREEN);
    colorMap.put("Light_Gray", Color.LIGHT_GRAY);
    colorMap.put("Magenta", Color.MAGENTA);
    colorMap.put("Orange", Color.ORANGE);
    colorMap.put("Pink", Color.PINK);
    colorMap.put("Red", Color.RED);
    colorMap.put("White", Color.WHITE);
    colorMap.put("Yellow", Color.YELLOW);
  }
  private static Object[] content = new Object[]{
    new Value(Color.BLACK),
    new Value(Color.BLUE),
    new Value(Color.CYAN),
    new Value(Color.DARK_GRAY),
    new Value(Color.GRAY),
    new Value(Color.GREEN),
    new Value(Color.LIGHT_GRAY),
    new Value(Color.MAGENTA),
    new Value(Color.ORANGE),
    new Value(Color.PINK),
    new Value(Color.RED),
    new Value(Color.WHITE),
    new Value(Color.YELLOW),
    CUSTOM_COLOR
  };

  /** Creates a new instance of ColorChooser */
  public ColorComboBox() {
    super(content);
    setRenderer(new Renderer());
    setEditable(true);
    setEditor(new Renderer());
    setSelectedItem(new Value(null, null));
    addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ev) {
        if (getSelectedItem() == CUSTOM_COLOR) {
          Color c = JColorChooser.showDialog(
                  SwingUtilities.getAncestorOfClass(Dialog.class, ColorComboBox.this),
                  "Selection Color",
                  null);
          setColor(c);
        }
        ColorComboBox.this.firePropertyChange(PROP_COLOR, null, null);
      }
    });
  }

  public void setDefaultColor(Color color) {
    Object[] ncontent = new Object[content.length];
    System.arraycopy(content, 0, ncontent, 0, content.length);
    if (color != null) {
      ncontent[content.length - 1] = new Value(
              "Default Color", color //NOI18N
              );
    } else {
      ncontent[content.length - 1] = new Value(
              "None Color", null //NOI18N
              );
    }
    setModel(new DefaultComboBoxModel(ncontent));
  }

  public void setColor(Color color) {
    if (color == null) {
      setSelectedIndex(content.length - 1);
    } else {
      setSelectedItem(new Value(color));
    }
  }

  public void setColor(String strColor) {
    if (colorMap.containsKey(strColor)) {
      setColor((Color) (colorMap.get(strColor)));
    }
    // expecting [R,G,B] format here
    int comaPos = strColor.indexOf(',');
    if (comaPos == -1) {
      return;
    }
    int r = Integer.parseInt(strColor.substring(1, comaPos));
    strColor = strColor.substring(comaPos + 1);
    comaPos = strColor.indexOf(',');
    int g = Integer.parseInt(strColor.substring(0, comaPos));
    strColor = strColor.substring(comaPos + 1);
    int b = Integer.parseInt(strColor.substring(0, strColor.length() - 1));
    setColor(new Color(r, g, b));
  }

  public Color getColor() {
    if (getSelectedIndex() == (content.length - 1)) {
      return null;
    }
    return ((Value) getSelectedItem()).color;
  }

  public String getStringColor() {
    if (getSelectedIndex() == (content.length - 1)) {
      return null;
    }
    Color sel = ((Value) getSelectedItem()).color;
    if (colorMap.get(sel) != null) {
      return (String) colorMap.get(sel);
    }
    return sel.toString();
  }

  // innerclasses ............................................................
  public static class Value {

    String text;
    Color color;

    Value(Color color) {
      this.color = color;
      text = (String) colorMap.get(color);
      if (text != null) {
        return;
      }
      StringBuffer sb = new StringBuffer();
      sb.append('[').append(color.getRed()).
              append(',').append(color.getGreen()).
              append(',').append(color.getBlue()).
              append(']');
      text = sb.toString();
    }

    Value(String text, Color color) {
      this.text = text;
      this.color = color;
    }
  }

  private class Renderer extends JComponent implements
          ListCellRenderer, ComboBoxEditor {

    private int SIZE = 9;
    private Value value;

    Renderer() {
      setPreferredSize(new Dimension(
              50, getFontMetrics(ColorComboBox.this.getFont()).getHeight() + 2));
      setOpaque(true);
    }

    @Override
    public void paint(Graphics g) {
      Color oldColor = g.getColor();
      Dimension size = getSize();
      g.setColor(getBackground());
      g.fillRect(0, 0, size.width, size.height);
      int i = (size.height - SIZE) / 2;
      if (value.color != null) {
        g.setColor(Color.black);
        g.drawRect(i, i, SIZE, SIZE);
        g.setColor(value.color);
        g.fillRect(i + 1, i + 1, SIZE - 1, SIZE - 1);
      }
      if (value.text != null) {
        g.setColor(Color.black);
        if (value.color != null) {
          g.drawString(value.text, i + SIZE + 5, i + SIZE);
        } else {
          g.drawString(value.text, 5, i + SIZE);
        }
      }
      g.setColor(oldColor);
    }

    @Override
    public void setEnabled(boolean enabled) {
      setBackground(enabled ? SystemColor.text : SystemColor.control);
      super.setEnabled(enabled);
    }

    @Override
    public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
      this.value = (Value) value;
      setEnabled(list.isEnabled());
      return this;
    }

    @Override
    public Component getEditorComponent() {
      setEnabled(ColorComboBox.this.isEnabled());
      return this;
    }

    @Override
    public void setItem(Object anObject) {
      this.value = (Value) anObject;
    }

    @Override
    public Object getItem() {
      return value;
    }

    @Override
    public void selectAll() {
    }

    @Override
    public void addActionListener(ActionListener l) {
    }

    @Override
    public void removeActionListener(ActionListener l) {
    }
  }

  public static void main(String args[]) {
    JFrame f = new JFrame("test Python Debug Frame");
    ColorComboBox box = new ColorComboBox();
    box.setColor(Color.BLUE);
    f.getContentPane().setLayout(new BorderLayout());
    f.getContentPane().add(BorderLayout.NORTH, box);

    f.addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    f.pack();
    f.setVisible(true);
  }
}
