package org.black.kotlin.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Александр
 */
public class KotlinClasspath {
    
    private static final String LIB_RUNTIME_NAME = "kotlin-runtime.jar";
    private static final String LIB_RUNTIME_SRC_NAME = "kotlin-runtime-sources.jar";
    private static final String KOTLIN_BIN_FOLDER = ProjectUtils.KT_HOME+"bin";
    
    public static List<String> getKotlinClasspath(){
        List<String> classpath = new ArrayList();
        
        classpath.add(ProjectUtils.KT_HOME + "lib\\"+LIB_RUNTIME_NAME);
        classpath.add(ProjectUtils.KT_HOME + "lib\\"+LIB_RUNTIME_SRC_NAME);
        
        return classpath;
    }
    
}
