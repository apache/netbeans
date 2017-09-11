/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.indent.project;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author vita
 */
@ServiceProvider(service=CodeStylePreferences.Provider.class)
public final class ProjectAwareCodeStylePreferences implements CodeStylePreferences.Provider {

    @Override
    public Preferences forFile(FileObject file, String mimeType) {
        return singleton.forFile(file, mimeType);
    }

    @Override
    public Preferences forDocument(Document doc, String mimeType) {
        return singleton.forDocument(doc, mimeType);
    }

    // ----------------------------------------------------------------------
    // private implementation
    // ----------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(ProjectAwareCodeStylePreferences.class.getName());
    private static final RequestProcessor RP = new RequestProcessor("Project Aware Code Style Preferences", 1, false, false); //NOI18N
    private static final CodeStylePreferences.Provider singleton = new CodeStylePreferences.Provider() {

        @Override
        public Preferences forFile(FileObject file, String mimeType) {
            return getCsp(file, mimeType).getPreferences();
        }

        @Override
        public Preferences forDocument(Document doc, String mimeType) {
            return getCsp(doc, mimeType).getPreferences();
        }

        // --------------------------------------------------------------------
        // private implementation
        // --------------------------------------------------------------------

        private final Map<Object, Reference<Map<String, Csp>>> cache = new WeakHashMap<Object, Reference<Map<String, Csp>>>();

        private Csp getCsp(final Object obj, final String mimeType) {
            Csp csp;
            FileObject file = null;
            synchronized (cache) {
                Reference<Map<String, Csp>> cspsRef = cache.get(obj);
                Map<String, Csp> csps = cspsRef != null ? cspsRef.get() : null;
                csp = csps != null ? csps.get(mimeType) : null;
                if (csp == null) {
                    Document doc;

                    if (obj instanceof FileObject) {
                        doc = null;
                        file = (FileObject) obj;
                    } else {
                        doc = (Document) obj;
                        file = findFileObject(doc);
                    }

                    csp = new Csp(
                            mimeType,
                            doc == null ? null : new CleaningWeakReference(doc),
                            file);
                    if (csps == null) {
                        csps = new HashMap<String, Csp>();
                        cache.put(obj, new SoftReference<Map<String, Csp>>(csps));
                    }
                    csps.put(mimeType, csp);
                }
            }
            
            if (file != null) {
                if (SwingUtilities.isEventDispatchThread()) {
                    final FileObject ffo = file;
                    final Csp fcsp = csp;
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            fcsp.initProjectPreferences(ffo);
                        }
                    });
                } else {
                    csp.initProjectPreferences(file);
                }
            }
            
            return csp;
        }

        final class CleaningWeakReference extends WeakReference<Document> implements Runnable {

            public CleaningWeakReference(Document referent) {
                super(referent, Utilities.activeReferenceQueue());
            }

            public @Override void run() {
                synchronized (cache) {
                    //expunge stale entries from the cache:
                    cache.size();
                }
            }

        }
    };

    private static final class Csp {

        private static final String NODE_CODE_STYLE = "CodeStyle"; //NOI18N
        private static final String PROP_USED_PROFILE = "usedProfile"; // NOI18N
        private static final String DEFAULT_PROFILE = "default"; // NOI18N
        private static final String PROJECT_PROFILE = "project"; // NOI18N

        private final String mimeType;
        private final Reference<Document> refDoc;
        private final String filePath;

        private final Preferences globalPrefs;
        private Preferences projectPrefs;
        private boolean useProject;

        private final PreferenceChangeListener switchTrakcer = new PreferenceChangeListener() {
            public @Override void preferenceChange(PreferenceChangeEvent evt) {
                if (evt.getKey() == null || PROP_USED_PROFILE.equals(evt.getKey())) {
                    synchronized (Csp.this) {
                        useProject = PROJECT_PROFILE.equals(evt.getNewValue());
                        LOG.fine("file '" + filePath + "' (" + mimeType + ") is using " + (useProject ? "project" : "global") + " Preferences"); //NOI18N
                    }
                }
            }
        };

        public Csp(String mimeType, Reference<Document> refDoc, final FileObject file) {
            this.mimeType = mimeType;
            this.refDoc = refDoc;
            this.filePath = file == null ? "no file" : file.getPath(); //NOI18N just for logging

            this.globalPrefs = MimeLookup.getLookup(mimeType == null ? MimePath.EMPTY : MimePath.parse(mimeType)).lookup(Preferences.class);
            this.projectPrefs = null;
            this.useProject = false;
        }
        
        public Preferences getPreferences() {
            synchronized (this) {
                Preferences prefs = useProject ? projectPrefs : globalPrefs;
                // to support tests that don't use editor.mimelookup.impl
                return prefs == null ? AbstractPreferences.systemRoot() : prefs;
            }
        }
        
        private void initProjectPreferences(final FileObject file) {
            ProjectManager.mutex().postReadRequest(new Runnable() {
                public @Override void run() {
                    Preferences projectRoot = findProjectPreferences(file);
                    if (projectRoot != null) {
                        synchronized (Csp.this) {
                            Preferences allLangCodeStyle = projectRoot.node(NODE_CODE_STYLE);
                            Preferences p = allLangCodeStyle.node(PROJECT_PROFILE);

                            // determine if we are using code style preferences from the project
                            String usedProfile = allLangCodeStyle.get(PROP_USED_PROFILE, DEFAULT_PROFILE);
                            useProject = PROJECT_PROFILE.equals(usedProfile);
                            projectPrefs = Csp.this.mimeType == null ?
                                p :
                                new ProxyPreferences(projectRoot.node(Csp.this.mimeType).node(NODE_CODE_STYLE).node(PROJECT_PROFILE), p);

                            // listen on changes
                            allLangCodeStyle.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, switchTrakcer, allLangCodeStyle));
                        }
                    }
                }
            });
            LOG.fine("file '" + filePath + "' (" + mimeType + ") is using " + (useProject ? "project" : "global") + " Preferences; doc=" + s2s(refDoc == null ? null : refDoc.get())); //NOI18N
        }
    } // End of Csp class
    
    private static Preferences findProjectPreferences(FileObject file) {
        if (file != null) {
            Project p = FileOwnerQuery.getOwner(file);
            if (p != null) {
                return ProjectUtils.getPreferences(p, IndentUtils.class, true);
            }
        }
        return null;
    }

    private static FileObject findFileObject(Document doc) {
        if (doc != null) {
            Object sdp = doc.getProperty(Document.StreamDescriptionProperty);
            if (sdp instanceof DataObject) {
                return ((DataObject) sdp).getPrimaryFile();
            } else if (sdp instanceof FileObject) {
                return (FileObject) sdp;
            }
        }
        return null;
    }

    private static String s2s(Object o) {
        return o == null ? "null" : o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o)); //NOI18N
    }
}
