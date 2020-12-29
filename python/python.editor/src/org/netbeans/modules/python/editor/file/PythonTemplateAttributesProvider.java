/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.python.editor.file;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.python.api.PythonMIMEResolver;
import org.netbeans.modules.python.source.queries.SourceLevelQuery;
import org.openide.filesystems.FileObject;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = CreateFromTemplateAttributesProvider.class)
public class PythonTemplateAttributesProvider implements CreateFromTemplateAttributesProvider {

    private static final Logger LOG = Logger.getLogger(PythonTemplateAttributesProvider.class.getName());
    private static final SpecificationVersion VER30 = new SpecificationVersion("3.0");
    
    @Override
    public Map<String, ?> attributesFor(DataObject template, DataFolder target, String name) {
        FileObject templateFO = template.getPrimaryFile();
        if (!PythonMIMEResolver.PYTHON_EXTENSION.equals(templateFO.getExt()) || templateFO.isFolder()) {
            return null;
        }
        
        FileObject targetFO = target.getPrimaryFile();
        Map<String,Object> result = new HashMap<>();
        
        ClassPath cp = ClassPath.getClassPath(targetFO, ClassPath.SOURCE);
        if (cp == null) {
            LOG.log(Level.WARNING, "No classpath was found for folder: {0}", target.getPrimaryFile()); // NOI18N
        }
        else {
            result.put("package", cp.getResourceName(targetFO, '.', false)); // NOI18N
        }
        
        String sourceLevel = SourceLevelQuery.getSourceLevel(targetFO);
        if (sourceLevel != null) {
            result.put("pythonSourceLevel", sourceLevel); // NOI18N
            if (isPython3orLater(sourceLevel))
                result.put("python3style", Boolean.TRUE); // NOI18N
        }
        
        return result;
    }

    private boolean isPython3orLater(String sourceLevel) {
        SpecificationVersion ver = new SpecificationVersion(sourceLevel);
        return (ver.compareTo(VER30) >= 0);
    }
}
