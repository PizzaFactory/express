versionBase=8.5.0
newVersion=${versionBase}.$(TZ=UTC date +M%Y%m%d-%H%M)
#!/bin/bash

set -eu

git flow release start $newVersion

mvn -f parent/pom.xml tycho-versions:set-version -DnewVersion=$newVersion
git add .
git commit -m 'Prepare for release.' 
git flow release finish -s $newVersion

mvn -f parent/pom.xml tycho-versions:set-version -DnewVersion=${versionBase}.qualifier
git add .
git commit -m 'Back to snapshots.'

git push --all
git push --tags
