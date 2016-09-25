package org.jetbrains.kotlin.projectsextensions.maven.classpath.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.jetbrains.kotlin.projectsextensions.maven.MavenHelper;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

/**
 *
 * @author  Milos Kleint
 */
public final class BootClassPathImpl implements ClassPathImplementation, PropertyChangeListener {

    private List<? extends PathResourceImplementation> resourcesCache;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final @NonNull Project project;
    private String lastHintValue = null;
    private boolean activePlatformValid = true;
    private JavaPlatformManager platformManager;
    private final EndorsedClassPathImpl ecpImpl;
        //lock for this class and EndorsedCPI
    final Object LOCK = new Object();

    

    @SuppressWarnings("LeakingThisInConstructor")
    BootClassPathImpl(@NonNull Project project, EndorsedClassPathImpl ecpImpl) {
        this.project = project;
        this.ecpImpl = ecpImpl;
        ecpImpl.setBCP(this);
        ecpImpl.addPropertyChangeListener(this);
    }

    private String getFromAuxProps(String opt, boolean bool) {
        Class clazz = project.getClass();
        try {
            Method getAuxProps = clazz.getMethod("getAuxProps");
            Object auxPropsImpl = getAuxProps.invoke(project);
            Class auxPropsImplClass = auxPropsImpl.getClass();
            Method get = auxPropsImplClass.getMethod("get", String.class, boolean.class);
            
            return (String) get.invoke(auxPropsImpl, opt, bool);
        } catch (ReflectiveOperationException ex) {
            Exceptions.printStackTrace(ex);
        } 
        
        return "";
    }
    
    public @Override List<? extends PathResourceImplementation> getResources() {
        synchronized (LOCK) {
            if (this.resourcesCache == null) {
                ArrayList<PathResourceImplementation> result = new ArrayList<PathResourceImplementation> ();
                boolean[] includeJDK = { true };
                boolean[] includeFX = { false };
                result.addAll(ecpImpl.getResources(includeJDK, includeFX));
                lastHintValue = getFromAuxProps(Constants.HINT_JDK_PLATFORM, true);
                if (includeJDK[0]) {
                    JavaPlatform pat = findActivePlatform();
                    boolean hasFx = false;
                    for (ClassPath.Entry entry : pat.getBootstrapLibraries().entries()) {
                        if (entry.getURL().getPath().endsWith("/jfxrt.jar!/")) {
                            hasFx = true;
                        }
                        result.add(ClassPathSupport.createResource(entry.getURL()));
                    }
                    if (includeFX[0] && !hasFx) {
                        PathResourceImplementation fxcp = createFxCPImpl(pat);
                        if (fxcp != null) {
                            result.add(fxcp);
                        }
                    }
                    result.addAll(nbPlatformJavaFxCp(project, pat));
                }
                resourcesCache = Collections.unmodifiableList (result);
            }
            return this.resourcesCache;
        }
    }

