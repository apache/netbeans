package org.black.kotlin.projectsextensions.j2se.lookup;

import org.black.kotlin.projectsextensions.j2se.J2SEProjectOpenedHook;
import org.black.kotlin.projectsextensions.j2se.J2SEProjectPropertiesModifier;
import org.black.kotlin.projectsextensions.j2se.classpath.J2SEExtendedClassPathProvider;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Alexander.Baratynski
 */
public class LookupProviderExtension implements LookupProvider{

    @Override
    public Lookup createAdditionalLookup(Lookup lkp) {
        
        J2SEProject j2seProject = lkp.lookup(J2SEProject.class);
        J2SEExtendedClassPathProvider myProvider = new J2SEExtendedClassPathProvider(j2seProject);
        J2SEProjectPropertiesModifier propertiesModifier = new J2SEProjectPropertiesModifier(j2seProject);
        
        return Lookups.fixed(
                new KotlinPrivilegedTemplates(),
                myProvider,
                propertiesModifier,
                new J2SEProjectOpenedHook(j2seProject));
    }
    
    private static final class KotlinPrivilegedTemplates implements PrivilegedTemplates {

        private static final String[] PRIVILEGED_NAMES = new String[]{
            "Templates/Kotlin/class.kt",
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
