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

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.TimerTask;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

/**
 * Displays information about any long pauses in the AWT thread.
 * @author Jesse Glick
 */
final class Monitor extends JPanel implements ActionListener {
    
    private static final int DELAY_CPU = 100; // msec between AWT pings
    private static final long DELAY_HEAP = 1000; // msec between heap recalcs
    private static final long GRACE = 500; // msec permitted without access
    
    private long last;
    private final Timer t1;
    private final java.util.Timer t2;
    private final TimerTask task;
    private final JTextField blockageField;
    private final JTextField heapField;
    private static final NumberFormat format = new DecimalFormat("0.00");
    private JCheckBox heapLiveCheckBox;
    private boolean heapLive;
    
    public Monitor() {
        setLayout(new GridLayout(3, 1));
        t1 = new Timer(DELAY_CPU, this);
        t1.setRepeats(true);
        t2 = new java.util.Timer(true);
        task = new TimerTask() {
            public void run() {
                updateHeap();
            }
        };
        last = System.currentTimeMillis();
        JPanel sub = new JPanel();
        sub.add(new JLabel("Last blockage above " + GRACE + "msec:"));
        blockageField = new JTextField(10);
        Font font = new Font("Monospaced", Font.PLAIN, blockageField.getFont().getSize());
        blockageField.setFont(font);
        blockageField.setEditable(false);
        sub.add(blockageField);
        add(sub);
        sub = new JPanel();
        sub.add(new JLabel("Heap:"));
        heapField = new JTextField(10);
        heapField.setFont(font);
        heapField.setEditable(false);
        sub.add(heapField);
        heapLiveCheckBox = new JCheckBox("Immediate Heap Updates");
        heapLiveCheckBox.setMnemonic('h');
        sub.add(heapLiveCheckBox);
        add(sub);
        sub = new JPanel();
        JButton gc = new JButton("GC");
        gc.setMnemonic('g');
        gc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                System.gc();
                System.runFinalization();
                System.gc();
            }
        });
        sub.add(gc);
        add(sub);
    }
    
    public void addNotify() {
        super.addNotify();
        t1.start();
        t2.schedule(task, 0L, DELAY_HEAP);
    }
    
    public void removeNotify() {
        t1.stop();
        t2.cancel();
    }
    
    public void actionPerformed(ActionEvent e) {
        long now = System.currentTimeMillis();
        long block = now - last;
        if (block > GRACE) {
            System.err.println("AWT blocked for " + block + "msec");
            blockageField.setText(Long.toString(block) + "msec");
            blockageField.paintImmediately(0, 0, blockageField.getWidth(), blockageField.getHeight());
        }
        last = now;
        // Also update heapLive since we are in AWT:
        heapLive = heapLiveCheckBox.isSelected();
    }
    
    private void updateHeap() {
        // setText is thread-safe:
        heapField.setText(format.format(heapUsedMega()) + "Mb");
        if (heapLive) {
            // Show live updates even when AWT is blocked.
            // Note that this can sometimes cause weird painting problems
            // in other windows, so off by default.
            heapField.paintImmediately(0, 0, heapField.getWidth(), heapField.getHeight());
        }
    }
    
    private static float heapUsedMega() {
        Runtime r = Runtime.getRuntime();
        return (r.totalMemory() - r.freeMemory()) / 1024.0f / 1024.0f;
    }
    
}
