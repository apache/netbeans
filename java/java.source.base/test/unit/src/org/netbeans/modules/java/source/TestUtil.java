/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.source;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.text.Document;
import junit.framework.TestCase;
import static junit.framework.TestCase.assertFalse;
import org.netbeans.ProxyURLStreamHandlerFactory;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.spi.editor.document.DocumentFactory;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.util.BaseUtilities;
import org.openide.util.Utilities;


/**
 *
 * @author Petr Hrebejk
 */
public class TestUtil {
        
    public static final String RT_JAR = "jre/lib/rt.jar";
    public static final String SRC_ZIP = "src.zip";
    private static final String MAC_SRC_ZIP = "Home/src.jar";   //NOI18N
    
    /** Creates a new instance of TestUtil */
    private TestUtil() {
    }
    
    public static void copyFiles( File destDir, String... resourceNames ) throws IOException {
        copyFiles(getDataDir(), destDir, resourceNames);
    }
    
    public static void copyContents(File sourceDir, File destDir) throws IOException {
        File[] files = sourceDir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                File newDir = new File(destDir, f.getName());
                newDir.mkdirs();
                
                copyContents(f, newDir);
            } else {
                    copyFiles(sourceDir, destDir, f.getName());
            }
        }
    }
    
    public static void copyFiles( File sourceDir, File destDir, String... resourceNames ) throws IOException {

        for( String resourceName : resourceNames ) {
            
            File src = new File( sourceDir, resourceName ); 
            
            if ( !src.canRead() ) {
                TestCase.fail( "The test requires the file: " + resourceName + " to be readable and stored in: " + sourceDir );
            }
            
            InputStream is = new FileInputStream( src );            
            BufferedInputStream bis = new BufferedInputStream( is );
                        
            File dest = new File( destDir, resourceName );            
            File parent = dest.getParentFile();
            
            if ( !parent.exists() ) {
                parent.mkdirs();
            }
            
            dest.createNewFile();
            BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( dest ) );
            
            copyFile( bis, bos );
        }
    }
    
    public static File createRT_JAR(File destDir) throws IOException {
        File src = new File( TestUtil.getJdkDir(), TestUtil.RT_JAR );

        if ( src.canRead() ) {
            TestUtil.copyFiles( TestUtil.getJdkDir(), destDir, TestUtil.RT_JAR );
            return new File( destDir, TestUtil.RT_JAR );
        }

        File rtJar = new File(destDir, "rt.jar");
        Set<String> seenPackages = new HashSet<>();

        try (OutputStream binOut = new FileOutputStream(rtJar);
             JarOutputStream out = new JarOutputStream(binOut)) {
            for (FileObject root : BootClassPathUtil.getBootClassPath().getRoots()) {
                Enumeration<? extends FileObject> en = root.getChildren(true);

                while (en.hasMoreElements()) {
                    FileObject f = en.nextElement();

                    if (f.isFolder()) {
                        String name = FileUtil.getRelativePath(root, f) + "/";
                        if (seenPackages.add(name)) {
                            out.putNextEntry(new ZipEntry(name));
                        }
                    } else {
                        if (!f.getNameExt().equals("module-info.class")) {
                            out.putNextEntry(new ZipEntry(FileUtil.getRelativePath(root, f)));
                            try (InputStream in = f.getInputStream()) {
                                FileUtil.copy(in, out);
                            }
                        }
                    }
                }
            }
        }

        return rtJar;
    }

    public static void unzip( ZipFile zip, File dest ) throws IOException {
        
        for( Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements(); ) {
            ZipEntry entry = e.nextElement();
            File f = new File( dest, entry.getName() );
            if ( entry.isDirectory() ) {
                f.mkdirs();
            }
            else {
                f.getParentFile().mkdirs();
                f.createNewFile();
                BufferedInputStream bis = new BufferedInputStream( zip.getInputStream( entry ) );            
                BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( f ) );            
                copyFile( bis, bos );
            }
        }
        
    }
    
    public static FileFilter createExtensionFilter( boolean folders, final String ... extensions ) {
        return new ExtensionFileFilter( folders, extensions );            
    }
    
    /** Good for debuging content of large collections.
     * Prints out readable diff of the collections passed as parameters.
     */    
    public static String collectionDiff( Iterable c1, Iterable c2 ) {
        return collectionDiff( c1.iterator(), c2.iterator() );
    }
    
    public static String collectionDiff( Iterator it1, Iterator it2 ) {
        
          StringBuilder sb = new StringBuilder();  
        
          int index = 0;
          boolean printing = false;
          while( it1.hasNext() ) {
                 
             Object o1 = it1.next();
             
             Object o2 = it2.hasNext() ? it2.next() : null ; 

             if ( !o1.equals( o2 ) ) {
                 if ( !printing ) {
                     printing = true;
                     sb.append("\n");
                 }
                 sb.append( index + " " + o1 + " -> " + ( o2 == null ? "NULL" : o2 )  + "\n" );
             } 
             else if ( printing ) {
                 printing = false;
             }
             
             index++;
          }
          
          if ( it2.hasNext() ) {
              sb.append( "\n" );
          }
          while( it2.hasNext() ) {
              sb.append( index + "   [NULL]" + " -> " + it2.next()  + "\n" );
              index ++;
          }
                  
          return sb.toString();
    }
    
    public static String fileToString( File file ) throws IOException {
        
        BufferedReader reader = new BufferedReader( new FileReader( file ) );
        StringBuffer sb = new StringBuffer();
        
        for( String line = reader.readLine(); line != null; line = reader.readLine() ) {
            //System.out.println("L " + line);
            sb.append( line ).append( "\n" ); 
        }
        
        return sb.toString();
    }
    
    /** Returns the tests data folder. Containing sample classes
     */
    public static File getDataDir() {
        return TemporaryTestCase.getDataFolder();
    }
    
    /** Returns current jdk directory
     */    
    public static File getJdkDir() {
	
	Properties p = System.getProperties();	
	String javaHomeProp = p.getProperty( "java.home" );
	
	if ( javaHomeProp == null ) {
	    throw new IllegalStateException( "Can't find java.home property ");
	}
	else {
	    File jre = new File( javaHomeProp );
	    if ( !jre.canRead() ) {
		throw new IllegalStateException( "Can't read " + jre );
	    }
	    File dir = jre.getParentFile();
	    if ( !jre.canRead() ) {
		throw new IllegalStateException( "Can't read " + dir);
	    }
	    return dir;
	}
    }
    
    
    /** Returns given JDK file 
     * @param path Relative path to the JDK file.
     * @return the file 
     * @throws IllegalArgumentException if the file can't be found or read.
     */
    public static File getJdkFile( String path ) {
	File dir = getJdkDir();
	
	File f = new File( dir, path );
	
	if ( f.canRead() ) {
	    return f;
	}  
	else {
	    throw new IllegalArgumentException( "Can't read file " + f );
	}
		
    }
    
    public static File getJdkSources() {
        try {
            return getJdkFile(SRC_ZIP);
        } catch (IllegalArgumentException iae) {
            if (Utilities.isMac()) {
                return getJdkFile(MAC_SRC_ZIP);
            } else {
                throw iae;
            }
        }
    }

    public static void setJavaFileFilter(JavaFileFilterImplementation impl) {
        JavaFileFilterQuery.setTestFileFilter(impl);
    }
    
    // Private methods ---------------------------------------------------------
    
    private static int BLOCK_SIZE = 16384;
    
    private static void copyFile( InputStream is, OutputStream os ) throws IOException {
        byte[] b = new byte[ BLOCK_SIZE ];   
        int count = is.read(b);     

        while (count != -1)
        {
         os.write(b, 0, count);
         count = is.read(b);
        }

        is.close();
        os.close();
    }
    
