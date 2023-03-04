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
package org.netbeans.modules.web.clientproject.sites;

import org.netbeans.modules.web.clientproject.api.sites.SiteHelper;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.web.clientproject.ClientSideProjectConstants;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation;
import org.netbeans.modules.web.clientproject.util.FileUtilities;
import org.netbeans.modules.web.clientproject.util.FileUtilities.ZipEntryFilter;
import org.netbeans.modules.web.clientproject.util.FileUtilities.ZipEntryTask;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.EditableProperties;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Special {@link SiteTemplateImplementation} (not registered in SFS).
 */
public class SiteZip implements SiteTemplateImplementation {

    static final Logger LOGGER = Logger.getLogger(SiteZip.class.getName());

    private static final String USED_TEMPLATES = "last.templates"; //NOI18N
    private static final String SEPARATOR = "=s e p="; //NOI18N
    private static final ZipEntryFilter NB_TEMPLATE_FILTER = new ZipEntryFilter() {
        @Override
        public boolean accept(ZipEntry zipEntry) {
            return !zipEntry.isDirectory()
                    && zipEntry.getName().equals(ClientSideProjectConstants.TEMPLATE_DESCRIPTOR);
        }
    };

    private Customizer cust;

    @Override
    public String getId() {
        return "ARCHIVE"; // NOI18N
    }

    @NbBundle.Messages("SiteZip.name=Archive File")
    @Override
    public String getName() {
        return Bundle.SiteZip_name();
    }

    @Override
    public String getDescription() {
        return getName();
    }

    public Customizer getCustomizer() {
        cust = new Customizer();
        return cust;
    }

    @Override
    public boolean isPrepared() {
        return getArchiveFile().isFile();
    }

    @Override
    public void prepare() throws IOException {
        assert !EventQueue.isDispatchThread();
        assert !isPrepared();
        String template = cust.panel.getTemplate();
        assert isRemoteUrl(template) : "Remote URL expected: " + template; //NOI18N
        SiteHelper.download(template, getArchiveFile(), null); // NOI18N
    }

    @Override
    public void configure(final ProjectProperties projectProperties) {
        assert !EventQueue.isDispatchThread();
        assert isPrepared();
        try {
            FileUtilities.runOnZipEntries(getArchiveFile(), new ZipEntryTask() {
                @Override
                public void run(ZipEntry zipEntry) {
                    // noop
                }
                @Override
                public void run(InputStream zipEntryInputStream) {
                    EditableProperties templateProperties = new EditableProperties(false);
                    try {
                        templateProperties.load(zipEntryInputStream);
                        projectProperties.setSiteRootFolder(templateProperties.getProperty(ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER))
                                .setSourceFolder(templateProperties.getProperty(ClientSideProjectConstants.PROJECT_SOURCE_FOLDER))
                                .setTestFolder(templateProperties.getProperty(ClientSideProjectConstants.PROJECT_TEST_FOLDER));
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Error while reading file", ex);
                    }
                }
            }, NB_TEMPLATE_FILTER);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Error while reading zip file", ex);
        }
    }

    @Override
    public void apply(FileObject projectDir, ProjectProperties projectProperties, ProgressHandle handle) throws IOException {
        assert !EventQueue.isDispatchThread();
        if (!isPrepared()) {
            // not correctly prepared, user has to know about it already
            LOGGER.info("Template not correctly prepared, nothing to be applied"); //NOI18N
            return;
        }
        SiteHelper.unzipProjectTemplate(getTargetDir(projectDir, projectProperties), getArchiveFile(), handle, ClientSideProjectConstants.TEMPLATE_DESCRIPTOR);
        registerTemplate(cust.panel.getTemplate());
    }

    /**
     * Return project dir for NB template, site root otherwise.
     */
    private FileObject getTargetDir(FileObject projectDir, ProjectProperties projectProperties) throws IOException {
        if (FileUtilities.listZipFiles(getArchiveFile(), NB_TEMPLATE_FILTER).isEmpty()) {
            // not nb template
            String siteRootFolder = projectProperties.getSiteRootFolder();
            assert siteRootFolder != null;
            return projectDir.getFileObject(siteRootFolder);
        }
        return projectDir;
    }

    @Override
    public void cleanup() {
        String template = cust.panel.getTemplate();
        if (isRemoteUrl(template)) {
            // noop
            return;
        }
        File archiveFile = getArchiveFile();
        if (archiveFile.isFile()) {
            if (!archiveFile.delete()) {
                archiveFile.deleteOnExit();
            }
        }
    }

    private File getArchiveFile() {
        String template = cust.panel.getTemplate();
        if (!isRemoteUrl(template)) {
            return new File(template);
        }
        // remote file => calculate hash of its url
        CRC32 crc = new CRC32();
        crc.update(template.getBytes(StandardCharsets.UTF_8));
        String filename = String.valueOf(crc.getValue()) + ".zip"; // NOI18N
        LOGGER.log(Level.INFO, "Remote URL \"{0}\" set, downloaded to {1}", new Object[] {template, filename}); //NOI18N
        return new File(SiteHelper.getJsLibsDirectory(), filename);
    }

    private boolean isRemoteUrl(String input) {
        return input.toLowerCase().startsWith("http"); // NOI18N
    }

    public static void registerTemplate(File f) {
        String name = f.getAbsolutePath();
        registerTemplate(name);
    }

    public static void registerTemplate(String name) {
        String templates = NbPreferences.forModule(SiteZip.class).get(USED_TEMPLATES, ""); //NOI18N
        templates = name + SEPARATOR + templates.replace(name+SEPARATOR, ""); //NOI18N
        NbPreferences.forModule(SiteZip.class).put(USED_TEMPLATES, templates);
    }

    public static List<String> getUsedTemplates() {
        String templates = NbPreferences.forModule(SiteZip.class).get(USED_TEMPLATES, ""); //NOI18N
        return Arrays.asList(templates.split(SEPARATOR));
    }

    //~ Inner classes

    public static class Customizer {

        private SiteZipPanel panel = new SiteZipPanel(this);
        private ChangeSupport sup = new ChangeSupport(this);
        private String error = ""; //NOI18N

        public void addChangeListener(ChangeListener listener) {
            sup.addChangeListener(listener);
        }

        public void removeChangeListener(ChangeListener listener) {
            sup.removeChangeListener(listener);
        }

        public JComponent getComponent() {
            return panel;
        }

        @NbBundle.Messages({
            "SiteZip.error.template.missing=Template file must be specified.",
            "SiteZip.error.template.invalid=Template file is invalid (http://... or local file expected).",
            "SiteZip.error.template.notZip=Template file must be ZIP archive (*.zip)."
        })
        public boolean isValid() {
            String tpl = panel.getTemplate();
            if (tpl.isEmpty()) {
                error = Bundle.SiteZip_error_template_missing();
                return false;
            }
            File localTpl = new File(tpl);
            if (!tpl.startsWith("http")  && !localTpl.isFile()) { //NOI18N
                error = Bundle.SiteZip_error_template_invalid();
                return false;
            }
            if (localTpl.isFile()
                    && !localTpl.getName().endsWith(".zip")) { // NOI18N
                error = Bundle.SiteZip_error_template_notZip();
                return false;
            }
            error = null;
            return true;
        }

        public String getErrorMessage() {
            return error;
        }

        public String getWarningMessage() {
            return null;
        }

        void fireChange() {
            sup.fireChange();
        }

    }

}
