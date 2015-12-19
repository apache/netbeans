package org.black.kotlin.project.nodes;

import java.awt.Image;
import org.netbeans.api.project.Project;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Александр
 */
public class ImportantFilesNode extends FilterNode{
        
    private static final Image IMG =
            ImageUtilities.loadImage("org/black/kotlin/important_files.png");
        
    public ImportantFilesNode(Project proj) throws DataObjectNotFoundException {
        super(DataObject.find(proj.getProjectDirectory().getFileObject("nbproject")).getNodeDelegate());
    }
    
    @Override
    public String getDisplayName() {
        return "Important Files";
    }
    
    @Override
    public Image getIcon(int type) {
        return IMG;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return IMG;
    }
    
}
