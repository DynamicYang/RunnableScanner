package org.dyang.runnablescanner.scanner;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.dyang.runnablescanner.ConfigScanner;
import org.dyang.runnablescanner.RunConfigurationUpdater;
import org.dyang.runnablescanner.scannerImpl.SpringBootMainClassScanner;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * @description:
 * @author: xyyang
 * @create: 2024/12/23
 * @see:
 **/
public class ScannerInvokeAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(ScannerInvokeAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        System.out.println("My Tool Action triggered!");
        Project project = anActionEvent.getProject();
        if (project == null) return;

        if (ApplicationManager.getApplication().isReadAccessAllowed()) {
            ConfigScanner scanner = new SpringBootMainClassScanner();
            new Task.Backgroundable(project, "Updating run configurations") {
                @Override
                public void run(ProgressIndicator indicator) {
                    List<String> mainClasses = scanner.scan(project, indicator);
                    // 更新运行配置
                    ApplicationManager.getApplication().invokeLater(() -> {
                        RunConfigurationUpdater.updateRunConfigurations(project, mainClasses);
                        Messages.showInfoMessage(project, "Run Configurations Updated Successfully!", "Success");
                    });
                }
            }.queue();
        } else {
            Messages.showMessageDialog(project, "IDEA is currently indexing. Please try again later.", "Indexing in Progress", Messages.getInformationIcon());
        }

        System.out.println("done!");
    }
}
