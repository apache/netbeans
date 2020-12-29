package org.netbeans.modules.python.console;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import org.netbeans.modules.python.api.PythonExecution;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.api.PythonPlatformManager;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.ImageUtilities;

/**
 * Action which shows PythonConsole component.
 */
public class PythonConsoleAction extends AbstractAction {
    public static String ICON_PATH = "org/netbeans/modules/python/console/actions/pyConsole.png";
    public PythonConsoleAction() {
        super(NbBundle.getMessage(PythonConsoleAction.class, "CTL_PythonConsoleAction"));
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_PATH, true));
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
//        PythonConsoleTopComponent win = PythonConsoleTopComponent.findInstance();
//        if (win.nTerm() > 1)
//            win.newTab();
//        else{
//            win.open();
//        }
//        win.requestActive();
          PythonPlatformManager manager = PythonPlatformManager.getInstance();
          PythonPlatform platform = manager.getPlatform(manager.getDefaultPlatform());
          String command = platform.getInterpreterConsoleComand();
          File info = InstalledFileLocator.getDefault().locate(
                 "console.py", "org.netbeans.modules.python.console", false);
          String script = info.getAbsolutePath();
          PythonExecution pye = new PythonExecution();
          pye.setCommand(command);
          pye.setCommandArgs(platform.getInterpreterArgs());
          pye.setDisplayName("Python Interactive Console");
          pye.setScript(script);
          pye.setScriptArgs("-p");
          pye.setRedirectError(true);
          pye.setWorkingDirectory(script.substring(0, script.lastIndexOf(File.separator)));
          pye.run();
    }
}
