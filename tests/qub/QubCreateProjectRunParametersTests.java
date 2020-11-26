package qub;

public interface QubCreateProjectRunParametersTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(QubCreateProjectRunParameters.class, () ->
        {
            runner.testGroup("create(CharacterToByteWriteStream,Folder,Folder)", () ->
            {
                runner.test("with null output", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();

                    final CharacterToByteWriteStream output = null;
                    final Folder projectFolder = fileSystem.getFolder("/project/").await();
                    final Folder qubProjectDataFolder = fileSystem.getFolder("/qub/project/data/").await();

                    test.assertThrows(() -> QubCreateProjectRunParameters.create(output, projectFolder, qubProjectDataFolder),
                        new PreConditionFailure("output cannot be null."));
                });

                runner.test("with null projectFolder", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();

                    final CharacterToByteWriteStream output = InMemoryCharacterToByteStream.create();
                    final Folder projectFolder = null;
                    final Folder qubProjectDataFolder = fileSystem.getFolder("/qub/project/data/").await();

                    test.assertThrows(() -> QubCreateProjectRunParameters.create(output, projectFolder, qubProjectDataFolder),
                        new PreConditionFailure("projectFolder cannot be null."));
                });

                runner.test("with null qubProjectDataFolder", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();

                    final CharacterToByteWriteStream output = InMemoryCharacterToByteStream.create();
                    final Folder projectFolder = fileSystem.getFolder("/project/").await();
                    final Folder qubProjectDataFolder = null;

                    test.assertThrows(() -> QubCreateProjectRunParameters.create(output, projectFolder, qubProjectDataFolder),
                        new PreConditionFailure("qubProjectDataFolder cannot be null."));
                });

                runner.test("with valid arguments", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();

                    final CharacterToByteWriteStream output = InMemoryCharacterToByteStream.create();
                    final Folder projectFolder = fileSystem.getFolder("/project/").await();
                    final Folder qubProjectDataFolder = fileSystem.getFolder("/qub/project/data/").await();

                    final QubCreateProjectRunParameters parameters = QubCreateProjectRunParameters.create(output, projectFolder, qubProjectDataFolder);

                    test.assertNotNull(parameters);
                    test.assertSame(output, parameters.getOutput());
                    test.assertSame(projectFolder, parameters.getProjectFolder());
                    test.assertSame(qubProjectDataFolder, parameters.getQubProjectDataFolder());

                    final VerboseCharacterToByteWriteStream verbose = parameters.getVerbose();
                    test.assertNotNull(verbose);
                    test.assertFalse(verbose.isVerbose());
                });
            });
        });
    }
}
