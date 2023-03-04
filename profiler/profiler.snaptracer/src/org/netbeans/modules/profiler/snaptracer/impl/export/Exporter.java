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
import java.io.IOException;
import java.io.Writer;
import javax.swing.table.TableModel;

/**
 *
 * @author Jiri Sedlacek
 */
abstract class Exporter {

    protected static final int MAX_STEPS = 300; // Defines also JProgressBar width

    
    protected abstract int getSteps(TableModel model);
    
    protected void writeHeader(TableModel model, String title, Writer writer,
                               TracerProgressObject progress) throws IOException {}

    protected void writeData(TableModel model, String title, Writer writer,
                             TracerProgressObject progress) throws IOException {}

    protected void writeFooter(TableModel model, String title, Writer writer,
                               TracerProgressObject progress) throws IOException {}


    protected static void writeLine(Writer writer) throws IOException {
        writer.write("\n"); // NOI18N
    }

    protected static void writeLine(Writer writer, String line) throws IOException {
        writer.write(line + "\n"); // NOI18N
    }

    protected static void write(Writer writer, String text) throws IOException {
        writer.write(text);
    }


    final ExportBatch createBatch(final TableModel model, final String title,
                                  final Writer writer) {
        
        final TracerProgressObject progress = new TracerProgressObject(getSteps(model) + 2);

        ExportBatch.BatchRunnable worker = new ExportBatch.BatchRunnable() {
            public void run() throws IOException {
                doExport(model, title, writer, progress);
            }
        };

        return new ExportBatch(progress, worker);
    }

    private void doExport(TableModel model, String title, Writer writer,
                          TracerProgressObject progress) throws IOException {
        progress.setText("Initializing export...");
        writeHeader(model, title, writer, progress);

        if (progress.isFinished()) return;

        progress.addStep("Exporting data...");
        writeData(model, title, writer, progress);

        if (progress.isFinished()) return;

        progress.setText("Finishing export...");
        writeFooter(model, title, writer, progress);

        if (progress.isFinished()) return;

        progress.setText("Data exported");
        progress.finish();
    }

}
