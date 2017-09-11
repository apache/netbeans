/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.editor.example;

import java.io.*;
import java.util.*;
import org.netbeans.editor.ext.html.dtd.ReaderProvider;
import org.netbeans.editor.ext.html.dtd.Registry;

public class SAReaderProvider implements ReaderProvider {

    private static final String DTD_FOLDER = "DTDs"; // NOI18 // NOI18N
    private static final String CATALOG_FILE_NAME = "catalog"; // NOI18N

    Map mapping = null;
    boolean valid = false;
    File dtdSetFolder;

    public static void setupReaders() {
        //We are not able to track changes!
        File rootFolder = Editor.getDistributionDirectory();
        
	//We are not able to track changes!
        File dtdFolder = findFile( rootFolder, DTD_FOLDER );
        if( dtdFolder != null) {
            processSubfolders( dtdFolder );
        }
    }
    
    

    public SAReaderProvider( File folder ) {
        dtdSetFolder = folder;
        revalidate(true);
	//We will not be able to catch changes in folder!
    }

    public Collection getIdentifiers() {
        return valid ? mapping.keySet() : new HashSet(0);
    }
    
    private static File findFile(File folder, String fileName) {
       if (fileName == null || folder == null) {
          return null;
       }
       
       File[] files = folder.listFiles();
       
       for (int cntr = 0; cntr < files.length; cntr++) {
          if (fileName.equals(files[cntr].getName())) {
	     return files[cntr];
	  }
       }
       return null;
    }

    public Reader getReaderForIdentifier( String identifier, String filename) {
        if( !valid ) return null;
        
        String fileName = (String)mapping.get( identifier );
        if( fileName == null ) return null;
        if( dtdSetFolder == null ) return null;
        
        File file = findFile( dtdSetFolder, fileName );
        if( file == null ) return null;
        
        try {
            return new InputStreamReader( new FileInputStream (file ) );
        } catch( FileNotFoundException exc ) {
            return null;
        }
    }

    private void invalidate() {
        if( valid ) {
            valid = false;
            Registry.invalidateReaderProvider( this );
        }
    }

    private boolean revalidate( boolean flag ) {
        if( mapping == null || flag ) {
            File catalog = findFile( dtdSetFolder, CATALOG_FILE_NAME );

            if( catalog == null ) {
                mapping = null;
            } else try {
                mapping = parseCatalog( new InputStreamReader( new FileInputStream( catalog ) ) );
            } catch( FileNotFoundException exc ) {
                mapping = null;
            }
            
            if( mapping == null ) {
                invalidate();
                return false;
            }
        }
        
        // check the availabilily
        Collection files = mapping.values();
        boolean all = true;
        for( Iterator it = files.iterator(); it.hasNext(); ) {
            String fname = (String)it.next();
            if( findFile( dtdSetFolder, fname ) == null ) {
                all = false;
                break;
            }
        }
        if( !all ) invalidate();
        valid = all;
        return valid;
    }

    private Map parseCatalog( Reader catalogReader ) {
        HashMap hashmap = new HashMap();
        LineNumberReader reader = new LineNumberReader( catalogReader );
        
        for( ;; ) {
            String line;

            try {
                line = reader.readLine();
            } catch( IOException exc ) {
                return null;
            }
            
            if( line == null ) break;
            
            StringTokenizer st = new StringTokenizer( line );
            if( st.hasMoreTokens() && "PUBLIC".equals( st.nextToken() ) && st.hasMoreTokens() ) { // NOI18N
                st.nextToken( "\"" ); // NOI18N
		if( !st.hasMoreTokens() ) continue;
                String id = st.nextToken( "\"" ); // NOI18N

		if( !st.hasMoreTokens() ) continue;
                st.nextToken( " \t\n\r\f" ); // NOI18N
		
		if( !st.hasMoreTokens() ) continue;
                String file = st.nextToken();
                hashmap.put( id, file );
            }
        }
        return hashmap;
    }

    private static void processSubfolders( File dtdFolder ) {
        File[] files = dtdFolder.listFiles();
	
        for (int cntr = 0; cntr < files.length; cntr++) {
	    File file = files[cntr];
	    
	    if (file != null && file.isDirectory()) {
               addFolder( file );
	    }
        }
    }

    static Map folder2provider = new HashMap();

    
    private static void removeSubfolders() {
        Iterator it = folder2provider.entrySet().iterator();
        folder2provider = new HashMap();
        while( it.hasNext() ) {
            Map.Entry entry = (Map.Entry)it.next();
            ReaderProvider prov = (ReaderProvider)entry.getValue();
            Registry.unregisterReaderProvider( prov );
        }
    }

    private static void addFolder( File folder ) {
        SAReaderProvider prov = new SAReaderProvider( folder );
        folder2provider.put( folder.getName(), prov );
        Registry.registerReaderProvider( prov );
    }

    private static void removeFolder( File folder ) {
        SAReaderProvider prov = (SAReaderProvider)folder2provider.remove( folder.getName() );
        if( prov != null ) Registry.unregisterReaderProvider( prov );
    }

}

