#Signature file v4.1
#Version 1.53

CLSS public abstract interface org.netbeans.modules.xml.jaxb.spi.JAXBWizModuleConstants
fld public final static java.lang.String CATALOG_FILE = "jaxb.catalog.file"
fld public final static java.lang.String EXISTING_SCHEMA_NAMES = "jaxb.cfg.existingSchemaNames"
fld public final static java.lang.String JAXB_BINDING_FILES = "jaxb.binding.files"
fld public final static java.lang.String JAXB_CLEAN_COMPILE_TARGET = "jaxb-clean-code-generation"
fld public final static java.lang.String JAXB_COMPILE_TARGET = "jaxb-code-generation"
fld public final static java.lang.String JAXB_COMPILE_TARGET_DEPENDS = "-pre-pre-compile"
fld public final static java.lang.String JAXB_OPTION_EXTENSION = "-extension"
fld public final static java.lang.String JAXB_OPTION_NPA = "-npa"
fld public final static java.lang.String JAXB_OPTION_NV = "-nv"
fld public final static java.lang.String JAXB_OPTION_QUIET = "-quiet"
fld public final static java.lang.String JAXB_OPTION_READ_ONLY = "-readOnly"
fld public final static java.lang.String JAXB_OPTION_VERBOSE = "-verbose"
fld public final static java.lang.String JAXB_SCHEMA_TYPE_DTD = "-dtd"
fld public final static java.lang.String JAXB_SCHEMA_TYPE_RELAX_NG = "-relaxng"
fld public final static java.lang.String JAXB_SCHEMA_TYPE_RELAX_NG_COMPACT = "-relaxng-compact"
fld public final static java.lang.String JAXB_SCHEMA_TYPE_WSDL = "-wsdl"
fld public final static java.lang.String JAXB_SCHEMA_TYPE_XML_SCHEMA = "-xmlschema"
fld public final static java.lang.String LAST_BROWSED_BINDING_DIR = "last.browsed.binding.dir"
fld public final static java.lang.String LAST_BROWSED_CATALOG_DIR = "last.browsed.catalog.dir"
fld public final static java.lang.String LAST_BROWSED_SCHEMA_DIR = "last.browsed.schema.dir"
fld public final static java.lang.String LOC_SCHEMA_ROOT = "localSchemaRoot"
fld public final static java.lang.String ORIG_LOCATION = "origLocation"
fld public final static java.lang.String ORIG_LOCATION_TYPE = "orginLocationType"
fld public final static java.lang.String PACKAGE_NAME = "xsd.package.name"
fld public final static java.lang.String PROJECT_DIR = "project.dir"
fld public final static java.lang.String PROJECT_NAME = "project.name"
fld public final static java.lang.String SCHEMA_NAME = "schema.name"
fld public final static java.lang.String SCHEMA_TYPE = "jaxb.schema.type"
fld public final static java.lang.String SOURCE_LOCATION_TYPE = "xsd.locatiom.type"
fld public final static java.lang.String SRC_LOC_TYPE_FS = "fileSystem"
fld public final static java.lang.String SRC_LOC_TYPE_URL = "url"
fld public final static java.lang.String WIZ_CONTENT_DISPLAYED = "WizardPanel_contentDisplayed"
fld public final static java.lang.String WIZ_CONTENT_NUMBERED = "WizardPanel_contentNumbered"
fld public final static java.lang.String WIZ_ERROR_MSG = "WizardPanel_errorMessage"
fld public final static java.lang.String WIZ_STYLE_AUTO = "WizardPanel_autoWizardStyle"
fld public final static java.lang.String XJC_OPTIONS = "jaxb.xjc.options"
fld public final static java.lang.String XSD_FILE_LIST = "xsd.file.list"
fld public final static java.math.BigDecimal LATEST_CFG_VERSION

CLSS public abstract interface org.netbeans.modules.xml.jaxb.spi.SchemaCompiler
meth public abstract void compileSchema(org.openide.WizardDescriptor)
meth public abstract void importResources(org.openide.WizardDescriptor) throws java.io.IOException

CLSS public abstract interface org.netbeans.modules.xml.jaxb.spi.SchemaCompilerProvider
meth public abstract org.netbeans.modules.xml.jaxb.spi.SchemaCompiler getSchemaCompiler(org.netbeans.api.project.Project)

