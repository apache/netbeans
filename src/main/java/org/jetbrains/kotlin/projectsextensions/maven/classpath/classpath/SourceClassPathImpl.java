package org.jetbrains.kotlin.projectsextensions.maven.classpath.classpath;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.kotlin.projectsextensions.maven.MavenHelper;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.openide.util.Utilities;

/**
 *
 * @author  Milos Kleint 
 */
class SourceClassPathImpl extends AbstractProjectClassPathImpl {

    /**
     * Creates a new instance of SourceClassPathImpl
     */
    public SourceClassPathImpl(Project proj) {
        super(proj);
        NbMavenProject projectWatcher = MavenHelper.getProjectWatcher(proj);
        if (projectWatcher != null) {
            projectWatcher.addWatchedPath("target/generated-sources");
        }
    }
    
    @Override
    URI[] createPath() {
        Collection<URI> col = new ArrayList<URI>();
        col.addAll(Arrays.asList(ClassPathUtils.getSourceRoots(getMavenProject(), false)));
        col.addAll(Arrays.asList(ClassPathUtils.getGeneratedSourceRoots(getMavenProject(), false)));
        //#180020 remote items from resources that are either duplicate or child roots of source roots.
        List<URI> resources = new ArrayList<URI>(Arrays.asList(ClassPathUtils.getResources(getMavenProject(), false)));
        Iterator<URI> it = resources.iterator();
        while (it.hasNext()) {
            URI res = it.next();
            for (URI srcs : col) {
                if (res.toString().startsWith(srcs.toString())
                        && resources.contains(res)) {
                    it.remove();
                }
            }
        }
        URI webSrc = ClassPathUtils.getWebAppDirectory(getMavenProject());
        if (webSrc != null && Utilities.toFile(webSrc).exists()) {
            col.add(webSrc);
        }
        col.addAll(resources);
        URI[] uris = new URI[col.size()];
        uris = col.toArray(uris);
        return uris;        
    }

    @Override protected boolean includes(URL root, String resource) {
        return !resource.startsWith("archetype-resources/");
    }

}
