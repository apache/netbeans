namespace One191610 {
    namespace Two191610 {

        struct Outer191610 {
        private:
            struct Hidden;

            Hidden* field;

        };

        struct Outer191610::Hidden {
            Hidden(const Hidden & clone);
        private:
            int field;
            Hidden* next;
        };

        Outer191610::Hidden::Hidden(const Hidden& clone) {
            this->field = clone.field;
        }
        
        class BooleanScorer191610 {
                private:
                    class Internal;
                    friend class Internal;
                    Internal* _internal;

                    class Coordinator;
                    void privateMethod() {}
        };

        
        class BooleanScorer191610::Coordinator {
        public:

            Coordinator(){
            }

            void initDoc() {
                nrMatchers = 0;
            }
            
        private:
            int nrMatchers;

        };

        class BooleanScorer191610::Internal {
        public:

            void method(BooleanScorer191610* p) {
                coord = new Coordinator();
                coord->initDoc();
                p->privateMethod();
            }
        private:
            Coordinator* coord;
        };
    }
}
