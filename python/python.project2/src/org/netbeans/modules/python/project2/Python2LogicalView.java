package org.netbeans.modules.python.project2;

import java.awt.Image;
import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.python.project2.ui.ChangePackageViewTypeAction;
import org.netbeans.modules.python.project2.ui.PackageView;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;
import static org.netbeans.modules.python.project2.Bundle.*;

@NbBundle.Messages({"# {0} - Path to of project",
    "PythonLogicalView.ProjectTooltipDescription=Python project in {0}",
    "LBL_RunAction_Name=Run",
    "LBL_DebugAction_Name=Debug",
    "LBL_TestAction_Name=Test",
    "LBL_BuildAction_Name=Build Egg",
    "LBL_CleanBuildAction_Name=Clean and Build Egg"})
class Python2LogicalView implements LogicalViewProvider {

    private static final Image brokenProjectBadge = ImageUtilities.loadImage("org/netbeans/modules/python/project2/resources/brokenProjectBadge.gif", true);
    private final PythonProject2 project;

    public Python2LogicalView(PythonProject2 project) {
        this.project = project;
    }

    @Override
    public Node createLogicalView() {
        return new PythonProjectNode();
    }

    @Override
    public Node findPath(Node root, Object target) {
        Project p = root.getLookup().lookup(Project.class);
        if (p == null) {
            return null;
        }
        if (target instanceof FileObject) {
            FileObject targetFO = (FileObject) target;
            Project owner = FileOwnerQuery.getOwner(targetFO);
            if (!p.equals(owner)) {
                return null; // Don't waste time if project does not own the fo
            }

            for (Node n : root.getChildren().getNodes(true)) {
                Node result = PackageView.findPath(n, target);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }
    
    private final class PythonProjectNode extends AbstractNode {

        private boolean broken; //for future use, marks the project as broken

        public PythonProjectNode() {
            super(NodeFactorySupport.createCompositeChildren(project, "Projects/org-netbeans-modules-python-project2/Nodes"),
                    Lookups.singleton(project));
            setIconBaseWithExtension("org/netbeans/modules/python/project2/resources/py_25_16.png");
            super.setName(ProjectUtils.getInformation(project).getDisplayName());
        }

        public 
        @Override
        String getShortDescription() {
            //todo: Add python platform description
            String dirName = FileUtil.getFileDisplayName(project.getProjectDirectory());
            return PythonLogicalView_ProjectTooltipDescription(dirName);
        }

        public 
        @Override
        String getHtmlDisplayName() {
            String dispName = super.getDisplayName();
            try {
                dispName = XMLUtil.toElementContent(dispName);
            } catch (CharConversionException ex) {
                return dispName;
            }
            // XXX text colors should be taken from UIManager, not hard-coded!
            return broken ? "<font color=\"#A40000\">" + dispName + "</font>" : null; //NOI18N
        }

        @Override
        public Image getIcon(int type) {
            Image original = super.getIcon(type);
            return broken ? ImageUtilities.mergeImages(original, brokenProjectBadge, 8, 0) : original;
        }

        @Override
        public Image getOpenedIcon(int type) {
            Image original = super.getOpenedIcon(type);
            return broken ? ImageUtilities.mergeImages(original, brokenProjectBadge, 8, 0) : original;
        }

        @Override
        public Action[] getActions(boolean context) {
            return getAdditionalActions();
        }

        @Override
        public boolean canRename() {
            return true;
        }

        @Override
        public void setName(String s) {
            DefaultProjectOperations.performDefaultRenameOperation(project, s);
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(PythonProjectNode.class);
        }

        private Action[] getAdditionalActions() {
            final List<Action> actions = new ArrayList<>();
            actions.add(CommonProjectActions.newFileAction());
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, LBL_RunAction_Name(), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG, LBL_DebugAction_Name(), null)); // NOI18N
//            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_TEST, LBL_TestAction_Name(), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, LBL_BuildAction_Name(), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, LBL_CleanBuildAction_Name(), null)); // NOI18N
            actions.add(null);
//            actions.add(CoverageActionFactory.createCollectorAction(null, null));
//            actions.add(null);
            actions.add(CommonProjectActions.setAsMainProjectAction());
            actions.add(CommonProjectActions.openSubprojectsAction());
            actions.add(CommonProjectActions.closeProjectAction());
            actions.add(null);
            actions.add(CommonProjectActions.renameProjectAction());
            actions.add(CommonProjectActions.moveProjectAction());
            actions.add(CommonProjectActions.copyProjectAction());
            actions.add(CommonProjectActions.deleteProjectAction());
            actions.add(null);
            actions.add(new ChangePackageViewTypeAction());
            actions.add(null);
            actions.add(SystemAction.get(FindAction.class));

            // honor 57874 contact
            actions.addAll(Utilities.actionsForPath("Projects/Actions")); //NOI18N

            actions.add(null);
            actions.add(CommonProjectActions.customizeProjectAction());
            return actions.toArray(new Action[actions.size()]);
        }
    }
}
