package org.black.kotlin.project;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.black.kotlin.run.KotlinCompiler;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;


/**
 * Class for ant-based Kotlin project.
 * @author Александр
 */
@AntBasedProjectRegistration(type = "org.black.kotlin.project.KotlinProject",
                            iconResource = "org/black/kotlin/kotlin.png",
                            sharedName = "data",
                            sharedNamespace = "http://www.netbeans.org/ns/kotlin-project/1",
                            privateName = "project-private",
                            privateNamespace = "http://www.netbeans.org/ns/kotlin-project-private/1")
public class KotlinProject implements Project {

    final AntProjectHelper helper;
    private final SourcesHelper sourcesHelper; 
    private final GlobalPathRegistry pathRegistry = GlobalPathRegistry.getDefault();
    
    /**
     * This class provides information about Kotlin project.
     */
    private final class Info implements ProjectInformation {

        @StaticResource()
        public static final String KOTLIN_ICON = "org/black/kotlin/kotlin.png";

        @Override
        public String getName() {
            return helper.getProjectDirectory().getName();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public Icon getIcon() {
            return new ImageIcon(ImageUtilities.loadImage(KOTLIN_ICON));
        }

        @Override
        public Project getProject() {
            return KotlinProject.this;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pl) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pl) {
        }

    }

    /**
     * Action provider class for Kotlin project.
     */
    private final class ActionProviderImpl implements ActionProvider {

        /**
         * Supported actions.
         */
        private final String[] supported = new String[]{
            ActionProvider.COMMAND_DELETE,
            ActionProvider.COMMAND_COPY,
            ActionProvider.COMMAND_BUILD,
            ActionProvider.COMMAND_CLEAN,
            ActionProvider.COMMAND_REBUILD,
            ActionProvider.COMMAND_RUN
        };

        /**
         * 
         * @return supported actions. 
         */
        @Override
        public String[] getSupportedActions() {
            return supported;
        }

        /**
         * Defines actions code. 
         * @throws IllegalArgumentException 
         */
        @Override
        public void invokeAction(String string, Lookup lookup) throws IllegalArgumentException {
            if (string.equalsIgnoreCase(ActionProvider.COMMAND_DELETE)) {
                DefaultProjectOperations.performDefaultDeleteOperation(KotlinProject.this);
            }
            if (string.equalsIgnoreCase(ActionProvider.COMMAND_COPY)) {
                DefaultProjectOperations.performDefaultCopyOperation(KotlinProject.this);
            }
            if (string.equalsIgnoreCase(ActionProvider.COMMAND_BUILD)) {
                KotlinCompiler.INSTANCE.antBuild(KotlinProject.this);
            }

            if (string.equalsIgnoreCase(ActionProvider.COMMAND_CLEAN)) {
                Thread newThread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        ProjectUtils.clean(KotlinProject.this);
                    }

                });

