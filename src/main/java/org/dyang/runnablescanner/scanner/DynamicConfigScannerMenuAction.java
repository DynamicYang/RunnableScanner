//package org.dyang.runnablescanner.scanner;
//
//import com.intellij.openapi.actionSystem.DefaultActionGroup;
//import com.intellij.openapi.actionSystem.AnAction;
//import com.intellij.openapi.actionSystem.AnActionEvent;
//import com.intellij.openapi.actionSystem.ActionManager;
//import com.intellij.openapi.application.ApplicationManager;
//import com.intellij.openapi.progress.ProgressIndicator;
//import com.intellij.openapi.progress.Task;
//import com.intellij.openapi.project.Project;
//import com.intellij.openapi.ui.Messages;
//import org.dyang.runnablescanner.ConfigScanner;
//import org.dyang.runnablescanner.RunConfigurationUpdater;
//import org.jetbrains.annotations.NotNull;
//
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.ServiceLoader;
//
//public class DynamicConfigScannerMenuAction extends AnAction {
//
//    @Override
//    public void actionPerformed(@NotNull AnActionEvent e) {
//        Project project = e.getProject();
//        if (project == null) {
//            return;
//        }
//
//
//        List<ConfigScanner> scanners = getConfigScanners();
//
//
//        DefaultActionGroup group = new DefaultActionGroup();
//
//        for (ConfigScanner scanner : scanners) {
//            String menuItemName = getMenuItemName(scanner);
//
//            AnAction menuItemAction = createMenuItem(menuItemName, scanner, project);
//            group.add(menuItemAction);
//        }
//
//
//        showSubMenu(group);
//    }
//
//    private List<ConfigScanner> getConfigScanners() {
//        List<ConfigScanner> scanners = new ArrayList<>();
//
//        ServiceLoader<ConfigScanner> loader = ServiceLoader.load(ConfigScanner.class);
//        loader.forEach(scanners::add);
//
//        return scanners;
//    }
//
//    private String getMenuItemName(ConfigScanner scanner) {
//
//        String className = scanner.getClass().getSimpleName();
//        return className.endsWith("Scanner") ? className.substring(0, className.length() - "Scanner".length()) : className;
//    }
//
//    private AnAction createMenuItem(String menuItemName, ConfigScanner scanner, Project project) {
//        return new AnAction(menuItemName) {
//            @Override
//            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
//                Project project = anActionEvent.getProject();
//                if (project == null) return;
//                getTemplatePresentation().setEnabled(false);
//                new Task.Backgroundable(project, "Updating Run Configurations") {
//                    @Override
//                    public void run(ProgressIndicator indicator) {
//                        List<String> mainClasses = scanner.scan(project,indicator);
//                        ApplicationManager.getApplication().invokeLater(() -> {
//                            RunConfigurationUpdater.updateRunConfigurations(project, mainClasses);
//                            Messages.showInfoMessage(project, "Run Configurations Updated Successfully!", "Success");
//                        });
//                    }
//                }.queue();
//
//            }
//        };
//    }
//
//    private void showSubMenu(DefaultActionGroup group) {
//        ActionManager actionManager = ActionManager.getInstance();
//        actionManager.registerAction("MyRunnableScanner", group);
//
//        actionManager.getAction("ToolsMenu").getTemplatePresentation().setVisible(true);
//    }
//}
