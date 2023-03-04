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
package org.netbeans.core.windows.view.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * A standalone Swing app that can be used to manually test {@link AutoHidingMenuBar} without
 * launching the full IDE. Tested on Windows 10. Not applicable to MacOS.
 */
public final class AutoHidingMenuBarManualTestApp {
    private final JFrame frame = new JFrame();
    private final JMenuBar mainMenuBar = new JMenuBar();

    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | IllegalAccessException |
                        InstantiationException | UnsupportedLookAndFeelException e)
                {
                    e.printStackTrace();
                }
                new AutoHidingMenuBarManualTestApp().setVisible(true);
            }
        });
    }

    public AutoHidingMenuBarManualTestApp() {
        initComponents();
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    private void initComponents() {
        addFakeMenuItems();
        frame.setJMenuBar(mainMenuBar);
        frame.setSize(400, 280);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        JEditorPane pane = new JEditorPane();
        // Make sure that consumed events do not cause problems.
        pane.addMouseMotionListener(new MouseMotionAdapter() { });
        frame.add(pane, BorderLayout.CENTER);
        frame.add(new JLabel("This is a label"), BorderLayout.SOUTH);
        /* Simulate a full-screen window so that the very top (y=0) of the JFrame can receive mouse
        motion events. The Window will end up aligned to the top of the screen. */
        frame.setUndecorated(true);
        new AutoHidingMenuBar(frame).setAutoHideEnabled(true);
    }

    private void addFakeMenuItems() {
        JMenu menuFile = new JMenu("File");
        JMenu menuEdit = new JMenu("Edit");
        JMenu menuHelp = new JMenu("Help");
        menuFile.setMnemonic('F');
        JMenuItem newItem = new JMenuItem("New");
        newItem.setMnemonic('N');
        newItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Invoked New");
            }
        });
        menuFile.add(newItem);
        menuFile.add(new JMenuItem("Open"));
        menuFile.add(new JMenuItem("Save"));
        menuFile.add(new JMenuItem("Save As"));
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic('E');
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        menuFile.add(exitItem);
        menuEdit.setMnemonic('E');
        menuEdit.add(new JMenuItem("Copy"));
        menuEdit.add(new JMenuItem("Paste"));
        menuHelp.setMnemonic('H');
        menuHelp.add(new JMenuItem("About"));
        mainMenuBar.add(menuFile);
        mainMenuBar.add(menuEdit);
        mainMenuBar.add(menuHelp);
        mainMenuBar.add(new JTextField());
    }
}
