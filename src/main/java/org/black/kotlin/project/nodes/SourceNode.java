package org.black.kotlin.project.nodes;

import java.awt.Image;
import org.black.kotlin.project.KotlinProject;
import org.black.kotlin.project.KotlinSources;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.util.ImageUtilities;

/**
 * Node for source files.
 * @author Александр
 */
public class SourceNode extends FilterNode{
        
    private final static Image IMG =
            ImageUtilities.loadImage("org/black/kotlin/src_packages.png");
        
    public SourceNode(Project proj) throws DataObjectNotFoundException {
        super(PackageView.createPackageView((new KotlinSources((KotlinProject) proj))
                .getSourceGroupForFileObject(
                proj.getProjectDirectory().getFileObject("src"))));
    }
    
    @Override
    public String getDisplayName() {
        return "Source Packages";
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

