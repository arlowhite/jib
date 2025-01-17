/*
 * Copyright 2020 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.tools.jib.cli.buildfile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.cloud.tools.jib.api.buildplan.AbsoluteUnixPath;
import com.google.common.collect.ImmutableList;
import java.nio.file.Paths;
import java.time.Instant;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link CopySpec}. */
public class CopySpecTest {

  private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

  @Test
  public void testCopySpec_full() throws JsonProcessingException {
    String data =
        "src: target/classes\n"
            + "dest: /app/classes\n"
            + "includes:\n"
            + "  - '**/*.in'\n"
            + "excludes:\n"
            + "  - '**/*.ex'\n"
            + "properties:\n" // only trivial test of file properties
            + "  timestamp: 1\n";

    CopySpec parsed = mapper.readValue(data, CopySpec.class);
    Assert.assertEquals(Paths.get("target/classes"), parsed.getSrc());
    Assert.assertEquals(AbsoluteUnixPath.get("/app/classes"), parsed.getDest());
    Assert.assertEquals(ImmutableList.of("**/*.in"), parsed.getIncludes());
    Assert.assertEquals(ImmutableList.of("**/*.ex"), parsed.getExcludes());
    Assert.assertEquals(Instant.ofEpochMilli(1), parsed.getProperties().get().getTimestamp().get());
  }

  @Test
  public void testCopySpec_srcRequired() {
    String data = "dest: /app/classes\n";

    try {
      mapper.readValue(data, CopySpec.class);
      Assert.fail();
    } catch (JsonProcessingException jpe) {
      MatcherAssert.assertThat(
          jpe.getMessage(), CoreMatchers.startsWith("Missing required creator property 'src'"));
    }
  }

  @Test
  public void testCopySpec_destRequired() {
    String data = "src: target/classes\n";

    try {
      mapper.readValue(data, CopySpec.class);
      Assert.fail();
    } catch (JsonProcessingException jpe) {
      MatcherAssert.assertThat(
          jpe.getMessage(), CoreMatchers.startsWith("Missing required creator property 'dest'"));
    }
  }

  @Test
  public void testCopySpec_srcNotNull() {
    String data = "src: null\n" + "dest: /app/classes\n";

    try {
      mapper.readValue(data, CopySpec.class);
      Assert.fail();
    } catch (JsonProcessingException jpe) {
      MatcherAssert.assertThat(
          jpe.getMessage(), CoreMatchers.containsString("Property 'src' cannot be null"));
    }
  }

  @Test
  public void testCopySpec_srcNotEmpty() {
    String data = "src: ''\n" + "dest: /app/classes\n";

    try {
      mapper.readValue(data, CopySpec.class);
      Assert.fail();
    } catch (JsonProcessingException jpe) {
      MatcherAssert.assertThat(
          jpe.getMessage(), CoreMatchers.containsString("Property 'src' cannot be empty"));
    }
  }

  @Test
  public void testCopySpec_destNotNull() {
    String data = "src: target/classes\n" + "dest: null\n";

    try {
      mapper.readValue(data, CopySpec.class);
      Assert.fail();
    } catch (JsonProcessingException jpe) {
      MatcherAssert.assertThat(
          jpe.getMessage(), CoreMatchers.containsString("Property 'dest' cannot be null"));
    }
  }

  @Test
  public void testCopySpec_destNotEmpty() {
    String data = "src: target/classes\n" + "dest: ''\n";

    try {
      mapper.readValue(data, CopySpec.class);
      Assert.fail();
    } catch (JsonProcessingException jpe) {
      MatcherAssert.assertThat(
          jpe.getMessage(), CoreMatchers.containsString("Property 'dest' cannot be empty"));
    }
  }

  @Test
  public void testCopySpec_nullCollections() throws JsonProcessingException {
    String data = "src: target/classes\n" + "dest: /app/classes\n";

    CopySpec parsed = mapper.readValue(data, CopySpec.class);
    Assert.assertEquals(ImmutableList.of(), parsed.getIncludes());
    Assert.assertEquals(ImmutableList.of(), parsed.getExcludes());
  }
}
