package org.black.kotlin.j2seprojectextension.lookup;

import org.black.kotlin.j2seprojectextension.buildextender.KotlinBuildExtender;
import org.black.kotlin.j2seprojectextension.classpath.J2SEExtendedClassPathProvider;
import org.black.kotlin.project.KotlinProjectOpenedHook;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.spi.java.classpath.ClassPathProvider;
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
        
        ClassPathProviderImpl provider = j2seProject.getClassPathProvider();
        J2SEExtendedClassPathProvider myProvider = new J2SEExtendedClassPathProvider(j2seProject);
        
        return Lookups.fixed(
                new KotlinPrivilegedTemplates(),
                new KotlinBuildExtender(lkp.lookup(Project.class)),
                myProvider);
//                new KotlinProjectOpenedHook(lkp.lookup(Project.class)));
    }
    
    private static final class KotlinPrivilegedTemplates implements PrivilegedTemplates {

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
