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
package com.google.idea.blaze.android.run.runner;

import static java.util.stream.Collectors.joining;

import com.android.tools.idea.run.editor.AndroidDebugger;
import com.android.tools.idea.run.editor.AndroidDebuggerState;
import com.android.tools.idea.run.editor.AndroidJavaDebugger;
import com.android.tools.ndk.run.editor.AutoAndroidDebuggerState;
import com.google.common.collect.ImmutableList;
import com.google.idea.blaze.android.cppimpl.debug.BlazeAutoAndroidDebugger;
import com.google.idea.blaze.android.run.deployinfo.BlazeAndroidDeployInfo;
import com.google.idea.blaze.base.model.primitives.WorkspaceRoot;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Provides android debuggers and debugger states for blaze projects. */
public interface BlazeAndroidDebuggerService {

  static BlazeAndroidDebuggerService getInstance(Project project) {
    return ServiceManager.getService(project, BlazeAndroidDebuggerService.class);
  }

  /** Returns the standard debugger for non-native (Java) debugging. */
  @NotNull
  AndroidDebugger<AndroidDebuggerState> getDebugger();

  /** Returns the standard debugger for native (C++) debugging. */
  @NotNull
  AndroidDebugger<AutoAndroidDebuggerState> getNativeDebugger();

  /**
   * Performs additional necessary setup for native debugging, incorporating info from {@link
   * BlazeAndroidDeployInfo}.
   */
  void configureNativeDebugger(
      AutoAndroidDebuggerState state, @Nullable BlazeAndroidDeployInfo deployInfo);

  /** Default debugger service. */
  class DefaultDebuggerService implements BlazeAndroidDebuggerService {
    private final Project project;

    public DefaultDebuggerService(Project project) {
      this.project = project;
    }

    @Override
    public AndroidDebugger<AndroidDebuggerState> getDebugger() {
      return new AndroidJavaDebugger();
    }

    @Override
    public AndroidDebugger<AutoAndroidDebuggerState> getNativeDebugger() {
      return new BlazeAutoAndroidDebugger();
    }

    @Override
    public void configureNativeDebugger(
        AutoAndroidDebuggerState state, @Nullable BlazeAndroidDeployInfo deployInfo) {
      if (!isNdkPluginLoaded()) {
        return;
      }
      // Source code is always relative to the workspace root in a blaze project.
      String workingDirPath = WorkspaceRoot.fromProject(project).directory().getPath();
      state.setWorkingDir(workingDirPath);

      // Remote built binaries may use /proc/self/cwd to represent the working directory,
      // so we manually map /proc/self/cwd to the workspace root.  We used to use
      // `plugin.symbol-file.dwarf.comp-dir-symlink-paths = "/proc/self/cwd"`
      // to automatically resolve this, but it's no longer supported in newer versions of
      // LLDB.
      String sourceMapToWorkspaceRootCommand =
          "settings append target.source-map /proc/self/cwd/ " + workingDirPath;

      String symbolSearchPathsCommand = "";
      if (deployInfo != null && !deployInfo.getSymbolFiles().isEmpty()) {
        symbolSearchPathsCommand =
            "settings append target.exec-search-paths "
                + deployInfo.getSymbolFiles().stream()
                    .map(symbol -> symbol.getParentFile().getAbsolutePath())
                    .collect(joining(" "));
      }

      ImmutableList<String> startupCommands =
          ImmutableList.<String>builder()
              .addAll(state.getUserStartupCommands())
              .add(sourceMapToWorkspaceRootCommand)
              .add(symbolSearchPathsCommand)
              .build();
      state.setUserStartupCommands(startupCommands);
    }

    private static boolean isNdkPluginLoaded() {
      return PluginManagerCore.getLoadedPlugins().stream()
          .anyMatch(
              d -> d.isEnabled() && d.getPluginId().getIdString().equals("com.android.tools.ndk"));
    }
  }
}
