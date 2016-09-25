package org.jetbrains.kotlin.projectsextensions.maven.classpath.classpath;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.jetbrains.kotlin.projectsextensions.maven.MavenHelper;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.classpath.FlaggedClassPathImplementation;
import org.openide.util.Utilities;

/**
 *
 * @author  Milos Kleint
 */
class CompileClassPathImpl extends AbstractProjectClassPathImpl implements FlaggedClassPathImplementation {

    private volatile boolean incomplete;

    /** Creates a new instance of SrcClassPathImpl */
    public CompileClassPathImpl(Project proj) {
        super(proj);
    }
    
    @Override
    URI[] createPath() {
        List<URI> lst = new ArrayList<URI>();
        // according the current 2.1 sources this is almost the same as getCompileClasspath()
        //except for the fact that multiproject references are not redirected to their respective
        // output folders.. we lways retrieve stuff from local repo..
        MavenProject originalProject = MavenHelper.getOriginalMavenProject(getMavenProject());
        if (originalProject == null) {
            return new URI[0];
        }
        List<Artifact> arts = originalProject.getCompileArtifacts();
        boolean broken = false;
        for (Artifact art : arts) {
            if (art.getFile() != null) {
                lst.add(Utilities.toURI(art.getFile()));
                broken |= !art.getFile().exists();
            } else {
              //NOPMD   //null means dependencies were not resolved..
                broken = true;
            } 
        }
        if (incomplete != broken) {
            incomplete = broken;
            firePropertyChange(PROP_FLAGS, null, null);
        }
        URI[] uris = new URI[lst.size()];
        uris = lst.toArray(uris);
        return uris;
    }

    @Override
    public Set<ClassPath.Flag> getFlags() {
        return incomplete ?
            EnumSet.of(ClassPath.Flag.INCOMPLETE) :
            Collections.<ClassPath.Flag>emptySet();
    }
}
