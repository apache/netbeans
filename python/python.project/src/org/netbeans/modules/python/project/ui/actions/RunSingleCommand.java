/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.project.ui.actions;

import javax.swing.JOptionPane;
import org.netbeans.modules.python.api.PythonExecution;
import org.netbeans.modules.python.api.PythonMIMEResolver;
import org.netbeans.modules.python.api.PythonOptions;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.api.PythonPlatformManager;

import org.netbeans.modules.python.editor.codecoverage.PythonCoverageProvider;
import org.netbeans.modules.python.project.GotoTest;
import org.netbeans.modules.python.project.PythonActionProvider;
import org.netbeans.modules.python.project.PythonProject;
import org.netbeans.modules.python.project.spi.TestRunner;
import org.netbeans.spi.gototest.TestLocator.LocationResult;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

import org.openide.util.Lookup;

public class RunSingleCommand extends Command {
    protected boolean isTest;

    public RunSingleCommand(PythonProject project, boolean isTest) {
        super(project);
        this.isTest = isTest;
    }

        
    @Override
    public String getCommandId() {
        return isTest ? ActionProvider.COMMAND_TEST_SINGLE : ActionProvider.COMMAND_RUN_SINGLE;
    }

    @Override
    public void invokeAction(Lookup context) throws IllegalArgumentException {
        Node[] activatedNodes = getSelectedNodes();
        DataObject gdo = activatedNodes[0].getLookup().lookup(DataObject.class);
        FileObject file = gdo.getPrimaryFile();
        if (file.getMIMEType().equals(PythonMIMEResolver.PYTHON_MIME_TYPE) ){
            String path = FileUtil.toFile(file.getParent()).getAbsolutePath();
            // String workingdir = FileUtil.toFile(getProject().getSrcFolder()).getAbsolutePath();
            //int pos = path.lastIndexOf("/");
            //path = path.substring(0, pos);
            String script = FileUtil.toFile(file).getAbsolutePath();
            //System.out.println("Folder " + path);
            //System.out.println("File " + script);

            final PythonProject pyProject = getProject();

            //String target = FileUtil.getRelativePath(getRoot(project.getSourceRoots().getRoots(),file), file);
            if (isTest || file.getName().endsWith("_test")) { // NOI18N

                // See if this looks like a test file; if not, see if we can find its corresponding
                // test
                boolean isTestFile = (file.getName().endsWith("_test"));
                if (!isTestFile) {
                    for (FileObject testRoot : pyProject.getTestSourceRootFiles()) {
                        if (FileUtil.isParentOf(testRoot, file)) {
                            isTestFile = true;
                            break;
                        }
                    }
                }
                if (!isTestFile) {
                    // Try to find the matching test
                    LocationResult result = new GotoTest().findTest(file, -1);
                    if (result != null && result.getFileObject() != null) {
                        file = result.getFileObject();
                    }
                }

                // Run test normally - don't pop up browser
                TestRunner testRunner = PythonActionProvider.getTestRunner(TestRunner.TestType.PY_UNIT);
                if (testRunner != null) {
                    testRunner.getInstance().runTest(file, false);
                    return;
                }
            }

            PythonExecution pyexec = new PythonExecution();
            pyexec.setDisplayName(gdo.getName());
            pyexec.setWorkingDirectory(path);
            if(PythonOptions.getInstance().getPromptForArgs()){
               String args =  JOptionPane.showInputDialog("Enter the args for this script.", "");
               pyexec.setScriptArgs(args);

            }
            final PythonPlatform platform = checkProjectPythonPlatform(pyProject);
            if ( platform == null )
              return ; // invalid platform user has been warn in check so safe to return
            pyexec.setCommand(platform.getInterpreterCommand());
            pyexec.setScript(script);
            pyexec.setCommandArgs(platform.getInterpreterArgs());
            pyexec.setPath(PythonPlatform.buildPath(super.buildPythonPath(platform, pyProject)));
            pyexec.setJavaPath(PythonPlatform.buildPath(super.buildJavaPath(platform, pyProject)));
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
    }

    @Override
    public boolean isActionEnabled(Lookup context) throws IllegalArgumentException {
        boolean results = false; //super.enable(activatedNodes);
        Node[] activatedNodes = getSelectedNodes();
        if(activatedNodes != null && activatedNodes.length > 0){
            DataObject gdo = activatedNodes[0].getLookup().lookup(DataObject.class);
            if(gdo != null && gdo.getPrimaryFile() != null)
                results = gdo.getPrimaryFile().getMIMEType().equals(
                        PythonMIMEResolver.PYTHON_MIME_TYPE);
        }
        return results;
    }


}
