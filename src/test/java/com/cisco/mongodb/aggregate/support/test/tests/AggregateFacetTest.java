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

package com.cisco.mongodb.aggregate.support.test.tests;

import com.cisco.mongodb.aggregate.support.test.beans.Artwork;
import com.cisco.mongodb.aggregate.support.test.config.AggregateTestConfiguration;
import com.cisco.mongodb.aggregate.support.test.repository.ArtworkRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Created by rkolliva
 * 2/19/17.
 */

@SuppressWarnings("ConstantConditions")
@ContextConfiguration(classes = AggregateTestConfiguration.class)
public class AggregateFacetTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private ArtworkRepository artworkRepository;

  private static final String [] ARTWORK_DOCUMENTS = {
      "{ \"id\" : 1, \"title\" : \"The Pillars of Society\", \"artist\" : \"Grosz\", \"year\" : 1926,\n" +
      "  \"price\" : 199.99,\n" +
      "  \"tags\" : [ \"painting\", \"satire\", \"Expressionism\", \"caricature\" ] }\n",
      "{ \"id\" : 2, \"title\" : \"Melancholy III\", \"artist\" : \"Munch\", \"year\" : 1902,\n" +
      "  \"price\" : 280.00,\n" +
      "  \"tags\" : [ \"woodcut\", \"Expressionism\" ] }\n",
      "{ \"id\" : 3, \"title\" : \"Dancer\", \"artist\" : \"Miro\", \"year\" : 1925,\n" +
      "  \"price\" : 76.04,\n" +
      "  \"tags\" : [ \"oil\", \"Surrealism\", \"painting\" ] }\n",
      "{ \"id\" : 4, \"title\" : \"The Great Wave off Kanagawa\", \"artist\" : \"Hokusai\",\n" +
      "  \"price\" : 167.30,\n" +
      "  \"tags\" : [ \"woodblock\", \"ukiyo-e\" ] }\n",
      "{ \"id\" : 5, \"title\" : \"The Persistence of Memory\", \"artist\" : \"Dali\", \"year\" : 1931,\n" +
      "  \"price\" : 483.00,\n" +
      "  \"tags\" : [ \"Surrealism\", \"painting\", \"oil\" ] }\n",
      "{ \"id\" : 6, \"title\" : \"Composition VII\", \"artist\" : \"Kandinsky\", \"year\" : 1913,\n" +
      "  \"price\" : 385.00,\n" +
      "  \"tags\" : [ \"oil\", \"painting\", \"abstract\" ] }\n",
      "{ \"id\" : 7, \"title\" : \"The Scream\", \"artist\" : \"Munch\", \"year\" : 1893,\n" +
      "  \"tags\" : [ \"Expressionism\", \"painting\", \"oil\" ] }\n",
      "{ \"id\" : 8, \"title\" : \"Blue Flower\", \"artist\" : \"O'Keefe\", \"year\" : 1918,\n" +
      "  \"price\" : 118.42,\n" +
      "  \"tags\" : [ \"abstract\", \"painting\" ] }"
  };

  @BeforeClass
  public void setup() throws Exception {
    artworkRepository.deleteAll();
    ObjectMapper mapper = new ObjectMapper();
    List<Artwork> artworks = new ArrayList<>();
    Arrays.asList(ARTWORK_DOCUMENTS).forEach((s) -> {
      try {
        artworks.add(mapper.readValue(s, Artwork.class));
      }
      catch (IOException e) {
        assertTrue(false, e.getMessage());
      }
    });
    artworkRepository.insert(artworks);
  }

  /**
   * This test is disabled because it needs to be run on a real mongo instance
   * Fongo does not yet support the $facet operator.
   */
  @Test(enabled = false)
  public void mustReturnBucketsFromRepository() {
    assertNotNull(artworkRepository, "Must have a repository");
    List<Artwork> artworks = artworkRepository.findAll();
    assertNotNull(artworks);
    assertEquals(artworks.size(), ARTWORK_DOCUMENTS.length);
    Map<String, Object> facets = artworkRepository.getFacetResults();
    assertNotNull(facets);
    assertEquals(facets.size(), 2);
    assertTrue(facets.containsKey("categorizedByTags"));
    assertTrue(facets.containsKey("categorizedByPrice"));
  }


}
