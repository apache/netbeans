package org.black.kotlin.project;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.black.kotlin.projectsextensions.KotlinProjectHelper;
import org.black.kotlin.resolve.lang.java.NetBeansJavaProjectElementUtils;
import org.black.kotlin.utils.KotlinClasspath;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public final class KotlinClassPathProvider implements ClassPathProvider {

    
    private final Project project;
    private ClassPath boot = null;
    private ClassPath compile = null;
    private ClassPath source = null;

    public KotlinClassPathProvider(Project project){
        this.project = project;
    }
    
    public void updateClassPathProvider() {
        boot = getBootClassPath();
        compile = getCompileAndExecuteClassPath();
        NetBeansJavaProjectElementUtils.updateClasspathInfo(project);
    }

    private ClassPath getBootClassPath() {
        String bootClassPath = System.getProperty("sun.boot.class.path");
        List<URL> urls = new ArrayList<URL>();
        List<String> paths = new ArrayList<String>();
        paths.add(KotlinClasspath.getKotlinBootClasspath());
        paths.addAll(Arrays.asList(bootClassPath.split(
                Pattern.quote(System.getProperty("path.separator")))));
        for (String path : paths) {
            File file = new File(path);
            if (!file.canRead()) {
                continue;
            }

            FileObject fileObject = FileUtil.toFileObject(file);
            if (FileUtil.isArchiveFile(fileObject)) {
                fileObject = FileUtil.getArchiveRoot(fileObject);
            }
            if (fileObject != null) {
                urls.add(fileObject.toURL());
            }
        }

        FileObject libDir = project.getProjectDirectory().getFileObject("lib");
        if (libDir != null){
            for (FileObject file : libDir.getChildren()) {
                if ("jar".equals(file.getExt().toLowerCase())) {
                    urls.add(FileUtil.getArchiveRoot(file.toURL()));
                }
            }
        }
        return ClassPathSupport.createClassPath(urls.toArray(new URL[urls.size()]));
    }

    private ClassPath getCompileAndExecuteClassPath() {
        List<URL> classPathList = new ArrayList<URL>();

        URL[] classPathArray = new URL[classPathList.size() + 1];
        int index = 0;
        for (URL url : classPathList) {
            if (FileUtil.isArchiveFile(url)) {
                classPathArray[index++] = FileUtil.getArchiveRoot(url);
            } else {
                classPathArray[index++] = url;
            }
        }
//        classPathArray[index++] = project.getProjectDirectory().getFileObject("build").getFileObject("classes").toURL();
        classPathArray[index] = KotlinProjectHelper.INSTANCE.getLightClassesDirectory(project).toURL();
        return ClassPathSupport.createClassPath(classPathArray);
    }

    @Override
    public ClassPath findClassPath(FileObject fo, String type) {
        if (type.equals(ClassPath.BOOT)) {
            if (boot == null) {
                boot = getBootClassPath();
            }
            return boot;
        } else if (type.equals(ClassPath.COMPILE) || type.equals(ClassPath.EXECUTE)) {
            if (compile == null) {
                compile = getCompileAndExecuteClassPath();
            }
            return compile;
        } else if (type.equals(ClassPath.SOURCE)) {
            if (source == null) {
                source = ClassPathSupport.createClassPath(project.
                        getProjectDirectory().getFileObject("src"));
            }
            return source;
        } else if (!fo.isFolder()) {
            return null;
        } else {
            return ClassPathSupport.createClassPath(fo);
        }
    }

}
