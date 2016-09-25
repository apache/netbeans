package org.jetbrains.kotlin.projectsextensions.maven.classpath.classpath;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.jetbrains.kotlin.projectsextensions.maven.MavenHelper;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.FileUtilities;
import org.openide.util.Utilities;


/**
 *
 * @author  Milos Kleint 
 */
public class TestRuntimeClassPathImpl extends AbstractProjectClassPathImpl {
    
    /**
     * Creates a new instance of TestRuntimeClassPathImpl
     */
    public TestRuntimeClassPathImpl(Project proj) {
        super(proj);
    }

    @Override
   URI[] createPath() {
       MavenProject originalProject = MavenHelper.getOriginalMavenProject(getMavenProject());
       if (originalProject == null) {
           return new URI[0];
       }
        List<URI> lst = createPath(originalProject);
        URI[] uris = new URI[lst.size()];
        uris = lst.toArray(uris);
        return uris;
   }
    
   public static List<URI>createPath(MavenProject prj) {
       assert prj != null;
        List<URI> lst = new ArrayList<URI>();
        Build build = prj.getBuild();
        if (build != null) {
            String testOutputDirectory = build.getTestOutputDirectory();
            if (testOutputDirectory != null) {
                lst.add(FileUtilities.convertStringToUri(testOutputDirectory));
            }
            String outputDirectory = build.getOutputDirectory();
            if (outputDirectory != null) {
                lst.add(FileUtilities.convertStringToUri(outputDirectory));
            }
        }
        List<Artifact> arts = prj.getTestArtifacts();
        for (Artifact art : arts) {
            if (art.getFile() != null) {
                lst.add(Utilities.toURI(art.getFile()));
            } else {
              //NOPMD   //null means dependencies were not resolved..
            }
        }
        return lst;
    }    
    
}
