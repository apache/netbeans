class X {
    def field

    def getField() {
        field += 1
    }
}

x = new X()
x.field = 1
println x.field
println x.@field
