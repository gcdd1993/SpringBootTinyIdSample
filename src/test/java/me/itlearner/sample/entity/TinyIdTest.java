package me.itlearner.sample.entity;

import com.xiaoju.uemc.tinyid.client.utils.TinyId;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * TODO
 *
 * @author gaochen
 * @date 2019/9/20
 */
@Slf4j
public class TinyIdTest {

    @Test
    public void test1() {
        for (int i = 0; i < 1000; i++) {
            Long test = TinyId.nextId("user");
            log.info("next id : {}", test);
        }
    }
}
