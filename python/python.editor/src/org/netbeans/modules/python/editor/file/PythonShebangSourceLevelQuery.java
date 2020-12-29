/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.python.editor.file;

import org.netbeans.modules.python.source.queries.SourceLevelQueryImplementation;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = SourceLevelQueryImplementation.class)
public class PythonShebangSourceLevelQuery implements SourceLevelQueryImplementation {

    @Override
    public Result getSourceLevel(FileObject pythonFile) {
        if(!pythonFile.isFolder()) {
            return new ResultImpl(pythonFile);
        }
        return null;
    }

    private final static class ResultImpl implements Result, FileChangeListener {
        private final ChangeSupport cs = new ChangeSupport(this);

        private final FileObject pythonFile;
        private String sourceLevel = "";

        @SuppressWarnings("LeakingThisInConstructor")
        private ResultImpl(FileObject pythonFile) {
            this.pythonFile = pythonFile;
            this.pythonFile.addFileChangeListener(this);
            this.fileChanged(new FileEvent(pythonFile, pythonFile, true));
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            this.cs.addChangeListener(listener);
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        @Override
        public void fileChanged(FileEvent fe) {
            FileObject file = fe.getFile();
            if (file.isValid()) {
                FileLock lock;
                try {
                    lock = file.lock();
                } catch (FileAlreadyLockedException e) {
                    return;
                } catch (IOException ex) {
                    return;
                }
                String shebang = null;
                try {
                    try (Scanner sc = new Scanner(file.getInputStream())) {
                        if (sc.hasNextLine()) {
                            shebang = sc.nextLine();
                        }
                    } catch (FileNotFoundException ex) {
                        // ignore
                    }
                } finally {
                    lock.releaseLock();
                }
                processShebang(shebang);
            }
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
        }

        @Override
        public void fileDeleted(FileEvent fe) {
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
        }

        @Override
        public String getSourceLevel() {
            return this.sourceLevel;
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            this.cs.removeChangeListener(listener);
        }

        private void setSourceLevel(String sourceLevel) {
            this.sourceLevel = sourceLevel;
            cs.fireChange();
        }

        private void processShebang(String shebang) {
            if (shebang != null && shebang.startsWith("#!")) {
                try {
                    Process proc = Runtime.getRuntime().exec(shebang.substring(2) + " --version");
                    String version = null;
                    try(Scanner sc = new Scanner(proc.getInputStream())) {
                        if(sc.hasNextLine()) {
                            version = sc.nextLine();
                        }
                    }
                    proc.destroy();
                    if(version != null && !version.isEmpty() && !version.equals(this.sourceLevel)) {
                        setSourceLevel(version);
                    }
                } catch(IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
