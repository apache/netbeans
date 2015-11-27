package org.black.kotlin.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Александр
 */
public class KotlinClasspath {
    
    private static String LIB_RUNTIME_NAME = "kotlin-runtime.jar";
    private static String LIB_RUNTIME_SRC_NAME = "kotlin-runtime-sources.jar";
    private static String LIB_REFLECT_NAME = "kotlin-reflect.jar";
//    private static String LIB_RUNNER_NAME = "kotlin-runner.jar";
//    private static String LIB_KOTLINR_NAME = "kotlinr.jar";
//    private static String LIB_KOTLIN_PRELOADER = "kotlin-preloader.jar";
//    private static String LIB_KOTLIN_JSLIB_NAME = "kotlin-jslib.jar";
//    private static String LIB_KOTLIN_JSLIB_SOURCES_NAME = "kotlin-jslib-sources.jar";
    private static String KOTLIN_BIN_FOLDER = ProjectUtils.KT_HOME+"bin";
    
    public static List<String> getKotlinClasspath(){
        List<String> classpath = new ArrayList();
        
//        classpath.add(KOTLIN_BIN_FOLDER);
//        classpath.add(ProjectUtils.KT_HOME + "lib\\");
        classpath.add(ProjectUtils.KT_HOME + "lib\\"+LIB_RUNTIME_NAME);
        classpath.add(ProjectUtils.KT_HOME + "lib\\"+LIB_RUNTIME_SRC_NAME);
        classpath.add(ProjectUtils.KT_HOME + "lib\\"+LIB_REFLECT_NAME);
        
        return classpath;
    }
    
}
