class Klazz {

    def fieldA = new String("Hello")
    def fieldA1 = "Hello"
    def fieldB = fieldA.c
    def fieldB1 = fieldA1.c
    def fieldC = fieldA.concat("b").c
    def fieldC1 = fieldA1.concat("b").c
    def fieldD =  "hi"
    def fieldE = fieldA.concat(fieldD)
    def fieldE1 = fieldA1.concat(fieldD)
    def fieldF = fieldE.c
    def fieldF1 = fieldE1.c
    
    def m() {
    
        def localA = "Hello"
        localA.c
    
        localA.concat("b").c

        def localB = "hi"

        localA.concat(localB).c


        def localC = localA.concat(localB)

        localC.c

    }
    
}