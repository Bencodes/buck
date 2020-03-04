/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.buck.jvm.java.toolchain;

import com.facebook.buck.core.toolchain.Toolchain;
import com.facebook.buck.core.util.immutables.BuckStyleValue;
import com.facebook.buck.jvm.java.JavacOptions;

@BuckStyleValue
public interface JavacOptionsProvider extends Toolchain {

  String DEFAULT_NAME = "javac-options";

  JavacOptions getJavacOptions();

  @Override
  default String getName() {
    return DEFAULT_NAME;
  }

  static JavacOptionsProvider of(JavacOptions javacOptions) {
    return ImmutableJavacOptionsProvider.ofImpl(javacOptions);
  }
}