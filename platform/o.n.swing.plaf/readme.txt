Provides basic colors, color support and custom ui delegates for standard swing
classes.  UI delegates for NetBeans specific components should live
elsewhere to avoid having this package depend on everything in the known
universe.

This library needs to be initialized early in the startup sequence to ensure
the ui defaults are set correctly before any UI components are created.