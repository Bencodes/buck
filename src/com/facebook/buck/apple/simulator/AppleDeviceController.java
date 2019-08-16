/*
 * Copyright 2019-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.facebook.buck.apple.simulator;

import com.facebook.buck.core.util.log.Logger;
import com.facebook.buck.util.ProcessExecutor;
import com.facebook.buck.util.ProcessExecutorParams;
import com.facebook.buck.util.json.ObjectMappers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Utility class to control Apple Devices (both simulators and physical devices). Examples include
 * booting them, installing and launching bundles inside of the devices.
 */
public class AppleDeviceController {

  private static final Logger LOG = Logger.get(AppleDeviceController.class);
  private final ProcessExecutor processExecutor;
  private final Path idbPath;

  public AppleDeviceController(ProcessExecutor processExecutor, Path idbPath) {
    this.processExecutor = processExecutor;
    this.idbPath = idbPath;
  }

  /** @return the set of all Apple simulators and physical devices available */
  private ImmutableSet<ImmutableAppleDevice> getDevices() throws IOException, InterruptedException {
    ImmutableList<String> command = ImmutableList.of(idbPath.toString(), "list-targets", "--json");
    ProcessExecutorParams processExecutorParams =
        ProcessExecutorParams.builder().setCommand(command).build();
    Set<ProcessExecutor.Option> options = EnumSet.of(ProcessExecutor.Option.EXPECTING_STD_OUT);
    ProcessExecutor.Result result =
        processExecutor.launchAndExecute(
            processExecutorParams,
            options,
            /* stdin */ Optional.empty(),
            /* timeOutMs */ Optional.empty(),
            /* timeOutHandler */ Optional.empty());
    if (result.getExitCode() != 0) {
      LOG.error(result.getMessageForUnexpectedResult(command.toString()));
    }
    if (!result.getStdout().isPresent()) {
      LOG.error("No targets were found.");
    }

    // Treating the json returned by idb
    String[] targetsJson = result.getStdout().orElse("").split("\n");

    ImmutableSet.Builder<ImmutableAppleDevice> targets = ImmutableSet.builder();

    for (String json : targetsJson) {
      targets.add(
          ObjectMappers.READER.readValue(
              ObjectMappers.createParser(json.trim()), ImmutableAppleDevice.class));
    }
    return targets.build();
  }

  /** @return the set of Apple simulators */
  public ImmutableSet<ImmutableAppleDevice> getSimulators() {
    ImmutableSet<ImmutableAppleDevice> devices;
    ImmutableSet.Builder<ImmutableAppleDevice> simulators = ImmutableSet.builder();
    try {
      devices = getDevices();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
      devices = ImmutableSet.of();
    }

    for (ImmutableAppleDevice device : devices) {
      if (device.getType().equals("simulator")) simulators.add(device);
    }

    return simulators.build();
  }

  /** @return set of udids of the booted devices */
  public ImmutableSet<String> getBootedSimulatorsUdids() throws IOException, InterruptedException {
    ImmutableSet.Builder<String> bootedSimulatorUdids = ImmutableSet.builder();
    ImmutableSet<ImmutableAppleDevice> allTargets = getSimulators();

    for (ImmutableAppleDevice target : allTargets) {
      if (target.getState().equals("Booted") && target.getType().equals("simulator")) {
        bootedSimulatorUdids.add(target.getUdid());
      }
    }
    return bootedSimulatorUdids.build();
  }
}