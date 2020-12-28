/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.python.actions;

import java.io.File;
import org.netbeans.modules.python.api.PythonExecution;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.api.PythonPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.python.api.PythonMIMEResolver;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public final class RunPyAction extends CookieAction {
    @Override
    protected void performAction(Node[] activatedNodes) {
        //listProperties();
        DataObject gdo = activatedNodes[0].getLookup().lookup(DataObject.class);
        if (gdo.getPrimaryFile().getMIMEType().equals(PythonMIMEResolver.PYTHON_MIME_TYPE)) {

            String path = gdo.getPrimaryFile().getParent().getPath();
            //int pos = path.lastIndexOf("/");
            //path = path.substring(0, pos);
            String script = FileUtil.toFile(gdo.getPrimaryFile()).getAbsolutePath();
            System.out.println("Folder " + path);
            System.out.println("File " + script);
            PythonExecution pyexec = new PythonExecution();
            pyexec.setDisplayName(gdo.getName());
            pyexec.setWorkingDirectory(getMainProjectWorkPath().getAbsolutePath());
            PythonPlatformManager manager = PythonPlatformManager.getInstance();
            PythonPlatform platform = manager.getPlatform(manager.getDefaultPlatform());
            platform.addPythonPath(getMainProjectWorkPath().getAbsolutePath());
            pyexec.setCommand(platform.getInterpreterCommand());
            pyexec.setScript(script);
            pyexec.setCommandArgs(platform.getInterpreterArgs());
            pyexec.setPath(PythonPlatform.buildPath(platform.getPythonPath()));
            pyexec.setShowControls(true);
            pyexec.setShowInput(true);
            pyexec.setShowWindow(true);
            pyexec.run();
        }
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(RunPyAction.class, "CTL_RunPyAction");
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[]{DataObject.class};
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean results = false; //super.enable(activatedNodes);
        if (activatedNodes.length > 0) {
            DataObject gdo = activatedNodes[0].getLookup().lookup(DataObject.class);
            results = gdo.getPrimaryFile().getMIMEType().equals(PythonMIMEResolver.PYTHON_MIME_TYPE);
        }
        return results;
    }

    @Override
    protected String iconResource() {
        return "org/netbeans/modules/python/actions/page_go.png";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    private File getMainProjectWorkPath() {
        File pwd = null;
        Project mainProject = OpenProjects.getDefault().getMainProject();
        if (mainProject != null) {
            FileObject fo = mainProject.getProjectDirectory();
            if (!fo.isFolder()) {
                fo = fo.getParent();
            }
            pwd = FileUtil.toFile(fo);
        }
        if (pwd == null) {
            String userHome = System.getProperty("user.home");
            pwd = new File(userHome);
        }
        return pwd;
    }

//    private void listProperties() {
//        Properties props = System.getProperties();
//        props.list(System.out);
//    }
}

