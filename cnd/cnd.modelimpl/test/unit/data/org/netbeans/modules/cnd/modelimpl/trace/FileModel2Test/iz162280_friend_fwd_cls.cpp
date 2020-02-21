namespace boost {
    namespace python {
        struct pickle_suite;

        namespace detail {
            struct pickle_suite_registration;
        }

        struct pickle_suite {
        private:

            struct inaccessible {
            };
            friend struct detail::pickle_suite_registration;
        public:

            static inaccessible * getinitargs() {
                return 0;
            }

            static inaccessible * getstate() {
                return 0;
            }

            static inaccessible * setstate() {
                return 0;
            }

            static bool getstate_manages_dict() {
                return false;
            }
        };

        namespace detail {

            struct pickle_suite_registration {
                typedef pickle_suite::inaccessible inaccessible;
            };
        }
    }
}
