LZMA_SDK_VERSION = 4.63
LZMA_JIO_MAJOR = 0
LZMA_JIO_MINOR = 95
LZMA_JIO_VERSION = $(LZMA_JIO_MAJOR).$(LZMA_JIO_MINOR)

JAR = jar
JAR_FILE = lzma-$(LZMA_SDK_VERSION)-jio-$(LZMA_JIO_VERSION).jar
DIST_NAME = lzmajio-$(LZMA_JIO_VERSION)
DIST_FILE = $(DIST_NAME).tar.gz
PACKAGE = net/contrapunctus/lzma
AUX_FILES = $(PACKAGE)/Version.java

default:

all: build $(AUX_FILES)
	javac -d build $(shell find SevenZip -name '*.java') $(PACKAGE)/*.java

build:
	-mkdir build

jar: $(JAR_FILE)

$(JAR_FILE): all
	$(JAR) cf $@ CPL.html LGPL.txt -C build .

$(PACKAGE)/Version.java: Version.pl
	darcs changes $(REPODIR) --context \
	  | perl $< $(LZMA_JIO_MAJOR) $(LZMA_JIO_MINOR) >$@

predist: $(AUX_FILES)

dist: $(DIST_FILE)

$(DIST_FILE):
	REPODIR=--repodir=$$PWD darcs dist --dist-name $(DIST_NAME)

public: $(JAR_FILE) $(DIST_FILE)
	scp $^ comsci.liu.edu:public_html/dist/lzmajio

clean:
	$(RM) -r build $(AUX_FILES)

reallyclean: clean
	$(RM) $(JAR_FILE) $(DIST_FILE)

.PHONY: default all build jar predist dist public clean reallyclean
