#Signature file v4.1
#Version 1.57.0

CLSS public com.ibm.icu.impl.Assert
cons public init()
meth public static void assrt(boolean)
meth public static void assrt(java.lang.String,boolean)
meth public static void fail(java.lang.Exception)
meth public static void fail(java.lang.String)
supr java.lang.Object

CLSS public final com.ibm.icu.impl.BMPSet
cons public init(com.ibm.icu.impl.BMPSet,int[],int)
cons public init(int[],int)
fld public static int U16_SURROGATE_OFFSET
meth public boolean contains(int)
meth public final int span(java.lang.CharSequence,int,int,com.ibm.icu.text.UnicodeSet$SpanCondition)
meth public final int spanBack(java.lang.CharSequence,int,com.ibm.icu.text.UnicodeSet$SpanCondition)
supr java.lang.Object
hfds bmpBlockBits,latin1Contains,list,list4kStarts,listLength,table7FF

CLSS public com.ibm.icu.impl.BOCU
meth public static int compress(java.lang.String,byte[],int)
meth public static int getCompressionLength(java.lang.String)
supr java.lang.Object
hfds SLOPE_LEAD_2_,SLOPE_LEAD_3_,SLOPE_MAX_,SLOPE_MIDDLE_,SLOPE_MIN_,SLOPE_REACH_NEG_1_,SLOPE_REACH_NEG_2_,SLOPE_REACH_NEG_3_,SLOPE_REACH_POS_1_,SLOPE_REACH_POS_2_,SLOPE_REACH_POS_3_,SLOPE_SINGLE_,SLOPE_START_NEG_2_,SLOPE_START_NEG_3_,SLOPE_START_POS_2_,SLOPE_START_POS_3_,SLOPE_TAIL_COUNT_

CLSS public abstract com.ibm.icu.impl.CacheBase<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object>
cons public init()
meth protected abstract {com.ibm.icu.impl.CacheBase%1} createInstance({com.ibm.icu.impl.CacheBase%0},{com.ibm.icu.impl.CacheBase%2})
meth public abstract {com.ibm.icu.impl.CacheBase%1} getInstance({com.ibm.icu.impl.CacheBase%0},{com.ibm.icu.impl.CacheBase%2})
supr java.lang.Object

CLSS public com.ibm.icu.impl.CalendarAstronomer
cons public init()
cons public init(double,double)
cons public init(java.util.Date)
cons public init(long)
fld public final static double SIDEREAL_DAY = 23.93446960027
fld public final static double SIDEREAL_MONTH = 27.32166
fld public final static double SIDEREAL_YEAR = 365.25636
fld public final static double SOLAR_DAY = 24.065709816
fld public final static double SYNODIC_MONTH = 29.530588853
fld public final static double TROPICAL_YEAR = 365.242191
fld public final static int HOUR_MS = 3600000
fld public final static int MINUTE_MS = 60000
fld public final static int SECOND_MS = 1000
fld public final static java.lang.Object AUTUMN_EQUINOX
fld public final static java.lang.Object FIRST_QUARTER
fld public final static java.lang.Object FULL_MOON
fld public final static java.lang.Object LAST_QUARTER
fld public final static java.lang.Object NEW_MOON
fld public final static java.lang.Object SUMMER_SOLSTICE
fld public final static java.lang.Object VERNAL_EQUINOX
fld public final static java.lang.Object WINTER_SOLSTICE
fld public final static long DAY_MS = 86400000
fld public final static long JULIAN_EPOCH_MS = -210866760000000
innr public final static Ecliptic
innr public final static Equatorial
innr public final static Horizon
meth public com.ibm.icu.impl.CalendarAstronomer$Equatorial getMoonPosition()
meth public com.ibm.icu.impl.CalendarAstronomer$Equatorial getSunPosition()
meth public com.ibm.icu.impl.CalendarAstronomer$Horizon eclipticToHorizon(double)
meth public double getGreenwichSidereal()
meth public double getJulianCentury()
meth public double getJulianDay()
meth public double getLocalSidereal()
meth public double getMoonAge()
meth public double getMoonPhase()
meth public double getSunLongitude()
meth public final com.ibm.icu.impl.CalendarAstronomer$Equatorial eclipticToEquatorial(com.ibm.icu.impl.CalendarAstronomer$Ecliptic)
meth public final com.ibm.icu.impl.CalendarAstronomer$Equatorial eclipticToEquatorial(double)
meth public final com.ibm.icu.impl.CalendarAstronomer$Equatorial eclipticToEquatorial(double,double)
meth public java.lang.String local(long)
meth public java.util.Date getDate()
meth public long getMoonRiseSet(boolean)
meth public long getMoonTime(com.ibm.icu.impl.CalendarAstronomer$MoonAge,boolean)
meth public long getMoonTime(double,boolean)
meth public long getSunRiseSet(boolean)
meth public long getSunTime(com.ibm.icu.impl.CalendarAstronomer$SolarLongitude,boolean)
meth public long getSunTime(double,boolean)
meth public long getTime()
meth public void setDate(java.util.Date)
meth public void setJulianDay(double)
meth public void setTime(long)
supr java.lang.Object
hfds DEG_RAD,EPOCH_2000_MS,INVALID,JD_EPOCH,PI,PI2,RAD_DEG,RAD_HOUR,SUN_E,SUN_ETA_G,SUN_OMEGA_G,eclipObliquity,fGmtOffset,fLatitude,fLongitude,julianCentury,julianDay,meanAnomalySun,moonA,moonE,moonEclipLong,moonI,moonL0,moonLongitude,moonN0,moonP0,moonPi,moonPosition,moonT0,siderealT0,siderealTime,sunLongitude,time
hcls AngleFunc,CoordFunc,MoonAge,SolarLongitude

CLSS public final static com.ibm.icu.impl.CalendarAstronomer$Ecliptic
 outer com.ibm.icu.impl.CalendarAstronomer
cons public init(double,double)
fld public final double latitude
fld public final double longitude
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final static com.ibm.icu.impl.CalendarAstronomer$Equatorial
 outer com.ibm.icu.impl.CalendarAstronomer
cons public init(double,double)
fld public final double ascension
fld public final double declination
meth public java.lang.String toHmsString()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final static com.ibm.icu.impl.CalendarAstronomer$Horizon
 outer com.ibm.icu.impl.CalendarAstronomer
cons public init(double,double)
fld public final double altitude
fld public final double azimuth
meth public java.lang.String toString()
supr java.lang.Object

CLSS public com.ibm.icu.impl.CalendarCache
cons public init()
fld public static long EMPTY
meth public long get(long)
meth public void put(long,long)
supr java.lang.Object
hfds arraySize,keys,pIndex,primes,size,threshold,values

CLSS public com.ibm.icu.impl.CalendarData
cons public init(com.ibm.icu.impl.ICUResourceBundle,java.lang.String)
cons public init(com.ibm.icu.util.ULocale,java.lang.String)
meth public com.ibm.icu.impl.ICUResourceBundle get(java.lang.String)
meth public com.ibm.icu.impl.ICUResourceBundle get(java.lang.String,java.lang.String)
meth public com.ibm.icu.impl.ICUResourceBundle get(java.lang.String,java.lang.String,java.lang.String)
meth public com.ibm.icu.util.ULocale getULocale()
meth public java.lang.String[] getDateTimePatterns()
meth public java.lang.String[] getEras(java.lang.String)
meth public java.lang.String[] getOverrides()
meth public java.lang.String[] getStringArray(java.lang.String)
meth public java.lang.String[] getStringArray(java.lang.String,java.lang.String)
meth public java.lang.String[] getStringArray(java.lang.String,java.lang.String,java.lang.String)
supr java.lang.Object
hfds fBundle,fFallbackType,fMainType

CLSS public com.ibm.icu.impl.CalendarUtil
cons public init()
meth public static java.lang.String getCalendarType(com.ibm.icu.util.ULocale)
supr java.lang.Object
hfds CALKEY,CALTYPE_CACHE,DEFCAL

CLSS public com.ibm.icu.impl.CharTrie
cons public init(int,int,com.ibm.icu.impl.Trie$DataManipulate)
cons public init(java.io.InputStream,com.ibm.icu.impl.Trie$DataManipulate) throws java.io.IOException
innr public FriendAgent
meth protected final int getInitialValue()
meth protected final int getSurrogateOffset(char,char)
meth protected final int getValue(int)
meth protected final void unserialize(java.io.InputStream) throws java.io.IOException
meth public boolean equals(java.lang.Object)
meth public final char getBMPValue(char)
meth public final char getCodePointValue(int)
meth public final char getLatin1LinearValue(char)
meth public final char getLeadValue(char)
meth public final char getSurrogateValue(char,char)
meth public final char getTrailValue(int,char)
meth public void putIndexData(com.ibm.icu.impl.UCharacterProperty)
supr com.ibm.icu.impl.Trie
hfds m_data_,m_friendAgent_,m_initialValue_

CLSS public com.ibm.icu.impl.CharTrie$FriendAgent
 outer com.ibm.icu.impl.CharTrie
cons public init(com.ibm.icu.impl.CharTrie)
meth public char[] getPrivateData()
meth public char[] getPrivateIndex()
meth public int getPrivateInitialValue()
supr java.lang.Object

CLSS public com.ibm.icu.impl.CharacterIteratorWrapper
cons public init(java.text.CharacterIterator)
meth public int current()
meth public int getIndex()
meth public int getLength()
meth public int getText(char[],int)
meth public int moveIndex(int)
meth public int next()
meth public int previous()
meth public java.lang.Object clone()
meth public java.text.CharacterIterator getCharacterIterator()
meth public void setIndex(int)
meth public void setToLimit()
supr com.ibm.icu.text.UCharacterIterator
hfds iterator

CLSS public com.ibm.icu.impl.CurrencyData
cons public init()
fld public final static com.ibm.icu.impl.CurrencyData$CurrencyDisplayInfoProvider provider
innr public abstract interface static CurrencyDisplayInfoProvider
innr public abstract static CurrencyDisplayInfo
innr public final static CurrencyFormatInfo
innr public final static CurrencySpacingInfo
innr public static DefaultInfo
supr java.lang.Object

CLSS public abstract static com.ibm.icu.impl.CurrencyData$CurrencyDisplayInfo
 outer com.ibm.icu.impl.CurrencyData
cons public init()
meth public abstract com.ibm.icu.impl.CurrencyData$CurrencyFormatInfo getFormatInfo(java.lang.String)
meth public abstract com.ibm.icu.impl.CurrencyData$CurrencySpacingInfo getSpacingInfo()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getUnitPatterns()
supr com.ibm.icu.text.CurrencyDisplayNames

CLSS public abstract interface static com.ibm.icu.impl.CurrencyData$CurrencyDisplayInfoProvider
 outer com.ibm.icu.impl.CurrencyData
meth public abstract boolean hasData()
meth public abstract com.ibm.icu.impl.CurrencyData$CurrencyDisplayInfo getInstance(com.ibm.icu.util.ULocale,boolean)

CLSS public final static com.ibm.icu.impl.CurrencyData$CurrencyFormatInfo
 outer com.ibm.icu.impl.CurrencyData
cons public init(java.lang.String,char,char)
fld public final char monetaryGroupingSeparator
fld public final char monetarySeparator
fld public final java.lang.String currencyPattern
supr java.lang.Object

CLSS public final static com.ibm.icu.impl.CurrencyData$CurrencySpacingInfo
 outer com.ibm.icu.impl.CurrencyData
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
fld public final java.lang.String afterContextMatch
fld public final java.lang.String afterCurrencyMatch
fld public final java.lang.String afterInsert
fld public final java.lang.String beforeContextMatch
fld public final java.lang.String beforeCurrencyMatch
fld public final java.lang.String beforeInsert
fld public final static com.ibm.icu.impl.CurrencyData$CurrencySpacingInfo DEFAULT
supr java.lang.Object
hfds DEFAULT_CTX_MATCH,DEFAULT_CUR_MATCH,DEFAULT_INSERT

CLSS public static com.ibm.icu.impl.CurrencyData$DefaultInfo
 outer com.ibm.icu.impl.CurrencyData
meth public com.ibm.icu.impl.CurrencyData$CurrencyFormatInfo getFormatInfo(java.lang.String)
meth public com.ibm.icu.impl.CurrencyData$CurrencySpacingInfo getSpacingInfo()
meth public com.ibm.icu.util.ULocale getLocale()
meth public final static com.ibm.icu.impl.CurrencyData$CurrencyDisplayInfo getWithFallback(boolean)
meth public java.lang.String getName(java.lang.String)
meth public java.lang.String getPluralName(java.lang.String,java.lang.String)
meth public java.lang.String getSymbol(java.lang.String)
meth public java.util.Map<java.lang.String,java.lang.String> getUnitPatterns()
meth public java.util.Map<java.lang.String,java.lang.String> nameMap()
meth public java.util.Map<java.lang.String,java.lang.String> symbolMap()
supr com.ibm.icu.impl.CurrencyData$CurrencyDisplayInfo
hfds FALLBACK_INSTANCE,NO_FALLBACK_INSTANCE,fallback

CLSS public final com.ibm.icu.impl.DateNumberFormat
cons public init(com.ibm.icu.util.ULocale,char)
meth public boolean equals(java.lang.Object)
meth public char getZeroDigit()
meth public int getMaximumIntegerDigits()
meth public int getMinimumIntegerDigits()
meth public java.lang.Number parse(java.lang.String,java.text.ParsePosition)
meth public java.lang.StringBuffer format(com.ibm.icu.math.BigDecimal,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.lang.StringBuffer format(double,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.lang.StringBuffer format(java.math.BigDecimal,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.lang.StringBuffer format(java.math.BigInteger,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.lang.StringBuffer format(long,java.lang.StringBuffer,java.text.FieldPosition)
meth public void setMaximumIntegerDigits(int)
meth public void setMinimumIntegerDigits(int)
meth public void setParsePositiveOnly(boolean)
meth public void setZeroDigit(char)
supr com.ibm.icu.text.NumberFormat
hfds CACHE,decimalBuf,maxIntDigits,minIntDigits,minusSign,positiveOnly,serialVersionUID,zeroDigit

CLSS public final com.ibm.icu.impl.Differ
cons public init(int,int)
meth public int find(java.lang.Object[],int,int,java.lang.Object[],int,int)
meth public int getACount()
meth public int getALine(int)
meth public int getBCount()
meth public int getBLine(int)
meth public java.lang.Object getA(int)
meth public java.lang.Object getB(int)
meth public void add(java.lang.Object,java.lang.Object)
meth public void addA(java.lang.Object)
meth public void addB(java.lang.Object)
meth public void checkMatch(boolean)
supr java.lang.Object
hfds EQUALSIZE,STACKSIZE,a,aCount,aLine,aTop,b,bCount,bLine,bTop,last,maxSame,next

CLSS public com.ibm.icu.impl.Grego
cons public init()
fld public final static int MILLIS_PER_DAY = 86400000
fld public final static int MILLIS_PER_HOUR = 3600000
fld public final static int MILLIS_PER_MINUTE = 60000
fld public final static int MILLIS_PER_SECOND = 1000
fld public final static long MAX_MILLIS = 183882168921600000
fld public final static long MIN_MILLIS = -184303902528000000
meth public final static boolean isLeapYear(int)
meth public final static int monthLength(int,int)
meth public final static int previousMonthLength(int,int)
meth public static int dayOfWeek(long)
meth public static int getDayOfWeekInMonth(int,int,int)
meth public static int[] dayToFields(long,int[])
meth public static int[] timeToFields(long,int[])
meth public static long fieldsToDay(int,int,int)
meth public static long floorDivide(long,long)
supr java.lang.Object
hfds DAYS_BEFORE,JULIAN_1970_CE,JULIAN_1_CE,MONTH_LENGTH

CLSS public final com.ibm.icu.impl.ICUBinary
cons public init()
innr public abstract interface static Authenticate
meth public final static byte[] readHeader(java.io.InputStream,byte[],com.ibm.icu.impl.ICUBinary$Authenticate) throws java.io.IOException
supr java.lang.Object
hfds BIG_ENDIAN_,CHAR_SET_,CHAR_SIZE_,HEADER_AUTHENTICATION_FAILED_,MAGIC1,MAGIC2,MAGIC_NUMBER_AUTHENTICATION_FAILED_

CLSS public abstract interface static com.ibm.icu.impl.ICUBinary$Authenticate
 outer com.ibm.icu.impl.ICUBinary
meth public abstract boolean isDataVersionAcceptable(byte[])

CLSS public abstract interface com.ibm.icu.impl.ICUCache<%0 extends java.lang.Object, %1 extends java.lang.Object>
fld public final static int SOFT = 0
fld public final static int WEAK = 1
fld public final static java.lang.Object NULL
meth public abstract void clear()
meth public abstract void put({com.ibm.icu.impl.ICUCache%0},{com.ibm.icu.impl.ICUCache%1})
meth public abstract {com.ibm.icu.impl.ICUCache%1} get(java.lang.Object)

CLSS public com.ibm.icu.impl.ICUConfig
cons public init()
fld public final static java.lang.String CONFIG_PROPS_FILE = "/com/ibm/icu/ICUConfig.properties"
meth public static java.lang.String get(java.lang.String)
meth public static java.lang.String get(java.lang.String,java.lang.String)
supr java.lang.Object
hfds CONFIG_PROPS

CLSS public com.ibm.icu.impl.ICUCurrencyDisplayInfoProvider
cons public init()
intf com.ibm.icu.impl.CurrencyData$CurrencyDisplayInfoProvider
meth public boolean hasData()
meth public com.ibm.icu.impl.CurrencyData$CurrencyDisplayInfo getInstance(com.ibm.icu.util.ULocale,boolean)
supr java.lang.Object
hcls ICUCurrencyDisplayInfo

CLSS public com.ibm.icu.impl.ICUCurrencyMetaInfo
cons public init()
meth public com.ibm.icu.text.CurrencyMetaInfo$CurrencyDigits currencyDigits(java.lang.String)
meth public java.util.List<com.ibm.icu.text.CurrencyMetaInfo$CurrencyInfo> currencyInfo(com.ibm.icu.text.CurrencyMetaInfo$CurrencyFilter)
meth public java.util.List<java.lang.String> currencies(com.ibm.icu.text.CurrencyMetaInfo$CurrencyFilter)
meth public java.util.List<java.lang.String> regions(com.ibm.icu.text.CurrencyMetaInfo$CurrencyFilter)
supr com.ibm.icu.text.CurrencyMetaInfo
hfds Currency,Date,MASK,Region,digitInfo,nonRegion,regionInfo
hcls Collector,CurrencyCollector,InfoCollector,RegionCollector,UniqueList

CLSS public final com.ibm.icu.impl.ICUData
cons public init()
meth public static boolean exists(java.lang.String)
meth public static java.io.InputStream getRequiredStream(java.lang.Class<?>,java.lang.String)
meth public static java.io.InputStream getRequiredStream(java.lang.ClassLoader,java.lang.String)
meth public static java.io.InputStream getRequiredStream(java.lang.String)
meth public static java.io.InputStream getStream(java.lang.Class<?>,java.lang.String)
meth public static java.io.InputStream getStream(java.lang.ClassLoader,java.lang.String)
meth public static java.io.InputStream getStream(java.lang.String)
supr java.lang.Object

CLSS public final com.ibm.icu.impl.ICUDataVersion
cons public init()
meth public static boolean isDataModified()
meth public static boolean isDataOlder(com.ibm.icu.util.VersionInfo)
meth public static com.ibm.icu.util.VersionInfo getDataVersion()
supr java.lang.Object
hfds U_ICU_DATA_KEY,U_ICU_STD_BUNDLE,U_ICU_VERSION_BUNDLE

CLSS public final com.ibm.icu.impl.ICUDebug
cons public init()
fld public final static boolean isJDK14OrHigher
fld public final static com.ibm.icu.util.VersionInfo javaVersion
fld public final static java.lang.String javaVersionString
meth public static boolean enabled()
meth public static boolean enabled(java.lang.String)
meth public static com.ibm.icu.util.VersionInfo getInstanceLenient(java.lang.String)
meth public static java.lang.String value(java.lang.String)
supr java.lang.Object
hfds debug,help,params

CLSS public com.ibm.icu.impl.ICULangDataTables
cons public init()
meth public com.ibm.icu.impl.LocaleDisplayNamesImpl$DataTable get(com.ibm.icu.util.ULocale)
meth public static java.lang.Object load(java.lang.String)
supr java.lang.Object

CLSS public com.ibm.icu.impl.ICULocaleService
cons public init()
cons public init(java.lang.String)
innr public abstract static LocaleKeyFactory
innr public static ICUResourceBundleFactory
innr public static LocaleKey
innr public static SimpleLocaleKeyFactory
meth public com.ibm.icu.impl.ICUService$Factory registerObject(java.lang.Object,com.ibm.icu.util.ULocale)
meth public com.ibm.icu.impl.ICUService$Factory registerObject(java.lang.Object,com.ibm.icu.util.ULocale,boolean)
meth public com.ibm.icu.impl.ICUService$Factory registerObject(java.lang.Object,com.ibm.icu.util.ULocale,int)
meth public com.ibm.icu.impl.ICUService$Factory registerObject(java.lang.Object,com.ibm.icu.util.ULocale,int,boolean)
meth public com.ibm.icu.impl.ICUService$Key createKey(com.ibm.icu.util.ULocale,int)
meth public com.ibm.icu.impl.ICUService$Key createKey(java.lang.String)
meth public com.ibm.icu.impl.ICUService$Key createKey(java.lang.String,int)
meth public com.ibm.icu.util.ULocale[] getAvailableULocales()
meth public java.lang.Object get(com.ibm.icu.util.ULocale)
meth public java.lang.Object get(com.ibm.icu.util.ULocale,com.ibm.icu.util.ULocale[])
meth public java.lang.Object get(com.ibm.icu.util.ULocale,int)
meth public java.lang.Object get(com.ibm.icu.util.ULocale,int,com.ibm.icu.util.ULocale[])
meth public java.lang.String validateFallbackLocale()
meth public java.util.Locale[] getAvailableLocales()
supr com.ibm.icu.impl.ICUService
hfds fallbackLocale,fallbackLocaleName

CLSS public static com.ibm.icu.impl.ICULocaleService$ICUResourceBundleFactory
 outer com.ibm.icu.impl.ICULocaleService
cons public init()
cons public init(java.lang.String)
fld protected final java.lang.String bundleName
meth protected java.lang.ClassLoader loader()
meth protected java.lang.Object handleCreate(com.ibm.icu.util.ULocale,int,com.ibm.icu.impl.ICUService)
meth protected java.util.Set<java.lang.String> getSupportedIDs()
meth public java.lang.String toString()
meth public void updateVisibleIDs(java.util.Map<java.lang.String,com.ibm.icu.impl.ICUService$Factory>)
supr com.ibm.icu.impl.ICULocaleService$LocaleKeyFactory

CLSS public static com.ibm.icu.impl.ICULocaleService$LocaleKey
 outer com.ibm.icu.impl.ICULocaleService
cons protected init(java.lang.String,java.lang.String,java.lang.String,int)
fld public final static int KIND_ANY = -1
meth public boolean fallback()
meth public boolean isFallbackOf(java.lang.String)
meth public com.ibm.icu.util.ULocale canonicalLocale()
meth public com.ibm.icu.util.ULocale currentLocale()
meth public int kind()
meth public java.lang.String canonicalID()
meth public java.lang.String currentDescriptor()
meth public java.lang.String currentID()
meth public java.lang.String prefix()
meth public static com.ibm.icu.impl.ICULocaleService$LocaleKey createWithCanonical(com.ibm.icu.util.ULocale,java.lang.String,int)
meth public static com.ibm.icu.impl.ICULocaleService$LocaleKey createWithCanonicalFallback(java.lang.String,java.lang.String)
meth public static com.ibm.icu.impl.ICULocaleService$LocaleKey createWithCanonicalFallback(java.lang.String,java.lang.String,int)
supr com.ibm.icu.impl.ICUService$Key
hfds currentID,fallbackID,kind,primaryID,varstart

CLSS public abstract static com.ibm.icu.impl.ICULocaleService$LocaleKeyFactory
 outer com.ibm.icu.impl.ICULocaleService
cons protected init(boolean)
cons protected init(boolean,java.lang.String)
fld protected final boolean visible
fld protected final java.lang.String name
fld public final static boolean INVISIBLE = false
fld public final static boolean VISIBLE = true
intf com.ibm.icu.impl.ICUService$Factory
meth protected boolean handlesKey(com.ibm.icu.impl.ICUService$Key)
meth protected boolean isSupportedID(java.lang.String)
meth protected java.lang.Object handleCreate(com.ibm.icu.util.ULocale,int,com.ibm.icu.impl.ICUService)
meth protected java.util.Set<java.lang.String> getSupportedIDs()
meth public java.lang.Object create(com.ibm.icu.impl.ICUService$Key,com.ibm.icu.impl.ICUService)
meth public java.lang.String getDisplayName(java.lang.String,com.ibm.icu.util.ULocale)
meth public java.lang.String toString()
meth public void updateVisibleIDs(java.util.Map<java.lang.String,com.ibm.icu.impl.ICUService$Factory>)
supr java.lang.Object

CLSS public static com.ibm.icu.impl.ICULocaleService$SimpleLocaleKeyFactory
 outer com.ibm.icu.impl.ICULocaleService
cons public init(java.lang.Object,com.ibm.icu.util.ULocale,int,boolean)
cons public init(java.lang.Object,com.ibm.icu.util.ULocale,int,boolean,java.lang.String)
meth protected boolean isSupportedID(java.lang.String)
meth public java.lang.Object create(com.ibm.icu.impl.ICUService$Key,com.ibm.icu.impl.ICUService)
meth public java.lang.String toString()
meth public void updateVisibleIDs(java.util.Map<java.lang.String,com.ibm.icu.impl.ICUService$Factory>)
supr com.ibm.icu.impl.ICULocaleService$LocaleKeyFactory
hfds id,kind,obj

CLSS public com.ibm.icu.impl.ICULogger
meth public boolean isLoggingOn()
meth public static com.ibm.icu.impl.ICULogger getICULogger(java.lang.String)
meth public static com.ibm.icu.impl.ICULogger getICULogger(java.lang.String,java.lang.String)
meth public void turnOffLogging()
meth public void turnOnLogging()
supr java.util.logging.Logger
hfds GLOBAL_FLAG_TURN_ON_LOGGING,SYSTEM_PROP_LOGGER,currentStatus
hcls LOGGER_STATUS

CLSS public abstract com.ibm.icu.impl.ICUNotifier
cons public init()
meth protected abstract boolean acceptsListener(java.util.EventListener)
meth protected abstract void notifyListener(java.util.EventListener)
meth public void addListener(java.util.EventListener)
meth public void notifyChanged()
meth public void removeListener(java.util.EventListener)
supr java.lang.Object
hfds listeners,notifyLock,notifyThread
hcls NotifyThread

CLSS public com.ibm.icu.impl.ICURWLock
cons public init()
innr public final static Stats
meth public com.ibm.icu.impl.ICURWLock$Stats clearStats()
meth public com.ibm.icu.impl.ICURWLock$Stats getStats()
meth public com.ibm.icu.impl.ICURWLock$Stats resetStats()
meth public void acquireRead()
meth public void acquireWrite()
meth public void releaseRead()
meth public void releaseWrite()
supr java.lang.Object
hfds NOTIFY_NONE,NOTIFY_READERS,NOTIFY_WRITERS,rc,readLock,stats,wrc,writeLock,wwc

CLSS public final static com.ibm.icu.impl.ICURWLock$Stats
 outer com.ibm.icu.impl.ICURWLock
fld public int _mrc
fld public int _rc
fld public int _wc
fld public int _wrc
fld public int _wwc
meth public java.lang.String toString()
supr java.lang.Object

CLSS public com.ibm.icu.impl.ICURegionDataTables
cons public init()
meth public com.ibm.icu.impl.LocaleDisplayNamesImpl$DataTable get(com.ibm.icu.util.ULocale)
meth public static java.lang.Object load(java.lang.String)
supr java.lang.Object

CLSS public com.ibm.icu.impl.ICUResourceBundle
cons protected init(com.ibm.icu.impl.ICUResourceBundleReader,java.lang.String,java.lang.String,int,com.ibm.icu.impl.ICUResourceBundle)
fld protected com.ibm.icu.impl.ICUCache<java.lang.Object,com.ibm.icu.util.UResourceBundle> lookup
fld protected com.ibm.icu.impl.ICUResourceBundleReader reader
fld protected com.ibm.icu.util.ULocale ulocale
fld protected final static java.lang.String ICU_DATA_PATH = "com/ibm/icu/impl/"
fld protected final static java.lang.String INSTALLED_LOCALES = "InstalledLocales"
fld protected int resource
fld protected java.lang.ClassLoader loader
fld protected java.lang.String baseName
fld protected java.lang.String key
fld protected java.lang.String localeID
fld protected java.lang.String resPath
fld public final static int ALIAS = 3
fld public final static int ARRAY16 = 9
fld public final static int FROM_DEFAULT = 3
fld public final static int FROM_FALLBACK = 1
fld public final static int FROM_LOCALE = 4
fld public final static int FROM_ROOT = 2
fld public final static int RES_BOGUS = -1
fld public final static int STRING_V2 = 6
fld public final static int TABLE16 = 5
fld public final static int TABLE32 = 4
fld public final static java.lang.ClassLoader ICU_DATA_CLASS_LOADER
fld public final static java.lang.String ICU_BASE_NAME = "com/ibm/icu/impl/data/icudt44b"
fld public final static java.lang.String ICU_BRKITR_BASE_NAME = "com/ibm/icu/impl/data/icudt44b/brkitr"
fld public final static java.lang.String ICU_BRKITR_NAME = "/brkitr"
fld public final static java.lang.String ICU_BUNDLE = "data/icudt44b"
fld public final static java.lang.String ICU_COLLATION_BASE_NAME = "com/ibm/icu/impl/data/icudt44b/coll"
fld public final static java.lang.String ICU_CURR_BASE_NAME = "com/ibm/icu/impl/data/icudt44b/curr"
fld public final static java.lang.String ICU_LANG_BASE_NAME = "com/ibm/icu/impl/data/icudt44b/lang"
fld public final static java.lang.String ICU_RBNF_BASE_NAME = "com/ibm/icu/impl/data/icudt44b/rbnf"
fld public final static java.lang.String ICU_REGION_BASE_NAME = "com/ibm/icu/impl/data/icudt44b/region"
fld public final static java.lang.String ICU_TRANSLIT_BASE_NAME = "com/ibm/icu/impl/data/icudt44b/translit"
fld public final static java.lang.String ICU_ZONE_BASE_NAME = "com/ibm/icu/impl/data/icudt44b/zone"
meth protected boolean isTopLevelResource()
meth protected com.ibm.icu.impl.ICUResourceBundle findResource(java.lang.String,int,java.util.HashMap<java.lang.String,java.lang.String>,com.ibm.icu.util.UResourceBundle)
meth protected com.ibm.icu.util.UResourceBundle handleGet(int,java.util.HashMap<java.lang.String,java.lang.String>,com.ibm.icu.util.UResourceBundle)
meth protected com.ibm.icu.util.UResourceBundle handleGet(java.lang.String,java.util.HashMap<java.lang.String,java.lang.String>,com.ibm.icu.util.UResourceBundle)
meth protected com.ibm.icu.util.UResourceBundle handleGetImpl(int,java.util.HashMap<java.lang.String,java.lang.String>,com.ibm.icu.util.UResourceBundle,boolean[])
meth protected com.ibm.icu.util.UResourceBundle handleGetImpl(java.lang.String,java.util.HashMap<java.lang.String,java.lang.String>,com.ibm.icu.util.UResourceBundle,int[],boolean[])
meth protected final static com.ibm.icu.impl.ICUResourceBundle findResourceWithFallback(java.lang.String,com.ibm.icu.util.UResourceBundle,com.ibm.icu.util.UResourceBundle)
meth protected int getTableResource(int)
meth protected int getTableResource(java.lang.String)
meth protected java.lang.String getBaseName()
meth protected java.lang.String getKey(int)
meth protected java.lang.String getLocaleID()
meth protected java.util.Enumeration<java.lang.String> handleGetKeys()
meth protected static com.ibm.icu.util.UResourceBundle instantiateBundle(java.lang.String,java.lang.String,java.lang.ClassLoader,boolean)
meth protected void createLookupCache()
meth protected void setParent(java.util.ResourceBundle)
meth public boolean equals(java.lang.Object)
meth public boolean isAlias()
meth public boolean isAlias(int)
meth public boolean isAlias(java.lang.String)
meth public com.ibm.icu.impl.ICUResourceBundle at(int)
meth public com.ibm.icu.impl.ICUResourceBundle at(java.lang.String)
meth public com.ibm.icu.impl.ICUResourceBundle findTopLevel(int)
meth public com.ibm.icu.impl.ICUResourceBundle findTopLevel(java.lang.String)
meth public com.ibm.icu.impl.ICUResourceBundle findWithFallback(java.lang.String)
meth public com.ibm.icu.impl.ICUResourceBundle getWithFallback(java.lang.String)
meth public com.ibm.icu.util.ULocale getULocale()
meth public com.ibm.icu.util.UResourceBundle getParent()
meth public final static com.ibm.icu.util.ULocale getFunctionalEquivalent(java.lang.String,java.lang.ClassLoader,java.lang.String,java.lang.String,com.ibm.icu.util.ULocale,boolean[],boolean)
meth public final static com.ibm.icu.util.ULocale[] getAvailableULocales()
meth public final static com.ibm.icu.util.ULocale[] getAvailableULocales(java.lang.String,java.lang.ClassLoader)
meth public final static java.lang.String[] getKeywordValues(java.lang.String,java.lang.String)
meth public final static java.util.Locale[] getAvailableLocales()
meth public final static java.util.Locale[] getAvailableLocales(java.lang.String,java.lang.ClassLoader)
meth public final static java.util.Locale[] getLocaleList(com.ibm.icu.util.ULocale[])
meth public int getLoadingStatus()
meth public int getType()
meth public java.lang.String getAliasPath()
meth public java.lang.String getAliasPath(int)
meth public java.lang.String getAliasPath(java.lang.String)
meth public java.lang.String getKey()
meth public java.lang.String getResPath()
meth public java.lang.String getStringWithFallback(java.lang.String)
meth public java.util.Enumeration<java.lang.String> getKeysSafe()
meth public java.util.Locale getLocale()
meth public static com.ibm.icu.impl.ICUResourceBundle createBundle(java.lang.String,java.lang.String,java.lang.ClassLoader)
meth public static com.ibm.icu.util.UResourceBundle getBundleInstance(java.lang.String,java.lang.String,java.lang.ClassLoader,boolean)
meth public static java.lang.String getFullName(java.lang.String,java.lang.String)
meth public static java.util.Set<java.lang.String> getAvailableLocaleNameSet()
meth public static java.util.Set<java.lang.String> getAvailableLocaleNameSet(java.lang.String,java.lang.ClassLoader)
meth public static java.util.Set<java.lang.String> getFullLocaleNameSet()
meth public static java.util.Set<java.lang.String> getFullLocaleNameSet(java.lang.String,java.lang.ClassLoader)
meth public void setLoadingStatus(int)
meth public void setLoadingStatus(java.lang.String)
supr com.ibm.icu.util.UResourceBundle
hfds DEBUG,DEFAULT_TAG,GET_AVAILABLE_CACHE,HYPHEN,ICUDATA,ICU_RESOURCE_INDEX,ICU_RESOURCE_SUFFIX,LOCALE,MAX_INITIAL_LOOKUP_SIZE,NULL_BUNDLE,RES_PATH_SEP_CHAR,RES_PATH_SEP_STR,cache,gPublicTypes,loadingStatus
hcls AvailEntry

CLSS public final com.ibm.icu.impl.ICUResourceBundleReader
intf com.ibm.icu.impl.ICUBinary$Authenticate
meth public boolean isDataVersionAcceptable(byte[])
supr java.lang.Object
hfds DATA_FORMAT_ID,DEBUG,URES_ATT_IS_POOL_BUNDLE,URES_ATT_NO_FALLBACK,URES_ATT_USES_POOL_BUNDLE,URES_INDEX_16BIT_TOP,URES_INDEX_ATTRIBUTES,URES_INDEX_BUNDLE_TOP,URES_INDEX_KEYS_TOP,URES_INDEX_LENGTH,URES_INDEX_POOL_CHECKSUM,dataVersion,emptyByteBuffer,emptyBytes,emptyChars,emptyInts,emptyString,indexes,isPoolBundle,keyStrings,keyStringsAsString,localKeyLimit,noFallback,poolBundleKeys,poolBundleKeysAsString,resourceBottom,resourceBytes,rootRes,s16BitUnits,usesPoolBundle
hcls Array,Array16,ByteSequence,Container,Table,Table16,Table1632,Table32

CLSS public com.ibm.icu.impl.ICUResourceTableAccess
cons public init()
meth public static java.lang.String getTableString(com.ibm.icu.impl.ICUResourceBundle,java.lang.String,java.lang.String,java.lang.String)
meth public static java.lang.String getTableString(java.lang.String,com.ibm.icu.util.ULocale,java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public com.ibm.icu.impl.ICUService
cons public init()
cons public init(java.lang.String)
fld protected final java.lang.String name
innr public abstract interface static Factory
innr public abstract interface static ServiceListener
innr public static Key
innr public static SimpleFactory
meth protected boolean acceptsListener(java.util.EventListener)
meth protected java.lang.Object handleDefault(com.ibm.icu.impl.ICUService$Key,java.lang.String[])
meth protected void clearCaches()
meth protected void clearServiceCache()
meth protected void markDefault()
meth protected void notifyListener(java.util.EventListener)
meth protected void reInitializeFactories()
meth public boolean isDefault()
meth public com.ibm.icu.impl.ICUService$Factory registerObject(java.lang.Object,java.lang.String)
meth public com.ibm.icu.impl.ICUService$Factory registerObject(java.lang.Object,java.lang.String,boolean)
meth public com.ibm.icu.impl.ICUService$Key createKey(java.lang.String)
meth public final boolean unregisterFactory(com.ibm.icu.impl.ICUService$Factory)
meth public final com.ibm.icu.impl.ICUService$Factory registerFactory(com.ibm.icu.impl.ICUService$Factory)
meth public final java.util.List<com.ibm.icu.impl.ICUService$Factory> factories()
meth public final void reset()
meth public java.lang.Object get(java.lang.String)
meth public java.lang.Object get(java.lang.String,java.lang.String[])
meth public java.lang.Object getKey(com.ibm.icu.impl.ICUService$Key)
meth public java.lang.Object getKey(com.ibm.icu.impl.ICUService$Key,java.lang.String[])
meth public java.lang.Object getKey(com.ibm.icu.impl.ICUService$Key,java.lang.String[],com.ibm.icu.impl.ICUService$Factory)
meth public java.lang.String getDisplayName(java.lang.String)
meth public java.lang.String getDisplayName(java.lang.String,com.ibm.icu.util.ULocale)
meth public java.lang.String getName()
meth public java.lang.String stats()
meth public java.lang.String toString()
meth public java.util.Set<java.lang.String> getVisibleIDs()
meth public java.util.Set<java.lang.String> getVisibleIDs(java.lang.String)
meth public java.util.SortedMap<java.lang.String,java.lang.String> getDisplayNames()
meth public java.util.SortedMap<java.lang.String,java.lang.String> getDisplayNames(com.ibm.icu.util.ULocale)
meth public java.util.SortedMap<java.lang.String,java.lang.String> getDisplayNames(com.ibm.icu.util.ULocale,java.lang.String)
meth public java.util.SortedMap<java.lang.String,java.lang.String> getDisplayNames(com.ibm.icu.util.ULocale,java.util.Comparator<java.lang.Object>)
meth public java.util.SortedMap<java.lang.String,java.lang.String> getDisplayNames(com.ibm.icu.util.ULocale,java.util.Comparator<java.lang.Object>,java.lang.String)
supr com.ibm.icu.impl.ICUNotifier
hfds DEBUG,cacheref,defaultSize,dnref,factories,factoryLock,idref
hcls CacheEntry,LocaleRef

CLSS public abstract interface static com.ibm.icu.impl.ICUService$Factory
 outer com.ibm.icu.impl.ICUService
meth public abstract java.lang.Object create(com.ibm.icu.impl.ICUService$Key,com.ibm.icu.impl.ICUService)
meth public abstract java.lang.String getDisplayName(java.lang.String,com.ibm.icu.util.ULocale)
meth public abstract void updateVisibleIDs(java.util.Map<java.lang.String,com.ibm.icu.impl.ICUService$Factory>)

CLSS public static com.ibm.icu.impl.ICUService$Key
 outer com.ibm.icu.impl.ICUService
cons public init(java.lang.String)
meth public boolean fallback()
meth public boolean isFallbackOf(java.lang.String)
meth public final java.lang.String id()
meth public java.lang.String canonicalID()
meth public java.lang.String currentDescriptor()
meth public java.lang.String currentID()
supr java.lang.Object
hfds id

CLSS public abstract interface static com.ibm.icu.impl.ICUService$ServiceListener
 outer com.ibm.icu.impl.ICUService
intf java.util.EventListener
meth public abstract void serviceChanged(com.ibm.icu.impl.ICUService)

CLSS public static com.ibm.icu.impl.ICUService$SimpleFactory
 outer com.ibm.icu.impl.ICUService
cons public init(java.lang.Object,java.lang.String)
cons public init(java.lang.Object,java.lang.String,boolean)
fld protected boolean visible
fld protected java.lang.Object instance
fld protected java.lang.String id
intf com.ibm.icu.impl.ICUService$Factory
meth public java.lang.Object create(com.ibm.icu.impl.ICUService$Key,com.ibm.icu.impl.ICUService)
meth public java.lang.String getDisplayName(java.lang.String,com.ibm.icu.util.ULocale)
meth public java.lang.String toString()
meth public void updateVisibleIDs(java.util.Map<java.lang.String,com.ibm.icu.impl.ICUService$Factory>)
supr java.lang.Object

CLSS public com.ibm.icu.impl.IllegalIcuArgumentException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
meth public com.ibm.icu.impl.IllegalIcuArgumentException initCause(java.lang.Throwable)
supr java.lang.IllegalArgumentException
hfds serialVersionUID

CLSS public com.ibm.icu.impl.ImplicitCEGenerator
cons public init(int,int)
cons public init(int,int,int,int,int,int)
fld public final static int CJK_A_BASE = 13312
fld public final static int CJK_A_LIMIT = 19904
fld public final static int CJK_BASE = 19968
fld public final static int CJK_B_BASE = 131072
fld public final static int CJK_B_LIMIT = 173792
fld public final static int CJK_COMPAT_USED_BASE = 64014
fld public final static int CJK_COMPAT_USED_LIMIT = 64048
fld public final static int CJK_LIMIT = 40960
meth public int getCodePointFromRaw(int)
meth public int getGap3()
meth public int getGap4()
meth public int getImplicitFromCodePoint(int)
meth public int getImplicitFromRaw(int)
meth public int getMaxTrail()
meth public int getMinTrail()
meth public int getRawFromCodePoint(int)
meth public int getRawFromImplicit(int)
meth public static int divideAndRoundUp(int,int)
supr java.lang.Object
hfds DEBUG,MAX_INPUT,NON_CJK_OFFSET,bottomByte,final3Count,final3Multiplier,final4Count,final4Multiplier,fourBytes,max3Trail,max4Primary,max4Trail,maxTrail,medialCount,min3Primary,min4Boundary,min4Primary,minTrail,topByte

CLSS public com.ibm.icu.impl.IntTrie
cons public init(int,int,com.ibm.icu.impl.Trie$DataManipulate)
cons public init(java.io.InputStream,com.ibm.icu.impl.Trie$DataManipulate) throws java.io.IOException
meth protected final int getInitialValue()
meth protected final int getSurrogateOffset(char,char)
meth protected final int getValue(int)
meth protected final void unserialize(java.io.InputStream) throws java.io.IOException
meth public boolean equals(java.lang.Object)
meth public final int getBMPValue(char)
meth public final int getCodePointValue(int)
meth public final int getLatin1LinearValue(char)
meth public final int getLeadValue(char)
meth public final int getSurrogateValue(char,char)
meth public final int getTrailValue(int,char)
supr com.ibm.icu.impl.Trie
hfds m_data_,m_initialValue_

CLSS public com.ibm.icu.impl.IntTrieBuilder
cons public init(com.ibm.icu.impl.IntTrieBuilder)
cons public init(int[],int,int,int,boolean)
fld protected int m_initialValue_
fld protected int[] m_data_
meth public boolean setRange(int,int,int,boolean)
meth public boolean setValue(int,int)
meth public com.ibm.icu.impl.IntTrie serialize(com.ibm.icu.impl.TrieBuilder$DataManipulate,com.ibm.icu.impl.Trie$DataManipulate)
meth public int getValue(int)
meth public int getValue(int,boolean[])
meth public int serialize(java.io.OutputStream,boolean,com.ibm.icu.impl.TrieBuilder$DataManipulate) throws java.io.IOException
supr com.ibm.icu.impl.TrieBuilder
hfds m_leadUnitValue_

CLSS public com.ibm.icu.impl.InvalidFormatException
cons public init()
cons public init(java.lang.String)
supr java.lang.Exception
hfds serialVersionUID

CLSS public com.ibm.icu.impl.IterableComparator<%0 extends java.lang.Object>
cons public init()
cons public init(java.util.Comparator<{com.ibm.icu.impl.IterableComparator%0}>)
cons public init(java.util.Comparator<{com.ibm.icu.impl.IterableComparator%0}>,boolean)
intf java.util.Comparator<java.lang.Iterable<{com.ibm.icu.impl.IterableComparator%0}>>
meth public int compare(java.lang.Iterable<{com.ibm.icu.impl.IterableComparator%0}>,java.lang.Iterable<{com.ibm.icu.impl.IterableComparator%0}>)
meth public static <%0 extends java.lang.Object> int compareIterables(java.lang.Iterable<{%%0}>,java.lang.Iterable<{%%0}>)
supr java.lang.Object
hfds NOCOMPARATOR,comparator,shorterFirst

CLSS public com.ibm.icu.impl.JavaTimeZone
cons public init()
cons public init(java.lang.String)
meth public boolean inDaylightTime(java.util.Date)
meth public boolean useDaylightTime()
meth public int getDSTSavings()
meth public int getOffset(int,int,int,int,int,int)
meth public int getRawOffset()
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.util.TimeZone unwrap()
meth public void getOffset(long,boolean,int[])
meth public void setRawOffset(int)
supr com.ibm.icu.util.TimeZone
hfds AVAILABLESET,javacal,javatz,serialVersionUID

CLSS public com.ibm.icu.impl.LocaleDisplayNamesImpl
cons public init(com.ibm.icu.util.ULocale,com.ibm.icu.text.LocaleDisplayNames$DialectHandling)
innr public final static !enum DataTableType
innr public static DataTable
meth public com.ibm.icu.text.LocaleDisplayNames$DialectHandling getDialectHandling()
meth public com.ibm.icu.util.ULocale getLocale()
meth public java.lang.String keyDisplayName(java.lang.String)
meth public java.lang.String keyValueDisplayName(java.lang.String,java.lang.String)
meth public java.lang.String languageDisplayName(java.lang.String)
meth public java.lang.String localeDisplayName(com.ibm.icu.util.ULocale)
meth public java.lang.String localeDisplayName(java.lang.String)
meth public java.lang.String localeDisplayName(java.util.Locale)
meth public java.lang.String regionDisplayName(java.lang.String)
meth public java.lang.String scriptDisplayName(int)
meth public java.lang.String scriptDisplayName(java.lang.String)
meth public java.lang.String variantDisplayName(java.lang.String)
meth public static boolean haveData(com.ibm.icu.impl.LocaleDisplayNamesImpl$DataTableType)
meth public static com.ibm.icu.text.LocaleDisplayNames getInstance(com.ibm.icu.util.ULocale,com.ibm.icu.text.LocaleDisplayNames$DialectHandling)
supr com.ibm.icu.text.LocaleDisplayNames
hfds appender,cache,dialectHandling,format,langData,locale,regionData
hcls Appender,Cache,DataTables,ICUDataTable,ICUDataTables,LangDataTables,RegionDataTables

CLSS public static com.ibm.icu.impl.LocaleDisplayNamesImpl$DataTable
 outer com.ibm.icu.impl.LocaleDisplayNamesImpl
cons public init()
supr java.lang.Object

CLSS public final static !enum com.ibm.icu.impl.LocaleDisplayNamesImpl$DataTableType
 outer com.ibm.icu.impl.LocaleDisplayNamesImpl
fld public final static com.ibm.icu.impl.LocaleDisplayNamesImpl$DataTableType LANG
fld public final static com.ibm.icu.impl.LocaleDisplayNamesImpl$DataTableType REGION
meth public static com.ibm.icu.impl.LocaleDisplayNamesImpl$DataTableType valueOf(java.lang.String)
meth public static com.ibm.icu.impl.LocaleDisplayNamesImpl$DataTableType[] values()
supr java.lang.Enum<com.ibm.icu.impl.LocaleDisplayNamesImpl$DataTableType>

CLSS public final com.ibm.icu.impl.LocaleIDParser
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
meth public java.lang.String getBaseName()
meth public java.lang.String getCountry()
meth public java.lang.String getKeywordValue(java.lang.String)
meth public java.lang.String getLanguage()
meth public java.lang.String getName()
meth public java.lang.String getScript()
meth public java.lang.String getVariant()
meth public java.lang.String[] getLanguageScriptCountryVariant()
meth public java.util.Iterator<java.lang.String> getKeywords()
meth public java.util.Map<java.lang.String,java.lang.String> getKeywordMap()
meth public void defaultKeywordValue(java.lang.String,java.lang.String)
meth public void parseBaseName()
meth public void setBaseName(java.lang.String)
meth public void setKeywordValue(java.lang.String,java.lang.String)
supr java.lang.Object
hfds COMMA,DONE,DOT,HYPHEN,ITEM_SEPARATOR,KEYWORD_ASSIGN,KEYWORD_SEPARATOR,UNDERSCORE,baseName,blen,buffer,canonicalize,hadCountry,id,index,keywords

CLSS public com.ibm.icu.impl.LocaleIDs
cons public init()
meth public static java.lang.String getCurrentCountryID(java.lang.String)
meth public static java.lang.String getCurrentLanguageID(java.lang.String)
meth public static java.lang.String getISO3Country(java.lang.String)
meth public static java.lang.String getISO3Language(java.lang.String)
meth public static java.lang.String threeToTwoLetterLanguage(java.lang.String)
meth public static java.lang.String threeToTwoLetterRegion(java.lang.String)
meth public static java.lang.String[] getISOCountries()
meth public static java.lang.String[] getISOLanguages()
supr java.lang.Object
hfds _countries,_countries3,_deprecatedCountries,_languages,_languages3,_obsoleteCountries,_obsoleteCountries3,_obsoleteLanguages,_obsoleteLanguages3,_replacementCountries,_replacementLanguages

CLSS public com.ibm.icu.impl.LocaleUtility
cons public init()
meth public static boolean isFallbackOf(java.lang.String,java.lang.String)
meth public static boolean isFallbackOf(java.util.Locale,java.util.Locale)
meth public static java.util.Locale fallback(java.util.Locale)
meth public static java.util.Locale getLocaleFromName(java.lang.String)
supr java.lang.Object

CLSS public com.ibm.icu.impl.MultiComparator<%0 extends java.lang.Object>
cons public !varargs init(java.util.Comparator<{com.ibm.icu.impl.MultiComparator%0}>[])
intf java.util.Comparator<{com.ibm.icu.impl.MultiComparator%0}>
meth public int compare({com.ibm.icu.impl.MultiComparator%0},{com.ibm.icu.impl.MultiComparator%0})
supr java.lang.Object
hfds comparators

CLSS public final com.ibm.icu.impl.Norm2AllModes
fld public final com.ibm.icu.impl.Norm2AllModes$ComposeNormalizer2 comp
fld public final com.ibm.icu.impl.Norm2AllModes$ComposeNormalizer2 fcc
fld public final com.ibm.icu.impl.Norm2AllModes$DecomposeNormalizer2 decomp
fld public final com.ibm.icu.impl.Norm2AllModes$FCDNormalizer2 fcd
fld public final com.ibm.icu.impl.Normalizer2Impl impl
fld public final static com.ibm.icu.impl.Norm2AllModes$NoopNormalizer2 NOOP_NORMALIZER2
innr public abstract static Normalizer2WithImpl
innr public final static ComposeNormalizer2
innr public final static DecomposeNormalizer2
innr public final static FCDNormalizer2
innr public final static NoopNormalizer2
meth public static com.ibm.icu.impl.Norm2AllModes getInstance(java.io.InputStream,java.lang.String)
meth public static com.ibm.icu.impl.Norm2AllModes getNFCInstance()
meth public static com.ibm.icu.impl.Norm2AllModes getNFKCInstance()
meth public static com.ibm.icu.impl.Norm2AllModes getNFKC_CFInstance()
meth public static com.ibm.icu.impl.Norm2AllModes$Normalizer2WithImpl getN2WithImpl(int)
meth public static com.ibm.icu.text.Normalizer2 getFCDNormalizer2()
supr java.lang.Object
hfds cache
hcls NFCSingleton,NFKCSingleton,NFKC_CFSingleton,Norm2AllModesSingleton

CLSS public final static com.ibm.icu.impl.Norm2AllModes$ComposeNormalizer2
 outer com.ibm.icu.impl.Norm2AllModes
cons public init(com.ibm.icu.impl.Normalizer2Impl,boolean)
meth protected void normalize(java.lang.CharSequence,com.ibm.icu.impl.Normalizer2Impl$ReorderingBuffer)
meth protected void normalizeAndAppend(java.lang.CharSequence,boolean,com.ibm.icu.impl.Normalizer2Impl$ReorderingBuffer)
meth public boolean hasBoundaryAfter(int)
meth public boolean hasBoundaryBefore(int)
meth public boolean isInert(int)
meth public boolean isNormalized(java.lang.CharSequence)
meth public com.ibm.icu.text.Normalizer$QuickCheckResult quickCheck(java.lang.CharSequence)
meth public int getQuickCheck(int)
meth public int spanQuickCheckYes(java.lang.CharSequence)
supr com.ibm.icu.impl.Norm2AllModes$Normalizer2WithImpl
hfds onlyContiguous

CLSS public final static com.ibm.icu.impl.Norm2AllModes$DecomposeNormalizer2
 outer com.ibm.icu.impl.Norm2AllModes
cons public init(com.ibm.icu.impl.Normalizer2Impl)
meth protected void normalize(java.lang.CharSequence,com.ibm.icu.impl.Normalizer2Impl$ReorderingBuffer)
meth protected void normalizeAndAppend(java.lang.CharSequence,boolean,com.ibm.icu.impl.Normalizer2Impl$ReorderingBuffer)
meth public boolean hasBoundaryAfter(int)
meth public boolean hasBoundaryBefore(int)
meth public boolean isInert(int)
meth public int getQuickCheck(int)
meth public int spanQuickCheckYes(java.lang.CharSequence)
supr com.ibm.icu.impl.Norm2AllModes$Normalizer2WithImpl

CLSS public final static com.ibm.icu.impl.Norm2AllModes$FCDNormalizer2
 outer com.ibm.icu.impl.Norm2AllModes
cons public init(com.ibm.icu.impl.Normalizer2Impl)
meth protected void normalize(java.lang.CharSequence,com.ibm.icu.impl.Normalizer2Impl$ReorderingBuffer)
meth protected void normalizeAndAppend(java.lang.CharSequence,boolean,com.ibm.icu.impl.Normalizer2Impl$ReorderingBuffer)
meth public boolean hasBoundaryAfter(int)
meth public boolean hasBoundaryBefore(int)
meth public boolean isInert(int)
meth public int getQuickCheck(int)
meth public int spanQuickCheckYes(java.lang.CharSequence)
supr com.ibm.icu.impl.Norm2AllModes$Normalizer2WithImpl

CLSS public final static com.ibm.icu.impl.Norm2AllModes$NoopNormalizer2
 outer com.ibm.icu.impl.Norm2AllModes
cons public init()
meth public boolean hasBoundaryAfter(int)
meth public boolean hasBoundaryBefore(int)
meth public boolean isInert(int)
meth public boolean isNormalized(java.lang.CharSequence)
meth public com.ibm.icu.text.Normalizer$QuickCheckResult quickCheck(java.lang.CharSequence)
meth public int spanQuickCheckYes(java.lang.CharSequence)
meth public java.lang.Appendable normalize(java.lang.CharSequence,java.lang.Appendable)
meth public java.lang.StringBuilder append(java.lang.StringBuilder,java.lang.CharSequence)
meth public java.lang.StringBuilder normalize(java.lang.CharSequence,java.lang.StringBuilder)
meth public java.lang.StringBuilder normalizeSecondAndAppend(java.lang.StringBuilder,java.lang.CharSequence)
supr com.ibm.icu.text.Normalizer2

CLSS public abstract static com.ibm.icu.impl.Norm2AllModes$Normalizer2WithImpl
 outer com.ibm.icu.impl.Norm2AllModes
cons public init(com.ibm.icu.impl.Normalizer2Impl)
fld public final com.ibm.icu.impl.Normalizer2Impl impl
meth protected abstract void normalize(java.lang.CharSequence,com.ibm.icu.impl.Normalizer2Impl$ReorderingBuffer)
meth protected abstract void normalizeAndAppend(java.lang.CharSequence,boolean,com.ibm.icu.impl.Normalizer2Impl$ReorderingBuffer)
meth public boolean isNormalized(java.lang.CharSequence)
meth public com.ibm.icu.text.Normalizer$QuickCheckResult quickCheck(java.lang.CharSequence)
meth public int getQuickCheck(int)
meth public java.lang.Appendable normalize(java.lang.CharSequence,java.lang.Appendable)
meth public java.lang.StringBuilder append(java.lang.StringBuilder,java.lang.CharSequence)
meth public java.lang.StringBuilder normalize(java.lang.CharSequence,java.lang.StringBuilder)
meth public java.lang.StringBuilder normalizeSecondAndAppend(java.lang.StringBuilder,java.lang.CharSequence)
meth public java.lang.StringBuilder normalizeSecondAndAppend(java.lang.StringBuilder,java.lang.CharSequence,boolean)
supr com.ibm.icu.text.Normalizer2

CLSS public final com.ibm.icu.impl.Normalizer2Impl
cons public init()
fld public final static int COMP_1_LAST_TUPLE = 32768
fld public final static int COMP_1_TRAIL_LIMIT = 13312
fld public final static int COMP_1_TRAIL_MASK = 32766
fld public final static int COMP_1_TRAIL_SHIFT = 9
fld public final static int COMP_1_TRIPLE = 1
fld public final static int COMP_2_TRAIL_MASK = 65472
fld public final static int COMP_2_TRAIL_SHIFT = 6
fld public final static int IX_COUNT = 16
fld public final static int IX_EXTRA_DATA_OFFSET = 1
fld public final static int IX_LIMIT_NO_NO = 12
fld public final static int IX_MIN_COMP_NO_MAYBE_CP = 9
fld public final static int IX_MIN_DECOMP_NO_CP = 8
fld public final static int IX_MIN_MAYBE_YES = 13
fld public final static int IX_MIN_NO_NO = 11
fld public final static int IX_MIN_YES_NO = 10
fld public final static int IX_NORM_TRIE_OFFSET = 0
fld public final static int IX_RESERVED2_OFFSET = 2
fld public final static int IX_TOTAL_SIZE = 7
fld public final static int JAMO_L = 1
fld public final static int JAMO_VT = 65280
fld public final static int MAPPING_HAS_CCC_LCCC_WORD = 128
fld public final static int MAPPING_LENGTH_MASK = 31
fld public final static int MAPPING_NO_COMP_BOUNDARY_AFTER = 32
fld public final static int MAPPING_PLUS_COMPOSITION_LIST = 64
fld public final static int MAX_DELTA = 64
fld public final static int MIN_CCC_LCCC_CP = 768
fld public final static int MIN_NORMAL_MAYBE_YES = 65024
fld public final static int MIN_YES_YES_WITH_CC = 65281
innr public final static Hangul
innr public final static ReorderingBuffer
innr public final static UTF16Plus
meth public boolean compose(java.lang.CharSequence,int,int,boolean,boolean,com.ibm.icu.impl.Normalizer2Impl$ReorderingBuffer)
meth public boolean getCanonStartSet(int,com.ibm.icu.text.UnicodeSet)
meth public boolean hasCompBoundaryAfter(int,boolean,boolean)
meth public boolean hasCompBoundaryBefore(int)
meth public boolean hasDecompBoundary(int,boolean)
meth public boolean hasFCDBoundaryAfter(int)
meth public boolean hasFCDBoundaryBefore(int)
meth public boolean isCanonSegmentStarter(int)
meth public boolean isCompNo(int)
meth public boolean isDecompInert(int)
meth public boolean isDecompYes(int)
meth public boolean isFCDInert(int)
meth public com.ibm.icu.impl.Normalizer2Impl ensureCanonIterData()
meth public com.ibm.icu.impl.Normalizer2Impl load(java.io.InputStream)
meth public com.ibm.icu.impl.Normalizer2Impl load(java.lang.String)
meth public com.ibm.icu.impl.Trie2_16 getFCDTrie()
meth public com.ibm.icu.impl.Trie2_16 getNormTrie()
meth public int composeQuickCheck(java.lang.CharSequence,int,int,boolean,boolean)
meth public int decompose(java.lang.CharSequence,int,int,com.ibm.icu.impl.Normalizer2Impl$ReorderingBuffer)
meth public int getCC(int)
meth public int getCompQuickCheck(int)
meth public int getFCD16(int)
meth public int getFCD16FromSingleLead(char)
meth public int getNorm16(int)
meth public int makeFCD(java.lang.CharSequence,int,int,com.ibm.icu.impl.Normalizer2Impl$ReorderingBuffer)
meth public java.lang.String getDecomposition(int)
meth public static int getCCFromYesOrMaybe(int)
meth public void addCanonIterPropertyStarts(com.ibm.icu.text.UnicodeSet)
meth public void addPropertyStarts(com.ibm.icu.text.UnicodeSet)
meth public void composeAndAppend(java.lang.CharSequence,boolean,boolean,com.ibm.icu.impl.Normalizer2Impl$ReorderingBuffer)
meth public void decomposeAndAppend(java.lang.CharSequence,boolean,com.ibm.icu.impl.Normalizer2Impl$ReorderingBuffer)
meth public void decomposeShort(java.lang.CharSequence,int,int,com.ibm.icu.impl.Normalizer2Impl$ReorderingBuffer)
meth public void makeFCDAndAppend(java.lang.CharSequence,boolean,com.ibm.icu.impl.Normalizer2Impl$ReorderingBuffer)
supr java.lang.Object
hfds CANON_HAS_COMPOSITIONS,CANON_HAS_SET,CANON_NOT_SEGMENT_STARTER,CANON_VALUE_MASK,READER,canonIterData,canonStartSets,dataVersion,extraData,fcdTrie,limitNoNo,maybeYesCompositions,minCompNoMaybeCP,minDecompNoCP,minMaybeYes,minNoNo,minYesNo,normTrie,segmentStarterMapper
hcls Reader

CLSS public final static com.ibm.icu.impl.Normalizer2Impl$Hangul
 outer com.ibm.icu.impl.Normalizer2Impl
cons public init()
fld public final static int HANGUL_BASE = 44032
fld public final static int HANGUL_COUNT = 11172
fld public final static int HANGUL_LIMIT = 55204
fld public final static int JAMO_L_BASE = 4352
fld public final static int JAMO_L_COUNT = 19
fld public final static int JAMO_L_LIMIT = 4371
fld public final static int JAMO_T_BASE = 4519
fld public final static int JAMO_T_COUNT = 28
fld public final static int JAMO_VT_COUNT = 588
fld public final static int JAMO_V_BASE = 4449
fld public final static int JAMO_V_COUNT = 21
fld public final static int JAMO_V_LIMIT = 4470
meth public static boolean isHangul(int)
meth public static boolean isHangulWithoutJamoT(char)
meth public static boolean isJamoL(int)
meth public static boolean isJamoV(int)
meth public static int decompose(int,java.lang.Appendable)
supr java.lang.Object

CLSS public final static com.ibm.icu.impl.Normalizer2Impl$ReorderingBuffer
 outer com.ibm.icu.impl.Normalizer2Impl
cons public init(com.ibm.icu.impl.Normalizer2Impl,java.lang.Appendable,int)
intf java.lang.Appendable
meth public boolean equals(java.lang.CharSequence,int,int)
meth public boolean isEmpty()
meth public com.ibm.icu.impl.Normalizer2Impl$ReorderingBuffer append(char)
meth public com.ibm.icu.impl.Normalizer2Impl$ReorderingBuffer append(java.lang.CharSequence)
meth public com.ibm.icu.impl.Normalizer2Impl$ReorderingBuffer append(java.lang.CharSequence,int,int)
meth public com.ibm.icu.impl.Normalizer2Impl$ReorderingBuffer flushAndAppendZeroCC(java.lang.CharSequence,int,int)
meth public int getLastCC()
meth public int length()
meth public java.lang.StringBuilder getStringBuilder()
meth public void append(int,int)
meth public void append(java.lang.CharSequence,int,int,int,int)
meth public void appendZeroCC(int)
meth public void flush()
meth public void remove()
meth public void removeSuffix(int)
meth public void setLastChar(char)
supr java.lang.Object
hfds app,appIsStringBuilder,codePointLimit,codePointStart,impl,lastCC,reorderStart,str

CLSS public final static com.ibm.icu.impl.Normalizer2Impl$UTF16Plus
 outer com.ibm.icu.impl.Normalizer2Impl
cons public init()
meth public static boolean equal(java.lang.CharSequence,int,int,java.lang.CharSequence,int,int)
meth public static boolean equal(java.lang.CharSequence,java.lang.CharSequence)
meth public static boolean isSurrogateLead(int)
supr java.lang.Object

CLSS public com.ibm.icu.impl.OlsonTimeZone
cons public init(com.ibm.icu.util.UResourceBundle,com.ibm.icu.util.UResourceBundle)
cons public init(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public boolean hasSameRules(com.ibm.icu.util.TimeZone)
meth public boolean inDaylightTime(java.util.Date)
meth public boolean useDaylightTime()
meth public com.ibm.icu.util.TimeZoneRule[] getTimeZoneRules()
meth public com.ibm.icu.util.TimeZoneTransition getNextTransition(long,boolean)
meth public com.ibm.icu.util.TimeZoneTransition getPreviousTransition(long,boolean)
meth public int getDSTSavings()
meth public int getOffset(int,int,int,int,int,int)
meth public int getOffset(int,int,int,int,int,int,int)
meth public int getRawOffset()
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.String toString()
meth public void getOffset(long,boolean,int[])
meth public void getOffsetFromLocal(long,int,int,int[])
meth public void setID(java.lang.String)
meth public void setRawOffset(int)
supr com.ibm.icu.util.BasicTimeZone
hfds DEBUG,SECONDS_PER_DAY,ZONEINFORES,currentSerialVersion,finalStartMillis,finalStartYear,finalZone,finalZoneWithStartYear,firstFinalTZTransition,firstTZTransition,firstTZTransitionIdx,historicRules,initialRule,serialVersionOnStream,serialVersionUID,transitionCount,transitionRulesInitialized,transitionTimes64,typeCount,typeMapData,typeOffsets

CLSS public com.ibm.icu.impl.PVecToTrieCompactHandler
cons public init()
fld public com.ibm.icu.impl.IntTrieBuilder builder
fld public int initialValue
intf com.ibm.icu.impl.PropsVectors$CompactHandler
meth public void setRowIndexForErrorValue(int)
meth public void setRowIndexForInitialValue(int)
meth public void setRowIndexForRange(int,int,int)
meth public void startRealValues(int)
supr java.lang.Object

CLSS public com.ibm.icu.impl.PatternTokenizer
cons public init()
fld public final static char BACK_SLASH = '\u005c'
fld public final static char SINGLE_QUOTE = '''
fld public final static int BROKEN_ESCAPE = 4
fld public final static int BROKEN_QUOTE = 3
fld public final static int DONE = 0
fld public final static int LITERAL = 2
fld public final static int SYNTAX = 1
fld public final static int UNKNOWN = 5
meth public boolean isUsingQuote()
meth public boolean isUsingSlash()
meth public com.ibm.icu.impl.PatternTokenizer setEscapeCharacters(com.ibm.icu.text.UnicodeSet)
meth public com.ibm.icu.impl.PatternTokenizer setExtraQuotingCharacters(com.ibm.icu.text.UnicodeSet)
meth public com.ibm.icu.impl.PatternTokenizer setIgnorableCharacters(com.ibm.icu.text.UnicodeSet)
meth public com.ibm.icu.impl.PatternTokenizer setLimit(int)
meth public com.ibm.icu.impl.PatternTokenizer setPattern(java.lang.CharSequence)
meth public com.ibm.icu.impl.PatternTokenizer setPattern(java.lang.String)
meth public com.ibm.icu.impl.PatternTokenizer setStart(int)
meth public com.ibm.icu.impl.PatternTokenizer setSyntaxCharacters(com.ibm.icu.text.UnicodeSet)
meth public com.ibm.icu.impl.PatternTokenizer setUsingQuote(boolean)
meth public com.ibm.icu.impl.PatternTokenizer setUsingSlash(boolean)
meth public com.ibm.icu.text.UnicodeSet getEscapeCharacters()
meth public com.ibm.icu.text.UnicodeSet getExtraQuotingCharacters()
meth public com.ibm.icu.text.UnicodeSet getIgnorableCharacters()
meth public com.ibm.icu.text.UnicodeSet getSyntaxCharacters()
meth public int getLimit()
meth public int getStart()
meth public int next(java.lang.StringBuffer)
meth public java.lang.String normalize()
meth public java.lang.String quoteLiteral(java.lang.CharSequence)
meth public java.lang.String quoteLiteral(java.lang.String)
supr java.lang.Object
hfds AFTER_QUOTE,HEX,IN_QUOTE,NONE,NORMAL_QUOTE,NO_QUOTE,SLASH_START,START_QUOTE,escapeCharacters,extraQuotingCharacters,ignorableCharacters,limit,needingQuoteCharacters,pattern,start,syntaxCharacters,usingQuote,usingSlash

CLSS public com.ibm.icu.impl.PluralRulesLoader
fld public final static com.ibm.icu.impl.PluralRulesLoader loader
meth public com.ibm.icu.text.PluralRules forLocale(com.ibm.icu.util.ULocale)
meth public com.ibm.icu.text.PluralRules getRulesForRulesId(java.lang.String)
meth public com.ibm.icu.util.ULocale getFunctionalEquivalent(com.ibm.icu.util.ULocale,boolean[])
meth public com.ibm.icu.util.ULocale[] getAvailableULocales()
meth public com.ibm.icu.util.UResourceBundle getPluralBundle()
meth public java.lang.String getRulesIdForLocale(com.ibm.icu.util.ULocale)
supr java.lang.Object
hfds localeIdToRulesId,rulesIdToEquivalentULocale,rulesIdToRules

CLSS public com.ibm.icu.impl.PropsVectors
cons public init(int)
fld public final static int ERROR_VALUE_CP = 1114113
fld public final static int FIRST_SPECIAL_CP = 1114112
fld public final static int INITIAL_ROWS = 4096
fld public final static int INITIAL_VALUE_CP = 1114112
fld public final static int MAX_CP = 1114113
fld public final static int MAX_ROWS = 1114114
fld public final static int MEDIUM_ROWS = 65536
innr public abstract interface static CompactHandler
meth public com.ibm.icu.impl.IntTrie compactToTrieWithRowIndexes()
meth public int getCompactedColumns()
meth public int getCompactedRows()
meth public int getRowEnd(int)
meth public int getRowStart(int)
meth public int getValue(int,int)
meth public int[] getCompactedArray()
meth public int[] getRow(int)
meth public void compact(com.ibm.icu.impl.PropsVectors$CompactHandler)
meth public void setValue(int,int,int,int,int)
supr java.lang.Object
hfds columns,isCompacted,maxRows,prevRow,rows,v
hcls DefaultGetFoldedValue,DefaultGetFoldingOffset

CLSS public abstract interface static com.ibm.icu.impl.PropsVectors$CompactHandler
 outer com.ibm.icu.impl.PropsVectors
meth public abstract void setRowIndexForErrorValue(int)
meth public abstract void setRowIndexForInitialValue(int)
meth public abstract void setRowIndexForRange(int,int,int)
meth public abstract void startRealValues(int)

CLSS public final com.ibm.icu.impl.Punycode
cons public init()
meth public static java.lang.StringBuffer decode(java.lang.StringBuffer,boolean[]) throws com.ibm.icu.text.StringPrepParseException
meth public static java.lang.StringBuffer encode(java.lang.StringBuffer,boolean[]) throws com.ibm.icu.text.StringPrepParseException
supr java.lang.Object
hfds BASE,CAPITAL_A,CAPITAL_Z,DAMP,DELIMITER,HYPHEN,INITIAL_BIAS,INITIAL_N,MAX_CP_COUNT,SKEW,SMALL_A,SMALL_Z,TMAX,TMIN,ZERO,basicToDigit

CLSS public com.ibm.icu.impl.RelativeDateFormat
cons public init(int,int,com.ibm.icu.util.ULocale)
innr public URelativeString
meth public java.lang.StringBuffer format(com.ibm.icu.util.Calendar,java.lang.StringBuffer,java.text.FieldPosition)
meth public void parse(java.lang.String,com.ibm.icu.util.Calendar,java.text.ParsePosition)
supr com.ibm.icu.text.DateFormat
hfds fCombinedFormat,fDateFormat,fDateStyle,fDates,fLocale,fTimeFormat,fTimeStyle,serialVersionUID

CLSS public com.ibm.icu.impl.RelativeDateFormat$URelativeString
 outer com.ibm.icu.impl.RelativeDateFormat
fld public int offset
fld public java.lang.String string
supr java.lang.Object

CLSS public com.ibm.icu.impl.ReplaceableUCharacterIterator
cons public init(com.ibm.icu.text.Replaceable)
cons public init(java.lang.String)
cons public init(java.lang.StringBuffer)
meth public int current()
meth public int currentCodePoint()
meth public int getIndex()
meth public int getLength()
meth public int getText(char[],int)
meth public int next()
meth public int previous()
meth public java.lang.Object clone()
meth public void setIndex(int)
supr com.ibm.icu.text.UCharacterIterator
hfds currentIndex,replaceable

CLSS public com.ibm.icu.impl.ResourceBundleWrapper
meth protected java.lang.Object handleGetObject(java.lang.String)
meth protected java.lang.String getBaseName()
meth protected java.lang.String getLocaleID()
meth protected static com.ibm.icu.util.UResourceBundle instantiateBundle(java.lang.String,java.lang.String,java.lang.ClassLoader,boolean)
meth protected void setLoadingStatus(int)
meth public com.ibm.icu.util.ULocale getULocale()
meth public com.ibm.icu.util.UResourceBundle getParent()
meth public java.util.Enumeration<java.lang.String> getKeys()
meth public static com.ibm.icu.util.UResourceBundle getBundleInstance(java.lang.String,java.lang.String,java.lang.ClassLoader,boolean)
supr com.ibm.icu.util.UResourceBundle
hfds DEBUG,baseName,bundle,keys,localeID

CLSS public com.ibm.icu.impl.Row<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object, %4 extends java.lang.Object>
cons public init()
fld protected boolean frozen
fld protected java.lang.Object[] items
innr public static R2
innr public static R3
innr public static R4
innr public static R5
intf com.ibm.icu.util.Freezable<com.ibm.icu.impl.Row<{com.ibm.icu.impl.Row%0},{com.ibm.icu.impl.Row%1},{com.ibm.icu.impl.Row%2},{com.ibm.icu.impl.Row%3},{com.ibm.icu.impl.Row%4}>>
intf java.lang.Cloneable
intf java.lang.Comparable
meth protected com.ibm.icu.impl.Row<{com.ibm.icu.impl.Row%0},{com.ibm.icu.impl.Row%1},{com.ibm.icu.impl.Row%2},{com.ibm.icu.impl.Row%3},{com.ibm.icu.impl.Row%4}> set(int,java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isFrozen()
meth public com.ibm.icu.impl.Row<{com.ibm.icu.impl.Row%0},{com.ibm.icu.impl.Row%1},{com.ibm.icu.impl.Row%2},{com.ibm.icu.impl.Row%3},{com.ibm.icu.impl.Row%4}> cloneAsThawed()
meth public com.ibm.icu.impl.Row<{com.ibm.icu.impl.Row%0},{com.ibm.icu.impl.Row%1},{com.ibm.icu.impl.Row%2},{com.ibm.icu.impl.Row%3},{com.ibm.icu.impl.Row%4}> freeze()
meth public com.ibm.icu.impl.Row<{com.ibm.icu.impl.Row%0},{com.ibm.icu.impl.Row%1},{com.ibm.icu.impl.Row%2},{com.ibm.icu.impl.Row%3},{com.ibm.icu.impl.Row%4}> set0({com.ibm.icu.impl.Row%0})
meth public com.ibm.icu.impl.Row<{com.ibm.icu.impl.Row%0},{com.ibm.icu.impl.Row%1},{com.ibm.icu.impl.Row%2},{com.ibm.icu.impl.Row%3},{com.ibm.icu.impl.Row%4}> set1({com.ibm.icu.impl.Row%1})
meth public com.ibm.icu.impl.Row<{com.ibm.icu.impl.Row%0},{com.ibm.icu.impl.Row%1},{com.ibm.icu.impl.Row%2},{com.ibm.icu.impl.Row%3},{com.ibm.icu.impl.Row%4}> set2({com.ibm.icu.impl.Row%2})
meth public com.ibm.icu.impl.Row<{com.ibm.icu.impl.Row%0},{com.ibm.icu.impl.Row%1},{com.ibm.icu.impl.Row%2},{com.ibm.icu.impl.Row%3},{com.ibm.icu.impl.Row%4}> set3({com.ibm.icu.impl.Row%3})
meth public com.ibm.icu.impl.Row<{com.ibm.icu.impl.Row%0},{com.ibm.icu.impl.Row%1},{com.ibm.icu.impl.Row%2},{com.ibm.icu.impl.Row%3},{com.ibm.icu.impl.Row%4}> set4({com.ibm.icu.impl.Row%4})
meth public int compareTo(java.lang.Object)
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object, %4 extends java.lang.Object> com.ibm.icu.impl.Row$R5<{%%0},{%%1},{%%2},{%%3},{%%4}> of({%%0},{%%1},{%%2},{%%3},{%%4})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object> com.ibm.icu.impl.Row$R4<{%%0},{%%1},{%%2},{%%3}> of({%%0},{%%1},{%%2},{%%3})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object> com.ibm.icu.impl.Row$R3<{%%0},{%%1},{%%2}> of({%%0},{%%1},{%%2})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> com.ibm.icu.impl.Row$R2<{%%0},{%%1}> of({%%0},{%%1})
meth public {com.ibm.icu.impl.Row%0} get0()
meth public {com.ibm.icu.impl.Row%1} get1()
meth public {com.ibm.icu.impl.Row%2} get2()
meth public {com.ibm.icu.impl.Row%3} get3()
meth public {com.ibm.icu.impl.Row%4} get4()
supr java.lang.Object

CLSS public static com.ibm.icu.impl.Row$R2<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer com.ibm.icu.impl.Row
cons public init({com.ibm.icu.impl.Row$R2%0},{com.ibm.icu.impl.Row$R2%1})
supr com.ibm.icu.impl.Row<{com.ibm.icu.impl.Row$R2%0},{com.ibm.icu.impl.Row$R2%1},{com.ibm.icu.impl.Row$R2%1},{com.ibm.icu.impl.Row$R2%1},{com.ibm.icu.impl.Row$R2%1}>

CLSS public static com.ibm.icu.impl.Row$R3<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object>
 outer com.ibm.icu.impl.Row
cons public init({com.ibm.icu.impl.Row$R3%0},{com.ibm.icu.impl.Row$R3%1},{com.ibm.icu.impl.Row$R3%2})
supr com.ibm.icu.impl.Row<{com.ibm.icu.impl.Row$R3%0},{com.ibm.icu.impl.Row$R3%1},{com.ibm.icu.impl.Row$R3%2},{com.ibm.icu.impl.Row$R3%2},{com.ibm.icu.impl.Row$R3%2}>

CLSS public static com.ibm.icu.impl.Row$R4<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object>
 outer com.ibm.icu.impl.Row
cons public init({com.ibm.icu.impl.Row$R4%0},{com.ibm.icu.impl.Row$R4%1},{com.ibm.icu.impl.Row$R4%2},{com.ibm.icu.impl.Row$R4%3})
supr com.ibm.icu.impl.Row<{com.ibm.icu.impl.Row$R4%0},{com.ibm.icu.impl.Row$R4%1},{com.ibm.icu.impl.Row$R4%2},{com.ibm.icu.impl.Row$R4%3},{com.ibm.icu.impl.Row$R4%3}>

CLSS public static com.ibm.icu.impl.Row$R5<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object, %4 extends java.lang.Object>
 outer com.ibm.icu.impl.Row
cons public init({com.ibm.icu.impl.Row$R5%0},{com.ibm.icu.impl.Row$R5%1},{com.ibm.icu.impl.Row$R5%2},{com.ibm.icu.impl.Row$R5%3},{com.ibm.icu.impl.Row$R5%4})
supr com.ibm.icu.impl.Row<{com.ibm.icu.impl.Row$R5%0},{com.ibm.icu.impl.Row$R5%1},{com.ibm.icu.impl.Row$R5%2},{com.ibm.icu.impl.Row$R5%3},{com.ibm.icu.impl.Row$R5%4}>

CLSS public com.ibm.icu.impl.RuleCharacterIterator
cons public init(java.lang.String,com.ibm.icu.text.SymbolTable,java.text.ParsePosition)
fld public final static int DONE = -1
fld public final static int PARSE_ESCAPES = 2
fld public final static int PARSE_VARIABLES = 1
fld public final static int SKIP_WHITESPACE = 4
meth public boolean atEnd()
meth public boolean inVariable()
meth public boolean isEscaped()
meth public int next(int)
meth public java.lang.Object getPos(java.lang.Object)
meth public java.lang.String lookahead()
meth public java.lang.String toString()
meth public void jumpahead(int)
meth public void setPos(java.lang.Object)
meth public void skipIgnored(int)
supr java.lang.Object
hfds buf,bufPos,isEscaped,pos,sym,text

CLSS public com.ibm.icu.impl.SimpleCache<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(int,int)
intf com.ibm.icu.impl.ICUCache<{com.ibm.icu.impl.SimpleCache%0},{com.ibm.icu.impl.SimpleCache%1}>
meth public void clear()
meth public void put({com.ibm.icu.impl.SimpleCache%0},{com.ibm.icu.impl.SimpleCache%1})
meth public {com.ibm.icu.impl.SimpleCache%1} get(java.lang.Object)
supr java.lang.Object
hfds DEFAULT_CAPACITY,cacheRef,capacity,type

CLSS public abstract com.ibm.icu.impl.SoftCache<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object>
cons public init()
meth public final {com.ibm.icu.impl.SoftCache%1} getInstance({com.ibm.icu.impl.SoftCache%0},{com.ibm.icu.impl.SoftCache%2})
supr com.ibm.icu.impl.CacheBase<{com.ibm.icu.impl.SoftCache%0},{com.ibm.icu.impl.SoftCache%1},{com.ibm.icu.impl.SoftCache%2}>
hfds map
hcls SettableSoftReference

CLSS public com.ibm.icu.impl.SortedSetRelation
cons public init()
fld public final static int A = 6
fld public final static int ADDALL = 7
fld public final static int ANY = 7
fld public final static int A_AND_B = 2
fld public final static int A_NOT_B = 4
fld public final static int B = 3
fld public final static int B_NOT_A = 1
fld public final static int B_REMOVEALL = 1
fld public final static int COMPLEMENTALL = 5
fld public final static int CONTAINS = 6
fld public final static int DISJOINT = 5
fld public final static int EQUALS = 2
fld public final static int ISCONTAINED = 3
fld public final static int NONE = 0
fld public final static int NO_A = 1
fld public final static int NO_B = 4
fld public final static int REMOVEALL = 4
fld public final static int RETAINALL = 2
meth public static <%0 extends java.lang.Object & java.lang.Comparable<? super {%%0}>> boolean hasRelation(java.util.SortedSet<{%%0}>,int,java.util.SortedSet<{%%0}>)
meth public static <%0 extends java.lang.Object & java.lang.Comparable<? super {%%0}>> java.util.SortedSet<? extends {%%0}> doOperation(java.util.SortedSet<{%%0}>,int,java.util.SortedSet<{%%0}>)
supr java.lang.Object

CLSS public final com.ibm.icu.impl.StringPrepDataReader
cons public init(java.io.InputStream) throws java.io.IOException
intf com.ibm.icu.impl.ICUBinary$Authenticate
meth public boolean isDataVersionAcceptable(byte[])
meth public byte[] getDataFormatVersion()
meth public byte[] getUnicodeVersion()
meth public int[] readIndexes(int) throws java.io.IOException
meth public void read(byte[],char[]) throws java.io.IOException
supr java.lang.Object
hfds DATA_FORMAT_ID,DATA_FORMAT_VERSION,dataInputStream,debug,unicodeVersion

CLSS public final com.ibm.icu.impl.StringUCharacterIterator
cons public init()
cons public init(java.lang.String)
meth public int current()
meth public int getIndex()
meth public int getLength()
meth public int getText(char[],int)
meth public int next()
meth public int previous()
meth public java.lang.Object clone()
meth public java.lang.String getText()
meth public void setIndex(int)
meth public void setText(java.lang.String)
supr com.ibm.icu.text.UCharacterIterator
hfds m_currentIndex_,m_text_

CLSS public com.ibm.icu.impl.TextTrieMap<%0 extends java.lang.Object>
cons public init(boolean)
innr public abstract interface static ResultHandler
meth public java.util.Iterator<{com.ibm.icu.impl.TextTrieMap%0}> get(java.lang.String)
meth public java.util.Iterator<{com.ibm.icu.impl.TextTrieMap%0}> get(java.lang.String,int)
meth public void find(java.lang.String,com.ibm.icu.impl.TextTrieMap$ResultHandler<{com.ibm.icu.impl.TextTrieMap%0}>)
meth public void find(java.lang.String,int,com.ibm.icu.impl.TextTrieMap$ResultHandler<{com.ibm.icu.impl.TextTrieMap%0}>)
meth public void put(java.lang.String,{com.ibm.icu.impl.TextTrieMap%0})
supr java.lang.Object
hfds ignoreCase,root
hcls CharacterNode,LongestMatchHandler

CLSS public abstract interface static com.ibm.icu.impl.TextTrieMap$ResultHandler<%0 extends java.lang.Object>
 outer com.ibm.icu.impl.TextTrieMap
meth public abstract boolean handlePrefixMatch(int,java.util.Iterator<{com.ibm.icu.impl.TextTrieMap$ResultHandler%0}>)

CLSS public com.ibm.icu.impl.TimeZoneAdapter
cons public init(com.ibm.icu.util.TimeZone)
meth public boolean equals(java.lang.Object)
meth public boolean hasSameRules(java.util.TimeZone)
meth public boolean inDaylightTime(java.util.Date)
meth public boolean useDaylightTime()
meth public com.ibm.icu.util.TimeZone unwrap()
meth public int getOffset(int,int,int,int,int,int)
meth public int getRawOffset()
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.String toString()
meth public static java.util.TimeZone wrap(com.ibm.icu.util.TimeZone)
meth public void setID(java.lang.String)
meth public void setRawOffset(int)
supr java.util.TimeZone
hfds serialVersionUID,zone

CLSS public abstract com.ibm.icu.impl.Trie
cons protected init(char[],int,com.ibm.icu.impl.Trie$DataManipulate)
cons protected init(java.io.InputStream,com.ibm.icu.impl.Trie$DataManipulate) throws java.io.IOException
fld protected char[] m_index_
fld protected com.ibm.icu.impl.Trie$DataManipulate m_dataManipulate_
fld protected final static int BMP_INDEX_LENGTH = 2048
fld protected final static int DATA_BLOCK_LENGTH = 32
fld protected final static int HEADER_LENGTH_ = 16
fld protected final static int HEADER_OPTIONS_DATA_IS_32_BIT_ = 256
fld protected final static int HEADER_OPTIONS_INDEX_SHIFT_ = 4
fld protected final static int HEADER_OPTIONS_LATIN1_IS_LINEAR_MASK_ = 512
fld protected final static int HEADER_SIGNATURE_ = 1416784229
fld protected final static int INDEX_STAGE_1_SHIFT_ = 5
fld protected final static int INDEX_STAGE_2_SHIFT_ = 2
fld protected final static int INDEX_STAGE_3_MASK_ = 31
fld protected final static int LEAD_INDEX_OFFSET_ = 320
fld protected final static int SURROGATE_BLOCK_BITS = 5
fld protected final static int SURROGATE_BLOCK_COUNT = 32
fld protected final static int SURROGATE_MASK_ = 1023
fld protected int m_dataLength_
fld protected int m_dataOffset_
innr public abstract interface static DataManipulate
meth protected abstract int getInitialValue()
meth protected abstract int getSurrogateOffset(char,char)
meth protected abstract int getValue(int)
meth protected final boolean isCharTrie()
meth protected final boolean isIntTrie()
meth protected final int getBMPOffset(char)
meth protected final int getCodePointOffset(int)
meth protected final int getLeadOffset(char)
meth protected final int getRawOffset(int,char)
meth protected void unserialize(java.io.InputStream) throws java.io.IOException
meth public boolean equals(java.lang.Object)
meth public final boolean isLatin1Linear()
meth public int getSerializedDataSize()
supr java.lang.Object
hfds HEADER_OPTIONS_SHIFT_MASK_,m_isLatin1Linear_,m_options_
hcls DefaultGetFoldingOffset

CLSS public abstract interface static com.ibm.icu.impl.Trie$DataManipulate
 outer com.ibm.icu.impl.Trie
meth public abstract int getFoldingOffset(int)

CLSS public abstract com.ibm.icu.impl.Trie2
cons public init()
innr public CharSequenceIterator
innr public abstract interface static ValueMapper
innr public static CharSequenceValues
innr public static Range
intf java.lang.Iterable<com.ibm.icu.impl.Trie2$Range>
meth protected int serializeHeader(java.io.DataOutputStream) throws java.io.IOException
meth public abstract int get(int)
meth public abstract int getFromU16SingleLead(char)
meth public com.ibm.icu.impl.Trie2$CharSequenceIterator charSequenceIterator(java.lang.CharSequence,int)
meth public final boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.util.Iterator<com.ibm.icu.impl.Trie2$Range> iterator()
meth public java.util.Iterator<com.ibm.icu.impl.Trie2$Range> iterator(com.ibm.icu.impl.Trie2$ValueMapper)
meth public java.util.Iterator<com.ibm.icu.impl.Trie2$Range> iteratorForLeadSurrogate(char)
meth public java.util.Iterator<com.ibm.icu.impl.Trie2$Range> iteratorForLeadSurrogate(char,com.ibm.icu.impl.Trie2$ValueMapper)
meth public static com.ibm.icu.impl.Trie2 createFromSerialized(java.io.InputStream) throws java.io.IOException
meth public static int getVersion(java.io.InputStream,boolean) throws java.io.IOException
supr java.lang.Object
hfds UNEWTRIE2_INDEX_1_LENGTH,UNEWTRIE2_INDEX_GAP_LENGTH,UNEWTRIE2_INDEX_GAP_OFFSET,UNEWTRIE2_MAX_DATA_LENGTH,UNEWTRIE2_MAX_INDEX_2_LENGTH,UTRIE2_BAD_UTF8_DATA_OFFSET,UTRIE2_CP_PER_INDEX_1_ENTRY,UTRIE2_DATA_BLOCK_LENGTH,UTRIE2_DATA_GRANULARITY,UTRIE2_DATA_MASK,UTRIE2_DATA_START_OFFSET,UTRIE2_INDEX_1_OFFSET,UTRIE2_INDEX_2_BLOCK_LENGTH,UTRIE2_INDEX_2_BMP_LENGTH,UTRIE2_INDEX_2_MASK,UTRIE2_INDEX_2_OFFSET,UTRIE2_INDEX_SHIFT,UTRIE2_LSCP_INDEX_2_LENGTH,UTRIE2_LSCP_INDEX_2_OFFSET,UTRIE2_MAX_INDEX_1_LENGTH,UTRIE2_OMITTED_BMP_INDEX_1_LENGTH,UTRIE2_OPTIONS_VALUE_BITS_MASK,UTRIE2_SHIFT_1,UTRIE2_SHIFT_1_2,UTRIE2_SHIFT_2,UTRIE2_UTF8_2B_INDEX_2_LENGTH,UTRIE2_UTF8_2B_INDEX_2_OFFSET,data16,data32,dataLength,dataNullOffset,defaultValueMapper,errorValue,fHash,header,highStart,highValueIndex,index,index2NullOffset,indexLength,initialValue
hcls Trie2Iterator,UTrie2Header,ValueWidth

CLSS public com.ibm.icu.impl.Trie2$CharSequenceIterator
 outer com.ibm.icu.impl.Trie2
intf java.util.Iterator<com.ibm.icu.impl.Trie2$CharSequenceValues>
meth public com.ibm.icu.impl.Trie2$CharSequenceValues next()
meth public com.ibm.icu.impl.Trie2$CharSequenceValues previous()
meth public final boolean hasNext()
meth public final boolean hasPrevious()
meth public void remove()
meth public void set(int)
supr java.lang.Object
hfds fResults,index,text,textLength

CLSS public static com.ibm.icu.impl.Trie2$CharSequenceValues
 outer com.ibm.icu.impl.Trie2
cons public init()
fld public int codePoint
fld public int index
fld public int value
supr java.lang.Object

CLSS public static com.ibm.icu.impl.Trie2$Range
 outer com.ibm.icu.impl.Trie2
cons public init()
fld public boolean leadSurrogate
fld public int endCodePoint
fld public int startCodePoint
fld public int value
meth public boolean equals(java.lang.Object)
meth public int hashCode()
supr java.lang.Object

CLSS public abstract interface static com.ibm.icu.impl.Trie2$ValueMapper
 outer com.ibm.icu.impl.Trie2
meth public abstract int map(int)

CLSS public com.ibm.icu.impl.Trie2Writable
cons public init(com.ibm.icu.impl.Trie2)
cons public init(int,int)
meth public com.ibm.icu.impl.Trie2Writable set(int,int)
meth public com.ibm.icu.impl.Trie2Writable setForLeadSurrogateCodeUnit(char,int)
meth public com.ibm.icu.impl.Trie2Writable setRange(com.ibm.icu.impl.Trie2$Range,boolean)
meth public com.ibm.icu.impl.Trie2Writable setRange(int,int,int,boolean)
meth public com.ibm.icu.impl.Trie2_16 toTrie2_16()
meth public com.ibm.icu.impl.Trie2_32 toTrie2_32()
meth public int get(int)
meth public int getFromU16SingleLead(char)
supr com.ibm.icu.impl.Trie2
hfds UNEWTRIE2_DATA_0800_OFFSET,UNEWTRIE2_DATA_NULL_OFFSET,UNEWTRIE2_DATA_START_OFFSET,UNEWTRIE2_INDEX_2_NULL_OFFSET,UNEWTRIE2_INDEX_2_START_OFFSET,UNEWTRIE2_INITIAL_DATA_LENGTH,UNEWTRIE2_MEDIUM_DATA_LENGTH,UTRIE2_DEBUG,UTRIE2_MAX_DATA_LENGTH,UTRIE2_MAX_INDEX_LENGTH,data,dataCapacity,firstFreeBlock,index1,index2,index2Length,index2NullOffset,isCompacted,map

CLSS public final com.ibm.icu.impl.Trie2_16
meth public final int get(int)
meth public int getFromU16SingleLead(char)
meth public int getSerializedLength()
meth public int serialize(java.io.OutputStream) throws java.io.IOException
meth public static com.ibm.icu.impl.Trie2_16 createFromSerialized(java.io.InputStream) throws java.io.IOException
supr com.ibm.icu.impl.Trie2

CLSS public com.ibm.icu.impl.Trie2_32
meth public final int get(int)
meth public int getFromU16SingleLead(char)
meth public int getSerializedLength()
meth public int serialize(java.io.OutputStream) throws java.io.IOException
meth public static com.ibm.icu.impl.Trie2_32 createFromSerialized(java.io.InputStream) throws java.io.IOException
supr com.ibm.icu.impl.Trie2

CLSS public com.ibm.icu.impl.TrieBuilder
cons protected init()
cons protected init(com.ibm.icu.impl.TrieBuilder)
fld protected boolean m_isCompacted_
fld protected boolean m_isLatin1Linear_
fld protected final static int BMP_INDEX_LENGTH_ = 2048
fld protected final static int DATA_GRANULARITY_ = 4
fld protected final static int INDEX_SHIFT_ = 2
fld protected final static int MASK_ = 31
fld protected final static int MAX_DATA_LENGTH_ = 262144
fld protected final static int MAX_INDEX_LENGTH_ = 34816
fld protected final static int OPTIONS_DATA_IS_32_BIT_ = 256
fld protected final static int OPTIONS_INDEX_SHIFT_ = 4
fld protected final static int OPTIONS_LATIN1_IS_LINEAR_ = 512
fld protected final static int SHIFT_ = 5
fld protected final static int SURROGATE_BLOCK_COUNT_ = 32
fld protected int m_dataCapacity_
fld protected int m_dataLength_
fld protected int m_indexLength_
fld protected int[] m_index_
fld protected int[] m_map_
fld public final static int DATA_BLOCK_LENGTH = 32
innr public abstract interface static DataManipulate
meth protected final static boolean equal_int(int[],int,int,int)
meth protected final static int findSameIndexBlock(int[],int,int)
meth protected void findUnusedBlocks()
meth public boolean isInZeroBlock(int)
supr java.lang.Object
hfds MAX_BUILD_TIME_DATA_LENGTH_

CLSS public abstract interface static com.ibm.icu.impl.TrieBuilder$DataManipulate
 outer com.ibm.icu.impl.TrieBuilder
meth public abstract int getFoldedValue(int,int)

CLSS public com.ibm.icu.impl.TrieIterator
cons public init(com.ibm.icu.impl.Trie)
intf com.ibm.icu.util.RangeValueIterator
meth protected int extract(int)
meth public final boolean next(com.ibm.icu.util.RangeValueIterator$Element)
meth public final void reset()
supr java.lang.Object
hfds BMP_INDEX_LENGTH_,DATA_BLOCK_LENGTH_,LEAD_SURROGATE_MIN_VALUE_,TRAIL_SURROGATE_COUNT_,TRAIL_SURROGATE_INDEX_BLOCK_LENGTH_,TRAIL_SURROGATE_MIN_VALUE_,m_currentCodepoint_,m_initialValue_,m_nextBlockIndex_,m_nextBlock_,m_nextCodepoint_,m_nextIndex_,m_nextTrailIndexOffset_,m_nextValue_,m_trie_

CLSS public final com.ibm.icu.impl.UBiDiProps
fld public final static com.ibm.icu.impl.UBiDiProps INSTANCE
meth public final boolean isBidiControl(int)
meth public final boolean isJoinControl(int)
meth public final boolean isMirrored(int)
meth public final int getClass(int)
meth public final int getJoiningGroup(int)
meth public final int getJoiningType(int)
meth public final int getMaxValue(int)
meth public final int getMirror(int)
meth public final void addPropertyStarts(com.ibm.icu.text.UnicodeSet)
meth public static com.ibm.icu.impl.UBiDiProps getDummy()
meth public static com.ibm.icu.impl.UBiDiProps getSingleton() throws java.io.IOException
supr java.lang.Object
hfds BIDI_CONTROL_SHIFT,CLASS_MASK,DATA_FILE_NAME,DATA_NAME,DATA_TYPE,DUMMY_INSTANCE,ESC_MIRROR_DELTA,FMT,FULL_INSTANCE,IS_MIRRORED_SHIFT,IX_INDEX_TOP,IX_JG_LIMIT,IX_JG_START,IX_MAX_VALUES,IX_MIRROR_LENGTH,IX_TOP,JOIN_CONTROL_SHIFT,JT_MASK,JT_SHIFT,MAX_JG_MASK,MAX_JG_SHIFT,MIRROR_DELTA_SHIFT,MIRROR_INDEX_SHIFT,indexes,jgArray,mirrors,trie
hcls IsAcceptable

CLSS public final com.ibm.icu.impl.UCaseProps
fld public final static com.ibm.icu.impl.UCaseProps INSTANCE
fld public final static int LOWER = 1
fld public final static int MAX_STRING_LENGTH = 31
fld public final static int NONE = 0
fld public final static int TITLE = 3
fld public final static int TYPE_MASK = 3
fld public final static int UPPER = 2
fld public final static java.lang.StringBuffer dummyStringBuffer
innr public abstract interface static ContextIterator
meth public final boolean addStringCaseClosure(java.lang.String,com.ibm.icu.text.UnicodeSet)
meth public final boolean hasBinaryProperty(int,int)
meth public final boolean isCaseSensitive(int)
meth public final boolean isSoftDotted(int)
meth public final int fold(int,int)
meth public final int getDotType(int)
meth public final int getType(int)
meth public final int getTypeOrIgnorable(int)
meth public final int toFullFolding(int,java.lang.StringBuffer,int)
meth public final int toFullLower(int,com.ibm.icu.impl.UCaseProps$ContextIterator,java.lang.StringBuffer,com.ibm.icu.util.ULocale,int[])
meth public final int toFullTitle(int,com.ibm.icu.impl.UCaseProps$ContextIterator,java.lang.StringBuffer,com.ibm.icu.util.ULocale,int[])
meth public final int toFullUpper(int,com.ibm.icu.impl.UCaseProps$ContextIterator,java.lang.StringBuffer,com.ibm.icu.util.ULocale,int[])
meth public final int tolower(int)
meth public final int totitle(int)
meth public final int toupper(int)
meth public final void addCaseClosure(int,com.ibm.icu.text.UnicodeSet)
meth public final void addPropertyStarts(com.ibm.icu.text.UnicodeSet)
meth public static com.ibm.icu.impl.UCaseProps getDummy()
meth public static com.ibm.icu.impl.UCaseProps getSingleton() throws java.io.IOException
supr java.lang.Object
hfds ABOVE,CASE_IGNORABLE,CLOSURE_MAX_LENGTH,DATA_FILE_NAME,DATA_NAME,DATA_TYPE,DELTA_SHIFT,DOT_MASK,DUMMY_INSTANCE,EXCEPTION,EXC_CASE_IGNORABLE,EXC_CLOSURE,EXC_CONDITIONAL_FOLD,EXC_CONDITIONAL_SPECIAL,EXC_DOT_SHIFT,EXC_DOUBLE_SLOTS,EXC_FOLD,EXC_FULL_MAPPINGS,EXC_LOWER,EXC_SHIFT,EXC_TITLE,EXC_UPPER,FMT,FOLD_CASE_OPTIONS_MASK,FULL_INSTANCE,FULL_LOWER,IX_EXC_LENGTH,IX_INDEX_TOP,IX_TOP,IX_UNFOLD_LENGTH,LOC_LITHUANIAN,LOC_ROOT,LOC_TURKISH,LOC_UNKNOWN,OTHER_ACCENT,SENSITIVE,SOFT_DOTTED,UNFOLD_ROWS,UNFOLD_ROW_WIDTH,UNFOLD_STRING_WIDTH,exceptions,flagsOffset,iDot,iDotAcute,iDotGrave,iDotTilde,iOgonekDot,indexes,jDot,rootLocCache,trie,unfold
hcls IsAcceptable

CLSS public abstract interface static com.ibm.icu.impl.UCaseProps$ContextIterator
 outer com.ibm.icu.impl.UCaseProps
meth public abstract int next()
meth public abstract void reset(int)

CLSS public final com.ibm.icu.impl.UCharArrayIterator
cons public init(char[],int,int)
meth public int current()
meth public int getIndex()
meth public int getLength()
meth public int getText(char[],int)
meth public int next()
meth public int previous()
meth public java.lang.Object clone()
meth public void setIndex(int)
supr com.ibm.icu.text.UCharacterIterator
hfds limit,pos,start,text

CLSS public com.ibm.icu.impl.UCharacterIteratorWrapper
cons public init(com.ibm.icu.text.UCharacterIterator)
intf java.text.CharacterIterator
meth public char current()
meth public char first()
meth public char last()
meth public char next()
meth public char previous()
meth public char setIndex(int)
meth public int getBeginIndex()
meth public int getEndIndex()
meth public int getIndex()
meth public java.lang.Object clone()
supr java.lang.Object
hfds iterator

CLSS public final com.ibm.icu.impl.UCharacterName
fld public final static com.ibm.icu.impl.UCharacterName INSTANCE
fld public final static int LINES_PER_GROUP_ = 32
fld public int m_groupcount_
meth public int getAlgorithmEnd(int)
meth public int getAlgorithmLength()
meth public int getAlgorithmStart(int)
meth public int getCharFromName(int,java.lang.String)
meth public int getGroup(int)
meth public int getGroupLengths(int,char[],char[])
meth public int getGroupMSB(int)
meth public int getMaxCharNameLength()
meth public int getMaxISOCommentLength()
meth public java.lang.String getAlgorithmName(int,int)
meth public java.lang.String getExtendedName(int)
meth public java.lang.String getExtendedOr10Name(int)
meth public java.lang.String getGroupName(int,int)
meth public java.lang.String getGroupName(int,int,int)
meth public java.lang.String getName(int,int)
meth public static int getCodepointMSB(int)
meth public static int getGroupLimit(int)
meth public static int getGroupMin(int)
meth public static int getGroupMinFromCodepoint(int)
meth public static int getGroupOffset(int)
meth public void getCharNameCharacters(com.ibm.icu.text.UnicodeSet)
meth public void getISOCommentCharacters(com.ibm.icu.text.UnicodeSet)
supr java.lang.Object
hfds EXTENDED_CATEGORY_,GROUP_MASK_,GROUP_SHIFT_,LEAD_SURROGATE_,NAME_BUFFER_SIZE_,NAME_FILE_NAME_,NON_CHARACTER_,OFFSET_HIGH_OFFSET_,OFFSET_LOW_OFFSET_,SINGLE_NIBBLE_MAX_,TRAIL_SURROGATE_,TYPE_NAMES_,UNKNOWN_TYPE_NAME_,m_ISOCommentSet_,m_algorithm_,m_groupinfo_,m_grouplengths_,m_groupoffsets_,m_groupsize_,m_groupstring_,m_maxISOCommentLength_,m_maxNameLength_,m_nameSet_,m_tokenstring_,m_tokentable_,m_utilIntBuffer_,m_utilStringBuffer_
hcls AlgorithmName

CLSS public abstract interface com.ibm.icu.impl.UCharacterNameChoice
fld public final static int CHAR_NAME_ALIAS = 3
fld public final static int CHAR_NAME_CHOICE_COUNT = 4
fld public final static int EXTENDED_CHAR_NAME = 2
fld public final static int ISO_COMMENT_ = 4
fld public final static int UNICODE_10_CHAR_NAME = 1
fld public final static int UNICODE_CHAR_NAME = 0

CLSS public final com.ibm.icu.impl.UCharacterProperty
fld public char[] m_trieData_
fld public char[] m_trieIndex_
fld public com.ibm.icu.impl.CharTrie m_trie_
fld public com.ibm.icu.util.VersionInfo m_unicodeVersion_
fld public final static char LATIN_CAPITAL_LETTER_I_WITH_DOT_ABOVE_ = '\u0130'
fld public final static char LATIN_SMALL_LETTER_DOTLESS_I_ = '\u0131'
fld public final static char LATIN_SMALL_LETTER_I_ = 'i'
fld public final static com.ibm.icu.impl.UCharacterProperty INSTANCE
fld public final static int SRC_BIDI = 5
fld public final static int SRC_CASE = 4
fld public final static int SRC_CASE_AND_NORM = 7
fld public final static int SRC_CHAR = 1
fld public final static int SRC_CHAR_AND_PROPSVEC = 6
fld public final static int SRC_COUNT = 12
fld public final static int SRC_NAMES = 3
fld public final static int SRC_NFC = 8
fld public final static int SRC_NFC_CANON_ITER = 11
fld public final static int SRC_NFKC = 9
fld public final static int SRC_NFKC_CF = 10
fld public final static int SRC_NONE = 0
fld public final static int SRC_PROPSVEC = 2
fld public final static int TYPE_MASK = 31
fld public int m_trieInitialValue_
meth public boolean hasBinaryProperty(int,int)
meth public com.ibm.icu.text.UnicodeSet addPropertyStarts(com.ibm.icu.text.UnicodeSet)
meth public com.ibm.icu.util.VersionInfo getAge(int)
meth public final int getProperty(int)
meth public final int getSource(int)
meth public final static int getMask(int)
meth public int getAdditional(int,int)
meth public int getMaxValues(int)
meth public static boolean isRuleWhiteSpace(int)
meth public static int getRawSupplementary(char,char)
meth public void setIndexData(com.ibm.icu.impl.CharTrie$FriendAgent)
meth public void upropsvec_addPropertyStarts(com.ibm.icu.text.UnicodeSet)
supr java.lang.Object
hfds AGE_SHIFT_,ALPHABETIC_PROPERTY_,ASCII_HEX_DIGIT_PROPERTY_,CGJ,CR,DASH_PROPERTY_,DATA_BUFFER_SIZE_,DATA_FILE_NAME_,DEFAULT_IGNORABLE_CODE_POINT_PROPERTY_,DEL,DEPRECATED_PROPERTY_,DIACRITIC_PROPERTY_,EXTENDER_PROPERTY_,FIGURESP,FIRST_NIBBLE_SHIFT_,GC_CC_MASK,GC_CN_MASK,GC_CS_MASK,GC_ZL_MASK,GC_ZP_MASK,GC_ZS_MASK,GC_Z_MASK,GRAPHEME_BASE_PROPERTY_,GRAPHEME_EXTEND_PROPERTY_,GRAPHEME_LINK_PROPERTY_,HAIRSP,HEX_DIGIT_PROPERTY_,HYPHEN_PROPERTY_,IDEOGRAPHIC_PROPERTY_,IDS_BINARY_OPERATOR_PROPERTY_,IDS_TRINARY_OPERATOR_PROPERTY_,ID_CONTINUE_PROPERTY_,ID_START_PROPERTY_,INHSWAP,LAST_NIBBLE_MASK_,LEAD_SURROGATE_SHIFT_,LOGICAL_ORDER_EXCEPTION_PROPERTY_,MATH_PROPERTY_,MY_MASK,NBSP,NL,NNBSP,NOMDIG,NONCHARACTER_CODE_POINT_PROPERTY_,PATTERN_SYNTAX,PATTERN_WHITE_SPACE,QUOTATION_MARK_PROPERTY_,RADICAL_PROPERTY_,RLM,SURROGATE_OFFSET_,S_TERM_PROPERTY_,TAB,TERMINAL_PUNCTUATION_PROPERTY_,UNIFIED_IDEOGRAPH_PROPERTY_,U_A,U_F,U_FW_A,U_FW_F,U_FW_Z,U_FW_a,U_FW_f,U_FW_z,U_Z,U_a,U_f,U_z,VARIATION_SELECTOR_PROPERTY_,WHITE_SPACE_PROPERTY_,WJ,XID_CONTINUE_PROPERTY_,XID_START_PROPERTY_,ZWNBSP,binProps,m_additionalColumnsCount_,m_additionalTrie_,m_additionalVectors_,m_maxBlockScriptValue_,m_maxJTGValue_
hcls BinaryProperties

CLSS public final com.ibm.icu.impl.UCharacterUtility
meth public static boolean isNonCharacter(int)
supr java.lang.Object
hfds NON_CHARACTER_MAX_3_1_,NON_CHARACTER_MIN_3_1_,NON_CHARACTER_SUFFIX_MIN_3_0_

CLSS public final com.ibm.icu.impl.UPropertyAliases
fld public final static com.ibm.icu.impl.UPropertyAliases INSTANCE
intf com.ibm.icu.impl.ICUBinary$Authenticate
meth public boolean isDataVersionAcceptable(byte[])
meth public int getPropertyEnum(java.lang.String)
meth public int getPropertyValueEnum(int,java.lang.String)
meth public java.lang.String getPropertyName(int,int)
meth public java.lang.String getPropertyValueName(int,int,int)
meth public static int compare(java.lang.String,java.lang.String)
supr java.lang.Object
hfds DATA_BUFFER_SIZE,DATA_FILE_NAME,DATA_FORMAT_ID,DATA_FORMAT_VERSION,DEBUG,enumToName,enumToValue,nameGroupPool,nameToEnum,stringPool,valueMapArray
hcls Builder,ContiguousEnumToShort,EnumToShort,NameToEnum,NonContiguousEnumToShort,ValueMap

CLSS public abstract com.ibm.icu.impl.URLHandler
cons public init()
fld public final static java.lang.String PROPNAME = "urlhandler.props"
innr public abstract interface static URLVisitor
meth protected static com.ibm.icu.impl.URLHandler getDefault(java.net.URL)
meth public abstract void guide(com.ibm.icu.impl.URLHandler$URLVisitor,boolean,boolean)
meth public static com.ibm.icu.impl.URLHandler get(java.net.URL)
meth public void guide(com.ibm.icu.impl.URLHandler$URLVisitor,boolean)
supr java.lang.Object
hfds DEBUG,handlers
hcls FileURLHandler,JarURLHandler

CLSS public abstract interface static com.ibm.icu.impl.URLHandler$URLVisitor
 outer com.ibm.icu.impl.URLHandler
meth public abstract void visit(java.lang.String)

CLSS public final com.ibm.icu.impl.USerializedSet
cons public init()
meth public final boolean contains(int)
meth public final boolean getRange(int,int[])
meth public final boolean getSet(char[],int)
meth public final int countRanges()
meth public final void setToOne(int)
supr java.lang.Object
hfds array,arrayOffset,bmpLength,length

CLSS public com.ibm.icu.impl.UnicodeRegex
cons public init()
intf com.ibm.icu.text.StringTransform
intf com.ibm.icu.util.Freezable<com.ibm.icu.impl.UnicodeRegex>
intf java.lang.Cloneable
meth public boolean isFrozen()
meth public com.ibm.icu.impl.UnicodeRegex cloneAsThawed()
meth public com.ibm.icu.impl.UnicodeRegex freeze()
meth public java.lang.String compileBnf(java.lang.String)
meth public java.lang.String compileBnf(java.util.List<java.lang.String>)
meth public java.lang.String getBnfCommentString()
meth public java.lang.String getBnfLineSeparator()
meth public java.lang.String getBnfVariableInfix()
meth public java.lang.String transform(java.lang.String)
meth public static java.lang.String fix(java.lang.String)
meth public static java.util.List<java.lang.String> appendLines(java.util.List<java.lang.String>,java.io.InputStream,java.lang.String) throws java.io.IOException
meth public static java.util.List<java.lang.String> appendLines(java.util.List<java.lang.String>,java.lang.String,java.lang.String) throws java.io.IOException
meth public static java.util.regex.Pattern compile(java.lang.String)
meth public static java.util.regex.Pattern compile(java.lang.String,int)
meth public void setBnfCommentString(java.lang.String)
meth public void setBnfLineSeparator(java.lang.String)
meth public void setBnfVariableInfix(java.lang.String)
supr java.lang.Object
hfds LongestFirst,STANDARD,bnfCommentString,bnfLineSeparator,bnfVariableInfix,log

CLSS public com.ibm.icu.impl.UnicodeSetStringSpan
cons public init(com.ibm.icu.impl.UnicodeSetStringSpan,java.util.ArrayList<java.lang.String>)
cons public init(com.ibm.icu.text.UnicodeSet,java.util.ArrayList<java.lang.String>,int)
fld public final static int ALL = 63
fld public final static int BACK = 16
fld public final static int BACK_UTF16_CONTAINED = 26
fld public final static int BACK_UTF16_NOT_CONTAINED = 25
fld public final static int CONTAINED = 2
fld public final static int FWD = 32
fld public final static int FWD_UTF16_CONTAINED = 42
fld public final static int FWD_UTF16_NOT_CONTAINED = 41
fld public final static int NOT_CONTAINED = 1
fld public final static int UTF16 = 8
meth public boolean contains(int)
meth public boolean needsStringSpanUTF16()
meth public int span(java.lang.CharSequence,int,int,com.ibm.icu.text.UnicodeSet$SpanCondition)
meth public int spanBack(java.lang.CharSequence,int,com.ibm.icu.text.UnicodeSet$SpanCondition)
supr java.lang.Object
hfds ALL_CP_CONTAINED,LONG_SPAN,all,maxLength16,offsets,spanLengths,spanNotSet,spanSet,strings
hcls OffsetList

CLSS public final com.ibm.icu.impl.Utility
cons public init()
fld public static java.lang.String LINE_SEPARATOR
meth public final static boolean arrayEquals(byte[],java.lang.Object)
meth public final static boolean arrayEquals(double[],java.lang.Object)
meth public final static boolean arrayEquals(int[],java.lang.Object)
meth public final static boolean arrayEquals(java.lang.Object,java.lang.Object)
meth public final static boolean arrayEquals(java.lang.Object[],java.lang.Object)
meth public final static boolean arrayRegionMatches(byte[],int,byte[],int,int)
meth public final static boolean arrayRegionMatches(char[],int,char[],int,int)
meth public final static boolean arrayRegionMatches(double[],int,double[],int,int)
meth public final static boolean arrayRegionMatches(int[],int,int[],int,int)
meth public final static boolean arrayRegionMatches(java.lang.Object[],int,java.lang.Object[],int,int)
meth public final static boolean objectEquals(java.lang.Object,java.lang.Object)
meth public final static byte highBit(int)
meth public final static byte[] RLEStringToByteArray(java.lang.String)
meth public final static char[] RLEStringToCharArray(java.lang.String)
meth public final static int compareUnsigned(int,int)
meth public final static int[] RLEStringToIntArray(java.lang.String)
meth public final static java.lang.String arrayToRLEString(byte[])
meth public final static java.lang.String arrayToRLEString(char[])
meth public final static java.lang.String arrayToRLEString(int[])
meth public final static java.lang.String arrayToRLEString(short[])
meth public final static java.lang.String escape(java.lang.String)
meth public final static java.lang.String format1ForSource(java.lang.String)
meth public final static java.lang.String formatForSource(java.lang.String)
meth public final static short[] RLEStringToShortArray(java.lang.String)
meth public static <%0 extends java.lang.Appendable> boolean escapeUnprintable({%%0},int)
meth public static <%0 extends java.lang.Appendable> {%%0} appendNumber({%%0},int,int,int)
meth public static <%0 extends java.lang.CharSequence, %1 extends java.lang.CharSequence, %2 extends java.lang.Appendable> {%%2} hex({%%0},int,{%%1},boolean,{%%2})
meth public static <%0 extends java.lang.CharSequence> java.lang.String hex({%%0},int,{%%0})
meth public static <%0 extends java.lang.Comparable<{%%0}>> int checkCompare({%%0},{%%0})
meth public static boolean isUnprintable(int)
meth public static boolean parseChar(java.lang.String,int[],char)
meth public static int checkHash(java.lang.Object)
meth public static int lookup(java.lang.String,java.lang.String[])
meth public static int parseInteger(java.lang.String,int[],int)
meth public static int parseNumber(java.lang.String,int[],int)
meth public static int parsePattern(java.lang.String,com.ibm.icu.text.Replaceable,int,int)
meth public static int parsePattern(java.lang.String,int,int,java.lang.String,int[])
meth public static int quotedIndexOf(java.lang.String,int,int,java.lang.String)
meth public static int skipWhitespace(java.lang.String,int)
meth public static int unescapeAt(java.lang.String,int[])
meth public static java.lang.ClassLoader getFallbackClassLoader()
meth public static java.lang.String deleteRuleWhiteSpace(java.lang.String)
meth public static java.lang.String fromHex(java.lang.String,int,java.lang.String)
meth public static java.lang.String fromHex(java.lang.String,int,java.util.regex.Pattern)
meth public static java.lang.String hex(java.lang.CharSequence)
meth public static java.lang.String hex(long)
meth public static java.lang.String hex(long,int)
meth public static java.lang.String parseUnicodeIdentifier(java.lang.String,int[])
meth public static java.lang.String repeat(java.lang.String,int)
meth public static java.lang.String unescape(java.lang.String)
meth public static java.lang.String unescapeLeniently(java.lang.String)
meth public static java.lang.String valueOf(int[])
meth public static java.lang.String[] split(java.lang.String,char)
meth public static java.lang.String[] splitString(java.lang.String,java.lang.String)
meth public static java.lang.String[] splitWhitespace(java.lang.String)
meth public static void appendToRule(java.lang.StringBuffer,com.ibm.icu.text.UnicodeMatcher,boolean,java.lang.StringBuffer)
meth public static void appendToRule(java.lang.StringBuffer,int,boolean,boolean,java.lang.StringBuffer)
meth public static void appendToRule(java.lang.StringBuffer,java.lang.String,boolean,boolean,java.lang.StringBuffer)
meth public static void skipWhitespace(java.lang.String,int[])
meth public static void split(java.lang.String,char,java.lang.String[])
supr java.lang.Object
hfds APOSTROPHE,BACKSLASH,DIGITS,ESCAPE,ESCAPE_BYTE,HEX_DIGIT,MAGIC_UNSIGNED,UNESCAPE_MAP

CLSS public com.ibm.icu.impl.UtilityExtensions
cons public init()
meth public static java.lang.String formatInput(com.ibm.icu.text.Replaceable,com.ibm.icu.text.Transliterator$Position)
meth public static java.lang.String formatInput(com.ibm.icu.text.ReplaceableString,com.ibm.icu.text.Transliterator$Position)
meth public static java.lang.StringBuffer formatInput(java.lang.StringBuffer,com.ibm.icu.text.Replaceable,com.ibm.icu.text.Transliterator$Position)
meth public static java.lang.StringBuffer formatInput(java.lang.StringBuffer,com.ibm.icu.text.ReplaceableString,com.ibm.icu.text.Transliterator$Position)
meth public static void appendToRule(java.lang.StringBuffer,com.ibm.icu.text.UnicodeMatcher,boolean,java.lang.StringBuffer)
meth public static void appendToRule(java.lang.StringBuffer,java.lang.String,boolean,boolean,java.lang.StringBuffer)
supr java.lang.Object

CLSS public final com.ibm.icu.impl.ZoneMeta
cons public init()
fld public final static java.lang.String FALLBACK_FORMAT = "fallbackFormat"
fld public final static java.lang.String FORWARD_SLASH = "/"
fld public final static java.lang.String GMT = "gmtFormat"
fld public final static java.lang.String HOUR = "hourFormat"
fld public final static java.lang.String REGION_FORMAT = "regionFormat"
fld public final static java.lang.String ZONE_STRINGS = "zoneStrings"
meth public static com.ibm.icu.util.TimeZone getCustomTimeZone(int)
meth public static com.ibm.icu.util.TimeZone getCustomTimeZone(java.lang.String)
meth public static com.ibm.icu.util.TimeZone getGMT()
meth public static com.ibm.icu.util.TimeZone getSystemTimeZone(java.lang.String)
meth public static com.ibm.icu.util.UResourceBundle openOlsonResource(com.ibm.icu.util.UResourceBundle,java.lang.String)
meth public static int countEquivalentIDs(java.lang.String)
meth public static java.lang.String getCanonicalCountry(java.lang.String)
meth public static java.lang.String getCanonicalSystemID(java.lang.String)
meth public static java.lang.String getCustomID(java.lang.String)
meth public static java.lang.String getEquivalentID(java.lang.String,int)
meth public static java.lang.String getLocationFormat(java.lang.String,java.lang.String,com.ibm.icu.util.ULocale)
meth public static java.lang.String getMetazoneID(java.lang.String,long)
meth public static java.lang.String getSingleCountry(java.lang.String)
meth public static java.lang.String getTZLocalizationInfo(com.ibm.icu.util.ULocale,java.lang.String)
meth public static java.lang.String getZoneIdByMetazone(java.lang.String,java.lang.String)
meth public static java.lang.String[] getAvailableIDs()
meth public static java.lang.String[] getAvailableIDs(int)
meth public static java.lang.String[] getAvailableIDs(java.lang.String)
supr java.lang.Object
hfds ASSERT,CANONICAL_ID_CACHE,DEF_FALLBACK_FORMAT,DEF_REGION_FORMAT,META_TO_OLSON_CACHE,OLSON_TO_META_CACHE,REGION_CACHE,SINGLE_COUNTRY_CACHE,SYSTEM_ZONE_CACHE,ZONEIDS,ZONEINFORESNAME,kCUSTOM_TZ_PREFIX,kGMT_ID,kMAX_CUSTOM_HOUR,kMAX_CUSTOM_MIN,kMAX_CUSTOM_SEC,kNAMES,kREGIONS,kZONES
hcls OlsonToMetaMappingEntry

CLSS public com.ibm.icu.impl.ZoneStringFormat
cons protected init(com.ibm.icu.util.ULocale)
cons public init(java.lang.String[][])
innr public static ZoneStringInfo
meth public com.ibm.icu.impl.ZoneStringFormat$ZoneStringInfo findGenericLocation(java.lang.String,int)
meth public com.ibm.icu.impl.ZoneStringFormat$ZoneStringInfo findGenericLong(java.lang.String,int)
meth public com.ibm.icu.impl.ZoneStringFormat$ZoneStringInfo findGenericShort(java.lang.String,int)
meth public com.ibm.icu.impl.ZoneStringFormat$ZoneStringInfo findSpecificLong(java.lang.String,int)
meth public com.ibm.icu.impl.ZoneStringFormat$ZoneStringInfo findSpecificShort(java.lang.String,int)
meth public java.lang.String getGenericLocation(java.lang.String)
meth public java.lang.String getGenericLocationString(com.ibm.icu.util.Calendar)
meth public java.lang.String getGenericLongString(com.ibm.icu.util.Calendar)
meth public java.lang.String getGenericShortString(com.ibm.icu.util.Calendar,boolean)
meth public java.lang.String getLongDaylight(java.lang.String,long)
meth public java.lang.String getLongGenericNonLocation(java.lang.String,long)
meth public java.lang.String getLongGenericPartialLocation(java.lang.String,long)
meth public java.lang.String getLongStandard(java.lang.String,long)
meth public java.lang.String getShortDaylight(java.lang.String,long,boolean)
meth public java.lang.String getShortGenericNonLocation(java.lang.String,long,boolean)
meth public java.lang.String getShortGenericPartialLocation(java.lang.String,long,boolean)
meth public java.lang.String getShortStandard(java.lang.String,long,boolean)
meth public java.lang.String getSpecificLongString(com.ibm.icu.util.Calendar)
meth public java.lang.String getSpecificShortString(com.ibm.icu.util.Calendar,boolean)
meth public java.lang.String[][] getZoneStrings()
meth public static com.ibm.icu.impl.ZoneStringFormat getInstance(com.ibm.icu.util.ULocale)
supr java.lang.Object
hfds DAYLIGHT_LONG,DAYLIGHT_SHORT,DST_CHECK_RANGE,GENERIC_LONG,GENERIC_SHORT,INDEXMAP,LOCATION,NAMETYPEMAP,RESKEY_COMMONLY_USED,RESKEY_EXEMPLAR_CITY,RESKEY_LONG_DAYLIGHT,RESKEY_LONG_GENERIC,RESKEY_LONG_STANDARD,RESKEY_SHORT_DAYLIGHT,RESKEY_SHORT_GENERIC,RESKEY_SHORT_STANDARD,STANDARD_LONG,STANDARD_SHORT,TZFORMAT_CACHE,ZSIDX_LOCATION,ZSIDX_LONG_DAYLIGHT,ZSIDX_LONG_GENERIC,ZSIDX_LONG_STANDARD,ZSIDX_MAX,ZSIDX_SHORT_DAYLIGHT,ZSIDX_SHORT_GENERIC,ZSIDX_SHORT_STANDARD,isFullyLoaded,locale,mzidToStrings,region,tzidToStrings,zoneStringsTrie
hcls ZoneStringSearchResultHandler,ZoneStrings

CLSS public static com.ibm.icu.impl.ZoneStringFormat$ZoneStringInfo
 outer com.ibm.icu.impl.ZoneStringFormat
meth public boolean isDaylight()
meth public boolean isGeneric()
meth public boolean isStandard()
meth public java.lang.String getID()
meth public java.lang.String getString()
supr java.lang.Object
hfds id,str,type

CLSS public com.ibm.icu.impl.data.BreakIteratorRules
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds contents

CLSS public com.ibm.icu.impl.data.BreakIteratorRules_th
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds DATA_NAME

CLSS public com.ibm.icu.impl.data.HolidayBundle
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents

CLSS public com.ibm.icu.impl.data.HolidayBundle_da
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents

CLSS public com.ibm.icu.impl.data.HolidayBundle_da_DK
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents,fHolidays

CLSS public com.ibm.icu.impl.data.HolidayBundle_de
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents

CLSS public com.ibm.icu.impl.data.HolidayBundle_de_AT
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents,fHolidays

CLSS public com.ibm.icu.impl.data.HolidayBundle_de_DE
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents,fHolidays

CLSS public com.ibm.icu.impl.data.HolidayBundle_el
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents

CLSS public com.ibm.icu.impl.data.HolidayBundle_el_GR
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents,fHolidays

CLSS public com.ibm.icu.impl.data.HolidayBundle_en
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents

CLSS public com.ibm.icu.impl.data.HolidayBundle_en_CA
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents,fHolidays

CLSS public com.ibm.icu.impl.data.HolidayBundle_en_GB
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents,fHolidays

CLSS public com.ibm.icu.impl.data.HolidayBundle_en_US
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents,fHolidays

CLSS public com.ibm.icu.impl.data.HolidayBundle_es
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents

CLSS public com.ibm.icu.impl.data.HolidayBundle_es_MX
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents,fHolidays

CLSS public com.ibm.icu.impl.data.HolidayBundle_fr
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents

CLSS public com.ibm.icu.impl.data.HolidayBundle_fr_CA
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents,fHolidays

CLSS public com.ibm.icu.impl.data.HolidayBundle_fr_FR
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents,fHolidays

CLSS public com.ibm.icu.impl.data.HolidayBundle_it
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents

CLSS public com.ibm.icu.impl.data.HolidayBundle_it_IT
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents,fHolidays

CLSS public com.ibm.icu.impl.data.HolidayBundle_iw
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents

CLSS public com.ibm.icu.impl.data.HolidayBundle_iw_IL
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents,fHolidays

CLSS public com.ibm.icu.impl.data.HolidayBundle_ja_JP
cons public init()
meth public java.lang.Object[][] getContents()
supr java.util.ListResourceBundle
hfds fContents,fHolidays

CLSS public com.ibm.icu.impl.data.ResourceReader
cons public init(java.io.InputStream,java.lang.String)
cons public init(java.io.InputStream,java.lang.String,java.lang.String)
cons public init(java.lang.Class<?>,java.lang.String)
cons public init(java.lang.Class<?>,java.lang.String,java.lang.String) throws java.io.UnsupportedEncodingException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String) throws java.io.UnsupportedEncodingException
meth public int getLineNumber()
meth public java.lang.String describePosition()
meth public java.lang.String readLine() throws java.io.IOException
meth public java.lang.String readLineSkippingComments() throws java.io.IOException
meth public java.lang.String readLineSkippingComments(boolean) throws java.io.IOException
meth public void reset()
supr java.lang.Object
hfds encoding,lineNo,reader,resourceName,root

CLSS public com.ibm.icu.impl.data.TokenIterator
cons public init(com.ibm.icu.impl.data.ResourceReader)
meth public int getLineNumber()
meth public java.lang.String describePosition()
meth public java.lang.String next() throws java.io.IOException
supr java.lang.Object
hfds buf,done,lastpos,line,pos,reader

CLSS public com.ibm.icu.impl.duration.BasicDurationFormat
cons public init()
cons public init(com.ibm.icu.util.ULocale)
meth public java.lang.String formatDuration(java.lang.Object)
meth public java.lang.String formatDurationFrom(long,long)
meth public java.lang.String formatDurationFromNow(long)
meth public java.lang.String formatDurationFromNowTo(java.util.Date)
meth public java.lang.StringBuffer format(java.lang.Object,java.lang.StringBuffer,java.text.FieldPosition)
meth public static com.ibm.icu.impl.duration.BasicDurationFormat getInstance(com.ibm.icu.util.ULocale)
supr com.ibm.icu.text.DurationFormat
hfds checkXMLDuration,formatter,pformatter,pfs,serialVersionUID

CLSS public com.ibm.icu.impl.duration.BasicPeriodFormatterFactory
intf com.ibm.icu.impl.duration.PeriodFormatterFactory
meth public boolean getDisplayLimit()
meth public boolean getDisplayPastFuture()
meth public com.ibm.icu.impl.duration.PeriodFormatter getFormatter()
meth public com.ibm.icu.impl.duration.PeriodFormatterFactory setCountVariant(int)
meth public com.ibm.icu.impl.duration.PeriodFormatterFactory setDisplayLimit(boolean)
meth public com.ibm.icu.impl.duration.PeriodFormatterFactory setDisplayPastFuture(boolean)
meth public com.ibm.icu.impl.duration.PeriodFormatterFactory setLocale(java.lang.String)
meth public com.ibm.icu.impl.duration.PeriodFormatterFactory setSeparatorVariant(int)
meth public com.ibm.icu.impl.duration.PeriodFormatterFactory setUnitVariant(int)
meth public int getCountVariant()
meth public int getSeparatorVariant()
meth public int getUnitVariant()
meth public static com.ibm.icu.impl.duration.BasicPeriodFormatterFactory getDefault()
supr java.lang.Object
hfds customizations,customizationsInUse,data,ds,localeName
hcls Customizations

CLSS public com.ibm.icu.impl.duration.BasicPeriodFormatterService
cons public init(com.ibm.icu.impl.duration.impl.PeriodFormatterDataService)
intf com.ibm.icu.impl.duration.PeriodFormatterService
meth public com.ibm.icu.impl.duration.DurationFormatterFactory newDurationFormatterFactory()
meth public com.ibm.icu.impl.duration.PeriodBuilderFactory newPeriodBuilderFactory()
meth public com.ibm.icu.impl.duration.PeriodFormatterFactory newPeriodFormatterFactory()
meth public java.util.Collection<java.lang.String> getAvailableLocaleNames()
meth public static com.ibm.icu.impl.duration.BasicPeriodFormatterService getInstance()
supr java.lang.Object
hfds ds,instance

CLSS public abstract interface com.ibm.icu.impl.duration.DateFormatter
meth public abstract com.ibm.icu.impl.duration.DateFormatter withLocale(java.lang.String)
meth public abstract com.ibm.icu.impl.duration.DateFormatter withTimeZone(java.util.TimeZone)
meth public abstract java.lang.String format(java.util.Date)
meth public abstract java.lang.String format(long)

CLSS public abstract interface com.ibm.icu.impl.duration.DurationFormatter
meth public abstract com.ibm.icu.impl.duration.DurationFormatter withLocale(java.lang.String)
meth public abstract com.ibm.icu.impl.duration.DurationFormatter withTimeZone(java.util.TimeZone)
meth public abstract java.lang.String formatDurationFrom(long,long)
meth public abstract java.lang.String formatDurationFromNow(long)
meth public abstract java.lang.String formatDurationFromNowTo(java.util.Date)

CLSS public abstract interface com.ibm.icu.impl.duration.DurationFormatterFactory
meth public abstract com.ibm.icu.impl.duration.DurationFormatter getFormatter()
meth public abstract com.ibm.icu.impl.duration.DurationFormatterFactory setFallback(com.ibm.icu.impl.duration.DateFormatter)
meth public abstract com.ibm.icu.impl.duration.DurationFormatterFactory setFallbackLimit(long)
meth public abstract com.ibm.icu.impl.duration.DurationFormatterFactory setLocale(java.lang.String)
meth public abstract com.ibm.icu.impl.duration.DurationFormatterFactory setPeriodBuilder(com.ibm.icu.impl.duration.PeriodBuilder)
meth public abstract com.ibm.icu.impl.duration.DurationFormatterFactory setPeriodFormatter(com.ibm.icu.impl.duration.PeriodFormatter)
meth public abstract com.ibm.icu.impl.duration.DurationFormatterFactory setTimeZone(java.util.TimeZone)

CLSS public final com.ibm.icu.impl.duration.Period
meth public boolean equals(com.ibm.icu.impl.duration.Period)
meth public boolean equals(java.lang.Object)
meth public boolean isInFuture()
meth public boolean isInPast()
meth public boolean isLessThan()
meth public boolean isMoreThan()
meth public boolean isSet()
meth public boolean isSet(com.ibm.icu.impl.duration.TimeUnit)
meth public com.ibm.icu.impl.duration.Period and(float,com.ibm.icu.impl.duration.TimeUnit)
meth public com.ibm.icu.impl.duration.Period at()
meth public com.ibm.icu.impl.duration.Period inFuture()
meth public com.ibm.icu.impl.duration.Period inFuture(boolean)
meth public com.ibm.icu.impl.duration.Period inPast()
meth public com.ibm.icu.impl.duration.Period inPast(boolean)
meth public com.ibm.icu.impl.duration.Period lessThan()
meth public com.ibm.icu.impl.duration.Period moreThan()
meth public com.ibm.icu.impl.duration.Period omit(com.ibm.icu.impl.duration.TimeUnit)
meth public float getCount(com.ibm.icu.impl.duration.TimeUnit)
meth public int hashCode()
meth public static com.ibm.icu.impl.duration.Period at(float,com.ibm.icu.impl.duration.TimeUnit)
meth public static com.ibm.icu.impl.duration.Period lessThan(float,com.ibm.icu.impl.duration.TimeUnit)
meth public static com.ibm.icu.impl.duration.Period moreThan(float,com.ibm.icu.impl.duration.TimeUnit)
supr java.lang.Object
hfds counts,inFuture,timeLimit

CLSS public abstract interface com.ibm.icu.impl.duration.PeriodBuilder
meth public abstract com.ibm.icu.impl.duration.Period create(long)
meth public abstract com.ibm.icu.impl.duration.Period createWithReferenceDate(long,long)
meth public abstract com.ibm.icu.impl.duration.PeriodBuilder withLocale(java.lang.String)
meth public abstract com.ibm.icu.impl.duration.PeriodBuilder withTimeZone(java.util.TimeZone)

CLSS public abstract interface com.ibm.icu.impl.duration.PeriodBuilderFactory
meth public abstract com.ibm.icu.impl.duration.PeriodBuilder getFixedUnitBuilder(com.ibm.icu.impl.duration.TimeUnit)
meth public abstract com.ibm.icu.impl.duration.PeriodBuilder getMultiUnitBuilder(int)
meth public abstract com.ibm.icu.impl.duration.PeriodBuilder getOneOrTwoUnitBuilder()
meth public abstract com.ibm.icu.impl.duration.PeriodBuilder getSingleUnitBuilder()
meth public abstract com.ibm.icu.impl.duration.PeriodBuilderFactory setAllowMilliseconds(boolean)
meth public abstract com.ibm.icu.impl.duration.PeriodBuilderFactory setAllowZero(boolean)
meth public abstract com.ibm.icu.impl.duration.PeriodBuilderFactory setAvailableUnitRange(com.ibm.icu.impl.duration.TimeUnit,com.ibm.icu.impl.duration.TimeUnit)
meth public abstract com.ibm.icu.impl.duration.PeriodBuilderFactory setLocale(java.lang.String)
meth public abstract com.ibm.icu.impl.duration.PeriodBuilderFactory setMaxLimit(float)
meth public abstract com.ibm.icu.impl.duration.PeriodBuilderFactory setMinLimit(float)
meth public abstract com.ibm.icu.impl.duration.PeriodBuilderFactory setTimeZone(java.util.TimeZone)
meth public abstract com.ibm.icu.impl.duration.PeriodBuilderFactory setUnitIsAvailable(com.ibm.icu.impl.duration.TimeUnit,boolean)
meth public abstract com.ibm.icu.impl.duration.PeriodBuilderFactory setWeeksAloneOnly(boolean)

CLSS public abstract interface com.ibm.icu.impl.duration.PeriodFormatter
meth public abstract com.ibm.icu.impl.duration.PeriodFormatter withLocale(java.lang.String)
meth public abstract java.lang.String format(com.ibm.icu.impl.duration.Period)

CLSS public abstract interface com.ibm.icu.impl.duration.PeriodFormatterFactory
meth public abstract com.ibm.icu.impl.duration.PeriodFormatter getFormatter()
meth public abstract com.ibm.icu.impl.duration.PeriodFormatterFactory setCountVariant(int)
meth public abstract com.ibm.icu.impl.duration.PeriodFormatterFactory setDisplayLimit(boolean)
meth public abstract com.ibm.icu.impl.duration.PeriodFormatterFactory setDisplayPastFuture(boolean)
meth public abstract com.ibm.icu.impl.duration.PeriodFormatterFactory setLocale(java.lang.String)
meth public abstract com.ibm.icu.impl.duration.PeriodFormatterFactory setSeparatorVariant(int)
meth public abstract com.ibm.icu.impl.duration.PeriodFormatterFactory setUnitVariant(int)

CLSS public abstract interface com.ibm.icu.impl.duration.PeriodFormatterService
meth public abstract com.ibm.icu.impl.duration.DurationFormatterFactory newDurationFormatterFactory()
meth public abstract com.ibm.icu.impl.duration.PeriodBuilderFactory newPeriodBuilderFactory()
meth public abstract com.ibm.icu.impl.duration.PeriodFormatterFactory newPeriodFormatterFactory()
meth public abstract java.util.Collection<java.lang.String> getAvailableLocaleNames()

CLSS public final com.ibm.icu.impl.duration.TimeUnit
fld public final static com.ibm.icu.impl.duration.TimeUnit DAY
fld public final static com.ibm.icu.impl.duration.TimeUnit HOUR
fld public final static com.ibm.icu.impl.duration.TimeUnit MILLISECOND
fld public final static com.ibm.icu.impl.duration.TimeUnit MINUTE
fld public final static com.ibm.icu.impl.duration.TimeUnit MONTH
fld public final static com.ibm.icu.impl.duration.TimeUnit SECOND
fld public final static com.ibm.icu.impl.duration.TimeUnit WEEK
fld public final static com.ibm.icu.impl.duration.TimeUnit YEAR
meth public com.ibm.icu.impl.duration.TimeUnit larger()
meth public com.ibm.icu.impl.duration.TimeUnit smaller()
meth public int ordinal()
meth public java.lang.String toString()
supr java.lang.Object
hfds approxDurations,name,ordinal,units

CLSS public abstract interface com.ibm.icu.impl.duration.TimeUnitConstants
fld public final static com.ibm.icu.impl.duration.TimeUnit DAY
fld public final static com.ibm.icu.impl.duration.TimeUnit HOUR
fld public final static com.ibm.icu.impl.duration.TimeUnit MILLISECOND
fld public final static com.ibm.icu.impl.duration.TimeUnit MINUTE
fld public final static com.ibm.icu.impl.duration.TimeUnit MONTH
fld public final static com.ibm.icu.impl.duration.TimeUnit SECOND
fld public final static com.ibm.icu.impl.duration.TimeUnit WEEK
fld public final static com.ibm.icu.impl.duration.TimeUnit YEAR

CLSS public com.ibm.icu.impl.duration.impl.DataRecord
cons public init()
innr public abstract interface static ECountVariant
innr public abstract interface static EDecimalHandling
innr public abstract interface static EFractionHandling
innr public abstract interface static EGender
innr public abstract interface static EHalfPlacement
innr public abstract interface static EHalfSupport
innr public abstract interface static EMilliSupport
innr public abstract interface static ENumberSystem
innr public abstract interface static EPluralization
innr public abstract interface static ESeparatorVariant
innr public abstract interface static ETimeDirection
innr public abstract interface static ETimeLimit
innr public abstract interface static EUnitVariant
innr public abstract interface static EZeroHandling
innr public static ScopeData
meth public static com.ibm.icu.impl.duration.impl.DataRecord read(java.lang.String,com.ibm.icu.impl.duration.impl.RecordReader)
meth public void write(com.ibm.icu.impl.duration.impl.RecordWriter)
supr java.lang.Object
hfds allowZero,countSep,decimalHandling,decimalSep,digitPrefix,fifteenMinutes,fiveMinutes,fractionHandling,genders,halfNames,halfPlacements,halfSupport,halves,measures,mediumNames,numberNames,numberSystem,omitDualCount,omitSingularCount,optSuffixes,pl,pluralNames,requiresDigitSeparator,requiresSkipMarker,rqdSuffixes,scopeData,shortNames,shortUnitSep,singularNames,skippedUnitMarker,unitSep,unitSepRequiresDP,useMilliseconds,weeksAloneOnly,zero,zeroHandling

CLSS public abstract interface static com.ibm.icu.impl.duration.impl.DataRecord$ECountVariant
 outer com.ibm.icu.impl.duration.impl.DataRecord
fld public final static byte DECIMAL1 = 3
fld public final static byte DECIMAL2 = 4
fld public final static byte DECIMAL3 = 5
fld public final static byte HALF_FRACTION = 2
fld public final static byte INTEGER = 0
fld public final static byte INTEGER_CUSTOM = 1
fld public final static java.lang.String[] names

CLSS public abstract interface static com.ibm.icu.impl.duration.impl.DataRecord$EDecimalHandling
 outer com.ibm.icu.impl.duration.impl.DataRecord
fld public final static byte DPAUCAL = 3
fld public final static byte DPLURAL = 0
fld public final static byte DSINGULAR = 1
fld public final static byte DSINGULAR_SUBONE = 2
fld public final static java.lang.String[] names

CLSS public abstract interface static com.ibm.icu.impl.duration.impl.DataRecord$EFractionHandling
 outer com.ibm.icu.impl.duration.impl.DataRecord
fld public final static byte FPAUCAL = 3
fld public final static byte FPLURAL = 0
fld public final static byte FSINGULAR_PLURAL = 1
fld public final static byte FSINGULAR_PLURAL_ANDAHALF = 2
fld public final static java.lang.String[] names

CLSS public abstract interface static com.ibm.icu.impl.duration.impl.DataRecord$EGender
 outer com.ibm.icu.impl.duration.impl.DataRecord
fld public final static byte F = 1
fld public final static byte M = 0
fld public final static byte N = 2
fld public final static java.lang.String[] names

CLSS public abstract interface static com.ibm.icu.impl.duration.impl.DataRecord$EHalfPlacement
 outer com.ibm.icu.impl.duration.impl.DataRecord
fld public final static byte AFTER_FIRST = 1
fld public final static byte LAST = 2
fld public final static byte PREFIX = 0
fld public final static java.lang.String[] names

CLSS public abstract interface static com.ibm.icu.impl.duration.impl.DataRecord$EHalfSupport
 outer com.ibm.icu.impl.duration.impl.DataRecord
fld public final static byte NO = 1
fld public final static byte ONE_PLUS = 2
fld public final static byte YES = 0
fld public final static java.lang.String[] names

CLSS public abstract interface static com.ibm.icu.impl.duration.impl.DataRecord$EMilliSupport
 outer com.ibm.icu.impl.duration.impl.DataRecord
fld public final static byte NO = 1
fld public final static byte WITH_SECONDS = 2
fld public final static byte YES = 0
fld public final static java.lang.String[] names

CLSS public abstract interface static com.ibm.icu.impl.duration.impl.DataRecord$ENumberSystem
 outer com.ibm.icu.impl.duration.impl.DataRecord
fld public final static byte CHINESE_SIMPLIFIED = 2
fld public final static byte CHINESE_TRADITIONAL = 1
fld public final static byte DEFAULT = 0
fld public final static byte KOREAN = 3
fld public final static java.lang.String[] names

CLSS public abstract interface static com.ibm.icu.impl.duration.impl.DataRecord$EPluralization
 outer com.ibm.icu.impl.duration.impl.DataRecord
fld public final static byte ARABIC = 5
fld public final static byte DUAL = 2
fld public final static byte HEBREW = 4
fld public final static byte NONE = 0
fld public final static byte PAUCAL = 3
fld public final static byte PLURAL = 1
fld public final static java.lang.String[] names

CLSS public abstract interface static com.ibm.icu.impl.duration.impl.DataRecord$ESeparatorVariant
 outer com.ibm.icu.impl.duration.impl.DataRecord
fld public final static byte FULL = 2
fld public final static byte NONE = 0
fld public final static byte SHORT = 1
fld public final static java.lang.String[] names

CLSS public abstract interface static com.ibm.icu.impl.duration.impl.DataRecord$ETimeDirection
 outer com.ibm.icu.impl.duration.impl.DataRecord
fld public final static byte FUTURE = 2
fld public final static byte NODIRECTION = 0
fld public final static byte PAST = 1
fld public final static java.lang.String[] names

CLSS public abstract interface static com.ibm.icu.impl.duration.impl.DataRecord$ETimeLimit
 outer com.ibm.icu.impl.duration.impl.DataRecord
fld public final static byte LT = 1
fld public final static byte MT = 2
fld public final static byte NOLIMIT = 0
fld public final static java.lang.String[] names

CLSS public abstract interface static com.ibm.icu.impl.duration.impl.DataRecord$EUnitVariant
 outer com.ibm.icu.impl.duration.impl.DataRecord
fld public final static byte MEDIUM = 1
fld public final static byte PLURALIZED = 0
fld public final static byte SHORT = 2
fld public final static java.lang.String[] names

CLSS public abstract interface static com.ibm.icu.impl.duration.impl.DataRecord$EZeroHandling
 outer com.ibm.icu.impl.duration.impl.DataRecord
fld public final static byte ZPLURAL = 0
fld public final static byte ZSINGULAR = 1
fld public final static java.lang.String[] names

CLSS public static com.ibm.icu.impl.duration.impl.DataRecord$ScopeData
 outer com.ibm.icu.impl.duration.impl.DataRecord
cons public init()
meth public static com.ibm.icu.impl.duration.impl.DataRecord$ScopeData read(com.ibm.icu.impl.duration.impl.RecordReader)
meth public void write(com.ibm.icu.impl.duration.impl.RecordWriter)
supr java.lang.Object
hfds prefix,requiresDigitPrefix,suffix

CLSS public com.ibm.icu.impl.duration.impl.PeriodFormatterData
cons public init(java.lang.String,com.ibm.icu.impl.duration.impl.DataRecord)
fld public static boolean trace
meth public boolean allowZero()
meth public boolean appendPrefix(int,int,java.lang.StringBuffer)
meth public boolean appendUnit(com.ibm.icu.impl.duration.TimeUnit,int,int,int,boolean,boolean,boolean,boolean,boolean,java.lang.StringBuffer)
meth public boolean appendUnitSeparator(com.ibm.icu.impl.duration.TimeUnit,boolean,boolean,boolean,java.lang.StringBuffer)
meth public boolean weeksAloneOnly()
meth public int appendCount(com.ibm.icu.impl.duration.TimeUnit,boolean,boolean,int,int,boolean,java.lang.String,boolean,java.lang.StringBuffer)
meth public int pluralization()
meth public int useMilliseconds()
meth public void appendCountValue(int,int,int,java.lang.StringBuffer)
meth public void appendDigits(long,int,int,java.lang.StringBuffer)
meth public void appendInteger(int,int,int,java.lang.StringBuffer)
meth public void appendSkippedUnit(java.lang.StringBuffer)
meth public void appendSuffix(int,int,java.lang.StringBuffer)
supr java.lang.Object
hfds FORM_DUAL,FORM_HALF_SPELLED,FORM_PAUCAL,FORM_PLURAL,FORM_SINGULAR,FORM_SINGULAR_NO_OMIT,FORM_SINGULAR_SPELLED,dr,localeName

CLSS public abstract com.ibm.icu.impl.duration.impl.PeriodFormatterDataService
cons public init()
meth public abstract com.ibm.icu.impl.duration.impl.PeriodFormatterData get(java.lang.String)
meth public abstract java.util.Collection<java.lang.String> getAvailableLocales()
supr java.lang.Object

CLSS public com.ibm.icu.impl.duration.impl.ResourceBasedPeriodFormatterDataService
meth public com.ibm.icu.impl.duration.impl.PeriodFormatterData get(java.lang.String)
meth public java.util.Collection<java.lang.String> getAvailableLocales()
meth public static com.ibm.icu.impl.duration.impl.ResourceBasedPeriodFormatterDataService getInstance()
supr com.ibm.icu.impl.duration.impl.PeriodFormatterDataService
hfds PATH,availableLocales,cache,lastData,lastLocale,singleton

CLSS public com.ibm.icu.impl.duration.impl.Utils
cons public init()
innr public static ChineseDigits
meth public final static java.util.Locale localeFromString(java.lang.String)
meth public static java.lang.String chineseNumber(long,com.ibm.icu.impl.duration.impl.Utils$ChineseDigits)
meth public static void main(java.lang.String[])
supr java.lang.Object

CLSS public static com.ibm.icu.impl.duration.impl.Utils$ChineseDigits
 outer com.ibm.icu.impl.duration.impl.Utils
fld public final static com.ibm.icu.impl.duration.impl.Utils$ChineseDigits DEBUG
fld public final static com.ibm.icu.impl.duration.impl.Utils$ChineseDigits KOREAN
fld public final static com.ibm.icu.impl.duration.impl.Utils$ChineseDigits SIMPLIFIED
fld public final static com.ibm.icu.impl.duration.impl.Utils$ChineseDigits TRADITIONAL
supr java.lang.Object
hfds digits,ko,levels,liang,units

CLSS public com.ibm.icu.impl.duration.impl.XMLRecordReader
cons public init(java.io.Reader)
meth public boolean bool(java.lang.String)
meth public boolean close()
meth public boolean open(java.lang.String)
meth public boolean[] boolArray(java.lang.String)
meth public byte namedIndex(java.lang.String,java.lang.String[])
meth public byte[] namedIndexArray(java.lang.String,java.lang.String[])
meth public char character(java.lang.String)
meth public char[] characterArray(java.lang.String)
meth public java.lang.String string(java.lang.String)
meth public java.lang.String[] stringArray(java.lang.String)
meth public java.lang.String[][] stringTable(java.lang.String)
supr java.lang.Object
hfds atTag,nameStack,r,tag

CLSS public com.ibm.icu.impl.duration.impl.XMLRecordWriter
cons public init(java.io.Writer)
meth public boolean close()
meth public boolean open(java.lang.String)
meth public static java.lang.String normalize(java.lang.String)
meth public void bool(java.lang.String,boolean)
meth public void boolArray(java.lang.String,boolean[])
meth public void character(java.lang.String,char)
meth public void characterArray(java.lang.String,char[])
meth public void flush()
meth public void namedIndex(java.lang.String,java.lang.String[],int)
meth public void namedIndexArray(java.lang.String,java.lang.String[],byte[])
meth public void string(java.lang.String,java.lang.String)
meth public void stringArray(java.lang.String,java.lang.String[])
meth public void stringTable(java.lang.String,java.lang.String[][])
supr java.lang.Object
hfds INDENT,NULL_NAME,nameStack,w

CLSS public com.ibm.icu.impl.duration.impl.YMDDateFormatter
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String,java.util.TimeZone)
intf com.ibm.icu.impl.duration.DateFormatter
meth public com.ibm.icu.impl.duration.DateFormatter withLocale(java.lang.String)
meth public com.ibm.icu.impl.duration.DateFormatter withTimeZone(java.util.TimeZone)
meth public java.lang.String format(java.util.Date)
meth public java.lang.String format(long)
supr java.lang.Object
hfds df,localeName,requestedFields,timeZone

CLSS public final com.ibm.icu.lang.UCharacter
fld public final static char MAX_HIGH_SURROGATE = '\udbff'
fld public final static char MAX_LOW_SURROGATE = '\udfff'
fld public final static char MAX_SURROGATE = '\udfff'
fld public final static char MIN_HIGH_SURROGATE = '\ud800'
fld public final static char MIN_LOW_SURROGATE = '\udc00'
fld public final static char MIN_SURROGATE = '\ud800'
fld public final static double NO_NUMERIC_VALUE = -1.23456789E8
fld public final static int FOLD_CASE_DEFAULT = 0
fld public final static int FOLD_CASE_EXCLUDE_SPECIAL_I = 1
fld public final static int MAX_CODE_POINT = 1114111
fld public final static int MAX_RADIX = 36
fld public final static int MAX_VALUE = 1114111
fld public final static int MIN_CODE_POINT = 0
fld public final static int MIN_RADIX = 2
fld public final static int MIN_SUPPLEMENTARY_CODE_POINT = 65536
fld public final static int MIN_VALUE = 0
fld public final static int REPLACEMENT_CHAR = 65533
fld public final static int SUPPLEMENTARY_MIN_VALUE = 65536
fld public final static int TITLECASE_NO_BREAK_ADJUSTMENT = 512
fld public final static int TITLECASE_NO_LOWERCASE = 256
innr public abstract interface static DecompositionType
innr public abstract interface static EastAsianWidth
innr public abstract interface static GraphemeClusterBreak
innr public abstract interface static HangulSyllableType
innr public abstract interface static JoiningGroup
innr public abstract interface static JoiningType
innr public abstract interface static LineBreak
innr public abstract interface static NumericType
innr public abstract interface static SentenceBreak
innr public abstract interface static WordBreak
innr public final static UnicodeBlock
intf com.ibm.icu.lang.UCharacterEnums$ECharacterCategory
intf com.ibm.icu.lang.UCharacterEnums$ECharacterDirection
meth public final static boolean isSupplementaryCodePoint(int)
meth public final static boolean isSurrogatePair(char,char)
meth public final static boolean isValidCodePoint(int)
meth public final static char[] toChars(int)
meth public final static int codePointAt(char[],int)
meth public final static int codePointAt(char[],int,int)
meth public final static int codePointAt(java.lang.CharSequence,int)
meth public final static int codePointBefore(char[],int)
meth public final static int codePointBefore(char[],int,int)
meth public final static int codePointBefore(java.lang.CharSequence,int)
meth public final static int toChars(int,char[],int)
meth public final static int toCodePoint(char,char)
meth public final static java.lang.String foldCase(java.lang.String,int)
meth public static boolean hasBinaryProperty(int,int)
meth public static boolean isBMP(int)
meth public static boolean isBaseForm(int)
meth public static boolean isDefined(int)
meth public static boolean isDigit(int)
meth public static boolean isHighSurrogate(char)
meth public static boolean isISOControl(int)
meth public static boolean isIdentifierIgnorable(int)
meth public static boolean isJavaIdentifierPart(int)
meth public static boolean isJavaIdentifierStart(int)
meth public static boolean isJavaLetter(int)
meth public static boolean isJavaLetterOrDigit(int)
meth public static boolean isLegal(int)
meth public static boolean isLegal(java.lang.String)
meth public static boolean isLetter(int)
meth public static boolean isLetterOrDigit(int)
meth public static boolean isLowSurrogate(char)
meth public static boolean isLowerCase(int)
meth public static boolean isMirrored(int)
meth public static boolean isPrintable(int)
meth public static boolean isSpace(int)
meth public static boolean isSpaceChar(int)
meth public static boolean isSupplementary(int)
meth public static boolean isTitleCase(int)
meth public static boolean isUAlphabetic(int)
meth public static boolean isULowercase(int)
meth public static boolean isUUppercase(int)
meth public static boolean isUWhiteSpace(int)
meth public static boolean isUnicodeIdentifierPart(int)
meth public static boolean isUnicodeIdentifierStart(int)
meth public static boolean isUpperCase(int)
meth public static boolean isWhitespace(int)
meth public static byte getDirectionality(int)
meth public static char forDigit(int,int)
meth public static com.ibm.icu.util.RangeValueIterator getTypeIterator()
meth public static com.ibm.icu.util.ValueIterator getExtendedNameIterator()
meth public static com.ibm.icu.util.ValueIterator getName1_0Iterator()
meth public static com.ibm.icu.util.ValueIterator getNameIterator()
meth public static com.ibm.icu.util.VersionInfo getAge(int)
meth public static com.ibm.icu.util.VersionInfo getUnicodeVersion()
meth public static double getUnicodeNumericValue(int)
meth public static int charCount(int)
meth public static int codePointCount(char[],int,int)
meth public static int codePointCount(java.lang.CharSequence,int,int)
meth public static int digit(int)
meth public static int digit(int,int)
meth public static int foldCase(int,boolean)
meth public static int foldCase(int,int)
meth public static int getCharFromExtendedName(java.lang.String)
meth public static int getCharFromName(java.lang.String)
meth public static int getCharFromName1_0(java.lang.String)
meth public static int getCharFromNameAlias(java.lang.String)
meth public static int getCodePoint(char)
meth public static int getCodePoint(char,char)
meth public static int getCombiningClass(int)
meth public static int getDirection(int)
meth public static int getHanNumericValue(int)
meth public static int getIntPropertyMaxValue(int)
meth public static int getIntPropertyMinValue(int)
meth public static int getIntPropertyValue(int,int)
meth public static int getMirror(int)
meth public static int getNumericValue(int)
meth public static int getPropertyEnum(java.lang.String)
meth public static int getPropertyValueEnum(int,java.lang.String)
meth public static int getType(int)
meth public static int offsetByCodePoints(char[],int,int,int,int)
meth public static int offsetByCodePoints(java.lang.CharSequence,int,int)
meth public static int toLowerCase(int)
meth public static int toTitleCase(int)
meth public static int toUpperCase(int)
meth public static java.lang.String foldCase(java.lang.String,boolean)
meth public static java.lang.String getExtendedName(int)
meth public static java.lang.String getISOComment(int)
meth public static java.lang.String getName(int)
meth public static java.lang.String getName(java.lang.String,java.lang.String)
meth public static java.lang.String getName1_0(int)
meth public static java.lang.String getNameAlias(int)
meth public static java.lang.String getPropertyName(int,int)
meth public static java.lang.String getPropertyValueName(int,int,int)
meth public static java.lang.String getStringPropertyValue(int,int,int)
meth public static java.lang.String toLowerCase(com.ibm.icu.util.ULocale,java.lang.String)
meth public static java.lang.String toLowerCase(java.lang.String)
meth public static java.lang.String toLowerCase(java.util.Locale,java.lang.String)
meth public static java.lang.String toString(int)
meth public static java.lang.String toTitleCase(com.ibm.icu.util.ULocale,java.lang.String,com.ibm.icu.text.BreakIterator)
meth public static java.lang.String toTitleCase(com.ibm.icu.util.ULocale,java.lang.String,com.ibm.icu.text.BreakIterator,int)
meth public static java.lang.String toTitleCase(java.lang.String,com.ibm.icu.text.BreakIterator)
meth public static java.lang.String toTitleCase(java.util.Locale,java.lang.String,com.ibm.icu.text.BreakIterator)
meth public static java.lang.String toUpperCase(com.ibm.icu.util.ULocale,java.lang.String)
meth public static java.lang.String toUpperCase(java.lang.String)
meth public static java.lang.String toUpperCase(java.util.Locale,java.lang.String)
supr java.lang.Object
hfds APPLICATION_PROGRAM_COMMAND_,BLOCK_MASK_,BLOCK_SHIFT_,CJK_IDEOGRAPH_COMPLEX_EIGHT_,CJK_IDEOGRAPH_COMPLEX_FIVE_,CJK_IDEOGRAPH_COMPLEX_FOUR_,CJK_IDEOGRAPH_COMPLEX_HUNDRED_,CJK_IDEOGRAPH_COMPLEX_NINE_,CJK_IDEOGRAPH_COMPLEX_ONE_,CJK_IDEOGRAPH_COMPLEX_SEVEN_,CJK_IDEOGRAPH_COMPLEX_SIX_,CJK_IDEOGRAPH_COMPLEX_TEN_,CJK_IDEOGRAPH_COMPLEX_THOUSAND_,CJK_IDEOGRAPH_COMPLEX_THREE_,CJK_IDEOGRAPH_COMPLEX_TWO_,CJK_IDEOGRAPH_COMPLEX_ZERO_,CJK_IDEOGRAPH_EIGHTH_,CJK_IDEOGRAPH_FIFTH_,CJK_IDEOGRAPH_FIRST_,CJK_IDEOGRAPH_FOURTH_,CJK_IDEOGRAPH_HUNDRED_,CJK_IDEOGRAPH_HUNDRED_MILLION_,CJK_IDEOGRAPH_NINETH_,CJK_IDEOGRAPH_SECOND_,CJK_IDEOGRAPH_SEVENTH_,CJK_IDEOGRAPH_SIXTH_,CJK_IDEOGRAPH_TEN_,CJK_IDEOGRAPH_TEN_THOUSAND_,CJK_IDEOGRAPH_THIRD_,CJK_IDEOGRAPH_THOUSAND_,DECOMPOSITION_TYPE_MASK_,DELETE_,EAST_ASIAN_MASK_,EAST_ASIAN_SHIFT_,FIGURE_SPACE_,GCB_MASK,GCB_SHIFT,IDEOGRAPHIC_NUMBER_ZERO_,LAST_CHAR_MASK_,LB_MASK,LB_SHIFT,LB_VWORD,NARROW_NO_BREAK_SPACE_,NO_BREAK_SPACE_,NTV_DECIMAL_START_,NTV_DIGIT_START_,NTV_FRACTION_START_,NTV_LARGE_START_,NTV_NONE_,NTV_NUMERIC_START_,NTV_RESERVED_START_,NUMERIC_TYPE_VALUE_SHIFT_,SB_MASK,SB_SHIFT,SCRIPT_MASK_,UNIT_SEPARATOR_,WB_MASK,WB_SHIFT,gcbToHst
hcls StringContextIterator

CLSS public abstract interface static com.ibm.icu.lang.UCharacter$DecompositionType
 outer com.ibm.icu.lang.UCharacter
fld public final static int CANONICAL = 1
fld public final static int CIRCLE = 3
fld public final static int COMPAT = 2
fld public final static int COUNT = 18
fld public final static int FINAL = 4
fld public final static int FONT = 5
fld public final static int FRACTION = 6
fld public final static int INITIAL = 7
fld public final static int ISOLATED = 8
fld public final static int MEDIAL = 9
fld public final static int NARROW = 10
fld public final static int NOBREAK = 11
fld public final static int NONE = 0
fld public final static int SMALL = 12
fld public final static int SQUARE = 13
fld public final static int SUB = 14
fld public final static int SUPER = 15
fld public final static int VERTICAL = 16
fld public final static int WIDE = 17

CLSS public abstract interface static com.ibm.icu.lang.UCharacter$EastAsianWidth
 outer com.ibm.icu.lang.UCharacter
fld public final static int AMBIGUOUS = 1
fld public final static int COUNT = 6
fld public final static int FULLWIDTH = 3
fld public final static int HALFWIDTH = 2
fld public final static int NARROW = 4
fld public final static int NEUTRAL = 0
fld public final static int WIDE = 5

CLSS public abstract interface static com.ibm.icu.lang.UCharacter$GraphemeClusterBreak
 outer com.ibm.icu.lang.UCharacter
fld public final static int CONTROL = 1
fld public final static int COUNT = 12
fld public final static int CR = 2
fld public final static int EXTEND = 3
fld public final static int L = 4
fld public final static int LF = 5
fld public final static int LV = 6
fld public final static int LVT = 7
fld public final static int OTHER = 0
fld public final static int PREPEND = 11
fld public final static int SPACING_MARK = 10
fld public final static int T = 8
fld public final static int V = 9

CLSS public abstract interface static com.ibm.icu.lang.UCharacter$HangulSyllableType
 outer com.ibm.icu.lang.UCharacter
fld public final static int COUNT = 6
fld public final static int LEADING_JAMO = 1
fld public final static int LVT_SYLLABLE = 5
fld public final static int LV_SYLLABLE = 4
fld public final static int NOT_APPLICABLE = 0
fld public final static int TRAILING_JAMO = 3
fld public final static int VOWEL_JAMO = 2

CLSS public abstract interface static com.ibm.icu.lang.UCharacter$JoiningGroup
 outer com.ibm.icu.lang.UCharacter
fld public final static int AIN = 1
fld public final static int ALAPH = 2
fld public final static int ALEF = 3
fld public final static int BEH = 4
fld public final static int BETH = 5
fld public final static int BURUSHASKI_YEH_BARREE = 54
fld public final static int COUNT = 57
fld public final static int DAL = 6
fld public final static int DALATH_RISH = 7
fld public final static int E = 8
fld public final static int FARSI_YEH = 55
fld public final static int FE = 51
fld public final static int FEH = 9
fld public final static int FINAL_SEMKATH = 10
fld public final static int GAF = 11
fld public final static int GAMAL = 12
fld public final static int HAH = 13
fld public final static int HAMZA_ON_HEH_GOAL = 14
fld public final static int HE = 15
fld public final static int HEH = 16
fld public final static int HEH_GOAL = 17
fld public final static int HETH = 18
fld public final static int KAF = 19
fld public final static int KAPH = 20
fld public final static int KHAPH = 52
fld public final static int KNOTTED_HEH = 21
fld public final static int LAM = 22
fld public final static int LAMADH = 23
fld public final static int MEEM = 24
fld public final static int MIM = 25
fld public final static int NOON = 26
fld public final static int NO_JOINING_GROUP = 0
fld public final static int NUN = 27
fld public final static int NYA = 56
fld public final static int PE = 28
fld public final static int QAF = 29
fld public final static int QAPH = 30
fld public final static int REH = 31
fld public final static int REVERSED_PE = 32
fld public final static int SAD = 33
fld public final static int SADHE = 34
fld public final static int SEEN = 35
fld public final static int SEMKATH = 36
fld public final static int SHIN = 37
fld public final static int SWASH_KAF = 38
fld public final static int SYRIAC_WAW = 39
fld public final static int TAH = 40
fld public final static int TAW = 41
fld public final static int TEH_MARBUTA = 42
fld public final static int TETH = 43
fld public final static int WAW = 44
fld public final static int YEH = 45
fld public final static int YEH_BARREE = 46
fld public final static int YEH_WITH_TAIL = 47
fld public final static int YUDH = 48
fld public final static int YUDH_HE = 49
fld public final static int ZAIN = 50
fld public final static int ZHAIN = 53

CLSS public abstract interface static com.ibm.icu.lang.UCharacter$JoiningType
 outer com.ibm.icu.lang.UCharacter
fld public final static int COUNT = 6
fld public final static int DUAL_JOINING = 2
fld public final static int JOIN_CAUSING = 1
fld public final static int LEFT_JOINING = 3
fld public final static int NON_JOINING = 0
fld public final static int RIGHT_JOINING = 4
fld public final static int TRANSPARENT = 5

CLSS public abstract interface static com.ibm.icu.lang.UCharacter$LineBreak
 outer com.ibm.icu.lang.UCharacter
fld public final static int ALPHABETIC = 2
fld public final static int AMBIGUOUS = 1
fld public final static int BREAK_AFTER = 4
fld public final static int BREAK_BEFORE = 5
fld public final static int BREAK_BOTH = 3
fld public final static int BREAK_SYMBOLS = 27
fld public final static int CARRIAGE_RETURN = 10
fld public final static int CLOSE_PARENTHESIS = 36
fld public final static int CLOSE_PUNCTUATION = 8
fld public final static int COMBINING_MARK = 9
fld public final static int COMPLEX_CONTEXT = 24
fld public final static int CONTINGENT_BREAK = 7
fld public final static int COUNT = 37
fld public final static int EXCLAMATION = 11
fld public final static int GLUE = 12
fld public final static int H2 = 31
fld public final static int H3 = 32
fld public final static int HYPHEN = 13
fld public final static int IDEOGRAPHIC = 14
fld public final static int INFIX_NUMERIC = 16
fld public final static int INSEPARABLE = 15
fld public final static int INSEPERABLE = 15
fld public final static int JL = 33
fld public final static int JT = 34
fld public final static int JV = 35
fld public final static int LINE_FEED = 17
fld public final static int MANDATORY_BREAK = 6
fld public final static int NEXT_LINE = 29
fld public final static int NONSTARTER = 18
fld public final static int NUMERIC = 19
fld public final static int OPEN_PUNCTUATION = 20
fld public final static int POSTFIX_NUMERIC = 21
fld public final static int PREFIX_NUMERIC = 22
fld public final static int QUOTATION = 23
fld public final static int SPACE = 26
fld public final static int SURROGATE = 25
fld public final static int UNKNOWN = 0
fld public final static int WORD_JOINER = 30
fld public final static int ZWSPACE = 28

CLSS public abstract interface static com.ibm.icu.lang.UCharacter$NumericType
 outer com.ibm.icu.lang.UCharacter
fld public final static int COUNT = 4
fld public final static int DECIMAL = 1
fld public final static int DIGIT = 2
fld public final static int NONE = 0
fld public final static int NUMERIC = 3

CLSS public abstract interface static com.ibm.icu.lang.UCharacter$SentenceBreak
 outer com.ibm.icu.lang.UCharacter
fld public final static int ATERM = 1
fld public final static int CLOSE = 2
fld public final static int COUNT = 15
fld public final static int CR = 11
fld public final static int EXTEND = 12
fld public final static int FORMAT = 3
fld public final static int LF = 13
fld public final static int LOWER = 4
fld public final static int NUMERIC = 5
fld public final static int OLETTER = 6
fld public final static int OTHER = 0
fld public final static int SCONTINUE = 14
fld public final static int SEP = 7
fld public final static int SP = 8
fld public final static int STERM = 9
fld public final static int UPPER = 10

CLSS public final static com.ibm.icu.lang.UCharacter$UnicodeBlock
 outer com.ibm.icu.lang.UCharacter
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock AEGEAN_NUMBERS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock ALPHABETIC_PRESENTATION_FORMS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock ANCIENT_GREEK_MUSICAL_NOTATION
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock ANCIENT_GREEK_NUMBERS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock ANCIENT_SYMBOLS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock ARABIC
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock ARABIC_PRESENTATION_FORMS_A
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock ARABIC_PRESENTATION_FORMS_B
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock ARABIC_SUPPLEMENT
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock ARMENIAN
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock ARROWS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock AVESTAN
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock BALINESE
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock BAMUM
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock BASIC_LATIN
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock BENGALI
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock BLOCK_ELEMENTS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock BOPOMOFO
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock BOPOMOFO_EXTENDED
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock BOX_DRAWING
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock BRAILLE_PATTERNS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock BUGINESE
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock BUHID
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock BYZANTINE_MUSICAL_SYMBOLS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CARIAN
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CHAM
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CHEROKEE
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CJK_COMPATIBILITY
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CJK_COMPATIBILITY_FORMS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CJK_RADICALS_SUPPLEMENT
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CJK_STROKES
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CJK_SYMBOLS_AND_PUNCTUATION
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CJK_UNIFIED_IDEOGRAPHS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock COMBINING_DIACRITICAL_MARKS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock COMBINING_DIACRITICAL_MARKS_SUPPLEMENT
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock COMBINING_HALF_MARKS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock COMBINING_MARKS_FOR_SYMBOLS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock COMMON_INDIC_NUMBER_FORMS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CONTROL_PICTURES
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock COPTIC
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock COUNTING_ROD_NUMERALS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CUNEIFORM
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CUNEIFORM_NUMBERS_AND_PUNCTUATION
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CURRENCY_SYMBOLS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CYPRIOT_SYLLABARY
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CYRILLIC
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CYRILLIC_EXTENDED_A
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CYRILLIC_EXTENDED_B
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CYRILLIC_SUPPLEMENT
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock CYRILLIC_SUPPLEMENTARY
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock DESERET
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock DEVANAGARI
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock DEVANAGARI_EXTENDED
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock DINGBATS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock DOMINO_TILES
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock EGYPTIAN_HIEROGLYPHS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock ENCLOSED_ALPHANUMERICS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock ENCLOSED_ALPHANUMERIC_SUPPLEMENT
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock ENCLOSED_CJK_LETTERS_AND_MONTHS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock ENCLOSED_IDEOGRAPHIC_SUPPLEMENT
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock ETHIOPIC
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock ETHIOPIC_EXTENDED
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock ETHIOPIC_SUPPLEMENT
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock GENERAL_PUNCTUATION
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock GEOMETRIC_SHAPES
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock GEORGIAN
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock GEORGIAN_SUPPLEMENT
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock GLAGOLITIC
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock GOTHIC
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock GREEK
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock GREEK_EXTENDED
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock GUJARATI
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock GURMUKHI
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock HALFWIDTH_AND_FULLWIDTH_FORMS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock HANGUL_COMPATIBILITY_JAMO
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock HANGUL_JAMO
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock HANGUL_JAMO_EXTENDED_A
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock HANGUL_JAMO_EXTENDED_B
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock HANGUL_SYLLABLES
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock HANUNOO
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock HEBREW
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock HIGH_PRIVATE_USE_SURROGATES
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock HIGH_SURROGATES
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock HIRAGANA
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock IDEOGRAPHIC_DESCRIPTION_CHARACTERS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock IMPERIAL_ARAMAIC
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock INSCRIPTIONAL_PAHLAVI
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock INSCRIPTIONAL_PARTHIAN
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock INVALID_CODE
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock IPA_EXTENSIONS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock JAVANESE
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock KAITHI
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock KANBUN
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock KANGXI_RADICALS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock KANNADA
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock KATAKANA
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock KATAKANA_PHONETIC_EXTENSIONS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock KAYAH_LI
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock KHAROSHTHI
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock KHMER
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock KHMER_SYMBOLS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock LAO
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock LATIN_1_SUPPLEMENT
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock LATIN_EXTENDED_A
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock LATIN_EXTENDED_ADDITIONAL
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock LATIN_EXTENDED_B
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock LATIN_EXTENDED_C
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock LATIN_EXTENDED_D
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock LEPCHA
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock LETTERLIKE_SYMBOLS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock LIMBU
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock LINEAR_B_IDEOGRAMS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock LINEAR_B_SYLLABARY
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock LISU
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock LOW_SURROGATES
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock LYCIAN
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock LYDIAN
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock MAHJONG_TILES
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock MALAYALAM
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock MATHEMATICAL_ALPHANUMERIC_SYMBOLS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock MATHEMATICAL_OPERATORS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock MEETEI_MAYEK
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock MISCELLANEOUS_SYMBOLS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock MISCELLANEOUS_SYMBOLS_AND_ARROWS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock MISCELLANEOUS_TECHNICAL
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock MODIFIER_TONE_LETTERS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock MONGOLIAN
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock MUSICAL_SYMBOLS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock MYANMAR
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock MYANMAR_EXTENDED_A
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock NEW_TAI_LUE
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock NKO
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock NO_BLOCK
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock NUMBER_FORMS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock OGHAM
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock OLD_ITALIC
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock OLD_PERSIAN
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock OLD_SOUTH_ARABIAN
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock OLD_TURKIC
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock OL_CHIKI
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock OPTICAL_CHARACTER_RECOGNITION
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock ORIYA
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock OSMANYA
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock PHAGS_PA
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock PHAISTOS_DISC
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock PHOENICIAN
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock PHONETIC_EXTENSIONS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock PHONETIC_EXTENSIONS_SUPPLEMENT
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock PRIVATE_USE
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock PRIVATE_USE_AREA
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock REJANG
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock RUMI_NUMERAL_SYMBOLS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock RUNIC
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock SAMARITAN
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock SAURASHTRA
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock SHAVIAN
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock SINHALA
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock SMALL_FORM_VARIANTS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock SPACING_MODIFIER_LETTERS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock SPECIALS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock SUNDANESE
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock SUPERSCRIPTS_AND_SUBSCRIPTS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock SUPPLEMENTAL_ARROWS_A
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock SUPPLEMENTAL_ARROWS_B
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock SUPPLEMENTAL_MATHEMATICAL_OPERATORS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock SUPPLEMENTAL_PUNCTUATION
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_A
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_B
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock SYLOTI_NAGRI
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock SYRIAC
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock TAGALOG
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock TAGBANWA
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock TAGS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock TAI_LE
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock TAI_THAM
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock TAI_VIET
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock TAI_XUAN_JING_SYMBOLS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock TAMIL
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock TELUGU
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock THAANA
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock THAI
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock TIBETAN
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock TIFINAGH
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock UGARITIC
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock VAI
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock VARIATION_SELECTORS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock VARIATION_SELECTORS_SUPPLEMENT
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock VEDIC_EXTENSIONS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock VERTICAL_FORMS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock YIJING_HEXAGRAM_SYMBOLS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock YI_RADICALS
fld public final static com.ibm.icu.lang.UCharacter$UnicodeBlock YI_SYLLABLES
fld public final static int AEGEAN_NUMBERS_ID = 119
fld public final static int ALPHABETIC_PRESENTATION_FORMS_ID = 80
fld public final static int ANCIENT_GREEK_MUSICAL_NOTATION_ID = 126
fld public final static int ANCIENT_GREEK_NUMBERS_ID = 127
fld public final static int ANCIENT_SYMBOLS_ID = 165
fld public final static int ARABIC_ID = 12
fld public final static int ARABIC_PRESENTATION_FORMS_A_ID = 81
fld public final static int ARABIC_PRESENTATION_FORMS_B_ID = 85
fld public final static int ARABIC_SUPPLEMENT_ID = 128
fld public final static int ARMENIAN_ID = 10
fld public final static int ARROWS_ID = 46
fld public final static int AVESTAN_ID = 188
fld public final static int BALINESE_ID = 147
fld public final static int BAMUM_ID = 177
fld public final static int BASIC_LATIN_ID = 1
fld public final static int BENGALI_ID = 16
fld public final static int BLOCK_ELEMENTS_ID = 53
fld public final static int BOPOMOFO_EXTENDED_ID = 67
fld public final static int BOPOMOFO_ID = 64
fld public final static int BOX_DRAWING_ID = 52
fld public final static int BRAILLE_PATTERNS_ID = 57
fld public final static int BUGINESE_ID = 129
fld public final static int BUHID_ID = 100
fld public final static int BYZANTINE_MUSICAL_SYMBOLS_ID = 91
fld public final static int CARIAN_ID = 168
fld public final static int CHAM_ID = 164
fld public final static int CHEROKEE_ID = 32
fld public final static int CJK_COMPATIBILITY_FORMS_ID = 83
fld public final static int CJK_COMPATIBILITY_ID = 69
fld public final static int CJK_COMPATIBILITY_IDEOGRAPHS_ID = 79
fld public final static int CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT_ID = 95
fld public final static int CJK_RADICALS_SUPPLEMENT_ID = 58
fld public final static int CJK_STROKES_ID = 130
fld public final static int CJK_SYMBOLS_AND_PUNCTUATION_ID = 61
fld public final static int CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A_ID = 70
fld public final static int CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B_ID = 94
fld public final static int CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C_ID = 197
fld public final static int CJK_UNIFIED_IDEOGRAPHS_ID = 71
fld public final static int COMBINING_DIACRITICAL_MARKS_ID = 7
fld public final static int COMBINING_DIACRITICAL_MARKS_SUPPLEMENT_ID = 131
fld public final static int COMBINING_HALF_MARKS_ID = 82
fld public final static int COMBINING_MARKS_FOR_SYMBOLS_ID = 43
fld public final static int COMMON_INDIC_NUMBER_FORMS_ID = 178
fld public final static int CONTROL_PICTURES_ID = 49
fld public final static int COPTIC_ID = 132
fld public final static int COUNT = 198
fld public final static int COUNTING_ROD_NUMERALS_ID = 154
fld public final static int CUNEIFORM_ID = 152
fld public final static int CUNEIFORM_NUMBERS_AND_PUNCTUATION_ID = 153
fld public final static int CURRENCY_SYMBOLS_ID = 42
fld public final static int CYPRIOT_SYLLABARY_ID = 123
fld public final static int CYRILLIC_EXTENDED_A_ID = 158
fld public final static int CYRILLIC_EXTENDED_B_ID = 160
fld public final static int CYRILLIC_ID = 9
fld public final static int CYRILLIC_SUPPLEMENTARY_ID = 97
fld public final static int CYRILLIC_SUPPLEMENT_ID = 97
fld public final static int DESERET_ID = 90
fld public final static int DEVANAGARI_EXTENDED_ID = 179
fld public final static int DEVANAGARI_ID = 15
fld public final static int DINGBATS_ID = 56
fld public final static int DOMINO_TILES_ID = 171
fld public final static int EGYPTIAN_HIEROGLYPHS_ID = 194
fld public final static int ENCLOSED_ALPHANUMERICS_ID = 51
fld public final static int ENCLOSED_ALPHANUMERIC_SUPPLEMENT_ID = 195
fld public final static int ENCLOSED_CJK_LETTERS_AND_MONTHS_ID = 68
fld public final static int ENCLOSED_IDEOGRAPHIC_SUPPLEMENT_ID = 196
fld public final static int ETHIOPIC_EXTENDED_ID = 133
fld public final static int ETHIOPIC_ID = 31
fld public final static int ETHIOPIC_SUPPLEMENT_ID = 134
fld public final static int GENERAL_PUNCTUATION_ID = 40
fld public final static int GEOMETRIC_SHAPES_ID = 54
fld public final static int GEORGIAN_ID = 29
fld public final static int GEORGIAN_SUPPLEMENT_ID = 135
fld public final static int GLAGOLITIC_ID = 136
fld public final static int GOTHIC_ID = 89
fld public final static int GREEK_EXTENDED_ID = 39
fld public final static int GREEK_ID = 8
fld public final static int GUJARATI_ID = 18
fld public final static int GURMUKHI_ID = 17
fld public final static int HALFWIDTH_AND_FULLWIDTH_FORMS_ID = 87
fld public final static int HANGUL_COMPATIBILITY_JAMO_ID = 65
fld public final static int HANGUL_JAMO_EXTENDED_A_ID = 180
fld public final static int HANGUL_JAMO_EXTENDED_B_ID = 185
fld public final static int HANGUL_JAMO_ID = 30
fld public final static int HANGUL_SYLLABLES_ID = 74
fld public final static int HANUNOO_ID = 99
fld public final static int HEBREW_ID = 11
fld public final static int HIGH_PRIVATE_USE_SURROGATES_ID = 76
fld public final static int HIGH_SURROGATES_ID = 75
fld public final static int HIRAGANA_ID = 62
fld public final static int IDEOGRAPHIC_DESCRIPTION_CHARACTERS_ID = 60
fld public final static int IMPERIAL_ARAMAIC_ID = 186
fld public final static int INSCRIPTIONAL_PAHLAVI_ID = 190
fld public final static int INSCRIPTIONAL_PARTHIAN_ID = 189
fld public final static int INVALID_CODE_ID = -1
fld public final static int IPA_EXTENSIONS_ID = 5
fld public final static int JAVANESE_ID = 181
fld public final static int KAITHI_ID = 193
fld public final static int KANBUN_ID = 66
fld public final static int KANGXI_RADICALS_ID = 59
fld public final static int KANNADA_ID = 22
fld public final static int KATAKANA_ID = 63
fld public final static int KATAKANA_PHONETIC_EXTENSIONS_ID = 107
fld public final static int KAYAH_LI_ID = 162
fld public final static int KHAROSHTHI_ID = 137
fld public final static int KHMER_ID = 36
fld public final static int KHMER_SYMBOLS_ID = 113
fld public final static int LAO_ID = 26
fld public final static int LATIN_1_SUPPLEMENT_ID = 2
fld public final static int LATIN_EXTENDED_ADDITIONAL_ID = 38
fld public final static int LATIN_EXTENDED_A_ID = 3
fld public final static int LATIN_EXTENDED_B_ID = 4
fld public final static int LATIN_EXTENDED_C_ID = 148
fld public final static int LATIN_EXTENDED_D_ID = 149
fld public final static int LEPCHA_ID = 156
fld public final static int LETTERLIKE_SYMBOLS_ID = 44
fld public final static int LIMBU_ID = 111
fld public final static int LINEAR_B_IDEOGRAMS_ID = 118
fld public final static int LINEAR_B_SYLLABARY_ID = 117
fld public final static int LISU_ID = 176
fld public final static int LOW_SURROGATES_ID = 77
fld public final static int LYCIAN_ID = 167
fld public final static int LYDIAN_ID = 169
fld public final static int MAHJONG_TILES_ID = 170
fld public final static int MALAYALAM_ID = 23
fld public final static int MATHEMATICAL_ALPHANUMERIC_SYMBOLS_ID = 93
fld public final static int MATHEMATICAL_OPERATORS_ID = 47
fld public final static int MEETEI_MAYEK_ID = 184
fld public final static int MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A_ID = 102
fld public final static int MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B_ID = 105
fld public final static int MISCELLANEOUS_SYMBOLS_AND_ARROWS_ID = 115
fld public final static int MISCELLANEOUS_SYMBOLS_ID = 55
fld public final static int MISCELLANEOUS_TECHNICAL_ID = 48
fld public final static int MODIFIER_TONE_LETTERS_ID = 138
fld public final static int MONGOLIAN_ID = 37
fld public final static int MUSICAL_SYMBOLS_ID = 92
fld public final static int MYANMAR_EXTENDED_A_ID = 182
fld public final static int MYANMAR_ID = 28
fld public final static int NEW_TAI_LUE_ID = 139
fld public final static int NKO_ID = 146
fld public final static int NUMBER_FORMS_ID = 45
fld public final static int OGHAM_ID = 34
fld public final static int OLD_ITALIC_ID = 88
fld public final static int OLD_PERSIAN_ID = 140
fld public final static int OLD_SOUTH_ARABIAN_ID = 187
fld public final static int OLD_TURKIC_ID = 191
fld public final static int OL_CHIKI_ID = 157
fld public final static int OPTICAL_CHARACTER_RECOGNITION_ID = 50
fld public final static int ORIYA_ID = 19
fld public final static int OSMANYA_ID = 122
fld public final static int PHAGS_PA_ID = 150
fld public final static int PHAISTOS_DISC_ID = 166
fld public final static int PHOENICIAN_ID = 151
fld public final static int PHONETIC_EXTENSIONS_ID = 114
fld public final static int PHONETIC_EXTENSIONS_SUPPLEMENT_ID = 141
fld public final static int PRIVATE_USE_AREA_ID = 78
fld public final static int PRIVATE_USE_ID = 78
fld public final static int REJANG_ID = 163
fld public final static int RUMI_NUMERAL_SYMBOLS_ID = 192
fld public final static int RUNIC_ID = 35
fld public final static int SAMARITAN_ID = 172
fld public final static int SAURASHTRA_ID = 161
fld public final static int SHAVIAN_ID = 121
fld public final static int SINHALA_ID = 24
fld public final static int SMALL_FORM_VARIANTS_ID = 84
fld public final static int SPACING_MODIFIER_LETTERS_ID = 6
fld public final static int SPECIALS_ID = 86
fld public final static int SUNDANESE_ID = 155
fld public final static int SUPERSCRIPTS_AND_SUBSCRIPTS_ID = 41
fld public final static int SUPPLEMENTAL_ARROWS_A_ID = 103
fld public final static int SUPPLEMENTAL_ARROWS_B_ID = 104
fld public final static int SUPPLEMENTAL_MATHEMATICAL_OPERATORS_ID = 106
fld public final static int SUPPLEMENTAL_PUNCTUATION_ID = 142
fld public final static int SUPPLEMENTARY_PRIVATE_USE_AREA_A_ID = 109
fld public final static int SUPPLEMENTARY_PRIVATE_USE_AREA_B_ID = 110
fld public final static int SYLOTI_NAGRI_ID = 143
fld public final static int SYRIAC_ID = 13
fld public final static int TAGALOG_ID = 98
fld public final static int TAGBANWA_ID = 101
fld public final static int TAGS_ID = 96
fld public final static int TAI_LE_ID = 112
fld public final static int TAI_THAM_ID = 174
fld public final static int TAI_VIET_ID = 183
fld public final static int TAI_XUAN_JING_SYMBOLS_ID = 124
fld public final static int TAMIL_ID = 20
fld public final static int TELUGU_ID = 21
fld public final static int THAANA_ID = 14
fld public final static int THAI_ID = 25
fld public final static int TIBETAN_ID = 27
fld public final static int TIFINAGH_ID = 144
fld public final static int UGARITIC_ID = 120
fld public final static int UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED_ID = 173
fld public final static int UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_ID = 33
fld public final static int VAI_ID = 159
fld public final static int VARIATION_SELECTORS_ID = 108
fld public final static int VARIATION_SELECTORS_SUPPLEMENT_ID = 125
fld public final static int VEDIC_EXTENSIONS_ID = 175
fld public final static int VERTICAL_FORMS_ID = 145
fld public final static int YIJING_HEXAGRAM_SYMBOLS_ID = 116
fld public final static int YI_RADICALS_ID = 73
fld public final static int YI_SYLLABLES_ID = 72
meth public final static com.ibm.icu.lang.UCharacter$UnicodeBlock forName(java.lang.String)
meth public int getID()
meth public static com.ibm.icu.lang.UCharacter$UnicodeBlock getInstance(int)
meth public static com.ibm.icu.lang.UCharacter$UnicodeBlock of(int)
supr java.lang.Character$Subset
hfds BLOCKS_,m_id_,mref

CLSS public abstract interface static com.ibm.icu.lang.UCharacter$WordBreak
 outer com.ibm.icu.lang.UCharacter
fld public final static int ALETTER = 1
fld public final static int COUNT = 13
fld public final static int CR = 8
fld public final static int EXTEND = 9
fld public final static int EXTENDNUMLET = 7
fld public final static int FORMAT = 2
fld public final static int KATAKANA = 3
fld public final static int LF = 10
fld public final static int MIDLETTER = 4
fld public final static int MIDNUM = 5
fld public final static int MIDNUMLET = 11
fld public final static int NEWLINE = 12
fld public final static int NUMERIC = 6
fld public final static int OTHER = 0

CLSS public final com.ibm.icu.lang.UCharacterCategory
intf com.ibm.icu.lang.UCharacterEnums$ECharacterCategory
meth public static java.lang.String toString(int)
supr java.lang.Object

CLSS public final com.ibm.icu.lang.UCharacterDirection
intf com.ibm.icu.lang.UCharacterEnums$ECharacterDirection
meth public static java.lang.String toString(int)
supr java.lang.Object

CLSS public com.ibm.icu.lang.UCharacterEnums
innr public abstract interface static ECharacterCategory
innr public abstract interface static ECharacterDirection
supr java.lang.Object

CLSS public abstract interface static com.ibm.icu.lang.UCharacterEnums$ECharacterCategory
 outer com.ibm.icu.lang.UCharacterEnums
fld public final static byte CHAR_CATEGORY_COUNT = 30
fld public final static byte COMBINING_SPACING_MARK = 8
fld public final static byte CONNECTOR_PUNCTUATION = 22
fld public final static byte CONTROL = 15
fld public final static byte CURRENCY_SYMBOL = 25
fld public final static byte DASH_PUNCTUATION = 19
fld public final static byte DECIMAL_DIGIT_NUMBER = 9
fld public final static byte ENCLOSING_MARK = 7
fld public final static byte END_PUNCTUATION = 21
fld public final static byte FINAL_PUNCTUATION = 29
fld public final static byte FINAL_QUOTE_PUNCTUATION = 29
fld public final static byte FORMAT = 16
fld public final static byte GENERAL_OTHER_TYPES = 0
fld public final static byte INITIAL_PUNCTUATION = 28
fld public final static byte INITIAL_QUOTE_PUNCTUATION = 28
fld public final static byte LETTER_NUMBER = 10
fld public final static byte LINE_SEPARATOR = 13
fld public final static byte LOWERCASE_LETTER = 2
fld public final static byte MATH_SYMBOL = 24
fld public final static byte MODIFIER_LETTER = 4
fld public final static byte MODIFIER_SYMBOL = 26
fld public final static byte NON_SPACING_MARK = 6
fld public final static byte OTHER_LETTER = 5
fld public final static byte OTHER_NUMBER = 11
fld public final static byte OTHER_PUNCTUATION = 23
fld public final static byte OTHER_SYMBOL = 27
fld public final static byte PARAGRAPH_SEPARATOR = 14
fld public final static byte PRIVATE_USE = 17
fld public final static byte SPACE_SEPARATOR = 12
fld public final static byte START_PUNCTUATION = 20
fld public final static byte SURROGATE = 18
fld public final static byte TITLECASE_LETTER = 3
fld public final static byte UNASSIGNED = 0
fld public final static byte UPPERCASE_LETTER = 1

CLSS public abstract interface static com.ibm.icu.lang.UCharacterEnums$ECharacterDirection
 outer com.ibm.icu.lang.UCharacterEnums
fld public final static byte DIRECTIONALITY_ARABIC_NUMBER = 5
fld public final static byte DIRECTIONALITY_BOUNDARY_NEUTRAL = 18
fld public final static byte DIRECTIONALITY_COMMON_NUMBER_SEPARATOR = 6
fld public final static byte DIRECTIONALITY_EUROPEAN_NUMBER = 2
fld public final static byte DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR = 3
fld public final static byte DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR = 4
fld public final static byte DIRECTIONALITY_LEFT_TO_RIGHT = 0
fld public final static byte DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING = 11
fld public final static byte DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE = 12
fld public final static byte DIRECTIONALITY_NONSPACING_MARK = 17
fld public final static byte DIRECTIONALITY_OTHER_NEUTRALS = 10
fld public final static byte DIRECTIONALITY_PARAGRAPH_SEPARATOR = 7
fld public final static byte DIRECTIONALITY_POP_DIRECTIONAL_FORMAT = 16
fld public final static byte DIRECTIONALITY_RIGHT_TO_LEFT = 1
fld public final static byte DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC = 13
fld public final static byte DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING = 14
fld public final static byte DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE = 15
fld public final static byte DIRECTIONALITY_SEGMENT_SEPARATOR = 8
fld public final static byte DIRECTIONALITY_UNDEFINED = -1
fld public final static byte DIRECTIONALITY_WHITESPACE = 9
fld public final static int ARABIC_NUMBER = 5
fld public final static int BLOCK_SEPARATOR = 7
fld public final static int BOUNDARY_NEUTRAL = 18
fld public final static int CHAR_DIRECTION_COUNT = 19
fld public final static int COMMON_NUMBER_SEPARATOR = 6
fld public final static int DIR_NON_SPACING_MARK = 17
fld public final static int EUROPEAN_NUMBER = 2
fld public final static int EUROPEAN_NUMBER_SEPARATOR = 3
fld public final static int EUROPEAN_NUMBER_TERMINATOR = 4
fld public final static int LEFT_TO_RIGHT = 0
fld public final static int LEFT_TO_RIGHT_EMBEDDING = 11
fld public final static int LEFT_TO_RIGHT_OVERRIDE = 12
fld public final static int OTHER_NEUTRAL = 10
fld public final static int POP_DIRECTIONAL_FORMAT = 16
fld public final static int RIGHT_TO_LEFT = 1
fld public final static int RIGHT_TO_LEFT_ARABIC = 13
fld public final static int RIGHT_TO_LEFT_EMBEDDING = 14
fld public final static int RIGHT_TO_LEFT_OVERRIDE = 15
fld public final static int SEGMENT_SEPARATOR = 8
fld public final static int WHITE_SPACE_NEUTRAL = 9

CLSS public abstract interface com.ibm.icu.lang.UProperty
fld public final static int AGE = 16384
fld public final static int ALPHABETIC = 0
fld public final static int ASCII_HEX_DIGIT = 1
fld public final static int BIDI_CLASS = 4096
fld public final static int BIDI_CONTROL = 2
fld public final static int BIDI_MIRRORED = 3
fld public final static int BIDI_MIRRORING_GLYPH = 16385
fld public final static int BINARY_LIMIT = 57
fld public final static int BINARY_START = 0
fld public final static int BLOCK = 4097
fld public final static int CANONICAL_COMBINING_CLASS = 4098
fld public final static int CASED = 49
fld public final static int CASE_FOLDING = 16386
fld public final static int CASE_IGNORABLE = 50
fld public final static int CASE_SENSITIVE = 34
fld public final static int CHANGES_WHEN_CASEFOLDED = 54
fld public final static int CHANGES_WHEN_CASEMAPPED = 55
fld public final static int CHANGES_WHEN_LOWERCASED = 51
fld public final static int CHANGES_WHEN_NFKC_CASEFOLDED = 56
fld public final static int CHANGES_WHEN_TITLECASED = 53
fld public final static int CHANGES_WHEN_UPPERCASED = 52
fld public final static int DASH = 4
fld public final static int DECOMPOSITION_TYPE = 4099
fld public final static int DEFAULT_IGNORABLE_CODE_POINT = 5
fld public final static int DEPRECATED = 6
fld public final static int DIACRITIC = 7
fld public final static int DOUBLE_LIMIT = 12289
fld public final static int DOUBLE_START = 12288
fld public final static int EAST_ASIAN_WIDTH = 4100
fld public final static int EXTENDER = 8
fld public final static int FULL_COMPOSITION_EXCLUSION = 9
fld public final static int GENERAL_CATEGORY = 4101
fld public final static int GENERAL_CATEGORY_MASK = 8192
fld public final static int GRAPHEME_BASE = 10
fld public final static int GRAPHEME_CLUSTER_BREAK = 4114
fld public final static int GRAPHEME_EXTEND = 11
fld public final static int GRAPHEME_LINK = 12
fld public final static int HANGUL_SYLLABLE_TYPE = 4107
fld public final static int HEX_DIGIT = 13
fld public final static int HYPHEN = 14
fld public final static int IDEOGRAPHIC = 17
fld public final static int IDS_BINARY_OPERATOR = 18
fld public final static int IDS_TRINARY_OPERATOR = 19
fld public final static int ID_CONTINUE = 15
fld public final static int ID_START = 16
fld public final static int INT_LIMIT = 4117
fld public final static int INT_START = 4096
fld public final static int ISO_COMMENT = 16387
fld public final static int JOINING_GROUP = 4102
fld public final static int JOINING_TYPE = 4103
fld public final static int JOIN_CONTROL = 20
fld public final static int LEAD_CANONICAL_COMBINING_CLASS = 4112
fld public final static int LINE_BREAK = 4104
fld public final static int LOGICAL_ORDER_EXCEPTION = 21
fld public final static int LOWERCASE = 22
fld public final static int LOWERCASE_MAPPING = 16388
fld public final static int MASK_LIMIT = 8193
fld public final static int MASK_START = 8192
fld public final static int MATH = 23
fld public final static int NAME = 16389
fld public final static int NFC_INERT = 39
fld public final static int NFC_QUICK_CHECK = 4110
fld public final static int NFD_INERT = 37
fld public final static int NFD_QUICK_CHECK = 4108
fld public final static int NFKC_INERT = 40
fld public final static int NFKC_QUICK_CHECK = 4111
fld public final static int NFKD_INERT = 38
fld public final static int NFKD_QUICK_CHECK = 4109
fld public final static int NONCHARACTER_CODE_POINT = 24
fld public final static int NUMERIC_TYPE = 4105
fld public final static int NUMERIC_VALUE = 12288
fld public final static int PATTERN_SYNTAX = 42
fld public final static int PATTERN_WHITE_SPACE = 43
fld public final static int POSIX_ALNUM = 44
fld public final static int POSIX_BLANK = 45
fld public final static int POSIX_GRAPH = 46
fld public final static int POSIX_PRINT = 47
fld public final static int POSIX_XDIGIT = 48
fld public final static int QUOTATION_MARK = 25
fld public final static int RADICAL = 26
fld public final static int SCRIPT = 4106
fld public final static int SEGMENT_STARTER = 41
fld public final static int SENTENCE_BREAK = 4115
fld public final static int SIMPLE_CASE_FOLDING = 16390
fld public final static int SIMPLE_LOWERCASE_MAPPING = 16391
fld public final static int SIMPLE_TITLECASE_MAPPING = 16392
fld public final static int SIMPLE_UPPERCASE_MAPPING = 16393
fld public final static int SOFT_DOTTED = 27
fld public final static int STRING_LIMIT = 16397
fld public final static int STRING_START = 16384
fld public final static int S_TERM = 35
fld public final static int TERMINAL_PUNCTUATION = 28
fld public final static int TITLECASE_MAPPING = 16394
fld public final static int TRAIL_CANONICAL_COMBINING_CLASS = 4113
fld public final static int UNDEFINED = -1
fld public final static int UNICODE_1_NAME = 16395
fld public final static int UNIFIED_IDEOGRAPH = 29
fld public final static int UPPERCASE = 30
fld public final static int UPPERCASE_MAPPING = 16396
fld public final static int VARIATION_SELECTOR = 36
fld public final static int WHITE_SPACE = 31
fld public final static int WORD_BREAK = 4116
fld public final static int XID_CONTINUE = 32
fld public final static int XID_START = 33
innr public abstract interface static NameChoice

CLSS public abstract interface static com.ibm.icu.lang.UProperty$NameChoice
 outer com.ibm.icu.lang.UProperty
fld public final static int COUNT = 2
fld public final static int LONG = 1
fld public final static int SHORT = 0

CLSS public final com.ibm.icu.lang.UScript
fld public final static int ARABIC = 2
fld public final static int ARMENIAN = 3
fld public final static int AVESTAN = 117
fld public final static int BALINESE = 62
fld public final static int BAMUM = 130
fld public final static int BATAK = 63
fld public final static int BENGALI = 4
fld public final static int BLISSYMBOLS = 64
fld public final static int BOOK_PAHLAVI = 124
fld public final static int BOPOMOFO = 5
fld public final static int BRAHMI = 65
fld public final static int BRAILLE = 46
fld public final static int BUGINESE = 55
fld public final static int BUHID = 44
fld public final static int CANADIAN_ABORIGINAL = 40
fld public final static int CARIAN = 104
fld public final static int CHAKMA = 118
fld public final static int CHAM = 66
fld public final static int CHEROKEE = 6
fld public final static int CIRTH = 67
fld public final static int CODE_LIMIT = 134
fld public final static int COMMON = 0
fld public final static int COPTIC = 7
fld public final static int CUNEIFORM = 101
fld public final static int CYPRIOT = 47
fld public final static int CYRILLIC = 8
fld public final static int DEMOTIC_EGYPTIAN = 69
fld public final static int DESERET = 9
fld public final static int DEVANAGARI = 10
fld public final static int EASTERN_SYRIAC = 97
fld public final static int EGYPTIAN_HIEROGLYPHS = 71
fld public final static int ESTRANGELO_SYRIAC = 95
fld public final static int ETHIOPIC = 11
fld public final static int GEORGIAN = 12
fld public final static int GLAGOLITIC = 56
fld public final static int GOTHIC = 13
fld public final static int GREEK = 14
fld public final static int GUJARATI = 15
fld public final static int GURMUKHI = 16
fld public final static int HAN = 17
fld public final static int HANGUL = 18
fld public final static int HANUNOO = 43
fld public final static int HARAPPAN_INDUS = 77
fld public final static int HEBREW = 19
fld public final static int HIERATIC_EGYPTIAN = 70
fld public final static int HIRAGANA = 20
fld public final static int IMPERIAL_ARAMAIC = 116
fld public final static int INHERITED = 1
fld public final static int INSCRIPTIONAL_PAHLAVI = 122
fld public final static int INSCRIPTIONAL_PARTHIAN = 125
fld public final static int INVALID_CODE = -1
fld public final static int JAPANESE = 105
fld public final static int JAVANESE = 78
fld public final static int KAITHI = 120
fld public final static int KANNADA = 21
fld public final static int KATAKANA = 22
fld public final static int KATAKANA_OR_HIRAGANA = 54
fld public final static int KAYAH_LI = 79
fld public final static int KHAROSHTHI = 57
fld public final static int KHMER = 23
fld public final static int KHUTSURI = 72
fld public final static int KOREAN = 119
fld public final static int LANNA = 106
fld public final static int LAO = 24
fld public final static int LATIN = 25
fld public final static int LATIN_FRAKTUR = 80
fld public final static int LATIN_GAELIC = 81
fld public final static int LEPCHA = 82
fld public final static int LIMBU = 48
fld public final static int LINEAR_A = 83
fld public final static int LINEAR_B = 49
fld public final static int LISU = 131
fld public final static int LYCIAN = 107
fld public final static int LYDIAN = 108
fld public final static int MALAYALAM = 26
fld public final static int MANDAEAN = 84
fld public final static int MANICHAEAN = 121
fld public final static int MATHEMATICAL_NOTATION = 128
fld public final static int MAYAN_HIEROGLYPHS = 85
fld public final static int MEITEI_MAYEK = 115
fld public final static int MEROITIC = 86
fld public final static int MONGOLIAN = 27
fld public final static int MOON = 114
fld public final static int MYANMAR = 28
fld public final static int NAKHI_GEBA = 132
fld public final static int NEW_TAI_LUE = 59
fld public final static int NKO = 87
fld public final static int OGHAM = 29
fld public final static int OLD_CHURCH_SLAVONIC_CYRILLIC = 68
fld public final static int OLD_HUNGARIAN = 76
fld public final static int OLD_ITALIC = 30
fld public final static int OLD_PERMIC = 89
fld public final static int OLD_PERSIAN = 61
fld public final static int OLD_SOUTH_ARABIAN = 133
fld public final static int OL_CHIKI = 109
fld public final static int ORIYA = 31
fld public final static int ORKHON = 88
fld public final static int OSMANYA = 50
fld public final static int PAHAWH_HMONG = 75
fld public final static int PHAGS_PA = 90
fld public final static int PHOENICIAN = 91
fld public final static int PHONETIC_POLLARD = 92
fld public final static int PSALTER_PAHLAVI = 123
fld public final static int REJANG = 110
fld public final static int RONGORONGO = 93
fld public final static int RUNIC = 32
fld public final static int SAMARITAN = 126
fld public final static int SARATI = 94
fld public final static int SAURASHTRA = 111
fld public final static int SHAVIAN = 51
fld public final static int SIGN_WRITING = 112
fld public final static int SIMPLIFIED_HAN = 73
fld public final static int SINHALA = 33
fld public final static int SUNDANESE = 113
fld public final static int SYLOTI_NAGRI = 58
fld public final static int SYMBOLS = 129
fld public final static int SYRIAC = 34
fld public final static int TAGALOG = 42
fld public final static int TAGBANWA = 45
fld public final static int TAI_LE = 52
fld public final static int TAI_VIET = 127
fld public final static int TAMIL = 35
fld public final static int TELUGU = 36
fld public final static int TENGWAR = 98
fld public final static int THAANA = 37
fld public final static int THAI = 38
fld public final static int TIBETAN = 39
fld public final static int TIFINAGH = 60
fld public final static int TRADITIONAL_HAN = 74
fld public final static int UCAS = 40
fld public final static int UGARITIC = 53
fld public final static int UNKNOWN = 103
fld public final static int UNWRITTEN_LANGUAGES = 102
fld public final static int VAI = 99
fld public final static int VISIBLE_SPEECH = 100
fld public final static int WESTERN_SYRIAC = 96
fld public final static int YI = 41
meth public final static int getCodeFromName(java.lang.String)
meth public final static int getScript(int)
meth public final static int[] getCode(com.ibm.icu.util.ULocale)
meth public final static int[] getCode(java.lang.String)
meth public final static int[] getCode(java.util.Locale)
meth public final static java.lang.String getName(int)
meth public final static java.lang.String getShortName(int)
supr java.lang.Object
hfds kLocaleScript

CLSS public final com.ibm.icu.lang.UScriptRun
cons public init()
cons public init(char[])
cons public init(char[],int,int)
cons public init(java.lang.String)
cons public init(java.lang.String,int,int)
meth public final boolean next()
meth public final int getScriptCode()
meth public final int getScriptLimit()
meth public final int getScriptStart()
meth public final void reset()
meth public final void reset(char[])
meth public final void reset(char[],int,int)
meth public final void reset(int,int)
meth public final void reset(java.lang.String)
meth public final void reset(java.lang.String,int,int)
supr java.lang.Object
hfds PAREN_STACK_DEPTH,emptyCharArray,fixupCount,pairedCharExtra,pairedCharPower,pairedChars,parenSP,parenStack,pushCount,scriptCode,scriptLimit,scriptStart,text,textIndex,textLimit,textStart
hcls ParenStackEntry

CLSS public com.ibm.icu.math.BigDecimal
cons public init(char[])
cons public init(char[],int,int)
cons public init(double)
cons public init(int)
cons public init(java.lang.String)
cons public init(java.math.BigDecimal)
cons public init(java.math.BigInteger)
cons public init(java.math.BigInteger,int)
cons public init(long)
fld public final static com.ibm.icu.math.BigDecimal ONE
fld public final static com.ibm.icu.math.BigDecimal TEN
fld public final static com.ibm.icu.math.BigDecimal ZERO
fld public final static int ROUND_CEILING = 2
fld public final static int ROUND_DOWN = 1
fld public final static int ROUND_FLOOR = 3
fld public final static int ROUND_HALF_DOWN = 5
fld public final static int ROUND_HALF_EVEN = 6
fld public final static int ROUND_HALF_UP = 4
fld public final static int ROUND_UNNECESSARY = 7
fld public final static int ROUND_UP = 0
intf java.io.Serializable
intf java.lang.Comparable<com.ibm.icu.math.BigDecimal>
meth public boolean equals(java.lang.Object)
meth public byte byteValueExact()
meth public char[] toCharArray()
meth public com.ibm.icu.math.BigDecimal abs()
meth public com.ibm.icu.math.BigDecimal abs(com.ibm.icu.math.MathContext)
meth public com.ibm.icu.math.BigDecimal add(com.ibm.icu.math.BigDecimal)
meth public com.ibm.icu.math.BigDecimal add(com.ibm.icu.math.BigDecimal,com.ibm.icu.math.MathContext)
meth public com.ibm.icu.math.BigDecimal divide(com.ibm.icu.math.BigDecimal)
meth public com.ibm.icu.math.BigDecimal divide(com.ibm.icu.math.BigDecimal,com.ibm.icu.math.MathContext)
meth public com.ibm.icu.math.BigDecimal divide(com.ibm.icu.math.BigDecimal,int)
meth public com.ibm.icu.math.BigDecimal divide(com.ibm.icu.math.BigDecimal,int,int)
meth public com.ibm.icu.math.BigDecimal divideInteger(com.ibm.icu.math.BigDecimal)
meth public com.ibm.icu.math.BigDecimal divideInteger(com.ibm.icu.math.BigDecimal,com.ibm.icu.math.MathContext)
meth public com.ibm.icu.math.BigDecimal max(com.ibm.icu.math.BigDecimal)
meth public com.ibm.icu.math.BigDecimal max(com.ibm.icu.math.BigDecimal,com.ibm.icu.math.MathContext)
meth public com.ibm.icu.math.BigDecimal min(com.ibm.icu.math.BigDecimal)
meth public com.ibm.icu.math.BigDecimal min(com.ibm.icu.math.BigDecimal,com.ibm.icu.math.MathContext)
meth public com.ibm.icu.math.BigDecimal movePointLeft(int)
meth public com.ibm.icu.math.BigDecimal movePointRight(int)
meth public com.ibm.icu.math.BigDecimal multiply(com.ibm.icu.math.BigDecimal)
meth public com.ibm.icu.math.BigDecimal multiply(com.ibm.icu.math.BigDecimal,com.ibm.icu.math.MathContext)
meth public com.ibm.icu.math.BigDecimal negate()
meth public com.ibm.icu.math.BigDecimal negate(com.ibm.icu.math.MathContext)
meth public com.ibm.icu.math.BigDecimal plus()
meth public com.ibm.icu.math.BigDecimal plus(com.ibm.icu.math.MathContext)
meth public com.ibm.icu.math.BigDecimal pow(com.ibm.icu.math.BigDecimal)
meth public com.ibm.icu.math.BigDecimal pow(com.ibm.icu.math.BigDecimal,com.ibm.icu.math.MathContext)
meth public com.ibm.icu.math.BigDecimal remainder(com.ibm.icu.math.BigDecimal)
meth public com.ibm.icu.math.BigDecimal remainder(com.ibm.icu.math.BigDecimal,com.ibm.icu.math.MathContext)
meth public com.ibm.icu.math.BigDecimal setScale(int)
meth public com.ibm.icu.math.BigDecimal setScale(int,int)
meth public com.ibm.icu.math.BigDecimal subtract(com.ibm.icu.math.BigDecimal)
meth public com.ibm.icu.math.BigDecimal subtract(com.ibm.icu.math.BigDecimal,com.ibm.icu.math.MathContext)
meth public double doubleValue()
meth public float floatValue()
meth public int compareTo(com.ibm.icu.math.BigDecimal)
meth public int compareTo(com.ibm.icu.math.BigDecimal,com.ibm.icu.math.MathContext)
meth public int hashCode()
meth public int intValue()
meth public int intValueExact()
meth public int scale()
meth public int signum()
meth public java.lang.String format(int,int)
meth public java.lang.String format(int,int,int,int,int,int)
meth public java.lang.String toString()
meth public java.math.BigDecimal toBigDecimal()
meth public java.math.BigInteger toBigInteger()
meth public java.math.BigInteger toBigIntegerExact()
meth public java.math.BigInteger unscaledValue()
meth public long longValue()
meth public long longValueExact()
meth public short shortValueExact()
meth public static com.ibm.icu.math.BigDecimal valueOf(double)
meth public static com.ibm.icu.math.BigDecimal valueOf(long)
meth public static com.ibm.icu.math.BigDecimal valueOf(long,int)
supr java.lang.Number
hfds MaxArg,MaxExp,MinArg,MinExp,bytecar,bytedig,exp,form,ind,isneg,ispos,iszero,mant,plainMC,serialVersionUID

CLSS public final com.ibm.icu.math.MathContext
cons public init(int)
cons public init(int,int)
cons public init(int,int,boolean)
cons public init(int,int,boolean,int)
fld public final static com.ibm.icu.math.MathContext DEFAULT
fld public final static int ENGINEERING = 2
fld public final static int PLAIN = 0
fld public final static int ROUND_CEILING = 2
fld public final static int ROUND_DOWN = 1
fld public final static int ROUND_FLOOR = 3
fld public final static int ROUND_HALF_DOWN = 5
fld public final static int ROUND_HALF_EVEN = 6
fld public final static int ROUND_HALF_UP = 4
fld public final static int ROUND_UNNECESSARY = 7
fld public final static int ROUND_UP = 0
fld public final static int SCIENTIFIC = 1
intf java.io.Serializable
meth public boolean getLostDigits()
meth public int getDigits()
meth public int getForm()
meth public int getRoundingMode()
meth public java.lang.String toString()
supr java.lang.Object
hfds DEFAULT_DIGITS,DEFAULT_FORM,DEFAULT_LOSTDIGITS,DEFAULT_ROUNDINGMODE,MAX_DIGITS,MIN_DIGITS,ROUNDS,ROUNDWORDS,digits,form,lostDigits,roundingMode,serialVersionUID

CLSS public final com.ibm.icu.text.ArabicShaping
cons public init(int)
fld public final static int DIGITS_AN2EN = 64
fld public final static int DIGITS_EN2AN = 32
fld public final static int DIGITS_EN2AN_INIT_AL = 128
fld public final static int DIGITS_EN2AN_INIT_LR = 96
fld public final static int DIGITS_MASK = 224
fld public final static int DIGITS_NOOP = 0
fld public final static int DIGIT_TYPE_AN = 0
fld public final static int DIGIT_TYPE_AN_EXTENDED = 256
fld public final static int DIGIT_TYPE_MASK = 256
fld public final static int LAMALEF_AUTO = 65536
fld public final static int LAMALEF_BEGIN = 3
fld public final static int LAMALEF_END = 2
fld public final static int LAMALEF_MASK = 65539
fld public final static int LAMALEF_NEAR = 1
fld public final static int LAMALEF_RESIZE = 0
fld public final static int LENGTH_FIXED_SPACES_AT_BEGINNING = 3
fld public final static int LENGTH_FIXED_SPACES_AT_END = 2
fld public final static int LENGTH_FIXED_SPACES_NEAR = 1
fld public final static int LENGTH_GROW_SHRINK = 0
fld public final static int LENGTH_MASK = 65539
fld public final static int LETTERS_MASK = 24
fld public final static int LETTERS_NOOP = 0
fld public final static int LETTERS_SHAPE = 8
fld public final static int LETTERS_SHAPE_TASHKEEL_ISOLATED = 24
fld public final static int LETTERS_UNSHAPE = 16
fld public final static int SEEN_MASK = 7340032
fld public final static int SEEN_TWOCELL_NEAR = 2097152
fld public final static int SHAPE_TAIL_NEW_UNICODE = 134217728
fld public final static int SHAPE_TAIL_TYPE_MASK = 134217728
fld public final static int SPACES_RELATIVE_TO_TEXT_BEGIN_END = 67108864
fld public final static int SPACES_RELATIVE_TO_TEXT_MASK = 67108864
fld public final static int TASHKEEL_BEGIN = 262144
fld public final static int TASHKEEL_END = 393216
fld public final static int TASHKEEL_MASK = 917504
fld public final static int TASHKEEL_REPLACE_BY_TATWEEL = 786432
fld public final static int TASHKEEL_RESIZE = 524288
fld public final static int TEXT_DIRECTION_LOGICAL = 0
fld public final static int TEXT_DIRECTION_MASK = 4
fld public final static int TEXT_DIRECTION_VISUAL_LTR = 4
fld public final static int TEXT_DIRECTION_VISUAL_RTL = 0
fld public final static int YEHHAMZA_MASK = 58720256
fld public final static int YEHHAMZA_TWOCELL_NEAR = 16777216
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public int shape(char[],int,int,char[],int,int) throws com.ibm.icu.text.ArabicShapingException
meth public java.lang.String shape(java.lang.String) throws com.ibm.icu.text.ArabicShapingException
meth public java.lang.String toString()
meth public void shape(char[],int,int) throws com.ibm.icu.text.ArabicShapingException
supr java.lang.Object
hfds ALEFTYPE,DESHAPE_MODE,HAMZA06_CHAR,HAMZAFE_CHAR,IRRELEVANT,LAMALEF_SPACE_SUB,LAMTYPE,LAM_CHAR,LINKL,LINKR,LINK_MASK,NEW_TAIL_CHAR,OLD_TAIL_CHAR,SHADDA_CHAR,SHADDA_TATWEEL_CHAR,SHAPE_MODE,SPACE_CHAR,TASHKEEL_SPACE_SUB,TATWEEL_CHAR,YEH_HAMZAFE_CHAR,YEH_HAMZA_CHAR,araLink,convertFEto06,convertNormalizedLamAlef,irrelevantPos,isLogical,options,presLink,shapeTable,spacesRelativeToTextBeginEnd,tailChar,tailFamilyIsolatedFinal,tashkeelMedial,yehHamzaToYeh

CLSS public final com.ibm.icu.text.ArabicShapingException
cons public init(java.lang.String)
supr java.lang.Exception
hfds serialVersionUID

CLSS public com.ibm.icu.text.Bidi
cons public init()
cons public init(char[],int,byte[],int,int,int)
cons public init(int,int)
cons public init(java.lang.String,int)
cons public init(java.text.AttributedCharacterIterator)
fld public final static byte LEVEL_DEFAULT_LTR = 126
fld public final static byte LEVEL_DEFAULT_RTL = 127
fld public final static byte LEVEL_OVERRIDE = -128
fld public final static byte LTR = 0
fld public final static byte MAX_EXPLICIT_LEVEL = 61
fld public final static byte MIXED = 2
fld public final static byte RTL = 1
fld public final static int CLASS_DEFAULT = 19
fld public final static int DIRECTION_DEFAULT_LEFT_TO_RIGHT = 126
fld public final static int DIRECTION_DEFAULT_RIGHT_TO_LEFT = 127
fld public final static int DIRECTION_LEFT_TO_RIGHT = 0
fld public final static int DIRECTION_RIGHT_TO_LEFT = 1
fld public final static int MAP_NOWHERE = -1
fld public final static int OPTION_DEFAULT = 0
fld public final static int OPTION_INSERT_MARKS = 1
fld public final static int OPTION_REMOVE_CONTROLS = 2
fld public final static int OPTION_STREAMING = 4
fld public final static short DO_MIRRORING = 2
fld public final static short INSERT_LRM_FOR_NUMERIC = 4
fld public final static short KEEP_BASE_COMBINING = 1
fld public final static short OUTPUT_REVERSE = 16
fld public final static short REMOVE_BIDI_CONTROLS = 8
fld public final static short REORDER_DEFAULT = 0
fld public final static short REORDER_GROUP_NUMBERS_WITH_R = 2
fld public final static short REORDER_INVERSE_FOR_NUMBERS_SPECIAL = 6
fld public final static short REORDER_INVERSE_LIKE_DIRECT = 5
fld public final static short REORDER_INVERSE_NUMBERS_AS_L = 4
fld public final static short REORDER_NUMBERS_SPECIAL = 1
fld public final static short REORDER_RUNS_ONLY = 3
meth public boolean baseIsLeftToRight()
meth public boolean isInverse()
meth public boolean isLeftToRight()
meth public boolean isMixed()
meth public boolean isOrderParagraphsLTR()
meth public boolean isRightToLeft()
meth public byte getDirection()
meth public byte getLevelAt(int)
meth public byte getParaLevel()
meth public byte[] getLevels()
meth public char[] getText()
meth public com.ibm.icu.text.Bidi createLineBidi(int,int)
meth public com.ibm.icu.text.Bidi setLine(int,int)
meth public com.ibm.icu.text.BidiClassifier getCustomClassifier()
meth public com.ibm.icu.text.BidiRun getLogicalRun(int)
meth public com.ibm.icu.text.BidiRun getParagraph(int)
meth public com.ibm.icu.text.BidiRun getParagraphByIndex(int)
meth public com.ibm.icu.text.BidiRun getVisualRun(int)
meth public int countParagraphs()
meth public int countRuns()
meth public int getBaseLevel()
meth public int getCustomizedClass(int)
meth public int getLength()
meth public int getLogicalIndex(int)
meth public int getParagraphIndex(int)
meth public int getProcessedLength()
meth public int getReorderingMode()
meth public int getReorderingOptions()
meth public int getResultLength()
meth public int getRunCount()
meth public int getRunLevel(int)
meth public int getRunLimit(int)
meth public int getRunStart(int)
meth public int getVisualIndex(int)
meth public int[] getLogicalMap()
meth public int[] getVisualMap()
meth public java.lang.String getTextAsString()
meth public java.lang.String writeReordered(int)
meth public static boolean requiresBidi(char[],int,int)
meth public static int[] invertMap(int[])
meth public static int[] reorderLogical(byte[])
meth public static int[] reorderVisual(byte[])
meth public static java.lang.String writeReverse(java.lang.String,int)
meth public static void reorderVisually(byte[],int,java.lang.Object[],int,int)
meth public void orderParagraphsLTR(boolean)
meth public void setCustomClassifier(com.ibm.icu.text.BidiClassifier)
meth public void setInverse(boolean)
meth public void setPara(char[],byte,byte[])
meth public void setPara(java.lang.String,byte,byte[])
meth public void setPara(java.text.AttributedCharacterIterator)
meth public void setReorderingMode(int)
meth public void setReorderingOptions(int)
supr java.lang.Object
hfds AL,AN,B,BN,CONTEXT_RTL,CONTEXT_RTL_SHIFT,CR,CS,DirPropFlagE,DirPropFlagLR,DirPropFlagMultiRuns,DirPropFlagO,EN,ES,ET,FIRSTALLOC,IMPTABLEVELS_COLUMNS,IMPTABLEVELS_RES,IMPTABPROPS_COLUMNS,IMPTABPROPS_RES,L,LF,LRE,LRM_AFTER,LRM_BEFORE,LRO,MASK_BN_EXPLICIT,MASK_B_S,MASK_EMBEDDING,MASK_ET_NSM_BN,MASK_EXPLICIT,MASK_LRX,MASK_LTR,MASK_N,MASK_OVERRIDE,MASK_POSSIBLE_N,MASK_RLX,MASK_RTL,MASK_R_AL,MASK_WS,NSM,ON,PDF,R,REORDER_COUNT,REORDER_LAST_LOGICAL_TO_VISUAL,RLE,RLM_AFTER,RLM_BEFORE,RLO,S,WS,_AN,_B,_EN,_L,_ON,_R,_S,bdp,controlCount,customClassifier,defaultParaLevel,dirProps,dirPropsMemory,direction,flags,groupProp,impAct0,impAct1,impAct2,impTabL_DEFAULT,impTabL_GROUP_NUMBERS_WITH_R,impTabL_INVERSE_FOR_NUMBERS_SPECIAL_WITH_MARKS,impTabL_INVERSE_LIKE_DIRECT_WITH_MARKS,impTabL_INVERSE_NUMBERS_AS_L,impTabL_NUMBERS_SPECIAL,impTabPair,impTabProps,impTabR_DEFAULT,impTabR_GROUP_NUMBERS_WITH_R,impTabR_INVERSE_LIKE_DIRECT,impTabR_INVERSE_LIKE_DIRECT_WITH_MARKS,impTabR_INVERSE_NUMBERS_AS_L,impTab_DEFAULT,impTab_GROUP_NUMBERS_WITH_R,impTab_INVERSE_FOR_NUMBERS_SPECIAL,impTab_INVERSE_FOR_NUMBERS_SPECIAL_WITH_MARKS,impTab_INVERSE_LIKE_DIRECT,impTab_INVERSE_LIKE_DIRECT_WITH_MARKS,impTab_INVERSE_NUMBERS_AS_L,impTab_NUMBERS_SPECIAL,insertPoints,isGoodLogicalToVisualRunsMap,isInverse,lastArabicPos,length,levels,levelsMemory,logicalToVisualRunsMap,mayAllocateRuns,mayAllocateText,orderParagraphsLTR,originalLength,paraBidi,paraCount,paraLevel,paras,parasMemory,reorderingMode,reorderingOptions,resultLength,runCount,runs,runsMemory,simpleParas,simpleRuns,text,trailingWSStart
hcls ImpTabPair,InsertPoints,LevState,Point

CLSS public com.ibm.icu.text.BidiClassifier
cons public init(java.lang.Object)
fld protected java.lang.Object context
meth public int classify(int)
meth public java.lang.Object getContext()
meth public void setContext(java.lang.Object)
supr java.lang.Object

CLSS public com.ibm.icu.text.BidiRun
meth public boolean isEvenRun()
meth public boolean isOddRun()
meth public byte getDirection()
meth public byte getEmbeddingLevel()
meth public int getLength()
meth public int getLimit()
meth public int getStart()
meth public java.lang.String toString()
supr java.lang.Object
hfds insertRemove,level,limit,start

CLSS public abstract com.ibm.icu.text.BreakIterator
cons protected init()
fld public final static int DONE = -1
fld public final static int KIND_CHARACTER = 0
fld public final static int KIND_LINE = 2
fld public final static int KIND_SENTENCE = 3
fld public final static int KIND_TITLE = 4
fld public final static int KIND_WORD = 1
intf java.lang.Cloneable
meth public abstract int current()
meth public abstract int first()
meth public abstract int following(int)
meth public abstract int last()
meth public abstract int next()
meth public abstract int next(int)
meth public abstract int previous()
meth public abstract java.text.CharacterIterator getText()
meth public abstract void setText(java.text.CharacterIterator)
meth public boolean isBoundary(int)
meth public final com.ibm.icu.util.ULocale getLocale(com.ibm.icu.util.ULocale$Type)
meth public int preceding(int)
meth public java.lang.Object clone()
meth public static boolean unregister(java.lang.Object)
meth public static com.ibm.icu.text.BreakIterator getBreakInstance(com.ibm.icu.util.ULocale,int)
meth public static com.ibm.icu.text.BreakIterator getCharacterInstance()
meth public static com.ibm.icu.text.BreakIterator getCharacterInstance(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.text.BreakIterator getCharacterInstance(java.util.Locale)
meth public static com.ibm.icu.text.BreakIterator getLineInstance()
meth public static com.ibm.icu.text.BreakIterator getLineInstance(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.text.BreakIterator getLineInstance(java.util.Locale)
meth public static com.ibm.icu.text.BreakIterator getSentenceInstance()
meth public static com.ibm.icu.text.BreakIterator getSentenceInstance(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.text.BreakIterator getSentenceInstance(java.util.Locale)
meth public static com.ibm.icu.text.BreakIterator getTitleInstance()
meth public static com.ibm.icu.text.BreakIterator getTitleInstance(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.text.BreakIterator getTitleInstance(java.util.Locale)
meth public static com.ibm.icu.text.BreakIterator getWordInstance()
meth public static com.ibm.icu.text.BreakIterator getWordInstance(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.text.BreakIterator getWordInstance(java.util.Locale)
meth public static com.ibm.icu.util.ULocale[] getAvailableULocales()
meth public static java.lang.Object registerInstance(com.ibm.icu.text.BreakIterator,com.ibm.icu.util.ULocale,int)
meth public static java.lang.Object registerInstance(com.ibm.icu.text.BreakIterator,java.util.Locale,int)
meth public static java.util.Locale[] getAvailableLocales()
meth public void setText(java.lang.String)
supr java.lang.Object
hfds DEBUG,KIND_COUNT,actualLocale,iterCache,shim,validLocale
hcls BreakIteratorCache,BreakIteratorServiceShim

CLSS public final com.ibm.icu.text.CanonicalIterator
cons public init(java.lang.String)
meth public java.lang.String getSource()
meth public java.lang.String next()
meth public static void permute(java.lang.String,boolean,java.util.Set<java.lang.String>)
meth public void reset()
meth public void setSource(java.lang.String)
supr java.lang.Object
hfds PROGRESS,SET_WITH_NULL_STRING,SKIP_ZEROS,buffer,current,done,nfcImpl,nfd,pieces,source

CLSS public com.ibm.icu.text.CharsetDetector
cons public init()
meth public boolean enableInputFilter(boolean)
meth public boolean inputFilterEnabled()
meth public com.ibm.icu.text.CharsetDetector setDeclaredEncoding(java.lang.String)
meth public com.ibm.icu.text.CharsetDetector setText(byte[])
meth public com.ibm.icu.text.CharsetDetector setText(java.io.InputStream) throws java.io.IOException
meth public com.ibm.icu.text.CharsetMatch detect()
meth public com.ibm.icu.text.CharsetMatch[] detectAll()
meth public java.io.Reader getReader(java.io.InputStream,java.lang.String)
meth public java.lang.String getString(byte[],java.lang.String)
meth public static java.lang.String[] getAllDetectableCharsets()
supr java.lang.Object
hfds fByteStats,fC1Bytes,fCSRecognizers,fCharsetNames,fDeclaredEncoding,fInputBytes,fInputLen,fInputStream,fRawInput,fRawLength,fStripTags,kBufSize

CLSS public com.ibm.icu.text.CharsetMatch
fld public final static int BOM = 2
fld public final static int DECLARED_ENCODING = 4
fld public final static int ENCODING_SCHEME = 1
fld public final static int LANG_STATISTICS = 8
intf java.lang.Comparable<com.ibm.icu.text.CharsetMatch>
meth public int compareTo(com.ibm.icu.text.CharsetMatch)
meth public int getConfidence()
meth public int getMatchType()
meth public java.io.Reader getReader()
meth public java.lang.String getLanguage()
meth public java.lang.String getName()
meth public java.lang.String getString() throws java.io.IOException
meth public java.lang.String getString(int) throws java.io.IOException
supr java.lang.Object
hfds fConfidence,fInputStream,fRawInput,fRawLength,fRecognizer

CLSS public com.ibm.icu.text.ChineseDateFormat
cons public init(java.lang.String,com.ibm.icu.util.ULocale)
cons public init(java.lang.String,java.lang.String,com.ibm.icu.util.ULocale)
cons public init(java.lang.String,java.util.Locale)
innr public static Field
meth protected com.ibm.icu.text.DateFormat$Field patternCharToDateFormatField(char)
meth protected int subParse(java.lang.String,int,char,int,boolean,boolean,boolean[],com.ibm.icu.util.Calendar)
meth protected void subFormat(java.lang.StringBuffer,char,int,int,java.text.FieldPosition,com.ibm.icu.util.Calendar)
supr com.ibm.icu.text.SimpleDateFormat
hfds serialVersionUID

CLSS public static com.ibm.icu.text.ChineseDateFormat$Field
 outer com.ibm.icu.text.ChineseDateFormat
cons protected init(java.lang.String,int)
fld public final static com.ibm.icu.text.ChineseDateFormat$Field IS_LEAP_MONTH
meth protected java.lang.Object readResolve() throws java.io.InvalidObjectException
meth public static com.ibm.icu.text.DateFormat$Field ofCalendarField(int)
supr com.ibm.icu.text.DateFormat$Field
hfds serialVersionUID

CLSS public com.ibm.icu.text.ChineseDateFormatSymbols
cons public init()
cons public init(com.ibm.icu.util.Calendar,com.ibm.icu.util.ULocale)
cons public init(com.ibm.icu.util.Calendar,java.util.Locale)
cons public init(com.ibm.icu.util.ULocale)
cons public init(java.util.Locale)
meth protected void initializeData(com.ibm.icu.util.ULocale,com.ibm.icu.impl.CalendarData)
meth public java.lang.String getLeapMonth(int)
supr com.ibm.icu.text.DateFormatSymbols
hfds isLeapMonth,serialVersionUID

CLSS public final com.ibm.icu.text.CollationElementIterator
fld public final static int IGNORABLE = 0
fld public final static int NULLORDER = -1
meth public boolean equals(java.lang.Object)
meth public final static int primaryOrder(int)
meth public final static int secondaryOrder(int)
meth public final static int tertiaryOrder(int)
meth public int getMaxExpansion(int)
meth public int getOffset()
meth public int next()
meth public int previous()
meth public void reset()
meth public void setOffset(int)
meth public void setText(com.ibm.icu.text.UCharacterIterator)
meth public void setText(java.lang.String)
meth public void setText(java.text.CharacterIterator)
supr java.lang.Object
hfds CE_BUFFER_INIT_SIZE_,CE_BYTE_COMMON_,CE_CHARSET_TAG_,CE_CJK_IMPLICIT_TAG_,CE_CONTRACTION_,CE_CONTRACTION_TAG_,CE_DIGIT_TAG_,CE_EXPANSION_TAG_,CE_HANGUL_SYLLABLE_TAG_,CE_IMPLICIT_TAG_,CE_LEAD_SURROGATE_TAG_,CE_LONG_PRIMARY_TAG_,CE_NOT_FOUND_,CE_NOT_FOUND_TAG_,CE_SPEC_PROC_TAG_,CE_TRAIL_SURROGATE_TAG_,DEBUG,FULL_ZERO_COMBINING_CLASS_FAST_LIMIT_,HANGUL_LBASE_,HANGUL_SBASE_,HANGUL_TBASE_,HANGUL_TCOUNT_,HANGUL_VBASE_,HANGUL_VCOUNT_,LAST_BYTE_MASK_,LEAD_ZERO_COMBINING_CLASS_FAST_LIMIT_,SECOND_LAST_BYTE_SHIFT_,m_CEBufferOffset_,m_CEBufferSize_,m_CEBuffer_,m_FCDLimit_,m_FCDStart_,m_bufferOffset_,m_buffer_,m_collator_,m_isCodePointHiragana_,m_isForwards_,m_n2Buffer_,m_nfcImpl_,m_source_,m_srcUtilIter_,m_unnormalized_,m_utilColEIter_,m_utilSkippedBuffer_,m_utilSpecialBackUp_,m_utilSpecialDiscontiguousBackUp_,m_utilSpecialEntryBackUp_,m_utilStringBuffer_
hcls Backup

CLSS public final com.ibm.icu.text.CollationKey
cons public init(java.lang.String,byte[])
cons public init(java.lang.String,com.ibm.icu.text.RawCollationKey)
innr public final static BoundMode
intf java.lang.Comparable<com.ibm.icu.text.CollationKey>
meth public boolean equals(com.ibm.icu.text.CollationKey)
meth public boolean equals(java.lang.Object)
meth public byte[] toByteArray()
meth public com.ibm.icu.text.CollationKey getBound(int,int)
meth public com.ibm.icu.text.CollationKey merge(com.ibm.icu.text.CollationKey)
meth public int compareTo(com.ibm.icu.text.CollationKey)
meth public int hashCode()
meth public java.lang.String getSourceString()
supr java.lang.Object
hfds MERGE_SEPERATOR_,m_hashCode_,m_key_,m_length_,m_source_

CLSS public final static com.ibm.icu.text.CollationKey$BoundMode
 outer com.ibm.icu.text.CollationKey
fld public final static int COUNT = 3
fld public final static int LOWER = 0
fld public final static int UPPER = 1
fld public final static int UPPER_LONG = 2
supr java.lang.Object

CLSS public abstract com.ibm.icu.text.Collator
cons protected init()
fld public final static int CANONICAL_DECOMPOSITION = 17
fld public final static int FULL_DECOMPOSITION = 15
fld public final static int IDENTICAL = 15
fld public final static int NO_DECOMPOSITION = 16
fld public final static int PRIMARY = 0
fld public final static int QUATERNARY = 3
fld public final static int SECONDARY = 1
fld public final static int TERTIARY = 2
innr public abstract static CollatorFactory
intf java.lang.Cloneable
intf java.util.Comparator<java.lang.Object>
meth public abstract com.ibm.icu.text.CollationKey getCollationKey(java.lang.String)
meth public abstract com.ibm.icu.text.RawCollationKey getRawCollationKey(java.lang.String,com.ibm.icu.text.RawCollationKey)
meth public abstract com.ibm.icu.util.VersionInfo getUCAVersion()
meth public abstract com.ibm.icu.util.VersionInfo getVersion()
meth public abstract int compare(java.lang.String,java.lang.String)
meth public abstract int getVariableTop()
meth public abstract int setVariableTop(java.lang.String)
meth public abstract void setVariableTop(int)
meth public boolean equals(java.lang.String,java.lang.String)
meth public com.ibm.icu.text.Collator setStrength2(int)
meth public com.ibm.icu.text.UnicodeSet getTailoredSet()
meth public final com.ibm.icu.util.ULocale getLocale(com.ibm.icu.util.ULocale$Type)
meth public final static boolean unregister(java.lang.Object)
meth public final static com.ibm.icu.text.Collator getInstance()
meth public final static com.ibm.icu.text.Collator getInstance(com.ibm.icu.util.ULocale)
meth public final static com.ibm.icu.text.Collator getInstance(java.util.Locale)
meth public final static com.ibm.icu.util.ULocale getFunctionalEquivalent(java.lang.String,com.ibm.icu.util.ULocale)
meth public final static com.ibm.icu.util.ULocale getFunctionalEquivalent(java.lang.String,com.ibm.icu.util.ULocale,boolean[])
meth public final static com.ibm.icu.util.ULocale[] getAvailableULocales()
meth public final static java.lang.Object registerFactory(com.ibm.icu.text.Collator$CollatorFactory)
meth public final static java.lang.Object registerInstance(com.ibm.icu.text.Collator,com.ibm.icu.util.ULocale)
meth public final static java.lang.String[] getKeywordValues(java.lang.String)
meth public final static java.lang.String[] getKeywordValuesForLocale(java.lang.String,com.ibm.icu.util.ULocale,boolean)
meth public final static java.lang.String[] getKeywords()
meth public int compare(java.lang.Object,java.lang.Object)
meth public int getDecomposition()
meth public int getStrength()
meth public java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public static java.lang.String getDisplayName(com.ibm.icu.util.ULocale)
meth public static java.lang.String getDisplayName(com.ibm.icu.util.ULocale,com.ibm.icu.util.ULocale)
meth public static java.lang.String getDisplayName(java.util.Locale)
meth public static java.lang.String getDisplayName(java.util.Locale,java.util.Locale)
meth public static java.util.Locale[] getAvailableLocales()
meth public void setDecomposition(int)
meth public void setStrength(int)
supr java.lang.Object
hfds BASE,DEBUG,KEYWORDS,RESOURCE,actualLocale,m_decomposition_,m_strength_,shim,validLocale
hcls ServiceShim

CLSS public abstract static com.ibm.icu.text.Collator$CollatorFactory
 outer com.ibm.icu.text.Collator
cons protected init()
meth public abstract java.util.Set<java.lang.String> getSupportedLocaleIDs()
meth public boolean visible()
meth public com.ibm.icu.text.Collator createCollator(com.ibm.icu.util.ULocale)
meth public com.ibm.icu.text.Collator createCollator(java.util.Locale)
meth public java.lang.String getDisplayName(com.ibm.icu.util.ULocale,com.ibm.icu.util.ULocale)
meth public java.lang.String getDisplayName(java.util.Locale,java.util.Locale)
supr java.lang.Object

CLSS public final com.ibm.icu.text.ComposedCharIter
cons public init()
cons public init(boolean,int)
fld public final static char DONE = '\uffff'
meth public boolean hasNext()
meth public char next()
meth public java.lang.String decomposition()
supr java.lang.Object
hfds curChar,decompBuf,n2impl,nextChar

CLSS public abstract com.ibm.icu.text.CurrencyDisplayNames
cons protected init()
meth public abstract com.ibm.icu.util.ULocale getLocale()
meth public abstract java.lang.String getName(java.lang.String)
meth public abstract java.lang.String getPluralName(java.lang.String,java.lang.String)
meth public abstract java.lang.String getSymbol(java.lang.String)
meth public abstract java.util.Map<java.lang.String,java.lang.String> nameMap()
meth public abstract java.util.Map<java.lang.String,java.lang.String> symbolMap()
meth public static boolean hasData()
meth public static com.ibm.icu.text.CurrencyDisplayNames getInstance(com.ibm.icu.util.ULocale)
supr java.lang.Object

CLSS public com.ibm.icu.text.CurrencyMetaInfo
cons protected init()
fld protected final static com.ibm.icu.text.CurrencyMetaInfo$CurrencyDigits defaultDigits
innr public final static CurrencyDigits
innr public final static CurrencyFilter
innr public final static CurrencyInfo
meth public com.ibm.icu.text.CurrencyMetaInfo$CurrencyDigits currencyDigits(java.lang.String)
meth public java.util.List<com.ibm.icu.text.CurrencyMetaInfo$CurrencyInfo> currencyInfo(com.ibm.icu.text.CurrencyMetaInfo$CurrencyFilter)
meth public java.util.List<java.lang.String> currencies(com.ibm.icu.text.CurrencyMetaInfo$CurrencyFilter)
meth public java.util.List<java.lang.String> regions(com.ibm.icu.text.CurrencyMetaInfo$CurrencyFilter)
meth public static boolean hasData()
meth public static com.ibm.icu.text.CurrencyMetaInfo getInstance()
supr java.lang.Object
hfds hasData,impl

CLSS public final static com.ibm.icu.text.CurrencyMetaInfo$CurrencyDigits
 outer com.ibm.icu.text.CurrencyMetaInfo
cons public init(int,int)
fld public final byte fractionDigits
fld public final byte roundingIncrement
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final static com.ibm.icu.text.CurrencyMetaInfo$CurrencyFilter
 outer com.ibm.icu.text.CurrencyMetaInfo
fld public final java.lang.String currency
fld public final java.lang.String region
fld public final long from
fld public final long to
meth public boolean equals(com.ibm.icu.text.CurrencyMetaInfo$CurrencyFilter)
meth public boolean equals(java.lang.Object)
meth public com.ibm.icu.text.CurrencyMetaInfo$CurrencyFilter withCurrency(java.lang.String)
meth public com.ibm.icu.text.CurrencyMetaInfo$CurrencyFilter withDate(java.util.Date)
meth public com.ibm.icu.text.CurrencyMetaInfo$CurrencyFilter withRange(java.util.Date,java.util.Date)
meth public com.ibm.icu.text.CurrencyMetaInfo$CurrencyFilter withRegion(java.lang.String)
meth public int hashCode()
meth public java.lang.String toString()
meth public static com.ibm.icu.text.CurrencyMetaInfo$CurrencyFilter all()
meth public static com.ibm.icu.text.CurrencyMetaInfo$CurrencyFilter now()
meth public static com.ibm.icu.text.CurrencyMetaInfo$CurrencyFilter onCurrency(java.lang.String)
meth public static com.ibm.icu.text.CurrencyMetaInfo$CurrencyFilter onDate(java.util.Date)
meth public static com.ibm.icu.text.CurrencyMetaInfo$CurrencyFilter onRange(java.util.Date,java.util.Date)
meth public static com.ibm.icu.text.CurrencyMetaInfo$CurrencyFilter onRegion(java.lang.String)
supr java.lang.Object
hfds ALL

CLSS public final static com.ibm.icu.text.CurrencyMetaInfo$CurrencyInfo
 outer com.ibm.icu.text.CurrencyMetaInfo
cons public init(java.lang.String,java.lang.String,long,long,int)
fld public final java.lang.String code
fld public final java.lang.String region
fld public final long from
fld public final long to
fld public final short priority
meth public java.lang.String toString()
supr java.lang.Object

CLSS public com.ibm.icu.text.CurrencyPluralInfo
cons public init()
cons public init(com.ibm.icu.util.ULocale)
cons public init(java.util.Locale)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.ibm.icu.text.PluralRules getPluralRules()
meth public com.ibm.icu.util.ULocale getLocale()
meth public java.lang.Object clone()
meth public java.lang.String getCurrencyPluralPattern(java.lang.String)
meth public static com.ibm.icu.text.CurrencyPluralInfo getInstance()
meth public static com.ibm.icu.text.CurrencyPluralInfo getInstance(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.text.CurrencyPluralInfo getInstance(java.util.Locale)
meth public void setCurrencyPluralPattern(java.lang.String,java.lang.String)
meth public void setLocale(com.ibm.icu.util.ULocale)
meth public void setPluralRules(java.lang.String)
supr java.lang.Object
hfds defaultCurrencyPluralPattern,defaultCurrencyPluralPatternChar,pluralCountToCurrencyUnitPattern,pluralRules,serialVersionUID,tripleCurrencySign,tripleCurrencyStr,ulocale

CLSS public abstract com.ibm.icu.text.DateFormat
cons protected init()
fld protected com.ibm.icu.text.NumberFormat numberFormat
fld protected com.ibm.icu.util.Calendar calendar
fld public final static int AM_PM_FIELD = 14
fld public final static int DATE_FIELD = 3
fld public final static int DAY_OF_WEEK_FIELD = 9
fld public final static int DAY_OF_WEEK_IN_MONTH_FIELD = 11
fld public final static int DAY_OF_YEAR_FIELD = 10
fld public final static int DEFAULT = 2
fld public final static int DOW_LOCAL_FIELD = 19
fld public final static int ERA_FIELD = 0
fld public final static int EXTENDED_YEAR_FIELD = 20
fld public final static int FIELD_COUNT = 30
fld public final static int FRACTIONAL_SECOND_FIELD = 8
fld public final static int FULL = 0
fld public final static int HOUR0_FIELD = 16
fld public final static int HOUR1_FIELD = 15
fld public final static int HOUR_OF_DAY0_FIELD = 5
fld public final static int HOUR_OF_DAY1_FIELD = 4
fld public final static int JULIAN_DAY_FIELD = 21
fld public final static int LONG = 1
fld public final static int MEDIUM = 2
fld public final static int MILLISECONDS_IN_DAY_FIELD = 22
fld public final static int MILLISECOND_FIELD = 8
fld public final static int MINUTE_FIELD = 6
fld public final static int MONTH_FIELD = 2
fld public final static int NONE = -1
fld public final static int QUARTER_FIELD = 27
fld public final static int RELATIVE = 128
fld public final static int RELATIVE_DEFAULT = 130
fld public final static int RELATIVE_FULL = 128
fld public final static int RELATIVE_LONG = 129
fld public final static int RELATIVE_MEDIUM = 130
fld public final static int RELATIVE_SHORT = 131
fld public final static int SECOND_FIELD = 7
fld public final static int SHORT = 3
fld public final static int STANDALONE_DAY_FIELD = 25
fld public final static int STANDALONE_MONTH_FIELD = 26
fld public final static int STANDALONE_QUARTER_FIELD = 28
fld public final static int TIMEZONE_FIELD = 17
fld public final static int TIMEZONE_GENERIC_FIELD = 24
fld public final static int TIMEZONE_RFC_FIELD = 23
fld public final static int TIMEZONE_SPECIAL_FIELD = 29
fld public final static int WEEK_OF_MONTH_FIELD = 13
fld public final static int WEEK_OF_YEAR_FIELD = 12
fld public final static int YEAR_FIELD = 1
fld public final static int YEAR_WOY_FIELD = 18
fld public final static java.lang.String ABBR_MONTH = "MMM"
fld public final static java.lang.String ABBR_MONTH_DAY = "MMMd"
fld public final static java.lang.String ABBR_MONTH_WEEKDAY_DAY = "MMMEd"
fld public final static java.lang.String ABBR_STANDALONE_MONTH = "LLL"
fld public final static java.lang.String DAY = "d"
fld public final static java.lang.String HOUR = "h"
fld public final static java.lang.String HOUR24_MINUTE = "Hm"
fld public final static java.lang.String HOUR24_MINUTE_SECOND = "Hms"
fld public final static java.lang.String HOUR_GENERIC_TZ = "hv"
fld public final static java.lang.String HOUR_MINUTE = "hm"
fld public final static java.lang.String HOUR_MINUTE_GENERIC_TZ = "hmv"
fld public final static java.lang.String HOUR_MINUTE_SECOND = "hms"
fld public final static java.lang.String HOUR_MINUTE_TZ = "hmz"
fld public final static java.lang.String HOUR_TZ = "hz"
fld public final static java.lang.String MINUTE_SECOND = "ms"
fld public final static java.lang.String MONTH = "MMMM"
fld public final static java.lang.String MONTH_DAY = "MMMMd"
fld public final static java.lang.String MONTH_WEEKDAY_DAY = "MMMMEEEEd"
fld public final static java.lang.String NUM_MONTH = "M"
fld public final static java.lang.String NUM_MONTH_DAY = "Md"
fld public final static java.lang.String NUM_MONTH_WEEKDAY_DAY = "MEd"
fld public final static java.lang.String STANDALONE_MONTH = "LLLL"
fld public final static java.lang.String YEAR = "y"
fld public final static java.lang.String YEAR_ABBR_MONTH = "yMMM"
fld public final static java.lang.String YEAR_ABBR_MONTH_DAY = "yMMMd"
fld public final static java.lang.String YEAR_ABBR_MONTH_WEEKDAY_DAY = "yMMMEd"
fld public final static java.lang.String YEAR_ABBR_QUARTER = "yQ"
fld public final static java.lang.String YEAR_MONTH = "yMMMM"
fld public final static java.lang.String YEAR_MONTH_DAY = "yMMMMd"
fld public final static java.lang.String YEAR_MONTH_WEEKDAY_DAY = "yMMMMEEEEd"
fld public final static java.lang.String YEAR_NUM_MONTH = "yM"
fld public final static java.lang.String YEAR_NUM_MONTH_DAY = "yMd"
fld public final static java.lang.String YEAR_NUM_MONTH_WEEKDAY_DAY = "yMEd"
fld public final static java.lang.String YEAR_QUARTER = "yQQQ"
innr public static Field
meth public abstract java.lang.StringBuffer format(com.ibm.icu.util.Calendar,java.lang.StringBuffer,java.text.FieldPosition)
meth public abstract void parse(java.lang.String,com.ibm.icu.util.Calendar,java.text.ParsePosition)
meth public boolean equals(java.lang.Object)
meth public boolean isLenient()
meth public com.ibm.icu.text.NumberFormat getNumberFormat()
meth public com.ibm.icu.util.Calendar getCalendar()
meth public com.ibm.icu.util.TimeZone getTimeZone()
meth public final java.lang.String format(java.util.Date)
meth public final java.lang.StringBuffer format(java.lang.Object,java.lang.StringBuffer,java.text.FieldPosition)
meth public final static com.ibm.icu.text.DateFormat getDateInstance()
meth public final static com.ibm.icu.text.DateFormat getDateInstance(com.ibm.icu.util.Calendar,int)
meth public final static com.ibm.icu.text.DateFormat getDateInstance(com.ibm.icu.util.Calendar,int,com.ibm.icu.util.ULocale)
meth public final static com.ibm.icu.text.DateFormat getDateInstance(com.ibm.icu.util.Calendar,int,java.util.Locale)
meth public final static com.ibm.icu.text.DateFormat getDateInstance(int)
meth public final static com.ibm.icu.text.DateFormat getDateInstance(int,com.ibm.icu.util.ULocale)
meth public final static com.ibm.icu.text.DateFormat getDateInstance(int,java.util.Locale)
meth public final static com.ibm.icu.text.DateFormat getDateTimeInstance()
meth public final static com.ibm.icu.text.DateFormat getDateTimeInstance(com.ibm.icu.util.Calendar,int,int)
meth public final static com.ibm.icu.text.DateFormat getDateTimeInstance(com.ibm.icu.util.Calendar,int,int,com.ibm.icu.util.ULocale)
meth public final static com.ibm.icu.text.DateFormat getDateTimeInstance(com.ibm.icu.util.Calendar,int,int,java.util.Locale)
meth public final static com.ibm.icu.text.DateFormat getDateTimeInstance(int,int)
meth public final static com.ibm.icu.text.DateFormat getDateTimeInstance(int,int,com.ibm.icu.util.ULocale)
meth public final static com.ibm.icu.text.DateFormat getDateTimeInstance(int,int,java.util.Locale)
meth public final static com.ibm.icu.text.DateFormat getInstance()
meth public final static com.ibm.icu.text.DateFormat getInstance(com.ibm.icu.util.Calendar)
meth public final static com.ibm.icu.text.DateFormat getInstance(com.ibm.icu.util.Calendar,com.ibm.icu.util.ULocale)
meth public final static com.ibm.icu.text.DateFormat getInstance(com.ibm.icu.util.Calendar,java.util.Locale)
meth public final static com.ibm.icu.text.DateFormat getPatternInstance(com.ibm.icu.util.Calendar,java.lang.String,com.ibm.icu.util.ULocale)
meth public final static com.ibm.icu.text.DateFormat getPatternInstance(com.ibm.icu.util.Calendar,java.lang.String,java.util.Locale)
meth public final static com.ibm.icu.text.DateFormat getPatternInstance(java.lang.String)
meth public final static com.ibm.icu.text.DateFormat getPatternInstance(java.lang.String,com.ibm.icu.util.ULocale)
meth public final static com.ibm.icu.text.DateFormat getPatternInstance(java.lang.String,java.util.Locale)
meth public final static com.ibm.icu.text.DateFormat getTimeInstance()
meth public final static com.ibm.icu.text.DateFormat getTimeInstance(com.ibm.icu.util.Calendar,int)
meth public final static com.ibm.icu.text.DateFormat getTimeInstance(com.ibm.icu.util.Calendar,int,com.ibm.icu.util.ULocale)
meth public final static com.ibm.icu.text.DateFormat getTimeInstance(com.ibm.icu.util.Calendar,int,java.util.Locale)
meth public final static com.ibm.icu.text.DateFormat getTimeInstance(int)
meth public final static com.ibm.icu.text.DateFormat getTimeInstance(int,com.ibm.icu.util.ULocale)
meth public final static com.ibm.icu.text.DateFormat getTimeInstance(int,java.util.Locale)
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.Object parseObject(java.lang.String,java.text.ParsePosition)
meth public java.lang.StringBuffer format(java.util.Date,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.util.Date parse(java.lang.String) throws java.text.ParseException
meth public java.util.Date parse(java.lang.String,java.text.ParsePosition)
meth public static com.ibm.icu.util.ULocale[] getAvailableULocales()
meth public static java.util.Locale[] getAvailableLocales()
meth public void setCalendar(com.ibm.icu.util.Calendar)
meth public void setLenient(boolean)
meth public void setNumberFormat(com.ibm.icu.text.NumberFormat)
meth public void setTimeZone(com.ibm.icu.util.TimeZone)
supr com.ibm.icu.text.UFormat
hfds serialVersionUID

CLSS public static com.ibm.icu.text.DateFormat$Field
 outer com.ibm.icu.text.DateFormat
cons protected init(java.lang.String,int)
fld public final static com.ibm.icu.text.DateFormat$Field AM_PM
fld public final static com.ibm.icu.text.DateFormat$Field DAY_OF_MONTH
fld public final static com.ibm.icu.text.DateFormat$Field DAY_OF_WEEK
fld public final static com.ibm.icu.text.DateFormat$Field DAY_OF_WEEK_IN_MONTH
fld public final static com.ibm.icu.text.DateFormat$Field DAY_OF_YEAR
fld public final static com.ibm.icu.text.DateFormat$Field DOW_LOCAL
fld public final static com.ibm.icu.text.DateFormat$Field ERA
fld public final static com.ibm.icu.text.DateFormat$Field EXTENDED_YEAR
fld public final static com.ibm.icu.text.DateFormat$Field HOUR0
fld public final static com.ibm.icu.text.DateFormat$Field HOUR1
fld public final static com.ibm.icu.text.DateFormat$Field HOUR_OF_DAY0
fld public final static com.ibm.icu.text.DateFormat$Field HOUR_OF_DAY1
fld public final static com.ibm.icu.text.DateFormat$Field JULIAN_DAY
fld public final static com.ibm.icu.text.DateFormat$Field MILLISECOND
fld public final static com.ibm.icu.text.DateFormat$Field MILLISECONDS_IN_DAY
fld public final static com.ibm.icu.text.DateFormat$Field MINUTE
fld public final static com.ibm.icu.text.DateFormat$Field MONTH
fld public final static com.ibm.icu.text.DateFormat$Field QUARTER
fld public final static com.ibm.icu.text.DateFormat$Field SECOND
fld public final static com.ibm.icu.text.DateFormat$Field TIME_ZONE
fld public final static com.ibm.icu.text.DateFormat$Field WEEK_OF_MONTH
fld public final static com.ibm.icu.text.DateFormat$Field WEEK_OF_YEAR
fld public final static com.ibm.icu.text.DateFormat$Field YEAR
fld public final static com.ibm.icu.text.DateFormat$Field YEAR_WOY
meth protected java.lang.Object readResolve() throws java.io.InvalidObjectException
meth public int getCalendarField()
meth public static com.ibm.icu.text.DateFormat$Field ofCalendarField(int)
supr java.text.Format$Field
hfds CAL_FIELDS,CAL_FIELD_COUNT,FIELD_NAME_MAP,calendarField,serialVersionUID

CLSS public com.ibm.icu.text.DateFormatSymbols
cons public init()
cons public init(com.ibm.icu.util.Calendar,com.ibm.icu.util.ULocale)
cons public init(com.ibm.icu.util.Calendar,java.util.Locale)
cons public init(com.ibm.icu.util.ULocale)
cons public init(java.lang.Class<? extends com.ibm.icu.util.Calendar>,com.ibm.icu.util.ULocale)
cons public init(java.lang.Class<? extends com.ibm.icu.util.Calendar>,java.util.Locale)
cons public init(java.util.Locale)
cons public init(java.util.ResourceBundle,com.ibm.icu.util.ULocale)
cons public init(java.util.ResourceBundle,java.util.Locale)
fld public final static int ABBREVIATED = 0
fld public final static int DT_CONTEXT_COUNT = 2
fld public final static int DT_WIDTH_COUNT = 3
fld public final static int FORMAT = 0
fld public final static int NARROW = 2
fld public final static int STANDALONE = 1
fld public final static int WIDE = 1
intf java.io.Serializable
intf java.lang.Cloneable
meth protected void initializeData(com.ibm.icu.util.ULocale,com.ibm.icu.impl.CalendarData)
meth protected void initializeData(com.ibm.icu.util.ULocale,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public final com.ibm.icu.util.ULocale getLocale(com.ibm.icu.util.ULocale$Type)
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.String getLocalPatternChars()
meth public java.lang.String[] getAmPmStrings()
meth public java.lang.String[] getEraNames()
meth public java.lang.String[] getEras()
meth public java.lang.String[] getMonths()
meth public java.lang.String[] getMonths(int,int)
meth public java.lang.String[] getQuarters(int,int)
meth public java.lang.String[] getShortMonths()
meth public java.lang.String[] getShortWeekdays()
meth public java.lang.String[] getWeekdays()
meth public java.lang.String[] getWeekdays(int,int)
meth public java.lang.String[][] getZoneStrings()
meth public static com.ibm.icu.text.DateFormatSymbols getInstance()
meth public static com.ibm.icu.text.DateFormatSymbols getInstance(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.text.DateFormatSymbols getInstance(java.util.Locale)
meth public static com.ibm.icu.util.ULocale[] getAvailableULocales()
meth public static java.util.Locale[] getAvailableLocales()
meth public static java.util.ResourceBundle getDateFormatBundle(com.ibm.icu.util.Calendar,com.ibm.icu.util.ULocale)
meth public static java.util.ResourceBundle getDateFormatBundle(com.ibm.icu.util.Calendar,java.util.Locale)
meth public static java.util.ResourceBundle getDateFormatBundle(java.lang.Class<? extends com.ibm.icu.util.Calendar>,com.ibm.icu.util.ULocale)
meth public static java.util.ResourceBundle getDateFormatBundle(java.lang.Class<? extends com.ibm.icu.util.Calendar>,java.util.Locale)
meth public void setAmPmStrings(java.lang.String[])
meth public void setEraNames(java.lang.String[])
meth public void setEras(java.lang.String[])
meth public void setLocalPatternChars(java.lang.String)
meth public void setMonths(java.lang.String[])
meth public void setMonths(java.lang.String[],int,int)
meth public void setQuarters(java.lang.String[],int,int)
meth public void setShortMonths(java.lang.String[])
meth public void setShortWeekdays(java.lang.String[])
meth public void setWeekdays(java.lang.String[])
meth public void setWeekdays(java.lang.String[],int,int)
meth public void setZoneStrings(java.lang.String[][])
supr java.lang.Object
hfds DEFAULT_GMT_HOUR_PATTERNS,DEFAULT_GMT_PATTERN,DFSCACHE,OFFSET_HM,OFFSET_HMS,OFFSET_NEGATIVE,OFFSET_POSITIVE,actualLocale,ampms,eraNames,eras,gmtFormat,gmtHourFormats,localPatternChars,millisPerHour,months,narrowEras,narrowMonths,narrowWeekdays,patternChars,quarters,requestedLocale,serialVersionUID,shortMonths,shortQuarters,shortWeekdays,standaloneMonths,standaloneNarrowMonths,standaloneNarrowWeekdays,standaloneQuarters,standaloneShortMonths,standaloneShortQuarters,standaloneShortWeekdays,standaloneWeekdays,validLocale,weekdays,zoneStrings,zsformat

CLSS public com.ibm.icu.text.DateIntervalFormat
meth public com.ibm.icu.text.DateFormat getDateFormat()
meth public com.ibm.icu.text.DateIntervalInfo getDateIntervalInfo()
meth public final java.lang.StringBuffer format(com.ibm.icu.util.Calendar,com.ibm.icu.util.Calendar,java.lang.StringBuffer,java.text.FieldPosition)
meth public final java.lang.StringBuffer format(com.ibm.icu.util.DateInterval,java.lang.StringBuffer,java.text.FieldPosition)
meth public final java.lang.StringBuffer format(java.lang.Object,java.lang.StringBuffer,java.text.FieldPosition)
meth public final static com.ibm.icu.text.DateIntervalFormat getInstance(java.lang.String)
meth public final static com.ibm.icu.text.DateIntervalFormat getInstance(java.lang.String,com.ibm.icu.text.DateIntervalInfo)
meth public final static com.ibm.icu.text.DateIntervalFormat getInstance(java.lang.String,com.ibm.icu.util.ULocale)
meth public final static com.ibm.icu.text.DateIntervalFormat getInstance(java.lang.String,com.ibm.icu.util.ULocale,com.ibm.icu.text.DateIntervalInfo)
meth public final static com.ibm.icu.text.DateIntervalFormat getInstance(java.lang.String,java.util.Locale)
meth public final static com.ibm.icu.text.DateIntervalFormat getInstance(java.lang.String,java.util.Locale,com.ibm.icu.text.DateIntervalInfo)
meth public java.lang.Object clone()
meth public java.lang.Object parseObject(java.lang.String,java.text.ParsePosition)
meth public void setDateIntervalInfo(com.ibm.icu.text.DateIntervalInfo)
supr com.ibm.icu.text.UFormat
hfds LOCAL_PATTERN_CACHE,fDateFormat,fFromCalendar,fInfo,fIntervalPatterns,fSkeleton,fToCalendar,serialVersionUID
hcls BestMatchInfo,SkeletonAndItsBestMatch

CLSS public com.ibm.icu.text.DateIntervalInfo
cons public init()
cons public init(com.ibm.icu.util.ULocale)
innr public final static PatternInfo
intf com.ibm.icu.util.Freezable<com.ibm.icu.text.DateIntervalInfo>
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public boolean getDefaultOrder()
meth public boolean isFrozen()
meth public com.ibm.icu.text.DateIntervalInfo cloneAsThawed()
meth public com.ibm.icu.text.DateIntervalInfo freeze()
meth public com.ibm.icu.text.DateIntervalInfo$PatternInfo getIntervalPattern(java.lang.String,int)
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.String getFallbackIntervalPattern()
meth public void setFallbackIntervalPattern(java.lang.String)
meth public void setIntervalPattern(java.lang.String,int,java.lang.String)
supr java.lang.Object
hfds CALENDAR_FIELD_TO_PATTERN_LETTER,DIICACHE,EARLIEST_FIRST_PREFIX,FALLBACK_STRING,LATEST_FIRST_PREFIX,MINIMUM_SUPPORTED_CALENDAR_FIELD,currentSerialVersion,fFallbackIntervalPattern,fFirstDateInPtnIsLaterDate,fIntervalPatterns,frozen,serialVersionUID

CLSS public final static com.ibm.icu.text.DateIntervalInfo$PatternInfo
 outer com.ibm.icu.text.DateIntervalInfo
cons public init(java.lang.String,java.lang.String,boolean)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public boolean firstDateInPtnIsLaterDate()
meth public int hashCode()
meth public java.lang.String getFirstPart()
meth public java.lang.String getSecondPart()
supr java.lang.Object
hfds currentSerialVersion,fFirstDateInPtnIsLaterDate,fIntervalPatternFirstPart,fIntervalPatternSecondPart,serialVersionUID

CLSS public com.ibm.icu.text.DateTimePatternGenerator
cons protected init()
fld public final static int DAY = 7
fld public final static int DAYPERIOD = 10
fld public final static int DAY_OF_WEEK_IN_MONTH = 9
fld public final static int DAY_OF_YEAR = 8
fld public final static int ERA = 0
fld public final static int FRACTIONAL_SECOND = 14
fld public final static int HOUR = 11
fld public final static int MATCH_ALL_FIELDS_LENGTH = 65535
fld public final static int MATCH_HOUR_FIELD_LENGTH = 2048
fld public final static int MATCH_MINUTE_FIELD_LENGTH = 4096
fld public final static int MATCH_NO_OPTIONS = 0
fld public final static int MATCH_SECOND_FIELD_LENGTH = 8192
fld public final static int MINUTE = 12
fld public final static int MONTH = 3
fld public final static int QUARTER = 2
fld public final static int SECOND = 13
fld public final static int TYPE_LIMIT = 16
fld public final static int WEEKDAY = 6
fld public final static int WEEK_OF_MONTH = 5
fld public final static int WEEK_OF_YEAR = 4
fld public final static int YEAR = 1
fld public final static int ZONE = 15
innr public final static PatternInfo
innr public static FormatParser
innr public static VariableField
intf com.ibm.icu.util.Freezable<com.ibm.icu.text.DateTimePatternGenerator>
intf java.lang.Cloneable
meth public boolean isFrozen()
meth public boolean skeletonsAreSimilar(java.lang.String,java.lang.String)
meth public com.ibm.icu.text.DateTimePatternGenerator addPattern(java.lang.String,boolean,com.ibm.icu.text.DateTimePatternGenerator$PatternInfo)
meth public com.ibm.icu.text.DateTimePatternGenerator cloneAsThawed()
meth public com.ibm.icu.text.DateTimePatternGenerator freeze()
meth public java.lang.Object clone()
meth public java.lang.String getAppendItemFormat(int)
meth public java.lang.String getAppendItemName(int)
meth public java.lang.String getBaseSkeleton(java.lang.String)
meth public java.lang.String getBestPattern(java.lang.String)
meth public java.lang.String getBestPattern(java.lang.String,int)
meth public java.lang.String getDateTimeFormat()
meth public java.lang.String getDecimal()
meth public java.lang.String getFields(java.lang.String)
meth public java.lang.String getSkeleton(java.lang.String)
meth public java.lang.String replaceFieldTypes(java.lang.String,java.lang.String)
meth public java.lang.String replaceFieldTypes(java.lang.String,java.lang.String,int)
meth public java.util.Collection<java.lang.String> getRedundants(java.util.Collection<java.lang.String>)
meth public java.util.Map<java.lang.String,java.lang.String> getSkeletons(java.util.Map<java.lang.String,java.lang.String>)
meth public java.util.Set<java.lang.String> getBaseSkeletons(java.util.Set<java.lang.String>)
meth public static boolean isSingleField(java.lang.String)
meth public static com.ibm.icu.text.DateTimePatternGenerator getEmptyInstance()
meth public static com.ibm.icu.text.DateTimePatternGenerator getFrozenInstance(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.text.DateTimePatternGenerator getInstance()
meth public static com.ibm.icu.text.DateTimePatternGenerator getInstance(com.ibm.icu.util.ULocale)
meth public void setAppendItemFormat(int,java.lang.String)
meth public void setAppendItemName(int,java.lang.String)
meth public void setDateTimeFormat(java.lang.String)
meth public void setDecimal(java.lang.String)
supr java.lang.Object
hfds CANONICAL_ITEMS,CANONICAL_SET,CLDR_FIELD_APPEND,CLDR_FIELD_NAME,DATE_MASK,DELTA,DTPNG_CACHE,EXTRA_FIELD,FIELD_NAME,FRACTIONAL_MASK,LONG,MISSING_FIELD,NARROW,NONE,NUMERIC,SECOND_AND_FRACTIONAL_MASK,SHORT,TIME_MASK,_distanceInfo,appendItemFormats,appendItemNames,basePattern_pattern,cldrAvailableFormatKeys,current,dateTimeFormat,decimal,defaultHourFormatChar,fp,frozen,skeleton2pattern,types
hcls DateTimeMatcher,DistanceInfo,PatternWithMatcher,PatternWithSkeletonFlag

CLSS public static com.ibm.icu.text.DateTimePatternGenerator$FormatParser
 outer com.ibm.icu.text.DateTimePatternGenerator
cons public init()
meth public boolean hasDateAndTimeFields()
meth public com.ibm.icu.text.DateTimePatternGenerator$FormatParser set(java.lang.String,boolean)
meth public final com.ibm.icu.text.DateTimePatternGenerator$FormatParser set(java.lang.String)
meth public java.lang.Object quoteLiteral(java.lang.String)
meth public java.lang.String toString()
meth public java.lang.String toString(int,int)
meth public java.util.List<java.lang.Object> getItems()
supr java.lang.Object
hfds items,tokenizer

CLSS public final static com.ibm.icu.text.DateTimePatternGenerator$PatternInfo
 outer com.ibm.icu.text.DateTimePatternGenerator
cons public init()
fld public final static int BASE_CONFLICT = 1
fld public final static int CONFLICT = 2
fld public final static int OK = 0
fld public int status
fld public java.lang.String conflictingPattern
supr java.lang.Object

CLSS public static com.ibm.icu.text.DateTimePatternGenerator$VariableField
 outer com.ibm.icu.text.DateTimePatternGenerator
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
meth protected boolean isNumeric()
meth public int getType()
meth public java.lang.String toString()
supr java.lang.Object
hfds canonicalIndex,string

CLSS public com.ibm.icu.text.DecimalFormat
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,com.ibm.icu.text.DecimalFormatSymbols)
cons public init(java.lang.String,com.ibm.icu.text.DecimalFormatSymbols,com.ibm.icu.text.CurrencyPluralInfo,int)
fld public final static int PAD_AFTER_PREFIX = 1
fld public final static int PAD_AFTER_SUFFIX = 3
fld public final static int PAD_BEFORE_PREFIX = 0
fld public final static int PAD_BEFORE_SUFFIX = 2
meth protected com.ibm.icu.util.Currency getEffectiveCurrency()
meth public boolean areSignificantDigitsUsed()
meth public boolean equals(java.lang.Object)
meth public boolean isDecimalSeparatorAlwaysShown()
meth public boolean isExponentSignAlwaysShown()
meth public boolean isParseBigDecimal()
meth public boolean isScientificNotation()
meth public byte getMinimumExponentDigits()
meth public char getPadCharacter()
meth public com.ibm.icu.math.MathContext getMathContextICU()
meth public com.ibm.icu.text.CurrencyPluralInfo getCurrencyPluralInfo()
meth public com.ibm.icu.text.DecimalFormatSymbols getDecimalFormatSymbols()
meth public int getFormatWidth()
meth public int getGroupingSize()
meth public int getMaximumSignificantDigits()
meth public int getMinimumSignificantDigits()
meth public int getMultiplier()
meth public int getPadPosition()
meth public int getRoundingMode()
meth public int getSecondaryGroupingSize()
meth public int hashCode()
meth public java.lang.Number parse(java.lang.String,java.text.ParsePosition)
meth public java.lang.Object clone()
meth public java.lang.String getNegativePrefix()
meth public java.lang.String getNegativeSuffix()
meth public java.lang.String getPositivePrefix()
meth public java.lang.String getPositiveSuffix()
meth public java.lang.String toLocalizedPattern()
meth public java.lang.String toPattern()
meth public java.lang.StringBuffer format(com.ibm.icu.math.BigDecimal,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.lang.StringBuffer format(double,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.lang.StringBuffer format(java.math.BigDecimal,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.lang.StringBuffer format(java.math.BigInteger,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.lang.StringBuffer format(long,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.math.BigDecimal getRoundingIncrement()
meth public java.math.MathContext getMathContext()
meth public java.text.AttributedCharacterIterator formatToCharacterIterator(java.lang.Object)
meth public void applyLocalizedPattern(java.lang.String)
meth public void applyPattern(java.lang.String)
meth public void setCurrency(com.ibm.icu.util.Currency)
meth public void setCurrencyPluralInfo(com.ibm.icu.text.CurrencyPluralInfo)
meth public void setDecimalFormatSymbols(com.ibm.icu.text.DecimalFormatSymbols)
meth public void setDecimalSeparatorAlwaysShown(boolean)
meth public void setExponentSignAlwaysShown(boolean)
meth public void setFormatWidth(int)
meth public void setGroupingSize(int)
meth public void setMathContext(java.math.MathContext)
meth public void setMathContextICU(com.ibm.icu.math.MathContext)
meth public void setMaximumFractionDigits(int)
meth public void setMaximumIntegerDigits(int)
meth public void setMaximumSignificantDigits(int)
meth public void setMinimumExponentDigits(byte)
meth public void setMinimumFractionDigits(int)
meth public void setMinimumIntegerDigits(int)
meth public void setMinimumSignificantDigits(int)
meth public void setMultiplier(int)
meth public void setNegativePrefix(java.lang.String)
meth public void setNegativeSuffix(java.lang.String)
meth public void setPadCharacter(char)
meth public void setPadPosition(int)
meth public void setParseBigDecimal(boolean)
meth public void setPositivePrefix(java.lang.String)
meth public void setPositiveSuffix(java.lang.String)
meth public void setRoundingIncrement(com.ibm.icu.math.BigDecimal)
meth public void setRoundingIncrement(double)
meth public void setRoundingIncrement(java.math.BigDecimal)
meth public void setRoundingMode(int)
meth public void setScientificNotation(boolean)
meth public void setSecondaryGroupingSize(int)
meth public void setSignificantDigitsUsed(boolean)
supr com.ibm.icu.text.NumberFormat
hfds CURRENCY_SIGN,CURRENCY_SIGN_COUNT_IN_ISO_FORMAT,CURRENCY_SIGN_COUNT_IN_PLURAL_FORMAT,CURRENCY_SIGN_COUNT_IN_SYMBOL_FORMAT,DOUBLE_FRACTION_DIGITS,DOUBLE_INTEGER_DIGITS,EMPTY_SET,MAX_SCIENTIFIC_INTEGER_DIGITS,PARSE_MAX_EXPONENT,PATTERN_DECIMAL_SEPARATOR,PATTERN_DIGIT,PATTERN_EXPONENT,PATTERN_GROUPING_SEPARATOR,PATTERN_MINUS,PATTERN_PAD_ESCAPE,PATTERN_PERCENT,PATTERN_PER_MILLE,PATTERN_PLUS_SIGN,PATTERN_SEPARATOR,PATTERN_SIGNIFICANT_DIGIT,PATTERN_ZERO_DIGIT,QUOTE,STATUS_INFINITE,STATUS_LENGTH,STATUS_POSITIVE,STATUS_UNDERFLOW,affixPatternsForCurrency,attributes,commaEquivalents,currencyChoice,currencyPluralInfo,currencySignCount,currentSerialVersion,decimalSeparatorAlwaysShown,defaultGroupingSeparators,digitList,dotEquivalents,epsilon,exponentSignAlwaysShown,formatPattern,formatWidth,groupingSize,groupingSize2,isReadyForParsing,mathContext,maxSignificantDigits,minExponentDigits,minSignificantDigits,multiplier,negPrefixPattern,negSuffixPattern,negativePrefix,negativeSuffix,pad,padPosition,parseBigDecimal,posPrefixPattern,posSuffixPattern,positivePrefix,positiveSuffix,roundingDouble,roundingDoubleReciprocal,roundingIncrement,roundingIncrementEpsilon,roundingIncrementICU,roundingMode,serialVersionOnStream,serialVersionUID,strictCommaEquivalents,strictDefaultGroupingSeparators,strictDotEquivalents,style,symbols,useExponentialNotation,useSignificantDigits
hcls AffixForCurrency

CLSS public com.ibm.icu.text.DecimalFormatSymbols
cons public init()
cons public init(com.ibm.icu.util.ULocale)
cons public init(java.util.Locale)
fld public final static int CURRENCY_SPC_CURRENCY_MATCH = 0
fld public final static int CURRENCY_SPC_INSERT = 2
fld public final static int CURRENCY_SPC_SURROUNDING_MATCH = 1
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public char getDecimalSeparator()
meth public char getDigit()
meth public char getGroupingSeparator()
meth public char getMinusSign()
meth public char getMonetaryDecimalSeparator()
meth public char getMonetaryGroupingSeparator()
meth public char getPadEscape()
meth public char getPatternSeparator()
meth public char getPerMill()
meth public char getPercent()
meth public char getPlusSign()
meth public char getSignificantDigit()
meth public char getZeroDigit()
meth public com.ibm.icu.util.Currency getCurrency()
meth public com.ibm.icu.util.ULocale getULocale()
meth public final com.ibm.icu.util.ULocale getLocale(com.ibm.icu.util.ULocale$Type)
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.String getCurrencySymbol()
meth public java.lang.String getExponentSeparator()
meth public java.lang.String getInfinity()
meth public java.lang.String getInternationalCurrencySymbol()
meth public java.lang.String getNaN()
meth public java.lang.String getPatternForCurrencySpacing(int,boolean)
meth public java.util.Locale getLocale()
meth public static com.ibm.icu.text.DecimalFormatSymbols getInstance()
meth public static com.ibm.icu.text.DecimalFormatSymbols getInstance(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.text.DecimalFormatSymbols getInstance(java.util.Locale)
meth public static com.ibm.icu.util.ULocale[] getAvailableULocales()
meth public static java.util.Locale[] getAvailableLocales()
meth public void setCurrency(com.ibm.icu.util.Currency)
meth public void setCurrencySymbol(java.lang.String)
meth public void setDecimalSeparator(char)
meth public void setDigit(char)
meth public void setExponentSeparator(java.lang.String)
meth public void setGroupingSeparator(char)
meth public void setInfinity(java.lang.String)
meth public void setInternationalCurrencySymbol(java.lang.String)
meth public void setMinusSign(char)
meth public void setMonetaryDecimalSeparator(char)
meth public void setMonetaryGroupingSeparator(char)
meth public void setNaN(java.lang.String)
meth public void setPadEscape(char)
meth public void setPatternForCurrencySpacing(int,boolean,java.lang.String)
meth public void setPatternSeparator(char)
meth public void setPerMill(char)
meth public void setPercent(char)
meth public void setPlusSign(char)
meth public void setSignificantDigit(char)
meth public void setZeroDigit(char)
supr java.lang.Object
hfds NaN,actualLocale,cachedLocaleData,currency,currencyPattern,currencySpcAfterSym,currencySpcBeforeSym,currencySymbol,currentSerialVersion,decimalSeparator,digit,exponentSeparator,exponential,groupingSeparator,infinity,intlCurrencySymbol,minusSign,monetaryGroupingSeparator,monetarySeparator,padEscape,patternSeparator,perMill,percent,plusSign,requestedLocale,serialVersionOnStream,serialVersionUID,sigDigit,ulocale,validLocale,zeroDigit

CLSS public com.ibm.icu.text.DictionaryBasedBreakIterator
cons protected init(java.io.InputStream) throws java.io.IOException
cons public init(java.io.InputStream,java.io.InputStream) throws java.io.IOException
cons public init(java.lang.String,java.io.InputStream) throws java.io.IOException
meth protected int handleNext()
meth public int first()
meth public int following(int)
meth public int getRuleStatus()
meth public int getRuleStatusVec(int[])
meth public int last()
meth public int preceding(int)
meth public int previous()
meth public void setText(java.text.CharacterIterator)
supr com.ibm.icu.text.RuleBasedBreakIterator
hfds cachedBreakPositions,dictionary,positionInCache,usingCTDictionary

CLSS public abstract com.ibm.icu.text.DurationFormat
cons protected init()
cons protected init(com.ibm.icu.util.ULocale)
meth public abstract java.lang.String formatDurationFrom(long,long)
meth public abstract java.lang.String formatDurationFromNow(long)
meth public abstract java.lang.String formatDurationFromNowTo(java.util.Date)
meth public abstract java.lang.StringBuffer format(java.lang.Object,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.lang.Object parseObject(java.lang.String,java.text.ParsePosition)
meth public static com.ibm.icu.text.DurationFormat getInstance(com.ibm.icu.util.ULocale)
supr com.ibm.icu.text.UFormat
hfds serialVersionUID

CLSS public com.ibm.icu.text.FilteredNormalizer2
cons public init(com.ibm.icu.text.Normalizer2,com.ibm.icu.text.UnicodeSet)
meth public boolean hasBoundaryAfter(int)
meth public boolean hasBoundaryBefore(int)
meth public boolean isInert(int)
meth public boolean isNormalized(java.lang.CharSequence)
meth public com.ibm.icu.text.Normalizer$QuickCheckResult quickCheck(java.lang.CharSequence)
meth public int spanQuickCheckYes(java.lang.CharSequence)
meth public java.lang.Appendable normalize(java.lang.CharSequence,java.lang.Appendable)
meth public java.lang.StringBuilder append(java.lang.StringBuilder,java.lang.CharSequence)
meth public java.lang.StringBuilder normalize(java.lang.CharSequence,java.lang.StringBuilder)
meth public java.lang.StringBuilder normalizeSecondAndAppend(java.lang.StringBuilder,java.lang.CharSequence)
supr com.ibm.icu.text.Normalizer2
hfds norm2,set

CLSS public final com.ibm.icu.text.IDNA
fld public final static int ALLOW_UNASSIGNED = 1
fld public final static int DEFAULT = 0
fld public final static int USE_STD3_RULES = 2
meth public static int compare(com.ibm.icu.text.UCharacterIterator,com.ibm.icu.text.UCharacterIterator,int) throws com.ibm.icu.text.StringPrepParseException
meth public static int compare(java.lang.String,java.lang.String,int) throws com.ibm.icu.text.StringPrepParseException
meth public static int compare(java.lang.StringBuffer,java.lang.StringBuffer,int) throws com.ibm.icu.text.StringPrepParseException
meth public static java.lang.StringBuffer convertIDNToASCII(com.ibm.icu.text.UCharacterIterator,int) throws com.ibm.icu.text.StringPrepParseException
meth public static java.lang.StringBuffer convertIDNToASCII(java.lang.String,int) throws com.ibm.icu.text.StringPrepParseException
meth public static java.lang.StringBuffer convertIDNToASCII(java.lang.StringBuffer,int) throws com.ibm.icu.text.StringPrepParseException
meth public static java.lang.StringBuffer convertIDNToUnicode(com.ibm.icu.text.UCharacterIterator,int) throws com.ibm.icu.text.StringPrepParseException
meth public static java.lang.StringBuffer convertIDNToUnicode(java.lang.String,int) throws com.ibm.icu.text.StringPrepParseException
meth public static java.lang.StringBuffer convertIDNToUnicode(java.lang.StringBuffer,int) throws com.ibm.icu.text.StringPrepParseException
meth public static java.lang.StringBuffer convertToASCII(com.ibm.icu.text.UCharacterIterator,int) throws com.ibm.icu.text.StringPrepParseException
meth public static java.lang.StringBuffer convertToASCII(java.lang.String,int) throws com.ibm.icu.text.StringPrepParseException
meth public static java.lang.StringBuffer convertToASCII(java.lang.StringBuffer,int) throws com.ibm.icu.text.StringPrepParseException
meth public static java.lang.StringBuffer convertToUnicode(com.ibm.icu.text.UCharacterIterator,int) throws com.ibm.icu.text.StringPrepParseException
meth public static java.lang.StringBuffer convertToUnicode(java.lang.String,int) throws com.ibm.icu.text.StringPrepParseException
meth public static java.lang.StringBuffer convertToUnicode(java.lang.StringBuffer,int) throws com.ibm.icu.text.StringPrepParseException
supr java.lang.Object
hfds ACE_PREFIX,CAPITAL_A,CAPITAL_Z,FULL_STOP,HYPHEN,LOWER_CASE_DELTA,MAX_DOMAIN_NAME_LENGTH,MAX_LABEL_LENGTH,namePrep,singleton

CLSS public com.ibm.icu.text.IndexCharacters
cons public init(com.ibm.icu.util.ULocale)
cons public init(com.ibm.icu.util.ULocale,com.ibm.icu.text.UnicodeSet,com.ibm.icu.text.Collator)
meth public com.ibm.icu.util.ULocale getLocale()
meth public java.util.Collection<java.lang.String> getIndexCharacters()
meth public java.util.List<java.lang.String> getNoDistinctSorting()
meth public java.util.List<java.lang.String> getNotAlphabetic()
meth public java.util.Map<java.lang.String,java.util.Set<java.lang.String>> getAlreadyIn()
supr java.lang.Object
hfds ALPHABETIC,CGJ,CORE_LATIN,ETHIOPIC,HANGUL,alreadyIn,comparator,indexCharacters,locale,noDistinctSorting,notAlphabetic
hcls PreferenceComparator

CLSS public abstract com.ibm.icu.text.LocaleDisplayNames
cons protected init()
innr public final static !enum DialectHandling
meth public abstract com.ibm.icu.text.LocaleDisplayNames$DialectHandling getDialectHandling()
meth public abstract com.ibm.icu.util.ULocale getLocale()
meth public abstract java.lang.String keyDisplayName(java.lang.String)
meth public abstract java.lang.String keyValueDisplayName(java.lang.String,java.lang.String)
meth public abstract java.lang.String languageDisplayName(java.lang.String)
meth public abstract java.lang.String localeDisplayName(com.ibm.icu.util.ULocale)
meth public abstract java.lang.String localeDisplayName(java.lang.String)
meth public abstract java.lang.String localeDisplayName(java.util.Locale)
meth public abstract java.lang.String regionDisplayName(java.lang.String)
meth public abstract java.lang.String scriptDisplayName(int)
meth public abstract java.lang.String scriptDisplayName(java.lang.String)
meth public abstract java.lang.String variantDisplayName(java.lang.String)
meth public static com.ibm.icu.text.LocaleDisplayNames getInstance(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.text.LocaleDisplayNames getInstance(com.ibm.icu.util.ULocale,com.ibm.icu.text.LocaleDisplayNames$DialectHandling)
supr java.lang.Object

CLSS public final static !enum com.ibm.icu.text.LocaleDisplayNames$DialectHandling
 outer com.ibm.icu.text.LocaleDisplayNames
fld public final static com.ibm.icu.text.LocaleDisplayNames$DialectHandling DIALECT_NAMES
fld public final static com.ibm.icu.text.LocaleDisplayNames$DialectHandling STANDARD_NAMES
meth public static com.ibm.icu.text.LocaleDisplayNames$DialectHandling valueOf(java.lang.String)
meth public static com.ibm.icu.text.LocaleDisplayNames$DialectHandling[] values()
supr java.lang.Enum<com.ibm.icu.text.LocaleDisplayNames$DialectHandling>

CLSS public abstract com.ibm.icu.text.MeasureFormat
cons protected init()
meth public static com.ibm.icu.text.MeasureFormat getCurrencyFormat()
meth public static com.ibm.icu.text.MeasureFormat getCurrencyFormat(com.ibm.icu.util.ULocale)
supr com.ibm.icu.text.UFormat
hfds serialVersionUID

CLSS public com.ibm.icu.text.MessageFormat
cons public init(java.lang.String)
cons public init(java.lang.String,com.ibm.icu.util.ULocale)
cons public init(java.lang.String,java.util.Locale)
innr public static Field
meth public boolean equals(java.lang.Object)
meth public boolean usesNamedArguments()
meth public com.ibm.icu.util.ULocale getULocale()
meth public final java.lang.StringBuffer format(java.lang.Object,java.lang.StringBuffer,java.text.FieldPosition)
meth public final java.lang.StringBuffer format(java.lang.Object[],java.lang.StringBuffer,java.text.FieldPosition)
meth public final java.lang.StringBuffer format(java.util.Map<java.lang.String,java.lang.Object>,java.lang.StringBuffer,java.text.FieldPosition)
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.Object parseObject(java.lang.String,java.text.ParsePosition)
meth public java.lang.Object[] parse(java.lang.String) throws java.text.ParseException
meth public java.lang.Object[] parse(java.lang.String,java.text.ParsePosition)
meth public java.lang.String toPattern()
meth public java.text.AttributedCharacterIterator formatToCharacterIterator(java.lang.Object)
meth public java.text.Format getFormatByArgumentName(java.lang.String)
meth public java.text.Format[] getFormats()
meth public java.text.Format[] getFormatsByArgumentIndex()
meth public java.util.Locale getLocale()
meth public java.util.Map<java.lang.String,java.lang.Object> parseToMap(java.lang.String) throws java.text.ParseException
meth public java.util.Map<java.lang.String,java.lang.Object> parseToMap(java.lang.String,java.text.ParsePosition)
meth public java.util.Set<java.lang.String> getFormatArgumentNames()
meth public static java.lang.String autoQuoteApostrophe(java.lang.String)
meth public static java.lang.String format(java.lang.String,java.lang.Object[])
meth public static java.lang.String format(java.lang.String,java.util.Map<java.lang.String,java.lang.Object>)
meth public void applyPattern(java.lang.String)
meth public void setFormat(int,java.text.Format)
meth public void setFormatByArgumentIndex(int,java.text.Format)
meth public void setFormatByArgumentName(java.lang.String,java.text.Format)
meth public void setFormats(java.text.Format[])
meth public void setFormatsByArgumentIndex(java.text.Format[])
meth public void setFormatsByArgumentName(java.util.Map<java.lang.String,java.text.Format>)
meth public void setLocale(com.ibm.icu.util.ULocale)
meth public void setLocale(java.util.Locale)
supr com.ibm.icu.text.UFormat
hfds CURLY_BRACE_LEFT,CURLY_BRACE_RIGHT,DATE_MODIFIER_EMPTY,DATE_MODIFIER_FULL,DATE_MODIFIER_LONG,DATE_MODIFIER_MEDIUM,DATE_MODIFIER_SHORT,INITIAL_FORMATS,MODIFIER_CURRENCY,MODIFIER_EMPTY,MODIFIER_INTEGER,MODIFIER_PERCENT,SINGLE_QUOTE,STATE_INITIAL,STATE_IN_QUOTE,STATE_MSG_ELEMENT,STATE_SINGLE_QUOTE,TYPE_CHOICE,TYPE_DATE,TYPE_DURATION,TYPE_EMPTY,TYPE_NUMBER,TYPE_ORDINAL,TYPE_PLURAL,TYPE_SELECT,TYPE_SPELLOUT,TYPE_TIME,argumentNames,argumentNamesAreNumeric,argumentNumbers,dateModifierList,formats,locale,maxOffset,modifierList,offsets,pattern,serialVersionUID,typeList,ulocale

CLSS public static com.ibm.icu.text.MessageFormat$Field
 outer com.ibm.icu.text.MessageFormat
cons protected init(java.lang.String)
fld public final static com.ibm.icu.text.MessageFormat$Field ARGUMENT
meth protected java.lang.Object readResolve() throws java.io.InvalidObjectException
supr java.text.Format$Field
hfds serialVersionUID

CLSS public final com.ibm.icu.text.Normalizer
cons public init(com.ibm.icu.text.UCharacterIterator,com.ibm.icu.text.Normalizer$Mode,int)
cons public init(java.lang.String,com.ibm.icu.text.Normalizer$Mode,int)
cons public init(java.text.CharacterIterator,com.ibm.icu.text.Normalizer$Mode,int)
fld public final static com.ibm.icu.text.Normalizer$Mode COMPOSE
fld public final static com.ibm.icu.text.Normalizer$Mode COMPOSE_COMPAT
fld public final static com.ibm.icu.text.Normalizer$Mode DECOMP
fld public final static com.ibm.icu.text.Normalizer$Mode DECOMP_COMPAT
fld public final static com.ibm.icu.text.Normalizer$Mode DEFAULT
fld public final static com.ibm.icu.text.Normalizer$Mode FCD
fld public final static com.ibm.icu.text.Normalizer$Mode NFC
fld public final static com.ibm.icu.text.Normalizer$Mode NFD
fld public final static com.ibm.icu.text.Normalizer$Mode NFKC
fld public final static com.ibm.icu.text.Normalizer$Mode NFKD
fld public final static com.ibm.icu.text.Normalizer$Mode NONE
fld public final static com.ibm.icu.text.Normalizer$Mode NO_OP
fld public final static com.ibm.icu.text.Normalizer$QuickCheckResult MAYBE
fld public final static com.ibm.icu.text.Normalizer$QuickCheckResult NO
fld public final static com.ibm.icu.text.Normalizer$QuickCheckResult YES
fld public final static int COMPARE_CODE_POINT_ORDER = 32768
fld public final static int COMPARE_IGNORE_CASE = 65536
fld public final static int COMPARE_NORM_OPTIONS_SHIFT = 20
fld public final static int DONE = -1
fld public final static int FOLD_CASE_DEFAULT = 0
fld public final static int FOLD_CASE_EXCLUDE_SPECIAL_I = 1
fld public final static int IGNORE_HANGUL = 1
fld public final static int INPUT_IS_FCD = 131072
fld public final static int UNICODE_3_2 = 32
innr public abstract static Mode
innr public final static QuickCheckResult
intf java.lang.Cloneable
meth public com.ibm.icu.text.Normalizer$Mode getMode()
meth public int current()
meth public int endIndex()
meth public int first()
meth public int getBeginIndex()
meth public int getEndIndex()
meth public int getIndex()
meth public int getLength()
meth public int getOption(int)
meth public int getText(char[])
meth public int last()
meth public int next()
meth public int previous()
meth public int setIndex(int)
meth public int startIndex()
meth public java.lang.Object clone()
meth public java.lang.String getText()
meth public static boolean isNormalized(char[],int,int,com.ibm.icu.text.Normalizer$Mode,int)
meth public static boolean isNormalized(int,com.ibm.icu.text.Normalizer$Mode,int)
meth public static boolean isNormalized(java.lang.String,com.ibm.icu.text.Normalizer$Mode,int)
meth public static com.ibm.icu.text.Normalizer$QuickCheckResult quickCheck(char[],com.ibm.icu.text.Normalizer$Mode,int)
meth public static com.ibm.icu.text.Normalizer$QuickCheckResult quickCheck(char[],int,int,com.ibm.icu.text.Normalizer$Mode,int)
meth public static com.ibm.icu.text.Normalizer$QuickCheckResult quickCheck(java.lang.String,com.ibm.icu.text.Normalizer$Mode)
meth public static com.ibm.icu.text.Normalizer$QuickCheckResult quickCheck(java.lang.String,com.ibm.icu.text.Normalizer$Mode,int)
meth public static int compare(char[],char[],int)
meth public static int compare(char[],int,int,char[],int,int,int)
meth public static int compare(int,int,int)
meth public static int compare(int,java.lang.String,int)
meth public static int compare(java.lang.String,java.lang.String,int)
meth public static int compose(char[],char[],boolean,int)
meth public static int compose(char[],int,int,char[],int,int,boolean,int)
meth public static int concatenate(char[],int,int,char[],int,int,char[],int,int,com.ibm.icu.text.Normalizer$Mode,int)
meth public static int decompose(char[],char[],boolean,int)
meth public static int decompose(char[],int,int,char[],int,int,boolean,int)
meth public static int getFC_NFKC_Closure(int,char[])
meth public static int normalize(char[],char[],com.ibm.icu.text.Normalizer$Mode,int)
meth public static int normalize(char[],int,int,char[],int,int,com.ibm.icu.text.Normalizer$Mode,int)
meth public static java.lang.String compose(java.lang.String,boolean)
meth public static java.lang.String compose(java.lang.String,boolean,int)
meth public static java.lang.String concatenate(char[],char[],com.ibm.icu.text.Normalizer$Mode,int)
meth public static java.lang.String concatenate(java.lang.String,java.lang.String,com.ibm.icu.text.Normalizer$Mode,int)
meth public static java.lang.String decompose(java.lang.String,boolean)
meth public static java.lang.String decompose(java.lang.String,boolean,int)
meth public static java.lang.String getFC_NFKC_Closure(int)
meth public static java.lang.String normalize(int,com.ibm.icu.text.Normalizer$Mode)
meth public static java.lang.String normalize(int,com.ibm.icu.text.Normalizer$Mode,int)
meth public static java.lang.String normalize(java.lang.String,com.ibm.icu.text.Normalizer$Mode)
meth public static java.lang.String normalize(java.lang.String,com.ibm.icu.text.Normalizer$Mode,int)
meth public void reset()
meth public void setIndexOnly(int)
meth public void setMode(com.ibm.icu.text.Normalizer$Mode)
meth public void setOption(int,boolean)
meth public void setText(char[])
meth public void setText(com.ibm.icu.text.UCharacterIterator)
meth public void setText(java.lang.String)
meth public void setText(java.lang.StringBuffer)
meth public void setText(java.text.CharacterIterator)
supr java.lang.Object
hfds COMPARE_EQUIV,buffer,bufferPos,currentIndex,mode,nextIndex,norm2,options,text
hcls CharsAppendable,CmpEquivLevel,FCD32ModeImpl,FCDMode,FCDModeImpl,ModeImpl,NFC32ModeImpl,NFCMode,NFCModeImpl,NFD32ModeImpl,NFDMode,NFDModeImpl,NFKC32ModeImpl,NFKCMode,NFKCModeImpl,NFKD32ModeImpl,NFKDMode,NFKDModeImpl,NONEMode,Unicode32

CLSS public abstract static com.ibm.icu.text.Normalizer$Mode
 outer com.ibm.icu.text.Normalizer
cons public init()
meth protected abstract com.ibm.icu.text.Normalizer2 getNormalizer2(int)
supr java.lang.Object

CLSS public final static com.ibm.icu.text.Normalizer$QuickCheckResult
 outer com.ibm.icu.text.Normalizer
supr java.lang.Object

CLSS public abstract com.ibm.icu.text.Normalizer2
cons protected init()
innr public final static !enum Mode
meth public abstract boolean hasBoundaryAfter(int)
meth public abstract boolean hasBoundaryBefore(int)
meth public abstract boolean isInert(int)
meth public abstract boolean isNormalized(java.lang.CharSequence)
meth public abstract com.ibm.icu.text.Normalizer$QuickCheckResult quickCheck(java.lang.CharSequence)
meth public abstract int spanQuickCheckYes(java.lang.CharSequence)
meth public abstract java.lang.Appendable normalize(java.lang.CharSequence,java.lang.Appendable)
meth public abstract java.lang.StringBuilder append(java.lang.StringBuilder,java.lang.CharSequence)
meth public abstract java.lang.StringBuilder normalize(java.lang.CharSequence,java.lang.StringBuilder)
meth public abstract java.lang.StringBuilder normalizeSecondAndAppend(java.lang.StringBuilder,java.lang.CharSequence)
meth public java.lang.String normalize(java.lang.CharSequence)
meth public static com.ibm.icu.text.Normalizer2 getInstance(java.io.InputStream,java.lang.String,com.ibm.icu.text.Normalizer2$Mode)
supr java.lang.Object

CLSS public final static !enum com.ibm.icu.text.Normalizer2$Mode
 outer com.ibm.icu.text.Normalizer2
fld public final static com.ibm.icu.text.Normalizer2$Mode COMPOSE
fld public final static com.ibm.icu.text.Normalizer2$Mode COMPOSE_CONTIGUOUS
fld public final static com.ibm.icu.text.Normalizer2$Mode DECOMPOSE
fld public final static com.ibm.icu.text.Normalizer2$Mode FCD
meth public static com.ibm.icu.text.Normalizer2$Mode valueOf(java.lang.String)
meth public static com.ibm.icu.text.Normalizer2$Mode[] values()
supr java.lang.Enum<com.ibm.icu.text.Normalizer2$Mode>

CLSS public abstract com.ibm.icu.text.NumberFormat
cons public init()
fld public final static int CURRENCYSTYLE = 1
fld public final static int FRACTION_FIELD = 1
fld public final static int INTEGERSTYLE = 4
fld public final static int INTEGER_FIELD = 0
fld public final static int ISOCURRENCYSTYLE = 5
fld public final static int NUMBERSTYLE = 0
fld public final static int PERCENTSTYLE = 2
fld public final static int PLURALCURRENCYSTYLE = 6
fld public final static int SCIENTIFICSTYLE = 3
innr public abstract static NumberFormatFactory
innr public abstract static SimpleNumberFormatFactory
innr public static Field
meth protected com.ibm.icu.util.Currency getEffectiveCurrency()
meth protected static java.lang.String getPattern(com.ibm.icu.util.ULocale,int)
meth protected static java.lang.String getPattern(java.util.Locale,int)
meth public abstract java.lang.Number parse(java.lang.String,java.text.ParsePosition)
meth public abstract java.lang.StringBuffer format(com.ibm.icu.math.BigDecimal,java.lang.StringBuffer,java.text.FieldPosition)
meth public abstract java.lang.StringBuffer format(double,java.lang.StringBuffer,java.text.FieldPosition)
meth public abstract java.lang.StringBuffer format(java.math.BigDecimal,java.lang.StringBuffer,java.text.FieldPosition)
meth public abstract java.lang.StringBuffer format(java.math.BigInteger,java.lang.StringBuffer,java.text.FieldPosition)
meth public abstract java.lang.StringBuffer format(long,java.lang.StringBuffer,java.text.FieldPosition)
meth public boolean equals(java.lang.Object)
meth public boolean isGroupingUsed()
meth public boolean isParseIntegerOnly()
meth public boolean isParseStrict()
meth public com.ibm.icu.util.Currency getCurrency()
meth public final java.lang.Object parseObject(java.lang.String,java.text.ParsePosition)
meth public final java.lang.String format(com.ibm.icu.math.BigDecimal)
meth public final java.lang.String format(com.ibm.icu.util.CurrencyAmount)
meth public final java.lang.String format(double)
meth public final java.lang.String format(java.math.BigDecimal)
meth public final java.lang.String format(java.math.BigInteger)
meth public final java.lang.String format(long)
meth public final static com.ibm.icu.text.NumberFormat getCurrencyInstance()
meth public final static com.ibm.icu.text.NumberFormat getInstance()
meth public final static com.ibm.icu.text.NumberFormat getInstance(int)
meth public final static com.ibm.icu.text.NumberFormat getIntegerInstance()
meth public final static com.ibm.icu.text.NumberFormat getNumberInstance()
meth public final static com.ibm.icu.text.NumberFormat getPercentInstance()
meth public final static com.ibm.icu.text.NumberFormat getScientificInstance()
meth public int getMaximumFractionDigits()
meth public int getMaximumIntegerDigits()
meth public int getMinimumFractionDigits()
meth public int getMinimumIntegerDigits()
meth public int getRoundingMode()
meth public int hashCode()
meth public java.lang.Number parse(java.lang.String) throws java.text.ParseException
meth public java.lang.Object clone()
meth public java.lang.StringBuffer format(com.ibm.icu.util.CurrencyAmount,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.lang.StringBuffer format(java.lang.Object,java.lang.StringBuffer,java.text.FieldPosition)
meth public static boolean unregister(java.lang.Object)
meth public static com.ibm.icu.text.NumberFormat getCurrencyInstance(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.text.NumberFormat getCurrencyInstance(java.util.Locale)
meth public static com.ibm.icu.text.NumberFormat getInstance(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.text.NumberFormat getInstance(com.ibm.icu.util.ULocale,int)
meth public static com.ibm.icu.text.NumberFormat getInstance(java.util.Locale)
meth public static com.ibm.icu.text.NumberFormat getInstance(java.util.Locale,int)
meth public static com.ibm.icu.text.NumberFormat getIntegerInstance(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.text.NumberFormat getIntegerInstance(java.util.Locale)
meth public static com.ibm.icu.text.NumberFormat getNumberInstance(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.text.NumberFormat getNumberInstance(java.util.Locale)
meth public static com.ibm.icu.text.NumberFormat getPercentInstance(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.text.NumberFormat getPercentInstance(java.util.Locale)
meth public static com.ibm.icu.text.NumberFormat getScientificInstance(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.text.NumberFormat getScientificInstance(java.util.Locale)
meth public static com.ibm.icu.util.ULocale[] getAvailableULocales()
meth public static java.lang.Object registerFactory(com.ibm.icu.text.NumberFormat$NumberFormatFactory)
meth public static java.util.Locale[] getAvailableLocales()
meth public void setCurrency(com.ibm.icu.util.Currency)
meth public void setGroupingUsed(boolean)
meth public void setMaximumFractionDigits(int)
meth public void setMaximumIntegerDigits(int)
meth public void setMinimumFractionDigits(int)
meth public void setMinimumIntegerDigits(int)
meth public void setParseIntegerOnly(boolean)
meth public void setParseStrict(boolean)
meth public void setRoundingMode(int)
supr com.ibm.icu.text.UFormat
hfds currency,currentSerialVersion,doubleCurrencySign,doubleCurrencyStr,groupingUsed,maxFractionDigits,maxIntegerDigits,maximumFractionDigits,maximumIntegerDigits,minFractionDigits,minIntegerDigits,minimumFractionDigits,minimumIntegerDigits,parseIntegerOnly,parseStrict,serialVersionOnStream,serialVersionUID,shim
hcls NumberFormatShim

CLSS public static com.ibm.icu.text.NumberFormat$Field
 outer com.ibm.icu.text.NumberFormat
cons protected init(java.lang.String)
fld public final static com.ibm.icu.text.NumberFormat$Field CURRENCY
fld public final static com.ibm.icu.text.NumberFormat$Field DECIMAL_SEPARATOR
fld public final static com.ibm.icu.text.NumberFormat$Field EXPONENT
fld public final static com.ibm.icu.text.NumberFormat$Field EXPONENT_SIGN
fld public final static com.ibm.icu.text.NumberFormat$Field EXPONENT_SYMBOL
fld public final static com.ibm.icu.text.NumberFormat$Field FRACTION
fld public final static com.ibm.icu.text.NumberFormat$Field GROUPING_SEPARATOR
fld public final static com.ibm.icu.text.NumberFormat$Field INTEGER
fld public final static com.ibm.icu.text.NumberFormat$Field PERCENT
fld public final static com.ibm.icu.text.NumberFormat$Field PERMILLE
fld public final static com.ibm.icu.text.NumberFormat$Field SIGN
meth protected java.lang.Object readResolve() throws java.io.InvalidObjectException
supr java.text.Format$Field
hfds serialVersionUID

CLSS public abstract static com.ibm.icu.text.NumberFormat$NumberFormatFactory
 outer com.ibm.icu.text.NumberFormat
cons protected init()
fld public final static int FORMAT_CURRENCY = 1
fld public final static int FORMAT_INTEGER = 4
fld public final static int FORMAT_NUMBER = 0
fld public final static int FORMAT_PERCENT = 2
fld public final static int FORMAT_SCIENTIFIC = 3
meth public abstract java.util.Set<java.lang.String> getSupportedLocaleNames()
meth public boolean visible()
meth public com.ibm.icu.text.NumberFormat createFormat(com.ibm.icu.util.ULocale,int)
meth public com.ibm.icu.text.NumberFormat createFormat(java.util.Locale,int)
supr java.lang.Object

CLSS public abstract static com.ibm.icu.text.NumberFormat$SimpleNumberFormatFactory
 outer com.ibm.icu.text.NumberFormat
cons public init(com.ibm.icu.util.ULocale)
cons public init(com.ibm.icu.util.ULocale,boolean)
cons public init(java.util.Locale)
cons public init(java.util.Locale,boolean)
meth public final boolean visible()
meth public final java.util.Set<java.lang.String> getSupportedLocaleNames()
supr com.ibm.icu.text.NumberFormat$NumberFormatFactory
hfds localeNames,visible

CLSS public com.ibm.icu.text.PluralFormat
cons public init()
cons public init(com.ibm.icu.text.PluralRules)
cons public init(com.ibm.icu.text.PluralRules,java.lang.String)
cons public init(com.ibm.icu.util.ULocale)
cons public init(com.ibm.icu.util.ULocale,com.ibm.icu.text.PluralRules)
cons public init(com.ibm.icu.util.ULocale,com.ibm.icu.text.PluralRules,java.lang.String)
cons public init(com.ibm.icu.util.ULocale,java.lang.String)
cons public init(java.lang.String)
meth public boolean equals(com.ibm.icu.text.PluralFormat)
meth public boolean equals(java.lang.Object)
meth public final java.lang.String format(double)
meth public int hashCode()
meth public java.lang.Number parse(java.lang.String,java.text.ParsePosition)
meth public java.lang.Object parseObject(java.lang.String,java.text.ParsePosition)
meth public java.lang.String toPattern()
meth public java.lang.String toString()
meth public java.lang.StringBuffer format(java.lang.Object,java.lang.StringBuffer,java.text.FieldPosition)
meth public void applyPattern(java.lang.String)
meth public void setLocale(com.ibm.icu.util.ULocale)
meth public void setNumberFormat(com.ibm.icu.text.NumberFormat)
supr com.ibm.icu.text.UFormat
hfds numberFormat,parsedValues,pattern,pluralRules,serialVersionUID,ulocale

CLSS public com.ibm.icu.text.PluralRules
fld public final static com.ibm.icu.text.PluralRules DEFAULT
fld public final static java.lang.String KEYWORD_FEW = "few"
fld public final static java.lang.String KEYWORD_MANY = "many"
fld public final static java.lang.String KEYWORD_ONE = "one"
fld public final static java.lang.String KEYWORD_OTHER = "other"
fld public final static java.lang.String KEYWORD_TWO = "two"
fld public final static java.lang.String KEYWORD_ZERO = "zero"
intf java.io.Serializable
meth public boolean equals(com.ibm.icu.text.PluralRules)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String select(double)
meth public java.lang.String toString()
meth public java.util.Set<java.lang.String> getKeywords()
meth public static com.ibm.icu.text.PluralRules createRules(java.lang.String)
meth public static com.ibm.icu.text.PluralRules forLocale(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.text.PluralRules parseDescription(java.lang.String) throws java.text.ParseException
meth public static com.ibm.icu.util.ULocale getFunctionalEquivalent(com.ibm.icu.util.ULocale,boolean[])
meth public static com.ibm.icu.util.ULocale[] getAvailableULocales()
supr java.lang.Object
hfds CONT_CHARS,DEFAULT_RULE,NO_CONSTRAINT,START_CHARS,keywords,repeatLimit,rules,serialVersionUID
hcls AndConstraint,BinaryConstraint,ConstrainedRule,Constraint,OrConstraint,RangeConstraint,Rule,RuleChain,RuleList

CLSS public final com.ibm.icu.text.RawCollationKey
cons public init()
cons public init(byte[])
cons public init(byte[],int)
cons public init(int)
meth public int compareTo(com.ibm.icu.text.RawCollationKey)
supr com.ibm.icu.util.ByteArrayWrapper

CLSS public abstract interface com.ibm.icu.text.RbnfLenientScanner
meth public abstract boolean allIgnorable(java.lang.String)
meth public abstract int prefixLength(java.lang.String,java.lang.String)
meth public abstract int[] findText(java.lang.String,java.lang.String,int)

CLSS public abstract interface com.ibm.icu.text.RbnfLenientScannerProvider
meth public abstract com.ibm.icu.text.RbnfLenientScanner get(com.ibm.icu.util.ULocale,java.lang.String)

CLSS public com.ibm.icu.text.RbnfScannerProviderImpl
cons public init()
intf com.ibm.icu.text.RbnfLenientScannerProvider
meth protected com.ibm.icu.text.RbnfLenientScanner createScanner(com.ibm.icu.util.ULocale,java.lang.String)
meth public com.ibm.icu.text.RbnfLenientScanner get(com.ibm.icu.util.ULocale,java.lang.String)
supr java.lang.Object
hfds cache
hcls RbnfLenientScannerImpl

CLSS public abstract interface com.ibm.icu.text.Replaceable
meth public abstract boolean hasMetaData()
meth public abstract char charAt(int)
meth public abstract int char32At(int)
meth public abstract int length()
meth public abstract void copy(int,int,int)
meth public abstract void getChars(int,int,char[],int)
meth public abstract void replace(int,int,char[],int,int)
meth public abstract void replace(int,int,java.lang.String)

CLSS public com.ibm.icu.text.ReplaceableString
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.StringBuffer)
intf com.ibm.icu.text.Replaceable
meth public boolean hasMetaData()
meth public char charAt(int)
meth public int char32At(int)
meth public int length()
meth public java.lang.String substring(int,int)
meth public java.lang.String toString()
meth public void copy(int,int,int)
meth public void getChars(int,int,char[],int)
meth public void replace(int,int,char[],int,int)
meth public void replace(int,int,java.lang.String)
supr java.lang.Object
hfds buf

CLSS public com.ibm.icu.text.RuleBasedBreakIterator
cons public init()
cons public init(java.lang.String)
fld protected int fDictionaryCharCount
fld protected java.lang.Object fRData
fld protected static java.lang.String fDebugEnv
fld public final static int WORD_IDEO = 400
fld public final static int WORD_IDEO_LIMIT = 500
fld public final static int WORD_KANA = 300
fld public final static int WORD_KANA_LIMIT = 400
fld public final static int WORD_LETTER = 200
fld public final static int WORD_LETTER_LIMIT = 300
fld public final static int WORD_NONE = 0
fld public final static int WORD_NONE_LIMIT = 100
fld public final static int WORD_NUMBER = 100
fld public final static int WORD_NUMBER_LIMIT = 200
fld public static boolean fTrace
meth protected final static void checkOffset(int,java.text.CharacterIterator)
meth public boolean equals(java.lang.Object)
meth public boolean isBoundary(int)
meth public int current()
meth public int first()
meth public int following(int)
meth public int getRuleStatus()
meth public int getRuleStatusVec(int[])
meth public int hashCode()
meth public int last()
meth public int next()
meth public int next(int)
meth public int preceding(int)
meth public int previous()
meth public java.lang.Object clone()
meth public java.lang.String toString()
meth public java.text.CharacterIterator getText()
meth public static com.ibm.icu.text.RuleBasedBreakIterator getInstanceFromCompiledRules(java.io.InputStream) throws java.io.IOException
meth public void dump()
meth public void setText(java.text.CharacterIterator)
supr com.ibm.icu.text.BreakIterator
hfds CI_DONE32,RBBI_DEBUG_ARG,RBBI_END,RBBI_RUN,RBBI_START,START_STATE,STOP_STATE,debugInitDone,fLastRuleStatusIndex,fLastStatusIndexValid,fText

CLSS public final com.ibm.icu.text.RuleBasedCollator
cons public init(java.lang.String) throws java.lang.Exception
meth public boolean equals(java.lang.Object)
meth public boolean getNumericCollation()
meth public boolean isAlternateHandlingShifted()
meth public boolean isCaseLevel()
meth public boolean isFrenchCollation()
meth public boolean isHiraganaQuaternary()
meth public boolean isLowerCaseFirst()
meth public boolean isUpperCaseFirst()
meth public com.ibm.icu.text.CollationElementIterator getCollationElementIterator(com.ibm.icu.text.UCharacterIterator)
meth public com.ibm.icu.text.CollationElementIterator getCollationElementIterator(java.lang.String)
meth public com.ibm.icu.text.CollationElementIterator getCollationElementIterator(java.text.CharacterIterator)
meth public com.ibm.icu.text.CollationKey getCollationKey(java.lang.String)
meth public com.ibm.icu.text.RawCollationKey getRawCollationKey(java.lang.String,com.ibm.icu.text.RawCollationKey)
meth public com.ibm.icu.text.UnicodeSet getTailoredSet()
meth public com.ibm.icu.util.VersionInfo getUCAVersion()
meth public com.ibm.icu.util.VersionInfo getVersion()
meth public final void setCaseFirstDefault()
meth public int compare(java.lang.String,java.lang.String)
meth public int getVariableTop()
meth public int hashCode()
meth public int setVariableTop(java.lang.String)
meth public java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public java.lang.String getRules()
meth public java.lang.String getRules(boolean)
meth public void getContractionsAndExpansions(com.ibm.icu.text.UnicodeSet,com.ibm.icu.text.UnicodeSet,boolean) throws java.lang.Exception
meth public void setAlternateHandlingDefault()
meth public void setAlternateHandlingShifted(boolean)
meth public void setCaseLevel(boolean)
meth public void setCaseLevelDefault()
meth public void setDecompositionDefault()
meth public void setFrenchCollation(boolean)
meth public void setFrenchCollationDefault()
meth public void setHiraganaQuaternary(boolean)
meth public void setHiraganaQuaternaryDefault()
meth public void setLowerCaseFirst(boolean)
meth public void setNumericCollation(boolean)
meth public void setNumericCollationDefault()
meth public void setStrength(int)
meth public void setStrengthDefault()
meth public void setUpperCaseFirst(boolean)
meth public void setVariableTop(int)
supr com.ibm.icu.text.Collator
hfds BAIL_OUT_CE_,BOTTOM_COUNT_2_,BYTE_COMMON_,BYTE_FIRST_NON_LATIN_PRIMARY_,BYTE_FIRST_TAILORED_,BYTE_SHIFT_PREFIX_,BYTE_UNSHIFTED_MAX_,BYTE_UNSHIFTED_MIN_,CASE_SWITCH_,CE_BUFFER_SIZE_,CE_CASE_BIT_MASK_,CE_CASE_MASK_3_,CE_CONTINUATION_MARKER_,CE_CONTINUATION_TAG_,CE_KEEP_CASE_,CE_PRIMARY_MASK_,CE_PRIMARY_SHIFT_,CE_REMOVE_CASE_,CE_REMOVE_CONTINUATION_MASK_,CE_SECONDARY_MASK_,CE_SECONDARY_SHIFT_,CE_SPECIAL_FLAG_,CE_SURROGATE_TAG_,CE_TAG_MASK_,CE_TAG_SHIFT_,CE_TERTIARY_MASK_,CODAN_PLACEHOLDER,COMMON_2_,COMMON_BOTTOM_2_,COMMON_BOTTOM_3,COMMON_BOTTOM_3_,COMMON_BOTTOM_CASE_SWITCH_LOWER_3_,COMMON_BOTTOM_CASE_SWITCH_UPPER_3_,COMMON_NORMAL_3_,COMMON_TOP_2_,COMMON_TOP_CASE_SWITCH_LOWER_3_,COMMON_TOP_CASE_SWITCH_OFF_3_,COMMON_TOP_CASE_SWITCH_UPPER_3_,COMMON_UPPER_FIRST_3_,DEFAULT_MIN_HEURISTIC_,ENDOFLATINONERANGE_,FLAG_BIT_MASK_CASE_SWITCH_OFF_,FLAG_BIT_MASK_CASE_SWITCH_ON_,HEURISTIC_MASK_,HEURISTIC_OVERFLOW_MASK_,HEURISTIC_OVERFLOW_OFFSET_,HEURISTIC_SHIFT_,HEURISTIC_SIZE_,LAST_BYTE_MASK_,LATINONETABLELEN_,NO_CASE_SWITCH_,PROPORTION_2_,PROPORTION_3_,SORT_BUFFER_INIT_SIZE_,SORT_BUFFER_INIT_SIZE_1_,SORT_BUFFER_INIT_SIZE_2_,SORT_BUFFER_INIT_SIZE_3_,SORT_BUFFER_INIT_SIZE_4_,SORT_BUFFER_INIT_SIZE_CASE_,SORT_CASE_BYTE_START_,SORT_CASE_SHIFT_START_,SORT_LEVEL_TERMINATOR_,TOP_COUNT_2_,TOTAL_2_,UCA_,UCA_CONSTANTS_,UCA_CONTRACTIONS_,UCA_INIT_COMPLETE,impCEGen_,latinOneCEs_,latinOneFailed_,latinOneRegenTable_,latinOneTableLen_,latinOneUse_,m_ContInfo_,m_UCA_version_,m_UCD_version_,m_addition3_,m_bottom3_,m_bottomCount3_,m_caseFirst_,m_caseSwitch_,m_common3_,m_contractionCE_,m_contractionEnd_,m_contractionIndex_,m_contractionOffset_,m_defaultCaseFirst_,m_defaultDecomposition_,m_defaultIsAlternateHandlingShifted_,m_defaultIsCaseLevel_,m_defaultIsFrenchCollation_,m_defaultIsHiragana4_,m_defaultIsNumericCollation_,m_defaultStrength_,m_defaultVariableTopValue_,m_expansionEndCEMaxSize_,m_expansionEndCE_,m_expansionOffset_,m_expansion_,m_isAlternateHandlingShifted_,m_isCaseLevel_,m_isFrenchCollation_,m_isHiragana4_,m_isJamoSpecial_,m_isNumericCollation_,m_isSimple3_,m_mask3_,m_minContractionEnd_,m_minUnsafe_,m_reallocLatinOneCEs_,m_rules_,m_srcUtilCEBufferSize_,m_srcUtilCEBuffer_,m_srcUtilColEIter_,m_srcUtilContOffset_,m_srcUtilIter_,m_srcUtilOffset_,m_tgtUtilCEBufferSize_,m_tgtUtilCEBuffer_,m_tgtUtilColEIter_,m_tgtUtilContOffset_,m_tgtUtilIter_,m_tgtUtilOffset_,m_top3_,m_topCount3_,m_trie_,m_unsafe_,m_utilBytes0_,m_utilBytes1_,m_utilBytes2_,m_utilBytes3_,m_utilBytes4_,m_utilBytesCount0_,m_utilBytesCount1_,m_utilBytesCount2_,m_utilBytesCount3_,m_utilBytesCount4_,m_utilCompare0_,m_utilCompare2_,m_utilCompare3_,m_utilCompare4_,m_utilCompare5_,m_utilCount2_,m_utilCount3_,m_utilCount4_,m_utilFrenchEnd_,m_utilFrenchStart_,m_utilRawCollationKey_,m_variableTopValue_,m_version_,maxImplicitPrimary,maxRegularPrimary,minImplicitPrimary
hcls Attribute,AttributeValue,ContractionInfo,DataManipulate,UCAConstants,contContext,shiftValues

CLSS public com.ibm.icu.text.RuleBasedNumberFormat
cons public init(com.ibm.icu.util.ULocale,int)
cons public init(int)
cons public init(java.lang.String)
cons public init(java.lang.String,com.ibm.icu.util.ULocale)
cons public init(java.lang.String,java.lang.String[][])
cons public init(java.lang.String,java.lang.String[][],com.ibm.icu.util.ULocale)
cons public init(java.lang.String,java.util.Locale)
cons public init(java.util.Locale,int)
fld public final static int DURATION = 3
fld public final static int NUMBERING_SYSTEM = 4
fld public final static int ORDINAL = 2
fld public final static int SPELLOUT = 1
meth public boolean equals(java.lang.Object)
meth public boolean lenientParseEnabled()
meth public com.ibm.icu.text.RbnfLenientScannerProvider getLenientScannerProvider()
meth public com.ibm.icu.util.ULocale[] getRuleSetDisplayNameLocales()
meth public java.lang.Number parse(java.lang.String,java.text.ParsePosition)
meth public java.lang.Object clone()
meth public java.lang.String format(double,java.lang.String)
meth public java.lang.String format(long,java.lang.String)
meth public java.lang.String getDefaultRuleSetName()
meth public java.lang.String getRuleSetDisplayName(java.lang.String)
meth public java.lang.String getRuleSetDisplayName(java.lang.String,com.ibm.icu.util.ULocale)
meth public java.lang.String toString()
meth public java.lang.StringBuffer format(com.ibm.icu.math.BigDecimal,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.lang.StringBuffer format(double,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.lang.StringBuffer format(java.math.BigDecimal,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.lang.StringBuffer format(java.math.BigInteger,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.lang.StringBuffer format(long,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.lang.String[] getRuleSetDisplayNames()
meth public java.lang.String[] getRuleSetDisplayNames(com.ibm.icu.util.ULocale)
meth public java.lang.String[] getRuleSetNames()
meth public void setDefaultRuleSet(java.lang.String)
meth public void setLenientParseMode(boolean)
meth public void setLenientScannerProvider(com.ibm.icu.text.RbnfLenientScannerProvider)
supr com.ibm.icu.text.NumberFormat
hfds DEBUG,NO_SPELLOUT_PARSE_LANGUAGES,decimalFormat,decimalFormatSymbols,defaultRuleSet,lenientParse,lenientParseRules,locale,locnames,lookedForScanner,noParse,postProcessRules,postProcessor,publicRuleSetNames,ruleSetDisplayNames,ruleSets,rulenames,scannerProvider,serialVersionUID

CLSS public com.ibm.icu.text.RuleBasedTransliterator
meth protected com.ibm.icu.text.UnicodeSet handleGetSourceSet()
meth protected void handleTransliterate(com.ibm.icu.text.Replaceable,com.ibm.icu.text.Transliterator$Position,boolean)
meth public com.ibm.icu.text.Transliterator safeClone()
meth public com.ibm.icu.text.UnicodeSet getTargetSet()
meth public java.lang.String toRules(boolean)
supr com.ibm.icu.text.Transliterator
hfds data
hcls Data

CLSS public abstract com.ibm.icu.text.SearchIterator
cons protected init(java.text.CharacterIterator,com.ibm.icu.text.BreakIterator)
fld protected com.ibm.icu.text.BreakIterator breakIterator
fld protected int matchLength
fld protected java.text.CharacterIterator targetText
fld public final static int DONE = -1
meth protected abstract int handleNext(int)
meth protected abstract int handlePrevious(int)
meth protected void setMatchLength(int)
meth public abstract int getIndex()
meth public boolean isOverlapping()
meth public com.ibm.icu.text.BreakIterator getBreakIterator()
meth public final int first()
meth public final int following(int)
meth public final int last()
meth public final int preceding(int)
meth public int getMatchLength()
meth public int getMatchStart()
meth public int next()
meth public int previous()
meth public java.lang.String getMatchedText()
meth public java.text.CharacterIterator getTarget()
meth public void reset()
meth public void setBreakIterator(com.ibm.icu.text.BreakIterator)
meth public void setIndex(int)
meth public void setOverlapping(boolean)
meth public void setTarget(java.text.CharacterIterator)
supr java.lang.Object
hfds m_isForwardSearching_,m_isOverlap_,m_lastMatchStart_,m_reset_,m_setOffset_

CLSS public com.ibm.icu.text.SelectFormat
cons public init(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public final java.lang.String format(java.lang.String)
meth public int hashCode()
meth public java.lang.Object parseObject(java.lang.String,java.text.ParsePosition)
meth public java.lang.String toPattern()
meth public java.lang.String toString()
meth public java.lang.StringBuffer format(java.lang.Object,java.lang.StringBuffer,java.text.FieldPosition)
meth public void applyPattern(java.lang.String)
supr java.text.Format
hfds KEYWORD_OTHER,parsedValues,pattern,serialVersionUID
hcls CharacterClass,State

CLSS public com.ibm.icu.text.SimpleDateFormat
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,com.ibm.icu.text.DateFormatSymbols)
cons public init(java.lang.String,com.ibm.icu.text.DateFormatSymbols,com.ibm.icu.util.ULocale)
cons public init(java.lang.String,com.ibm.icu.util.ULocale)
cons public init(java.lang.String,java.lang.String,com.ibm.icu.util.ULocale)
cons public init(java.lang.String,java.util.Locale)
meth protected com.ibm.icu.text.DateFormat$Field patternCharToDateFormatField(char)
meth protected com.ibm.icu.text.DateFormatSymbols getSymbols()
meth protected com.ibm.icu.text.NumberFormat getNumberFormat(char)
meth protected int matchQuarterString(java.lang.String,int,int,java.lang.String[],com.ibm.icu.util.Calendar)
meth protected int matchString(java.lang.String,int,int,java.lang.String[],com.ibm.icu.util.Calendar)
meth protected int subParse(java.lang.String,int,char,int,boolean,boolean,boolean[],com.ibm.icu.util.Calendar)
meth protected java.lang.String subFormat(char,int,int,java.text.FieldPosition,com.ibm.icu.text.DateFormatSymbols,com.ibm.icu.util.Calendar)
meth protected java.lang.String zeroPaddingNumber(long,int,int)
meth protected void subFormat(java.lang.StringBuffer,char,int,int,java.text.FieldPosition,com.ibm.icu.util.Calendar)
meth protected void zeroPaddingNumber(com.ibm.icu.text.NumberFormat,java.lang.StringBuffer,int,int,int)
meth public boolean equals(java.lang.Object)
meth public com.ibm.icu.text.DateFormatSymbols getDateFormatSymbols()
meth public final java.lang.StringBuffer intervalFormatByAlgorithm(com.ibm.icu.util.Calendar,com.ibm.icu.util.Calendar,java.lang.StringBuffer,java.text.FieldPosition)
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.String toLocalizedPattern()
meth public java.lang.String toPattern()
meth public java.lang.StringBuffer format(com.ibm.icu.util.Calendar,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.text.AttributedCharacterIterator formatToCharacterIterator(java.lang.Object)
meth public java.util.Date get2DigitYearStart()
meth public static com.ibm.icu.text.SimpleDateFormat getInstance(com.ibm.icu.util.Calendar$FormatConfiguration)
meth public void applyLocalizedPattern(java.lang.String)
meth public void applyPattern(java.lang.String)
meth public void parse(java.lang.String,com.ibm.icu.util.Calendar,java.text.ParsePosition)
meth public void set2DigitYearStart(java.util.Date)
meth public void setDateFormatSymbols(com.ibm.icu.text.DateFormatSymbols)
meth public void setNumberFormat(com.ibm.icu.text.NumberFormat)
supr com.ibm.icu.text.DateFormat
hfds CALENDAR_FIELD_TO_LEVEL,COLON,DelayedHebrewMonthCheck,FALLBACKPATTERN,MINUS,NUMERIC_FORMAT_CHARS,PARSED_PATTERN_CACHE,PATTERN_CHAR_BASE,PATTERN_CHAR_TO_INDEX,PATTERN_CHAR_TO_LEVEL,PATTERN_INDEX_TO_CALENDAR_FIELD,PATTERN_INDEX_TO_DATE_FORMAT_ATTRIBUTE,PATTERN_INDEX_TO_DATE_FORMAT_FIELD,PLUS,STR_GMT,STR_GMT_LEN,STR_UT,STR_UTC,STR_UTC_LEN,STR_UT_LEN,SUPPRESS_NEGATIVE_PREFIX,TZTYPE_DST,TZTYPE_STD,TZTYPE_UNK,cachedDefaultLocale,cachedDefaultPattern,currentSerialVersion,decimalBuf,defaultCenturyBase,defaultCenturyStart,defaultCenturyStartYear,formatData,gmtFormatHmsMinLen,gmtfmtCache,locale,millisPerHour,millisPerMinute,millisPerSecond,numberFormatters,override,overrideMap,pattern,patternItems,serialVersionOnStream,serialVersionUID,tztype,useFastFormat,useLocalZeroPaddingNumberFormat,zeroDigit
hcls PatternItem

CLSS public final com.ibm.icu.text.StringCharacterIterator
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,int,int)
intf java.text.CharacterIterator
meth public boolean equals(java.lang.Object)
meth public char current()
meth public char first()
meth public char last()
meth public char next()
meth public char previous()
meth public char setIndex(int)
meth public int getBeginIndex()
meth public int getEndIndex()
meth public int getIndex()
meth public int hashCode()
meth public java.lang.Object clone()
meth public void setText(java.lang.String)
supr java.lang.Object
hfds begin,end,pos,text

CLSS public final com.ibm.icu.text.StringPrep
cons public init(java.io.InputStream) throws java.io.IOException
fld public final static int ALLOW_UNASSIGNED = 1
fld public final static int DEFAULT = 0
fld public final static int RFC3491_NAMEPREP = 0
fld public final static int RFC3530_NFS4_CIS_PREP = 3
fld public final static int RFC3530_NFS4_CS_PREP = 1
fld public final static int RFC3530_NFS4_CS_PREP_CI = 2
fld public final static int RFC3530_NFS4_MIXED_PREP_PREFIX = 4
fld public final static int RFC3530_NFS4_MIXED_PREP_SUFFIX = 5
fld public final static int RFC3722_ISCSI = 6
fld public final static int RFC3920_NODEPREP = 7
fld public final static int RFC3920_RESOURCEPREP = 8
fld public final static int RFC4011_MIB = 9
fld public final static int RFC4013_SASLPREP = 10
fld public final static int RFC4505_TRACE = 11
fld public final static int RFC4518_LDAP = 12
fld public final static int RFC4518_LDAP_CI = 13
meth public java.lang.String prepare(java.lang.String,int) throws com.ibm.icu.text.StringPrepParseException
meth public java.lang.StringBuffer prepare(com.ibm.icu.text.UCharacterIterator,int) throws com.ibm.icu.text.StringPrepParseException
meth public static com.ibm.icu.text.StringPrep getInstance(int)
supr java.lang.Object
hfds CACHE,CHECK_BIDI_ON,DATA_BUFFER_SIZE,DELETE,FOUR_UCHARS_MAPPING_INDEX_START,INDEX_MAPPING_DATA_SIZE,INDEX_TOP,INDEX_TRIE_SIZE,MAP,MAX_INDEX_VALUE,MAX_PROFILE,NORMALIZATION_ON,NORM_CORRECTNS_LAST_UNI_VERSION,ONE_UCHAR_MAPPING_INDEX_START,OPTIONS,PROFILE_NAMES,PROHIBITED,THREE_UCHARS_MAPPING_INDEX_START,TWO_UCHARS_MAPPING_INDEX_START,TYPE_LIMIT,TYPE_THRESHOLD,UNASSIGNED,bdp,checkBiDi,doNFKC,indexes,mappingData,normCorrVer,sprepTrie,sprepUniVer
hcls Values

CLSS public com.ibm.icu.text.StringPrepParseException
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,java.lang.String,int)
cons public init(java.lang.String,int,java.lang.String,int,int)
fld public final static int ACE_PREFIX_ERROR = 6
fld public final static int BUFFER_OVERFLOW_ERROR = 9
fld public final static int CHECK_BIDI_ERROR = 4
fld public final static int DOMAIN_NAME_TOO_LONG_ERROR = 11
fld public final static int ILLEGAL_CHAR_FOUND = 1
fld public final static int INVALID_CHAR_FOUND = 0
fld public final static int LABEL_TOO_LONG_ERROR = 8
fld public final static int PROHIBITED_ERROR = 2
fld public final static int STD3_ASCII_RULES_ERROR = 5
fld public final static int UNASSIGNED_ERROR = 3
fld public final static int VERIFICATION_ERROR = 7
fld public final static int ZERO_LENGTH_LABEL = 10
meth public boolean equals(java.lang.Object)
meth public int getError()
meth public java.lang.String toString()
supr java.text.ParseException
hfds PARSE_CONTEXT_LEN,error,line,postContext,preContext,serialVersionUID

CLSS public final com.ibm.icu.text.StringSearch
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.text.CharacterIterator,com.ibm.icu.text.RuleBasedCollator)
cons public init(java.lang.String,java.text.CharacterIterator,com.ibm.icu.text.RuleBasedCollator,com.ibm.icu.text.BreakIterator)
cons public init(java.lang.String,java.text.CharacterIterator,com.ibm.icu.util.ULocale)
cons public init(java.lang.String,java.text.CharacterIterator,java.util.Locale)
meth protected int handleNext(int)
meth protected int handlePrevious(int)
meth public boolean isCanonical()
meth public com.ibm.icu.text.RuleBasedCollator getCollator()
meth public int getIndex()
meth public java.lang.String getPattern()
meth public void reset()
meth public void setCanonical(boolean)
meth public void setCollator(com.ibm.icu.text.RuleBasedCollator)
meth public void setIndex(int)
meth public void setPattern(java.lang.String)
meth public void setTarget(java.text.CharacterIterator)
supr com.ibm.icu.text.SearchIterator
hfds INITIAL_ARRAY_SIZE_,LAST_BYTE_MASK_,MAX_TABLE_SIZE_,SECOND_LAST_BYTE_SHIFT_,UNSIGNED_32BIT_MASK,m_canonicalPrefixAccents_,m_canonicalSuffixAccents_,m_ceMask_,m_charBreakIter_,m_colEIter_,m_collator_,m_isCanonicalMatch_,m_matchedIndex_,m_nfcImpl_,m_pattern_,m_textBeginOffset_,m_textLimitOffset_,m_utilBuffer_,m_utilColEIter_
hcls Pattern

CLSS public abstract interface com.ibm.icu.text.StringTransform
intf com.ibm.icu.text.Transform<java.lang.String,java.lang.String>
meth public abstract java.lang.String transform(java.lang.String)

CLSS public abstract interface com.ibm.icu.text.SymbolTable
fld public final static char SYMBOL_REF = '$'
meth public abstract char[] lookup(java.lang.String)
meth public abstract com.ibm.icu.text.UnicodeMatcher lookupMatcher(int)
meth public abstract java.lang.String parseReference(java.lang.String,java.text.ParsePosition,int)

CLSS public com.ibm.icu.text.TimeUnitFormat
cons public init()
cons public init(com.ibm.icu.util.ULocale)
cons public init(com.ibm.icu.util.ULocale,int)
cons public init(java.util.Locale)
cons public init(java.util.Locale,int)
fld public final static int ABBREVIATED_NAME = 1
fld public final static int FULL_NAME = 0
meth public com.ibm.icu.text.TimeUnitFormat setLocale(com.ibm.icu.util.ULocale)
meth public com.ibm.icu.text.TimeUnitFormat setLocale(java.util.Locale)
meth public com.ibm.icu.text.TimeUnitFormat setNumberFormat(com.ibm.icu.text.NumberFormat)
meth public java.lang.Object parseObject(java.lang.String,java.text.ParsePosition)
meth public java.lang.StringBuffer format(java.lang.Object,java.lang.StringBuffer,java.text.FieldPosition)
supr com.ibm.icu.text.MeasureFormat
hfds DEFAULT_PATTERN_FOR_DAY,DEFAULT_PATTERN_FOR_HOUR,DEFAULT_PATTERN_FOR_MINUTE,DEFAULT_PATTERN_FOR_MONTH,DEFAULT_PATTERN_FOR_SECOND,DEFAULT_PATTERN_FOR_WEEK,DEFAULT_PATTERN_FOR_YEAR,TOTAL_STYLES,format,isReady,locale,pluralRules,serialVersionUID,style,timeUnitToCountToPatterns

CLSS public abstract interface com.ibm.icu.text.Transform<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract {com.ibm.icu.text.Transform%1} transform({com.ibm.icu.text.Transform%0})

CLSS public abstract com.ibm.icu.text.Transliterator
cons protected init(java.lang.String,com.ibm.icu.text.UnicodeFilter)
fld public final static int FORWARD = 0
fld public final static int REVERSE = 1
innr public abstract interface static Factory
innr public static Position
intf com.ibm.icu.text.StringTransform
meth protected abstract void handleTransliterate(com.ibm.icu.text.Replaceable,com.ibm.icu.text.Transliterator$Position,boolean)
meth protected com.ibm.icu.text.UnicodeSet handleGetSourceSet()
meth protected final java.lang.String baseToRules(boolean)
meth protected final void setID(java.lang.String)
meth protected void setMaximumContextLength(int)
meth public com.ibm.icu.text.Transliterator[] getElements()
meth public com.ibm.icu.text.UnicodeSet getTargetSet()
meth public final com.ibm.icu.text.Transliterator getInverse()
meth public final com.ibm.icu.text.UnicodeFilter getFilter()
meth public final com.ibm.icu.text.UnicodeSet getSourceSet()
meth public final int getMaximumContextLength()
meth public final int transliterate(com.ibm.icu.text.Replaceable,int,int)
meth public final java.lang.String getID()
meth public final java.lang.String transliterate(java.lang.String)
meth public final static com.ibm.icu.text.Transliterator createFromRules(java.lang.String,java.lang.String,int)
meth public final static com.ibm.icu.text.Transliterator getInstance(java.lang.String)
meth public final static java.lang.String getDisplayName(java.lang.String)
meth public final static java.util.Enumeration<java.lang.String> getAvailableIDs()
meth public final static java.util.Enumeration<java.lang.String> getAvailableSources()
meth public final static java.util.Enumeration<java.lang.String> getAvailableTargets(java.lang.String)
meth public final static java.util.Enumeration<java.lang.String> getAvailableVariants(java.lang.String,java.lang.String)
meth public final void finishTransliteration(com.ibm.icu.text.Replaceable,com.ibm.icu.text.Transliterator$Position)
meth public final void transliterate(com.ibm.icu.text.Replaceable)
meth public final void transliterate(com.ibm.icu.text.Replaceable,com.ibm.icu.text.Transliterator$Position)
meth public final void transliterate(com.ibm.icu.text.Replaceable,com.ibm.icu.text.Transliterator$Position,int)
meth public final void transliterate(com.ibm.icu.text.Replaceable,com.ibm.icu.text.Transliterator$Position,java.lang.String)
meth public java.lang.String toRules(boolean)
meth public java.lang.String transform(java.lang.String)
meth public static com.ibm.icu.text.Transliterator getInstance(java.lang.String,int)
meth public static java.lang.String getDisplayName(java.lang.String,com.ibm.icu.util.ULocale)
meth public static java.lang.String getDisplayName(java.lang.String,java.util.Locale)
meth public static void registerAlias(java.lang.String,java.lang.String)
meth public static void registerAny()
meth public static void registerClass(java.lang.String,java.lang.Class<? extends com.ibm.icu.text.Transliterator>,java.lang.String)
meth public static void registerFactory(java.lang.String,com.ibm.icu.text.Transliterator$Factory)
meth public static void registerInstance(com.ibm.icu.text.Transliterator)
meth public static void unregister(java.lang.String)
meth public void filteredTransliterate(com.ibm.icu.text.Replaceable,com.ibm.icu.text.Transliterator$Position,boolean)
meth public void setFilter(com.ibm.icu.text.UnicodeFilter)
supr java.lang.Object
hfds DEBUG,ID,ID_DELIM,ID_SEP,INDEX,RB_DISPLAY_NAME_PATTERN,RB_DISPLAY_NAME_PREFIX,RB_RULE_BASED_IDS,RB_SCRIPT_DISPLAY_NAME_PREFIX,VARIANT_SEP,displayNameCache,filter,maximumContextLength,registry

CLSS public abstract interface static com.ibm.icu.text.Transliterator$Factory
 outer com.ibm.icu.text.Transliterator
meth public abstract com.ibm.icu.text.Transliterator getInstance(java.lang.String)

CLSS public static com.ibm.icu.text.Transliterator$Position
 outer com.ibm.icu.text.Transliterator
cons public init()
cons public init(com.ibm.icu.text.Transliterator$Position)
cons public init(int,int,int)
cons public init(int,int,int,int)
fld public int contextLimit
fld public int contextStart
fld public int limit
fld public int start
meth public boolean equals(java.lang.Object)
meth public final void validate(int)
meth public java.lang.String toString()
meth public void set(com.ibm.icu.text.Transliterator$Position)
supr java.lang.Object

CLSS public abstract com.ibm.icu.text.UCharacterIterator
cons protected init()
intf com.ibm.icu.text.UForwardCharacterIterator
intf java.lang.Cloneable
meth public abstract int current()
meth public abstract int getIndex()
meth public abstract int getLength()
meth public abstract int getText(char[],int)
meth public abstract int next()
meth public abstract int previous()
meth public abstract void setIndex(int)
meth public final int getText(char[])
meth public final static com.ibm.icu.text.UCharacterIterator getInstance(char[])
meth public final static com.ibm.icu.text.UCharacterIterator getInstance(char[],int,int)
meth public final static com.ibm.icu.text.UCharacterIterator getInstance(com.ibm.icu.text.Replaceable)
meth public final static com.ibm.icu.text.UCharacterIterator getInstance(java.lang.String)
meth public final static com.ibm.icu.text.UCharacterIterator getInstance(java.lang.StringBuffer)
meth public final static com.ibm.icu.text.UCharacterIterator getInstance(java.text.CharacterIterator)
meth public int currentCodePoint()
meth public int moveCodePointIndex(int)
meth public int moveIndex(int)
meth public int nextCodePoint()
meth public int previousCodePoint()
meth public java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public java.lang.String getText()
meth public java.text.CharacterIterator getCharacterIterator()
meth public void setToLimit()
meth public void setToStart()
supr java.lang.Object

CLSS public abstract com.ibm.icu.text.UFormat
cons public init()
meth public final com.ibm.icu.util.ULocale getLocale(com.ibm.icu.util.ULocale$Type)
supr java.text.Format
hfds actualLocale,serialVersionUID,validLocale

CLSS public abstract interface com.ibm.icu.text.UForwardCharacterIterator
fld public final static int DONE = -1
meth public abstract int next()
meth public abstract int nextCodePoint()

CLSS public final com.ibm.icu.text.UTF16
fld public final static int CODEPOINT_MAX_VALUE = 1114111
fld public final static int CODEPOINT_MIN_VALUE = 0
fld public final static int LEAD_SURROGATE_BOUNDARY = 2
fld public final static int LEAD_SURROGATE_MAX_VALUE = 56319
fld public final static int LEAD_SURROGATE_MIN_VALUE = 55296
fld public final static int SINGLE_CHAR_BOUNDARY = 1
fld public final static int SUPPLEMENTARY_MIN_VALUE = 65536
fld public final static int SURROGATE_MAX_VALUE = 57343
fld public final static int SURROGATE_MIN_VALUE = 55296
fld public final static int TRAIL_SURROGATE_BOUNDARY = 5
fld public final static int TRAIL_SURROGATE_MAX_VALUE = 57343
fld public final static int TRAIL_SURROGATE_MIN_VALUE = 56320
innr public final static StringComparator
meth public static boolean hasMoreCodePointsThan(char[],int,int,int)
meth public static boolean hasMoreCodePointsThan(java.lang.String,int)
meth public static boolean hasMoreCodePointsThan(java.lang.StringBuffer,int)
meth public static boolean isLeadSurrogate(char)
meth public static boolean isSurrogate(char)
meth public static boolean isTrailSurrogate(char)
meth public static char getLeadSurrogate(int)
meth public static char getTrailSurrogate(int)
meth public static int append(char[],int,int)
meth public static int bounds(char[],int,int,int)
meth public static int bounds(java.lang.String,int)
meth public static int bounds(java.lang.StringBuffer,int)
meth public static int charAt(char[],int,int,int)
meth public static int charAt(com.ibm.icu.text.Replaceable,int)
meth public static int charAt(java.lang.CharSequence,int)
meth public static int charAt(java.lang.String,int)
meth public static int charAt(java.lang.StringBuffer,int)
meth public static int countCodePoint(char[],int,int)
meth public static int countCodePoint(java.lang.String)
meth public static int countCodePoint(java.lang.StringBuffer)
meth public static int delete(char[],int,int)
meth public static int findCodePointOffset(char[],int,int,int)
meth public static int findCodePointOffset(java.lang.String,int)
meth public static int findCodePointOffset(java.lang.StringBuffer,int)
meth public static int findOffsetFromCodePoint(char[],int,int,int)
meth public static int findOffsetFromCodePoint(java.lang.String,int)
meth public static int findOffsetFromCodePoint(java.lang.StringBuffer,int)
meth public static int getCharCount(int)
meth public static int indexOf(java.lang.String,int)
meth public static int indexOf(java.lang.String,int,int)
meth public static int indexOf(java.lang.String,java.lang.String)
meth public static int indexOf(java.lang.String,java.lang.String,int)
meth public static int insert(char[],int,int,int)
meth public static int lastIndexOf(java.lang.String,int)
meth public static int lastIndexOf(java.lang.String,int,int)
meth public static int lastIndexOf(java.lang.String,java.lang.String)
meth public static int lastIndexOf(java.lang.String,java.lang.String,int)
meth public static int moveCodePointOffset(char[],int,int,int,int)
meth public static int moveCodePointOffset(java.lang.String,int,int)
meth public static int moveCodePointOffset(java.lang.StringBuffer,int,int)
meth public static int setCharAt(char[],int,int,int)
meth public static java.lang.String newString(int[],int,int)
meth public static java.lang.String replace(java.lang.String,int,int)
meth public static java.lang.String replace(java.lang.String,java.lang.String,java.lang.String)
meth public static java.lang.String valueOf(char[],int,int,int)
meth public static java.lang.String valueOf(int)
meth public static java.lang.String valueOf(java.lang.String,int)
meth public static java.lang.String valueOf(java.lang.StringBuffer,int)
meth public static java.lang.StringBuffer append(java.lang.StringBuffer,int)
meth public static java.lang.StringBuffer appendCodePoint(java.lang.StringBuffer,int)
meth public static java.lang.StringBuffer delete(java.lang.StringBuffer,int)
meth public static java.lang.StringBuffer insert(java.lang.StringBuffer,int,int)
meth public static java.lang.StringBuffer reverse(java.lang.StringBuffer)
meth public static void setCharAt(java.lang.StringBuffer,int,int)
supr java.lang.Object
hfds LEAD_SURROGATE_BITMASK,LEAD_SURROGATE_BITS,LEAD_SURROGATE_OFFSET_,LEAD_SURROGATE_SHIFT_,SURROGATE_BITMASK,SURROGATE_BITS,TRAIL_SURROGATE_BITMASK,TRAIL_SURROGATE_BITS,TRAIL_SURROGATE_MASK_

CLSS public final static com.ibm.icu.text.UTF16$StringComparator
 outer com.ibm.icu.text.UTF16
cons public init()
cons public init(boolean,boolean,int)
fld public final static int FOLD_CASE_DEFAULT = 0
fld public final static int FOLD_CASE_EXCLUDE_SPECIAL_I = 1
intf java.util.Comparator<java.lang.String>
meth public boolean getCodePointCompare()
meth public boolean getIgnoreCase()
meth public int compare(java.lang.String,java.lang.String)
meth public int getIgnoreCaseOption()
meth public void setCodePointCompare(boolean)
meth public void setIgnoreCase(boolean,int)
supr java.lang.Object
hfds CODE_POINT_COMPARE_SURROGATE_OFFSET_,m_codePointCompare_,m_foldCase_,m_ignoreCase_

CLSS public final com.ibm.icu.text.UnicodeCompressor
cons public init()
fld public final static int ARMENIANINDEX = 252
fld public final static int COMPRESSIONOFFSET = 128
fld public final static int GREEKINDEX = 251
fld public final static int HALFWIDTHKATAKANAINDEX = 255
fld public final static int HIRAGANAINDEX = 253
fld public final static int INVALIDCHAR = -1
fld public final static int INVALIDWINDOW = -1
fld public final static int IPAEXTENSIONINDEX = 250
fld public final static int KATAKANAINDEX = 254
fld public final static int LATININDEX = 249
fld public final static int MAXINDEX = 255
fld public final static int NUMSTATICWINDOWS = 8
fld public final static int NUMWINDOWS = 8
fld public final static int RESERVEDINDEX = 0
fld public final static int SCHANGE0 = 16
fld public final static int SCHANGE1 = 17
fld public final static int SCHANGE2 = 18
fld public final static int SCHANGE3 = 19
fld public final static int SCHANGE4 = 20
fld public final static int SCHANGE5 = 21
fld public final static int SCHANGE6 = 22
fld public final static int SCHANGE7 = 23
fld public final static int SCHANGEU = 15
fld public final static int SDEFINE0 = 24
fld public final static int SDEFINE1 = 25
fld public final static int SDEFINE2 = 26
fld public final static int SDEFINE3 = 27
fld public final static int SDEFINE4 = 28
fld public final static int SDEFINE5 = 29
fld public final static int SDEFINE6 = 30
fld public final static int SDEFINE7 = 31
fld public final static int SDEFINEX = 11
fld public final static int SINGLEBYTEMODE = 0
fld public final static int SQUOTE0 = 1
fld public final static int SQUOTE1 = 2
fld public final static int SQUOTE2 = 3
fld public final static int SQUOTE3 = 4
fld public final static int SQUOTE4 = 5
fld public final static int SQUOTE5 = 6
fld public final static int SQUOTE6 = 7
fld public final static int SQUOTE7 = 8
fld public final static int SQUOTEU = 14
fld public final static int SRESERVED = 12
fld public final static int UCHANGE0 = 224
fld public final static int UCHANGE1 = 225
fld public final static int UCHANGE2 = 226
fld public final static int UCHANGE3 = 227
fld public final static int UCHANGE4 = 228
fld public final static int UCHANGE5 = 229
fld public final static int UCHANGE6 = 230
fld public final static int UCHANGE7 = 231
fld public final static int UDEFINE0 = 232
fld public final static int UDEFINE1 = 233
fld public final static int UDEFINE2 = 234
fld public final static int UDEFINE3 = 235
fld public final static int UDEFINE4 = 236
fld public final static int UDEFINE5 = 237
fld public final static int UDEFINE6 = 238
fld public final static int UDEFINE7 = 239
fld public final static int UDEFINEX = 241
fld public final static int UNICODEMODE = 1
fld public final static int UQUOTEU = 240
fld public final static int URESERVED = 242
fld public final static int[] sOffsetTable
fld public final static int[] sOffsets
meth public int compress(char[],int,int,int[],byte[],int,int)
meth public static byte[] compress(char[],int,int)
meth public static byte[] compress(java.lang.String)
meth public void reset()
supr java.lang.Object
hfds fCurrentWindow,fIndexCount,fMode,fOffsets,fTimeStamp,fTimeStamps,sSingleTagTable,sUnicodeTagTable

CLSS public final com.ibm.icu.text.UnicodeDecompressor
cons public init()
fld public final static int ARMENIANINDEX = 252
fld public final static int COMPRESSIONOFFSET = 128
fld public final static int GREEKINDEX = 251
fld public final static int HALFWIDTHKATAKANAINDEX = 255
fld public final static int HIRAGANAINDEX = 253
fld public final static int INVALIDCHAR = -1
fld public final static int INVALIDWINDOW = -1
fld public final static int IPAEXTENSIONINDEX = 250
fld public final static int KATAKANAINDEX = 254
fld public final static int LATININDEX = 249
fld public final static int MAXINDEX = 255
fld public final static int NUMSTATICWINDOWS = 8
fld public final static int NUMWINDOWS = 8
fld public final static int RESERVEDINDEX = 0
fld public final static int SCHANGE0 = 16
fld public final static int SCHANGE1 = 17
fld public final static int SCHANGE2 = 18
fld public final static int SCHANGE3 = 19
fld public final static int SCHANGE4 = 20
fld public final static int SCHANGE5 = 21
fld public final static int SCHANGE6 = 22
fld public final static int SCHANGE7 = 23
fld public final static int SCHANGEU = 15
fld public final static int SDEFINE0 = 24
fld public final static int SDEFINE1 = 25
fld public final static int SDEFINE2 = 26
fld public final static int SDEFINE3 = 27
fld public final static int SDEFINE4 = 28
fld public final static int SDEFINE5 = 29
fld public final static int SDEFINE6 = 30
fld public final static int SDEFINE7 = 31
fld public final static int SDEFINEX = 11
fld public final static int SINGLEBYTEMODE = 0
fld public final static int SQUOTE0 = 1
fld public final static int SQUOTE1 = 2
fld public final static int SQUOTE2 = 3
fld public final static int SQUOTE3 = 4
fld public final static int SQUOTE4 = 5
fld public final static int SQUOTE5 = 6
fld public final static int SQUOTE6 = 7
fld public final static int SQUOTE7 = 8
fld public final static int SQUOTEU = 14
fld public final static int SRESERVED = 12
fld public final static int UCHANGE0 = 224
fld public final static int UCHANGE1 = 225
fld public final static int UCHANGE2 = 226
fld public final static int UCHANGE3 = 227
fld public final static int UCHANGE4 = 228
fld public final static int UCHANGE5 = 229
fld public final static int UCHANGE6 = 230
fld public final static int UCHANGE7 = 231
fld public final static int UDEFINE0 = 232
fld public final static int UDEFINE1 = 233
fld public final static int UDEFINE2 = 234
fld public final static int UDEFINE3 = 235
fld public final static int UDEFINE4 = 236
fld public final static int UDEFINE5 = 237
fld public final static int UDEFINE6 = 238
fld public final static int UDEFINE7 = 239
fld public final static int UDEFINEX = 241
fld public final static int UNICODEMODE = 1
fld public final static int UQUOTEU = 240
fld public final static int URESERVED = 242
fld public final static int[] sOffsetTable
fld public final static int[] sOffsets
meth public int decompress(byte[],int,int,int[],char[],int,int)
meth public static char[] decompress(byte[],int,int)
meth public static java.lang.String decompress(byte[])
meth public void reset()
supr java.lang.Object
hfds BUFSIZE,fBuffer,fBufferLength,fCurrentWindow,fMode,fOffsets

CLSS public abstract com.ibm.icu.text.UnicodeFilter
cons protected init()
intf com.ibm.icu.text.UnicodeMatcher
meth public abstract boolean contains(int)
meth public int matches(com.ibm.icu.text.Replaceable,int[],int,boolean)
supr java.lang.Object

CLSS public abstract interface com.ibm.icu.text.UnicodeMatcher
fld public final static char ETHER = '\uffff'
fld public final static int U_MATCH = 2
fld public final static int U_MISMATCH = 0
fld public final static int U_PARTIAL_MATCH = 1
meth public abstract boolean matchesIndexValue(int)
meth public abstract int matches(com.ibm.icu.text.Replaceable,int[],int,boolean)
meth public abstract java.lang.String toPattern(boolean)
meth public abstract void addMatchSetTo(com.ibm.icu.text.UnicodeSet)

CLSS public com.ibm.icu.text.UnicodeSet
cons public !varargs init(int[])
cons public init()
cons public init(com.ibm.icu.text.UnicodeSet)
cons public init(int,int)
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(java.lang.String,int)
cons public init(java.lang.String,java.text.ParsePosition,com.ibm.icu.text.SymbolTable)
cons public init(java.lang.String,java.text.ParsePosition,com.ibm.icu.text.SymbolTable,int)
fld public final static int ADD_CASE_MAPPINGS = 4
fld public final static int CASE = 2
fld public final static int CASE_INSENSITIVE = 2
fld public final static int IGNORE_SPACE = 1
fld public final static int MAX_VALUE = 1114111
fld public final static int MIN_VALUE = 0
innr public abstract static XSymbolTable
innr public final static !enum ComparisonStyle
innr public final static !enum SpanCondition
intf com.ibm.icu.util.Freezable<com.ibm.icu.text.UnicodeSet>
intf java.lang.Comparable<com.ibm.icu.text.UnicodeSet>
intf java.lang.Iterable<java.lang.String>
meth public !varargs com.ibm.icu.text.UnicodeSet addAll(java.lang.String[])
meth public <%0 extends java.util.Collection<java.lang.String>> {%%0} addAllTo({%%0})
meth public boolean contains(int)
meth public boolean contains(int,int)
meth public boolean containsAll(com.ibm.icu.text.UnicodeSet)
meth public boolean containsAll(java.lang.String)
meth public boolean containsAll(java.util.Collection<java.lang.String>)
meth public boolean containsNone(com.ibm.icu.text.UnicodeSet)
meth public boolean containsNone(int,int)
meth public boolean containsNone(java.lang.String)
meth public boolean containsNone(java.util.Collection<java.lang.String>)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public boolean isFrozen()
meth public boolean matchesIndexValue(int)
meth public com.ibm.icu.text.UnicodeSet add(int,int)
meth public com.ibm.icu.text.UnicodeSet add(java.util.Collection<?>)
meth public com.ibm.icu.text.UnicodeSet addAll(com.ibm.icu.text.UnicodeSet)
meth public com.ibm.icu.text.UnicodeSet addAll(int,int)
meth public com.ibm.icu.text.UnicodeSet addAll(java.util.Collection<?>)
meth public com.ibm.icu.text.UnicodeSet addBridges(com.ibm.icu.text.UnicodeSet)
meth public com.ibm.icu.text.UnicodeSet applyIntPropertyValue(int,int)
meth public com.ibm.icu.text.UnicodeSet applyPattern(java.lang.String,boolean)
meth public com.ibm.icu.text.UnicodeSet applyPattern(java.lang.String,int)
meth public com.ibm.icu.text.UnicodeSet applyPattern(java.lang.String,java.text.ParsePosition,com.ibm.icu.text.SymbolTable,int)
meth public com.ibm.icu.text.UnicodeSet applyPropertyAlias(java.lang.String,java.lang.String)
meth public com.ibm.icu.text.UnicodeSet applyPropertyAlias(java.lang.String,java.lang.String,com.ibm.icu.text.SymbolTable)
meth public com.ibm.icu.text.UnicodeSet clear()
meth public com.ibm.icu.text.UnicodeSet cloneAsThawed()
meth public com.ibm.icu.text.UnicodeSet closeOver(int)
meth public com.ibm.icu.text.UnicodeSet compact()
meth public com.ibm.icu.text.UnicodeSet complement()
meth public com.ibm.icu.text.UnicodeSet complement(int,int)
meth public com.ibm.icu.text.UnicodeSet complementAll(com.ibm.icu.text.UnicodeSet)
meth public com.ibm.icu.text.UnicodeSet freeze()
meth public com.ibm.icu.text.UnicodeSet remove(int,int)
meth public com.ibm.icu.text.UnicodeSet removeAll(com.ibm.icu.text.UnicodeSet)
meth public com.ibm.icu.text.UnicodeSet removeAll(java.util.Collection<java.lang.String>)
meth public com.ibm.icu.text.UnicodeSet retain(int,int)
meth public com.ibm.icu.text.UnicodeSet retainAll(com.ibm.icu.text.UnicodeSet)
meth public com.ibm.icu.text.UnicodeSet retainAll(java.util.Collection<java.lang.String>)
meth public com.ibm.icu.text.UnicodeSet set(com.ibm.icu.text.UnicodeSet)
meth public com.ibm.icu.text.UnicodeSet set(int,int)
meth public final boolean contains(java.lang.String)
meth public final boolean containsSome(com.ibm.icu.text.UnicodeSet)
meth public final boolean containsSome(int,int)
meth public final boolean containsSome(java.lang.String)
meth public final boolean containsSome(java.util.Collection<java.lang.String>)
meth public final com.ibm.icu.text.UnicodeSet add(int)
meth public final com.ibm.icu.text.UnicodeSet add(java.lang.String)
meth public final com.ibm.icu.text.UnicodeSet addAll(java.lang.String)
meth public final com.ibm.icu.text.UnicodeSet applyPattern(java.lang.String)
meth public final com.ibm.icu.text.UnicodeSet complement(int)
meth public final com.ibm.icu.text.UnicodeSet complement(java.lang.String)
meth public final com.ibm.icu.text.UnicodeSet complementAll(java.lang.String)
meth public final com.ibm.icu.text.UnicodeSet remove(int)
meth public final com.ibm.icu.text.UnicodeSet remove(java.lang.String)
meth public final com.ibm.icu.text.UnicodeSet removeAll(java.lang.String)
meth public final com.ibm.icu.text.UnicodeSet removeAllStrings()
meth public final com.ibm.icu.text.UnicodeSet retain(int)
meth public final com.ibm.icu.text.UnicodeSet retain(java.lang.String)
meth public final com.ibm.icu.text.UnicodeSet retainAll(java.lang.String)
meth public int charAt(int)
meth public int compareTo(com.ibm.icu.text.UnicodeSet)
meth public int compareTo(com.ibm.icu.text.UnicodeSet,com.ibm.icu.text.UnicodeSet$ComparisonStyle)
meth public int compareTo(java.lang.Iterable<java.lang.String>)
meth public int findIn(java.lang.CharSequence,int,boolean)
meth public int findLastIn(java.lang.CharSequence,int,boolean)
meth public int getRangeCount()
meth public int getRangeEnd(int)
meth public int getRangeStart(int)
meth public int hashCode()
meth public int indexOf(int)
meth public int matches(com.ibm.icu.text.Replaceable,int[],int,boolean)
meth public int matchesAt(java.lang.CharSequence,int)
meth public int size()
meth public int span(java.lang.CharSequence,com.ibm.icu.text.UnicodeSet$SpanCondition)
meth public int span(java.lang.CharSequence,int,com.ibm.icu.text.UnicodeSet$SpanCondition)
meth public int spanBack(java.lang.CharSequence,com.ibm.icu.text.UnicodeSet$SpanCondition)
meth public int spanBack(java.lang.CharSequence,int,com.ibm.icu.text.UnicodeSet$SpanCondition)
meth public java.lang.Iterable<java.lang.String> strings()
meth public java.lang.Object clone()
meth public java.lang.String getRegexEquivalent()
meth public java.lang.String stripFrom(java.lang.CharSequence,boolean)
meth public java.lang.String toPattern(boolean)
meth public java.lang.String toString()
meth public java.lang.StringBuffer _generatePattern(java.lang.StringBuffer,boolean)
meth public java.lang.StringBuffer _generatePattern(java.lang.StringBuffer,boolean,boolean)
meth public java.lang.String[] addAllTo(java.lang.String[])
meth public java.util.Iterator<java.lang.String> iterator()
meth public static <%0 extends java.lang.Comparable<{%%0}>> int compare(java.lang.Iterable<{%%0}>,java.lang.Iterable<{%%0}>)
meth public static <%0 extends java.lang.Comparable<{%%0}>> int compare(java.util.Collection<{%%0}>,java.util.Collection<{%%0}>,com.ibm.icu.text.UnicodeSet$ComparisonStyle)
meth public static <%0 extends java.lang.Object, %1 extends java.util.Collection<{%%0}>> {%%1} addAllTo(java.lang.Iterable<{%%0}>,{%%1})
meth public static <%0 extends java.lang.Object> {%%0}[] addAllTo(java.lang.Iterable<{%%0}>,{%%0}[])
meth public static boolean resemblesPattern(java.lang.String,int)
meth public static com.ibm.icu.text.UnicodeSet from(java.lang.String)
meth public static com.ibm.icu.text.UnicodeSet fromAll(java.lang.String)
meth public static int compare(int,java.lang.String)
meth public static int compare(java.lang.String,int)
meth public static int getSingleCodePoint(java.lang.String)
meth public static java.lang.String[] toArray(com.ibm.icu.text.UnicodeSet)
meth public void addMatchSetTo(com.ibm.icu.text.UnicodeSet)
supr com.ibm.icu.text.UnicodeFilter
hfds ANY_ID,ASCII_ID,ASSIGNED,GROW_EXTRA,HIGH,INCLUSIONS,LOW,NO_VERSION,START_EXTRA,bmpSet,buffer,len,list,pat,rangeList,stringSpan,strings
hcls Filter,GeneralCategoryMaskFilter,IntPropertyFilter,NumericValueFilter,UnicodeSetIterator2,VersionFilter

CLSS public final static !enum com.ibm.icu.text.UnicodeSet$ComparisonStyle
 outer com.ibm.icu.text.UnicodeSet
fld public final static com.ibm.icu.text.UnicodeSet$ComparisonStyle LEXICOGRAPHIC
fld public final static com.ibm.icu.text.UnicodeSet$ComparisonStyle LONGER_FIRST
fld public final static com.ibm.icu.text.UnicodeSet$ComparisonStyle SHORTER_FIRST
meth public static com.ibm.icu.text.UnicodeSet$ComparisonStyle valueOf(java.lang.String)
meth public static com.ibm.icu.text.UnicodeSet$ComparisonStyle[] values()
supr java.lang.Enum<com.ibm.icu.text.UnicodeSet$ComparisonStyle>

CLSS public final static !enum com.ibm.icu.text.UnicodeSet$SpanCondition
 outer com.ibm.icu.text.UnicodeSet
fld public final static com.ibm.icu.text.UnicodeSet$SpanCondition CONDITION_COUNT
fld public final static com.ibm.icu.text.UnicodeSet$SpanCondition CONTAINED
fld public final static com.ibm.icu.text.UnicodeSet$SpanCondition NOT_CONTAINED
fld public final static com.ibm.icu.text.UnicodeSet$SpanCondition SIMPLE
meth public static com.ibm.icu.text.UnicodeSet$SpanCondition valueOf(java.lang.String)
meth public static com.ibm.icu.text.UnicodeSet$SpanCondition[] values()
supr java.lang.Enum<com.ibm.icu.text.UnicodeSet$SpanCondition>

CLSS public abstract static com.ibm.icu.text.UnicodeSet$XSymbolTable
 outer com.ibm.icu.text.UnicodeSet
cons public init()
intf com.ibm.icu.text.SymbolTable
meth public boolean applyPropertyAlias(java.lang.String,java.lang.String,com.ibm.icu.text.UnicodeSet)
meth public char[] lookup(java.lang.String)
meth public com.ibm.icu.text.UnicodeMatcher lookupMatcher(int)
meth public java.lang.String parseReference(java.lang.String,java.text.ParsePosition,int)
supr java.lang.Object

CLSS public com.ibm.icu.text.UnicodeSetIterator
cons public init()
cons public init(com.ibm.icu.text.UnicodeSet)
fld protected int endElement
fld protected int nextElement
fld public int codepoint
fld public int codepointEnd
fld public java.lang.String string
fld public static int IS_STRING
meth protected void loadRange(int)
meth public boolean next()
meth public boolean nextRange()
meth public com.ibm.icu.text.UnicodeSet getSet()
meth public java.lang.String getString()
meth public void reset()
meth public void reset(com.ibm.icu.text.UnicodeSet)
supr java.lang.Object
hfds endRange,range,set,stringIterator

CLSS public com.ibm.icu.util.AnnualTimeZoneRule
cons public init(java.lang.String,int,int,com.ibm.icu.util.DateTimeRule,int,int)
fld public final static int MAX_YEAR = 2147483647
meth public boolean isEquivalentTo(com.ibm.icu.util.TimeZoneRule)
meth public boolean isTransitionRule()
meth public com.ibm.icu.util.DateTimeRule getRule()
meth public int getEndYear()
meth public int getStartYear()
meth public java.lang.String toString()
meth public java.util.Date getFinalStart(int,int)
meth public java.util.Date getFirstStart(int,int)
meth public java.util.Date getNextStart(long,int,int,boolean)
meth public java.util.Date getPreviousStart(long,int,int,boolean)
meth public java.util.Date getStartInYear(int,int,int)
supr com.ibm.icu.util.TimeZoneRule
hfds dateTimeRule,endYear,serialVersionUID,startYear

CLSS public abstract com.ibm.icu.util.BasicTimeZone
cons protected init()
fld protected final static int FORMER_LATTER_MASK = 12
fld protected final static int STD_DST_MASK = 3
fld public final static int LOCAL_DST = 3
fld public final static int LOCAL_FORMER = 4
fld public final static int LOCAL_LATTER = 12
fld public final static int LOCAL_STD = 1
meth public abstract com.ibm.icu.util.TimeZoneRule[] getTimeZoneRules()
meth public abstract com.ibm.icu.util.TimeZoneTransition getNextTransition(long,boolean)
meth public abstract com.ibm.icu.util.TimeZoneTransition getPreviousTransition(long,boolean)
meth public boolean hasEquivalentTransitions(com.ibm.icu.util.TimeZone,long,long)
meth public boolean hasEquivalentTransitions(com.ibm.icu.util.TimeZone,long,long,boolean)
meth public com.ibm.icu.util.TimeZoneRule[] getSimpleTimeZoneRulesNear(long)
meth public com.ibm.icu.util.TimeZoneRule[] getTimeZoneRules(long)
meth public void getOffsetFromLocal(long,int,int,int[])
supr com.ibm.icu.util.TimeZone
hfds MILLIS_PER_YEAR,serialVersionUID

CLSS public com.ibm.icu.util.BuddhistCalendar
cons public init()
cons public init(com.ibm.icu.util.TimeZone)
cons public init(com.ibm.icu.util.TimeZone,com.ibm.icu.util.ULocale)
cons public init(com.ibm.icu.util.TimeZone,java.util.Locale)
cons public init(com.ibm.icu.util.ULocale)
cons public init(int,int,int)
cons public init(int,int,int,int,int,int)
cons public init(java.util.Date)
cons public init(java.util.Locale)
fld public final static int BE = 0
meth protected int handleComputeMonthStart(int,int,boolean)
meth protected int handleGetExtendedYear()
meth protected int handleGetLimit(int,int)
meth protected void handleComputeFields(int)
meth public java.lang.String getType()
supr com.ibm.icu.util.GregorianCalendar
hfds BUDDHIST_ERA_START,GREGORIAN_EPOCH,serialVersionUID

CLSS public com.ibm.icu.util.ByteArrayWrapper
cons public init()
cons public init(byte[],int)
cons public init(java.nio.ByteBuffer)
fld public byte[] bytes
fld public int size
intf java.lang.Comparable<com.ibm.icu.util.ByteArrayWrapper>
meth public boolean equals(java.lang.Object)
meth public com.ibm.icu.util.ByteArrayWrapper ensureCapacity(int)
meth public final byte[] releaseBytes()
meth public final com.ibm.icu.util.ByteArrayWrapper append(byte[],int,int)
meth public final com.ibm.icu.util.ByteArrayWrapper set(byte[],int,int)
meth public int compareTo(com.ibm.icu.util.ByteArrayWrapper)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract com.ibm.icu.util.Calendar
cons protected init()
cons protected init(com.ibm.icu.util.TimeZone,com.ibm.icu.util.ULocale)
cons protected init(com.ibm.icu.util.TimeZone,java.util.Locale)
fld protected final static int BASE_FIELD_COUNT = 23
fld protected final static int EPOCH_JULIAN_DAY = 2440588
fld protected final static int GREATEST_MINIMUM = 1
fld protected final static int INTERNALLY_SET = 1
fld protected final static int JAN_1_1_JULIAN_DAY = 1721426
fld protected final static int LEAST_MAXIMUM = 2
fld protected final static int MAXIMUM = 3
fld protected final static int MAX_FIELD_COUNT = 32
fld protected final static int MAX_JULIAN = 2130706432
fld protected final static int MINIMUM = 0
fld protected final static int MINIMUM_USER_STAMP = 2
fld protected final static int MIN_JULIAN = -2130706432
fld protected final static int ONE_HOUR = 3600000
fld protected final static int ONE_MINUTE = 60000
fld protected final static int ONE_SECOND = 1000
fld protected final static int RESOLVE_REMAP = 32
fld protected final static int UNSET = 0
fld protected final static java.util.Date MAX_DATE
fld protected final static java.util.Date MIN_DATE
fld protected final static long MAX_MILLIS = 183882168921600000
fld protected final static long MIN_MILLIS = -184303902528000000
fld protected final static long ONE_DAY = 86400000
fld protected final static long ONE_WEEK = 604800000
fld public final static int AM = 0
fld public final static int AM_PM = 9
fld public final static int APRIL = 3
fld public final static int AUGUST = 7
fld public final static int DATE = 5
fld public final static int DAY_OF_MONTH = 5
fld public final static int DAY_OF_WEEK = 7
fld public final static int DAY_OF_WEEK_IN_MONTH = 8
fld public final static int DAY_OF_YEAR = 6
fld public final static int DECEMBER = 11
fld public final static int DOW_LOCAL = 18
fld public final static int DST_OFFSET = 16
fld public final static int ERA = 0
fld public final static int EXTENDED_YEAR = 19
fld public final static int FEBRUARY = 1
fld public final static int FRIDAY = 6
fld public final static int HOUR = 10
fld public final static int HOUR_OF_DAY = 11
fld public final static int IS_LEAP_MONTH = 22
fld public final static int JANUARY = 0
fld public final static int JULIAN_DAY = 20
fld public final static int JULY = 6
fld public final static int JUNE = 5
fld public final static int MARCH = 2
fld public final static int MAY = 4
fld public final static int MILLISECOND = 14
fld public final static int MILLISECONDS_IN_DAY = 21
fld public final static int MINUTE = 12
fld public final static int MONDAY = 2
fld public final static int MONTH = 2
fld public final static int NOVEMBER = 10
fld public final static int OCTOBER = 9
fld public final static int PM = 1
fld public final static int SATURDAY = 7
fld public final static int SECOND = 13
fld public final static int SEPTEMBER = 8
fld public final static int SUNDAY = 1
fld public final static int THURSDAY = 5
fld public final static int TUESDAY = 3
fld public final static int UNDECIMBER = 12
fld public final static int WEDNESDAY = 4
fld public final static int WEEKDAY = 0
fld public final static int WEEKEND = 1
fld public final static int WEEKEND_CEASE = 3
fld public final static int WEEKEND_ONSET = 2
fld public final static int WEEK_OF_MONTH = 4
fld public final static int WEEK_OF_YEAR = 3
fld public final static int YEAR = 1
fld public final static int YEAR_WOY = 17
fld public final static int ZONE_OFFSET = 15
innr public static FormatConfiguration
intf java.io.Serializable
intf java.lang.Cloneable
intf java.lang.Comparable<com.ibm.icu.util.Calendar>
meth protected abstract int handleComputeMonthStart(int,int,boolean)
meth protected abstract int handleGetExtendedYear()
meth protected abstract int handleGetLimit(int,int)
meth protected com.ibm.icu.text.DateFormat handleGetDateFormat(java.lang.String,com.ibm.icu.util.ULocale)
meth protected com.ibm.icu.text.DateFormat handleGetDateFormat(java.lang.String,java.lang.String,com.ibm.icu.util.ULocale)
meth protected com.ibm.icu.text.DateFormat handleGetDateFormat(java.lang.String,java.lang.String,java.util.Locale)
meth protected com.ibm.icu.text.DateFormat handleGetDateFormat(java.lang.String,java.util.Locale)
meth protected final int getGregorianDayOfMonth()
meth protected final int getGregorianDayOfYear()
meth protected final int getGregorianMonth()
meth protected final int getGregorianYear()
meth protected final int getStamp(int)
meth protected final int internalGet(int)
meth protected final int internalGet(int,int)
meth protected final int weekNumber(int,int)
meth protected final long internalGetTimeInMillis()
meth protected final static boolean isGregorianLeapYear(int)
meth protected final static int floorDivide(int,int)
meth protected final static int floorDivide(int,int,int[])
meth protected final static int floorDivide(long,int,int[])
meth protected final static int gregorianMonthLength(int,int)
meth protected final static int gregorianPreviousMonthLength(int,int)
meth protected final static int julianDayToDayOfWeek(int)
meth protected final static int millisToJulianDay(long)
meth protected final static long floorDivide(long,long)
meth protected final static long julianDayToMillis(int)
meth protected final void computeGregorianFields(int)
meth protected final void internalSet(int,int)
meth protected final void validateField(int,int,int)
meth protected int computeGregorianMonthStart(int,int)
meth protected int computeJulianDay()
meth protected int computeMillisInDay()
meth protected int computeZoneOffset(long,int)
meth protected int getDefaultDayInMonth(int,int)
meth protected int getDefaultMonthInYear(int)
meth protected int getLimit(int,int)
meth protected int handleComputeJulianDay(int)
meth protected int handleGetMonthLength(int,int)
meth protected int handleGetYearLength(int)
meth protected int newerField(int,int)
meth protected int newestStamp(int,int,int)
meth protected int resolveFields(int[][][])
meth protected int weekNumber(int,int,int)
meth protected int[] handleCreateFields()
meth protected int[][][] getFieldResolutionTable()
meth protected java.lang.String fieldName(int)
meth protected void complete()
meth protected void computeFields()
meth protected void computeTime()
meth protected void handleComputeFields(int)
meth protected void pinField(int)
meth protected void prepareGetActual(int,boolean)
meth protected void validateField(int)
meth protected void validateFields()
meth public boolean after(java.lang.Object)
meth public boolean before(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEquivalentTo(com.ibm.icu.util.Calendar)
meth public boolean isLenient()
meth public boolean isWeekend()
meth public boolean isWeekend(java.util.Date)
meth public com.ibm.icu.text.DateFormat getDateTimeFormat(int,int,com.ibm.icu.util.ULocale)
meth public com.ibm.icu.text.DateFormat getDateTimeFormat(int,int,java.util.Locale)
meth public com.ibm.icu.util.TimeZone getTimeZone()
meth public final boolean isSet(int)
meth public final com.ibm.icu.util.ULocale getLocale(com.ibm.icu.util.ULocale$Type)
meth public final int get(int)
meth public final int getFieldCount()
meth public final int getGreatestMinimum(int)
meth public final int getLeastMaximum(int)
meth public final int getMaximum(int)
meth public final int getMinimum(int)
meth public final java.util.Date getTime()
meth public final static java.lang.String[] getKeywordValuesForLocale(java.lang.String,com.ibm.icu.util.ULocale,boolean)
meth public final void clear()
meth public final void clear(int)
meth public final void roll(int,boolean)
meth public final void set(int,int)
meth public final void set(int,int,int)
meth public final void set(int,int,int,int,int)
meth public final void set(int,int,int,int,int,int)
meth public final void setTime(java.util.Date)
meth public int compareTo(com.ibm.icu.util.Calendar)
meth public int fieldDifference(java.util.Date,int)
meth public int getActualMaximum(int)
meth public int getActualMinimum(int)
meth public int getDayOfWeekType(int)
meth public int getFirstDayOfWeek()
meth public int getMinimalDaysInFirstWeek()
meth public int getWeekendTransition(int)
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.String getDisplayName(com.ibm.icu.util.ULocale)
meth public java.lang.String getDisplayName(java.util.Locale)
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public long getTimeInMillis()
meth public static com.ibm.icu.util.Calendar getInstance()
meth public static com.ibm.icu.util.Calendar getInstance(com.ibm.icu.util.TimeZone)
meth public static com.ibm.icu.util.Calendar getInstance(com.ibm.icu.util.TimeZone,com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.util.Calendar getInstance(com.ibm.icu.util.TimeZone,java.util.Locale)
meth public static com.ibm.icu.util.Calendar getInstance(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.util.Calendar getInstance(java.util.Locale)
meth public static com.ibm.icu.util.ULocale[] getAvailableULocales()
meth public static java.lang.String getDateTimePattern(com.ibm.icu.util.Calendar,com.ibm.icu.util.ULocale,int)
meth public static java.util.Locale[] getAvailableLocales()
meth public void add(int,int)
meth public void roll(int,int)
meth public void setFirstDayOfWeek(int)
meth public void setLenient(boolean)
meth public void setMinimalDaysInFirstWeek(int)
meth public void setTimeInMillis(long)
meth public void setTimeZone(com.ibm.icu.util.TimeZone)
supr java.lang.Object
hfds CALTYPE_BUDDHIST,CALTYPE_CHINESE,CALTYPE_COPTIC,CALTYPE_ETHIOPIC,CALTYPE_ETHIOPIC_AMETE_ALEM,CALTYPE_GREGORIAN,CALTYPE_HEBREW,CALTYPE_INDIAN,CALTYPE_ISLAMIC,CALTYPE_ISLAMIC_CIVIL,CALTYPE_JAPANESE,CALTYPE_PERSIAN,CALTYPE_ROC,CALTYPE_UNKNOWN,DATE_PRECEDENCE,DEFAULT_PATTERNS,DOW_PRECEDENCE,FIELD_NAME,GREGORIAN_MONTH_COUNT,LIMITS,PATTERN_CACHE,QUOTE,actualLocale,areAllFieldsSet,areFieldsSet,areFieldsVirtuallySet,cachedLocaleData,calTypes,fields,firstDayOfWeek,gregorianDayOfMonth,gregorianDayOfYear,gregorianMonth,gregorianYear,internalSetMask,isTimeSet,lenient,minimalDaysInFirstWeek,nextStamp,serialVersionUID,shim,stamp,time,validLocale,weekendCease,weekendCeaseMillis,weekendOnset,weekendOnsetMillis,zone
hcls CalendarFactory,CalendarShim,PatternData,WeekData

CLSS public static com.ibm.icu.util.Calendar$FormatConfiguration
 outer com.ibm.icu.util.Calendar
meth public com.ibm.icu.text.DateFormatSymbols getDateFormatSymbols()
meth public com.ibm.icu.util.Calendar getCalendar()
meth public com.ibm.icu.util.ULocale getLocale()
meth public java.lang.String getOverrideString()
meth public java.lang.String getPatternString()
supr java.lang.Object
hfds cal,formatData,loc,override,pattern

CLSS public com.ibm.icu.util.CaseInsensitiveString
cons public init(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getString()
meth public java.lang.String toString()
supr java.lang.Object
hfds folded,hash,string

CLSS public com.ibm.icu.util.ChineseCalendar
cons public init()
cons public init(com.ibm.icu.util.TimeZone)
cons public init(com.ibm.icu.util.TimeZone,com.ibm.icu.util.ULocale)
cons public init(com.ibm.icu.util.TimeZone,java.util.Locale)
cons public init(com.ibm.icu.util.ULocale)
cons public init(int,int,int,int)
cons public init(int,int,int,int,int,int,int)
cons public init(java.util.Date)
cons public init(java.util.Locale)
meth protected com.ibm.icu.text.DateFormat handleGetDateFormat(java.lang.String,java.lang.String,com.ibm.icu.util.ULocale)
meth protected int handleComputeMonthStart(int,int,boolean)
meth protected int handleGetExtendedYear()
meth protected int handleGetLimit(int,int)
meth protected int handleGetMonthLength(int,int)
meth protected int[][][] getFieldResolutionTable()
meth protected void handleComputeFields(int)
meth public java.lang.String getType()
meth public void add(int,int)
meth public void roll(int,int)
supr com.ibm.icu.util.Calendar
hfds CHINA_OFFSET,CHINESE_DATE_PRECEDENCE,CHINESE_EPOCH_YEAR,LIMITS,SYNODIC_GAP,astro,isLeapYear,newYearCache,serialVersionUID,winterSolsticeCache

CLSS public final com.ibm.icu.util.CompactByteArray
cons public init()
cons public init(byte)
cons public init(char[],byte[])
cons public init(java.lang.String,java.lang.String)
fld public final static int UNICODECOUNT = 65536
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public byte elementAt(char)
meth public byte[] getValueArray()
meth public char[] getIndexArray()
meth public int hashCode()
meth public java.lang.Object clone()
meth public void compact()
meth public void compact(boolean)
meth public void setElementAt(char,byte)
meth public void setElementAt(char,char,byte)
supr java.lang.Object
hfds BLOCKCOUNT,BLOCKMASK,BLOCKSHIFT,INDEXCOUNT,INDEXSHIFT,defaultValue,hashes,indices,isCompact,values

CLSS public final com.ibm.icu.util.CompactCharArray
cons public init()
cons public init(char)
cons public init(char[],char[])
cons public init(java.lang.String,java.lang.String)
fld public final static int BLOCKSHIFT = 5
fld public final static int UNICODECOUNT = 65536
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public char elementAt(char)
meth public char[] getIndexArray()
meth public char[] getValueArray()
meth public int hashCode()
meth public java.lang.Object clone()
meth public void compact()
meth public void compact(boolean)
meth public void setElementAt(char,char)
meth public void setElementAt(char,char,char)
supr java.lang.Object
hfds BLOCKCOUNT,BLOCKMASK,INDEXCOUNT,INDEXSHIFT,defaultValue,hashes,indices,isCompact,values

CLSS public final com.ibm.icu.util.CopticCalendar
cons public init()
cons public init(com.ibm.icu.util.TimeZone)
cons public init(com.ibm.icu.util.TimeZone,com.ibm.icu.util.ULocale)
cons public init(com.ibm.icu.util.TimeZone,java.util.Locale)
cons public init(com.ibm.icu.util.ULocale)
cons public init(int,int,int)
cons public init(int,int,int,int,int,int)
cons public init(java.util.Date)
cons public init(java.util.Locale)
fld public final static int AMSHIR = 5
fld public final static int BABA = 1
fld public final static int BARAMHAT = 6
fld public final static int BARAMOUDA = 7
fld public final static int BASHANS = 8
fld public final static int EPEP = 10
fld public final static int HATOR = 2
fld public final static int KIAHK = 3
fld public final static int MESRA = 11
fld public final static int NASIE = 12
fld public final static int PAONA = 9
fld public final static int TOBA = 4
fld public final static int TOUT = 0
meth protected int getJDEpochOffset()
meth protected int handleComputeMonthStart(int,int,boolean)
meth protected int handleGetExtendedYear()
meth protected int handleGetLimit(int,int)
meth protected int handleGetMonthLength(int,int)
meth protected void handleComputeFields(int)
meth public java.lang.String getType()
meth public static int ceToJD(long,int,int,int)
meth public static int copticToJD(long,int,int)
meth public static void jdToCE(int,int,int[])
supr com.ibm.icu.util.Calendar
hfds BCE,CE,JD_EPOCH_OFFSET,serialVersionUID

CLSS public com.ibm.icu.util.Currency
cons protected init(java.lang.String)
fld public final static int LONG_NAME = 1
fld public final static int PLURAL_LONG_NAME = 2
fld public final static int SYMBOL_NAME = 0
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public double getRoundingIncrement()
meth public final com.ibm.icu.util.ULocale getLocale(com.ibm.icu.util.ULocale$Type)
meth public final static java.lang.String[] getKeywordValuesForLocale(java.lang.String,com.ibm.icu.util.ULocale,boolean)
meth public int getDefaultFractionDigits()
meth public int hashCode()
meth public java.lang.String getCurrencyCode()
meth public java.lang.String getName(com.ibm.icu.util.ULocale,int,boolean[])
meth public java.lang.String getName(com.ibm.icu.util.ULocale,int,java.lang.String,boolean[])
meth public java.lang.String getName(java.util.Locale,int,boolean[])
meth public java.lang.String getName(java.util.Locale,int,java.lang.String,boolean[])
meth public java.lang.String getSymbol()
meth public java.lang.String getSymbol(com.ibm.icu.util.ULocale)
meth public java.lang.String getSymbol(java.util.Locale)
meth public java.lang.String toString()
meth public static boolean unregister(java.lang.Object)
meth public static com.ibm.icu.util.Currency getInstance(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.util.Currency getInstance(java.lang.String)
meth public static com.ibm.icu.util.Currency getInstance(java.util.Locale)
meth public static com.ibm.icu.util.ULocale[] getAvailableULocales()
meth public static java.lang.Object registerInstance(com.ibm.icu.util.Currency,com.ibm.icu.util.ULocale)
meth public static java.lang.String parse(com.ibm.icu.util.ULocale,java.lang.String,int,java.text.ParsePosition)
meth public static java.lang.String[] getAvailableCurrencyCodes(com.ibm.icu.util.ULocale,java.util.Date)
meth public static java.util.Locale[] getAvailableLocales()
supr com.ibm.icu.util.MeasureUnit
hfds CURRENCY_NAME_CACHE,DEBUG,EMPTY_STRING_ARRAY,EUR_STR,POW10,UND,actualLocale,isoCode,serialVersionUID,shim,validLocale
hcls CurrencyNameResultHandler,CurrencyStringInfo,ServiceShim

CLSS public com.ibm.icu.util.CurrencyAmount
cons public init(double,com.ibm.icu.util.Currency)
cons public init(java.lang.Number,com.ibm.icu.util.Currency)
meth public com.ibm.icu.util.Currency getCurrency()
supr com.ibm.icu.util.Measure

CLSS public final com.ibm.icu.util.DateInterval
cons public init(long,long)
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public long getFromDate()
meth public long getToDate()
supr java.lang.Object
hfds fromDate,serialVersionUID,toDate

CLSS public abstract interface com.ibm.icu.util.DateRule
meth public abstract boolean isBetween(java.util.Date,java.util.Date)
meth public abstract boolean isOn(java.util.Date)
meth public abstract java.util.Date firstAfter(java.util.Date)
meth public abstract java.util.Date firstBetween(java.util.Date,java.util.Date)

CLSS public com.ibm.icu.util.DateTimeRule
cons public init(int,int,int,boolean,int,int)
cons public init(int,int,int,int)
cons public init(int,int,int,int,int)
fld public final static int DOM = 0
fld public final static int DOW = 1
fld public final static int DOW_GEQ_DOM = 2
fld public final static int DOW_LEQ_DOM = 3
fld public final static int STANDARD_TIME = 1
fld public final static int UTC_TIME = 2
fld public final static int WALL_TIME = 0
intf java.io.Serializable
meth public int getDateRuleType()
meth public int getRuleDayOfMonth()
meth public int getRuleDayOfWeek()
meth public int getRuleMillisInDay()
meth public int getRuleMonth()
meth public int getRuleWeekInMonth()
meth public int getTimeRuleType()
meth public java.lang.String toString()
supr java.lang.Object
hfds DOWSTR,MONSTR,dateRuleType,dayOfMonth,dayOfWeek,millisInDay,month,serialVersionUID,timeRuleType,weekInMonth

CLSS public com.ibm.icu.util.EasterHoliday
cons public init(int,boolean,java.lang.String)
cons public init(int,java.lang.String)
cons public init(java.lang.String)
fld public final static com.ibm.icu.util.EasterHoliday ASCENSION
fld public final static com.ibm.icu.util.EasterHoliday ASH_WEDNESDAY
fld public final static com.ibm.icu.util.EasterHoliday CORPUS_CHRISTI
fld public final static com.ibm.icu.util.EasterHoliday EASTER_MONDAY
fld public final static com.ibm.icu.util.EasterHoliday EASTER_SUNDAY
fld public final static com.ibm.icu.util.EasterHoliday GOOD_FRIDAY
fld public final static com.ibm.icu.util.EasterHoliday MAUNDY_THURSDAY
fld public final static com.ibm.icu.util.EasterHoliday PALM_SUNDAY
fld public final static com.ibm.icu.util.EasterHoliday PENTECOST
fld public final static com.ibm.icu.util.EasterHoliday SHROVE_TUESDAY
fld public final static com.ibm.icu.util.EasterHoliday WHIT_MONDAY
fld public final static com.ibm.icu.util.EasterHoliday WHIT_SUNDAY
supr com.ibm.icu.util.Holiday

CLSS public final com.ibm.icu.util.EthiopicCalendar
cons public init()
cons public init(com.ibm.icu.util.TimeZone)
cons public init(com.ibm.icu.util.TimeZone,com.ibm.icu.util.ULocale)
cons public init(com.ibm.icu.util.TimeZone,java.util.Locale)
cons public init(com.ibm.icu.util.ULocale)
cons public init(int,int,int)
cons public init(int,int,int,int,int,int)
cons public init(java.util.Date)
cons public init(java.util.Locale)
fld public final static int GENBOT = 8
fld public final static int HAMLE = 10
fld public final static int HEDAR = 2
fld public final static int MEGABIT = 6
fld public final static int MESKEREM = 0
fld public final static int MIAZIA = 7
fld public final static int NEHASSE = 11
fld public final static int PAGUMEN = 12
fld public final static int SENE = 9
fld public final static int TAHSAS = 3
fld public final static int TEKEMT = 1
fld public final static int TER = 4
fld public final static int YEKATIT = 5
meth protected int getJDEpochOffset()
meth protected int handleComputeMonthStart(int,int,boolean)
meth protected int handleGetExtendedYear()
meth protected int handleGetLimit(int,int)
meth protected int handleGetMonthLength(int,int)
meth protected void handleComputeFields(int)
meth public boolean isAmeteAlemEra()
meth public java.lang.String getType()
meth public static int EthiopicToJD(long,int,int)
meth public static int ceToJD(long,int,int,int)
meth public static void jdToCE(int,int,int[])
meth public void setAmeteAlemEra(boolean)
supr com.ibm.icu.util.Calendar
hfds AMETE_ALEM,AMETE_ALEM_ERA,AMETE_MIHRET,AMETE_MIHRET_DELTA,AMETE_MIHRET_ERA,JD_EPOCH_OFFSET_AMETE_MIHRET,eraType,serialVersionUID

CLSS public abstract interface com.ibm.icu.util.Freezable<%0 extends java.lang.Object>
intf java.lang.Cloneable
meth public abstract boolean isFrozen()
meth public abstract {com.ibm.icu.util.Freezable%0} cloneAsThawed()
meth public abstract {com.ibm.icu.util.Freezable%0} freeze()

CLSS public com.ibm.icu.util.GlobalizationPreferences
cons public init()
fld public final static int BI_CHARACTER = 0
fld public final static int BI_LINE = 2
fld public final static int BI_SENTENCE = 3
fld public final static int BI_TITLE = 4
fld public final static int BI_WORD = 1
fld public final static int DF_FULL = 0
fld public final static int DF_LONG = 1
fld public final static int DF_MEDIUM = 2
fld public final static int DF_NONE = 4
fld public final static int DF_SHORT = 3
fld public final static int ID_CURRENCY = 7
fld public final static int ID_CURRENCY_SYMBOL = 8
fld public final static int ID_KEYWORD = 5
fld public final static int ID_KEYWORD_VALUE = 6
fld public final static int ID_LANGUAGE = 1
fld public final static int ID_LOCALE = 0
fld public final static int ID_SCRIPT = 2
fld public final static int ID_TERRITORY = 3
fld public final static int ID_TIMEZONE = 9
fld public final static int ID_VARIANT = 4
fld public final static int NF_CURRENCY = 1
fld public final static int NF_INTEGER = 4
fld public final static int NF_NUMBER = 0
fld public final static int NF_PERCENT = 2
fld public final static int NF_SCIENTIFIC = 3
intf com.ibm.icu.util.Freezable<com.ibm.icu.util.GlobalizationPreferences>
meth protected com.ibm.icu.text.BreakIterator guessBreakIterator(int)
meth protected com.ibm.icu.text.Collator guessCollator()
meth protected com.ibm.icu.text.DateFormat guessDateFormat(int,int)
meth protected com.ibm.icu.text.NumberFormat guessNumberFormat(int)
meth protected com.ibm.icu.util.Calendar guessCalendar()
meth protected com.ibm.icu.util.Currency guessCurrency()
meth protected com.ibm.icu.util.TimeZone guessTimeZone()
meth protected java.lang.String guessTerritory()
meth protected java.util.List<com.ibm.icu.util.ULocale> guessLocales()
meth protected java.util.List<com.ibm.icu.util.ULocale> processLocales(java.util.List<com.ibm.icu.util.ULocale>)
meth public boolean isFrozen()
meth public com.ibm.icu.text.BreakIterator getBreakIterator(int)
meth public com.ibm.icu.text.Collator getCollator()
meth public com.ibm.icu.text.DateFormat getDateFormat(int,int)
meth public com.ibm.icu.text.NumberFormat getNumberFormat(int)
meth public com.ibm.icu.util.Calendar getCalendar()
meth public com.ibm.icu.util.Currency getCurrency()
meth public com.ibm.icu.util.GlobalizationPreferences cloneAsThawed()
meth public com.ibm.icu.util.GlobalizationPreferences freeze()
meth public com.ibm.icu.util.GlobalizationPreferences reset()
meth public com.ibm.icu.util.GlobalizationPreferences setBreakIterator(int,com.ibm.icu.text.BreakIterator)
meth public com.ibm.icu.util.GlobalizationPreferences setCalendar(com.ibm.icu.util.Calendar)
meth public com.ibm.icu.util.GlobalizationPreferences setCollator(com.ibm.icu.text.Collator)
meth public com.ibm.icu.util.GlobalizationPreferences setCurrency(com.ibm.icu.util.Currency)
meth public com.ibm.icu.util.GlobalizationPreferences setDateFormat(int,int,com.ibm.icu.text.DateFormat)
meth public com.ibm.icu.util.GlobalizationPreferences setLocale(com.ibm.icu.util.ULocale)
meth public com.ibm.icu.util.GlobalizationPreferences setLocales(com.ibm.icu.util.ULocale[])
meth public com.ibm.icu.util.GlobalizationPreferences setLocales(java.lang.String)
meth public com.ibm.icu.util.GlobalizationPreferences setLocales(java.util.List<com.ibm.icu.util.ULocale>)
meth public com.ibm.icu.util.GlobalizationPreferences setNumberFormat(int,com.ibm.icu.text.NumberFormat)
meth public com.ibm.icu.util.GlobalizationPreferences setTerritory(java.lang.String)
meth public com.ibm.icu.util.GlobalizationPreferences setTimeZone(com.ibm.icu.util.TimeZone)
meth public com.ibm.icu.util.TimeZone getTimeZone()
meth public com.ibm.icu.util.ULocale getLocale(int)
meth public java.lang.String getDisplayName(java.lang.String,int)
meth public java.lang.String getTerritory()
meth public java.util.List<com.ibm.icu.util.ULocale> getLocales()
meth public java.util.ResourceBundle getResourceBundle(java.lang.String)
meth public java.util.ResourceBundle getResourceBundle(java.lang.String,java.lang.ClassLoader)
supr java.lang.Object
hfds BI_LIMIT,DF_LIMIT,NF_LIMIT,TYPE_BREAKITERATOR,TYPE_CALENDAR,TYPE_COLLATOR,TYPE_DATEFORMAT,TYPE_GENERIC,TYPE_LIMIT,TYPE_NUMBERFORMAT,available_locales,breakIterators,calendar,collator,currency,dateFormats,frozen,implicitLocales,language_territory_hack,language_territory_hack_map,locales,numberFormats,territory,territory_tzid_hack,territory_tzid_hack_map,timezone

CLSS public com.ibm.icu.util.GregorianCalendar
cons public init()
cons public init(com.ibm.icu.util.TimeZone)
cons public init(com.ibm.icu.util.TimeZone,com.ibm.icu.util.ULocale)
cons public init(com.ibm.icu.util.TimeZone,java.util.Locale)
cons public init(com.ibm.icu.util.ULocale)
cons public init(int,int,int)
cons public init(int,int,int,int,int)
cons public init(int,int,int,int,int,int)
cons public init(java.util.Locale)
fld protected boolean invertGregorian
fld protected boolean isGregorian
fld public final static int AD = 1
fld public final static int BC = 0
meth protected int handleComputeJulianDay(int)
meth protected int handleComputeMonthStart(int,int,boolean)
meth protected int handleGetExtendedYear()
meth protected int handleGetLimit(int,int)
meth protected int handleGetMonthLength(int,int)
meth protected int handleGetYearLength(int)
meth protected void handleComputeFields(int)
meth public boolean isEquivalentTo(com.ibm.icu.util.Calendar)
meth public boolean isLeapYear(int)
meth public final java.util.Date getGregorianChange()
meth public int getActualMaximum(int)
meth public int getActualMinimum(int)
meth public int hashCode()
meth public java.lang.String getType()
meth public void roll(int,int)
meth public void setGregorianChange(java.util.Date)
supr com.ibm.icu.util.Calendar
hfds EPOCH_YEAR,LIMITS,MONTH_COUNT,cutoverJulianDay,gregorianCutover,gregorianCutoverYear,serialVersionUID

CLSS public com.ibm.icu.util.HebrewCalendar
cons public init()
cons public init(com.ibm.icu.util.TimeZone)
cons public init(com.ibm.icu.util.TimeZone,com.ibm.icu.util.ULocale)
cons public init(com.ibm.icu.util.TimeZone,java.util.Locale)
cons public init(com.ibm.icu.util.ULocale)
cons public init(int,int,int)
cons public init(int,int,int,int,int,int)
cons public init(java.util.Date)
cons public init(java.util.Locale)
fld public final static int ADAR = 6
fld public final static int ADAR_1 = 5
fld public final static int AV = 11
fld public final static int ELUL = 12
fld public final static int HESHVAN = 1
fld public final static int IYAR = 8
fld public final static int KISLEV = 2
fld public final static int NISAN = 7
fld public final static int SHEVAT = 4
fld public final static int SIVAN = 9
fld public final static int TAMUZ = 10
fld public final static int TEVET = 3
fld public final static int TISHRI = 0
meth protected int handleComputeMonthStart(int,int,boolean)
meth protected int handleGetExtendedYear()
meth protected int handleGetLimit(int,int)
meth protected int handleGetMonthLength(int,int)
meth protected int handleGetYearLength(int)
meth protected void handleComputeFields(int)
meth public java.lang.String getType()
meth public static boolean isLeapYear(int)
meth public void add(int,int)
meth public void roll(int,int)
supr com.ibm.icu.util.Calendar
hfds BAHARAD,DAY_PARTS,HOUR_PARTS,LEAP_MONTH_START,LIMITS,MONTH_DAYS,MONTH_FRACT,MONTH_LENGTH,MONTH_PARTS,MONTH_START,cache,serialVersionUID

CLSS public com.ibm.icu.util.HebrewHoliday
cons public init(int,int,int,java.lang.String)
cons public init(int,int,java.lang.String)
fld public static com.ibm.icu.util.HebrewHoliday ESTHER
fld public static com.ibm.icu.util.HebrewHoliday GEDALIAH
fld public static com.ibm.icu.util.HebrewHoliday HANUKKAH
fld public static com.ibm.icu.util.HebrewHoliday HOSHANAH_RABBAH
fld public static com.ibm.icu.util.HebrewHoliday LAG_BOMER
fld public static com.ibm.icu.util.HebrewHoliday PASSOVER
fld public static com.ibm.icu.util.HebrewHoliday PESACH_SHEINI
fld public static com.ibm.icu.util.HebrewHoliday PURIM
fld public static com.ibm.icu.util.HebrewHoliday ROSH_HASHANAH
fld public static com.ibm.icu.util.HebrewHoliday SELIHOT
fld public static com.ibm.icu.util.HebrewHoliday SHAVUOT
fld public static com.ibm.icu.util.HebrewHoliday SHEMINI_ATZERET
fld public static com.ibm.icu.util.HebrewHoliday SHUSHAN_PURIM
fld public static com.ibm.icu.util.HebrewHoliday SIMCHAT_TORAH
fld public static com.ibm.icu.util.HebrewHoliday SUKKOT
fld public static com.ibm.icu.util.HebrewHoliday TAMMUZ_17
fld public static com.ibm.icu.util.HebrewHoliday TEVET_10
fld public static com.ibm.icu.util.HebrewHoliday TISHA_BAV
fld public static com.ibm.icu.util.HebrewHoliday TU_BSHEVAT
fld public static com.ibm.icu.util.HebrewHoliday YOM_HAATZMAUT
fld public static com.ibm.icu.util.HebrewHoliday YOM_HASHOAH
fld public static com.ibm.icu.util.HebrewHoliday YOM_HAZIKARON
fld public static com.ibm.icu.util.HebrewHoliday YOM_KIPPUR
fld public static com.ibm.icu.util.HebrewHoliday YOM_YERUSHALAYIM
supr com.ibm.icu.util.Holiday
hfds gCalendar

CLSS public abstract com.ibm.icu.util.Holiday
cons protected init(java.lang.String,com.ibm.icu.util.DateRule)
intf com.ibm.icu.util.DateRule
meth public boolean isBetween(java.util.Date,java.util.Date)
meth public boolean isOn(java.util.Date)
meth public com.ibm.icu.util.DateRule getRule()
meth public java.lang.String getDisplayName()
meth public java.lang.String getDisplayName(com.ibm.icu.util.ULocale)
meth public java.lang.String getDisplayName(java.util.Locale)
meth public java.util.Date firstAfter(java.util.Date)
meth public java.util.Date firstBetween(java.util.Date,java.util.Date)
meth public static com.ibm.icu.util.Holiday[] getHolidays()
meth public static com.ibm.icu.util.Holiday[] getHolidays(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.util.Holiday[] getHolidays(java.util.Locale)
meth public void setRule(com.ibm.icu.util.DateRule)
supr java.lang.Object
hfds name,noHolidays,rule

CLSS public com.ibm.icu.util.IllformedLocaleException
cons public init(java.lang.String)
cons public init(java.lang.String,int)
meth public int getErrorIndex()
supr java.lang.IllegalArgumentException
hfds _errIdx,serialVersionUID

CLSS public com.ibm.icu.util.IndianCalendar
cons public init()
cons public init(com.ibm.icu.util.TimeZone)
cons public init(com.ibm.icu.util.TimeZone,com.ibm.icu.util.ULocale)
cons public init(com.ibm.icu.util.TimeZone,java.util.Locale)
cons public init(com.ibm.icu.util.ULocale)
cons public init(int,int,int)
cons public init(int,int,int,int,int,int)
cons public init(java.util.Date)
cons public init(java.util.Locale)
fld public final static int AGRAHAYANA = 8
fld public final static int ASADHA = 3
fld public final static int ASVINA = 6
fld public final static int BHADRA = 5
fld public final static int CHAITRA = 0
fld public final static int IE = 0
fld public final static int JYAISTHA = 2
fld public final static int KARTIKA = 7
fld public final static int MAGHA = 10
fld public final static int PAUSA = 9
fld public final static int PHALGUNA = 11
fld public final static int SRAVANA = 4
fld public final static int VAISAKHA = 1
meth protected int handleComputeMonthStart(int,int,boolean)
meth protected int handleGetExtendedYear()
meth protected int handleGetLimit(int,int)
meth protected int handleGetMonthLength(int,int)
meth protected int handleGetYearLength(int)
meth protected void handleComputeFields(int)
meth public java.lang.String getType()
supr com.ibm.icu.util.Calendar
hfds INDIAN_ERA_START,INDIAN_YEAR_START,LIMITS,serialVersionUID

CLSS public com.ibm.icu.util.InitialTimeZoneRule
cons public init(java.lang.String,int,int)
meth public boolean isEquivalentTo(com.ibm.icu.util.TimeZoneRule)
meth public boolean isTransitionRule()
meth public java.util.Date getFinalStart(int,int)
meth public java.util.Date getFirstStart(int,int)
meth public java.util.Date getNextStart(long,int,int,boolean)
meth public java.util.Date getPreviousStart(long,int,int,boolean)
supr com.ibm.icu.util.TimeZoneRule
hfds serialVersionUID

CLSS public com.ibm.icu.util.IslamicCalendar
cons public init()
cons public init(com.ibm.icu.util.TimeZone)
cons public init(com.ibm.icu.util.TimeZone,com.ibm.icu.util.ULocale)
cons public init(com.ibm.icu.util.TimeZone,java.util.Locale)
cons public init(com.ibm.icu.util.ULocale)
cons public init(int,int,int)
cons public init(int,int,int,int,int,int)
cons public init(java.util.Date)
cons public init(java.util.Locale)
fld public final static int DHU_AL_HIJJAH = 11
fld public final static int DHU_AL_QIDAH = 10
fld public final static int JUMADA_1 = 4
fld public final static int JUMADA_2 = 5
fld public final static int MUHARRAM = 0
fld public final static int RABI_1 = 2
fld public final static int RABI_2 = 3
fld public final static int RAJAB = 6
fld public final static int RAMADAN = 8
fld public final static int SAFAR = 1
fld public final static int SHABAN = 7
fld public final static int SHAWWAL = 9
meth protected int handleComputeMonthStart(int,int,boolean)
meth protected int handleGetExtendedYear()
meth protected int handleGetLimit(int,int)
meth protected int handleGetMonthLength(int,int)
meth protected int handleGetYearLength(int)
meth protected void handleComputeFields(int)
meth public boolean isCivil()
meth public java.lang.String getType()
meth public void setCivil(boolean)
supr com.ibm.icu.util.Calendar
hfds HIJRA_MILLIS,LIMITS,astro,cache,civil,serialVersionUID

CLSS public com.ibm.icu.util.JapaneseCalendar
cons public init()
cons public init(com.ibm.icu.util.TimeZone)
cons public init(com.ibm.icu.util.TimeZone,com.ibm.icu.util.ULocale)
cons public init(com.ibm.icu.util.TimeZone,java.util.Locale)
cons public init(com.ibm.icu.util.ULocale)
cons public init(int,int,int)
cons public init(int,int,int,int)
cons public init(int,int,int,int,int,int)
cons public init(java.util.Date)
cons public init(java.util.Locale)
fld public final static int CURRENT_ERA
fld public final static int HEISEI
fld public final static int MEIJI
fld public final static int SHOWA
fld public final static int TAISHO
meth protected int getDefaultDayInMonth(int,int)
meth protected int getDefaultMonthInYear(int)
meth protected int handleGetExtendedYear()
meth protected int handleGetLimit(int,int)
meth protected void handleComputeFields(int)
meth public int getActualMaximum(int)
meth public java.lang.String getType()
supr com.ibm.icu.util.GregorianCalendar
hfds ERAS,GREGORIAN_EPOCH,serialVersionUID

CLSS public final com.ibm.icu.util.LocaleData
fld public final static int ALT_QUOTATION_END = 3
fld public final static int ALT_QUOTATION_START = 2
fld public final static int DELIMITER_COUNT = 4
fld public final static int ES_AUXILIARY = 1
fld public final static int ES_COUNT = 2
fld public final static int ES_STANDARD = 0
fld public final static int QUOTATION_END = 1
fld public final static int QUOTATION_START = 0
innr public final static MeasurementSystem
innr public final static PaperSize
meth public boolean getNoSubstitute()
meth public com.ibm.icu.text.UnicodeSet getExemplarSet(int,int)
meth public final static com.ibm.icu.util.LocaleData getInstance()
meth public final static com.ibm.icu.util.LocaleData getInstance(com.ibm.icu.util.ULocale)
meth public final static com.ibm.icu.util.LocaleData$MeasurementSystem getMeasurementSystem(com.ibm.icu.util.ULocale)
meth public final static com.ibm.icu.util.LocaleData$PaperSize getPaperSize(com.ibm.icu.util.ULocale)
meth public java.lang.String getDelimiter(int)
meth public java.lang.String getLocaleDisplayPattern()
meth public java.lang.String getLocaleSeparator()
meth public static com.ibm.icu.text.UnicodeSet getExemplarSet(com.ibm.icu.util.ULocale,int)
meth public static com.ibm.icu.util.VersionInfo getCLDRVersion()
meth public void setNoSubstitute(boolean)
supr java.lang.Object
hfds EXEMPLAR_CHARS,LOCALE_DISPLAY_PATTERN,MEASUREMENT_SYSTEM,PAPER_SIZE,PATTERN,SEPARATOR,bundle,gCLDRVersion,langBundle,noSubstitute

CLSS public final static com.ibm.icu.util.LocaleData$MeasurementSystem
 outer com.ibm.icu.util.LocaleData
fld public final static com.ibm.icu.util.LocaleData$MeasurementSystem SI
fld public final static com.ibm.icu.util.LocaleData$MeasurementSystem US
supr java.lang.Object
hfds systemID

CLSS public final static com.ibm.icu.util.LocaleData$PaperSize
 outer com.ibm.icu.util.LocaleData
meth public int getHeight()
meth public int getWidth()
supr java.lang.Object
hfds height,width

CLSS public com.ibm.icu.util.LocaleMatcher
cons public init(com.ibm.icu.util.LocalePriorityList)
cons public init(com.ibm.icu.util.LocalePriorityList,com.ibm.icu.util.LocaleMatcher$LanguageMatcherData)
cons public init(java.lang.String)
innr public static LanguageMatcherData
meth public com.ibm.icu.util.ULocale canonicalize(com.ibm.icu.util.ULocale)
meth public com.ibm.icu.util.ULocale getBestMatch(com.ibm.icu.util.LocalePriorityList)
meth public com.ibm.icu.util.ULocale getBestMatch(com.ibm.icu.util.ULocale)
meth public com.ibm.icu.util.ULocale getBestMatch(java.lang.String)
meth public double match(com.ibm.icu.util.ULocale,com.ibm.icu.util.ULocale,com.ibm.icu.util.ULocale,com.ibm.icu.util.ULocale)
meth public java.lang.String toString()
supr java.lang.Object
hfds DEBUG,DEFAULT_THRESHOLD,canonicalMap,defaultLanguage,defaultWritten,matcherData,maximizedLanguageToWeight
hcls Level,LocalePatternMatcher,ScoreData

CLSS public static com.ibm.icu.util.LocaleMatcher$LanguageMatcherData
 outer com.ibm.icu.util.LocaleMatcher
cons public init()
intf com.ibm.icu.util.Freezable<com.ibm.icu.util.LocaleMatcher$LanguageMatcherData>
meth public boolean isFrozen()
meth public com.ibm.icu.util.LocaleMatcher$LanguageMatcherData addDistance(java.lang.String,java.lang.String,int,boolean)
meth public com.ibm.icu.util.LocaleMatcher$LanguageMatcherData addDistance(java.lang.String,java.lang.String,int,java.lang.String)
meth public com.ibm.icu.util.LocaleMatcher$LanguageMatcherData cloneAsThawed()
meth public com.ibm.icu.util.LocaleMatcher$LanguageMatcherData freeze()
meth public double match(com.ibm.icu.util.ULocale,com.ibm.icu.util.ULocale,com.ibm.icu.util.ULocale,com.ibm.icu.util.ULocale)
supr java.lang.Object
hfds frozen,languageScores,regionScores,scriptScores

CLSS public com.ibm.icu.util.LocalePriorityList
innr public static Builder
intf java.lang.Iterable<com.ibm.icu.util.ULocale>
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Double getWeight(com.ibm.icu.util.ULocale)
meth public java.lang.String toString()
meth public java.util.Iterator<com.ibm.icu.util.ULocale> iterator()
meth public static com.ibm.icu.util.LocalePriorityList$Builder add(com.ibm.icu.util.LocalePriorityList)
meth public static com.ibm.icu.util.LocalePriorityList$Builder add(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.util.LocalePriorityList$Builder add(com.ibm.icu.util.ULocale,double)
meth public static com.ibm.icu.util.LocalePriorityList$Builder add(java.lang.String)
supr java.lang.Object
hfds D0,D1,languageSplitter,languagesAndWeights,myDescendingDouble,weightSplitter

CLSS public static com.ibm.icu.util.LocalePriorityList$Builder
 outer com.ibm.icu.util.LocalePriorityList
meth public !varargs com.ibm.icu.util.LocalePriorityList$Builder add(com.ibm.icu.util.ULocale[])
meth public com.ibm.icu.util.LocalePriorityList build()
meth public com.ibm.icu.util.LocalePriorityList build(boolean)
meth public com.ibm.icu.util.LocalePriorityList$Builder add(com.ibm.icu.util.LocalePriorityList)
meth public com.ibm.icu.util.LocalePriorityList$Builder add(com.ibm.icu.util.ULocale)
meth public com.ibm.icu.util.LocalePriorityList$Builder add(com.ibm.icu.util.ULocale,double)
meth public com.ibm.icu.util.LocalePriorityList$Builder add(java.lang.String)
supr java.lang.Object
hfds languageToWeight

CLSS public abstract com.ibm.icu.util.Measure
cons protected init(java.lang.Number,com.ibm.icu.util.MeasureUnit)
meth public boolean equals(java.lang.Object)
meth public com.ibm.icu.util.MeasureUnit getUnit()
meth public int hashCode()
meth public java.lang.Number getNumber()
meth public java.lang.String toString()
supr java.lang.Object
hfds number,unit

CLSS public abstract com.ibm.icu.util.MeasureUnit
cons protected init()
supr java.lang.Object

CLSS public com.ibm.icu.util.OverlayBundle
cons public init(java.lang.String[],java.util.Locale)
meth protected java.lang.Object handleGetObject(java.lang.String)
meth public java.util.Enumeration<java.lang.String> getKeys()
supr java.util.ResourceBundle
hfds baseNames,bundles,locale

CLSS public com.ibm.icu.util.RangeDateRule
cons public init()
intf com.ibm.icu.util.DateRule
meth public boolean isBetween(java.util.Date,java.util.Date)
meth public boolean isOn(java.util.Date)
meth public java.util.Date firstAfter(java.util.Date)
meth public java.util.Date firstBetween(java.util.Date,java.util.Date)
meth public void add(com.ibm.icu.util.DateRule)
meth public void add(java.util.Date,com.ibm.icu.util.DateRule)
supr java.lang.Object
hfds ranges

CLSS public abstract interface com.ibm.icu.util.RangeValueIterator
innr public static Element
meth public abstract boolean next(com.ibm.icu.util.RangeValueIterator$Element)
meth public abstract void reset()

CLSS public static com.ibm.icu.util.RangeValueIterator$Element
 outer com.ibm.icu.util.RangeValueIterator
cons public init()
fld public int limit
fld public int start
fld public int value
supr java.lang.Object

CLSS public com.ibm.icu.util.RuleBasedTimeZone
cons public init(java.lang.String,com.ibm.icu.util.InitialTimeZoneRule)
meth public boolean hasSameRules(com.ibm.icu.util.TimeZone)
meth public boolean inDaylightTime(java.util.Date)
meth public boolean useDaylightTime()
meth public com.ibm.icu.util.TimeZoneRule[] getTimeZoneRules()
meth public com.ibm.icu.util.TimeZoneTransition getNextTransition(long,boolean)
meth public com.ibm.icu.util.TimeZoneTransition getPreviousTransition(long,boolean)
meth public int getOffset(int,int,int,int,int,int)
meth public int getRawOffset()
meth public java.lang.Object clone()
meth public void addTransitionRule(com.ibm.icu.util.TimeZoneRule)
meth public void getOffset(long,boolean,int[])
meth public void getOffsetFromLocal(long,int,int,int[])
meth public void setRawOffset(int)
supr com.ibm.icu.util.BasicTimeZone
hfds finalRules,historicRules,historicTransitions,initialRule,serialVersionUID,upToDate

CLSS public com.ibm.icu.util.SimpleDateRule
cons public init(int,int)
cons public init(int,int,int,boolean)
intf com.ibm.icu.util.DateRule
meth public boolean isBetween(java.util.Date,java.util.Date)
meth public boolean isOn(java.util.Date)
meth public java.util.Date firstAfter(java.util.Date)
meth public java.util.Date firstBetween(java.util.Date,java.util.Date)
supr java.lang.Object
hfds calendar,dayOfMonth,dayOfWeek,gCalendar,month

CLSS public com.ibm.icu.util.SimpleHoliday
cons public init(int,int,int,java.lang.String)
cons public init(int,int,int,java.lang.String,int)
cons public init(int,int,int,java.lang.String,int,int)
cons public init(int,int,java.lang.String)
cons public init(int,int,java.lang.String,int)
cons public init(int,int,java.lang.String,int,int)
fld public final static com.ibm.icu.util.SimpleHoliday ALL_SAINTS_DAY
fld public final static com.ibm.icu.util.SimpleHoliday ALL_SOULS_DAY
fld public final static com.ibm.icu.util.SimpleHoliday ASSUMPTION
fld public final static com.ibm.icu.util.SimpleHoliday BOXING_DAY
fld public final static com.ibm.icu.util.SimpleHoliday CHRISTMAS
fld public final static com.ibm.icu.util.SimpleHoliday CHRISTMAS_EVE
fld public final static com.ibm.icu.util.SimpleHoliday EPIPHANY
fld public final static com.ibm.icu.util.SimpleHoliday IMMACULATE_CONCEPTION
fld public final static com.ibm.icu.util.SimpleHoliday MAY_DAY
fld public final static com.ibm.icu.util.SimpleHoliday NEW_YEARS_DAY
fld public final static com.ibm.icu.util.SimpleHoliday NEW_YEARS_EVE
fld public final static com.ibm.icu.util.SimpleHoliday ST_STEPHENS_DAY
supr com.ibm.icu.util.Holiday

CLSS public com.ibm.icu.util.SimpleTimeZone
cons public init(int,java.lang.String)
cons public init(int,java.lang.String,int,int,int,int,int,int,int,int)
cons public init(int,java.lang.String,int,int,int,int,int,int,int,int,int)
cons public init(int,java.lang.String,int,int,int,int,int,int,int,int,int,int,int)
fld public final static int STANDARD_TIME = 1
fld public final static int UTC_TIME = 2
fld public final static int WALL_TIME = 0
meth public boolean equals(java.lang.Object)
meth public boolean hasSameRules(com.ibm.icu.util.TimeZone)
meth public boolean inDaylightTime(java.util.Date)
meth public boolean useDaylightTime()
meth public com.ibm.icu.util.TimeZoneRule[] getTimeZoneRules()
meth public com.ibm.icu.util.TimeZoneTransition getNextTransition(long,boolean)
meth public com.ibm.icu.util.TimeZoneTransition getPreviousTransition(long,boolean)
meth public int getDSTSavings()
meth public int getOffset(int,int,int,int,int,int)
meth public int getOffset(int,int,int,int,int,int,int)
meth public int getRawOffset()
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.String toString()
meth public void getOffsetFromLocal(long,int,int,int[])
meth public void setDSTSavings(int)
meth public void setEndRule(int,int,int)
meth public void setEndRule(int,int,int,int)
meth public void setEndRule(int,int,int,int,boolean)
meth public void setID(java.lang.String)
meth public void setRawOffset(int)
meth public void setStartRule(int,int,int)
meth public void setStartRule(int,int,int,int)
meth public void setStartRule(int,int,int,int,boolean)
meth public void setStartYear(int)
supr com.ibm.icu.util.BasicTimeZone
hfds DOM_MODE,DOW_GE_DOM_MODE,DOW_IN_MONTH_MODE,DOW_LE_DOM_MODE,dst,dstRule,endDay,endDayOfWeek,endMode,endMonth,endTime,endTimeMode,firstTransition,initialRule,raw,serialVersionUID,startDay,startDayOfWeek,startMode,startMonth,startTime,startTimeMode,startYear,staticMonthLength,stdRule,transitionRulesInitialized,useDaylight,xinfo

CLSS public final com.ibm.icu.util.StringTokenizer
cons public init(java.lang.String)
cons public init(java.lang.String,com.ibm.icu.text.UnicodeSet)
cons public init(java.lang.String,com.ibm.icu.text.UnicodeSet,boolean)
cons public init(java.lang.String,com.ibm.icu.text.UnicodeSet,boolean,boolean)
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,boolean)
cons public init(java.lang.String,java.lang.String,boolean,boolean)
intf java.util.Enumeration<java.lang.Object>
meth public boolean hasMoreElements()
meth public boolean hasMoreTokens()
meth public int countTokens()
meth public java.lang.Object nextElement()
meth public java.lang.String nextToken()
meth public java.lang.String nextToken(com.ibm.icu.text.UnicodeSet)
meth public java.lang.String nextToken(java.lang.String)
supr java.lang.Object
hfds DEFAULT_DELIMITERS_,EMPTY_DELIMITER_,TOKEN_SIZE_,delims,m_coalesceDelimiters_,m_delimiters_,m_length_,m_nextOffset_,m_returnDelimiters_,m_source_,m_tokenLimit_,m_tokenOffset_,m_tokenSize_,m_tokenStart_

CLSS public com.ibm.icu.util.TaiwanCalendar
cons public init()
cons public init(com.ibm.icu.util.TimeZone)
cons public init(com.ibm.icu.util.TimeZone,com.ibm.icu.util.ULocale)
cons public init(com.ibm.icu.util.TimeZone,java.util.Locale)
cons public init(com.ibm.icu.util.ULocale)
cons public init(int,int,int)
cons public init(int,int,int,int,int,int)
cons public init(java.util.Date)
cons public init(java.util.Locale)
fld public final static int BEFORE_MINGUO = 0
fld public final static int MINGUO = 1
meth protected int handleGetExtendedYear()
meth protected int handleGetLimit(int,int)
meth protected void handleComputeFields(int)
meth public java.lang.String getType()
supr com.ibm.icu.util.GregorianCalendar
hfds GREGORIAN_EPOCH,Taiwan_ERA_START,serialVersionUID

CLSS public com.ibm.icu.util.TimeArrayTimeZoneRule
cons public init(java.lang.String,int,int,long[],int)
meth public boolean isEquivalentTo(com.ibm.icu.util.TimeZoneRule)
meth public boolean isTransitionRule()
meth public int getTimeType()
meth public java.lang.String toString()
meth public java.util.Date getFinalStart(int,int)
meth public java.util.Date getFirstStart(int,int)
meth public java.util.Date getNextStart(long,int,int,boolean)
meth public java.util.Date getPreviousStart(long,int,int,boolean)
meth public long[] getStartTimes()
supr com.ibm.icu.util.TimeZoneRule
hfds serialVersionUID,startTimes,timeType

CLSS public com.ibm.icu.util.TimeUnit
fld public static com.ibm.icu.util.TimeUnit DAY
fld public static com.ibm.icu.util.TimeUnit HOUR
fld public static com.ibm.icu.util.TimeUnit MINUTE
fld public static com.ibm.icu.util.TimeUnit MONTH
fld public static com.ibm.icu.util.TimeUnit SECOND
fld public static com.ibm.icu.util.TimeUnit WEEK
fld public static com.ibm.icu.util.TimeUnit YEAR
meth public java.lang.String toString()
meth public static com.ibm.icu.util.TimeUnit[] values()
supr com.ibm.icu.util.MeasureUnit
hfds name,valueCount,values

CLSS public com.ibm.icu.util.TimeUnitAmount
cons public init(double,com.ibm.icu.util.TimeUnit)
cons public init(java.lang.Number,com.ibm.icu.util.TimeUnit)
meth public com.ibm.icu.util.TimeUnit getTimeUnit()
supr com.ibm.icu.util.Measure

CLSS public abstract com.ibm.icu.util.TimeZone
cons public init()
fld public final static int GENERIC_LOCATION = 7
fld public final static int LONG = 1
fld public final static int LONG_GENERIC = 3
fld public final static int LONG_GMT = 5
fld public final static int SHORT = 0
fld public final static int SHORT_COMMONLY_USED = 6
fld public final static int SHORT_GENERIC = 2
fld public final static int SHORT_GMT = 4
fld public final static int TIMEZONE_ICU = 0
fld public final static int TIMEZONE_JDK = 1
fld public static com.ibm.icu.impl.ICULogger TimeZoneLogger
intf java.io.Serializable
intf java.lang.Cloneable
meth public abstract boolean inDaylightTime(java.util.Date)
meth public abstract boolean useDaylightTime()
meth public abstract int getOffset(int,int,int,int,int,int)
meth public abstract int getRawOffset()
meth public abstract void setRawOffset(int)
meth public boolean equals(java.lang.Object)
meth public boolean hasSameRules(com.ibm.icu.util.TimeZone)
meth public final java.lang.String getDisplayName()
meth public final java.lang.String getDisplayName(boolean,int)
meth public final java.lang.String getDisplayName(com.ibm.icu.util.ULocale)
meth public final java.lang.String getDisplayName(java.util.Locale)
meth public int getDSTSavings()
meth public int getOffset(long)
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.String getDisplayName(boolean,int,com.ibm.icu.util.ULocale)
meth public java.lang.String getDisplayName(boolean,int,java.util.Locale)
meth public java.lang.String getID()
meth public static com.ibm.icu.util.TimeZone getDefault()
meth public static com.ibm.icu.util.TimeZone getTimeZone(java.lang.String)
meth public static com.ibm.icu.util.TimeZone getTimeZone(java.lang.String,int)
meth public static int countEquivalentIDs(java.lang.String)
meth public static int getDefaultTimeZoneType()
meth public static java.lang.String getCanonicalID(java.lang.String)
meth public static java.lang.String getCanonicalID(java.lang.String,boolean[])
meth public static java.lang.String getEquivalentID(java.lang.String,int)
meth public static java.lang.String getTZDataVersion()
meth public static java.lang.String[] getAvailableIDs()
meth public static java.lang.String[] getAvailableIDs(int)
meth public static java.lang.String[] getAvailableIDs(java.lang.String)
meth public static void setDefault(com.ibm.icu.util.TimeZone)
meth public static void setDefaultTimeZoneType(int)
meth public void getOffset(long,boolean,int[])
meth public void setID(java.lang.String)
supr java.lang.Object
hfds ID,TZDATA_VERSION,TZIMPL_CONFIG_ICU,TZIMPL_CONFIG_JDK,TZIMPL_CONFIG_KEY,TZ_IMPL,cachedLocaleData,defaultZone,serialVersionUID

CLSS public abstract com.ibm.icu.util.TimeZoneRule
cons public init(java.lang.String,int,int)
intf java.io.Serializable
meth public abstract boolean isTransitionRule()
meth public abstract java.util.Date getFinalStart(int,int)
meth public abstract java.util.Date getFirstStart(int,int)
meth public abstract java.util.Date getNextStart(long,int,int,boolean)
meth public abstract java.util.Date getPreviousStart(long,int,int,boolean)
meth public boolean isEquivalentTo(com.ibm.icu.util.TimeZoneRule)
meth public int getDSTSavings()
meth public int getRawOffset()
meth public java.lang.String getName()
meth public java.lang.String toString()
supr java.lang.Object
hfds dstSavings,name,rawOffset,serialVersionUID

CLSS public com.ibm.icu.util.TimeZoneTransition
cons public init(long,com.ibm.icu.util.TimeZoneRule,com.ibm.icu.util.TimeZoneRule)
meth public com.ibm.icu.util.TimeZoneRule getFrom()
meth public com.ibm.icu.util.TimeZoneRule getTo()
meth public java.lang.String toString()
meth public long getTime()
supr java.lang.Object
hfds from,time,to

CLSS public final com.ibm.icu.util.ULocale
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
fld public final static char PRIVATE_USE_EXTENSION = 'x'
fld public final static char UNICODE_LOCALE_EXTENSION = 'u'
fld public final static com.ibm.icu.util.ULocale CANADA
fld public final static com.ibm.icu.util.ULocale CANADA_FRENCH
fld public final static com.ibm.icu.util.ULocale CHINA
fld public final static com.ibm.icu.util.ULocale CHINESE
fld public final static com.ibm.icu.util.ULocale ENGLISH
fld public final static com.ibm.icu.util.ULocale FRANCE
fld public final static com.ibm.icu.util.ULocale FRENCH
fld public final static com.ibm.icu.util.ULocale GERMAN
fld public final static com.ibm.icu.util.ULocale GERMANY
fld public final static com.ibm.icu.util.ULocale ITALIAN
fld public final static com.ibm.icu.util.ULocale ITALY
fld public final static com.ibm.icu.util.ULocale JAPAN
fld public final static com.ibm.icu.util.ULocale JAPANESE
fld public final static com.ibm.icu.util.ULocale KOREA
fld public final static com.ibm.icu.util.ULocale KOREAN
fld public final static com.ibm.icu.util.ULocale PRC
fld public final static com.ibm.icu.util.ULocale ROOT
fld public final static com.ibm.icu.util.ULocale SIMPLIFIED_CHINESE
fld public final static com.ibm.icu.util.ULocale TAIWAN
fld public final static com.ibm.icu.util.ULocale TRADITIONAL_CHINESE
fld public final static com.ibm.icu.util.ULocale UK
fld public final static com.ibm.icu.util.ULocale US
fld public static com.ibm.icu.util.ULocale$Type ACTUAL_LOCALE
fld public static com.ibm.icu.util.ULocale$Type VALID_LOCALE
innr public final static Builder
innr public final static Type
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public com.ibm.icu.util.ULocale getFallback()
meth public com.ibm.icu.util.ULocale setKeywordValue(java.lang.String,java.lang.String)
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.String getBaseName()
meth public java.lang.String getCharacterOrientation()
meth public java.lang.String getCountry()
meth public java.lang.String getDisplayCountry()
meth public java.lang.String getDisplayCountry(com.ibm.icu.util.ULocale)
meth public java.lang.String getDisplayKeywordValue(java.lang.String)
meth public java.lang.String getDisplayKeywordValue(java.lang.String,com.ibm.icu.util.ULocale)
meth public java.lang.String getDisplayLanguage()
meth public java.lang.String getDisplayLanguage(com.ibm.icu.util.ULocale)
meth public java.lang.String getDisplayLanguageWithDialect()
meth public java.lang.String getDisplayLanguageWithDialect(com.ibm.icu.util.ULocale)
meth public java.lang.String getDisplayName()
meth public java.lang.String getDisplayName(com.ibm.icu.util.ULocale)
meth public java.lang.String getDisplayNameWithDialect()
meth public java.lang.String getDisplayNameWithDialect(com.ibm.icu.util.ULocale)
meth public java.lang.String getDisplayScript()
meth public java.lang.String getDisplayScript(com.ibm.icu.util.ULocale)
meth public java.lang.String getDisplayVariant()
meth public java.lang.String getDisplayVariant(com.ibm.icu.util.ULocale)
meth public java.lang.String getExtension(char)
meth public java.lang.String getISO3Country()
meth public java.lang.String getISO3Language()
meth public java.lang.String getKeywordValue(java.lang.String)
meth public java.lang.String getLanguage()
meth public java.lang.String getLineOrientation()
meth public java.lang.String getName()
meth public java.lang.String getScript()
meth public java.lang.String getUnicodeLocaleType(java.lang.String)
meth public java.lang.String getVariant()
meth public java.lang.String toLanguageTag()
meth public java.lang.String toString()
meth public java.util.Iterator<java.lang.String> getKeywords()
meth public java.util.Locale toLocale()
meth public java.util.Set<java.lang.Character> getExtensionKeys()
meth public java.util.Set<java.lang.String> getUnicodeLocaleKeys()
meth public static com.ibm.icu.util.ULocale acceptLanguage(com.ibm.icu.util.ULocale[],boolean[])
meth public static com.ibm.icu.util.ULocale acceptLanguage(com.ibm.icu.util.ULocale[],com.ibm.icu.util.ULocale[],boolean[])
meth public static com.ibm.icu.util.ULocale acceptLanguage(java.lang.String,boolean[])
meth public static com.ibm.icu.util.ULocale acceptLanguage(java.lang.String,com.ibm.icu.util.ULocale[],boolean[])
meth public static com.ibm.icu.util.ULocale addLikelySubtags(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.util.ULocale createCanonical(java.lang.String)
meth public static com.ibm.icu.util.ULocale forLanguageTag(java.lang.String)
meth public static com.ibm.icu.util.ULocale forLocale(java.util.Locale)
meth public static com.ibm.icu.util.ULocale getDefault()
meth public static com.ibm.icu.util.ULocale minimizeSubtags(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.util.ULocale[] getAvailableLocales()
meth public static java.lang.String canonicalize(java.lang.String)
meth public static java.lang.String getBaseName(java.lang.String)
meth public static java.lang.String getCountry(java.lang.String)
meth public static java.lang.String getDisplayCountry(java.lang.String,com.ibm.icu.util.ULocale)
meth public static java.lang.String getDisplayCountry(java.lang.String,java.lang.String)
meth public static java.lang.String getDisplayKeyword(java.lang.String)
meth public static java.lang.String getDisplayKeyword(java.lang.String,com.ibm.icu.util.ULocale)
meth public static java.lang.String getDisplayKeyword(java.lang.String,java.lang.String)
meth public static java.lang.String getDisplayKeywordValue(java.lang.String,java.lang.String,com.ibm.icu.util.ULocale)
meth public static java.lang.String getDisplayKeywordValue(java.lang.String,java.lang.String,java.lang.String)
meth public static java.lang.String getDisplayLanguage(java.lang.String,com.ibm.icu.util.ULocale)
meth public static java.lang.String getDisplayLanguage(java.lang.String,java.lang.String)
meth public static java.lang.String getDisplayLanguageWithDialect(java.lang.String,com.ibm.icu.util.ULocale)
meth public static java.lang.String getDisplayLanguageWithDialect(java.lang.String,java.lang.String)
meth public static java.lang.String getDisplayName(java.lang.String,com.ibm.icu.util.ULocale)
meth public static java.lang.String getDisplayName(java.lang.String,java.lang.String)
meth public static java.lang.String getDisplayNameWithDialect(java.lang.String,com.ibm.icu.util.ULocale)
meth public static java.lang.String getDisplayNameWithDialect(java.lang.String,java.lang.String)
meth public static java.lang.String getDisplayScript(java.lang.String,com.ibm.icu.util.ULocale)
meth public static java.lang.String getDisplayScript(java.lang.String,java.lang.String)
meth public static java.lang.String getDisplayVariant(java.lang.String,com.ibm.icu.util.ULocale)
meth public static java.lang.String getDisplayVariant(java.lang.String,java.lang.String)
meth public static java.lang.String getFallback(java.lang.String)
meth public static java.lang.String getISO3Country(java.lang.String)
meth public static java.lang.String getISO3Language(java.lang.String)
meth public static java.lang.String getKeywordValue(java.lang.String,java.lang.String)
meth public static java.lang.String getLanguage(java.lang.String)
meth public static java.lang.String getName(java.lang.String)
meth public static java.lang.String getScript(java.lang.String)
meth public static java.lang.String getVariant(java.lang.String)
meth public static java.lang.String setKeywordValue(java.lang.String,java.lang.String,java.lang.String)
meth public static java.lang.String[] getISOCountries()
meth public static java.lang.String[] getISOLanguages()
meth public static java.util.Iterator<java.lang.String> getKeywords(java.lang.String)
meth public static void setDefault(com.ibm.icu.util.ULocale)
supr java.lang.Object
hfds CACHE,CANONICALIZE_MAP,EMPTY_LOCALE,EMPTY_STRING,UNDEFINED_LANGUAGE,UNDEFINED_REGION,UNDEFINED_SCRIPT,UNDERSCORE,_javaLocaleMap,baseLocale,defaultLocale,defaultULocale,extensions,locale,localeID,nameCache,serialVersionUID,variantsToKeywords

CLSS public final static com.ibm.icu.util.ULocale$Builder
 outer com.ibm.icu.util.ULocale
cons public init()
cons public init(boolean)
meth public boolean isLenientVariant()
meth public com.ibm.icu.util.ULocale build()
meth public com.ibm.icu.util.ULocale$Builder clear()
meth public com.ibm.icu.util.ULocale$Builder clearExtensions()
meth public com.ibm.icu.util.ULocale$Builder setExtension(char,java.lang.String)
meth public com.ibm.icu.util.ULocale$Builder setLanguage(java.lang.String)
meth public com.ibm.icu.util.ULocale$Builder setLanguageTag(java.lang.String)
meth public com.ibm.icu.util.ULocale$Builder setLocale(com.ibm.icu.util.ULocale)
meth public com.ibm.icu.util.ULocale$Builder setRegion(java.lang.String)
meth public com.ibm.icu.util.ULocale$Builder setScript(java.lang.String)
meth public com.ibm.icu.util.ULocale$Builder setUnicodeLocaleKeyword(java.lang.String,java.lang.String)
meth public com.ibm.icu.util.ULocale$Builder setVariant(java.lang.String)
supr java.lang.Object
hfds _locbld

CLSS public final static com.ibm.icu.util.ULocale$Type
 outer com.ibm.icu.util.ULocale
supr java.lang.Object

CLSS public abstract com.ibm.icu.util.UResourceBundle
cons public init()
fld public final static int ARRAY = 8
fld public final static int BINARY = 1
fld public final static int INT = 7
fld public final static int INT_VECTOR = 14
fld public final static int NONE = -1
fld public final static int STRING = 0
fld public final static int TABLE = 2
meth protected abstract com.ibm.icu.util.UResourceBundle getParent()
meth protected abstract java.lang.String getBaseName()
meth protected abstract java.lang.String getLocaleID()
meth protected abstract void setLoadingStatus(int)
meth protected boolean isTopLevelResource()
meth protected com.ibm.icu.util.UResourceBundle findTopLevel(int)
meth protected com.ibm.icu.util.UResourceBundle findTopLevel(java.lang.String)
meth protected com.ibm.icu.util.UResourceBundle handleGet(int,java.util.HashMap<java.lang.String,java.lang.String>,com.ibm.icu.util.UResourceBundle)
meth protected com.ibm.icu.util.UResourceBundle handleGet(java.lang.String,java.util.HashMap<java.lang.String,java.lang.String>,com.ibm.icu.util.UResourceBundle)
meth protected java.lang.Object handleGetObject(java.lang.String)
meth protected java.lang.String[] handleGetStringArray()
meth protected java.util.Enumeration<java.lang.String> handleGetKeys()
meth protected java.util.Set<java.lang.String> handleKeySet()
meth protected static com.ibm.icu.util.UResourceBundle addToCache(java.lang.ClassLoader,java.lang.String,com.ibm.icu.util.ULocale,com.ibm.icu.util.UResourceBundle)
meth protected static com.ibm.icu.util.UResourceBundle getBundleInstance(java.lang.String,java.lang.String,java.lang.ClassLoader,boolean)
meth protected static com.ibm.icu.util.UResourceBundle instantiateBundle(java.lang.String,java.lang.String,java.lang.ClassLoader,boolean)
meth protected static com.ibm.icu.util.UResourceBundle loadFromCache(java.lang.ClassLoader,java.lang.String,com.ibm.icu.util.ULocale)
meth public abstract com.ibm.icu.util.ULocale getULocale()
meth public byte[] getBinary(byte[])
meth public com.ibm.icu.util.UResourceBundle get(int)
meth public com.ibm.icu.util.UResourceBundle get(java.lang.String)
meth public com.ibm.icu.util.UResourceBundleIterator getIterator()
meth public com.ibm.icu.util.VersionInfo getVersion()
meth public int getInt()
meth public int getSize()
meth public int getType()
meth public int getUInt()
meth public int[] getIntVector()
meth public java.lang.String getKey()
meth public java.lang.String getString()
meth public java.lang.String getString(int)
meth public java.lang.String[] getStringArray()
meth public java.nio.ByteBuffer getBinary()
meth public java.util.Enumeration<java.lang.String> getKeys()
meth public java.util.Locale getLocale()
meth public java.util.Set<java.lang.String> keySet()
meth public static com.ibm.icu.util.UResourceBundle getBundleInstance(com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.util.UResourceBundle getBundleInstance(java.lang.String)
meth public static com.ibm.icu.util.UResourceBundle getBundleInstance(java.lang.String,com.ibm.icu.util.ULocale)
meth public static com.ibm.icu.util.UResourceBundle getBundleInstance(java.lang.String,com.ibm.icu.util.ULocale,java.lang.ClassLoader)
meth public static com.ibm.icu.util.UResourceBundle getBundleInstance(java.lang.String,java.lang.String)
meth public static com.ibm.icu.util.UResourceBundle getBundleInstance(java.lang.String,java.lang.String,java.lang.ClassLoader)
meth public static com.ibm.icu.util.UResourceBundle getBundleInstance(java.lang.String,java.util.Locale)
meth public static com.ibm.icu.util.UResourceBundle getBundleInstance(java.lang.String,java.util.Locale,java.lang.ClassLoader)
meth public static void resetBundleCache()
supr java.util.ResourceBundle
hfds BUNDLE_CACHE,ROOT_CACHE,ROOT_ICU,ROOT_JAVA,ROOT_MISSING,cacheKey,keys
hcls ResourceCacheKey

CLSS public com.ibm.icu.util.UResourceBundleIterator
cons public init(com.ibm.icu.util.UResourceBundle)
meth public boolean hasNext()
meth public com.ibm.icu.util.UResourceBundle next()
meth public java.lang.String nextString()
meth public void reset()
supr java.lang.Object
hfds bundle,index,size

CLSS public com.ibm.icu.util.UResourceTypeMismatchException
cons public init(java.lang.String)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public final com.ibm.icu.util.UniversalTimeScale
fld public final static int DB2_TIME = 8
fld public final static int DOTNET_DATE_TIME = 4
fld public final static int EPOCH_OFFSET_MINUS_1_VALUE = 7
fld public final static int EPOCH_OFFSET_PLUS_1_VALUE = 6
fld public final static int EPOCH_OFFSET_VALUE = 1
fld public final static int EXCEL_TIME = 7
fld public final static int FROM_MAX_VALUE = 3
fld public final static int FROM_MIN_VALUE = 2
fld public final static int ICU4C_TIME = 2
fld public final static int JAVA_TIME = 0
fld public final static int MAC_OLD_TIME = 5
fld public final static int MAC_TIME = 6
fld public final static int MAX_ROUND_VALUE = 10
fld public final static int MAX_SCALE = 10
fld public final static int MAX_SCALE_VALUE = 11
fld public final static int MIN_ROUND_VALUE = 9
fld public final static int TO_MAX_VALUE = 5
fld public final static int TO_MIN_VALUE = 4
fld public final static int UNITS_ROUND_VALUE = 8
fld public final static int UNITS_VALUE = 0
fld public final static int UNIX_MICROSECONDS_TIME = 9
fld public final static int UNIX_TIME = 1
fld public final static int WINDOWS_FILE_TIME = 3
meth public static com.ibm.icu.math.BigDecimal bigDecimalFrom(com.ibm.icu.math.BigDecimal,int)
meth public static com.ibm.icu.math.BigDecimal bigDecimalFrom(double,int)
meth public static com.ibm.icu.math.BigDecimal bigDecimalFrom(long,int)
meth public static com.ibm.icu.math.BigDecimal toBigDecimal(com.ibm.icu.math.BigDecimal,int)
meth public static com.ibm.icu.math.BigDecimal toBigDecimal(long,int)
meth public static com.ibm.icu.math.BigDecimal toBigDecimalTrunc(com.ibm.icu.math.BigDecimal,int)
meth public static long from(long,int)
meth public static long getTimeScaleValue(int,int)
meth public static long toLong(long,int)
supr java.lang.Object
hfds days,hours,microseconds,milliseconds,minutes,seconds,ticks,timeScaleTable
hcls TimeScaleData

CLSS public com.ibm.icu.util.VTimeZone
meth public boolean hasEquivalentTransitions(com.ibm.icu.util.TimeZone,long,long)
meth public boolean hasSameRules(com.ibm.icu.util.TimeZone)
meth public boolean inDaylightTime(java.util.Date)
meth public boolean useDaylightTime()
meth public com.ibm.icu.util.TimeZoneRule[] getTimeZoneRules()
meth public com.ibm.icu.util.TimeZoneRule[] getTimeZoneRules(long)
meth public com.ibm.icu.util.TimeZoneTransition getNextTransition(long,boolean)
meth public com.ibm.icu.util.TimeZoneTransition getPreviousTransition(long,boolean)
meth public int getOffset(int,int,int,int,int,int)
meth public int getRawOffset()
meth public java.lang.Object clone()
meth public java.lang.String getTZURL()
meth public java.util.Date getLastModified()
meth public static com.ibm.icu.util.VTimeZone create(java.io.Reader)
meth public static com.ibm.icu.util.VTimeZone create(java.lang.String)
meth public void getOffset(long,boolean,int[])
meth public void getOffsetFromLocal(long,int,int,int[])
meth public void setLastModified(java.util.Date)
meth public void setRawOffset(int)
meth public void setTZURL(java.lang.String)
meth public void write(java.io.Writer) throws java.io.IOException
meth public void write(java.io.Writer,long) throws java.io.IOException
meth public void writeSimple(java.io.Writer,long) throws java.io.IOException
supr com.ibm.icu.util.BasicTimeZone
hfds COLON,COMMA,DEF_DSTSAVINGS,DEF_TZSTARTTIME,EQUALS_SIGN,ERR,ICAL_BEGIN,ICAL_BEGIN_VTIMEZONE,ICAL_BYDAY,ICAL_BYMONTH,ICAL_BYMONTHDAY,ICAL_DAYLIGHT,ICAL_DOW_NAMES,ICAL_DTSTART,ICAL_END,ICAL_END_VTIMEZONE,ICAL_FREQ,ICAL_LASTMOD,ICAL_RDATE,ICAL_RRULE,ICAL_STANDARD,ICAL_TZID,ICAL_TZNAME,ICAL_TZOFFSETFROM,ICAL_TZOFFSETTO,ICAL_TZURL,ICAL_UNTIL,ICAL_VTIMEZONE,ICAL_YEARLY,ICU_TZINFO_PROP,ICU_TZVERSION,INI,MAX_TIME,MIN_TIME,MONTHLENGTH,NEWLINE,SEMICOLON,TZI,VTZ,lastmod,olsonzid,serialVersionUID,tz,tzurl,vtzlines

CLSS public abstract interface com.ibm.icu.util.ValueIterator
innr public final static Element
meth public abstract boolean next(com.ibm.icu.util.ValueIterator$Element)
meth public abstract void reset()
meth public abstract void setRange(int,int)

CLSS public final static com.ibm.icu.util.ValueIterator$Element
 outer com.ibm.icu.util.ValueIterator
cons public init()
fld public int integer
fld public java.lang.Object value
supr java.lang.Object

CLSS public final com.ibm.icu.util.VersionInfo
fld public final static com.ibm.icu.util.VersionInfo ICU_DATA_VERSION
fld public final static com.ibm.icu.util.VersionInfo ICU_VERSION
fld public final static com.ibm.icu.util.VersionInfo UCOL_BUILDER_VERSION
fld public final static com.ibm.icu.util.VersionInfo UCOL_RUNTIME_VERSION
fld public final static com.ibm.icu.util.VersionInfo UCOL_TAILORINGS_VERSION
fld public final static com.ibm.icu.util.VersionInfo UNICODE_1_0
fld public final static com.ibm.icu.util.VersionInfo UNICODE_1_0_1
fld public final static com.ibm.icu.util.VersionInfo UNICODE_1_1_0
fld public final static com.ibm.icu.util.VersionInfo UNICODE_1_1_5
fld public final static com.ibm.icu.util.VersionInfo UNICODE_2_0
fld public final static com.ibm.icu.util.VersionInfo UNICODE_2_1_2
fld public final static com.ibm.icu.util.VersionInfo UNICODE_2_1_5
fld public final static com.ibm.icu.util.VersionInfo UNICODE_2_1_8
fld public final static com.ibm.icu.util.VersionInfo UNICODE_2_1_9
fld public final static com.ibm.icu.util.VersionInfo UNICODE_3_0
fld public final static com.ibm.icu.util.VersionInfo UNICODE_3_0_1
fld public final static com.ibm.icu.util.VersionInfo UNICODE_3_1_0
fld public final static com.ibm.icu.util.VersionInfo UNICODE_3_1_1
fld public final static com.ibm.icu.util.VersionInfo UNICODE_3_2
fld public final static com.ibm.icu.util.VersionInfo UNICODE_4_0
fld public final static com.ibm.icu.util.VersionInfo UNICODE_4_0_1
fld public final static com.ibm.icu.util.VersionInfo UNICODE_4_1
fld public final static com.ibm.icu.util.VersionInfo UNICODE_5_0
fld public final static com.ibm.icu.util.VersionInfo UNICODE_5_1
fld public final static com.ibm.icu.util.VersionInfo UNICODE_5_2
fld public final static java.lang.String ICU_DATA_VERSION_PATH = "44b"
intf java.lang.Comparable<com.ibm.icu.util.VersionInfo>
meth public boolean equals(java.lang.Object)
meth public int compareTo(com.ibm.icu.util.VersionInfo)
meth public int getMajor()
meth public int getMicro()
meth public int getMilli()
meth public int getMinor()
meth public java.lang.String toString()
meth public static com.ibm.icu.util.VersionInfo getInstance(int)
meth public static com.ibm.icu.util.VersionInfo getInstance(int,int)
meth public static com.ibm.icu.util.VersionInfo getInstance(int,int,int)
meth public static com.ibm.icu.util.VersionInfo getInstance(int,int,int,int)
meth public static com.ibm.icu.util.VersionInfo getInstance(java.lang.String)
meth public static com.ibm.icu.util.VersionInfo javaVersion()
supr java.lang.Object
hfds INVALID_VERSION_NUMBER_,LAST_BYTE_MASK_,MAP_,javaVersion,m_version_

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract java.io.InputStream
cons public init()
intf java.io.Closeable
meth public abstract int read() throws java.io.IOException
meth public boolean markSupported()
meth public int available() throws java.io.IOException
meth public int read(byte[]) throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr java.lang.Object

CLSS public abstract java.io.Reader
cons protected init()
cons protected init(java.lang.Object)
fld protected java.lang.Object lock
intf java.io.Closeable
intf java.lang.Readable
meth public abstract int read(char[],int,int) throws java.io.IOException
meth public abstract void close() throws java.io.IOException
meth public boolean markSupported()
meth public boolean ready() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(char[]) throws java.io.IOException
meth public int read(java.nio.CharBuffer) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void mark(int) throws java.io.IOException
meth public void reset() throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Appendable
meth public abstract java.lang.Appendable append(char) throws java.io.IOException
meth public abstract java.lang.Appendable append(java.lang.CharSequence) throws java.io.IOException
meth public abstract java.lang.Appendable append(java.lang.CharSequence,int,int) throws java.io.IOException

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public final java.lang.Character
cons public init(char)
fld public final static byte COMBINING_SPACING_MARK = 8
fld public final static byte CONNECTOR_PUNCTUATION = 23
fld public final static byte CONTROL = 15
fld public final static byte CURRENCY_SYMBOL = 26
fld public final static byte DASH_PUNCTUATION = 20
fld public final static byte DECIMAL_DIGIT_NUMBER = 9
fld public final static byte DIRECTIONALITY_ARABIC_NUMBER = 6
fld public final static byte DIRECTIONALITY_BOUNDARY_NEUTRAL = 9
fld public final static byte DIRECTIONALITY_COMMON_NUMBER_SEPARATOR = 7
fld public final static byte DIRECTIONALITY_EUROPEAN_NUMBER = 3
fld public final static byte DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR = 4
fld public final static byte DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR = 5
fld public final static byte DIRECTIONALITY_LEFT_TO_RIGHT = 0
fld public final static byte DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING = 14
fld public final static byte DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE = 15
fld public final static byte DIRECTIONALITY_NONSPACING_MARK = 8
fld public final static byte DIRECTIONALITY_OTHER_NEUTRALS = 13
fld public final static byte DIRECTIONALITY_PARAGRAPH_SEPARATOR = 10
fld public final static byte DIRECTIONALITY_POP_DIRECTIONAL_FORMAT = 18
fld public final static byte DIRECTIONALITY_RIGHT_TO_LEFT = 1
fld public final static byte DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC = 2
fld public final static byte DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING = 16
fld public final static byte DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE = 17
fld public final static byte DIRECTIONALITY_SEGMENT_SEPARATOR = 11
fld public final static byte DIRECTIONALITY_UNDEFINED = -1
fld public final static byte DIRECTIONALITY_WHITESPACE = 12
fld public final static byte ENCLOSING_MARK = 7
fld public final static byte END_PUNCTUATION = 22
fld public final static byte FINAL_QUOTE_PUNCTUATION = 30
fld public final static byte FORMAT = 16
fld public final static byte INITIAL_QUOTE_PUNCTUATION = 29
fld public final static byte LETTER_NUMBER = 10
fld public final static byte LINE_SEPARATOR = 13
fld public final static byte LOWERCASE_LETTER = 2
fld public final static byte MATH_SYMBOL = 25
fld public final static byte MODIFIER_LETTER = 4
fld public final static byte MODIFIER_SYMBOL = 27
fld public final static byte NON_SPACING_MARK = 6
fld public final static byte OTHER_LETTER = 5
fld public final static byte OTHER_NUMBER = 11
fld public final static byte OTHER_PUNCTUATION = 24
fld public final static byte OTHER_SYMBOL = 28
fld public final static byte PARAGRAPH_SEPARATOR = 14
fld public final static byte PRIVATE_USE = 18
fld public final static byte SPACE_SEPARATOR = 12
fld public final static byte START_PUNCTUATION = 21
fld public final static byte SURROGATE = 19
fld public final static byte TITLECASE_LETTER = 3
fld public final static byte UNASSIGNED = 0
fld public final static byte UPPERCASE_LETTER = 1
fld public final static char MAX_HIGH_SURROGATE = '\udbff'
fld public final static char MAX_LOW_SURROGATE = '\udfff'
fld public final static char MAX_SURROGATE = '\udfff'
fld public final static char MAX_VALUE = '\uffff'
fld public final static char MIN_HIGH_SURROGATE = '\ud800'
fld public final static char MIN_LOW_SURROGATE = '\udc00'
fld public final static char MIN_SURROGATE = '\ud800'
fld public final static char MIN_VALUE = '\u0000'
fld public final static int BYTES = 2
fld public final static int MAX_CODE_POINT = 1114111
fld public final static int MAX_RADIX = 36
fld public final static int MIN_CODE_POINT = 0
fld public final static int MIN_RADIX = 2
fld public final static int MIN_SUPPLEMENTARY_CODE_POINT = 65536
fld public final static int SIZE = 16
fld public final static java.lang.Class<java.lang.Character> TYPE
innr public final static !enum UnicodeScript
innr public final static UnicodeBlock
innr public static Subset
intf java.io.Serializable
intf java.lang.Comparable<java.lang.Character>
meth public boolean equals(java.lang.Object)
meth public char charValue()
meth public int compareTo(java.lang.Character)
meth public int hashCode()
meth public java.lang.String toString()
meth public static boolean isAlphabetic(int)
meth public static boolean isBmpCodePoint(int)
meth public static boolean isDefined(char)
meth public static boolean isDefined(int)
meth public static boolean isDigit(char)
meth public static boolean isDigit(int)
meth public static boolean isHighSurrogate(char)
meth public static boolean isISOControl(char)
meth public static boolean isISOControl(int)
meth public static boolean isIdentifierIgnorable(char)
meth public static boolean isIdentifierIgnorable(int)
meth public static boolean isIdeographic(int)
meth public static boolean isJavaIdentifierPart(char)
meth public static boolean isJavaIdentifierPart(int)
meth public static boolean isJavaIdentifierStart(char)
meth public static boolean isJavaIdentifierStart(int)
meth public static boolean isJavaLetter(char)
 anno 0 java.lang.Deprecated()
meth public static boolean isJavaLetterOrDigit(char)
 anno 0 java.lang.Deprecated()
meth public static boolean isLetter(char)
meth public static boolean isLetter(int)
meth public static boolean isLetterOrDigit(char)
meth public static boolean isLetterOrDigit(int)
meth public static boolean isLowSurrogate(char)
meth public static boolean isLowerCase(char)
meth public static boolean isLowerCase(int)
meth public static boolean isMirrored(char)
meth public static boolean isMirrored(int)
meth public static boolean isSpace(char)
 anno 0 java.lang.Deprecated()
meth public static boolean isSpaceChar(char)
meth public static boolean isSpaceChar(int)
meth public static boolean isSupplementaryCodePoint(int)
meth public static boolean isSurrogate(char)
meth public static boolean isSurrogatePair(char,char)
meth public static boolean isTitleCase(char)
meth public static boolean isTitleCase(int)
meth public static boolean isUnicodeIdentifierPart(char)
meth public static boolean isUnicodeIdentifierPart(int)
meth public static boolean isUnicodeIdentifierStart(char)
meth public static boolean isUnicodeIdentifierStart(int)
meth public static boolean isUpperCase(char)
meth public static boolean isUpperCase(int)
meth public static boolean isValidCodePoint(int)
meth public static boolean isWhitespace(char)
meth public static boolean isWhitespace(int)
meth public static byte getDirectionality(char)
meth public static byte getDirectionality(int)
meth public static char forDigit(int,int)
meth public static char highSurrogate(int)
meth public static char lowSurrogate(int)
meth public static char reverseBytes(char)
meth public static char toLowerCase(char)
meth public static char toTitleCase(char)
meth public static char toUpperCase(char)
meth public static char[] toChars(int)
meth public static int charCount(int)
meth public static int codePointAt(char[],int)
meth public static int codePointAt(char[],int,int)
meth public static int codePointAt(java.lang.CharSequence,int)
meth public static int codePointBefore(char[],int)
meth public static int codePointBefore(char[],int,int)
meth public static int codePointBefore(java.lang.CharSequence,int)
meth public static int codePointCount(char[],int,int)
meth public static int codePointCount(java.lang.CharSequence,int,int)
meth public static int compare(char,char)
meth public static int digit(char,int)
meth public static int digit(int,int)
meth public static int getNumericValue(char)
meth public static int getNumericValue(int)
meth public static int getType(char)
meth public static int getType(int)
meth public static int hashCode(char)
meth public static int offsetByCodePoints(char[],int,int,int,int)
meth public static int offsetByCodePoints(java.lang.CharSequence,int,int)
meth public static int toChars(int,char[],int)
meth public static int toCodePoint(char,char)
meth public static int toLowerCase(int)
meth public static int toTitleCase(int)
meth public static int toUpperCase(int)
meth public static java.lang.Character valueOf(char)
meth public static java.lang.String getName(int)
meth public static java.lang.String toString(char)
supr java.lang.Object

CLSS public static java.lang.Character$Subset
 outer java.lang.Character
cons protected init(java.lang.String)
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final java.lang.String toString()
supr java.lang.Object

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract java.lang.Enum<%0 extends java.lang.Enum<{java.lang.Enum%0}>>
cons protected init(java.lang.String,int)
intf java.io.Serializable
intf java.lang.Comparable<{java.lang.Enum%0}>
meth protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected final void finalize()
meth public final boolean equals(java.lang.Object)
meth public final int compareTo({java.lang.Enum%0})
meth public final int hashCode()
meth public final int ordinal()
meth public final java.lang.Class<{java.lang.Enum%0}> getDeclaringClass()
meth public final java.lang.String name()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Enum<{%%0}>> {%%0} valueOf(java.lang.Class<{%%0}>,java.lang.String)
supr java.lang.Object

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public java.lang.IllegalArgumentException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException

CLSS public abstract interface java.lang.Iterable<%0 extends java.lang.Object>
meth public abstract java.util.Iterator<{java.lang.Iterable%0}> iterator()
meth public java.util.Spliterator<{java.lang.Iterable%0}> spliterator()
meth public void forEach(java.util.function.Consumer<? super {java.lang.Iterable%0}>)

CLSS public abstract java.lang.Number
cons public init()
intf java.io.Serializable
meth public abstract double doubleValue()
meth public abstract float floatValue()
meth public abstract int intValue()
meth public abstract long longValue()
meth public byte byteValue()
meth public short shortValue()
supr java.lang.Object

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

CLSS public abstract interface java.lang.Readable
meth public abstract int read(java.nio.CharBuffer) throws java.io.IOException

CLSS public java.lang.RuntimeException
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public java.lang.Throwable
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
intf java.io.Serializable
meth public final java.lang.Throwable[] getSuppressed()
meth public final void addSuppressed(java.lang.Throwable)
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String getLocalizedMessage()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable fillInStackTrace()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
meth public void setStackTrace(java.lang.StackTraceElement[])
supr java.lang.Object

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

CLSS public abstract interface java.text.AttributedCharacterIterator
innr public static Attribute
intf java.text.CharacterIterator
meth public abstract int getRunLimit()
meth public abstract int getRunLimit(java.text.AttributedCharacterIterator$Attribute)
meth public abstract int getRunLimit(java.util.Set<? extends java.text.AttributedCharacterIterator$Attribute>)
meth public abstract int getRunStart()
meth public abstract int getRunStart(java.text.AttributedCharacterIterator$Attribute)
meth public abstract int getRunStart(java.util.Set<? extends java.text.AttributedCharacterIterator$Attribute>)
meth public abstract java.lang.Object getAttribute(java.text.AttributedCharacterIterator$Attribute)
meth public abstract java.util.Map<java.text.AttributedCharacterIterator$Attribute,java.lang.Object> getAttributes()
meth public abstract java.util.Set<java.text.AttributedCharacterIterator$Attribute> getAllAttributeKeys()

CLSS public static java.text.AttributedCharacterIterator$Attribute
 outer java.text.AttributedCharacterIterator
cons protected init(java.lang.String)
fld public final static java.text.AttributedCharacterIterator$Attribute INPUT_METHOD_SEGMENT
fld public final static java.text.AttributedCharacterIterator$Attribute LANGUAGE
fld public final static java.text.AttributedCharacterIterator$Attribute READING
intf java.io.Serializable
meth protected java.lang.Object readResolve() throws java.io.InvalidObjectException
meth protected java.lang.String getName()
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract interface java.text.CharacterIterator
fld public final static char DONE = '\uffff'
intf java.lang.Cloneable
meth public abstract char current()
meth public abstract char first()
meth public abstract char last()
meth public abstract char next()
meth public abstract char previous()
meth public abstract char setIndex(int)
meth public abstract int getBeginIndex()
meth public abstract int getEndIndex()
meth public abstract int getIndex()
meth public abstract java.lang.Object clone()

CLSS public abstract java.text.Format
cons protected init()
innr public static Field
intf java.io.Serializable
intf java.lang.Cloneable
meth public abstract java.lang.Object parseObject(java.lang.String,java.text.ParsePosition)
meth public abstract java.lang.StringBuffer format(java.lang.Object,java.lang.StringBuffer,java.text.FieldPosition)
meth public final java.lang.String format(java.lang.Object)
meth public java.lang.Object clone()
meth public java.lang.Object parseObject(java.lang.String) throws java.text.ParseException
meth public java.text.AttributedCharacterIterator formatToCharacterIterator(java.lang.Object)
supr java.lang.Object

CLSS public static java.text.Format$Field
 outer java.text.Format
cons protected init(java.lang.String)
supr java.text.AttributedCharacterIterator$Attribute

CLSS public java.text.ParseException
cons public init(java.lang.String,int)
meth public int getErrorOffset()
supr java.lang.Exception

CLSS public abstract interface java.util.Comparator<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public <%0 extends java.lang.Comparable<? super {%%0}>> java.util.Comparator<{java.util.Comparator%0}> thenComparing(java.util.function.Function<? super {java.util.Comparator%0},? extends {%%0}>)
meth public <%0 extends java.lang.Object> java.util.Comparator<{java.util.Comparator%0}> thenComparing(java.util.function.Function<? super {java.util.Comparator%0},? extends {%%0}>,java.util.Comparator<? super {%%0}>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract int compare({java.util.Comparator%0},{java.util.Comparator%0})
meth public java.util.Comparator<{java.util.Comparator%0}> reversed()
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparing(java.util.Comparator<? super {java.util.Comparator%0}>)
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparingDouble(java.util.function.ToDoubleFunction<? super {java.util.Comparator%0}>)
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparingInt(java.util.function.ToIntFunction<? super {java.util.Comparator%0}>)
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparingLong(java.util.function.ToLongFunction<? super {java.util.Comparator%0}>)
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> java.util.Comparator<{%%0}> naturalOrder()
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> java.util.Comparator<{%%0}> reverseOrder()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Comparable<? super {%%1}>> java.util.Comparator<{%%0}> comparing(java.util.function.Function<? super {%%0},? extends {%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Comparator<{%%0}> comparing(java.util.function.Function<? super {%%0},? extends {%%1}>,java.util.Comparator<? super {%%1}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> comparingDouble(java.util.function.ToDoubleFunction<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> comparingInt(java.util.function.ToIntFunction<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> comparingLong(java.util.function.ToLongFunction<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> nullsFirst(java.util.Comparator<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> nullsLast(java.util.Comparator<? super {%%0}>)

CLSS public abstract interface java.util.Enumeration<%0 extends java.lang.Object>
meth public abstract boolean hasMoreElements()
meth public abstract {java.util.Enumeration%0} nextElement()

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface java.util.Iterator<%0 extends java.lang.Object>
meth public abstract boolean hasNext()
meth public abstract {java.util.Iterator%0} next()
meth public void forEachRemaining(java.util.function.Consumer<? super {java.util.Iterator%0}>)
meth public void remove()

CLSS public abstract java.util.ListResourceBundle
cons public init()
meth protected abstract java.lang.Object[][] getContents()
meth protected java.util.Set<java.lang.String> handleKeySet()
meth public final java.lang.Object handleGetObject(java.lang.String)
meth public java.util.Enumeration<java.lang.String> getKeys()
supr java.util.ResourceBundle

CLSS public abstract java.util.ResourceBundle
cons public init()
fld protected java.util.ResourceBundle parent
innr public static Control
meth protected abstract java.lang.Object handleGetObject(java.lang.String)
meth protected java.util.Set<java.lang.String> handleKeySet()
meth protected void setParent(java.util.ResourceBundle)
meth public abstract java.util.Enumeration<java.lang.String> getKeys()
meth public boolean containsKey(java.lang.String)
meth public final java.lang.Object getObject(java.lang.String)
meth public final java.lang.String getString(java.lang.String)
meth public final java.lang.String[] getStringArray(java.lang.String)
meth public final static java.util.ResourceBundle getBundle(java.lang.String)
meth public final static java.util.ResourceBundle getBundle(java.lang.String,java.util.Locale)
meth public final static java.util.ResourceBundle getBundle(java.lang.String,java.util.Locale,java.util.ResourceBundle$Control)
meth public final static java.util.ResourceBundle getBundle(java.lang.String,java.util.ResourceBundle$Control)
meth public final static void clearCache()
meth public final static void clearCache(java.lang.ClassLoader)
meth public java.lang.String getBaseBundleName()
meth public java.util.Locale getLocale()
meth public java.util.Set<java.lang.String> keySet()
meth public static java.util.ResourceBundle getBundle(java.lang.String,java.util.Locale,java.lang.ClassLoader)
meth public static java.util.ResourceBundle getBundle(java.lang.String,java.util.Locale,java.lang.ClassLoader,java.util.ResourceBundle$Control)
supr java.lang.Object

CLSS public abstract java.util.TimeZone
cons public init()
fld public final static int LONG = 1
fld public final static int SHORT = 0
intf java.io.Serializable
intf java.lang.Cloneable
meth public abstract boolean inDaylightTime(java.util.Date)
meth public abstract boolean useDaylightTime()
meth public abstract int getOffset(int,int,int,int,int,int)
meth public abstract int getRawOffset()
meth public abstract void setRawOffset(int)
meth public boolean hasSameRules(java.util.TimeZone)
meth public boolean observesDaylightTime()
meth public final java.lang.String getDisplayName()
meth public final java.lang.String getDisplayName(boolean,int)
meth public final java.lang.String getDisplayName(java.util.Locale)
meth public int getDSTSavings()
meth public int getOffset(long)
meth public java.lang.Object clone()
meth public java.lang.String getDisplayName(boolean,int,java.util.Locale)
meth public java.lang.String getID()
meth public java.time.ZoneId toZoneId()
meth public static java.lang.String[] getAvailableIDs()
meth public static java.lang.String[] getAvailableIDs(int)
meth public static java.util.TimeZone getDefault()
meth public static java.util.TimeZone getTimeZone(java.lang.String)
meth public static java.util.TimeZone getTimeZone(java.time.ZoneId)
meth public static void setDefault(java.util.TimeZone)
meth public void setID(java.lang.String)
supr java.lang.Object

CLSS public java.util.logging.Logger
cons protected init(java.lang.String,java.lang.String)
fld public final static java.lang.String GLOBAL_LOGGER_NAME = "global"
fld public final static java.util.logging.Logger global
 anno 0 java.lang.Deprecated()
meth public !varargs void logrb(java.util.logging.Level,java.lang.String,java.lang.String,java.util.ResourceBundle,java.lang.String,java.lang.Object[])
meth public boolean getUseParentHandlers()
meth public boolean isLoggable(java.util.logging.Level)
meth public final static java.util.logging.Logger getGlobal()
meth public java.lang.String getName()
meth public java.lang.String getResourceBundleName()
meth public java.util.ResourceBundle getResourceBundle()
meth public java.util.logging.Filter getFilter()
meth public java.util.logging.Handler[] getHandlers()
meth public java.util.logging.Level getLevel()
meth public java.util.logging.Logger getParent()
meth public static java.util.logging.Logger getAnonymousLogger()
meth public static java.util.logging.Logger getAnonymousLogger(java.lang.String)
meth public static java.util.logging.Logger getLogger(java.lang.String)
meth public static java.util.logging.Logger getLogger(java.lang.String,java.lang.String)
meth public void addHandler(java.util.logging.Handler)
meth public void config(java.lang.String)
meth public void config(java.util.function.Supplier<java.lang.String>)
meth public void entering(java.lang.String,java.lang.String)
meth public void entering(java.lang.String,java.lang.String,java.lang.Object)
meth public void entering(java.lang.String,java.lang.String,java.lang.Object[])
meth public void exiting(java.lang.String,java.lang.String)
meth public void exiting(java.lang.String,java.lang.String,java.lang.Object)
meth public void fine(java.lang.String)
meth public void fine(java.util.function.Supplier<java.lang.String>)
meth public void finer(java.lang.String)
meth public void finer(java.util.function.Supplier<java.lang.String>)
meth public void finest(java.lang.String)
meth public void finest(java.util.function.Supplier<java.lang.String>)
meth public void info(java.lang.String)
meth public void info(java.util.function.Supplier<java.lang.String>)
meth public void log(java.util.logging.Level,java.lang.String)
meth public void log(java.util.logging.Level,java.lang.String,java.lang.Object)
meth public void log(java.util.logging.Level,java.lang.String,java.lang.Object[])
meth public void log(java.util.logging.Level,java.lang.String,java.lang.Throwable)
meth public void log(java.util.logging.Level,java.lang.Throwable,java.util.function.Supplier<java.lang.String>)
meth public void log(java.util.logging.Level,java.util.function.Supplier<java.lang.String>)
meth public void log(java.util.logging.LogRecord)
meth public void logp(java.util.logging.Level,java.lang.String,java.lang.String,java.lang.String)
meth public void logp(java.util.logging.Level,java.lang.String,java.lang.String,java.lang.String,java.lang.Object)
meth public void logp(java.util.logging.Level,java.lang.String,java.lang.String,java.lang.String,java.lang.Object[])
meth public void logp(java.util.logging.Level,java.lang.String,java.lang.String,java.lang.String,java.lang.Throwable)
meth public void logp(java.util.logging.Level,java.lang.String,java.lang.String,java.lang.Throwable,java.util.function.Supplier<java.lang.String>)
meth public void logp(java.util.logging.Level,java.lang.String,java.lang.String,java.util.function.Supplier<java.lang.String>)
meth public void logrb(java.util.logging.Level,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void logrb(java.util.logging.Level,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public void logrb(java.util.logging.Level,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Object[])
 anno 0 java.lang.Deprecated()
meth public void logrb(java.util.logging.Level,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Throwable)
 anno 0 java.lang.Deprecated()
meth public void logrb(java.util.logging.Level,java.lang.String,java.lang.String,java.util.ResourceBundle,java.lang.String,java.lang.Throwable)
meth public void removeHandler(java.util.logging.Handler)
meth public void setFilter(java.util.logging.Filter)
meth public void setLevel(java.util.logging.Level)
meth public void setParent(java.util.logging.Logger)
meth public void setResourceBundle(java.util.ResourceBundle)
meth public void setUseParentHandlers(boolean)
meth public void severe(java.lang.String)
meth public void severe(java.util.function.Supplier<java.lang.String>)
meth public void throwing(java.lang.String,java.lang.String,java.lang.Throwable)
meth public void warning(java.lang.String)
meth public void warning(java.util.function.Supplier<java.lang.String>)
supr java.lang.Object

CLSS public abstract javax.xml.parsers.DocumentBuilder
cons protected init()
meth public abstract boolean isNamespaceAware()
meth public abstract boolean isValidating()
meth public abstract org.w3c.dom.DOMImplementation getDOMImplementation()
meth public abstract org.w3c.dom.Document newDocument()
meth public abstract org.w3c.dom.Document parse(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public abstract void setEntityResolver(org.xml.sax.EntityResolver)
meth public abstract void setErrorHandler(org.xml.sax.ErrorHandler)
meth public boolean isXIncludeAware()
meth public javax.xml.validation.Schema getSchema()
meth public org.w3c.dom.Document parse(java.io.File) throws java.io.IOException,org.xml.sax.SAXException
meth public org.w3c.dom.Document parse(java.io.InputStream) throws java.io.IOException,org.xml.sax.SAXException
meth public org.w3c.dom.Document parse(java.io.InputStream,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public org.w3c.dom.Document parse(java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void reset()
supr java.lang.Object

CLSS public abstract interface !annotation nu.validator.htmlparser.annotation.Auto
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation nu.validator.htmlparser.annotation.CharacterName
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation nu.validator.htmlparser.annotation.Const
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation nu.validator.htmlparser.annotation.Creator
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation nu.validator.htmlparser.annotation.HtmlCreator
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation nu.validator.htmlparser.annotation.IdType
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation nu.validator.htmlparser.annotation.Inline
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation nu.validator.htmlparser.annotation.Literal
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation nu.validator.htmlparser.annotation.Local
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation nu.validator.htmlparser.annotation.NoLength
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation nu.validator.htmlparser.annotation.NsUri
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation nu.validator.htmlparser.annotation.Prefix
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation nu.validator.htmlparser.annotation.QName
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation nu.validator.htmlparser.annotation.SvgCreator
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation nu.validator.htmlparser.annotation.Unsigned
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation nu.validator.htmlparser.annotation.Virtual
intf java.lang.annotation.Annotation

CLSS public abstract interface nu.validator.htmlparser.common.ByteReadable
meth public abstract int readByte() throws java.io.IOException

CLSS public abstract interface nu.validator.htmlparser.common.CharacterHandler
meth public abstract void characters(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void end() throws org.xml.sax.SAXException
meth public abstract void start() throws org.xml.sax.SAXException

CLSS public final !enum nu.validator.htmlparser.common.DoctypeExpectation
fld public final static nu.validator.htmlparser.common.DoctypeExpectation AUTO
fld public final static nu.validator.htmlparser.common.DoctypeExpectation HTML
fld public final static nu.validator.htmlparser.common.DoctypeExpectation HTML401_STRICT
fld public final static nu.validator.htmlparser.common.DoctypeExpectation HTML401_TRANSITIONAL
fld public final static nu.validator.htmlparser.common.DoctypeExpectation NO_DOCTYPE_ERRORS
meth public static nu.validator.htmlparser.common.DoctypeExpectation valueOf(java.lang.String)
meth public static nu.validator.htmlparser.common.DoctypeExpectation[] values()
supr java.lang.Enum<nu.validator.htmlparser.common.DoctypeExpectation>

CLSS public final !enum nu.validator.htmlparser.common.DocumentMode
fld public final static nu.validator.htmlparser.common.DocumentMode ALMOST_STANDARDS_MODE
fld public final static nu.validator.htmlparser.common.DocumentMode QUIRKS_MODE
fld public final static nu.validator.htmlparser.common.DocumentMode STANDARDS_MODE
meth public static nu.validator.htmlparser.common.DocumentMode valueOf(java.lang.String)
meth public static nu.validator.htmlparser.common.DocumentMode[] values()
supr java.lang.Enum<nu.validator.htmlparser.common.DocumentMode>

CLSS public abstract interface nu.validator.htmlparser.common.DocumentModeHandler
meth public abstract void documentMode(nu.validator.htmlparser.common.DocumentMode,java.lang.String,java.lang.String,boolean) throws org.xml.sax.SAXException

CLSS public abstract interface nu.validator.htmlparser.common.EncodingDeclarationHandler
meth public abstract boolean internalEncodingDeclaration(java.lang.String) throws org.xml.sax.SAXException
meth public abstract java.lang.String getCharacterEncoding() throws org.xml.sax.SAXException

CLSS public final !enum nu.validator.htmlparser.common.Heuristics
fld public final static nu.validator.htmlparser.common.Heuristics ALL
fld public final static nu.validator.htmlparser.common.Heuristics CHARDET
fld public final static nu.validator.htmlparser.common.Heuristics ICU
fld public final static nu.validator.htmlparser.common.Heuristics NONE
meth public static nu.validator.htmlparser.common.Heuristics valueOf(java.lang.String)
meth public static nu.validator.htmlparser.common.Heuristics[] values()
supr java.lang.Enum<nu.validator.htmlparser.common.Heuristics>

CLSS public abstract interface nu.validator.htmlparser.common.Interner

CLSS public abstract interface nu.validator.htmlparser.common.TokenHandler
meth public abstract boolean cdataSectionAllowed() throws org.xml.sax.SAXException
meth public abstract boolean wantsComments() throws org.xml.sax.SAXException
meth public abstract void characters(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void comment(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void doctype(java.lang.String,java.lang.String,java.lang.String,boolean) throws org.xml.sax.SAXException
meth public abstract void endTag(nu.validator.htmlparser.impl.ElementName) throws org.xml.sax.SAXException
meth public abstract void endTokenization() throws org.xml.sax.SAXException
meth public abstract void ensureBufferSpace(int) throws org.xml.sax.SAXException
meth public abstract void eof() throws org.xml.sax.SAXException
meth public abstract void startTag(nu.validator.htmlparser.impl.ElementName,nu.validator.htmlparser.impl.HtmlAttributes,boolean) throws org.xml.sax.SAXException
meth public abstract void startTokenization(nu.validator.htmlparser.impl.Tokenizer) throws org.xml.sax.SAXException
meth public abstract void zeroOriginatingReplacementCharacter() throws org.xml.sax.SAXException

CLSS public abstract interface nu.validator.htmlparser.common.TransitionHandler
meth public abstract void transition(int,int,boolean,int) throws org.xml.sax.SAXException

CLSS public final !enum nu.validator.htmlparser.common.XmlViolationPolicy
fld public final static nu.validator.htmlparser.common.XmlViolationPolicy ALLOW
fld public final static nu.validator.htmlparser.common.XmlViolationPolicy ALTER_INFOSET
fld public final static nu.validator.htmlparser.common.XmlViolationPolicy FATAL
meth public static nu.validator.htmlparser.common.XmlViolationPolicy valueOf(java.lang.String)
meth public static nu.validator.htmlparser.common.XmlViolationPolicy[] values()
supr java.lang.Enum<nu.validator.htmlparser.common.XmlViolationPolicy>

CLSS public nu.validator.htmlparser.dom.Dom2Sax
cons public init(org.xml.sax.ContentHandler,org.xml.sax.ext.LexicalHandler)
meth public void parse(org.w3c.dom.Node) throws org.xml.sax.SAXException
supr java.lang.Object
hfds attributes,contentHandler,lexicalHandler
hcls NamedNodeMapAttributes

CLSS public nu.validator.htmlparser.dom.HtmlDocumentBuilder
cons public init()
cons public init(nu.validator.htmlparser.common.XmlViolationPolicy)
cons public init(org.w3c.dom.DOMImplementation)
cons public init(org.w3c.dom.DOMImplementation,nu.validator.htmlparser.common.XmlViolationPolicy)
meth public boolean isCheckingNormalization()
meth public boolean isHtml4ModeCompatibleWithXhtml1Schemata()
meth public boolean isMappingLangToXmlLang()
meth public boolean isNamespaceAware()
meth public boolean isReportingDoctype()
meth public boolean isScriptingEnabled()
meth public boolean isValidating()
meth public nu.validator.htmlparser.common.DoctypeExpectation getDoctypeExpectation()
meth public nu.validator.htmlparser.common.DocumentModeHandler getDocumentModeHandler()
meth public nu.validator.htmlparser.common.Heuristics getHeuristics()
meth public nu.validator.htmlparser.common.XmlViolationPolicy getBogusXmlnsPolicy()
meth public nu.validator.htmlparser.common.XmlViolationPolicy getCommentPolicy()
meth public nu.validator.htmlparser.common.XmlViolationPolicy getContentNonXmlCharPolicy()
meth public nu.validator.htmlparser.common.XmlViolationPolicy getContentSpacePolicy()
meth public nu.validator.htmlparser.common.XmlViolationPolicy getNamePolicy()
meth public nu.validator.htmlparser.common.XmlViolationPolicy getStreamabilityViolationPolicy()
meth public nu.validator.htmlparser.common.XmlViolationPolicy getXmlnsPolicy()
meth public org.w3c.dom.DOMImplementation getDOMImplementation()
meth public org.w3c.dom.Document newDocument()
meth public org.w3c.dom.Document parse(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public org.w3c.dom.DocumentFragment parseFragment(org.xml.sax.InputSource,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public org.w3c.dom.DocumentFragment parseFragment(org.xml.sax.InputSource,java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public org.xml.sax.Locator getDocumentLocator()
meth public void addCharacterHandler(nu.validator.htmlparser.common.CharacterHandler)
meth public void setBogusXmlnsPolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setCheckingNormalization(boolean)
meth public void setCommentPolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setContentNonXmlCharPolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setContentSpacePolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setDoctypeExpectation(nu.validator.htmlparser.common.DoctypeExpectation)
meth public void setDocumentModeHandler(nu.validator.htmlparser.common.DocumentModeHandler)
meth public void setEntityResolver(org.xml.sax.EntityResolver)
meth public void setErrorHandler(org.xml.sax.ErrorHandler)
meth public void setHeuristics(nu.validator.htmlparser.common.Heuristics)
meth public void setHtml4ModeCompatibleWithXhtml1Schemata(boolean)
meth public void setIgnoringComments(boolean)
meth public void setMappingLangToXmlLang(boolean)
meth public void setNamePolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setReportingDoctype(boolean)
meth public void setScriptingEnabled(boolean)
meth public void setStreamabilityViolationPolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setTransitionHander(nu.validator.htmlparser.common.TransitionHandler)
meth public void setXmlPolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setXmlnsPolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
supr javax.xml.parsers.DocumentBuilder
hfds characterHandlers,checkingNormalization,commentPolicy,contentNonXmlCharPolicy,contentSpacePolicy,doctypeExpectation,documentModeHandler,driver,entityResolver,errorHandler,heuristics,html4ModeCompatibleWithXhtml1Schemata,implementation,mappingLangToXmlLang,namePolicy,reportingDoctype,scriptingEnabled,streamabilityViolationPolicy,transitionHandler,treeBuilder,treeBuilderErrorHandler,xmlnsPolicy

CLSS public nu.validator.htmlparser.extra.ChardetSniffer
hfds length,returnValue,source

CLSS public nu.validator.htmlparser.extra.IcuDetectorSniffer
cons public init(nu.validator.htmlparser.common.ByteReadable)
meth public int read() throws java.io.IOException
meth public nu.validator.htmlparser.io.Encoding sniff() throws java.io.IOException
meth public static void main(java.lang.String[])
supr java.io.InputStream
hfds source

CLSS public final nu.validator.htmlparser.extra.NormalizationChecker
cons public init(org.xml.sax.Locator)
intf nu.validator.htmlparser.common.CharacterHandler
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void end() throws org.xml.sax.SAXException
meth public void err(java.lang.String) throws org.xml.sax.SAXException
meth public void setErrorHandler(org.xml.sax.ErrorHandler)
meth public void start()
supr java.lang.Object
hfds COMPOSING_CHARACTERS,alreadyComplainedAboutThisRun,atStartOfRun,buf,bufHolder,errorHandler,locator,pos

CLSS public final nu.validator.htmlparser.impl.AttributeName
fld public final static int BOOLEAN = 64
fld public final static int CASE_FOLDED = 32
fld public final static int HTML = 0
fld public final static int HTML_LANG = 3
fld public final static int IS_XMLNS = 16
fld public final static int MATHML = 1
fld public final static int NCNAME_FOREIGN = 6
fld public final static int NCNAME_HTML = 1
fld public final static int NCNAME_LANG = 8
fld public final static int SVG = 2
fld public final static nu.validator.htmlparser.impl.AttributeName ABBR
fld public final static nu.validator.htmlparser.impl.AttributeName ACCENT
fld public final static nu.validator.htmlparser.impl.AttributeName ACCENTUNDER
fld public final static nu.validator.htmlparser.impl.AttributeName ACCEPT
fld public final static nu.validator.htmlparser.impl.AttributeName ACCEPT_CHARSET
fld public final static nu.validator.htmlparser.impl.AttributeName ACCESSKEY
fld public final static nu.validator.htmlparser.impl.AttributeName ACCUMULATE
fld public final static nu.validator.htmlparser.impl.AttributeName ACTION
fld public final static nu.validator.htmlparser.impl.AttributeName ACTIONTYPE
fld public final static nu.validator.htmlparser.impl.AttributeName ACTIVE
fld public final static nu.validator.htmlparser.impl.AttributeName ADDITIVE
fld public final static nu.validator.htmlparser.impl.AttributeName ALIGN
fld public final static nu.validator.htmlparser.impl.AttributeName ALIGNMENTSCOPE
fld public final static nu.validator.htmlparser.impl.AttributeName ALIGNMENT_BASELINE
fld public final static nu.validator.htmlparser.impl.AttributeName ALINK
fld public final static nu.validator.htmlparser.impl.AttributeName ALT
fld public final static nu.validator.htmlparser.impl.AttributeName ALTIMG
fld public final static nu.validator.htmlparser.impl.AttributeName ALTTEXT
fld public final static nu.validator.htmlparser.impl.AttributeName AMPLITUDE
fld public final static nu.validator.htmlparser.impl.AttributeName ARCHIVE
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_ACTIVEDESCENDANT
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_ATOMIC
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_AUTOCOMPLETE
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_BUSY
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_CHANNEL
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_CHECKED
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_CONTROLS
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_DATATYPE
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_DESCRIBEDBY
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_DISABLED
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_DROPEFFECT
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_EXPANDED
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_FLOWTO
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_GRAB
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_HASPOPUP
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_HIDDEN
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_INVALID
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_LABELLEDBY
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_LEVEL
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_LIVE
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_MULTILINE
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_MULTISELECTABLE
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_OWNS
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_POSINSET
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_PRESSED
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_READONLY
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_RELEVANT
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_REQUIRED
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_SECRET
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_SELECTED
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_SETSIZE
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_SORT
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_TEMPLATEID
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_VALUEMAX
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_VALUEMIN
fld public final static nu.validator.htmlparser.impl.AttributeName ARIA_VALUENOW
fld public final static nu.validator.htmlparser.impl.AttributeName ASYNC
fld public final static nu.validator.htmlparser.impl.AttributeName ATTRIBUTENAME
fld public final static nu.validator.htmlparser.impl.AttributeName ATTRIBUTETYPE
fld public final static nu.validator.htmlparser.impl.AttributeName AUTOCOMPLETE
fld public final static nu.validator.htmlparser.impl.AttributeName AUTOFOCUS
fld public final static nu.validator.htmlparser.impl.AttributeName AUTOPLAY
fld public final static nu.validator.htmlparser.impl.AttributeName AXIS
fld public final static nu.validator.htmlparser.impl.AttributeName AZIMUTH
fld public final static nu.validator.htmlparser.impl.AttributeName BACKGROUND
fld public final static nu.validator.htmlparser.impl.AttributeName BASE
fld public final static nu.validator.htmlparser.impl.AttributeName BASEFREQUENCY
fld public final static nu.validator.htmlparser.impl.AttributeName BASELINE
fld public final static nu.validator.htmlparser.impl.AttributeName BASELINE_SHIFT
fld public final static nu.validator.htmlparser.impl.AttributeName BASEPROFILE
fld public final static nu.validator.htmlparser.impl.AttributeName BEGIN
fld public final static nu.validator.htmlparser.impl.AttributeName BEVELLED
fld public final static nu.validator.htmlparser.impl.AttributeName BGCOLOR
fld public final static nu.validator.htmlparser.impl.AttributeName BIAS
fld public final static nu.validator.htmlparser.impl.AttributeName BORDER
fld public final static nu.validator.htmlparser.impl.AttributeName BY
fld public final static nu.validator.htmlparser.impl.AttributeName CALCMODE
fld public final static nu.validator.htmlparser.impl.AttributeName CELLPADDING
fld public final static nu.validator.htmlparser.impl.AttributeName CELLSPACING
fld public final static nu.validator.htmlparser.impl.AttributeName CHAR
fld public final static nu.validator.htmlparser.impl.AttributeName CHAROFF
fld public final static nu.validator.htmlparser.impl.AttributeName CHARSET
fld public final static nu.validator.htmlparser.impl.AttributeName CHECKED
fld public final static nu.validator.htmlparser.impl.AttributeName CITE
fld public final static nu.validator.htmlparser.impl.AttributeName CLASS
fld public final static nu.validator.htmlparser.impl.AttributeName CLASSID
fld public final static nu.validator.htmlparser.impl.AttributeName CLEAR
fld public final static nu.validator.htmlparser.impl.AttributeName CLIP
fld public final static nu.validator.htmlparser.impl.AttributeName CLIPPATHUNITS
fld public final static nu.validator.htmlparser.impl.AttributeName CLIP_PATH
fld public final static nu.validator.htmlparser.impl.AttributeName CLIP_RULE
fld public final static nu.validator.htmlparser.impl.AttributeName CLOSE
fld public final static nu.validator.htmlparser.impl.AttributeName CLOSURE
fld public final static nu.validator.htmlparser.impl.AttributeName CODE
fld public final static nu.validator.htmlparser.impl.AttributeName CODEBASE
fld public final static nu.validator.htmlparser.impl.AttributeName CODETYPE
fld public final static nu.validator.htmlparser.impl.AttributeName COLOR
fld public final static nu.validator.htmlparser.impl.AttributeName COLOR_INTERPOLATION
fld public final static nu.validator.htmlparser.impl.AttributeName COLOR_INTERPOLATION_FILTERS
fld public final static nu.validator.htmlparser.impl.AttributeName COLOR_PROFILE
fld public final static nu.validator.htmlparser.impl.AttributeName COLOR_RENDERING
fld public final static nu.validator.htmlparser.impl.AttributeName COLS
fld public final static nu.validator.htmlparser.impl.AttributeName COLSPAN
fld public final static nu.validator.htmlparser.impl.AttributeName COLUMNALIGN
fld public final static nu.validator.htmlparser.impl.AttributeName COLUMNLINES
fld public final static nu.validator.htmlparser.impl.AttributeName COLUMNSPACING
fld public final static nu.validator.htmlparser.impl.AttributeName COLUMNSPAN
fld public final static nu.validator.htmlparser.impl.AttributeName COLUMNWIDTH
fld public final static nu.validator.htmlparser.impl.AttributeName COMPACT
fld public final static nu.validator.htmlparser.impl.AttributeName CONTENT
fld public final static nu.validator.htmlparser.impl.AttributeName CONTENTEDITABLE
fld public final static nu.validator.htmlparser.impl.AttributeName CONTEXTMENU
fld public final static nu.validator.htmlparser.impl.AttributeName CONTROLS
fld public final static nu.validator.htmlparser.impl.AttributeName COORDS
fld public final static nu.validator.htmlparser.impl.AttributeName CROSSORIGIN
fld public final static nu.validator.htmlparser.impl.AttributeName CURSOR
fld public final static nu.validator.htmlparser.impl.AttributeName CX
fld public final static nu.validator.htmlparser.impl.AttributeName CY
fld public final static nu.validator.htmlparser.impl.AttributeName D
fld public final static nu.validator.htmlparser.impl.AttributeName DATA
fld public final static nu.validator.htmlparser.impl.AttributeName DATETIME
fld public final static nu.validator.htmlparser.impl.AttributeName DECLARE
fld public final static nu.validator.htmlparser.impl.AttributeName DEFAULT
fld public final static nu.validator.htmlparser.impl.AttributeName DEFER
fld public final static nu.validator.htmlparser.impl.AttributeName DEFINITIONURL
fld public final static nu.validator.htmlparser.impl.AttributeName DEPTH
fld public final static nu.validator.htmlparser.impl.AttributeName DIFFUSECONSTANT
fld public final static nu.validator.htmlparser.impl.AttributeName DIR
fld public final static nu.validator.htmlparser.impl.AttributeName DIRECTION
fld public final static nu.validator.htmlparser.impl.AttributeName DISABLED
fld public final static nu.validator.htmlparser.impl.AttributeName DISPLAY
fld public final static nu.validator.htmlparser.impl.AttributeName DISPLAYSTYLE
fld public final static nu.validator.htmlparser.impl.AttributeName DIVISOR
fld public final static nu.validator.htmlparser.impl.AttributeName DOMINANT_BASELINE
fld public final static nu.validator.htmlparser.impl.AttributeName DRAGGABLE
fld public final static nu.validator.htmlparser.impl.AttributeName DUR
fld public final static nu.validator.htmlparser.impl.AttributeName DX
fld public final static nu.validator.htmlparser.impl.AttributeName DY
fld public final static nu.validator.htmlparser.impl.AttributeName EDGE
fld public final static nu.validator.htmlparser.impl.AttributeName EDGEMODE
fld public final static nu.validator.htmlparser.impl.AttributeName ELEVATION
fld public final static nu.validator.htmlparser.impl.AttributeName ENABLE_BACKGROUND
fld public final static nu.validator.htmlparser.impl.AttributeName ENCODING
fld public final static nu.validator.htmlparser.impl.AttributeName ENCTYPE
fld public final static nu.validator.htmlparser.impl.AttributeName END
fld public final static nu.validator.htmlparser.impl.AttributeName EQUALCOLUMNS
fld public final static nu.validator.htmlparser.impl.AttributeName EQUALROWS
fld public final static nu.validator.htmlparser.impl.AttributeName EXPONENT
fld public final static nu.validator.htmlparser.impl.AttributeName FACE
fld public final static nu.validator.htmlparser.impl.AttributeName FENCE
fld public final static nu.validator.htmlparser.impl.AttributeName FILL
fld public final static nu.validator.htmlparser.impl.AttributeName FILL_OPACITY
fld public final static nu.validator.htmlparser.impl.AttributeName FILL_RULE
fld public final static nu.validator.htmlparser.impl.AttributeName FILTER
fld public final static nu.validator.htmlparser.impl.AttributeName FILTERUNITS
fld public final static nu.validator.htmlparser.impl.AttributeName FLOOD_COLOR
fld public final static nu.validator.htmlparser.impl.AttributeName FLOOD_OPACITY
fld public final static nu.validator.htmlparser.impl.AttributeName FONTFAMILY
fld public final static nu.validator.htmlparser.impl.AttributeName FONTSIZE
fld public final static nu.validator.htmlparser.impl.AttributeName FONTSTYLE
fld public final static nu.validator.htmlparser.impl.AttributeName FONTWEIGHT
fld public final static nu.validator.htmlparser.impl.AttributeName FONT_FAMILY
fld public final static nu.validator.htmlparser.impl.AttributeName FONT_SIZE
fld public final static nu.validator.htmlparser.impl.AttributeName FONT_SIZE_ADJUST
fld public final static nu.validator.htmlparser.impl.AttributeName FONT_STRETCH
fld public final static nu.validator.htmlparser.impl.AttributeName FONT_STYLE
fld public final static nu.validator.htmlparser.impl.AttributeName FONT_VARIANT
fld public final static nu.validator.htmlparser.impl.AttributeName FONT_WEIGHT
fld public final static nu.validator.htmlparser.impl.AttributeName FOR
fld public final static nu.validator.htmlparser.impl.AttributeName FORM
fld public final static nu.validator.htmlparser.impl.AttributeName FORMAT
fld public final static nu.validator.htmlparser.impl.AttributeName FRAME
fld public final static nu.validator.htmlparser.impl.AttributeName FRAMEBORDER
fld public final static nu.validator.htmlparser.impl.AttributeName FRAMESPACING
fld public final static nu.validator.htmlparser.impl.AttributeName FROM
fld public final static nu.validator.htmlparser.impl.AttributeName FX
fld public final static nu.validator.htmlparser.impl.AttributeName FY
fld public final static nu.validator.htmlparser.impl.AttributeName GLYPHREF
fld public final static nu.validator.htmlparser.impl.AttributeName GLYPH_ORIENTATION_HORIZONTAL
fld public final static nu.validator.htmlparser.impl.AttributeName GLYPH_ORIENTATION_VERTICAL
fld public final static nu.validator.htmlparser.impl.AttributeName GRADIENTTRANSFORM
fld public final static nu.validator.htmlparser.impl.AttributeName GRADIENTUNITS
fld public final static nu.validator.htmlparser.impl.AttributeName GROUPALIGN
fld public final static nu.validator.htmlparser.impl.AttributeName HEADERS
fld public final static nu.validator.htmlparser.impl.AttributeName HEIGHT
fld public final static nu.validator.htmlparser.impl.AttributeName HIDDEN
fld public final static nu.validator.htmlparser.impl.AttributeName HIGH
fld public final static nu.validator.htmlparser.impl.AttributeName HREF
fld public final static nu.validator.htmlparser.impl.AttributeName HREFLANG
fld public final static nu.validator.htmlparser.impl.AttributeName HSPACE
fld public final static nu.validator.htmlparser.impl.AttributeName HTTP_EQUIV
fld public final static nu.validator.htmlparser.impl.AttributeName ICON
fld public final static nu.validator.htmlparser.impl.AttributeName ID
fld public final static nu.validator.htmlparser.impl.AttributeName IMAGE_RENDERING
fld public final static nu.validator.htmlparser.impl.AttributeName IN
fld public final static nu.validator.htmlparser.impl.AttributeName IN2
fld public final static nu.validator.htmlparser.impl.AttributeName INDEX
fld public final static nu.validator.htmlparser.impl.AttributeName INPUTMODE
fld public final static nu.validator.htmlparser.impl.AttributeName INTEGRITY
fld public final static nu.validator.htmlparser.impl.AttributeName INTERCEPT
fld public final static nu.validator.htmlparser.impl.AttributeName IS
fld public final static nu.validator.htmlparser.impl.AttributeName ISMAP
fld public final static nu.validator.htmlparser.impl.AttributeName K1
fld public final static nu.validator.htmlparser.impl.AttributeName K2
fld public final static nu.validator.htmlparser.impl.AttributeName K3
fld public final static nu.validator.htmlparser.impl.AttributeName K4
fld public final static nu.validator.htmlparser.impl.AttributeName KERNELMATRIX
fld public final static nu.validator.htmlparser.impl.AttributeName KERNELUNITLENGTH
fld public final static nu.validator.htmlparser.impl.AttributeName KERNING
fld public final static nu.validator.htmlparser.impl.AttributeName KEYPOINTS
fld public final static nu.validator.htmlparser.impl.AttributeName KEYSPLINES
fld public final static nu.validator.htmlparser.impl.AttributeName KEYSYSTEM
fld public final static nu.validator.htmlparser.impl.AttributeName KEYTIMES
fld public final static nu.validator.htmlparser.impl.AttributeName LABEL
fld public final static nu.validator.htmlparser.impl.AttributeName LANG
fld public final static nu.validator.htmlparser.impl.AttributeName LANGUAGE
fld public final static nu.validator.htmlparser.impl.AttributeName LARGEOP
fld public final static nu.validator.htmlparser.impl.AttributeName LENGTHADJUST
fld public final static nu.validator.htmlparser.impl.AttributeName LETTER_SPACING
fld public final static nu.validator.htmlparser.impl.AttributeName LIGHTING_COLOR
fld public final static nu.validator.htmlparser.impl.AttributeName LIMITINGCONEANGLE
fld public final static nu.validator.htmlparser.impl.AttributeName LINEBREAK
fld public final static nu.validator.htmlparser.impl.AttributeName LINETHICKNESS
fld public final static nu.validator.htmlparser.impl.AttributeName LINK
fld public final static nu.validator.htmlparser.impl.AttributeName LIST
fld public final static nu.validator.htmlparser.impl.AttributeName LOCAL
fld public final static nu.validator.htmlparser.impl.AttributeName LONGDESC
fld public final static nu.validator.htmlparser.impl.AttributeName LOOP
fld public final static nu.validator.htmlparser.impl.AttributeName LOW
fld public final static nu.validator.htmlparser.impl.AttributeName LOWSRC
fld public final static nu.validator.htmlparser.impl.AttributeName LQUOTE
fld public final static nu.validator.htmlparser.impl.AttributeName LSPACE
fld public final static nu.validator.htmlparser.impl.AttributeName MANIFEST
fld public final static nu.validator.htmlparser.impl.AttributeName MARGINHEIGHT
fld public final static nu.validator.htmlparser.impl.AttributeName MARGINWIDTH
fld public final static nu.validator.htmlparser.impl.AttributeName MARKERHEIGHT
fld public final static nu.validator.htmlparser.impl.AttributeName MARKERUNITS
fld public final static nu.validator.htmlparser.impl.AttributeName MARKERWIDTH
fld public final static nu.validator.htmlparser.impl.AttributeName MARKER_END
fld public final static nu.validator.htmlparser.impl.AttributeName MARKER_MID
fld public final static nu.validator.htmlparser.impl.AttributeName MARKER_START
fld public final static nu.validator.htmlparser.impl.AttributeName MASK
fld public final static nu.validator.htmlparser.impl.AttributeName MASKCONTENTUNITS
fld public final static nu.validator.htmlparser.impl.AttributeName MASKUNITS
fld public final static nu.validator.htmlparser.impl.AttributeName MATHBACKGROUND
fld public final static nu.validator.htmlparser.impl.AttributeName MATHCOLOR
fld public final static nu.validator.htmlparser.impl.AttributeName MATHSIZE
fld public final static nu.validator.htmlparser.impl.AttributeName MATHVARIANT
fld public final static nu.validator.htmlparser.impl.AttributeName MAX
fld public final static nu.validator.htmlparser.impl.AttributeName MAXLENGTH
fld public final static nu.validator.htmlparser.impl.AttributeName MAXSIZE
fld public final static nu.validator.htmlparser.impl.AttributeName MEDIA
fld public final static nu.validator.htmlparser.impl.AttributeName METHOD
fld public final static nu.validator.htmlparser.impl.AttributeName MIN
fld public final static nu.validator.htmlparser.impl.AttributeName MINSIZE
fld public final static nu.validator.htmlparser.impl.AttributeName MODE
fld public final static nu.validator.htmlparser.impl.AttributeName MOVABLELIMITS
fld public final static nu.validator.htmlparser.impl.AttributeName MULTIPLE
fld public final static nu.validator.htmlparser.impl.AttributeName NAME
fld public final static nu.validator.htmlparser.impl.AttributeName NOHREF
fld public final static nu.validator.htmlparser.impl.AttributeName NOMODULE
fld public final static nu.validator.htmlparser.impl.AttributeName NORESIZE
fld public final static nu.validator.htmlparser.impl.AttributeName NOSHADE
fld public final static nu.validator.htmlparser.impl.AttributeName NOTATION
fld public final static nu.validator.htmlparser.impl.AttributeName NOWRAP
fld public final static nu.validator.htmlparser.impl.AttributeName NUMOCTAVES
fld public final static nu.validator.htmlparser.impl.AttributeName OBJECT
fld public final static nu.validator.htmlparser.impl.AttributeName OFFSET
fld public final static nu.validator.htmlparser.impl.AttributeName ONABORT
fld public final static nu.validator.htmlparser.impl.AttributeName ONACTIVATE
fld public final static nu.validator.htmlparser.impl.AttributeName ONAFTERPRINT
fld public final static nu.validator.htmlparser.impl.AttributeName ONBEFORECOPY
fld public final static nu.validator.htmlparser.impl.AttributeName ONBEFORECUT
fld public final static nu.validator.htmlparser.impl.AttributeName ONBEFOREPASTE
fld public final static nu.validator.htmlparser.impl.AttributeName ONBEFOREPRINT
fld public final static nu.validator.htmlparser.impl.AttributeName ONBEFOREUNLOAD
fld public final static nu.validator.htmlparser.impl.AttributeName ONBEGIN
fld public final static nu.validator.htmlparser.impl.AttributeName ONBLUR
fld public final static nu.validator.htmlparser.impl.AttributeName ONCHANGE
fld public final static nu.validator.htmlparser.impl.AttributeName ONCLICK
fld public final static nu.validator.htmlparser.impl.AttributeName ONCONTEXTMENU
fld public final static nu.validator.htmlparser.impl.AttributeName ONCOPY
fld public final static nu.validator.htmlparser.impl.AttributeName ONCUT
fld public final static nu.validator.htmlparser.impl.AttributeName ONDATAAVAILABLE
fld public final static nu.validator.htmlparser.impl.AttributeName ONDBLCLICK
fld public final static nu.validator.htmlparser.impl.AttributeName ONDRAG
fld public final static nu.validator.htmlparser.impl.AttributeName ONDRAGDROP
fld public final static nu.validator.htmlparser.impl.AttributeName ONDRAGEND
fld public final static nu.validator.htmlparser.impl.AttributeName ONDRAGENTER
fld public final static nu.validator.htmlparser.impl.AttributeName ONDRAGLEAVE
fld public final static nu.validator.htmlparser.impl.AttributeName ONDRAGOVER
fld public final static nu.validator.htmlparser.impl.AttributeName ONDRAGSTART
fld public final static nu.validator.htmlparser.impl.AttributeName ONDROP
fld public final static nu.validator.htmlparser.impl.AttributeName ONEND
fld public final static nu.validator.htmlparser.impl.AttributeName ONERROR
fld public final static nu.validator.htmlparser.impl.AttributeName ONFINISH
fld public final static nu.validator.htmlparser.impl.AttributeName ONFOCUS
fld public final static nu.validator.htmlparser.impl.AttributeName ONFOCUSIN
fld public final static nu.validator.htmlparser.impl.AttributeName ONFOCUSOUT
fld public final static nu.validator.htmlparser.impl.AttributeName ONINPUT
fld public final static nu.validator.htmlparser.impl.AttributeName ONINVALID
fld public final static nu.validator.htmlparser.impl.AttributeName ONKEYDOWN
fld public final static nu.validator.htmlparser.impl.AttributeName ONKEYPRESS
fld public final static nu.validator.htmlparser.impl.AttributeName ONKEYUP
fld public final static nu.validator.htmlparser.impl.AttributeName ONLOAD
fld public final static nu.validator.htmlparser.impl.AttributeName ONMESSAGE
fld public final static nu.validator.htmlparser.impl.AttributeName ONMOUSEDOWN
fld public final static nu.validator.htmlparser.impl.AttributeName ONMOUSEENTER
fld public final static nu.validator.htmlparser.impl.AttributeName ONMOUSELEAVE
fld public final static nu.validator.htmlparser.impl.AttributeName ONMOUSEMOVE
fld public final static nu.validator.htmlparser.impl.AttributeName ONMOUSEOUT
fld public final static nu.validator.htmlparser.impl.AttributeName ONMOUSEOVER
fld public final static nu.validator.htmlparser.impl.AttributeName ONMOUSEUP
fld public final static nu.validator.htmlparser.impl.AttributeName ONMOUSEWHEEL
fld public final static nu.validator.htmlparser.impl.AttributeName ONPASTE
fld public final static nu.validator.htmlparser.impl.AttributeName ONREADYSTATECHANGE
fld public final static nu.validator.htmlparser.impl.AttributeName ONREPEAT
fld public final static nu.validator.htmlparser.impl.AttributeName ONRESET
fld public final static nu.validator.htmlparser.impl.AttributeName ONRESIZE
fld public final static nu.validator.htmlparser.impl.AttributeName ONSCROLL
fld public final static nu.validator.htmlparser.impl.AttributeName ONSELECT
fld public final static nu.validator.htmlparser.impl.AttributeName ONSELECTSTART
fld public final static nu.validator.htmlparser.impl.AttributeName ONSTART
fld public final static nu.validator.htmlparser.impl.AttributeName ONSTOP
fld public final static nu.validator.htmlparser.impl.AttributeName ONSUBMIT
fld public final static nu.validator.htmlparser.impl.AttributeName ONUNLOAD
fld public final static nu.validator.htmlparser.impl.AttributeName ONZOOM
fld public final static nu.validator.htmlparser.impl.AttributeName OPACITY
fld public final static nu.validator.htmlparser.impl.AttributeName OPEN
fld public final static nu.validator.htmlparser.impl.AttributeName OPERATOR
fld public final static nu.validator.htmlparser.impl.AttributeName OPTIMUM
fld public final static nu.validator.htmlparser.impl.AttributeName ORDER
fld public final static nu.validator.htmlparser.impl.AttributeName ORIENT
fld public final static nu.validator.htmlparser.impl.AttributeName ORIENTATION
fld public final static nu.validator.htmlparser.impl.AttributeName ORIGIN
fld public final static nu.validator.htmlparser.impl.AttributeName OTHER
fld public final static nu.validator.htmlparser.impl.AttributeName OVERFLOW
fld public final static nu.validator.htmlparser.impl.AttributeName PATH
fld public final static nu.validator.htmlparser.impl.AttributeName PATHLENGTH
fld public final static nu.validator.htmlparser.impl.AttributeName PATTERN
fld public final static nu.validator.htmlparser.impl.AttributeName PATTERNCONTENTUNITS
fld public final static nu.validator.htmlparser.impl.AttributeName PATTERNTRANSFORM
fld public final static nu.validator.htmlparser.impl.AttributeName PATTERNUNITS
fld public final static nu.validator.htmlparser.impl.AttributeName PING
fld public final static nu.validator.htmlparser.impl.AttributeName POINTER_EVENTS
fld public final static nu.validator.htmlparser.impl.AttributeName POINTS
fld public final static nu.validator.htmlparser.impl.AttributeName POINTSATX
fld public final static nu.validator.htmlparser.impl.AttributeName POINTSATY
fld public final static nu.validator.htmlparser.impl.AttributeName POINTSATZ
fld public final static nu.validator.htmlparser.impl.AttributeName POSTER
fld public final static nu.validator.htmlparser.impl.AttributeName PRESERVEALPHA
fld public final static nu.validator.htmlparser.impl.AttributeName PRESERVEASPECTRATIO
fld public final static nu.validator.htmlparser.impl.AttributeName PRIMITIVEUNITS
fld public final static nu.validator.htmlparser.impl.AttributeName PROFILE
fld public final static nu.validator.htmlparser.impl.AttributeName PROMPT
fld public final static nu.validator.htmlparser.impl.AttributeName PROPERTY
fld public final static nu.validator.htmlparser.impl.AttributeName R
fld public final static nu.validator.htmlparser.impl.AttributeName RADIOGROUP
fld public final static nu.validator.htmlparser.impl.AttributeName RADIUS
fld public final static nu.validator.htmlparser.impl.AttributeName READONLY
fld public final static nu.validator.htmlparser.impl.AttributeName REFERRERPOLICY
fld public final static nu.validator.htmlparser.impl.AttributeName REFX
fld public final static nu.validator.htmlparser.impl.AttributeName REFY
fld public final static nu.validator.htmlparser.impl.AttributeName REL
fld public final static nu.validator.htmlparser.impl.AttributeName RENDERING_INTENT
fld public final static nu.validator.htmlparser.impl.AttributeName REPEAT
fld public final static nu.validator.htmlparser.impl.AttributeName REPEATCOUNT
fld public final static nu.validator.htmlparser.impl.AttributeName REPEATDUR
fld public final static nu.validator.htmlparser.impl.AttributeName REPLACE
fld public final static nu.validator.htmlparser.impl.AttributeName REQUIRED
fld public final static nu.validator.htmlparser.impl.AttributeName REQUIREDEXTENSIONS
fld public final static nu.validator.htmlparser.impl.AttributeName REQUIREDFEATURES
fld public final static nu.validator.htmlparser.impl.AttributeName RESTART
fld public final static nu.validator.htmlparser.impl.AttributeName RESULT
fld public final static nu.validator.htmlparser.impl.AttributeName REV
fld public final static nu.validator.htmlparser.impl.AttributeName ROLE
fld public final static nu.validator.htmlparser.impl.AttributeName ROTATE
fld public final static nu.validator.htmlparser.impl.AttributeName ROWALIGN
fld public final static nu.validator.htmlparser.impl.AttributeName ROWLINES
fld public final static nu.validator.htmlparser.impl.AttributeName ROWS
fld public final static nu.validator.htmlparser.impl.AttributeName ROWSPACING
fld public final static nu.validator.htmlparser.impl.AttributeName ROWSPAN
fld public final static nu.validator.htmlparser.impl.AttributeName RQUOTE
fld public final static nu.validator.htmlparser.impl.AttributeName RSPACE
fld public final static nu.validator.htmlparser.impl.AttributeName RT
fld public final static nu.validator.htmlparser.impl.AttributeName RULES
fld public final static nu.validator.htmlparser.impl.AttributeName RX
fld public final static nu.validator.htmlparser.impl.AttributeName RY
fld public final static nu.validator.htmlparser.impl.AttributeName SANDBOX
fld public final static nu.validator.htmlparser.impl.AttributeName SCALE
fld public final static nu.validator.htmlparser.impl.AttributeName SCHEME
fld public final static nu.validator.htmlparser.impl.AttributeName SCOPE
fld public final static nu.validator.htmlparser.impl.AttributeName SCOPED
fld public final static nu.validator.htmlparser.impl.AttributeName SCRIPTLEVEL
fld public final static nu.validator.htmlparser.impl.AttributeName SCRIPTMINSIZE
fld public final static nu.validator.htmlparser.impl.AttributeName SCRIPTSIZEMULTIPLIER
fld public final static nu.validator.htmlparser.impl.AttributeName SCROLLING
fld public final static nu.validator.htmlparser.impl.AttributeName SEED
fld public final static nu.validator.htmlparser.impl.AttributeName SELECTED
fld public final static nu.validator.htmlparser.impl.AttributeName SELECTION
fld public final static nu.validator.htmlparser.impl.AttributeName SEPARATOR
fld public final static nu.validator.htmlparser.impl.AttributeName SEPARATORS
fld public final static nu.validator.htmlparser.impl.AttributeName SHAPE
fld public final static nu.validator.htmlparser.impl.AttributeName SHAPE_RENDERING
fld public final static nu.validator.htmlparser.impl.AttributeName SIZE
fld public final static nu.validator.htmlparser.impl.AttributeName SIZES
fld public final static nu.validator.htmlparser.impl.AttributeName SLOPE
fld public final static nu.validator.htmlparser.impl.AttributeName SPACING
fld public final static nu.validator.htmlparser.impl.AttributeName SPAN
fld public final static nu.validator.htmlparser.impl.AttributeName SPECULARCONSTANT
fld public final static nu.validator.htmlparser.impl.AttributeName SPECULAREXPONENT
fld public final static nu.validator.htmlparser.impl.AttributeName SPREADMETHOD
fld public final static nu.validator.htmlparser.impl.AttributeName SRC
fld public final static nu.validator.htmlparser.impl.AttributeName SRCDOC
fld public final static nu.validator.htmlparser.impl.AttributeName SRCSET
fld public final static nu.validator.htmlparser.impl.AttributeName STANDBY
fld public final static nu.validator.htmlparser.impl.AttributeName START
fld public final static nu.validator.htmlparser.impl.AttributeName STARTOFFSET
fld public final static nu.validator.htmlparser.impl.AttributeName STDDEVIATION
fld public final static nu.validator.htmlparser.impl.AttributeName STEP
fld public final static nu.validator.htmlparser.impl.AttributeName STITCHTILES
fld public final static nu.validator.htmlparser.impl.AttributeName STOP_COLOR
fld public final static nu.validator.htmlparser.impl.AttributeName STOP_OPACITY
fld public final static nu.validator.htmlparser.impl.AttributeName STRETCHY
fld public final static nu.validator.htmlparser.impl.AttributeName STROKE
fld public final static nu.validator.htmlparser.impl.AttributeName STROKE_DASHARRAY
fld public final static nu.validator.htmlparser.impl.AttributeName STROKE_DASHOFFSET
fld public final static nu.validator.htmlparser.impl.AttributeName STROKE_LINECAP
fld public final static nu.validator.htmlparser.impl.AttributeName STROKE_LINEJOIN
fld public final static nu.validator.htmlparser.impl.AttributeName STROKE_MITERLIMIT
fld public final static nu.validator.htmlparser.impl.AttributeName STROKE_OPACITY
fld public final static nu.validator.htmlparser.impl.AttributeName STROKE_WIDTH
fld public final static nu.validator.htmlparser.impl.AttributeName STYLE
fld public final static nu.validator.htmlparser.impl.AttributeName SUBSCRIPTSHIFT
fld public final static nu.validator.htmlparser.impl.AttributeName SUMMARY
fld public final static nu.validator.htmlparser.impl.AttributeName SUPERSCRIPTSHIFT
fld public final static nu.validator.htmlparser.impl.AttributeName SURFACESCALE
fld public final static nu.validator.htmlparser.impl.AttributeName SYMMETRIC
fld public final static nu.validator.htmlparser.impl.AttributeName SYSTEMLANGUAGE
fld public final static nu.validator.htmlparser.impl.AttributeName TABINDEX
fld public final static nu.validator.htmlparser.impl.AttributeName TABLEVALUES
fld public final static nu.validator.htmlparser.impl.AttributeName TARGET
fld public final static nu.validator.htmlparser.impl.AttributeName TARGETX
fld public final static nu.validator.htmlparser.impl.AttributeName TARGETY
fld public final static nu.validator.htmlparser.impl.AttributeName TEMPLATE
fld public final static nu.validator.htmlparser.impl.AttributeName TEXT
fld public final static nu.validator.htmlparser.impl.AttributeName TEXTLENGTH
fld public final static nu.validator.htmlparser.impl.AttributeName TEXT_ANCHOR
fld public final static nu.validator.htmlparser.impl.AttributeName TEXT_DECORATION
fld public final static nu.validator.htmlparser.impl.AttributeName TEXT_RENDERING
fld public final static nu.validator.htmlparser.impl.AttributeName TITLE
fld public final static nu.validator.htmlparser.impl.AttributeName TO
fld public final static nu.validator.htmlparser.impl.AttributeName TRANSFORM
fld public final static nu.validator.htmlparser.impl.AttributeName TYPE
fld public final static nu.validator.htmlparser.impl.AttributeName USEMAP
fld public final static nu.validator.htmlparser.impl.AttributeName VALIGN
fld public final static nu.validator.htmlparser.impl.AttributeName VALUE
fld public final static nu.validator.htmlparser.impl.AttributeName VALUES
fld public final static nu.validator.htmlparser.impl.AttributeName VALUETYPE
fld public final static nu.validator.htmlparser.impl.AttributeName VERSION
fld public final static nu.validator.htmlparser.impl.AttributeName VIEWBOX
fld public final static nu.validator.htmlparser.impl.AttributeName VIEWTARGET
fld public final static nu.validator.htmlparser.impl.AttributeName VISIBILITY
fld public final static nu.validator.htmlparser.impl.AttributeName VLINK
fld public final static nu.validator.htmlparser.impl.AttributeName VSPACE
fld public final static nu.validator.htmlparser.impl.AttributeName WHEN
fld public final static nu.validator.htmlparser.impl.AttributeName WIDTH
fld public final static nu.validator.htmlparser.impl.AttributeName WORD_SPACING
fld public final static nu.validator.htmlparser.impl.AttributeName WRAP
fld public final static nu.validator.htmlparser.impl.AttributeName WRITING_MODE
fld public final static nu.validator.htmlparser.impl.AttributeName X
fld public final static nu.validator.htmlparser.impl.AttributeName X1
fld public final static nu.validator.htmlparser.impl.AttributeName X2
fld public final static nu.validator.htmlparser.impl.AttributeName XCHANNELSELECTOR
fld public final static nu.validator.htmlparser.impl.AttributeName XLINK_ACTUATE
fld public final static nu.validator.htmlparser.impl.AttributeName XLINK_ARCROLE
fld public final static nu.validator.htmlparser.impl.AttributeName XLINK_HREF
fld public final static nu.validator.htmlparser.impl.AttributeName XLINK_ROLE
fld public final static nu.validator.htmlparser.impl.AttributeName XLINK_SHOW
fld public final static nu.validator.htmlparser.impl.AttributeName XLINK_TITLE
fld public final static nu.validator.htmlparser.impl.AttributeName XLINK_TYPE
fld public final static nu.validator.htmlparser.impl.AttributeName XMLNS
fld public final static nu.validator.htmlparser.impl.AttributeName XMLNS_XLINK
fld public final static nu.validator.htmlparser.impl.AttributeName XML_LANG
fld public final static nu.validator.htmlparser.impl.AttributeName XML_SPACE
fld public final static nu.validator.htmlparser.impl.AttributeName XREF
fld public final static nu.validator.htmlparser.impl.AttributeName Y
fld public final static nu.validator.htmlparser.impl.AttributeName Y1
fld public final static nu.validator.htmlparser.impl.AttributeName Y2
fld public final static nu.validator.htmlparser.impl.AttributeName YCHANNELSELECTOR
fld public final static nu.validator.htmlparser.impl.AttributeName Z
fld public final static nu.validator.htmlparser.impl.AttributeName ZOOMANDPAN
meth public boolean isNcName(int)
meth public boolean isXmlns()
meth public java.lang.String getLocal(int)
meth public java.lang.String getPrefix(int)
meth public java.lang.String getQName(int)
meth public java.lang.String getUri(int)
supr java.lang.Object
hfds ALL_NO_NS,ALL_NO_PREFIX,ATTRIBUTE_HASHES,ATTRIBUTE_NAMES,LANG_NS,LANG_PREFIX,XLINK_NS,XLINK_PREFIX,XMLNS_NS,XMLNS_PREFIX,XML_NS,XML_PREFIX,flags,local,prefix,qName,uri

CLSS public abstract nu.validator.htmlparser.impl.CoalescingTreeBuilder<%0 extends java.lang.Object>
cons public init()
meth protected abstract void appendCharacters({nu.validator.htmlparser.impl.CoalescingTreeBuilder%0},java.lang.String) throws org.xml.sax.SAXException
meth protected abstract void appendComment({nu.validator.htmlparser.impl.CoalescingTreeBuilder%0},java.lang.String) throws org.xml.sax.SAXException
meth protected abstract void appendCommentToDocument(java.lang.String) throws org.xml.sax.SAXException
meth protected abstract void insertFosterParentedCharacters(java.lang.String,{nu.validator.htmlparser.impl.CoalescingTreeBuilder%0},{nu.validator.htmlparser.impl.CoalescingTreeBuilder%0}) throws org.xml.sax.SAXException
meth protected final void accumulateCharacters(char[],int,int) throws org.xml.sax.SAXException
 anno 1 nu.validator.htmlparser.annotation.NoLength()
meth protected final void appendCharacters({nu.validator.htmlparser.impl.CoalescingTreeBuilder%0},char[],int,int) throws org.xml.sax.SAXException
meth protected final void appendComment({nu.validator.htmlparser.impl.CoalescingTreeBuilder%0},char[],int,int) throws org.xml.sax.SAXException
meth protected final void appendCommentToDocument(char[],int,int) throws org.xml.sax.SAXException
meth protected final void insertFosterParentedCharacters(char[],int,int,{nu.validator.htmlparser.impl.CoalescingTreeBuilder%0},{nu.validator.htmlparser.impl.CoalescingTreeBuilder%0}) throws org.xml.sax.SAXException
supr nu.validator.htmlparser.impl.TreeBuilder<{nu.validator.htmlparser.impl.CoalescingTreeBuilder%0}>

CLSS public final nu.validator.htmlparser.impl.ElementName
cons public init()
fld public final int flags
fld public final static int FOSTER_PARENTING = 268435456
fld public final static int GROUP_MASK = 127
fld public final static int HTML_INTEGRATION_POINT = 16777216
fld public final static int NOT_INTERNED = 1073741824
fld public final static int OPTIONAL_END_TAG = 8388608
fld public final static int SCOPING = 134217728
fld public final static int SCOPING_AS_MATHML = 33554432
fld public final static int SCOPING_AS_SVG = 67108864
fld public final static int SPECIAL = 536870912
fld public final static nu.validator.htmlparser.impl.ElementName A
fld public final static nu.validator.htmlparser.impl.ElementName ABBR
fld public final static nu.validator.htmlparser.impl.ElementName ACRONYM
fld public final static nu.validator.htmlparser.impl.ElementName ADDRESS
fld public final static nu.validator.htmlparser.impl.ElementName ALTGLYPH
fld public final static nu.validator.htmlparser.impl.ElementName ALTGLYPHDEF
fld public final static nu.validator.htmlparser.impl.ElementName ALTGLYPHITEM
fld public final static nu.validator.htmlparser.impl.ElementName ANIMATE
fld public final static nu.validator.htmlparser.impl.ElementName ANIMATECOLOR
fld public final static nu.validator.htmlparser.impl.ElementName ANIMATEMOTION
fld public final static nu.validator.htmlparser.impl.ElementName ANIMATETRANSFORM
fld public final static nu.validator.htmlparser.impl.ElementName ANNOTATION_XML
fld public final static nu.validator.htmlparser.impl.ElementName APPLET
fld public final static nu.validator.htmlparser.impl.ElementName AREA
fld public final static nu.validator.htmlparser.impl.ElementName ARTICLE
fld public final static nu.validator.htmlparser.impl.ElementName ASIDE
fld public final static nu.validator.htmlparser.impl.ElementName AUDIO
fld public final static nu.validator.htmlparser.impl.ElementName B
fld public final static nu.validator.htmlparser.impl.ElementName BASE
fld public final static nu.validator.htmlparser.impl.ElementName BASEFONT
fld public final static nu.validator.htmlparser.impl.ElementName BDI
fld public final static nu.validator.htmlparser.impl.ElementName BDO
fld public final static nu.validator.htmlparser.impl.ElementName BGSOUND
fld public final static nu.validator.htmlparser.impl.ElementName BIG
fld public final static nu.validator.htmlparser.impl.ElementName BLOCKQUOTE
fld public final static nu.validator.htmlparser.impl.ElementName BODY
fld public final static nu.validator.htmlparser.impl.ElementName BR
fld public final static nu.validator.htmlparser.impl.ElementName BUTTON
fld public final static nu.validator.htmlparser.impl.ElementName CANVAS
fld public final static nu.validator.htmlparser.impl.ElementName CAPTION
fld public final static nu.validator.htmlparser.impl.ElementName CENTER
fld public final static nu.validator.htmlparser.impl.ElementName CIRCLE
fld public final static nu.validator.htmlparser.impl.ElementName CITE
fld public final static nu.validator.htmlparser.impl.ElementName CLIPPATH
fld public final static nu.validator.htmlparser.impl.ElementName CODE
fld public final static nu.validator.htmlparser.impl.ElementName COL
fld public final static nu.validator.htmlparser.impl.ElementName COLGROUP
fld public final static nu.validator.htmlparser.impl.ElementName DATA
fld public final static nu.validator.htmlparser.impl.ElementName DATALIST
fld public final static nu.validator.htmlparser.impl.ElementName DD
fld public final static nu.validator.htmlparser.impl.ElementName DEFS
fld public final static nu.validator.htmlparser.impl.ElementName DEL
fld public final static nu.validator.htmlparser.impl.ElementName DESC
fld public final static nu.validator.htmlparser.impl.ElementName DETAILS
fld public final static nu.validator.htmlparser.impl.ElementName DFN
fld public final static nu.validator.htmlparser.impl.ElementName DIALOG
fld public final static nu.validator.htmlparser.impl.ElementName DIR
fld public final static nu.validator.htmlparser.impl.ElementName DIV
fld public final static nu.validator.htmlparser.impl.ElementName DL
fld public final static nu.validator.htmlparser.impl.ElementName DT
fld public final static nu.validator.htmlparser.impl.ElementName ELLIPSE
fld public final static nu.validator.htmlparser.impl.ElementName EM
fld public final static nu.validator.htmlparser.impl.ElementName EMBED
fld public final static nu.validator.htmlparser.impl.ElementName FEBLEND
fld public final static nu.validator.htmlparser.impl.ElementName FECOLORMATRIX
fld public final static nu.validator.htmlparser.impl.ElementName FECOMPONENTTRANSFER
fld public final static nu.validator.htmlparser.impl.ElementName FECOMPOSITE
fld public final static nu.validator.htmlparser.impl.ElementName FECONVOLVEMATRIX
fld public final static nu.validator.htmlparser.impl.ElementName FEDIFFUSELIGHTING
fld public final static nu.validator.htmlparser.impl.ElementName FEDISPLACEMENTMAP
fld public final static nu.validator.htmlparser.impl.ElementName FEDISTANTLIGHT
fld public final static nu.validator.htmlparser.impl.ElementName FEDROPSHADOW
fld public final static nu.validator.htmlparser.impl.ElementName FEFLOOD
fld public final static nu.validator.htmlparser.impl.ElementName FEFUNCA
fld public final static nu.validator.htmlparser.impl.ElementName FEFUNCB
fld public final static nu.validator.htmlparser.impl.ElementName FEFUNCG
fld public final static nu.validator.htmlparser.impl.ElementName FEFUNCR
fld public final static nu.validator.htmlparser.impl.ElementName FEGAUSSIANBLUR
fld public final static nu.validator.htmlparser.impl.ElementName FEIMAGE
fld public final static nu.validator.htmlparser.impl.ElementName FEMERGE
fld public final static nu.validator.htmlparser.impl.ElementName FEMERGENODE
fld public final static nu.validator.htmlparser.impl.ElementName FEMORPHOLOGY
fld public final static nu.validator.htmlparser.impl.ElementName FEOFFSET
fld public final static nu.validator.htmlparser.impl.ElementName FEPOINTLIGHT
fld public final static nu.validator.htmlparser.impl.ElementName FESPECULARLIGHTING
fld public final static nu.validator.htmlparser.impl.ElementName FESPOTLIGHT
fld public final static nu.validator.htmlparser.impl.ElementName FETILE
fld public final static nu.validator.htmlparser.impl.ElementName FETURBULENCE
fld public final static nu.validator.htmlparser.impl.ElementName FIELDSET
fld public final static nu.validator.htmlparser.impl.ElementName FIGCAPTION
fld public final static nu.validator.htmlparser.impl.ElementName FIGURE
fld public final static nu.validator.htmlparser.impl.ElementName FILTER
fld public final static nu.validator.htmlparser.impl.ElementName FONT
fld public final static nu.validator.htmlparser.impl.ElementName FOOTER
fld public final static nu.validator.htmlparser.impl.ElementName FOREIGNOBJECT
fld public final static nu.validator.htmlparser.impl.ElementName FORM
fld public final static nu.validator.htmlparser.impl.ElementName FRAME
fld public final static nu.validator.htmlparser.impl.ElementName FRAMESET
fld public final static nu.validator.htmlparser.impl.ElementName G
fld public final static nu.validator.htmlparser.impl.ElementName GLYPHREF
fld public final static nu.validator.htmlparser.impl.ElementName H1
fld public final static nu.validator.htmlparser.impl.ElementName H2
fld public final static nu.validator.htmlparser.impl.ElementName H3
fld public final static nu.validator.htmlparser.impl.ElementName H4
fld public final static nu.validator.htmlparser.impl.ElementName H5
fld public final static nu.validator.htmlparser.impl.ElementName H6
fld public final static nu.validator.htmlparser.impl.ElementName HEAD
fld public final static nu.validator.htmlparser.impl.ElementName HEADER
fld public final static nu.validator.htmlparser.impl.ElementName HGROUP
fld public final static nu.validator.htmlparser.impl.ElementName HR
fld public final static nu.validator.htmlparser.impl.ElementName HTML
fld public final static nu.validator.htmlparser.impl.ElementName I
fld public final static nu.validator.htmlparser.impl.ElementName IFRAME
fld public final static nu.validator.htmlparser.impl.ElementName IMAGE
fld public final static nu.validator.htmlparser.impl.ElementName IMG
fld public final static nu.validator.htmlparser.impl.ElementName INPUT
fld public final static nu.validator.htmlparser.impl.ElementName INS
fld public final static nu.validator.htmlparser.impl.ElementName KBD
fld public final static nu.validator.htmlparser.impl.ElementName KEYGEN
fld public final static nu.validator.htmlparser.impl.ElementName LABEL
fld public final static nu.validator.htmlparser.impl.ElementName LEGEND
fld public final static nu.validator.htmlparser.impl.ElementName LI
fld public final static nu.validator.htmlparser.impl.ElementName LINE
fld public final static nu.validator.htmlparser.impl.ElementName LINEARGRADIENT
fld public final static nu.validator.htmlparser.impl.ElementName LINK
fld public final static nu.validator.htmlparser.impl.ElementName LISTING
fld public final static nu.validator.htmlparser.impl.ElementName MAIN
fld public final static nu.validator.htmlparser.impl.ElementName MALIGNMARK
fld public final static nu.validator.htmlparser.impl.ElementName MAP
fld public final static nu.validator.htmlparser.impl.ElementName MARK
fld public final static nu.validator.htmlparser.impl.ElementName MARKER
fld public final static nu.validator.htmlparser.impl.ElementName MARQUEE
fld public final static nu.validator.htmlparser.impl.ElementName MASK
fld public final static nu.validator.htmlparser.impl.ElementName MATH
fld public final static nu.validator.htmlparser.impl.ElementName MENU
fld public final static nu.validator.htmlparser.impl.ElementName MENUITEM
fld public final static nu.validator.htmlparser.impl.ElementName META
fld public final static nu.validator.htmlparser.impl.ElementName METADATA
fld public final static nu.validator.htmlparser.impl.ElementName METER
fld public final static nu.validator.htmlparser.impl.ElementName MGLYPH
fld public final static nu.validator.htmlparser.impl.ElementName MI
fld public final static nu.validator.htmlparser.impl.ElementName MN
fld public final static nu.validator.htmlparser.impl.ElementName MO
fld public final static nu.validator.htmlparser.impl.ElementName MPATH
fld public final static nu.validator.htmlparser.impl.ElementName MS
fld public final static nu.validator.htmlparser.impl.ElementName MTEXT
fld public final static nu.validator.htmlparser.impl.ElementName NAV
fld public final static nu.validator.htmlparser.impl.ElementName NOBR
fld public final static nu.validator.htmlparser.impl.ElementName NOEMBED
fld public final static nu.validator.htmlparser.impl.ElementName NOFRAMES
fld public final static nu.validator.htmlparser.impl.ElementName NOSCRIPT
fld public final static nu.validator.htmlparser.impl.ElementName OBJECT
fld public final static nu.validator.htmlparser.impl.ElementName OL
fld public final static nu.validator.htmlparser.impl.ElementName OPTGROUP
fld public final static nu.validator.htmlparser.impl.ElementName OPTION
fld public final static nu.validator.htmlparser.impl.ElementName OUTPUT
fld public final static nu.validator.htmlparser.impl.ElementName P
fld public final static nu.validator.htmlparser.impl.ElementName PARAM
fld public final static nu.validator.htmlparser.impl.ElementName PATH
fld public final static nu.validator.htmlparser.impl.ElementName PATTERN
fld public final static nu.validator.htmlparser.impl.ElementName PICTURE
fld public final static nu.validator.htmlparser.impl.ElementName PLAINTEXT
fld public final static nu.validator.htmlparser.impl.ElementName POLYGON
fld public final static nu.validator.htmlparser.impl.ElementName POLYLINE
fld public final static nu.validator.htmlparser.impl.ElementName PRE
fld public final static nu.validator.htmlparser.impl.ElementName PROGRESS
fld public final static nu.validator.htmlparser.impl.ElementName Q
fld public final static nu.validator.htmlparser.impl.ElementName RADIALGRADIENT
fld public final static nu.validator.htmlparser.impl.ElementName RB
fld public final static nu.validator.htmlparser.impl.ElementName RECT
fld public final static nu.validator.htmlparser.impl.ElementName RP
fld public final static nu.validator.htmlparser.impl.ElementName RT
fld public final static nu.validator.htmlparser.impl.ElementName RTC
fld public final static nu.validator.htmlparser.impl.ElementName RUBY
fld public final static nu.validator.htmlparser.impl.ElementName S
fld public final static nu.validator.htmlparser.impl.ElementName SAMP
fld public final static nu.validator.htmlparser.impl.ElementName SCRIPT
fld public final static nu.validator.htmlparser.impl.ElementName SECTION
fld public final static nu.validator.htmlparser.impl.ElementName SELECT
fld public final static nu.validator.htmlparser.impl.ElementName SET
fld public final static nu.validator.htmlparser.impl.ElementName SLOT
fld public final static nu.validator.htmlparser.impl.ElementName SMALL
fld public final static nu.validator.htmlparser.impl.ElementName SOURCE
fld public final static nu.validator.htmlparser.impl.ElementName SPAN
fld public final static nu.validator.htmlparser.impl.ElementName STOP
fld public final static nu.validator.htmlparser.impl.ElementName STRIKE
fld public final static nu.validator.htmlparser.impl.ElementName STRONG
fld public final static nu.validator.htmlparser.impl.ElementName STYLE
fld public final static nu.validator.htmlparser.impl.ElementName SUB
fld public final static nu.validator.htmlparser.impl.ElementName SUMMARY
fld public final static nu.validator.htmlparser.impl.ElementName SUP
fld public final static nu.validator.htmlparser.impl.ElementName SVG
fld public final static nu.validator.htmlparser.impl.ElementName SWITCH
fld public final static nu.validator.htmlparser.impl.ElementName SYMBOL
fld public final static nu.validator.htmlparser.impl.ElementName TABLE
fld public final static nu.validator.htmlparser.impl.ElementName TBODY
fld public final static nu.validator.htmlparser.impl.ElementName TD
fld public final static nu.validator.htmlparser.impl.ElementName TEMPLATE
fld public final static nu.validator.htmlparser.impl.ElementName TEXT
fld public final static nu.validator.htmlparser.impl.ElementName TEXTAREA
fld public final static nu.validator.htmlparser.impl.ElementName TEXTPATH
fld public final static nu.validator.htmlparser.impl.ElementName TFOOT
fld public final static nu.validator.htmlparser.impl.ElementName TH
fld public final static nu.validator.htmlparser.impl.ElementName THEAD
fld public final static nu.validator.htmlparser.impl.ElementName TIME
fld public final static nu.validator.htmlparser.impl.ElementName TITLE
fld public final static nu.validator.htmlparser.impl.ElementName TR
fld public final static nu.validator.htmlparser.impl.ElementName TRACK
fld public final static nu.validator.htmlparser.impl.ElementName TSPAN
fld public final static nu.validator.htmlparser.impl.ElementName TT
fld public final static nu.validator.htmlparser.impl.ElementName U
fld public final static nu.validator.htmlparser.impl.ElementName UL
fld public final static nu.validator.htmlparser.impl.ElementName USE
fld public final static nu.validator.htmlparser.impl.ElementName VAR
fld public final static nu.validator.htmlparser.impl.ElementName VIDEO
fld public final static nu.validator.htmlparser.impl.ElementName VIEW
fld public final static nu.validator.htmlparser.impl.ElementName WBR
fld public final static nu.validator.htmlparser.impl.ElementName XMP
meth public boolean isInterned()
meth public int getFlags()
meth public int getGroup()
meth public java.lang.String getCamelCaseName()
meth public java.lang.String getName()
meth public void destructor()
meth public void setNameForNonInterned(java.lang.String)
supr java.lang.Object
hfds ELEMENT_HASHES,ELEMENT_NAMES,camelCaseName,name

CLSS public nu.validator.htmlparser.impl.ErrorReportingTokenizer
cons public init(nu.validator.htmlparser.common.TokenHandler)
cons public init(nu.validator.htmlparser.common.TokenHandler,boolean)
meth protected char checkChar(char[],int) throws org.xml.sax.SAXException
meth protected char errNcrControlChar(char) throws org.xml.sax.SAXException
meth protected char errNcrNonCharacter(char) throws org.xml.sax.SAXException
meth protected int transition(int,int,boolean,int) throws org.xml.sax.SAXException
meth protected void errAstralNonCharacter(int) throws org.xml.sax.SAXException
meth protected void errAttributeValueMissing() throws org.xml.sax.SAXException
meth protected void errBadCharAfterLt(char) throws org.xml.sax.SAXException
meth protected void errBadCharBeforeAttributeNameOrNull(char) throws org.xml.sax.SAXException
meth protected void errBogusComment() throws org.xml.sax.SAXException
meth protected void errBogusDoctype() throws org.xml.sax.SAXException
meth protected void errCharRefLacksSemicolon() throws org.xml.sax.SAXException
meth protected void errConsecutiveHyphens() throws org.xml.sax.SAXException
meth protected void errDuplicateAttribute() throws org.xml.sax.SAXException
meth protected void errEofAfterLt() throws org.xml.sax.SAXException
meth protected void errEofInAttributeName() throws org.xml.sax.SAXException
meth protected void errEofInAttributeValue() throws org.xml.sax.SAXException
meth protected void errEofInComment() throws org.xml.sax.SAXException
meth protected void errEofInDoctype() throws org.xml.sax.SAXException
meth protected void errEofInEndTag() throws org.xml.sax.SAXException
meth protected void errEofInPublicId() throws org.xml.sax.SAXException
meth protected void errEofInSystemId() throws org.xml.sax.SAXException
meth protected void errEofInTagName() throws org.xml.sax.SAXException
meth protected void errEofWithoutGt() throws org.xml.sax.SAXException
meth protected void errEqualsSignBeforeAttributeName() throws org.xml.sax.SAXException
meth protected void errExpectedPublicId() throws org.xml.sax.SAXException
meth protected void errExpectedSystemId() throws org.xml.sax.SAXException
meth protected void errGarbageAfterLtSlash() throws org.xml.sax.SAXException
meth protected void errGtInPublicId() throws org.xml.sax.SAXException
meth protected void errGtInSystemId() throws org.xml.sax.SAXException
meth protected void errHtml4LtSlashInRcdata(char) throws org.xml.sax.SAXException
meth protected void errHtml4NonNameInUnquotedAttribute(char) throws org.xml.sax.SAXException
meth protected void errHtml4XmlVoidSyntax() throws org.xml.sax.SAXException
meth protected void errHyphenHyphenBang() throws org.xml.sax.SAXException
meth protected void errLtGt() throws org.xml.sax.SAXException
meth protected void errLtOrEqualsOrGraveInUnquotedAttributeOrNull(char) throws org.xml.sax.SAXException
meth protected void errLtSlashGt() throws org.xml.sax.SAXException
meth protected void errMissingSpaceBeforeDoctypeName() throws org.xml.sax.SAXException
meth protected void errNamelessDoctype() throws org.xml.sax.SAXException
meth protected void errNcrControlChar() throws org.xml.sax.SAXException
meth protected void errNcrCr() throws org.xml.sax.SAXException
meth protected void errNcrInC1Range() throws org.xml.sax.SAXException
meth protected void errNcrOutOfRange() throws org.xml.sax.SAXException
meth protected void errNcrSurrogate() throws org.xml.sax.SAXException
meth protected void errNcrUnassigned() throws org.xml.sax.SAXException
meth protected void errNcrZero() throws org.xml.sax.SAXException
meth protected void errNoDigitsInNCR() throws org.xml.sax.SAXException
meth protected void errNoNamedCharacterMatch() throws org.xml.sax.SAXException
meth protected void errNoSpaceBetweenAttributes() throws org.xml.sax.SAXException
meth protected void errNoSpaceBetweenDoctypePublicKeywordAndQuote() throws org.xml.sax.SAXException
meth protected void errNoSpaceBetweenDoctypeSystemKeywordAndQuote() throws org.xml.sax.SAXException
meth protected void errNoSpaceBetweenPublicAndSystemIds() throws org.xml.sax.SAXException
meth protected void errNotSemicolonTerminated() throws org.xml.sax.SAXException
meth protected void errPrematureEndOfComment() throws org.xml.sax.SAXException
meth protected void errProcessingInstruction() throws org.xml.sax.SAXException
meth protected void errQuoteBeforeAttributeName(char) throws org.xml.sax.SAXException
meth protected void errQuoteOrLtInAttributeNameOrNull(char) throws org.xml.sax.SAXException
meth protected void errSlashNotFollowedByGt() throws org.xml.sax.SAXException
meth protected void errUnescapedAmpersandInterpretedAsCharacterReference() throws org.xml.sax.SAXException
meth protected void errUnquotedAttributeValOrNull(char) throws org.xml.sax.SAXException
meth protected void errWarnLtSlashInRcdata() throws org.xml.sax.SAXException
meth protected void flushChars(char[],int) throws org.xml.sax.SAXException
meth protected void maybeErrAttributesOnEndTag(nu.validator.htmlparser.impl.HtmlAttributes) throws org.xml.sax.SAXException
meth protected void maybeErrSlashInEndTag(boolean) throws org.xml.sax.SAXException
meth protected void maybeWarnPrivateUse(char) throws org.xml.sax.SAXException
meth protected void maybeWarnPrivateUseAstral() throws org.xml.sax.SAXException
meth protected void noteAttributeWithoutValue() throws org.xml.sax.SAXException
meth protected void noteUnquotedAttributeValue() throws org.xml.sax.SAXException
meth protected void silentCarriageReturn()
meth protected void silentLineFeed()
meth protected void startErrorReporting() throws org.xml.sax.SAXException
meth public boolean isNextCharOnNewLine()
meth public int getCol()
meth public int getColumnNumber()
meth public int getLine()
meth public int getLineNumber()
meth public void note(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setContentNonXmlCharPolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setErrorProfile(java.util.HashMap<java.lang.String,java.lang.String>)
meth public void setTransitionBaseOffset(int)
meth public void setTransitionHandler(nu.validator.htmlparser.common.TransitionHandler)
supr nu.validator.htmlparser.impl.Tokenizer
hfds SURROGATE_OFFSET,alreadyWarnedAboutPrivateUseCharacters,col,colPrev,contentNonXmlCharPolicy,errorProfileMap,line,linePrev,nextCharOnNewLine,prev,transitionBaseOffset,transitionHandler

CLSS public final nu.validator.htmlparser.impl.HtmlAttributes
cons public init(int)
fld public final static nu.validator.htmlparser.impl.HtmlAttributes EMPTY_ATTRIBUTES
intf org.xml.sax.Attributes
meth public boolean equalsAnother(nu.validator.htmlparser.impl.HtmlAttributes)
meth public int getIndex(java.lang.String)
meth public int getIndex(java.lang.String,java.lang.String)
meth public int getIndex(nu.validator.htmlparser.impl.AttributeName)
meth public int getLength()
meth public int getXmlnsIndex(nu.validator.htmlparser.impl.AttributeName)
meth public int getXmlnsLength()
meth public java.lang.String getId()
meth public java.lang.String getLocalName(int)
meth public java.lang.String getLocalNameNoBoundsCheck(int)
meth public java.lang.String getPrefix(int)
meth public java.lang.String getPrefixNoBoundsCheck(int)
meth public java.lang.String getQName(int)
meth public java.lang.String getQNameNoBoundsCheck(int)
meth public java.lang.String getType(int)
meth public java.lang.String getType(java.lang.String)
meth public java.lang.String getType(java.lang.String,java.lang.String)
meth public java.lang.String getTypeNoBoundsCheck(int)
meth public java.lang.String getURI(int)
meth public java.lang.String getURINoBoundsCheck(int)
meth public java.lang.String getValue(int)
meth public java.lang.String getValue(java.lang.String)
meth public java.lang.String getValue(java.lang.String,java.lang.String)
meth public java.lang.String getValue(nu.validator.htmlparser.impl.AttributeName)
meth public java.lang.String getValueNoBoundsCheck(int)
meth public java.lang.String getXmlnsLocalName(int)
meth public java.lang.String getXmlnsURI(int)
meth public java.lang.String getXmlnsValue(int)
meth public java.lang.String getXmlnsValue(nu.validator.htmlparser.impl.AttributeName)
meth public nu.validator.htmlparser.impl.AttributeName getAttributeName(int)
meth public nu.validator.htmlparser.impl.AttributeName getAttributeNameNoBoundsCheck(int)
meth public nu.validator.htmlparser.impl.AttributeName getXmlnsAttributeName(int)
meth public nu.validator.htmlparser.impl.HtmlAttributes cloneAttributes() throws org.xml.sax.SAXException
meth public void adjustForMath()
meth public void adjustForSvg()
meth public void merge(nu.validator.htmlparser.impl.HtmlAttributes) throws org.xml.sax.SAXException
supr java.lang.Object
hfds EMPTY_ATTRIBUTENAMES,EMPTY_STRINGS,idValue,length,mode,names,values,xmlnsLength,xmlnsNames,xmlnsValues

CLSS public nu.validator.htmlparser.impl.LocatorImpl
cons public init(org.xml.sax.Locator)
intf org.xml.sax.Locator
meth public final int getColumnNumber()
meth public final int getLineNumber()
meth public final java.lang.String getPublicId()
meth public final java.lang.String getSystemId()
supr java.lang.Object
hfds column,line,publicId,systemId

CLSS public abstract nu.validator.htmlparser.impl.MetaScanner
cons public init()
fld protected int stateSave
fld protected nu.validator.htmlparser.common.ByteReadable readable
meth protected abstract boolean tryCharset(java.lang.String) throws org.xml.sax.SAXException
meth protected final void stateLoop(int) throws java.io.IOException,org.xml.sax.SAXException
meth protected int read() throws java.io.IOException
supr java.lang.Object
hfds A,AFTER_ATTRIBUTE_NAME,AFTER_ATTRIBUTE_VALUE_QUOTED,ATTRIBUTE_NAME,ATTRIBUTE_VALUE_DOUBLE_QUOTED,ATTRIBUTE_VALUE_SINGLE_QUOTED,ATTRIBUTE_VALUE_UNQUOTED,BEFORE_ATTRIBUTE_NAME,BEFORE_ATTRIBUTE_VALUE,CHARSET,COMMENT,COMMENT_END,COMMENT_END_DASH,COMMENT_START,COMMENT_START_DASH,CONTENT,CONTENT_TYPE,DATA,E,HTTP_EQUIV,HTTP_EQUIV_CONTENT_TYPE,HTTP_EQUIV_NOT_SEEN,HTTP_EQUIV_OTHER,M,MARKUP_DECLARATION_HYPHEN,MARKUP_DECLARATION_OPEN,NO,SCAN_UNTIL_GT,SELF_CLOSING_START_TAG,T,TAG_NAME,TAG_OPEN,charset,charsetIndex,content,contentIndex,contentTypeIndex,httpEquivIndex,httpEquivState,metaState,strBuf,strBufLen

CLSS public final nu.validator.htmlparser.impl.NCName
cons public init()
meth public static boolean isNCName(java.lang.String)
meth public static boolean isNCNameStart(char)
meth public static boolean isNCNameTrail(char)
meth public static java.lang.String escapeName(java.lang.String)
supr java.lang.Object
hfds HEX_TABLE,SURROGATE_OFFSET

CLSS public final nu.validator.htmlparser.impl.NamedCharacters
cons public init()
supr java.lang.Object
hfds NAMES,VALUES,WINDOWS_1252

CLSS public final nu.validator.htmlparser.impl.NamedCharactersAccel
cons public init()
supr java.lang.Object
hfds HILO_ACCEL

CLSS public final nu.validator.htmlparser.impl.Portability
cons public init()
meth public static boolean literalEqualsString(java.lang.String,java.lang.String)
meth public static boolean localEqualsBuffer(java.lang.String,char[],int)
meth public static boolean lowerCaseLiteralEqualsIgnoreAsciiCaseString(java.lang.String,java.lang.String)
meth public static boolean lowerCaseLiteralIsPrefixOfIgnoreAsciiCaseString(java.lang.String,java.lang.String)
meth public static boolean stringEqualsString(java.lang.String,java.lang.String)
meth public static char[] newCharArrayFromLocal(java.lang.String)
meth public static char[] newCharArrayFromString(java.lang.String)
meth public static java.lang.String newEmptyString()
meth public static java.lang.String newLocalNameFromBuffer(char[],int,nu.validator.htmlparser.common.Interner)
meth public static java.lang.String newStringFromBuffer(char[],int,int)
meth public static java.lang.String newStringFromLiteral(java.lang.String)
meth public static java.lang.String newStringFromString(java.lang.String)
meth public static void delete(java.lang.Object)
meth public static void deleteArray(java.lang.Object)
meth public static void releaseString(java.lang.String)
supr java.lang.Object

CLSS public nu.validator.htmlparser.impl.PushedLocation
cons public init(int,int,int,int,boolean,java.lang.String,java.lang.String,nu.validator.htmlparser.impl.PushedLocation)
meth public boolean isNextCharOnNewLine()
meth public int getCol()
meth public int getColPrev()
meth public int getLine()
meth public int getLinePrev()
meth public java.lang.String getPublicId()
meth public java.lang.String getSystemId()
meth public nu.validator.htmlparser.impl.PushedLocation getNext()
supr java.lang.Object
hfds col,colPrev,line,linePrev,next,nextCharOnNewLine,publicId,systemId

CLSS public nu.validator.htmlparser.impl.StateSnapshot<%0 extends java.lang.Object>
intf nu.validator.htmlparser.impl.TreeBuilderState<{nu.validator.htmlparser.impl.StateSnapshot%0}>
meth public boolean isFramesetOk()
meth public boolean isNeedToDropLF()
meth public boolean isQuirks()
meth public int getListOfActiveFormattingElementsLength()
meth public int getMode()
meth public int getOriginalMode()
meth public int getStackLength()
meth public int getTemplateModeStackLength()
meth public int[] getTemplateModeStack()
meth public nu.validator.htmlparser.impl.StackNode<{nu.validator.htmlparser.impl.StateSnapshot%0}>[] getListOfActiveFormattingElements()
meth public nu.validator.htmlparser.impl.StackNode<{nu.validator.htmlparser.impl.StateSnapshot%0}>[] getStack()
meth public {nu.validator.htmlparser.impl.StateSnapshot%0} getFormPointer()
meth public {nu.validator.htmlparser.impl.StateSnapshot%0} getHeadPointer()
supr java.lang.Object
hfds formPointer,framesetOk,headPointer,listOfActiveFormattingElements,mode,needToDropLF,originalMode,quirks,stack,templateModeStack

CLSS public nu.validator.htmlparser.impl.TaintableLocatorImpl
cons public init(org.xml.sax.Locator)
meth public boolean isTainted()
meth public void markTainted()
supr nu.validator.htmlparser.impl.LocatorImpl
hfds tainted

CLSS public nu.validator.htmlparser.impl.Tokenizer
cons public init(nu.validator.htmlparser.common.TokenHandler)
cons public init(nu.validator.htmlparser.common.TokenHandler,boolean)
fld protected boolean confident
fld protected boolean endTag
fld protected boolean html4
fld protected boolean lastCR
fld protected final nu.validator.htmlparser.common.TokenHandler tokenHandler
fld protected int cstart
fld protected int currentBufferGlobalOffset
fld protected int index
fld protected int stateSave
fld protected int value
fld protected nu.validator.htmlparser.common.EncodingDeclarationHandler encodingDeclarationHandler
fld protected nu.validator.htmlparser.impl.AttributeName attributeName
fld protected nu.validator.htmlparser.impl.ElementName endTagExpectation
fld protected nu.validator.htmlparser.impl.LocatorImpl ampersandLocation
fld protected org.xml.sax.ErrorHandler errorHandler
fld public final static int AFTER_ATTRIBUTE_NAME = 14
fld public final static int AFTER_ATTRIBUTE_VALUE_QUOTED = 16
fld public final static int AFTER_DOCTYPE_NAME = 22
fld public final static int AFTER_DOCTYPE_PUBLIC_IDENTIFIER = 26
fld public final static int AFTER_DOCTYPE_PUBLIC_KEYWORD = 43
fld public final static int AFTER_DOCTYPE_SYSTEM_IDENTIFIER = 30
fld public final static int AFTER_DOCTYPE_SYSTEM_KEYWORD = 45
fld public final static int ATTRIBUTE_NAME = 13
fld public final static int ATTRIBUTE_VALUE_DOUBLE_QUOTED = 5
fld public final static int ATTRIBUTE_VALUE_SINGLE_QUOTED = 6
fld public final static int ATTRIBUTE_VALUE_UNQUOTED = 7
fld public final static int BEFORE_ATTRIBUTE_NAME = 12
fld public final static int BEFORE_ATTRIBUTE_VALUE = 15
fld public final static int BEFORE_DOCTYPE_NAME = 20
fld public final static int BEFORE_DOCTYPE_PUBLIC_IDENTIFIER = 23
fld public final static int BEFORE_DOCTYPE_SYSTEM_IDENTIFIER = 27
fld public final static int BETWEEN_DOCTYPE_PUBLIC_AND_SYSTEM_IDENTIFIERS = 44
fld public final static int BOGUS_COMMENT = 17
fld public final static int BOGUS_COMMENT_HYPHEN = 64
fld public final static int BOGUS_DOCTYPE = 31
fld public final static int CDATA_RSQB = 57
fld public final static int CDATA_RSQB_RSQB = 58
fld public final static int CDATA_SECTION = 56
fld public final static int CDATA_START = 55
fld public final static int CHARACTER_REFERENCE_HILO_LOOKUP = 53
fld public final static int CHARACTER_REFERENCE_TAIL = 48
fld public final static int CLOSE_TAG_OPEN = 10
fld public final static int COMMENT = 34
fld public final static int COMMENT_END = 36
fld public final static int COMMENT_END_BANG = 37
fld public final static int COMMENT_END_DASH = 35
fld public final static int COMMENT_START = 32
fld public final static int COMMENT_START_DASH = 33
fld public final static int CONSUME_CHARACTER_REFERENCE = 46
fld public final static int CONSUME_NCR = 47
fld public final static int DATA = 0
fld public final static int DECIMAL_NRC_LOOP = 50
fld public final static int DOCTYPE = 19
fld public final static int DOCTYPE_NAME = 21
fld public final static int DOCTYPE_PUBLIC_IDENTIFIER_DOUBLE_QUOTED = 24
fld public final static int DOCTYPE_PUBLIC_IDENTIFIER_SINGLE_QUOTED = 25
fld public final static int DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED = 28
fld public final static int DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED = 29
fld public final static int DOCTYPE_UBLIC = 41
fld public final static int DOCTYPE_YSTEM = 42
fld public final static int HANDLE_NCR_VALUE = 51
fld public final static int HANDLE_NCR_VALUE_RECONSUME = 52
fld public final static int HEX_NCR_LOOP = 49
fld public final static int MARKUP_DECLARATION_HYPHEN = 39
fld public final static int MARKUP_DECLARATION_OCTYPE = 40
fld public final static int MARKUP_DECLARATION_OPEN = 18
fld public final static int NON_DATA_END_TAG_NAME = 38
fld public final static int PLAINTEXT = 8
fld public final static int PROCESSING_INSTRUCTION = 73
fld public final static int PROCESSING_INSTRUCTION_QUESTION_MARK = 74
fld public final static int RAWTEXT = 3
fld public final static int RAWTEXT_RCDATA_LESS_THAN_SIGN = 65
fld public final static int RCDATA = 1
fld public final static int SCRIPT_DATA = 2
fld public final static int SCRIPT_DATA_DOUBLE_ESCAPED = 68
fld public final static int SCRIPT_DATA_DOUBLE_ESCAPED_DASH = 70
fld public final static int SCRIPT_DATA_DOUBLE_ESCAPED_DASH_DASH = 71
fld public final static int SCRIPT_DATA_DOUBLE_ESCAPED_LESS_THAN_SIGN = 69
fld public final static int SCRIPT_DATA_DOUBLE_ESCAPE_END = 72
fld public final static int SCRIPT_DATA_DOUBLE_ESCAPE_START = 67
fld public final static int SCRIPT_DATA_ESCAPED = 4
fld public final static int SCRIPT_DATA_ESCAPED_DASH = 62
fld public final static int SCRIPT_DATA_ESCAPED_DASH_DASH = 63
fld public final static int SCRIPT_DATA_ESCAPED_LESS_THAN_SIGN = 66
fld public final static int SCRIPT_DATA_ESCAPE_START = 60
fld public final static int SCRIPT_DATA_ESCAPE_START_DASH = 61
fld public final static int SCRIPT_DATA_LESS_THAN_SIGN = 59
fld public final static int SELF_CLOSING_START_TAG = 54
fld public final static int TAG_NAME = 11
fld public final static int TAG_OPEN = 9
intf org.xml.sax.Locator
meth protected char checkChar(char[],int) throws org.xml.sax.SAXException
meth protected char errNcrControlChar(char) throws org.xml.sax.SAXException
meth protected char errNcrNonCharacter(char) throws org.xml.sax.SAXException
meth protected int transition(int,int,boolean,int) throws org.xml.sax.SAXException
meth protected java.lang.String strBufToString()
meth protected void errAstralNonCharacter(int) throws org.xml.sax.SAXException
meth protected void errAttributeValueMissing() throws org.xml.sax.SAXException
meth protected void errBadCharAfterLt(char) throws org.xml.sax.SAXException
meth protected void errBadCharBeforeAttributeNameOrNull(char) throws org.xml.sax.SAXException
meth protected void errBogusComment() throws org.xml.sax.SAXException
meth protected void errBogusDoctype() throws org.xml.sax.SAXException
meth protected void errCharRefLacksSemicolon() throws org.xml.sax.SAXException
meth protected void errConsecutiveHyphens() throws org.xml.sax.SAXException
meth protected void errDuplicateAttribute() throws org.xml.sax.SAXException
meth protected void errEofAfterLt() throws org.xml.sax.SAXException
meth protected void errEofInAttributeName() throws org.xml.sax.SAXException
meth protected void errEofInAttributeValue() throws org.xml.sax.SAXException
meth protected void errEofInComment() throws org.xml.sax.SAXException
meth protected void errEofInDoctype() throws org.xml.sax.SAXException
meth protected void errEofInEndTag() throws org.xml.sax.SAXException
meth protected void errEofInPublicId() throws org.xml.sax.SAXException
meth protected void errEofInSystemId() throws org.xml.sax.SAXException
meth protected void errEofInTagName() throws org.xml.sax.SAXException
meth protected void errEofWithoutGt() throws org.xml.sax.SAXException
meth protected void errEqualsSignBeforeAttributeName() throws org.xml.sax.SAXException
meth protected void errExpectedPublicId() throws org.xml.sax.SAXException
meth protected void errExpectedSystemId() throws org.xml.sax.SAXException
meth protected void errGarbageAfterLtSlash() throws org.xml.sax.SAXException
meth protected void errGtInPublicId() throws org.xml.sax.SAXException
meth protected void errGtInSystemId() throws org.xml.sax.SAXException
meth protected void errHtml4LtSlashInRcdata(char) throws org.xml.sax.SAXException
meth protected void errHtml4NonNameInUnquotedAttribute(char) throws org.xml.sax.SAXException
meth protected void errHtml4XmlVoidSyntax() throws org.xml.sax.SAXException
meth protected void errHyphenHyphenBang() throws org.xml.sax.SAXException
meth protected void errLtGt() throws org.xml.sax.SAXException
meth protected void errLtOrEqualsOrGraveInUnquotedAttributeOrNull(char) throws org.xml.sax.SAXException
meth protected void errLtSlashGt() throws org.xml.sax.SAXException
meth protected void errMissingSpaceBeforeDoctypeName() throws org.xml.sax.SAXException
meth protected void errNamelessDoctype() throws org.xml.sax.SAXException
meth protected void errNcrControlChar() throws org.xml.sax.SAXException
meth protected void errNcrCr() throws org.xml.sax.SAXException
meth protected void errNcrInC1Range() throws org.xml.sax.SAXException
meth protected void errNcrOutOfRange() throws org.xml.sax.SAXException
meth protected void errNcrSurrogate() throws org.xml.sax.SAXException
meth protected void errNcrUnassigned() throws org.xml.sax.SAXException
meth protected void errNcrZero() throws org.xml.sax.SAXException
meth protected void errNoDigitsInNCR() throws org.xml.sax.SAXException
meth protected void errNoNamedCharacterMatch() throws org.xml.sax.SAXException
meth protected void errNoSpaceBetweenAttributes() throws org.xml.sax.SAXException
meth protected void errNoSpaceBetweenDoctypePublicKeywordAndQuote() throws org.xml.sax.SAXException
meth protected void errNoSpaceBetweenDoctypeSystemKeywordAndQuote() throws org.xml.sax.SAXException
meth protected void errNoSpaceBetweenPublicAndSystemIds() throws org.xml.sax.SAXException
meth protected void errNotSemicolonTerminated() throws org.xml.sax.SAXException
meth protected void errPrematureEndOfComment() throws org.xml.sax.SAXException
meth protected void errProcessingInstruction() throws org.xml.sax.SAXException
meth protected void errQuoteBeforeAttributeName(char) throws org.xml.sax.SAXException
meth protected void errQuoteOrLtInAttributeNameOrNull(char) throws org.xml.sax.SAXException
meth protected void errSlashNotFollowedByGt() throws org.xml.sax.SAXException
meth protected void errUnescapedAmpersandInterpretedAsCharacterReference() throws org.xml.sax.SAXException
meth protected void errUnquotedAttributeValOrNull(char) throws org.xml.sax.SAXException
meth protected void errWarnLtSlashInRcdata() throws org.xml.sax.SAXException
meth protected void flushChars(char[],int) throws org.xml.sax.SAXException
meth protected void maybeErrAttributesOnEndTag(nu.validator.htmlparser.impl.HtmlAttributes) throws org.xml.sax.SAXException
meth protected void maybeErrSlashInEndTag(boolean) throws org.xml.sax.SAXException
meth protected void maybeWarnPrivateUse(char) throws org.xml.sax.SAXException
meth protected void maybeWarnPrivateUseAstral() throws org.xml.sax.SAXException
meth protected void noteAttributeWithoutValue() throws org.xml.sax.SAXException
meth protected void noteUnquotedAttributeValue() throws org.xml.sax.SAXException
meth protected void silentCarriageReturn()
meth protected void silentLineFeed()
meth protected void startErrorReporting() throws org.xml.sax.SAXException
meth public boolean internalEncodingDeclaration(java.lang.String) throws org.xml.sax.SAXException
meth public boolean isInDataState()
meth public boolean isMappingLangToXmlLang()
meth public boolean isNextCharOnNewLine()
meth public boolean isPrevCR()
meth public boolean tokenizeBuffer(nu.validator.htmlparser.impl.UTF16Buffer) throws org.xml.sax.SAXException
meth public int getCol()
meth public int getColumnNumber()
meth public int getLine()
meth public int getLineNumber()
meth public java.lang.String getPublicId()
meth public java.lang.String getSystemId()
meth public org.xml.sax.ErrorHandler getErrorHandler()
meth public void becomeConfident()
meth public void end() throws org.xml.sax.SAXException
meth public void eof() throws org.xml.sax.SAXException
meth public void err(java.lang.String) throws org.xml.sax.SAXException
meth public void errTreeBuilder(java.lang.String) throws org.xml.sax.SAXException
meth public void fatal(java.lang.String) throws org.xml.sax.SAXException
meth public void initLocation(java.lang.String,java.lang.String)
meth public void initializeWithoutStarting() throws org.xml.sax.SAXException
meth public void loadState(nu.validator.htmlparser.impl.Tokenizer) throws org.xml.sax.SAXException
meth public void notifyAboutMetaBoundary()
meth public void requestSuspension()
meth public void resetToDataState()
meth public void setCommentPolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setContentNonXmlCharPolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setContentSpacePolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setEncodingDeclarationHandler(nu.validator.htmlparser.common.EncodingDeclarationHandler)
meth public void setErrorHandler(org.xml.sax.ErrorHandler)
meth public void setHtml4ModeCompatibleWithXhtml1Schemata(boolean)
meth public void setInterner(nu.validator.htmlparser.common.Interner)
meth public void setLineNumber(int)
meth public void setMappingLangToXmlLang(boolean)
meth public void setNamePolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setStateAndEndTagExpectation(int,java.lang.String)
meth public void setStateAndEndTagExpectation(int,nu.validator.htmlparser.impl.ElementName)
meth public void setTransitionBaseOffset(int)
meth public void setXmlnsPolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void start() throws org.xml.sax.SAXException
meth public void warn(java.lang.String) throws org.xml.sax.SAXException
supr java.lang.Object
hfds CDATA_LSQB,DATA_AND_RCDATA_MASK,IFRAME_ARR,LEAD_OFFSET,LF,LT_GT,LT_SOLIDUS,NOEMBED_ARR,NOFRAMES_ARR,NOSCRIPT_ARR,OCTYPE,PLAINTEXT_ARR,REPLACEMENT_CHARACTER,RSQB_RSQB,SCRIPT_ARR,SPACE,STYLE_ARR,TEXTAREA_ARR,TITLE_ARR,UBLIC,XMP_ARR,YSTEM,additional,astralChar,attributes,bmpChar,candidate,charRefBuf,charRefBufLen,charRefBufMark,commentPolicy,containsHyphen,contentSpacePolicy,doctypeName,endTagExpectationAsArray,entCol,firstCharKey,forceQuirks,hi,html4ModeCompatibleWithXhtml1Schemata,interner,line,lo,mappingLangToXmlLang,metaBoundaryPassed,namePolicy,newAttributesEachTime,nonInternedTagName,publicId,publicIdentifier,returnStateSave,seenDigits,shouldSuspend,strBuf,strBufLen,systemId,systemIdentifier,tagName,wantsComments,xmlnsPolicy

CLSS public abstract nu.validator.htmlparser.impl.TreeBuilder<%0 extends java.lang.Object>
cons protected init()
fld protected char[] charBuffer
 anno 0 nu.validator.htmlparser.annotation.Auto()
fld protected int charBufferLen
fld protected nu.validator.htmlparser.impl.Tokenizer tokenizer
fld protected org.xml.sax.ErrorHandler errorHandler
intf nu.validator.htmlparser.common.TokenHandler
intf nu.validator.htmlparser.impl.TreeBuilderState<{nu.validator.htmlparser.impl.TreeBuilder%0}>
meth protected abstract boolean hasChildren({nu.validator.htmlparser.impl.TreeBuilder%0}) throws org.xml.sax.SAXException
meth protected abstract void addAttributesToElement({nu.validator.htmlparser.impl.TreeBuilder%0},nu.validator.htmlparser.impl.HtmlAttributes) throws org.xml.sax.SAXException
meth protected abstract void appendCharacters({nu.validator.htmlparser.impl.TreeBuilder%0},char[],int,int) throws org.xml.sax.SAXException
 anno 2 nu.validator.htmlparser.annotation.NoLength()
meth protected abstract void appendChildrenToNewParent({nu.validator.htmlparser.impl.TreeBuilder%0},{nu.validator.htmlparser.impl.TreeBuilder%0}) throws org.xml.sax.SAXException
meth protected abstract void appendComment({nu.validator.htmlparser.impl.TreeBuilder%0},char[],int,int) throws org.xml.sax.SAXException
 anno 2 nu.validator.htmlparser.annotation.NoLength()
meth protected abstract void appendCommentToDocument(char[],int,int) throws org.xml.sax.SAXException
 anno 1 nu.validator.htmlparser.annotation.NoLength()
meth protected abstract void appendElement({nu.validator.htmlparser.impl.TreeBuilder%0},{nu.validator.htmlparser.impl.TreeBuilder%0}) throws org.xml.sax.SAXException
meth protected abstract void detachFromParent({nu.validator.htmlparser.impl.TreeBuilder%0}) throws org.xml.sax.SAXException
meth protected abstract void insertFosterParentedCharacters(char[],int,int,{nu.validator.htmlparser.impl.TreeBuilder%0},{nu.validator.htmlparser.impl.TreeBuilder%0}) throws org.xml.sax.SAXException
 anno 1 nu.validator.htmlparser.annotation.NoLength()
meth protected abstract void insertFosterParentedChild({nu.validator.htmlparser.impl.TreeBuilder%0},{nu.validator.htmlparser.impl.TreeBuilder%0},{nu.validator.htmlparser.impl.TreeBuilder%0}) throws org.xml.sax.SAXException
meth protected abstract {nu.validator.htmlparser.impl.TreeBuilder%0} createAndInsertFosterParentedElement(java.lang.String,java.lang.String,nu.validator.htmlparser.impl.HtmlAttributes,{nu.validator.htmlparser.impl.TreeBuilder%0},{nu.validator.htmlparser.impl.TreeBuilder%0}) throws org.xml.sax.SAXException
 anno 1 nu.validator.htmlparser.annotation.NsUri()
 anno 2 nu.validator.htmlparser.annotation.Local()
meth protected abstract {nu.validator.htmlparser.impl.TreeBuilder%0} createElement(java.lang.String,java.lang.String,nu.validator.htmlparser.impl.HtmlAttributes,{nu.validator.htmlparser.impl.TreeBuilder%0}) throws org.xml.sax.SAXException
 anno 1 nu.validator.htmlparser.annotation.NsUri()
 anno 2 nu.validator.htmlparser.annotation.Local()
meth protected abstract {nu.validator.htmlparser.impl.TreeBuilder%0} createHtmlElementSetAsRoot(nu.validator.htmlparser.impl.HtmlAttributes) throws org.xml.sax.SAXException
meth protected final void fatal(java.lang.Exception) throws org.xml.sax.SAXException
meth protected final void requestSuspension()
meth protected final {nu.validator.htmlparser.impl.TreeBuilder%0} currentNode()
meth protected void accumulateCharacters(char[],int,int) throws org.xml.sax.SAXException
 anno 1 nu.validator.htmlparser.annotation.Const()
 anno 1 nu.validator.htmlparser.annotation.NoLength()
meth protected void appendDoctypeToDocument(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
 anno 1 nu.validator.htmlparser.annotation.Local()
meth protected void documentMode(nu.validator.htmlparser.common.DocumentMode,java.lang.String,java.lang.String,boolean) throws org.xml.sax.SAXException
meth protected void elementPopped(java.lang.String,java.lang.String,{nu.validator.htmlparser.impl.TreeBuilder%0}) throws org.xml.sax.SAXException
 anno 1 nu.validator.htmlparser.annotation.NsUri()
 anno 2 nu.validator.htmlparser.annotation.Local()
meth protected void elementPushed(java.lang.String,java.lang.String,{nu.validator.htmlparser.impl.TreeBuilder%0}) throws org.xml.sax.SAXException
 anno 1 nu.validator.htmlparser.annotation.NsUri()
 anno 2 nu.validator.htmlparser.annotation.Local()
meth protected void end() throws org.xml.sax.SAXException
meth protected void fatal() throws org.xml.sax.SAXException
meth protected void markMalformedIfScript({nu.validator.htmlparser.impl.TreeBuilder%0}) throws org.xml.sax.SAXException
meth protected void start(boolean) throws org.xml.sax.SAXException
meth protected {nu.validator.htmlparser.impl.TreeBuilder%0} createAndInsertFosterParentedElement(java.lang.String,java.lang.String,nu.validator.htmlparser.impl.HtmlAttributes,{nu.validator.htmlparser.impl.TreeBuilder%0},{nu.validator.htmlparser.impl.TreeBuilder%0},{nu.validator.htmlparser.impl.TreeBuilder%0}) throws org.xml.sax.SAXException
 anno 1 nu.validator.htmlparser.annotation.NsUri()
 anno 2 nu.validator.htmlparser.annotation.Local()
meth protected {nu.validator.htmlparser.impl.TreeBuilder%0} createElement(java.lang.String,java.lang.String,nu.validator.htmlparser.impl.HtmlAttributes,{nu.validator.htmlparser.impl.TreeBuilder%0},{nu.validator.htmlparser.impl.TreeBuilder%0}) throws org.xml.sax.SAXException
 anno 1 nu.validator.htmlparser.annotation.NsUri()
 anno 2 nu.validator.htmlparser.annotation.Local()
meth public boolean cdataSectionAllowed() throws org.xml.sax.SAXException
 anno 0 nu.validator.htmlparser.annotation.Inline()
meth public boolean isFramesetOk()
meth public boolean isNeedToDropLF()
meth public boolean isQuirks()
meth public boolean isScriptingEnabled()
meth public boolean snapshotMatches(nu.validator.htmlparser.impl.TreeBuilderState<{nu.validator.htmlparser.impl.TreeBuilder%0}>)
meth public boolean wantsComments()
meth public final void characters(char[],int,int) throws org.xml.sax.SAXException
 anno 1 nu.validator.htmlparser.annotation.Const()
 anno 1 nu.validator.htmlparser.annotation.NoLength()
meth public final void comment(char[],int,int) throws org.xml.sax.SAXException
 anno 1 nu.validator.htmlparser.annotation.NoLength()
meth public final void doctype(java.lang.String,java.lang.String,java.lang.String,boolean) throws org.xml.sax.SAXException
 anno 1 nu.validator.htmlparser.annotation.Local()
meth public final void endTokenization() throws org.xml.sax.SAXException
meth public final void eof() throws org.xml.sax.SAXException
meth public final void flushCharacters() throws org.xml.sax.SAXException
meth public final void setErrorHandler(org.xml.sax.ErrorHandler)
meth public final void setFragmentContext(java.lang.String)
 anno 1 nu.validator.htmlparser.annotation.Local()
meth public final void setFragmentContext(java.lang.String,java.lang.String,{nu.validator.htmlparser.impl.TreeBuilder%0},boolean)
 anno 1 nu.validator.htmlparser.annotation.Local()
 anno 2 nu.validator.htmlparser.annotation.NsUri()
meth public final void startTokenization(nu.validator.htmlparser.impl.Tokenizer) throws org.xml.sax.SAXException
meth public int getListOfActiveFormattingElementsLength()
meth public int getMode()
meth public int getOriginalMode()
meth public int getStackLength()
meth public int getTemplateModeStackLength()
meth public int[] getTemplateModeStack()
meth public nu.validator.htmlparser.impl.StackNode<{nu.validator.htmlparser.impl.TreeBuilder%0}>[] getListOfActiveFormattingElements()
meth public nu.validator.htmlparser.impl.StackNode<{nu.validator.htmlparser.impl.TreeBuilder%0}>[] getStack()
meth public nu.validator.htmlparser.impl.TreeBuilderState<{nu.validator.htmlparser.impl.TreeBuilder%0}> newSnapshot() throws org.xml.sax.SAXException
meth public org.xml.sax.ErrorHandler getErrorHandler()
meth public static java.lang.String extractCharsetFromContent(java.lang.String)
meth public void endTag(nu.validator.htmlparser.impl.ElementName) throws org.xml.sax.SAXException
meth public void ensureBufferSpace(int) throws org.xml.sax.SAXException
meth public void loadState(nu.validator.htmlparser.impl.TreeBuilderState<{nu.validator.htmlparser.impl.TreeBuilder%0}>) throws org.xml.sax.SAXException
meth public void setDoctypeExpectation(nu.validator.htmlparser.common.DoctypeExpectation)
meth public void setDocumentModeHandler(nu.validator.htmlparser.common.DocumentModeHandler)
meth public void setIgnoringComments(boolean)
meth public void setIsSrcdocDocument(boolean)
meth public void setNamePolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setReportingDoctype(boolean)
meth public void setScriptingEnabled(boolean)
meth public void startTag(nu.validator.htmlparser.impl.ElementName,nu.validator.htmlparser.impl.HtmlAttributes,boolean) throws org.xml.sax.SAXException
meth public void zeroOriginatingReplacementCharacter() throws org.xml.sax.SAXException
meth public {nu.validator.htmlparser.impl.TreeBuilder%0} getFormPointer()
meth public {nu.validator.htmlparser.impl.TreeBuilder%0} getHeadPointer()
supr java.lang.Object
hfds A,ADDRESS_OR_ARTICLE_OR_ASIDE_OR_DETAILS_OR_DIALOG_OR_DIR_OR_FIGCAPTION_OR_FIGURE_OR_FOOTER_OR_HEADER_OR_HGROUP_OR_MAIN_OR_NAV_OR_SECTION_OR_SUMMARY,AFTER_AFTER_BODY,AFTER_AFTER_FRAMESET,AFTER_BODY,AFTER_FRAMESET,AFTER_HEAD,ANNOTATION_XML,AREA_OR_WBR,BASE,BEFORE_HEAD,BEFORE_HTML,BODY,BR,BUTTON,B_OR_BIG_OR_CODE_OR_EM_OR_I_OR_S_OR_SMALL_OR_STRIKE_OR_STRONG_OR_TT_OR_U,CAPTION,CHARSET_A,CHARSET_C,CHARSET_DOUBLE_QUOTED,CHARSET_E,CHARSET_EQUALS,CHARSET_H,CHARSET_INITIAL,CHARSET_R,CHARSET_S,CHARSET_SINGLE_QUOTED,CHARSET_T,CHARSET_UNQUOTED,COL,COLGROUP,DD_OR_DT,DIV_OR_BLOCKQUOTE_OR_CENTER_OR_MENU,EMBED,FIELDSET,FONT,FOREIGNOBJECT_OR_DESC,FORM,FRAME,FRAMESET,FRAMESET_OK,H1_OR_H2_OR_H3_OR_H4_OR_H5_OR_H6,HEAD,HR,HTML,HTML4_PUBLIC_IDS,HTML_LOCAL,IFRAME,IMAGE,IMG,INITIAL,INPUT,IN_BODY,IN_CAPTION,IN_CELL,IN_COLUMN_GROUP,IN_FRAMESET,IN_HEAD,IN_HEAD_NOSCRIPT,IN_ROW,IN_SELECT,IN_SELECT_IN_TABLE,IN_TABLE,IN_TABLE_BODY,IN_TEMPLATE,KEYGEN,LI,LINK_OR_BASEFONT_OR_BGSOUND,MARQUEE_OR_APPLET,MATH,MENUITEM,META,MGLYPH_OR_MALIGNMARK,MI_MO_MN_MS_MTEXT,NOBR,NOEMBED,NOFRAMES,NOSCRIPT,NOT_FOUND_ON_STACK,OBJECT,OPTGROUP,OPTION,OTHER,OUTPUT,P,PARAM_OR_SOURCE_OR_TRACK,PLAINTEXT,PRE_OR_LISTING,QUIRKY_PUBLIC_IDS,RB_OR_RTC,REPLACEMENT_CHARACTER,RT_OR_RP,RUBY_OR_SPAN_OR_SUB_OR_SUP_OR_VAR,SCRIPT,SELECT,STYLE,SVG,TABLE,TBODY_OR_THEAD_OR_TFOOT,TD_OR_TH,TEMPLATE,TEXT,TEXTAREA,TITLE,TR,UL_OR_OL_OR_DL,XMP,contextName,contextNamespace,contextNode,currentPtr,doctypeExpectation,documentModeHandler,firstCommentLocation,formPointer,fragment,framesetOk,headPointer,html4,idLocations,isSrcdocDocument,listOfActiveFormattingElements,listPtr,mode,namePolicy,needToDropLF,numStackNodes,originalMode,quirks,reportingDoctype,scriptingEnabled,stack,stackNodes,stackNodesIdx,templateModePtr,templateModeStack,wantingComments

CLSS public abstract interface nu.validator.htmlparser.impl.TreeBuilderState<%0 extends java.lang.Object>
meth public abstract boolean isFramesetOk()
meth public abstract boolean isNeedToDropLF()
meth public abstract boolean isQuirks()
meth public abstract int getListOfActiveFormattingElementsLength()
meth public abstract int getMode()
meth public abstract int getOriginalMode()
meth public abstract int getStackLength()
meth public abstract int getTemplateModeStackLength()
meth public abstract int[] getTemplateModeStack()
meth public abstract nu.validator.htmlparser.impl.StackNode<{nu.validator.htmlparser.impl.TreeBuilderState%0}>[] getListOfActiveFormattingElements()
meth public abstract nu.validator.htmlparser.impl.StackNode<{nu.validator.htmlparser.impl.TreeBuilderState%0}>[] getStack()
meth public abstract {nu.validator.htmlparser.impl.TreeBuilderState%0} getFormPointer()
meth public abstract {nu.validator.htmlparser.impl.TreeBuilderState%0} getHeadPointer()

CLSS public final nu.validator.htmlparser.impl.UTF16Buffer
cons public init(char[],int,int)
meth public boolean hasMore()
meth public char[] getBuffer()
meth public int getEnd()
meth public int getLength()
meth public int getStart()
meth public void adjust(boolean)
meth public void setEnd(int)
meth public void setStart(int)
supr java.lang.Object
hfds buffer,end,start

CLSS public final nu.validator.htmlparser.io.BomSniffer
cons public init(nu.validator.htmlparser.common.ByteReadable)
supr java.lang.Object
hfds source

CLSS public final !enum nu.validator.htmlparser.io.Confidence
fld public final static nu.validator.htmlparser.io.Confidence CERTAIN
fld public final static nu.validator.htmlparser.io.Confidence TENTATIVE
meth public static nu.validator.htmlparser.io.Confidence valueOf(java.lang.String)
meth public static nu.validator.htmlparser.io.Confidence[] values()
supr java.lang.Enum<nu.validator.htmlparser.io.Confidence>

CLSS public nu.validator.htmlparser.io.Driver
cons public init(nu.validator.htmlparser.impl.Tokenizer)
intf nu.validator.htmlparser.common.EncodingDeclarationHandler
meth protected nu.validator.htmlparser.io.Encoding encodingFromExternalDeclaration(java.lang.String) throws org.xml.sax.SAXException
meth protected nu.validator.htmlparser.io.Encoding whineAboutEncodingAndReturnActual(java.lang.String,nu.validator.htmlparser.io.Encoding) throws org.xml.sax.SAXException
meth protected void warnWithoutLocation(java.lang.String) throws org.xml.sax.SAXException
meth public boolean internalEncodingDeclaration(java.lang.String) throws org.xml.sax.SAXException
meth public boolean isAllowRewinding()
meth public boolean isCheckingNormalization()
meth public java.lang.String getCharacterEncoding() throws org.xml.sax.SAXException
meth public org.xml.sax.Locator getDocumentLocator()
meth public void addCharacterHandler(nu.validator.htmlparser.common.CharacterHandler)
meth public void setAllowRewinding(boolean)
meth public void setCheckingNormalization(boolean)
meth public void setCommentPolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setContentNonXmlCharPolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setContentSpacePolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setEncoding(nu.validator.htmlparser.io.Encoding,nu.validator.htmlparser.io.Confidence)
meth public void setErrorHandler(org.xml.sax.ErrorHandler)
meth public void setHeuristics(nu.validator.htmlparser.common.Heuristics)
meth public void setHtml4ModeCompatibleWithXhtml1Schemata(boolean)
meth public void setMappingLangToXmlLang(boolean)
meth public void setNamePolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setTransitionHandler(nu.validator.htmlparser.common.TransitionHandler)
meth public void setXmlnsPolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void tokenize(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
supr java.lang.Object
hfds allowRewinding,characterEncoding,characterHandlers,confidence,heuristics,reader,rewindableInputStream,swallowBom,tokenizer
hcls ReparseException

CLSS public nu.validator.htmlparser.io.Encoding
fld public final static nu.validator.htmlparser.io.Encoding UTF16
fld public final static nu.validator.htmlparser.io.Encoding UTF16BE
fld public final static nu.validator.htmlparser.io.Encoding UTF16LE
fld public final static nu.validator.htmlparser.io.Encoding UTF8
fld public final static nu.validator.htmlparser.io.Encoding WINDOWS1252
meth public boolean canEncode()
meth public boolean isAsciiSuperset()
meth public boolean isLikelyEbcdic()
meth public boolean isObscure()
meth public boolean isRegistered()
meth public boolean isShouldNot()
meth public java.lang.String getCanonName()
meth public java.nio.charset.CharsetDecoder newDecoder()
meth public java.nio.charset.CharsetEncoder newEncoder()
meth public nu.validator.htmlparser.io.Encoding getActualHtmlEncoding()
meth public static java.lang.String toAsciiLowerCase(java.lang.String)
meth public static java.lang.String toNameKey(java.lang.String)
meth public static nu.validator.htmlparser.io.Encoding forName(java.lang.String)
meth public static void main(java.lang.String[])
supr java.lang.Object
hfds BANNED,NOT_OBSCURE,SHOULD_NOT,actualHtmlEncoding,asciiSuperset,canonName,charset,encodingByCookedName,likelyEbcdic,obscure,shouldNot

CLSS public final nu.validator.htmlparser.io.HtmlInputStreamReader
cons public init(java.io.InputStream,org.xml.sax.ErrorHandler,nu.validator.htmlparser.impl.Tokenizer,nu.validator.htmlparser.io.Driver,nu.validator.htmlparser.common.Heuristics) throws java.io.IOException,org.xml.sax.SAXException
cons public init(java.io.InputStream,org.xml.sax.ErrorHandler,nu.validator.htmlparser.impl.Tokenizer,nu.validator.htmlparser.io.Driver,nu.validator.htmlparser.io.Encoding) throws java.io.IOException,org.xml.sax.SAXException
intf nu.validator.htmlparser.common.ByteReadable
intf org.xml.sax.Locator
meth public int getColumnNumber()
meth public int getLineNumber()
meth public int read() throws java.io.IOException
meth public int read(char[]) throws java.io.IOException
meth public int read(char[],int,int) throws java.io.IOException
meth public int read(java.nio.CharBuffer) throws java.io.IOException
meth public int readByte() throws java.io.IOException
meth public java.lang.String getPublicId()
meth public java.lang.String getSystemId()
meth public java.nio.charset.Charset getCharset()
meth public static void main(java.lang.String[])
meth public void close() throws java.io.IOException
meth public void switchEncoding(nu.validator.htmlparser.io.Encoding)
supr java.io.Reader
hfds SNIFFING_LIMIT,byteArray,byteBuffer,bytesRead,charsetBoundaryPassed,col,decoder,driver,eofSeen,errorHandler,flushing,hasPendingReplacementCharacter,inputStream,limit,line,lineColPos,needToNotifyTokenizer,nextCharOnNewLine,position,prevWasCR,shouldReadBytes,sniffing,tokenizer

CLSS public nu.validator.htmlparser.io.MetaSniffer
cons public init(org.xml.sax.ErrorHandler,org.xml.sax.Locator)
intf org.xml.sax.Locator
meth protected boolean tryCharset(java.lang.String) throws org.xml.sax.SAXException
meth protected int read() throws java.io.IOException
meth public int getColumnNumber()
meth public int getLineNumber()
meth public java.lang.String getPublicId()
meth public java.lang.String getSystemId()
meth public nu.validator.htmlparser.io.Encoding sniff(nu.validator.htmlparser.common.ByteReadable) throws java.io.IOException,org.xml.sax.SAXException
supr nu.validator.htmlparser.impl.MetaScanner
hfds characterEncoding,col,errorHandler,line,locator,prevWasCR

CLSS public abstract interface nu.validator.htmlparser.rewindable.Rewindable
meth public abstract boolean canRewind()
meth public abstract void rewind()
meth public abstract void willNotRewind()

CLSS public nu.validator.htmlparser.rewindable.RewindableInputStream
cons public init(java.io.InputStream)
intf nu.validator.htmlparser.rewindable.Rewindable
meth public boolean canRewind()
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void rewind()
meth public void willNotRewind()
supr java.io.InputStream
hfds curBlock,curBlockAvail,curBlockPos,eof,head,in,lastBlock,pretendClosed,saving
hcls Block

CLSS public nu.validator.htmlparser.sax.HtmlParser
cons public init()
cons public init(nu.validator.htmlparser.common.XmlViolationPolicy)
intf org.xml.sax.XMLReader
meth public boolean getFeature(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public boolean isCheckingNormalization()
meth public boolean isHtml4ModeCompatibleWithXhtml1Schemata()
meth public boolean isMappingLangToXmlLang()
meth public boolean isReportingDoctype()
meth public boolean isScriptingEnabled()
meth public java.lang.Object getProperty(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public nu.validator.htmlparser.common.DoctypeExpectation getDoctypeExpectation()
meth public nu.validator.htmlparser.common.DocumentModeHandler getDocumentModeHandler()
meth public nu.validator.htmlparser.common.Heuristics getHeuristics()
meth public nu.validator.htmlparser.common.XmlViolationPolicy getBogusXmlnsPolicy()
meth public nu.validator.htmlparser.common.XmlViolationPolicy getCommentPolicy()
meth public nu.validator.htmlparser.common.XmlViolationPolicy getContentNonXmlCharPolicy()
meth public nu.validator.htmlparser.common.XmlViolationPolicy getContentSpacePolicy()
meth public nu.validator.htmlparser.common.XmlViolationPolicy getNamePolicy()
meth public nu.validator.htmlparser.common.XmlViolationPolicy getStreamabilityViolationPolicy()
meth public nu.validator.htmlparser.common.XmlViolationPolicy getXmlnsPolicy()
meth public org.xml.sax.ContentHandler getContentHandler()
meth public org.xml.sax.DTDHandler getDTDHandler()
meth public org.xml.sax.EntityResolver getEntityResolver()
meth public org.xml.sax.ErrorHandler getErrorHandler()
meth public org.xml.sax.Locator getDocumentLocator()
meth public org.xml.sax.ext.LexicalHandler getLexicalHandler()
meth public void addCharacterHandler(nu.validator.htmlparser.common.CharacterHandler)
meth public void parse(java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void parse(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public void parseFragment(org.xml.sax.InputSource,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void parseFragment(org.xml.sax.InputSource,java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void setBogusXmlnsPolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setCheckingNormalization(boolean)
meth public void setCommentPolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setContentHandler(org.xml.sax.ContentHandler)
meth public void setContentNonXmlCharPolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setContentSpacePolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setDTDHandler(org.xml.sax.DTDHandler)
meth public void setDoctypeExpectation(nu.validator.htmlparser.common.DoctypeExpectation)
meth public void setDocumentModeHandler(nu.validator.htmlparser.common.DocumentModeHandler)
meth public void setEntityResolver(org.xml.sax.EntityResolver)
meth public void setErrorHandler(org.xml.sax.ErrorHandler)
meth public void setErrorProfile(java.util.HashMap<java.lang.String,java.lang.String>)
meth public void setFeature(java.lang.String,boolean) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public void setHeuristics(nu.validator.htmlparser.common.Heuristics)
meth public void setHtml4ModeCompatibleWithXhtml1Schemata(boolean)
meth public void setLexicalHandler(org.xml.sax.ext.LexicalHandler)
meth public void setMappingLangToXmlLang(boolean)
meth public void setNamePolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setProperty(java.lang.String,java.lang.Object) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public void setReportingDoctype(boolean)
meth public void setScriptingEnabled(boolean)
meth public void setStreamabilityViolationPolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setTransitionHandler(nu.validator.htmlparser.common.TransitionHandler)
meth public void setTreeBuilderErrorHandlerOverride(org.xml.sax.ErrorHandler)
meth public void setXmlPolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
meth public void setXmlnsPolicy(nu.validator.htmlparser.common.XmlViolationPolicy)
supr java.lang.Object
hfds characterHandlers,checkingNormalization,commentPolicy,contentHandler,contentNonXmlCharPolicy,contentSpacePolicy,doctypeExpectation,documentModeHandler,driver,dtdHandler,entityResolver,errorHandler,errorProfileMap,heuristics,html4ModeCompatibleWithXhtml1Schemata,lexicalHandler,mappingLangToXmlLang,namePolicy,reportingDoctype,saxStreamer,saxTreeBuilder,scriptingEnabled,streamabilityViolationPolicy,transitionHandler,treeBuilder,treeBuilderErrorHandler,xmlnsPolicy

CLSS public nu.validator.htmlparser.sax.HtmlSerializer
cons public init(java.io.OutputStream)
cons public init(java.io.Writer)
intf org.xml.sax.ContentHandler
intf org.xml.sax.ext.LexicalHandler
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void comment(char[],int,int) throws org.xml.sax.SAXException
meth public void endCDATA() throws org.xml.sax.SAXException
meth public void endDTD() throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startCDATA() throws org.xml.sax.SAXException
meth public void startDTD(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
supr java.lang.Object
hfds NON_ESCAPING,VOID_ELEMENTS,escapeLevel,ignoreLevel,writer

CLSS public nu.validator.htmlparser.sax.InfosetCoercingHtmlParser
cons public init()
supr nu.validator.htmlparser.sax.HtmlParser

CLSS public nu.validator.htmlparser.sax.NameCheckingXmlSerializer
cons public init(java.io.OutputStream)
cons public init(java.io.Writer)
meth protected void checkNCName(java.lang.String) throws org.xml.sax.SAXException
supr nu.validator.htmlparser.sax.XmlSerializer

CLSS public nu.validator.htmlparser.sax.XmlSerializer
cons public init(java.io.OutputStream)
cons public init(java.io.Writer)
intf org.xml.sax.ContentHandler
intf org.xml.sax.ext.LexicalHandler
meth protected void checkNCName(java.lang.String) throws org.xml.sax.SAXException
meth public final void characters(char[],int,int) throws org.xml.sax.SAXException
meth public final void comment(char[],int,int) throws org.xml.sax.SAXException
meth public final void endCDATA() throws org.xml.sax.SAXException
meth public final void endDTD() throws org.xml.sax.SAXException
meth public final void endDocument() throws org.xml.sax.SAXException
meth public final void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public final void endEntity(java.lang.String) throws org.xml.sax.SAXException
meth public final void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public final void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public final void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public final void setDocumentLocator(org.xml.sax.Locator)
meth public final void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public final void startCDATA() throws org.xml.sax.SAXException
meth public final void startDTD(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public final void startDocument() throws org.xml.sax.SAXException
meth public final void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public final void startEntity(java.lang.String) throws org.xml.sax.SAXException
meth public final void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public final void startPrefixMappingPrivate(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
supr java.lang.Object
hfds WELL_KNOWN_ATTRIBUTE_PREFIXES,WELL_KNOWN_ELEMENT_PREFIXES,stack,writer
hcls PrefixMapping,StackNode

CLSS public abstract interface nu.validator.htmlparser.xom.FormPointer
meth public abstract nu.xom.Element getForm()
meth public abstract void setForm(nu.xom.Element)

CLSS public nu.validator.htmlparser.xom.FormPtrElement
hfds form

CLSS public nu.validator.htmlparser.xom.HtmlBuilder
hfds characterHandlers,checkingNormalization,commentPolicy,contentNonXmlCharPolicy,contentSpacePolicy,doctypeExpectation,documentModeHandler,driver,entityResolver,errorHandler,heuristics,html4ModeCompatibleWithXhtml1Schemata,mappingLangToXmlLang,namePolicy,reportingDoctype,scriptingEnabled,simpleNodeFactory,streamabilityViolationPolicy,transitionHandler,treeBuilder,treeBuilderErrorHandler,xmlnsPolicy

CLSS public nu.validator.htmlparser.xom.ModalDocument
hfds mode

CLSS public abstract interface nu.validator.htmlparser.xom.Mode
meth public abstract nu.validator.htmlparser.common.DocumentMode getMode()
meth public abstract void setMode(nu.validator.htmlparser.common.DocumentMode)

CLSS public nu.validator.htmlparser.xom.SimpleNodeFactory
cons public init()
meth public nu.xom.Attribute makeAttribute(java.lang.String,java.lang.String,java.lang.String,nu.xom.Attribute$Type)
meth public nu.xom.Comment makeComment(java.lang.String)
meth public nu.xom.Document makeDocument()
meth public nu.xom.Element makeElement(java.lang.String,java.lang.String)
meth public nu.xom.Element makeElement(java.lang.String,java.lang.String,nu.xom.Element)
meth public nu.xom.Text makeText(java.lang.String)
supr java.lang.Object

CLSS public final nu.validator.saxtree.CDATA
cons public init(org.xml.sax.Locator)
meth public nu.validator.saxtree.NodeType getNodeType()
supr nu.validator.saxtree.ParentNode

CLSS public abstract nu.validator.saxtree.CharBufferNode
fld protected final char[] buffer
meth public java.lang.String toString()
supr nu.validator.saxtree.Node

CLSS public final nu.validator.saxtree.Characters
cons public init(org.xml.sax.Locator,char[],int,int)
meth public nu.validator.saxtree.NodeType getNodeType()
supr nu.validator.saxtree.CharBufferNode

CLSS public final nu.validator.saxtree.Comment
cons public init(org.xml.sax.Locator,char[],int,int)
meth public nu.validator.saxtree.NodeType getNodeType()
supr nu.validator.saxtree.CharBufferNode

CLSS public final nu.validator.saxtree.DTD
cons public init(org.xml.sax.Locator,java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getName()
meth public java.lang.String getPublicIdentifier()
meth public java.lang.String getSystemIdentifier()
meth public nu.validator.saxtree.NodeType getNodeType()
supr nu.validator.saxtree.ParentNode
hfds name,publicIdentifier,systemIdentifier

CLSS public final nu.validator.saxtree.Document
cons public init(org.xml.sax.Locator)
meth public nu.validator.saxtree.NodeType getNodeType()
supr nu.validator.saxtree.ParentNode

CLSS public final nu.validator.saxtree.DocumentFragment
cons public init()
meth public nu.validator.saxtree.NodeType getNodeType()
supr nu.validator.saxtree.ParentNode

CLSS public final nu.validator.saxtree.Element
cons public init(org.xml.sax.Locator,java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes,boolean,java.util.List<nu.validator.saxtree.PrefixMapping>)
meth public java.lang.String getLocalName()
meth public java.lang.String getQName()
meth public java.lang.String getUri()
meth public java.util.List<nu.validator.saxtree.PrefixMapping> getPrefixMappings()
meth public nu.validator.saxtree.NodeType getNodeType()
meth public org.xml.sax.Attributes getAttributes()
supr nu.validator.saxtree.ParentNode
hfds attributes,localName,prefixMappings,qName,uri

CLSS public final nu.validator.saxtree.Entity
cons public init(org.xml.sax.Locator,java.lang.String)
meth public java.lang.String getName()
meth public nu.validator.saxtree.NodeType getNodeType()
supr nu.validator.saxtree.ParentNode
hfds name

CLSS public final nu.validator.saxtree.IgnorableWhitespace
cons public init(org.xml.sax.Locator,char[],int,int)
meth public nu.validator.saxtree.NodeType getNodeType()
supr nu.validator.saxtree.CharBufferNode

CLSS public final nu.validator.saxtree.LocatorImpl
cons public init(org.xml.sax.Locator)
intf org.xml.sax.Locator
meth public int getColumnNumber()
meth public int getLineNumber()
meth public java.lang.String getPublicId()
meth public java.lang.String getSystemId()
supr java.lang.Object
hfds column,line,publicId,systemId

CLSS public abstract nu.validator.saxtree.Node
intf org.xml.sax.Locator
meth public abstract nu.validator.saxtree.NodeType getNodeType()
meth public final nu.validator.saxtree.Node getNextSibling()
meth public final nu.validator.saxtree.Node getPreviousSibling()
meth public final nu.validator.saxtree.ParentNode getParentNode()
meth public int getColumnNumber()
meth public int getLineNumber()
meth public java.lang.String getData()
meth public java.lang.String getLocalName()
meth public java.lang.String getName()
meth public java.lang.String getPublicId()
meth public java.lang.String getPublicIdentifier()
meth public java.lang.String getQName()
meth public java.lang.String getSystemId()
meth public java.lang.String getSystemIdentifier()
meth public java.lang.String getTarget()
meth public java.lang.String getUri()
meth public java.util.List<nu.validator.saxtree.PrefixMapping> getPrefixMappings()
meth public nu.validator.saxtree.Node getFirstChild()
meth public org.xml.sax.Attributes getAttributes()
meth public void detach()
supr java.lang.Object
hfds column,line,nextSibling,parentNode,publicId,systemId

CLSS public final !enum nu.validator.saxtree.NodeType
fld public final static nu.validator.saxtree.NodeType CDATA
fld public final static nu.validator.saxtree.NodeType CHARACTERS
fld public final static nu.validator.saxtree.NodeType COMMENT
fld public final static nu.validator.saxtree.NodeType DOCUMENT
fld public final static nu.validator.saxtree.NodeType DOCUMENT_FRAGMENT
fld public final static nu.validator.saxtree.NodeType DTD
fld public final static nu.validator.saxtree.NodeType ELEMENT
fld public final static nu.validator.saxtree.NodeType ENTITY
fld public final static nu.validator.saxtree.NodeType IGNORABLE_WHITESPACE
fld public final static nu.validator.saxtree.NodeType PROCESSING_INSTRUCTION
fld public final static nu.validator.saxtree.NodeType SKIPPED_ENTITY
meth public static nu.validator.saxtree.NodeType valueOf(java.lang.String)
meth public static nu.validator.saxtree.NodeType[] values()
supr java.lang.Enum<nu.validator.saxtree.NodeType>

CLSS public abstract nu.validator.saxtree.ParentNode
fld protected org.xml.sax.Locator endLocator
meth public final nu.validator.saxtree.Node getFirstChild()
meth public final nu.validator.saxtree.Node getLastChild()
meth public nu.validator.saxtree.Node appendChild(nu.validator.saxtree.Node)
meth public nu.validator.saxtree.Node insertBefore(nu.validator.saxtree.Node,nu.validator.saxtree.Node)
meth public nu.validator.saxtree.Node insertBetween(nu.validator.saxtree.Node,nu.validator.saxtree.Node,nu.validator.saxtree.Node)
meth public void appendChildren(nu.validator.saxtree.Node)
meth public void copyEndLocator(nu.validator.saxtree.ParentNode)
meth public void setEndLocator(org.xml.sax.Locator)
supr nu.validator.saxtree.Node
hfds firstChild,lastChild

CLSS public final nu.validator.saxtree.PrefixMapping
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getPrefix()
meth public java.lang.String getUri()
supr java.lang.Object
hfds prefix,uri

CLSS public final nu.validator.saxtree.ProcessingInstruction
cons public init(org.xml.sax.Locator,java.lang.String,java.lang.String)
meth public java.lang.String getData()
meth public java.lang.String getTarget()
meth public nu.validator.saxtree.NodeType getNodeType()
supr nu.validator.saxtree.Node
hfds data,target

CLSS public final nu.validator.saxtree.SkippedEntity
cons public init(org.xml.sax.Locator,java.lang.String)
meth public java.lang.String getName()
meth public nu.validator.saxtree.NodeType getNodeType()
supr nu.validator.saxtree.Node
hfds name

CLSS public nu.validator.saxtree.TreeBuilder
cons public init()
cons public init(boolean,boolean)
intf org.xml.sax.ContentHandler
intf org.xml.sax.ext.LexicalHandler
meth public nu.validator.saxtree.ParentNode getRoot()
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void comment(char[],int,int) throws org.xml.sax.SAXException
meth public void endCDATA() throws org.xml.sax.SAXException
meth public void endDTD() throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startCDATA() throws org.xml.sax.SAXException
meth public void startDTD(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
supr java.lang.Object
hfds current,locator,prefixMappings,retainAttributes

CLSS public final nu.validator.saxtree.TreeParser
cons public init(org.xml.sax.ContentHandler,org.xml.sax.ext.LexicalHandler)
intf org.xml.sax.Locator
meth public int getColumnNumber()
meth public int getLineNumber()
meth public java.lang.String getPublicId()
meth public java.lang.String getSystemId()
meth public void parse(nu.validator.saxtree.Node) throws org.xml.sax.SAXException
supr java.lang.Object
hfds contentHandler,lexicalHandler,locatorDelegate

CLSS public abstract interface org.xml.sax.Attributes
meth public abstract int getIndex(java.lang.String)
meth public abstract int getIndex(java.lang.String,java.lang.String)
meth public abstract int getLength()
meth public abstract java.lang.String getLocalName(int)
meth public abstract java.lang.String getQName(int)
meth public abstract java.lang.String getType(int)
meth public abstract java.lang.String getType(java.lang.String)
meth public abstract java.lang.String getType(java.lang.String,java.lang.String)
meth public abstract java.lang.String getURI(int)
meth public abstract java.lang.String getValue(int)
meth public abstract java.lang.String getValue(java.lang.String)
meth public abstract java.lang.String getValue(java.lang.String,java.lang.String)

CLSS public abstract interface org.xml.sax.ContentHandler
meth public abstract void characters(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void endDocument() throws org.xml.sax.SAXException
meth public abstract void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void setDocumentLocator(org.xml.sax.Locator)
meth public abstract void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void startDocument() throws org.xml.sax.SAXException
meth public abstract void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public abstract void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.Locator
meth public abstract int getColumnNumber()
meth public abstract int getLineNumber()
meth public abstract java.lang.String getPublicId()
meth public abstract java.lang.String getSystemId()

CLSS public abstract interface org.xml.sax.XMLReader
meth public abstract boolean getFeature(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public abstract java.lang.Object getProperty(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public abstract org.xml.sax.ContentHandler getContentHandler()
meth public abstract org.xml.sax.DTDHandler getDTDHandler()
meth public abstract org.xml.sax.EntityResolver getEntityResolver()
meth public abstract org.xml.sax.ErrorHandler getErrorHandler()
meth public abstract void parse(java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public abstract void parse(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public abstract void setContentHandler(org.xml.sax.ContentHandler)
meth public abstract void setDTDHandler(org.xml.sax.DTDHandler)
meth public abstract void setEntityResolver(org.xml.sax.EntityResolver)
meth public abstract void setErrorHandler(org.xml.sax.ErrorHandler)
meth public abstract void setFeature(java.lang.String,boolean) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public abstract void setProperty(java.lang.String,java.lang.Object) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException

CLSS public abstract interface org.xml.sax.ext.LexicalHandler
meth public abstract void comment(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void endCDATA() throws org.xml.sax.SAXException
meth public abstract void endDTD() throws org.xml.sax.SAXException
meth public abstract void endEntity(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void startCDATA() throws org.xml.sax.SAXException
meth public abstract void startDTD(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void startEntity(java.lang.String) throws org.xml.sax.SAXException

