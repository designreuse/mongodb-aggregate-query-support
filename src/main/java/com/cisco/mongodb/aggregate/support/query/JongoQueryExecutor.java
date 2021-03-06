/*
 *  Copyright (c) 2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 *
 */
package com.cisco.mongodb.aggregate.support.query;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.jongo.Aggregate;
import org.jongo.Aggregate.ResultsIterator;
import org.jongo.Jongo;
import org.jongo.bson.Bson;
import org.jongo.bson.BsonDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.*;

/**
 * Created by rkolliva on 10/21/2015.
 * A query executor for test cases
 */
@SuppressWarnings("unchecked")
public class JongoQueryExecutor implements MongoQueryExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(JongoQueryExecutor.class);

  private final Jongo jongo;

  @Autowired
  public JongoQueryExecutor(Jongo jongo) {
    this.jongo = jongo;
  }

  @Override
  public Object executeQuery(QueryProvider queryProvider) throws MongoQueryException {
    if (queryProvider.isIterable()) {
      return doMultiQuery(queryProvider);
    }
    else {
      return doSingleQuery(queryProvider);
    }
  }

  private Object doMultiQuery(final QueryProvider queryProvider) throws MongoQueryException {
    Iterator<String> iterator = (Iterator) queryProvider;
    int i = 0;
    Aggregate aggregate = null;

    while (iterator.hasNext()) {
      String query = iterator.next();
      if (i++ == 0) {
        aggregate = jongo.getCollection(queryProvider.getCollectionName()).aggregate(query);
      }
      else {
        aggregate.and(query);
      }
    }

    Map<String, Object> resultMap = new HashMap<>();

    ResultsIterator resultsIterator = aggregate.as(resultMap.getClass());
    if (!resultsIterator.hasNext() || Void.TYPE.equals(queryProvider.getMethodReturnType())) {
      return null;
    }
    final String resultKey = queryProvider.getQueryResultKey();
    final List retval = new ArrayList();
    resultsIterator.forEachRemaining(o -> {
      LOGGER.debug("Got object {}", o.toString());
      Assert.isTrue(o instanceof Map);
      Map<String, Object> instanceMap = (Map) o;
      Object resultObject = null;
      Object valueObject = null;
      //If there is no result key, we should consider the whole return values map
      //If not consider only the value in result key
      if (resultKey.isEmpty()) {
        valueObject = instanceMap;
      }
      else {
        valueObject = instanceMap.get(resultKey);
      }
      //If the result is a map(by default map will be returned) we need to
      // deserialize. If not, then it is a generic type(Eg. String, Integer)
      // which does not need deserialization
      if (valueObject instanceof Map) {
        Map value = (Map) valueObject;
        DBObject dbObject = new BasicDBObject(value);
        Class outputClass = queryProvider.getOutputClass();
        if(outputClass == DBObject.class) {
          resultObject = dbObject;
        }
        else {
          BsonDocument bsonDocument = Bson.createDocument(dbObject);
          resultObject = jongo.getMapper().getUnmarshaller().unmarshall(bsonDocument, outputClass);
        }
      }
      else {
        resultObject = valueObject;
      }
      retval.add(resultObject);
    });
    if (queryProvider.returnCollection()) {
      return retval;
    }
    else if (!queryProvider.returnCollection() && retval.size() == 1) {
      return retval.get(0);
    }
    throw new MongoQueryException("Query is expecting a single object to be returned but the result set had multiple rows");
  }

  private Object doSingleQuery(QueryProvider queryProvider) {
    return null;
  }
}
