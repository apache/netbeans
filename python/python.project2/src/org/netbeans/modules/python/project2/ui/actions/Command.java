/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.python.project2.ui.actions;

import java.io.File;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.api.PythonPlatformManager;
import org.netbeans.modules.python.project2.PythonProject2;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

public abstract class Command {

    private final PythonProject2 project;

    public Command(PythonProject2 project) {
        this.project = project;
        assert project != null;
    }

    public abstract String getCommandId();

    public abstract void invokeAction(Lookup context) throws IllegalArgumentException;

    public abstract boolean isActionEnabled(Lookup context) throws IllegalArgumentException;

    public boolean asyncCallRequired() {
        return true;
    }

    public boolean saveRequired() {
        return true;
    }

    public final PythonProject2 getProject() {
        return project;
    }

    public Node[] getSelectedNodes() {
        return TopComponent.getRegistry().getCurrentNodes();
    }

    protected void showLaunchError(String message) {
        JOptionPane.showMessageDialog(null, message, "Python Launch Error", JOptionPane.ERROR_MESSAGE);

    }

    /**
     * used by children to handle sever launched errors
     *
     * @param errMessage
     */
    protected PythonPlatform checkProjectPythonPlatform(PythonProject2 pyProject) {
        return pyProject.getActivePlatform();
    }

    /**
     *
     * provide a reasonable common Build of PYTHONPATH for Run or Debug commands
     *
     * @param platform current platform
     * @param project current project
     * @return PythonPath FileList
     */
    protected ArrayList<String> buildPythonPath(PythonPlatform platform, PythonProject2 project) {
        final ArrayList<String> pythonPath = new ArrayList<>();
        // start with platform
        pythonPath.addAll(platform.getPythonPath());
        Sources sources = ProjectUtils.getSources(project);
        for (SourceGroup fo : sources.getSourceGroups(PythonProject2.SOURCES_TYPE_PYTHON)) {
            File f = FileUtil.toFile(fo.getRootFolder());
            pythonPath.add(f.getAbsolutePath());
        }
        return pythonPath;
    }

    /**
     *
     * provide a reasonable common Build of JAVAPATH for Run or Debug Jython
     * commands
     *
     * @param platform current platform
     * @param project current project
     * @return JavaPath fileList for jython CLASSPATH command
     */
    protected ArrayList<String> buildJavaPath( PythonPlatform platform , PythonProject2 project ) {
      final ArrayList<String> javaPath = new ArrayList<>() ;
      // start with platform
      javaPath.addAll(platform.getJavaPath());
//      javaPath.addAll(getProperties().getJavaPath());
      return javaPath ;
    }
}
