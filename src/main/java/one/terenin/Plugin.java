package one.terenin;

import lombok.SneakyThrows;
import one.terenin.service.GitService;
import one.terenin.util.LineValidator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mojo(name = "validate-new-code")
public class Plugin extends AbstractMojo {

    @Parameter(property = "gitFolder", defaultValue = "/dev/null")
    private String gitFolder;
    @Parameter(property = "mainBranchName", defaultValue = "main")
    private String mainBranchName;
    @Parameter(property = "teamLabel", defaultValue = "AGONA")
    private String teamLabel;
    @Parameter(property = "warnPhrases", defaultValue = "TODO,FIXME")
    private String warnPhrases;

    @SneakyThrows
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        GitService service = new GitService(gitFolder);
        List<String> diffAsLines = service.getDiffAsLines(mainBranchName);
        Set<String> notValidLines = diffAsLines.stream().filter(it -> LineValidator.checkLine(it, teamLabel, warnPhrases))
                .collect(Collectors.toSet());
        if (!notValidLines.isEmpty()) {
            String formattedSet = notValidLines.stream()
                    .collect(Collectors.joining(", ", "{ ", " }"));
            throw new MojoFailureException("Build failed because you have new lines of code that are not valid, here they are: \n" + formattedSet);
        }
        getLog().info("New lines of code is valid. Accepted.");
    }
}
