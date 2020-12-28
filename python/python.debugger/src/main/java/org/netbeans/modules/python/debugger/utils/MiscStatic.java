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

import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.Window;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

/**
 * @author jean-yves Mengant
 * Misc static utility methods 
 */
public class MiscStatic {

  private static final String _OS_NAME_ = "os.name";
  private static final String _OS_ = System.getProperty(_OS_NAME_).toLowerCase();
  private static final String _WINDOWS_ = "windows";

  public static boolean isFileSystemCaseSensitive() {
    if (_OS_.startsWith(_WINDOWS_)) {
      return false;
    }
    return true;
  }

  public static boolean sameFile(String f1, String f2) {
    if (!isFileSystemCaseSensitive()) {
      return f1.equalsIgnoreCase(f2);
    }
    return f1.equals(f2);
  }

  public static String nonSensitiveFileName(String f1) {
    if (!isFileSystemCaseSensitive()) {
      return f1.toLowerCase();
    }
    return f1;
  }
  //{{{ escapesToChars() method

  /**
   * Converts "\n" and "\t" escapes in the specified string to
   * newlines and tabs.
   * @param str The string
   * @since jEdit 2.3pre1
   */
  public static String escapesToChars(String str) {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      switch (c) {
        case '\\':
          if (i == str.length() - 1) {
            buf.append('\\');
            break;
          }
          c = str.charAt(++i);
          switch (c) {
            case 'n':
              buf.append('\n');
              break;
            case 't':
              buf.append('\t');
              break;
            default:
              buf.append(c);
              break;
          }
          break;
        default:
          buf.append(c);
      }
    }
    return buf.toString();
  } //}}}

  //{{{ charsToEscapes() method
  /**
   * Escapes newlines, tabs, backslashes, and quotes in the specified
   * string.
   * @param str The string
   * @since jEdit 2.3pre1
   */
  public static String charsToEscapes(String str) {
    return charsToEscapes(str, "\n\t\\\"'");
  } //}}}

  //{{{ charsToEscapes() method
  /**
   * Escapes the specified characters in the specified string.
   * @param str The string
   * @param toEscape Any characters that require escaping
   * @since jEdit 4.1pre3
   */
  public static String charsToEscapes(String str, String toEscape) {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (toEscape.indexOf(c) != -1) {
        if (c == '\n') {
          buf.append("\\n");
        } else if (c == '\t') {
          buf.append("\\t");
        } else {
          buf.append('\\');
          buf.append(c);
        }
      } else {
        buf.append(c);
      }
    }
    return buf.toString();
  } //}}}

  //{{{ showPopupMenu() method
  /**
   * Shows the specified popup menu, ensuring it is displayed within
   * the bounds of the screen.
   * @param popup The popup menu
   * @param comp The component to show it for
   * @param x The x co-ordinate
   * @param y The y co-ordinate
   * @param point If true, then the popup originates from a single point;
   * otherwise it will originate from the component itself. This affects
   * positioning in the case where the popup does not fit onscreen.
   *
   * @since jEdit 4.1pre1
   */
  public static void showPopupMenu(JPopupMenu popup, Component comp,
          int x, int y, boolean point) {
    int offsetX = 0;
    int offsetY = 0;

    int extraOffset = (point ? 1 : 0);

    Component win = comp;
    while (!(win instanceof Window || win == null)) {
      offsetX += win.getX();
      offsetY += win.getY();
      win = win.getParent();
    }

    if (win != null) {
      Dimension size = popup.getPreferredSize();

      Rectangle screenSize = win.getGraphicsConfiguration().getBounds();

      if (x + offsetX + size.width + win.getX() > screenSize.width && x + offsetX + win.getX() >= size.width) {
        //System.err.println("x overflow");
        if (point) {
          x -= (size.width + extraOffset);
        } else {
          x = (win.getWidth() - size.width - offsetX + extraOffset);
        }
      } else {
        x += extraOffset;
      }

      //System.err.println("y=" + y + ",offsetY=" + offsetY
      //	+ ",size.height=" + size.height
      //	+ ",win.height=" + win.getHeight());
      if (y + offsetY + size.height + win.getY() > screenSize.height && y + offsetY + win.getY() >= size.height) {
        if (point) {
          y = (win.getHeight() - size.height - offsetY + extraOffset);
        } else {
          y = -size.height - 1;
        }
      } else {
        y += extraOffset;
      }

      popup.show(comp, x, y);
    } else {
      popup.show(comp, x + extraOffset, y + extraOffset);
    }

  } //}}}

  //{{{ isPopupTrigger() method
  /**
   * Returns if the specified event is the popup trigger event.
   * This implements precisely defined behavior, as opposed to
   * MouseEvent.isPopupTrigger().
   * @param evt The event
   * @since jEdit 3.2pre8
   */
  public static boolean isPopupTrigger(MouseEvent evt) {
    return isRightButton(evt.getModifiers());
  } //}}}

  public static boolean isRightButton(int modifiers) {
    return ((modifiers & MouseEvent.BUTTON3_MASK) != 0);
  } //}}}
}