                newThread.start();
            }

            if (string.equalsIgnoreCase(ActionProvider.COMMAND_REBUILD)) {
                ProjectUtils.clean(KotlinProject.this);
                KotlinCompiler.INSTANCE.antBuild(KotlinProject.this);
            }
            
            if (string.equalsIgnoreCase(ActionProvider.COMMAND_RUN)) {
                
                try {
                    KotlinCompiler.INSTANCE.antRun(KotlinProject.this);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                
            }
        }

        /**
         * 
         * @return is action enabled or not.
         * @throws IllegalArgumentException 
         */
        @Override
        public boolean isActionEnabled(String command, Lookup lookup) throws IllegalArgumentException {
            if ((command.equals(ActionProvider.COMMAND_DELETE))) {
                return true;
            } else if ((command.equals(ActionProvider.COMMAND_COPY))) {
                return true;
            } else if ((command.equals(ActionProvider.COMMAND_BUILD))) {
                return true;
            } else if ((command.equals(ActionProvider.COMMAND_CLEAN))) {
                return true;
            } else if ((command.equals(ActionProvider.COMMAND_REBUILD))) {
                return true;
            } else if ((command.equals(ActionProvider.COMMAND_RUN))) {
                return true;
            } else {
                throw new IllegalArgumentException(command);
            }
        }

    }

    /**
     * This class defines the location of Kotlin sources.
     */
    public final class KotlinSources implements Sources {
        
        private void findSrc(FileObject fo, Collection<FileObject> files, KotlinProjectConstants type ) {
            if (fo.isFolder()) {
                for (FileObject file : fo.getChildren()) {
                    findSrc(file, files, type);
                }
            } else {
                if (null != type)switch (type) {
                    case KOTLIN_SOURCE:
                        if (fo.hasExt("kt")) {
                            files.add(fo.getParent());
                        }   break;
                    case JAVA_SOURCE:
                        if (fo.hasExt("java")) {
                            files.add(fo.getParent());
                        }   break;
                    case JAR:
                        if (fo.hasExt("jar")) {
                            if (!fo.getParent().getName().equals("build"))
                                files.add(fo.getParent());
                        }   break;
                    default:
                        break;
                }
            }
        }

        @NotNull
        public List<FileObject> getSrcDirectories(KotlinProjectConstants type) {
            Set<FileObject> orderedFiles = Sets.newLinkedHashSet();

            findSrc(KotlinProject.this.getProjectDirectory(), orderedFiles, type);
            return Lists.newArrayList(orderedFiles);
        
        }

        @Override
        public SourceGroup[] getSourceGroups(String string) {
            List<SourceGroup> srcGroups = new ArrayList();
            srcGroups.add(new KotlinSourceGroup(KotlinProject.this.getProjectDirectory().getFileObject("src")));
            if (string.equals(KotlinProjectConstants.FOLDER.toString())){
            
            } else if (string.equals(KotlinProjectConstants.JAR.toString())){
                List<FileObject> src = getSrcDirectories(KotlinProjectConstants.JAR);
                for (FileObject srcFolder : src){
                    srcGroups.add(new KotlinSourceGroup(srcFolder));
                }
            } else if (string.equals(KotlinProjectConstants.JAVA_SOURCE.toString())){
                List<FileObject> src = getSrcDirectories(KotlinProjectConstants.JAVA_SOURCE);
                for (FileObject srcFolder : src){
                    srcGroups.add(new KotlinSourceGroup(srcFolder));
                }
            } else if (string.equals(KotlinProjectConstants.KOTLIN_SOURCE.toString())){
                List<FileObject> src = getSrcDirectories(KotlinProjectConstants.KOTLIN_SOURCE);
                for (FileObject srcFolder : src){
                    srcGroups.add(new KotlinSourceGroup(srcFolder));
                }
            }
            
            return srcGroups.toArray(new SourceGroup[srcGroups.size()]);
        }

        @Override
        public void addChangeListener(ChangeListener cl) {
        }

        @Override
        public void removeChangeListener(ChangeListener cl) {
        }

        class KotlinSourceGroup implements SourceGroup {

            private final FileObject root;

            public KotlinSourceGroup(FileObject root) {
                this.root = root;
            }

            @Override
            public FileObject getRootFolder() {
                //return KotlinProject.this.getProjectDirectory().getFileObject("src");
                return root;
            }

            @Override
            public String getName() {
                return getRootFolder().getPath();
            }

            @Override
            public String getDisplayName() {
                return getRootFolder().getName();
            }

            @Override
            public Icon getIcon(boolean bln) {
                return new ImageIcon("org/black/kotlinkt.png");
            }

            @Override
            public boolean contains(FileObject fo) {
                return root.getFileObject(fo.getName()) != null;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener pl) {
            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener pl) {
            }

        }

    }
    
    public final class KotlinClasspathProvider implements ClassPathProvider{

        @Override
        public ClassPath findClassPath(FileObject fo, String string) {
            if (!fo.isFolder())
                return null;
            else
                return ClassPathSupport.createClassPath(fo);
        }
        
    }
        
    private final class KotlinSourceForBinaryQueryImplementation 
            implements SourceForBinaryQueryImplementation{
        
        KotlinSourceForBinaryQueryResult res = new KotlinSourceForBinaryQueryResult();

        @Override
        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            return res;
        }
        
        private final class KotlinSourceForBinaryQueryResult 
            implements SourceForBinaryQuery.Result{

            private final FileObject root = KotlinProject.this.getProjectDirectory().getFileObject("src");
            private final FileObject[] roots = new FileObject[]{root};
            
            @Override
            public FileObject[] getRoots() {
                return roots;
            }

            @Override
            public void addChangeListener(ChangeListener l) {
            }

            @Override
            public void removeChangeListener(ChangeListener l) {
            }
            
        } 
    
}
    
    private Lookup lkp;

    public KotlinProject(AntProjectHelper helper) {
        this.helper = helper;
        sourcesHelper = new SourcesHelper(this,helper,helper.getStandardPropertyEvaluator());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                KotlinProject.this.helper.setLibrariesLocation(KotlinProject.this.getProjectDirectory().getPath()+"/lib");
            }
        });
        sourcesHelper.sourceRoot(KotlinProject.this.getProjectDirectory().getFileObject("src").getPath());
        sourcesHelper.createSources();
    }

    @Override
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    @Override
    public Lookup getLookup() {
        if (lkp == null) {
            lkp = Lookups.fixed(new Object[]{
                this,
                new Info(),
                new KotlinProjectLogicalView(this),
                new KotlinSources(),
                new ActionProviderImpl(),
            //    new KotlinPrivilegedTemplates(),
            //    new KotlinProjectOpenedHook(this),
                sourcesHelper.createSourceGroupModifierImplementation()
            });
        }
        return lkp;
    }
    
    public AntProjectHelper getHelper(){
        return helper;
    }

    public GlobalPathRegistry getPathRegistry(){
        return pathRegistry;
    } 
    
}
