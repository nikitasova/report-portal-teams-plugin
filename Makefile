JAVA_21   := $(shell /usr/libexec/java_home -v 21 2>/dev/null)
JAVA_HOME := $(if $(JAVA_21),$(JAVA_21),$(JAVA_HOME))
GRADLE     = JAVA_HOME=$(JAVA_HOME) ./gradlew
JAR        = build/libs/plugin-teams-1.0.0.jar

.PHONY: build clean test jar check all compile deps

all: clean build test

build:
	$(GRADLE) shadowJar

jar: build

clean:
	$(GRADLE) clean

test:
	$(GRADLE) test

compile:
	$(GRADLE) compileJava

check: compile test

deps:
	$(GRADLE) dependencies --configuration compileClasspath
