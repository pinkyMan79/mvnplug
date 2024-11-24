package one.terenin.service;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class GitService {

    final Git git;
    // we can store at as final because plugin has no state
    final String currentBranch;

    public GitService(String gitDir) throws IOException {
        log.info("Open git dir: {}", gitDir);
        this.git = Git.open(new File(gitDir));
        this.currentBranch = git.getRepository().getBranch();
        log.info("Current branch is: {}", currentBranch);
    }

    public List<String> getDiffAsLines(String mainBranch) throws IOException {
        List<String> resultLines = new ArrayList<>();
        log.info("Try to get diff with main branch {}", mainBranch);
        Ref main = git.getRepository().findRef(mainBranch);
        Ref current = git.getRepository().findRef(currentBranch);
        try (RevWalk revWalk = new RevWalk(git.getRepository())) {
            RevCommit mainBaseCommit = revWalk.parseCommit(main.getObjectId());
            RevCommit currentBaseCommit = revWalk.parseCommit(current.getObjectId());

            RevTree mainRevisionTree = mainBaseCommit.getTree();
            RevTree currentRevisionTree = currentBaseCommit.getTree();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (DiffFormatter diffFormatter = new DiffFormatter(out)) {
                diffFormatter.setRepository(git.getRepository());
                List<DiffEntry> diffs = diffFormatter.scan(mainRevisionTree, currentRevisionTree);

                for (DiffEntry diff : diffs) {
                    diffFormatter.format(diff);
                    String diffLine = out.toString(StandardCharsets.UTF_8);
                    resultLines.add(diffLine);
                    if (log.isDebugEnabled()) log.debug(diffLine);
                    out.reset();
                }
            }
        }
        return resultLines;
    }
}
