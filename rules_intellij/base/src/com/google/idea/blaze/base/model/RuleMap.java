/*
 * Copyright 2016 The Bazel Authors. All rights reserved.
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
package com.google.idea.blaze.base.model;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.idea.blaze.base.ideinfo.RuleIdeInfo;
import com.google.idea.blaze.base.model.primitives.Label;
import java.io.Serializable;

/** Map of configured targets (and soon aspects). */
public class RuleMap implements Serializable {
  private static final long serialVersionUID = 1L;

  private final ImmutableMap<Label, RuleIdeInfo> ruleMap;

  public RuleMap(ImmutableMap<Label, RuleIdeInfo> ruleMap) {
    this.ruleMap = ruleMap;
  }

  public RuleIdeInfo get(Label label) {
    return ruleMap.get(label);
  }

  public boolean contains(Label label) {
    return ruleMap.containsKey(label);
  }

  public ImmutableCollection<RuleIdeInfo> rules() {
    return ruleMap.values();
  }

  public ImmutableMap<Label, RuleIdeInfo> map() {
    return ruleMap;
  }
}
