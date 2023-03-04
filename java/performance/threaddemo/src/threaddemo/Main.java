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
