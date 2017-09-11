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
