/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.buildtools.compile;

import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.LauncherException;
import com.skcraft.launcher.LauncherUtils;
import com.skcraft.launcher.builder.PackageBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ModpackBuilder implements Callable<ModpackBuilder>, ProgressObservable {

    private final File inputDir;
    private final File outputDir;
    private final String version;
    private final String manifestFilename;
    private final boolean clean;

    public ModpackBuilder(File inputDir, File outputDir, String version, String manifestFilename, boolean clean) {
        this.inputDir = inputDir;
        this.outputDir = outputDir;
        this.version = version;
        this.manifestFilename = manifestFilename;
        this.clean = clean;
    }

    @Override
    public ModpackBuilder call() throws Exception {
        if (clean) {
            List<File> failures = new ArrayList<File>();

            try {
                LauncherUtils.interruptibleDelete(outputDir, failures);
            } catch (IOException e) {
                Thread.sleep(1000);
                LauncherUtils.interruptibleDelete(outputDir, failures);
            }

            if (failures.size() > 0) {
                throw new LauncherException(failures.size() + " failed to delete", "There were " + failures.size() + " failures during cleaning.");
            }
        }

        //noinspection ResultOfMethodCallIgnored
        outputDir.mkdirs();

        String[] args = {
                "--version", version,
                "--manifest-dest", new File(outputDir, manifestFilename).getAbsolutePath(),
                "-i", inputDir.getAbsolutePath(),
                "-o", outputDir.getAbsolutePath()
        };
        PackageBuilder.main(args);

        return this;
    }

    @Override
    public double getProgress() {
        return -1;
    }

    @Override
    public String getStatus() {
        return "Building modpack...";
    }
}