    public @Override void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener (listener);
    }

    public @Override void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener (listener);
    }

    @NonNull JavaPlatform findActivePlatform () {
        synchronized (LOCK) {
            activePlatformValid = true;
            if (platformManager == null) {
                platformManager = JavaPlatformManager.getDefault();
                platformManager.addPropertyChangeListener(WeakListeners.propertyChange(this, platformManager));
                NbMavenProject watch = MavenHelper.getProjectWatcher(project);
                if (watch != null) {
                    watch.addPropertyChangeListener(this);
                }
            }

            //TODO ideally we would handle this by toolchains in future.

            //only use the default auximpl otherwise we get recursive calls problems.
            String val = getFromAuxProps(Constants.HINT_JDK_PLATFORM, true);

            JavaPlatform plat = getActivePlatform(val);
            if (plat == null) {
                //TODO report how?
                Logger.getLogger(BootClassPathImpl.class.getName()).log(Level.FINE, "Cannot find java platform with id of ''{0}''", val); //NOI18N
                plat = platformManager.getDefaultPlatform();
                activePlatformValid = false;
            }
            //Invalid platform ID or default platform
            return plat;
        }
    }
    
    /**
     * Returns the active platform used by the project or null if the active
     * project platform is broken.
     * @param activePlatformId the name of platform used by Ant script or null
     * for default platform.
     * @return active {@link JavaPlatform} or null if the project's platform
     * is broken
     */
    public static JavaPlatform getActivePlatform (final String activePlatformId) {
        final JavaPlatformManager pm = JavaPlatformManager.getDefault();
        if (activePlatformId == null) {
            return pm.getDefaultPlatform();
        }
        else {
            JavaPlatform[] installedPlatforms = pm.getPlatforms(null, new Specification ("j2se",null));   //NOI18N
            for (JavaPlatform installedPlatform : installedPlatforms) {
                String antName = installedPlatform.getProperties().get("platform.ant.name"); //NOI18N
                if (antName != null && antName.equals(activePlatformId)) {
                    return installedPlatform;
                }
            }
            return null;
        }
    }

    public @Override void propertyChange(PropertyChangeEvent evt) {
        String newVal = getFromAuxProps(Constants.HINT_JDK_PLATFORM, true);
        if (evt.getSource() == project && evt.getPropertyName().equals(NbMavenProject.PROP_PROJECT)) {
            if (ecpImpl.resetCache()) {
                resetCache();
            } else {
                //Active platform was changed
                if ( (newVal == null && lastHintValue != null) || (newVal != null && !newVal.equals(lastHintValue))) {
                    resetCache ();
                }
            }
        }
        else if (evt.getSource() == platformManager && 
                JavaPlatformManager.PROP_INSTALLED_PLATFORMS.equals(evt.getPropertyName()) 
                && lastHintValue != null) {
            lastHintValue = newVal;
            //Platform definitions were changed, check if the platform was not resolved or deleted
            if (activePlatformValid) {
                if (getActivePlatform (lastHintValue) == null) {
                    //the platform was removed
                    resetCache();
                }
            }
            else {
                if (getActivePlatform (lastHintValue) != null) {
                    //platform was added
                    resetCache();
                }
            }
        } else if (evt.getSource() == ecpImpl) {
            resetCache();
        }
    }
    
    /**
     * Resets the cache and firesPropertyChange
     */
    private void resetCache () {
        synchronized (LOCK) {
            resourcesCache = null;
        }
        support.firePropertyChange(PROP_RESOURCES, null, null);
    }
    
    @Override public boolean equals(Object obj) {
        return obj instanceof BootClassPathImpl && project.equals(((BootClassPathImpl) obj).project);
    }

    @Override public int hashCode() {
        return project.hashCode() ^ 191;
    }

    private Collection<? extends PathResourceImplementation> nbPlatformJavaFxCp(Project project, JavaPlatform pat) {
        List<PathResourceImplementation> toRet = new ArrayList<PathResourceImplementation>();
        //TODO better to iterate dependencies first or check what jdk we are using?
        //this should actually be part of maven.apisupport but there's no viable api right now..
        //TODO do we even need this, once people setup compiler plugin correctly to use jfxrt.jar, it should appear on boot cp anyway
        MavenProject originalProject = MavenHelper.getOriginalMavenProject(project);
        if (originalProject == null) {
            return toRet;
        }
        for (Object a : originalProject.getArtifacts()) {
            if ("org.netbeans.api".equals(((Artifact) a).getGroupId()) 
                    && "org-netbeans-libs-javafx".equals(((Artifact) a).getArtifactId())) {
                PathResourceImplementation fxcp = createFxCPImpl(pat);
                if (fxcp != null) {
                    toRet.add(fxcp);
                }
            }
        }
        return toRet;
    }

    private PathResourceImplementation createFxCPImpl(JavaPlatform pat) {
        for (FileObject fo : pat.getInstallFolders()) {
            FileObject jdk8 = fo.getFileObject("jre/lib/ext/jfxrt.jar"); // NOI18N
            if (jdk8 == null) {
                FileObject jdk7 = fo.getFileObject("jre/lib/jfxrt.jar"); // NOI18N
                if (jdk7 != null) {
                    // jdk7 add the classes on bootclasspath
                    if (FileUtil.isArchiveFile(jdk7)) {
                        return ClassPathSupport.createResource(FileUtil.getArchiveRoot(jdk7.toURL()));
                    }
                }
            }
        }
        return null;
    }
    
}
