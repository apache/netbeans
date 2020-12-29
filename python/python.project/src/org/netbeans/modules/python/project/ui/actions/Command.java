/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.project.ui.actions;

import java.io.File;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.api.PythonPlatformManager;
import org.netbeans.modules.python.project.PythonProject;
import org.netbeans.modules.python.project.PythonProjectUtil;
import org.netbeans.modules.python.project.ui.customizer.PythonProjectProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

public abstract class Command {


    private final PythonProject project;
    private final PythonProjectProperties properties;
    public Command(PythonProject project) {
        this.project = project;
        assert project != null;
        properties = new PythonProjectProperties(this.project);
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

    public final PythonProject getProject() {
        return project;
    }
    public Node[] getSelectedNodes(){
        return TopComponent.getRegistry().getCurrentNodes();
    }

    protected PythonProjectProperties getProperties() {
        return properties;
    }

    protected void showLaunchError( String message ){
      JOptionPane.showMessageDialog(null,message ,"Python Launch Error", JOptionPane.ERROR_MESSAGE);

    }


    /**
     * used by children to handle sever launched errors
     * @param errMessage
     */
    protected PythonPlatform checkProjectPythonPlatform( PythonProject pyProject ){
       PythonPlatform platform = PythonProjectUtil.getActivePlatform(pyProject);
       if ( platform == null ) {
         // Better to inform the user than try to use a default unsuited
         String platformId = pyProject.getEvaluator().getProperty(PythonProjectProperties.ACTIVE_PLATFORM);
         showLaunchError( "The selected project specifies a missing or invalid Python platform : " + // NOI18N
                           platformId +
                           "\nPlease add the Python platform or choose an existing one in Project Properties. " // NOI18N
                         );
       }
       return platform ;
    }



    /**
     *
     * provide a reasonable common Build of PYTHONPATH for Run or Debug commands
     *
     * @param platform current platform
     * @param project current project
     * @return PythonPath FileList
     */
    protected ArrayList<String> buildPythonPath( PythonPlatform platform , PythonProject project ) {
      final ArrayList<String> pythonPath = new ArrayList<>() ;
      // start with platform
      pythonPath.addAll(platform.getPythonPath());
      for (FileObject fo : project.getSourceRoots().getRoots()) {
        File f = FileUtil.toFile(fo);
        pythonPath.add(f.getAbsolutePath());
      }
      pythonPath.addAll(getProperties().getPythonPath());
      return pythonPath ;
    }

    /**
     *
     * provide a reasonable common Build of JAVAPATH for Run or Debug Jython commands
     * @param platform current platform
     * @param project current project
     * @return JavaPath fileList for jython CLASSPATH command
     */
    protected ArrayList<String> buildJavaPath( PythonPlatform platform , PythonProject project ) {
      final ArrayList<String> javaPath = new ArrayList<>() ;
      // start with platform
      javaPath.addAll(platform.getJavaPath());
      javaPath.addAll(getProperties().getJavaPath());
      return javaPath ;
    }

}
