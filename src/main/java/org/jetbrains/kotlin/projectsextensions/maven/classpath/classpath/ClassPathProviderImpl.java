package org.jetbrains.kotlin.projectsextensions.maven.classpath.classpath;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.api.execute.ActiveJ2SEPlatformProvider;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Defines class path for maven2 projects..
 *
 * @author  Milos Kleint 
 */
@ProjectServiceProvider(service={ClassPathProvider.class, ActiveJ2SEPlatformProvider.class, ProjectSourcesClassPathProvider.class}, projectType="org-netbeans-modules-maven")
public final class ClassPathProviderImpl implements ClassPathProvider, ActiveJ2SEPlatformProvider, ProjectSourcesClassPathProvider {
    
    private static final int TYPE_SRC = 0;
    private static final int TYPE_TESTSRC = 1;
    private static final int TYPE_WEB = 5;
    private static final int TYPE_UNKNOWN = -1;
    
    private final @NonNull Project proj;
    private final ClassPath[] cache = new ClassPath[9];
    
    public ClassPathProviderImpl(@NonNull Project proj) {
        this.proj = proj;
    }
    
    /**
     * Returns array of all classpaths of the given type in the project.
     * The result is used for example for GlobalPathRegistry registrations.
     */
    @Override public ClassPath[] getProjectClassPaths(String type) {
        if (ClassPath.BOOT.equals(type)) {
            //TODO
            return new ClassPath[]{ getBootClassPath() };
        }
        if (ClassPathSupport.ENDORSED.equals(type)) {
            return new ClassPath[]{ getEndorsedClassPath() };
        }
        if (ClassPath.COMPILE.equals(type)) {
            List<ClassPath> l = new ArrayList<ClassPath>(2);
            l.add(getCompileTimeClasspath(TYPE_SRC));
            l.add(getCompileTimeClasspath(TYPE_TESTSRC));
            return l.toArray(new ClassPath[l.size()]);
        }
        if (ClassPath.EXECUTE.equals(type)) {
            List<ClassPath> l = new ArrayList<ClassPath>(2);
            l.add(getRuntimeClasspath(TYPE_SRC));
            l.add(getRuntimeClasspath(TYPE_TESTSRC));
            return l.toArray(new ClassPath[l.size()]);
        }
        
        if (ClassPath.SOURCE.equals(type)) {
            List<ClassPath> l = new ArrayList<ClassPath>(2);
            l.add(getSourcepath(TYPE_SRC));
            l.add(getSourcepath(TYPE_TESTSRC));
            return l.toArray(new ClassPath[l.size()]);
        }
        return new ClassPath[0];
    }
    
    /**
     * Returns the given type of the classpath for the project sources
     * (i.e., excluding tests roots).
     */
    @Override public ClassPath getProjectSourcesClassPath(String type) {
        if (ClassPath.BOOT.equals(type)) {
            return getBootClassPath();
        }
        if (ClassPathSupport.ENDORSED.equals(type)) {
            return getEndorsedClassPath();
        }
        if (ClassPath.COMPILE.equals(type)) {
            return getCompileTimeClasspath(TYPE_SRC);
        }
        if (ClassPath.SOURCE.equals(type)) {
            return getSourcepath(TYPE_SRC);
        }
        if (ClassPath.EXECUTE.equals(type)) {
            return getRuntimeClasspath(TYPE_SRC);
        }
        assert false;
        return null;
    }
    
    
    @Override public ClassPath findClassPath(FileObject file, String type) {
        int fileType = getType(file);
        if (fileType != TYPE_SRC &&  fileType != TYPE_TESTSRC && fileType != TYPE_WEB) {
            Logger.getLogger(ClassPathProviderImpl.class.getName()).log(Level.FINEST, " bad type={0} for {1}", new Object[] {type, file}); //NOI18N
            return null;
        }
        if (type.equals(ClassPath.COMPILE)) {
            return getCompileTimeClasspath(fileType);
        } else if (type.equals(ClassPath.EXECUTE)) {
            return getRuntimeClasspath(fileType);
        } else if (ClassPath.SOURCE.equals(type)) {
            return getSourcepath(fileType);
        } else if (type.equals(ClassPath.BOOT)) {
            return getBootClassPath();
        } else if (type.equals(ClassPathSupport.ENDORSED)) {
            return getEndorsedClassPath();
        } else if (type.equals(JavaClassPathConstants.PROCESSOR_PATH)) {
            // XXX read <processorpath> from maven-compiler-plugin config
            return getCompileTimeClasspath(fileType);
        } else {
            return null;
        }
    }

