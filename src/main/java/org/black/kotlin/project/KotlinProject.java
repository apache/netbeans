package org.black.kotlin.project;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeListener;
import org.black.kotlin.run.KotlinCompiler;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Александр
 */
public class KotlinProject implements Project {

    private final class Info implements ProjectInformation {

        @StaticResource()
        public static final String KOTLIN_ICON = "org/black/kotlin/kotlin.png";

        @Override
        public String getName() {
            return getProjectDirectory().getName();
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

    class KotlinProjectLogicalView implements LogicalViewProvider {

        @StaticResource()
        public static final String KOTLIN_ICON = "org/black/kotlin/kotlin.png";

        private final KotlinProject project;

        public KotlinProjectLogicalView(KotlinProject project) {
            this.project = project;
        }

        @Override
        public Node createLogicalView() {
            try {
                FileObject projectDirectory = project.getProjectDirectory();
                DataFolder projectFolder = DataFolder.findFolder(projectDirectory);
                Node nodeOfProjectFolder = projectFolder.getNodeDelegate();
                return new ProjectNode(nodeOfProjectFolder, project);
            } catch (DataObjectNotFoundException donfe) {
                Exceptions.printStackTrace(donfe);
                return new AbstractNode(Children.LEAF);
            }
        }

        private final class ProjectNode extends FilterNode {

            final KotlinProject project;

            public ProjectNode(Node node, KotlinProject project) throws DataObjectNotFoundException {
                super(node,
                        new FilterNode.Children(node),
                        new ProxyLookup(
                                new Lookup[]{
                                    Lookups.singleton(project),
                                    node.getLookup()
                                }));
                this.project = project;
            }

            @Override
            public Action[] getActions(boolean arg0) {
                return new Action[]{
                    ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD,
                    "Build Project", null),
                    ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN,
                    "Clean Project", null),
                    ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD,
                    "Rebuild Project", null),
                    CommonProjectActions.newFileAction(),
                    CommonProjectActions.copyProjectAction(),
                    CommonProjectActions.deleteProjectAction(),
                    CommonProjectActions.closeProjectAction()
                };
            }

            @Override
            public Image getIcon(int type) {
                return ImageUtilities.loadImage(KOTLIN_ICON);
            }

            @Override
            public Image getOpenedIcon(int type) {
                return getIcon(type);
            }

            @Override
            public String getDisplayName() {
                return project.getProjectDirectory().getName();
            }
        }

        @Override
        public Node findPath(Node node, Object o) {
            return null;
        }

    }

    private final class ActionProviderImpl implements ActionProvider {

        private String[] supported = new String[]{
            ActionProvider.COMMAND_DELETE,
            ActionProvider.COMMAND_COPY,
            ActionProvider.COMMAND_BUILD,
            ActionProvider.COMMAND_CLEAN,
            ActionProvider.COMMAND_REBUILD
        };

        @Override
        public String[] getSupportedActions() {
            return supported;
        }

        @Override
        public void invokeAction(String string, Lookup lookup) throws IllegalArgumentException {
            if (string.equalsIgnoreCase(ActionProvider.COMMAND_DELETE)) {
                DefaultProjectOperations.performDefaultDeleteOperation(KotlinProject.this);
            }
            if (string.equalsIgnoreCase(ActionProvider.COMMAND_COPY)) {
                DefaultProjectOperations.performDefaultCopyOperation(KotlinProject.this);
            }
            if (string.equalsIgnoreCase(ActionProvider.COMMAND_BUILD)) {

                Thread newThread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            KotlinCompiler.INSTANCE.compile(KotlinProject.this);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }

                });

                newThread.start();
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
                Thread newThread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            ProjectUtils.clean(KotlinProject.this);
                            KotlinCompiler.INSTANCE.compile(KotlinProject.this);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }

                });

                newThread.start();
            }
        }

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
            } else {
                throw new IllegalArgumentException(command);
            }
        }

    }

    private final class KotlinSources implements Sources {

        private void findSrc(FileObject fo, Collection<FileObject> files, KotlinProjectConstants type ) {
            if (fo.isFolder()) {
                for (FileObject file : fo.getChildren()) {
                    findSrc(file, files, type);
                }
            } else {
                if (type == KotlinProjectConstants.KOTLIN_SOURCE){
                    if (fo.hasExt("kt")) {
                        files.add(fo.getParent());
                    }
                } else if (type == KotlinProjectConstants.JAVA_SOURCE){
                    if (fo.hasExt("java")) {
                        files.add(fo.getParent());
                    }
                } else if (type == KotlinProjectConstants.JAR){
                    if (fo.hasExt("jar")) {
                        if (!fo.getParent().getName().equals("build"))
                            files.add(fo.getParent());
                    }
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

            private FileObject root;

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
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

    private final FileObject projectDir;
    private final ProjectState state;
    private Lookup lkp;

    public KotlinProject(FileObject dir, ProjectState state) {
        this.projectDir = dir;
        this.state = state;
    }

    @Override
    public FileObject getProjectDirectory() {
        return projectDir;
    }

    @Override
    public Lookup getLookup() {
        if (lkp == null) {
            lkp = Lookups.fixed(new Object[]{
                new Info(),
                new KotlinProjectLogicalView(this),
                new KotlinSources(),
                new ActionProviderImpl(),});
        }
        return lkp;
    }

}
