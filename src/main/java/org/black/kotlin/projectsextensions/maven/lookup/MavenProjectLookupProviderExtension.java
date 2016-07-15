package org.black.kotlin.projectsextensions.maven.lookup;

import java.util.List;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Alexander.Baratynski
 */
public class MavenProjectLookupProviderExtension implements LookupProvider {

    @Override
    public Lookup createAdditionalLookup(Lookup lkp) {
        
//        NbMavenProjectImpl impl = lkp.lookup(NbMavenProjectImpl.class);
//        try {
//            List<String> deps = impl.getOriginalMavenProject().getCompileClasspathElements();
//            List<String> deps2 = impl.getOriginalMavenProject().getBuildExtensions();
//        } catch (DependencyResolutionRequiredException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//        
        return Lookups.fixed(
                new MavenProjectPrivilegedTemplates()
        );
    }
    
    
    private static final class MavenProjectPrivilegedTemplates implements PrivilegedTemplates {

        private static final String[] PRIVILEGED_NAMES = new String[]{
            "Templates/Kotlin/content.kt",
            "Templates/Classes/Class.java",
            "Templates/Classes/Package", 
            "Templates/Classes/Interface.java", 
            "Templates/GUIForms/JPanel.java", 
            "Templates/GUIForms/JFrame.java" 
        };

        @Override
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }

    }
}