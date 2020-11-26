package qub;

public class QubCreateProjectRunParameters
{
    private final CharacterToByteWriteStream output;
    private final Folder projectFolder;
    private final Folder qubProjectDataFolder;

    private VerboseCharacterToByteWriteStream verbose;

    private QubCreateProjectRunParameters(CharacterToByteWriteStream output, Folder projectFolder, Folder qubProjectDataFolder)
    {
        PreCondition.assertNotNull(output, "output");
        PreCondition.assertNotNull(projectFolder, "projectFolder");
        PreCondition.assertNotNull(qubProjectDataFolder, "qubProjectDataFolder");

        this.output = output;
        this.projectFolder = projectFolder;
        this.qubProjectDataFolder = qubProjectDataFolder;

        this.setVerbose(VerboseCharacterToByteWriteStream.create(InMemoryCharacterToByteStream.create()).setIsVerbose(false));
    }

    public static QubCreateProjectRunParameters create(CharacterToByteWriteStream output, Folder projectFolder, Folder qubProjectDataFolder)
    {
        return new QubCreateProjectRunParameters(output, projectFolder, qubProjectDataFolder);
    }

    public CharacterToByteWriteStream getOutput()
    {
        return this.output;
    }

    public Folder getProjectFolder()
    {
        return this.projectFolder;
    }

    public Folder getQubProjectDataFolder()
    {
        return this.qubProjectDataFolder;
    }

    public VerboseCharacterToByteWriteStream getVerbose()
    {
        return this.verbose;
    }

    public QubCreateProjectRunParameters setVerbose(VerboseCharacterToByteWriteStream verbose)
    {
        this.verbose = verbose;
        return this;
    }
}
