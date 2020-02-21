class base {};
class sub1 : base{};
class sub2 : public base{};
class sub3 : private virtual base{};
class sub4 : sub3{};
class sub5 : sub2{};
