/*
 * Copyright 2017 The Bazel Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.idea.blaze.base.command.buildresult;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.idea.blaze.base.model.primitives.Label;
import java.util.List;

/** Assists in getting build artifacts from a build operation. */
public interface BuildResultHelper extends AutoCloseable {

  /**
   * Returns the build flags necessary for the build result helper to work.
   *
   * <p>The user must add these flags to their build command.
   */
  List<String> getBuildFlags();

  /**
   * Returns the build result. May only be called once, after the build is complete, or no artifacts
   * will be returned.
   *
   * @return The build artifacts from the build operation.
   */
  ImmutableList<OutputArtifact> getBuildArtifacts() throws GetArtifactsException;

  /**
   * Returns the build artifacts, filtering out all artifacts not directly produced by the specified
   * target.
   *
   * <p>May only be called once, after the build is complete, or no artifacts will be returned.
   */
  ImmutableList<OutputArtifact> getBuildArtifactsForTarget(Label target)
      throws GetArtifactsException;

  /**
   * Returns all build artifacts belonging to the given output groups. May only be called once,
   * after the build is complete, or no artifacts will be returned.
   */
  default ImmutableList<OutputArtifact> getArtifactsForOutputGroup(String outputGroup)
      throws GetArtifactsException {
    return getPerOutputGroupArtifacts().get(outputGroup);
  }

  /**
   * Returns all build artifacts split by output group (note artifacts may belong to multiple output
   * groups). May only be called once, after the build is complete, or no artifacts will be
   * returned.
   */
  ImmutableListMultimap<String, OutputArtifact> getPerOutputGroupArtifacts()
      throws GetArtifactsException;

  @Override
  void close();

  /** Indicates a failure to get artifact information */
  class GetArtifactsException extends Exception {
    public GetArtifactsException(String message) {
      super(message);
    }
  }
}
