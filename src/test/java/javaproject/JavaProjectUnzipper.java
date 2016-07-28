package javaproject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.black.kotlin.utils.ProjectUtils;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 *
 * @author Alexander.Baratynski
 */
public class JavaProjectUnzipper extends NbTestCase {
    
    public static JavaProjectUnzipper INSTANCE = new JavaProjectUnzipper("ProjectUnzipper");
    
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
    
    public FileObject getTestProject() throws IOException {
        getKtHomeForTests();
        File projectFile = new File(".\\src\\test\\resources\\projForTest");
        FileObject projectFileObject = FileUtil.toFileObject(FileUtil.normalizeFile(projectFile));
        FileObject destFileObject = FileUtil.toFileObject(getWorkDir());
        
        FileObject testProject = destFileObject.getFileObject("projForTest");
        if (testProject != null) {
            testProject.delete();
        }
        
        testProject = FileUtil.copyFile(projectFileObject, destFileObject, "projForTest");
        
        return testProject;
    }
    
    private void getKtHomeForTests() throws IOException {
        FileObject kotlincFolder = getUnzippedFolder(".\\src\\main\\resources\\org\\black\\kotlin\\kotlinc\\kotlinc.zip");
        
        ProjectUtils.KT_HOME = kotlincFolder.getPath() + ProjectUtils.FILE_SEPARATOR;
    }
    
}
