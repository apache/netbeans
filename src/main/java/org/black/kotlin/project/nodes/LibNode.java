package org.black.kotlin.project.nodes;

import com.google.common.io.Files;
import java.awt.Event;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.black.kotlin.project.KotlinProject;
import org.black.kotlin.utils.ProjectUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Александр
 */
public class LibNode extends FilterNode{
        
    private static Image img =
            ImageUtilities.loadImage("org/black/kotlin/lib.png");
        
    private Project project;
    
    public LibNode(Project proj) throws DataObjectNotFoundException {
        super(DataObject.find(proj.getProjectDirectory().getFileObject("lib")).getNodeDelegate());
        project = proj;
    }
    
    @Override
    public String getDisplayName() {
        return "External Libraries";
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
    
    @Override
    public Action[] getActions(boolean context){
        return new Action[]{new AddJarAction()};
    }
    
    private class AddJarAction extends AbstractAction {

        public AddJarAction() {
            putValue (NAME, "Add .JAR");
        }

    @Override
    public void actionPerformed(ActionEvent e) {
        
//            try {
//                DialogDisplayer.getDefault().notify(new NotifyDescriptor.
//                        Message(ProjectUtils.getMainFileClass(project.getProjectDirectory().getChildren())));
//            } catch (IOException ex) {
//                Exceptions.printStackTrace(ex);
//            }

        
        JFileChooser fileChooser = new JFileChooser();
        FileFilter filter = new FileNameExtensionFilter("jar","JAR","jar");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(filter);
        int ret = fileChooser.showDialog(null, "Add");
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            FileObject libDir = project.getProjectDirectory().getFileObject("lib");
            String dest = libDir.getPath() + "\\" + file.getName();
            File destFile = new File(dest);
            if (!destFile.exists()){
                try {
                    destFile.createNewFile();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            try {
                Files.copy(file,destFile);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
    
}