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
package org.netbeans.modules.javascript.grunt.file;

import java.io.File;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.clientproject.api.util.WatchedFile;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

@MIMEResolver.Registration(displayName = "Gruntfile", resource = "../resources/gruntfile-resolver.xml", position = 123)
public final class Gruntfile implements ChangeListener {

    public static final String FILE_NAME = "Gruntfile.js"; // NOI18N

    final WatchedFile gruntfile;
    private final ChangeSupport changeSupport = new ChangeSupport(this);


    private Gruntfile(FileObject directory) {
        assert directory != null;
        gruntfile = WatchedFile.create(FILE_NAME, directory);
    }

    public static Gruntfile create(FileObject directory) {
        Gruntfile gruntfile = new Gruntfile(directory);
        gruntfile.gruntfile.addChangeListener(WeakListeners.change(gruntfile, gruntfile.gruntfile));
        return gruntfile;
    }

    public boolean exists() {
        return gruntfile.exists();
    }

    public File getFile() {
        return gruntfile.getFile();
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }

}
