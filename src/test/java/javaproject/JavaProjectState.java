package javaproject;

import org.netbeans.spi.project.ProjectState;

/**
 *
 * @author Alexander.Baratynski
 */
public class JavaProjectState implements ProjectState{

    @Override
    public void markModified() {}

    @Override
    public void notifyDeleted() throws IllegalStateException {}
    
}
