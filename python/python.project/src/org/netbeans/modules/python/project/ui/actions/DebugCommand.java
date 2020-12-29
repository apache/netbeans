/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.project.ui.actions;

import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.debugger.DebugPythonSource;
import org.netbeans.modules.python.debugger.Debuggee;
import org.netbeans.modules.python.project.PythonProject;
import org.netbeans.modules.python.project.ui.customizer.PythonProjectProperties;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

public class DebugCommand extends RunCommand {

    public DebugCommand(PythonProject project) {
        super(project, false);
    }

    @Override
    public String getCommandId() {
        return ActionProvider.COMMAND_DEBUG;
    }


    @Override
    public void invokeAction(Lookup context) throws IllegalArgumentException {
        final PythonProject pyProject = getProject();
        final PythonPlatform platform = checkProjectPythonPlatform(pyProject);
        if ( platform == null )
          return ; // invalid platform user has been warn in check so safe to return
        final FileObject script = RunCommand.findMainFile(pyProject);
        if ( script == null )
        {
          showLaunchError("undefined(null) python script => unable to debug") ;
          return ; // give up
        }
        final FileObject parent = script.getParent();

        //final Debuggee pyDebuggee = Utils.getDebuggee(script);
        final Debuggee pyDebuggee = Debuggee.createDebuggee(script);

        pyDebuggee.setDisplayName (ProjectUtils.getInformation(pyProject).getDisplayName());
        //Set work dir - probably we need a property to store work dir
        String path = FileUtil.toFile(parent).getAbsolutePath();
        pyDebuggee.setWorkingDirectory(path);
        pyDebuggee.setPlatform(platform);
        //Set python script
        path = FileUtil.toFile(script).getAbsolutePath();
        pyDebuggee.setScript(path);

        // TODO - insert coverage script here?
        // See PythonCoverageProvider.getCoverageExecution(pyProject);

        // set user Arguments
        pyDebuggee.setScriptArgs(pyProject.getEvaluator().getProperty(PythonProjectProperties.APPLICATION_ARGS) ) ;

        //build path & set
        pyDebuggee.setPath(PythonPlatform.buildPath(super.buildPythonPath(platform,pyProject)));
        pyDebuggee.setJavaPath(PythonPlatform.buildPath(super.buildJavaPath(platform,pyProject)));

        // debugger console activation
        DebugPythonSource debugger = new DebugPythonSource( pyDebuggee , false ) ;
        debugger.startDebugging();
    }



}
