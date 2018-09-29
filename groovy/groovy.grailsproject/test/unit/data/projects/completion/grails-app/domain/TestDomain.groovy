class TestDomain {

    String name
    int age

    static constraints = {}

    def test1() {
        TestDomain.findBy
    }

    def test2() {
        TestDomain.findByAg
    }

    def test3() {
        TestDomain.findByAge
    }

    def test4() {
        TestDomain.findByAgeAnd
    }

    def test5() {
        TestDomain.findByAgeAndNa
    }

    def test6() {
        TestDomain.findByAgeAndName
    }

    def test7() {
        TestDomain.findR
    }

    def test8() {
        TestDomain.find
    }
}
