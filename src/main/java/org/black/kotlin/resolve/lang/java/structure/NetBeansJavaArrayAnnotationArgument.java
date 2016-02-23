package org.black.kotlin.resolve.lang.java.structure;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationArgument;
import org.jetbrains.kotlin.load.java.structure.JavaArrayAnnotationArgument;
import org.jetbrains.kotlin.name.Name;
import org.netbeans.api.project.Project;

/**
 *
 * @author Александр
 */
public class NetBeansJavaArrayAnnotationArgument implements JavaArrayAnnotationArgument {

    private final Name name;
    private final Project project;
    private final Collection<?> arguments;
    
    public NetBeansJavaArrayAnnotationArgument(Collection<?> arguments, Name name, Project project){
        this.name = name;
        this.project = project;
        this.arguments = arguments;
    }
    
    @Override
    public List<JavaAnnotationArgument> getElements() {
        List<JavaAnnotationArgument> argumentList = Lists.newArrayList();
        for (Object obj : arguments){
            argumentList.add(NetBeansJavaAnnotationArgument.create(obj, name, project));
        }
        
        return argumentList;
        
    }

    @Override
    public Name getName() {
        return name;
    }
    
    
    
}
