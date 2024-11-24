package one.terenin;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import one.terenin.service.GitService;
import one.terenin.util.LineValidator;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Plugin implements org.gradle.api.Plugin<Project> {

    @Override
    public void apply(Project target) {
        // Логика плагина
        target.getTasks().create("validate-new-code", ValidateNewCodeTask.class, task -> {
            // get properties from build.gradle
            Map<String, ?> properties = target.getProperties();
            task.setGitFolder(Optional.ofNullable(String.valueOf(properties.get("gitFolder"))).orElseThrow(() -> new GradleException("Git folder is required")));
            task.setTeamLabel(Optional.ofNullable(String.valueOf(properties.get("teamLabel"))).orElse("AGONA"));
            task.setMainBranchName(Optional.ofNullable(String.valueOf(properties.get("mainBranchName"))).orElse("main"));
            task.setWarnPhrases(Optional.ofNullable(String.valueOf(properties.get("warnPhrases"))).orElse("TODO,FIXME"));
        });
    }

    @Setter
    public static class ValidateNewCodeTask extends org.gradle.api.DefaultTask {

        private String gitFolder;
        private String mainBranchName;
        private String teamLabel;
        private String warnPhrases;

        @Input
        public String getGitFolder() {
            return gitFolder;
        }

        @Input
        public String getMainBranchName() {
            return mainBranchName;
        }

        @Input
        public String getTeamLabel() {
            return teamLabel;
        }

        @Input
        public String getWarnPhrases() {
            return warnPhrases;
        }

        @TaskAction
        public void validateNewCode() throws IOException {
            GitService service = new GitService(gitFolder);
            List<String> diffAsLines = service.getDiffAsLines(mainBranchName);
            Set<String> notValidLines = diffAsLines.stream().filter(it -> LineValidator.checkLine(it, teamLabel, warnPhrases))
                    .collect(Collectors.toSet());
            if (!notValidLines.isEmpty()) {
                String formattedSet = notValidLines.stream()
                        .collect(Collectors.joining(", ", "{ ", " }"));
                throw new GradleException("Build failed because you have new lines of code that are not valid, here they are: \n" + formattedSet);
            }
            getLogger().info("New code validated");
        }
    }
}
