package fr.radequin.copyassql;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import java.util.List;

/**
 * Base class for actions that copy SQL data as SQL clauses.
 * This class provides common constants used by the SQL clause formatters,
 * as well as utility methods for notifications and error handling.
 */
public abstract class CopyAsSqlAction extends AnAction {

    /** SQL AND operator with spaces */
    protected static final String AND = " AND ";

    /** SQL OR operator with spaces */
    protected static final String OR = " OR ";

    /** SQL WHERE keyword with spaces */
    protected static final String WHERE = " WHERE ";

    /** SQL IN keyword with spaces */
    protected static final String IN = " IN ";

    /** New line character for formatting */
    protected static final String NEW_LINE = "\n";

    /**
     * Shows a notification to the user.
     *
     * @param project The current project
     * @param title The title of the notification
     * @param content The content of the notification
     * @param type The type of notification (INFO, WARNING, ERROR)
     */
    protected void showNotification(Project project, String title, String content, NotificationType type) {
        NotificationGroup notificationGroup = NotificationGroupManager.getInstance()
                .getNotificationGroup("Copy as SQL");
        Notification notification = notificationGroup.createNotification(content, type);
        notification.setTitle(title);
        notification.notify(project);
    }

    /**
     * Shows a success notification when SQL is copied to clipboard.
     *
     * @param e The action event
     * @param clauseType The type of SQL clause that was copied
     */
    protected void showCopySuccessNotification(AnActionEvent e, String clauseType) {
        Project project = e.getProject();
        if (project != null) {
            showNotification(
                project,
                "Copy as SQL",
                clauseType + " copied to clipboard",
                NotificationType.INFORMATION
            );
        }
    }

    /**
     * Shows an error notification when an operation fails.
     *
     * @param e The action event
     * @param errorMessage The error message to display
     */
    protected void showErrorNotification(AnActionEvent e, String errorMessage) {
        Project project = e.getProject();
        if (project != null) {
            showNotification(
                project,
                "Copy as SQL Error",
                errorMessage,
                NotificationType.ERROR
            );
        }
    }
}
