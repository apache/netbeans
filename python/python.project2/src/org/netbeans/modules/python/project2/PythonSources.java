/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.python.project2;

import javax.swing.event.ChangeListener;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;

public class PythonSources implements Sources {
    private final PythonProject2 project;
    private SourceGroup[] roots;
    

    public PythonSources(PythonProject2 project) {
        this.project = project;
    }

    @Override
    public SourceGroup[] getSourceGroups(String type) {
        synchronized(this) {
            if(roots == null) {
                FileObject fo = project.getProjectDirectory();
                roots = new SourceGroup[]{GenericSources.group(project, fo, fo.getPath(), "Source Packages", null, null)};
            }
        }
        return roots;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
    }
}
