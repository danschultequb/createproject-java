package qub;

public interface QubCreateProjectTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(QubCreateProject.class, () ->
        {
            runner.testGroup("main(String[])", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> QubCreateProject.main(null),
                        new PreConditionFailure("args cannot be null."));
                });
            });

            runner.testGroup("run(QubProcess)", () ->
            {
                runner.test("with null process", (Test test) ->
                {
                    test.assertThrows(() -> QubCreateProject.run(null),
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

                        QubCreateProject.run(process);

                        test.assertEqual(
                            Iterable.create(
                                "Usage: qub-createproject [--action=]<action-name> [--help]",
                                "  Used to create new Qub projects.",
                                "  --action(a): The name of the action to invoke.",
                                "  --help(?):   Show the help message for this application.",
                                "",
                                "Actions:",
                                "  logs:          Show the logs folder.",
                                "  run (default): Create a new Qub project."),
                            Strings.getLines(output.getText().await()));
                        test.assertEqual("", error.getText().await());
                        test.assertEqual(-1, process.getExitCode());
                    }
                });
            });

            runner.testGroup("getActionFullName(String)", () ->
            {
                final Action2<String,Throwable> getActionFullNameErrorTest = (String actionName, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(actionName), (Test test) ->
                    {
                        test.assertThrows(() -> QubCreateProject.getActionFullName(actionName), expected);
                    });
                };

                getActionFullNameErrorTest.run(null, new PreConditionFailure("actionName cannot be null."));
                getActionFullNameErrorTest.run("", new PreConditionFailure("actionName cannot be empty."));

                final Action2<String,String> getActionFullNameTest = (String actionName, String expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(actionName), (Test test) ->
                    {
                        test.assertEqual(expected, QubCreateProject.getActionFullName(actionName));
                    });
                };

                getActionFullNameTest.run("a", "qub-createproject a");
                getActionFullNameTest.run("bananas", "qub-createproject bananas");
            });
        });
    }
}
