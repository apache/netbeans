/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.Locale;
import org.netbeans.modules.cnd.api.project.NativeFileItem.LanguageFlavor;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.makeproject.configurations.ItemXMLCodec;

public class ItemConfiguration implements ConfigurationAuxObject, ConfigurationAuxObjectWithDictionary {

    // enabled by default for now, see #217779
    private boolean needSave = false;
    private Configuration configuration;
    private Item item;
    // General
    private BooleanConfiguration excluded;
    private PredefinedToolKind tool = PredefinedToolKind.UnknownTool;
    private boolean toolDirty = false;
    private LanguageFlavor languageFlavor = LanguageFlavor.UNKNOWN;
    // Tools
    private ConfigurationBase lastConfiguration;
    private CustomToolConfiguration customToolConfiguration;

    // cached id of item
//    private String id;
    public ItemConfiguration(Configuration configuration, Item item) {
        // General
        this.configuration = configuration;
        setItem(item);
        // we want non-default (bold) title to be only for excluded items => use false
        this.excluded = new BooleanConfiguration(false);

        // This is side effect of lazy configuration. We should init folder configuration
        // TODO: remove folder initialization. Folder should be responsible for it
        item.getFolder().getFolderConfiguration(configuration);

        clearChanged();
    }

    public ItemConfiguration(ItemConfiguration itemConfiguration) {
        this.configuration = itemConfiguration.configuration;
        this.item = itemConfiguration.item;
        this.excluded = itemConfiguration.excluded;
        this.needSave = itemConfiguration.needSave;
        this.tool = itemConfiguration.tool;
        this.languageFlavor = itemConfiguration.languageFlavor;
        this.lastConfiguration = itemConfiguration.lastConfiguration;
        this.customToolConfiguration = itemConfiguration.customToolConfiguration;
    }

    public boolean isDefaultConfiguration() {
        // was included => not default state to allow serialization
        if (!excluded.getValue()) {
            return false;
        }
        if (lastConfiguration != null && lastConfiguration.getModified()) {
            return false;
        }
        if (customToolConfiguration != null && customToolConfiguration.getModified()) {
            return false;
        }
        if (getLanguageFlavor() != null && getLanguageFlavor() != LanguageFlavor.UNKNOWN) {
            return false;
        }
        // we do not check tools for excluded items in unmanaged projects
        // but check for all logical as before
        if (!isItemFromDiskFolder()) {
            if (getTool() != item.getDefaultTool()) {
                return false;
            }
        }
        return true;
    }

    public boolean isCompilerToolConfiguration() {
        switch (getTool()) {
            case Assembler:
            case CCCompiler:
            case CCompiler:
            case FortranCompiler:
                return true;
        }
        return false;
    }

