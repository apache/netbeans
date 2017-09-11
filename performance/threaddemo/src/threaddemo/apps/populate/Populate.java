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

package threaddemo.apps.populate;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.BoundedRangeModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// XXX offer a Cancel button

/**
 * Simple app to populate a file tree with some test data.
 * @author Jesse Glick
 */
public class Populate {
    
    /** Static usage only. */
    private Populate() {}
    
    /**
     * Run the app.
     * @param root root of the phadhail tree to add test data to
     * @param app application frame (may be null)
     */
    public static void run(final File root, Frame app) {
        String msg = "How many files should I create?";
        String title = "Choose Size of Test Data";
        Integer[] sizes = new Integer[] {
            new Integer(5),
            new Integer(10),
            new Integer(25),
            new Integer(50),
            new Integer(100),
            new Integer(250),
            new Integer(500),
            new Integer(1000),
            new Integer(2500),
            new Integer(5000),
            new Integer(10000),
            new Integer(25000),
        };
        Integer def = new Integer(500);
        Integer i = (Integer)JOptionPane.showInputDialog(null, msg, title,
                                                         JOptionPane.QUESTION_MESSAGE,
                                                         null, sizes, def);
        if (i == null) {
            // Cancelled.
            return;
        }
        final int val = i.intValue();
        final JProgressBar progress = new JProgressBar(0, val);
        final JDialog dialog = new JDialog(app, "Creating test files...", true);
        dialog.getContentPane().setLayout(new FlowLayout());
        JLabel label = new JLabel("Creating files:");
        label.setLabelFor(progress);
        dialog.getContentPane().add(label);
        dialog.getContentPane().add(progress);
        dialog.pack();
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        new Thread(new Runnable() {
            public void run() {
                try {
                    create(root, val, progress.getModel());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        dialog.setVisible(false);
                    }
                });
            }
        }, "Populating test files").start();
        dialog.setVisible(true);
    }
    
    private static void create(File root, int val, final BoundedRangeModel progress) throws IOException {
        for (int i = 0; i < val; i++) {
            final int progval = i;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progress.setValue(progval);
                }
            });
            boolean xml = i % 5 > 2;
            String fname = "file" + i + (xml ? ".xml" : ".txt");
            int bit = 0;
            int x = i;
            while (x > 0) {
                if (x % 3 == 0) {
                    fname = "dir" + bit + File.separatorChar + fname;
                }
                bit++;
                x /= 3;
            }
            File tomake = new File(root, "test" + File.separatorChar + fname);
            tomake.getParentFile().mkdirs();
            if (tomake.createNewFile()) {
                OutputStream os = new FileOutputStream(tomake);
                try {
                    if (xml) {
                        Document doc = createXML(i);
                        XMLUtil.write(doc, os, "UTF-8");
                    } else {
                        PrintStream ps = new PrintStream(os);
                        ps.println("Sample data for file #" + i);
                        ps.close();
                    }
                } finally {
                    os.close();
                }
            }
        }
    }
    
    private static Document createXML(int n) {
        Document doc = XMLUtil.createDocument("sample", null, null, null);
        Element el = doc.createElement("file");
        el.setAttribute("number", Integer.toString(n));
        doc.getDocumentElement().appendChild(el);
        int count = (int)Math.pow(n + 20, .85);
        for (int i = 0; i < count; i++) {
            el = doc.getDocumentElement();
            int x = i;
            int bit = 0;
            while (x > 0) {
                if (x % 3 == 0) {
                    String tagname = "tag-" + bit;
                    Element el2;
                    NodeList nl = el.getElementsByTagName(tagname);
                    if (nl.getLength() < 3) {
                        el2 = doc.createElement("tag-" + bit);
                        el.appendChild(el2);
                    } else {
                        el2 = (Element)nl.item(0);
                    }
                    el = el2;
                }
                bit++;
                x /= 3;
            }
            Element el2 = doc.createElement("datum");
            el2.setAttribute("number", Integer.toString(i));
            el.appendChild(el2);
        }
        return doc;
    }
    
}
