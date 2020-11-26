package qub;

public interface QubCreateProject
{
    String applicationName = "qub-createproject";
    String applicationDescription = "Used to create new Qub projects.";

    static void main(String[] args)
    {
        QubProcess.run(args, QubCreateProject::run);
    }

    static void run(QubProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        final CommandLineActions<QubProcess> actions = process.<QubProcess>createCommandLineActions()
            .setApplicationName(QubCreateProject.applicationName)
            .setApplicationDescription(QubCreateProject.applicationDescription);

        actions.addAction(QubCreateProjectRun.actionName, QubCreateProjectRun::getParameters, QubCreateProjectRun::run)
            .setDescription(QubCreateProjectRun.actionDescription)
            .setDefaultAction();

        CommandLineLogsAction.addAction(actions);

        actions.run(process);
    }

    static String getActionFullName(String actionName)
    {
        PreCondition.assertNotNullAndNotEmpty(actionName, "actionName");

        return QubCreateProject.applicationName + " " + actionName;
    }
}