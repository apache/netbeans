package org.black.kotlin.project;

import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.openide.util.ImageUtilities;

/**
* This class provides information about Kotlin project.
*/
public final class KotlinProjectInfo implements ProjectInformation {
    
        private final KotlinProject project;
    
        @StaticResource()
        public static final String KOTLIN_ICON = "org/black/kotlin/kotlin.png";

        public KotlinProjectInfo(KotlinProject project){
            this.project = project;
        }
        
        @Override
        public String getName() {
            return project.getProjectDirectory().getName();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public Icon getIcon() {
            return new ImageIcon(ImageUtilities.loadImage(KOTLIN_ICON));
        }

        @Override
        public Project getProject() {
            return project;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pl) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pl) {
        }

    }
