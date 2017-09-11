/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.api.java.source.ui;

import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.TestCase;

/**
 *
 * @author Radim Kubacki
 */
public class HTMLJavadocParserTest extends TestCase {
    
  public HTMLJavadocParserTest(String testName) {
      super(testName);
  }

  /**
   * Test of getJavadocText method ised with HTML produced by standard doclet.
   */
  public void testGetJavadocText() throws MalformedURLException {
    URL url = HTMLJavadocParserTest.class.getResource("HTMLJavadocParser.html");
    String result = HTMLJavadocParser.getJavadocText(url, false);
    assertNotNull(result);

    result = HTMLJavadocParser.getJavadocText(
        new URL(url, "HTMLJavadocParser.html#getJavadocText(java.net.URL, boolean)"), false);
    assertNotNull(result);
  }

  /**
   * Test of getJavadocText method used with javadoc from Android SDK.
   */
  public void testGetAndroidJavadocText() throws MalformedURLException {
    URL url = HTMLJavadocParserTest.class.getResource("Activity.html");
    String result = HTMLJavadocParser.getJavadocText(url, false);
    assertNotNull(result);

    result = HTMLJavadocParser.getJavadocText(
        new URL(url, "Activity.html#dispatchKeyEvent(android.view.KeyEvent)"), false);
    // check that there is begining of javadoc
    assertTrue(result.contains("Called to process key events."));
    // ... and return value documentation too
    assertTrue(result.contains("if this event was consumed."));

    result = HTMLJavadocParser.getJavadocText(
        new URL(url, "Activity.html#onActivityResult%28int%2C%20int%2C%20android.content.Intent%29"), false);
    // check that there is begining of javadoc
    assertTrue(result.contains("Called when an activity"));
    // ... and return value documentation too
    assertTrue(result.contains("See Also"));

  }

  public void test199194() throws MalformedURLException {
    URL url = HTMLJavadocParserTest.class.getResource("JavaApplication1.html");
    String result = HTMLJavadocParser.getJavadocText(url, false);
    assertNotNull(result);

    result = HTMLJavadocParser.getJavadocText(
        new URL(url, "JavaApplication1.html#test(java.lang.Object)"), false);
    assertNotNull(result);
    assertTrue(result.contains("C"));
  }
  
  public void test209707() throws MalformedURLException {
        URL url = HTMLJavadocParserTest.class.getResource("FileChooser.html");
        String result = HTMLJavadocParser.getJavadocText(url, false);
        assertNotNull(result);
        assertTrue(result.contains(" may be restricted "));

        result = HTMLJavadocParser.getJavadocText(
                new URL(url, "FileChooser.html#showSaveDialog(javafx.stage.Window)"), false);
        assertNotNull(result);
        assertTrue(result.contains("the selected file or"));
  }
}
