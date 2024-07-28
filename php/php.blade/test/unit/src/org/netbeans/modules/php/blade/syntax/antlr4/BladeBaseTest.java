package org.netbeans.modules.php.blade.syntax.antlr4;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author bhaidu
 */
public abstract class BladeBaseTest extends NbTestCase {

    public BladeBaseTest(String testName) {
        super(testName);
    }
            
    protected FileObject touch(final FileObject dir, final String path) throws IOException {
        return FileUtil.createData(dir, path);
    }

    public File getDataDir() {
        URL codebase = getClass().getProtectionDomain().getCodeSource().getLocation();
        File dataDir = null;
        try {
            dataDir = new File(new File(codebase.toURI()), "data");
        } catch (URISyntaxException x) {
            throw new Error(x);
        }
        return dataDir;
    }


    protected FileObject touch(final String dir, final String path) throws IOException {
        return touch(new File(dir), path);
    }

    protected FileObject touch(final File dir, final String path) throws IOException {
        if (!dir.isDirectory()) {
            assertTrue("success to create " + dir, dir.mkdirs());
        }
        FileObject dirFO = FileUtil.toFileObject(FileUtil.normalizeFile(dir));
        return touch(dirFO, path);
    }
    
    abstract protected String getTestResult(String filename) throws Exception;
}
