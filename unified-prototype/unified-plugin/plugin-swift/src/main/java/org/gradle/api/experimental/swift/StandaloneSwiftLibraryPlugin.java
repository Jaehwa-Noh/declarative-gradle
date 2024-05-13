package org.gradle.api.experimental.swift;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.plugins.software.SoftwareType;
import org.gradle.language.swift.plugins.SwiftLibraryPlugin;

public abstract class StandaloneSwiftLibraryPlugin implements Plugin<Project> {
    @SoftwareType(name = "swiftLibrary", modelPublicType = SwiftLibrary.class)
    abstract public SwiftLibrary getLibrary();

    @Override
    public void apply(Project project) {
        SwiftLibrary library = getLibrary();

        project.getPlugins().apply(SwiftLibraryPlugin.class);
    }
}
