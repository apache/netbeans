package org.black.kotlin.project;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
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
public class KotlinProjectLogicalView implements LogicalViewProvider {

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

        private final class ProjectNode extends AbstractNode { //FilterNode {

            final KotlinProject project;

            public ProjectNode(Node node, KotlinProject project) throws DataObjectNotFoundException {
                super(//node,
                         NodeFactorySupport.createCompositeChildren(
                             project, "Projects/org-black-kotlin/Nodes"),
                        //new FilterNode.Children(node),
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
                    ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN,
                    "Run Project", null),
                    CommonProjectActions.newFileAction(),
                    CommonProjectActions.copyProjectAction(),
                    CommonProjectActions.deleteProjectAction(),
                    CommonProjectActions.setAsMainProjectAction(),
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