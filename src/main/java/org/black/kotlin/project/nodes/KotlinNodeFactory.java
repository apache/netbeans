package org.black.kotlin.project.nodes;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 * Kotlin project nodes factory.
 * @author Александр
 */
@NodeFactory.Registration(projectType = "org-black-kotlin", position = 10)
public class KotlinNodeFactory implements NodeFactory {

    /**
     * This method creates nodes of the project.
     * @param project Kotlin project
     * @return list of project nodes.
     */
    @Override
    public NodeList<?> createNodes(Project project) {
        try{
            SourceNode srcNode = new SourceNode(project);
            ImportantFilesNode nd = new ImportantFilesNode(project);
            LibNode libNode = new LibNode(project);
            
            return NodeFactorySupport.fixedNodeList(srcNode,nd, libNode);
        
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return NodeFactorySupport.fixedNodeList();
    }
    
}