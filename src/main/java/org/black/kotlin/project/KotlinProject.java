package org.black.kotlin.project;

import java.awt.Image;
import java.beans.PropertyChangeListener; 
import java.io.IOException;
import javax.swing.Action;
import javax.swing.Icon; 
import javax.swing.ImageIcon; 
import org.black.kotlin.run.KotlinCompiler;
import org.netbeans.api.annotations.common.StaticResource; 
import org.netbeans.api.project.Project; 
import org.netbeans.api.project.ProjectInformation; 
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

public class KotlinProject implements Project{

    private final class Info implements ProjectInformation{
        
        @StaticResource()
        public static final String KOTLIN_ICON="org/black/kotlin/kotlin.png";

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
        public void addPropertyChangeListener(PropertyChangeListener pl) {}

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pl) {}
        
    }
    
    class KotlinProjectLogicalView implements LogicalViewProvider{
        
        @StaticResource()
        public static final String KOTLIN_ICON = "org/black/kotlin/kotlin.png";
        
        private final KotlinProject project;
        
        public KotlinProjectLogicalView(KotlinProject project){
            this.project = project;
        }

        @Override
        public Node createLogicalView() {
            try{
                FileObject projectDirectory = project.getProjectDirectory();
                DataFolder projectFolder = DataFolder.findFolder(projectDirectory);
                Node nodeOfProjectFolder = projectFolder.getNodeDelegate();
                return new ProjectNode(nodeOfProjectFolder, project);
            } catch(DataObjectNotFoundException donfe){
                Exceptions.printStackTrace(donfe);
                return new AbstractNode(Children.LEAF);
            }
        }
        
        private final class ProjectNode extends FilterNode{
            
            final KotlinProject project;
            
            public ProjectNode(Node node, KotlinProject project) throws DataObjectNotFoundException{
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
            public Action[] getActions(boolean arg0){
                return new Action[]{
                    ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD,
                            "Build Project", null),
                    CommonProjectActions.newFileAction(),
                    CommonProjectActions.copyProjectAction(),
                    CommonProjectActions.deleteProjectAction(),
                    CommonProjectActions.closeProjectAction()
                };
            }
            
            @Override
            public Image getIcon(int type){
                return ImageUtilities.loadImage(KOTLIN_ICON);
            }
            
            @Override
            public Image getOpenedIcon(int type){
                return getIcon(type);
            }
            
            @Override
            public String getDisplayName(){
                return project.getProjectDirectory().getName();
            }
        }

        @Override
        public Node findPath(Node node, Object o) {
            return null;
        }
        
        
    }
    
    private final class ActionProviderImpl implements ActionProvider{
        
        private String[] supported = new String[]{
            ActionProvider.COMMAND_DELETE,
            ActionProvider.COMMAND_COPY,
            ActionProvider.COMMAND_BUILD,
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
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("BUILD"));
                
                Thread newThread = new Thread(new Runnable(){

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
        }
        
        @Override
        public boolean isActionEnabled(String command, Lookup lookup) throws IllegalArgumentException {
            if ((command.equals(ActionProvider.COMMAND_DELETE))) {
                return true;
            } else if ((command.equals(ActionProvider.COMMAND_COPY))) {
                return true;
            } else if ((command.equals(ActionProvider.COMMAND_BUILD))) {
                return true;
            } else {
                throw new IllegalArgumentException(command);
            }
        }
            
    }
    
    private final FileObject projectDir;
    private final ProjectState state;
    private Lookup lkp;
    
    public KotlinProject(FileObject dir, ProjectState state){
        this.projectDir = dir;
        this.state = state;
    }
    
    @Override
    public FileObject getProjectDirectory() {
        return projectDir;
    }

    @Override
    public Lookup getLookup() {
        if (lkp == null){
            lkp = Lookups.fixed(new Object[]{
                new Info(),
                new KotlinProjectLogicalView(this),
                new ActionProviderImpl()
            });
        }
        return lkp;
    }
    
}
