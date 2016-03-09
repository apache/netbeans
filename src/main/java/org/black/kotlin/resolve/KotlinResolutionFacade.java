package org.black.kotlin.resolve;

import org.jetbrains.kotlin.container.ComponentProvider;
import org.jetbrains.kotlin.descriptors.ModuleDescriptor;
//import org.jetbrains.kotlin.idea.resolve.ResolutionFacade;
import org.netbeans.api.project.Project;

/**
 *
 * @author Александр
 */
public class KotlinResolutionFacade { //implements ResolutionFacade {
    
    private final Project javaProject;
    private final ComponentProvider componentProvider;
    
    public KotlinResolutionFacade(Project javaProject, ComponentProvider componentProvider, 
            ModuleDescriptor moduleDescriptor){
        this.javaProject = javaProject;
        this.componentProvider = componentProvider;
        
    }
    
}
