package org.black.kotlin.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Александр
 */
public class KotlinClasspath {
    
    private static final String LIB_RUNTIME_NAME = "kotlin-runtime.jar";
    //private static final String LIB_RUNTIME_SRC_NAME = "kotlin-runtime-sources.jar";
    
    public static List<String> getKotlinClasspath(){
        List<String> classpath = new ArrayList<String>();
        
        classpath.add(ProjectUtils.KT_HOME + "lib\\" + LIB_RUNTIME_NAME);
        
        return classpath;
    }
    
    public static String getKotlinBootClasspath(){
        return ProjectUtils.KT_HOME + "lib\\" + LIB_RUNTIME_NAME;
    }
    
}
