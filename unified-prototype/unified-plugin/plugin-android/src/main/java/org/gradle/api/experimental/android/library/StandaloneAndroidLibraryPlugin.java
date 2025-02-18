package org.gradle.api.experimental.android.library;

import com.android.build.api.dsl.CommonExtension;
import com.android.build.api.dsl.LibraryExtension;
import com.android.build.api.variant.LibraryAndroidComponentsExtension;
import com.google.protobuf.gradle.ProtobufExtension;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyCollector;
import org.gradle.api.experimental.android.AbstractAndroidSoftwarePlugin;
import org.gradle.api.experimental.android.AndroidSoftware;
import org.gradle.api.experimental.android.nia.NiaSupport;
import org.gradle.api.internal.plugins.software.SoftwareType;
import org.jetbrains.kotlin.com.google.common.base.Preconditions;

import java.io.File;
import java.util.Objects;
import java.util.Set;

/**
 * Creates a declarative {@link AndroidLibrary} DSL model, applies the official Android plugin,
 * and links the declarative model to the official plugin.
 */
@SuppressWarnings("UnstableApiUsage")
public abstract class StandaloneAndroidLibraryPlugin extends AbstractAndroidSoftwarePlugin {
    @SoftwareType(name = "androidLibrary", modelPublicType=AndroidLibrary.class)
    public abstract AndroidLibrary getAndroidLibrary();

    @Override
    protected AndroidSoftware getAndroidSoftware() {
        return getAndroidLibrary();
    }

    @Override
    public void apply(Project project) {
        super.apply(project);

        AndroidLibrary dslModel = getAndroidLibrary();

        // Setup library-specific conventions
        dslModel.getProtobuf().getEnabled().convention(false);

        // Register an afterEvaluate listener before we apply the Android plugin to ensure we can
        // run actions before Android does.
        project.afterEvaluate(p -> linkDslModelToPlugin(p, dslModel));

        // Apply the official Android plugin and support for Kotlin
        project.getPlugins().apply("com.android.library");
        project.getPlugins().apply("org.jetbrains.kotlin.android");

        // After AGP creates configurations, link deps to the collectors
        linkCommonDependencies(dslModel.getDependencies(), project.getConfigurations());
    }

    /**
     * Performs linking actions that must occur within an afterEvaluate block.
     */
    private void linkDslModelToPlugin(Project project, AndroidLibrary dslModel) {
        LibraryExtension android = project.getExtensions().getByType(LibraryExtension.class);
        super.linkDslModelToPlugin(project, dslModel, android);

        configureProtobuf(project, dslModel, android);

        // TODO: All this configuration should be moved to the NiA project
        if (NiaSupport.isNiaProject(project)) {
            NiaSupport.configureNiaLibrary(project, dslModel);
        }

        ifPresent(dslModel.getConsumerProguardFile(), android.getDefaultConfig()::consumerProguardFile);
    }

    protected void configureProtobuf(Project project, AndroidLibrary dslModel, CommonExtension<?, ?, ?, ?, ?, ?> android) {
        if (dslModel.getProtobuf().getEnabled().get()) {
            project.getPlugins().apply("com.google.protobuf");

            String option = dslModel.getProtobuf().getOption().getOrNull();
            project.getLogger().info("Protobuf is enabled using option=" + option + " in: " + project.getPath());
            if (Objects.equals(option, "lite")) {
                dslModel.getDependencies().getApi().add("com.google.protobuf:protobuf-kotlin-lite:" + dslModel.getProtobuf().getVersion().get());

                ProtobufExtension protobuf = project.getExtensions().getByType(ProtobufExtension.class);
                String protocGAV = getProtocDepGAV(dslModel);
                protobuf.protoc(protoc -> protoc.setArtifact(protocGAV));

                protobuf.generateProtoTasks(generator -> {
                    generator.all().forEach(task -> {
                        task.getBuiltins().create("java", builtin -> builtin.getOptions().add("lite"));
                        task.getBuiltins().create("kotlin", builtin -> builtin.getOptions().add("lite"));
                    });
                });

                /*
                 * TODO: We don't want to rely on beforeVariants here, but how to do without hardcoding:
                 *  the NiA variants: "demoDebug, demoRelease, prodDebug, prodRelease"?
                 * This would seem to require some sort of ProductFlavor support (and maybe enumerated buildTypes?)
                 * which we don't want to add just yet.
                 */
                LibraryAndroidComponentsExtension androidComponents = project.getExtensions().getByType(LibraryAndroidComponentsExtension.class);
                File buildDir = project.getLayout().getBuildDirectory().get().getAsFile();
                androidComponents.beforeVariants(androidComponents.selector().all(), variant -> {
                    android.getSourceSets().register(variant.getName()).configure(sourceSet -> {
                        sourceSet.getJava().srcDir(new File(buildDir, dslModel.getProtobuf().getGeneratedRootDir().get() + "/" + variant.getName() + "/java"));
                        sourceSet.getKotlin().srcDir(new File(buildDir, dslModel.getProtobuf().getGeneratedRootDir().get() + "/" + variant.getName() + "/kotlin"));
                    });
                });
            } else {
                throw new IllegalStateException("We only support lite option currently, not: " + option);
            }
        }
    }

    private static String getProtocDepGAV(AndroidLibrary dslModel) {
        Set<Dependency> protocDeps = dslModel.getProtobuf().getDependencies().getProtoc().getDependencies().get();
        Preconditions.checkState(protocDeps.size() == 1, "Should have a single dependency, but had: " + protocDeps.size());
        Dependency protocDep = protocDeps.iterator().next();
        return protocDep.getGroup() + ":" + protocDep.getName() + ":" + protocDep.getVersion();
    }

    private File resolveSingleDependency(Project project, DependencyCollector dependencyCollector) {
        Configuration resolver = project.getConfigurations().detachedConfiguration();
        resolver.setCanBeResolved(true);
        resolver.fromDependencyCollector(dependencyCollector);

        Set<Dependency> deps = dependencyCollector.getDependencies().get();
        Preconditions.checkState(deps.size() == 1, "Should have a single dependency, but had: " + deps.size());

        Set<File> files = resolver.resolve();
        Preconditions.checkState(files.size() == 1, "Should have resolved a single file, but resolved: " + files.size());
        return files.iterator().next();
    }

    @SuppressWarnings("UnstableApiUsage")
    private void linkCommonDependencies(AndroidLibraryDependencies dependencies, ConfigurationContainer configurations) {
        super.linkCommonDependencies(dependencies, configurations);
        configurations.getByName("api").fromDependencyCollector(dependencies.getApi()); // API deps added for libraries
    }
}
