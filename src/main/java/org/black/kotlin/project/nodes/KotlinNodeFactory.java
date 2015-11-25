package org.black.kotlin.project.nodes;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.black.kotlin.project.KotlinProject;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

@NodeFactory.Registration(projectType = "org-black-kotlin", position = 10)
public class KotlinNodeFactory implements NodeFactory {

    @Override
    public NodeList<?> createNodes(Project project) {
//        KotlinProject p = project.getLookup().lookup(KotlinProject.class);
//        assert p != null;
//        return new KotlinNodeList(p);
        try{
            KotlinFilterNode nd = new KotlinFilterNode(project);
            return NodeFactorySupport.fixedNodeList(nd);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return NodeFactorySupport.fixedNodeList();
    }

//    private class KotlinNodeList implements NodeList<Node> {
//
//        KotlinProject project;
//
//        public KotlinNodeList(KotlinProject project) {
//            this.project = project;
//        }
//
//        @Override
//        public List<Node> keys() {
//            FileObject projectFolder = project.getProjectDirectory();
//            
//            
//            List<Node> result = new ArrayList<Node>();
//            for (FileObject file : projectFolder.getChildren()){
//                try {
//                    if (!file.getNameExt().equals("build") && !file.getNameExt().equals("kotlin.ktproj"))
//                        result.add(DataObject.find(file).getNodeDelegate());
//                } catch (DataObjectNotFoundException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }
//            
//            
//            return result;
//        }
//
//        @Override
//        public Node node(Node node) {
//            return new FilterNode(node);
//        }
//
//        @Override
//        public void addNotify() {
//        }
//
//        @Override
//        public void removeNotify() {
//        }
//
//        @Override
//        public void addChangeListener(ChangeListener cl) {
//        }
//
//        @Override
//        public void removeChangeListener(ChangeListener cl) {
//        }
//        
//    }
    
}