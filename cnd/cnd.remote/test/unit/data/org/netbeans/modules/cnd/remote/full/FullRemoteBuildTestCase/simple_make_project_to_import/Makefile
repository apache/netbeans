CGLAGS="-g"
LDGLAGS="-g"
CC=gcc

BUILD=build
BINARY=${BUILD}/simple-bin
OBJFILES=simple.o tolower.o toupper.o

all: ${BINARY}
	mkdir -p ${BUILD}
	${CC} ${LDGLAGS} ${OBJFILES} -o ${BINARY}

clean:
	rm -rf ${BINARY}
	rm -rf *.o

${BINARY}: ${OBJFILES}
	
%.o : %.c
	${CC} -c ${CFLAGS} -o $@ $<
