namespace bug254133 {
    enum LogLevel254133 : unsigned short __attribute__ ((visibility ("default"))) {
                                    LogLevel_Emergency	= 0,	// system is unusable
                                    LogLevel_Alert		= 1,	// action must be taken immediately
                                    LogLevel_Critical	= 2,	// critical conditions
                                    LogLevel_Error		= 3,	// error conditions
                                    LogLevel_Warning	= 4,	// warning conditions
                                    LogLevel_Notice		= 5,	// normal, but significant, condition
                                    LogLevel_Info		= 6,	// informational message
                                    LogLevel_Debug		= 7	// debug-level message
    };
}