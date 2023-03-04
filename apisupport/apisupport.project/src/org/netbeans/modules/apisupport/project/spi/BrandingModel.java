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

package org.netbeans.modules.apisupport.project.spi;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.spi.BrandingSupport.BrandableModule;
import org.netbeans.modules.apisupport.project.spi.BrandingSupport.BrandedFile;
import org.netbeans.modules.apisupport.project.spi.BrandingSupport.BundleKey;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import static org.netbeans.modules.apisupport.project.spi.Bundle.*;
import org.openide.util.NbBundle.Messages;

/**
 * Model of what is branded and how.
 * @author Radek Matous, S. Aubrecht
 */
public abstract class BrandingModel {
    
    private BrandingSupport branding;
    
    /** for generating property branding.token*/
    private boolean brandingEnabled;
    
    /** for properties (app.name, app.title, app.icon)*/
    private String name;
    private String title;
    private @NullAllowed BrandingSupport.BrandedFile icon48 = null;
    private BrandingSupport.BrandedFile icon16 = null;
    private BrandingSupport.BrandedFile icon32 = null;
    private BrandingSupport.BrandedFile icon256 = null;
    private BrandingSupport.BrandedFile icon512 = null;
    private BrandingSupport.BrandedFile icon1024 = null;
    
    /** representation of bundle keys depending on app.title */
    private BrandingSupport.BundleKey productInformation = null;
    private BrandingSupport.BundleKey mainWindowTitle = null;
    private BrandingSupport.BundleKey splashWindowTitle = null;    
    private BrandingSupport.BundleKey mainWindowTitleNoProject = null;
    private BrandingSupport.BundleKey currentVersion = null;
    
    /** representation of bundle keys for splash section */
    private BrandingSupport.BrandedFile splash = null;
    
    private BrandingSupport.BundleKey splashWidth = null;
    private BrandingSupport.BundleKey splashHeight = null;
    private BrandingSupport.BundleKey splashShowProgressBar = null;
    private BrandingSupport.BundleKey splashRunningTextBounds = null;
    private BrandingSupport.BundleKey splashProgressBarBounds = null;
    private BrandingSupport.BundleKey splashRunningTextFontSize = null;
    private BrandingSupport.BundleKey splashRunningTextColor = null;
    private BrandingSupport.BundleKey splashProgressBarColor = null;
    private BrandingSupport.BundleKey splashProgressBarEdgeColor = null;
    private BrandingSupport.BundleKey splashProgressBarCornerColor = null;
    
    /**all above splash BundleKeys in set*/
    private final Set<BrandingSupport.BundleKey> splashKeys = new HashSet<BrandingSupport.BundleKey>();
    
    /** representation of bundle keys for window system section */
    private BrandingSupport.BundleKey wsEnableDragAndDrop = null;
    private BrandingSupport.BundleKey wsEnableFloating = null;
    private BrandingSupport.BundleKey wsEnableSliding = null;
    private BrandingSupport.BundleKey wsEnableClosingViews = null;
    private BrandingSupport.BundleKey wsEnableClosingEditors = null;
    private BrandingSupport.BundleKey wsEnableResizing = null;
    private BrandingSupport.BundleKey wsEnableMinimumSize = null;
    private BrandingSupport.BundleKey wsEnableMaximization = null;
    private BundleKey wsEnableAutoSlideInMinimizedMode = null;
    private BundleKey wsEnableEditorModeUndocking = null;
    private BundleKey wsEnableModeClosing = null;
    private BundleKey wsEnableEditorModeDnD = null;
    private BundleKey wsEnableModeSliding = null;
    private BundleKey wsEnableViewModeDnD = null;
    private BundleKey wsEnableViewModeUndocking = null;
    
    /**all above splash BundleKeys in set*/
    private final Set<BrandingSupport.BundleKey> winsysKeys = new HashSet<BrandingSupport.BundleKey>();

    /**all BundleKeys the user may have modified through Resource Bundle editor panel */
    private final Set<BrandingSupport.BundleKey> generalResourceBundleKeys = new HashSet<BrandingSupport.BundleKey>();
    
