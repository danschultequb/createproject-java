package qub;

public interface QubCreateProjectRunTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(QubCreateProjectRun.class, () ->
        {
            runner.testGroup("getParameters(QubProcess)", () ->
            {
                runner.test("with null process", (Test test) ->
                {
                    test.assertThrows(() -> QubCreateProjectRun.getParameters(null),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with " + Strings.escapeAndQuote("-?"), (Test test) ->
                {
                    try (final QubProcess process = QubProcess.create("-?"))
                    {
                        final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                        process.setOutputWriteStream(output);

                        final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                        process.setErrorWriteStream(error);

                        final QubCreateProjectRunParameters parameters = QubCreateProjectRun.getParameters(process);

                        test.assertNull(parameters);
                        test.assertEqual(
                            Iterable.create(
                                "Usage: qub-createproject run [[--projectFolder=]<project-folder-path>] [--help] [--verbose]",
                                "  Create a new Qub project.",
                                "  --projectFolder: The path to the project folder. The current folder will be used if this isn't defined.",
                                "  --help(?):       Show the help message for this application.",
                                "  --verbose(v):    Whether or not to show verbose logs."),
                            Strings.getLines(output.getText().await()));
                        test.assertEqual("", error.getText().await());
                        test.assertEqual(-1, process.getExitCode());
                    }
                });

                runner.test("with no arguments", (Test test) ->
                {
                    try (final QubProcess process = QubProcess.create())
                    {
                        final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                        process.setOutputWriteStream(output);

                        final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                        process.setErrorWriteStream(error);

                        final QubCreateProjectRunParameters parameters = QubCreateProjectRun.getParameters(process);
                        test.assertNotNull(parameters);

                        final Folder currentFolder = process.getCurrentFolder();
                        test.assertEqual(currentFolder, parameters.getProjectFolder());

                        final Folder qubProjectDataFolder = currentFolder.getFileSystem().getFolder("C:/qub/qub/test-java/data/").await();
                        test.assertEqual(qubProjectDataFolder, parameters.getQubProjectDataFolder());

                        final VerboseCharacterToByteWriteStream verbose = parameters.getVerbose();
                        test.assertNotNull(verbose);
                        test.assertFalse(verbose.isVerbose());

                        test.assertEqual("", output.getText().await());
                        test.assertEqual("", error.getText().await());
                        test.assertEqual(0, process.getExitCode());
                    }
                });

                runner.test("with " + Strings.escapeAndQuote("-verbose"), (Test test) ->
                {
                    try (final QubProcess process = QubProcess.create("-verbose"))
                    {
                        final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                        process.setOutputWriteStream(output);

                        final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                        process.setErrorWriteStream(error);

                        final QubCreateProjectRunParameters parameters = QubCreateProjectRun.getParameters(process);
                        test.assertNotNull(parameters);

                        final Folder currentFolder = process.getCurrentFolder();
                        test.assertEqual(currentFolder, parameters.getProjectFolder());

                        final Folder qubProjectDataFolder = currentFolder.getFileSystem().getFolder("C:/qub/qub/test-java/data/").await();
                        test.assertEqual(qubProjectDataFolder, parameters.getQubProjectDataFolder());

                        final VerboseCharacterToByteWriteStream verbose = parameters.getVerbose();
                        test.assertNotNull(verbose);
                        test.assertTrue(verbose.isVerbose());

                        test.assertEqual("", output.getText().await());
                        test.assertEqual("", error.getText().await());
                        test.assertEqual(0, process.getExitCode());
                    }
                });

                runner.test("with " + Strings.escapeAndQuote("C:/project/folder/"), (Test test) ->
                {
                    try (final QubProcess process = QubProcess.create("C:/project/folder/"))
                    {
                        final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                        process.setOutputWriteStream(output);

                        final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                        process.setErrorWriteStream(error);

                        final QubCreateProjectRunParameters parameters = QubCreateProjectRun.getParameters(process);
                        test.assertNotNull(parameters);

                        final FileSystem fileSystem = process.getFileSystem();
                        test.assertEqual(fileSystem.getFolder("C:/project/folder/").await(), parameters.getProjectFolder());

                        final Folder qubProjectDataFolder = fileSystem.getFolder("C:/qub/qub/test-java/data/").await();
                        test.assertEqual(qubProjectDataFolder, parameters.getQubProjectDataFolder());

                        final VerboseCharacterToByteWriteStream verbose = parameters.getVerbose();
                        test.assertNotNull(verbose);
                        test.assertFalse(verbose.isVerbose());

                        test.assertEqual("", output.getText().await());
                        test.assertEqual("", error.getText().await());
                        test.assertEqual(0, process.getExitCode());
                    }
                });
            });

            runner.testGroup("run(QubCreateProjectRunParameters)", () ->
            {
                runner.test("with null parameters", (Test test) ->
                {
                    test.assertThrows(() -> QubCreateProjectRun.run(null),
                        new PreConditionFailure("parameters cannot be null."));
                });

                runner.test("with non-existing projectFolder", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final Folder qubProjectDataFolder = fileSystem.getFolder("/qub/project/data/").await();
                    final InMemoryCharacterToByteStream verbose = InMemoryCharacterToByteStream.create();
                    final QubCreateProjectRunParameters parameters = QubCreateProjectRunParameters.create(output, projectFolder, qubProjectDataFolder)
                        .setVerbose(VerboseCharacterToByteWriteStream.create(verbose));

                    final int exitCode = QubCreateProjectRun.run(parameters);

                    test.assertEqual(
                        Iterable.create(
                            "Creating Qub project in folder /project/folder/... Done."),
                        Strings.getLines(output.getText().await()));
                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Checking if project folder (/project/folder/) already exists... Done.",
                            "VERBOSE: Project folder (/project/folder/) doesn't exist. Creating it now... Done.",
                            "",
                            "VERBOSE: Creating project.json file (/project/folder/project.json)... Done.",
                            "VERBOSE: Creating README.md file (/project/folder/README.md)... Done.",
                            "VERBOSE: Creating LICENSE file (/project/folder/LICENSE)... Done.",
                            "VERBOSE: Creating .gitignore file (/project/folder/.gitignore)... Done.",
                            "VERBOSE: Creating sources folder (/project/folder/sources/)... Done.",
                            "VERBOSE: Creating tests folder (/project/folder/tests/)... Done."),
                        Strings.getLines(verbose.getText().await()));
                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Checking if project folder (/project/folder/) already exists... Done.",
                            "VERBOSE: Project folder (/project/folder/) doesn't exist. Creating it now... Done.",
                            "Creating Qub project in folder /project/folder/...",
                            "VERBOSE: Creating project.json file (/project/folder/project.json)... Done.",
                            "VERBOSE: Creating README.md file (/project/folder/README.md)... Done.",
                            "VERBOSE: Creating LICENSE file (/project/folder/LICENSE)... Done.",
                            "VERBOSE: Creating .gitignore file (/project/folder/.gitignore)... Done.",
                            "VERBOSE: Creating sources folder (/project/folder/sources/)... Done.",
                            "VERBOSE: Creating tests folder (/project/folder/tests/)... Done.",
                            " Done."),
                        Strings.getLines(qubProjectDataFolder.getFile("logs/1.log").await().getContentsAsString().await()));
                    test.assertEqual(0, exitCode);
                    test.assertEqual(
                        Iterable.create(
                            projectFolder.getFolder("sources").await(),
                            projectFolder.getFolder("tests").await(),
                            projectFolder.getFile(".gitignore").await(),
                            projectFolder.getFile("LICENSE").await(),
                            projectFolder.getFile("README.md").await(),
                            projectFolder.getFile("project.json").await()),
                        projectFolder.getFilesAndFoldersRecursively().await());
                });

                runner.test("with existing projectFolder", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder projectFolder = fileSystem.createFolder("/project/folder/").await();
                    final Folder qubProjectDataFolder = fileSystem.getFolder("/qub/project/data/").await();
                    final InMemoryCharacterToByteStream verbose = InMemoryCharacterToByteStream.create();
                    final QubCreateProjectRunParameters parameters = QubCreateProjectRunParameters.create(output, projectFolder, qubProjectDataFolder)
                        .setVerbose(VerboseCharacterToByteWriteStream.create(verbose));

                    final int exitCode = QubCreateProjectRun.run(parameters);

                    test.assertEqual(
                        Iterable.create(
                            "Creating Qub project in folder /project/folder/... Done."),
                        Strings.getLines(output.getText().await()));
                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Checking if project folder (/project/folder/) already exists... Done.",
                            "VERBOSE: Project folder (/project/folder/) already exists.",
                            "",
                            "VERBOSE: Creating project.json file (/project/folder/project.json)... Done.",
                            "VERBOSE: Creating README.md file (/project/folder/README.md)... Done.",
                            "VERBOSE: Creating LICENSE file (/project/folder/LICENSE)... Done.",
                            "VERBOSE: Creating .gitignore file (/project/folder/.gitignore)... Done.",
                            "VERBOSE: Creating sources folder (/project/folder/sources/)... Done.",
                            "VERBOSE: Creating tests folder (/project/folder/tests/)... Done."),
                        Strings.getLines(verbose.getText().await()));
                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Checking if project folder (/project/folder/) already exists... Done.",
                            "VERBOSE: Project folder (/project/folder/) already exists.",
                            "Creating Qub project in folder /project/folder/...",
                            "VERBOSE: Creating project.json file (/project/folder/project.json)... Done.",
                            "VERBOSE: Creating README.md file (/project/folder/README.md)... Done.",
                            "VERBOSE: Creating LICENSE file (/project/folder/LICENSE)... Done.",
                            "VERBOSE: Creating .gitignore file (/project/folder/.gitignore)... Done.",
                            "VERBOSE: Creating sources folder (/project/folder/sources/)... Done.",
                            "VERBOSE: Creating tests folder (/project/folder/tests/)... Done.",
                            " Done."),
                        Strings.getLines(qubProjectDataFolder.getFile("logs/1.log").await().getContentsAsString().await()));
                    test.assertEqual(0, exitCode);
                    test.assertEqual(
                        Iterable.create(
                            projectFolder.getFolder("sources").await(),
                            projectFolder.getFolder("tests").await(),
                            projectFolder.getFile(".gitignore").await(),
                            projectFolder.getFile("LICENSE").await(),
                            projectFolder.getFile("README.md").await(),
                            projectFolder.getFile("project.json").await()),
                        projectFolder.getFilesAndFoldersRecursively().await());
                });

                runner.test("with existing project.json file", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder projectFolder = fileSystem.createFolder("/project/folder/").await();
                    final File projectJsonFile = projectFolder.createFile("project.json").await();
                    final Folder qubProjectDataFolder = fileSystem.getFolder("/qub/project/data/").await();
                    final InMemoryCharacterToByteStream verbose = InMemoryCharacterToByteStream.create();
                    final QubCreateProjectRunParameters parameters = QubCreateProjectRunParameters.create(output, projectFolder, qubProjectDataFolder)
                        .setVerbose(VerboseCharacterToByteWriteStream.create(verbose));

                    final int exitCode = QubCreateProjectRun.run(parameters);

                    test.assertEqual(
                        Iterable.create(
                            "A Qub project already exists in folder /project/folder/."),
                        Strings.getLines(output.getText().await()));
                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Checking if project folder (/project/folder/) already exists... Done.",
                            "VERBOSE: Project folder (/project/folder/) already exists.",
                            "VERBOSE: project.json file (/project/folder/project.json) already exists."),
                        Strings.getLines(verbose.getText().await()));
                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Checking if project folder (/project/folder/) already exists... Done.",
                            "VERBOSE: Project folder (/project/folder/) already exists.",
                            "VERBOSE: project.json file (/project/folder/project.json) already exists.",
                            "A Qub project already exists in folder /project/folder/."),
                        Strings.getLines(qubProjectDataFolder.getFile("logs/1.log").await().getContentsAsString().await()));
                    test.assertEqual(-1, exitCode);
                    test.assertEqual(
                        Iterable.create(
                            projectJsonFile),
                        projectFolder.getFilesAndFoldersRecursively().await());
                });
            });
        });
    }
}
