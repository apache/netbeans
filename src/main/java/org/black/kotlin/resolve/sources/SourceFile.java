package org.black.kotlin.resolve.sources;

/**
 *
 * @author Александр
 */
public class SourceFile {

    public final String path;
    public String effectivePackage;
    
    public SourceFile(String path){
        this.path = path;
        effectivePackage = null;
    }
    
}