    /**Internationalized BundleKeys the user may have modified through Internationalized Resource Bundle editor panel */
    private final Set<BrandingSupport.BundleKey> internationalizedResourceBundleKeys = new HashSet<BrandingSupport.BundleKey>();
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    
    protected Locale locale;
    
    protected BrandingModel() {}
    
    public final boolean isBrandingEnabled() {
        return brandingEnabled;
    }
    
    public void setBrandingEnabled(boolean brandingEnabled) {
        if (this.brandingEnabled != brandingEnabled) {
            this.brandingEnabled = brandingEnabled;
            changeSupport.fireChange();
        }
    }
    
    public final String getName() {
        return name;
    }
    
    public void setName(String name) /*throws IllegalArgumentException*/ {
        /*if (name != null && !name.matches("[a-z][a-z0-9]*(_[a-z][a-z0-9]*)*")) { // NOI18N
            throw new IllegalArgumentException("Malformed name: " + name); // NOI18N
        }*/
     
        if (isBrandingEnabled()) {
            this.name = name;
        }
    }
    
    public final String getTitle() {
        return title;
    }
    
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    @Messages("LBL_splash_window_title_prefix=Starting")
    public void setTitle(String title) {
        if (isBrandingEnabled()) {
            this.title = title;
            if (productInformation != null) {
                productInformation.setValue(title);
            }
            if (mainWindowTitle != null) {
                mainWindowTitle.setValue(title + " {0}"); //NOI18N
            }
            if (splashWindowTitle != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(LBL_splash_window_title_prefix());//NOI18N
                sb.append(" ").append(title);//NOI18N
                splashWindowTitle.setValue(sb.toString());//NOI18N
            }
            if (mainWindowTitleNoProject != null) {
                mainWindowTitleNoProject.setValue(title + " {0}"); //NOI18N
            }
            if (currentVersion != null) {
                currentVersion.setValue(title + " {0}"); //NOI18N
            }
        }
    }
    
    public final URL getIconSource(int size) {
        switch( size ) {
            case 16:
                return icon16 != null ? icon16.getBrandingSource() : null;
            case 32:
                return icon32 != null ? icon32.getBrandingSource() : null;
            case 48:
                return icon48 != null ? icon48.getBrandingSource() : null;
            case 256:
                return icon256 != null ? icon256.getBrandingSource() : null;
            case 512:
                return icon512 != null ? icon512.getBrandingSource() : null;
            case 1024:
                return icon1024 != null ? icon1024.getBrandingSource() : null;
        }
        throw new IllegalArgumentException("Invalid icon size: " + size);
    }
    
    public void setIconSource(int size, final URL url) {
        if (isBrandingEnabled()) {
            BrandingSupport.BrandedFile icon = null;
            switch( size ) {
                case 16:
                    icon = icon16;
                    break;
                case 32:
                    icon = icon32;
                    break;
                case 48:
                    icon = icon48;
                    break;
                case 256:
                    icon = icon256;
                    break;
                case 512:
                    icon = icon512;
                    break;
                case 1024:
                    icon = icon1024;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid icon size: " + size);
            }
            if (icon != null && url != null) {
                icon.setBrandingSource(url);
            }
        }
    }
    
    protected final @CheckForNull String getIconLocation() {
        if (icon48 == null) {
            return null;
        }
        File prj = getProjectDirectoryFile();
        String relativePath = PropertyUtils.relativizeFile(prj, icon48.getFileLocation());
        
        return relativePath;
    }

    public abstract Project getProject();

    public final Set<File> getBrandableJars() {
        return getBranding().getBrandableJars();
    }

    protected abstract File getProjectDirectoryFile();
    
