VERSION="1.0.3-SNAPSHOT"

CONTRIBS="jersey-apache-client jersey-atom-abdera jersey-multipart jersey-spring"

BASEDIR=`pwd`

processJavadocJar(){ # $1=jar $2=svn dir, both must be relative paths
 PREV_JAVADOC_DIR=$2
 JAVADOC_JAR=$1
 echo "Processing $JAVADOC_JAR and $PREV_JAVADOC_DIR"
 cd $BASEDIR
 if test -d $PREV_JAVADOC_DIR ; then
 # need to look for deleted files
   rm -rf tmp filesToDelete
   mkdir tmp
   cd tmp
   jar xvf $BASEDIR/$JAVADOC_JAR
   cd ..
   diff -u -r $PREV_JAVADOC_DIR tmp | grep -v .svn\$ | grep "^Only in $PREV_JAVADOC_DIR" \
       | sed -e "s|Only in $PREV_JAVADOC_DIR||" -e "s|: |/|" -e "s|^/|svn delete |" \
       > filesToDelete
   cd $PREV_JAVADOC_DIR
   jar xvf $BASEDIR/$JAVADOC_JAR
   svn add * --force
   cat $BASEDIR/filesToDelete | /bin/sh
 else
   mkdir -p $PREV_JAVADOC_DIR
   cd $PREV_JAVADOC_DIR
   jar xvf $JAVADOC_JAR
   cd $BASEDIR/jersey-apidocs
   svn add * --force
 fi
}


echo "Retrieving javadoc.jar for jersey version $VERSION"
wget -O javadoc.jar http://download.java.net/maven/2/com/sun/jersey/jersey-bundle/$VERSION/jersey-bundle-${VERSION}-javadoc.jar

for comp in $CONTRIBS ; do
  echo "Retrieving javadoc.jar for $comp version $VERSION"
  wget -O ${comp}-javadoc.jar http://download.java.net/maven/2/com/sun/jersey/contribs/${comp}/$VERSION/${comp}-${VERSION}-javadoc.jar
done

echo "Cleaning up the workspace..."
rm -rf jersey-apidocs

echo "Checkouting www/apidocs base from jersey svn..."
yes p | svn --username jerseyrobot --password cycling co https://jersey.dev.java.net/svn/jersey/trunk/www/apidocs jersey-apidocs 

processJavadocJar javadoc.jar jersey-apidocs/$VERSION/jersey
for comp in $CONTRIBS ; do
  processJavadocJar ${comp}-javadoc.jar jersey-apidocs/$VERSION/contribs/${comp}
done

cd $BASEDIR/jersey-apidocs
svn ci -m "automatical update of $VERSION apidocs"