//    public static void printInsane( Object... roots ) {
//        
//        System.gc(); 
//        System.gc();
//        System.out.println( "FREE MEMORY :" + ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) );
//        System.out.println("");
//        
//        final CountingVisitor cv = new CountingVisitor();
//        try {
//            ScannerUtils.scan( null, cv, Arrays.asList( roots ), true );
//        }
//        catch ( Exception e ) {
//            e.printStackTrace();
//        }
//
//        Set ordered = new TreeSet(new Comparator() {
//            public int compare(Object c1, Object c2) {
//                int diff = cv.getSizeForClass((Class)c2)
//                           cv.getSizeForClass((Class)c1);
//
//                if (diff != 0 || c1 == c2) return diff;
//                return ((Class)c1).getName().compareTo(((Class)c2).getName());
//            }
//        });
//
//        ordered.addAll(cv.getClasses());
//
//        System.out.println("Usage: [instances class.Name: totalSizeInBytes]");
//        for (Iterator it = ordered.iterator(); it.hasNext();) {
//            Class cls = (Class)it.next();
//            System.out.println(cv.getCountForClass(cls) + " " +
//                            cls.getName() + ": " + cv.getSizeForClass(cls));
//        }
//
//        System.out.println("total: " + cv.getTotalSize() + " in " +
//            cv.getTotalCount() + " objects.");
//        System.out.println("Classes:" + cv.getClasses().size());
//
//            
//        
//        
//    }
    
    
      // Private innerclasses --------------------------------------------------
    
      private static class ExtensionFileFilter implements FileFilter {
          
          private boolean folders;
          private String[] extensions;
          
          public ExtensionFileFilter( boolean folders, String... extensions ) {
              this.folders = folders;
              this.extensions = extensions;
          }
          
                    
          public boolean accept( File file ) {
          
              if ( folders && file.isDirectory() ) {
                  return true;
              }
                            
              for( String ext : extensions ) {
                  if ( file.getName().endsWith( ext ) ) {
                      return true;
                  }                  
              }
              
              return false;
          }
          
          
      }
      
      private static class TemporaryTestCase extends NbTestCase {
    
          private static TemporaryTestCase INSTANCE = new TemporaryTestCase();
          
          TemporaryTestCase() {
              super( TemporaryTestCase.class.toString() );
          }
          
          public static File getDataFolder() {
              return INSTANCE.getDataDir();
          }
          
      }
      

      public static void setupEditorMockServices() {
        MockMimeLookup.setInstances(MimePath.EMPTY, new DocumentFactory() {

            @Override
            public Document getDocument(FileObject file) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public FileObject getFileObject(Document document) {
                Object sdp = document.getProperty(Document.StreamDescriptionProperty);
                if (sdp instanceof FileObject) {
                    return (FileObject)sdp;
                }
                if (sdp instanceof DataObject) {
                    return ((DataObject)sdp).getPrimaryFile();
                }
                return null;
            }

            @Override
            public Document createDocument(String mimeType) {
                return new BaseDocument(false, mimeType);
            }
            
        });
      }

    public static ClassPath getBootClassPath() {
        return BootClassPathUtil.getBootClassPath();
    }

}
