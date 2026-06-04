CC      := gcc
CFLAGS  := -g -O0 -Wall -Wextra -Wno-unused-parameter -fno-omit-frame-pointer
LDFLAGS := -ldl

SRC := bionic_loader.c
OBJ := $(SRC:.c=.o)
LIB := libnova_bionic_loader.so
TEST := bionic_dlopen_test

.PHONY: all test clean

all: $(LIB) $(TEST)

%.o: %.c
	$(CC) $(CFLAGS) -fPIC -c -o $@ $<

$(LIB): $(OBJ)
	$(CC) -shared -fPIC -o $@ $^ $(LDFLAGS)

$(TEST): tests/bionic_dlopen_test.c $(LIB)
	$(CC) $(CFLAGS) -I. -o $@ $< -L. -lnova_bionic_loader $(LDFLAGS) -Wl,-rpath,$(CURDIR)

test: $(TEST)
	./$(TEST) $(LIB_PATH)

clean:
	rm -f $(OBJ) $(LIB) $(TEST)
