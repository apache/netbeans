package org.black.kotlin.j2seprojectextension.buildextender;

import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectUtil;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinBuildExtender {
    
    public KotlinBuildExtender(Project project) {
        if (!(project instanceof J2SEProject)) {
            return;
        }
//        FileObject projDir=project.getProjectDirectory();
//        final FileObject buildXmlFO=J2SEProjectUtil.getBuildXml((J2SEProject)project);
//        if (buildXmlFO == null) {
//            return;
//        }
        
        AntBuildExtender buildExtender = project.getLookup().lookup(AntBuildExtender.class);
        List<String> targets = buildExtender.getExtensibleTargets();
        AntBuildExtender.Extension extension = buildExtender.getExtension(targets.get(4));
        System.out.println();
    }
    
}
