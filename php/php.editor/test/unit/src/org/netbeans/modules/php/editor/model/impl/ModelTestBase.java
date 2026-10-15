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

package org.netbeans.modules.php.editor.model.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Future;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.php.editor.model.*;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.php.editor.csl.PHPNavTestBase;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Radek Matous
 */
public class ModelTestBase extends PHPNavTestBase {

    public ModelTestBase(String testName) {
        super(testName);
    }

    protected Source getTestSource(String relativeFilePath) {
        FileObject fileObject = FileUtil.toFileObject(new File(getDataDir(), relativeFilePath));
        return getTestSource(fileObject);
    }

    protected Model getModel(Source testSource) throws Exception {
        return getModel(testSource, true);
    }

    protected Model getModel(Source testSource, boolean wait) throws Exception {
        final Model[] globals = new Model[1];
        UserTask userTask = new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                PHPParseResult parameter = (PHPParseResult) resultIterator.getParserResult();
                if (parameter != null) {
                    Model model = parameter.getModel();
                    globals[0] = model;
                }
            }
        };
        if (wait) {
            Future<Void> parseWhenScanFinished = ParserManager.parseWhenScanFinished(Collections.singleton(testSource), userTask);
            if (!parseWhenScanFinished.isDone()) {
                parseWhenScanFinished.get();
            }
        } else {
            ParserManager.parse(Collections.singleton(testSource), userTask);
        }
        return globals[0];
    }

    @Override
    protected FileObject[] createSourceClassPathsForTest() {
        FileObject dataDir = FileUtil.toFileObject(getDataDir());
        try {
            return new FileObject[]{toFileObject(dataDir, "testfiles/model", true)}; //NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
