# just in case...
rm -rf test

# grizzly quickstart archetype
mvn archetype:generate -DarchetypeCatalog=http://download.java.net/maven/2 -DinteractiveMode=false -DarchetypeArtifactId=jersey-quickstart-grizzly -DarchetypeGroupId=com.sun.jersey.archetypes -DgroupId=example.com -DartifactId=test -Dpackage=com.example
cd test
mvn clean package
cd .. && rm -rf test

# WAR appl archetype
mvn archetype:generate -DarchetypeCatalog=http://download.java.net/maven/2 -DinteractiveMode=false -DarchetypeArtifactId=jersey-quickstart-webapp -DarchetypeGroupId=com.sun.jersey.archetypes -DgroupId=example.com -DartifactId=test -Dpackage=com.example
cd test
mvn clean package
cd .. && rm -rf test
