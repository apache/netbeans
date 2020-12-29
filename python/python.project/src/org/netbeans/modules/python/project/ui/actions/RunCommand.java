
package org.netbeans.modules.python.project.ui.actions;

import java.io.File;
import org.netbeans.modules.python.api.PythonExecution;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.project.PythonProject;
import org.netbeans.modules.python.project.ui.customizer.PythonProjectProperties;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.python.editor.codecoverage.PythonCoverageProvider;
import org.netbeans.modules.python.project.PythonActionProvider;
import org.netbeans.modules.python.project.spi.TestRunner;
import org.netbeans.modules.python.project.ui.Utils;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

public class RunCommand extends Command {
    protected final boolean isTest;

    public RunCommand(PythonProject project, boolean isTest) {
        super(project);
        this.isTest = isTest;
    }

    @Override
    public String getCommandId() {
        return isTest ? ActionProvider.COMMAND_TEST : ActionProvider.COMMAND_RUN;
    }

    @Override
    public void invokeAction(Lookup context) throws IllegalArgumentException {
        if (isTest) {
            TestRunner testRunner = PythonActionProvider.getTestRunner(TestRunner.TestType.PY_UNIT);
            //boolean testTaskExist = RakeSupport.getRakeTask(project, TEST_TASK_NAME) != null;
            //if (testTaskExist) {
            //    File pwd = FileUtil.toFile(project.getProjectDirectory());
            //    RakeRunner runner = new RakeRunner(project);
            //    runner.setPWD(pwd);
            //    runner.setFileLocator(new RubyFileLocator(context, project));
            //    runner.showWarnings(true);
            //    runner.setDebug(COMMAND_DEBUG_SINGLE.equals(command));
            //    runner.run(TEST_TASK_NAME);
            /*} else */if (testRunner != null) { // don't invoke null.getInstance()...
                    testRunner.getInstance().runAllTests(getProject(), false);
                }
        }

        final PythonProject pyProject = getProject();
        final PythonPlatform platform = checkProjectPythonPlatform(pyProject);
        if ( platform == null )
          return ; // invalid platform user has been warn in check so safe to return
         
        if (getProperties().getMainModule() == null ||
                getProperties().getMainModule().equals("")){
            String main = Utils.chooseMainModule(getProject().getSourceRoots().getRoots());
            getProperties().setMainModule(main);
            getProperties().save();
        }
        //System.out.println("main module " + getProperties().getMainModule());
        FileObject script = findMainFile(pyProject);       
        //assert script != null;        
        if (script == null ){
            String main = Utils.chooseMainModule(getProject().getSourceRoots().getRoots());
            getProperties().setMainModule(main);
            getProperties().save();
            script = findMainFile(pyProject);
        }
        final FileObject parent = script.getParent();
        PythonExecution pyexec = new PythonExecution();
        pyexec.setDisplayName (ProjectUtils.getInformation(pyProject).getDisplayName());                
        //Set work dir - probably we need a property to store work dir
        String path = FileUtil.toFile(parent).getAbsolutePath();
        pyexec.setWorkingDirectory(path);        
        pyexec.setCommand(platform.getInterpreterCommand());
        //Set python script
        path = FileUtil.toFile(script).getAbsolutePath();
        pyexec.setScript(path);
        pyexec.setCommandArgs(platform.getInterpreterArgs());
        pyexec.setScriptArgs(pyProject.getEvaluator().getProperty(PythonProjectProperties.APPLICATION_ARGS));
        //build path & set 
        //build path & set
        pyexec.setPath(PythonPlatform.buildPath(super.buildPythonPath(platform,pyProject)));
        pyexec.setJavaPath(PythonPlatform.buildPath(super.buildJavaPath(platform,pyProject)));
        pyexec.setShowControls(true);
        pyexec.setShowInput(true);
        pyexec.setShowWindow(true);
        pyexec.addStandardRecognizers();

        PythonCoverageProvider coverageProvider = PythonCoverageProvider.get(pyProject);
        if (coverageProvider != null && coverageProvider.isEnabled()) {
            pyexec = coverageProvider.wrapWithCoverage(pyexec);
        }

        pyexec.run();
    }

    @Override
    public boolean isActionEnabled(Lookup context) throws IllegalArgumentException {
//        final PythonProject pyProject = getProject();
//        PythonPlatform platform = PythonProjectUtil.getActivePlatform(pyProject);
//        if (platform == null) {
//            return false;
//        }
//        else{
//            return true;
//        }
//        final FileObject fo = findMainFile (pyProject);
//        if (fo == null) {
//            return false;
//        }
//        return PythonMIMEResolver.PYTHON_MIME_TYPE.equals(fo.getMIMEType());
        return true;
    }
    
    protected static FileObject findMainFile (final PythonProject pyProject) {
        final FileObject[] roots = pyProject.getSourceRoots().getRoots();
        final String mainFile = pyProject.getEvaluator().getProperty(PythonProjectProperties.MAIN_FILE);
        if (mainFile == null) {
            return null;
        }
        FileObject fo = null;
        for (FileObject root : roots) {
            fo = root.getFileObject(mainFile);
            if (fo != null) {
                break;
            }
        }
        return fo;
    }

}
