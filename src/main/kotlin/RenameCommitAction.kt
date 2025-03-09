
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
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
            Messages.showInputDialog("No project found", "Error", Messages.getErrorIcon());
            return
        };

        // get the git repository manager
        val repoManager = GitRepositoryManager.getInstance(project);
        val repo = repoManager.repositories.firstOrNull() ?: run {
            Messages.showInputDialog("No repository found", "Error", Messages.getErrorIcon());
            return;
        }

        // get the new commit message
        val newCommitMessage = Messages.showInputDialog(
            project,
            "Enter the new commit message",
            "Rename Commit",
            Messages.getQuestionIcon()
        )

        // check if the commit message is empty
        if (newCommitMessage.equals("")) {
            Messages.showInputDialog("Commit message cannot be empty", "Error", Messages.getErrorIcon());
            return
        }

        val git = Git.getInstance();
        try {
            val handler = GitLineHandler(project, repo.root, GitCommand.COMMIT)
            handler.addParameters("--amend", "-m", newCommitMessage)
            git.runCommand(handler);
            Messages.showInputDialog("Commit message changed", "Success", Messages.getInformationIcon());
        } catch (e: Exception) {
            Messages.showInputDialog(e.message, "Error", Messages.getErrorIcon());
        }
    }

    override fun update(@NotNull e: AnActionEvent) {
        // Enable the action only if a project is open and a Git repository exists
        val project = e.project;
        if (project == null) {
            e.presentation.isEnabledAndVisible = false;
            return;
        }

        val repoManager = GitRepositoryManager.getInstance(project);
        val hasRepo = repoManager.repositories.isNotEmpty();
        e.presentation.isEnabledAndVisible = hasRepo;
    }
}