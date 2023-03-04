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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.versioning.core.VersioningAnnotationProvider;
import org.netbeans.modules.versioning.core.VersioningManager;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSAnnotator;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.core.spi.VersioningSystem;
import org.openide.filesystems.*;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.test.MockLookup;

/**
 *
 * @author ondra
 */
public class VCSAnnotationProviderTestCase extends NbTestCase {

    private static VCSAnnotator annotator;
    private StatusListener statusListener;
    private String workDirPath;
    private static final BufferedImage IMAGE = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
    private static final BufferedImage IMAGE_ANNOTATION = new BufferedImage(6, 6, BufferedImage.TYPE_INT_ARGB);
    private FileObject workDirFO;

    public VCSAnnotationProviderTestCase(String arg) {
        super(arg);
    }

    @Override
    protected void setUp () throws IOException {
        MockLookup.setLayersAndInstances();
        
        File userdir = new File(getWorkDir() + "/userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        
        System.setProperty("versioning.asyncAnnotator", "true");
        workDirPath = getWorkDir().getParentFile().getName() + "/" + getWorkDir().getName();
        
        workDirFO = VCSFilesystemTestFactory.getInstance(this).createFolder(workDirPath);
        // cleanup the owner cache, this folder just became versioned 
        VersioningManager.getInstance().flushNullOwners(); 
        DummyVCS.topmostFile = VCSFileProxy.createFileProxy(workDirFO);
    }

    @RandomlyFails
    public void testAnnotationChanged () throws Exception {
        
        HashMap<FileObject, String> expectedLabelAnnotations = new HashMap<FileObject, String>();
        HashMap<FileObject, String> expectedIconToolTips = new HashMap<FileObject, String>();
        expectedLabelAnnotations.put(workDirFO, workDirFO.getNameExt() + " - annotated");
        expectedIconToolTips.put(workDirFO, workDirFO.getNameExt() + "<br>Annotated");
        FileObject f = workDirFO.createFolder("folder1");
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
        f = workDirFO.createFolder("folder2");
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

        statusListener = new VCSAnnotationProviderTestCase.StatusListener(expectedLabelAnnotations.keySet());
        annotator = new VCSAnnotationProviderTestCase.DummyVCSAnnotator();
        FileSystem fileSystem = (FileSystem) workDirFO.getFileSystem();
        fileSystem.addFileStatusListener(statusListener);
        statusListener.startAnnotation(expectedLabelAnnotations.keySet());
        Thread.sleep(500);
        // annotations should not be ready yet, test that
        for (Map.Entry<FileObject, String> e : expectedLabelAnnotations.entrySet()) {
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

        private long lastEvent;
        private Exception ex;
        private HashMap<FileObject, String> annotationsLabels = new HashMap<FileObject, String>();
        private HashMap<FileObject, Image> annotationsIcons = new HashMap<FileObject, Image>();
        private final Set<FileObject> allFiles;

        private StatusListener(Set<FileObject> keySet) {
            this.allFiles = keySet;
        }

        @Override
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
            ex = null;
            lastEvent = 0;
        }

        private void startAnnotation(final Set<FileObject> files) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    lastEvent = System.currentTimeMillis();
                    long time = System.currentTimeMillis();
                    for (FileObject fo : files) {
                        String name = fo.getNameExt();
                        name = VersioningAnnotationProvider.getDefault().annotateNameHtml(name, Collections.singleton(fo));
                        annotationsLabels.put(fo, name);
                        Image image = ImageUtilities.assignToolTipToImage(VCSAnnotationProviderTestCase.IMAGE, fo.getNameExt());
                        ImageUtilities.getImageToolTip(image);
                        image = VersioningAnnotationProvider.getDefault().annotateIcon(image, 0, Collections.singleton(fo));
                        annotationsIcons.put(fo, image);
                    }
                    time = System.currentTimeMillis() - time;
                    if (time > 500) {
                        ex = new Exception("Annotation takes more than 200ms");
                    }
                }     
            });
        }

        private void waitForSilence() throws Exception {
            while (System.currentTimeMillis() - lastEvent < 10000) {
                if (ex != null) {
                    throw ex;
                }
                Thread.sleep(1000);
            }
        }

    }

    @VersioningSystem.Registration(actionsCategory="dummy", displayName="DummyVCS", menuLabel="DummyVCS", metadataFolderNames="")
    public static class DummyVCS extends VersioningSystem {
        private static VCSFileProxy topmostFile;
        public DummyVCS () {
            
        }

        @Override
        public VCSFileProxy getTopmostManagedAncestor(VCSFileProxy file) {
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
            icon = ImageUtilities.mergeImages(icon, VCSAnnotationProviderTestCase.IMAGE_ANNOTATION, 16, 16);
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
