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

package threaddemo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;
import threaddemo.apps.index.IndexApp;
import threaddemo.apps.populate.Populate;
import threaddemo.apps.refactor.Refactor;
import threaddemo.model.Phadhail;
import threaddemo.model.PhadhailEvent;
import threaddemo.model.PhadhailListener;
import threaddemo.model.PhadhailNameEvent;
import threaddemo.model.Phadhails;
import threaddemo.views.PhadhailViews;

/**
 * Demonstrate various models and views for big data sets.
 * @author Jesse Glick
 */
public final class Main extends JFrame {
    
    private static JFrame mainFrame;
    
    public static void main(String[] args) {
        File root;
        if (args.length == 1) {
            root = new File(args[0]);
            if (!root.exists()) {
                root.mkdirs();
            }
        } else {
            root = File.listRoots()[0];
        }
        mainFrame = new Main(root);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setLocation(0, 0);
        // #35804: needs to happen in EQ, since it grabs tree lock etc.
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                mainFrame.pack();
                mainFrame.setVisible(true);
            }
        });
    }
    
    private final File root;
    private final JRadioButton synchButton, monitoredButton, lockedButton, eventHybridLockedButton, spunButton, swungButton, nodeButton, lookNodeButton, lookButton, rawButton;
    
    private Main(File root) {
        super("Thread Demo [" + root.getAbsolutePath() + "]");
        this.root = root;
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JPanel modelPanel1 = new JPanel();
        JPanel modelPanel2 = new JPanel();
        ButtonGroup modelGroup = new ButtonGroup();
        synchButton = new JRadioButton("Synchronous", true);
        synchButton.setMnemonic('y');
        monitoredButton = new JRadioButton("Monitored", false);
        monitoredButton.setMnemonic('m');
        lockedButton = new JRadioButton("Locked", false);
        lockedButton.setMnemonic('k');
        eventHybridLockedButton = new JRadioButton("Event-Hybrid-Locked", false);
        eventHybridLockedButton.setMnemonic('e');
        spunButton = new JRadioButton("Spun", false);
        spunButton.setMnemonic('u');
        swungButton = new JRadioButton("Swung", false);
        swungButton.setMnemonic('w');
        modelGroup.add(synchButton);
        modelGroup.add(monitoredButton);
        modelGroup.add(lockedButton);
        modelGroup.add(eventHybridLockedButton);
        modelGroup.add(spunButton);
        modelGroup.add(swungButton);
        modelPanel1.add(synchButton);
        modelPanel1.add(monitoredButton);
        modelPanel1.add(lockedButton);
        modelPanel2.add(eventHybridLockedButton);
        modelPanel2.add(spunButton);
        modelPanel2.add(swungButton);
        getContentPane().add(modelPanel1);
        getContentPane().add(modelPanel2);
        JPanel viewPanel = new JPanel();
        ButtonGroup viewGroup = new ButtonGroup();
        nodeButton = new JRadioButton("Node", false);
        nodeButton.setMnemonic('n');
        lookNodeButton = new JRadioButton("Look Node", true);
        lookNodeButton.setMnemonic('o');
        lookButton = new JRadioButton("Look", false);
        lookButton.setMnemonic('l');
        rawButton = new JRadioButton("Raw", false);
        rawButton.setMnemonic('r');
        viewGroup.add(nodeButton);
        viewGroup.add(lookNodeButton);
        viewGroup.add(lookButton);
        viewGroup.add(rawButton);
        viewPanel.add(rawButton);
        viewPanel.add(nodeButton);
        viewPanel.add(lookNodeButton);
        viewPanel.add(lookButton);
        getContentPane().add(viewPanel);
        JButton showB = new JButton("Show");
        showB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                showView();
            }
        });
        JButton popB = new JButton("Populate Test Files");
        popB.setMnemonic('p');
        popB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                Populate.run(Main.this.root, Main.this);
            }
        });
        JPanel bPanel = new JPanel();
        bPanel.add(showB);
        bPanel.add(popB);
        getContentPane().add(bPanel);
        getContentPane().add(new Monitor());
        getRootPane().setDefaultButton(showB);
    }
    
    private void showView() {
        // Clear caches first!
        System.gc();
        System.runFinalization();
        System.gc();
        final Phadhail model;
        final String modelType;
        if (synchButton.isSelected()) {
            model = Phadhails.synchronous(root);
            modelType = "Synchronous";
        } else if (monitoredButton.isSelected()) {
            model = Phadhails.monitored(root);
            modelType = "Monitored";
        } else if (lockedButton.isSelected()) {
            model = Phadhails.locked(root);
            modelType = "Locked";
        } else if (eventHybridLockedButton.isSelected()) {
            model = Phadhails.eventHybridLocked(root);
            modelType = "Event-Hybrid-Locked";
        } else if (spunButton.isSelected()) {
            model = Phadhails.spun(root);
            modelType = "Spun";
        } else {
            assert swungButton.isSelected();
            model = Phadhails.swung(root);
            modelType = "Swung";
        }
        Component view;
        final String viewType;
        if (nodeButton.isSelected()) {
            view = PhadhailViews.nodeView(model);
            viewType = "Node";
        } else if (lookNodeButton.isSelected()) {
            view = PhadhailViews.lookNodeView(model);
            viewType = "Look Node";
        } else if (lookButton.isSelected()) {
            view = PhadhailViews.lookView(model);
            viewType = "Look";
        } else {
            assert rawButton.isSelected();
            view = PhadhailViews.rawView(model);
            viewType = "Raw";
        }
        final JFrame frame = new JFrame();
        // For the benefit of Swung model which will produce the root path asynch:
        final PhadhailListener l = new PhadhailListener() {
            public void nameChanged(PhadhailNameEvent ev) {
                frame.setTitle(modelType + " " + viewType + ": " + model.getPath());
            }
            public void childrenChanged(PhadhailEvent ev) {}
        };
        l.nameChanged(null);
        model.addPhadhailListener(l);
        frame.getContentPane().add(view);
        JButton indexB = new JButton("View Index");
        indexB.setMnemonic('i');
        indexB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                viewIndex(model);
            }
        });
        JButton refactorB = new JButton("Refactor");
        refactorB.setMnemonic('r');
        refactorB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                Refactor.run(model, frame);
            }
        });
        JPanel bPanel = new JPanel();
        bPanel.add(indexB);
        bPanel.add(refactorB);
        frame.getContentPane().add(bPanel, BorderLayout.SOUTH);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                model.removePhadhailListener(l);
                frame.removeWindowListener(this);
                // Just to make sure the view is collected:
                frame.getContentPane().removeAll();
            }
        });
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setSize(500, 500);
        frame.setLocation(mainFrame.getX() + mainFrame.getWidth(), 0);
        frame.setVisible(true);
    }
    
    private void viewIndex(Phadhail model) {
        new IndexApp(model).setVisible(true);
    }
    
}
