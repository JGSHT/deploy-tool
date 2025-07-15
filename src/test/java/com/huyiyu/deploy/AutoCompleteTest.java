package com.huyiyu.deploy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import picocli.AutoComplete;

/**
 * @author chenJG
 * @description:
 * @since 2025/7/15 17:18
 */
@SpringBootTest
public class AutoCompleteTest {


    @Test
    public void test(){
        AutoComplete.main(new String[]{"-n=deploy-tool","com.huyiyu.deploy.DeployToolApplication"});
    }
}
