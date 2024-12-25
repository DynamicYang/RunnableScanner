package org.dyang.runnablescanner.scannerImpl;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import org.dyang.runnablescanner.ConfigScanner;

import java.util.ArrayList;

import java.util.List;


public class SpringBootMainClassScanner implements ConfigScanner {
    private static final Logger LOG = Logger.getInstance(SpringBootMainClassScanner.class);
    
    public List<String> scan(Project project, ProgressIndicator indicator) {
        List<String> mainClasses = new ArrayList<>();
        
        
        Application application = ApplicationManager.getApplication();
        application.runReadAction(() -> {
        
            Module[] modules = ModuleManager.getInstance(project).getModules();
            
            for (Module module : modules) {
                // 读取每个模块的类
                PsiShortNamesCache cache = PsiShortNamesCache.getInstance(project);
                String[] allClassNames = cache.getAllClassNames();
                
                for (String className : allClassNames) {
                    if (indicator.isCanceled()) {
                        return;
                    }
                    
                    PsiClass[] classes = cache.getClassesByName(className, GlobalSearchScope.moduleScope(module));
                    for (PsiClass psiClass : classes) {
                        if (hasSpringBootApplicationAnnotation(psiClass)) {
                            String qualifiedName = psiClass.getQualifiedName();
                            if (qualifiedName != null) {
                                mainClasses.add(qualifiedName);
                            }
                        }
                    }
                }
            }
        });
        
        return mainClasses;
    }
    
    /**
     *
     *
     * @param psiClass PsiClass 对象
     * @return
     */
    private static boolean hasSpringBootApplicationAnnotation(PsiClass psiClass) {
        PsiAnnotation[] annotations = psiClass.getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            if ("org.springframework.boot.autoconfigure.SpringBootApplication".equals(annotation.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }
}
