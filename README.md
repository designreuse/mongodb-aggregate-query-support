[![Build Status](https://travis-ci.org/krraghavan/mongodb-aggregate-query-support.svg)](https://travis-ci.org/krraghavan/mongodb-aggregate-query-support) [![Release Version](https://img.shields.io/badge/version-v0.7.8-red.svg)](https://github.com/krraghavan/mongodb-aggregate-query-support) [![License](https://img.shields.io/hexpm/l/plug.svg)](https://img.shields.io/hexpm/l/plug.svg)

# MONGO DB AGGREGATE QUERY SUPPORT
This module provides annotated support for MongoDB aggregate queries much like the @Query annotation provided by the 
Spring Data module.

The @Query annotation provided by Spring Data MongoDb allows queries to be executed with minimum code being written.  
It is highly desirable to have a similar mechanism for MongoDB aggregate queries which allow us to execute sophisticated
queries with practically no code being written.


## Current Limitations
Not all aggregate pipeline steps are supported.  As of 0.7.2 support is provided for

* Project
* Unwind
* Lookup
* Group
* Match
* Out
* Limit
* Bucket (Mongo 3.4+) - added in v0.7.2
* AddFields (Mongo 3.4+) - added in v0.7.2
* ReplaceRoot (Mongo 3.4+) - added in v0.7.2
* Facet (Mongo 3.4+) - added in v0.7.3
* Count - added in v0.7.4
* Skip - added in v0.7.9

## New in 0.7.5 version
Ability to parameterize keys in aggregate queries.  When the structure of the query is the same but the only difference 
is in one or more keys, it makes for better maintenance to parameterize the aggregate queries and have one
parameterized method serve multiple queries.  See the unit test for an (admittedly contrived)
example of this usage.  To parameterize the keys (as opposed to values which are parameterized by ?<n> where n is the index
 of the parameter in the method), use the notation @n (i.e use @ instead of ? to denote a placeholder).  The n in both cases
 refers to the positional index of the parameter that provides the key name in the method.

Example: (from the unit test)

```

public interface PossessionsRepository extends MongoRepository<Possessions, Integer> {

@Aggregate(inputType = Possessions.class, outputBeanType = Possessions.class,
             match = {
                 @Match(query = "{" +
                                "   \"assets.@0\": { $exists: true, $ne : []}" +
                                "   }" +
                                "}", order = 0)
             })
  List<Possessions> getPossessions(String type);
  

  default boolean hasCars() {
    return CollectionUtils.isNotEmpty(getPossessions( "cars"));
  }
  
  default boolean hasHomes(String id) {
    return CollectionUtils.isNotEmpty(getPossessions( "homes"));
  }

}
```
## New in 0.7.6 version
In some use cases it is desirable to give control of the unmarshalling process to the clients of the Aggregate query.
To achieve this the ```outputClass``` attribute of the Aggregate annotation can be set to return the DBObject type.  This
package also provides an implementation of the ResultsExtractor interface that uses the Jongo unmarshaller to return the
desired class.  However, callers are free to implement this interface and unmarshall results as they see fit.
 
## New in 0.7.7 version
Support for parameterized JSON objects passed in as method arguments in the form of a String.  Useful for instance when
it is desired to have the sort pipeline stage be passed in as a method argument.  Without this feature, each sort combination
will require a different interface method to be defined.  To specify sort strings (see the unit test) call the string as 
follows:

```
String sortString = "{sortTestNumber:-1}";
       List<Possessions> possessions = possessionsRepository.getPossesionsSortedByTag(tag, sortString);
```
and the repository method 
```
@Aggregate(inputType = Possessions.class, outputBeanType = Possessions.class,
             match = {
                 @Match(query = "{" +
                                "   \"tag\": ?0" +
                                "}", order = 0)
             },
             sort = {
                 @Sort(query = "\"@@1\"", order = 1)
             })
  List<Possessions> getPossesionsSortedByTag(String tag, String sort);
``` 
Note the use of the extra escaped quotes and the use of the double @@ (both required).

## New in 0.7.8 version
This supports the use case where the query structure is largely the same with minor variations (say in a match stage) based
on different conditions.  In order to avoid needing to have multiple copies of queries that vary only in the definition of
one or more stages.  A new annotation and attribute ```@Conditional``` is defined that can be associated with each pipeline 
 stage (see the unit test for more details).  One or more conditional annotations can be specified and if any one of the 
 conditions match, the pipeline stage is used.  With this capability, the order attribute of each pipeline stage becomes relative.
   Stages that don't match and should not be included in the pipeline are filtered out when the pipeline is fully built.

```

  @Aggregate(inputType = Possessions.class, outputBeanType = Possessions.class,
             match = {
                 @Match(query = "{" +
                                "   \"tag\"  : ?0," +
                                "   \"assets.cars\" : { $exists: true, $ne : []}" +
                                "}", order = 0, condition = {
                     @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 1)
                 }),
                 @Match(query = "{" +
                                "   \"tag\": ?0," +
                                "   \"assets.homes\" : { $exists: true, $ne : []}" +
                                "}", order = 0, condition = {
                     @Conditional(condition = ParameterValueNotNullCondition.class, parameterIndex = 2)
                 })
             })
  List<Possessions> mutuallyExclusiveStages(String tag, Boolean getCars, Boolean getHomes);

```
In this case, when only one of the condition matches, only the matching @Match annotation is used in the pipeline.  If
both boolean flags are true, the second match criteria will overwrite the first match (a warning is emitted to the log).
The ```ParameterValueNotNullCondition``` class returns true if the parameter identified by the index is not null.

_It is the responsibility of the user to ensure that all the pipeline stages are correct after evaluating the conditions.  Using
distinct order values so that the relative order of the stages are all correct is necessary for this functionality to work correctly.
This functionality needs more work and should be used with care._


## Java version
Minimum Java version supported is 1.8 

## Usage
See the unit test classes and test repository definitions for examples of how to use the Aggregate query annotations.

The @Aggregate annotation is used identically to the way the Spring data @Query annotation is used.  You can put the annotation
on any interface method (with placeholders) to get Aggregate query support.  The easiest way to do this is to test the 
pipeline on a MongoDB client (like RoboMongo or MongoChef) and then copy the pipeline steps into each annotation.


# Contributors
* Kollivakkam Raghavan (owner)
* Sukhdev Singh
* Cameron Javier