    public void store() throws IOException {
        if (brandingEnabled) {
            getBranding().brandBundleKey(productInformation);
            getBranding().brandBundleKey(mainWindowTitle);
            getBranding().brandBundleKey(splashWindowTitle);
            getBranding().brandBundleKey(mainWindowTitleNoProject);
            getBranding().brandBundleKey(currentVersion);

            if (icon48 != null) { // #176423
                getBranding().brandFile(icon48, getScaleAndStoreIconTask(icon48, 48, 48));
            }

            if (icon16 != null) {
                getBranding().brandFile(icon16, getScaleAndStoreIconTask(icon16, 16, 16));
            }

            if (icon32 != null) {
                getBranding().brandFile(icon32, getScaleAndStoreIconTask(icon32, 32, 32));
            }

            if (icon256 != null) {
                getBranding().brandFile(icon256, getScaleAndStoreIconTask(icon256, 256, 256));
            }

            if (icon512 != null) {
                getBranding().brandFile(icon512, getScaleAndStoreIconTask(icon512, 512, 512));
            }
            
            if (icon1024 != null) {
                getBranding().brandFile(icon1024, getScaleAndStoreIconTask(icon1024, 1024, 1024));
            }
                                    
            getBranding().brandBundleKeys(splashKeys);
            if (splash != null) {
                getBranding().brandFile(splash);
            }
            getBranding().brandBundleKeys(winsysKeys);

            getBranding().brandBundleKeys(generalResourceBundleKeys);

            getBranding().brandBundleKeys(internationalizedResourceBundleKeys);

            FileObject root = FileUtil.toFileObject(getBranding().getBrandingRoot());
            if( null != root ) {
                root.refresh();
            }
        }
    }
    
    private static Runnable getScaleAndStoreIconTask(final BrandedFile icon, final int width, final int height) throws IOException {
        return new Runnable() {
            @Override
            public void run() {
                BufferedImage bi = new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_ARGB);
                
                Graphics2D g2 = bi.createGraphics();
                ImageIcon image = new ImageIcon(icon.getBrandingSource());
                g2.drawImage(image.getImage(),0, 0, 
                        width, height, null);
                
                g2.dispose();
                try {
                    File iconLocation = icon.getFileLocation();
                    if( !iconLocation.exists() )
                        iconLocation.createNewFile();
                    FileObject fo = FileUtil.toFileObject(iconLocation);
                    OutputStream os = fo == null ? new FileOutputStream(iconLocation) : fo.getOutputStream();
                    try {
                        ImageIO.write(bi, "png", os);
                    } finally {
                        os.close();
                    }
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        };
    }
    
    public void refreshLocalizedBundles(Locale locale) {
        this.locale = locale;
        try {
            branding.refreshLocalizedBundles(locale);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
         
        
    }
    
    private BrandingSupport getBranding() {
        if (branding == null) {
            try {
                branding = createBranding();
                branding.init();
            } catch (IOException x) {
                Logger.getLogger(BrandingModel.class.getName()).log(Level.INFO, null, x);
                branding = new BrandingSupport(getProject(), "nowhere") {
                    @Override protected BrandableModule findBrandableModule(String moduleCodeNameBase) {
                        return null;
                    }
                    @Override public Set<File> getBrandableJars() {
                        return Collections.emptySet();
                    }
                    @Override protected Set<BrandableModule> loadModules() throws IOException {
                        return null;
                    }
                    @Override protected Map<String,String> localizingBundle(BrandableModule moduleEntry) {
                        return Collections.emptyMap();
                    }
                };
            }
        }
        return branding;
    }

    protected abstract BrandingSupport createBranding() throws IOException;
    
    public void init() {
        initBundleKeys();
        initName(false);
        initTitle(false);
        brandingEnabledRefresh();
    }
    
    public final void brandingEnabledRefresh() {
        brandingEnabled = isBrandingEnabledRefresh();
    }

    protected abstract boolean isBrandingEnabledRefresh();
    
    protected String getSimpleName() {
        if (mainWindowTitle == null) {
            return getProjectDirectoryFile().getName();
        }
        String res = mainWindowTitle.getValue();
        if (res.endsWith(" {0}")) { //NOI18N
            res = res.substring(0, res.lastIndexOf(" {0}")); //NOI18N
        }
        return res;
    }

    public final void initName(boolean reread)  {
        if (name == null || reread) {
            name = loadName();
        }
        
        if (name == null) {
            name = getSimpleName().toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9]", "_"); // NOI18N
            if (!name.matches("[a-z][a-z0-9]*(_[a-z][a-z0-9]*)*")) { // NOI18N
                // Too far from a decent name, forget it.
                name = "app"; // NOI18N
            }
        }
        
        assert name != null;
    }

    protected abstract String loadName();
    
    public final void initTitle(boolean reread)  {
        if (title == null || reread) {
            String initTitle = loadTitle();
            if (initTitle == null) {
                initTitle = getSimpleName();
                // Just make a rough attempt to uppercase it, to hint that it can be a display name.
                if (Character.isLowerCase(initTitle.charAt(0))) {
                    initTitle = String.valueOf(Character.toLowerCase(initTitle.charAt(0))) + initTitle.substring(1);
                }
            }
            assert initTitle != null;
            title = initTitle;
        }
    }

    protected abstract String loadTitle();
    
    private void initBundleKeys() {
        productInformation = getBranding().getBundleKey(
                "org.netbeans.core",//NOI18N
                "org/netbeans/core/ui/Bundle.properties" ,//NOI18N
                "LBL_ProductInformation");//NOI18N
        
        mainWindowTitle = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/view/ui/Bundle.properties", // NOI18N
                "CTL_MainWindow_Title");//NOI18N

        splashWindowTitle = getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "LBL_splash_window_title");//NOI18N                
        
