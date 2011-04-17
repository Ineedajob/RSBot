CC=javac
CFLAGS=
SRC=src
LIB=lib
RES=resources
BINDIR=bin
LSTF=temp.txt
IMGDIR=$(RES)/images
MANIFEST=$(RES)/Manifest.txt
VERSIONFILE=version.txt
VERSION=`cat $(RES)/$(VERSIONFILE)`
SCRIPTS=scripts
DIST=RSBot.jar

.PHONY: all Bot Scripts mostlyclean clean

all: Bot Scripts
	@rm -fv $(LSTF)
	@cp $(MANIFEST) $(LSTF)
	@echo "Specification-Version: \"$(VERSION)\"" >> $(LSTF)
	@echo "Implementation-Version: \"$(VERSION)\"" >> $(LSTF)
	@if [ -e $(DIST) ]; then rm -fv $(DIST); fi
	jar cfm $(DIST) $(LSTF) -C $(BINDIR) . $(SCRIPTS)/*.class $(IMGDIR)/* $(RES)/*.bat $(RES)/*.sh $(RES)/version.txt
	@rm -f $(LSTF)

Bot:
	@if [ ! -d $(BINDIR) ]; then mkdir $(BINDIR); fi
	$(CC) $(CFLAGS) -d $(BINDIR) `find $(SRC) -name *.java`

Scripts: mostlyclean
	$(CC) $(CFLAGS) -cp $(BINDIR) $(SCRIPTS)/*.java

mostlyclean:
	@rm -f $(SCRIPTS)/*.class

clean: mostlyclean
	@rm -fv $(DIST)
	@rm -rfv $(BINDIR)/*