    private boolean isChildOf(FileObject child, URI[] uris) {
        for (int i = 0; i < uris.length; i++) {
            FileObject fo = FileUtilities.convertURItoFileObject(uris[i]);
            if (fo != null  && fo.isFolder() && (fo.equals(child) || FileUtil.isParentOf(fo, child))) {
                return true;
            }
        }
        return false;
    }
    
    public static FileObject[] convertStringsToFileObjects(List<String> strings) {
        FileObject[] fos = new FileObject[strings.size()];
        int index = 0;
        Iterator<String> it = strings.iterator();
        while (it.hasNext()) {
            String str = it.next();
            fos[index] = FileUtilities.convertStringToFileObject(str);
            index++;
        }
        return fos;
    }
    
    
    private int getType(FileObject file) {
        if (isChildOf(file, ClassPathUtils.getSourceRoots(proj, false)) ||
            isChildOf(file, ClassPathUtils.getGeneratedSourceRoots(proj, false))) {
            return TYPE_SRC;
        }
        if (isChildOf(file, ClassPathUtils.getSourceRoots(proj, true)) ||
            isChildOf(file, ClassPathUtils.getGeneratedSourceRoots(proj, true))) {
            return TYPE_TESTSRC;
        }
        
        URI web = ClassPathUtils.getWebAppDirectory(proj);
        FileObject fo = FileUtil.toFileObject(Utilities.toFile(web));
        if (fo != null && (fo.equals(file) || FileUtil.isParentOf(fo, file))) {
            return TYPE_WEB;
        }
        
        //MEVENIDE-613, #125603 need to check later than the actual java sources..
        // sometimes the root of resources is the basedir for example that screws up 
        // test sources.
        if (isChildOf(file, ClassPathUtils.getResources(proj, false))) {
            return TYPE_SRC;
        }
        if (isChildOf(file, ClassPathUtils.getResources(proj, true))) {
            return TYPE_TESTSRC;
        }
        return TYPE_UNKNOWN;
    }
    
    
    
    private synchronized ClassPath getSourcepath(int type) {
        if (type == TYPE_WEB) {
            type = TYPE_SRC;
        }
        ClassPath cp = cache[type];
        if (cp == null) {
            if (type == TYPE_SRC) {
                cp = ClassPathFactory.createClassPath(new SourceClassPathImpl(proj));
            } else {
                cp = ClassPathFactory.createClassPath(new TestSourceClassPathImpl(proj));
            }
            cache[type] = cp;
        }
        return cp;
    }
    
    private synchronized ClassPath getCompileTimeClasspath(int type) {
        if (type == TYPE_WEB) {
            type = TYPE_SRC;
        }
        ClassPath cp = cache[2+type];
        if (cp == null) {
            if (type == TYPE_SRC) {
                cp = ClassPathFactory.createClassPath(new CompileClassPathImpl(proj));
            } else {
                cp = ClassPathFactory.createClassPath(new TestCompileClassPathImpl(proj));
            }
            cache[2+type] = cp;
        }
        return cp;
    }
    
    private synchronized ClassPath getRuntimeClasspath(int type) {
        if (type == TYPE_WEB) {
            type = TYPE_SRC;
        }
        ClassPath cp = cache[4+type];
        if (cp == null) {
            if (type == TYPE_SRC) {
                cp = ClassPathFactory.createClassPath(new RuntimeClassPathImpl(proj));
            } else {
                cp = ClassPathFactory.createClassPath(new TestRuntimeClassPathImpl(proj));
            }
            cache[4+type] = cp;
        }
        return cp;
    }
    
    private synchronized ClassPath getBootClassPath() {
        ClassPath cp = cache[6];
        if (cp == null) {
            cp = ClassPathFactory.createClassPath(getBootClassPathImpl());
            cache[6] = cp;
        }
        return cp;
    }
    
    private BootClassPathImpl bcpImpl;
    private synchronized BootClassPathImpl getBootClassPathImpl() {
        if (bcpImpl == null) {
            bcpImpl = new BootClassPathImpl(proj, getEndorsedClassPathImpl());
        }
        return bcpImpl;
    }

    @Override public @NonNull JavaPlatform getJavaPlatform() {
        return getBootClassPathImpl().findActivePlatform();
    }

    private EndorsedClassPathImpl ecpImpl;
    private synchronized EndorsedClassPathImpl getEndorsedClassPathImpl() {
        if (ecpImpl == null) {
            ecpImpl = new EndorsedClassPathImpl(proj);
        }
        return ecpImpl;
    }

    private ClassPath getEndorsedClassPath() {
        ClassPath cp = cache[8];
        if (cp == null) {
            getBootClassPathImpl();
            cp = ClassPathFactory.createClassPath(getEndorsedClassPathImpl());
            cache[8] = cp;
        }
        return cp;
    }
}

