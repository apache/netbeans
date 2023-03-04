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

package org.netbeans.modules.profiler.snaptracer.impl.export;

import org.netbeans.modules.profiler.snaptracer.TracerProgressObject;
import org.netbeans.modules.profiler.snaptracer.impl.swing.VerticalLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;
import org.netbeans.modules.profiler.snaptracer.impl.TracerSupportImpl;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jiri Sedlacek
 */
public final class DataExport {

    private static final Logger LOGGER = Logger.getLogger(DataExport.class.getName());

    private static final int INDETERMINATE_PROGRESS_THRESHOLD =
                Integer.getInteger("visualvm.tracer.indeterminateProgressThreshold", 2500); // NOI18N

    private static final Filter CSV_FILTER  = Filter.create("CSV Files", ".csv");
    private static final Filter HTML_FILTER = Filter.create("HTML Files", ".html");
    private static final Filter XML_FILTER  = Filter.create("XML Files", ".xml");

    private static JFileChooser fileChooser;
    private static File lastDirectory;
    private static Filter lastFilter = CSV_FILTER;


    public static void exportData(final TableModel model, final String title) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFileChooser chooser = getFileChooser();
                if (chooser.showSaveDialog(WindowManager.getDefault().getRegistry().
                        getActivated()) == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    Filter filter = (Filter)chooser.getFileFilter();
                    doExportData(model, title, file, filter);
                }
                lastDirectory = chooser.getCurrentDirectory();
                lastFilter = (Filter)chooser.getFileFilter();
            }
        });
    }


    private static void doExportData(final TableModel model, final String title,
                                     final File file, final Filter filter) {
        
        TracerSupportImpl.getInstance().perform(new Runnable() {
            public void run() {
                Writer writer = null;
                TracerProgressObject progress = null;
                try {
                    writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
                    ExportBatch batch = null;

                    if (filter == XML_FILTER)
                        batch = new XMLExporter().createBatch(model, title, writer);
                    else if (filter == HTML_FILTER)
                        batch = new HTMLExporter().createBatch(model, title, writer);
                    else if (filter == CSV_FILTER)
                        batch = new CSVExporter().createBatch(model, title, writer);

                    if (batch != null) {
                        progress = batch.getProgress();
                        final TracerProgressObject progressF = progress;
                        SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() { showProgressDialog(progressF); }
                        });
                        batch.getWorker().run();
                    }
                } catch (Throwable t) {
                    if (progress != null) {
                        progress.setText("Exporting data failed");
                        progress.finish();
                    }
                    LOGGER.log(Level.INFO, "Exporting data failed", t); // NOI18N
                } finally {
                    if (writer != null) try { writer.close(); } catch (Exception e) {}
                }
            }
        });
    }

    private static void showProgressDialog(final TracerProgressObject progress) {
        final JLabel l = new JLabel();
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));

        final JProgressBar p = new JProgressBar(0, progress.getSteps()) {
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = Exporter.MAX_STEPS;
                d.height += 2;
                return d;
            }
        };
        p.setBorder(BorderFactory.createEmptyBorder());
        final Timer t = new Timer(INDETERMINATE_PROGRESS_THRESHOLD, null);
        t.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                p.setIndeterminate(true);
                t.stop();
            }
        });

        final JButton b = new JButton() {
            protected void fireActionPerformed(ActionEvent e) {
                SwingUtilities.getWindowAncestor(this).setVisible(false);
            }
        };

        progress.addListener(new TracerProgressObject.Listener() {
            public void progressChanged(int addedSteps, int currentStep, String text) {
                t.stop();
                p.setIndeterminate(false);
                p.setValue(currentStep);
                l.setText(text == null ? "" : text); // NOI18N
                if (!progress.isFinished()) t.start();
                else b.setText("Close");
            }
        });
        l.setText(progress.getText());
        p.setValue(progress.getCurrentStep());
        b.setText(progress.isFinished() ? "Close" : "Cancel");

        JPanel c = new JPanel(new VerticalLayout(false));
        c.setBorder(BorderFactory.createEmptyBorder(20, 10, 15, 10));
        c.add(l);
        c.add(p);

        final DialogDescriptor dd = new DialogDescriptor(c, "Export Tracer Data",
                                                        true, new Object[] { b },
                                                        b, DialogDescriptor.BOTTOM_ALIGN,
                                                        null, null);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Dialog d = DialogDisplayer.getDefault().createDialog(dd);
                openDialog(d);
                progress.finish();
            }
        });
    }
    

    private static JFileChooser getFileChooser() {
        if (fileChooser == null)   fileChooser = createFileChooser();
        if (lastDirectory != null) fileChooser.setCurrentDirectory(lastDirectory);
        if (lastFilter != null)    fileChooser.setFileFilter(lastFilter);
        return fileChooser;
    }

    private static JFileChooser createFileChooser() {
        JFileChooser chooser = new JFileChooser() {
            public void approveSelection() {
                File file = getSelectedFile();
                Filter filter = (Filter)getFileFilter();

                if (!file.getName().endsWith(filter.getExt())) {
                    file = new File(file.getPath() + filter.getExt());
                    setSelectedFile(file);
                }

                if (!file.isFile()) {
                    super.approveSelection();
                    return;
                }
                
                final boolean[] ret = new boolean[1];
                JButton yesB = new JButton("Yes") {
                    protected void fireActionPerformed(ActionEvent e) {
                        ret[0] = true;
                        super.fireActionPerformed(e);
                    }
                };
                DialogDescriptor desc = new DialogDescriptor(
                    "File \"" + file.getName() + "\" already exists.\n" +
                    "Do you want to replace it?", "Replace Existing File",
                    true, new Object[] { yesB, new JButton("No") }, yesB,
                    DialogDescriptor.BOTTOM_ALIGN, null, null);
                desc.setMessageType(NotifyDescriptor.QUESTION_MESSAGE);
                Dialog d = DialogDisplayer.getDefault().createDialog(desc);
                openDialog(d);
                if (ret[0] == true) super.approveSelection();
            }
        };

        chooser.setDialogTitle("Export Tracer Data");
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        chooser.setAcceptAllFileFilterUsed(false);

        chooser.addChoosableFileFilter(CSV_FILTER);
        chooser.addChoosableFileFilter(HTML_FILTER);
        chooser.addChoosableFileFilter(XML_FILTER);

        return chooser;
    }

    private static void openDialog(Dialog d) {
        if (d instanceof JDialog) ((JDialog)d).setLocationRelativeTo(
                WindowManager.getDefault().getRegistry().getActivated());
        d.setVisible(true);
    }

    
    private abstract static class Filter extends FileFilter {

        abstract String getExt();

        static Filter create(final String descr, final String ext) {
            return new Filter() {
                public boolean accept(File f) {
                    return f.isDirectory() || getFileExt(f.getName()).equals(ext);
                }
                public String getExt() {
                    return ext;
                }
                public String getDescription() {
                    return descr + " (*" + ext + ")";
                }
            };
        }
        
        private static String getFileExt(String fileName) {
            int extIndex = fileName.lastIndexOf('.'); // NOI18N
            if (extIndex == -1) return ""; // NOI18N
            return fileName.substring(extIndex);
        }

        private Filter() {}

    }


    private DataExport() {}

}
