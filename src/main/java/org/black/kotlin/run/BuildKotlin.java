package org.black.kotlin.run;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.black.kotlin.project.KotlinProject;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Kotlin",
        id = "org.black.kotlin.project.BuildKotlin"
)
@ActionRegistration(
        iconBase = "org/black/kotlin/project/maintainIcon16.png",
        displayName = "#CTL_BuildKotlin"
)
@ActionReferences({
    @ActionReference(path = "Menu/BuildProject", position = -90),
    @ActionReference(path = "Toolbars/Build", position = 100),//-20),
    @ActionReference(path = "Editors/text/x-kt/Popup", position = 1300),
    @ActionReference(path = "Projects/package/Actions", position = 500),
    @ActionReference(path = "Loaders/text/x-kt/Actions", position = 500)
})
@Messages("CTL_BuildKotlin=Build Kotlin Project")
public final class BuildKotlin implements ActionListener {

    private final List<KotlinProject> context;

    public BuildKotlin(List<KotlinProject> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        for (KotlinProject kotlinProject : context) {
            String file;
            try {
                file = findMain(kotlinProject.getProjectDirectory().getChildren());
                NotifyDescriptor nd = new NotifyDescriptor.Message(file);
                DialogDisplayer.getDefault().notify(nd);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }

    private String findMain(FileObject[] files) throws FileNotFoundException, IOException {
        for (FileObject file : files) {
            if (!file.isFolder()) {
                for (String line : file.asLines()) {
                    if (line.contains("fun main(")) {
                        return file.getPath();
                    }
                }
            } else {
                String main = findMain(file.getChildren());
                if (main != null) {
                    return main;
                }
            }
        }
        
        return null;
    }

}
