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
package org.netbeans.modules.dlight.sendto.output;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 */
public final class OutputConvertorFactory implements ExecutionDescriptor.LineConvertorFactory {

    private final LineConvertors.FileLocator fileLocator;

    public OutputConvertorFactory(Collection<? extends FileObject> fos) {
        fileLocator = new FileLocator(fos);
    }

    @Override
    public LineConvertor newLineConvertor() {
        List<LineConvertor> convertors = new ArrayList<LineConvertor>();
        for (OutputPattern outputPattern : OutputPatterns.getPatterns()) {
            int fileGrp = outputPattern.order == OutputPattern.Order.FILE_LINE ? 1 : 2;
            int lineGrp = outputPattern.order == OutputPattern.Order.FILE_LINE ? 2 : 1;
            convertors.add(LineConvertors.filePattern(
                    fileLocator, outputPattern.pattern, null, fileGrp, lineGrp));
        }
        return LineConvertors.proxy(convertors.toArray(new LineConvertor[convertors.size()]));
    }

    private static class FileLocator implements LineConvertors.FileLocator {

        private final Collection<? extends FileObject> fos;

        public FileLocator(Collection<? extends FileObject> fos) {
            this.fos = fos;
        }

        @Override
        public FileObject find(String filePath) {
            File file = new File(filePath);
            if (file.isAbsolute()) {
                FileObject result = FileUtil.toFileObject(file);

                if (result != null) {
                    return result;
                }
            } else {
                for (FileObject fo : fos) {
                    while (fo.getParent() != null) {
                        FileObject res = fo.getFileObject(filePath);
                        if (res != null) {
                            return res;
                        }
                        fo = fo.getParent();
                    }
                }
            }

            return null;
        }
    }
}
