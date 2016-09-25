package org.jetbrains.kotlin.projectsextensions.maven.classpath.classpath;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.kotlin.projectsextensions.maven.MavenHelper;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;

/**
 *
 * @author  Milos Kleint 
 */
class TestSourceClassPathImpl extends AbstractProjectClassPathImpl {
    
    /**
     * Creates a new instance of TestSourceClassPathImpl
     */
    public TestSourceClassPathImpl(Project proj) {
        super(proj);
        NbMavenProject projectWatcher = MavenHelper.getProjectWatcher(proj);
        if (projectWatcher != null) {
            projectWatcher.addWatchedPath("target/generated-test-sources");
            projectWatcher.addWatchedPath("target/generated-sources"); // MCOMPILER-167
        }
    }
    
    @Override
    URI[] createPath() {
        Collection<URI> col = new ArrayList<URI>();
        col.addAll(Arrays.asList(ClassPathUtils.getSourceRoots(getMavenProject(), true)));
        col.addAll(Arrays.asList(ClassPathUtils.getGeneratedSourceRoots(getMavenProject(), true)));
        //#180020 remote items from resources that are either duplicate or child roots of source roots.
        List<URI> resources = new ArrayList<URI>(Arrays.asList(ClassPathUtils.getResources(getMavenProject(), true)));
        Iterator<URI> it = resources.iterator();
        while (it.hasNext()) {
            URI res = it.next();
            for (URI srcs : col) {
                if (res.toString().startsWith(srcs.toString())) {
                    it.remove();
                }
            }
        }
        col.addAll(resources);
        
        URI[] uris = new URI[col.size()];
        uris = col.toArray(uris);
        return uris;        
    }
    
}
