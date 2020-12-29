/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.project.ui.actions;

import javax.swing.JOptionPane;
import org.netbeans.modules.python.api.PythonMIMEResolver;
import org.netbeans.modules.python.api.PythonOptions;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.api.PythonPlatformManager;
import org.netbeans.modules.python.debugger.DebugPythonSource;
import org.netbeans.modules.python.debugger.Debuggee;
import org.netbeans.modules.python.project.PythonActionProvider;
import org.netbeans.modules.python.project.PythonProject;
import org.netbeans.modules.python.project.PythonProjectUtil;
import org.netbeans.modules.python.project.spi.TestRunner;
import org.netbeans.modules.python.project.ui.customizer.PythonProjectProperties;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

public class DebugSingleCommand extends RunSingleCommand {
    public DebugSingleCommand(PythonProject project, boolean isTest) {
        super(project, isTest);
    }

    @Override
    public String getCommandId() {
        return isTest ? ActionProvider.COMMAND_DEBUG_TEST_SINGLE : ActionProvider.COMMAND_DEBUG_SINGLE;
    }

    @Override
    public void invokeAction(Lookup context) throws IllegalArgumentException {
        Node[] activatedNodes = getSelectedNodes();
        DataObject gdo = activatedNodes[0].getLookup().lookup(DataObject.class);
        FileObject file = gdo.getPrimaryFile();
        if (file.getMIMEType().equals(PythonMIMEResolver.PYTHON_MIME_TYPE) ){

            if (isTest) {
                // Run test normally - don't pop up browser
                TestRunner testRunner = PythonActionProvider.getTestRunner(TestRunner.TestType.PY_UNIT);
                if (testRunner != null) {
                    testRunner.getInstance().runTest(file, true);
                    return;
                }
            }

            String path = FileUtil.toFile(file.getParent()).getAbsolutePath();
            String workingdir = FileUtil.toFile(getProject().getSrcFolder()).getAbsolutePath();
            //int pos = path.lastIndexOf("/");
            //path = path.substring(0, pos);
            String script = FileUtil.toFile(file).getAbsolutePath();
            //System.out.println("Folder " + path);
            //System.out.println("File " + script);
            final Debuggee pyDebuggee = Debuggee.createDebuggee(file);
            pyDebuggee.setDisplayName(gdo.getName());
            pyDebuggee.setWorkingDirectory(path);
            if(PythonOptions.getInstance().getPromptForArgs()){
               String args =  JOptionPane.showInputDialog("Enter the args for this script.", "");
               pyDebuggee.setScriptArgs(args);
            }

            // TODO - insert coverage script here?
            // See PythonCoverageProvider.getCoverageExecution(pyProject);

            final PythonProject pyProject = getProject();
            final PythonPlatform platform = checkProjectPythonPlatform(pyProject);
            if ( platform == null )
              return ; // invalid platform user has been warn in check so safe to return
            pyDebuggee.setPlatform(platform);
            pyDebuggee.setScript(script);
            //build path & set
            pyDebuggee.setPath(PythonPlatform.buildPath(super.buildPythonPath(platform,pyProject)));
            pyDebuggee.setJavaPath(PythonPlatform.buildPath(super.buildJavaPath(platform,pyProject)));
            
            // debugger console activation
            DebugPythonSource debugger = new DebugPythonSource( pyDebuggee , false ) ;
            debugger.startDebugging();
        }
    }
    

}
