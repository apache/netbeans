package org.black.kotlin.projectsextensions;

import org.netbeans.api.java.classpath.ClassPath;

/**
 *
 * @author Alexander.Baratynski
 */
public interface ClassPathExtender {
    public ClassPath getProjectSourcesClassPath(String type);
}
