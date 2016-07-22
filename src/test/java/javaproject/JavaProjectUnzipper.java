package javaproject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import org.black.kotlin.utils.ProjectUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Alexander.Baratynski
 */
public class JavaProjectUnzipper extends NbTestCase {
    
    public static JavaProjectUnzipper INSTANCE = new JavaProjectUnzipper("Project unzipper");
    
    private JavaProjectUnzipper(String name) {
        super(name);
    }
    
    private void unZipFile(File archiveFile, FileObject destDir) throws IOException {
        FileInputStream fis = new FileInputStream(archiveFile);
        try {
            ZipInputStream str = new ZipInputStream(fis);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(destDir, entry.getName());
                } else {
                    FileObject fo = FileUtil.createData(destDir, entry.getName());
                    FileLock lock = fo.lock();
                    try {
                        OutputStream out = fo.getOutputStream(lock);
                        try {
                            FileUtil.copy(str, out);
                        } finally {
                            out.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                }
            }
        } finally {
            fis.close();
        }

    }
    
    private FileObject getUnzippedFolder(String path) throws IOException {
        assertNotNull(path);
        File archiveFile = new File(path);
        
        FileObject destFileObject = FileUtil.toFileObject(getWorkDir());
        unZipFile(archiveFile, destFileObject);
        assertTrue(destFileObject.isValid());
        
        String folderName = null;
        String[] pathParts = path.split("\\\\");
        folderName = pathParts[pathParts.length-1].replace(".zip", "");
        
        FileObject unzippedFolder = destFileObject.getFileObject(folderName);
        assertNotNull(unzippedFolder);
        
        return unzippedFolder;
    }
    
    private void getKtHomeForTests() throws IOException {
        FileObject kotlincFolder = getUnzippedFolder(".\\src\\main\\resources\\org\\black\\kotlin\\kotlinc\\kotlinc.zip");
        
        ProjectUtils.KT_HOME = kotlincFolder.getPath() + ProjectUtils.FILE_SEPARATOR;
    }
    
    public FileObject getProjectFolder() throws IOException {
        getKtHomeForTests();
        FileObject testApp = getUnzippedFolder(".\\src\\test\\resources\\projForTest.zip");

        return testApp;
    }
}
