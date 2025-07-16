package org.deploy.tool.complete;

import org.junit.jupiter.api.Test;
import picocli.AutoComplete;

/**
 * @author chenJG
 * @description:
 * @since 2025/7/15 17:18
 */

public class AutoCompleteTest {


    @Test
    public void generateAutoCompleteUnixLike() {
        AutoComplete.main("-n=deploy-tool", "org.deploy.tool.complete.RootCommand");
    }

    @Test
    public void generateAutoCompleteWindows() {
        AutoComplete.main("-n=deploy-tool.exe", "org.deploy.tool.complete.RootCommand");
    }

}
