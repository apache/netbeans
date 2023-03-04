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
final class CSVExporter extends Exporter {

    private static final String DELIMITER = ","; // NOI18N

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

    protected void writeData(TableModel model, String title, Writer writer,
                             TracerProgressObject progress) throws IOException {
        int columnsCount = model.getColumnCount();
        int rowsCount = model.getRowCount();

        for (int c = 0; c < columnsCount; c++) {
            write(writer, "\"" + model.getColumnName(c) + "\""); // NOI18N
            if (c < columnsCount - 1) write(writer, DELIMITER);
        }
        writeLine(writer);

        for (int r = 0; r < rowsCount; r++) {
            for (int c = 0; c < columnsCount; c++) {
                write(writer, "\"" + model.getValueAt(r, c) + "\""); // NOI18N
                if (c < columnsCount - 1) write(writer, DELIMITER);
            }
            writeLine(writer);
            
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
    }

}
