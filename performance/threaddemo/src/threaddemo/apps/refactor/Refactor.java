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

package threaddemo.apps.refactor;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.openide.cookies.SaveCookie;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import threaddemo.data.DomProvider;
import threaddemo.data.PhadhailLookups;
import threaddemo.locking.LockAction;
import threaddemo.model.Phadhail;

/**
 * Simulates some big model-based refactoring of files.
 * In this case, increments the number in all tag-nnn elements in XML files.
 * @author Jesse Glick
 */
public class Refactor {
    
    private static final Logger logger = Logger.getLogger(Refactor.class.getName());
    
    /** No instances. */
    private Refactor() {}
    
    /**
     * Begin a refactoring session.
     * Call from event thread; will proceed in its own thread.
     * @param root the root of the tree of phadhails to work on
     * @param app owner app, or null
     */
    public static void run(final Phadhail root, Frame app) {
        final Map<Phadhail,DomProvider> data = collectData(root);
        final BoundedRangeModel progress = new DefaultBoundedRangeModel();
        progress.setMinimum(0);
        progress.setMaximum(data.size());
        progress.setValue(0);
        final JProgressBar progressBar = new JProgressBar(progress);
        progressBar.setStringPainted(true);
        Dimension d = progressBar.getPreferredSize();
        d.width = 500;
        progressBar.setPreferredSize(d);
        final JDialog dialog = new JDialog(app, "Refactoring...", false);
        JLabel label = new JLabel("Progress:");
        label.setLabelFor(progressBar);
        final boolean[] cancelled = new boolean[] {false};
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                cancelled[0] = true;
            }
        });
        dialog.getContentPane().setLayout(new FlowLayout());
        dialog.getContentPane().add(label);
        dialog.getContentPane().add(progressBar);
        dialog.getContentPane().add(cancel);
        dialog.pack();
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
        new Thread(new Runnable() {
            public void run() {
                Iterator<Map.Entry<Phadhail, DomProvider>> it = data.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Phadhail, DomProvider> e = it.next();
                    if (cancelled[0]) {
                        break;
                    }
                    // Avoid keeping a reference to the old data, since we have
                    // cached DomProvider's and such heavyweight stuff open on them:
                    it.remove();
                    final Phadhail ph = e.getKey();
                    logger.log(Level.FINER, "Refactoring {0}", ph);
                    final DomProvider p = e.getValue();
                    ph.lock().read(new Runnable() {
                        public void run() {
                            final String path = ph.getPath();
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    progress.setValue(progress.getValue() + 1);
                                    progressBar.setString(path);
                                }
                            });
                        }
                    });
                    ph.lock().write(new Runnable() {
                        public void run() {
                            SaveCookie s = (SaveCookie)PhadhailLookups.getLookup(ph).lookup(SaveCookie.class);
                            refactor(p);
                            if (s == null) {
                                // Was unmodified before, so save it now.
                                s = (SaveCookie)PhadhailLookups.getLookup(ph).lookup(SaveCookie.class);
                                if (s != null) {
                                    try {
                                        s.save();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    });
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        dialog.setVisible(false);
                    }
                });
            }
        }, "Refactoring").start();
    }
    
    private static Map<Phadhail, DomProvider> collectData(final Phadhail root) {
        return root.lock().read(new LockAction<Map<Phadhail,DomProvider>>() {
            private final Map<Phadhail, DomProvider> data = new HashMap<Phadhail,DomProvider>(); 
            public Map<Phadhail, DomProvider> run() {
                collect(root);
                return data;
            }
            private void collect(Phadhail ph) {
                if (ph.hasChildren()) {
                    for (Phadhail child : ph.getChildren()) {
                        collect(child);
                    }
                } else {
                    DomProvider p = (DomProvider)PhadhailLookups.getLookup(ph).lookup(DomProvider.class);
                    if (p != null) {
                        data.put(ph, p);
                    }
                }
            }
        });
    }
    
    private static void refactor(DomProvider p) {
        final Document doc;
        try {
            doc = p.getDocument();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        NodeList nl = doc.getElementsByTagName("*");
        final List<Element> l = new ArrayList<Element>();
        for (int i = 0; i < nl.getLength(); i++) {
            l.add((Element) nl.item(i));
        }
        p.isolatingChange(new Runnable() {
            public void run() {
                for (Element el : l) {
                    String tagname = el.getTagName();
                    if (tagname.startsWith("tag-")) {
                        int n = Integer.parseInt(tagname.substring(4));
                        tagname = "tag-" + (n + 1);
                        Element el2 = doc.createElement(tagname);
                        Node parent = el.getParentNode();
                        parent.insertBefore(el2, el);
                        NodeList nl2 = el.getChildNodes();
                        while (nl2.getLength() > 0) {
                            el2.appendChild(nl2.item(0));
                        }
                        parent.removeChild(el);
                    }
                }
            }
        });
        /*
        org.apache.xml.serialize.XMLSerializer ser = new org.apache.xml.serialize.XMLSerializer(System.err, new org.apache.xml.serialize.OutputFormat(doc, "UTF-8", true));
        try {
            ser.serialize(doc);
        } catch (IOException e) {
            e.printStackTrace();
        }
         */
    }
    
}
