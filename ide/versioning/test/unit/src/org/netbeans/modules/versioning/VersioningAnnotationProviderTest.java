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

package org.netbeans.modules.versioning;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.masterfs.providers.BaseAnnotationProvider;
import org.netbeans.modules.versioning.masterfs.VersioningAnnotationProvider;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ondra
 */
public class VersioningAnnotationProviderTest extends NbTestCase {

    private static VCSAnnotator annotator;
    private StatusListener statusListener;
    private FileObject workDir;
    private static final BufferedImage IMAGE = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
    private static final BufferedImage IMAGE_ANNOTATION = new BufferedImage(6, 6, BufferedImage.TYPE_INT_ARGB);

    public VersioningAnnotationProviderTest(String arg) {
        super(arg);
    }

    protected void setUp () throws IOException {
        File userdir = new File(getWorkDir() + "/userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        System.setProperty("versioning.asyncAnnotator", "true");
        File wDir = new File(getWorkDir(), String.valueOf(System.currentTimeMillis()));
        wDir.mkdirs();
        workDir = FileUtil.toFileObject(wDir);
    }

    public void testProviderPresent () {
        Collection<? extends BaseAnnotationProvider> providers = Lookup.getDefault().lookupAll(BaseAnnotationProvider.class);
        for (BaseAnnotationProvider provider : providers) {
            if (provider.getClass().getName().equals(VersioningAnnotationProvider.class.getName())) {
                return;
            }
        }
        assert false;
    }

    @RandomlyFails // NB-Core-Build #8151: expected:<1334355608338[ - annotated]> but was:<1334355608338[]>
    public void testAnnotationChanged () throws Exception {
        Lookup.getDefault().lookup(DummyVCS.class).topmostFile = FileUtil.toFile(workDir);
        HashMap<FileObject, String> expectedLabelAnnotations = new HashMap<FileObject, String>();
        HashMap<FileObject, String> expectedIconToolTips = new HashMap<FileObject, String>();
        expectedLabelAnnotations.put(workDir, workDir.getNameExt() + " - annotated");
        expectedIconToolTips.put(workDir, workDir.getNameExt() + "<br>Annotated");
        FileObject f = workDir.createFolder("folder1");
        expectedLabelAnnotations.put(f, f.getNameExt() + " - annotated");
        expectedIconToolTips.put(f, f.getNameExt() + "<br>Annotated");
        f = f.createFolder("folder1_1");
        expectedLabelAnnotations.put(f, f.getNameExt() + " - annotated");
        expectedIconToolTips.put(f, f.getNameExt() + "<br>Annotated");
        FileObject file = f.createData("file1", "txt");
        expectedLabelAnnotations.put(file, file.getNameExt() + " - annotated");
        expectedIconToolTips.put(file, file.getNameExt() + "<br>Annotated");
        file = f.createData("file2", "txt");
        expectedLabelAnnotations.put(file, file.getNameExt() + " - annotated");
        expectedIconToolTips.put(file, file.getNameExt() + "<br>Annotated");
        f = workDir.createFolder("folder2");
        expectedLabelAnnotations.put(f, f.getNameExt() + " - annotated");
        expectedIconToolTips.put(f, f.getNameExt() + "<br>Annotated");
        f = f.createFolder("folder2_1");
        expectedLabelAnnotations.put(f, f.getNameExt() + " - annotated");
        expectedIconToolTips.put(f, f.getNameExt() + "<br>Annotated");
        file = f.createData("file1", "txt");
        expectedLabelAnnotations.put(file, file.getNameExt() + " - annotated");
        expectedIconToolTips.put(file, file.getNameExt() + "<br>Annotated");
        file = f.createData("file2", "txt");
        expectedLabelAnnotations.put(file, file.getNameExt() + " - annotated");
        expectedIconToolTips.put(file, file.getNameExt() + "<br>Annotated");

        statusListener = new VersioningAnnotationProviderTest.StatusListener(expectedLabelAnnotations.keySet());
        annotator = new VersioningAnnotationProviderTest.DummyVCSAnnotator();
        FileSystem fileSystem = (FileSystem) workDir.getFileSystem();
        fileSystem.addFileStatusListener(statusListener);
        statusListener.startAnnotation(expectedLabelAnnotations.keySet());
        // annotations should not be ready yet, test that
        for (Map.Entry<FileObject, String> e : expectedLabelAnnotations.entrySet()) {
            while (!statusListener.annotationsIcons.containsKey(e.getKey())) {
                Thread.sleep(100);
            }
            assertEquals(e.getKey().getNameExt(), statusListener.annotationsLabels.get(e.getKey()));
            Image annotatedIcon = statusListener.annotationsIcons.get(e.getKey());
            assertTrue(10 == annotatedIcon.getWidth(null));
            assertTrue(10 == annotatedIcon.getHeight(null));
            assertEquals(e.getKey().getNameExt(), ImageUtilities.getImageToolTip(annotatedIcon));
        }
        
        statusListener.waitForSilence();
        // annotations should be ready
        for (Map.Entry<FileObject, String> e : expectedLabelAnnotations.entrySet()) {
            assertEquals(e.getValue(), statusListener.annotationsLabels.get(e.getKey()));
        }
        for (Map.Entry<FileObject, String> e : expectedIconToolTips.entrySet()) {
            Image annotatedIcon = statusListener.annotationsIcons.get(e.getKey());
            assertTrue(22 == annotatedIcon.getWidth(null));
            assertTrue(22 == annotatedIcon.getHeight(null));
            assertEquals(e.getValue(), ImageUtilities.getImageToolTip(annotatedIcon));
        }

        statusListener.clear();
        statusListener.startAnnotation(expectedLabelAnnotations.keySet());
        Thread.sleep(500);
        // annotations should be already cached in Versioning AP and should be immediately returned
        for (Map.Entry<FileObject, String> e : expectedLabelAnnotations.entrySet()) {
            assertEquals(e.getValue(), statusListener.annotationsLabels.get(e.getKey()));
        }
        for (Map.Entry<FileObject, String> e : expectedIconToolTips.entrySet()) {
            Image annotatedIcon = statusListener.annotationsIcons.get(e.getKey());
            assertTrue(22 == annotatedIcon.getWidth(null));
            assertTrue(22 == annotatedIcon.getHeight(null));
            assertEquals(e.getValue(), ImageUtilities.getImageToolTip(annotatedIcon));
        }
    }

    private class StatusListener implements FileStatusListener {

        private volatile long lastEvent;
        private volatile boolean refreshDone = true;
        private HashMap<FileObject, String> annotationsLabels = new HashMap<FileObject, String>();
        private HashMap<FileObject, Image> annotationsIcons = new HashMap<FileObject, Image>();
        private final Set<FileObject> allFiles;

        private StatusListener(Set<FileObject> keySet) {
            this.allFiles = keySet;
        }

        public void annotationChanged(FileStatusEvent ev) {
            HashSet<FileObject> fos = new HashSet<FileObject>();
            for (FileObject fo : allFiles) {
                if (ev.hasChanged(fo)) {
                    fos.add(fo);
                }
            }
            startAnnotation(fos);
        }

        private void clear () {
            annotationsLabels.clear();
            annotationsIcons.clear();
            lastEvent = 0;
        }

        private void startAnnotation(final Set<FileObject> files) {
            lastEvent = 0;
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    org.netbeans.modules.versioning.core.VersioningAnnotationProvider provider = org.netbeans.modules.versioning.core.VersioningAnnotationProvider.getDefault();
                    refreshDone = false;
                    lastEvent = System.currentTimeMillis();
                    for (FileObject fo : files) {
                        lastEvent = System.currentTimeMillis();
                        String name = fo.getNameExt();
                        name = provider.annotateNameHtml(name, Collections.singleton(fo));
                        annotationsLabels.put(fo, name);
                        Image image = ImageUtilities.assignToolTipToImage(VersioningAnnotationProviderTest.IMAGE, fo.getNameExt());
                        ImageUtilities.getImageToolTip(image);
                        image = provider.annotateIcon(image, 0, Collections.singleton(fo));
                        annotationsIcons.put(fo, image);
                    }
                    refreshDone = true;
                }
            });
        }

        private void waitForSilence() throws Exception {
            while (lastEvent == 0 || System.currentTimeMillis() - lastEvent < 10000 || !refreshDone) {
                Thread.sleep(1000);
            }
        }

    }

    @org.openide.util.lookup.ServiceProviders ({ @ServiceProvider(service=VersioningSystem.class),
        @ServiceProvider(service=DummyVCS.class)})
    public static class DummyVCS extends VersioningSystem {
        private File topmostFile;
        public DummyVCS () {
            
        }

        @Override
        public File getTopmostManagedAncestor(File file) {
            return topmostFile;
        }

        @Override
        public VCSAnnotator getVCSAnnotator() {
            return annotator;
        }
    }

    private class DummyVCSAnnotator extends VCSAnnotator {

        @Override
        public Image annotateIcon(Image icon, VCSContext context) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            icon = ImageUtilities.mergeImages(icon, VersioningAnnotationProviderTest.IMAGE_ANNOTATION, 16, 16);
            icon = ImageUtilities.addToolTipToImage(icon, "Annotated");
            return icon;
        }

        @Override
        public String annotateName(String name, VCSContext context) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            return name + " - annotated";
        }
    }
}
