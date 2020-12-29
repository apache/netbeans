/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.python.source.impl;

import org.netbeans.modules.python.source.queries.SourceLevelQueryImplementation;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = SourceLevelQueryImplementation.class, position = 400)
public class PythonProjectSourceLevelQuery implements SourceLevelQueryImplementation {

    @Override
    public Result getSourceLevel(FileObject pythonFile) {
        final Project project = FileOwnerQuery.getOwner(pythonFile);
        if (project != null) {
            SourceLevelQueryImplementation impl = project.getLookup().lookup(SourceLevelQueryImplementation.class);
            if (impl != null) {
                return impl.getSourceLevel(pythonFile);
            }
        }
        return null;
    }

}
