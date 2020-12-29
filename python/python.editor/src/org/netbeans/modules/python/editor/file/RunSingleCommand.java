package org.netbeans.modules.python.editor.file;

import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.netbeans.modules.python.api.PythonException;
import org.netbeans.modules.python.api.PythonExecution;
import org.netbeans.modules.python.api.PythonMIMEResolver;
import org.netbeans.modules.python.api.PythonOptions;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.api.PythonPlatformManager;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

@ServiceProvider(service = ActionProvider.class)
public class RunSingleCommand implements ActionProvider {
    private static final Logger LOG = Logger.getLogger(RunSingleCommand.class.getName());

    PythonPlatformManager manager = PythonPlatformManager.getInstance();

    public RunSingleCommand() {
    }

    private Node[] getSelectedNodes() {
        return TopComponent.getRegistry().getCurrentNodes();
    }
    
    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        Node[] activatedNodes = getSelectedNodes();
        DataObject gdo = activatedNodes[0].getLookup().lookup(DataObject.class);
        FileObject file = gdo.getPrimaryFile();
        if (file.getMIMEType().equals(PythonMIMEResolver.PYTHON_MIME_TYPE)) {
            String path = FileUtil.toFile(file.getParent()).getAbsolutePath();
            String script = FileUtil.toFile(file).getAbsolutePath();
            String shebang = null;
            try(Scanner sc = new Scanner(file.getInputStream())) {
                if(sc.hasNextLine()) {
                    shebang = sc.nextLine();
                }
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }

            PythonExecution pyexec = new PythonExecution();
            pyexec.setDisplayName(gdo.getName());
            pyexec.setWorkingDirectory(path);
            if (PythonOptions.getInstance().getPromptForArgs()) {
                String args = JOptionPane.showInputDialog("Enter the args for this script.", "");
                pyexec.setScriptArgs(args);

            }
            PythonPlatform platform = null;
            if (shebang != null && shebang.startsWith("#!")) {
                try {
                    platform = manager.findPlatformProperties(shebang.substring(2), null);
                } catch (PythonException ex) {
                    LOG.log(Level.WARNING, "Unable to get platform from shebang: " + shebang, ex);
                }
            }
            if(platform == null) {
                platform = manager.getPlatform(manager.getDefaultPlatform());
                if (platform == null) {
                    return; // invalid platform user has been warn in check so safe to return
                }
            }
            pyexec.setCommand(platform.getInterpreterCommand());
            pyexec.setScript(script);
            pyexec.setCommandArgs(platform.getInterpreterArgs());
            pyexec.setPath(PythonPlatform.buildPath(platform.getPythonPath()));
//            pyexec.setJavaPath(PythonPlatform.buildPath(super.buildJavaPath(platform, pyProject)));
            pyexec.setShowControls(true);
            pyexec.setShowInput(true);
            pyexec.setShowWindow(true);
            pyexec.addStandardRecognizers();

//            PythonCoverageProvider coverageProvider = PythonCoverageProvider.get(pyProject);
//            if (coverageProvider != null && coverageProvider.isEnabled()) {
//                pyexec = coverageProvider.wrapWithCoverage(pyexec);
//            }

            pyexec.run();
        }
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        boolean results = false; //super.enable(activatedNodes);
        Node[] activatedNodes = getSelectedNodes();
        if (activatedNodes != null && activatedNodes.length > 0) {
            DataObject gdo = activatedNodes[0].getLookup().lookup(DataObject.class);
            if (gdo != null && gdo.getPrimaryFile() != null) {
                results = gdo.getPrimaryFile().getMIMEType().equals(
                        PythonMIMEResolver.PYTHON_MIME_TYPE);
            }
        }
        return results;
    }

    @Override
    public String[] getSupportedActions() {
        return new String[] {ActionProvider.COMMAND_RUN_SINGLE};
    }
}
