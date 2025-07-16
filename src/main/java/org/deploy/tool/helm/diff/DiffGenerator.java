package org.deploy.tool.helm.diff;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DiffGenerator {

    public String generateDiff(String original, String revised, String originalLabel, String revisedLabel) {
        List<String> originalLines = Arrays.asList(original.split("\\r?\\n"));
        List<String> revisedLines = Arrays.asList(revised.split("\\r?\\n"));

        Patch<String> patch = DiffUtils.diff(originalLines, revisedLines);

        StringBuilder diff = new StringBuilder();
        diff.append("--- ").append(originalLabel).append("\n");
        diff.append("+++ ").append(revisedLabel).append("\n");

        for (AbstractDelta<String> delta : patch.getDeltas()) {
            diff.append(delta).append("\n");

            diff.append("原始内容:\n");
            for (String line : delta.getSource().getLines()) {
                diff.append("- ").append(line).append("\n");
            }

            diff.append("变更内容:\n");
            for (String line : delta.getTarget().getLines()) {
                diff.append("+ ").append(line).append("\n");
            }
        }

        return diff.toString();
    }
}