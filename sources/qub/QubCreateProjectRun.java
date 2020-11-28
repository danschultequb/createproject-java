package qub;

/**
 * A QubProject action that can be used to create a new project.
 */
public interface QubCreateProjectRun
{
    String actionName = "run";
    String actionDescription = "Create a new Qub project.";

    static CommandLineParameter<Folder> addProjectFolderParameter(CommandLineParameters parameters, QubProcess process)
    {
        PreCondition.assertNotNull(parameters, "parameters");
        PreCondition.assertNotNull(process, "process");

        return parameters.addPositionalFolder("projectFolder", process)
            .setValueName("<project-folder-path>")
            .setDescription("The path to the project folder. The current folder will be used if this isn't defined.");
    }

    static QubCreateProjectRunParameters getParameters(QubProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        final CommandLineParameters parameters = process.createCommandLineParameters()
            .setApplicationName(QubCreateProject.getActionFullName(QubCreateProjectRun.actionName))
            .setApplicationDescription(QubCreateProjectRun.actionDescription);

        final CommandLineParameter<Folder> projectFolderParameter = QubCreateProjectRun.addProjectFolderParameter(parameters, process);
        final CommandLineParameterHelp helpParameter = parameters.addHelp();
        final CommandLineParameterVerbose verboseParameter = parameters.addVerbose(process);

        QubCreateProjectRunParameters result = null;
        if (!helpParameter.showApplicationHelpLines(process).await())
        {
            final CharacterToByteWriteStream output = process.getOutputWriteStream();
            final Folder projectFolder = projectFolderParameter.getValue().await();
            final Folder qubProjectDataFolder = process.getQubProjectDataFolder().await();
            final VerboseCharacterToByteWriteStream verbose = verboseParameter.getVerboseCharacterToByteWriteStream().await();

            result = QubCreateProjectRunParameters.create(output, projectFolder, qubProjectDataFolder)
                .setVerbose(verbose);
        }

        return result;
    }

    static int run(QubCreateProjectRunParameters parameters)
    {
        PreCondition.assertNotNull(parameters, "parameters");

        int result = 0;

        final Folder projectFolder = parameters.getProjectFolder();
        final Folder qubProjectDataFolder = parameters.getQubProjectDataFolder();
        final LogStreams logStreams = CommandLineLogsAction.addLogStream(qubProjectDataFolder, parameters.getOutput(), parameters.getVerbose());
        try (final Disposable logStream = logStreams.getLogStream())
        {
            final CharacterToByteWriteStream output = logStreams.getOutput();
            final VerboseCharacterToByteWriteStream verbose = logStreams.getVerbose();

            verbose.write("Checking if project folder (" + projectFolder + ") already exists...").await();
            final boolean projectFolderAlreadyExists = projectFolder.exists().await();
            verbose.writeLine(" Done.").await();
            if (projectFolderAlreadyExists)
            {
                verbose.writeLine("Project folder (" + projectFolder + ") already exists.").await();
            }
            else
            {
                verbose.write("Project folder (" + projectFolder + ") doesn't exist. Creating it now...").await();
                projectFolder.create().await();
                verbose.writeLine(" Done.");
            }

            final File projectJsonFile = projectFolder.getFile("project.json").await();
            if (projectJsonFile.exists().await())
            {
                verbose.writeLine("project.json file (" + projectJsonFile + ") already exists.").await();
                output.writeLine("A Qub project already exists in folder " + projectFolder + ".").await();
                result--;
            }
            else
            {
                output.write("Creating Qub project in folder " + projectFolder + "...").await();
                verbose.writeLine().await();

                final String project = projectFolder.getName();
                final String publisher = "qub";
                final String version = "1";

                verbose.write("Creating project.json file (" + projectJsonFile + ")...").await();
                projectJsonFile.setContentsAsString(
                    ProjectJSON.create()
                        .setProject(project)
                        .setPublisher(publisher)
                        .setVersion(version)
                        .setJava(ProjectJSONJava.create())
                        .toString(JSONFormat.pretty))
                    .await();
                verbose.writeLine(" Done.").await();

                final File readmeMdFile = projectFolder.getFile("README.md").await();
                if (readmeMdFile.exists().await())
                {
                    verbose.writeLine("README.md file (" + readmeMdFile + ") already exists.").await();
                }
                else
                {
                    verbose.write("Creating README.md file (" + readmeMdFile + ")...").await();
                    readmeMdFile.setContentsAsString("# " + ProjectSignature.create(publisher, project, version).toStringIgnoreVersion() + "\n").await();
                    verbose.writeLine(" Done.");
                }

                final File licenseFile = projectFolder.getFile("LICENSE").await();
                if (licenseFile.exists().await())
                {
                    verbose.writeLine("LICENSE file (" + licenseFile + ") already exists.").await();
                }
                else
                {
                    verbose.write("Creating LICENSE file (" + licenseFile + ")...").await();
                    licenseFile.setContentsAsString(
                        Strings.join(
                            '\n',
                            Iterable.create(
                                "MIT License",
                                "",
                                "Copyright (c) 2020 danschultequb",
                                "",
                                "Permission is hereby granted, free of charge, to any person obtaining a copy",
                                "of this software and associated documentation files (the \"Software\"), to deal",
                                "in the Software without restriction, including without limitation the rights",
                                "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell",
                                "copies of the Software, and to permit persons to whom the Software is",
                                "furnished to do so, subject to the following conditions:",
                                "",
                                "The above copyright notice and this permission notice shall be included in all",
                                "copies or substantial portions of the Software.",
                                "",
                                "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR",
                                "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,",
                                "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE",
                                "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER",
                                "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,",
                                "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE",
                                "SOFTWARE.")))
                        .await();
                    verbose.writeLine(" Done.");
                }

                final File gitIgnoreFile = projectFolder.getFile(".gitignore").await();
                if (gitIgnoreFile.exists().await())
                {
                    verbose.writeLine(".gitignore file (" + gitIgnoreFile + ") already exists.").await();
                }
                else
                {
                    verbose.write("Creating .gitignore file (" + gitIgnoreFile + ")...").await();
                    gitIgnoreFile.setContentsAsString(
                        Strings.join('\n', Iterable.create(
                            ".idea",
                            "out",
                            "outputs",
                            "target")))
                        .await();
                    verbose.writeLine(" Done.");
                }

                final Folder sourcesFolder = projectFolder.getFolder("sources").await();
                if (sourcesFolder.exists().await())
                {
                    verbose.writeLine("sources folder (" + sourcesFolder + ") already exists.").await();
                }
                else
                {
                    verbose.write("Creating sources folder (" + sourcesFolder + ")...").await();
                    sourcesFolder.create().catchError(FolderAlreadyExistsException.class).await();
                    verbose.writeLine(" Done.");
                }

                final Folder testsFolder = projectFolder.getFolder("tests").await();
                if (testsFolder.exists().await())
                {
                    verbose.writeLine("tests folder (" + testsFolder + ") already exists.").await();
                }
                else
                {
                    verbose.write("Creating tests folder (" + testsFolder + ")...").await();
                    testsFolder.create().catchError(FolderAlreadyExistsException.class).await();
                    verbose.writeLine(" Done.");
                }

                output.writeLine(" Done.").await();
            }

            return result;
        }
    }
}
