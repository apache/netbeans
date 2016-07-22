package org.black.kotlin.file.wizard.packagechooser;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.openide.WizardDescriptor;

/**
 *
 * @author Alexander.Baratynski
 */
public class PackageChooser {
    
    public static String pack = null;
    
    public static TargetChooserPanel createPackageChooser(Project project, SourceGroup[] folders, 
        WizardDescriptor.Panel<WizardDescriptor> bottomPanel, String type) {
        if (folders.length == 0) {
            throw new IllegalArgumentException("No folders selected");
        }
        return new TargetChooserPanel(project, folders, bottomPanel, Type.FILE, false, type);
    }
    
}
