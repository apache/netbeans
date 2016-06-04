package org.black.kotlin.project;

import java.io.IOException;
import org.black.kotlin.run.KotlinCompiler;
import org.black.kotlin.utils.ProjectUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Action provider class for Kotlin project.
 */
public final class KotlinActionProvider implements ActionProvider {

    private final KotlinProject project;
    
    public KotlinActionProvider(KotlinProject project){
        this.project = project;
    }
    
    /**
     * Supported actions.
     */
    private final String[] supported = new String[]{
        ActionProvider.COMMAND_DELETE,
        ActionProvider.COMMAND_COPY,
        ActionProvider.COMMAND_BUILD,
        ActionProvider.COMMAND_CLEAN,
        ActionProvider.COMMAND_REBUILD,
        ActionProvider.COMMAND_RUN
    };

    /**
     *
     * @return supported actions.
     */
    @Override
    public String[] getSupportedActions() {
        return supported;
    }

    /**
     * Defines actions code.
     *
     * @throws IllegalArgumentException
     */
    @Override
    public void invokeAction(String string, Lookup lookup) throws IllegalArgumentException {
        if (string.equalsIgnoreCase(ActionProvider.COMMAND_DELETE)) {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
        }
        if (string.equalsIgnoreCase(ActionProvider.COMMAND_COPY)) {
            DefaultProjectOperations.performDefaultCopyOperation(project);
        }
        if (string.equalsIgnoreCase(ActionProvider.COMMAND_BUILD)) {
            KotlinCompiler.INSTANCE.antBuild(project);
        }

        if (string.equalsIgnoreCase(ActionProvider.COMMAND_CLEAN)) {
            Thread newThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    ProjectUtils.clean(project);
                }

            });
            newThread.start();
        }

        if (string.equalsIgnoreCase(ActionProvider.COMMAND_REBUILD)) {
            ProjectUtils.clean(project);
            KotlinCompiler.INSTANCE.antBuild(project);
        }

        if (string.equalsIgnoreCase(ActionProvider.COMMAND_RUN)) {

            try {
                KotlinCompiler.INSTANCE.antRun(project);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }

    /**
     *
     * @return is action enabled or not.
     * @throws IllegalArgumentException
     */
    @Override
    public boolean isActionEnabled(String command, Lookup lookup) throws IllegalArgumentException {
        if ((command.equals(ActionProvider.COMMAND_DELETE))) {
            return true;
        } else if ((command.equals(ActionProvider.COMMAND_COPY))) {
            return true;
        } else if ((command.equals(ActionProvider.COMMAND_BUILD))) {
            return true;
        } else if ((command.equals(ActionProvider.COMMAND_CLEAN))) {
            return true;
        } else if ((command.equals(ActionProvider.COMMAND_REBUILD))) {
            return true;
        } else if ((command.equals(ActionProvider.COMMAND_RUN))) {
            return true;
        } else {
            throw new IllegalArgumentException(command);
        }
    }

}
