mkdir("build");
mkdir("build/classes");

javac("src", "build/classes");
copy("res", "build/classes");

mkdir("dist");
jar("dist/jgraphpad.jar", "build/classes", ".*", "manifest.mf");

publish("dist")
