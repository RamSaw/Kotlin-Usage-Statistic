package com.mikhail.pravilov.mit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Class which describes tool window and gathers data for Kotlin usage statistic output.
 */
public class KotlinUsageStatistic implements ToolWindowFactory {
    private JPanel MainPanel;
    private JLabel sourceFilesLabel;
    private JLabel testFilesLabel;
    private JButton refreshButton;
    private JButton hideButton;
    private ToolWindow kotlinStatisticToolWindow;

    /**
     * Constructor sets two onclick listeners for hide and refresh buttons.
     */
    public KotlinUsageStatistic() {
        hideButton.addActionListener(e -> kotlinStatisticToolWindow.hide(null));
        refreshButton.addActionListener(e -> KotlinUsageStatistic.this.displayStatistic());
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        kotlinStatisticToolWindow = toolWindow;
        displayStatistic();
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(MainPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    /* TODO: Display statistic for each project window. Now if several are opened, window tool is active and shows data only for the last opened project. */
    /**
     * Shows gained statistic in tool window.
     */
    private void displayStatistic() {
        double[] statistic = getStatistic();
        sourceFilesLabel.setText("Kotlin percentage in all source files: " + statistic[0]);
        testFilesLabel.setText("Kotlin percentage in test source files: " + statistic[1]);
    }

    /**
     * Method to get statistics considering all files in the current project (last opened if several).
     * @return statistic stored in array of two doubles: first - percentage of Kotlin files in source files, second - in test source files.
     */
    private double[] getStatistic() {
        final int[] numberOfKotlinFiles = {0, 0};
        final int[] totalNumberOfFiles = {0, 0};
        double[] statistic = new double[2];

        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(getCurrentProject());
        projectRootManager.getFileIndex().iterateContent(virtualFile -> {
            if (!virtualFile.isDirectory()) {
                if (projectRootManager.getFileIndex().isInTestSourceContent(virtualFile)) {
                    numberOfKotlinFiles[1] += isKotlinFile(virtualFile) ? 1 : 0;
                    totalNumberOfFiles[1]++;
                }
                if (projectRootManager.getFileIndex().isInSourceContent(virtualFile)) {
                    numberOfKotlinFiles[0] += isKotlinFile(virtualFile) ? 1 : 0;
                    totalNumberOfFiles[0]++;
                }
            }
            return true;
        });

        statistic[0] = totalNumberOfFiles[0] == 0 ? 0 : (double) numberOfKotlinFiles[0] / totalNumberOfFiles[0] * 100;
        statistic[1] = totalNumberOfFiles[1] == 0 ? 0 : (double) numberOfKotlinFiles[1] / totalNumberOfFiles[1] * 100;
        return statistic;
    }

    /**
     * Returns instance of current opened project (last opened if several project are opened).
     * @return project instance - last in array instances.
     */
    private Project getCurrentProject() {
        /* Sometimes the line below returns null.
        DataContext dataContext = DataManager.getInstance().getDataContextFromFocus().getResult();
        return DataKeys.PROJECT.getData(dataContext);
        */
        return ProjectManager.getInstance().getOpenProjects()[ProjectManager.getInstance().getOpenProjects().length - 1];
    }

    /**
     * Checks whether given virtual file is Kotlin or not.
     * @param file to check.
     * @return true - if given file is Kotlin file, false - otherwise.
     */
    private boolean isKotlinFile(VirtualFile file) {
        return file.getFileType().getName().equals("Kotlin");
    }
}
