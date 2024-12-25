package org.dyang.runnablescanner;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RunConfigurationUpdater {
    
    public static void updateRunConfigurations(Project project, List<String> configurations) {
        RunManager runManager = RunManager.getInstance(project);
        

        ConfigurationType configurationType = findConfigurationType("Application");
        if (configurationType == null) {
            throw new IllegalStateException("ConfigurationType not found for 'Application'");
        }
        
        ConfigurationFactory factory = configurationType.getConfigurationFactories()[0];
        
        for (String qualifiedClassName : configurations) {
            boolean exists = runManager.getAllSettings().stream()
                    .anyMatch(setting -> setting.getName().equals(qualifiedClassName));
            if (!exists) {
                String[] location = qualifiedClassName.split("\\.");
                RunnerAndConfigurationSettings settings = runManager.createConfiguration(location[location.length -1], factory);
                
         
                configureApplication(settings, project, qualifiedClassName);
                
  
                runManager.addConfiguration(settings);
            }
        }
    }
    
    /**
     *
     */
    private static void configureApplication(RunnerAndConfigurationSettings settings, Project project, String qualifiedClassName) {
 
        Module module = findModuleForClass(project, qualifiedClassName);
        if (module == null) {
            throw new IllegalStateException("Module not found for class: " + qualifiedClassName);
        }
        
    
        ApplicationConfiguration applicationConfiguration = (ApplicationConfiguration) settings.getConfiguration();
        applicationConfiguration.setModule(module);
        applicationConfiguration.setMainClassName(qualifiedClassName);
    }
    
    /**
     *
     */
    @Nullable
    private static Module findModuleForClass(Project project, String qualifiedClassName) {
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            GlobalSearchScope moduleScope = GlobalSearchScope.moduleScope(module);
            PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(qualifiedClassName, moduleScope);
            if (psiClass != null) {
                return module;
            }
        }
        return null;
    }
    
    /**
     *
     */
    private static ConfigurationType findConfigurationType(String id) {
        for (ConfigurationType type : ConfigurationType.CONFIGURATION_TYPE_EP.getExtensionList()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}