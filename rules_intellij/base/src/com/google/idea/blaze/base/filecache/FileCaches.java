/*
 * Copyright 2016 The Bazel Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.idea.blaze.base.filecache;

import com.google.idea.blaze.base.async.executor.BlazeExecutor;
import com.google.idea.blaze.base.model.BlazeProjectData;
import com.google.idea.blaze.base.projectview.ProjectViewSet;
import com.google.idea.blaze.base.scope.BlazeContext;
import com.google.idea.blaze.base.scope.Scope;
import com.google.idea.blaze.base.scope.output.StatusOutput;
import com.google.idea.blaze.base.scope.scopes.TimingScope;
import com.google.idea.blaze.base.sync.BlazeSyncParams;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;

/** Static helper methods to update file caches. */
public class FileCaches {
  /** Call on sync. Updates the file cache and deletes any old files. */
  public static void onSync(
      Project project,
      BlazeContext context,
      ProjectViewSet projectViewSet,
      BlazeProjectData blazeProjectData,
      BlazeSyncParams.SyncMode syncMode) {
    for (FileCache fileCache : FileCache.EP_NAME.getExtensions()) {
      Scope.push(
          context,
          childContext -> {
            childContext.push(new TimingScope(fileCache.getName()));
            childContext.output(new StatusOutput("Updating " + fileCache.getName() + "..."));
            fileCache.onSync(project, context, projectViewSet, blazeProjectData, syncMode);
          });
    }
    LocalFileSystem.getInstance().refresh(true);
  }

  /** Call at the end of build when you want the IDE to pick up any changes. */
  public static void refresh(Project project) {
    BlazeExecutor.submitTask(
        project,
        indicator -> {
          indicator.setIndeterminate(true);
          for (FileCache fileCache : FileCache.EP_NAME.getExtensions()) {
            indicator.setText("Updating " + fileCache.getName() + "...");
            fileCache.refreshFiles(project);
          }
          LocalFileSystem.getInstance().refresh(true);
        });
  }
}
