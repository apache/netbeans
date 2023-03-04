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

package org.netbeans.modules.maven.grammar;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.TopComponent;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Messages("CTL_SourceTabCaption=&Source")
public class POMDataObject extends MultiDataObject {

    public static final String SETTINGS_MIME_TYPE = "text/x-maven-settings+xml";

    static final @StaticResource String POM_ICON = "org/netbeans/modules/maven/grammar/Maven2Icon.gif";

    private static final Logger LOG = Logger.getLogger(POMDataObject.class.getName());

    public POMDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        cookies.add(new POMDataEditor());
        cookies.add(new ValidateXMLSupport(DataObjectAdapters.inputSource(this)));
    }

    @MultiViewElement.Registration(
        displayName="#CTL_SourceTabCaption",
        iconBase=POM_ICON,
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="maven.pom",
        mimeType=Constants.POM_MIME_TYPE,
        position=1
    )
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    @MultiViewElement.Registration(
        displayName="#CTL_SourceTabCaption",
        iconBase=POM_ICON,
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="xml.text",
        mimeType=SETTINGS_MIME_TYPE,
        position=1
    )
    public static MultiViewEditorElement createSettingsMultiViewEditorElement(Lookup context) { // #202900 workaround
        return new MultiViewEditorElement(context);
    }

    @Override protected int associateLookup() {
        return 1;
    }

    private class POMDataEditor extends DataEditorSupport implements EditorCookie.Observable, OpenCookie, EditCookie, PrintCookie, CloseCookie {

        private String cachedTitleSuffix = null;
        private final Object TITLE_LOCK = new Object();
        private final SaveCookie save = new SaveCookie() {
            public @Override void save() throws IOException {
                saveDocument();
            }
            @Override public String toString() {
                return getPrimaryFile().getNameExt();
            }
        };

        private final FileChangeListener listener = new FileChangeAdapter() {
            public @Override void fileChanged(FileEvent fe) {
                synchronized (TITLE_LOCK) {
                    cachedTitleSuffix = null;
                }
                updateTitles();
            }
        };

        POMDataEditor() {
            super(POMDataObject.this, null, new POMEnv(POMDataObject.this));
            getPrimaryFile().addFileChangeListener(FileUtil.weakFileChangeListener(listener, getPrimaryFile()));
        }

        @Override protected Pane createPane() {
            return (CloneableEditorSupport.Pane) MultiViews.createCloneableMultiView(Constants.POM_MIME_TYPE, getDataObject());
        }

        protected @Override boolean notifyModified() {
            if (!super.notifyModified()) {
                return false;
            }
            if (getLookup().lookup(SaveCookie.class) == null) {
                getCookieSet().add(save);
                setModified(true);
            }
            return true;
        }

        protected @Override void notifyUnmodified() {
            super.notifyUnmodified();
            if (getLookup().lookup(SaveCookie.class) == save) {
                getCookieSet().remove(save);
                setModified(false);
            }
        }

        protected @Override String messageName() {
            String titleSuffix = null;
            synchronized (TITLE_LOCK) {
                if (cachedTitleSuffix == null) {
                    cachedTitleSuffix = annotateWithProjectName(getPrimaryFile());
                }
                titleSuffix = cachedTitleSuffix;
            }
            return super.messageName() + titleSuffix;
        }

        protected @Override String messageHtmlName() {
            String titleSuffix = null;
            synchronized (TITLE_LOCK) {
                if (cachedTitleSuffix == null) {
                    cachedTitleSuffix = annotateWithProjectName(getPrimaryFile());
                }
                titleSuffix = cachedTitleSuffix;
            }
            return super.messageHtmlName() + titleSuffix;
        }


        protected @Override boolean asynchronousOpen() {
            return true;
        }

        // XXX override initializeCloneableEditor if needed; see AntProjectDataEditor

    }
    
        static String annotateWithProjectName(FileObject primaryFile) { // #154508
            if (primaryFile.getNameExt().equals("pom.xml")) { // NOI18N
                try {
                    //TODO faster and less memory intensive to have just FileObject().asText()-> regexp?
                    Element artifactId = XMLUtil.findElement(XMLUtil.parse(new InputSource(primaryFile.toURL().toString()), false, false, XMLUtil.defaultErrorHandler(), null).getDocumentElement(), "artifactId", null); // NOI18N
                    if (artifactId != null) {
                        String text = XMLUtil.findText(artifactId);
                        if (text != null) {
                            return " [" + text + "]"; // NOI18N
                        }
                    }
                } catch (IOException x) {
                    LOG.log(Level.INFO, null, x);
                } catch (IllegalArgumentException x) { // #193630
                    LOG.log(Level.FINE, null, x);
                } catch (SAXException x) {
                    LOG.log(Level.FINE, null, x);
                }
            }
            return "";
        }
    

    private static class POMEnv extends DataEditorSupport.Env {

        private static final long serialVersionUID = 1L;

        POMEnv(MultiDataObject d) {
            super(d);
        }

        protected @Override FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        protected @Override FileLock takeLock() throws IOException {
            return ((MultiDataObject) getDataObject()).getPrimaryEntry().takeLock();
        }

        public @Override CloneableOpenSupport findCloneableOpenSupport() {
            return getDataObject().getLookup().lookup(POMDataEditor.class);
        }

    }

}
