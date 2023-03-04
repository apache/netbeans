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
final class HTMLExporter extends Exporter {

    private float step = 1;
    private int lastStep = 0;


    protected int getSteps(TableModel model) {
        int steps = model.getRowCount();
        if (steps > MAX_STEPS) {
            step = MAX_STEPS / (float)steps;
            steps = MAX_STEPS;
        }
        return steps;
    }

    protected void writeHeader(TableModel model, String title, Writer writer,
                               TracerProgressObject progress) throws IOException {
        writeLine(writer, "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">"); // NOI18N
        writeLine(writer);

        writeLine(writer, "<html>"); // NOI18N
        writeLine(writer, "<head>"); // NOI18N
        writeLine(writer, "  <title>"); // NOI18N
        writeLine(writer, "    " + title); // NOI18N
        writeLine(writer, "  </title>"); // NOI18N
        writeLine(writer, "</head>"); // NOI18N
        writeLine(writer);
        
        writeLine(writer, "<body>"); // NOI18N
    }

    protected void writeData(TableModel model, String title, Writer writer,
                             TracerProgressObject progress) throws IOException {
        int columnsCount = model.getColumnCount();
        int rowsCount = model.getRowCount();

        writeLine(writer, "  <table border=\"1\" summary=\"" + title + "\">"); // NOI18N

        writeLine(writer, "    <thead>"); // NOI18N
        writeLine(writer, "      <tr>"); // NOI18N
        for (int c = 0; c < columnsCount; c++)
            writeLine(writer, "        <td>" + model.getColumnName(c) + "</td>"); // NOI18N
        writeLine(writer, "      </tr>"); // NOI18N
        writeLine(writer, "    </thead>"); // NOI18N

        writeLine(writer, "    <tbody>"); // NOI18N
        for (int r = 0; r < rowsCount; r++) {
            writeLine(writer, "      <tr>"); // NOI18N
            for (int c = 0; c < columnsCount; c++)
                writeLine(writer, "        <td>" + model.getValueAt(r, c) + "</td>"); // NOI18N
            writeLine(writer, "      </tr>"); // NOI18N
            
            if (progress.isFinished()) break;

            if (step == 1) {
                progress.addStep();
            } else {
                int currentStep = (int)(r * step);
                if (currentStep > lastStep) {
                    progress.addStep();
                    lastStep = currentStep;
                }
            }
        }
        writeLine(writer, "    </tbody>"); // NOI18N

        writeLine(writer, "  </table>"); // NOI18N
    }

    protected void writeFooter(TableModel model, String title, Writer writer,
                               TracerProgressObject progress) throws IOException {
        writeLine(writer, "</body>"); // NOI18N
        writeLine(writer, "</html>"); // NOI18N
    }

}
