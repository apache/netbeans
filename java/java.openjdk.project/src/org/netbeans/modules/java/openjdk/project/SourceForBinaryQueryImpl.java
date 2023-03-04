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
package org.netbeans.modules.java.openjdk.project;

import java.net.URL;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lahvac
 */
public class SourceForBinaryQueryImpl implements SourceForBinaryQueryImplementation2 {

    private final URL fakeOutput;
    private final Result result;

    public SourceForBinaryQueryImpl(URL fakeOutput, ClassPath sourceCP) {
        this.fakeOutput = fakeOutput;
        this.result = new ResultImpl(sourceCP);
    }

    @Override
    public Result findSourceRoots2(URL binaryRoot) {
        if (fakeOutput.equals(binaryRoot)) {
            return result;
        }
        return null;
    }

    @Override
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        return findSourceRoots2(binaryRoot);
    }

    private static final class ResultImpl implements Result {

        private final ClassPath sourceCP;

        public ResultImpl(ClassPath sourceCP) {
            this.sourceCP = sourceCP; //XXX: listener
        }

        @Override
        public boolean preferSources() {
            return true;
        }

        @Override
        public FileObject[] getRoots() {
            return sourceCP.getRoots();
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

    }

}