        mainWindowTitleNoProject = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/view/ui/Bundle.properties",//NOI18N
                "CTL_MainWindow_Title_No_Project");//NOI18N
        
        currentVersion = getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "currentVersion");//NOI18N
        
        icon48 = getBranding().getBrandedFile(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/frame48.gif");//NOI18N

        icon16 = getBranding().getBrandedFile(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/frame.gif");//NOI18N               
        
        icon32 = getBranding().getBrandedFile(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/frame32.gif");//NOI18N
        
        icon256 = getBranding().getBrandedFile(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/frame256.png");//NOI18N
        
        icon512 = getBranding().getBrandedFile(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/frame512.png");//NOI18N
        
        icon1024 = getBranding().getBrandedFile(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/frame1024.png");//NOI18N

        splash = getBranding().getBrandedFile(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/splash.gif");//NOI18N
        
        // init of splash keys
        
        splashWidth = getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "SPLASH_WIDTH");//NOI18N
        
        splashHeight = getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "SPLASH_HEIGHT");//NOI18N
        
        splashShowProgressBar = getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "SplashShowProgressBar");//NOI18N
        
        splashRunningTextFontSize= getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "SplashRunningTextFontSize");//NOI18N
        
        splashProgressBarBounds= getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "SplashProgressBarBounds");//NOI18N
        
        splashRunningTextBounds= getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "SplashRunningTextBounds");//NOI18N
        
        splashRunningTextColor= getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "SplashRunningTextColor");//NOI18N
        
        splashProgressBarColor= getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "SplashProgressBarColor");//NOI18N
        
        splashProgressBarEdgeColor= getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "SplashProgressBarEdgeColor");//NOI18N
        
        splashProgressBarCornerColor= getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "SplashProgressBarCornerColor");//NOI18N
        
        splashKeys.clear();
        
        if (splashWidth != null) {
            splashKeys.add(splashWidth);
        }
        if (splashHeight != null) {
            splashKeys.add(splashHeight);
        }
        if (splashShowProgressBar != null) {
            splashKeys.add(splashShowProgressBar);
        }
        if (splashRunningTextBounds != null) {
            splashKeys.add(splashRunningTextBounds);
        }
        if (splashProgressBarBounds != null) {
            splashKeys.add(splashProgressBarBounds);
        }
        if (splashRunningTextFontSize != null) {
            splashKeys.add(splashRunningTextFontSize);
        }
        if (splashRunningTextColor != null) {
            splashKeys.add(splashRunningTextColor );
        }
        if (splashProgressBarColor != null) {
            splashKeys.add(splashProgressBarColor);
        }
        if (splashProgressBarEdgeColor != null) {
            splashKeys.add(splashProgressBarEdgeColor);
        }
        if (splashProgressBarCornerColor != null) {
            splashKeys.add(splashProgressBarCornerColor);
        }
        splashKeys.remove(null);

            
        wsEnableClosingEditors = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "Editor.TopComponent.Closing.Enabled");//NOI18N
            
        wsEnableClosingViews = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "View.TopComponent.Closing.Enabled");//NOI18N
            
        wsEnableDragAndDrop = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "TopComponent.DragAndDrop.Enabled");//NOI18N
            
        wsEnableFloating = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "TopComponent.Undocking.Enabled");//NOI18N
            
        wsEnableMinimumSize = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "Splitter.Respect.MinimumSize.Enabled");//NOI18N
            
        wsEnableResizing = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "TopComponent.Resizing.Enabled");//NOI18N
            
        wsEnableSliding = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "TopComponent.Sliding.Enabled");//NOI18N
            
        wsEnableMaximization = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "TopComponent.Maximization.Enabled");//NOI18N
            
        wsEnableAutoSlideInMinimizedMode = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "TopComponent.Auto.Slide.In.Minimized.Mode.Enabled");//NOI18N
        
        wsEnableEditorModeUndocking = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "Mode.Editor.Undocking.Enabled");//NOI18N
        
        wsEnableModeClosing = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "Mode.Closing.Enabled");//NOI18N
        
        wsEnableEditorModeDnD = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "Mode.Editor.DragAndDrop.Enabled");//NOI18N
        
        wsEnableModeSliding = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "Mode.Sliding.Enabled");//NOI18N
        
        wsEnableViewModeDnD = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "Mode.View.DragAndDrop.Enabled");//NOI18N
        
        wsEnableViewModeUndocking = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "Mode.View.Undocking.Enabled");//NOI18N

        winsysKeys.clear();
        
        if (wsEnableClosingEditors != null) {
            winsysKeys.add(wsEnableClosingEditors);
        }
        if (wsEnableClosingViews != null) {
            winsysKeys.add(wsEnableClosingViews);
        }
        if (wsEnableDragAndDrop != null) {
            winsysKeys.add(wsEnableDragAndDrop);
        }
        if (wsEnableFloating != null) {
            winsysKeys.add(wsEnableFloating);
        }
        if (wsEnableMaximization != null) {
            winsysKeys.add(wsEnableMaximization);
        }
        if (wsEnableMinimumSize != null) {
            winsysKeys.add(wsEnableMinimumSize);
        }
        if (wsEnableResizing != null) {
            winsysKeys.add(wsEnableResizing);
        }
        if (wsEnableSliding != null) {
            winsysKeys.add(wsEnableSliding);
        }
        if( wsEnableAutoSlideInMinimizedMode != null )
            winsysKeys.add(wsEnableAutoSlideInMinimizedMode);
        if( wsEnableEditorModeUndocking != null )
            winsysKeys.add(wsEnableEditorModeUndocking);
        if( wsEnableModeClosing != null )
            winsysKeys.add(wsEnableModeClosing);
        if( wsEnableEditorModeDnD != null )
            winsysKeys.add(wsEnableEditorModeDnD);
        if( wsEnableModeSliding != null )
            winsysKeys.add(wsEnableModeSliding);
        if( wsEnableViewModeDnD != null )
            winsysKeys.add(wsEnableViewModeDnD);
        if( wsEnableViewModeUndocking != null )
            winsysKeys.add(wsEnableViewModeUndocking);

        winsysKeys.remove(null);

        generalResourceBundleKeys.clear();
        
        internationalizedResourceBundleKeys.clear();
    }

    private String backslashesToSlashes (String text) {
        return text.replace('\\', '/'); // NOI18N
    }

    private BrandingSupport.BundleKey findInModifiedGeneralBundleKeys (String codenamebase, String bundlepath, String key) {
        for (BundleKey bundleKey : generalResourceBundleKeys) {
            if (key.equals(bundleKey.getKey()) &&
                backslashesToSlashes(bundleKey.getBundleFilePath()).endsWith(bundlepath) &&
                codenamebase.equals(bundleKey.getModuleEntry().getCodeNameBase()))
                return bundleKey;
        }
        return null;
    }

    private BrandingSupport.BundleKey findInModifiedInternationalizedBundleKeys (String codenamebase, String bundlepath, String key) {
        for (BundleKey bundleKey : internationalizedResourceBundleKeys) {
            String localizedBundlepath = bundlepath;
            if(!localizedBundlepath.endsWith("_" + this.locale.toString() + ".properties"))
                localizedBundlepath = bundlepath.replace(".properties", "_" + this.locale.toString() + ".properties");
            if (key.equals(bundleKey.getKey()) &&
                backslashesToSlashes(bundleKey.getBundleFilePath()).endsWith(localizedBundlepath) &&
                codenamebase.equals(bundleKey.getModuleEntry().getCodeNameBase()))
                return bundleKey;
        }
        return null;
    }

    public final void addModifiedGeneralBundleKey (BrandingSupport.BundleKey key) {
        generalResourceBundleKeys.add (key);
    }

    public final void addModifiedInternationalizedBundleKey (BrandingSupport.BundleKey key) {
        Set<BrandingSupport.BundleKey> brandedBundleKeys = branding.getBrandedBundleKeys();
        String localizedBundlepath = key.getBundleFilePath();
        if(!localizedBundlepath.endsWith("_" + this.locale.toString() + ".properties"))
                localizedBundlepath = localizedBundlepath.replace(".properties", "_" + this.locale.toString() + ".properties");
        File bundleFile = new File(localizedBundlepath);
        if(!bundleFile.exists()) {
            for(BrandingSupport.BundleKey keyIter:brandedBundleKeys) {
                if(keyIter.getBundleFilePath().equals(key.getBundleFilePath()) && !key.getKey().equals(keyIter.getKey())) {
                    internationalizedResourceBundleKeys.add(branding.createModifiedBundleKey(keyIter.getModuleEntry(), bundleFile, keyIter.getKey(), keyIter.getValue()));
                }
            }
        }
        internationalizedResourceBundleKeys.add (branding.createModifiedBundleKey(key.getModuleEntry(), bundleFile, key.getKey(), key.getValue()));
    }

    public final BrandingSupport.BundleKey getGeneralBundleKeyForModification(String codenamebase, String bundlepath, String key) {
        BrandingSupport.BundleKey bKey = findInModifiedGeneralBundleKeys(codenamebase, bundlepath, key);
        return null != bKey ? bKey : getBranding().getBundleKey(codenamebase, bundlepath, key);
    }

    public final String getKeyValue(String bundlepath, String codenamebase, String key) {
        BrandingSupport.BundleKey bKey = findInModifiedGeneralBundleKeys(codenamebase, bundlepath, key);
        return null != bKey ? bKey.getValue()
                : getBranding().getBundleKey(codenamebase, bundlepath, key).getValue();
    }

    public final BrandingSupport.BundleKey getGeneralLocalizedBundleKeyForModification(String codenamebase, String bundlepath, String key) {
        BrandingSupport.BundleKey bKey = findInModifiedInternationalizedBundleKeys(codenamebase, bundlepath, key);
        return null != bKey ? bKey : getBranding().getLocalizedBundleKey(codenamebase, bundlepath, key);
    }

    public final String getLocalizedKeyValue(String bundlepath, String codenamebase, String key) {
        BrandingSupport.BundleKey bKey = findInModifiedInternationalizedBundleKeys(codenamebase, bundlepath, key);
        return null != bKey ? bKey.getValue()
                : getBranding().getLocalizedBundleKey(codenamebase, bundlepath, key).getValue();
    }

    public final boolean isKeyBranded(String bundlepath, String codenamebase, String key) {
        // in modified keys?
        if(inModifiedKeys(bundlepath, codenamebase, key))
            return true;
        // in branded but not modified keys?
        Set<BundleKey> bundleKeys = getBranding().getBrandedBundleKeys();
        for (BundleKey bundleKey : bundleKeys) {
            if (key.equals(bundleKey.getKey()) &&
                backslashesToSlashes(bundleKey.getBundleFilePath()).endsWith(bundlepath) &&
                codenamebase.equals(bundleKey.getModuleEntry().getCodeNameBase()))
                return true;
        }
        return false;
    }
    
    public final boolean isKeyLocallyBranded(String bundlepath, String codenamebase, String key) {
        // in modified keys?
        if(inModifiedLocalizedKeys(bundlepath, codenamebase, key))
            return true;
        // in branded but not modified keys?
        Set<BundleKey> bundleKeys = getBranding().getLocalizedBrandedBundleKeys();
        for (BundleKey bundleKey : bundleKeys) {
            String bundleFilePath = bundleKey.getBundleFilePath();
            String localizedBundlepath = bundlepath;
            if(bundleFilePath.endsWith("_" + this.locale.toString() + ".properties"))
                localizedBundlepath = bundlepath.replace(".properties", "_" + this.locale.toString() + ".properties");
            if (key.equals(bundleKey.getKey()) &&
                backslashesToSlashes(bundleFilePath).endsWith(localizedBundlepath) &&
                codenamebase.equals(bundleKey.getModuleEntry().getCodeNameBase()))
                return true;
        }
        return false;
    }
    
    private boolean inModifiedKeys(String bundlepath, String codenamebase, String key)
    {
        // in modified keys?
        for (BundleKey bundleKey : generalResourceBundleKeys) {
            if (key.equals(bundleKey.getKey()) &&
                backslashesToSlashes(bundleKey.getBundleFilePath()).endsWith(bundlepath) &&
                codenamebase.equals(bundleKey.getModuleEntry().getCodeNameBase()))
                return true;
        }
        return false;
    }

    private boolean inModifiedLocalizedKeys(String bundlepath, String codenamebase, String key)
    {
        // in modified keys?
        for (BundleKey bundleKey : internationalizedResourceBundleKeys) {
            String localizedBundlepath = bundlepath;
            if(!localizedBundlepath.endsWith("_" + this.locale.toString() + ".properties"))
                localizedBundlepath = bundlepath.replace(".properties", "_" + this.locale.toString() + ".properties");
            if (key.equals(bundleKey.getKey()) &&
                backslashesToSlashes(bundleKey.getBundleFilePath()).endsWith(localizedBundlepath) &&
                codenamebase.equals(bundleKey.getModuleEntry().getCodeNameBase()))
                return true;
        }
        return false;
    }

    public final boolean isBundleBranded(String bundlepath, String codenamebase) {
        // in modified keys?
        if(inModifiedKeysBundle(bundlepath, codenamebase))
            return true;
        // in branded but not modified keys?
        Set<BundleKey> bundleKeys = getBranding().getBrandedBundleKeys();
        for (BundleKey bundleKey : bundleKeys) {
            if (backslashesToSlashes(bundleKey.getBundleFilePath()).endsWith(bundlepath) &&
                codenamebase.equals(bundleKey.getModuleEntry().getCodeNameBase()))
                return true;
        }
        return false;
    }
    
    public final boolean isBundleLocallyBranded(String bundlepath, String codenamebase) {
        // in modified keys?
        if(inModifiedLocalizedKeysBundle(bundlepath, codenamebase))
            return true;
        // in branded but not modified keys?
        Set<BundleKey> bundleKeys = getBranding().getLocalizedBrandedBundleKeys();
        for (BundleKey bundleKey : bundleKeys) {
            String bundleFilePath = bundleKey.getBundleFilePath();
            String localizedBundlepath = bundlepath;
            if(bundleFilePath.endsWith("_" + this.locale.toString() + ".properties"))
                localizedBundlepath = bundlepath.replace(".properties", "_" + this.locale.toString() + ".properties");
            if (backslashesToSlashes(bundleFilePath).endsWith(localizedBundlepath) &&
                codenamebase.equals(bundleKey.getModuleEntry().getCodeNameBase()))
                return true;
        }
        return false;
    }
    
    private boolean inModifiedKeysBundle(String bundlepath, String codenamebase)
    {
        // in modified keys?
        for (BundleKey bundleKey : generalResourceBundleKeys) {
            if (backslashesToSlashes(bundleKey.getBundleFilePath()).endsWith(bundlepath) &&
                codenamebase.equals(bundleKey.getModuleEntry().getCodeNameBase()))
                return true;
        }
        return false;
    }

    private boolean inModifiedLocalizedKeysBundle(String bundlepath, String codenamebase)
    {
        // in modified keys?
        for (BundleKey bundleKey : internationalizedResourceBundleKeys) {
            String localizedBundlepath = bundlepath;
            if(!localizedBundlepath.endsWith("_" + this.locale.toString() + ".properties"))
                localizedBundlepath = bundlepath.replace(".properties", "_" + this.locale.toString() + ".properties");
            if (backslashesToSlashes(bundleKey.getBundleFilePath()).endsWith(localizedBundlepath) &&
                codenamebase.equals(bundleKey.getModuleEntry().getCodeNameBase()))
                return true;
        }
        return false;
    }

    public final @CheckForNull BrandingSupport.BundleKey getSplashWidth() {
        return splashWidth;
    }
    
    public final @CheckForNull BrandingSupport.BundleKey getSplashHeight() {
        return splashHeight;
    }
    
    public final @CheckForNull BrandingSupport.BundleKey getSplashShowProgressBar() {
        return splashShowProgressBar;
    }
    
    public final @CheckForNull BrandingSupport.BundleKey getSplashRunningTextBounds() {
        return splashRunningTextBounds;
    }
    
    public final @CheckForNull BrandingSupport.BundleKey getSplashProgressBarBounds() {
        return splashProgressBarBounds;
    }
    
    public final @CheckForNull BrandingSupport.BundleKey getSplashRunningTextFontSize() {
        return splashRunningTextFontSize;
    }
    
    public final @CheckForNull BrandingSupport.BundleKey getSplashRunningTextColor() {
        return splashRunningTextColor;
    }
    
    public final @CheckForNull BrandingSupport.BundleKey getSplashProgressBarColor() {
        return splashProgressBarColor;
    }
    
    public final @CheckForNull BrandingSupport.BundleKey getSplashProgressBarEdgeColor() {
        return splashProgressBarEdgeColor;
    }
    
    public final @CheckForNull BrandingSupport.BundleKey getSplashProgressBarCornerColor() {
        return splashProgressBarCornerColor;
    }
    
    public final @CheckForNull BrandingSupport.BrandedFile getSplash() {
        return splash;
    }

    public final @CheckForNull BundleKey getWsEnableClosingEditors() {
        return wsEnableClosingEditors;
    }

    public final @CheckForNull BundleKey getWsEnableClosingViews() {
        return wsEnableClosingViews;
    }

    public final @CheckForNull BundleKey getWsEnableDragAndDrop() {
        return wsEnableDragAndDrop;
    }

    public final @CheckForNull BundleKey getWsEnableFloating() {
        return wsEnableFloating;
    }

    public final @CheckForNull BundleKey getWsEnableMaximization() {
        return wsEnableMaximization;
    }

    public final @CheckForNull BundleKey getWsEnableMinimumSize() {
        return wsEnableMinimumSize;
    }

    public final @CheckForNull BundleKey getWsEnableResizing() {
        return wsEnableResizing;
    }

    public final @CheckForNull BundleKey getWsEnableSliding() {
        return wsEnableSliding;
    }

    public final @CheckForNull BundleKey getWsEnableAutoSlideInMinimizedMode() {
        return wsEnableAutoSlideInMinimizedMode;
    }

    public final @CheckForNull BundleKey getWsEnableEditorModeDnD() {
        return wsEnableEditorModeDnD;
    }

    public final @CheckForNull BundleKey getWsEnableEditorModeUndocking() {
        return wsEnableEditorModeUndocking;
    }

    public final @CheckForNull BundleKey getWsEnableModeClosing() {
        return wsEnableModeClosing;
    }

    public final @CheckForNull BundleKey getWsEnableModeSliding() {
        return wsEnableModeSliding;
    }

    public final @CheckForNull BundleKey getWsEnableViewModeDnD() {
        return wsEnableViewModeDnD;
    }

    public final @CheckForNull BundleKey getWsEnableViewModeUndocking() {
        return wsEnableViewModeUndocking;
    }

    public void doSave() {
        try {
            store();
        } catch (IOException ioE) {
            Exceptions.printStackTrace(ioE);
        }
    }

    public void reloadProperties() {}
    
    public abstract void updateProjectInternationalizationLocales();

    public Locale getLocale() {
        return locale;
    }
    
}
