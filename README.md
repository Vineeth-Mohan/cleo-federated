#cleo-federated

cleo-federated is a clone of cleo-primer with folowing features

### Grouping or federated result
   Groups can be mentioned while indexing and while querying you can mention that N number of results should come from each group.
### Automatic ID generation
    The user dont have to specify the id while indexing.
### Cross domain support
    The UI need not be on the same server / same port. JSONP is enabled.
### Jetty server support
### Automatated build creation support

## Test run
Run the following to bootstrap cleo in dev enviornment
MAVEN_OPTS="-Xms2g -Xmx2g" mvn jetty:run

Run the following to populate data of congress and company group

./scripts/post-element-list.sh congress dat/us-congress-representative-list.xml
./scripts/post-element-list.sh company dat/nasdaq-company-list.xml

Visit following link to see the auto complete feature in action

http://localhost:8080/cleo-primer/

