package org.black.kotlin.projectsextensions.maven.lookup;

import org.black.kotlin.projectsextensions.maven.MavenProjectOpenedHook;
import org.black.kotlin.projectsextensions.maven.buildextender.PomXmlModifier;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
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
        
        return Lookups.fixed(new MavenProjectPrivilegedTemplates(),
                new MavenProjectOpenedHook(project),
                new PomXmlModifier(project)
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