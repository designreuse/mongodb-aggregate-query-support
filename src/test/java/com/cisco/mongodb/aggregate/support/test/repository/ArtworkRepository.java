/*
 *  Copyright (c) 2017 the original author or authors.
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

package com.cisco.mongodb.aggregate.support.test.repository;

import com.cisco.mongodb.aggregate.support.annotation.Aggregate;
import com.cisco.mongodb.aggregate.support.annotation.Facet;
import com.cisco.mongodb.aggregate.support.test.beans.Artwork;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rkolliva
 * 2/19/17.
 */
public interface ArtworkRepository extends MongoRepository<Artwork, Integer> {

  @Aggregate(inputType = Artwork.class, genericType = true, outputBeanType = HashMap.class,
             facet = {
                 @Facet(query = "{" +
                                "      \"categorizedByTags\": [\n" +
                                "        { $unwind: \"$tags\" },\n" +
                                "        { $sortByCount: \"$tags\" }\n" +
                                "      ],\n" +
                                "      \"categorizedByPrice\": [\n" +
                                "        { $match: { price: { $exists: 1 } } },\n" +
                                "        {\n" +
                                "          $bucket: {\n" +
                                "            groupBy: \"$price\",\n" +
                                "            boundaries: [  0, 150, 200, 300, 400 ],\n" +
                                "            default: \"Other\",\n" +
                                "            output: {\n" +
                                "              \"count\": { $sum: 1 },\n" +
                                "              \"titles\": { $push: \"$title\" }\n" +
                                "            }\n" +
                                "          }\n" +
                                "        }\n" +
                                "      ]" +
                                "}\n", order = 0)
             })
  Map<String, Object> getFacetResults();
}
