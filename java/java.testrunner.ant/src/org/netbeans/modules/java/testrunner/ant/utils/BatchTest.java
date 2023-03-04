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

package org.netbeans.modules.java.testrunner.ant.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.apache.tools.ant.module.spi.TaskStructure;

/**
 *
 * @author  Marian Petras
 */
final class BatchTest {

    /** */
    private final AntProject project;
    
    /** */
    private Collection<FileSet> fileSets = new ArrayList<FileSet>();

    /**
     */
    BatchTest(AntProject project) {
        this.project = project;
    }
    
    /**
     */
    void handleChildrenAndAttrs(TaskStructure struct) {
        for (TaskStructure child : struct.getChildren()) {
            String childName = child.getName();
            if (childName.equals("fileset")) {                          //NOI18N
                FileSet fs = new FileSet(project);
                fileSets.add(fs);
                fs.handleChildrenAndAttrs(child);
                continue;
            }
        }
    }
    
    /**
     *
     */
    int countTestClasses() {
        int count = 0;
        for (FileSet fileSet : fileSets) {
            Collection<File> matchingFiles = FileSetScanner.listFiles(fileSet);
            for (File file : matchingFiles) {
                final String name = file.getName();
                if (name.endsWith(".java") || name.endsWith(".class")) {//NOI18N
                    count++;
                }
            }
        }
        //TODO - handle the situation that two or more filesets contain
        //       the same file
        return count;
    }

}
