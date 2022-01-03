
template <typename T> struct TemplateAndSpecialization_1 {
    int Param0;
};

template <> struct TemplateAndSpecialization_1<int> {
    int Param1;
};

class SpecializationDescendant_1 : public TemplateAndSpecialization_1<int>  {
    int foo() {
       return Param1;
    }
};

class SpecializationDescendant_2 : public TemplateAndSpecialization_1<char>  {
    int foo() {
       return Param0;
    }
};
