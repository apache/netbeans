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
package org.netbeans.modules.cnd.discovery.performance;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;
import javax.swing.text.html.HTMLEditorKit;
import org.netbeans.modules.cnd.discovery.performance.AnalyzeStat.AgregatedStat;
import org.netbeans.modules.dlight.libs.common.PerformanceLogger;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class StatisticPanel extends JPanel {
    private static final RequestProcessor RP = new RequestProcessor("statistic", 1); //NOI18N
    private final PerformanceIssueDetector activeInstance;
    private final RequestProcessor.Task update;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private String lastUnusedFolders;
    private String lastSlowFolders;
    private String lastInfiniteParsing;

    /**
     * Creates new form StatisticPanel
     */
    public StatisticPanel() {
        initComponents();
        slowFolders.setEditorKit(new HTMLEditorKit());
        slowFolders.setBackground(getBackground());
        slowFolders.setForeground(getForeground());
        slowFolders.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        ((DefaultCaret) slowFolders.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        unusedFolders.setEditorKit(new HTMLEditorKit());
        unusedFolders.setBackground(getBackground());
        unusedFolders.setForeground(getForeground());
        unusedFolders.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        ((DefaultCaret) unusedFolders.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        infiniteParsing.setEditorKit(new HTMLEditorKit());
        infiniteParsing.setBackground(getBackground());
        infiniteParsing.setForeground(getForeground());
        infiniteParsing.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        ((DefaultCaret) infiniteParsing.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        activeInstance = PerformanceIssueDetector.getActiveInstance();
        update = RP.post(new Runnable() {
            @Override
            public void run() {
                if (SwingUtilities.isEventDispatchThread()) {
                    countStatistic();
                } else {
                    SwingUtilities.invokeLater(this);
                }
            }
        });
        addHierarchyListener(new HierarchyListener() {

            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if (e.getChangeFlags() == HierarchyEvent.SHOWING_CHANGED) {
                    if (!e.getChanged().isVisible()){
                        closed.set(true);
                    }
                }
            }
        });
    }

    @Messages({
        "Details.slowest.unused.folders=<table><tbody><tr><th>Slowest still unused folder</th><th>Items</th><th>Time,s</th></tr>",
        "Details.slowest.reading.folders=<table><tbody><tr><th>Slowest reading folder</th><th>Lines</th><th>Time,s</th></tr>",
        "Details.slowest.parsing.files=<table><tbody><tr><th>Possible infinite parsing files</th><th>Time (s)</th></tr>"
    })
    private void countStatistic() {
        if (closed.get()) {
            return;
        }
        if (activeInstance == null) {
            return;
        }
        TreeMap<String, AnalyzeStat.AgregatedStat> statistic = activeInstance.getStatistic();
        if (statistic != null) {
            {
                long count = 0;
                long time = 0;
                long cpu = 0;
                for (Map.Entry<String, AnalyzeStat.AgregatedStat> entry : statistic.entrySet()) {
                    count += entry.getValue().itemNumber;
                    time += entry.getValue().itemTime;
                    cpu += entry.getValue().itemCPU;
                }
                if (time > 0) {
                    long speed = (count * PerformanceIssueDetector.NANO_TO_SEC) / time;
                    itemSpeed.setText(PerformanceIssueDetector.format(speed));
                    getLimit(itemSpeedPanel, PerformanceIssueDetector.CREATION_SPEED_LIMIT, PerformanceIssueDetector.CREATION_SPEED_LIMIT_NORMAL, (int)speed);
                    itemNumber.setText(PerformanceIssueDetector.format(count));
                    itemWallTime.setText(PerformanceIssueDetector.format(time/PerformanceIssueDetector.NANO_TO_MILLI));
                    itemCpuTime.setText(PerformanceIssueDetector.format(cpu/PerformanceIssueDetector.NANO_TO_MILLI));
                    itemRatio.setText(PerformanceIssueDetector.format(cpu*100/time));
                }
            }
            {
                long count = 0;
                long time = 0;
                long cpu = 0;
                for (Map.Entry<String, AnalyzeStat.AgregatedStat> entry : statistic.entrySet()) {
                    count += entry.getValue().readBytes;
                    time += entry.getValue().readTime;
                    cpu += entry.getValue().readCPU;
                }
                if (time > 0) {
                    long speed = (count * PerformanceIssueDetector.NANO_TO_SEC) / time / 1024;
                    readSpeed.setText(PerformanceIssueDetector.format(speed));
                    getLimit(readSpeedPanel, PerformanceIssueDetector.READING_SPEED_LIMIT, PerformanceIssueDetector.READING_SPEED_LIMIT_NORMAL, (int)speed);
                    readNumber.setText(PerformanceIssueDetector.format(count / 1024));
                    readWallTime.setText(PerformanceIssueDetector.format(time/PerformanceIssueDetector.NANO_TO_MILLI));
                    readCpuTime.setText(PerformanceIssueDetector.format(cpu/PerformanceIssueDetector.NANO_TO_MILLI));
                    readRatio.setText(PerformanceIssueDetector.format(cpu*100/time));
                }
            }
            {
                long count = 0;
                long files = 0;
                long time = 0;
                long cpu = 0;
                for (Map.Entry<String, AnalyzeStat.AgregatedStat> entry : statistic.entrySet()) {
                    count += entry.getValue().parseLines;
                    files += entry.getValue().parseNumber;
                    time += entry.getValue().parseTime;
                    cpu += entry.getValue().parseCPU;
                }
                if (time > 0) {
                    //PerformanceIssueDetector.PARSING_SPEED_LIMIT = 1000;
                    //PerformanceIssueDetector.PARSING_RATIO_LIMIT = 5;
                    long speed = (count * PerformanceIssueDetector.NANO_TO_SEC) / time;
                    parsingSpeed.setText(PerformanceIssueDetector.format(speed));
                    getLimit(parsingSpeedPanel,PerformanceIssueDetector.PARSING_SPEED_LIMIT, PerformanceIssueDetector.PARSING_SPEED_LIMIT_NORMAL, (int)speed);
                    parsingLines.setText(PerformanceIssueDetector.format(count));
                    parsingNumber.setText(PerformanceIssueDetector.format(files));
                    parsingWallTime.setText(PerformanceIssueDetector.format(time/PerformanceIssueDetector.NANO_TO_MILLI));
                    parsingCpuTime.setText(PerformanceIssueDetector.format(cpu/PerformanceIssueDetector.NANO_TO_MILLI));
                    parsingRatio.setText(PerformanceIssueDetector.format(cpu*100/time));
                    getLimit(parsingRatioPanel, 100/PerformanceIssueDetector.PARSING_RATIO_LIMIT, 100/PerformanceIssueDetector.PARSING_RATIO_LIMIT_NORMAL, (int)(cpu*100/time));
                }
            }
            {
                AnalyzeStat.upEmptyFolder(statistic);
                StringBuilder buf = new StringBuilder();
                int i = 0;
                for (Map.Entry<String, AgregatedStat> entry : AnalyzeStat.getBigUnused(statistic)) {
                    if (buf.length()==0) {
                        buf.append(Bundle.Details_slowest_unused_folders());
                    }
                    buf.append("<tr><td>"); //NOI18N
                    buf.append(entry.getKey());
                    buf.append("</td><td>"); //NOI18N
                    buf.append(PerformanceIssueDetector.format(entry.getValue().itemNumber));
                    buf.append("</td><td>"); //NOI18N
                    buf.append(PerformanceIssueDetector.format(entry.getValue().itemTime/PerformanceIssueDetector.NANO_TO_SEC));
                    buf.append("</td></tr>"); //NOI18N
                    i++;
                    if (i == 3) {
                        break;
                    }
                }
                if (buf.length()>0) {
                    buf.append("</tbody></table>"); //NOI18N
                }
                if (lastUnusedFolders == null || !lastUnusedFolders.equals(buf.toString())) {
                    lastUnusedFolders = buf.toString();
                    updatePane(lastUnusedFolders, unusedFolders, unusedFoldersScrollPane);
                }
                AnalyzeStat.groupByReadingSpeed(statistic);
                i = 0;
                buf.setLength(0);
                for (Map.Entry<String, AgregatedStat> entry : AnalyzeStat.getSlowReading(statistic)) {
                    if (buf.length()==0) {
                        buf.append(Bundle.Details_slowest_reading_folders());
                    }
                    buf.append("<tr><td>"); //NOI18N
                    buf.append(entry.getKey());
                    buf.append("</td><td>"); //NOI18N
                    buf.append(PerformanceIssueDetector.format(entry.getValue().readLines));
                    buf.append("</td><td>"); //NOI18N
                    buf.append(PerformanceIssueDetector.format(entry.getValue().readTime/PerformanceIssueDetector.NANO_TO_SEC));
                    buf.append("</td></tr>"); //NOI18N
                     i++;
                     if (i == 3) {
                         break;
                     }
                }
                if (buf.length()>0) {
                    buf.append("</tbody></table>"); //NOI18N
                }
                if (lastSlowFolders == null || !lastSlowFolders.equals(buf.toString())) {
                    lastSlowFolders = buf.toString();
                    updatePane(lastSlowFolders, slowFolders, slowFoldersScrollPane);
                }
                buf.setLength(0);
                for(Map.Entry<FileObject, PerformanceLogger.PerformanceEvent> entry : activeInstance.getParseTimeout().entrySet()) {
                    if (buf.length()==0) {
                        buf.append(Bundle.Details_slowest_parsing_files());
                    }
                    buf.append("<tr><td>"); //NOI18N
                    buf.append(entry.getKey().getPath());
                    buf.append("</td><td>"); //NOI18N
                    buf.append(PerformanceIssueDetector.format((System.nanoTime() - entry.getValue().getStartTime())/PerformanceIssueDetector.NANO_TO_SEC));
                    buf.append("</td><td>"); //NOI18N
                }
                if (buf.length()>0) {
                    buf.append("</tbody></table>"); //NOI18N
                }
                if (lastInfiniteParsing == null || !lastInfiniteParsing.equals(buf.toString())) {
                    lastInfiniteParsing = buf.toString();
                    updatePane(lastInfiniteParsing, infiniteParsing, infiniteParsingScrollPane);
                }
            }
        }
        update.schedule(2000);
    }
    
    private void updatePane(String text, JTextPane pane, JScrollPane scroll) {
        text = "<head></head><body>"+text+"</body>"; //NOI18N
        pane.setText(text);
    }
    
    private JPanel getLimit(JPanel parent,int low, int normal, int fact) {
        parent.removeAll();
        JPanel panel = new MyPanel(low, normal, fact);
        parent.add(panel, BorderLayout.CENTER);
        parent.validate();
        parent.repaint();
        return panel;
    }

    private static final class MyPanel extends JPanel {
        private static final int NORMAL_COLOR = 192;
        private static final int BRIGHT_COLOR = 224;
        private final int low;
        private final int normal;
        private final int fact;
        private MyPanel(int low, int normal, int fact) {
            this.low = low;
            this.normal = normal;
            this.fact = fact;
        }

        @Override
        public Color getBackground() {
            return Color.WHITE;
        }

        @Override
        public void paint(Graphics g) {
            int MY_HEIGHT = getHeight();
            int MY_WIDTH = getWidth();
            Graphics2D graphics = (Graphics2D)g;
            graphics.drawRect(0, 0, MY_WIDTH, MY_HEIGHT);
            if (MY_WIDTH < 100 || MY_HEIGHT < 20) {
                return;
            }
            double m1 = Math.log(low);
            double m2 = Math.log(normal);
            double m3;
            if (fact == 0) {
                m3 = low - 2;
            } else {
                m3 = Math.log(fact);
            }
            
            double min = Math.min(Math.min(m1, m2),m3);
            double max = Math.max(Math.max(m1, m2),m3);
            int borders = MY_WIDTH/4;
            double point = (MY_WIDTH - borders)/(max - min);
            int m1x = (int)(borders/2 + (m1 - min) * point);
            int m2x = (int)(borders/2 + (m2 - min) * point);
            int m3x = (int)(borders/2 + (m3 - min) * point);
            Color def = graphics.getColor();
            
            float[] fractions = new float[]{0f, 1f};  
            Color[] colors = new Color[]{new Color(BRIGHT_COLOR, 0, 0), new Color(NORMAL_COLOR, 0, 0)};  
            LinearGradientPaint gradient = new LinearGradientPaint(1, 1, m1x-1, 1, fractions, colors);  
            graphics.setPaint(gradient);
            graphics.fillRect(1, 1, m1x-1, MY_HEIGHT-2);

            fractions = new float[]{0f, 0.3f, 0.5f, 0.7f, 1f};
            colors = new Color[]{new Color(NORMAL_COLOR, 0, 0),
                                 new Color(NORMAL_COLOR, NORMAL_COLOR, 0),
                                 new Color(BRIGHT_COLOR, BRIGHT_COLOR, 0),
                                 new Color(NORMAL_COLOR, NORMAL_COLOR, 0),
                                 new Color(0, NORMAL_COLOR, 0)};  
            gradient = new LinearGradientPaint(m1x+1, 1, m2x-1 ,1, fractions, colors);  
            graphics.setPaint(gradient);
            graphics.fillRect(m1x+1, 1, m2x-m1x-1 ,MY_HEIGHT-2);

            fractions = new float[]{0f, 1f};
            colors = new Color[]{new Color(0, NORMAL_COLOR, 0), new Color(0, BRIGHT_COLOR, 0)};  
            gradient = new LinearGradientPaint(m2x+1, 1, MY_WIDTH-1 ,1, fractions, colors);  
            graphics.setPaint(gradient);
            graphics.fillRect(m2x+1, 1, MY_WIDTH-m2x-1 ,MY_HEIGHT-2);

            graphics.setColor(Color.blue);
            graphics.fillOval(m3x-MY_HEIGHT/4, MY_HEIGHT/4, MY_HEIGHT/2, MY_HEIGHT/2);
            graphics.setColor(def);

            graphics.drawLine(m1x, MY_HEIGHT-2, m1x, 1);
            String what = ""+low; //NOI18N
            int shift = graphics.getFontMetrics().getStringBounds(what, g).getBounds().width/2;
            graphics.drawString(what, m1x-shift, MY_HEIGHT -5);
            
            graphics.drawLine(m2x, MY_HEIGHT-2, m2x, 1);
            what = ""+normal; //NOI18N
            shift = graphics.getFontMetrics().getStringBounds(what, g).getBounds().width/2;
            graphics.drawString(what, m2x-shift, MY_HEIGHT -5);
            graphics.draw3DRect(0, 0, MY_WIDTH-1, MY_HEIGHT-1, false);

        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        itemLabel = new javax.swing.JLabel();
        itemSpeedLabel = new javax.swing.JLabel();
        itemSpeed = new javax.swing.JTextField();
        itemNumberLabel = new javax.swing.JLabel();
        itemNumber = new javax.swing.JTextField();
        itemWallTimeLabel = new javax.swing.JLabel();
        itemWallTime = new javax.swing.JTextField();
        itemCpuTimeLabel = new javax.swing.JLabel();
        itemCpuTime = new javax.swing.JTextField();
        itemRatioLabel = new javax.swing.JLabel();
        itemRatio = new javax.swing.JTextField();
        readLabel = new javax.swing.JLabel();
        readSpeedLabel = new javax.swing.JLabel();
        readSpeed = new javax.swing.JTextField();
        readNumberLabel = new javax.swing.JLabel();
        readNumber = new javax.swing.JTextField();
        readWallTimeLabel = new javax.swing.JLabel();
        readWallTime = new javax.swing.JTextField();
        readCpuTimeLabel = new javax.swing.JLabel();
        readCpuTime = new javax.swing.JTextField();
        readRatioLabel = new javax.swing.JLabel();
        readRatio = new javax.swing.JTextField();
        parsingLabel = new javax.swing.JLabel();
        parsingSpeedLabel = new javax.swing.JLabel();
        parsingSpeed = new javax.swing.JTextField();
        parsingLinesLabel = new javax.swing.JLabel();
        parsingLines = new javax.swing.JTextField();
        parsingNumberLabel = new javax.swing.JLabel();
        parsingNumber = new javax.swing.JTextField();
        parsingWallTimeLabel = new javax.swing.JLabel();
        parsingWallTime = new javax.swing.JTextField();
        parsingCpuTimeLabel = new javax.swing.JLabel();
        parsingCpuTime = new javax.swing.JTextField();
        parsingRatioLabel = new javax.swing.JLabel();
        parsingRatio = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        itemSpeedPanel = new javax.swing.JPanel();
        readSpeedPanel = new javax.swing.JPanel();
        parsingSpeedPanel = new javax.swing.JPanel();
        parsingRatioPanel = new javax.swing.JPanel();
        itemsSlowPanel = new javax.swing.JPanel();
        unusedFoldersScrollPane = new javax.swing.JScrollPane();
        unusedFolders = new javax.swing.JTextPane();
        readSlowPanel = new javax.swing.JPanel();
        slowFoldersScrollPane = new javax.swing.JScrollPane();
        slowFolders = new javax.swing.JTextPane();
        parsingPanel = new javax.swing.JPanel();
        infiniteParsingScrollPane = new javax.swing.JScrollPane();
        infiniteParsing = new javax.swing.JTextPane();

        setMinimumSize(new java.awt.Dimension(500, 350));
        setPreferredSize(new java.awt.Dimension(700, 550));
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(itemLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.itemLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(itemLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(itemSpeedLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.itemSpeedLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(itemSpeedLabel, gridBagConstraints);

        itemSpeed.setEditable(false);
        itemSpeed.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(itemSpeed, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(itemNumberLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.itemNumberLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(itemNumberLabel, gridBagConstraints);

        itemNumber.setEditable(false);
        itemNumber.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(itemNumber, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(itemWallTimeLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.itemWallTimeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(itemWallTimeLabel, gridBagConstraints);

        itemWallTime.setEditable(false);
        itemWallTime.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(itemWallTime, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(itemCpuTimeLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.itemCpuTimeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(itemCpuTimeLabel, gridBagConstraints);

        itemCpuTime.setEditable(false);
        itemCpuTime.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(itemCpuTime, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(itemRatioLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.itemRatioLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(itemRatioLabel, gridBagConstraints);

        itemRatio.setEditable(false);
        itemRatio.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(itemRatio, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(readLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.readLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(readLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(readSpeedLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.readSpeedLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(readSpeedLabel, gridBagConstraints);

        readSpeed.setEditable(false);
        readSpeed.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(readSpeed, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(readNumberLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.readNumberLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(readNumberLabel, gridBagConstraints);

        readNumber.setEditable(false);
        readNumber.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(readNumber, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(readWallTimeLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.readWallTimeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(readWallTimeLabel, gridBagConstraints);

        readWallTime.setEditable(false);
        readWallTime.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(readWallTime, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(readCpuTimeLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.readCpuTimeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(readCpuTimeLabel, gridBagConstraints);

        readCpuTime.setEditable(false);
        readCpuTime.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(readCpuTime, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(readRatioLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.readRatioLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(readRatioLabel, gridBagConstraints);

        readRatio.setEditable(false);
        readRatio.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(readRatio, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(parsingLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.parsingLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(parsingLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(parsingSpeedLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.parsingSpeedLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(parsingSpeedLabel, gridBagConstraints);

        parsingSpeed.setEditable(false);
        parsingSpeed.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(parsingSpeed, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(parsingLinesLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.parsingLinesLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(parsingLinesLabel, gridBagConstraints);

        parsingLines.setEditable(false);
        parsingLines.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(parsingLines, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(parsingNumberLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.parsingNumberLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(parsingNumberLabel, gridBagConstraints);

        parsingNumber.setEditable(false);
        parsingNumber.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(parsingNumber, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(parsingWallTimeLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.parsingWallTimeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(parsingWallTimeLabel, gridBagConstraints);

        parsingWallTime.setEditable(false);
        parsingWallTime.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(parsingWallTime, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(parsingCpuTimeLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.parsingCpuTimeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(parsingCpuTimeLabel, gridBagConstraints);

        parsingCpuTime.setEditable(false);
        parsingCpuTime.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(parsingCpuTime, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(parsingRatioLabel, org.openide.util.NbBundle.getMessage(StatisticPanel.class, "StatisticPanel.parsingRatioLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(parsingRatioLabel, gridBagConstraints);

        parsingRatio.setEditable(false);
        parsingRatio.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(parsingRatio, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(jSeparator1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(jSeparator2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(jSeparator3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        itemSpeedPanel.setMinimumSize(new java.awt.Dimension(202, 22));
        itemSpeedPanel.setPreferredSize(new java.awt.Dimension(202, 22));
        itemSpeedPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(itemSpeedPanel, gridBagConstraints);

        readSpeedPanel.setMinimumSize(new java.awt.Dimension(202, 22));
        readSpeedPanel.setPreferredSize(new java.awt.Dimension(202, 22));
        readSpeedPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(readSpeedPanel, gridBagConstraints);

        parsingSpeedPanel.setMinimumSize(new java.awt.Dimension(202, 22));
        parsingSpeedPanel.setPreferredSize(new java.awt.Dimension(202, 22));
        parsingSpeedPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(parsingSpeedPanel, gridBagConstraints);

        parsingRatioPanel.setInheritsPopupMenu(true);
        parsingRatioPanel.setMinimumSize(new java.awt.Dimension(202, 22));
        parsingRatioPanel.setPreferredSize(new java.awt.Dimension(202, 22));
        parsingRatioPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(parsingRatioPanel, gridBagConstraints);

        itemsSlowPanel.setLayout(new java.awt.BorderLayout());

        unusedFolders.setEditable(false);
        unusedFoldersScrollPane.setViewportView(unusedFolders);

        itemsSlowPanel.add(unusedFoldersScrollPane, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(itemsSlowPanel, gridBagConstraints);

        readSlowPanel.setLayout(new java.awt.BorderLayout());

        slowFolders.setEditable(false);
        slowFoldersScrollPane.setViewportView(slowFolders);

        readSlowPanel.add(slowFoldersScrollPane, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(readSlowPanel, gridBagConstraints);

        parsingPanel.setLayout(new java.awt.BorderLayout());

        infiniteParsing.setEditable(false);
        infiniteParsingScrollPane.setViewportView(infiniteParsing);

        parsingPanel.add(infiniteParsingScrollPane, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(parsingPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane infiniteParsing;
    private javax.swing.JScrollPane infiniteParsingScrollPane;
    private javax.swing.JTextField itemCpuTime;
    private javax.swing.JLabel itemCpuTimeLabel;
    private javax.swing.JLabel itemLabel;
    private javax.swing.JTextField itemNumber;
    private javax.swing.JLabel itemNumberLabel;
    private javax.swing.JTextField itemRatio;
    private javax.swing.JLabel itemRatioLabel;
    private javax.swing.JTextField itemSpeed;
    private javax.swing.JLabel itemSpeedLabel;
    private javax.swing.JPanel itemSpeedPanel;
    private javax.swing.JTextField itemWallTime;
    private javax.swing.JLabel itemWallTimeLabel;
    private javax.swing.JPanel itemsSlowPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTextField parsingCpuTime;
    private javax.swing.JLabel parsingCpuTimeLabel;
    private javax.swing.JLabel parsingLabel;
    private javax.swing.JTextField parsingLines;
    private javax.swing.JLabel parsingLinesLabel;
    private javax.swing.JTextField parsingNumber;
    private javax.swing.JLabel parsingNumberLabel;
    private javax.swing.JPanel parsingPanel;
    private javax.swing.JTextField parsingRatio;
    private javax.swing.JLabel parsingRatioLabel;
    private javax.swing.JPanel parsingRatioPanel;
    private javax.swing.JTextField parsingSpeed;
    private javax.swing.JLabel parsingSpeedLabel;
    private javax.swing.JPanel parsingSpeedPanel;
    private javax.swing.JTextField parsingWallTime;
    private javax.swing.JLabel parsingWallTimeLabel;
    private javax.swing.JTextField readCpuTime;
    private javax.swing.JLabel readCpuTimeLabel;
    private javax.swing.JLabel readLabel;
    private javax.swing.JTextField readNumber;
    private javax.swing.JLabel readNumberLabel;
    private javax.swing.JTextField readRatio;
    private javax.swing.JLabel readRatioLabel;
    private javax.swing.JPanel readSlowPanel;
    private javax.swing.JTextField readSpeed;
    private javax.swing.JLabel readSpeedLabel;
    private javax.swing.JPanel readSpeedPanel;
    private javax.swing.JTextField readWallTime;
    private javax.swing.JLabel readWallTimeLabel;
    private javax.swing.JTextPane slowFolders;
    private javax.swing.JScrollPane slowFoldersScrollPane;
    private javax.swing.JTextPane unusedFolders;
    private javax.swing.JScrollPane unusedFoldersScrollPane;
    // End of variables declaration//GEN-END:variables

}
