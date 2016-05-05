package org.black.kotlin.filesystem;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.java.preprocessorbridge.spi.VirtualSourceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Александр
 */
@ServiceProvider(service=VirtualSourceProvider.class)
public class KotlinVirtualSourceProvider implements VirtualSourceProvider{

    @Override
    public Set<String> getSupportedExtensions() {
        return Collections.singleton("kt");
    }

    @Override
    public boolean index() {
        return true;
    }

    @Override
    public void translate(Iterable<File> files, File sourceRoot, VirtualSourceProvider.Result result) {
        for (File file : files){
            FileObject fileObj = FileUtil.toFileObject(file);
            if (!fileObj.getExt().equals("kt")){
                continue;
            }
            
//            result.add(file, pkgQName,clzName, light code)
//            result.add(file, string, string1, cs);
        }
    }
    
}
