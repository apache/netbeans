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
package org.netbeans.modules.rust.cargo.output;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 * An LineConvertorFactory that adds hyperlinks in Rust error messages to go
 * directly to the origin of a problem.
 */
public class RustErrorHyperlinkConvertorFactory implements OutputListener, LineConvertor, ExecutionDescriptor.LineConvertorFactory {

    private static final Pattern RUST_ERROR_HYPERLINK = Pattern.compile("^[\\s]+--> ([^:]+):([\\d]+):([\\d]+)$");

    static Matcher matchesSourcePosition(String line) {
        return RUST_ERROR_HYPERLINK.matcher(line);
    }

    private final CargoTOML cargo;
    private final InputOutput inputOutput;

    public RustErrorHyperlinkConvertorFactory(CargoTOML cargo, InputOutput inputOutput) {
        this.cargo = cargo;
        this.inputOutput = inputOutput;
    }

    // ExecutionDescriptor.LineConverterFactory
    @Override
    public LineConvertor newLineConvertor() {
        return this;
    }

    // LineConverter
    @Override
    public List<ConvertedLine> convert(String line) {
        Matcher errorLocationLineMatcher = matchesSourcePosition(line);
        if (errorLocationLineMatcher.matches()) {
            ConvertedLine cline = ConvertedLine.forText(line, this);
            return Collections.singletonList(cline);
        }
        return null;
    }

    // OutputListener
    @Override
    public void outputLineAction(OutputEvent ev) {
        // Invoked to show a given position in the source code
        Matcher m = matchesSourcePosition(ev.getLine());
        if (m.matches()) {
            String dirAndFile = m.group(1);
            int lineNumber = Integer.parseInt(m.group(2));
            int column = Integer.parseInt(m.group(3));
            openFile(dirAndFile, lineNumber, column);
        }
    }

    private void openFile(String dirAndFile, int lineNumber, int column) {
        FileObject projectDirectory = cargo.getFileObject().getParent();
        FileObject file = projectDirectory.getFileObject(dirAndFile);

        if (file != null) {
            LineCookie lineCookie = file.getLookup().lookup(LineCookie.class);
            if (lineCookie != null) {
                Line theLine = lineCookie.getLineSet().getCurrent(lineNumber - 1);
                if (theLine != null) {
                    theLine.show(Line.ShowOpenType.OPEN,
                            Line.ShowVisibilityType.FOCUS,
                            column - 1);
                }
            }
        }
    }

}
