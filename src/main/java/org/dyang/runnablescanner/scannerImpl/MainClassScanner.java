package org.dyang.runnablescanner.scannerImpl;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import org.dyang.runnablescanner.ConfigScanner;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

/**
 * @description:
 * @author: xyyang
 * @create: 2024/12/23
 * @see:
 **/

public class MainClassScanner implements ConfigScanner {
    
     private static final Logger LOG = Logger.getInstance(MainClassScanner.class);
    
    public  List<String> scan(Project project, ProgressIndicator indicator) {
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
                        if (hasValidMainMethod(psiClass)) {
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
    
    private static boolean hasValidMainMethod(PsiClass psiClass) {
        PsiMethod[] methods = psiClass.findMethodsByName("main", false);
        for (PsiMethod method : methods) {
            if (isValidMainMethod(method)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isValidMainMethod(PsiMethod method) {
        return method.getModifierList().hasExplicitModifier("public")
                && method.getModifierList().hasExplicitModifier("static")
                && "void".equals(method.getReturnType().getPresentableText())
                && method.getParameterList().getParametersCount() == 1
                && "java.lang.String[]".equals(method.getParameterList().getParameters()[0].getType().getCanonicalText());
    }
}
