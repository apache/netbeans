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

package org.netbeans.modules.cnd.meson.project;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.api.extexecution.ExecutionDescriptor.LineConvertorFactory;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

public class LineConvertorFactoryImpl implements LineConvertorFactory
{
    @Override
    public LineConvertor newLineConvertor()
    {
        return new MesonLineConvertor();
    }

    // This thing works for meson, gcc, and clang output.  It might work for other things, but
    // it would probably be best to use the toolchain appropriate instance of
    // org.netbeans.modules.cnd.spi.toolchain.CompilerLineConvertor for compiler output.
    private static final class MesonLineConvertor implements LineConvertor {
        private static final Pattern ERROR_LINE = Pattern.compile("(.*):(\\d+):(\\d+):.*");
        @Override
        public List<ConvertedLine> convert(String line) {
            Matcher matcher = ERROR_LINE.matcher(line);
            if (matcher.matches()) {
                String fileName = matcher.group(1);
                int lineNum = Integer.parseInt(matcher.group(2)) - 1;
                int columnNum = Integer.parseInt(matcher.group(3)) - 1;
                return Collections.singletonList(ConvertedLine.forText(line, new OutputListener() {
                    @Override
                    public void outputLineSelected(OutputEvent ev) {}
                    @Override
                    public void outputLineAction(OutputEvent ev) {
                        FileObject file = FileUtil.toFileObject(new File(fileName));
                        if (file != null) {
                            LineCookie lc = file.getLookup().lookup(LineCookie.class);
                            lc.getLineSet().getCurrent(lineNum).show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS, columnNum);
                        }
                    }
                    @Override
                    public void outputLineCleared(OutputEvent ev) {}
                }));
            }
            return Collections.singletonList(ConvertedLine.forText(line, null));
        }
    }
}