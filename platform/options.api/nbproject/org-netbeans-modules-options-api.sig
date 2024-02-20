#Signature file v4.1
#Version 1.68

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public abstract interface java.lang.annotation.Annotation
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.lang.Class<? extends java.lang.annotation.Annotation> annotationType()
meth public abstract java.lang.String toString()

CLSS public abstract interface !annotation java.lang.annotation.Documented
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation java.lang.annotation.Retention
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.RetentionPolicy value()

CLSS public abstract interface !annotation java.lang.annotation.Target
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.ElementType[] value()

CLSS public final org.netbeans.api.options.OptionsDisplayer
fld public final static java.lang.String ADVANCED = "Advanced"
fld public final static java.lang.String EDITOR = "Editor"
fld public final static java.lang.String FONTSANDCOLORS = "FontsAndColors"
fld public final static java.lang.String GENERAL = "General"
fld public final static java.lang.String KEYMAPS = "Keymaps"
meth public boolean open()
meth public boolean open(boolean)
meth public boolean open(java.lang.String)
meth public boolean open(java.lang.String,boolean)
meth public static org.netbeans.api.options.OptionsDisplayer getDefault()
supr java.lang.Object
hfds INSTANCE,categoryModel,currentCategoryID,impl,log,operationCancelled

CLSS public abstract org.netbeans.spi.options.AdvancedOption
cons protected init()
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getTooltip()
meth public abstract org.netbeans.spi.options.OptionsPanelController create()
supr java.lang.Object
hfds CONTROLLER,DISPLAYNAME,KEYWORDS,KEYWORDS_CATEGORY,TOOLTIP

CLSS public abstract org.netbeans.spi.options.OptionsCategory
cons public init()
meth public abstract java.lang.String getCategoryName()
meth public abstract java.lang.String getTitle()
meth public abstract org.netbeans.spi.options.OptionsPanelController create()
meth public java.lang.String getIconBase()
 anno 0 java.lang.Deprecated()
meth public javax.swing.Icon getIcon()
supr java.lang.Object
hfds ADVANCED_OPTIONS_FOLDER,CATEGORY_NAME,CONTROLLER,ICON,KEYWORDS,KEYWORDS_CATEGORY,TITLE

CLSS public abstract org.netbeans.spi.options.OptionsPanelController
cons public init()
fld public final static java.lang.String PROP_CHANGED = "changed"
fld public final static java.lang.String PROP_HELP_CTX = "helpCtx"
fld public final static java.lang.String PROP_VALID = "valid"
innr public abstract interface static !annotation ContainerRegistration
innr public abstract interface static !annotation Keywords
innr public abstract interface static !annotation KeywordsRegistration
innr public abstract interface static !annotation SubRegistration
innr public abstract interface static !annotation TopLevelRegistration
meth protected void setCurrentSubcategory(java.lang.String)
meth public abstract boolean isChanged()
meth public abstract boolean isValid()
meth public abstract javax.swing.JComponent getComponent(org.openide.util.Lookup)
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void applyChanges()
meth public abstract void cancel()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void update()
meth public final static org.netbeans.spi.options.OptionsPanelController createAdvanced(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public final void setSubcategory(java.lang.String)
meth public org.openide.util.Lookup getLookup()
meth public void handleSuccessfulSearch(java.lang.String,java.util.List<java.lang.String>)
supr java.lang.Object

CLSS public abstract interface static !annotation org.netbeans.spi.options.OptionsPanelController$ContainerRegistration
 outer org.netbeans.spi.options.OptionsPanelController
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PACKAGE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract !hasdefault java.lang.String keywords()
meth public abstract !hasdefault java.lang.String keywordsCategory()
meth public abstract java.lang.String categoryName()
meth public abstract java.lang.String iconBase()
meth public abstract java.lang.String id()

CLSS public abstract interface static !annotation org.netbeans.spi.options.OptionsPanelController$Keywords
 outer org.netbeans.spi.options.OptionsPanelController
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String tabTitle()
meth public abstract java.lang.String location()
meth public abstract java.lang.String[] keywords()

CLSS public abstract interface static !annotation org.netbeans.spi.options.OptionsPanelController$KeywordsRegistration
 outer org.netbeans.spi.options.OptionsPanelController
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract org.netbeans.spi.options.OptionsPanelController$Keywords[] value()

CLSS public abstract interface static !annotation org.netbeans.spi.options.OptionsPanelController$SubRegistration
 outer org.netbeans.spi.options.OptionsPanelController
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract !hasdefault java.lang.String id()
meth public abstract !hasdefault java.lang.String keywords()
meth public abstract !hasdefault java.lang.String keywordsCategory()
meth public abstract !hasdefault java.lang.String location()
meth public abstract java.lang.String displayName()

CLSS public abstract interface static !annotation org.netbeans.spi.options.OptionsPanelController$TopLevelRegistration
 outer org.netbeans.spi.options.OptionsPanelController
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract !hasdefault java.lang.String id()
meth public abstract !hasdefault java.lang.String keywords()
meth public abstract !hasdefault java.lang.String keywordsCategory()
meth public abstract java.lang.String categoryName()
meth public abstract java.lang.String iconBase()

