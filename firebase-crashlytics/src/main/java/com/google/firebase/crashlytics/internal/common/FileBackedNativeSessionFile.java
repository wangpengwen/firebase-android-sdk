// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.firebase.crashlytics.internal.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/** A {@link NativeSessionFile} backed by a {@link File} currently on disk. */
class FileBackedNativeSessionFile implements NativeSessionFile {

  private final File file;
  private final String dataTransportFilename;
  private final String reportsEndpointFilename;

  FileBackedNativeSessionFile(
      @NonNull String dataTransportFilename,
      @NonNull String reportsEndpointFilename,
      @NonNull File file) {
    this.dataTransportFilename = dataTransportFilename;
    this.reportsEndpointFilename = reportsEndpointFilename;
    this.file = file;
  }

  @Override
  @NonNull
  public String getReportsEndpointFilename() {
    return this.reportsEndpointFilename;
  }

  @Override
  @Nullable
  public InputStream getStream() {
    if (!file.exists() || !file.isFile()) {
      return null;
    }
    try {
      return new FileInputStream(file);
    } catch (FileNotFoundException f) {
      return null;
    }
  }

  @Override
  @Nullable
  public CrashlyticsReport.FilesPayload.File asFilePayload() {
    byte[] bytes = asBytes();
    return bytes != null
        ? CrashlyticsReport.FilesPayload.File.builder()
            .setContents(bytes)
            .setFilename(dataTransportFilename)
            .build()
        : null;
  }

  private byte[] asBytes() {
    final byte[] readBuffer = new byte[8192];
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (InputStream stream = getStream()) {
      if (stream == null) {
        return null;
      }
      int read;
      while ((read = stream.read(readBuffer)) > 0) {
        bos.write(readBuffer, 0, read);
      }
      return bos.toByteArray();
    } catch (IOException e) {
      return null;
    }
  }
}
