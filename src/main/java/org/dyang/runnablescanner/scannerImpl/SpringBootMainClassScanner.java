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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class SpringBootMainClassScanner implements ConfigScanner {
    
    private static final Logger LOG = Logger.getInstance(SpringBootMainClassScanner.class);
    
    private static final ExecutorService executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors());
    
    public List<String> scan(Project project, ProgressIndicator indicator) {
        List<String> mainClasses = new CopyOnWriteArrayList<>();
        Application application = ApplicationManager.getApplication();
        application.runReadAction(() -> {
            Module[] modules = ModuleManager.getInstance(project).getModules();
            List<Future<?>> futures = new ArrayList<>();
            for (Module module : modules) {
                if (indicator.isCanceled()) {
                    break;
                }
                futures.add(executorService.submit(
                        () -> application.runReadAction(() -> scanModule(module, project, mainClasses, indicator))));
            }
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    LOG.error("Error scanning module", e);
                }
            }
        });
        return mainClasses;
    }
    
    private static void scanModule(Module module, Project project, List<String> mainClasses,
            ProgressIndicator indicator) {
        PsiShortNamesCache cache = PsiShortNamesCache.getInstance(project);
        GlobalSearchScope moduleScope = GlobalSearchScope.moduleScope(module);
        String[] allClassNames = cache.getAllClassNames();
        for (String className : allClassNames) {
            if (indicator.isCanceled()) {
                return;
            }
            PsiClass[] classes = cache.getClassesByName(className, moduleScope);
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
    
    private static boolean hasSpringBootApplicationAnnotation(PsiClass psiClass) {
        PsiAnnotation[] annotations = psiClass.getAnnotations();
        Optional<PsiAnnotation> annotationOptional = Arrays.stream(annotations)
                .filter(psiAnnotation -> "org.springframework.boot.test.context.SpringBootTest".equals(
                        psiAnnotation.getQualifiedName())).findAny();
        if(annotationOptional.isPresent()){
            return false;
        }
        for (PsiAnnotation annotation : annotations) {
            
            if ("org.springframework.boot.autoconfigure.SpringBootApplication".equals(annotation.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }
}
