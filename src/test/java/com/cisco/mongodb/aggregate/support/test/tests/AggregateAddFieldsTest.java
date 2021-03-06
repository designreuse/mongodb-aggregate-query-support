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

import com.cisco.mongodb.aggregate.support.test.beans.ScoreResultsBean;
import com.cisco.mongodb.aggregate.support.test.config.AggregateTestConfiguration;
import com.cisco.mongodb.aggregate.support.test.repository.TestAddFieldsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Created by rkolliva
 * 1/24/17.
 */


@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@ContextConfiguration(classes = AggregateTestConfiguration.class)
public class AggregateAddFieldsTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private TestAddFieldsRepository testAddFieldsRepository;

  @Test(enabled = false)
  public void mustAddFieldsToResults() {
    assertNotNull(testAddFieldsRepository);

    List<ScoreResultsBean> resultsBeanList = testAddFieldsRepository.addFieldsToScore();
    assertNotNull(resultsBeanList);
    assertEquals(resultsBeanList.size(), 2);
    for (ScoreResultsBean scoreResultsBean : resultsBeanList) {
      int totalHw, totalScore, totalQuiz = 0;
      if(scoreResultsBean.getId() == 1) {
        totalHw = 25;
        totalQuiz = 18;
        totalScore = 43;
      }
      else {
        totalHw = 16;
        totalQuiz = 16;
        totalScore = 40;
      }
      assertEquals(scoreResultsBean.getTotalHomework(), totalHw);
      assertEquals(scoreResultsBean.getTotalQuiz(), totalQuiz);
      assertEquals(scoreResultsBean.getTotalScore(), totalScore);
    }
  }


}
