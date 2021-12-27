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
package org.netbeans.modules.java.api.common.singlesourcefile;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Arunava Sinha
 */
@ServiceProvider(service = CompilerOptionsQueryImplementation.class, position = 100)
public class SingleSourceCompilerOptQueryImpl implements CompilerOptionsQueryImplementation {

    @Override
    public CompilerOptionsQueryImplementation.Result getOptions(FileObject file) {
        String fileName = file.getName();
        Result res = null;
        if (SingleSourceFileUtil.isSingleSourceFile(file)) {
            res = new ResultImpl(file);
        }
        return res;
    }

    private static final class ResultImpl extends CompilerOptionsQueryImplementation.Result implements PropertyChangeListener {

        private final ChangeSupport cs;
        private final FileObject file;

        ResultImpl(FileObject file) {

            this.cs = new ChangeSupport(this);
            this.file = file;
        }

        @Override
        public List<? extends String> getArguments() {

            Object vmOptionsObj = file.getAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS);
            return vmOptionsObj != null ? parseLine((String) vmOptionsObj) : new ArrayList<String>();
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            cs.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            cs.removeChangeListener(listener);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            cs.fireChange();
        }
    }
}
