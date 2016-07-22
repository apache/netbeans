package org.black.kotlin.projectsextensions.maven.lookup;

import org.black.kotlin.projectsextensions.KotlinPrivilegedTemplates;
import org.black.kotlin.projectsextensions.maven.MavenProjectOpenedHook;
import org.black.kotlin.projectsextensions.maven.classpath.MavenClassPathProviderImpl;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Alexander.Baratynski
 */
public class MavenProjectLookupProviderExtension implements LookupProvider {

    @Override
    public Lookup createAdditionalLookup(Lookup lkp) {
        NbMavenProjectImpl project = lkp.lookup(NbMavenProjectImpl.class);
        
        return Lookups.fixed(new KotlinPrivilegedTemplates(),
                new MavenProjectOpenedHook(project),
                new MavenClassPathProviderImpl(project)
        );
    }

}