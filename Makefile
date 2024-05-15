JAVAC=/usr/bin/javac
SRCDIR=src
BINDIR=bin

# Find all Java files in the source directory and its subdirectories
JAVA_FILES=$(shell find $(SRCDIR) -name '*.java')
CLASS_FILES=$(JAVA_FILES:$(SRCDIR)/%.java=$(BINDIR)/%.class)

.PHONY: default run clean

default: $(CLASS_FILES)

$(CLASS_FILES): $(JAVA_FILES)
	@mkdir -p $(BINDIR)
	$(JAVAC) -d $(BINDIR) $(JAVA_FILES)

clean:
	find $(BINDIR) -type f -name '*.class' -delete

run:
	java -cp $(BINDIR) barScheduling.SchedulingSimulation $(ARGS)