    public BasicCompilerConfiguration getCompilerConfiguration() {
        switch (getTool()) {
            case Assembler:
                return getAssemblerConfiguration();
            case CCCompiler:
                return getCCCompilerConfiguration();
            case CCompiler:
                return getCCompilerConfiguration();
            case FortranCompiler:
                return getFortranCompilerConfiguration();
        }
        return null;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public Item getItem() {
        return item;
    }

    private void setItem(Item item) {
        if (this.item != item) {
            this.item = item;
            this.needSave = true;
//            this.id = null;
        //this.tool = item.getDefaultTool();
        }
    }

    // General
    public BooleanConfiguration getExcluded() {
        return excluded;
    }

    public void setExcluded(BooleanConfiguration excluded) {
        this.excluded = excluded;
        needSave = true;
    }

    public boolean isToolDirty() {
        return toolDirty;
    }

    public void setToolDirty(boolean dirty) {
        toolDirty = dirty;
    }

    public void setTool(PredefinedToolKind tool) {
        if (this.tool != tool){
            lastConfiguration = null;
            toolDirty = true;
        }
        this.tool = tool;
   }

    public PredefinedToolKind getTool() {
        if (tool == PredefinedToolKind.UnknownTool) {
            tool = item.getDefaultTool();
        }
        return tool;
    }

    public void setLanguageFlavor(LanguageFlavor flavor) {
        this.languageFlavor = flavor;
        {
            CCompilerConfiguration conf = getCCompilerConfiguration();
            if (conf != null) {
                conf.setCStandardExternal(flavor.toExternal());
                return;
            }
        }
        {
            CCCompilerConfiguration conf = getCCCompilerConfiguration();
            if (conf != null) {
                conf.setCppStandardExternal(flavor.toExternal());
            }
        }
    }

    private void updateLanguageFlavor() {
        {
            CCompilerConfiguration conf = getCCompilerConfiguration();
            if (conf != null) {
                languageFlavor = LanguageFlavor.fromExternal(conf.getCStandardExternal());
                return;
            }
        }
        {
            CCCompilerConfiguration conf = getCCCompilerConfiguration();
            if (conf != null) {
                languageFlavor = LanguageFlavor.fromExternal(conf.getCppStandardExternal());
            }
        }
   }

    public LanguageFlavor getLanguageFlavor() {
        return languageFlavor;
    }

    public String[] getToolNames() {
        return new String[]{PredefinedToolKind.CCompiler.getDisplayName(), PredefinedToolKind.CCCompiler.getDisplayName(),
                            PredefinedToolKind.FortranCompiler.getDisplayName(), PredefinedToolKind.Assembler.getDisplayName(),
                            PredefinedToolKind.CustomTool.getDisplayName()};
    }

    // Custom Tool
    public void setCustomToolConfiguration(CustomToolConfiguration customToolConfiguration) {
        this.customToolConfiguration = customToolConfiguration;
    }

    public synchronized CustomToolConfiguration getCustomToolConfiguration() {
        if (getTool() == PredefinedToolKind.CustomTool || isProCFile()) {
            if (customToolConfiguration == null) {
                customToolConfiguration = new CustomToolConfiguration();
            }
            return customToolConfiguration;
        }
        return null;
    }

    // C Compiler
    public void setCCompilerConfiguration(CCompilerConfiguration cCompilerConfiguration) {
        this.lastConfiguration = cCompilerConfiguration;
    }

    public synchronized CCompilerConfiguration getCCompilerConfiguration() {
        if (getTool() == PredefinedToolKind.CCompiler) {
            if (lastConfiguration == null) {
                FolderConfiguration folderConfiguration = item.getFolder().getFolderConfiguration(configuration);
                if (folderConfiguration != null) {
                    lastConfiguration = new CCompilerConfiguration(((MakeConfiguration) configuration).getBaseDir(), folderConfiguration.getCCompilerConfiguration(), (MakeConfiguration) configuration);
                } else {
                    lastConfiguration = new CCompilerConfiguration(((MakeConfiguration) configuration).getBaseDir(), null, (MakeConfiguration) configuration);
                }
            }
            assert lastConfiguration instanceof CCompilerConfiguration;
            return  (CCompilerConfiguration) lastConfiguration;
        }
        return null;
    }

    // CC Compiler
    public void setCCCompilerConfiguration(CCCompilerConfiguration ccCompilerConfiguration) {
        this.lastConfiguration = ccCompilerConfiguration;
    }

    public synchronized CCCompilerConfiguration getCCCompilerConfiguration() {
        if (getTool() == PredefinedToolKind.CCCompiler) {
            if (lastConfiguration == null) {
                FolderConfiguration folderConfiguration = item.getFolder().getFolderConfiguration(configuration);
                if (folderConfiguration != null) {
                    lastConfiguration = new CCCompilerConfiguration(((MakeConfiguration) configuration).getBaseDir(), folderConfiguration.getCCCompilerConfiguration(), (MakeConfiguration) configuration);
                } else {
                    lastConfiguration = new CCCompilerConfiguration(((MakeConfiguration) configuration).getBaseDir(), null, (MakeConfiguration) configuration);
                }
            }
            assert lastConfiguration instanceof CCCompilerConfiguration;
            return  (CCCompilerConfiguration) lastConfiguration;
        }
        return null;
    }

    // Fortran Compiler
    public void setFortranCompilerConfiguration(FortranCompilerConfiguration fortranCompilerConfiguration) {
        this.lastConfiguration = fortranCompilerConfiguration;
    }

    public synchronized FortranCompilerConfiguration getFortranCompilerConfiguration() {
        if (getTool() == PredefinedToolKind.FortranCompiler) {
            if (lastConfiguration == null) {
                lastConfiguration = new FortranCompilerConfiguration(((MakeConfiguration) configuration).getBaseDir(), ((MakeConfiguration) configuration).getFortranCompilerConfiguration(), (MakeConfiguration) configuration);
            }
            assert lastConfiguration instanceof FortranCompilerConfiguration;
            return  (FortranCompilerConfiguration) lastConfiguration;
        }
        return null;
    }

    // Assembler
    public void setAssemblerConfiguration(AssemblerConfiguration assemblerConfiguration) {
        this.lastConfiguration = assemblerConfiguration;
    }

    public synchronized AssemblerConfiguration getAssemblerConfiguration() {
        if (getTool() == PredefinedToolKind.Assembler) {
            if (lastConfiguration == null) {
                lastConfiguration = new AssemblerConfiguration(((MakeConfiguration) configuration).getBaseDir(), ((MakeConfiguration) configuration).getAssemblerConfiguration(), (MakeConfiguration) configuration);
            }
            assert lastConfiguration instanceof AssemblerConfiguration;
            return  (AssemblerConfiguration) lastConfiguration;
        }
        return null;
    }

    // interface ConfigurationAuxObject
    @Override
    public boolean shared() {
        return true;
    }

    public boolean isVCSVisible() {
        if (item != null && getExcluded() != null && isItemFromDiskFolder()) {
            return !getExcluded().getValue();
        }
        return shared();
    }

    // interface ConfigurationAuxObject
    @Override
    public boolean hasChanged() {
        return needSave;
    }

    // interface ProfileAuxObject
    @Override
    public final void clearChanged() {
        needSave = false;
    }

    /**
     * Returns an unique id (String) used to retrive this object from the
     * pool of aux objects
     */
//    public String getId() {
//        if (this.id == null) {
//            this.id = getId(getItem().getPath());
//        }
//        assert this.id != null;
//        return this.id;
//    }
//
//    static public String getId(String path) {
//        return "item-" + path; // NOI18N
//    }
    @Override
    public String getId() {
        return item.getId();
    }

    @Override
    public void assign(ConfigurationAuxObject profileAuxObject) {
        if (!(profileAuxObject instanceof ItemConfiguration)) {
            // FIXUP: exception ????
            System.err.println("Item - assign: Profile object type expected - got " + profileAuxObject); // NOI18N
            return;
        }
        ItemConfiguration i = (ItemConfiguration) profileAuxObject;
        if (!getId().equals(i.getItem().getId())) {
            System.err.println("Item - assign: Item ID " + getId() + " expected - got " + i.getItem().getId()); // NOI18N
            return;
        }
        setConfiguration(i.getConfiguration());
        setItem(i.getItem());
        getExcluded().assign(i.getExcluded());
        setTool(i.getTool());
        switch (getTool()) {
            case Assembler:
                getAssemblerConfiguration().assign(i.getAssemblerConfiguration());
                break;
            case CCCompiler:
                getCCCompilerConfiguration().assign(i.getCCCompilerConfiguration());
                if(isProCFile()) {
                    getCustomToolConfiguration().assign(i.getCustomToolConfiguration());
                }
                break;
            case CCompiler:
                getCCompilerConfiguration().assign(i.getCCompilerConfiguration());
                if(isProCFile()) {
                    getCustomToolConfiguration().assign(i.getCustomToolConfiguration());
                }
                break;
            case CustomTool:
                getCustomToolConfiguration().assign(i.getCustomToolConfiguration());
                break;
            case FortranCompiler:
                getFortranCompilerConfiguration().assign(i.getFortranCompilerConfiguration());
                break;
            default:
                assert false;
        }
        updateLanguageFlavor();
    }

    public void assignValues(ConfigurationAuxObject profileAuxObject) {
        if (!(profileAuxObject instanceof ItemConfiguration)) {
            // FIXUP: exception ????
            System.err.println("Item - assign: Profile object type expected - got " + profileAuxObject); // NOI18N
            return;
        }
        ItemConfiguration i = (ItemConfiguration) profileAuxObject;
        getExcluded().assign(i.getExcluded());
        setTool(i.getTool());
        setLanguageFlavor(i.getLanguageFlavor());
        switch (getTool()) {
            case Assembler:
                getAssemblerConfiguration().assign(i.getAssemblerConfiguration());
                break;
            case CCCompiler:
                getCCCompilerConfiguration().assign(i.getCCCompilerConfiguration());
                if(isProCFile()) {
                    getCustomToolConfiguration().assign(i.getCustomToolConfiguration());
                }
                break;
            case CCompiler:
                getCCompilerConfiguration().assign(i.getCCompilerConfiguration());
                if(isProCFile()) {
                    getCustomToolConfiguration().assign(i.getCustomToolConfiguration());
                }
                break;
            case CustomTool:
                getCustomToolConfiguration().assign(i.getCustomToolConfiguration());
                break;
            case FortranCompiler:
                getFortranCompilerConfiguration().assign(i.getFortranCompilerConfiguration());
                break;
            default:
                assert false;
        }
    }

    public ItemConfiguration copy(MakeConfiguration makeConfiguration) {
        ItemConfiguration copy = new ItemConfiguration(makeConfiguration, getItem());
        // safe using
        copy.assign(this);
        copy.setConfiguration(makeConfiguration);
        return copy;
    }

    @Override
    public ItemConfiguration clone(Configuration conf) {
        ItemConfiguration i = new ItemConfiguration(conf, getItem());

        i.setExcluded(getExcluded().clone());
        i.setTool(getTool());
        switch (getTool()) {
            case Assembler:
                i.setAssemblerConfiguration(getAssemblerConfiguration().clone());
                break;
            case CCCompiler:
                i.setCCCompilerConfiguration(getCCCompilerConfiguration().clone());
                if(isProCFile()) {
                    i.setCustomToolConfiguration(getCustomToolConfiguration().clone());
                }
                break;
            case CCompiler:
                i.setCCompilerConfiguration(getCCompilerConfiguration().clone());
                if(isProCFile()) {
                    i.setCustomToolConfiguration(getCustomToolConfiguration().clone());
                }
                break;
            case CustomTool:
                i.setCustomToolConfiguration(getCustomToolConfiguration().clone());
                break;
            case FortranCompiler:
                i.setFortranCompilerConfiguration(getFortranCompilerConfiguration().clone());
                break;
            default:
                assert false;
        }
        return i;
    }

    //
    // XML codec support
    @Override
    public XMLDecoder getXMLDecoder() {
        return new ItemXMLCodec(this);
    }

    @Override
    public XMLEncoder getXMLEncoder() {
        return new ItemXMLCodec(this);
    }

    @Override
    public XMLEncoder getXMLEncoder(Dictionaries dictionaries) {
        return new ItemXMLCodec(this, dictionaries);
    }

    @Override
    public void initialize() {
        // FIXUP: this doesn't make sense...
    }

    public boolean isProCFile() {
        return isProCFile(item, tool);
    }

    public static boolean isProCFile(Item item, PredefinedToolKind tool) {
        if (tool == PredefinedToolKind.CCompiler
                || tool == PredefinedToolKind.CCCompiler) {
            if (item != null) {
                if (item.getName().toLowerCase(Locale.getDefault()).endsWith(".pc")) { //NOI18N
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isItemFromDiskFolder() {
        if (item != null) {
            Folder folder = item.getFolder();
            if (folder != null && folder.isDiskFolder()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String pref = "";
        if (this.excluded != null && excluded.getValue()) {
            pref = "[excluded]"; // NOI18N
        }
        return pref + getItem().getPath();
    }
}
