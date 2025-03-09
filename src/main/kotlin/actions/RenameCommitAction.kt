package actions
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepositoryManager
import org.jetbrains.annotations.NotNull

class RenameCommitAction: AnAction() {
    override fun actionPerformed(@NotNull e: AnActionEvent) {
        // get the current project
        val project = e.project ?: run {
            Messages.showErrorDialog("No project found", "Error");
            return
        };

        // get the git repository manager
        val repoManager = GitRepositoryManager.getInstance(project);
        val repo = repoManager.repositories.firstOrNull() ?: run {
            Messages.showErrorDialog("No repository found", "Error");
            return;
        };

        // get the new commit message
        val newCommitMessage = Messages.showInputDialog(
            project,
            "Enter the new commit message",
            "Rename Commit",
            Messages.getQuestionIcon()
        ) ?: return;

        // check if the commit message is empty
        if (newCommitMessage.isEmpty()) {
            Messages.showErrorDialog("Commit message cannot be empty", "Error");
            return
        };

        // run the Git command in a background task
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Renaming commit", true) {
            override fun run(indicator: ProgressIndicator) {
                val git = Git.getInstance()
                try {
                    val handler = GitLineHandler(project, repo.root, GitCommand.COMMIT)
                    handler.addParameters("--amend", "-m", newCommitMessage)
                    git.runCommand(handler);
                } catch (e: Exception) {
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog(e.message, "Error")
                    }
                }
            }
        })
    };

    override fun update(@NotNull e: AnActionEvent) {
        // enable the action only if a project is open and a Git repository exists
        val project = e.project;
        if (project == null) {
            e.presentation.isEnabledAndVisible = false;
            return;
        };

        val repoManager = GitRepositoryManager.getInstance(project);
        val hasRepo = repoManager.repositories.isNotEmpty();
        e.presentation.isEnabledAndVisible = hasRepo;
    };

    // override the getActionUpdateThread method to run the action on the EDT
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT;
    }
}