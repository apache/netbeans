package org.black.kotlin.project.nodes;

import java.awt.Image;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Александр
 */
public class SourceNode extends FilterNode{
        
    private static Image img =
            ImageUtilities.loadImage("org/black/kotlin/src_packages.png");
        
    public SourceNode(Project proj) throws DataObjectNotFoundException {
        super(DataObject.find(proj.getProjectDirectory().getFileObject("src")).getNodeDelegate());
    }
    
    @Override
    public String getDisplayName() {
        return "Source Packages";
    }
    
    @Override
    public Image getIcon(int type) {
        DataFolder root = DataFolder.findFolder(FileUtil.getConfigRoot());
//        Image original = root.getNodeDelegate().getIcon(type);
        return img;
//        return ImageUtilities.mergeImages(original, smallImage, 7, 7);
    }

    @Override
    public Image getOpenedIcon(int type) {
        DataFolder root = DataFolder.findFolder(FileUtil.getConfigRoot());
//        Image original = root.getNodeDelegate().getIcon(type);
        return img;
//    return ImageUtilities.mergeImages(original, smallImage, 7, 7);
    }
    
}

