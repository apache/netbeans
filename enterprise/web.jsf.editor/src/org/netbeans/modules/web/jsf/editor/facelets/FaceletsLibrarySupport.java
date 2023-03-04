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
package org.netbeans.modules.web.jsf.editor.facelets;

import java.io.IOException;
import java.util.Enumeration;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.editor.facelets.mojarra.FaceletsTaglibConfigProcessor;
import com.sun.faces.config.DocumentInfo;
import com.sun.faces.spi.ConfigurationResourceProvider;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.web.jsf.api.editor.JsfFacesComponentsProvider;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;
import org.netbeans.modules.web.jsf.editor.JsfSupportImpl;
import org.netbeans.modules.web.jsf.editor.facelets.mojarra.ConfigManager;
import org.netbeans.modules.web.jsf.editor.index.IndexedFile;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.LibraryType;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author marekfukala
 */
public class FaceletsLibrarySupport {

    private static final RequestProcessor RP = new RequestProcessor(FaceletsLibrarySupport.class);
    private static final RequestProcessor FC_REFRESH_RP = new RequestProcessor("FLSFacesComponentsRefresh", 1);

    private JsfSupportImpl jsfSupport;

    /**
     * Library's namespace to library instance map.
     *
     * A composite library can be mapped to two namespaces,
     * the default and the declared one when
     * there is a tag library descriptor for the composite library
     */
    private Map<String, Library> faceletsLibraries;

    private long libraries_hash;
    
    private boolean checkLibrariesUpToDate;

    private static final Logger LOGGER = Logger.getLogger(FaceletsLibrarySupport.class.getSimpleName());

    private RequestProcessor.Task facesComponentsRefreshTask;
    private volatile Collection<? extends Library> facesComponentsCache = new ArrayList<>();

