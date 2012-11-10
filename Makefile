
CLASSPATH=$(HOME)/minecraft/craftbukkit/craftbukkit-1.3.2-R1.0.jar:$(HOME)/minecraft/mod/rhino/rhino1_7R4/js-14.jar

all:
	javac -classpath $(CLASSPATH) -d target/classes src/com/elazary/jsRobot/*.java
	jar cvfm target/jsRobot.jar Manifest.txt plugin.yml config.yml -C target/classes .

install:
	cp target/jsRobot.jar ../../craftbukkit/plugins/
