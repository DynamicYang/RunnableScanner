package org.dyang.runnablescanner;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

import java.util.List;

public interface ConfigScanner {
    /**
     */
    List<String> scan(Project project, ProgressIndicator indicator);
}
