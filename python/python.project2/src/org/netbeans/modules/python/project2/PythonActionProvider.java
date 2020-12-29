package org.netbeans.modules.python.project2;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.python.project2.ui.actions.Command;
import org.netbeans.modules.python.project2.ui.actions.RunCommand;
import org.netbeans.modules.python.project2.ui.actions.RunSingleCommand;
import org.netbeans.spi.project.ActionProvider;
import org.openide.LifecycleManager;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

public class PythonActionProvider implements ActionProvider {

    private final Map<String, Command> commands;

    public PythonActionProvider(PythonProject2 project) {
        assert project != null;
        commands = new LinkedHashMap<>();
        Command[] commandArray = new Command[]{
            //            new DeleteCommand(project),
            //            new CopyCommand(project),
            //            new MoveCommand(project),
            //            new RenameCommand(project),
            //            new CleanCommand(project),
            new RunSingleCommand(project, false),
            new RunSingleCommand(project, true), // Run as Test
            new RunCommand(project, false),
            new RunCommand(project, true), // Run project as Test
        //            new DebugCommand(project) ,
        //            new DebugSingleCommand(project, false),
        //            new DebugSingleCommand(project, true), // Debug as Test
        //            new BuildCommand(project), //Build Egg
        //            new CleanBuildCommand(project) //Clean and Build Egg
        };
        for (Command command : commandArray) {
            commands.put(command.getCommandId(), command);
        }
    }

//    public static TestRunner getTestRunner(TestRunner.TestType testType) {
//        Collection<? extends TestRunner> testRunners = Lookup.getDefault().lookupAll(TestRunner.class);
//        for (TestRunner each : testRunners) {
//            if (each.supports(testType)) {
//                return each;
//            }
//        }
//        return null;
//    }
    @Override
    public String[] getSupportedActions() {
        final Set<String> names = commands.keySet();
        return names.toArray(new String[names.size()]);
    }

    @Override
    public void invokeAction(final String commandName, final Lookup context) throws IllegalArgumentException {
        final Command command = findCommand(commandName);
        assert command != null;
        if (command.saveRequired()) {
            LifecycleManager.getDefault().saveAll();
        }
        if (!command.asyncCallRequired()) {
            command.invokeAction(context);
        } else {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    command.invokeAction(context);
                }
            });
        }
    }

    @Override
    public boolean isActionEnabled(String commandName, Lookup context) throws IllegalArgumentException {
        final Command command = findCommand(commandName);
        assert command != null;
        return command.isActionEnabled(context);
    }

    private Command findCommand(final String commandName) {
        assert commandName != null;
        return commands.get(commandName);
    }

}