    private FileChangeListener DDLISTENER = new FileChangeAdapter() {
        @Override
        public void fileChanged(FileEvent fe) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    LOGGER.info("Invalidating facelets libraries due to changes in web.xml deployment descriptor."); //NOI18N
                    invalidateLibrariesCache();
                }
            });
        }
    };
    
    private static final String DD_FILE_NAME = "web.xml"; //NOI18N

    public FaceletsLibrarySupport(JsfSupportImpl jspSupport) {
        this.jsfSupport = jspSupport;

        //listen on /WEB-INF/web.xml changes - <param-name>javax.faces.FACELETS_LIBRARIES</param-name>
        //may change and redefine the libraries
        WebModule webModule = jsfSupport.getWebModule();
        if(webModule != null) {
            final FileObject dd = webModule.getDeploymentDescriptor();
            if (dd != null) {
                dd.addFileChangeListener(DDLISTENER);
            }

            //listen on the /WEB-INF folder since the dd is arbitrary and may
            //be created later
            FileObject webInf = webModule.getWebInf();
            if (webInf != null) {
                webInf.addFileChangeListener(new FileChangeAdapter() {

                    @Override
                    public void fileDataCreated(FileEvent fe) {
                        FileObject file = fe.getFile();
                        if (file.getNameExt().equalsIgnoreCase(DD_FILE_NAME)) {
                            file.addFileChangeListener(DDLISTENER);
                        }
                    }

                    @Override
                    public void fileDeleted(FileEvent fe) {
                        FileObject file = fe.getFile();
                        if (file.getNameExt().equalsIgnoreCase(DD_FILE_NAME)) {
                            file.removeFileChangeListener(DDLISTENER);
                        }
                    }
                });
            }
        }
    }

    public JsfSupportImpl getJsfSupport() {
        return jsfSupport;
    }

    private synchronized void invalidateLibrariesCache() {
        faceletsLibraries = null;
        
        // !!! Can't be used here - leads to issues like issue #230198 !!!
        // IndexingManager.getDefault().refreshAllIndices(getJsfSupport().getClassPathRoots());
    }
    
    /*
     * Called via the JsfSupport from the JSF indexers when their source roots have been rescanned.
     * that can mean the files related to the JSF libraries might have changed so we need to re-check
     * the libraries up-to-date status next time when one calls getLibraries().
     */
    public void indexedContentPossiblyChanged() {
        checkLibraryDescriptorsUpToDate();
    }

    /** @return URI -> library map */
    public synchronized Map<String, Library> getLibraries() {
        if (faceletsLibraries == null) {
            // preload FacesComponents
            refreshFacesComponentsCache(0);

            //not initialized yet, or invalidated by checkLibraryDescriptorsUpToDate()
            faceletsLibraries = findLibraries();

            if (faceletsLibraries == null) {
                //an error when scanning libraries, return no libraries, but give it a next try
                return Collections.emptyMap();
            }
            
            updateCompositeLibraries(faceletsLibraries);
        }
        updateFacesComponentLibraries(faceletsLibraries);

        return faceletsLibraries;
    }

    private void checkLibraryDescriptorsUpToDate() {
        //check whether the library descriptors have changes since the last time
        long hash = 7;
        for (IndexedFile indexedFile : getJsfSupport().getIndex().getAllFaceletsLibraryDescriptors()) {
            String md5checksum = indexedFile.getMD5Checksum();
            hash = 79 * hash + md5checksum.hashCode();
        }

        //Check whether a new composite component library has been created or removed.
        //The changes within the composite component libraries does not need to be
        //checked here since the CC libraries are doing index queries whenever a library
        //metdata are requested.
        for(String ccLibName : getJsfSupport().getIndex().getAllCompositeLibraryNames()) {
            hash = 79 * hash + ccLibName.hashCode();
        }

        if(hash != libraries_hash) {
            LOGGER.info("Invalidating facelets libraries due to a library descriptor change."); //NOI18N
            
            //some library descriptor has been modified, invalidate the cache
            invalidateLibrariesCache();
            libraries_hash = hash;
        }

    }

    // This method creates a library instances for the composite libraries without
    // a library descriptor and also adds the default composite library
    // namespace as a new key to the libraries map.
    private void updateCompositeLibraries(Map<String, Library> faceletsLibraries) {
        List<String> libraryNames = new ArrayList<>(jsfSupport.getIndex().getAllCompositeLibraryNames());
        //go through all the declared libraries, filter composite libraries
        //and add default namespace to the libraries map
        Map<String, Library> cclibsMap = new HashMap<>();
        for (Library lib : faceletsLibraries.values()) {
            if (lib instanceof CompositeComponentLibrary) {
                CompositeComponentLibrary cclib = (CompositeComponentLibrary)lib;
                //add default namespace to the map
                cclibsMap.put(cclib.getDefaultNamespace(), cclib);

                String libraryName = cclib.getLibraryName();
                libraryNames.remove(libraryName);
            }
        }

        faceletsLibraries.putAll(cclibsMap);

        //create libraries for the rest of the libraries (which have no facelets library descriptor associated)
        for (String libraryName : libraryNames) {
            CompositeComponentLibrary ccl = new PureCompositeComponentLibrary(this, libraryName);
            //map the library only to the default namespace, it has no declaration
            faceletsLibraries.put(ccl.getDefaultNamespace(), ccl);
        }

    }

    /**
     * This method obtains a library instances for the elements declared by annotation without a library descriptor.
     */
    private void updateFacesComponentLibraries(Map<String, Library> faceletsLibraries) {
        refreshFacesComponentsCache(300);

        // remove all FacesComponentLibraries
        Iterator<Map.Entry<String, Library>> iterator = faceletsLibraries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Library> entry = iterator.next();
            if (entry.getValue().getType() == LibraryType.COMPONENT) {
                iterator.remove();
            }
        }

        // add the refreshed ones
        for (Library library : facesComponentsCache) {
            faceletsLibraries.put(library.getDefaultNamespace(), library);
        }
    }

    //handle progress
    private Map<String, Library> findLibraries() {
        ProgressHandle progress = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(FaceletsLibrarySupport.class, "MSG_ParsingFaceletsLibraries")); //NOI18N
        progress.start();
        progress.switchToIndeterminate();
        try {
            return _findLibraries();
        } finally {
            progress.finish();
        }
    }

    private Map<String, Library> _findLibraries() {
        //use this module classloader
        ClassLoader originalLoader = this.getClass().getClassLoader();
        LOGGER.log(Level.FINE, "Scanning facelets libraries, current classloader class={0}, "
                + "the used URLClassLoader will also contain following roots:",
                originalLoader.getClass().getName()); //NOI18N

        Collection<URL> urlsToLoad = new ArrayList<>();
        for (FileObject cpRoot : getJsfSupport().getClassPath().getRoots()) {
            try {
                //exclude the jsf jars from the classpath, if jsf20 library is available,
                //we'll use the jars from the netbeans library instead
                String fsName = cpRoot.getFileSystem().getDisplayName(); //any better way?
                if(!fsName.endsWith("javax.faces.jar")) { //NOI18N
                    urlsToLoad.add(URLMapper.findURL(cpRoot, URLMapper.INTERNAL));
                    LOGGER.log(Level.FINE, "+++{0}", cpRoot); //NOI18N
                } else {
                    LOGGER.log(Level.FINE, "---{0}", cpRoot); //NOI18N
                }

            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        ClassLoader proxyLoader = new URLClassLoader(urlsToLoad.toArray(new URL[]{}), originalLoader) {

	    //prevent services loading from mojarra's sources
	    @Override
	    public URL findResource(String name) {
		return name.startsWith("META-INF/services") ? null : super.findResource(name); //NOI18N
	    }

	    @Override
	    public Enumeration<URL> findResources(String name) throws IOException {
		if(name.startsWith("META-INF/services")) { //NOI18N
		    return Collections.enumeration(Collections.<URL>emptyList());
		} else {
		    return super.findResources(name);
		}
	    }
	    
	};

        ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(proxyLoader);

            //do the parse
            return parseLibraries();

        } finally {
            //reset the original loader
            Thread.currentThread().setContextClassLoader(originalContextClassLoader);
        }
    }

    private Map<String, Library> parseLibraries() {
        // initialize the resource providers for facelet-taglib documents
        List<ConfigurationResourceProvider> faceletTaglibProviders =
                new ArrayList<>();

        //1. first add provider which looks for libraries defined in web-inf.xml
        //WEB-INF/web.xml <param-name>javax.faces.FACELETS_LIBRARIES</param-name> context param provider
        WebModule webModule = getJsfSupport().getWebModule();
        if(webModule != null) {
            faceletTaglibProviders.add(new WebFaceletTaglibResourceProvider(webModule));
        }

        //2. second add a provider returning URIs of library descriptors found during indexing
        //   the URIs points to both source roots and binary roots of dependent libraries.
        final Collection<URI> uris = new ArrayList<>();
        for (IndexedFile file : getJsfSupport().getIndex().getAllFaceletsLibraryDescriptors()) {
            try {
                uris.add(URLMapper.findURL(file.getFile(), URLMapper.EXTERNAL).toURI());
            } catch (URISyntaxException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
        faceletTaglibProviders.add(new ConfigurationResourceProvider() {

            @Override
            public Collection<URI> getResources(ServletContext sc) {
                return uris;
            }
        });

        //3. last add a provider for default jsf libs
        //
        //Add a facelet taglib provider which provides the libraries from
        //netbeans jsf2.0 library
        //
        //This is needed for the standart JSF 2.0 libraries since it may
        //happen that there is no javax-faces.jar with the .taglib.xml files
        //on the compile classpath and we still want the features like code
        //completion work. This happens for example in Maven web projects.
        //
        //The provider is last in the list so the provided libraries will
        //be overridden if the descriptors are found in any of the jars
        //on compile classpath.
        Collection<FileObject> libraryDescriptorFiles = DefaultFaceletLibraries.getInstance().getLibrariesDescriptorsFiles();
        final Collection<URI> libraryURIs = new ArrayList<>();
        for(FileObject fo : libraryDescriptorFiles) {
            try {
                libraryURIs.add(fo.toURL().toURI());
            } catch (URISyntaxException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
        faceletTaglibProviders.add(new ConfigurationResourceProvider() {
            @Override
            public Collection<URI> getResources(ServletContext sc) {
                return libraryURIs;
            }
        });

        //parse the libraries
        ServletContext sc = new EmptyServletContext();
        DocumentInfo[] documents = ConfigManager.getConfigDocuments(sc, faceletTaglibProviders, null, true);
        if (documents == null) {
            return null; //error????
        }

        //process the found documents
        FaceletsTaglibConfigProcessor processor = new FaceletsTaglibConfigProcessor(this);
        processor.process(new EmptyServletContext(), documents);
        
        Map<String, Library> libsMap = new HashMap<>();
        for (Library lib : processor.compiler.libraries) {
            if (lib.getLegacyNamespace() != null) {
                libsMap.put(lib.getLegacyNamespace(), lib);
            } else {
                libsMap.put(lib.getNamespace(), lib);
            }
        }

        //4. in case of JSF2.2 include pseudo-libraries (http://java.sun.com/jsf/passthrough, http://java.sun.com/jsf)
        // right now, we have no idea whether such libraries will be included into the JSF bundle or not
        if (webModule != null) {
            JSFVersion jsfVersion = JSFVersion.forWebModule(webModule);
            if (jsfVersion != null && jsfVersion.isAtLeast(JSFVersion.JSF_2_2)) {
                libsMap.putAll(DefaultFaceletLibraries.getJsf22FaceletPseudoLibraries(this));
            }
        }

        return libsMap;

    }

    private synchronized void refreshFacesComponentsCache(int timeToWait) {
        if (facesComponentsRefreshTask == null || facesComponentsRefreshTask.isFinished()) {
            facesComponentsRefreshTask = FC_REFRESH_RP.post(new RefreshFacesComponentsTask(), timeToWait);
        }
    }


//    private void debugLibraries() {
//        System.out.println("Facelets Libraries:");  //NOI18N
//        System.out.println("====================");  //NOI18N
//        for (FaceletsLibrary lib : faceletsLibraries.values()) {
//            System.out.println("Library: " + lib.getNamespace());  //NOI18N
//            System.out.println("----------------------------------------------------");  //NOI18N
//            for (FaceletsLibrary.NamedComponent comp : lib.getComponents()) {
//                System.out.println(comp.getName() + "(" + comp.getClass().getSimpleName() + ")");  //NOI18N
//            }
//            System.out.println();
//        }
//    }

    public static class Compiler {

        private Collection<Library> libraries = new HashSet<>();

        //FaceletsTaglibConfigProcessor puts the libraries here and since the
        //equals on the libraries is defined by comparing the namespaces,
        //the first library with a namespace will be preserved, the other
        //will be ignored
        public void addTagLibrary(Library lib) {
            libraries.add(lib);
        }
    }

    private final class RefreshFacesComponentsTask implements Runnable {

        @Override
        public void run() {
            Collection<? extends Library> libraries = JsfFacesComponentsProvider.getLibraries(jsfSupport.getProject());
            facesComponentsCache = libraries;
        }
    }
}
