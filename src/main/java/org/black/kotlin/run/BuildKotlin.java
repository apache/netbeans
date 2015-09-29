package org.black.kotlin.run;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import org.black.kotlin.project.KotlinProject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
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
                
            try {
              //                NotifyDescriptor nd2 = new NotifyDescriptor.Message(context.get(0).getProjectDirectory().getPath()+"/build/output/hello.jar");
              //                DialogDisplayer.getDefault().notify(nd2);
                KotlinCompiler.INSTANCE.compile(kotlinProject);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }


    

    
}
