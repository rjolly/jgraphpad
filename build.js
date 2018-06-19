mkdir("build");
mkdir("build/classes");

javac("src/application", "build/classes");

mkdir("dist");
jar("dist/jgraphpad.jar", "build/classes", ".*", "manifest.mf");
