CC=javac
CFLAGS=
SRC=src
LIB=lib
RES=resources
BINDIR=bin
LSTF=temp.txt
IMGDIR=$(RES)/images
DATADIR=data
MANIFEST=$(RES)/Manifest.txt
VERSIONFILE=.version
VERSION=`cat $(VERSIONFILE)`
SCRIPTS=scripts
DIST=$(DATADIR)/RSBot.jar

.PHONY: all Bot Scripts mostlyclean clean

all: $(DATADIR) Bot Scripts
	@rm -f $(DIST)
	@cp $(MANIFEST) $(LSTF)
	@echo "Specification-Version: \"$(VERSION)\"" >> $(LSTF)
	@echo "Implementation-Version: \"$(VERSION)\"" >> $(LSTF)
	jar cfm $(DIST) $(LSTF) -C $(BINDIR) . $(SCRIPTS)/*.class $(IMGDIR)/*.png $(RES)/*.bat $(RES)/*.sh $(RES)/version.dat

Bot: $(BINDIR)
	$(CC) $(CFLAGS) -d $(BINDIR) `find $(SRC) -name *.java`

Scripts: mostlyclean
	$(CC) $(CFLAGS) -cp $(BINDIR) $(SCRIPTS)/*.java

mostlyclean:
	@rm -f $(SCRIPTS)/*.class

clean: mostlyclean
	@rm -fv $(DIST)
	@rm -rfv $(BINDIR)/*
	@rm -fv $(LSTF)

$(DATADIR):
	@mkdir $(DATADIR)
$(BINDIR):
	@mkdir $(BINDIR)