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
package org.netbeans.modules.maven.hints.pom;

import java.util.Collection;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;
import org.openide.util.WeakListeners;

/**
 * Parses POM files.
 *
 * @author sdedic
 */
public class MavenPOMParser extends Parser implements PreferenceChangeListener {
    private final ChangeSupport chs = new ChangeSupport(this);
    private POMModel    theModel;
    private Snapshot    lastSnapshot;

    public MavenPOMParser() {
        Preferences prefs = NbPreferences.root().node("org/netbeans/modules/maven");
        prefs.addPreferenceChangeListener(
                WeakListeners.create(PreferenceChangeListener.class, this, prefs));
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if(EmbedderFactory.PROP_COMMANDLINE_PATH.equals(evt.getKey())) {
            chs.fireChange();
        }
    }
    
    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        final FileObject sFile = snapshot.getSource().getFileObject();
        if (sFile == null) {
            synchronized (this) {
                theModel = null;
                lastSnapshot = snapshot;
            }
            return;
        }
        //#236116 passing document protects from looking it up later and causing a deadlock.
        final BaseDocument document = (BaseDocument)snapshot.getSource().getDocument(false);
        final DataObject d = sFile.getLookup().lookup(DataObject.class);
        ModelSource ms = Utilities.createModelSource(sFile, d, document);
        synchronized (this) {
            theModel = POMModelFactory.getDefault().getModel(ms);
            lastSnapshot = snapshot;
        }
    }

    @Override
    public Result getResult(Task task) throws ParseException {
        synchronized (this) {
            if (lastSnapshot == null) {
                return null;
            }
            return new MavenResult(theModel, lastSnapshot.getSource().getFileObject(), lastSnapshot);
        }
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        chs.addChangeListener(changeListener);
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        chs.removeChangeListener(changeListener);
    }

    
    @MimeRegistration(mimeType = "text/x-maven-pom+xml", service = ParserFactory.class)
    public static class F extends ParserFactory {

        @Override
        public Parser createParser(Collection<Snapshot> snapshots) {
            return new MavenPOMParser();
        }
    }
    
}
