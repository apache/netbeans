package org.netbeans.modules.python.project2.ui.actions;

import org.netbeans.modules.python.api.PythonExecution;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.project2.PythonProject2;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.python.api.PythonPlatformManager;
import org.netbeans.modules.python.project2.ui.Utils;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

public class RunCommand extends Command {

    protected final boolean isTest;

    public RunCommand(PythonProject2 project, boolean isTest) {
        super(project);
        this.isTest = isTest;
    }

    @Override
    public String getCommandId() {
        return isTest ? ActionProvider.COMMAND_TEST : ActionProvider.COMMAND_RUN;
    }

    @Override
    public void invokeAction(Lookup context) throws IllegalArgumentException {
//        if (isTest) {
//            TestRunner testRunner = PythonActionProvider.getTestRunner(TestRunner.TestType.PY_UNIT);
//            //boolean testTaskExist = RakeSupport.getRakeTask(project, TEST_TASK_NAME) != null;
//            //if (testTaskExist) {
//            //    File pwd = FileUtil.toFile(project.getProjectDirectory());
//            //    RakeRunner runner = new RakeRunner(project);
//            //    runner.setPWD(pwd);
//            //    runner.setFileLocator(new RubyFileLocator(context, project));
//            //    runner.showWarnings(true);
//            //    runner.setDebug(COMMAND_DEBUG_SINGLE.equals(command));
//            //    runner.run(TEST_TASK_NAME);
//            //} else if (testRunner != null) {
//            testRunner.getInstance().runAllTests(getProject(), false);
//            //}
//            return;
//        }

        final PythonProject2 pyProject = getProject();
        PythonPlatform platform = checkProjectPythonPlatform(pyProject);
        if (platform == null) {
            String platformName = Utils.choosePythonPlatform(pyProject);
            PythonPlatformManager ppm = PythonPlatformManager.getInstance();
            if (platformName == null) {
                platformName = ppm.getDefaultPlatform();
            }
            platform = ppm.getPlatform(platformName);
            if (platform == null) {
                return;
            }
            pyProject.setActivePlatform(platform);
        }

        String main = pyProject.getMainModule();
        if (main == null || main.isEmpty()) {
            main = Utils.chooseMainModule(pyProject);
            pyProject.setMainModule(main);
        }
        //System.out.println("main module " + getProperties().getMainModule());
        FileObject script = findMainFile(pyProject, main);
        //assert script != null;
        if (script == null) {
            main = Utils.chooseMainModule(pyProject);
            pyProject.setMainModule(main);
            script = findMainFile(pyProject, main);
        }
        if(script == null) {
            return;
        }
        final FileObject parent = script.getParent();
        PythonExecution pyexec = new PythonExecution();
        pyexec.setDisplayName(ProjectUtils.getInformation(pyProject).getDisplayName());
        //Set work dir - probably we need a property to store work dir
        String path = FileUtil.toFile(parent).getAbsolutePath();
        pyexec.setWorkingDirectory(path);
        pyexec.setCommand(platform.getInterpreterCommand());
        //Set python script
        path = FileUtil.toFile(script).getAbsolutePath();
        pyexec.setScript(path);
        pyexec.setCommandArgs(platform.getInterpreterArgs());
        pyexec.setScriptArgs(pyProject.getApplicationArgs());
        //build path & set
        //build path & set
        pyexec.setPath(PythonPlatform.buildPath(super.buildPythonPath(platform, pyProject)));
        pyexec.setJavaPath(PythonPlatform.buildPath(super.buildJavaPath(platform, pyProject)));
        pyexec.setShowControls(true);
        pyexec.setShowInput(true);
        pyexec.setShowWindow(true);
        pyexec.addStandardRecognizers();

//        PythonCoverageProvider coverageProvider = PythonCoverageProvider.get(pyProject);
//        if (coverageProvider != null && coverageProvider.isEnabled()) {
//            pyexec = coverageProvider.wrapWithCoverage(pyexec);
//        }

        pyexec.run();
    }


    @Override
    public boolean isActionEnabled(Lookup context) throws IllegalArgumentException {
//        final PythonProject2 pyProject = getProject();
//        PythonPlatform platform = PythonProject2Util.getActivePlatform(pyProject);
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

    protected static FileObject findMainFile(final PythonProject2 pyProject, final String mainFile) {
        if (mainFile == null) {
            return null;
        }
        final Sources sources = ProjectUtils.getSources(pyProject);
        FileObject fo = null;
        for (SourceGroup root : sources.getSourceGroups(PythonProject2.SOURCES_TYPE_PYTHON)) {
            fo = root.getRootFolder().getFileObject(mainFile);
            if (fo != null) {
                break;
            }
        }
        return fo;
    }

}
