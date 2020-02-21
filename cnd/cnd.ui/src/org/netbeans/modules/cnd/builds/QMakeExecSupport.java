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

package org.netbeans.modules.cnd.builds;

import java.io.IOException;
import org.netbeans.modules.cnd.execution.ExecutionSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

public class QMakeExecSupport extends ExecutionSupport {

    // the property sheet where properties are shown
    private Sheet.Set sheetSet;
    private static final String PROP_QMAKE_COMMAND = "qmakeCommand"; // NOI18N
    private static final String PROP_RUN_DIRECTORY = "rundirectory"; // NOI18N
    private static final String PROP_ENVIRONMENT = "environment"; // NOI18N
    // The list of our properties
    private PropertySupport<String> qmakeRunDirectory;
    private PropertySupport<String> qmakeCommandProperty;
    private PropertySupport<String> qmakeEnvironmentProperty;

    public QMakeExecSupport(MultiDataObject.Entry entry) {
        super(entry);
    }

    public FileObject getFileObject() {
        return getEntry().getFile();
    }

    private void createProperties() {
        if (qmakeCommandProperty == null) {
            qmakeCommandProperty = createQMakeCommandProperty();
            qmakeRunDirectory = createRunDirectoryProperty();
            qmakeEnvironmentProperty = createEnvironmentProperty(PROP_ENVIRONMENT, getString("PROP_QMAKE_ENVIRONMENT"), getString("HINT_QMAKE_ENVIRONMENT")); // NOI18N
        }
    }

    @Override
    public void addProperties(Sheet.Set set) {
        createProperties();
        this.sheetSet = set;
        set.put(createParamsProperty(PROP_FILE_PARAMS, getString("PROP_QMAKE_PARAMS"), getString("HINT_QMAKE_PARAMS"))); // NOI18N);
        set.put(qmakeRunDirectory);
        set.put(qmakeCommandProperty);
        set.put(qmakeEnvironmentProperty);
    }

    private PropertySupport<String> createQMakeCommandProperty() {
         PropertySupport<String> result = new PropertySupport.ReadWrite<String>(PROP_QMAKE_COMMAND, String.class,
                getString("PROP_QMAKE_COMMAND"), getString("HINT_QMAKE_COMMAND")) { // NOI18N
            @Override
            public String getValue() {
                return getQMakeCommand();
            }
            @Override
            public void setValue(String val) {
                setQMakeCommand(val);
            }
            @Override public boolean supportsDefaultValue() {
                return true;
            }
            @Override public void restoreDefaultValue() {
                setValue(null);
            }
            @Override public boolean canWrite() {
                return getEntry().getFile().getParent().canWrite();
            }
        };
        //String editor hint to use a JTextField, not a JTextArea for the
        //custom editor.  Arguments can't be multiline anyway.
        result.setValue("oneline", Boolean.TRUE);// NOI18N
        return result;
    }

    public String getQMakeCommand() {
        String make = (String) getEntry().getFile().getAttribute(PROP_QMAKE_COMMAND);
        if (make == null || make.equals("")) { // NOI18N
            make = "qmake";// NOI18N
            setQMakeCommand(make);
        }
        return make;
    }

    public void setQMakeCommand(String make) {
        try {
            getEntry().getFile().setAttribute(PROP_QMAKE_COMMAND, make);
        } catch (IOException ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                ex.printStackTrace();
            }
        }
    }

    private PropertySupport<String> createRunDirectoryProperty() {
        PropertySupport<String> result = new PropertySupport.ReadWrite<String>(PROP_RUN_DIRECTORY, String.class,
                getString("PROP_RUN_QMAKE_DIRECTORY"), getString("HINT_RUN_QMAKE_DIRECTORY")) { // NOI18N
            @Override
            public String getValue() {
                return getRunDirectory();
            }
            @Override
            public void setValue(String val) {
                setRunDirectory(val);
            }
            @Override public boolean supportsDefaultValue() {
                return true;
            }
            @Override public void restoreDefaultValue() {
                setValue(null);
            }
            @Override public boolean canWrite() {
                return getEntry().getFile().getParent().canWrite();
            }
        };
        //String editor hint to use a JTextField, not a JTextArea for the
        //custom editor.  Arguments can't be multiline anyway.
        result.setValue("oneline", Boolean.TRUE);// NOI18N
        return result;
    }

    public String getRunDirectory() {
        String dir = (String) getEntry().getFile().getAttribute(PROP_RUN_DIRECTORY);
        if (dir == null) {
            dir = "."; // NOI18N
            setRunDirectory(dir);
        }
        return dir;
    }

    public void setRunDirectory(String dir) {
        FileObject fo = getEntry().getFile();
        try {
            fo.setAttribute(PROP_RUN_DIRECTORY, dir);
        } catch (IOException ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                ex.printStackTrace();
            }
        }
    }

    private static String getString(String key){
        return NbBundle.getBundle(QMakeExecSupport.class).getString(key);
    }
}
