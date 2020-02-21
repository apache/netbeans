/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
